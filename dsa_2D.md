## Dynamic Programming
---

### 1D DP

The current state depends on one or more previous states (usually `i-1, i-2, or i-k`).

**The State**: `dp[i] = Answer for index i`.

**The Logic**: Usually `dp[i] = f(dp[i-1], dp[i-2], ...)`.

**Example**: Climbing Stairs, House Robber (can't pick adjacent houses).

**Senior Tip**: Look for the "Decision Point." (e.g., Should I rob this house or skip it?)

### 0/1 Knapsack

Identifying Feature: You have a fixed capacity and a list of items with weight/value. Each item can be picked exactly once.

**The State**: `dp[i][w]` where i is the item index and w is the remaining weight capacity.

**The Logic**: `max(Pick current item, Don't pick current item)`.

**If pick**: `value[i] + dp[i-1][w - weight[i]]`

**If don't**: `dp[i-1][w]`

### Unbounded Knapsack

**Identifying Feature**: Similar to 0/1 Knapsack, but you can use each item infinite times.

**The State**: `dp[w]` (you don't need the i index because you can reuse the item).

**The Logic**: You don't move to the i-1 index; you stay at i.

`dp[w] = max(dp[w], value[i] + dp[w - weight[i]])`

**Example**: Coin Change (finding number of ways), Rod Cutting.

### LIS (Longest Increasing Subsequence)

**Identifying Feature**: You are looking for a sequence (not necessarily contiguous) that follows a property (e.g., increasing).

**The State**: dp[i] = length of the longest subsequence ending at index i.

**The Logic**: For each `i`, check all `j < i`. If `arr[i] > arr[j]`, then `dp[i] = max(dp[i], dp[j] + 1)`.

### String DP

**Identifying Feature**: Problems involving comparing two strings or transforming one to another.

**The State**: `dp[i][j] = Result for string1[0...i] and string2[0...j]`.

**The Logic**:

**If characters match**: `dp[i][j] = 1 + dp[i-1][j-1]`.

**If they don't**: `min(dp[i-1][j], dp[i][j-1])` (for Deletion/Insertion).

**Example**: Longest Common Subsequence, Edit Distance (Levenshtein), Palindromic Substrings.

### 2D DP

Identifying Feature: Movement in a matrix with constraints.

**The State**: `dp[i][j] = Answer for grid position (i, j)`.

**The Logic**: `dp[i][j] = grid[i][j] + min(dp[i-1][j], dp[i][j-1])`.

