# NeetCode 150 Revision Guide

## 1. Arrays and Hashing

### Core Theory & Patterns

1.  **Frequency Mapping:** 
    *   Use `int[26]` for lowercase English letters (`c - 'a'`). It is faster than `HashMap` due to cache locality.
    *   **Interview Tip:** If the range of values is small (e.g., ASCII 0-127), use a fixed-size array. If the range is large or sparse (e.g., full range of `int`), use a `HashMap`.
2.  **HashSet for Uniqueness:** `Set.add(x)` returns `false` if `x` already exists. Use this for O(1) duplicate detection.
3.  **The Complement Pattern:** Instead of searching for two elements, calculate the "complement" needed and look it up in O(1).
4.  **Bucket Sort Pattern:** If results are based on counts and those counts are bounded by the input size $N$, you can achieve **O(N)** time by using counts as indices in a "Bucket" array.
5.  **Sorting as a Key:** For anagrams or grouped states, a sorted string or a frequency string (e.g., `"1#2#0..."`) makes an ideal `HashMap` key.
6.  **Trade-offs (Space vs. Time):** 
    *   **Sorting:** Usually $O(N \log N)$ time and $O(1)$ space.
    *   **Hashing:** Usually $O(N)$ time and $O(N)$ space.
    *   **Interviewers often ask:** "Can you solve this with $O(1)$ space?" -> Usually implies Sorting or In-place modification.

---

### Q1: Contains Duplicate (LC 217)
**Example:** `nums = [1, 2, 3, 1]` -> `true`  
**Explanation:** Return true if any value appears at least twice. 

**Pattern:** $O(N)$ Space/Time trade-off using a `HashSet`.

```java
public boolean containsDuplicate(int[] nums) {
    Set<Integer> seen = new HashSet<>();
    for (int num : nums) {
        if (!seen.add(num)) return true;
    }
    return false;
}
```

---

### Q2: Valid Anagram (LC 242)
**Example:** `s = "anagram", t = "nagaram"` -> `true`  
**Explanation:** Check if `t` is a rearrangement of `s`.

**Key Point:** Use a frequency array of size 26. Increment for `s`, decrement for `t`.

```java
public boolean isAnagram(String s, String t) {
    if (s.length() != t.length()) return false;
    int[] freq = new int[26];
    for (int i = 0; i < s.length(); i++) {
        freq[s.charAt(i) - 'a']++;
        freq[t.charAt(i) - 'a']--;
    }
    for (int count : freq) {
        if (count != 0) return false;
    }
    return true;
}
```

---

### Q3: Two Sum (LC 1)
**Example:** `nums = [2,7,11,15], target = 9` -> `[0,1]`  
**Explanation:** Find indices of two numbers that add up to target.

**Pattern:** **One-pass Hash Map**. Search for the complement ($target - current$) while populating the map to ensure you don't use the same element twice.
**Trade-off:** $O(N)$ time vs $O(N^2)$ brute force.

```java
public int[] twoSum(int[] nums, int target) {
    Map<Integer, Integer> map = new HashMap<>();
    for (int i = 0; i < nums.length; i++) {
        int complement = target - nums[i];
        if (map.containsKey(complement)) {
            return new int[] { map.get(complement), i };
        }
        map.put(nums[i], i);
    }
    return new int[] {};
}
```

---

### Q4: Group Anagrams (LC 49)
**Example:** `strs = ["eat","tea","tan","ate","nat","bat"]`  
**Output:** `[["bat"],["nat","tan"],["ate","eat","tea"]]`  

**Pattern:** **Canonical Form**. Anagrams share the same "sorted" version or "frequency count".
**Optimization:** For very long strings, sorting is $O(K \log K)$. Using a frequency-based string (e.g., `"2#1#0..."`) as a key is $O(K)$.

```java
public List<List<String>> groupAnagrams(String[] strs) {
    Map<String, List<String>> res = new HashMap<>();
    for (String s : strs) {
        // Alternate Pattern: Frequency string key
        // int[] count = new int[26];
        // for(char c : s.toCharArray()) count[c - 'a']++;
        // StringBuilder sb = new StringBuilder();
        // for(int i : count) sb.append("#").append(i);
        // String key = sb.toString();

        // Standard Pattern: Sorting key
        char[] ca = s.toCharArray();
        Arrays.sort(ca);
        String key = String.valueOf(ca);
        
        // computeIfAbsent creates the list if the key doesn't exist
        res.computeIfAbsent(key, k -> new ArrayList<>()).add(s);
    }
    return new ArrayList<>(res.values());
}
```

---

### Q5: Top K Frequent Elements (LC 347)
**Example:** `nums = [1,1,1,2,2,3], k = 2` -> `[1,2]`  
**Explanation:** Return the `k` most frequent elements in **O(N)**.

**Pattern:** **Bucket Sort**. Instead of a Max-Heap ($O(N \log K)$), use an array where `array[i]` contains numbers with frequency `i`. This is $O(N)$ because frequencies are bounded by array length.

```java
public int[] topKFrequent(int[] nums, int k) {
    // 1. Count frequencies
    Map<Integer, Integer> count = new HashMap<>();
    for (int n : nums) {
        count.put(n, count.getOrDefault(n, 0) + 1);
    }

    // 2. Bucket Sort: Index is the frequency, value is the list of numbers
    // Max frequency is nums.length
    List<Integer>[] buckets = new List[nums.length + 1];
    for (int num : count.keySet()) {
        int freq = count.get(num);
        if (buckets[freq] == null) {
            buckets[freq] = new ArrayList<>();
        }
        buckets[freq].add(num);
    }

    // 3. Collect top k from right to left
    int[] res = new int[k];
    int idx = 0;
    for (int i = buckets.length - 1; i >= 0 && idx < k; i--) {
        if (buckets[i] != null) {
            for (int num : buckets[i]) {
                res[idx++] = num;
                if (idx == k) return res;
            }
        }
    }
    return res;
}
```
---

### Q6: Product of Array Except Self (LC 238)
**Example:** `nums = [1,2,3,4]` -> `[24,12,8,6]`  
**Explanation:** Find products of all elements except `nums[i]` without using division in **O(N)**.

**Pattern:** **Precomputation (Prefix/Suffix)**. $ans[i] = (\text{product of elements to the left}) \times (\text{product of elements to the right})$. 

```java
public int[] productExceptSelf(int[] nums) {
    int n = nums.length;
    int[] res = new int[n];
    
    // Pass 1: Prefix products
    res[0] = 1;
    for (int i = 1; i < n; i++) {
        res[i] = res[i - 1] * nums[i - 1];
    }
    
    // Pass 2: Suffix products
    int suffix = 1;
    for (int i = n - 1; i >= 0; i--) {
        res[i] *= suffix;
        suffix *= nums[i];
    }
    return res;
}
```

---

### Q7: Valid Sudoku (LC 36)
**Example:** Standard 9x9 Sudoku board.  
**Explanation:** Check if filled cells are valid (no repeats in row, col, or 3x3 box).

**Pattern:** **Composite Keys**. Track three constraints simultaneously.
**Optimization:** Use bitmasks (`int[9]`) instead of `HashSet<String>` for $O(1)$ space/time in a professional HFT/low-latency context.

```java
public boolean isValidSudoku(char[][] board) {
    Set<String> seen = new HashSet<>();
    for (int r = 0; r < 9; r++) {
        for (int c = 0; c < 9; c++) {
            char val = board[r][c];
            if (val == '.') continue;
            
            // Try to add unique identifiers to the set
            if (!seen.add(val + " in row " + r) ||
                !seen.add(val + " in col " + c) ||
                !seen.add(val + " in box " + r/3 + "-" + c/3)) {
                return false;
            }
        }
    }
    return true;
}
```

---

### Q8: Longest Consecutive Sequence (LC 128)
**Example:** `nums = [100,4,200,1,3,2]` -> `4` ([1, 2, 3, 4])  
**Explanation:** Find length of the longest consecutive sequence in **O(N)**.

**Pattern:** **Sequence Starting Point**. A number $n$ is the start of a sequence only if $n-1$ is missing.
**Crucial Step:** This ensures each number is visited only twice (once to put in set, once to process in a sequence), maintaining $O(N)$.

```java
public int longestConsecutive(int[] nums) {
    Set<Integer> set = new HashSet<>();
    for (int n : nums) set.add(n);
    
    int longest = 0;
    for (int n : set) {
        // Check if n is the start of a sequence
        if (!set.contains(n - 1)) {
            int currentNum = n;
            int streak = 1;
            
            while (set.contains(currentNum + 1)) {
                currentNum++;
                streak++;
            }
            longest = Math.max(longest, streak);
        }
    }
    return longest;
}
```

---

### Q9: Encode and Decode Strings (LC 271)
**Example:** `["lint","code"]` -> `"4#lint4#code"`  
**Explanation:** Design an algorithm to encode/decode a list of strings.

**Pattern:** **Length-Prefixing**. Like HTTP Chunked Transfer Encoding, prepending the length allows you to know exactly how many characters to skip, regardless of the content (including symbols or delimiters).

```java
public String encode(List<String> strs) {
    StringBuilder sb = new StringBuilder();
    for (String s : strs) sb.append(s.length()).append("#").append(s);
    return sb.toString();
}

public List<String> decode(String s) {
    List<String> res = new ArrayList<>();
    int i = 0;
    while (i < s.length()) {
        int j = s.indexOf('#', i);
        int len = Integer.parseInt(s.substring(i, j));
        res.add(s.substring(j + 1, j + 1 + len));
        i = j + 1 + len;
    }
    return res;
}
```
---

## 2. Two Pointers

### Core Theory & Patterns

1.  **Converging Pointers (Opposite Ends):** Used primarily on **sorted** arrays. `left` starts at 0, `right` at $N-1$. Move them toward each other to shrink the search space from $O(N^2)$ to $O(N)$.
2.  **The "Shrinking Search Space" Logic:** In problems like *Two Sum II* or *Container with Most Water*, we prove that moving the pointer from the "better" or "shorter" side is the only way to find a potentially superior solution.
3.  **Non-Sorted Two Pointers:** Used for *Valid Palindrome* (skipping junk) or *Trapping Rain Water* (maintaining regional maximums).
4.  **Handling Duplicates:** In $N$-sum problems (3Sum, 4Sum), sorting is mandatory. Skip duplicates using `while (l < r && nums[l] == nums[l+1]) l++` to avoid redundant results in your $O(N^2)$ or $O(N^{k-1})$ solution.
5.  **Space Efficiency:** Two Pointers usually achieve **$O(1)$ auxiliary space**, which is a common follow-up requirement after providing a Hashing-based solution.

---

### Q1: Valid Palindrome (LC 125)
**Example:** `"A man, a plan, a canal: Panama"` -> `true`  
**Explanation:** Check if a string is a palindrome, ignoring non-alphanumeric characters and case.

**Pattern:** Converging pointers. Use `Character.isLetterOrDigit` to skip "junk".

```java
public boolean isPalindrome(String s) {
    int l = 0, r = s.length() - 1;
    while (l < r) {
        // Skip non-alphanumeric from left and right
        while (l < r && !Character.isLetterOrDigit(s.charAt(l))) l++;
        while (l < r && !Character.isLetterOrDigit(s.charAt(r))) r--;
        
        if (Character.toLowerCase(s.charAt(l)) != Character.toLowerCase(s.charAt(r))) {
            return false;
        }
        l++; r--;
    }
    return true;
}
```

---

### Q2: Two Sum II - Input Array Is Sorted (LC 167)
**Example:** `nums = [2,7,11,15], target = 9` -> `[1, 2]` (1-indexed)  
**Explanation:** Find two numbers that sum to target in a sorted array using $O(1)$ space.

**Pattern:** Opposite ends. If `sum > target`, the only way to decrease it is moving the `right` pointer in.

```java
public int[] twoSum(int[] numbers, int target) {
    int l = 0, r = numbers.length - 1;
    while (l < r) {
        int sum = numbers[l] + numbers[r];
        if (sum == target) return new int[]{l + 1, r + 1};
        if (sum < target) l++;
        else r--;
    }
    return new int[]{-1, -1};
}
```

---

### Q3: 3Sum (LC 15)
**Example:** `nums = [-1,0,1,2,-1,-4]` -> `[[-1,-1,2],[-1,0,1]]`  
**Explanation:** Find all unique triplets that sum to zero.

**Pattern:** **Sort + Fixed Pointer + Two Pointers**. Fix one number `i`, then solve `Two Sum II` for the remaining array `[i+1, n-1]`.
**Crucial:** To avoid duplicates, skip the same value for `i`, `l`, and `r` after a match.

```java
public List<List<Integer>> threeSum(int[] nums) {
    Arrays.sort(nums);
    List<List<Integer>> res = new ArrayList<>();
    
    for (int i = 0; i < nums.length - 2; i++) {
        // Skip duplicate starting elements
        if (i > 0 && nums[i] == nums[i - 1]) continue;
        
        int l = i + 1, r = nums.length - 1;
        while (l < r) {
            int sum = nums[i] + nums[l] + nums[r];
            if (sum == 0) {
                res.add(Arrays.asList(nums[i], nums[l], nums[r]));
                l++;
                // Skip duplicate left pointers
                while (l < r && nums[l] == nums[l - 1]) l++;
            } else if (sum < 0) l++;
            else r--;
        }
    }
    return res;
}
```

---

### Q4: Container With Most Water (LC 11)
**Example:** `height = [1,8,6,2,5,4,8,3,7]` -> `49`  
**Explanation:** Find two lines that together with the x-axis forms a container containing the most water.

**Pattern:** **Greedy Two Pointers**. Water volume is limited by the **shorter** line.
**Logic:** Moving the pointer at the taller line can only decrease width without increasing height. Moving the shorter line is the only way to potentially find a taller bottleneck.

```java
public int maxArea(int[] height) {
    int l = 0, r = height.length - 1;
    int maxArea = 0;
    
    while (l < r) {
        int h = Math.min(height[l], height[r]);
        maxArea = Math.max(maxArea, h * (r - l));
        
        // Always move the pointer with the smaller height
        if (height[l] < height[r]) l++;
        else r--;
    }
    return maxArea;
}
```

---

### Q5: Trapping Rain Water (LC 42)
**Example:** `height = [0,1,0,2,1,0,1,3,2,1,2,1]` -> `6`  
**Explanation:** Compute how much water it can trap after raining.

**Pattern:** **Two Pointer Global Maximums**.
**Logic:** Water trapped at index `i` is `min(leftMax, rightMax) - height[i]`. Instead of two passes (O(N) space), use two pointers. If `leftMax < rightMax`, the bottleneck is on the left; process the left pointer and move inward.

```java
public int trap(int[] height) {
    if (height == null || height.length == 0) return 0;
    
    int l = 0, r = height.length - 1;
    int leftMax = height[l], rightMax = height[r];
    int res = 0;
    
    while (l < r) {
        // The side with the smaller max determines the water level
        if (leftMax < rightMax) {
            l++;
            leftMax = Math.max(leftMax, height[l]);
            res += leftMax - height[l];
        } else {
            r--;
            rightMax = Math.max(rightMax, height[r]);
            res += rightMax - height[r];
        }
    }
    return res;
}
```
---

## 3. Sliding Window

### Core Theory & Patterns

1.  **Fixed vs. Variable Window:**
    *   **Fixed:** The window size $K$ is constant. Move by adding the new element at `right` and removing the one at `left = right - K`.
    *   **Variable:** The window expands until a constraint is broken, then "shrinks" from the left until valid again.
2.  **The Monotonicity Property:** Sliding window works when adding an element to the window changes the state in one direction (e.g., sum increases) and removing it changes it in the other. If negative numbers are involved (for sums), use **Prefix Sums** instead.
3.  **Frequency Arrays for Speed:** In HFT or low-latency interviews, use `int[26]` or `int[128]` instead of `HashMap`. Comparing two `int[26]` arrays is $O(26)$, which is effectively $O(1)$.
4.  **State Tracking:** Keep a variable (like `matches` or `need`) to avoid re-scanning the entire window at every step.

---

### Q1: Best Time to Buy and Sell Stock (LC 121)
**Example:** `prices = [7,1,5,3,6,4]` -> `5` (Buy at 1, sell at 6)  
**Explanation:** Find the maximum profit from one transaction.

**Pattern:** **Two-Pointer Sliding Window**. Maintain a `minPrice` (the "left" bottleneck) and calculate profit as `current - minPrice`.

```java
public int maxProfit(int[] prices) {
    int minPrice = Integer.MAX_VALUE;
    int maxProfit = 0;
    for (int price : prices) {
        if (price < minPrice) minPrice = price; // Update the buy point
        else maxProfit = Math.max(maxProfit, price - minPrice); // Update potential profit
    }
    return maxProfit;
}
```

---

### Q2: Longest Substring Without Repeating Characters (LC 3)
**Example:** `"abcabcbb"` -> `3` ("abc")  
**Explanation:** Find the length of the longest substring with unique characters.

**Pattern:** **Variable Window + HashSet/Frequency Map**. Expand `right`. If a duplicate is found, shrink `left` until the duplicate is removed.

```java
public int lengthOfLongestSubstring(String s) {
    Set<Character> charSet = new HashSet<>();
    int l = 0, res = 0;
    for (int r = 0; r < s.length(); r++) {
        // If duplicate found, shrink window from left
        while (charSet.contains(s.charAt(r))) {
            charSet.remove(s.charAt(l));
            l++;
        }
        charSet.add(s.charAt(r));
        res = Math.max(res, r - l + 1);
    }
    return res;
}
```

---

### Q3: Longest Repeating Character Replacement (LC 424)
**Example:** `s = "AABABBA", k = 1` -> `4`  
**Explanation:** You can flip $k$ characters. Find the longest substring containing the same letter.

**Pattern:** **Non-Shrinkable Window**. Constraint: `windowSize - maxFrequency <= k`. 
**Insight:** We don't actually need the *current* max frequency; we only care when `maxFrequency` increases.

```java
public int characterReplacement(String s, int k) {
    int[] count = new int[26];
    int l = 0, maxF = 0, res = 0;
    for (int r = 0; r < s.length(); r++) {
        maxF = Math.max(maxF, ++count[s.charAt(r) - 'a']);
        
        // If swaps needed > k, shift the whole window (non-shrinking)
        if ((r - l + 1) - maxF > k) {
            count[s.charAt(l) - 'a']--;
            l++;
        }
        res = Math.max(res, r - l + 1);
    }
    return res;
}
```

---

### Q4: Permutation in String (LC 567)
**Example:** `s1 = "ab", s2 = "eidbaooo"` -> `true`  
**Explanation:** Check if a permutation of `s1` is a substring of `s2`.

**Pattern:** **Fixed Window ($O(26)$ or $O(1)$ comparison)**. Use two frequency arrays. Instead of `Arrays.equals`, track a `matches` count of characters having the same frequency to reach true $O(N)$.

```java
public boolean checkInclusion(String s1, String s2) {
    if (s1.length() > s2.length()) return false;
    int[] s1Count = new int[26], s2Count = new int[26];
    for (int i = 0; i < s1.length(); i++) {
        s1Count[s1.charAt(i) - 'a']++;
        s2Count[s2.charAt(i) - 'a']++;
    }
    
    int matches = 0;
    for (int i = 0; i < 26; i++) if (s1Count[i] == s2Count[i]) matches++;

    for (int r = s1.length(); r < s2.length(); r++) {
        if (matches == 26) return true;
        
        int add = s2.charAt(r) - 'a', remove = s2.charAt(r - s1.length()) - 'a';
        
        // Update state for 'add'
        s2Count[add]++;
        if (s2Count[add] == s1Count[add]) matches++;
        else if (s2Count[add] == s1Count[add] + 1) matches--;
        
        // Update state for 'remove'
        s2Count[remove]--;
        if (s2Count[remove] == s1Count[remove]) matches++;
        else if (s2Count[remove] == s1Count[remove] - 1) matches--;
    }
    return matches == 26;
}
```

---

### Q5: Minimum Window Substring (LC 76)
**Example:** `s = "ADOBECODEBANC", t = "ABC"` -> `"BANC"`  
**Explanation:** Find the smallest substring in `s` that contains all characters of `t`.

**Pattern:** **Variable Window + Need/Have Tracking**. Expand `right` until `have == need`. Then shrink `left` as much as possible while maintaining `have == need`.

```java
public String minWindow(String s, String t) {
    if (t.isEmpty()) return "";
    Map<Character, Integer> countT = new HashMap<>(), window = new HashMap<>();
    for (char c : t.toCharArray()) countT.put(c, countT.getOrDefault(c, 0) + 1);
    
    int have = 0, need = countT.size();
    int[] resRange = {-1, -1};
    int minLen = Integer.MAX_VALUE, l = 0;
    
    for (int r = 0; r < s.length(); r++) {
        char c = s.charAt(r);
        window.put(c, window.getOrDefault(c, 0) + 1);
        
        if (countT.containsKey(c) && window.get(c).equals(countT.get(c))) have++;
        
        while (have == need) {
            if ((r - l + 1) < minLen) {
                minLen = r - l + 1;
                resRange[0] = l; resRange[1] = r;
            }
            // Shrink from left
            char leftChar = s.charAt(l);
            window.put(leftChar, window.get(leftChar) - 1);
            if (countT.containsKey(leftChar) && window.get(leftChar) < countT.get(leftChar)) {
                have--;
            }
            l++;
        }
    }
    return minLen == Integer.MAX_VALUE ? "" : s.substring(resRange[0], resRange[1] + 1);
}
```

---

### Q6: Sliding Window Maximum (LC 239)
**Example:** `nums = [1,3,-1,-3,5,3,6,7], k = 3` -> `[3,3,5,5,6,7]`  
**Explanation:** Find the maximum in every window of size $k$.

**Pattern:** **Monotonic Deque**. Store indices in a `Deque`. Maintain **decreasing order** of values. The `peekFirst()` index always points to the current maximum.

```java
public int[] maxSlidingWindow(int[] nums, int k) {
    int n = nums.length;
    int[] res = new int[n - k + 1];
    Deque<Integer> q = new ArrayDeque<>(); // stores indices
    
    for (int i = 0; i < n; i++) {
        // 1. Remove out-of-bounds index
        if (!q.isEmpty() && q.peekFirst() < i - k + 1) q.pollFirst();
        
        // 2. Maintain monotonic decreasing order
        while (!q.isEmpty() && nums[q.peekLast()] < nums[i]) q.pollLast();
        
        q.offerLast(i);
        
        // 3. Current max is at the front
        if (i >= k - 1) {
            res[i - k + 1] = nums[q.peekFirst()];
        }
    }
    return res;
}
```
---

## 4. Stack

### Core Theory & Patterns

1.  **LIFO (Last-In, First-Out):** The core property. Useful for "reversing" a process or tracking the "most recent" state.
2.  **ArrayDeque vs. Stack:** In Java, always use `Deque<Integer> stack = new ArrayDeque<>();`. The legacy `java.util.Stack` class is synchronized, which introduces unnecessary overhead for DSA.
3.  **Monotonic Stack:** A stack where elements are always in increasing or decreasing order.
    *   **Decreasing Stack:** Used to find the "Next Greater Element."
    *   **Increasing Stack:** Used to find the "Next Smaller Element."
4.  **Sentinel Values:** Adding a "dummy" value (like `0` or `-1`) to the input or the stack can simplify boundary conditions (especially in Histogram/Area problems).
5.  **State Reconstruction:** Stacks are used behind the scenes for recursion. Any recursive problem can be solved iteratively using an explicit stack to save memory on the JVM's call stack.

---

### Q1: Valid Parentheses (LC 20)
**Example:** `s = "()[]{}"` -> `true`  
**Explanation:** Determine if input brackets are closed in the correct order.

**Pattern:** **Matching Brackets**. Push the expected *closing* bracket onto the stack. If the current character doesn't match `stack.pop()`, the string is invalid.

```java
public boolean isValid(String s) {
    Deque<Character> stack = new ArrayDeque<>();
    for (char c : s.toCharArray()) {
        if (c == '(') stack.push(')');
        else if (c == '{') stack.push('}');
        else if (c == '[') stack.push(']');
        else if (stack.isEmpty() || stack.pop() != c) return false;
    }
    return stack.isEmpty();
}
```

---

### Q2: Min Stack (LC 155)
**Example:** `push(-2), push(0), push(-3), getMin()` -> `-3`  
**Explanation:** Design a stack that retrieves the minimum element in **O(1)**.

**Pattern:** **Synchronized Stacks**. Maintain a second stack (`minStack`) that stores the minimum value seen up to that height.

```java
class MinStack {
    private Deque<Integer> stack = new ArrayDeque<>();
    private Deque<Integer> minStack = new ArrayDeque<>();

    public void push(int val) {
        stack.push(val);
        // The current min is the smaller of val or the previous min
        val = Math.min(val, minStack.isEmpty() ? val : minStack.peek());
        minStack.push(val);
    }

    public void pop() {
        stack.pop();
        minStack.pop();
    }

    public int top() { return stack.peek(); }
    public int getMin() { return minStack.peek(); }
}
```

---

### Q3: Evaluate Reverse Polish Notation (LC 150)
**Example:** `["2","1","+","3","*"]` -> `(2 + 1) * 3 = 9`  
**Explanation:** Evaluate an arithmetic expression in postfix notation.

**Pattern:** **Operand Stack**. When you see a number, push it. When you see an operator, pop two operands, apply the operator, and push the result.

```java
public int evalRPN(String[] tokens) {
    Deque<Integer> stack = new ArrayDeque<>();
    for (String t : tokens) {
        if ("+-*/".contains(t)) {
            int b = stack.pop();
            int a = stack.pop();
            if (t.equals("+")) stack.push(a + b);
            else if (t.equals("-")) stack.push(a - b);
            else if (t.equals("*")) stack.push(a * b);
            else stack.push(a / b);
        } else {
            stack.push(Integer.parseInt(t));
        }
    }
    return stack.pop();
}
```

---

### Q4: Generate Parentheses (LC 22)
**Example:** `n = 3` -> `["((()))","(()())","(())()","()(())","()()()"]`  
**Explanation:** Generate all combinations of well-formed parentheses.

**Pattern:** **Backtracking**. Keep track of `open` and `closed` counts. You can add `(` if `open < n` and add `)` if `closed < open`.

```java
public List<String> generateParenthesis(int n) {
    List<String> res = new ArrayList<>();
    backtrack(res, new StringBuilder(), 0, 0, n);
    return res;
}

private void backtrack(List<String> res, StringBuilder sb, int open, int close, int n) {
    if (sb.length() == n * 2) {
        res.add(sb.toString());
        return;
    }
    if (open < n) {
        sb.append("(");
        backtrack(res, sb, open + 1, close, n);
        sb.deleteCharAt(sb.length() - 1); // Backtrack
    }
    if (close < open) {
        sb.append(")");
        backtrack(res, sb, open, close + 1, n);
        sb.deleteCharAt(sb.length() - 1); // Backtrack
    }
}
```

---

### Q5: Daily Temperatures (LC 739)
**Example:** `temp = [73,74,75,71,69,72,76,73]` -> `[1,1,4,2,1,1,0,0]`  
**Explanation:** Find how many days to wait for a warmer temperature.

**Pattern:** **Monotonic Decreasing Stack**. Store *indices* in the stack. When you find a warmer temperature, pop and calculate the index difference.

```java
public int[] dailyTemperatures(int[] temperatures) {
    int[] res = new int[temperatures.length];
    Deque<Integer> stack = new ArrayDeque<>(); // stores indices
    
    for (int i = 0; i < temperatures.length; i++) {
        while (!stack.isEmpty() && temperatures[i] > temperatures[stack.peek()]) {
            int idx = stack.pop();
            res[idx] = i - idx;
        }
        stack.push(i);
    }
    return res;
}
```

---

### Q6: Car Fleet (LC 853)
**Example:** `target = 12, pos = [10,8,0,5], speed = [2,4,1,1]` -> `3`  
**Explanation:** Cars moving towards a target. If a faster car catches a slower one, they form a fleet.

**Pattern:** **Time-to-Target + Monotonic Stack**. Sort cars by position (descending). Calculate `time = (target - pos) / speed`. If a car takes less or equal time than the car in front, it joins the fleet.

```java
public int carFleet(int target, int[] position, int[] speed) {
    int n = position.length;
    double[][] cars = new double[n][2];
    for (int i = 0; i < n; i++) {
        cars[i] = new double[] {position[i], (double)(target - position[i]) / speed[i]};
    }
    Arrays.sort(cars, (a, b) -> Double.compare(b[0], a[0])); // Sort by pos desc
    
    int fleets = 0;
    double lastTime = 0;
    for (double[] car : cars) {
        if (car[1] > lastTime) { // Cannot catch up to the car in front
            fleets++;
            lastTime = car[1];
        }
    }
    return fleets;
}
```

---

### Q7: Largest Rectangle in Histogram (LC 84)
**Example:** `heights = [2,1,5,6,2,3]` -> `10`  
**Explanation:** Find the largest rectangle area in a histogram.

**Pattern:** **Monotonic Increasing Stack**. When the current bar is shorter than `stack.peek()`, pop and calculate area. The "height" is the popped value, and the "width" is the distance between the current index and the new `stack.peek()`.

```java
public int largestRectangleArea(int[] heights) {
    int maxArea = 0;
    Deque<Integer> stack = new ArrayDeque<>();
    stack.push(-1); // Sentinel for left boundary
    
    for (int i = 0; i <= heights.length; i++) {
        int currH = (i == heights.length) ? 0 : heights[i];
        while (stack.peek() != -1 && heights[stack.peek()] >= currH) {
            int h = heights[stack.pop()];
            int w = i - stack.peek() - 1;
            maxArea = Math.max(maxArea, h * w);
        }
        stack.push(i);
    }
    return maxArea;
}
```
---

## 5. Binary Search

### Core Theory & Patterns

1.  **Search Space Reduction:** Binary search works on any **monotonic** search space (not just sorted arrays). If $f(x)$ is true, then $f(x+1)$ must also be true (or false) for the logic to hold.
2.  **The Overflow Guard:** Always use `mid = l + (r - l) / 2` instead of `(l + r) / 2` to prevent `Integer` overflow when `l` and `r` are large.
3.  **Boundary Conditions:**
    *   `while (l <= r)`: Used when looking for a specific value.
    *   `while (l < r)`: Used when looking for a boundary or the "best" value in a range.
4.  **Binary Search on Answer:** When the problem asks for a "Minimum X such that..." or "Maximum Y such that...", define the range of possible answers and binary search through them, using a `check(mid)` function.
5.  **Virtual Indexing:** Treat a 2D matrix as a 1D array using `matrix[mid / cols][mid % cols]`.

---

### Q1: Binary Search (LC 704)
**Example:** `nums = [-1,0,3,5,9,12], target = 9` -> `4`  
**Explanation:** Standard search for a target in a sorted array.

**Pattern:** Standard iterative template.

```java
public int search(int[] nums, int target) {
    int l = 0, r = nums.length - 1;
    while (l <= r) {
        int mid = l + (r - l) / 2;
        if (nums[mid] == target) return mid;
        if (nums[mid] < target) l = mid + 1;
        else r = mid - 1;
    }
    return -1;
}
```

---

### Q2: Search a 2D Matrix (LC 74)
**Example:** `matrix = [[1,3,5,7],[10,11,16,20],[23,30,34,60]], target = 3` -> `true`  
**Explanation:** Search in a matrix where rows and columns are sorted.

**Pattern:** **Virtual 1D Array**. Map the 1D index `mid` back to `(r, c)` using `r = mid / cols` and `c = mid % cols`.

```java
public boolean searchMatrix(int[][] matrix, int target) {
    int rows = matrix.length, cols = matrix[0].length;
    int l = 0, r = rows * cols - 1;
    
    while (l <= r) {
        int mid = l + (r - l) / 2;
        // Transform 1D index to 2D coordinates
        int val = matrix[mid / cols][mid % cols];
        
        if (val == target) return true;
        if (val < target) l = mid + 1;
        else r = mid - 1;
    }
    return false;
}
```

---

### Q3: Koko Eating Bananas (LC 875)
**Example:** `piles = [3,6,7,11], h = 8` -> `4`  
**Explanation:** Find the minimum speed `k` to eat all bananas within `h` hours.

**Pattern:** **Binary Search on Answer**. The search space is `[1, max(piles)]`. For each `mid`, calculate total hours.

```java
public int minEatingSpeed(int[] piles, int h) {
    int l = 1, r = 0;
    for (int p : piles) r = Math.max(r, p);
    int res = r;
    
    while (l <= r) {
        int k = l + (r - l) / 2;
        long totalHours = 0;
        for (int p : piles) {
            totalHours += (p + k - 1) / k; // Ceiling division
        }
        
        if (totalHours <= h) {
            res = k;
            r = k - 1;
        } else {
            l = k + 1;
        }
    }
    return res;
}
```

---

### Q4: Find Minimum in Rotated Sorted Array (LC 153)
**Example:** `nums = [3,4,5,1,2]` -> `1`  
**Explanation:** Find the minimum element in an array that was rotated.

**Pattern:** **Pivot Detection**. In a rotated array, the minimum is the only element where the left neighbor is greater, or simply the point where the "sorted" property breaks.

```java
public int findMin(int[] nums) {
    int l = 0, r = nums.length - 1;
    while (l < r) {
        int mid = l + (r - l) / 2;
        // If mid is greater than right, min is in the right half
        if (nums[mid] > nums[r]) l = mid + 1;
        // Otherwise, mid could be the min or min is in the left half
        else r = mid;
    }
    return nums[l];
}
```

---

### Q5: Search in Rotated Sorted Array (LC 33)
**Example:** `nums = [4,5,6,7,0,1,2], target = 0` -> `4`  
**Explanation:** Search for a target in an array that was rotated.

**Pattern:** **Identify Sorted Half**. At any `mid`, at least one half (left or right) is guaranteed to be sorted. Check if the target lies within that sorted half.

```java
public int search(int[] nums, int target) {
    int l = 0, r = nums.length - 1;
    while (l <= r) {
        int mid = l + (r - l) / 2;
        if (nums[mid] == target) return mid;
        
        // Left half is sorted
        if (nums[l] <= nums[mid]) {
            if (target >= nums[l] && target < nums[mid]) r = mid - 1;
            else l = mid + 1;
        } 
        // Right half is sorted
        else {
            if (target > nums[mid] && target <= nums[r]) l = mid + 1;
            else r = mid - 1;
        }
    }
    return -1;
}
```

---

### Q6: Time Based Key-Value Store (LC 981)
**Example:** `set("foo", "bar", 1), get("foo", 1)` -> `"bar"`  
**Explanation:** Store keys with multiple values at different timestamps. `get` returns the value with the largest `timestamp_prev <= timestamp`.

**Pattern:** **Map to List + Binary Search**. Use `Map<String, List<Pair>>`. Since timestamps are added in increasing order, the list is already sorted by time.

```java
class TimeMap {
    private Map<String, List<Data>> map = new HashMap<>();

    class Data {
        String val; int time;
        Data(String v, int t) { val = v; time = t; }
    }

    public void set(String key, String value, int timestamp) {
        map.computeIfAbsent(key, k -> new ArrayList<>()).add(new Data(value, timestamp));
    }

    public String get(String key, int timestamp) {
        List<Data> list = map.get(key);
        if (list == null) return "";
        
        // Binary search for largest time <= timestamp
        int l = 0, r = list.size() - 1;
        String res = "";
        while (l <= r) {
            int mid = l + (r - l) / 2;
            if (list.get(mid).time <= timestamp) {
                res = list.get(mid).val;
                l = mid + 1;
            } else r = mid - 1;
        }
        return res;
    }
}
```

---

### Q7: Median of Two Sorted Arrays (LC 4)
**Example:** `nums1 = [1,3], nums2 = [2]` -> `2.0`  
**Explanation:** Find the median of the combined sorted arrays in $O(\log(\min(n, m)))$.

**Pattern:** **Partitioning**. Partition the smaller array `A` at `i`. The partition in `B` is `(total + 1) / 2 - i`. Ensure `A[left] <= B[right]` and `B[left] <= A[right]`.

```java
public double findMedianSortedArrays(int[] nums1, int[] nums2) {
    int[] A = nums1, B = nums2;
    if (A.length > B.length) return findMedianSortedArrays(nums2, nums1); // Ensure A is smaller
    
    int m = A.length, n = B.length;
    int l = 0, r = m;
    
    while (l <= r) {
        int i = l + (r - l) / 2; // Partition index for A
        int j = (m + n + 1) / 2 - i; // Partition index for B
        
        int AL = (i == 0) ? Integer.MIN_VALUE : A[i - 1];
        int AR = (i == m) ? Integer.MAX_VALUE : A[i];
        int BL = (j == 0) ? Integer.MIN_VALUE : B[j - 1];
        int BR = (j == n) ? Integer.MAX_VALUE : B[j];
        
        if (AL <= BR && BL <= AR) {
            // Correct partition found
            if ((m + n) % 2 == 0) {
                return (Math.max(AL, BL) + Math.min(AR, BR)) / 2.0;
            } else {
                return Math.max(AL, BL);
            }
        } else if (AL > BR) {
            r = i - 1; // Move left in A
        } else {
            l = i + 1; // Move right in A
        }
    }
    return 0.0;
}
```
---

## 6. Linked List

### Core Theory & Patterns

1.  **Sentinel (Dummy) Nodes:** Use `ListNode dummy = new ListNode(0); dummy.next = head;`. This eliminates edge cases for inserting/deleting the head and allows you to return `dummy.next` easily.
2.  **Slow & Fast Pointers (Tortoise & Hare):** 
    *   **Finding Midpoint:** Fast moves 2x, Slow moves 1x. When Fast reaches the end, Slow is at the mid.
    *   **Cycle Detection:** If Fast and Slow ever meet, a cycle exists.
3.  **In-Place Reversal:** Standard 3-pointer swap: `next = curr.next; curr.next = prev; prev = curr; curr = next;`.
4.  **The "Gap" Strategy:** To find the $N$-th element from the end, move a "Fast" pointer $N$ steps ahead, then move both at the same speed.
5.  **Doubly Linked List + HashMap:** The standard architecture for an **LRU Cache**.

---

### Q1: Reverse Linked List (LC 206)
**Example:** `1->2->3` -> `3->2->1`  
**Pattern:** Iterative 3-pointer swap.

```java
public ListNode reverseList(ListNode head) {
    ListNode prev = null, curr = head;
    while (curr != null) {
        ListNode next = curr.next; // Save next
        curr.next = prev;          // Reverse link
        prev = curr;               // Move prev
        curr = next;               // Move curr
    }
    return prev;
}
```

---

### Q2: Merge Two Sorted Lists (LC 21)
**Example:** `1->2->4, 1->3->4` -> `1->1->2->3->4->4`  
**Pattern:** **Dummy Node**. Compare heads and attach the smaller one to the result list.

```java
public ListNode mergeTwoLists(ListNode list1, ListNode list2) {
    ListNode dummy = new ListNode(0);
    ListNode curr = dummy;
    
    while (list1 != null && list2 != null) {
        if (list1.val <= list2.val) {
            curr.next = list1;
            list1 = list1.next;
        } else {
            curr.next = list2;
            list2 = list2.next;
        }
        curr = curr.next;
    }
    curr.next = (list1 != null) ? list1 : list2;
    return dummy.next;
}
```

---

### Q3: Reorder List (LC 143)
**Example:** `1->2->3->4` -> `1->4->2->3`  
**Pattern:** **Find Mid -> Reverse Second Half -> Merge**.

```java
public void reorderList(ListNode head) {
    // 1. Find middle
    ListNode slow = head, fast = head.next;
    while (fast != null && fast.next != null) {
        slow = slow.next;
        fast = fast.next.next;
    }
    
    // 2. Reverse second half
    ListNode second = slow.next;
    slow.next = null; // Split
    ListNode prev = null;
    while (second != null) {
        ListNode tmp = second.next;
        second.next = prev;
        prev = second;
        second = tmp;
    }
    
    // 3. Merge two halves
    ListNode first = head;
    second = prev;
    while (second != null) {
        ListNode t1 = first.next, t2 = second.next;
        first.next = second;
        second.next = t1;
        first = t1;
        second = t2;
    }
}
```

---

### Q4: Remove Nth Node From End of List (LC 19)
**Example:** `1->2->3->4->5, n = 2` -> `1->2->3->5`  
**Pattern:** **Two Pointers with Gap**. Move `fast` $n$ steps ahead. Then move `slow` and `fast` until `fast.next` is null.

```java
public ListNode removeNthFromEnd(ListNode head, int n) {
    ListNode dummy = new ListNode(0, head);
    ListNode left = dummy, right = head;
    
    while (n > 0 && right != null) {
        right = right.next;
        n--;
    }
    
    while (right != null) {
        left = left.next;
        right = right.next;
    }
    
    left.next = left.next.next; // Remove node
    return dummy.next;
}
```

---

### Q5: Copy List with Random Pointer (LC 138)
**Example:** List with `next` and `random` pointers.  
**Pattern:** **Interweaving**. 1. Create copies and insert them next to original nodes. 2. Set random pointers for copies. 3. Separate lists. $O(1)$ space optimization over HashMap.

```java
public Node copyRandomList(Node head) {
    if (head == null) return null;
    Node curr = head;
    // 1. Interweave copies
    while (curr != null) {
        Node copy = new Node(curr.val);
        copy.next = curr.next;
        curr.next = copy;
        curr = copy.next;
    }
    // 2. Assign randoms
    curr = head;
    while (curr != null) {
        if (curr.random != null) curr.next.random = curr.random.next;
        curr = curr.next.next;
    }
    // 3. Separate
    Node newHead = head.next;
    curr = head;
    while (curr != null) {
        Node copy = curr.next;
        curr.next = copy.next;
        if (copy.next != null) copy.next = copy.next.next;
        curr = curr.next;
    }
    return newHead;
}
```

---

### Q6: Add Two Numbers (LC 2)
**Example:** `(2->4->3) + (5->6->4)` -> `7->0->8` (342 + 465 = 807)  
**Pattern:** **Elementary Math**. Iterate both lists, maintain a `carry`. Use `dummy` to build the result.

```java
public ListNode addTwoNumbers(ListNode l1, ListNode l2) {
    ListNode dummy = new ListNode(0);
    ListNode curr = dummy;
    int carry = 0;
    while (l1 != null || l2 != null || carry != 0) {
        int v1 = (l1 != null) ? l1.val : 0;
        int v2 = (l2 != null) ? l2.val : 0;
        
        int sum = v1 + v2 + carry;
        carry = sum / 10;
        curr.next = new ListNode(sum % 10);
        
        curr = curr.next;
        if (l1 != null) l1 = l1.next;
        if (l2 != null) l2 = l2.next;
    }
    return dummy.next;
}
```

---

### Q7: Linked List Cycle (LC 141)
**Pattern:** **Floyd's Tortoise and Hare**. Fast moves 2, Slow moves 1.

```java
public boolean hasCycle(ListNode head) {
    ListNode slow = head, fast = head;
    while (fast != null && fast.next != null) {
        slow = slow.next;
        fast = fast.next.next;
        if (slow == fast) return true;
    }
    return false;
}
```

---

### Q8: Find the Duplicate Number (LC 287)
**Example:** `[1,3,4,2,2]` -> `2`  
**Pattern:** **Cycle Detection (Linked List cycle II)**. Treat array values as pointers (index $i$ points to $nums[i]$). The duplicate number is the entry point of the cycle.

```java
public int findDuplicate(int[] nums) {
    int slow = 0, fast = 0;
    // Phase 1: Meet in cycle
    do {
        slow = nums[slow];
        fast = nums[nums[fast]];
    } while (slow != fast);
    
    // Phase 2: Find entrance
    int slow2 = 0;
    while (slow2 != slow) {
        slow = nums[slow];
        slow2 = nums[slow2];
    }
    return slow;
}
```

---

### Q9: LRU Cache (LC 146)
**Explanation:** Design a data structure that follows the constraints of a Least Recently Used (LRU) cache.
**Pattern:** **HashMap + Doubly Linked List**. HashMap for $O(1)$ lookups, DLL for $O(1)$ updates to the "Most Recently Used" position.

```java
class LRUCache {
    class Node { int key, val; Node prev, next; }
    private Map<Integer, Node> map = new HashMap<>();
    private Node head, tail;
    private int capacity;

    public LRUCache(int capacity) {
        this.capacity = capacity;
        head = new Node(); tail = new Node();
        head.next = tail; tail.prev = head;
    }
    
    public int get(int key) {
        if (!map.containsKey(key)) return -1;
        Node node = map.get(key);
        remove(node); insert(node); // Move to head
        return node.val;
    }

    public void put(int key, int value) {
        if (map.containsKey(key)) remove(map.get(key));
        if (map.size() == capacity) {
            map.remove(tail.prev.key);
            remove(tail.prev);
        }
        Node newNode = new Node(); newNode.key = key; newNode.val = value;
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

---

### Q10: Merge K Sorted Lists (LC 23)
**Example:** `[[1,4,5],[1,3,4],[2,6]]` -> `[1,1,2,3,4,4,5,6]`  
**Pattern:** **PriorityQueue (Min-Heap)**. Add all heads to PQ. Repeatedly poll the min, add it to result, and add its `next` to PQ. $O(N \log K)$.

```java
public ListNode mergeKLists(ListNode[] lists) {
    PriorityQueue<ListNode> pq = new PriorityQueue<>((a, b) -> a.val - b.val);
    for (ListNode l : lists) if (l != null) pq.add(l);
    
    ListNode dummy = new ListNode(0);
    ListNode curr = dummy;
    while (!pq.isEmpty()) {
        ListNode node = pq.poll();
        curr.next = node;
        curr = curr.next;
        if (node.next != null) pq.add(node.next);
    }
    return dummy.next;
}
```

---

### Q11: Reverse Nodes in k-Group (LC 25)
**Example:** `1->2->3->4->5, k = 2` -> `2->1->4->3->5`  
**Pattern:** **Sub-list Reversal**. Find the $k$-th node. If it exists, reverse that segment. Connect the tail of the reversed segment to the result of the next recursive call.

```java
public ListNode reverseKGroup(ListNode head, int k) {
    ListNode curr = head;
    int count = 0;
    while (curr != null && count != k) { // Find k-th node
        curr = curr.next;
        count++;
    }
    if (count == k) {
        curr = reverseKGroup(curr, k); // Reverse the rest
        // Reverse current k-group
        while (count > 0) {
            ListNode tmp = head.next;
            head.next = curr;
            curr = head;
            head = tmp;
            count--;
        }
        head = curr;
    }
    return head;
}
```
---

## 7. Trees

### Core Theory & Patterns

1.  **The Recursive Template (DFS):** Most tree problems follow this:
    *   **Base Case:** `if (root == null) return ...;`
    *   **Recursive Call:** Get values from `root.left` and `root.right`.
    *   **Logic:** Combine those values at the current `root` and return to the parent.
2.  **BFS (Level Order Traversal):** Use a `Queue`. The key is to capture the `size` of the queue at the start of each level to process nodes level-by-level.
3.  **BST (Binary Search Tree) Property:** For every node: `Left < Node < Right`. 
    *   **In-order Traversal** of a BST always yields a **sorted array**.
    *   **Search/Insert** is $O(H)$, where $H$ is height ($O(\log N)$ if balanced).
4.  **Height vs. Depth:**
    *   **Depth:** Distance from root to node (Top-down).
    *   **Height:** Distance from node to furthest leaf (Bottom-up). Use recursion to return height up to parents.

---

### Q1: Invert Binary Tree (LC 226)
**Example:** `[4,2,7,1,3,6,9]` -> `[4,7,2,9,6,3,1]`  
**Pattern:** Recursive swap.

```java
public TreeNode invertTree(TreeNode root) {
    if (root == null) return null;
    
    // Swap children
    TreeNode tmp = root.left;
    root.left = root.right;
    root.right = tmp;
    
    invertTree(root.left);
    invertTree(root.right);
    return root;
}
```

---

### Q2: Maximum Depth of Binary Tree (LC 104)
**Pattern:** **Bottom-up DFS**. The height of a node is `1 + max(leftHeight, rightHeight)`.

```java
public int maxDepth(TreeNode root) {
    if (root == null) return 0;
    return 1 + Math.max(maxDepth(root.left), maxDepth(root.right));
}
```

---

### Q3: Diameter of Binary Tree (LC 543)
**Explanation:** Longest path between any two nodes. Path may or may not pass through root.

**Pattern:** **Global Variable + Height DFS**. At each node, calculate height, but update a global `max` with `leftHeight + rightHeight`.

```java
int maxD = 0;
public int diameterOfBinaryTree(TreeNode root) {
    height(root);
    return maxD;
}

private int height(TreeNode node) {
    if (node == null) return 0;
    int L = height(node.left);
    int R = height(node.right);
    
    maxD = Math.max(maxD, L + R); // Update global diameter
    return 1 + Math.max(L, R);    // Return height to parent
}
```

---

### Q4: Balanced Binary Tree (LC 110)
**Explanation:** A tree is balanced if heights of left and right subtrees of *every* node differ by no more than 1.

**Pattern:** **Sentinel Return Value**. Return `-1` if any subtree is unbalanced; otherwise return the height.

```java
public boolean isBalanced(TreeNode root) {
    return dfs(root) != -1;
}

private int dfs(TreeNode root) {
    if (root == null) return 0;
    
    int left = dfs(root.left);
    if (left == -1) return -1;
    
    int right = dfs(root.right);
    if (right == -1) return -1;
    
    if (Math.abs(left - right) > 1) return -1;
    return 1 + Math.max(left, right);
}
```

---

### Q5: Same Tree (LC 100)
**Pattern:** Structural recursion. Both must be null, or both must have same val and matching children.

```java
public boolean isSameTree(TreeNode p, TreeNode q) {
    if (p == null && q == null) return true;
    if (p == null || q == null || p.val != q.val) return false;
    
    return isSameTree(p.left, q.left) && isSameTree(p.right, q.right);
}
```

---

### Q6: Subtree of Another Tree (LC 572)
**Pattern:** **Double Recursion**. For each node in `root`, check if `isSameTree(node, subRoot)`.

```java
public boolean isSubtree(TreeNode root, TreeNode subRoot) {
    if (root == null) return false;
    if (isSame(root, subRoot)) return true;
    return isSubtree(root.left, subRoot) || isSubtree(root.right, subRoot);
}

private boolean isSame(TreeNode s, TreeNode t) {
    if (s == null && t == null) return true;
    if (s == null || t == null || s.val != t.val) return false;
    return isSame(s.left, t.left) && isSame(s.right, t.right);
}
```

---

### Q7: Lowest Common Ancestor of a BST (LC 235)
**Pattern:** **BST Binary Search**. 
*   If both values are smaller than `root`, LCA is in the left.
*   If both are larger, LCA is in the right.
*   If they split (one smaller, one larger) or one equals `root`, then `root` IS the LCA.

```java
public TreeNode lowestCommonAncestor(TreeNode root, TreeNode p, TreeNode q) {
    if (p.val < root.val && q.val < root.val) 
        return lowestCommonAncestor(root.left, p, q);
    if (p.val > root.val && q.val > root.val) 
        return lowestCommonAncestor(root.right, p, q);
    return root;
}
```

---

### Q8: Binary Tree Level Order Traversal (LC 102)
**Pattern:** **Queue BFS**. Snapshot size at each level.

```java
public List<List<Integer>> levelOrder(TreeNode root) {
    List<List<Integer>> res = new ArrayList<>();
    if (root == null) return res;
    
    Queue<TreeNode> q = new LinkedList<>();
    q.add(root);
    
    while (!q.isEmpty()) {
        int size = q.size(); // Number of nodes at current level
        List<Integer> level = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            TreeNode curr = q.poll();
            level.add(curr.val);
            if (curr.left != null) q.add(curr.left);
            if (curr.right != null) q.add(curr.right);
        }
        res.add(level);
    }
    return res;
}
```
---

### Q9: Binary Tree Right Side View (LC 199)
**Example:** `root = [1,2,3,null,5,null,4]` -> `[1,3,4]`
**Explanation:** Return the values of the nodes you can see when standing on the right side of the tree.

**Pattern:** **BFS Level Order**. In each level, the last node processed is the one visible from the right.

```java
public List<Integer> rightSideView(TreeNode root) {
    List<Integer> res = new ArrayList<>();
    if (root == null) return res;
    Queue<TreeNode> q = new LinkedList<>();
    q.offer(root);
    
    while (!q.isEmpty()) {
        int size = q.size();
        for (int i = 0; i < size; i++) {
            TreeNode curr = q.poll();
            // If it's the last element in the current level, add to result
            if (i == size - 1) res.add(curr.val);
            if (curr.left != null) q.offer(curr.left);
            if (curr.right != null) q.offer(curr.right);
        }
    }
    return res;
}
```

---

### Q10: Count Good Nodes in Binary Tree (LC 1448)
**Example:** `root = [3,1,4,3,null,1,5]` -> `4`
**Explanation:** A node is "good" if in the path from root to the node, there are no nodes with a value greater than its value.

**Pattern:** **Top-down DFS with State**. Pass the current `max` encountered in the path down to children.

```java
public int goodNodes(TreeNode root) {
    return dfs(root, root.val);
}

private int dfs(TreeNode node, int maxSoFar) {
    if (node == null) return 0;
    
    int res = (node.val >= maxSoFar) ? 1 : 0;
    maxSoFar = Math.max(maxSoFar, node.val);
    
    res += dfs(node.left, maxSoFar);
    res += dfs(node.right, maxSoFar);
    return res;
}
```

---

### Q11: Validate Binary Search Tree (LC 98)
**Pattern:** **Range Boundaries**. Every node must be strictly between a `min` and a `max`. Update these boundaries as you go left or right. Use `Long` to avoid issues with `Integer.MIN_VALUE`.

```java
public boolean isValidBST(TreeNode root) {
    return validate(root, Long.MIN_VALUE, Long.MAX_VALUE);
}

private boolean validate(TreeNode node, long min, long max) {
    if (node == null) return true;
    if (node.val <= min || node.val >= max) return false;
    
    // Left child must be < node.val; Right child must be > node.val
    return validate(node.left, min, node.val) && validate(node.right, node.val, max);
}
```

---

### Q12: Kth Smallest Element in a BST (LC 230)
**Pattern:** **Iterative In-order Traversal**. In a BST, in-order gives values in ascending order. Use a stack to simulate the traversal and stop at the $k$-th element.

```java
public int kthSmallest(TreeNode root, int k) {
    Deque<TreeNode> stack = new ArrayDeque<>();
    TreeNode curr = root;
    
    while (curr != null || !stack.isEmpty()) {
        while (curr != null) {
            stack.push(curr);
            curr = curr.left;
        }
        curr = stack.pop();
        if (--k == 0) return curr.val;
        curr = curr.right;
    }
    return -1;
}
```

---

### Q13: Construct Binary Tree from Preorder and Inorder Traversal (LC 105)
**Pattern:** **Divide & Conquer + HashMap**. `preorder[0]` is always the root. Find its index in `inorder` to determine the size of left and right subtrees.

```java
int preIdx = 0;
Map<Integer, Integer> inMap = new HashMap<>();

public TreeNode buildTree(int[] preorder, int[] inorder) {
    for (int i = 0; i < inorder.length; i++) inMap.put(inorder[i], i);
    return helper(preorder, 0, inorder.length - 1);
}

private TreeNode helper(int[] preorder, int left, int right) {
    if (left > right) return null;
    
    int val = preorder[preIdx++];
    TreeNode root = new TreeNode(val);
    int mid = inMap.get(val);
    
    root.left = helper(preorder, left, mid - 1);
    root.right = helper(preorder, mid + 1, right);
    return root;
}
```

---

### Q14: Binary Tree Maximum Path Sum (LC 124)
**Explanation:** Find the maximum path sum (can start and end at any node). 
**Pattern:** **Post-order DFS**. At each node, calculate the max branch (left or right). Simultaneously, update a global max by considering a path that "peaks" at the current node.

```java
int maxPath = Integer.MIN_VALUE;

public int maxPathSum(TreeNode root) {
    dfs(root);
    return maxPath;
}

private int dfs(TreeNode node) {
    if (node == null) return 0;
    
    // If branch sum is negative, ignore it (take 0)
    int left = Math.max(0, dfs(node.left));
    int right = Math.max(0, dfs(node.right));
    
    // Update global max: current node value + both branches
    maxPath = Math.max(maxPath, node.val + left + right);
    
    // Return the best single branch to parent
    return node.val + Math.max(left, right);
}
```

---

### Q15: Serialize and Deserialize Binary Tree (LC 297)
**Pattern:** **DFS with String Joiner**. Use pre-order traversal for serialization. Use a `Queue` to rebuild the tree during deserialization.

```java
public String serialize(TreeNode root) {
    if (root == null) return "N";
    return root.val + "," + serialize(root.left) + "," + serialize(root.right);
}

public TreeNode deserialize(String data) {
    Queue<String> nodes = new LinkedList<>(Arrays.asList(data.split(",")));
    return build(nodes);
}

private TreeNode build(Queue<String> nodes) {
    String val = nodes.poll();
    if (val.equals("N")) return null;
    
    TreeNode node = new TreeNode(Integer.parseInt(val));
    node.left = build(nodes);
    node.right = build(nodes);
    return node;
}
```
---

## 8. Tries

### Core Theory & Patterns

1.  **Definition:** A Trie (pronounced "try" or "tree") is a tree-like data structure used to store a dynamic set of strings where the keys are usually strings. It's particularly efficient for operations like prefix matching.
2.  **TrieNode Structure:** Each node typically stores:
    *   An array or `HashMap` of children nodes (e.g., `TrieNode[] children = new TrieNode[26]` for lowercase English letters).
    *   A boolean flag `isEndOfWord` to mark if the path to this node forms a complete word.
3.  **Prefix Matching:** The primary advantage of Tries is their ability to quickly find all words with a given prefix.
4.  **Space-Time Trade-off:** Tries can be very space-inefficient if the stored strings are very long and don't share many prefixes. However, they offer excellent time complexity for search, insert, and prefix search operations (typically $O(L)$ where $L$ is the length of the string).

```java
// Basic TrieNode structure
class TrieNode {
    TrieNode[] children;
    boolean isEndOfWord;

    public TrieNode() {
        children = new TrieNode; // For 'a' through 'z'
        isEndOfWord = false;
    }
}
```

---

### Q1: Implement Trie (Prefix Tree) (LC 208)
**Example:**
```
Trie trie = new Trie();
trie.insert("apple");
trie.search("apple");   // return True
trie.search("app");     // return False
trie.startsWith("app"); // return True
trie.insert("app");     
trie.search("app");     // return True
```
**Explanation:** Implement a Trie with `insert`, `search`, and `startsWith` methods.

**Pattern:** **Node Traversal**. Each character in the string corresponds to traversing one level down the Trie.

```java
class Trie {
    private TrieNode root;

    public Trie() {
        root = new TrieNode();
    }
    
    public void insert(String word) {
        TrieNode curr = root;
        for (char c : word.toCharArray()) {
            int idx = c - 'a';
            if (curr.children[idx] == null) {
                curr.children[idx] = new TrieNode();
            }
            curr = curr.children[idx];
        }
        curr.isEndOfWord = true;
    }
    
    public boolean search(String word) {
        TrieNode curr = root;
        for (char c : word.toCharArray()) {
            int idx = c - 'a';
            if (curr.children[idx] == null) {
                return false;
            }
            curr = curr.children[idx];
        }
        return curr.isEndOfWord; // Must be an end of word
    }
    
    public boolean startsWith(String prefix) {
        TrieNode curr = root;
        for (char c : prefix.toCharArray()) {
            int idx = c - 'a';
            if (curr.children[idx] == null) {
                return false;
            }
            curr = curr.children[idx];
        }
        return true; // Reached the end of the prefix path
    }
}
```

---

### Q2: Design Add and Search Words Data Structure (LC 211)
**Example:**
```
WordDictionary wordDictionary = new WordDictionary();
wordDictionary.addWord("bad");
wordDictionary.addWord("dad");
wordDictionary.addWord("mad");
wordDictionary.search("pad"); // return False
wordDictionary.search("bad"); // return True
wordDictionary.search(".ad"); // return True
wordDictionary.search("b.."); // return True
```
**Explanation:** Design a data structure that supports adding new words and finding if a string matches any previously added word. The search string can contain dots `.` which can represent any single letter.

**Pattern:** **DFS with Wildcard**. The `search` method will use recursion (DFS) to handle the wildcard `.` character.

```java
class WordDictionary {
    private TrieNode root;

    public WordDictionary() {
        root = new TrieNode();
    }
    
    public void addWord(String word) {
        TrieNode curr = root;
        for (char c : word.toCharArray()) {
            int idx = c - 'a';
            if (curr.children[idx] == null) {
                curr.children[idx] = new TrieNode();
            }
            curr = curr.children[idx];
        }
        curr.isEndOfWord = true;
    }
    
    public boolean search(String word) {
        return dfsSearch(word, 0, root);
    }

    private boolean dfsSearch(String word, int k, TrieNode node) {
        if (k == word.length()) {
            return node.isEndOfWord;
        }

        char c = word.charAt(k);
        if (c == '.') {
            // Try all possible children for '.'
            for (TrieNode child : node.children) {
                if (child != null && dfsSearch(word, k + 1, child)) {
                    return true;
                }
            }
            return false;
        } else {
            // Normal character match
            int idx = c - 'a';
            if (node.children[idx] == null) {
                return false;
            }
            return dfsSearch(word, k + 1, node.children[idx]);
        }
    }
}
```

---

### Q3: Word Search II (LC 212)
**Example:**
```
board = [["o","a","a","n"],["e","t","a","e"],["i","h","k","r"],["i","f","l","v"]]
words = ["oath","pea","eat","rain"]
Output: ["eat","oath"]
```
**Explanation:** Given an `m x n` `board` of characters and a `list` of strings `words`, return all words on the board that can be formed by sequentially adjacent cells. The same letter cell may not be used more than once in the same word.

**Pattern:** **Trie + Backtracking (DFS)**. Build a Trie from `words`. Then, for each cell in the board, start a DFS. During DFS, traverse the Trie. If a path forms a word, add it to results and mark it as found in the Trie to avoid duplicates.

```java
class Solution {
    // Use the same TrieNode structure as above
    // class TrieNode { TrieNode[] children; boolean isEndOfWord; String word; ... }
    // For this problem, it's useful to store the actual word in the TrieNode
    // when isEndOfWord is true.
    class TrieNode {
        TrieNode[] children = new TrieNode;
        boolean isEndOfWord = false;
        String word = null; // Store the word here if it's an end of word
    }

    private TrieNode buildTrie(String[] words) {
        TrieNode root = new TrieNode();
        for (String word : words) {
            TrieNode curr = root;
            for (char c : word.toCharArray()) {
                int idx = c - 'a';
                if (curr.children[idx] == null) {
                    curr.children[idx] = new TrieNode();
                }
                curr = curr.children[idx];
            }
            curr.isEndOfWord = true;
            curr.word = word; // Store the actual word
        }
        return root;
    }

    public List<String> findWords(char[][] board, String[] words) {
        TrieNode root = buildTrie(words);
        List<String> result = new ArrayList<>();
        
        for (int r = 0; r < board.length; r++) {
            for (int c = 0; c < board.length; c++) {
                dfs(board, r, c, root, result);
            }
        }
        return result;
    }

    private void dfs(char[][] board, int r, int c, TrieNode node, List<String> result) {
        // Boundary conditions and visited check
        if (r < 0 || r >= board.length || c < 0 || c >= board.length || board[r][c] == '#') {
            return;
        }

        char ch = board[r][c];
        TrieNode nextNode = node.children[ch - 'a'];
        if (nextNode == null) { // No path in Trie for this character
            return;
        }

        if (nextNode.isEndOfWord) {
            result.add(nextNode.word);
            nextNode.isEndOfWord = false; // Mark as found to avoid duplicates
            nextNode.word = null; // Clear the word to prevent re-adding
        }

        // Mark current cell as visited
        board[r][c] = '#'; 

        // Explore neighbors
        dfs(board, r + 1, c, nextNode, result);
        dfs(board, r - 1, c, nextNode, result);
        dfs(board, r, c + 1, nextNode, result);
        dfs(board, r, c - 1, nextNode, result);

        // Backtrack: unmark current cell
        board[r][c] = ch;
    }
}
```
---

## 9. Graphs

### Core Theory & Patterns

1.  **Implicit vs. Explicit:** A grid (`char[][]`) is an implicit graph where neighbors are up, down, left, right. Adjacency lists are explicit.
2.  **DFS (Connectivity):** Best for exploring paths, finding connected components, and cycle detection. Use a `visited` set to avoid infinite loops.
3.  **BFS (Shortest Path):** Use a `Queue` to find the shortest distance in an unweighted graph.
4.  **Topological Sort (Kahn's Algorithm):** Essential for dependency problems (Course Schedule). Uses **In-degrees**:
    *   Nodes with 0 in-degrees are processed first.
    *   Decreasing in-degree of neighbors mimics "completing a prerequisite."
5.  **Union-Find (DSU):** Optimized for checking if two nodes are in the same component. Use **Path Compression** and **Union by Rank** to keep operations near $O(1)$.
6.  **Multi-source BFS:** Instead of starting BFS from one node, add all "starting points" (e.g., all rotten oranges) to the queue initially.

---

### Q1: Number of Islands (LC 200)
**Example:** `grid = [["1","1","0"],["1","1","0"],["0","0","1"]]` -> `2`  
**Pattern:** **Grid DFS/BFS**. When you hit '1', increment count and sink the island (turn '1's to '0's) using recursion.

```java
public int numIslands(char[][] grid) {
    int count = 0;
    for (int r = 0; r < grid.length; r++) {
        for (int c = 0; c < grid.length; c++) {
            if (grid[r][c] == '1') {
                dfs(grid, r, c);
                count++;
            }
        }
    }
    return count;
}

private void dfs(char[][] grid, int r, int c) {
    if (r < 0 || c < 0 || r >= grid.length || c >= grid.length || grid[r][c] == '0') return;
    grid[r][c] = '0'; // Sink island
    dfs(grid, r + 1, c); dfs(grid, r - 1, c);
    dfs(grid, r, c + 1); dfs(grid, r, c - 1);
}
```

---

### Q2: Max Area of Island (LC 695)
**Pattern:** **Recursive Count**. Similar to Number of Islands, but return `1 + sum of neighbors` to accumulate area.

```java
public int maxAreaOfIsland(int[][] grid) {
    int maxArea = 0;
    for (int r = 0; r < grid.length; r++) {
        for (int c = 0; c < grid.length; c++) {
            if (grid[r][c] == 1) maxArea = Math.max(maxArea, dfs(grid, r, c));
        }
    }
    return maxArea;
}

private int dfs(int[][] grid, int r, int c) {
    if (r < 0 || r >= grid.length || c < 0 || c >= grid.length || grid[r][c] == 0) return 0;
    grid[r][c] = 0; // Mark visited
    return 1 + dfs(grid, r + 1, c) + dfs(grid, r - 1, c) + dfs(grid, r, c + 1) + dfs(grid, r, c - 1);
}
```

---

### Q3: Clone Graph (LC 133)
**Pattern:** **DFS + HashMap**. Use a Map to map `OriginalNode -> CopyNode`. If the node exists in Map, return the copy; otherwise, create it and recursively clone neighbors.

```java
private Map<Node, Node> map = new HashMap<>();
public Node cloneGraph(Node node) {
    if (node == null) return null;
    if (map.containsKey(node)) return map.get(node);
    
    Node copy = new Node(node.val);
    map.put(node, copy);
    for (Node neighbor : node.neighbors) {
        copy.neighbors.add(cloneGraph(neighbor));
    }
    return copy;
}
```

---

### Q4: Rotting Oranges (LC 994)
**Pattern:** **Multi-source BFS**. Put all rotten oranges in a `Queue`. Level-by-level, rot adjacent fresh oranges. Return minutes.

```java
public int orangesRotting(int[][] grid) {
    Queue<int[]> q = new ArrayDeque<>();
    int fresh = 0, time = 0;
    for (int r = 0; r < grid.length; r++) {
        for (int c = 0; c < grid.length; c++) {
            if (grid[r][c] == 2) q.offer(new int[]{r, c});
            if (grid[r][c] == 1) fresh++;
        }
    }
    int[][] dirs = {{0,1},{0,-1},{1,0},{-1,0}};
    while (!q.isEmpty() && fresh > 0) {
        int size = q.size();
        for (int i = 0; i < size; i++) {
            int[] curr = q.poll();
            for (int[] d : dirs) {
                int nr = curr + d, nc = curr + d;
                if (nr >= 0 && nr < grid.length && nc >= 0 && nc < grid.length && grid[nr][nc] == 1) {
                    grid[nr][nc] = 2; fresh--;
                    q.offer(new int[]{nr, nc});
                }
            }
        }
        time++;
    }
    return fresh == 0 ? time : -1;
}
```

---

### Q5: Pacific Atlantic Water Flow (LC 417)
**Pattern:** **Reverse Flow**. DFS from the boundaries (Pacific and Atlantic) inland. Find cells reachable from both.

```java
public List<List<Integer>> pacificAtlantic(int[][] heights) {
    int R = heights.length, C = heights.length;
    boolean[][] pac = new boolean[R][C], atl = new boolean[R][C];
    for (int i = 0; i < R; i++) { dfs(heights, i, 0, pac, 0); dfs(heights, i, C-1, atl, 0); }
    for (int j = 0; j < C; j++) { dfs(heights, 0, j, pac, 0); dfs(heights, R-1, j, atl, 0); }
    
    List<List<Integer>> res = new ArrayList<>();
    for (int r = 0; r < R; r++)
        for (int c = 0; c < C; c++)
            if (pac[r][c] && atl[r][c]) res.add(Arrays.asList(r, c));
    return res;
}

private void dfs(int[][] h, int r, int c, boolean[][] visit, int prevH) {
    if (r < 0 || c < 0 || r >= h.length || c >= h.length || visit[r][c] || h[r][c] < prevH) return;
    visit[r][c] = true;
    dfs(h, r+1, c, visit, h[r][c]); dfs(h, r-1, c, visit, h[r][c]);
    dfs(h, r, c+1, visit, h[r][c]); dfs(h, r, c-1, visit, h[r][c]);
}
```

---

### Q6: Surrounded Regions (LC 130)
**Pattern:** **Boundary Protection**. DFS from 'O's on the border and mark them 'T'. All remaining 'O's are surrounded and become 'X'. Finally, turn 'T's back to 'O'.

```java
public void solve(char[][] board) {
    int R = board.length, C = board.length;
    for (int i = 0; i < R; i++) { dfs(board, i, 0); dfs(board, i, C-1); }
    for (int j = 0; j < C; j++) { dfs(board, 0, j); dfs(board, R-1, j); }
    
    for (int r = 0; r < R; r++) {
        for (int c = 0; c < C; c++) {
            if (board[r][c] == 'O') board[r][c] = 'X';
            if (board[r][c] == 'T') board[r][c] = 'O';
        }
    }
}

private void dfs(char[][] b, int r, int c) {
    if (r < 0 || c < 0 || r >= b.length || c >= b.length || b[r][c] != 'O') return;
    b[r][c] = 'T';
    dfs(b, r+1, c); dfs(b, r-1, c); dfs(b, r, c+1); dfs(b, r, c-1);
}
```

---

### Q7: Course Schedule (LC 207)
**Pattern:** **Directed Cycle Detection / Topological Sort**. Use Kahn's algorithm (BFS). If we can't process all nodes, there is a cycle.

```java
public boolean canFinish(int numCourses, int[][] prerequisites) {
    int[] indegree = new int[numCourses];
    Map<Integer, List<Integer>> adj = new HashMap<>();
    for (int[] pre : prerequisites) {
        adj.computeIfAbsent(pre, k -> new ArrayList<>()).add(pre);
        indegree[pre]++;
    }
    Queue<Integer> q = new ArrayDeque<>();
    for (int i = 0; i < numCourses; i++) if (indegree[i] == 0) q.offer(i);
    
    int count = 0;
    while (!q.isEmpty()) {
        int curr = q.poll();
        count++;
        for (int next : adj.getOrDefault(curr, new ArrayList<>())) {
            if (--indegree[next] == 0) q.offer(next);
        }
    }
    return count == numCourses;
}
```

---

### Q8: Course Schedule II (LC 210)
**Pattern:** **Kahn's Algorithm**. Same as Q7, but add nodes to a result array as they are polled from the queue.

```java
public int[] findOrder(int numCourses, int[][] prerequisites) {
    int[] indegree = new int[numCourses];
    Map<Integer, List<Integer>> adj = new HashMap<>();
    for (int[] pre : prerequisites) {
        adj.computeIfAbsent(pre, k -> new ArrayList<>()).add(pre);
        indegree[pre]++;
    }
    Queue<Integer> q = new ArrayDeque<>();
    for (int i = 0; i < numCourses; i++) if (indegree[i] == 0) q.offer(i);
    
    int[] res = new int[numCourses];
    int idx = 0;
    while (!q.isEmpty()) {
        int curr = q.poll();
        res[idx++] = curr;
        for (int next : adj.getOrDefault(curr, new ArrayList<>())) {
            if (--indegree[next] == 0) q.offer(next);
        }
    }
    return idx == numCourses ? res : new int;
}
```

---

### Q9: Redundant Connection (LC 684)
**Pattern:** **Union-Find**. Iterate edges. For each edge `(u, v)`, if `find(u) == find(v)`, this edge is redundant.

```java
public int[] findRedundantConnection(int[][] edges) {
    int[] parent = new int[edges.length + 1];
    for (int i = 0; i < parent.length; i++) parent[i] = i;
    
    for (int[] edge : edges) {
        int rootU = find(parent, edge);
        int rootV = find(parent, edge);
        if (rootU == rootV) return edge;
        parent[rootU] = rootV; // Simple union
    }
    return new int;
}

private int find(int[] parent, int i) {
    if (parent[i] == i) return i;
    return parent[i] = find(parent, parent[i]); // Path compression
}
```

---

### Q10: Number of Connected Components (LC 323)
**Pattern:** **Union-Find or DFS**. Start with `n` components. For each valid union, decrement the count.

```java
public int countComponents(int n, int[][] edges) {
    int[] parent = new int[n];
    for (int i = 0; i < n; i++) parent[i] = i;
    int components = n;
    
    for (int[] edge : edges) {
        int root1 = find(parent, edge);
        int root2 = find(parent, edge);
        if (root1 != root2) {
            parent[root1] = root2;
            components--;
        }
    }
    return components;
}

private int find(int[] parent, int i) {
    if (parent[i] == i) return i;
    return parent[i] = find(parent, parent[i]);
}
```

---

## 10. Advanced Graphs

### Core Theory & Patterns

1.  **Shortest Path Algorithms:**
    *   **Dijkstra's Algorithm:** Finds single-source shortest paths in graphs with **non-negative** edge weights. Uses a `PriorityQueue` to greedily explore the closest unvisited node. Time: $O(E \log V)$ or $O(E + V \log V)$ with Fibonacci heap.
    *   **Bellman-Ford Algorithm:** Finds single-source shortest paths in graphs with **potentially negative** edge weights. Can detect negative cycles. Time: $O(V \cdot E)$.
2.  **Minimum Spanning Tree (MST):**
    *   **Kruskal's Algorithm:** Builds an MST by adding edges in increasing order of weight, using Union-Find to efficiently detect cycles. Time: $O(E \log E)$ or $O(E \log V)$.
    *   **Prim's Algorithm:** Builds an MST by growing a single component, using a `PriorityQueue` to select the cheapest edge to add. Time: $O(E \log V)$ or $O(E + V \log V)$.
3.  **Topological Sort (Advanced):** For Directed Acyclic Graphs (DAGs). Ordering nodes based on dependencies. Can be used to detect cycles in directed graphs.
4.  **Eulerian Path/Circuit:** A path/circuit that visits every edge exactly once. Conditions depend on node degrees. Often solved with a modified DFS.
5.  **Graph Representation:** For weighted graphs, use `Map<Integer, List<int[]>>` where `int[]` is `[neighbor, weight]`.

---

### Q1: Network Delay Time (LC 743)
**Example:** `times = [[2,1,1],[2,3,1],[3,4,1]], n = 4, k = 2` -> `2`  
**Explanation:** Send a signal from node `k`. Find the minimum time it takes for all `n` nodes to receive the signal. If not all nodes can receive the signal, return -1.

**Pattern:** **Dijkstra's Algorithm**. Single-source shortest path on a graph with non-negative edge weights.

```java
public int networkDelayTime(int[][] times, int n, int k) {
    // Build adjacency list: node -> List<[neighbor, weight]>
    Map<Integer, List<int[]>> adj = new HashMap<>();
    for (int[] time : times) {
        adj.computeIfAbsent(time, x -> new ArrayList<>()).add(new int[]{time, time});
    }

    // Dijkstra's: {distance, node}
    PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> a - b);
    pq.offer(new int[]{0, k}); // Start with source node k, distance 0

    // Store min distance to each node
    Map<Integer, Integer> dist = new HashMap<>();

    while (!pq.isEmpty()) {
        int[] current = pq.poll();
        int d = current;
        int node = current;

        if (dist.containsKey(node)) continue; // Already found shortest path
        dist.put(node, d);

        if (adj.containsKey(node)) {
            for (int[] neighbor : adj.get(node)) {
                int nextNode = neighbor;
                int weight = neighbor;
                if (!dist.containsKey(nextNode)) {
                    pq.offer(new int[]{d + weight, nextNode});
                }
            }
        }
    }

    if (dist.size() != n) return -1; // Not all nodes reached

    int maxDelay = 0;
    for (int d : dist.values()) {
        maxDelay = Math.max(maxDelay, d);
    }
    return maxDelay;
}
```

---

### Q2: Cheapest Flights Within K Stops (LC 787)
**Example:** `n = 3, flights = [[0,1,100],[1,2,100],[0,2,500]], src = 0, dst = 2, k = 1` -> `200`  
**Explanation:** Find the cheapest price from `src` to `dst` with at most `k` stops.

**Pattern:** **Bellman-Ford Algorithm (modified)** or **BFS with level tracking**. Bellman-Ford is a good fit because it iteratively relaxes edges for a fixed number of steps, which directly corresponds to "stops".

```java
public int findCheapestPrice(int n, int[][] flights, int src, int dst, int k) {
    // dist[i] stores the minimum cost to reach node i
    int[] dist = new int[n];
    Arrays.fill(dist, Integer.MAX_VALUE);
    dist[src] = 0;

    // Perform k+1 relaxations (k stops means k+1 edges)
    for (int stops = 0; stops <= k; stops++) {
        // Create a temporary array to store distances for the current iteration
        // This is crucial to avoid using distances from the current iteration
        // to update other nodes in the same iteration (which would be more than 'stops' edges)
        int[] tempDist = Arrays.copyOf(dist, n); 
        
        for (int[] flight : flights) {
            int u = flight;
            int v = flight;
            int price = flight;

            if (dist[u] == Integer.MAX_VALUE) continue; // Cannot reach 'u'

            // Relax edge (u, v)
            if (dist[u] + price < tempDist[v]) {
                tempDist[v] = dist[u] + price;
            }
        }
        dist = tempDist; // Update distances for the next iteration
    }

    return dist[dst] == Integer.MAX_VALUE ? -1 : dist[dst];
}
```

---

### Q3: Alien Dictionary (LC 269)
**Example:** `words = ["wrt","wrf","er","ett","rftt"]` -> `"wertf"`  
**Explanation:** Given a sorted list of words from an alien language, find the order of letters. If the order is invalid, return an empty string.

**Pattern:** **Topological Sort (Kahn's Algorithm)**. Build a directed graph where an edge `u -> v` means `u` comes before `v`. Then perform topological sort.

```java
public String alienOrder(String[] words) {
    Map<Character, List<Character>> adj = new HashMap<>();
    Map<Character, Integer> indegree = new HashMap<>();

    // Initialize all characters with 0 indegree and empty adjacency lists
    for (String word : words) {
        for (char c : word.toCharArray()) {
            indegree.put(c, 0);
            adj.put(c, new ArrayList<>());
        }
    }

    // Build graph and calculate indegrees
    for (int i = 0; i < words.length - 1; i++) {
        String w1 = words[i];
        String w2 = words[i+1];

        // Check for invalid order (e.g., "abc", "ab")
        if (w1.length() > w2.length() && w1.startsWith(w2)) return "";

        for (int j = 0; j < Math.min(w1.length(), w2.length()); j++) {
            char c1 = w1.charAt(j);
            char c2 = w2.charAt(j);
            if (c1 != c2) {
                // Add edge c1 -> c2
                adj.get(c1).add(c2);
                indegree.put(c2, indegree.get(c2) + 1);
                break; // Only the first differing character matters
            }
        }
    }

    // Kahn's Algorithm (BFS for Topological Sort)
    Queue<Character> q = new ArrayDeque<>();
    for (char c : indegree.keySet()) {
        if (indegree.get(c) == 0) {
            q.offer(c);
        }
    }

    StringBuilder sb = new StringBuilder();
    while (!q.isEmpty()) {
        char curr = q.poll();
        sb.append(curr);

        for (char neighbor : adj.get(curr)) {
            indegree.put(neighbor, indegree.get(neighbor) - 1);
            if (indegree.get(neighbor) == 0) {
                q.offer(neighbor);
            }
        }
    }

    // If not all characters are in the result, there was a cycle
    return sb.length() == indegree.size() ? sb.toString() : "";
}
```

---

### Q4: Graph Valid Tree (LC 261)
**Example:** `n = 5, edges = [[0,1],[0,2],[0,3],[1,4]]` -> `true`  
**Explanation:** Given `n` nodes labeled from `0` to `n-1` and a list of undirected `edges`, write a function to check whether these edges make up a valid tree.

**Pattern:** **Union-Find** or **DFS/BFS with cycle detection and connectivity check**. A graph is a valid tree if and only if it is connected and contains no cycles. For `n` nodes, a connected graph with `n-1` edges is always a tree.

```java
class UnionFind {
    int[] parent;
    int count; // Number of connected components

    public UnionFind(int n) {
        parent = new int[n];
        for (int i = 0; i < n; i++) {
            parent[i] = i;
        }
        count = n;
    }

    public int find(int i) {
        if (parent[i] == i) {
            return i;
        }
        return parent[i] = find(parent[i]); // Path compression
    }

    // Returns true if a union happened (i.e., u and v were in different sets)
    public boolean union(int u, int v) {
        int rootU = find(u);
        int rootV = find(v);
        if (rootU != rootV) {
            parent[rootU] = rootV; // Simple union (can be optimized with rank/size)
            count--; // Decrement component count
            return true;
        }
        return false; // u and v were already connected (cycle detected)
    }
}

public boolean validTree(int n, int[][] edges) {
    // A tree must have exactly n-1 edges
    if (edges.length != n - 1) {
        return false;
    }

    UnionFind uf = new UnionFind(n);

    // Iterate through edges and perform union operations
    for (int[] edge : edges) {
        int u = edge;
        int v = edge;
        // If union returns false, it means adding this edge creates a cycle
        if (!uf.union(u, v)) {
            return false; // Cycle detected
        }
    }

    // After processing all edges, if there's exactly one component, it's connected
    return uf.count == 1;
}
```

---

### Q5: Reconstruct Itinerary (LC 332)
**Example:** `tickets = [["MUC","LHR"],["JFK","MUC"],["SFO","SJC"],["LHR","SFO"]]` -> `["JFK","MUC","LHR","SFO","SJC"]`  
**Explanation:** Given a list of airline tickets, where `tickets[i] = [from_i, to_i]` represents the departure and arrival airports of one flight. Reconstruct the itinerary in order. Always start from `JFK`. If there are multiple valid itineraries, you should return the itinerary that has the smallest lexical order when read as a single string.

**Pattern:** **Hierholzer's Algorithm (Eulerian Path/Circuit)**. This is a variation of DFS. Since we need the lexicographically smallest path, we use a `TreeMap` (or sort `LinkedList`s) for neighbors.

```java
public List<String> findItinerary(List<List<String>> tickets) {
    // Build graph: airport -> min-heap of destinations (for lexical order)
    Map<String, PriorityQueue<String>> adj = new HashMap<>();
    for (List<String> ticket : tickets) {
        adj.computeIfAbsent(ticket.get(0), k -> new PriorityQueue<>()).add(ticket.get(1));
    }

    List<String> itinerary = new LinkedList<>(); // Use LinkedList for efficient addFirst
    dfs("JFK", adj, itinerary);
    return itinerary;
}

private void dfs(String airport, Map<String, PriorityQueue<String>> adj, List<String> itinerary) {
    // While there are unvisited edges from the current airport
    PriorityQueue<String> destinations = adj.get(airport);
    while (destinations != null && !destinations.isEmpty()) {
        String nextAirport = destinations.poll();
        dfs(nextAirport, adj, itinerary);
    }
    // Add airport to the front of the list (post-order traversal)
    itinerary.add(0, airport); 
}
```

---

### Q6: Min Cost to Connect All Points (LC 1584)
**Example:** `points = [[0,0],[2,2],[3,10],[5,2],[7,0]]` -> `20`  
**Explanation:** You are given an array of points representing integer coordinates of some points on a 2D plane. The cost of connecting two points `(x1, y1)` and `(x2, y2)` is their Manhattan distance: `|x1 - x2| + |y1 - y2|`. Find the minimum cost to connect all points.

**Pattern:** **Minimum Spanning Tree (Kruskal's Algorithm)**. Build all possible edges with their Manhattan distances, sort them, and use Union-Find to add edges without forming cycles until `n-1` edges are added.

```java
class UnionFind {
    int[] parent;
    int count; // Number of disjoint sets

    public UnionFind(int n) {
        parent = new int[n];
        for (int i = 0; i < n; i++) parent[i] = i;
        count = n;
    }

    public int find(int i) {
        if (parent[i] == i) return i;
        return parent[i] = find(parent[i]);
    }

    public boolean union(int u, int v) {
        int rootU = find(u);
        int rootV = find(v);
        if (rootU != rootV) {
            parent[rootU] = rootV;
            count--;
            return true;
        }
        return false;
    }
}

public int minCostConnectPoints(int[][] points) {
    int n = points.length;
    List<int[]> allEdges = new ArrayList<>(); // {cost, point1_idx, point2_idx}

    // 1. Generate all possible edges and their costs
    for (int i = 0; i < n; i++) {
        for (int j = i + 1; j < n; j++) {
            int dist = Math.abs(points[i] - points[j]) + Math.abs(points[i] - points[j]);
            allEdges.add(new int[]{dist, i, j});
        }
    }

    // 2. Sort edges by cost
    Collections.sort(allEdges, (a, b) -> a - b);

    // 3. Use Kruskal's algorithm with Union-Find
    UnionFind uf = new UnionFind(n);
    int minCost = 0;
    int edgesUsed = 0;

    for (int[] edge : allEdges) {
        int cost = edge;
        int p1 = edge;
        int p2 = edge;

        if (uf.union(p1, p2)) { // If adding this edge doesn't form a cycle
            minCost += cost;
            edgesUsed++;
            if (edgesUsed == n - 1) { // MST has n-1 edges
                break;
            }
        }
    }
    return minCost;
}
```
---

## 11. Dynamic Programming (1-D)

### Core Theory & Patterns

1.  **Memoization (Top-Down):** Start with the big problem and break it down recursively. Store results in a `Map` or `Array` to avoid re-calculating (Overlapping Subproblems).
2.  **Tabulation (Bottom-Up):** Solve the smallest subproblems first and build up to the target. This is usually more space-efficient and avoids `StackOverflowError`.
3.  **Optimal Substructure:** The global optimal solution can be constructed from the optimal solutions of its subproblems.
4.  **The Decision Tree:** For every element, ask: "Do I take this or skip it?" or "Which of the previous $k$ steps is the best?".
5.  **Space Optimization:** If `dp[i]` only depends on `dp[i-1]` and `dp[i-2]`, you don't need an $O(N)$ array. Use two variables to achieve **$O(1)$ space**.

---

### Q1: Climbing Stairs (LC 70)
**Example:** `n = 3` -> `3` (1+1+1, 1+2, 2+1)  
**Pattern:** **Fibonacci**. To reach step $i$, you must come from $i-1$ or $i-2$.

```java
public int climbStairs(int n) {
    if (n <= 2) return n;
    int one = 1, two = 2; // Steps for n-2 and n-1
    for (int i = 3; i <= n; i++) {
        int temp = one + two;
        one = two;
        two = temp;
    }
    return two;
}
```

---

### Q2: Min Cost Climbing Stairs (LC 746)
**Pattern:** **Greedy + DP**. At each step, the cost is `cost[i] + min(dp[i-1], dp[i-2])`.

```java
public int minCostClimbingStairs(int[] cost) {
    int n = cost.length;
    for (int i = 2; i < n; i++) {
        cost[i] += Math.min(cost[i - 1], cost[i - 2]);
    }
    return Math.min(cost[n - 1], cost[n - 2]);
}
```

---

### Q3: House Robber (LC 198)
**Pattern:** **Select vs. Skip**. `dp[i] = max(rob current + dp[i-2], rob previous)`.

```java
public int rob(int[] nums) {
    int rob1 = 0, rob2 = 0;
    // [rob1, rob2, n, n+1, ...]
    for (int n : nums) {
        int temp = Math.max(rob1 + n, rob2);
        rob1 = rob2;
        rob2 = temp;
    }
    return rob2;
}
```

---

### Q4: House Robber II (LC 213)
**Explanation:** Houses are in a circle (1st and last are neighbors).  
**Pattern:** **Conditional DP**. Run `House Robber` twice: once excluding the first house, once excluding the last. Return the max of both.

```java
public int rob(int[] nums) {
    if (nums.length == 1) return nums[0];
    return Math.max(robRange(nums, 0, nums.length - 2), 
                    robRange(nums, 1, nums.length - 1));
}

private int robRange(int[] nums, int start, int end) {
    int r1 = 0, r2 = 0;
    for (int i = start; i <= end; i++) {
        int temp = Math.max(r1 + nums[i], r2);
        r1 = r2;
        r2 = temp;
    }
    return r2;
}
```

---

### Q5: Longest Palindromic Substring (LC 5)
**Pattern:** **Expand Around Center**. For every character (and every space between characters), expand outward as long as it's a palindrome. $O(N^2)$ time, $O(1)$ space.

```java
public String longestPalindrome(String s) {
    String res = "";
    for (int i = 0; i < s.length(); i++) {
        // Odd length
        String s1 = expand(s, i, i);
        if (s1.length() > res.length()) res = s1;
        // Even length
        String s2 = expand(s, i, i + 1);
        if (s2.length() > res.length()) res = s2;
    }
    return res;
}

private String expand(String s, int l, int r) {
    while (l >= 0 && r < s.length() && s.charAt(l) == s.charAt(r)) {
        l--; r++;
    }
    return s.substring(l + 1, r);
}
```

---

### Q6: Palindromic Substrings (LC 647)
**Pattern:** **Center Expansion Count**. Same as Q5, but return the total count of valid expansions.

```java
public int countSubstrings(String s) {
    int count = 0;
    for (int i = 0; i < s.length(); i++) {
        count += countPal(s, i, i);     // Odd
        count += countPal(s, i, i + 1); // Even
    }
    return count;
}

private int countPal(String s, int l, int r) {
    int res = 0;
    while (l >= 0 && r < s.length() && s.charAt(l) == s.charAt(r)) {
        res++; l--; r++;
    }
    return res;
}
```

---

### Q7: Decode Ways (LC 91)
**Example:** `"12"` -> `2` (A-B "1-2" or L "12")  
**Pattern:** **Conditional Fibonacci**. $dp[i]$ depends on $dp[i-1]$ (if valid single digit) and $dp[i-2]$ (if valid two-digit number between 10-26).

```java
public int numDecodings(String s) {
    int n = s.length();
    int[] dp = new int[n + 1];
    dp[0] = 1; // Base case for empty string
    dp[1] = s.charAt(0) == '0' ? 0 : 1;
    
    for (int i = 2; i <= n; i++) {
        // Single digit
        if (s.charAt(i - 1) != '0') dp[i] += dp[i - 1];
        // Two digits
        int twoDigit = Integer.parseInt(s.substring(i - 2, i));
        if (twoDigit >= 10 && twoDigit <= 26) dp[i] += dp[i - 2];
    }
    return dp[n];
}
```

---

### Q8: Coin Change (LC 322)
**Pattern:** **Unbounded Knapsack**. $dp[amount] = \min(dp[amount], 1 + dp[amount - coin])$. Initialize with `amount + 1` (acting as infinity).

```java
public int coinChange(int[] coins, int amount) {
    int[] dp = new int[amount + 1];
    Arrays.fill(dp, amount + 1);
    dp[0] = 0;
    
    for (int a = 1; a <= amount; a++) {
        for (int c : coins) {
            if (a - c >= 0) {
                dp[a] = Math.min(dp[a], 1 + dp[a - c]);
            }
        }
    }
    return dp[amount] > amount ? -1 : dp[amount];
}
```

---

### Q9: Maximum Product Subarray (LC 152)
**Pattern:** **Dual-State DP**. Because of negative numbers, keep track of both the `currentMax` and `currentMin` product ending at index $i$.

```java
public int maxProduct(int[] nums) {
    int res = nums[0];
    int curMin = 1, curMax = 1;
    
    for (int n : nums) {
        int tmp = curMax * n;
        curMax = Math.max(Math.max(n * curMax, n * curMin), n);
        curMin = Math.min(Math.min(tmp, n * curMin), n);
        res = Math.max(res, curMax);
    }
    return res;
}
```

---

### Q10: Word Break (LC 139)
**Pattern:** **Linear DP with Substrings**. $dp[i]$ is true if the prefix $s[0...i]$ can be segmented. To check $dp[i]$, look at all previous $dp[j]$ where $j < i$ and check if $s[j...i]$ is in the dictionary.

```java
public boolean wordBreak(String s, List<String> wordDict) {
    boolean[] dp = new boolean[s.length() + 1];
    dp[0] = true;
    
    for (int i = 1; i <= s.length(); i++) {
        for (String w : wordDict) {
            int start = i - w.length();
            if (start >= 0 && dp[start] && s.substring(start, i).equals(w)) {
                dp[i] = true;
                break;
            }
        }
    }
    return dp[s.length()];
}
```
---

### Q11: Longest Increasing Subsequence (LC 300)
**Example:** `nums = [10,9,2,5,3,7,101,18]` -> `4` ([2,3,7,18])
**Pattern:** **LIS DP**. For every $i$, look at all $j < i$. If `nums[i] > nums[j]`, then `dp[i] = max(dp[i], 1 + dp[j])`.

```java
public int lengthOfLIS(int[] nums) {
    int[] dp = new int[nums.length];
    Arrays.fill(dp, 1);
    int maxLIS = 1;
    
    for (int i = 1; i < nums.length; i++) {
        for (int j = 0; j < i; j++) {
            if (nums[i] > nums[j]) {
                dp[i] = Math.max(dp[i], 1 + dp[j]);
            }
        }
        maxLIS = Math.max(maxLIS, dp[i]);
    }
    return maxLIS;
}
```

---

### Q12: Partition Equal Subset Sum (LC 416)
**Pattern:** **0/1 Knapsack (Subset Sum)**. Can we find a subset that sums to `totalSum / 2`? Use a `HashSet` to store possible sums or a 1D boolean array (iterating backwards to avoid using the same element twice).

```java
public boolean canPartition(int[] nums) {
    int sum = 0;
    for (int n : nums) sum += n;
    if (sum % 2 != 0) return false;
    
    int target = sum / 2;
    boolean[] dp = new boolean[target + 1];
    dp[0] = true;
    
    for (int n : nums) {
        // Iterate backwards to ensure we only use each number once
        for (int i = target; i >= n; i--) {
            if (dp[i - n]) dp[i] = true;
        }
    }
    return dp[target];
}
```

---

## 12. Dynamic Programming (2-D)

### Core Theory & Patterns

1.  **The Grid/Matrix State:** `dp[i][j]` usually represents the optimal solution for `prefix1[0...i]` and `prefix2[0...j]`.
2.  **String Matching Logic:**
    *   **Match:** `dp[i][j] = 1 + dp[i-1][j-1]` (move diagonally).
    *   **Mismatch:** `max(dp[i-1][j], dp[i][j-1])` (skip one character from either string).
3.  **Space Compression:** Since `dp[i]` often only depends on `dp[i-1]`, you can reduce $O(M \times N)$ space to $O(N)$ by using two rows or updating a single row in-place.

---

### Q1: Unique Paths (LC 62)
**Example:** `m = 3, n = 7` -> `28`
**Pattern:** **Grid DP**. $dp[r][c] = dp[r-1][c] + dp[r][c-1]$. You can only arrive from the top or the left.

```java
public int uniquePaths(int m, int n) {
    int[] row = new int[n];
    Arrays.fill(row, 1); // Top row is all 1s
    
    for (int i = 1; i < m; i++) {
        for (int j = 1; j < n; j++) {
            row[j] = row[j] + row[j - 1]; // Current = Top + Left
        }
    }
    return row[n - 1];
}
```

---

### Q14: Longest Common Subsequence (LC 1143)
**Pattern:** **2D String DP**. If `s1[i] == s2[j]`, take $1 +$ diagonal. Else, take `max(top, left)`.

```java
public int longestCommonSubsequence(String text1, String text2) {
    int[][] dp = new int[text1.length() + 1][text2.length() + 1];
    
    for (int i = 1; i <= text1.length(); i++) {
        for (int j = 1; j <= text2.length(); j++) {
            if (text1.charAt(i-1) == text2.charAt(j-1)) {
                dp[i][j] = 1 + dp[i-1][j-1];
            } else {
                dp[i][j] = Math.max(dp[i-1][j], dp[i][j-1]);
            }
        }
    }
    return dp[text1.length()][text2.length()];
}
```

---

### Q15: Best Time to Buy and Sell Stock with Cooldown (LC 309)
**Pattern:** **State Machine DP**. States: `Buying`, `Selling`. If you sell, the next state *must* be a cooldown day (skip index+2).

```java
public int maxProfit(int[] prices) {
    Map<String, Integer> memo = new HashMap<>();
    return dfs(0, true, prices, memo);
}

private int dfs(int i, boolean buying, int[] prices, Map<String, Integer> memo) {
    if (i >= prices.length) return 0;
    String key = i + "-" + buying;
    if (memo.containsKey(key)) return memo.get(key);
    
    int cooldown = dfs(i + 1, buying, prices, memo);
    if (buying) {
        int buy = dfs(i + 1, false, prices, memo) - prices[i];
        memo.put(key, Math.max(buy, cooldown));
    } else {
        // Sell: next index is i + 2 due to cooldown
        int sell = dfs(i + 2, true, prices, memo) + prices[i];
        memo.put(key, Math.max(sell, cooldown));
    }
    return memo.get(key);
}
```

---

### Q16: Coin Change II (LC 518)
**Pattern:** **2D Unbounded Knapsack**. Number of combinations. $dp[i] += dp[i - coin]$.

```java
public int change(int amount, int[] coins) {
    int[] dp = new int[amount + 1];
    dp[0] = 1;
    for (int coin : coins) {
        for (int i = coin; i <= amount; i++) {
            dp[i] += dp[i - coin];
        }
    }
    return dp[amount];
}
```

---

### Q17: Target Sum (LC 494)
**Pattern:** **0/1 Knapsack**. Find number of ways to assign +/- to reach target. This is equivalent to finding a subset with sum $(target + totalSum) / 2$.

```java
public int findTargetSumWays(int[] nums, int target) {
    int total = 0;
    for (int n : nums) total += n;
    if (Math.abs(target) > total || (target + total) % 2 != 0) return 0;
    
    int s1 = (target + total) / 2;
    int[] dp = new int[s1 + 1];
    dp[0] = 1;
    for (int n : nums) {
        for (int i = s1; i >= n; i--) dp[i] += dp[i - n];
    }
    return dp[s1];
}
```

---

### Q18: Interleaving String (LC 97)
**Pattern:** **2D Grid Path**. $dp[i][j]$ is true if `s3[i+j]` can be formed by `s1[i]` or `s2[j]`.

```java
public boolean isInterleave(String s1, String s2, String s3) {
    if (s1.length() + s2.length() != s3.length()) return false;
    boolean[][] dp = new boolean[s1.length() + 1][s2.length() + 1];
    dp[0][0] = true;
    
    for (int i = 0; i <= s1.length(); i++) {
        for (int j = 0; j <= s2.length(); j++) {
            if (i > 0 && s1.charAt(i-1) == s3.charAt(i+j-1)) dp[i][j] |= dp[i-1][j];
            if (j > 0 && s2.charAt(j-1) == s3.charAt(i+j-1)) dp[i][j] |= dp[i][j-1];
        }
    }
    return dp[s1.length()][s2.length()];
}
```

---

### Q19: Edit Distance (LC 72)
**Pattern:** **2D String DP**. 
*   If match: `dp[i-1][j-1]`.
*   If mismatch: $1 + \min(\text{insert, delete, replace})$.

```java
public int minDistance(String word1, String word2) {
    int m = word1.length(), n = word2.length();
    int[][] dp = new int[m + 1][n + 1];
    
    for (int i = 1; i <= m; i++) dp[i][0] = i;
    for (int j = 1; j <= n; j++) dp[0][j] = j;
    
    for (int i = 1; i <= m; i++) {
        for (int j = 1; j <= n; j++) {
            if (word1.charAt(i-1) == word2.charAt(j-1)) {
                dp[i][j] = dp[i-1][j-1];
            } else {
                dp[i][j] = 1 + Math.min(dp[i-1][j-1], Math.min(dp[i-1][j], dp[i][j-1]));
            }
        }
    }
    return dp[m][n];
}
```
---

### Q20: Longest Increasing Path in a Matrix (LC 329)
**Example:** `matrix = [[9,9,4],[6,6,8],[2,1,1]]` -> `4` ([1, 2, 6, 9])  
**Explanation:** Find the length of the longest increasing path in an $m \times n$ integers matrix.

**Pattern:** **DFS + Memoization**. Since the path must be strictly increasing, there are no cycles. Use a 2D `memo` array to store the longest path starting from each cell $(r, c)$.

```java
public int longestIncreasingPath(int[][] matrix) {
    int R = matrix.length, C = matrix[0].length;
    int[][] memo = new int[R][C];
    int maxPath = 0;
    for (int r = 0; r < R; r++) {
        for (int c = 0; c < C; c++) {
            maxPath = Math.max(maxPath, dfs(matrix, r, c, -1, memo));
        }
    }
    return maxPath;
}

private int dfs(int[][] matrix, int r, int c, int prevVal, int[][] memo) {
    if (r < 0 || r >= matrix.length || c < 0 || c >= matrix[0].length || matrix[r][c] <= prevVal) return 0;
    if (memo[r][c] != 0) return memo[r][c];
    
    int res = 1;
    res = Math.max(res, 1 + dfs(matrix, r + 1, c, matrix[r][c], memo));
    res = Math.max(res, 1 + dfs(matrix, r - 1, c, matrix[r][c], memo));
    res = Math.max(res, 1 + dfs(matrix, r, c + 1, matrix[r][c], memo));
    res = Math.max(res, 1 + dfs(matrix, r, c - 1, matrix[r][c], memo));
    
    return memo[r][c] = res;
}
```

---

### Q21: Distinct Subsequences (LC 115)
**Example:** `s = "rabbbit", t = "rabbit"` -> `3`  
**Explanation:** Count how many ways string `t` can be formed as a subsequence of `s`.

**Pattern:** **2D String Matching**. 
*   If `s[i] == t[j]`: We have two choices. Use `s[i]` to match `t[j]` (`dp[i-1][j-1]`) OR skip `s[i]` and look for `t[j]` earlier in `s` (`dp[i-1][j]`).
*   If `s[i] != t[j]`: We must skip `s[i]` (`dp[i-1][j]`).

```java
public int numDistinct(String s, String t) {
    int m = s.length(), n = t.length();
    int[][] dp = new int[m + 1][n + 1];
    for (int i = 0; i <= m; i++) dp[i][0] = 1; // Empty t can be formed by any s
    
    for (int i = 1; i <= m; i++) {
        for (int j = 1; j <= n; j++) {
            if (s.charAt(i - 1) == t.charAt(j - 1)) {
                dp[i][j] = dp[i - 1][j - 1] + dp[i - 1][j];
            } else {
                dp[i][j] = dp[i - 1][j];
            }
        }
    }
    return dp[m][n];
}
```

---

### Q22: Burst Balloons (LC 312)
**Example:** `nums = [3,1,5,8]` -> `167`  
**Explanation:** Burst balloons to maximize coins. If you burst `i`, you get `nums[left] * nums[i] * nums[right]`.

**Pattern:** **Interval DP**. Think backwards: which balloon is the **last** to burst in the range `(L, R)`? 
`dp[L][R] = max(nums[L-1]*nums[i]*nums[R+1] + dp[L][i-1] + dp[i+1][R])`.

```java
public int maxCoins(int[] nums) {
    int n = nums.length;
    int[] B = new int[n + 2]; // Add virtual boundaries of 1
    B[0] = B[n + 1] = 1;
    for (int i = 0; i < n; i++) B[i + 1] = nums[i];
    
    int[][] dp = new int[n + 2][n + 2];
    for (int len = 1; len <= n; len++) {
        for (int L = 1; L <= n - len + 1; L++) {
            int R = L + len - 1;
            for (int i = L; i <= R; i++) {
                dp[L][R] = Math.max(dp[L][R], 
                    B[L-1] * B[i] * B[R+1] + dp[L][i-1] + dp[i+1][R]);
            }
        }
    }
    return dp[1][n];
}
```

---

### Q23: Regular Expression Matching (LC 10)
**Example:** `s = "aa", p = "a*"` -> `true`  
**Explanation:** Support `.` (any char) and `*` (zero or more of the preceding element).

**Pattern:** **2D DP with Lookahead**. When encountering `*` at `p[j]`, you can either skip the `char*` combo (`dp[i][j-2]`) or, if characters match, use the `*` and stay at the same pattern index (`dp[i-1][j]`).

```java
public boolean isMatch(String s, String p) {
    int m = s.length(), n = p.length();
    boolean[][] dp = new boolean[m + 1][n + 1];
    dp[0][0] = true;
    
    // Handle empty s matching patterns like a*b*
    for (int j = 2; j <= n; j++) {
        if (p.charAt(j - 1) == '*') dp[0][j] = dp[0][j - 2];
    }
    
    for (int i = 1; i <= m; i++) {
        for (int j = 1; j <= n; j++) {
            char sc = s.charAt(i - 1), pc = p.charAt(j - 1);
            if (pc == sc || pc == '.') {
                dp[i][j] = dp[i - 1][j - 1];
            } else if (pc == '*') {
                // Case 1: 0 occurrences of the preceding char
                dp[i][j] = dp[i][j - 2];
                // Case 2: 1 or more occurrences (if sc matches the char before *)
                char prevP = p.charAt(j - 2);
                if (prevP == sc || prevP == '.') {
                    dp[i][j] |= dp[i - 1][j];
                }
            }
        }
    }
    return dp[m][n];
}
```

---

## 13. Heaps / Priority Queue

### Core Theory & Patterns

1.  **Properties:** A Heap is a complete binary tree.
    *   **Min-Heap:** `parent <= children`. Root is the minimum.
    *   **Max-Heap:** `parent >= children`. Root is the maximum.
2.  **Complexity:** `push` and `pop` are $O(\log N)$. `peek` is $O(1)$. `Heapify` (building a heap from an array) is $O(N)$.
3.  **Top-K Pattern:** To find the $K$ largest elements, maintain a **Min-Heap** of size $K$. If the next element is larger than the root, replace the root. At the end, the heap contains the $K$ largest values.
4.  **Two Heaps Pattern:** Used for finding the median of a dynamic data stream.
5.  **Task Scheduling:** Combine a Max-Heap (for priority/frequency) with a Queue (for cooldown/waiting periods).
6.  **Java Implementation:** Always use `PriorityQueue<T>`. For a Max-Heap, use `new PriorityQueue<>(Collections.reverseOrder())` or a custom comparator `(a, b) -> b - a`.

---

### Q1: Kth Largest Element in a Stream (LC 703)
**Pattern:** **Fixed-Size Min-Heap**. Maintain a Min-Heap of size $K$. The root will always be the $K$-th largest element.

```java
class KthLargest {
    private PriorityQueue<Integer> minHeap;
    private int k;

    public KthLargest(int k, int[] nums) {
        this.k = k;
        this.minHeap = new PriorityQueue<>();
        for (int n : nums) add(n);
    }
    
    public int add(int val) {
        minHeap.offer(val);
        if (minHeap.size() > k) minHeap.poll();
        return minHeap.peek();
    }
}
```

---

### Q2: Last Stone Weight (LC 1046)
**Pattern:** **Max-Heap Simulation**. Always smash the two heaviest stones.

```java
public int lastStoneWeight(int[] stones) {
    PriorityQueue<Integer> maxHeap = new PriorityQueue<>(Collections.reverseOrder());
    for (int s : stones) maxHeap.add(s);
    
    while (maxHeap.size() > 1) {
        int stone1 = maxHeap.poll();
        int stone2 = maxHeap.poll();
        if (stone1 != stone2) maxHeap.add(stone1 - stone2);
    }
    return maxHeap.isEmpty() ? 0 : maxHeap.peek();
}
```

### Q3: K Closest Points to Origin (LC 973)
**Pattern:** **Max-Heap of size K**. To find the $K$ closest (smallest) distances, maintain a Max-Heap. When the size exceeds $K$, remove the largest distance.

```java
public int[][] kClosest(int[][] points, int k) {
    // Max-Heap: Compare distances descending
    PriorityQueue<int[]> pq = new PriorityQueue<>(
        (a, b) -> Integer.compare((b[0]*b[0] + b[1]*b[1]), (a[0]*a[0] + a[1]*a[1]))
    );
    
    for (int[] p : points) {
        pq.offer(p);
        if (pq.size() > k) pq.poll();
    }
    
    int[][] res = new int[k][2];
    while (k > 0) res[--k] = pq.poll();
    return res;
}
```

---

### Q4: Kth Largest Element in an Array (LC 215)
**Pattern:** **Min-Heap**. Similar to Q1, maintain a Min-Heap of size $K$. This is $O(N \log K)$.

```java
public int findKthLargest(int[] nums, int k) {
    PriorityQueue<Integer> minHeap = new PriorityQueue<>();
    for (int n : nums) {
        minHeap.offer(n);
        if (minHeap.size() > k) minHeap.poll();
    }
    return minHeap.peek();
}
```

---

### Q5: Task Scheduler (LC 621)
**Pattern:** **Frequency Map + Max-Heap + Queue**. Use Max-Heap for highest frequency tasks and a Queue to track tasks in the cooldown period.

```java
public int leastInterval(char[] tasks, int n) {
    int[] freq = new int[26];
    for (char t : tasks) freq[t - 'a']++;
    
    PriorityQueue<Integer> maxHeap = new PriorityQueue<>(Collections.reverseOrder());
    for (int f : freq) if (f > 0) maxHeap.add(f);
    
    Queue<int[]> q = new ArrayDeque<>(); // [count, idleTime]
    int time = 0;
    
    while (!maxHeap.isEmpty() || !q.isEmpty()) {
        time++;
        if (!maxHeap.isEmpty()) {
            int count = maxHeap.poll() - 1;
            if (count > 0) q.add(new int[]{count, time + n});
        }
        
        if (!q.isEmpty() && q.peek()[1] == time) {
            maxHeap.add(q.poll()[0]);
        }
    }
    return time;
}
```

---

### Q6: Design Twitter (LC 355)
**Pattern:** **Hashing + Merging K-Sorted Lists**. Track follows/tweets in Maps. Feed generation uses a Min-Heap to pick the 10 most recent tweets from multiple lists.

```java
class Twitter {
    private static int timeStamp = 0;
    private Map<Integer, Set<Integer>> followMap = new HashMap<>();
    private Map<Integer, List<int[]>> tweetMap = new HashMap<>(); // [time, tweetId]

    public void postTweet(int userId, int tweetId) {
        tweetMap.computeIfAbsent(userId, k -> new ArrayList<>()).add(new int[]{timeStamp++, tweetId});
    }

    public List<Integer> getNewsFeed(int userId) {
        PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> b[0] - a[0]); // Max-heap on time
        Set<Integer> users = new HashSet<>(followMap.getOrDefault(userId, new HashSet<>()));
        users.add(userId);
        
        for (int user : users) {
            List<int[]> tweets = tweetMap.get(user);
            if (tweets == null) continue;
            // Optimize: only add the last 10 tweets from each user to start
            int n = tweets.size();
            for (int i = n - 1; i >= Math.max(0, n - 10); i--) {
                pq.add(tweets.get(i));
            }
        }
        
        List<Integer> res = new ArrayList<>();
        while (!pq.isEmpty() && res.size() < 10) res.add(pq.poll()[1]);
        return res;
    }

    public void follow(int followerId, int followeeId) {
        followMap.computeIfAbsent(followerId, k -> new HashSet<>()).add(followeeId);
    }

    public void unfollow(int followerId, int followeeId) {
        if (followMap.containsKey(followerId)) followMap.get(followerId).remove(followeeId);
    }
}
```

---

### Q7: Find Median from Data Stream (LC 295)
**Pattern:** **Two Heaps**. `small` (Max-Heap) for the left half, `large` (Min-Heap) for the right half.

```java
class MedianFinder {
    private PriorityQueue<Integer> small = new PriorityQueue<>(Collections.reverseOrder());
    private PriorityQueue<Integer> large = new PriorityQueue<>();

    public void addNum(int num) {
        small.add(num);
        // Balancing: small elements must be <= large elements
        if (!small.isEmpty() && !large.isEmpty() && small.peek() > large.peek()) {
            large.add(small.poll());
        }
        // Balancing: size difference <= 1
        if (small.size() > large.size() + 1) {
            large.add(small.poll());
        } else if (large.size() > small.size() + 1) {
            small.add(large.poll());
        }
    }

    public double findMedian() {
        if (small.size() > large.size()) return small.peek();
        if (large.size() > small.size()) return large.peek();
        return (small.peek() + large.peek()) / 2.0;
    }
}
```
---

## 14. Greedy

### Core Theory & Patterns

1.  **Local Optimum -> Global Optimum:** The fundamental principle. A greedy algorithm makes the best choice at each step, hoping this will lead to the best overall solution.
2.  **No Regrets:** A greedy choice, once made, is never reconsidered. This is what differentiates it from Dynamic Programming or Backtracking.
3.  **Proof of Correctness:** Often the hardest part. Typically involves an "exchange argument" or showing that any optimal solution can be transformed into the greedy solution without loss of optimality.
4.  **Sorting:** Many greedy problems require sorting the input (by start time, end time, value, etc.) to make the "best local choice" evident.
5.  **Priority Queues:** Used when the "best local choice" isn't immediately obvious from sorting, but needs to be dynamically selected (e.g., always picking the smallest available item).

---

### Q1: Jump Game (LC 55)
**Example:** `nums = [2,3,1,1,4]` -> `true`  
**Explanation:** Determine if you can reach the last index.

**Pattern:** **Track Furthest Reachable**. Iterate through the array, updating the maximum index you can reach. If at any point you can't move past the current index, and you haven't reached the end, it's impossible.

```java
public boolean canJump(int[] nums) {
    int reachable = 0;
    for (int i = 0; i < nums.length; i++) {
        if (i > reachable) return false; // Cannot reach current index
        reachable = Math.max(reachable, i + nums[i]);
    }
    return true;
}
```

---

### Q2: Jump Game II (LC 45)
**Example:** `nums = [2,3,1,1,4]` -> `2`  
**Explanation:** Find the minimum number of jumps to reach the last index.

**Pattern:** **BFS-like Greedy**. At each jump, find the furthest possible reach.

```java
public int jump(int[] nums) {
    int jumps = 0;
    int currentEnd = 0; // Max index reachable with current number of jumps
    int furthest = 0;   // Max index reachable from any point in current jump range
    
    for (int i = 0; i < nums.length - 1; i++) {
        furthest = Math.max(furthest, i + nums[i]);
        
        // If we've reached the end of the current jump's range
        if (i == currentEnd) {
            jumps++;
            currentEnd = furthest; // Start a new jump from the furthest point reached
        }
    }
    return jumps;
}
```

---

### Q3: Gas Station (LC 134)
**Example:** `gas = [1,2,3,4,5], cost = [3,4,5,1,2]` -> `3`  
**Explanation:** Find the starting gas station index if you can complete a circular tour.

**Pattern:** **Net Gain/Loss + Single Pass**. If `totalGas < totalCost`, it's impossible. Otherwise, there's a unique solution. If `currentTank` drops below zero, reset `start` to `i+1` and `currentTank` to zero.

```java
public int canCompleteCircuit(int[] gas, int[] cost) {
    int totalGas = 0, totalCost = 0;
    for (int i = 0; i < gas.length; i++) {
        totalGas += gas[i];
        totalCost += cost[i];
    }
    if (totalGas < totalCost) return -1; // Impossible to complete
    
    int start = 0;
    int currentTank = 0;
    for (int i = 0; i < gas.length; i++) {
        currentTank += gas[i] - cost[i];
        if (currentTank < 0) {
            start = i + 1; // Try next station as start
            currentTank = 0; // Reset tank
        }
    }
    return start;
}
```

---

### Q4: Hand of Straights (LC 846)
**Example:** `hand = [1,2,3,6,2,3,4,7,8], groupSize = 3` -> `true`  
**Explanation:** Check if the hand can be arranged into groups of `groupSize` consecutive cards.

**Pattern:** **Frequency Map + Greedy Smallest**. Use a `TreeMap` to store card frequencies (maintains sorted order). Always try to form a group starting with the smallest available card.

```java
public boolean isNStraightHand(int[] hand, int groupSize) {
    if (hand.length % groupSize != 0) return false;
    
    TreeMap<Integer, Integer> cardCounts = new TreeMap<>();
    for (int card : hand) {
        cardCounts.put(card, cardCounts.getOrDefault(card, 0) + 1);
    }
    
    while (!cardCounts.isEmpty()) {
        int firstCard = cardCounts.firstKey();
        for (int i = 0; i < groupSize; i++) {
            int currentCard = firstCard + i;
            if (!cardCounts.containsKey(currentCard) || cardCounts.get(currentCard) == 0) {
                return false; // Missing a consecutive card
            }
            cardCounts.put(currentCard, cardCounts.get(currentCard) - 1);
            if (cardCounts.get(currentCard) == 0) {
                cardCounts.remove(currentCard);
            }
        }
    }
    return true;
}
```

---

### Q5: Merge Triplets to Form Target (LC 1899)
**Example:** `triplets = [[2,5,3],[1,8,4],[1,7,5]], target = [2,7,5]` -> `true`  
**Explanation:** You are given an array of `triplets` and a `target` triplet. You can merge two triplets `[a,b,c]` and `[d,e,f]` into `[max(a,d), max(b,e), max(c,f)]`. Return `true` if you can obtain the `target` triplet.

**Pattern:** **Greedy Filtering**. Only consider triplets where all elements are less than or equal to the corresponding target element. Then, greedily merge these valid triplets to see if the target can be formed.

```java
public boolean mergeTriplets(int[][] triplets, int[] target) {
    int[] res = new int; // Represents the merged triplet so far
    
    for (int[] t : triplets) {
        // Only consider triplets that don't exceed any target component
        if (t <= target && t <= target && t <= target) {
            res = Math.max(res, t);
            res = Math.max(res, t);
            res = Math.max(res, t);
        }
    }
    
    // Check if the merged result matches the target
    return res == target && res == target && res == target;
}
```

### Q6: Partition Labels (LC 763)
**Example:** `triplets = [[2,5,3],[1,8,4],[1,7,5]], target = [2,7,5]` -> `true`  
**Explanation:** You are given an array of `triplets` and a `target` triplet. You can merge two triplets `[a,b,c]` and `[d,e,f]` into `[max(a,d), max(b,e), max(c,f)]`. Return `true` if you can obtain the `target` triplet.

**Pattern:** **Last Occurrence Map**. Record the last index of every character. Iterate through the string, maintaining a "furthest" index. When your current index reaches that furthest index, you've found a partition.

```java
public List<Integer> partitionLabels(String s) {
    int[] last = new int[26];
    for (int i = 0; i < s.length(); i++) last[s.charAt(i) - 'a'] = i;

    List<Integer> res = new ArrayList<>();
    int start = 0, end = 0;
    for (int i = 0; i < s.length(); i++) {
        end = Math.max(end, last[s.charAt(i) - 'a']);
        if (i == end) {
            res.add(end - start + 1);
            start = i + 1;
        }
    }
    return res;
}
```

### Q7: Valid Parenthesis String (LC 678)
**Example:** `triplets = [[2,5,3],[1,8,4],[1,7,5]], target = [2,7,5]` -> `true`  
**Explanation:** You are given an array of `triplets` and a `target` triplet. You can merge two triplets `[a,b,c]` and `[d,e,f]` into `[max(a,d), max(b,e), max(c,f)]`. Return `true` if you can obtain the `target` triplet.

**Pattern:** **Min/Max Open Count**. Track the range of possible open parentheses counts. `(` increments both, `)` decrements both, and `*` allows for both or staying the same.

```java
public boolean checkValidString(String s) {
    int minOpen = 0, maxOpen = 0;
    for (char c : s.toCharArray()) {
        if (c == '(') { minOpen++; maxOpen++; }
        else if (c == ')') { minOpen--; maxOpen--; }
        else { minOpen--; maxOpen++; }
        
        if (maxOpen < 0) return false;
        if (minOpen < 0) minOpen = 0; // * acted as empty or ')'
    }
    return minOpen == 0;
}
```

---

## 15. Backtracking

### Core Theory & Patterns

1.  **The Three Pillars:**
    *   **Our Choices:** What decisions can we make at this step? (e.g., iterate through numbers, move 4 directions).
    *   **Our Constraints:** What makes a choice "bad"? (e.g., already used, out of bounds, violates Sudoku rules).
    *   **Our Goal:** When do we stop and add the "path" to our results? (e.g., target length reached, word found).
2.  **Pick vs. Not Pick (Subsets Pattern):** For each element, you have two branches in the recursion tree. Total complexity: $O(2^N)$.
3.  **The For-Loop (Permutations Pattern):** At each level, you iterate through all available candidates. Total complexity: $O(N!)$.
4.  **Pruning:** The secret to FAANG performance. If you can detect a constraint violation early, return immediately to save thousands of recursive calls.
5.  **State Management:** Always "Undo" the change after the recursive call (e.g., `list.remove(list.size() - 1)` or `visited[r][c] = false`) to restore the state for the next branch.

---

### Q1: Subsets (LC 78)
**Example:** `nums = [1,2,3]` -> `[[],[1],[2],[1,2],[3],[1,3],[2,3],[1,2,3]]`  
**Pattern:** **Pick / Not Pick**. At each index, decide whether to include the element.

```java
public List<List<Integer>> subsets(int[] nums) {
    List<List<Integer>> res = new ArrayList<>();
    backtrack(0, nums, new ArrayList<>(), res);
    return res;
}

private void backtrack(int i, int[] nums, List<Integer> curr, List<List<Integer>> res) {
    if (i == nums.length) {
        res.add(new ArrayList<>(curr)); // Add deep copy
        return;
    }
    // Decision: Include nums[i]
    curr.add(nums[i]);
    backtrack(i + 1, nums, curr, res);
    
    // Decision: Don't include nums[i] (Backtrack)
    curr.remove(curr.size() - 1);
    backtrack(i + 1, nums, curr, res);
}
```

---

### Q2: Combination Sum (LC 39)
**Example:** `candidates = [2,3,6,7], target = 7` -> `[[2,2,3],[7]]`  
**Pattern:** **Pick / Not Pick with Reuse**. You can pick the same element multiple times, so the "Pick" branch stays at index `i`.

```java
public List<List<Integer>> combinationSum(int[] nums, int target) {
    List<List<Integer>> res = new ArrayList<>();
    backtrack(0, target, nums, new ArrayList<>(), res);
    return res;
}

private void backtrack(int i, int target, int[] nums, List<Integer> curr, List<List<Integer>> res) {
    if (target == 0) {
        res.add(new ArrayList<>(curr));
        return;
    }
    if (i == nums.length || target < 0) return;

    // Option 1: Pick nums[i] (stay at i to allow reuse)
    curr.add(nums[i]);
    backtrack(i, target - nums[i], nums, curr, res);
    curr.remove(curr.size() - 1);

    // Option 2: Skip nums[i]
    backtrack(i + 1, target, nums, curr, res);
}
```

---

### Q3: Permutations (LC 46)
**Example:** `[1,2,3]` -> `[[1,2,3],[1,3,2],[2,1,3]...]`  
**Pattern:** **For-Loop with "Used" Tracking**. Order matters, so we iterate all numbers at each level but skip those already in the path.

```java
public List<List<Integer>> permute(int[] nums) {
    List<List<Integer>> res = new ArrayList<>();
    backtrack(nums, new ArrayList<>(), res, new boolean[nums.length]);
    return res;
}

private void backtrack(int[] nums, List<Integer> curr, List<List<Integer>> res, boolean[] used) {
    if (curr.size() == nums.length) {
        res.add(new ArrayList<>(curr));
        return;
    }
    for (int i = 0; i < nums.length; i++) {
        if (used[i]) continue;
        
        used[i] = true;
        curr.add(nums[i]);
        backtrack(nums, curr, res, used);
        
        curr.remove(curr.size() - 1);
        used[i] = false;
    }
}
```

---

### Q4: Subsets II (LC 90)
**Example:** `[1,2,2]` -> `[[],[1],[1,2],[1,2,2],[2],[2,2]]`  
**Pattern:** **Sort + Duplicate Pruning**. To avoid duplicate subsets, sort the array and skip the "Pick" branch for the same value at the same level.

```java
public List<List<Integer>> subsetsWithDup(int[] nums) {
    Arrays.sort(nums);
    List<List<Integer>> res = new ArrayList<>();
    backtrack(0, nums, new ArrayList<>(), res);
    return res;
}

private void backtrack(int i, int[] nums, List<Integer> curr, List<List<Integer>> res) {
    if (i == nums.length) {
        res.add(new ArrayList<>(curr));
        return;
    }
    // Branch 1: Pick nums[i]
    curr.add(nums[i]);
    backtrack(i + 1, nums, curr, res);
    curr.remove(curr.size() - 1);

    // Branch 2: Skip all duplicates of nums[i]
    while (i + 1 < nums.length && nums[i] == nums[i+1]) i++;
    backtrack(i + 1, nums, curr, res);
}
```

---

### Q5: Combination Sum II (LC 40)
**Example:** `candidates = [10,1,2,7,6,1,5], target = 8`  
**Pattern:** **Sort + Pruning**. Each number used once. Same pruning logic as Subsets II to avoid duplicate combinations.

```java
public List<List<Integer>> combinationSum2(int[] nums, int target) {
    Arrays.sort(nums);
    List<List<Integer>> res = new ArrayList<>();
    backtrack(0, target, nums, new ArrayList<>(), res);
    return res;
}

private void backtrack(int i, int target, int[] nums, List<Integer> curr, List<List<Integer>> res) {
    if (target == 0) {
        res.add(new ArrayList<>(curr));
        return;
    }
    if (i == nums.length || target < 0) return;

    // Choice 1: Pick nums[i]
    curr.add(nums[i]);
    backtrack(i + 1, target - nums[i], nums, curr, res);
    curr.remove(curr.size() - 1);

    // Choice 2: Skip nums[i] and all its duplicates
    while (i + 1 < nums.length && nums[i] == nums[i+1]) i++;
    backtrack(i + 1, target, nums, curr, res);
}
```

---

### Q6: Word Search (LC 79)
**Example:** `board = [["A","B","C","E"],["S","F","C","S"],["A","D","E","E"]], word = "ABCCED"` -> `true`  
**Pattern:** **Grid Backtracking**. DFS in 4 directions. Mark current cell as visited (e.g., `#`) before the call and restore after.

```java
public boolean exist(char[][] board, String word) {
    for (int r = 0; r < board.length; r++) {
        for (int c = 0; c < board[0].length; c++) {
            if (backtrack(board, word, r, c, 0)) return true;
        }
    }
    return false;
}

private boolean backtrack(char[][] board, String word, int r, int c, int i) {
    if (i == word.length()) return true;
    if (r < 0 || c < 0 || r >= board.length || c >= board[0].length || board[r][c] != word.charAt(i)) {
        return false;
    }

    char temp = board[r][c];
    board[r][c] = '#'; // Mark visited
    
    boolean found = backtrack(board, word, r + 1, c, i + 1) ||
                    backtrack(board, word, r - 1, c, i + 1) ||
                    backtrack(board, word, r, c + 1, i + 1) ||
                    backtrack(board, word, r, c - 1, i + 1);
    
    board[r][c] = temp; // Unmark
    return found;
}
```
---

### Q7: Palindrome Partitioning (LC 131)
**Example:** `s = "aab"` -> `[["a","a","b"],["aa","b"]]`  
**Explanation:** Partition string such that every substring is a palindrome.

**Pattern:** **For-loop Partitioning**. At each step, try all possible prefix lengths. If the prefix is a palindrome, recurse on the remaining suffix.

```java
public List<List<String>> partition(String s) {
    List<List<String>> res = new ArrayList<>();
    backtrack(0, s, new ArrayList<>(), res);
    return res;
}

private void backtrack(int start, String s, List<String> curr, List<List<String>> res) {
    if (start == s.length()) {
        res.add(new ArrayList<>(curr));
        return;
    }
    for (int end = start; end < s.length(); end++) {
        // If current prefix is palindrome, explore the suffix
        if (isPalindrome(s, start, end)) {
            curr.add(s.substring(start, end + 1));
            backtrack(end + 1, s, curr, res);
            curr.remove(curr.size() - 1); // Backtrack
        }
    }
}

private boolean isPalindrome(String s, int l, int r) {
    while (l < r) if (s.charAt(l++) != s.charAt(r--)) return false;
    return true;
}
```

---

### Q8: Letter Combinations of a Phone Number (LC 17)
**Example:** `"23"` -> `["ad","ae","af", ...]`  
**Explanation:** Return all possible letter combinations that the digits could represent.

**Pattern:** **Mapping + Layered Recursion**. Use an array to map digits to strings. Each digit represents a "layer" in the recursion tree.

```java
private String[] map = {"", "", "abc", "def", "ghi", "jkl", "mno", "pqrs", "tuv", "wxyz"};

public List<String> letterCombinations(String digits) {
    List<String> res = new ArrayList<>();
    if (digits.isEmpty()) return res;
    backtrack(0, digits, new StringBuilder(), res);
    return res;
}

private void backtrack(int i, String digits, StringBuilder sb, List<String> res) {
    if (i == digits.length()) {
        res.add(sb.toString());
        return;
    }
    String letters = map[digits.charAt(i) - '0'];
    for (char c : letters.toCharArray()) {
        sb.append(c);
        backtrack(i + 1, digits, sb, res);
        sb.setLength(sb.length() - 1); // Undo/Backtrack
    }
}
```

---

### Q9: N-Queens (LC 51)
**Example:** `n = 4` -> Two valid board arrangements.  
**Explanation:** Place $n$ queens on an $n \times n$ board such that no two queens attack each other.

**Pattern:** **Row-by-row with Diagonal Constraints**. Track used columns and two types of diagonals using sets. 
*   Positive Diagonal: `row + col` is constant.
*   Negative Diagonal: `row - col` is constant.

```java
public List<List<String>> solveNQueens(int n) {
    List<List<String>> res = new ArrayList<>();
    char[][] board = new char[n][n];
    for (char[] row : board) Arrays.fill(row, '.');
    
    backtrack(0, new HashSet<>(), new HashSet<>(), new HashSet<>(), board, res);
    return res;
}

private void backtrack(int r, Set<Integer> cols, Set<Integer> posDiag, Set<Integer> negDiag, char[][] board, List<List<String>> res) {
    if (r == board.length) {
        res.add(format(board));
        return;
    }
    for (int c = 0; c < board.length; c++) {
        // Constraint check: O(1) using Sets
        if (cols.contains(c) || posDiag.contains(r + c) || negDiag.contains(r - c)) continue;

        cols.add(c); posDiag.add(r + c); negDiag.add(r - c);
        board[r][c] = 'Q';
        
        backtrack(r + 1, cols, posDiag, negDiag, board, res);
        
        // Backtrack: Undo state
        cols.remove(c); posDiag.remove(r + c); negDiag.remove(r - c);
        board[r][c] = '.';
    }
}

private List<String> format(char[][] board) {
    List<String> res = new ArrayList<>();
    for (char[] r : board) res.add(new String(r));
    return res;
}
```

---

### Q10: Sudoku Solver (LC 37)
**Explanation:** Fill an empty $9 \times 9$ Sudoku board.
**Pattern:** **Full State Exploration**. For every empty cell, try digits 1-9. If the board becomes unsolveable, backtrack.

```java
public void solveSudoku(char[][] board) {
    solve(board);
}

private boolean solve(char[][] board) {
    for (int r = 0; r < 9; r++) {
        for (int c = 0; c < 9; c++) {
            if (board[r][c] == '.') {
                for (char val = '1'; val <= '9'; val++) {
                    if (isValid(board, r, c, val)) {
                        board[r][c] = val;
                        if (solve(board)) return true; // Success!
                        board[r][c] = '.'; // Backtrack
                    }
                }
                return false; // No digit works in this cell
            }
        }
    }
    return true; // Whole board filled
}

private boolean isValid(char[][] b, int r, int c, char val) {
    for (int i = 0; i < 9; i++) {
        if (b[r][i] == val) return false; // Row
        if (b[i][c] == val) return false; // Col
        // Box: check 3x3 square
        if (b[3 * (r / 3) + i / 3][3 * (c / 3) + i % 3] == val) return false;
    }
    return true;
}
```

---

### Q11: Restore IP Addresses (LC 93)
**Example:** `"25525511135"` -> `["255.255.11.135", ...]`  
**Explanation:** Generate all valid IP addresses by inserting dots.

**Pattern:** **Section Partitioning**. Split string into exactly 4 segments.
**Constraints:** Value $\leq 255$, no leading zeros (unless segment is "0"), and max segment length is 3.

```java
public List<String> restoreIpAddresses(String s) {
    List<String> res = new ArrayList<>();
    if (s.length() > 12) return res;
    backtrack(0, 0, "", s, res);
    return res;
}

private void backtrack(int i, int dots, String curr, String s, List<String> res) {
    if (dots == 4 && i == s.length()) {
        res.add(curr.substring(0, curr.length() - 1)); // Remove trailing dot
        return;
    }
    if (dots > 4) return;

    for (int len = 1; len <= 3 && i + len <= s.length(); len++) {
        String segment = s.substring(i, i + len);
        // Validate segment
        if (Integer.parseInt(segment) <= 255 && (segment.equals("0") || !segment.startsWith("0"))) {
            backtrack(i + len, dots + 1, curr + segment + ".", s, res);
        }
    }
}
```

---

### Q12: Combination Sum III (LC 216)
**Example:** `k = 3, n = 7` -> `[[1,2,4]]`  
**Explanation:** Find all combinations of $k$ unique numbers (1-9) that sum to $n$.

**Pattern:** **For-Loop with Fixed Count**.

```java
public List<List<Integer>> combinationSum3(int k, int n) {
    List<List<Integer>> res = new ArrayList<>();
    backtrack(1, n, k, new ArrayList<>(), res);
    return res;
}

private void backtrack(int start, int target, int k, List<Integer> curr, List<List<Integer>> res) {
    if (curr.size() == k) {
        if (target == 0) res.add(new ArrayList<>(curr));
        return;
    }
    for (int i = start; i <= 9; i++) {
        if (i > target) break; // Pruning
        curr.add(i);
        backtrack(i + 1, target - i, k, curr, res);
        curr.remove(curr.size() - 1); // Backtrack
    }
}
```
---

## 16. Intervals

### Core Theory & Patterns

1.  **Sorting is Mandatory:** Almost every interval problem starts with sorting the intervals by their **start time** $O(N \log N)$.
2.  **Overlap Logic:** Two intervals $[s1, e1]$ and $[s2, e2]$ overlap if $s2 \le e1$ (assuming $s2 \ge s1$ after sorting).
3.  **The Three Scenarios:** When comparing a new interval to an existing one:
    *   **Before:** No overlap, finishes before the current one.
    *   **Overlap:** Merge them by taking $[\min(s1, s2), \max(e1, e2)]$.
    *   **After:** No overlap, starts after the current one.
4.  **Greedy for Deletions:** If you need to remove intervals to prevent overlap, always remove the one that **ends later**. This leaves the most "room" for future intervals.
5.  **Chronological Ordering:** For "Meeting Rooms II" (simultaneous intervals), treat starts and ends as separate events on a timeline.

---

### Q1: Insert Interval (LC 57)
**Example:** `intervals = [[1,3],[6,9]], newInterval = [2,5]` -> `[[1,5],[6,9]]`  
**Explanation:** Insert a new interval into a sorted list and merge if necessary.

**Pattern:** **Three-Phase Linear Scan**. 1. Add all intervals ending before `newInterval` starts. 2. Merge all overlapping intervals. 3. Add remaining intervals.

```java
public int[][] insert(int[][] intervals, int[] newInterval) {
    List<int[]> res = new ArrayList<>();
    int i = 0, n = intervals.length;
    
    // Phase 1: Add intervals before newInterval
    while (i < n && intervals[i][1] < newInterval[0]) res.add(intervals[i++]);
    
    // Phase 2: Merge overlapping intervals
    while (i < n && intervals[i][0] <= newInterval[1]) {
        newInterval[0] = Math.min(newInterval[0], intervals[i][0]);
        newInterval[1] = Math.max(newInterval[1], intervals[i][1]);
        i++;
    }
    res.add(newInterval);
    
    // Phase 3: Add remaining
    while (i < n) res.add(intervals[i++]);
    
    return res.toArray(new int[res.size()][]);
}
```

---

### Q2: Merge Intervals (LC 56)
**Example:** `[[1,3],[2,6],[8,10]]` -> `[[1,6],[8,10]]`  
**Pattern:** **Sort + Running Merge**. Sort by start time. Compare current interval with the last one added to results.

```java
public int[][] merge(int[][] intervals) {
    if (intervals.length <= 1) return intervals;
    Arrays.sort(intervals, (a, b) -> Integer.compare(a[0], b[0]));
    
    List<int[]> res = new ArrayList<>();
    int[] curr = intervals[0];
    res.add(curr);
    
    for (int[] next : intervals) {
        if (next[0] <= curr[1]) { // Overlap
            curr[1] = Math.max(curr[1], next[1]);
        } else { // No overlap
            curr = next;
            res.add(curr);
        }
    }
    return res.toArray(new int[res.size()][]);
}
```

---

### Q3: Non-overlapping Intervals (LC 435)
**Example:** `[[1,2],[2,3],[3,4],[1,3]]` -> `1` (Remove [1,3])  
**Explanation:** Minimum removals to make all intervals non-overlapping.

**Pattern:** **Greedy Sorting by End Time**. To fit the most intervals, always pick the one that finishes first. 
**Alternative Pattern:** Sort by start time, and when an overlap occurs, "delete" the one with the larger end time.

```java
public int eraseOverlapIntervals(int[][] intervals) {
    if (intervals.length == 0) return 0;
    Arrays.sort(intervals, (a, b) -> Integer.compare(a[1], b[1]));
    
    int count = 0;
    int prevEnd = intervals[0][1];
    
    for (int i = 1; i < intervals.length; i++) {
        if (intervals[i][0] < prevEnd) {
            count++; // Overlap: remove this one
        } else {
            prevEnd = intervals[i][1]; // No overlap: update boundary
        }
    }
    return count;
}
```

---

### Q4: Meeting Rooms (LC 252)
**Explanation:** Can a person attend all meetings? (Check for any overlap).
**Pattern:** Sort by start, check if `next.start < curr.end`.

```java
public boolean canAttendMeetings(int[][] intervals) {
    Arrays.sort(intervals, (a, b) -> Integer.compare(a[0], b[0]));
    for (int i = 1; i < intervals.length; i++) {
        if (intervals[i][0] < intervals[i-1][1]) return false;
    }
    return true;
}
```

---

### Q5: Meeting Rooms II (LC 253)
**Explanation:** Minimum number of rooms required.
**Pattern:** **Chronological Ordering**. Separate start times and end times into two sorted arrays. Iterate through starts; if a meeting starts before the earliest ending one finishes, you need a new room.

```java
public int minMeetingRooms(int[][] intervals) {
    int n = intervals.length;
    int[] starts = new int[n], ends = new int[n];
    for (int i = 0; i < n; i++) {
        starts[i] = intervals[i][0];
        ends[i] = intervals[i][1];
    }
    Arrays.sort(starts);
    Arrays.sort(ends);
    
    int res = 0, count = 0, s = 0, e = 0;
    while (s < n) {
        if (starts[s] < ends[e]) {
            count++; // Need a room
            s++;
        } else {
            count--; // Room freed up
            e++;
        }
        res = Math.max(res, count);
    }
    return res;
}
```

---

### Q6: Minimum Number of Arrows to Burst Balloons (LC 452)
**Example:** `[[10,16],[2,8],[1,6],[7,12]]` -> `2`  
**Pattern:** **Intersection Greedy**. Similar to Non-overlapping intervals. Sort by end time. If the current balloon starts after the last arrow position, you need a new arrow.

```java
public int findMinArrowShots(int[][] points) {
    if (points.length == 0) return 0;
    // Use Integer.compare to avoid overflow with subtractions
    Arrays.sort(points, (a, b) -> Integer.compare(a[1], b[1]));
    
    int arrows = 1;
    int prevEnd = points[0][1];
    
    for (int i = 1; i < points.length; i++) {
        if (points[i][0] > prevEnd) {
            arrows++;
            prevEnd = points[i][1];
        }
    }
    return arrows;
}
```
---

## 17. Math & Geometry

### Core Theory & Patterns

1.  **Matrix Rotation:** To rotate a matrix by 90 degrees clockwise:
    *   **Transpose:** Swap `matrix[i][j]` with `matrix[j][i]`.
    *   **Reflect:** Reverse each row of the transposed matrix.
2.  **Spiral Traversal:** Maintain 4 boundaries (`top`, `bottom`, `left`, `right`). Move along the boundaries and shrink them after each side is completed until they cross.
3.  **Carry Logic:** Standard in big-integer arithmetic (Plus One, Multiply Strings). Track `carry = sum / 10` and `digit = sum % 10`.
4.  **Binary Exponentiation:** Calculate $x^n$ in $O(\log N)$ by halving $n$ and squaring $x$ at each step ($x^n = (x^2)^{n/2}$).
5.  **Geometric Hashing:** In coordinate problems, use a `Map` or `Set` with keys like `"x,y"` for $O(1)$ point lookups.
6.  **Cycle Detection (Math):** For sequences like Happy Number, use Floyd's Tortoise and Hare (Slow/Fast pointers) to detect if the sequence loops infinitely.

---

### Q1: Rotate Image (LC 48)
**Example:** `[[1,2],[3,4]]` -> `[[3,1],[4,2]]`  
**Pattern:** **Transpose + Reflect**. Transpose flips over the main diagonal. Reflecting (reversing rows) completes the 90-degree turn.

```java
public void rotate(int[][] matrix) {
    int n = matrix.length;
    // 1. Transpose: flip over the diagonal
    for (int i = 0; i < n; i++) {
        for (int j = i + 1; j < n; j++) {
            int temp = matrix[i][j];
            matrix[i][j] = matrix[j][i];
            matrix[j][i] = temp;
        }
    }
    // 2. Reverse each row: standard array reverse
    for (int i = 0; i < n; i++) {
        for (int j = 0; j < n / 2; j++) {
            int temp = matrix[i][j];
            matrix[i][j] = matrix[i][n - 1 - j];
            matrix[i][n - 1 - j] = temp;
        }
    }
}
```

---

### Q2: Spiral Matrix (LC 54)
**Example:** `[[1,2,3],[4,5,6],[7,8,9]]` -> `[1,2,3,6,9,8,7,4,5]`  
**Pattern:** **Boundary Shrinking**. Process top row, right col, bottom row, left col, then shrink.

```java
public List<Integer> spiralOrder(int[][] matrix) {
    List<Integer> res = new ArrayList<>();
    int left = 0, right = matrix[0].length - 1;
    int top = 0, bottom = matrix.length - 1;
    
    while (left <= right && top <= bottom) {
        for (int i = left; i <= right; i++) res.add(matrix[top][i]);
        top++;
        for (int i = top; i <= bottom; i++) res.add(matrix[i][right]);
        right--;
        
        // Re-check condition before moving back/up
        if (!(left <= right && top <= bottom)) break;
        
        for (int i = right; i >= left; i--) res.add(matrix[bottom][i]);
        bottom--;
        for (int i = bottom; i >= top; i--) res.add(matrix[i][left]);
        left++;
    }
    return res;
}
```

---

### Q3: Set Matrix Zeroes (LC 73)
**Pattern:** **First Row/Col as Flag**. Use the 0th row and 0th column to store whether that row/column should be zeroed. Use one extra variable for the first cell's ambiguity.

```java
public void setZeroes(int[][] matrix) {
    int R = matrix.length, C = matrix[0].length;
    boolean firstRow = false;
    
    for (int r = 0; r < R; r++) {
        for (int c = 0; c < C; c++) {
            if (matrix[r][c] == 0) {
                matrix[0][c] = 0; // Mark column
                if (r > 0) matrix[r][0] = 0; // Mark row
                else firstRow = true;
            }
        }
    }
    
    // Zero out sub-matrix based on flags
    for (int r = 1; r < R; r++) {
        for (int c = 1; c < C; c++) {
            if (matrix[0][c] == 0 || matrix[r][0] == 0) matrix[r][c] = 0;
        }
    }
    if (matrix[0][0] == 0) for (int r = 0; r < R; r++) matrix[r][0] = 0;
    if (firstRow) for (int c = 0; c < C; c++) matrix[0][c] = 0;
}
```

---

### Q4: Happy Number (LC 202)
**Pattern:** **Cycle Detection**. A number is happy if the sum of squares of digits eventually hits 1. If it hits a cycle, it's not. Use Tortoise & Hare logic.

```java
public boolean isHappy(int n) {
    int slow = n, fast = sumSquares(n);
    while (slow != fast) {
        slow = sumSquares(slow);
        fast = sumSquares(sumSquares(fast));
    }
    return slow == 1;
}

private int sumSquares(int n) {
    int sum = 0;
    while (n > 0) {
        int digit = n % 10;
        sum += digit * digit;
        n /= 10;
    }
    return sum;
}
```

---

### Q5: Plus One (LC 66)
**Pattern:** **Reverse Iteration**. If a digit is less than 9, increment and return. If it's 9, it becomes 0 and carry moves to the next digit.

```java
public int[] plusOne(int[] digits) {
    for (int i = digits.length - 1; i >= 0; i--) {
        if (digits[i] < 9) {
            digits[i]++;
            return digits;
        }
        digits[i] = 0;
    }
    // If loop finishes, it means we had something like [9,9,9]
    int[] res = new int[digits.length + 1];
    res[0] = 1;
    return res;
}
```

---

### Q6: Pow(x, n) (LC 50)
**Pattern:** **Binary Exponentiation**. Recursively (or iteratively) square the base while halving the exponent.

```java
public double myPow(double x, int n) {
    long N = n; // Use long to avoid Integer.MIN_VALUE overflow
    if (N < 0) { x = 1 / x; N = -N; }
    double res = 1;
    while (N > 0) {
        if (N % 2 == 1) res *= x;
        x *= x;
        N /= 2;
    }
    return res;
}
```

---

### Q7: Multiply Strings (LC 43)
**Pattern:** **Digit-by-Digit Multiplication**. Multiply $num1[i]$ and $num2[j]$ and store in result array at index $i+j+1$.

```java
public String multiply(String num1, String num2) {
    if (num1.equals("0") || num2.equals("0")) return "0";
    int m = num1.length(), n = num2.length();
    int[] res = new int[m + n];
    
    for (int i = m - 1; i >= 0; i--) {
        for (int j = n - 1; j >= 0; j--) {
            int mul = (num1.charAt(i) - '0') * (num2.charAt(j) - '0');
            int p1 = i + j, p2 = i + j + 1;
            int sum = mul + res[p2];
            
            res[p2] = sum % 10;
            res[p1] += sum / 10;
        }
    }
    StringBuilder sb = new StringBuilder();
    for (int p : res) if (!(sb.length() == 0 && p == 0)) sb.append(p);
    return sb.toString();
}
```

---

### Q8: Detect Squares (LC 2013)
**Pattern:** **Coordinate Hashing**. To find squares forming with point $(x, y)$, iterate through existing points $(x_i, y_i)$ that could be the opposite diagonal corner (check if absolute differences match).

```java
class DetectSquares {
    private Map<String, Integer> counts = new HashMap<>();
    private List<int[]> points = new ArrayList<>();

    public void add(int[] point) {
        String key = point[0] + "," + point[1];
        counts.put(key, counts.getOrDefault(key, 0) + 1);
        points.add(point);
    }

    public int count(int[] point) {
        int x1 = point[0], y1 = point[1];
        int total = 0;
        for (int[] p : points) {
            int x2 = p[0], y2 = p[1];
            // Check if point p is a valid diagonal corner
            if (Math.abs(x1 - x2) != Math.abs(y1 - y2) || x1 == x2) continue;
            // Check if other two corners exist
            total += counts.getOrDefault(x1 + "," + y2, 0) * 
                     counts.getOrDefault(x2 + "," + y1, 0);
        }
        return total;
    }
}
```
---

## 18. Bit Manipulation

### Core Theory & Patterns

1.  **XOR Properties:**
    *   `x ^ x = 0` (Identical bits cancel out).
    *   `x ^ 0 = x` (XOR with zero does nothing).
    *   **Commutative/Associative:** Order doesn't matter.
2.  **The "Rightmost 1" Trick:** `n & (n - 1)` clears the least significant bit that is set to 1. This is the most efficient way to count set bits (Kernighan's Algorithm).
3.  **The "Get LSB" Trick:** `n & 1` returns 1 if the number is odd, 0 if even.
4.  **Bit Shifting:**
    *   `<< 1`: Multiplies by 2.
    *   `>> 1`: Divides by 2 (keeps sign).
    *   `>>> 1`: Unsigned right shift (fills with 0, regardless of sign).
5.  **Two's Complement:** In Java, negative numbers are stored as `~n + 1`. The sign bit is the most significant bit.

---

### Q1: Single Number (LC 136)
**Example:** `[4,1,2,1,2]` -> `4`  
**Pattern:** **XOR Accumulator**. Since `x^x = 0`, all numbers appearing twice will cancel each other out, leaving only the unique number.

```java
public int singleNumber(int[] nums) {
    int res = 0;
    for (int n : nums) res ^= n;
    return res;
}
```

---

### Q2: Number of 1 Bits (LC 191)
**Pattern:** **Kernighan’s Algorithm**. Instead of checking all 32 bits, only iterate as many times as there are set bits using `n & (n - 1)`.

```java
public int hammingWeight(int n) {
    int count = 0;
    while (n != 0) {
        n &= (n - 1); // Removes the rightmost 1-bit
        count++;
    }
    return count;
}
```

---

### Q3: Counting Bits (LC 338)
**Example:** `n = 2` -> `[0,1,1]` (binary 0, 1, 10)  
**Pattern:** **DP + Offset**. The number of bits in $i$ is $1 + \text{bits in } (i - \text{LargestPowerOf2})$.
**Alternative Pattern:** `dp[i] = dp[i >> 1] + (i & 1)`. The bits in $i$ are the bits in $i/2$ plus 1 if $i$ is odd.

```java
public int[] countBits(int n) {
    int[] res = new int[n + 1];
    for (int i = 1; i <= n; i++) {
        // Bits in i = bits in i/2 + last bit of i
        res[i] = res[i >> 1] + (i & 1);
    }
    return res;
}
```

---

### Q4: Reverse Bits (LC 190)
**Pattern:** **Bit-by-Bit Shift**. Iterate 32 times. Shift result left, add the last bit of $n$ to the result, then shift $n$ right.

```java
public int reverseBits(int n) {
    int res = 0;
    for (int i = 0; i < 32; i++) {
        res = (res << 1) | (n & 1); // Shift result left, add n's LSB
        n >>= 1; // Move to n's next bit
    }
    return res;
}
```

---

### Q5: Missing Number (LC 268)
**Example:** `nums = [3,0,1]` -> `2` (range is 0 to 3)  
**Pattern:** **XOR Index and Value**. XOR all numbers from $0$ to $n$ and all numbers in the array. The matching pairs will cancel out, leaving the missing number.

```java
public int missingNumber(int[] nums) {
    int res = nums.length; // Start with n
    for (int i = 0; i < nums.length; i++) {
        res ^= i ^ nums[i]; // XOR index and value
    }
    return res;
}
```

---

### Q6: Sum of Two Integers (LC 371)
**Explanation:** Sum two integers without using `+` or `-`.
**Pattern:** **XOR for Sum, AND-Shift for Carry**. 
*   `a ^ b` calculates the sum without carrying.
*   `(a & b) << 1` calculates the carry.

```java
public int getSum(int a, int b) {
    while (b != 0) {
        int carry = (a & b) << 1;
        a = a ^ b; // Sum without carry
        b = carry; // Continue until no carry left
    }
    return a;
}
```

---

### Q7: Reverse Integer (LC 7)
**Example:** `-123` -> `-321`  
**Pattern:** **Overflow Check**. While popping digits using `% 10`, check if the result will exceed `Integer.MAX_VALUE / 10` before multiplying by 10.

```java
public int reverse(int x) {
    int res = 0;
    while (x != 0) {
        int pop = x % 10;
        x /= 10;
        
        // Check for positive/negative overflow
        if (res > Integer.MAX_VALUE / 10 || (res == Integer.MAX_VALUE / 10 && pop > 7)) return 0;
        if (res < Integer.MIN_VALUE / 10 || (res == Integer.MIN_VALUE / 10 && pop < -8)) return 0;
        
        res = res * 10 + pop;
    }
    return res;
}
```
