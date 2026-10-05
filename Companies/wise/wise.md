# Wise Fullstack Engineer Interview: The Ultimate Pair Programming & Coding Guide

This guide is heavily optimized for Wise (formerly TransferWise). Based on interview reports, Wise values **correctness, thread safety, data consistency, and fault tolerance** over tricky algorithms. They want to see how you write production-grade fintech code.

---

### 1. The Wise Product & Business Context

*   **The Scenario:** You are tasked with implementing the core of a `PaymentTransferService` and its React frontend. The service must deduct funds from a user's balance and execute the payout via a third-party Bank API. Because external bank APIs can be flaky, you must protect your system using a **Circuit Breaker** (a highly requested Wise interview topic), ensure database concurrency to prevent double-spending, and handle idempotency so retries don't double-charge.
*   **Customer Impact:** Wise indexes heavily on transparency and reliability. If a partner bank goes down, we shouldn't leave money in limbo or freeze the UI—we must fail fast (Circuit Breaker) and inform the customer immediately. We must guarantee absolute trust: a customer should *never* be charged twice for a failed network request.
*   **Common Pitfalls (Trap Doors):**
    1.  **Precision Loss:** Using `float` or `double` instead of `BigDecimal` for currency. (Instant red flag).
    2.  **Locking anti-patterns:** Holding a database transaction/lock (`@Transactional`) open while making a slow, synchronous network call to the 3rd-party bank. This exhausts database connection pools.
    3.  **Missing Idempotency:** Failing to handle network drops where the client sends the same request twice.
    4.  **Frontend Desync:** Not disabling the submit button or lacking an AbortController, allowing users to spam the API.

---

### 2. Pair Programming Blueprint (The Implementation)

We will separate the database transaction from the network call to avoid DB pool exhaustion.

#### Backend (Java / Spring Boot)

**1. The Circuit Breaker (Often asked to be implemented from scratch)**
Wise loves asking candidates to write a thread-safe Circuit Breaker.
```java
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * Custom Thread-Safe Circuit Breaker.
 * Blocks requests after `failureThreshold` is reached within a window.
 */
public class CircuitBreaker {
    private enum State { CLOSED, OPEN, HALF_OPEN }
    
    private final int failureThreshold;
    private final long resetTimeoutMs;
    private final AtomicInteger failureCount = new AtomicInteger(0);
    
    private volatile State state = State.CLOSED;
    private volatile long lastFailureTime = 0;

    public CircuitBreaker(int threshold, long timeoutMs) {
        this.failureThreshold = threshold;
        this.resetTimeoutMs = timeoutMs;
    }

    public <T> T execute(Supplier<T> action) {
        if (state == State.OPEN) {
            if (System.currentTimeMillis() - lastFailureTime > resetTimeoutMs) {
                // Time to test if the service is back up
                state = State.HALF_OPEN; 
            } else {
                throw new RuntimeException("Circuit is OPEN: Fast failing request.");
            }
        }

        try {
            T result = action.get();
            reset(); // Success! Reset everything.
            return result;
        } catch (Exception e) {
            recordFailure();
            throw e;
        }
    }

    private synchronized void recordFailure() {
        lastFailureTime = System.currentTimeMillis();
        // If it fails during HALF_OPEN, immediately trip it back to OPEN
        if (state == State.HALF_OPEN || failureCount.incrementAndGet() >= failureThreshold) {
            state = State.OPEN;
        }
    }

    private void reset() {
        failureCount.set(0);
        state = State.CLOSED;
    }
}
```

**2. The Service Layer (Concurrency & DB Logic)**
```java
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TransferService {
    
    private final AccountRepository accountRepo;
    private final TransferRepository transferRepo;
    private final BankClient bankClient; // The external API
    private final CircuitBreaker circuitBreaker = new CircuitBreaker(3, 300000); // 3 fails, 5 min cooldown

    /**
     * Step 1: Secure the funds locally. (Short DB transaction)
     */
    @Transactional
    public Transfer processLocalTransfer(UUID accountId, BigDecimal amount, String idempotencyKey) {
        // 1. Idempotency Check (DB constraint on idempotency_key column prevents duplicates)
        if (transferRepo.existsByIdempotencyKey(idempotencyKey)) {
            return transferRepo.findByIdempotencyKey(idempotencyKey); 
        }

        // 2. Pessimistic Write Lock to prevent race conditions from concurrent requests
        Account account = accountRepo.findByIdForUpdate(accountId)
            .orElseThrow(() -> new AccountNotFoundException());

        // 3. Exact math comparison
        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException();
        }

        // 4. Deduct and create PENDING transfer
        account.setBalance(account.getBalance().subtract(amount));
        accountRepo.save(account);

        Transfer transfer = new Transfer(accountId, amount, "PENDING", idempotencyKey);
        return transferRepo.save(transfer);
    }

    /**
     * Step 2: Execute the external call OUTSIDE the database transaction.
     */
    public void executeExternalPayout(Transfer transfer) {
        try {
            // Call external bank API wrapped in our custom Circuit Breaker
            String extTxId = circuitBreaker.execute(() -> bankClient.sendFunds(transfer));
            
            transfer.setStatus("COMPLETED");
            transfer.setExternalId(extTxId);
        } catch (Exception e) {
            // If the bank or circuit breaker fails, mark as FAILED.
            // A separate cron/job will refund PENDING/FAILED transfers.
            transfer.setStatus("FAILED");
        } finally {
            transferRepo.save(transfer);
        }
    }
}
```

#### Frontend (React / TypeScript)

We need a custom hook that guarantees we don't spam the API, generates idempotency keys, and handles loading states correctly.

```tsx
import { useState, useRef } from 'react';

// DTOs
interface TransferPayload {
    accountId: string;
    amount: number;
    currency: string;
}

export const useTransfer = () => {
    const [status, setStatus] = useState<'idle' | 'loading' | 'success' | 'error'>('idle');
    const [error, setError] = useState<string | null>(null);
    
    // Use an abort controller to cancel in-flight requests if component unmounts
    const abortControllerRef = useRef<AbortController | null>(null);

    const submitTransfer = async (payload: TransferPayload) => {
        // Prevent double submissions on the client side
        if (status === 'loading') return; 

        setStatus('loading');
        setError(null);
        
        abortControllerRef.current = new AbortController();
        
        // Generate a unique idempotency key for this specific retry lifecycle
        // If the network drops but the backend received it, retrying with the same key is safe.
        const idempotencyKey = crypto.randomUUID(); 

        try {
            const response = await fetch('/api/transfers', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Idempotency-Key': idempotencyKey,
                },
                body: JSON.stringify(payload),
                signal: abortControllerRef.current.signal,
            });

            if (!response.ok) {
                // If the Circuit Breaker is open, this might return a 503 Service Unavailable
                const errData = await response.json();
                throw new Error(errData.message || 'Transfer failed');
            }

            setStatus('success');
        } catch (err: any) {
            if (err.name === 'AbortError') {
                console.log('Request was aborted');
                return;
            }
            setError(err.message || 'Network error');
            setStatus('error');
        }
    };

    return { status, error, submitTransfer };
};

// Component Usage
export const TransferForm = () => {
    const { status, error, submitTransfer } = useTransfer();

    return (
        <div className="transfer-container">
            {error && <div className="error-banner">{error}</div>}
            
            <button 
                onClick={() => submitTransfer({ accountId: '123', amount: 100.00, currency: 'EUR' })}
                disabled={status === 'loading'}
                className={status === 'loading' ? 'spinner' : ''}
            >
                {status === 'loading' ? 'Processing...' : 'Send Money'}
            </button>
        </div>
    );
};
```

---

### 3. Concurrency, State, & Distributed Systems (The Fintech Core)

If you don't explain these concepts while coding, you will fail the round. 

*   **Race Conditions:** If two requests (e.g., from a mobile app and web browser) hit the API simultaneously to transfer $100 from an account with $150, a race condition occurs. Both threads read $150, both subtract $100, and both save $50. The user has magically spent $200 but has a $50 balance.
*   **Database Locking:** To solve the race condition, we use `SELECT ... FOR UPDATE` (Pessimistic Locking). In Spring Data JPA, this is done via `@Lock(LockModeType.PESSIMISTIC_WRITE)`. The first thread locks the database row; the second thread is forced to wait until the first transaction commits. By the time the second thread reads the row, the balance is $50, and it correctly throws `InsufficientFundsException`.
*   **The Idempotency Key:** What if the network drops exactly after the backend successfully commits the transfer, but before the HTTP 200 reaches the React frontend? The frontend thinks it failed. If the user clicks "Retry", we don't want to deduct *another* $100. By sending a generated `Idempotency-Key` (UUID), the backend queries the DB: "Have I seen this UUID?". If yes, it just returns the previous successful result instead of processing it again.
*   **Separation of Network and Transactions:** We explicitly separate the DB write (`processLocalTransfer`) from the 3rd-party API call (`executeExternalPayout`). If we kept the DB row locked while waiting 5 seconds for a slow bank API, our entire database would gridlock.

---

### 4. Testing Strategy (TDD Approach)

Wise interviewers expect you to drive the solution via tests or at least explicitly dictate how you would test it.

*   **Backend Concurrency Tests:**
    *   *The Multi-Thread Test:* Write a test using `ExecutorService` and `CountDownLatch`. Spawn 10 threads trying to deduct $10 from a $50 balance concurrently. Assert that exactly 5 threads succeed, 5 throw `InsufficientFundsException`, and the final balance is exactly $0.
    *   *The Circuit Breaker Test:* Mock the `BankClient` to throw `RuntimeException`. Loop 3 times to trigger the threshold. On the 4th call, assert that `CircuitBreakerOpenException` is thrown *without* the `BankClient` being invoked. Advance a mock clock by 5 minutes, call it again, and assert it transitions to `HALF_OPEN`.
*   **Frontend Tests (React Testing Library):**
    *   *Idempotency Test:* Mock `fetch`. Click the button, assert `fetch` was called with an `Idempotency-Key` header.
    *   *Disabled State:* Assert the "Send Money" button is strictly `toBeDisabled()` while the promise is pending.

---

### 5. The Wise Follow-Up Gauntlet

In the last 10–15 minutes, the interviewer will try to break your code. Here is exactly how to pivot.

#### Twist 1: "The Bank API only accepts batched transactions. How do we handle partial failures?"
*   **The Problem:** We send 100 transfers in one array. 99 succeed, 1 fails. If we throw an exception, we rollback and retry all 100, causing double payouts!
*   **The Pivot:** Never treat batched network calls as atomic.
*   **The Solution:** 
    1. The API should return a `207 Multi-Status` with a list of individual successes/failures.
    2. We update each `Transfer` row in our DB independently inside a `forEach` loop. 
    3. We must ensure the bank API itself supports idempotency per item in the batch.

#### Twist 2: "Instead of a Circuit Breaker, implement a Rate Limiter to protect our own API."
*   **The Pivot:** A Circuit Breaker protects *outbound* calls; a Rate Limiter protects *inbound* calls.
*   **The Solution:** Use the **Token Bucket Algorithm**.
    ```java
    // Quick mental outline to explain:
    long now = System.currentTimeMillis();
    long timePassed = now - lastRefillTime;
    tokens = Math.min(capacity, tokens + (timePassed * refillRate));
    if (tokens >= 1) {
        tokens--;
        lastRefillTime = now;
        return true; // Allow
    }
    return false; // HTTP 429 Too Many Requests
    ```

#### Twist 3: "If user A sends money to user B, how do you prevent Database Deadlocks?"
*   **The Problem:** Thread 1 transfers User A -> User B (locks A, then B). Thread 2 transfers User B -> User A (locks B, then A). They wait on each other forever.
*   **The Pivot:** Lock ordering.
*   **The Solution:** Always acquire database locks in a globally consistent order, regardless of who is sender or receiver. 
    ```java
    // Sort account IDs alphanumerically before locking
    UUID firstLock = A.compareTo(B) < 0 ? A : B;
    UUID secondLock = A.compareTo(B) < 0 ? B : A;
    accountRepo.findByIdForUpdate(firstLock);
    accountRepo.findByIdForUpdate(secondLock);
    ```

#### Twist 4: "We need the frontend to show a live-updating FX rate. How do you integrate that?"
*   **The Pivot:** `fetch` polling vs WebSockets vs Server-Sent Events (SSE).
*   **The Solution:** 
    Use **Server-Sent Events (SSE)**. We don't need bi-directional WebSocket communication because the client isn't sending FX rates back. SSE is natively supported via `EventSource` in React, lighter on the backend, and perfectly streams one-way pricing data to the UI to guarantee the customer sees the most accurate rate before clicking send.

