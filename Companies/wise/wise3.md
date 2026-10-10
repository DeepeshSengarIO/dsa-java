# Wise Frontend Interview Preparation Guide (React & TypeScript)

This document covers the most common frontend interview topics asked at Wise over the past year. It focuses on functional UI components and core JavaScript/TypeScript utilities that power data-heavy interfaces. Each topic includes problem descriptions, theory, key points, production-ready code, and testing strategies.

---

## 1. The React Dashboard Component: Flagged Transactions

**Problem Description:**
Build a React component that fetches a list of flagged transactions from an API. It must include a search bar to filter the results by User ID, and it must gracefully handle loading states and network errors.

**Theory:**
In React, fetching data natively requires managing three distinct states: `loading`, `error`, and `data`. You must also handle side-effects cleanly via `useEffect`. Filtering can be done client-side (if the dataset is small and fully loaded) or server-side (triggering a new fetch when the search input changes).

**Key Points & Tricks to Remember:**
- **Race Conditions:** If you type fast, multiple requests might fire. The response from an older request might arrive *after* a newer one, overwriting the UI with stale data. Use an `AbortController` or a cleanup function boolean flag (e.g., `ignore = true`) in `useEffect` to prevent this.
- **Separation of Concerns:** Move the fetching logic into a custom hook (e.g., `useFlaggedTransactions`) to keep the UI component clean.
- **Debouncing:** Don't fire an API request on every keystroke. Use a debounce function (covered in the next section) or a debounced value.

**TypeScript/React Implementation:**

```tsx
import React, { useState, useEffect } from 'react';

// --- Types ---
export interface Transaction {
  id: string;
  userId: string;
  amount: number;
  currency: string;
  reason: string;
}

// --- Custom Hook ---
function useFlaggedTransactions(searchUserId: string) {
  const [data, setData] = useState<Transaction[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    // 1. Setup AbortController to cancel stale requests
    const controller = new AbortController();
    setIsLoading(true);
    setError(null);

    const fetchTransactions = async () => {
      try {
        const url = searchUserId 
          ? `/api/transactions/flagged?userId=${searchUserId}`
          : `/api/transactions/flagged`;
          
        const response = await fetch(url, { signal: controller.signal });
        
        if (!response.ok) {
          throw new Error(`Failed to fetch: ${response.statusText}`);
        }
        
        const result: Transaction[] = await response.json();
        setData(result);
      } catch (err: any) {
        // Ignore abort errors as they are intentional
        if (err.name === 'AbortError') return;
        setError(err.message || 'An unknown error occurred');
      } finally {
        setIsLoading(false);
      }
    };

    fetchTransactions();

    // Cleanup function runs on unmount or before the next effect runs
    return () => {
      controller.abort();
    };
  }, [searchUserId]); // Re-run whenever searchUserId changes

  return { data, isLoading, error };
}

// --- UI Component ---
export const FlaggedTransactionsDashboard: React.FC = () => {
  const [searchInput, setSearchInput] = useState('');
  const [debouncedUserId, setDebouncedUserId] = useState('');

  // Simple debounce for the input state
  useEffect(() => {
    const handler = setTimeout(() => {
      setDebouncedUserId(searchInput);
    }, 500);
    return () => clearTimeout(handler);
  }, [searchInput]);

  const { data, isLoading, error } = useFlaggedTransactions(debouncedUserId);

  return (
    <div className="dashboard-container">
      <h2>Flagged Transactions</h2>
      
      <input 
        type="text" 
        placeholder="Search by User ID..." 
        value={searchInput}
        onChange={(e) => setSearchInput(e.target.value)}
        className="search-input"
      />

      {isLoading && <div className="loader">Loading transactions...</div>}
      {error && <div className="error-banner">Error: {error}</div>}
      
      {!isLoading && !error && data.length === 0 && (
        <p>No flagged transactions found.</p>
      )}

      {!isLoading && !error && data.length > 0 && (
        <ul className="transaction-list">
          {data.map((tx) => (
            <li key={tx.id} className="transaction-item">
              <strong>User {tx.userId}</strong> flagged for "{tx.reason}". 
              Amount: {tx.amount} {tx.currency}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
};
```

**Follow-up: How do I test this implementation?**
- **Unit Testing (React Testing Library):** Mock `fetch` globally (e.g., using MSW - Mock Service Worker) to return a 200 OK with mock data, a 404, or a 500.
- **User Interaction:** Simulate a user typing in the input using `userEvent.type`. Assert that the loading spinner appears, then disappears, and the filtered data is rendered.
- **Race Condition Testing:** Assert that `fetch` is called with an `AbortSignal`, proving that stale requests are cancelled on fast keystrokes.

---

## 2. Core Utility: Debounce Function

**Problem Description:**
Write a `debounce` function for a search input that limits the rate at which a function can fire. 

**Input & Expected Output:**
- **Input:** A function `fn` and a delay `ms`.
- **Output:** A wrapped version of `fn` that, when called repeatedly, only executes once the caller stops calling it for `ms` milliseconds.

**Theory:**
Debouncing is achieved through **Closures**. The outer function returns an inner function that holds a reference to a timer variable in its lexical scope. Every time the inner function is called, it clears the existing timer and starts a new one.

**Key Points & Tricks to Remember:**
- **`this` context and arguments:** You must ensure that the debounced function passes `this` (if applicable) and all arguments (`...args`) to the original function.
- **TypeScript Generics:** Use generics to type the function properly so that the returned function accepts the same arguments as the original.

**TypeScript Implementation:**

```typescript
// Define a generic type for any function
type AnyFunction = (...args: any[]) => any;

export function debounce<T extends AnyFunction>(
  func: T,
  delayMs: number
): (...args: Parameters<T>) => void {
  
  // Timer is stored in the closure
  let timeoutId: ReturnType<typeof setTimeout> | null = null;

  return function (this: any, ...args: Parameters<T>) {
    // If function is called again, clear the old timer
    if (timeoutId !== null) {
      clearTimeout(timeoutId);
    }

    // Set a new timer
    timeoutId = setTimeout(() => {
      // Apply the original function with the correct 'this' and arguments
      func.apply(this, args);
    }, delayMs);
  };
}
```

**Follow-up: How do I test this implementation?**
- **Jest Fake Timers:** In a test environment like Jest, use `jest.useFakeTimers()`. 
- **Execution Test:** Call the debounced function 5 times synchronously. Fast-forward time by `delayMs - 1` and assert it hasn't been called. Fast-forward by `1` more ms and assert it was called exactly *once*.

---

## 3. Core Utility: Fetch Wrapper with Retries

**Problem Description:**
Write a wrapper around the native `fetch` API that automatically retries failed requests up to 3 times before throwing an error.

**Theory:**
Networks are unreliable (especially on mobile). If an API call fails due to a network glitch or a 5xx server error, retrying the request often succeeds. We achieve this using a recursive function or an asynchronous `while` loop.

**Key Points & Tricks to Remember:**
- **Exponential Backoff:** Immediately retrying a struggling server will just overwhelm it further. Introduce a delay between retries that increases (e.g., 500ms, then 1000ms, then 2000ms).
- **Idempotency:** Only retry `GET`, `PUT`, or `DELETE` requests safely. Retrying `POST` requests blindly might result in double charges unless you pass an idempotency key.
- **Fail-Fast on Client Errors:** Do not retry 4xx errors (like 400 Bad Request or 401 Unauthorized), because retrying them will just result in the exact same error. Only retry network failures and 5xx (Server) errors.

**TypeScript Implementation:**

```typescript
// Helper function to pause execution
const delay = (ms: number) => new Promise(resolve => setTimeout(resolve, ms));

export async function fetchWithRetry(
  url: string,
  options: RequestInit = {},
  maxRetries: number = 3,
  backoffMs: number = 500
): Promise<Response> {
  let attempt = 0;

  while (attempt < maxRetries) {
    try {
      const response = await fetch(url, options);

      // If response is successful, return immediately
      if (response.ok) {
        return response;
      }

      // Do NOT retry 4xx client errors (e.g., 400, 401, 404)
      if (response.status >= 400 && response.status < 500) {
        throw new Error(`Client Error ${response.status}: ${response.statusText}`);
      }

      // If it's a 5xx error, we throw to trigger the catch block & retry logic
      throw new Error(`Server Error ${response.status}`);
      
    } catch (error: any) {
      attempt++;
      
      // If we've exhausted all retries, throw the final error
      if (attempt >= maxRetries) {
        throw new Error(`Failed after ${maxRetries} attempts. Last error: ${error.message}`);
      }
      
      // Wait before retrying (Exponential Backoff: 500ms, 1000ms, 2000ms...)
      const waitTime = backoffMs * Math.pow(2, attempt - 1);
      console.warn(`Attempt ${attempt} failed. Retrying in ${waitTime}ms...`);
      await delay(waitTime);
    }
  }

  // Fallback (TypeScript requires a return, though logic shouldn't reach here)
  throw new Error("Unreachable");
}
```

**Follow-up: How do I test this implementation?**
- **Mocking Fetch:** Mock `global.fetch` to reject twice and resolve on the third call. Assert that the final result resolves correctly and `fetch` was called exactly 3 times.
- **Client Error Test:** Mock `fetch` to return a `404`. Assert that the function throws immediately and does *not* retry.

---

## 4. Currency Input & Formatting Component

**Problem Description:**
Wise heavily deals with money. Build a React input component that formats a raw number into a localized currency string (e.g., `$1,234.56`) while the user types, without losing mathematical precision.

**Theory:**
In JavaScript, formatting money using `Number` can lead to floating-point precision issues (e.g., `0.1 + 0.2 === 0.30000000000000004`). For display, always rely on `Intl.NumberFormat`. Separate the internal numerical state (the raw value) from the formatted display state.

**Key Points & Tricks to Remember:**
- **State Separation:** Maintain the raw unformatted string (e.g. `"1234.56"`) in state to avoid losing precision or parsing errors, and calculate the formatted display value dynamically.
- **Cursor jumping:** Masked inputs often suffer from cursor jumping to the end. (An advanced point to mention in an interview).

**TypeScript/React Implementation:**

```tsx
import React, { useState } from 'react';

interface CurrencyInputProps {
  currencyCode: string; // e.g., 'USD', 'EUR', 'GBP'
  locale?: string;      // e.g., 'en-US', 'en-GB'
  onChange: (rawAmount: number) => void;
}

export const CurrencyInput: React.FC<CurrencyInputProps> = ({ 
  currencyCode, 
  locale = 'en-US',
  onChange 
}) => {
  // Store the raw string value (to handle trailing dots like "12.")
  const [rawValue, setRawValue] = useState<string>('');

  const formatter = new Intl.NumberFormat(locale, {
    style: 'currency',
    currency: currencyCode,
    minimumFractionDigits: 0, 
    maximumFractionDigits: 2,
  });

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    // 1. Strip out everything except digits and decimal points
    const input = e.target.value.replace(/[^0-9.]/g, '');
    
    // 2. Prevent multiple decimals
    const parts = input.split('.');
    let sanitizedInput = input;
    if (parts.length > 2) {
      sanitizedInput = parts[0] + '.' + parts.slice(1).join('');
    }

    setRawValue(sanitizedInput);

    // 3. Pass the parsed float to the parent safely
    const parsed = parseFloat(sanitizedInput);
    onChange(isNaN(parsed) ? 0 : parsed);
  };

  // 4. Calculate the display value
  // If the user typed a dot at the end (e.g., "12."), preserve it visually temporarily
  let displayValue = '';
  if (rawValue) {
    const parsed = parseFloat(rawValue);
    if (!isNaN(parsed)) {
      displayValue = formatter.format(parsed);
      // Append decimal if they are in the middle of typing fractions
      if (rawValue.endsWith('.')) {
        displayValue += '.';
      }
    }
  }

  return (
    <div className="currency-input-wrapper">
      <label>Amount in {currencyCode}</label>
      <input
        type="text"
        value={displayValue}
        onChange={handleChange}
        placeholder={formatter.format(0)}
      />
    </div>
  );
};
```

**Follow-up: How do I test this implementation?**
- **Typing behavior:** Assert that typing `1`, `2`, `3`, `4` results in `$1,234` displaying, but the parent receives the raw integer `1234`.
- **Invalid characters:** Try typing `$abc1.2z` using `userEvent` and assert that the letters are stripped and ignored seamlessly.

---

## 5. React Hook: `usePolling`

**Problem Description:**
Wise requires live currency exchange rates to be updated frequently, or to check the status of a money transfer. Implement a `usePolling` hook that executes an async function every `N` milliseconds. 

**Theory:**
Similar to `setInterval`, but with async operations, you don't want to use standard `setInterval` because if the API call takes longer than the interval, you will stack overlapping requests. Instead, use a recursive `setTimeout` that only schedules the next execution *after* the previous async fetch has completed.

**TypeScript Implementation:**

```typescript
import { useEffect, useRef } from 'react';

export function usePolling(
  callback: () => Promise<void>,
  intervalMs: number,
  isEnabled: boolean = true
) {
  // Use refs to always have the latest callback without re-triggering effects
  const callbackRef = useRef(callback);
  
  useEffect(() => {
    callbackRef.current = callback;
  }, [callback]);

  useEffect(() => {
    if (!isEnabled) return;

    let timeoutId: ReturnType<typeof setTimeout>;
    let isMounted = true;

    const executePoll = async () => {
      try {
        await callbackRef.current();
      } catch (error) {
        console.error("Polling error:", error);
      } finally {
        // Schedule the next poll ONLY after this one completes
        if (isMounted) {
          timeoutId = setTimeout(executePoll, intervalMs);
        }
      }
    };

    // Start the first poll
    executePoll();

    return () => {
      isMounted = false;
      clearTimeout(timeoutId);
    };
  }, [intervalMs, isEnabled]);
}
```

**Follow-up: How do I test this implementation?**
- **Timing:** Use `jest.useFakeTimers()`. Start the hook, wait for the mock API promise to resolve, then advance time by `intervalMs`. Assert the callback was called exactly twice.
- **Overlap Prevention:** Resolve the mock API call very slowly. Advance the clock. Assert that a second call does *not* fire until the first one actually resolves.
