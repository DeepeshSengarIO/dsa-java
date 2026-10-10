# Wise System Design Interview Guide: Core Theory

This document covers the essential theoretical concepts needed to ace the Wise system design interview, tailored directly to Wise's engineering culture and their specific technical focus areas (fintech, high-volume transactions, distributed systems). 

---

## 1. The Wise Interview Format & Expectations

At Wise, you are evaluated as a **"Product Engineer"**. This means you shouldn't just design a system blindly; you need to think from the customer's perspective. 

- **The Task:** You will be given a product problem. You need to propose a high-level architecture and occasionally dive into low-level data structures or specific logic.
- **The Goal:** They are **not** looking for a perfect, bulletproof solution in 45 minutes. They are evaluating your reasoning, how you structure the problem, and how you articulate trade-offs.
- **The Process:** 
  1. Understand the question and ask clarifying questions.
  2. Start simple. Don't overengineer from minute one.
  3. Iterate and improve. 
  4. Talk out loud constantly. Explain *why* you chose a tool (e.g., PostgreSQL over MongoDB).
  5. Treat it collaboratively. If the interviewer steps in, it's to guide you, not punish you.

---

## 2. Distributed Systems & Microservices

Wise relies heavily on microservices to handle global scale independently. 

### Sync vs. Async Communication
- **Synchronous (REST / gRPC):**
  - *How it works:* Service A calls Service B and waits for the response.
  - *Pros:* Easy to reason about, immediate feedback to the client.
  - *Cons:* Tight coupling. If B is down, A fails (cascading failures). Slower for high-throughput.
  - *Use Case:* Fetching user profiles, requesting exchange rates.
- **Asynchronous (Message Queues / Event Streams like Kafka/RabbitMQ):**
  - *How it works:* Service A publishes an event to a broker. Service B consumes it at its own pace.
  - *Pros:* Loose coupling, high resilience, smooths out traffic spikes (buffering).
  - *Cons:* Introduces **Eventual Consistency** (the system isn't immediately synced globally) and complexity in tracing.
  - *Use Case:* Processing a money transfer, sending emails, fraud detection logging.

### Microservice Resilience
- **API Gateways:** Acts as the single entry point for clients, handling routing, rate limiting, and authentication.
- **Service Mesh:** Manages service-to-service communication, handling retries, encryption, and tracing automatically.

---

## 3. High-Volume Transaction Processing (The Core of Wise)

Because Wise moves real money, data consistency is strictly non-negotiable.

### Distributed Transactions
In a monolith, you use a single database transaction (ACID). In microservices, money leaves the `AccountService` and enters the `TransferService`. How do you guarantee both succeed or both fail?
- **Saga Pattern:** The standard for microservices. It breaks a distributed transaction into a sequence of local transactions.
  - *Choreography:* Services publish events to trigger the next step. (Harder to track).
  - *Orchestration:* A central "Coordinator" service tells other services what to do. (Easier to manage for complex money flows).
  - *Compensating Transactions:* If step 3 fails, you must automatically execute "undo" transactions for steps 1 and 2 (e.g., refunding the deducted money).

### Concurrency & Locking
When two transfers happen simultaneously for the same account, you prevent race conditions using locking:
- **Pessimistic Locking:** `SELECT * FROM accounts WHERE id = 1 FOR UPDATE;`. Locks the row. Very safe, but can cause bottlenecks and deadlocks if not ordered properly.
- **Optimistic Locking:** Adds a `version` column. Before updating, check if the version is the same as when you read it. Good for read-heavy, low-collision environments.

### Idempotency
If a client's network drops right after pressing "Send Money", they might press it again. 
- You must guarantee the API is **idempotent** (doing it twice has the same effect as doing it once).
- **Implementation:** The client generates a unique `Idempotency-Key` (UUID) and sends it in the header. The server checks a fast datastore (like Redis or a DB table):
  - If key exists: Return the cached successful response. Do NOT process the payment again.
  - If key doesn't exist: Process the payment and save the key.

---

## 4. Scalability, Availability, & Error Handling

### CAP Theorem
- **C**onsistency, **A**vailability, **P**artition Tolerance. You can only pick two.
- **Wise's Stance:** Financial ledgers *must* be CP (Consistent and Partition Tolerant). It is better for a transfer to be temporarily unavailable than for a balance to be wrong. 

### Error Handling & Fault Tolerance
- **Circuit Breaker:** If a downstream bank API is failing, the Circuit Breaker "opens" and immediately returns an error rather than waiting for timeouts, preventing your whole system from exhausting threads.
- **Dead Letter Queue (DLQ):** If a Kafka consumer fails to process a transfer message 5 times, it is moved to a DLQ for manual inspection by an engineer.
- **Retries with Exponential Backoff:** When a network call fails, retry after 1s, then 2s, then 4s, adding jitter to avoid thundering herds.

---

## 5. Databases & Data Storage

Choosing the right database and justifying it is the most critical part of a system design interview.

### Relational Databases (SQL - PostgreSQL / MySQL)
- **Why use it?** Strict schema validation, ACID compliance (Atomicity, Consistency, Isolation, Durability), and complex joins.
- **Wise Use Case:** Financial ledgers, user account balances, transactional history. (Money always goes in SQL).

### NoSQL Databases (Cassandra / DynamoDB / MongoDB)
- **Why use it?** Massively scalable, schema-less, highly available.
- **Wise Use Case:** Storing temporary user session data, KYC (Know Your Customer) document metadata, massive application activity logs.

### Caching (Redis / Memcached)
- **Why use it?** Extremely fast in-memory key-value store. Reduces load on the primary DB.
- **Wise Use Case:** Storing live currency exchange rates (which update every second), storing idempotency keys, rate limiting (Token Bucket).

---

## 6. Testing, Monitoring & Observability

You must explain how you know your system is working in production.
- **Metrics (Prometheus / Grafana):** Counting things. "How many transfers are failing per minute?" "What is the CPU usage of the fraud service?"
- **Distributed Tracing (Jaeger / OpenTelemetry):** Attaching a `trace_id` to a request when it hits the API Gateway and passing it along to every microservice. Allows you to see exactly which microservice caused a bottleneck in a slow request.
- **Centralized Logging (ELK Stack - Elasticsearch, Logstash, Kibana):** Aggregating all text logs into one searchable database. 
- **Testing Strategy:** Emphasize contract testing between microservices, integration testing with testcontainers (spinning up real DBs in Docker), and chaos engineering (randomly killing instances to ensure resilience).

---

## 7. A Blueprint for the 45-Minute Interview

When the interviewer gives you the prompt, follow this structure:

1. **Requirements Gathering (5 mins)**
   - Functional (What does it do? e.g., "Send money from UK to US")
   - Non-Functional (Scale? e.g., "10,000 transactions per second, High Availability, Strict Consistency")
2. **Core Entities & API Design (5-10 mins)**
   - Define the main database tables (e.g., `User`, `Account`, `Transaction`).
   - Define the main API endpoints (e.g., `POST /v1/transfers`).
3. **High-Level Design / Happy Path (10-15 mins)**
   - Draw the boxes: Client -> Load Balancer -> API Gateway -> Services -> Databases.
   - Explain the flow of a successful request.
4. **Deep Dive & Handling Failure (10-15 mins)**
   - *This is where you pass or fail.* 
   - "What if the database goes down?" -> (Replicas, failover).
   - "What if the downstream bank API times out?" -> (Async queues, circuit breakers, idempotency).
   - "How do we make it faster?" -> (Caching, Database Sharding/Partitioning).
5. **Review & Trade-offs (5 mins)**
   - Acknowledge the weaknesses in your design. "I used a synchronous call here for simplicity, but at extreme scale, we'd want Kafka."
