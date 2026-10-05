# Wise Interview Preparation Guide

This document covers all the interview topics mentioned, providing the problem description, input/output examples, underlying theory, key points, interview tricks, complete production-ready Java implementations, and follow-up answers on how to test these implementations.

---

## 1. Circuit Breaker Pattern

**Problem Description:**
Design a Web API client wrapper that acts as a Circuit Breaker. It should block requests to an underlying service after 3 consecutive failures within a 10-minute window to prevent cascading failures. It must automatically transition to a "Half-Open" state after a 5-minute recovery period to test if the service is back up.

**Input & Expected Output:**
- **Input:** A sequence of API execution tasks (`Callable<T>`) over time.
- **Output:** The result of the API call `T`, or a `RuntimeException` thrown directly by the Circuit Breaker if the circuit is OPEN.

**Theory:**
The Circuit Breaker pattern acts as a state machine:
1. **CLOSED**: Normal operation. If failures exceed a threshold in a time window, it trips to OPEN.
2. **OPEN**: Requests are blocked immediately (fail-fast). After a timeout, it transitions to HALF-OPEN.
3. **HALF-OPEN**: A limited number of requests pass through. If successful, it resets to CLOSED. If they fail, it goes back to OPEN.

**Key Points & Tricks to Remember:**
- **Concurrency is key**: Multiple threads will call the `execute` method simultaneously. State transitions and counters must be thread-safe.
- **Tip**: Use `AtomicInteger` for failure counters and `volatile` or `AtomicReference` for the state to ensure visibility across threads.
- **Tip**: Don't use a background thread for timers. Instead, calculate elapsed time lazily upon the next request.

**Java Implementation:**
```java
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class CircuitBreaker {

    public enum State { CLOSED, OPEN, HALF_OPEN }

    private final long failureWindowInMillis;
    private final int failureThreshold;
    private final long recoveryTimeoutInMillis;

    private final AtomicReference<State> state = new AtomicReference<>(State.CLOSED);
    private final AtomicInteger failureCount = new AtomicInteger(0);
    
    private volatile long windowStartTime;
    private volatile long lastFailureTime;

    public CircuitBreaker(int failureThreshold, long failureWindowInMillis, long recoveryTimeoutInMillis) {
        this.failureThreshold = failureThreshold;
        this.failureWindowInMillis = failureWindowInMillis;
        this.recoveryTimeoutInMillis = recoveryTimeoutInMillis;
        this.windowStartTime = System.currentTimeMillis();
    }

    public <T> T execute(Callable<T> task) throws Exception {
        evaluateState();

        if (state.get() == State.OPEN) {
            throw new RuntimeException("Circuit Breaker is OPEN. Request blocked.");
        }

        try {
            T result = task.call();
            onSuccess();
            return result;
        } catch (Exception e) {
            onFailure();
            throw e;
        }
    }

    private synchronized void evaluateState() {
        long now = System.currentTimeMillis();
        
        if (state.get() == State.OPEN) {
            if (now - lastFailureTime >= recoveryTimeoutInMillis) {
                state.set(State.HALF_OPEN);
            }
        } else if (state.get() == State.CLOSED) {
            if (now - windowStartTime > failureWindowInMillis) {
                failureCount.set(0);
                windowStartTime = now;
            }
        }
    }

    private synchronized void onSuccess() {
        if (state.get() == State.HALF_OPEN) {
            state.set(State.CLOSED);
            failureCount.set(0);
            windowStartTime = System.currentTimeMillis();
        }
    }

    private synchronized void onFailure() {
        long now = System.currentTimeMillis();
        lastFailureTime = now;
        
        if (state.get() == State.HALF_OPEN) {
            state.set(State.OPEN);
        } else {
            if (failureCount.incrementAndGet() >= failureThreshold) {
                state.set(State.OPEN);
            }
        }
    }
}
```

**Follow-up: How do I test this implementation in Java?**
- **Unit Testing (JUnit + Mockito)**: Mock the `Callable` task to return a value or throw an Exception.
- **State Transitions**: Simulate 3 failures in a row. Assert the state changes to `OPEN` and the 4th request throws the custom exception without invoking the `Callable`.
- **Time Simulation**: Replace `System.currentTimeMillis()` with an injected `Clock` interface so you can advance time manually in tests.
- **Concurrency Testing**: Use `ExecutorService` to trigger 100 concurrent requests when `CLOSED`. Assert failure counts are exact without race conditions.

---

## 2. Debug a Payment Order Service

**Problem Description:**
You are handed an existing, buggy payment transfer service. Your task is to fix concurrency issues (deadlocks, race conditions) and ensure strict data consistency when transferring money between two accounts.

**Input & Expected Output:**
- **Input:** Concurrent `transfer(fromAccountId, toAccountId, amount)` calls.
- **Output:** Deduct from `fromAccount`, add to `toAccount`. Throw an exception if insufficient funds. Total sum of money across all accounts must remain unchanged.

**Theory:**
Money transfer must guarantee ACID properties. The primary issue in multi-threaded environments is **Deadlock** (Thread 1 locks Account A, waits for B; Thread 2 locks Account B, waits for A).

**Key Points & Tricks to Remember:**
- **Deadlock Prevention:** Always acquire locks in a globally consistent order (e.g., sort by Account ID).
- **Tip**: Never use `double` or `float` for currency. Always use `BigDecimal` to prevent precision loss.
- **Tip**: Lock the accounts before checking balances, not after.

**Java Implementation:**
```java
import java.math.BigDecimal;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Account {
    final String id;
    BigDecimal balance;
    final Lock lock = new ReentrantLock();

    public Account(String id, BigDecimal initialBalance) {
        this.id = id;
        this.balance = initialBalance;
    }
}

public class PaymentService {

    public void transfer(Account from, Account to, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }

        // Lock ordering by ID string comparison to prevent deadlocks
        Account firstLock = from.id.compareTo(to.id) < 0 ? from : to;
        Account secondLock = from.id.compareTo(to.id) < 0 ? to : from;

        firstLock.lock.lock();
        try {
            secondLock.lock.lock();
            try {
                if (from.balance.compareTo(amount) < 0) {
                    throw new RuntimeException("Insufficient funds");
                }
                
                from.balance = from.balance.subtract(amount);
                to.balance = to.balance.add(amount);
            } finally {
                secondLock.lock.unlock();
            }
        } finally {
            firstLock.lock.unlock();
        }
    }
}
```

**Follow-up: How do I test this implementation in Java?**
- **Deadlock Test:** Spawn 100 threads where 50 transfer from A to B, and 50 from B to A simultaneously. With a timeout in the test, it will hang if a deadlock occurs.
- **Consistency Test:** Initialize Account A with 1000 and Account B with 1000. Run thousands of random concurrent transfers. Wait for termination and assert `A.balance + B.balance == 2000`.

---

## 3. Batched Transactions 

**Problem Description:**
Implement a function that processes a batch of transactions. It must guarantee at-least-once processing but also handle deduplication (idempotency) so identical transactions aren't processed twice. It should gracefully handle partial failures within the batch.

**Input & Expected Output:**
- **Input:** A list of `Transaction` objects (some might have the same ID).
- **Output:** A `BatchResult` object containing a list of `successfulIds` and a list of `failedIds`.

**Theory:**
In distributed systems, messages can be delivered more than once. Idempotency guarantees that a duplicate transaction does not change the state beyond the first application. 

**Key Points & Tricks to Remember:**
- **Tip**: Use `ConcurrentHashMap.putIfAbsent()` as a thread-safe, atomic way to check and mark a transaction as processed.
- **Tip**: Revert the processed state if the business logic throws an exception, allowing it to be retried in a future batch.

**Java Implementation:**
```java
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

class Transaction {
    String id;
    String data;
    public Transaction(String id, String data) { this.id = id; this.data = data; }
}

class BatchResult {
    List<String> successfulIds = new ArrayList<>();
    List<String> failedIds = new ArrayList<>();
}

public class BatchProcessor {
    // Simulates a persistent store of processed IDs for idempotency
    private final ConcurrentHashMap<String, Boolean> processedDb = new ConcurrentHashMap<>();

    public BatchResult processBatch(List<Transaction> batch) {
        BatchResult result = new BatchResult();

        for (Transaction tx : batch) {
            // Deduplication check: Atomically try to mark as processing/processed
            if (processedDb.putIfAbsent(tx.id, true) != null) {
                // Already processed, skip (Idempotent)
                result.successfulIds.add(tx.id); 
                continue;
            }

            try {
                processSafely(tx);
                result.successfulIds.add(tx.id);
            } catch (Exception e) {
                // Revert deduplication state so it can be retried later
                processedDb.remove(tx.id);
                result.failedIds.add(tx.id);
            }
        }
        return result;
    }

    private void processSafely(Transaction tx) throws Exception {
        if (tx.data.equals("FAIL")) throw new Exception("Simulated DB failure");
    }
}
```

**Follow-up: How do I test this implementation in Java?**
- **Idempotency Test:** Submit a list of identical transactions. Assert that `processSafely` is called only once, but `successfulIds` contains the IDs.
- **Partial Failure Test:** Pass a batch where some transactions trigger exceptions. Assert `BatchResult` separates successful and failed IDs properly.

---

## 4. Cache Implementation (LRU)

**Problem Description:**
Design a thread-safe Least Recently Used (LRU) cache with O(1) time complexity for `get` and `put` operations. When capacity is reached, it should evict the least recently used item.

**Input & Expected Output:**
- **Input:** `LRUCache(capacity=2)`, `put(1,1)`, `put(2,2)`, `get(1)`, `put(3,3)`, `get(2)`.
- **Output:** `get(1) -> 1`, `get(2) -> null` (since 2 was evicted when 3 was inserted).

**Theory:**
An LRU cache requires O(1) reads and writes, achieved by combining a `HashMap` (for O(1) lookup) and a `Doubly Linked List` (for O(1) removals and moving items to the front).

**Key Points & Tricks to Remember:**
- **Tip**: Use dummy `head` and `tail` nodes in your Doubly Linked List to avoid null checks during insertion and removal.
- **Concurrency**: Wrapping methods in `synchronized` is generally accepted in interviews for simplicity, though it bottlenecks throughput compared to fine-grained locking or `ConcurrentHashMap` combined with thread-safe queues.

**Java Implementation:**
```java
import java.util.HashMap;
import java.util.Map;

public class LRUCache<K, V> {
    class Node {
        K key;
        V value;
        Node prev, next;
        Node(K k, V v) { key = k; value = v; }
    }

    private final int capacity;
    private final Map<K, Node> map;
    private final Node head, tail;

    public LRUCache(int capacity) {
        this.capacity = capacity;
        this.map = new HashMap<>();
        head = new Node(null, null);
        tail = new Node(null, null);
        head.next = tail;
        tail.prev = head;
    }

    public synchronized V get(K key) {
        if (!map.containsKey(key)) return null;
        Node node = map.get(key);
        remove(node);
        insert(node); // Move to front
        return node.value;
    }

    public synchronized void put(K key, V value) {
        if (map.containsKey(key)) {
            remove(map.get(key));
        }
        if (map.size() == capacity) {
            map.remove(tail.prev.key); // Remove LRU
            remove(tail.prev);
        }
        Node newNode = new Node(key, value);
        insert(newNode);
        map.put(key, newNode);
    }

    private void remove(Node node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }

    private void insert(Node node) {
        node.next = head.next;
        node.next.prev = node;
        head.next = node;
        node.prev = head;
    }
}
```

**Follow-up: How do I test this implementation in Java?**
- **Capacity/Eviction Test:** Put elements beyond capacity. Assert that fetching the least recently used keys returns null.
- **Thread-Safety Test:** Have 10 threads spamming `put` and 10 spamming `get`. Assert `map.size() <= capacity` and no exceptions (like `ConcurrentModificationException`) are thrown.

---

## 5. Subarray Product Less Than K

**Problem Description:**
Given an array of integers `nums` and an integer `k`, return the number of contiguous subarrays where the product of all elements is strictly less than `k`.

**Input & Expected Output:**
- **Input:** `nums = [10, 5, 2, 6]`, `k = 100`
- **Output:** `8` (The subarrays are: [10], [5], [2], [6], [10, 5], [5, 2], [2, 6], [5, 2, 6]).

**Theory:**
Sliding Window. Because all numbers are positive, the product strictly increases as the window expands. If window `[left, right]` is valid, then all subarrays ending at `right` and starting within that window are also valid.

**Key Points & Tricks to Remember:**
- **Tip**: If `K <= 1`, return 0 immediately (since array elements are typically >= 1).
- **Tip**: Number of valid subarrays added at each step is `right - left + 1`.

**Java Implementation:**
```java
public class SubarrayProduct {
    public int numSubarrayProductLessThanK(int[] nums, int k) {
        if (k <= 1) return 0;
        
        long prod = 1;
        int count = 0;
        int left = 0;
        
        for (int right = 0; right < nums.length; right++) {
            prod *= nums[right];
            
            // Shrink window if product is too large
            while (prod >= k && left <= right) {
                prod /= nums[left];
                left++;
            }
            
            // Add all valid subarrays ending at 'right'
            count += right - left + 1;
        }
        
        return count;
    }
}
```

**Follow-up: How do I test this implementation in Java?**
- **Edge cases:** `k = 0`, `k = 1`. 
- **Normal cases:** Assert combinations.

---

## 6. Maximal Square

**Problem Description:**
Given an `m x n` binary matrix filled with 0's and 1's, find the largest square containing only 1's and return its area.

**Input & Expected Output:**
- **Input:** `matrix = [["1","0","1","0","0"],["1","0","1","1","1"],["1","1","1","1","1"],["1","0","0","1","0"]]`
- **Output:** `4`

**Theory:**
Dynamic Programming. Let `dp[i][j]` be the side length of the largest square whose bottom-right corner is `(i, j)`. 
`dp[i][j] = min(dp[i-1][j], dp[i][j-1], dp[i-1][j-1]) + 1`

**Key Points & Tricks to Remember:**
- **Space Optimization Tip:** You only need the current row array and one variable `prev` to store the top-left diagonal value `dp[i-1][j-1]`. This reduces space from O(M*N) to O(N).

**Java Implementation (Space Optimized):**
```java
public class MaximalSquare {
    public int maximalSquare(char[][] matrix) {
        if (matrix == null || matrix.length == 0) return 0;
        
        int m = matrix.length;
        int n = matrix[0].length;
        int[] dp = new int[n + 1];
        int maxSide = 0;
        int prev = 0; // Stores dp[i-1][j-1]
        
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                int temp = dp[j];
                if (matrix[i - 1][j - 1] == '1') {
                    dp[j] = Math.min(Math.min(dp[j - 1], dp[j]), prev) + 1;
                    maxSide = Math.max(maxSide, dp[j]);
                } else {
                    dp[j] = 0;
                }
                prev = temp;
            }
        }
        
        return maxSide * maxSide;
    }
}
```

**Follow-up: How do I test this implementation in Java?**
- **Empty matrix:** `[]` or `[[]]`.
- **No squares:** Matrix with all `'0'`s (Output 0).
- **Full matrix:** Matrix with all `'1'`s.

---

## 7. Design Tic-Tac-Toe

**Problem Description:**
Design a Tic-Tac-Toe game on an `n x n` board. Implement `move(row, col, player)` which returns `0` if no one wins, `1` if player 1 wins, and `2` if player 2 wins.

**Input & Expected Output:**
- **Input:** `TicTacToe(3)`, `move(0, 0, 1)`, `move(0, 2, 2)`, `move(2, 2, 1)`, `move(1, 1, 2)`, `move(2, 0, 1)`, `move(1, 0, 2)`, `move(2, 1, 1)`.
- **Output:** `move(2,1,1)` returns `1` (Player 1 wins on the bottom row).

**Theory:**
Instead of an O(N^2) board check, track the sum of marks for rows, columns, and the two diagonals. Player 1 adds 1, Player 2 adds -1. A sum of `N` means Player 1 wins. A sum of `-N` means Player 2 wins.

**Key Points & Tricks to Remember:**
- **Tip:** The condition for the main diagonal is `row == col`.
- **Tip:** The condition for the anti-diagonal is `row + col == n - 1`.
- **Time Complexity**: Achieves O(1) per move.

**Java Implementation:**
```java
public class TicTacToe {
    private int[] rows;
    private int[] cols;
    private int diagonal;
    private int antiDiagonal;
    private int n;

    public TicTacToe(int n) {
        this.n = n;
        rows = new int[n];
        cols = new int[n];
    }

    public int move(int row, int col, int player) {
        int toAdd = player == 1 ? 1 : -1;

        rows[row] += toAdd;
        cols[col] += toAdd;

        if (row == col) {
            diagonal += toAdd;
        }

        if (col + row == n - 1) {
            antiDiagonal += toAdd;
        }

        if (Math.abs(rows[row]) == n || 
            Math.abs(cols[col]) == n || 
            Math.abs(diagonal) == n || 
            Math.abs(antiDiagonal) == n) {
            return player;
        }

        return 0; // No winner yet
    }
}
```

**Follow-up: How do I test this implementation in Java?**
- **Win conditions:** Provide move sequences that trigger a row win, column win, diagonal win, and anti-diagonal win.
- **Draw condition:** Fill the board without triggering a win and assert it always returns 0.

---

## 8. Rate Limiter (Token Bucket)

**Problem Description:**
Design a Rate Limiter to control traffic. Requests should be allowed if within the rate limit, otherwise rejected.

**Input & Expected Output:**
- **Input:** Sequence of `allowRequest()` calls over time.
- **Output:** `true` if allowed, `false` if rejected.

**Theory:**
Token Bucket algorithm adds tokens at a fixed rate up to a max capacity. A request consumes 1 token. If 0 tokens are available, it rejects.

**Key Points & Tricks to Remember:**
- **Tip:** Do NOT use a background thread to refill tokens. Use a lazy calculation (`now - lastRefillTime`) during the request check.
- **Concurrency:** Ensure `allowRequest` is synchronized since multiple threads will modify the `currentTokens`.

**Java Implementation:**
```java
public class RateLimiter {
    private final long maxTokens;
    private final long refillRatePerSecond;
    private double currentTokens;
    private long lastRefillTimestamp;

    public RateLimiter(long maxTokens, long refillRatePerSecond) {
        this.maxTokens = maxTokens;
        this.refillRatePerSecond = refillRatePerSecond;
        this.currentTokens = maxTokens;
        this.lastRefillTimestamp = System.nanoTime();
    }

    public synchronized boolean allowRequest() {
        refill();
        if (currentTokens >= 1.0) {
            currentTokens -= 1.0;
            return true;
        }
        return false;
    }

    private void refill() {
        long now = System.nanoTime();
        double elapsedSeconds = (now - lastRefillTimestamp) / 1_000_000_000.0;
        double tokensToAdd = elapsedSeconds * refillRatePerSecond;

        if (tokensToAdd > 0) {
            currentTokens = Math.min(maxTokens, currentTokens + tokensToAdd);
            lastRefillTimestamp = now;
        }
    }
}
```

**Follow-up: How do I test this implementation in Java?**
- **Basic Limits:** Assert that requesting more than `maxTokens` in a tight loop eventually returns `false`.
- **Refill Delay:** Simulate a thread sleep, then assert the next request returns `true`.
