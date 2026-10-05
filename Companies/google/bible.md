# Category A: Advanced Data Structures, Sliding Windows & Deques

## 1. LC 239: Sliding Window Maximum (Hard)

### 1. The Full Problem Specification
*   **Problem Statement:** You are given an array of integers `nums`, there is a sliding window of size `k` which is moving from the very left of the array to the very right. You can only see the `k` numbers in the window. Each time the sliding window moves right by one position. Return the max sliding window.
    *   Constraints: $1 \le nums.length \le 10^5$, $-10^4 \le nums[i] \le 10^4$, $1 \le k \le nums.length$.
*   **Input & Output Examples:**
    *   *Example 1:* `nums = [1,3,-1,-3,5,3,6,7], k = 3` $\to$ `Output: [3,3,5,5,6,7]`. (Window moves, taking max of each 3-element contiguous subarray).
    *   *Example 2 (Edge Case):* `nums = [1, -1], k = 1` $\to$ `Output: [1, -1]`. (Window size 1, output is identical to input).

### 2. The Core Insights & Tricks
*   **The "Aha!" Moment:** We don't need to keep all $k$ elements in our window state. If a new element comes in that is larger than the previous elements in the window, those previous elements can *never* be the maximum again. Thus, we can maintain a strictly monotonically decreasing deque of indices. 
*   **Common Pitfalls:** Using a Max-Heap (Priority Queue) seems natural but removal in a standard Java `PriorityQueue` is $O(k)$ (since it's an array-based heap, finding the element to remove takes linear time unless we use lazy deletion). Lazy deletion works but takes $O(N \log N)$ overall. We need $O(N)$.
*   **Pattern Recognition:** "Sliding window" + "Maximum/Minimum of window" instantly screams Monotonic Deque. 

### 3. The Proposed Idea (Base Optimal Solution)
*   We use a double-ended queue (`Deque`) that stores the **indices** of elements.
*   The deque will maintain indices such that the elements they point to are strictly decreasing.
*   For each element `nums[i]`:
    1.  **Remove out-of-bounds:** If the index at the front of the deque is out of the current window (`deque.peekFirst() < i - k + 1`), remove it.
    2.  **Maintain monotonicity:** While the deque is not empty and the current element `nums[i]` is greater than or equal to the element at the back of the deque (`nums[deque.peekLast()]`), pop the back. The smaller elements are useless now.
    3.  **Add current:** Add the current index `i` to the back of the deque.
    4.  **Record answer:** If our window has reached size `k` (i.e., `i >= k - 1`), the maximum is at the front of the deque (`nums[deque.peekFirst()]`).
*   **Time Complexity:** $O(N)$. Each element is pushed and popped from the deque at most once. 
*   **Space Complexity:** $O(k)$. The deque will hold at most $k$ indices at any point.

### 4. Production-Ready Java Implementation (Base)
```java
class Solution {
    public int[] maxSlidingWindow(int[] nums, int k) {
        if (nums == null || nums.length == 0 || k <= 0) {
            return new int[0];
        }
        
        int n = nums.length;
        int[] windowMaxes = new int[n - k + 1];
        // Deque stores indices, not values, to easily check window boundaries
        Deque<Integer> monotonicDeque = new ArrayDeque<>();
        
        for (int i = 0; i < n; i++) {
            // 1. Evict indices that are out of the current window of size k
            if (!monotonicDeque.isEmpty() && monotonicDeque.peekFirst() < i - k + 1) {
                monotonicDeque.pollFirst();
            }
            
            // 2. Maintain monotonic strictly decreasing order
            // If current element is larger, it invalidates smaller preceding elements
            while (!monotonicDeque.isEmpty() && nums[monotonicDeque.peekLast()] <= nums[i]) {
                monotonicDeque.pollLast();
            }
            
            // 3. Add the current element's index
            monotonicDeque.offerLast(i);
            
            // 4. Extract the max for the current window if the window is fully formed
            if (i >= k - 1) {
                windowMaxes[i - k + 1] = nums[monotonicDeque.peekFirst()];
            }
        }
        
        return windowMaxes;
    }
}
```

### 5. Edge Cases & Dry-Run Checklist
*   **Edge Cases Handled:**
    *   `k = 1`: Works perfectly, pops out-of-bound at each step, pushing each element.
    *   Strictly decreasing array (`[5, 4, 3, 2]`, `k=2`): Deque size grows to `k`, front is always the max.
    *   Strictly increasing array (`[1, 2, 3, 4]`, `k=2`): `while` loop clears deque every time, size is always 1.
    *   Duplicates (`[3, 3, 3]`, `k=2`): `nums[peekLast()] <= nums[i]` ensures we pop older duplicates, keeping the freshest index.
*   **Dry-Run Checklist:**
    *   Track `i` (current index).
    *   Track `monotonicDeque` (state of indices).
    *   Track `windowMaxes` (output array).
    *   Verify the `i - k + 1` window boundary calculation.

### 6. The Google Follow-Up Gauntlet

*   **Follow-Up 1: The Twist - Stream Constraint (Unlimited incoming data)**
    *   *The Twist:* Instead of a fixed array, you receive a continuous stream of integers `add(int val)`. You need to query `getMax()` over the last `k` elements at any time.
    *   *The Architectural Pivot:* We can no longer use an output array. We still use a monotonic deque, but we can't store absolute indices endlessly because they will eventually integer overflow. We use a `long` for the index, or store pairs of `{value, current_index}`.
    *   *The Follow-Up Solution (Java):*
        ```java
        class MovingWindowMax {
            private final int k;
            private long currentIndex; 
            private final Deque<long[]> deque; // Stores {value, absoluteIndex}
            
            public MovingWindowMax(int k) {
                this.k = k;
                this.currentIndex = 0;
                this.deque = new ArrayDeque<>();
            }
            
            public void add(int val) {
                // Remove elements outside the window
                if (!deque.isEmpty() && deque.peekFirst()[1] <= currentIndex - k) {
                    deque.pollFirst();
                }
                
                // Maintain monotonically decreasing property
                while (!deque.isEmpty() && deque.peekLast()[0] <= val) {
                    deque.pollLast();
                }
                
                deque.offerLast(new long[]{val, currentIndex});
                currentIndex++;
            }
            
            public int getMax() {
                if (deque.isEmpty()) throw new IllegalStateException("Stream is empty");
                return (int) deque.peekFirst()[0];
            }
        }
        ```
        *Time/Space:* $O(1)$ amortized per `add`, $O(1)$ per `getMax`. Space: $O(k)$.

*   **Follow-Up 2: The Twist - Dynamic Time Window**
    *   *The Twist:* The window is no longer fixed size `k` elements. Instead, the window is defined by a time constraint. You receive `add(int timestamp, int val)` and `getMax(int current_timestamp, int time_window)`.
    *   *The Architectural Pivot:* A simple monotonic deque still works, but eviction shifts to a lazy evaluation inside `getMax`. Instead of evicting based on index `i - k + 1`, we evict from the front where `timestamp < current_timestamp - time_window`.
    *   *The Follow-Up Solution (Java):*
        ```java
        class TimeSlidingWindowMax {
            // Stores {timestamp, value}
            private final Deque<long[]> deque = new ArrayDeque<>();
            
            public void add(int timestamp, int val) {
                while (!deque.isEmpty() && deque.peekLast()[1] <= val) {
                    deque.pollLast();
                }
                deque.offerLast(new long[]{timestamp, val});
            }
            
            public int getMax(int current_timestamp, int time_window) {
                int threshold = current_timestamp - time_window;
                // Lazy eviction
                while (!deque.isEmpty() && deque.peekFirst()[0] <= threshold) {
                    deque.pollFirst();
                }
                if (deque.isEmpty()) throw new IllegalStateException("No elements in window");
                return (int) deque.peekFirst()[1];
            }
        }
        ```

---

## 2. LC 862: Shortest Subarray with Sum at Least K (Hard)

### 1. The Full Problem Specification
*   **Problem Statement:** Given an integer array `nums` and an integer `k`, return the length of the shortest non-empty subarray of `nums` with a sum of at least `k`. If there is no such subarray, return `-1`.
    *   Constraints: $1 \le nums.length \le 10^5$, $-10^5 \le nums[i] \le 10^5$, $1 \le k \le 10^9$.
*   **Input & Output Examples:**
    *   *Example 1:* `nums = [2,-1,2], k = 3` $\to$ `Output: 3`. (The entire array sums to 3. Subarrays like `[2,-1]` sum to 1).
    *   *Example 2:* `nums = [1,2], k = 4` $\to$ `Output: -1`. (Max sum is 3).

### 2. The Core Insights & Tricks
*   **The "Aha!" Moment:** Because the array contains **negative numbers**, a standard two-pointer sliding window fails (expanding the window doesn't guarantee the sum increases, and shrinking it doesn't guarantee the sum decreases). Instead, we map the problem to prefix sums: we need `Prefix[y] - Prefix[x] >= k` where `y > x`, and we want to minimize `y - x`. This translates to: for a given `y`, find the largest `x` such that `Prefix[x] <= Prefix[y] - k`. 
*   **Common Pitfalls:** Trying to use a HashMap of Prefix sums (like LC 560: Subarray Sum Equals K). The HashMap works for *exact* equality, not *at least* `k`. Another pitfall is using a binary search tree (like TreeMap in Java) to find the largest `x`, which gives $O(N \log N)$. 
*   **Pattern Recognition:** "Shortest subarray" + "Sum at least K" + "Negative numbers" = Prefix Sum + Monotonic Deque.

### 3. The Proposed Idea (Base Optimal Solution)
*   Compute the prefix sum array `prefix` where `prefix[i]` is the sum of the first `i` elements. `prefix.length == n + 1`.
*   Maintain a monotonic deque of indices of the `prefix` array. The deque will store indices in **strictly increasing order of their prefix sum values**.
*   Why increasing order? If `x1 < x2` and `Prefix[x1] >= Prefix[x2]`, then `x1` is completely useless! Any future `Prefix[y]` that satisfies `Prefix[y] - Prefix[x1] >= k` will *also* satisfy `Prefix[y] - Prefix[x2] >= k`. And since `x2` is a larger index, `y - x2` will yield a strictly shorter (better) subarray length than `y - x1`. This eliminates "bad" starting points.
*   For each index `y` from 0 to `n`:
    1.  **Check for valid subarrays:** While the deque is not empty and `prefix[y] - prefix[deque.peekFirst()] >= k`, we found a valid subarray! Update the minimum length. We can then `pollFirst()` because we already found the shortest subarray starting at `deque.peekFirst()` that reaches the sum `k` (any future `y` would result in a longer subarray).
    2.  **Maintain monotonicity:** While the deque is not empty and `prefix[deque.peekLast()] >= prefix[y]`, pop the back. (As established, these indices are mathematically inferior).
    3.  **Add current:** Add `y` to the deque.
*   **Time Complexity:** $O(N)$. Each index is pushed and popped exactly once.
*   **Space Complexity:** $O(N)$ for the prefix array and the deque.

### 4. Production-Ready Java Implementation (Base)
```java
class Solution {
    public int shortestSubarray(int[] nums, int k) {
        int n = nums.length;
        // prefix[i] stores the sum of first i elements
        long[] prefixSum = new long[n + 1];
        for (int i = 0; i < n; i++) {
            prefixSum[i + 1] = prefixSum[i] + nums[i];
        }
        
        int shortestLength = Integer.MAX_VALUE;
        // Deque stores indices of prefixSum array
        Deque<Integer> monotonicDeque = new ArrayDeque<>();
        
        for (int i = 0; i <= n; i++) {
            // 1. Check if we have found a valid subarray
            // We evaluate from the front (smallest prefix sums)
            while (!monotonicDeque.isEmpty() && prefixSum[i] - prefixSum[monotonicDeque.peekFirst()] >= k) {
                shortestLength = Math.min(shortestLength, i - monotonicDeque.pollFirst());
            }
            
            // 2. Maintain monotonic strictly increasing property of prefix sums
            // If the current prefix sum is smaller than or equal to the back of the deque,
            // the back element is useless (it's older AND has a larger sum)
            while (!monotonicDeque.isEmpty() && prefixSum[monotonicDeque.peekLast()] >= prefixSum[i]) {
                monotonicDeque.pollLast();
            }
            
            // 3. Add current index to deque
            monotonicDeque.offerLast(i);
        }
        
        return shortestLength == Integer.MAX_VALUE ? -1 : shortestLength;
    }
}
```

### 5. Edge Cases & Dry-Run Checklist
*   **Edge Cases Handled:**
    *   No valid subarray: `shortestLength` remains `Integer.MAX_VALUE`, returns `-1`.
    *   Single element array `nums=[k]`: `prefixSum` is `[0, k]`. `i=1` will see `prefixSum[1] - prefixSum[0] == k >= k`, length 1.
    *   Large arrays causing integer overflow: `prefixSum` is correctly typed as `long[]`.
*   **Dry-Run Checklist:**
    *   Track `prefixSum` array generation.
    *   Track `monotonicDeque` (indices, but keep in mind their corresponding `prefixSum` values).
    *   Track `shortestLength`.

### 6. The Google Follow-Up Gauntlet

*   **Follow-Up 1: The Twist - Memory Constraint ($O(1)$ Space / Disk-bound)**
    *   *The Twist:* The input array `nums` is massive and streaming. You cannot allocate a `long[] prefixSum` array of size $O(N)$. Can you solve it in $O(1)$ space?
    *   *The Architectural Pivot:* If $O(1)$ space is strictly required and negative numbers exist, the problem is impossible unless $K$ is exceptionally small or the stream properties are constrained. If the array had **no negative numbers**, we could revert to a standard sliding window `[left, right]` in $O(1)$ space. 
    *   *The Follow-Up Solution (No Negatives allowed, $O(1)$ space):*
        ```java
        public int shortestSubarrayPositiveOnly(int[] nums, int k) {
            int shortestLength = Integer.MAX_VALUE;
            int currentSum = 0;
            int left = 0;
            
            for (int right = 0; right < nums.length; right++) {
                currentSum += nums[right];
                while (currentSum >= k) {
                    shortestLength = Math.min(shortestLength, right - left + 1);
                    currentSum -= nums[left];
                    left++;
                }
            }
            return shortestLength == Integer.MAX_VALUE ? -1 : shortestLength;
        }
        ```

*   **Follow-Up 2: The Twist - Dynamic Mutability (Segment Tree)**
    *   *The Twist:* You are given the array, but the user can call `update(index, val)` to modify elements, and then ask for `shortestSubarray(k)`.
    *   *The Architectural Pivot:* A Monotonic Deque is static. For dynamic point updates and range queries, we must pivot to a **Segment Tree**. Specifically, we need a segment tree that maintains the prefix sums and the minimum prefix sum over ranges. This dramatically changes the algorithm to a divide-and-conquer approach where querying takes $O(\log N)$ or $O(N \log N)$. 

---

## 3. LC 84: Largest Rectangle in Histogram (Hard)

### 1. The Full Problem Specification
*   **Problem Statement:** Given an array of integers `heights` representing the histogram's bar height where the width of each bar is `1`, return the area of the largest rectangle in the histogram.
*   **Input & Output Examples:**
    *   *Example 1:* `heights = [2,1,5,6,2,3]` $\to$ `Output: 10`. (The largest rectangle is bounded by `5` and `6`, with height 5 and width 2).
    *   *Example 2:* `heights = [2,4]` $\to$ `Output: 4`. (Rectangle of height 2 and width 2).

### 2. The Core Insights & Tricks
*   **The "Aha!" Moment:** Every maximal rectangle is bottlenecked by the shortest bar within its width. Thus, for *every* bar `i`, we can assume it is the shortest bar and try to extend the rectangle as far left and as far right as possible. We need to find the First Smaller Element to the Left (LSE) and First Smaller Element to the Right (RSE). 
*   **Common Pitfalls:** The $O(N^2)$ expansion check will TLE.
*   **Pattern Recognition:** "First Smaller Element to the Left/Right" dictates a **Monotonic Stack**.

### 3. The Proposed Idea (Base Optimal Solution)
*   We use a stack to store the **indices** of the histogram bars.
*   The stack maintains indices such that their heights are strictly **increasing**.
*   As we iterate through the heights (adding a pseudo-height of `0` at the end to force the stack to flush):
    *   While the current height is **less than** the height of the bar at the top of the stack, it means the current bar is the RSE for the stack's top bar! 
    *   We pop the top bar. We calculate its area. Its height is `heights[popped_index]`.
    *   Its LSE is the *new* top of the stack (because the stack is increasing). 
    *   The width is `current_index - new_top_index - 1`. If the stack is empty, the width is simply `current_index`.
*   **Time Complexity:** $O(N)$. Every index is pushed and popped exactly once.
*   **Space Complexity:** $O(N)$ for the stack.

### 4. Production-Ready Java Implementation (Base)
```java
class Solution {
    public int largestRectangleArea(int[] heights) {
        if (heights == null || heights.length == 0) return 0;
        
        int n = heights.length;
        int maxArea = 0;
        // Monotonic stack storing indices, maintaining strictly increasing heights
        Deque<Integer> stack = new ArrayDeque<>();
        
        // Loop runs to n (inclusive) to flush the stack at the end
        for (int i = 0; i <= n; i++) {
            // Treat the out-of-bounds height as 0 to force-pop everything in the stack
            int currentHeight = (i == n) ? 0 : heights[i];
            
            // While the current height is smaller, we've found the Right Smaller Element
            while (!stack.isEmpty() && currentHeight < heights[stack.peekFirst()]) {
                int poppedIndex = stack.pollFirst(); // The bar dictating the rectangle's height
                int h = heights[poppedIndex];
                
                // The Left Smaller Element is the new top of the stack
                // If stack is empty, there is no smaller element to the left
                int width = stack.isEmpty() ? i : i - stack.peekFirst() - 1;
                
                maxArea = Math.max(maxArea, h * width);
            }
            
            stack.offerFirst(i); // Push current index as a potential boundary
        }
        
        return maxArea;
    }
}
```

### 5. Edge Cases & Dry-Run Checklist
*   **Edge Cases Handled:**
    *   All elements equal (`[2,2,2]`): Stack grows to 3, then at `i=n`, pops all, multiplying `2 * (3 - 0)`, `2 * (3 - 1)` etc. Works perfectly.
    *   Strictly descending (`[3,2,1]`): Pops at each step, correctly calculating width 1, 2, 3.
    *   Empty array: Base check returns `0`.
*   **Dry-Run Checklist:**
    *   Always verify the pseudo-element `0` at `i == n`.
    *   Verify width calculation `i - peek - 1`. It is the source of 90% of off-by-one errors in this problem.

### 6. The Google Follow-Up Gauntlet

*   **Follow-Up 1: The Twist - 2D Matrix (LC 85 Maximal Rectangle)**
    *   *The Twist:* Instead of a 1D histogram, you are given a 2D binary matrix. Find the largest rectangle of `1`s.
    *   *The Architectural Pivot:* We treat each row of the matrix as the base of a histogram. We maintain an array of `heights`. For each row, if the matrix cell is `1`, we increment `heights[col]`. If it is `0`, we reset `heights[col] = 0`. We then run the $O(N)$ histogram algorithm on each row.
    *   *The Follow-Up Solution (Java snippet):*
        ```java
        public int maximalRectangle(char[][] matrix) {
            if (matrix.length == 0) return 0;
            int maxArea = 0;
            int[] heights = new int[matrix[0].length];
            
            for (char[] row : matrix) {
                for (int c = 0; c < row.length; c++) {
                    heights[c] = (row[c] == '1') ? heights[c] + 1 : 0;
                }
                maxArea = Math.max(maxArea, largestRectangleArea(heights)); // Call Base Method
            }
            return maxArea;
        }
        ```
        *Time:* $O(R \times C)$. *Space:* $O(C)$.

*   **Follow-Up 2: The Twist - Dynamic Mutability (Online Querying)**
    *   *The Twist:* The histogram bars can be updated `update(index, newHeight)`, followed by `getLargestRectangle()`.
    *   *The Architectural Pivot:* A monotonic stack cannot handle online updates. We must pivot to a Divide and Conquer Segment Tree approach. The segment tree node represents a range and stores the *minimum height index* in that range. To find the max area, we query the minimum height in the whole array, calculate the area, and recursively do the same for the left and right halves. 
    *   *Complexity:* Updates take $O(\log N)$. Querying the max rectangle takes $O(N \log N)$ worst case (or $O(N)$ if unbalanced). This is a massive jump in complexity and changes the core algorithm entirely.

---

## 4. LC 992: Subarrays with K Different Integers (Hard)

### 1. The Full Problem Specification
*   **Problem Statement:** Given an integer array `nums` and an integer `k`, return the number of good subarrays of `nums`. A good subarray is a contiguous part of an array where the number of different integers in that array is exactly `k`.
    *   Constraints: $1 \le nums.length \le 2 \times 10^4$, $1 \le nums[i], k \le nums.length$.
*   **Input & Output Examples:**
    *   *Example 1:* `nums = [1,2,1,2,3], k = 2` $\to$ `Output: 7`. (Subarrays: `[1,2]`, `[2,1]`, `[1,2]`, `[2,3]`, `[1,2,1]`, `[2,1,2]`, `[1,2,1,2]`).
    *   *Example 2:* `nums = [1,2,1,3,4], k = 3` $\to$ `Output: 3`. (`[1,2,1,3]`, `[2,1,3]`, `[1,3,4]`).

### 2. The Core Insights & Tricks
*   **The "Aha!" Moment:** Finding "exactly $K$" distinct elements in a sliding window is extremely difficult because a single right-pointer expansion might not change the distinct count, and moving the left pointer might or might not change it, creating ambiguity in counting. The trick is the **At Most $K$ Transformation**. Mathematically: $\text{Exact}(K) = \text{AtMost}(K) - \text{AtMost}(K-1)$. 
*   **Common Pitfalls:** Trying to maintain 3 pointers in a single pass. While a 3-pointer sliding window *does* exist, it's highly error-prone under interview pressure. The `AtMost(K)` mathematical abstraction is much cleaner.
*   **Pattern Recognition:** "Subarrays with EXACTLY K properties" $\to$ `AtMost(K) - AtMost(K-1)`.

### 3. The Proposed Idea (Base Optimal Solution)
*   Write a helper function `atMostK(int[] nums, int k)` that returns the number of subarrays with *at most* `k` distinct integers.
*   In `atMostK`:
    *   Use a sliding window `[left, right]`.
    *   Use an integer array `freq` (since $nums[i] \le N$) to track frequencies.
    *   Expand `right`. If `freq[nums[right]] == 0`, increment a `distinctCount`.
    *   If `distinctCount > k`, shrink from `left` until `distinctCount == k`.
    *   The number of valid subarrays ending at `right` is precisely `right - left + 1`. Add this to the total.
*   **Time Complexity:** $O(N)$. `atMostK` does a standard sliding window $O(N)$. We call it twice. Total time $O(N)$.
*   **Space Complexity:** $O(N)$. The frequency array requires space proportional to the maximum value in `nums`.

### 4. Production-Ready Java Implementation (Base)
```java
class Solution {
    public int subarraysWithKDistinct(int[] nums, int k) {
        // The mathematical trick: Exact(K) = AtMost(K) - AtMost(K-1)
        return subarraysWithAtMostK(nums, k) - subarraysWithAtMostK(nums, k - 1);
    }
    
    private int subarraysWithAtMostK(int[] nums, int k) {
        if (k == 0) return 0; // Edge case optimization
        
        int n = nums.length;
        // Frequency array is faster than HashMap because nums[i] <= nums.length
        int[] freq = new int[n + 1]; 
        
        int left = 0;
        int distinctCount = 0;
        int totalSubarrays = 0;
        
        for (int right = 0; right < n; right++) {
            // Include right element in window
            if (freq[nums[right]] == 0) {
                distinctCount++;
            }
            freq[nums[right]]++;
            
            // Shrink window if we exceed k distinct elements
            while (distinctCount > k) {
                freq[nums[left]]--;
                if (freq[nums[left]] == 0) {
                    distinctCount--;
                }
                left++;
            }
            
            // All subarrays ending at 'right' and starting anywhere from 'left' to 'right' are valid
            totalSubarrays += (right - left + 1);
        }
        
        return totalSubarrays;
    }
}
```

### 5. Edge Cases & Dry-Run Checklist
*   **Edge Cases Handled:**
    *   `k = 0`: Handled beautifully. `atMostK(nums, 0)` immediately returns 0.
    *   Array with all same elements `[1,1,1]`, `k=1`: `atMostK(1)` gives 6, `atMostK(0)` gives 0. Total 6.
    *   `k` larger than distinct elements available: `atMostK(k)` processes entire array without shrinking.
*   **Dry-Run Checklist:**
    *   Track `left`, `right`.
    *   Track `distinctCount`.
    *   Verify the `totalSubarrays += (right - left + 1)` accumulation logic.

### 6. The Google Follow-Up Gauntlet

*   **Follow-Up 1: The Twist - Avoid Multi-Pass (One Pass Sliding Window)**
    *   *The Twist:* The interviewer bans calling `atMostK` twice. They want a strict 1-pass solution (perhaps the stream is unbuffered).
    *   *The Architectural Pivot:* We must use a 3-pointer sliding window. We maintain `right`, `left1` (tracks the longest window with exactly $K$ distinct elements), and `left2` (tracks the shortest window with exactly $K$ distinct elements). The number of valid subarrays ending at `right` is `left2 - left1`.
    *   *The Follow-Up Solution (Java snippet):*
        ```java
        // Requires two frequency maps and maintaining two left boundaries in one loop.
        // It is incredibly tedious but proves mastery of the window state.
        ```

---

## 5. LC 432: All O(1) Data Structure (Hard)

### 1. The Full Problem Specification
*   **Problem Statement:** Design a data structure to store strings with their count with $O(1)$ time complexity for all operations:
    *   `inc(String key)`: Increments count of key by 1.
    *   `dec(String key)`: Decrements count of key by 1. Removes key if count reaches 0.
    *   `getMaxKey()`: Returns any key with the max count.
    *   `getMinKey()`: Returns any key with the min count.
*   **Input & Output Examples:**
    *   `inc("hello"), inc("hello"), getMaxKey() -> "hello", getMinKey() -> "hello", inc("leet"), getMaxKey() -> "hello", getMinKey() -> "leet"`.

### 2. The Core Insights & Tricks
*   **The "Aha!" Moment:** To achieve $O(1)$ for min and max, we need a Doubly Linked List where nodes are kept strictly sorted by frequency. Because counts only change by `+1` or `-1`, a key will only ever move one node to the left or right in this linked list. 
*   **Common Pitfalls:** Using a TreeMap gives $O(\log N)$. Using a Priority Queue gives $O(\log N)$. 
*   **Pattern Recognition:** $O(1)$ dynamic ordering $\to$ Doubly Linked List of Frequency Buckets + HashMap pointing to the buckets (like LFU Cache).

### 3. The Proposed Idea (Base Optimal Solution)
*   **Structures:**
    *   `BucketNode`: A node in a doubly linked list containing an integer `count` and a `HashSet<String>` of keys that have this count.
    *   `HashMap<String, BucketNode> keyMap`: Maps a string to its current `BucketNode`.
    *   A Doubly Linked List (`head` and `tail` dummy nodes) to keep `BucketNodes` sorted by `count`.
*   **Logic (`inc`):** 
    *   If key exists, find its node. We need to move it to a node with `count + 1`. If the next node's count isn't `count + 1`, insert a new node.
    *   Move key to the new node, remove from old. If old node's set is empty, remove old node from DLL.
*   **Logic (`getMax`/`getMin`):**
    *   Max is just `tail.prev.keys.iterator().next()`. Min is `head.next.keys.iterator().next()`. $O(1)$ time.
*   **Time Complexity:** $O(1)$ for all operations.
*   **Space Complexity:** $O(U)$ where $U$ is unique keys.

### 4. Production-Ready Java Implementation (Base)
```java
class AllOne {
    private class Bucket {
        int count;
        Set<String> keys;
        Bucket prev, next;
        
        public Bucket(int count) {
            this.count = count;
            this.keys = new HashSet<>();
        }
    }
    
    private Bucket head;
    private Bucket tail;
    private Map<String, Bucket> keyMap;
    
    public AllOne() {
        head = new Bucket(0); // Dummy head
        tail = new Bucket(0); // Dummy tail
        head.next = tail;
        tail.prev = head;
        keyMap = new HashMap<>();
    }
    
    public void inc(String key) {
        if (keyMap.containsKey(key)) {
            Bucket currentBucket = keyMap.get(key);
            Bucket nextBucket = currentBucket.next;
            
            // Create a new bucket if the next one isn't exactly count + 1
            if (nextBucket == tail || nextBucket.count != currentBucket.count + 1) {
                Bucket newBucket = new Bucket(currentBucket.count + 1);
                insertBucketAfter(newBucket, currentBucket);
                nextBucket = newBucket;
            }
            
            nextBucket.keys.add(key);
            keyMap.put(key, nextBucket);
            
            // Clean up the old bucket
            currentBucket.keys.remove(key);
            if (currentBucket.keys.isEmpty()) {
                removeBucket(currentBucket);
            }
        } else {
            // New key, goes into bucket with count 1
            Bucket firstBucket = head.next;
            if (firstBucket == tail || firstBucket.count != 1) {
                Bucket newBucket = new Bucket(1);
                insertBucketAfter(newBucket, head);
                firstBucket = newBucket;
            }
            firstBucket.keys.add(key);
            keyMap.put(key, firstBucket);
        }
    }
    
    public void dec(String key) {
        if (!keyMap.containsKey(key)) return;
        
        Bucket currentBucket = keyMap.get(key);
        
        if (currentBucket.count == 1) {
            keyMap.remove(key);
        } else {
            Bucket prevBucket = currentBucket.prev;
            if (prevBucket == head || prevBucket.count != currentBucket.count - 1) {
                Bucket newBucket = new Bucket(currentBucket.count - 1);
                insertBucketAfter(newBucket, currentBucket.prev);
                prevBucket = newBucket;
            }
            prevBucket.keys.add(key);
            keyMap.put(key, prevBucket);
        }
        
        currentBucket.keys.remove(key);
        if (currentBucket.keys.isEmpty()) {
            removeBucket(currentBucket);
        }
    }
    
    public String getMaxKey() {
        if (tail.prev == head) return "";
        return tail.prev.keys.iterator().next(); // O(1) fetch
    }
    
    public String getMinKey() {
        if (head.next == tail) return "";
        return head.next.keys.iterator().next(); // O(1) fetch
    }
    
    // DLL Helper methods
    private void insertBucketAfter(Bucket newBucket, Bucket prevBucket) {
        newBucket.prev = prevBucket;
        newBucket.next = prevBucket.next;
        prevBucket.next.prev = newBucket;
        prevBucket.next = newBucket;
    }
    
    private void removeBucket(Bucket bucket) {
        bucket.prev.next = bucket.next;
        bucket.next.prev = bucket.prev;
    }
}
```

### 5. Edge Cases & Dry-Run Checklist
*   **Edge Cases Handled:**
    *   `getMaxKey` when empty: Handled by checking if `head.next == tail`, returning `""`.
    *   Deleting the last item in a bucket: `removeBucket()` properly splices out the node.
    *   Incrementing from 1 to 2 when no bucket '2' exists: Properly inserts between '1' and whatever is next.
*   **Dry-Run Checklist:**
    *   Trace the `prev` and `next` pointers during `insertBucketAfter` and `removeBucket`. Missing a pointer update causes silent linked list breaks.

### 6. The Google Follow-Up Gauntlet

*   **Follow-Up 1: The Twist - Return a List of All Max Keys**
    *   *The Twist:* Instead of returning a single `String`, `getMaxKeys()` must return `List<String>`.
    *   *The Architectural Pivot:* Trivial. Just return `new ArrayList<>(tail.prev.keys)`. However, if the list is huge, this is $O(M)$ where $M$ is the number of tied max keys. If they demand an $O(1)$ *iterator* instead, return an iterator over the set.
*   **Follow-Up 2: The Twist - Increment by Random Amount**
    *   *The Twist:* The function signature changes to `add(String key, int amount)`. Amount can be large (e.g., +100).
    *   *The Architectural Pivot:* We lose $O(1)$ time. If a key jumps from count 5 to 105, we cannot simply step to `next`. We must binary search for the correct bucket insertion point. This pivots the data structure to a `TreeMap<Integer, Set<String>>` + `HashMap`, yielding $O(\log N)$ time. $O(1)$ is mathematically impossible with arbitrary jump amounts.

---

## 6. LC 855: Exam Room (Medium/Hard)

### 1. The Full Problem Specification
*   **Problem Statement:** In an exam room with `n` seats in a row (0 to n-1), students enter and leave. When a student enters, they must sit in the seat that maximizes the distance to the closest person. If there are multiple such seats, they sit in the seat with the lowest index. No one is in the room initially.
    *   `int seat()`: Returns the seat the student sits in.
    *   `void leave(int p)`: Student in seat `p` leaves.
    *   Constraints: $1 \le n \le 10^9$. Number of calls $\le 10^4$.
*   **Input & Output Examples:**
    *   `ExamRoom(10)`, `seat() -> 0`, `seat() -> 9` (Max distance 9), `seat() -> 4` (Distance to 0 is 4, distance to 9 is 5), `seat() -> 2`.

### 2. The Core Insights & Tricks
*   **The "Aha!" Moment:** Because $n$ is up to $10^9$, we cannot use an array to track seats! The state must be defined by the **Intervals** between occupied seats. A student will sit in the exact middle of an interval `[left, right]`. The seat is `left + (right - left) / 2`. 
*   **Common Pitfalls:** Using a boolean array ($O(N)$ space and $O(N)$ time per seat, will TLE and Memory Limit Exceed on $10^9$). Trying to use a Priority Queue of intervals leads to issues with the `leave` function because removing a specific interval from a Java PriorityQueue is $O(K)$. 
*   **Pattern Recognition:** Dynamic max gaps on a massive sparse line $\to$ `TreeSet` of occupied seats OR `TreeSet` of custom Interval objects.

### 3. The Proposed Idea (Base Optimal Solution)
*   We use a `TreeSet<Integer> students` to keep the currently occupied seats in sorted order.
*   **Logic (`seat()`):**
    *   If `TreeSet` is empty, sit at `0`.
    *   Iterate through adjacent pairs of students in the `TreeSet` (since the max number of students at any time is $10^4$, iterating is $O(S)$ where $S$ is current students. Wait, can we do better? Yes, with a custom interval PQ, but $O(S)$ is well within limits for $10^4$ calls and is drastically easier to implement bug-free).
    *   Check distance from 0 to the first student.
    *   Check distance between each adjacent pair: `(right - left) / 2`.
    *   Check distance from the last student to `n - 1`.
    *   Track the max distance and the corresponding seat.
    *   Insert the chosen seat into the `TreeSet` ($O(\log S)$).
*   **Logic (`leave()`):**
    *   Simply `students.remove(p)`. ($O(\log S)$ time).
*   **Time Complexity:** `seat()` is $O(S)$ where $S$ is number of seated students. `leave()` is $O(\log S)$.
*   **Space Complexity:** $O(S)$ for the TreeSet.

### 4. Production-Ready Java Implementation (Base)
```java
class ExamRoom {
    private final int n;
    private final TreeSet<Integer> students;

    public ExamRoom(int n) {
        this.n = n;
        this.students = new TreeSet<>();
    }
    
    public int seat() {
        // Base case: Room is empty
        if (students.isEmpty()) {
            students.add(0);
            return 0;
        }
        
        int maxDist = students.first(); // Distance from 0 to first student
        int bestSeat = 0;
        
        int prevStudent = -1;
        // Iterate through all seated students to find the largest gap
        for (int student : students) {
            if (prevStudent != -1) {
                // Calculate distance if we sit in the exact middle
                int dist = (student - prevStudent) / 2;
                // If this distance is strictly greater than maxDist, we update.
                // We use strict > to enforce the "lowest index" tie-breaker rule
                if (dist > maxDist) {
                    maxDist = dist;
                    bestSeat = prevStudent + dist;
                }
            }
            prevStudent = student;
        }
        
        // Edge case: Distance from last student to the end of the row
        int distToLast = n - 1 - students.last();
        if (distToLast > maxDist) {
            bestSeat = n - 1;
        }
        
        students.add(bestSeat);
        return bestSeat;
    }
    
    public void leave(int p) {
        students.remove(p);
    }
}
```

### 5. Edge Cases & Dry-Run Checklist
*   **Edge Cases Handled:**
    *   Empty room: explicitly caught.
    *   Sitting at boundaries: Initializing `maxDist = students.first()` accounts for sitting at index 0. The check at the end accounts for `n-1`.
    *   Tie-breaking: The strict `>` ensures we only update `bestSeat` if we find a strictly larger gap, preserving the smaller index naturally because we iterate from left to right.
*   **Dry-Run Checklist:**
    *   Calculate `dist = (right - left) / 2`. E.g., `(4 - 0) / 2 = 2`. Seat is `0 + 2 = 2`.
    *   Ensure boundaries `0` and `n-1` don't divide by 2, they are absolute distances!

### 6. The Google Follow-Up Gauntlet

*   **Follow-Up 1: The Twist - Optimize `seat()` to $O(\log S)$**
    *   *The Twist:* The interviewer says $S$ is now $10^6$. $O(S)$ per `seat()` call will TLE.
    *   *The Architectural Pivot:* We must use a `TreeSet` of **Intervals**. We sort intervals first by max distance, then by lowest starting index.
        *   When someone seats, we pull the max interval $O(\log S)$, split it into two new intervals, and put them back $O(\log S)$.
        *   When someone leaves, we must merge the two adjacent intervals. This requires maintaining a secondary data structure (HashMap of Start Indices and End Indices to quickly find adjacent intervals to delete and merge).
    *   *The Follow-Up Solution (Java Architectural Snippet):*
        ```java
        // Requires deep OOP design:
        class Interval {
            int left, right;
            int dist;
            public Interval(int l, int r) {
                left = l; right = r;
                if (l == -1) dist = r;
                else if (r == n) dist = n - 1 - l;
                else dist = (r - l) / 2;
            }
        }
        // TreeSet<Interval> ordered by dist DESC, then left ASC.
        // seat(): poll max Interval, split, add two new, return midpoint.
        // leave(): Requires O(log S) removal and merging of neighboring intervals.
        ```

## 7. LC 355: Design Twitter (Medium/Hard depending on scale)

### 1. The Full Problem Specification
*   **Problem Statement:** Design a simplified version of Twitter where users can post tweets, follow/unfollow another user, and is able to see the 10 most recent tweets in the user's news feed.
    *   `postTweet(userId, tweetId)`: Composes a new tweet.
    *   `getNewsFeed(userId)`: Retrieves the 10 most recent tweet IDs in the user's news feed. Each item must be posted by users who the user followed or by the user themself.
    *   `follow(followerId, followeeId)`: Follower follows a followee.
    *   `unfollow(followerId, followeeId)`: Follower unfollows a followee.
*   **Input & Output Examples:**
    *   `Twitter()`, `postTweet(1, 5)`, `getNewsFeed(1) -> [5]`, `follow(1, 2)`, `postTweet(2, 6)`, `getNewsFeed(1) -> [6, 5]`.

### 2. The Core Insights & Tricks
*   **The "Aha!" Moment:** `getNewsFeed` is exactly the **"Merge K Sorted Lists"** problem. Each user's tweets form a reverse-chronologically sorted list. To get the top 10 tweets among a user's network, we take the head of each user's tweet list and put them in a Max-Heap (PriorityQueue) sorted by timestamp. 
*   **Common Pitfalls:** Storing a massive global list of all tweets and filtering them on `getNewsFeed`. This gives $O(T)$ where $T$ is total tweets on the platform, which will instantly fail at Twitter scale.
*   **Pattern Recognition:** Merging streams of chronological events $\to$ $K$-way Merge with Max-Heap.

### 3. The Proposed Idea (Base Optimal Solution)
*   **Structures:**
    *   `Tweet` class: A Linked List node containing `id`, `timestamp`, and `next` pointer.
    *   `User` class: Contains a `HashSet` of `followed` user IDs and a pointer to the `head` of their `Tweet` list.
    *   A global `HashMap<Integer, User>` to store all users.
    *   A global atomic `timestamp` counter.
*   **Logic (`postTweet`):**
    *   Get or create the `User`. Create a new `Tweet` and prepend it to the user's `Tweet` linked list.
*   **Logic (`follow`/`unfollow`):**
    *   Add/remove `followeeId` from the user's `followed` set.
*   **Logic (`getNewsFeed`):**
    *   Create a Max-Heap of `Tweet` nodes ordered by `timestamp` descending.
    *   Push the `head` tweet of the user and the `head` tweet of everyone they follow into the heap.
    *   Poll the max tweet 10 times. Every time you poll a tweet, if it has a `next` tweet, push the `next` tweet into the heap.
*   **Time Complexity:** `postTweet`/`follow`/`unfollow` are $O(1)$. `getNewsFeed` is $O(F + 10 \log F)$ where $F$ is the number of followees (to build initial heap and poll 10 times).
*   **Space Complexity:** $O(U + T)$ where $U$ is users and $T$ is total tweets.

### 4. Production-Ready Java Implementation (Base)
```java
class Twitter {
    private static int globalTimestamp = 0;
    
    private class Tweet {
        int id;
        int time;
        Tweet next;
        
        public Tweet(int id) {
            this.id = id;
            this.time = globalTimestamp++; // Increment atomically logically
            this.next = null;
        }
    }
    
    private class User {
        int id;
        Set<Integer> followed;
        Tweet tweetHead;
        
        public User(int id) {
            this.id = id;
            this.followed = new HashSet<>();
            follow(id); // User always follows themselves
            this.tweetHead = null;
        }
        
        public void follow(int id) {
            followed.add(id);
        }
        
        public void unfollow(int id) {
            // Cannot unfollow yourself
            if (id != this.id) {
                followed.remove(id);
            }
        }
        
        public void post(int id) {
            Tweet t = new Tweet(id);
            t.next = tweetHead;
            tweetHead = t;
        }
    }
    
    private Map<Integer, User> userMap;

    public Twitter() {
        userMap = new HashMap<>();
    }
    
    public void postTweet(int userId, int tweetId) {
        userMap.putIfAbsent(userId, new User(userId));
        userMap.get(userId).post(tweetId);
    }
    
    public void follow(int followerId, int followeeId) {
        userMap.putIfAbsent(followerId, new User(followerId));
        userMap.putIfAbsent(followeeId, new User(followeeId));
        userMap.get(followerId).follow(followeeId);
    }
    
    public void unfollow(int followerId, int followeeId) {
        if (!userMap.containsKey(followerId) || followerId == followeeId) return;
        userMap.get(followerId).unfollow(followeeId);
    }
    
    public List<Integer> getNewsFeed(int userId) {
        List<Integer> feed = new ArrayList<>();
        if (!userMap.containsKey(userId)) return feed;
        
        Set<Integer> usersToPull = userMap.get(userId).followed;
        // Max-heap based on timestamp
        PriorityQueue<Tweet> maxHeap = new PriorityQueue<>((a, b) -> b.time - a.time);
        
        // Add the head of each user's tweet list to the heap
        for (int followedUser : usersToPull) {
            Tweet t = userMap.get(followedUser).tweetHead;
            if (t != null) {
                maxHeap.add(t);
            }
        }
        
        // Pull the 10 most recent tweets
        int n = 0;
        while (!maxHeap.isEmpty() && n < 10) {
            Tweet t = maxHeap.poll();
            feed.add(t.id);
            n++;
            if (t.next != null) {
                maxHeap.add(t.next);
            }
        }
        
        return feed;
    }
}
```

### 5. Edge Cases & Dry-Run Checklist
*   **Edge Cases Handled:**
    *   Unfollowing yourself: Guarded explicitly (`if (id != this.id)`).
    *   `getNewsFeed` for non-existent user: Returns empty list.
    *   Following/Unfollowing non-existent users: Handles elegantly with `putIfAbsent`.
*   **Dry-Run Checklist:**
    *   Track `globalTimestamp` increment logic.
    *   Ensure the PriorityQueue comparator is `b.time - a.time` for a Max-Heap.

### 6. The Google Follow-Up Gauntlet

*   **Follow-Up 1: The Twist - The Celebrity Problem (Push vs Pull)**
    *   *The Twist:* Justin Bieber has 100 million followers. When he calls `postTweet`, it's fast. But when his 100M followers call `getNewsFeed`, they all have to query his list. This causes a read bottleneck (The "Pull" model).
    *   *The Architectural Pivot:* Hybrid Push/Pull model. 
        *   For normal users, we use **Push**: When they tweet, we push the tweet into a pre-computed `news_feed` array/cache for every follower. `getNewsFeed` is now $O(1)$.
        *   For celebrities, we use **Pull**: We *don't* push their tweets to 100M followers. Instead, when a follower calls `getNewsFeed`, they fetch their pre-computed cache, and then merge it *only* with the celebrities they follow.
*   **Follow-Up 2: The Twist - Pagination**
    *   *The Twist:* The user wants to scroll infinitely, not just see the top 10. `getNewsFeed(userId, pageToken)`.
    *   *The Architectural Pivot:* We cannot just re-run the K-way merge and discard the first $N$ tweets. We must return a `pageToken` that serializes the state of the Priority Queue (e.g., the exact pointers of the tweets currently at the frontier of the K-way merge) so the next call can resume exactly where it left off.

---

## 8. LC 146: LRU Cache (Medium - Absolute Must Know)

### 1. The Full Problem Specification
*   **Problem Statement:** Design a data structure that follows the constraints of a Least Recently Used (LRU) cache.
    *   `LRUCache(int capacity)`: Initialize with positive capacity.
    *   `int get(int key)`: Return value if exists, else -1. Updates the key to be most recently used.
    *   `void put(int key, int value)`: Update value if key exists (and update to most recent). Otherwise, insert. If capacity is exceeded, evict the least recently used key.
*   **Input & Output Examples:**
    *   `capacity = 2`. `put(1,1), put(2,2), get(1)->1, put(3,3)` (evicts 2), `get(2)->-1`, `put(4,4)` (evicts 1), `get(1)->-1, get(3)->3, get(4)->4`.

### 2. The Core Insights & Tricks
*   **The "Aha!" Moment:** To get $O(1)$ lookup, we need a HashMap. To get $O(1)$ eviction and MRU updates, we need an ordered sequence where we can instantly move a node to the front. Only a **Doubly Linked List** allows $O(1)$ node relocation given the node pointer.
*   **Common Pitfalls:** Using `LinkedHashMap` in Java (banned in interviews). Using a Singly Linked List (requires $O(N)$ to find the previous node for deletion).
*   **Pattern Recognition:** $O(1)$ Eviction based on time $\to$ HashMap + Doubly Linked List.

### 3. The Proposed Idea (Base Optimal Solution)
*   **Structures:**
    *   `Node`: Doubly linked list node `key`, `value`, `prev`, `next`. We MUST store `key` in the node so that when we pop the tail (LRU), we know which key to delete from the HashMap!
    *   `HashMap<Integer, Node>` maps key to its location in the DLL.
    *   Dummy `head` (Most Recent) and `tail` (Least Recent) nodes to avoid null pointer edge cases during splicing.
*   **Logic:**
    *   `get`: If in map, move node to right after `head`. Return value.
    *   `put`: If in map, update value, move to right after `head`. If not, create node, add right after `head`. If size > capacity, remove node right before `tail` from DLL, and remove its `key` from HashMap.
*   **Time & Space:** Time $O(1)$ all operations. Space $O(\text{capacity})$.

### 4. Production-Ready Java Implementation (Base)
```java
class LRUCache {
    class Node {
        int key;
        int value;
        Node prev;
        Node next;
        public Node(int key, int value) {
            this.key = key;
            this.value = value;
        }
    }
    
    private final int capacity;
    private final Map<Integer, Node> cache;
    private final Node head;
    private final Node tail;
    
    public LRUCache(int capacity) {
        this.capacity = capacity;
        this.cache = new HashMap<>();
        this.head = new Node(-1, -1);
        this.tail = new Node(-1, -1);
        head.next = tail;
        tail.prev = head;
    }
    
    public int get(int key) {
        if (!cache.containsKey(key)) return -1;
        Node node = cache.get(key);
        moveToHead(node);
        return node.value;
    }
    
    public void put(int key, int value) {
        if (cache.containsKey(key)) {
            Node node = cache.get(key);
            node.value = value;
            moveToHead(node);
        } else {
            Node newNode = new Node(key, value);
            cache.put(key, newNode);
            addNode(newNode);
            
            if (cache.size() > capacity) {
                Node tailNode = popTail();
                cache.remove(tailNode.key); // This is why we store key in Node
            }
        }
    }
    
    // DLL Helper Methods
    private void addNode(Node node) {
        node.prev = head;
        node.next = head.next;
        head.next.prev = node;
        head.next = node;
    }
    
    private void removeNode(Node node) {
        Node prevNode = node.prev;
        Node nextNode = node.next;
        prevNode.next = nextNode;
        nextNode.prev = prevNode;
    }
    
    private void moveToHead(Node node) {
        removeNode(node);
        addNode(node);
    }
    
    private Node popTail() {
        Node res = tail.prev;
        removeNode(res);
        return res;
    }
}
```

### 5. Edge Cases & Dry-Run Checklist
*   **Edge Cases Handled:**
    *   Updating an existing key doesn't increase size but updates MRU status.
    *   Capacity of 1: Handled perfectly by dummy head/tail.
*   **Dry-Run Checklist:**
    *   Trace dummy head/tail pointers closely. 99% of failures are `NullPointerException`s in `removeNode`.

### 6. The Google Follow-Up Gauntlet

*   **Follow-Up 1: The Twist - Concurrent Access (Thread-Safe LRU)**
    *   *The Twist:* The cache is accessed by 1000 threads simultaneously. Make it thread-safe without bottlenecking.
    *   *The Architectural Pivot:* Putting `synchronized` on the methods makes the whole cache a bottleneck. We must use a `ConcurrentHashMap`. But the DLL is still not thread-safe! The standard solution is **Lock Striping** (like Guava Cache): create an array of `N` independent LRU Caches (segments), and use `hash(key) % N` to route to a specific cache lock. This allows $N$ concurrent writes.
*   **Follow-Up 2: The Twist - TTL (Time To Live)**
    *   *The Twist:* Elements expire after 5 seconds, regardless of capacity.
    *   *The Architectural Pivot:* Add `timestamp` to `Node`. We cannot lazily check expiration only on `get` because a full cache of expired items will prevent new insertions. We need a background cleanup thread, OR a PriorityQueue based on expiration time to quickly clean up expired keys during `put`.

---

## 9. LC 460: LFU Cache (Hard)

### 1. The Full Problem Specification
*   **Problem Statement:** Design a Least Frequently Used (LFU) cache. 
    *   Evict the least frequently used key. If there is a tie, evict the least recently used key among them.
*   **Input & Output Examples:**
    *   `capacity = 2`. `put(1,1)`, `put(2,2)`, `get(1)->1` (freq 1=2), `put(3,3)` (evicts 2, since freq 2=1, freq 1=2, freq 3=1 but 2 is older), `get(2)->-1`.

### 2. The Core Insights & Tricks
*   **The "Aha!" Moment:** We need to group elements by frequency. Each frequency needs its own LRU Cache! We maintain a `minFreq` variable so we know which frequency bucket to evict from in $O(1)$ time. 
*   **Common Pitfalls:** Trying to maintain a single DLL sorted by frequency and time. This forces $O(N)$ insertion times.
*   **Pattern Recognition:** $O(1)$ Frequency + Time Eviction $\to$ Dual HashMaps (Key to Val, Freq to LRU Cache).

### 3. The Proposed Idea (Base Optimal Solution)
*   **Structures:**
    *   `HashMap<Integer, Integer> keyToVal`
    *   `HashMap<Integer, Integer> keyToFreq`
    *   `HashMap<Integer, LinkedHashSet<Integer>> freqToLRU`: Maps a frequency to an ordered set of keys (Java's `LinkedHashSet` is an LRU cache without capacity, it gives $O(1)$ add, remove, and get-first). In a strict interview, you build a custom DLL for this.
    *   `minFreq` integer variable.
*   **Logic (`put`):**
    *   If key exists, update value, increment its frequency.
    *   If key doesn't exist:
        *   If at capacity, find the `LinkedHashSet` for `minFreq`. Remove its first element (the LRU). Delete from `keyToVal` and `keyToFreq`.
        *   Add new key to `keyToVal`, set `keyToFreq` to 1. Add to `freqToLRU.get(1)`. Set `minFreq = 1`.
*   **Logic (Increment Freq Helper):**
    *   Get old freq. Remove key from `freqToLRU.get(oldFreq)`.
    *   **Crucial step:** If `oldFreq == minFreq` and that bucket is now empty, `minFreq++`!
    *   Add key to `freqToLRU.get(oldFreq + 1)`.

### 4. Production-Ready Java Implementation (Base)
```java
class LFUCache {
    private final int capacity;
    private int minFreq;
    private Map<Integer, Integer> keyToVal;
    private Map<Integer, Integer> keyToFreq;
    // frequency -> Set of keys (LinkedHashSet preserves insertion order, serving as LRU)
    private Map<Integer, LinkedHashSet<Integer>> freqToLRU;

    public LFUCache(int capacity) {
        this.capacity = capacity;
        this.minFreq = 0;
        this.keyToVal = new HashMap<>();
        this.keyToFreq = new HashMap<>();
        this.freqToLRU = new HashMap<>();
    }
    
    public int get(int key) {
        if (!keyToVal.containsKey(key)) return -1;
        
        int value = keyToVal.get(key);
        updateFreq(key);
        return value;
    }
    
    public void put(int key, int value) {
        if (capacity == 0) return;
        
        if (keyToVal.containsKey(key)) {
            keyToVal.put(key, value);
            updateFreq(key);
            return;
        }
        
        // Eviction logic
        if (keyToVal.size() >= capacity) {
            LinkedHashSet<Integer> keysAtMinFreq = freqToLRU.get(minFreq);
            int keyToEvict = keysAtMinFreq.iterator().next(); // First element is LRU
            keysAtMinFreq.remove(keyToEvict);
            keyToVal.remove(keyToEvict);
            keyToFreq.remove(keyToEvict);
        }
        
        // Add new key
        keyToVal.put(key, value);
        keyToFreq.put(key, 1);
        freqToLRU.putIfAbsent(1, new LinkedHashSet<>());
        freqToLRU.get(1).add(key);
        minFreq = 1; // New key resets min frequency to 1
    }
    
    private void updateFreq(int key) {
        int oldFreq = keyToFreq.get(key);
        int newFreq = oldFreq + 1;
        keyToFreq.put(key, newFreq);
        
        freqToLRU.get(oldFreq).remove(key);
        
        // If the old frequency bucket was the minFreq and is now empty, increment minFreq
        if (oldFreq == minFreq && freqToLRU.get(oldFreq).isEmpty()) {
            minFreq++;
        }
        
        freqToLRU.putIfAbsent(newFreq, new LinkedHashSet<>());
        freqToLRU.get(newFreq).add(key);
    }
}
```

### 5. Edge Cases & Dry-Run Checklist
*   **Edge Cases Handled:**
    *   `capacity = 0`: Checked at the top of `put`.
    *   Updating existing keys: correctly doesn't trigger eviction, correctly increments freq.
*   **Dry-Run Checklist:**
    *   Track `minFreq`. When a key goes from `freq=1` to `freq=2`, does `minFreq` update if `freq=1` bucket is empty? Yes.

### 6. The Google Follow-Up Gauntlet

*   **Follow-Up 1: The Twist - Strict $O(1)$ without standard libraries**
    *   *The Twist:* You cannot use `LinkedHashSet`. 
    *   *The Architectural Pivot:* You must implement the Doubly Linked List manually. You will have `Map<Integer, Node> cache` and `Map<Integer, DoublyLinkedList> freqMap`. 
*   **Follow-Up 2: The Twist - W-TinyLFU (Real World Caching)**
    *   *The Twist:* The interviewer points out that standard LFU suffers from "Cache Pollution" (a key accessed 100 times yesterday stays forever, blocking new keys today). How do you fix this?
    *   *The Architectural Pivot:* Explain Window-TinyLFU (used in Caffeine cache). Maintain a small LRU "Window" cache for new items, and a larger main cache. Items in the main cache have their frequencies periodically halved (decay) so old items eventually die out. Use a Count-Min Sketch for memory-efficient frequency tracking instead of exact HashMaps. This demonstrates Staff-level systems knowledge.

---

# Category B: Advanced Binary Search & Space Optimization

## 10. LC 410: Split Array Largest Sum (Hard)

### 1. The Full Problem Specification
*   **Problem Statement:** Given an integer array `nums` and an integer `k`, split `nums` into `k` non-empty continuous subarrays such that the largest sum among these `k` subarrays is minimized. Return this minimized largest sum.
    *   Constraints: $1 \le nums.length \le 1000$, $0 \le nums[i] \le 10^6$, $1 \le k \le \min(50, nums.length)$.
*   **Input & Output Examples:**
    *   *Example 1:* `nums = [7,2,5,10,8], k = 2` $\to$ `Output: 18`. (Split into `[7,2,5]` sum=14 and `[10,8]` sum=18. Max is 18. Any other split has a larger max, e.g., `[7,2,5,10]` sum=24).
    *   *Example 2:* `nums = [1,2,3,4,5], k = 2` $\to$ `Output: 9`. (`[1,2,3]` sum=6, `[4,5]` sum=9).

### 2. The Core Insights & Tricks
*   **The "Aha!" Moment:** We are searching for an optimal *value* (the minimized largest sum), not an index. Notice the monotonicity: if we can split the array such that the max sum is $\le X$, then we can definitely do it for $X+1$. But if we *cannot* do it for $X-1$, then $X$ is our answer. This allows us to **Binary Search on the Answer**.
*   **Common Pitfalls:** Trying to solve this with Dynamic Programming ($O(k \cdot N^2)$). While DP is valid, $O(N \log S)$ (where $S$ is the sum of array) is drastically faster and expected at L5.
*   **Pattern Recognition:** "Minimize the Maximum" or "Maximize the Minimum" + continuous arrays $\to$ Binary Search on Answer space.

### 3. The Proposed Idea (Base Optimal Solution)
*   **Search Space:** The minimum possible answer is the maximum element in `nums` (if $k = N$, each element is its own subarray). The maximum possible answer is the sum of all elements in `nums` (if $k = 1$, the whole array is one subarray). Let `left = max(nums)` and `right = sum(nums)`.
*   **Binary Search:** We pick `mid = left + (right - left) / 2` as a candidate max sum.
*   **Greedy Validity Check (`canSplit`):** We iterate through `nums` greedily adding to a running sum. If adding `nums[i]` exceeds `mid`, we must start a new subarray. We count the number of subarrays needed. If `subarraysNeeded > k`, then `mid` is too small (it forced too many splits), so `left = mid + 1`. Otherwise, `mid` is possible, but we might be able to do better, so `right = mid`.
*   **Time Complexity:** $O(N \log S)$ where $S$ is the sum of `nums`. 
*   **Space Complexity:** $O(1)$ auxiliary space.

### 4. Production-Ready Java Implementation (Base)
```java
class Solution {
    public int splitArray(int[] nums, int k) {
        int left = 0;
        int right = 0;
        
        // Find boundaries for binary search
        for (int num : nums) {
            left = Math.max(left, num);
            right += num;
        }
        
        // Binary search on the answer
        while (left < right) {
            int mid = left + (right - left) / 2; // Potential minimized largest sum
            
            if (canSplit(nums, k, mid)) {
                // We can split it into k or fewer arrays with max sum <= mid
                // Try to find a smaller max sum
                right = mid;
            } else {
                // mid is too restrictive; it forces more than k splits
                left = mid + 1;
            }
        }
        
        return left;
    }
    
    // Greedy helper to check if we can form valid subarrays
    private boolean canSplit(int[] nums, int k, int maxSumAllowed) {
        int subarraysCount = 1;
        int currentSubarraySum = 0;
        
        for (int num : nums) {
            if (currentSubarraySum + num > maxSumAllowed) {
                // Must start a new subarray
                subarraysCount++;
                currentSubarraySum = num;
                if (subarraysCount > k) {
                    return false;
                }
            } else {
                currentSubarraySum += num;
            }
        }
        
        return true;
    }
}
```

### 5. Edge Cases & Dry-Run Checklist
*   **Edge Cases Handled:**
    *   `k = nums.length`: `left` starts at `max(nums)`. `canSplit` will evaluate `true` on the very first iteration where `mid == max(nums)`.
    *   `k = 1`: `right` is `sum(nums)`.
    *   Integer overflow: If `sum(nums)` exceeds `Integer.MAX_VALUE`, `right` and `mid` must be cast to `long`. (Given constraints $1000 \times 10^6 = 10^9$, `int` is perfectly safe here).
*   **Dry-Run Checklist:**
    *   Track `left` and `right`.
    *   Inside `canSplit`, ensure `subarraysCount` starts at `1` (not 0, because there is always at least one subarray).

### 6. The Google Follow-Up Gauntlet

*   **Follow-Up 1: The Twist - Add "Remove K Elements" Flexibility**
    *   *The Twist:* The interviewer asks: "What if you are allowed to remove up to $M$ elements from the array before splitting?"
    *   *The Architectural Pivot:* Binary Search on the answer still applies! However, the `canSplit` check changes. Inside `canSplit(mid)`, you use Dynamic Programming instead of a Greedy sweep. The state becomes `dp[i][j] = minimum splits for prefix i, having removed j elements`. 
*   **Follow-Up 2: The Twist - Distributed Processing (Disk Bound)**
    *   *The Twist:* The array is $10^{12}$ elements and stored on disk. You cannot load it into memory.
    *   *The Architectural Pivot:* Binary search still takes $O(\log S)$ steps. For each step, `canSplit` streams the data sequentially from disk. Because the greedy logic only needs `currentSubarraySum` and `num`, it operates in perfect $O(1)$ memory stream processing. The only change is reading from a file iterator instead of a memory array.

---

## 11. LC 774: Minimize Max Distance to Gas Station (Hard)

### 1. The Full Problem Specification
*   **Problem Statement:** You are given a sorted integer array `stations` representing gas station positions on a 1D line. You can add `k` new gas stations anywhere. Return the minimum possible value of the maximum distance between adjacent gas stations after adding the `k` stations. Answers within $10^{-6}$ of the true value are accepted.
    *   Constraints: $10 \le stations.length \le 2000$, $1 \le k \le 10^6$.
*   **Input & Output Examples:**
    *   `stations = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10], k = 9` $\to$ `Output: 0.50000`. (Place a station halfway between every existing station).

### 2. The Core Insights & Tricks
*   **The "Aha!" Moment:** Similar to LC 410, this is "Minimize the Maximum", but the search space is **continuous (floating-point)** rather than discrete. If a max gap of $X$ is achievable with $\le k$ stations, so is $X+0.1$. 
*   **Common Pitfalls:** Trying to use a Priority Queue (Max-Heap) to pick the largest gap and split it repeatedly. Splitting a gap of `10` once makes it `5, 5`. Splitting it again makes it `3.33, 3.33, 3.33`. This requires mathematically updating the priority queue which is error-prone, and with $K = 10^6$, $O(K \log N)$ will Time Limit Exceed. 
*   **Pattern Recognition:** Continuous search space + "Minimize max" $\to$ Binary Search on floating-point answer.

### 3. The Proposed Idea (Base Optimal Solution)
*   **Search Space:** Minimum gap is `0`. Maximum gap is `stations[n-1] - stations[0]`.
*   **Binary Search:** We pick a candidate gap `mid`. 
*   **Validity Check (`canForm`):** For every existing adjacent pair `gap = stations[i+1] - stations[i]`, if `gap > mid`, we must add stations between them. The number of stations needed is exactly `Math.ceil(gap / mid) - 1`. We sum the required stations across all gaps. If `required_stations <= k`, then `mid` is a valid maximum gap (we have enough `k` to enforce it).
*   **Precision Termination:** Instead of `while(left < right)`, for floats we use `while (right - left > 1e-6)`.
*   **Time Complexity:** $O(N \log(\text{MaxGap} / 10^{-6}))$. The binary search runs ~60 times.
*   **Space Complexity:** $O(1)$.

### 4. Production-Ready Java Implementation (Base)
```java
class Solution {
    public double minmaxGasDist(int[] stations, int k) {
        double left = 0;
        double right = stations[stations.length - 1] - stations[0];
        
        // Required precision is 10^-6
        while (right - left > 1e-6) {
            double mid = left + (right - left) / 2.0;
            
            if (isValid(stations, k, mid)) {
                // We can achieve a max gap of 'mid' using k or fewer stations
                // Let's try to find an even smaller max gap
                right = mid;
            } else {
                // 'mid' is too small; we'd need more than k stations
                left = mid;
            }
        }
        
        return left;
    }
    
    private boolean isValid(int[] stations, int k, double maxGap) {
        int requiredStations = 0;
        for (int i = 0; i < stations.length - 1; i++) {
            double distance = stations[i+1] - stations[i];
            // Calculate how many cuts are needed to make all pieces <= maxGap
            requiredStations += Math.ceil(distance / maxGap) - 1;
        }
        return requiredStations <= k;
    }
}
```

### 5. Edge Cases & Dry-Run Checklist
*   **Edge Cases Handled:**
    *   Floating point math: `Math.ceil(distance / maxGap) - 1` elegantly computes the required additions without precision rounding errors of integer casting.
*   **Dry-Run Checklist:**
    *   If `distance = 10, maxGap = 5`, `10/5 = 2.0`. `ceil(2.0) - 1 = 1`. 1 station is required. Correct.

### 6. The Google Follow-Up Gauntlet
*   **Follow-Up 1: The Twist - Integer Coordinates Only**
    *   *The Twist:* The new stations MUST be placed on integer coordinates. (This converts it to an entirely different problem, specifically "Minimize Maximum Gap with K integer cuts").
    *   *The Architectural Pivot:* Binary Search on the answer still works, but the state becomes discrete integers `left = 1`, `right = max_gap`. The required stations becomes `(distance - 1) / mid` using pure integer math. This makes the code even cleaner and prevents precision issues!
*   **Follow-Up 2: The Twist - Cost-Weighted Stations**
    *   *The Twist:* Adding a station at coordinate $X$ costs $f(X)$. You have a budget $B$ instead of count $k$.
    *   *The Architectural Pivot:* Binary search on Answer fails. The check function cannot greedily place stations because placement location now matters financially. This pivots to a DP approach or Dijkstra on a graph of possible cuts. 

## 12. LC 153 & LC 154: Find Minimum in Rotated Sorted Array I & II (Medium / Hard)

### 1. The Full Problem Specification
*   **Problem Statement:** Suppose an array of length `n` sorted in ascending order is rotated between `1` and `n` times. Given the sorted rotated array `nums`, return the minimum element of this array. You must write an algorithm that runs in $O(\log N)$ time. (LC 154 adds: The array may contain **duplicates**).
    *   Constraints: $n \le 5000$, $-5000 \le nums[i] \le 5000$.
*   **Input & Output Examples:**
    *   *Example 1 (Unique):* `nums = [3,4,5,1,2]` $\to$ `Output: 1`. (Rotated 3 times).
    *   *Example 2 (Duplicates):* `nums = [2,2,2,0,1,2]` $\to$ `Output: 0`.

### 2. The Core Insights & Tricks
*   **The "Aha!" Moment:** We compare `nums[mid]` with `nums[right]`. If `nums[mid] > nums[right]`, it means the pivot (minimum) MUST be to the right of `mid`. If `nums[mid] < nums[right]`, the right side is strictly sorted, so the minimum is at `mid` or to its left. 
*   **Common Pitfalls:** Comparing `nums[mid]` with `nums[left]`. This creates annoying edge cases when the array is NOT rotated (e.g., `[1, 2, 3]`). Comparing with `right` unconditionally solves all rotation bounds perfectly.
*   **The Duplicate Trap (LC 154):** If `nums[mid] == nums[right]`, we cannot know which half the minimum is in (e.g., `[1, 0, 1, 1, 1]` vs `[1, 1, 1, 0, 1]`). The *only* safe operation is `right--`, shrinking the bound linearly. This is the L5 discriminator.
*   **Pattern Recognition:** Sorted array + Rotated + $O(\log N)$ requirement $\to$ Binary Search comparing `mid` to `right`.

### 3. The Proposed Idea (Base Optimal Solution for LC 154 with Duplicates)
*   Initialize `left = 0`, `right = nums.length - 1`.
*   While `left < right`:
    *   `mid = left + (right - left) / 2`.
    *   If `nums[mid] > nums[right]`: Minimum is in the right unsorted half. `left = mid + 1`.
    *   Else if `nums[mid] < nums[right]`: Right half is sorted, min is `mid` or in the left half. `right = mid`.
    *   Else (`nums[mid] == nums[right]`): We don't know. But we know `nums[right]` is identical to `nums[mid]`, so we can safely drop `nums[right]` without losing the minimum value. `right--`.
*   **Time Complexity:** $O(\log N)$ on average for unique elements. Worst case $O(N)$ if all elements are identical (e.g., `[1,1,1,1]`).
*   **Space Complexity:** $O(1)$.

### 4. Production-Ready Java Implementation (Base)
```java
class Solution {
    public int findMin(int[] nums) {
        int left = 0;
        int right = nums.length - 1;
        
        while (left < right) {
            int mid = left + (right - left) / 2;
            
            if (nums[mid] > nums[right]) {
                // Min must be to the right of mid
                left = mid + 1;
            } else if (nums[mid] < nums[right]) {
                // Right side is perfectly sorted, min is mid or to the left
                right = mid;
            } else {
                // nums[mid] == nums[right], we cannot eliminate a half safely.
                // However, since they are equal, dropping right is safe.
                right--;
            }
        }
        
        // left == right, points to the minimum
        return nums[left];
    }
}
```

### 5. Edge Cases & Dry-Run Checklist
*   **Edge Cases Handled:**
    *   Not rotated at all (`[1, 2, 3]`): `mid=1`, `nums[1] < nums[2]`, `right=1`. `mid=0`, `nums[0] < nums[1]`, `right=0`. Returns `nums[0]`. Works flawlessly.
    *   All duplicates (`[2, 2, 2]`): `right` decrements by 1 each time until `left == right`. 
*   **Dry-Run Checklist:**
    *   Ensure the `while` loop condition is strictly `<` and not `<=`. If you use `<=`, `right = mid` will cause an infinite loop.

### 6. The Google Follow-Up Gauntlet

*   **Follow-Up 1: The Twist - Find Target (LC 33 / LC 81 Search in Rotated Sorted Array)**
    *   *The Twist:* Instead of finding the minimum, find a specific `target` integer.
    *   *The Architectural Pivot:* We must first determine *which side* is the sorted side.
        *   If left side is sorted (`nums[left] <= nums[mid]`): Check if `target` is in `[nums[left], nums[mid]]`. If yes, `right = mid - 1`. Else, `left = mid + 1`.
        *   If right side is sorted (`nums[mid] <= nums[right]`): Check if `target` is in `[nums[mid], nums[right]]`. If yes, `left = mid + 1`. Else, `right = mid - 1`.
        *   For duplicates, `nums[left] == nums[mid]` triggers `left++` to eliminate ambiguity.
*   **Follow-Up 2: The Twist - Minimum in an Implicit Infinite Rotated Stream**
    *   *The Twist:* The array is infinite, and you have an API `int get(index)`. The array is sorted, rotated, and periodically repeats (e.g., `...4, 5, 1, 2, 3, 4, 5, 1, 2, 3...`).
    *   *The Architectural Pivot:* First, use exponential backoff (checking index 1, 2, 4, 8, 16...) to find a bounding window `[left, right]` where a drop occurs (`get(right) < get(left)`). Once bounded, apply standard rotated binary search.

---

## 13. LC 1060: Missing Element in Sorted Array (Medium)

### 1. The Full Problem Specification
*   **Problem Statement:** Given an integer array `nums` which is sorted in strictly increasing order, and an integer `k`, return the `k`-th missing number starting from the leftmost number of the array.
    *   Constraints: $1 \le nums.length \le 5 \times 10^4$, $1 \le nums[i] \le 10^7$, $1 \le k \le 10^8$.
*   **Input & Output Examples:**
    *   *Example 1:* `nums = [4,7,9,10], k = 1` $\to$ `Output: 5`. (Missing numbers are 5, 6, 8. 1st missing is 5).
    *   *Example 2:* `nums = [4,7,9,10], k = 3` $\to$ `Output: 8`.
    *   *Example 3:* `nums = [1,2,4], k = 3` $\to$ `Output: 6`. (Missing are 3, 5, 6... 3rd missing is 6, which is outside the array bounds).

### 2. The Core Insights & Tricks
*   **The "Aha!" Moment:** To find how many numbers are missing between `nums[0]` and `nums[i]`, we use a simple formula: `Total Expected Numbers - Actual Numbers Present`. 
    *   Expected numbers in range `[nums[0], nums[i]]` is `nums[i] - nums[0] + 1`.
    *   Actual numbers present is `i + 1`.
    *   Therefore, `missing_count(i) = nums[i] - nums[0] - i`.
    *   Because `nums` is strictly increasing, `missing_count(i)` is monotonically increasing! Thus, we can **Binary Search** the indices to find the interval where the `k`-th missing number falls.
*   **Common Pitfalls:** A linear scan $O(N)$ is trivial but will fail Google's optimal bounds. Failing to handle the edge case where the missing number is *larger* than the maximum element in the array.
*   **Pattern Recognition:** Sorted Array + Searching for rank `K` $\to$ Implicit Binary Search.

### 3. The Proposed Idea (Base Optimal Solution)
*   Create a helper function `missingCount(index)` that returns `nums[index] - nums[0] - index`.
*   **Check right boundary:** If `k > missingCount(n - 1)`, the missing number is beyond the last element. The answer is simply `nums[n - 1] + (k - missingCount(n - 1))`.
*   **Binary Search:** We want to find the smallest index `left` such that `missingCount(left) >= k`. 
    *   `left = 0`, `right = n - 1`.
    *   `mid = left + (right - left) / 2`.
    *   If `missingCount(mid) < k`, the target is to the right: `left = mid + 1`.
    *   Else, target is `mid` or left: `right = mid`.
*   Once `left` is found, the answer is bounded between `nums[left - 1]` and `nums[left]`.
*   The exact number is `nums[left - 1] + k - missingCount(left - 1)`.
*   **Time Complexity:** $O(\log N)$.
*   **Space Complexity:** $O(1)$.

### 4. Production-Ready Java Implementation (Base)
```java
class Solution {
    public int missingElement(int[] nums, int k) {
        int n = nums.length;
        
        // Edge Case: The k-th missing number is beyond the last element of the array.
        int totalMissing = missingCount(nums, n - 1);
        if (k > totalMissing) {
            return nums[n - 1] + (k - totalMissing);
        }
        
        int left = 0;
        int right = n - 1;
        
        // Binary search to find the index where missingCount(index) >= k
        while (left < right) {
            int mid = left + (right - left) / 2;
            
            if (missingCount(nums, mid) < k) {
                left = mid + 1;
            } else {
                right = mid;
            }
        }
        
        // At this point, 'left' is the smallest index where missingCount >= k.
        // Therefore, the k-th missing number is strictly between nums[left - 1] and nums[left].
        // We start from nums[left - 1] and add the remaining required missing numbers.
        int missingBeforeLeftMinusOne = missingCount(nums, left - 1);
        int remainingMissingNeeded = k - missingBeforeLeftMinusOne;
        
        return nums[left - 1] + remainingMissingNeeded;
    }
    
    // Calculates how many numbers are missing between nums[0] and nums[index]
    private int missingCount(int[] nums, int index) {
        return nums[index] - nums[0] - index;
    }
}
```

### 5. Edge Cases & Dry-Run Checklist
*   **Edge Cases Handled:**
    *   Target is beyond max element (`k > missingCount`): Caught immediately, runs in $O(1)$.
    *   `nums = [4,7,9,10], k = 1`: `totalMissing = 10 - 4 - 3 = 3`. Binary search finds `left = 1`. `nums[0] + (1 - 0) = 5`.
*   **Dry-Run Checklist:**
    *   Calculate `missingCount(mid)`.
    *   Verify the math at the end: `nums[left-1] + k - missingCount(left-1)`. This is the exact offset calculation.

### 6. The Google Follow-Up Gauntlet

*   **Follow-Up 1: The Twist - Massive K & Compressed Stream**
    *   *The Twist:* The input array is too large to fit in memory, so it's given as Run-Length Encoded (RLE) blocks, e.g., `[[start1, end1], [start2, end2]]`.
    *   *The Architectural Pivot:* Binary Search over indices is replaced with Binary Search over the blocks. The `missingCount` formula adapts to interval gaps: `missing = block[i].start - block[i-1].end - 1`. We accumulate these gaps. Since we binary search over $B$ blocks, time complexity becomes $O(\log B)$.
*   **Follow-Up 2: The Twist - Dynamic Mutability (Segment Tree / Fenwick Tree)**
    *   *The Twist:* You can `add(val)` to the array (which maintains sorted order), and then query `missingElement(k)`.
    *   *The Architectural Pivot:* We need dynamic ranks. A Fenwick Tree (Binary Indexed Tree) or an Order Statistic Tree (using Red-Black Tree node counts) allows querying the number of elements less than $X$ in $O(\log N)$. The problem translates to Binary Searching the answer $X$ across the integer domain, and at each step using the Fenwick tree to verify how many elements exist below $X$.

# Category C: Complex Data Structures & State-Space Graphs

## 14. LC 381: Insert Delete GetRandom O(1) - Duplicates Allowed (Hard)

### 1. The Full Problem Specification
*   **Problem Statement:** Design a data structure that supports inserting a value, removing a value, and getting a random element in $O(1)$ average time. Duplicates are allowed. When getting a random element, each element must have a probability of being returned linearly proportional to its frequency in the collection.
    *   `insert(int val)`: Inserts an item `val`. Returns `true` if item was not present, `false` otherwise.
    *   `remove(int val)`: Removes an item `val` if present. Returns `true` if present, `false` otherwise.
    *   `getRandom()`: Returns a random element.
*   **Input & Output Examples:**
    *   `insert(1) -> true`, `insert(1) -> false`, `insert(2) -> true`, `getRandom() -> 1 (66% prob) or 2 (33% prob)`, `remove(1) -> true`, `getRandom() -> 1 (50% prob) or 2 (50% prob)`.

### 2. The Core Insights & Tricks
*   **The "Aha!" Moment:** To get $O(1)$ random access, all elements MUST be stored in a contiguous array (`ArrayList` in Java). To delete in $O(1)$ from an array, we must swap the target element with the *last* element in the array and then pop the tail. To find the element to swap in $O(1)$, we need a `HashMap` mapping values to their indices. Since duplicates are allowed, the `HashMap` must map a value to a `Set` of indices.
*   **Common Pitfalls:** Using a `List` of indices inside the `HashMap`. When deleting an element, removing a specific index from an `ArrayList` takes $O(N)$. We *must* use a `LinkedHashSet` or `HashSet` to achieve $O(1)$ removal of a specific index from the map.
*   **Pattern Recognition:** $O(1)$ Random Access + $O(1)$ Deletion $\to$ HashMap + ArrayList "Swap and Pop".

### 3. The Proposed Idea (Base Optimal Solution)
*   **Structures:**
    *   `ArrayList<Integer> nums`: Stores all elements to allow `nums.get(random_index)` in $O(1)$.
    *   `HashMap<Integer, LinkedHashSet<Integer>> map`: Maps a value to all indices where it appears in `nums`. (`LinkedHashSet` prevents deterministic iteration issues, though a standard `HashSet` works too).
*   **Logic (`insert`):**
    *   Add the element to the end of `nums`. Get the index.
    *   Add this index to `map.get(val)`.
*   **Logic (`remove`):**
    *   Find the `Set` of indices for `val`. If missing or empty, return false.
    *   Get *any* index from this Set (e.g., `set.iterator().next()`). Let this be `removeIndex`.
    *   Remove `removeIndex` from the Set.
    *   Get the last element in `nums`, let's call it `lastVal` at `lastIndex`.
    *   **The Swap:** If `removeIndex != lastIndex`, we move `lastVal` to `removeIndex` inside the `nums` array. 
    *   Update the `map` for `lastVal`: remove `lastIndex` and add `removeIndex`.
    *   Finally, pop the last element from `nums` (`nums.remove(nums.size() - 1)`).
*   **Logic (`getRandom`):**
    *   `nums.get(random.nextInt(nums.size()))`.
*   **Time Complexity:** $O(1)$ average for all operations. 
*   **Space Complexity:** $O(N)$ where $N$ is total elements inserted.

### 4. Production-Ready Java Implementation (Base)
```java
class RandomizedCollection {
    private ArrayList<Integer> nums;
    private HashMap<Integer, LinkedHashSet<Integer>> map;
    private Random rand;

    public RandomizedCollection() {
        nums = new ArrayList<>();
        map = new HashMap<>();
        rand = new Random();
    }
    
    public boolean insert(int val) {
        boolean isNotPresent = !map.containsKey(val) || map.get(val).isEmpty();
        
        map.putIfAbsent(val, new LinkedHashSet<>());
        map.get(val).add(nums.size()); // Add current last index
        nums.add(val);
        
        return isNotPresent;
    }
    
    public boolean remove(int val) {
        if (!map.containsKey(val) || map.get(val).isEmpty()) {
            return false;
        }
        
        // 1. Get an index to remove for 'val'
        LinkedHashSet<Integer> targetIndices = map.get(val);
        int removeIndex = targetIndices.iterator().next(); // O(1) fetch
        targetIndices.remove(removeIndex); // Remove it from the set
        
        // 2. Identify the last element in the array
        int lastIndex = nums.size() - 1;
        int lastVal = nums.get(lastIndex);
        
        // 3. Swap the last element into the hole left by removeIndex
        if (removeIndex != lastIndex) {
            nums.set(removeIndex, lastVal);
            
            // 4. Update the map for the last element's new position
            LinkedHashSet<Integer> lastValIndices = map.get(lastVal);
            lastValIndices.remove(lastIndex);
            lastValIndices.add(removeIndex);
        }
        
        // 5. Pop the tail
        nums.remove(lastIndex);
        
        // Cleanup empty sets (optional but good for memory)
        if (targetIndices.isEmpty()) {
            map.remove(val);
        }
        
        return true;
    }
    
    public int getRandom() {
        return nums.get(rand.nextInt(nums.size()));
    }
}
```

### 5. Edge Cases & Dry-Run Checklist
*   **Edge Cases Handled:**
    *   Removing the very last element: The swap `if (removeIndex != lastIndex)` cleanly bypasses moving anything. It just deletes `removeIndex` from the map and pops the array. This prevents a nasty bug where you might re-add the `removeIndex` back into the map for `lastVal`.
    *   Multiple identical elements: The `LinkedHashSet` tracks multiple indices flawlessly.
*   **Dry-Run Checklist:**
    *   Track `removeIndex` and `lastIndex`.
    *   Make sure `lastValIndices.remove(lastIndex)` happens BEFORE `lastValIndices.add(removeIndex)`. (If `lastVal == val` and `removeIndex == lastIndex`, order matters if there is no `if` check).

### 6. The Google Follow-Up Gauntlet
*   **Follow-Up 1: The Twist - Concurrency / Thread Safety**
    *   *The Twist:* The system is accessed by thousands of threads.
    *   *The Architectural Pivot:* Using `ReadWriteLock`. `getRandom` acquires a read lock. `insert` and `remove` acquire a write lock. However, `remove` forces a swap, which modifies `nums`. To minimize lock contention, we can change the architecture: instead of physically removing and swapping, we mark the element as a "Tombstone" (deleted). `getRandom` loops until it finds a non-tombstone. When tombstones exceed 50% of the array, a single background thread performs compaction.
*   **Follow-Up 2: The Twist - Distributed Servers**
    *   *The Twist:* The data is too big for one machine and lives on $M$ shards. How do you implement `getRandom()`?
    *   *The Architectural Pivot:* The coordinator node requests the `count` of elements from each shard. It generates a random number $R$ between $0$ and $\text{total\_count} - 1$. It determines which shard holds the $R$-th element, and routes a `getRandom()` call directly to that shard. 

---

## 15. LC 1172: Dinner Plate Stacks (Hard)

### 1. The Full Problem Specification
*   **Problem Statement:** You have an infinite number of stacks arranged in a row and numbered (left to right) from 0, each with a maximum capacity `capacity`.
    *   `push(val)`: Pushes `val` into the leftmost stack with size `< capacity`.
    *   `pop()`: Returns the value at the top of the rightmost non-empty stack and removes it.
    *   `popAtStack(index)`: Returns the value at the top of the stack with the given `index` and removes it.
*   **Input & Output Examples:**
    *   `capacity = 2`. `push(1), push(2), push(3), push(4)`. (Stack 0: [1,2], Stack 1: [3,4]).
    *   `popAtStack(0) -> 2`. (Stack 0: [1], Stack 1: [3,4]).
    *   `push(5)`. (Stack 0: [1,5] - 5 fills the hole in the leftmost available stack).

### 2. The Core Insights & Tricks
*   **The "Aha!" Moment:** We need to quickly find the *leftmost* not-full stack and the *rightmost* non-empty stack. An `ArrayList` of stacks natively handles the rightmost non-empty stack (just check the last stack and pop empty ones). For the leftmost not-full stack, a `TreeSet` of integers is perfect. It will store the indices of any stack that currently has room. 
*   **Common Pitfalls:** Using a PriorityQueue for available stacks. When a stack becomes full, we must remove it from the PriorityQueue, which is $O(N)$. `TreeSet.remove()` is $O(\log N)$.
*   **Pattern Recognition:** "Leftmost available" + dynamic inserts/deletes $\to$ TreeSet of indices.

### 3. The Proposed Idea (Base Optimal Solution)
*   **Structures:**
    *   `List<Stack<Integer>> stacks`: To store the actual data.
    *   `TreeSet<Integer> availableStacks`: Stores the indices of all stacks that have size `< capacity`.
    *   `int capacity`: Max capacity per stack.
*   **Logic (`push`):**
    *   If `availableStacks` is empty, we must create a new stack. Add it to `stacks`, push the value, and if `capacity > 1`, add the new index to `availableStacks`.
    *   If not empty, get the lowest index: `idx = availableStacks.first()`.
    *   Push value to `stacks.get(idx)`. If `stacks.get(idx).size() == capacity`, remove `idx` from `availableStacks`.
*   **Logic (`pop`):**
    *   We need the rightmost element. But `popAtStack` might leave empty stacks at the very end of our `List`. We must trim empty stacks from the right end of the `List` first.
    *   After trimming, if list is empty, return -1.
    *   Return `popAtStack(stacks.size() - 1)`.
*   **Logic (`popAtStack(idx)`):**
    *   If `idx >= stacks.size()` or `stacks.get(idx)` is empty, return -1.
    *   Pop the value.
    *   Because we just removed an item, this stack is definitely not full! Add `idx` to `availableStacks`.
    *   Return the value.
*   **Time Complexity:** `push` is $O(\log N)$ due to TreeSet. `pop` amortized $O(1)$ (ignoring the trim, or worst case $O(N)$ if many empty stacks). `popAtStack` is $O(\log N)$.
*   **Space Complexity:** $O(N)$ for $N$ elements.

### 4. Production-Ready Java Implementation (Base)
```java
class DinnerPlates {
    private final int capacity;
    private final List<Deque<Integer>> stacks; // Using Deque instead of java.util.Stack for performance
    private final TreeSet<Integer> availableStacks; // Tracks indices of not-full stacks

    public DinnerPlates(int capacity) {
        this.capacity = capacity;
        this.stacks = new ArrayList<>();
        this.availableStacks = new TreeSet<>();
    }
    
    public void push(int val) {
        if (availableStacks.isEmpty()) {
            // All existing stacks are full, create a new one
            Deque<Integer> newStack = new ArrayDeque<>();
            newStack.push(val);
            stacks.add(newStack);
            
            int newIdx = stacks.size() - 1;
            if (capacity > 1) {
                availableStacks.add(newIdx); // It has room for more
            }
        } else {
            // Find the leftmost available stack
            int leftmostIdx = availableStacks.first();
            Deque<Integer> stack = stacks.get(leftmostIdx);
            stack.push(val);
            
            // If it just became full, remove from available
            if (stack.size() == capacity) {
                availableStacks.remove(leftmostIdx);
            }
        }
    }
    
    public int pop() {
        // Trim trailing empty stacks that might have been emptied by popAtStack
        while (!stacks.isEmpty() && stacks.get(stacks.size() - 1).isEmpty()) {
            int lastIdx = stacks.size() - 1;
            stacks.remove(lastIdx);
            availableStacks.remove(lastIdx); // Remove from TreeSet to prevent pushing to an out-of-bounds index
        }
        
        if (stacks.isEmpty()) return -1;
        
        return popAtStack(stacks.size() - 1);
    }
    
    public int popAtStack(int index) {
        if (index < 0 || index >= stacks.size()) return -1;
        
        Deque<Integer> stack = stacks.get(index);
        if (stack.isEmpty()) return -1;
        
        int val = stack.pop();
        // Since we popped, it is guaranteed not full
        availableStacks.add(index);
        
        return val;
    }
}
```

### 5. Edge Cases & Dry-Run Checklist
*   **Edge Cases Handled:**
    *   `capacity = 1`: The TreeSet logic dynamically handles this because a push immediately fills the stack, and the `if (capacity > 1)` prevents it from ever entering `availableStacks` initially.
    *   `popAtStack` on trailing empty stacks: The `pop()` trim logic perfectly cleans up the trailing null spaces. Note that `popAtStack` *leaves* empty stacks in the middle of the array, which is intended.
*   **Dry-Run Checklist:**
    *   Ensure `trim` in `pop()` removes the index from `availableStacks` before removing the stack from the list.

### 6. The Google Follow-Up Gauntlet
*   **Follow-Up 1: The Twist - Strict $O(1)$ Optimization for `popAtStack`**
    *   *The Twist:* You can't use a `TreeSet` ($O(\log N)$). You must optimize the average case.
    *   *The Architectural Pivot:* Segment Tree over the array of stacks. Each node stores the minimum index of a non-full stack in its range. Updates and queries are still $O(\log N)$ but with a massively lower constant factor than a heavy `TreeSet` rebalancing. Or, a min-heap (PriorityQueue) with lazy deletion: we push available indices to a heap. On `push`, we poll the heap until we find a valid index (where `stacks.get(idx).size() < capacity`). 
*   **Follow-Up 2: The Twist - Variable Capacity**
    *   *The Twist:* Each stack has a different predefined capacity `C[i]`.
    *   *The Architectural Pivot:* The `TreeSet` approach handles this gracefully! We just change the check to `stack.size() == C.get(leftmostIdx)`.

---

## 16. LC 815: Bus Routes (Hard)

### 1. The Full Problem Specification
*   **Problem Statement:** You are given an array `routes` where `routes[i]` is a bus route that the $i$-th bus repeats forever. (e.g., `routes[0] = [1, 5, 7]` means bus 0 visits stop 1 $\to$ 5 $\to$ 7 $\to$ 1...). Given a `source` and `target` stop, return the least number of buses you must take to travel from `source` to `target`. Return `-1` if it is not possible.
    *   Constraints: $1 \le routes.length \le 500$, $1 \le routes[i].length \le 10^5$, $\sum routes[i].length \le 10^5$.
*   **Input & Output Examples:**
    *   `routes = [[1,2,7], [3,6,7]], source = 1, target = 6` $\to$ `Output: 2`. (Take bus 0 from 1 to 7, then bus 1 from 7 to 6).
    *   `routes = [[7,12], [4,5,15], [6], [15,19], [9,12,13]], source = 15, target = 12` $\to$ `Output: -1`.

### 2. The Core Insights & Tricks
*   **The "Aha!" Moment:** A standard graph where Stops are Nodes and Bus Routes are Edges will cause an instant Time Limit Exceeded (TLE) because a route with 100,000 stops creates a dense clique of $10^{10}$ edges. Instead, we must invert the graph: **The Bus Routes are the Nodes!** Intersecting routes share an edge. The shortest path is the minimum number of *routes* traversed.
*   **Common Pitfalls:** Building a Stop-to-Stop adjacency list. 
*   **Pattern Recognition:** "Minimum number of [transfers/vehicles/groups]" $\to$ Hypergraph BFS.

### 3. The Proposed Idea (Base Optimal Solution)
*   **Build the Stop-to-Route Map:** Iterate through `routes`. For each `stop`, add the `route_id` (the index $i$) to a `Map<Integer, List<Integer>> stopToRoutes`.
*   **BFS Setup:** 
    *   Queue stores `route_id`s. (Or, store `stop` in the queue, but track visited *routes*). Let's store `stop` in the queue for simplicity.
    *   Queue `q` initialized with `source`.
    *   `HashSet<Integer> visitedStops` to avoid checking a stop twice.
    *   `HashSet<Integer> visitedRoutes` to avoid riding the same bus twice (CRITICAL FOR PERFORMANCE).
*   **BFS Traversal:**
    *   Pop a `stop`. If `stop == target`, return `buses`.
    *   For every `route_id` passing through this `stop` (lookup in `stopToRoutes`):
        *   If `route_id` is in `visitedRoutes`, skip it.
        *   Mark `route_id` as visited.
        *   For every `nextStop` in `routes[route_id]`:
            *   If `nextStop` is not in `visitedStops`, add it to queue and mark visited.
*   **Time Complexity:** $O(N)$ where $N$ is the total number of stops across all routes $\sum routes[i].length$. Each route and each stop is processed exactly once.
*   **Space Complexity:** $O(N)$ for the `stopToRoutes` map and the BFS queues/sets.

### 4. Production-Ready Java Implementation (Base)
```java
class Solution {
    public int numBusesToDestination(int[][] routes, int source, int target) {
        if (source == target) return 0;
        
        // Map: Stop -> List of Route IDs (Bus IDs)
        Map<Integer, List<Integer>> stopToRoutes = new HashMap<>();
        for (int i = 0; i < routes.length; i++) {
            for (int stop : routes[i]) {
                stopToRoutes.putIfAbsent(stop, new ArrayList<>());
                stopToRoutes.get(stop).add(i);
            }
        }
        
        // Edge case: target or source not in any route
        if (!stopToRoutes.containsKey(source) || !stopToRoutes.containsKey(target)) {
            return -1;
        }
        
        Queue<Integer> queue = new ArrayDeque<>();
        Set<Integer> visitedStops = new HashSet<>();
        Set<Integer> visitedRoutes = new HashSet<>();
        
        queue.offer(source);
        visitedStops.add(source);
        int buses = 0;
        
        while (!queue.isEmpty()) {
            int levelSize = queue.size();
            buses++; // Taking a bus to reach the next set of stops
            
            for (int i = 0; i < levelSize; i++) {
                int currStop = queue.poll();
                
                // Explore all buses that pass through the current stop
                for (int routeId : stopToRoutes.get(currStop)) {
                    if (visitedRoutes.contains(routeId)) continue;
                    visitedRoutes.add(routeId); // Board the bus
                    
                    // Traverse all stops this bus goes to
                    for (int nextStop : routes[routeId]) {
                        if (nextStop == target) {
                            return buses;
                        }
                        if (!visitedStops.contains(nextStop)) {
                            visitedStops.add(nextStop);
                            queue.offer(nextStop);
                        }
                    }
                }
            }
        }
        
        return -1;
    }
}
```

### 5. Edge Cases & Dry-Run Checklist
*   **Edge Cases Handled:**
    *   `source == target`: Returns `0` instantly.
    *   Source or target completely disconnected/missing: `containsKey` checks prevent useless BFS initialization.
*   **Dry-Run Checklist:**
    *   Make sure `buses++` is attached to the *level* progression of the BFS, not every node. The loop structure `for(int i = 0; i < levelSize; i++)` handles this.
    *   Verify `visitedRoutes` is checked! If you only check `visitedStops`, a route with 100,000 stops overlapping with another will cause $O(N^2)$ traversal.

### 6. The Google Follow-Up Gauntlet
*   **Follow-Up 1: The Twist - Bi-Directional BFS**
    *   *The Twist:* The interviewer notes that the branching factor is huge. Can you optimize the BFS?
    *   *The Architectural Pivot:* Use Bi-Directional BFS. Start one queue from `source` and one from `target`. Expand the smaller queue at each step. This cuts the branching explosion in half and is a classic L5 signal.
*   **Follow-Up 2: The Twist - Time Tables / Schedules**
    *   *The Twist:* Instead of a repeating route, the input is `flights = [source, target, start_time, end_time, price]`. You must reach the target in minimum transfers, and you can only transfer if `arrival_time <= next_start_time`.
    *   *The Architectural Pivot:* This changes from Hypergraph BFS to a modified Dijkstra or Bellman-Ford on a time-expanded graph. A simple queue no longer works because time monotonically increases. (This essentially becomes LC 787 Cheapest Flights Within K Stops with a temporal dimension).

## 17. LC 778: Swim in Rising Water (Hard)

### 1. The Full Problem Specification
*   **Problem Statement:** You are given an `n x n` integer matrix `grid` where each value `grid[i][j]` represents the elevation at that point. Rain starts to fall. At time `t`, the depth of the water everywhere is `t`. You can swim from a square to an adjacent square if and only if the elevation of both squares individually is at most `t`. Return the least time until you can reach the bottom right square `(n - 1, n - 1)` if you start at the top left square `(0, 0)`.
    *   Constraints: $n \le 50$, grid values are a permutation of $0$ to $n^2 - 1$.
*   **Input & Output Examples:**
    *   `grid = [[0,2],[1,3]]` $\to$ `Output: 3`. (Wait till t=3 to enter `(1,1)`).
    *   `grid = [[0,1,2,3,4],[24,23,22,21,5],[12,13,14,15,16],[11,17,18,19,20],[10,9,8,7,6]]` $\to$ `Output: 16`.

### 2. The Core Insights & Tricks
*   **The "Aha!" Moment:** The path we take will have a "bottleneck" — a maximum elevation we MUST cross. We want to find a path that *minimizes* this maximum bottleneck. This is the definition of **Dijkstra's Algorithm** (modifying the relaxation step from `sum of weights` to `max of weights`). 
*   **Alternative Aha:** We can also use **Binary Search on the Answer**. If we can reach the end at time $T$, we can reach it at $T+1$. We binary search $T$ and use a simple DFS/BFS to verify if a path exists using only cells $\le T$.
*   **Alternative Aha 2:** **Disjoint Set Union (DSU) / Union-Find**. Sort all cells by elevation. Add cells one by one (simulate water rising). Merge adjacent cells. Stop when `(0,0)` and `(n-1, n-1)` belong to the same component!
*   **Common Pitfalls:** Standard BFS without a Priority Queue will explore paths blindly and fail to track the minimal bottleneck efficiently, essentially turning into exponential DFS.
*   **Pattern Recognition:** "Minimize the maximum edge on a path" $\to$ Dijkstra (Min-Heap) OR Binary Search + BFS OR Union Find.

### 3. The Proposed Idea (Base Optimal Solution: Dijkstra)
*   *Why Dijkstra?* It's the most flexible for follow-ups (handles non-permutations and varying edge weights dynamically).
*   **Structures:**
    *   `PriorityQueue<int[]>` storing `{row, col, max_elevation_so_far}` ordered by `max_elevation_so_far`.
    *   `boolean[][] visited` to prevent cycles.
*   **Logic:**
    *   Push `{0, 0, grid[0][0]}` into the PQ. Mark `(0,0)` as visited.
    *   Pop the cell with the smallest `max_elevation_so_far`.
    *   If it is `(n-1, n-1)`, return `max_elevation_so_far`.
    *   For each of the 4 neighbors:
        *   If valid and not visited: Mark visited. The new bottleneck for this path is `Math.max(current_bottleneck, grid[nr][nc])`. Push to PQ.
*   **Time Complexity:** $O(N^2 \log N)$. There are $N^2$ cells. PQ operations take $\log(N^2) = 2 \log N$.
*   **Space Complexity:** $O(N^2)$ for the PQ and visited matrix.

### 4. Production-Ready Java Implementation (Base)
```java
class Solution {
    public int swimInWater(int[][] grid) {
        int n = grid.length;
        // Priority Queue stores {row, col, max_elevation_so_far}
        PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> a[2] - b[2]);
        boolean[][] visited = new boolean[n][n];
        
        // Directions for adjacent cells: Up, Down, Left, Right
        int[][] dirs = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        
        pq.offer(new int[]{0, 0, grid[0][0]});
        visited[0][0] = true;
        
        while (!pq.isEmpty()) {
            int[] current = pq.poll();
            int r = current[0];
            int c = current[1];
            int maxElevation = current[2];
            
            // Reached the destination
            if (r == n - 1 && c == n - 1) {
                return maxElevation;
            }
            
            // Explore neighbors
            for (int[] dir : dirs) {
                int nr = r + dir[0];
                int nc = c + dir[1];
                
                if (nr >= 0 && nr < n && nc >= 0 && nc < n && !visited[nr][nc]) {
                    visited[nr][nc] = true;
                    // The bottleneck of the path is the maximum elevation encountered
                    int nextElevation = Math.max(maxElevation, grid[nr][nc]);
                    pq.offer(new int[]{nr, nc, nextElevation});
                }
            }
        }
        
        return -1;
    }
}
```

### 5. Edge Cases & Dry-Run Checklist
*   **Edge Cases Handled:**
    *   `grid[0][0]` is the largest element in the grid: The `nextElevation` math naturally pulls `grid[0][0]` all the way to the end, outputting the correct bottleneck.
    *   `n = 1`: Instantly returns `grid[0][0]`.
*   **Dry-Run Checklist:**
    *   Ensure `visited[nr][nc] = true` is set *before* pushing to the PQ. If you set it after popping, duplicate cells will flood the PQ and cause a TLE.

### 6. The Google Follow-Up Gauntlet
*   **Follow-Up 1: The Twist - Multiple Queries**
    *   *The Twist:* Instead of just `(0,0)` to `(n-1, n-1)`, the interviewer gives you $Q$ queries of `[source_row, source_col, dest_row, dest_col]`. Returning Dijkstra $Q$ times is $O(Q \cdot N^2 \log N)$, which TLEs.
    *   *The Architectural Pivot:* Kruskal's Minimum Spanning Tree / Union-Find. Sort all edges between adjacent cells. For each query, binary search the answer. Wait, there's a better way: **Kruskal Reconstruction Tree (Reachability Tree)**. But a simpler Google L5 approach is to process all queries offline using Union Find. Sort cells by elevation. Add cells to DSU. After each addition, check if `find(source) == find(dest)` for pending queries.
*   **Follow-Up 2: The Twist - Water Flows Down (Topological Sort / DP)**
    *   *The Twist:* The problem changes to: "Find the longest path you can ski downwards." (LC 329 Longest Increasing Path in a Matrix).
    *   *The Architectural Pivot:* Dijkstra no longer applies. You pivot to DFS with Memoization.

---

## 18. LC 827: Making A Large Island (Hard)

### 1. The Full Problem Specification
*   **Problem Statement:** You are given an `n x n` binary matrix `grid`. You are allowed to change at most one `0` to be a `1`. Return the size of the largest island in `grid` after applying this operation. An island is a 4-directionally connected group of `1`s.
    *   Constraints: $n \le 500$.
*   **Input & Output Examples:**
    *   `grid = [[1, 0], [0, 1]]` $\to$ `Output: 3`. (Change one 0 to 1, connecting with one of the existing 1s).
    *   `grid = [[1, 1], [1, 1]]` $\to$ `Output: 4`. (No 0s to change, return 4).

### 2. The Core Insights & Tricks
*   **The "Aha!" Moment:** A naive approach tries to flip every `0` and run a full DFS/BFS ($O(N^4)$ time), which fails instantly. The optimal trick is **Component Labeling (Two-Pass)**. 
    *   *Pass 1:* Find all existing islands. Give each island a unique ID (e.g., 2, 3, 4...). Store their areas in a `HashMap<IslandID, Area>`.
    *   *Pass 2:* Iterate over every `0`. Check its 4 neighbors. If a neighbor has an island ID, add that island's area. Sum the unique adjacent island areas + 1 (the flipped 0). Track the global maximum.
*   **Common Pitfalls:** During Pass 2, a `0` might be surrounded by the *same* island on multiple sides (e.g., inside a 'U' shape). You must use a `HashSet` of neighboring Island IDs to avoid double-counting the area.
*   **Pattern Recognition:** "Change 1 element to connect components" $\to$ Component Labeling / Disjoint Set Union (DSU).

### 3. The Proposed Idea (Base Optimal Solution)
*   **Structures:**
    *   Modify `grid` in-place to store the `islandId`. (Start IDs at 2 to avoid confusing with 0 and 1).
    *   `HashMap<Integer, Integer> areaMap` mapping `islandId -> area`.
*   **Logic:**
    *   Iterate through grid. If `grid[i][j] == 1`, launch a DFS. 
    *   In the DFS, change `1`s to `currentIslandId`. Count and return the area. Store in `areaMap`. Increment `currentIslandId`.
    *   Iterate through grid again. If `grid[i][j] == 0`:
        *   Create `HashSet<Integer> neighborIds`.
        *   Check 4 neighbors. If neighbor $> 1$, add to `neighborIds`.
        *   Calculate potential area = $1 + \sum \text{areaMap.get(id)}$ for `id` in `neighborIds`.
        *   Update `maxArea`.
*   **Edge Case:** If the grid is fully `1`s, the second loop will never trigger. We must initialize `maxArea` to the largest area found in Pass 1.
*   **Time Complexity:** $O(N^2)$. First pass touches cells a few times, second pass does 4 $O(1)$ checks per cell.
*   **Space Complexity:** $O(N^2)$ for the recursion stack and HashMap.

### 4. Production-Ready Java Implementation (Base)
```java
class Solution {
    public int largestIsland(int[][] grid) {
        int n = grid.length;
        Map<Integer, Integer> areaMap = new HashMap<>();
        int currentIslandId = 2; // IDs start from 2
        int maxArea = 0;
        
        // Pass 1: Label all islands and compute their areas
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                if (grid[r][c] == 1) {
                    int area = dfs(grid, r, c, currentIslandId);
                    areaMap.put(currentIslandId, area);
                    maxArea = Math.max(maxArea, area); // In case we can't flip any 0
                    currentIslandId++;
                }
            }
        }
        
        // Pass 2: Try flipping every 0 to 1
        int[][] dirs = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                if (grid[r][c] == 0) {
                    Set<Integer> uniqueNeighborIds = new HashSet<>();
                    for (int[] dir : dirs) {
                        int nr = r + dir[0];
                        int nc = c + dir[1];
                        if (nr >= 0 && nr < n && nc >= 0 && nc < n && grid[nr][nc] > 1) {
                            uniqueNeighborIds.add(grid[nr][nc]);
                        }
                    }
                    
                    int potentialArea = 1; // The flipped 0
                    for (int id : uniqueNeighborIds) {
                        potentialArea += areaMap.get(id);
                    }
                    maxArea = Math.max(maxArea, potentialArea);
                }
            }
        }
        
        return maxArea;
    }
    
    private int dfs(int[][] grid, int r, int c, int islandId) {
        int n = grid.length;
        // Check bounds and if cell is a 1
        if (r < 0 || r >= n || c < 0 || c >= n || grid[r][c] != 1) {
            return 0;
        }
        
        grid[r][c] = islandId; // Mark cell with ID
        int area = 1;
        
        area += dfs(grid, r - 1, c, islandId);
        area += dfs(grid, r + 1, c, islandId);
        area += dfs(grid, r, c - 1, islandId);
        area += dfs(grid, r, c + 1, islandId);
        
        return area;
    }
}
```

### 5. Edge Cases & Dry-Run Checklist
*   **Edge Cases Handled:**
    *   No `0`s (Grid is all 1s): Pass 2 does nothing. Handled by `maxArea = Math.max(maxArea, area)` in Pass 1.
    *   No `1`s (Grid is all 0s): Pass 1 does nothing. Pass 2 calculates `1 + 0 = 1`. Correct.
*   **Dry-Run Checklist:**
    *   Verify the `HashSet` behavior inside Pass 2. If a `0` touches `islandId=2` on the top and left, `potentialArea` must only add `areaMap.get(2)` *once*.

### 6. The Google Follow-Up Gauntlet
*   **Follow-Up 1: The Twist - Flip K Zeroes**
    *   *The Twist:* You can flip up to `K` zeroes.
    *   *The Architectural Pivot:* The 2-pass component labeling breaks down because flipping K zeroes requires exploring combinations of gap-bridging. This converts the problem into a BFS tracking state `(row, col, zeroes_flipped)`, exactly like LC 1293 Shortest Path in a Grid with Obstacles Elimination.
*   **Follow-Up 2: The Twist - Memory Constraints (Read-Only Grid)**
    *   *The Twist:* The `grid` is read-only (you cannot alter values to `islandId`).
    *   *The Architectural Pivot:* You must use an explicit **Disjoint Set Union (DSU)** structure where each cell `(r, c)` is mapped to a 1D ID `r * n + c`. The DSU stores the parents and the component sizes. Pass 1 unions adjacent 1s. Pass 2 iterates 0s and checks parents via `find()`.

---

## 19. LC 489: Robot Room Cleaner (Hard)

### 1. The Full Problem Specification
*   **Problem Statement:** You are controlling a robot in a room modeled as an `m x n` grid. The robot's starting position is unknown, and the grid boundaries are unknown. You have an API:
    *   `move()`: Moves 1 step forward, returns true if successful (no wall).
    *   `turnLeft()`, `turnRight()`: Turns 90 degrees.
    *   `clean()`: Cleans the current cell.
    Design an algorithm to clean the entire room.
*   **Input & Output Examples:**
    *   (Implicit) The robot explores blindly and terminates when all reachable cells are cleaned.

### 2. The Core Insights & Tricks
*   **The "Aha!" Moment:** Since the robot doesn't know global coordinates, it must use **relative coordinates**. Assuming start is `(0,0)` facing UP (direction 0). A move forward changes coordinates based on current direction. To explore everything, we use **Backtracking DFS**. 
*   **The Golden Rule of Robot Backtracking:** Whenever a DFS recursive call finishes exploring a cell, the robot MUST physically return to the exact same cell and exact same orientation it was in before the call. Otherwise, the recursive state machine goes out of sync with physical reality.
*   **Common Pitfalls:** Forgetting to turn the robot around twice (180 degrees) to move back, and then turning it twice again to restore its original facing direction.
*   **Pattern Recognition:** Relative exploration without a map $\to$ State-Space Backtracking DFS.

### 3. The Proposed Idea (Base Optimal Solution)
*   **Structures:**
    *   `HashSet<String> visited` to store relative coordinates `"r,c"`.
    *   Directions array: `{{-1,0}, {0,1}, {1,0}, {0,-1}}` (UP, RIGHT, DOWN, LEFT). This ordering ensures turning right `(dir + 1) % 4` cycles perfectly.
*   **Logic (DFS(row, col, dir)):**
    *   Clean cell. Mark `"row,col"` visited.
    *   Loop 4 times (for 4 directions):
        *   Calculate potential next coordinates `nr`, `nc` based on `dir`.
        *   If not visited and `robot.move()` is true:
            *   Recursively call `DFS(nr, nc, dir)`.
            *   **Backtrack physically:** `robot.turnRight(); robot.turnRight(); robot.move(); robot.turnRight(); robot.turnRight();`
        *   Turn the robot to check the next direction: `robot.turnRight()`.
        *   Update internal `dir = (dir + 1) % 4`.
*   **Time Complexity:** $O(N - E)$ where $N$ is cells and $E$ is obstacles. We visit each empty cell once and perform a constant number of operations.
*   **Space Complexity:** $O(N - E)$ for the HashSet and Recursion Stack.

### 4. Production-Ready Java Implementation (Base)
```java
/**
 * // This is the robot's control interface.
 * interface Robot {
 *     // Returns true if the cell in front is open and robot moves into the cell.
 *     // Returns false if the cell in front is blocked and robot stays in current cell.
 *     public boolean move();
 *     public void turnLeft();
 *     public void turnRight();
 *     public void clean();
 * }
 */

class Solution {
    // UP, RIGHT, DOWN, LEFT (Clockwise ensures turnRight() = dir + 1)
    private static final int[][] DIRS = {{-1, 0}, {0, 1}, {1, 0}, {0, -1}};
    private Set<String> visited = new HashSet<>();
    private Robot robot;

    public void cleanRoom(Robot robot) {
        this.robot = robot;
        backtrack(0, 0, 0); // start at relative (0,0) facing UP (0)
    }
    
    private void backtrack(int r, int c, int dir) {
        robot.clean();
        visited.add(r + "," + c);
        
        for (int i = 0; i < 4; i++) {
            int newDir = (dir + i) % 4;
            int nr = r + DIRS[newDir][0];
            int nc = c + DIRS[newDir][1];
            
            if (!visited.contains(nr + "," + nc) && robot.move()) {
                // Robot physically moved, explore further
                backtrack(nr, nc, newDir);
                
                // CRITICAL: Physically backtrack to the previous cell and orientation
                goBack();
            }
            
            // Turn right to test the next direction in the for-loop
            robot.turnRight();
        }
    }
    
    private void goBack() {
        robot.turnRight();
        robot.turnRight();
        robot.move();      // Move back
        robot.turnRight();
        robot.turnRight(); // Restore original facing direction
    }
}
```

### 5. Edge Cases & Dry-Run Checklist
*   **Edge Cases Handled:**
    *   Corridors (1xN grid): Explores fully, hits wall, recursive stack unwinds while calling `goBack()`, backing out flawlessly.
*   **Dry-Run Checklist:**
    *   Verify the `DIRS` array mathematically aligns with the `robot.turnRight()` orientation. If UP is `{-1, 0}`, a 90-deg clockwise turn is RIGHT `{0, 1}`.
    *   Verify `goBack()` does 2 rights, 1 move, 2 rights.

### 6. The Google Follow-Up Gauntlet
*   **Follow-Up 1: The Twist - Unknown Grid, but shortest path to Target**
    *   *The Twist:* There's a target item in the room. You have a radar that tells you the Manhattan distance to the item.
    *   *The Architectural Pivot:* This becomes A* Search with blind discovery. The robot uses the heuristic to guide physical moves, but if it hits a dead end, it must physically backtrack.
*   **Follow-Up 2: The Twist - The Battery Constraint**
    *   *The Twist:* The robot has a battery limit $B$. It must return to `(0,0)` to recharge.
    *   *The Architectural Pivot:* During the DFS, pass down a `steps_taken` variable. Before taking a step, check if `steps_taken + 1 (forward) + distance_home <= B`. If true, proceed. If false, immediately execute `goBack()` and trigger the recharge logic. The `distance_home` is calculated by running a BFS on the locally discovered `visited` graph!

# Category D: Advanced Graphs, Sweep Line & Range Intervals

## 20. LC 305: Number of Islands II (Hard)

### 1. The Full Problem Specification
*   **Problem Statement:** You are given an empty 2D binary grid `grid` of size `m x n`. Initially, all cells are water (`0`). You are given an array of `positions` where `positions[i] = [r, c]` represents turning the cell at `(r, c)` into land (`1`). Return an array of integers `answer` where `answer[i]` is the number of islands after turning the cell at `positions[i]` into land. An island is a 4-directionally connected group of `1`s.
    *   Constraints: $1 \le m, n \le 10^4$, $1 \le positions.length \le 10^4$.
*   **Input & Output Examples:**
    *   `m = 3, n = 3, positions = [[0,0], [0,1], [1,2], [2,1]]` $\to$ `Output: [1, 1, 2, 3]`.

### 2. The Core Insights & Tricks
*   **The "Aha!" Moment:** We are dynamically adding connectivity over time. Running a DFS/BFS after every single insertion takes $O(K \cdot M \cdot N)$, which Time Limit Exceeds immediately. The perfect data structure for incremental dynamic connectivity is **Disjoint Set Union (DSU) / Union-Find**.
*   **Common Pitfalls:** Not mapping the 2D coordinates `(r, c)` to a 1D ID correctly (`r * n + c`). Another pitfall is handling duplicate positions in the input array; flipping a `1` that is already a `1` shouldn't increase the island count.
*   **Pattern Recognition:** Dynamic graph edges + "Count components" $\to$ Disjoint Set Union (Union-Find).

### 3. The Proposed Idea (Base Optimal Solution)
*   **Structures:**
    *   `parent` array of size `m * n` initialized to `-1`. A `-1` indicates water.
    *   `count` variable tracking the current number of islands.
*   **Logic (For each position):**
    *   Convert `(r, c)` to `id = r * n + c`.
    *   If `parent[id] != -1`, it's a duplicate position. Just append current `count` and continue.
    *   Otherwise, it's new land. Set `parent[id] = id`. Increment `count`.
    *   Check all 4 adjacent neighbors. If a neighbor is valid land (i.e., `parent[neighbor_id] != -1`), union them.
    *   To `union(A, B)`: Find the root of A and root of B. If they are different, make one point to the other, and **decrement** `count` (since two separate islands just merged into one).
    *   Append `count` to the answer array.
*   **Time Complexity:** $O(K \cdot \alpha(M \cdot N))$ where $K$ is the number of positions. With Path Compression and Union by Rank, $\alpha$ is the Inverse Ackermann function (effectively $O(1)$). Total time $O(K)$.
*   **Space Complexity:** $O(M \cdot N)$ for the `parent` array.

### 4. Production-Ready Java Implementation (Base)
```java
class Solution {
    class UnionFind {
        int[] parent;
        int[] rank;
        int count;

        public UnionFind(int size) {
            parent = new int[size];
            rank = new int[size];
            Arrays.fill(parent, -1); // -1 indicates water
            count = 0;
        }

        public void addLand(int id) {
            if (parent[id] == -1) {
                parent[id] = id; // Root points to itself
                count++;
            }
        }

        public boolean isLand(int id) {
            return parent[id] != -1;
        }

        public int find(int i) {
            if (parent[i] == i) {
                return i;
            }
            // Path compression
            return parent[i] = find(parent[i]);
        }

        public void union(int x, int y) {
            int rootX = find(x);
            int rootY = find(y);

            if (rootX != rootY) {
                // Union by rank
                if (rank[rootX] > rank[rootY]) {
                    parent[rootY] = rootX;
                } else if (rank[rootX] < rank[rootY]) {
                    parent[rootX] = rootY;
                } else {
                    parent[rootY] = rootX;
                    rank[rootX]++;
                }
                count--; // Merging two islands decreases the total count by 1
            }
        }
    }

    public List<Integer> numIslands2(int m, int n, int[][] positions) {
        List<Integer> result = new ArrayList<>();
        UnionFind uf = new UnionFind(m * n);
        int[][] dirs = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        for (int[] pos : positions) {
            int r = pos[0];
            int c = pos[1];
            int id = r * n + c;

            if (uf.isLand(id)) {
                result.add(uf.count);
                continue;
            }

            uf.addLand(id);

            // Connect with neighboring lands
            for (int[] dir : dirs) {
                int nr = r + dir[0];
                int nc = c + dir[1];
                int nId = nr * n + nc;

                if (nr >= 0 && nr < m && nc >= 0 && nc < n && uf.isLand(nId)) {
                    uf.union(id, nId);
                }
            }

            result.add(uf.count);
        }

        return result;
    }
}
```

### 5. Edge Cases & Dry-Run Checklist
*   **Edge Cases Handled:**
    *   Duplicates in `positions`: Blocked by `if (uf.isLand(id)) continue`.
    *   Merging 4 isolated islands with 1 drop: The new land connects to all 4 neighbors. `count` increases by 1, then the `union` triggers 4 times, decrementing `count` by 4. Net change: -3. Correct!
*   **Dry-Run Checklist:**
    *   Ensure 1D mapping logic is strict: `id = row * COLS + col`. (Not `row * ROWS`).

### 6. The Google Follow-Up Gauntlet
*   **Follow-Up 1: The Twist - Sparse Grid (Memory Constraint)**
    *   *The Twist:* The grid is $10^9 \times 10^9$, but there are only $10^4$ positions. The `parent` array of size $M \cdot N$ will result in OutOfMemoryError.
    *   *The Architectural Pivot:* Change the `int[] parent` to a `HashMap<Integer, Integer> parent`. Only initialize entries when land is added. Time complexity remains $O(K)$, but Space drops to $O(K)$.
*   **Follow-Up 2: The Twist - Remove Land**
    *   *The Twist:* You can add land AND remove land dynamically.
    *   *The Architectural Pivot:* DSU **cannot** handle edge deletions. This converts the problem into dynamic connectivity, requiring Link-Cut Trees or maintaining a full graph and re-running BFS on the broken components (or "time reversal" offline if all queries are known in advance).

---

## 21. LC 269: Alien Dictionary (Hard)

### 1. The Full Problem Specification
*   **Problem Statement:** There is a new alien language that uses the English alphabet. However, the order among letters is unknown to you. You are given a list of strings `words` from the alien language's dictionary, where the strings in `words` are sorted lexicographically by the rules of this new language. Return a string of the unique letters in the new alien language sorted in lexicographically increasing order by the new language's rules. If there is no valid ordering (cycles), return `""`.
*   **Input & Output Examples:**
    *   `words = ["wrt","wrf","er","ett","rftt"]` $\to$ `Output: "wertf"`.
    *   `words = ["z","x","z"]` $\to$ `Output: ""`. (Cycle).

### 2. The Core Insights & Tricks
*   **The "Aha!" Moment:** Lexicographical sorting only tells us about the *first differing character* between two adjacent words. E.g., `["wrt", "wrf"]` tells us `t` comes before `f`. Comparing non-adjacent words gives redundant information. The problem reduces to extracting these directed edges `u -> v` and performing a **Topological Sort**.
*   **Common Pitfalls:** 
    1. Comparing characters *after* the first difference (e.g., in `wrt` and `wrf`, you only know `t < f`. You know nothing about what comes after).
    2. Failing the prefix edge case: If `words = ["abc", "ab"]`, it's an invalid dictionary because a longer string cannot precede its own prefix. Must return `""`.
*   **Pattern Recognition:** "Order of characters/tasks" + "Dependency" $\to$ Graph + Topological Sort (Kahn's Algorithm BFS or DFS 3-Coloring).

### 3. The Proposed Idea (Base Optimal Solution)
*   **Graph Construction:**
    *   Initialize an adjacency list `Map<Character, List<Character>> adjList` and an `inDegree` map for EVERY unique character in the input (important for chars with no incoming/outgoing edges).
    *   Iterate through pairs of adjacent words.
    *   Find the first differing character. Add an edge `char1 -> char2`. Increment `inDegree` of `char2`. `break` (ignore rest of word).
    *   If `word1` is longer than `word2` and `word1` starts with `word2` (e.g., `abc` before `ab`), return `""`.
*   **Kahn's BFS (Topological Sort):**
    *   Push all nodes with `inDegree == 0` into a Queue.
    *   While Queue is not empty: pop node, append to result string. For each neighbor, decrement their `inDegree`. If it becomes 0, push to Queue.
*   **Cycle Detection:** If the resulting string length != number of unique characters, a cycle exists. Return `""`.
*   **Time Complexity:** $O(C)$ where $C$ is the total length of all words in the input array.
*   **Space Complexity:** $O(U + E) = O(1)$ since $U \le 26$ and $E \le 26^2$.

### 4. Production-Ready Java Implementation (Base)
```java
class Solution {
    public String alienOrder(String[] words) {
        Map<Character, List<Character>> adjList = new HashMap<>();
        Map<Character, Integer> inDegree = new HashMap<>();
        
        // Initialize graphs for every unique character
        for (String word : words) {
            for (char c : word.toCharArray()) {
                adjList.putIfAbsent(c, new ArrayList<>());
                inDegree.putIfAbsent(c, 0);
            }
        }
        
        // Build the graph
        for (int i = 0; i < words.length - 1; i++) {
            String w1 = words[i];
            String w2 = words[i + 1];
            
            // Invalid dictionary check: prefix case
            if (w1.length() > w2.length() && w1.startsWith(w2)) {
                return "";
            }
            
            // Find the first non-matching character
            for (int j = 0; j < Math.min(w1.length(), w2.length()); j++) {
                char parent = w1.charAt(j);
                char child = w2.charAt(j);
                if (parent != child) {
                    adjList.get(parent).add(child);
                    inDegree.put(child, inDegree.get(child) + 1);
                    break; // Only the first differing character implies order
                }
            }
        }
        
        // Topological Sort (Kahn's BFS)
        Queue<Character> queue = new ArrayDeque<>();
        for (char c : inDegree.keySet()) {
            if (inDegree.get(c) == 0) {
                queue.offer(c);
            }
        }
        
        StringBuilder sb = new StringBuilder();
        while (!queue.isEmpty()) {
            char curr = queue.poll();
            sb.append(curr);
            
            for (char neighbor : adjList.get(curr)) {
                inDegree.put(neighbor, inDegree.get(neighbor) - 1);
                if (inDegree.get(neighbor) == 0) {
                    queue.offer(neighbor);
                }
            }
        }
        
        // If string doesn't contain all unique characters, there was a cycle
        if (sb.length() != inDegree.size()) {
            return "";
        }
        
        return sb.toString();
    }
}
```

### 5. Edge Cases & Dry-Run Checklist
*   **Edge Cases Handled:**
    *   `["z", "z"]`: Works fine, no edges added, `z` in-degree is 0. Returns `"z"`.
    *   `["abc", "ab"]`: Caught by the prefix check, returns `""`.
    *   Disconnected graphs (e.g. `["a", "b"]`, no relations): Both have in-degree 0, order doesn't matter, Kahn's outputs `"ab"`.
*   **Dry-Run Checklist:**
    *   Remember the `break`! Only the FIRST mismatched char creates an edge.

### 6. The Google Follow-Up Gauntlet
*   **Follow-Up 1: The Twist - Return ALL valid topological sorts**
    *   *The Twist:* If there are multiple valid alphabets, return all of them.
    *   *The Architectural Pivot:* Kahn's BFS gives only one. To get all permutations, use Backtracking DFS over the `inDegree == 0` nodes. At each step, pick a 0-degree node, subtract degrees from neighbors, recurse, then restore degrees and unpick (backtrack).
*   **Follow-Up 2: The Twist - Dynamic Dictionary**
    *   *The Twist:* The dictionary is provided streamingly `addWord(String word)`. At any point, query `isValid()`.
    *   *The Architectural Pivot:* Maintaining cycle detection incrementally in a directed graph is complex. We maintain an incremental topological order. If adding a word creates a back-edge (violating the current order), we trigger a localized re-sort DFS or Kahn's to update the order. If it fails, `isValid` is false.

## 22. LC 218: The Skyline Problem (Hard)

### 1. The Full Problem Specification
*   **Problem Statement:** A city's skyline is the outer contour of the silhouette formed by all the buildings in that city when viewed from a distance. Given the locations and heights of all the buildings `buildings[i] = [left, right, height]`, return the skyline. The skyline is formatted as a list of "key points" `[x, y]` representing the left endpoints of horizontal line segments.
    *   Constraints: $1 \le buildings.length \le 10^4$.
*   **Input & Output Examples:**
    *   `buildings = [[2,9,10],[3,7,15],[5,12,12],[15,20,10],[19,24,8]]` $\to$ `Output: [[2,10],[3,15],[7,12],[12,0],[15,10],[20,8],[24,0]]`.

### 2. The Core Insights & Tricks
*   **The "Aha!" Moment:** We only care about the contour changing height. The height changes ONLY at the `left` or `right` edges of buildings. We can break down every building into two events: a "Start" event and an "End" event. By sweeping a vertical line from left to right over these events, we can maintain the active buildings using a **PriorityQueue (Max-Heap) or TreeMap**.
*   **Common Pitfalls:** The tie-breaking rules when multiple events happen at the exact same $X$ coordinate.
    1. If two starts happen at the same $X$, process the TALLER building first (so the skyline jumps directly to the max height without intermediate points).
    2. If two ends happen at the same $X$, process the SHORTER building first (so we don't prematurely drop the skyline height).
    3. If a start and an end happen at the same $X$, process the START first (so the skyline height is maintained seamlessly).
*   **Pattern Recognition:** Intervals + Overlapping heights + Critical points $\to$ Line Sweep + Max-Heap/TreeMap.

### 3. The Proposed Idea (Base Optimal Solution)
*   **Event Generation:** For each building `[L, R, H]`, create two events: `[L, -H]` (negative height denotes a Start event, cleverly handling tie-breakers) and `[R, H]` (positive height denotes an End event).
*   **Sorting:** Sort events by $X$ coordinate. If $X$ is the same, sort by height (`a[1] - b[1]`). The negative height trick perfectly handles all 3 tie-breaker pitfalls natively!
*   **Sweep Line:** 
    *   Use a `TreeMap<Integer, Integer>` as a multi-set to store active heights and their counts (Java's PriorityQueue `remove(Object)` is $O(N)$, which causes TLE on testcases with many buildings of the same height. TreeMap gives $O(\log N)$ deletion).
    *   Initialize `TreeMap` with `{0: 1}` (ground level).
    *   Iterate through sorted events.
    *   If it's a Start event (`H < 0`), add `-H` to TreeMap.
    *   If it's an End event (`H > 0`), remove `H` from TreeMap.
    *   Check the current max height `treeMap.lastKey()`. If it differs from the previous max height, we've found a new skyline point. Add `[X, currentMax]` to result.
*   **Time Complexity:** $O(N \log N)$ for sorting events and TreeMap operations.
*   **Space Complexity:** $O(N)$ for events and TreeMap.

### 4. Production-Ready Java Implementation (Base)
```java
class Solution {
    public List<List<Integer>> getSkyline(int[][] buildings) {
        List<int[]> events = new ArrayList<>();
        for (int[] b : buildings) {
            // Negative height for start event ensures tall buildings are processed first if X is same
            events.add(new int[]{b[0], -b[2]});
            // Positive height for end event ensures short buildings are processed first if X is same
            events.add(new int[]{b[1], b[2]});
        }
        
        // Sort by X coordinate. If X is the same, sort by height.
        Collections.sort(events, (a, b) -> {
            if (a[0] != b[0]) return a[0] - b[0];
            return a[1] - b[1];
        });
        
        List<List<Integer>> result = new ArrayList<>();
        // TreeMap to keep track of active building heights: Height -> Count
        TreeMap<Integer, Integer> activeHeights = new TreeMap<>();
        activeHeights.put(0, 1); // Ground level
        
        int prevMaxHeight = 0;
        
        for (int[] event : events) {
            int x = event[0];
            int h = event[1];
            
            if (h < 0) {
                // Start event: add height
                activeHeights.put(-h, activeHeights.getOrDefault(-h, 0) + 1);
            } else {
                // End event: remove height
                int count = activeHeights.get(h);
                if (count == 1) {
                    activeHeights.remove(h);
                } else {
                    activeHeights.put(h, count - 1);
                }
            }
            
            // Check if the maximum height changed
            int currentMaxHeight = activeHeights.lastKey(); // O(log N)
            if (currentMaxHeight != prevMaxHeight) {
                result.add(Arrays.asList(x, currentMaxHeight));
                prevMaxHeight = currentMaxHeight;
            }
        }
        
        return result;
    }
}
```

### 5. Edge Cases & Dry-Run Checklist
*   **Edge Cases Handled:**
    *   Identical buildings `[[1,2,3], [1,2,3]]`: The count in TreeMap handles multiple identical heights perfectly.
    *   Adjacent buildings `[[1,2,3], [2,3,3]]`: Start event of 2nd building is processed *before* End event of 1st building (due to `-3 < 3`). So max height stays 3, no redundant point is added.
*   **Dry-Run Checklist:**
    *   Verify the sorting logic: `[2, -10]` and `[2, -15]` $\to$ `[2, -15]` comes first. Taller building added first. Correct.

### 6. The Google Follow-Up Gauntlet
*   **Follow-Up 1: The Twist - Avoid TreeMap's Overhead**
    *   *The Twist:* The interviewer bans `TreeMap` because it's slow in practice. Use a standard `PriorityQueue` without hitting $O(N^2)$ TLE on deletions.
    *   *The Architectural Pivot:* Use **Lazy Deletion**. Store `{right_x, height}` in the Max-Heap. When sweeping, you *don't* search and remove End events. Instead, you just poll the heap's top element repeatedly in a `while` loop if its `right_x <= current_sweep_x`. 
*   **Follow-Up 2: The Twist - Massive Coordinates (Segment Tree)**
    *   *The Twist:* The problem is online `addBuilding()` and `getSkyline()`.
    *   *The Architectural Pivot:* A sweep line is strictly offline. We must use a Segment Tree with Lazy Propagation, discretizing the X coordinates.

---

## 23. LC 759: Employee Free Time (Hard)

### 1. The Full Problem Specification
*   **Problem Statement:** You are given a list `schedule` of employees, which represents the working time for each employee. Each employee has a list of non-overlapping `Intervals`. Return the list of finite intervals representing common, positive-length free time for all employees, sorted.
*   **Input & Output Examples:**
    *   `schedule = [[[1,2],[5,6]], [[1,3]], [[4,10]]]` $\to$ `Output: [[3,4]]`. (Everyone is free between 3 and 4).

### 2. The Core Insights & Tricks
*   **The "Aha!" Moment:** We don't care *which* employee is working. We only care if *anyone* is working. If we throw all intervals into a single list and merge overlapping intervals, the gaps between the merged intervals are exactly the common free time!
*   **Alternative Aha (The Google Optimal):** We have $K$ lists of intervals, and each list is *already sorted*. Flattening and sorting everything takes $O(N \log N)$. We can use a **Min-Heap (K-way Merge)** to process intervals in sorted order in $O(N \log K)$ time.
*   **Pattern Recognition:** Sorted Lists of Intervals $\to$ K-Way Merge Heap + Merge Intervals.

### 3. The Proposed Idea (Base Optimal Solution)
*   **Structures:**
    *   `PriorityQueue<Job>` tracking `{interval_start, interval_end, employee_id, interval_index}`.
*   **Logic:**
    *   Push the first interval of every employee into the Min-Heap, sorted by `start` time.
    *   Poll the earliest interval. Track the `currentMaxEnd`.
    *   Poll the next interval.
        *   If `nextInterval.start > currentMaxEnd`, we found a gap! Add `[currentMaxEnd, nextInterval.start]` to the result.
        *   Update `currentMaxEnd = Math.max(currentMaxEnd, nextInterval.end)`.
    *   Push the next interval from the same employee's list into the heap (if it exists).
*   **Time Complexity:** $O(N \log K)$ where $N$ is total intervals and $K$ is number of employees.
*   **Space Complexity:** $O(K)$ for the Min-Heap.

### 4. Production-Ready Java Implementation (Base)
```java
// Definition for an Interval.
/*
class Interval {
    public int start;
    public int end;
    public Interval(int _start, int _end) { start = _start; end = _end; }
}
*/
class Solution {
    class Job {
        int empIdx, intervalIdx;
        Interval interval;
        public Job(int empIdx, int intervalIdx, Interval interval) {
            this.empIdx = empIdx;
            this.intervalIdx = intervalIdx;
            this.interval = interval;
        }
    }

    public List<Interval> employeeFreeTime(List<List<Interval>> schedule) {
        List<Interval> result = new ArrayList<>();
        PriorityQueue<Job> pq = new PriorityQueue<>((a, b) -> a.interval.start - b.interval.start);
        
        // Initialize heap with the first interval of each employee
        for (int i = 0; i < schedule.size(); i++) {
            if (!schedule.get(i).isEmpty()) {
                pq.offer(new Job(i, 0, schedule.get(i).get(0)));
            }
        }
        
        if (pq.isEmpty()) return result;
        
        int currentMaxEnd = pq.peek().interval.end;
        
        while (!pq.isEmpty()) {
            Job curr = pq.poll();
            Interval currInterval = curr.interval;
            
            // If the current interval starts after the maximum end time we've seen so far,
            // the gap between them is common free time.
            if (currInterval.start > currentMaxEnd) {
                result.add(new Interval(currentMaxEnd, currInterval.start));
            }
            
            // Update the maximum end time seen so far
            currentMaxEnd = Math.max(currentMaxEnd, currInterval.end);
            
            // Advance to the next interval for this employee
            if (curr.intervalIdx + 1 < schedule.get(curr.empIdx).size()) {
                pq.offer(new Job(curr.empIdx, curr.intervalIdx + 1, schedule.get(curr.empIdx).get(curr.intervalIdx + 1)));
            }
        }
        
        return result;
    }
}
```

### 5. Edge Cases & Dry-Run Checklist
*   **Edge Cases Handled:**
    *   Empty schedules: Handled by skipping empty lists during heap initialization.
    *   Nested intervals: `currentMaxEnd = Math.max(...)` handles an interval completely swallowing another one.
*   **Dry-Run Checklist:**
    *   Verify the heap comparator operates strictly on `start` time. Tie-breaking doesn't matter mathematically because `currentMaxEnd` tracks the boundary.

### 6. The Google Follow-Up Gauntlet
*   **Follow-Up 1: The Twist - Stream of Updates**
    *   *The Twist:* Employees dynamically add meetings to their calendars.
    *   *The Architectural Pivot:* The K-way merge is offline. To handle online range updates, we need an **Interval Tree** or a **TreeMap**. A `TreeMap<Integer, Integer>` (Line Sweep) where we `+1` on start and `-1` on end can find gaps where the running sum hits `0`.
*   **Follow-Up 2: The Twist - Minimum Meeting Duration**
    *   *The Twist:* Only return free time intervals that are at least `M` hours long.
    *   *The Architectural Pivot:* Trivial addition. In the gap check: `if (curr.start - currentMaxEnd >= M) { result.add(...); }`.

---

## 24. LC 1235: Maximum Profit in Job Scheduling (Hard)

### 1. The Full Problem Specification
*   **Problem Statement:** You have `n` jobs, where every job is scheduled to be done from `startTime[i]` to `endTime[i]`, obtaining a profit of `profit[i]`. Return the maximum profit you can take such that there are no two jobs in the subset with overlapping time range. (If you choose a job that ends at time X you will be able to start another job that starts at time X).
    *   Constraints: $1 \le startTime.length = endTime.length = profit.length \le 5 \times 10^4$.
*   **Input & Output Examples:**
    *   `startTime = [1,2,3,3], endTime = [3,4,5,6], profit = [50,10,40,70]` $\to$ `Output: 120`. (Choose job 1 (1-3) and job 4 (3-6) $\to 50 + 70 = 120$).

### 2. The Core Insights & Tricks
*   **The "Aha!" Moment:** This is the classic Activity Selection Problem upgraded with weights. We sort the jobs by their `endTime`. We process jobs one by one. For each job, we have a choice:
    1. Skip it: Profit is the same as the max profit before this job.
    2. Take it: Profit is `profit[i] + (max profit of a valid non-overlapping previous job)`.
    To find the non-overlapping previous job quickly, we use **Binary Search (Bisect-Right)** on the sorted end times. 
*   **Common Pitfalls:** Sorting by `startTime`. While possible (using a Max-Heap to track running profits or right-to-left DP), sorting by `endTime` aligns perfectly with standard DP progression. 
*   **Pattern Recognition:** Non-overlapping intervals + weights/profits $\to$ Sort by End Time + DP + Binary Search.

### 3. The Proposed Idea (Base Optimal Solution)
*   **Structures:**
    *   Array of `Job` objects `(start, end, profit)`. Sort by `end` time.
    *   `TreeMap<Integer, Integer> dp`: Maps `endTime` to `maxProfit` up to that time. (Alternatively, use two arrays `endTimeDP` and `profitDP` and `Arrays.binarySearch`, but `TreeMap` is much cleaner in Java).
*   **Logic:**
    *   Initialize `dp.put(0, 0)` (base case).
    *   For each job:
        *   Find the largest end time in the map that is $\le$ current job's `startTime`. In Java, `dp.floorEntry(job.start)`.
        *   Calculate `currentProfit = job.profit + dp.floorEntry(job.start).getValue()`.
        *   If `currentProfit` is greater than the last recorded max profit (`dp.lastEntry().getValue()`), we add it to the map: `dp.put(job.end, currentProfit)`.
*   **Time Complexity:** $O(N \log N)$ to sort, and $N$ times $O(\log N)$ for TreeMap `floorEntry`. Total $O(N \log N)$.
*   **Space Complexity:** $O(N)$ for jobs array and TreeMap.

### 4. Production-Ready Java Implementation (Base)
```java
class Solution {
    class Job {
        int start, end, profit;
        public Job(int start, int end, int profit) {
            this.start = start; this.end = end; this.profit = profit;
        }
    }

    public int jobScheduling(int[] startTime, int[] endTime, int[] profit) {
        int n = startTime.length;
        Job[] jobs = new Job[n];
        for (int i = 0; i < n; i++) {
            jobs[i] = new Job(startTime[i], endTime[i], profit[i]);
        }
        
        // Sort jobs by end time
        Arrays.sort(jobs, (a, b) -> a.end - b.end);
        
        // TreeMap maps EndTime -> MaxProfit
        TreeMap<Integer, Integer> dp = new TreeMap<>();
        dp.put(0, 0); // Base case
        
        for (Job job : jobs) {
            // Find the maximum profit made up to job.start
            int profitUpToStart = dp.floorEntry(job.start).getValue();
            int currentProfit = profitUpToStart + job.profit;
            
            // Get the maximum profit recorded so far (which is the last entry since we insert monotonically)
            int maxProfitSoFar = dp.lastEntry().getValue();
            
            // If taking this job yields a strictly greater profit, record it at this end time
            if (currentProfit > maxProfitSoFar) {
                dp.put(job.end, currentProfit);
            }
        }
        
        return dp.lastEntry().getValue();
    }
}
```

### 5. Edge Cases & Dry-Run Checklist
*   **Edge Cases Handled:**
    *   Identical end times: TreeMap overwrites the key. However, since the array is sorted, if a later job has the same end time and higher profit, it will overwrite correctly. (Wait! If it has the same end time but lower profit, the `if (currentProfit > maxProfitSoFar)` prevents it from overwriting the good one. Excellent!).
*   **Dry-Run Checklist:**
    *   `dp.floorEntry()` strictly requires $\le$. The problem states jobs starting at time X can overlap with jobs ending at time X. So `floorEntry(job.start)` natively includes perfectly adjacent intervals.

### 6. The Google Follow-Up Gauntlet
*   **Follow-Up 1: The Twist - Return the Chosen Jobs**
    *   *The Twist:* Don't just return the max profit. Return the indices of the jobs that make up the max profit.
    *   *The Architectural Pivot:* The `TreeMap` must store the history of chosen jobs. A naive `List<Integer>` in the map takes $O(N^2)$ space. We must store a `parent` pointer or `previous_dp_state_key` for backtracing, turning the map into `Map<EndTime, DPState>` where `DPState = {maxProfit, currentJobId, previousEndTime}`. Then backtrack from `lastEntry()`.
*   **Follow-Up 2: The Twist - Max 2 Concurrent Jobs**
    *   *The Twist:* You can run up to 2 jobs at the same time (e.g. 2 parallel CPU cores).
    *   *The Architectural Pivot:* The state space expands. `dp[time]` becomes `dp[time][core]`. Alternatively, use Min-Cost Max-Flow graph algorithms, which handles generalized $K$ parallel workers.

# Category E: Complex Trees, Search Spaces & Math Elimination

## 25. LC 124: Binary Tree Maximum Path Sum (Hard)

### 1. The Full Problem Specification
*   **Problem Statement:** A path in a binary tree is a sequence of nodes where each pair of adjacent nodes has an edge connecting them. A node can only appear in the sequence at most once. The path sum is the sum of the node's values in the path. Given the `root` of a binary tree, return the maximum path sum of any non-empty path.
    *   Constraints: The number of nodes is in the range $[1, 3 \times 10^4]$. $-1000 \le Node.val \le 1000$.
*   **Input & Output Examples:**
    *   `root = [1,2,3]` $\to$ `Output: 6`. (Path is `2 -> 1 -> 3`).
    *   `root = [-10,9,20,null,null,15,7]` $\to$ `Output: 42`. (Path is `15 -> 20 -> 7`).

### 2. The Core Insights & Tricks
*   **The "Aha!" Moment:** Every valid path has a "highest" node (the node closest to the root in that path). For any node, the maximum path that treats this node as the "highest" node is `node.val + max(0, left_branch_sum) + max(0, right_branch_sum)`. 
*   However, if this node is *part* of a path where its parent is the highest node, it can only return a SINGLE branch upwards (either its left branch + itself, or its right branch + itself).
*   **Common Pitfalls:** Returning the double-branch sum to the parent. A path cannot fork. Therefore, the recursive function must *return* the single-branch maximum while updating a *global* variable with the double-branch maximum.
*   **Pattern Recognition:** Path spanning across a subtree $\to$ Post-Order Traversal with global maximum state.

### 3. The Proposed Idea (Base Optimal Solution)
*   Initialize a global variable `maxSum = Integer.MIN_VALUE`.
*   Create a recursive `dfs(node)` function that returns the maximum path sum of a *single branch* descending from `node`.
*   Inside `dfs`:
    *   If `node == null`, return `0`.
    *   Recursively call `leftSum = Math.max(0, dfs(node.left))`. (The `max(0, ...)` explicitly handles negative branches. If a branch is entirely negative, we simply don't include it in our path).
    *   Recursively call `rightSum = Math.max(0, dfs(node.right))`.
    *   Calculate the max path sum treating the current node as the peak: `currentPeakSum = node.val + leftSum + rightSum`.
    *   Update global max: `maxSum = Math.max(maxSum, currentPeakSum)`.
    *   Return the max single branch to the parent: `node.val + Math.max(leftSum, rightSum)`.
*   **Time Complexity:** $O(N)$ where $N$ is the number of nodes. Each node is visited exactly once.
*   **Space Complexity:** $O(H)$ where $H$ is the height of the tree (for the recursion stack). Worst case $O(N)$ for a skewed tree.

### 4. Production-Ready Java Implementation (Base)
```java
/**
 * Definition for a binary tree node.
 * public class TreeNode {
 *     int val;
 *     TreeNode left;
 *     TreeNode right;
 *     TreeNode(int x) { val = x; }
 * }
 */
class Solution {
    private int globalMaxSum;

    public int maxPathSum(TreeNode root) {
        globalMaxSum = Integer.MIN_VALUE;
        getSingleBranchMax(root);
        return globalMaxSum;
    }
    
    private int getSingleBranchMax(TreeNode node) {
        if (node == null) {
            return 0;
        }
        
        // Recursively get the max sum from left and right children.
        // If the sum is negative, we drop that branch entirely (hence max with 0).
        int leftBranchSum = Math.max(0, getSingleBranchMax(node.left));
        int rightBranchSum = Math.max(0, getSingleBranchMax(node.right));
        
        // The max path sum evaluating the current node as the "highest" node in the path
        int currentPeakSum = node.val + leftBranchSum + rightBranchSum;
        
        // Update the global maximum if this local peak is better
        globalMaxSum = Math.max(globalMaxSum, currentPeakSum);
        
        // A path going up to the parent can only include ONE of the branches (left or right)
        return node.val + Math.max(leftBranchSum, rightBranchSum);
    }
}
```

### 5. Edge Cases & Dry-Run Checklist
*   **Edge Cases Handled:**
    *   All negative nodes: `[-3]`. `left=0`, `right=0`. `currentPeakSum = -3 + 0 + 0 = -3`. `globalMaxSum = -3`. Returns `-3`. Correct!
    *   `Math.max(0, ...)` is the most crucial part to silently discard toxic negative subtrees.
*   **Dry-Run Checklist:**
    *   Ensure the function returns `node.val + Math.max(left, right)`. If it returned `node.val + left + right`, the path would fork uncontrollably.

### 6. The Google Follow-Up Gauntlet
*   **Follow-Up 1: The Twist - Leaf-to-Leaf Path Only**
    *   *The Twist:* The path MUST start at a leaf node and end at a DIFFERENT leaf node.
    *   *The Architectural Pivot:* The `Math.max(0, ...)` trick is banned because you CANNOT drop a branch; you are forced to go down to a leaf even if it's negative. The global max is ONLY updated if `node.left != null && node.right != null`.
*   **Follow-Up 2: The Twist - Return the actual path**
    *   *The Twist:* Return `List<TreeNode>` representing the path.
    *   *The Architectural Pivot:* The DFS must return a complex object: `class Result { int sum; List<TreeNode> path; }`. When updating the global maximum, we concatenate the left path, the root, and the reversed right path to store the global best path.

---

## 26. LC 297: Serialize and Deserialize Binary Tree (Hard)

### 1. The Full Problem Specification
*   **Problem Statement:** Design an algorithm to serialize and deserialize a binary tree. Serialization is the process of converting a data structure or object into a sequence of bits (string).
*   **Input & Output Examples:**
    *   `root = [1,2,3,null,null,4,5]` $\to$ `String = "1,2,X,X,3,4,X,X,5,X,X"` $\to$ Deserializes back to `[1,2,3,null,null,4,5]`.

### 2. The Core Insights & Tricks
*   **The "Aha!" Moment:** A standard tree traversal (Pre-order, In-order) loses structural information unless you explicitly record `null` pointers. If you record `null` as a special character (e.g., `"X"`), a simple **Pre-Order Traversal** uniquely defines the exact shape and contents of the tree.
*   **Alternative Aha:** Level-Order (BFS) serialization is exactly what LeetCode uses under the hood (e.g., `[1,2,3,null,null,4,5]`), but Pre-Order DFS is drastically easier to implement with a simple recursive function and a `Queue` for deserialization.
*   **Pattern Recognition:** Graph persistence $\to$ DFS Pre-order with explicit Null markers.

### 3. The Proposed Idea (Base Optimal Solution)
*   **Serialization (Pre-Order):**
    *   Base case: If node is null, append `"X,"` to a `StringBuilder`.
    *   Append `node.val + ","`.
    *   Recurse left. Recurse right.
*   **Deserialization:**
    *   Split the string by `","` into an array of strings.
    *   Dump the array into a `Queue<String>` (to process left-to-right easily).
    *   Recursive `buildTree(Queue queue)`:
        *   Pop the next string `val`.
        *   If `val.equals("X")`, return `null`.
        *   Create `TreeNode root = new TreeNode(Integer.parseInt(val))`.
        *   `root.left = buildTree(queue)`.
        *   `root.right = buildTree(queue)`.
        *   Return `root`.
*   **Time Complexity:** $O(N)$ for both.
*   **Space Complexity:** $O(N)$ for string building and the queue.

### 4. Production-Ready Java Implementation (Base)
```java
public class Codec {

    // Encodes a tree to a single string.
    public String serialize(TreeNode root) {
        StringBuilder sb = new StringBuilder();
        buildString(root, sb);
        return sb.toString();
    }
    
    private void buildString(TreeNode node, StringBuilder sb) {
        if (node == null) {
            sb.append("X").append(",");
            return;
        }
        sb.append(node.val).append(",");
        buildString(node.left, sb);
        buildString(node.right, sb);
    }

    // Decodes your encoded data to tree.
    public TreeNode deserialize(String data) {
        // Split drops trailing empty strings, which is fine
        String[] nodes = data.split(",");
        Queue<String> queue = new ArrayDeque<>(Arrays.asList(nodes));
        return buildTree(queue);
    }
    
    private TreeNode buildTree(Queue<String> queue) {
        if (queue.isEmpty()) return null;
        
        String val = queue.poll();
        if (val.equals("X")) {
            return null;
        }
        
        TreeNode node = new TreeNode(Integer.parseInt(val));
        node.left = buildTree(queue);
        node.right = buildTree(queue);
        
        return node;
    }
}
```

### 5. Edge Cases & Dry-Run Checklist
*   **Edge Cases Handled:**
    *   Empty tree (`root = null`): Serializes to `"X,"`. Deserializes: splits to `["X"]`, pops `"X"`, returns `null`. Works perfectly.
    *   Negative numbers: `Integer.parseInt` handles `"-1"` effortlessly.
*   **Dry-Run Checklist:**
    *   `ArrayDeque<>(Arrays.asList(nodes))` is an incredibly fast way to convert the parsed array into a mutable stream.

### 6. The Google Follow-Up Gauntlet
*   **Follow-Up 1: The Twist - Optimize for N-Ary Trees**
    *   *The Twist:* The tree is not binary. Nodes can have infinite children `List<Node> children`.
    *   *The Architectural Pivot:* Pre-order still works, but you must append a special "End of Children" marker (e.g., `]`) or include the number of children in the serialization `val,num_children`. 
*   **Follow-Up 2: The Twist - Binary Search Tree (Omit Nulls)**
    *   *The Twist:* The tree is a strictly valid BST (LC 449). Serialize it as compactly as possible (no `"X"` allowed).
    *   *The Architectural Pivot:* Pre-order traversal *without* nulls uniquely identifies a BST! During deserialization, pass `MIN` and `MAX` bounds down the recursion. If the next queue element is out of bounds, return `null`. This saves $\sim 50\%$ space.

---

## 27. LC 843: Guess the Word (Hard)

### 1. The Full Problem Specification
*   **Problem Statement:** You are given an array of unique strings `words` (length 6) and a hidden `secret` word. You have an API `guess(word)` that returns the number of exact matches (value and position). Find the secret word in $\le 10$ guesses.
    *   Constraints: $100 \le words.length \le 100$. All words are 6 lowercase letters.
*   **Input & Output Examples:**
    *   `secret = "acckzz", words = ["acckzz","ccbazz","eiowzz","abcczz"]`. `guess("acckzz")` returns 6.

### 2. The Core Insights & Tricks
*   **The "Aha!" Moment:** Every time you guess a word $W$, the API returns a score $S$. This means the true `secret` MUST have exactly $S$ matches with $W$. We can filter our list of candidates, throwing away any word that doesn't have exactly $S$ matches with $W$.
*   **The Second "Aha!" (Minimax Strategy):** Randomly picking a word to guess might leave a huge pool of candidates. To minimize the worst-case remaining candidates, we should guess the word that overlaps the *most* with all other words. By picking a "central" word, we ensure the candidate pool shrinks massively no matter what score the API returns.
*   **Pattern Recognition:** "Guessing Game" + "API feedback" $\to$ Candidate Pool Elimination + Minimax Heuristic.

### 3. The Proposed Idea (Base Optimal Solution)
*   **Candidate Pool:** Start with `List<String> candidates = new ArrayList<>(Arrays.asList(words))`.
*   **Loop (up to 10 times):**
    *   Pick a word from `candidates`. (A random pick works 95% of the time, but for 100% Google pass rate, pick the word that shares the most characters in the same positions with the rest of the pool).
    *   **Minimax Heuristic:** Count the frequency of each character at each of the 6 positions across all current candidates. Score each candidate word by summing the frequencies of its characters. Pick the candidate with the highest score.
    *   Call `matches = master.guess(bestWord)`.
    *   If `matches == 6`, we win!
    *   Filter the `candidates` list: keep only words where `exactMatchCount(bestWord, candidate) == matches`.
*   **Time Complexity:** $O(10 \cdot N)$ where $N=100$. Extremely fast.
*   **Space Complexity:** $O(N)$ to store candidates.

### 4. Production-Ready Java Implementation (Base)
```java
/**
 * // This is the Master's API interface.
 * // You should not implement it, or speculate about its implementation
 * interface Master {
 *     public int guess(String word);
 * }
 */
class Solution {
    public void findSecretWord(String[] words, Master master) {
        List<String> candidates = new ArrayList<>();
        for (String w : words) candidates.add(w);
        
        for (int attempt = 0; attempt < 10; attempt++) {
            // 1. Calculate positional letter frequencies in the current candidate pool
            int[][] counts = new int[6][26];
            for (String w : candidates) {
                for (int i = 0; i < 6; i++) {
                    counts[i][w.charAt(i) - 'a']++;
                }
            }
            
            // 2. Score words and pick the one with the highest overlap heuristic
            String bestWord = "";
            int bestScore = -1;
            for (String w : candidates) {
                int score = 0;
                for (int i = 0; i < 6; i++) {
                    score += counts[i][w.charAt(i) - 'a'];
                }
                if (score > bestScore) {
                    bestScore = score;
                    bestWord = w;
                }
            }
            
            // 3. Make the guess
            int matches = master.guess(bestWord);
            if (matches == 6) return;
            
            // 4. Eliminate impossible candidates
            List<String> nextCandidates = new ArrayList<>();
            for (String w : candidates) {
                if (getMatches(bestWord, w) == matches) {
                    nextCandidates.add(w);
                }
            }
            candidates = nextCandidates;
        }
    }
    
    private int getMatches(String a, String b) {
        int matches = 0;
        for (int i = 0; i < 6; i++) {
            if (a.charAt(i) == b.charAt(i)) {
                matches++;
            }
        }
        return matches;
    }
}
```

### 5. Edge Cases & Dry-Run Checklist
*   **Edge Cases Handled:**
    *   API returns `0` matches: The filter `getMatches(bestWord, w) == 0` brilliantly drops a massive chunk of words that share any character with the guess.
*   **Dry-Run Checklist:**
    *   The `counts[i][w.charAt(i) - 'a']` logic successfully acts as a probabilistic proxy. If 'a' is common at position 0, guessing a word starting with 'a' gives us the highest chance of eliminating words if the true secret *doesn't* start with 'a', or confirming it quickly.

### 6. The Google Follow-Up Gauntlet
*   **Follow-Up 1: The Twist - Strict Minimax**
    *   *The Twist:* The interviewer demands the absolute mathematical guarantee that the pool shrinks by the largest possible minimum amount.
    *   *The Architectural Pivot:* Instead of frequency counting, run a full $O(N^2)$ Minimax. For every word A, calculate how it partitions the pool if we guess it. Find the size of the largest partition (the worst-case API response). Pick the word A that has the *smallest* worst-case partition. With $N=100$, $100^2 = 10000$ operations per guess is perfectly optimal.

## 28. LC 719: Find K-th Smallest Pair Distance (Hard)

### 1. The Full Problem Specification
*   **Problem Statement:** The distance of a pair of integers `a` and `b` is defined as the absolute difference between `a` and `b`. Given an integer array `nums` and an integer `k`, return the `k`-th smallest distance among all the pairs `nums[i]` and `nums[j]` where `0 <= i < j < nums.length`.
    *   Constraints: $2 \le nums.length \le 10^4$, $0 \le nums[i] \le 10^6$, $1 \le k \le n(n-1)/2$.
*   **Input & Output Examples:**
    *   `nums = [1,3,1], k = 1` $\to$ `Output: 0`. (Pairs: (1,3)=2, (1,1)=0, (3,1)=2. Smallest is 0).

### 2. The Core Insights & Tricks
*   **The "Aha!" Moment:** There are $O(N^2)$ pairs. Generating them all and sorting takes $O(N^2 \log N)$, which will TLE since $N=10^4 \implies N^2 = 10^8$. 
    *   Instead, what if we guess a distance $D$? Can we quickly count how many pairs have a distance $\le D$? Yes! If we **sort the array**, finding the number of pairs with distance $\le D$ can be done with a **Sliding Window** in $O(N)$ time.
    *   Since the number of pairs $\le D$ increases monotonically as $D$ increases, we can **Binary Search the Answer** space!
*   **Common Pitfalls:** Trying to use a Max-Heap of size K. While valid for $K \le 10^4$, $K$ can be up to $5 \cdot 10^7$, causing a massive memory limit exceeded and TLE.
*   **Pattern Recognition:** "K-th Smallest" on a derived matrix/pairs $\to$ Binary Search on Answer + Sliding Window.

### 3. The Proposed Idea (Base Optimal Solution)
*   **Sort:** Sort `nums`.
*   **Search Space:** Minimum possible distance is `0`. Maximum is `nums[n-1] - nums[0]`.
*   **Binary Search:** 
    *   `left = 0`, `right = nums[n-1] - nums[0]`.
    *   `mid = left + (right - left) / 2`.
    *   **Count Pairs $\le$ mid:** Use a sliding window `[left_ptr, right_ptr]`. For each `right_ptr`, slide `left_ptr` up until `nums[right_ptr] - nums[left_ptr] <= mid`. The number of valid pairs ending at `right_ptr` is `right_ptr - left_ptr`. Add this to a running total.
    *   If `total >= k`: We have $K$ or more pairs with distance $\le mid$. `mid` might be our answer, or we can go smaller. `right = mid`.
    *   If `total < k`: `mid` is too small to be the K-th distance. `left = mid + 1`.
*   **Time Complexity:** $O(N \log N + N \log W)$ where $W$ is the maximum distance in the array.
*   **Space Complexity:** $O(1)$ beyond the sorting space.

### 4. Production-Ready Java Implementation (Base)
```java
class Solution {
    public int smallestDistancePair(int[] nums, int k) {
        Arrays.sort(nums);
        
        int n = nums.length;
        int left = 0; // min possible distance
        int right = nums[n - 1] - nums[0]; // max possible distance
        
        while (left < right) {
            int mid = left + (right - left) / 2;
            
            // Check if there are at least k pairs with distance <= mid
            if (countPairs(nums, mid) >= k) {
                // mid could be the answer, try to find a smaller distance
                right = mid;
            } else {
                // mid is too small
                left = mid + 1;
            }
        }
        
        return left;
    }
    
    // Sliding window to count pairs with distance <= maxDistance in O(N)
    private int countPairs(int[] nums, int maxDistance) {
        int count = 0;
        int leftPtr = 0;
        
        for (int rightPtr = 0; rightPtr < nums.length; rightPtr++) {
            // Shrink window if distance exceeds maxDistance
            while (nums[rightPtr] - nums[leftPtr] > maxDistance) {
                leftPtr++;
            }
            // Number of pairs ending at rightPtr that satisfy the condition
            count += rightPtr - leftPtr;
        }
        
        return count;
    }
}
```

### 5. Edge Cases & Dry-Run Checklist
*   **Edge Cases Handled:**
    *   `k = 1`, all elements identical `[1, 1, 1]`: `right = 0`. Loop doesn't execute. Returns `left = 0`. Correct.
*   **Dry-Run Checklist:**
    *   The `count += rightPtr - leftPtr` logic guarantees we count distinct pairs cleanly without $O(N^2)$ loops. E.g. window `[1, 3, 4]`, distance $\le 3$. At `4`, `4-1=3`. Valid. Added pairs ending at `4` are `(1,4)` and `(3,4)`. `rightPtr(2) - leftPtr(0) = 2`. Spot on!

### 6. The Google Follow-Up Gauntlet
*   **Follow-Up 1: The Twist - Distributed Matrix (K-th Smallest in Multiplication Table LC 668)**
    *   *The Twist:* Instead of pair differences from an array, find the K-th smallest in an $M \times N$ multiplication table.
    *   *The Architectural Pivot:* The exact same Binary Search on Answer logic applies! The only thing that changes is `countPairs`. For a multiplication table, you don't even need an array in memory. You iterate through rows $1$ to $M$, and the number of elements $\le X$ in row $i$ is `Math.min(X / i, N)`. This gives $O(M \log(M \cdot N))$ time, completely bypassing generating the matrix.

---

## 29. LC 4: Median of Two Sorted Arrays (Hard)

### 1. The Full Problem Specification
*   **Problem Statement:** Given two sorted arrays `nums1` and `nums2` of size `m` and `n` respectively, return the median of the two sorted arrays. The overall run time complexity should be $O(\log(m+n))$.
    *   Constraints: $0 \le m, n \le 1000$, $1 \le m+n \le 2000$.
*   **Input & Output Examples:**
    *   `nums1 = [1,3], nums2 = [2]` $\to$ `Output: 2.00000`.
    *   `nums1 = [1,2], nums2 = [3,4]` $\to$ `Output: 2.50000`.

### 2. The Core Insights & Tricks
*   **The "Aha!" Moment:** Finding the median is identical to partitioning the combined arrays into two halves of equal length, such that every element in the left half is $\le$ every element in the right half. We only need to binary search the partition point in the **smaller** array. The partition point in the larger array is strictly derived from the smaller array's partition to maintain equal half-lengths!
*   **Common Pitfalls:** 
    1. Binary searching the larger array (causes index out of bounds on the derived smaller array).
    2. Failing to handle edges where the partition is at index `0` or `length` (requires assigning `-INF` or `+INF` to simulate empty partitions).
*   **Pattern Recognition:** Two Sorted Arrays + Median/K-th element $\to$ Partition Binary Search.

### 3. The Proposed Idea (Base Optimal Solution)
*   Ensure `nums1` is the smaller array.
*   `left = 0`, `right = nums1.length`.
*   Total half length = `(m + n + 1) / 2`.
*   While `left <= right`:
    *   `partitionX = left + (right - left) / 2`.
    *   `partitionY = halfLength - partitionX`.
    *   `maxLeftX` = (partitionX == 0) ? -INF : `nums1[partitionX - 1]`.
    *   `minRightX` = (partitionX == m) ? +INF : `nums1[partitionX]`.
    *   `maxLeftY` = (partitionY == 0) ? -INF : `nums2[partitionY - 1]`.
    *   `minRightY` = (partitionY == n) ? +INF : `nums2[partitionY]`.
    *   **Validity Check:** If `maxLeftX <= minRightY` AND `maxLeftY <= minRightX`, we found the perfect partition!
        *   If `m + n` is odd: Return `max(maxLeftX, maxLeftY)`.
        *   If even: Return `(max(maxLeftX, maxLeftY) + min(minRightX, minRightY)) / 2.0`.
    *   **Shift:** If `maxLeftX > minRightY`, we are too far right in `nums1`. `right = partitionX - 1`.
    *   Else: `left = partitionX + 1`.
*   **Time Complexity:** $O(\log(\min(m, n)))$.
*   **Space Complexity:** $O(1)$.

### 4. Production-Ready Java Implementation (Base)
```java
class Solution {
    public double findMedianSortedArrays(int[] nums1, int[] nums2) {
        // Enforce nums1 is the smaller array to prevent out of bounds on partitionY
        if (nums1.length > nums2.length) {
            return findMedianSortedArrays(nums2, nums1);
        }
        
        int x = nums1.length;
        int y = nums2.length;
        
        int left = 0;
        int right = x; // Binary searching the partition index, not the array indices
        int halfLength = (x + y + 1) / 2;
        
        while (left <= right) {
            int partitionX = left + (right - left) / 2;
            int partitionY = halfLength - partitionX;
            
            // Edge cases: if partition is at edges, substitute with INF / -INF
            int maxLeftX = (partitionX == 0) ? Integer.MIN_VALUE : nums1[partitionX - 1];
            int minRightX = (partitionX == x) ? Integer.MAX_VALUE : nums1[partitionX];
            
            int maxLeftY = (partitionY == 0) ? Integer.MIN_VALUE : nums2[partitionY - 1];
            int minRightY = (partitionY == y) ? Integer.MAX_VALUE : nums2[partitionY];
            
            // Check if partition is valid
            if (maxLeftX <= minRightY && maxLeftY <= minRightX) {
                // Found it
                if ((x + y) % 2 == 0) {
                    return ((double) Math.max(maxLeftX, maxLeftY) + Math.min(minRightX, minRightY)) / 2;
                } else {
                    return (double) Math.max(maxLeftX, maxLeftY);
                }
            } else if (maxLeftX > minRightY) {
                // We are too far on right side for partitionX. Go on left side.
                right = partitionX - 1;
            } else {
                // We are too far on left side for partitionX. Go on right side.
                left = partitionX + 1;
            }
        }
        
        throw new IllegalArgumentException("Input arrays are not sorted.");
    }
}
```

### 5. Edge Cases & Dry-Run Checklist
*   **Edge Cases Handled:**
    *   One array is empty: The swap guarantees `nums1` is the empty one. `partitionX` hits 0, returning `-INF` and `+INF`. `partitionY` evaluates the median cleanly.
*   **Dry-Run Checklist:**
    *   Verify the even parity cast to `(double)` before division, otherwise Java will integer divide and truncate the `.5`!

### 6. The Google Follow-Up Gauntlet
*   **Follow-Up 1: The Twist - Find the K-th Element**
    *   *The Twist:* Instead of the median, find the K-th smallest element.
    *   *The Architectural Pivot:* The logic is essentially identical. `halfLength` becomes `k`. You must cap the `right` boundary of the binary search to `Math.min(x, k)` to prevent overflow.
*   **Follow-Up 2: The Twist - Unsorted Streams**
    *   *The Twist:* The input isn't sorted arrays, but a continuous unsorted data stream `addNum(val)` and `findMedian()`.
    *   *The Architectural Pivot:* (This is LC 295 Find Median from Data Stream). Use Two Heaps. A Max-Heap for the lower half and a Min-Heap for the upper half. Keep them balanced.

---

## 30. LC 212: Word Search II (Hard)

### 1. The Full Problem Specification
*   **Problem Statement:** Given an `m x n` `board` of characters and a list of strings `words`, return all words on the board. Each word must be constructed from letters of sequentially adjacent cells (horizontally or vertically). The same letter cell may not be used more than once in a word.
*   **Input & Output Examples:**
    *   `board = [["o","a","a","n"],["e","t","a","e"],["i","h","k","r"],["i","f","l","v"]]`, `words = ["oath","pea","eat","rain"]` $\to$ `Output: ["eat","oath"]`.

### 2. The Core Insights & Tricks
*   **The "Aha!" Moment:** Running standard DFS for *every* word is $O(W \cdot 4^L)$ which TLEs. Instead, we should insert all words into a **Trie (Prefix Tree)**, and run DFS on the grid *once*. As we traverse the grid, we simultaneously traverse the Trie. If our current grid string is not a prefix in the Trie, we prune the search instantly!
*   **The Google Secret Sauce (Optimization):** To avoid duplicate results, once a word is found, set its `word` marker in the Trie to `null`. To drastically speed up processing, **prune empty leaf nodes dynamically**. If a Trie node becomes useless (all its words found), delete it from its parent. This reduces redundant DFS paths by 50%+.
*   **Pattern Recognition:** Grid Search + Multiple Words $\to$ Trie + Backtracking DFS.

### 3. The Proposed Idea (Base Optimal Solution)
*   **Trie Node:** Contains `TrieNode[] children = new TrieNode[26]` and `String word = null`.
*   **Logic:**
    *   Build Trie from `words`.
    *   Iterate through grid `(r, c)`.
    *   If `board[r][c]` exists in Trie root, launch DFS.
    *   In DFS:
        *   Store original char `c`, mark cell as visited (e.g. `board[r][c] = '#'`).
        *   If `node.word != null`, we found a word! Add to results, and set `node.word = null` (prevents dupes).
        *   Recurse to 4 neighbors.
        *   Backtrack: `board[r][c] = c`.
        *   **Optimization:** If the current `TrieNode` has no children, tell the parent to sever the link.
*   **Time Complexity:** $O(M \cdot N \cdot 4^L)$ in the extreme theoretical worst case, but practically bounded by $O(M \cdot N)$ and Trie size due to massive pruning.
*   **Space Complexity:** $O(\text{Sum of all word lengths})$ for Trie + $O(L)$ recursion depth.

### 4. Production-Ready Java Implementation (Base)
```java
class Solution {
    class TrieNode {
        TrieNode[] children = new TrieNode[26];
        String word = null;
    }
    
    public List<String> findWords(char[][] board, String[] words) {
        // 1. Build the Trie
        TrieNode root = new TrieNode();
        for (String w : words) {
            TrieNode node = root;
            for (char c : w.toCharArray()) {
                int idx = c - 'a';
                if (node.children[idx] == null) {
                    node.children[idx] = new TrieNode();
                }
                node = node.children[idx];
            }
            node.word = w; // Store the word at the leaf
        }
        
        List<String> result = new ArrayList<>();
        int m = board.length;
        int n = board[0].length;
        
        // 2. Backtracking DFS
        for (int r = 0; r < m; r++) {
            for (int c = 0; c < n; c++) {
                if (root.children[board[r][c] - 'a'] != null) {
                    dfs(board, r, c, root, result);
                }
            }
        }
        
        return result;
    }
    
    private void dfs(char[][] board, int r, int c, TrieNode parent, List<String> result) {
        char letter = board[r][c];
        TrieNode currNode = parent.children[letter - 'a'];
        
        // We found a match
        if (currNode.word != null) {
            result.add(currNode.word);
            currNode.word = null; // De-duplicate
        }
        
        // Mark visited
        board[r][c] = '#';
        
        int[][] dirs = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        for (int[] dir : dirs) {
            int nr = r + dir[0];
            int nc = c + dir[1];
            if (nr >= 0 && nr < board.length && nc >= 0 && nc < board[0].length) {
                char nextChar = board[nr][nc];
                if (nextChar != '#' && currNode.children[nextChar - 'a'] != null) {
                    dfs(board, nr, nc, currNode, result);
                }
            }
        }
        
        // Backtrack
        board[r][c] = letter;
        
        // OPTIMIZATION: Incrementally remove leaf nodes
        boolean hasChildren = false;
        for (TrieNode child : currNode.children) {
            if (child != null) {
                hasChildren = true;
                break;
            }
        }
        if (!hasChildren) {
            parent.children[letter - 'a'] = null; // Sever the link
        }
    }
}
```

### 5. Edge Cases & Dry-Run Checklist
*   **Edge Cases Handled:**
    *   Duplicate prefixes (e.g. `app`, `apple`): When DFS hits `app`, it adds it, nullifies the word, but keeps traversing down to `apple`.
*   **Dry-Run Checklist:**
    *   The `currNode.word = null` guarantees we don't return duplicates if a word can be formed multiple ways on the board!

### 6. The Google Follow-Up Gauntlet
*   **Follow-Up 1: The Twist - Massive Boggle Board (Parallelization)**
    *   *The Twist:* The board is $10^5 \times 10^5$.
    *   *The Architectural Pivot:* DFS recursion will stack overflow. You must pivot to BFS, OR split the board into chunks and run multi-threaded workers. The Trie must be accessed concurrently (Read locks on Trie traversal, Write locks ONLY when nullifying a found word and pruning).
*   **Follow-Up 2: The Twist - Prefix Wildcards**
    *   *The Twist:* Words can contain `.` which matches any character.
    *   *The Architectural Pivot:* Inside the Trie DFS, if the word has `.`, you must loop over all 26 children of `currNode` and fork the DFS exploration to all valid paths!
