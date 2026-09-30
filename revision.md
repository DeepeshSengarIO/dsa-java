# Google SWE III (L4) DSA Revision Bible

> **Daily Revision Rule**: Review one priority block per session. In Google interviews, prioritize: **Invariants $\rightarrow$ Clean Templates $\rightarrow$ Time/Space Proof $\rightarrow$ Edge Cases**.

---

## Priority Index (Google L4 Frequency Order)

1. [P1: Graphs & Grids (BFS, DFS, Dijkstra, TopoSort, DSU, Weighted DSU, 0-1 BFS)](#p1-graphs--grids)
2. [P2: Trees, LCA & Trie](#p2-trees-lca--trie)
3. [P3: Binary Search on Values & Feasibility Predicates](#p3-binary-search--feasibility-space)
4. [P4: Dynamic Programming & State Machines](#p4-dynamic-programming--state-machines)
5. [P5: Monotonic Stack & Monotonic Deque](#p5-monotonic-stack--monotonic-deque)
6. [P6: Prefix Sum, Two Pointers & Sliding Window](#p6-prefix-sum-two-pointers--sliding-window)
7. [P7: Intervals, Sweep Line & Fenwick Trees](#p7-intervals--sweep-line)
8. [P8: Heaps, Top-K & QuickSelect](#p8-heaps-top-k--quickselect)
9. [P9: Backtracking & State-Space Pruning](#p9-backtracking--state-space-pruning)
10. [P10: Bit Manipulation & Core Math](#p10-bit-manipulation--core-math)
11. [P11: Google Warsaw L4 Edge Cases & Java Speed Cheat-Sheet](#p11-google-l4-edge-cases--java-speed-cheat-sheet)

---

## P1: Graphs & Grids

### 1.1 Multi-Source BFS (Grids / Unweighted Graphs)
- **Invariant**: All initial sources are enqueued at time $t = 0$. Mark `visited = true` **at the moment of enqueueing** (never on pop) to prevent duplicate states in queue.
- **Complexity**: $O(V + E) = O(R \times C)$ time, $O(R \times C)$ space.

```java
int[][] dirs = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

public int multiSourceBFS(int[][] grid, List<int[]> sources) {
    int m = grid.length, n = grid[0].length;
    Queue<int[]> queue = new ArrayDeque<>();
    boolean[][] visited = new boolean[m][n];

    for (int[] src : sources) {
        queue.offer(new int[]{src[0], src[1], 0}); // {r, c, dist}
        visited[src[0]][src[1]] = true;
    }

    int maxDist = 0;
    while (!queue.isEmpty()) {
        int[] curr = queue.poll();
        int r = curr[0], c = curr[1], d = curr[2];
        maxDist = Math.max(maxDist, d);

        for (int[] dir : dirs) {
            int nr = r + dir[0], nc = c + dir[1];
            if (nr >= 0 && nr < m && nc >= 0 && nc < n && !visited[nr][nc] && grid[nr][nc] == 0) {
                visited[nr][nc] = true; // Mark BEFORE enqueue
                queue.offer(new int[]{nr, nc, d + 1});
            }
        }
    }
    return maxDist;
}
```

---

### 1.2 0-1 BFS (Shortest Path with Edge Weights 0 or 1)
- **Key Idea**: Use a `Deque`. If edge weight is $0$, `offerFirst()`. If $1$, `offerLast()`.
- **Complexity**: $O(V + E)$ strictly linear, strictly beats Dijkstra $O(E \log V)$.

```java
public int zeroOneBFS(int n, List<int[]>[] adj, int src, int dest) {
    int[] dist = new int[n];
    Arrays.fill(dist, Integer.MAX_VALUE);
    Deque<Integer> deque = new ArrayDeque<>();

    dist[src] = 0;
    deque.offerFirst(src);

    while (!deque.isEmpty()) {
        int u = deque.pollFirst();
        if (u == dest) return dist[dest];

        for (int[] edge : adj[u]) {
            int v = edge[0], w = edge[1]; // w is 0 or 1
            if (dist[u] + w < dist[v]) {
                dist[v] = dist[u] + w;
                if (w == 0) deque.offerFirst(v);
                else deque.offerLast(v);
            }
        }
    }
    return dist[dest] == Integer.MAX_VALUE ? -1 : dist[dest];
}
```

---

### 1.3 Dijkstra’s Algorithm (Non-Negative Weighted Shortest Path)
- **Crucial L4 Detail**: Skip stale heap entries via `if (d > dist[u]) continue;`.
- **Complexity**: $O(E \log V)$ time, $O(V)$ space.

```java
public int[] dijkstra(int n, List<int[]>[] adj, int src) {
    int[] dist = new int[n];
    Arrays.fill(dist, Integer.MAX_VALUE);
    PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[1]));

    dist[src] = 0;
    pq.offer(new int[]{src, 0});

    while (!pq.isEmpty()) {
        int[] curr = pq.poll();
        int u = curr[0], d = curr[1];
        if (d > dist[u]) continue; // Skip stale records!

        for (int[] edge : adj[u]) {
            int v = edge[0], weight = edge[1];
            if (dist[u] + weight < dist[v]) {
                dist[v] = dist[u] + weight;
                pq.offer(new int[]{v, dist[v]});
            }
        }
    }
    return dist;
}
```

---

### 1.4 Topological Sort & Cycle Detection

#### A. Kahn’s Algorithm (BFS with In-Degrees)
- **Cycle Invariant**: If `processedCount != n`, a directed cycle exists.

```java
public int[] topoSortKahn(int n, List<Integer>[] adj) {
    int[] inDegree = new int[n];
    for (int u = 0; u < n; u++) {
        for (int v : adj[u]) inDegree[v]++;
    }

    Queue<Integer> q = new ArrayDeque<>();
    for (int i = 0; i < n; i++) {
        if (inDegree[i] == 0) q.offer(i);
    }

    int[] order = new int[n];
    int idx = 0;
    while (!q.isEmpty()) {
        int u = q.poll();
        order[idx++] = u;
        for (int v : adj[u]) {
            if (--inDegree[v] == 0) q.offer(v);
        }
    }
    return idx == n ? order : new int[0]; // Cycle if idx != n
}
```

#### B. Directed Cycle Detection via 3-State Coloring (DFS)
- `state[u] = 0` (unvisited), `1` (visiting / in recursion stack), `2` (visited / settled).
- If neighbor has `state[v] == 1`, a **back-edge** exists $\rightarrow$ Cycle found!

```java
public boolean hasCycleDirected(int n, List<Integer>[] adj) {
    int[] state = new int[n];
    for (int i = 0; i < n; i++) {
        if (state[i] == 0 && dfs(i, adj, state)) return true;
    }
    return false;
}

private boolean dfs(int u, List<Integer>[] adj, int[] state) {
    state[u] = 1; // In recursion stack
    for (int v : adj[u]) {
        if (state[v] == 1) return true; // Cycle
        if (state[v] == 0 && dfs(v, adj, state)) return true;
    }
    state[u] = 2; // Settled
    return false;
}
```

---

### 1.5 Disjoint Set Union (DSU / Union-Find)
- **Path Compression + Union by Size/Rank**: $\approx O(\alpha(N))$ nearly $O(1)$.
- Used for: Dynamic connectivity, Kruskal's MST, detecting cycles in undirected graphs.

```java
static class DSU {
    int[] parent, size;
    int components;

    DSU(int n) {
        parent = new int[n];
        size = new int[n];
        components = n;
        for (int i = 0; i < n; i++) {
            parent[i] = i;
            size[i] = 1;
        }
    }

    int find(int i) {
        if (parent[i] != i) {
            parent[i] = find(parent[i]); // Path compression
        }
        return parent[i];
    }

    boolean union(int i, int j) {
        int rootI = find(i), rootJ = find(j);
        if (rootI == rootJ) return false; // Cycle / already connected

        if (size[rootI] < size[rootJ]) {
            parent[rootI] = rootJ;
            size[rootJ] += size[rootI];
        } else {
            parent[rootJ] = rootI;
            size[rootI] += size[rootJ];
        }
        components--;
        return true;
    }
}
```

---

### 1.6 Bipartite Graph Verification (2-Coloring)
- Graph is bipartite $\iff$ No odd-length cycle.

```java
public boolean isBipartite(int[][] graph) {
    int n = graph.length;
    int[] color = new int[n]; // 0: uncolored, 1: red, -1: blue

    for (int i = 0; i < n; i++) {
        if (color[i] != 0) continue;
        Queue<Integer> q = new ArrayDeque<>();
        q.offer(i);
        color[i] = 1;

        while (!q.isEmpty()) {
            int u = q.poll();
            for (int v : graph[u]) {
                if (color[v] == 0) {
                    color[v] = -color[u];
                    q.offer(v);
                } else if (color[v] == color[u]) {
                    return false; // Adjacent same color -> not bipartite
                }
            }
        }
    }
    return true;
}
```

---

### 1.7 Weighted Disjoint Set Union (Evaluate Division / Relative Equations)
- **Problem Archetype**: LC 399 (Evaluate Division). Variables connected by ratios $a / b = v$.
- **Path Compression Invariant**: When compressing $u \to \text{root}$, multiply weight: $\text{weight}[u] = \text{weight}[u] \times \text{weight}[\text{parent}[u]]$.

```java
static class WeightedDSU {
    Map<String, String> parent = new HashMap<>();
    Map<String, Double> weight = new HashMap<>(); // weight[x] = x / parent[x]

    public void add(String x) {
        if (!parent.containsKey(x)) {
            parent.put(x, x);
            weight.put(x, 1.0);
        }
    }

    public String find(String x) {
        if (!parent.get(x).equals(x)) {
            String origParent = parent.get(x);
            parent.put(x, find(origParent)); // Path compression
            weight.put(x, weight.get(x) * weight.get(origParent)); // Chain ratio
        }
        return parent.get(x);
    }

    public void union(String a, String b, double value) { // a / b = value
        add(a); add(b);
        String rootA = find(a), rootB = find(b);
        if (!rootA.equals(rootB)) {
            parent.put(rootA, rootB);
            // rootA / rootB = (a / rootB) / (a / rootA) = (value * weight[b]) / weight[a]
            weight.put(rootA, value * weight.get(b) / weight.get(a));
        }
    }

    public double query(String a, String b) {
        if (!parent.containsKey(a) || !parent.containsKey(b)) return -1.0;
        String rootA = find(a), rootB = find(b);
        if (!rootA.equals(rootB)) return -1.0;
        return weight.get(a) / weight.get(b);
    }
}
```

---

## P2: Trees, LCA & Trie

### 2.1 Lowest Common Ancestor (LCA)

#### A. Binary Tree (General)
```java
public TreeNode lowestCommonAncestor(TreeNode root, TreeNode p, TreeNode q) {
    if (root == null || root == p || root == q) return root;
    TreeNode left = lowestCommonAncestor(root.left, p, q);
    TreeNode right = lowestCommonAncestor(root.right, p, q);

    if (left != null && right != null) return root; // Split point
    return left != null ? left : right;
}
```

#### B. Binary Search Tree (BST) — $O(H)$ Iterative
```java
public TreeNode lowestCommonAncestorBST(TreeNode root, TreeNode p, TreeNode q) {
    TreeNode curr = root;
    while (curr != null) {
        if (p.val < curr.val && q.val < curr.val) curr = curr.left;
        else if (p.val > curr.val && q.val > curr.val) curr = curr.right;
        else return curr;
    }
    return null;
}
```

---

### 2.2 Tree DP: Path Sum & Diameter Pattern
- **Template Core**: Function returns the **max single-branch path** extending to parent, while updating a global tracker with `left + right + root.val`.

```java
int maxPath = Integer.MIN_VALUE;

public int maxPathSum(TreeNode root) {
    gainFromSubtree(root);
    return maxPath;
}

private int gainFromSubtree(TreeNode root) {
    if (root == null) return 0;
    // Discard negative paths
    int leftGain = Math.max(0, gainFromSubtree(root.left));
    int rightGain = Math.max(0, gainFromSubtree(root.right));

    // Path with current node as highest apex
    maxPath = Math.max(maxPath, leftGain + rightGain + root.val);

    // Return maximum single branch extending upward to parent
    return root.val + Math.max(leftGain, rightGain);
}
```

---

### 2.3 Binary Tree Serialization & Deserialization
- Pre-order traversal with delimiter `","` and null symbol `"#"`.

```java
public class Codec {
    public String serialize(TreeNode root) {
        StringBuilder sb = new StringBuilder();
        buildString(root, sb);
        return sb.toString();
    }
    private void buildString(TreeNode root, StringBuilder sb) {
        if (root == null) { sb.append("#,"); return; }
        sb.append(root.val).append(",");
        buildString(root.left, sb);
        buildString(root.right, sb);
    }

    public TreeNode deserialize(String data) {
        Queue<String> nodes = new LinkedList<>(Arrays.asList(data.split(",")));
        return buildTree(nodes);
    }
    private TreeNode buildTree(Queue<String> nodes) {
        String val = nodes.poll();
        if (val == null || val.equals("#")) return null;
        TreeNode node = new TreeNode(Integer.parseInt(val));
        node.left = buildTree(nodes);
        node.right = buildTree(nodes);
        return node;
    }
}
```

---

### 2.4 Prefix Tree (Trie)
- Essential for prefix search, auto-complete, word game boards.

```java
static class TrieNode {
    TrieNode[] children = new TrieNode[26];
    boolean isEnd = false;
}

static class Trie {
    TrieNode root = new TrieNode();

    public void insert(String word) {
        TrieNode curr = root;
        for (int i = 0; i < word.length(); i++) {
            int idx = word.charAt(i) - 'a';
            if (curr.children[idx] == null) curr.children[idx] = new TrieNode();
            curr = curr.children[idx];
        }
        curr.isEnd = true;
    }

    public boolean search(String word) {
        TrieNode node = find(word);
        return node != null && node.isEnd;
    }

    public boolean startsWith(String prefix) {
        return find(prefix) != null;
    }

    private TrieNode find(String s) {
        TrieNode curr = root;
        for (int i = 0; i < s.length(); i++) {
            int idx = s.charAt(i) - 'a';
            if (curr.children[idx] == null) return null;
            curr = curr.children[idx];
        }
        return curr;
    }
}
```

---

## P3: Binary Search & Feasibility Space

### 3.1 The Universal Binary Search Templates

#### Pattern A: Minimization (`F F F T T T`) — Find First `True`
- Example: Capacity to ship packages, Koko eating bananas, First Bad Version.
```java
int l = low, r = high;
while (l < r) {
    int mid = l + (r - l) / 2; // Left-biased
    if (feasible(mid)) {
        r = mid;     // mid is feasible, try smaller
    } else {
        l = mid + 1; // mid is impossible
    }
}
return l; // l == r
```

#### Pattern B: Maximization (`T T T F F F`) — Find Last `True`
- Example: Max distance to gas stations, allocate minimum pages, Aggressive Cows.
```java
int l = low, r = high;
while (l < r) {
    int mid = l + (r - l + 1) / 2; // Right-biased to prevent infinite loop on 2 elements
    if (feasible(mid)) {
        l = mid;     // mid is feasible, try larger
    } else {
        r = mid - 1; // mid is impossible
    }
}
return l; // l == r
```

---

### 3.2 Lower Bound & Upper Bound (Exact Indexing)
- `lowerBound`: First index where `nums[i] >= target` (equivalent to C++ `std::lower_bound`).
- `upperBound`: First index where `nums[i] > target` (equivalent to C++ `std::upper_bound`).

```java
public int lowerBound(int[] nums, int target) {
    int l = 0, r = nums.length; // Range [0, n]
    while (l < r) {
        int mid = l + (r - l) / 2;
        if (nums[mid] >= target) r = mid;
        else l = mid + 1;
    }
    return l;
}

public int upperBound(int[] nums, int target) {
    int l = 0, r = nums.length;
    while (l < r) {
        int mid = l + (r - l) / 2;
        if (nums[mid] > target) r = mid;
        else l = mid + 1;
    }
    return l;
}
// Count occurrences of target: upperBound(nums, x) - lowerBound(nums, x)
```

---

### 3.3 Search in Rotated Sorted Array
- **Decision Rule**: One half is ALWAYS strictly ordered. Determine which half is sorted, then check if target is inside the sorted bounds.

```java
public int searchRotated(int[] nums, int target) {
    int l = 0, r = nums.length - 1;
    while (l <= r) {
        int mid = l + (r - l) / 2;
        if (nums[mid] == target) return mid;

        if (nums[l] <= nums[mid]) { // Left half is sorted
            if (target >= nums[l] && target < nums[mid]) r = mid - 1;
            else l = mid + 1;
        } else { // Right half is sorted
            if (target > nums[mid] && target <= nums[r]) l = mid + 1;
            else r = mid - 1;
        }
    }
    return -1;
}

// Variation with DUPLICATES (worst case O(N)):
// if (nums[l] == nums[mid] && nums[mid] == nums[r]) { l++; r--; }
```

---

## P4: Dynamic Programming & State Machines

### 4.1 Knapsack Patterns

#### A. 0/1 Knapsack (Pick item at most once)
- **Rule**: Inner weight loop **MUST run backward** to prevent using same item multiple times.
```java
int[] dp = new int[W + 1];
for (int i = 0; i < n; i++) {
    for (int w = W; w >= weight[i]; w--) {
        dp[w] = Math.max(dp[w], dp[w - weight[i]] + value[i]);
    }
}
```

#### B. Unbounded Knapsack / Coin Change (Unlimited item use)
- **Rule**: Inner weight loop **MUST run forward**.
```java
int[] dp = new int[amount + 1];
Arrays.fill(dp, amount + 1);
dp[0] = 0;
for (int coin : coins) {
    for (int w = coin; w <= amount; w++) {
        dp[w] = Math.min(dp[w], dp[w - coin] + 1);
    }
}
return dp[amount] > amount ? -1 : dp[amount];
```

---

### 4.2 Longest Increasing Subsequence (LIS) in $O(N \log N)$
- **Patience Sorting**: `tails[i]` stores the smallest tail of all increasing subsequences of length `i + 1`.

```java
public int lengthOfLIS(int[] nums) {
    int[] tails = new int[nums.length];
    int len = 0;

    for (int x : nums) {
        int l = 0, r = len;
        while (l < r) {
            int mid = l + (r - l) / 2;
            if (tails[mid] >= x) r = mid;
            else l = mid + 1;
        }
        tails[l] = x;
        if (l == len) len++;
    }
    return len;
}
```

---

### 4.3 String DP: Edit Distance & LCS

#### Edit Distance (Levenshtein)
- Transitions:
  - Match: `dp[i-1][j-1]`
  - Insert: `dp[i][j-1] + 1`
  - Delete: `dp[i-1][j] + 1`
  - Replace: `dp[i-1][j-1] + 1`

```java
public int minDistance(String word1, String word2) {
    int m = word1.length(), n = word2.length();
    int[][] dp = new int[m + 1][n + 1];

    for (int i = 0; i <= m; i++) dp[i][0] = i;
    for (int j = 0; j <= n; j++) dp[0][j] = j;

    for (int i = 1; i <= m; i++) {
        for (int j = 1; j <= n; j++) {
            if (word1.charAt(i - 1) == word2.charAt(j - 1)) {
                dp[i][j] = dp[i - 1][j - 1];
            } else {
                dp[i][j] = 1 + Math.min(dp[i - 1][j - 1], // replace
                               Math.min(dp[i - 1][j],    // delete
                                        dp[i][j - 1]));  // insert
            }
        }
    }
    return dp[m][n];
}
```

---

### 4.4 State Machine DP (Stock Trading with Cooldown)
- **States**: `held` (holding stock), `sold` (sold today, cooling down next), `rest` (can buy).

```java
public int maxProfitCooldown(int[] prices) {
    int held = -prices[0], sold = 0, rest = 0;
    for (int i = 1; i < prices.length; i++) {
        int prevSold = sold;
        sold = held + prices[i];
        held = Math.max(held, rest - prices[i]);
        rest = Math.max(rest, prevSold);
    }
    return Math.max(sold, rest);
}
```

---

### 4.5 Interval DP Template (Burst Balloons / Matrix Chain)
- **Loop by interval length `len`** from 2 to $N$.

```java
int[][] dp = new int[n][n];
for (int len = 1; len <= n; len++) {
    for (int i = 0; i <= n - len; i++) {
        int j = i + len - 1;
        for (int k = i; k <= j; k++) {
            int left = (k == i) ? 0 : dp[i][k - 1];
            int right = (k == j) ? 0 : dp[k + 1][j];
            dp[i][j] = Math.max(dp[i][j], left + right + cost(i, k, j));
        }
    }
}
```

---

## P5: Monotonic Stack & Monotonic Deque

### 5.1 Next Greater Element (Template)
- **Monotonic Decreasing Stack** (stores indices): Pop all elements smaller than current incoming element.

```java
public int[] nextGreaterElements(int[] nums) {
    int n = nums.length;
    int[] res = new int[n];
    Arrays.fill(res, -1);
    Deque<Integer> stack = new ArrayDeque<>(); // Indices

    for (int i = 0; i < n; i++) {
        while (!stack.isEmpty() && nums[stack.peek()] < nums[i]) {
            res[stack.pop()] = nums[i];
        }
        stack.push(i);
    }
    return res;
}
```

---

### 5.2 Largest Rectangle in Histogram ($O(N)$)
- **Sentinel Trick**: Iterate to `n` with height $0$ to automatically flush remaining elements from stack.

```java
public int largestRectangleArea(int[] heights) {
    int n = heights.length;
    Deque<Integer> stack = new ArrayDeque<>();
    int maxArea = 0;

    for (int i = 0; i <= n; i++) {
        int h = (i == n) ? 0 : heights[i];
        while (!stack.isEmpty() && h < heights[stack.peek()]) {
            int height = heights[stack.pop()];
            int width = stack.isEmpty() ? i : i - stack.peek() - 1;
            maxArea = Math.max(maxArea, height * width);
        }
        stack.push(i);
    }
    return maxArea;
}
```

---

### 5.3 Sliding Window Maximum ($O(N)$ Monotonic Deque)
- **Deque Invariant**: Elements in deque are indices with strictly decreasing values: `nums[deque[0]] > nums[deque[1]] > ...`

```java
public int[] maxSlidingWindow(int[] nums, int k) {
    int n = nums.length;
    int[] res = new int[n - k + 1];
    Deque<Integer> deque = new ArrayDeque<>();

    for (int i = 0; i < n; i++) {
        // 1. Evict elements outside sliding window
        if (!deque.isEmpty() && deque.peekFirst() < i - k + 1) {
            deque.pollFirst();
        }
        // 2. Maintain monotonic decreasing order
        while (!deque.isEmpty() && nums[deque.peekLast()] <= nums[i]) {
            deque.pollLast();
        }
        deque.offerLast(i);

        // 3. Deque head is max for window ending at i
        if (i >= k - 1) {
            res[i - k + 1] = nums[deque.peekFirst()];
        }
    }
    return res;
}
```

---

## P6: Prefix Sum, Two Pointers & Sliding Window

### 6.1 Dynamic Sliding Window (Universal Structure)
- **Pattern**: Expand `r` unconditionally, shrink `l` until window is valid, record answer.

```java
public int longestValidWindow(String s) {
    int l = 0, maxLen = 0;
    int[] count = new int[128];

    for (int r = 0; r < s.length(); r++) {
        char inChar = s.charAt(r);
        count[inChar]++;

        // Window invalid condition: shrink from left
        while (/* window is invalid, e.g. count[inChar] > 1 */) {
            char outChar = s.charAt(l);
            count[outChar]--;
            l++;
        }
        maxLen = Math.max(maxLen, r - l + 1);
    }
    return maxLen;
}
```

---

### 6.2 The "Exact $K$" Subarray Trick
- **Identity**: $\text{count}(\text{exact } K) = \text{atMost}(K) - \text{atMost}(K - 1)$.
- Solves: Subarrays with $K$ distinct elements, Binary subarrays with sum $K$.

```java
public int subarraysWithExactK(int[] nums, int k) {
    return atMostK(nums, k) - atMostK(nums, k - 1);
}

private int atMostK(int[] nums, int k) {
    if (k < 0) return 0;
    int l = 0, count = 0, distinct = 0;
    Map<Integer, Integer> freq = new HashMap<>();

    for (int r = 0; r < nums.length; r++) {
        if (freq.merge(nums[r], 1, Integer::sum) == 1) distinct++;

        while (distinct > k) {
            freq.put(nums[l], freq.get(nums[l]) - 1);
            if (freq.get(nums[l]) == 0) distinct--;
            l++;
        }
        count += (r - l + 1); // Number of valid subarrays ending at index r
    }
    return count;
}
```

---

### 6.3 3-Sum (Duplicate Skipping Pointers)
- Sort first. For each $i$, run two pointers `l = i + 1`, `r = n - 1`. Always skip identical adjacent values.

```java
public List<List<Integer>> threeSum(int[] nums) {
    Arrays.sort(nums);
    List<List<Integer>> res = new ArrayList<>();
    int n = nums.length;

    for (int i = 0; i < n - 2; i++) {
        if (nums[i] > 0) break; // Cannot sum to 0
        if (i > 0 && nums[i] == nums[i - 1]) continue; // Skip dup i

        int l = i + 1, r = n - 1;
        while (l < r) {
            int sum = nums[i] + nums[l] + nums[r];
            if (sum == 0) {
                res.add(Arrays.asList(nums[i], nums[l], nums[r]));
                while (l < r && nums[l] == nums[l + 1]) l++; // Skip dup l
                while (l < r && nums[r] == nums[r - 1]) r--; // Skip dup r
                l++; r--;
            } else if (sum < 0) {
                l++;
            } else {
                r--;
            }
        }
    }
    return res;
}
```

---

### 6.4 1D Prefix Sum + HashMap Complement (Subarray Sum Equals K)
- **Invariant**: A subarray $nums[i \dots j]$ sums to $k \iff \text{prefixSum}[j] - \text{prefixSum}[i - 1] = k \iff \text{prefixSum}[i - 1] = \text{prefixSum}[j] - k$.
- **Base Case**: Always initialize `map.put(0, 1)` to capture valid subarrays starting at index $0$.
- **Complexity**: $O(N)$ time, $O(N)$ space. Works with negative numbers where sliding window fails.

```java
public int subarraySum(int[] nums, int k) {
    Map<Integer, Integer> prefixFreq = new HashMap<>();
    prefixFreq.put(0, 1); // 1 empty prefix before index 0
    int sum = 0, count = 0;

    for (int num : nums) {
        sum += num;
        if (prefixFreq.containsKey(sum - k)) {
            count += prefixFreq.get(sum - k);
        }
        prefixFreq.merge(sum, 1, Integer::sum);
    }
    return count;
}
```

---

### 6.5 2D Prefix Sum Formula (Matrix Range Query in $O(1)$)
- **Precomputation**: $\text{dp}[r+1][c+1] = \text{mat}[r][c] + \text{dp}[r][c+1] + \text{dp}[r+1][c] - \text{dp}[r][c]$.
- **Query $(r1, c1) \to (r2, c2)$**: $\text{dp}[r2+1][c2+1] - \text{dp}[r1][c2+1] - \text{dp}[r2+1][c1] + \text{dp}[r1][c1]$.

```java
class MatrixBlockSum {
    int[][] dp;

    public MatrixBlockSum(int[][] matrix) {
        int m = matrix.length, n = matrix[0].length;
        dp = new int[m + 1][n + 1];
        for (int r = 0; r < m; r++) {
            for (int c = 0; c < n; c++) {
                dp[r + 1][c + 1] = matrix[r][c] + dp[r][c + 1] + dp[r + 1][c] - dp[r][c];
            }
        }
    }

    public int sumRegion(int r1, int c1, int r2, int c2) {
        return dp[r2 + 1][c2 + 1] - dp[r1][c2 + 1] - dp[r2 + 1][c1] + dp[r1][c1];
    }
}
```

---

### 6.6 Difference Array (1D Range Updates in $O(1)$)
- **Idea**: To apply $k$ range updates $[l, r]$ by adding $val$: mark `diff[l] += val` and `diff[r + 1] -= val`.
- **Reconstruction**: Prefix sum of `diff` yields final values in $O(N)$.

```java
public int[] rangeAddition(int length, int[][] updates) {
    int[] diff = new int[length + 1];
    for (int[] u : updates) {
        int l = u[0], r = u[1], val = u[2];
        diff[l] += val;
        diff[r + 1] -= val;
    }

    int[] res = new int[length];
    int running = 0;
    for (int i = 0; i < length; i++) {
        running += diff[i];
        res[i] = running;
    }
    return res;
}
```

---

## P7: Intervals & Sweep Line

### 7.1 Merge Intervals
- Sort by start time. Merge greedily if `curr.start <= prev.end`.

```java
public int[][] merge(int[][] intervals) {
    Arrays.sort(intervals, Comparator.comparingInt(a -> a[0]));
    List<int[]> merged = new ArrayList<>();
    int[] curr = intervals[0];

    for (int i = 1; i < intervals.length; i++) {
        if (intervals[i][0] <= curr[1]) {
            curr[1] = Math.max(curr[1], intervals[i][1]); // Extend end
        } else {
            merged.add(curr);
            curr = intervals[i];
        }
    }
    merged.add(curr);
    return merged.toArray(new int[merged.size()][]);
}
```

---

### 7.2 Meeting Rooms II / Sweep Line Algorithm
- **Event Representation**: `start -> +1`, `end -> -1`.
- **Tie-Breaking Rule**: If $t_{\text{end}} == t_{\text{start}}$, process `end (-1)` before `start (+1)` if interval boundaries don't conflict (e.g. $[1, 2]$ and $[2, 3]$ use 1 room).

```java
public int minMeetingRooms(int[][] intervals) {
    List<int[]> events = new ArrayList<>();
    for (int[] inv : intervals) {
        events.add(new int[]{inv[0], 1});   // Start
        events.add(new int[]{inv[1], -1});  // End
    }

    // Sort by time; break ties by putting -1 (end) before +1 (start)
    events.sort((a, b) -> a[0] != b[0] ? Integer.compare(a[0], b[0]) : Integer.compare(a[1], b[1]));

    int maxRooms = 0, rooms = 0;
    for (int[] ev : events) {
        rooms += ev[1];
        maxRooms = Math.max(maxRooms, rooms);
    }
    return maxRooms;
}
```

---

### 7.3 Fenwick Tree / Binary Indexed Tree (Dynamic Range Sums & Point Updates)
- **Google L4 Follow-up Weapon**: When asked *"What if elements change dynamically and you need range sum in $O(\log N)$ instead of $O(N)$?"*
- **1-based indexing invariant**: Low bit isolated via `i & (-i)`. Point update climbs `i += i & (-i)`. Prefix query descends `i -= i & (-i)`.

```java
static class FenwickTree {
    int[] tree;
    int n;

    public FenwickTree(int n) {
        this.n = n;
        this.tree = new int[n + 1]; // 1-based indexing
    }

    public void update(int i, int delta) { // 1-based index
        for (; i <= n; i += i & (-i)) {
            tree[i] += delta;
        }
    }

    public int query(int i) { // Prefix sum from 1 to i
        int sum = 0;
        for (; i > 0; i -= i & (-i)) {
            sum += tree[i];
        }
        return sum;
    }

    public int rangeSum(int l, int r) { // Inclusive range [l, r]
        return query(r) - query(l - 1);
    }
}
```

---

## P8: Heaps, Top-K & QuickSelect

### 8.1 Continuous Median from Data Stream (Two Heaps)
- **Max-Heap** `small` stores smaller half.
- **Min-Heap** `large` stores larger half.
- **Balance Invariant**: `small.size() == large.size()` or `small.size() == large.size() + 1`.

```java
class MedianFinder {
    PriorityQueue<Integer> small = new PriorityQueue<>(Collections.reverseOrder()); // Max-heap
    PriorityQueue<Integer> large = new PriorityQueue<>(); // Min-heap

    public void addNum(int num) {
        small.offer(num);
        large.offer(small.poll()); // Pass through small to large

        if (large.size() > small.size()) { // Rebalance size
            small.offer(large.poll());
        }
    }

    public double findMedian() {
        if (small.size() > large.size()) return small.peek();
        return (small.peek() + large.peek()) / 2.0;
    }
}
```

---

### 8.2 QuickSelect ($O(N)$ Average, $O(1)$ Extra Space)
- Finds $k$-th smallest element (0-indexed).

```java
public int quickSelect(int[] nums, int k) {
    int l = 0, r = nums.length - 1;
    Random rand = new Random();

    while (l <= r) {
        int pivotIdx = l + rand.nextInt(r - l + 1);
        int p = partition(nums, l, r, pivotIdx);
        if (p == k) return nums[p];
        else if (p < k) l = p + 1;
        else r = p - 1;
    }
    return -1;
}

private int partition(int[] nums, int l, int r, int pivotIdx) {
    int pivot = nums[pivotIdx];
    swap(nums, pivotIdx, r); // Move pivot to end
    int store = l;
    for (int i = l; i < r; i++) {
        if (nums[i] < pivot) {
            swap(nums, store++, i);
        }
    }
    swap(nums, store, r); // Put pivot at final sorted position
    return store;
}

private void swap(int[] arr, int i, int j) {
    int t = arr[i]; arr[i] = arr[j]; arr[j] = t;
}
```

---

## P9: Backtracking & State-Space Pruning

### 9.1 Combinations / Subsets with Duplicate Pruning
- **Deduplication Rule**: Array MUST be sorted. Skip duplicate elements at the **same recursion level** (`i > start && nums[i] == nums[i-1]`).

```java
public List<List<Integer>> subsetsWithDup(int[] nums) {
    Arrays.sort(nums); // Mandatory
    List<List<Integer>> res = new ArrayList<>();
    backtrack(nums, 0, new ArrayList<>(), res);
    return res;
}

private void backtrack(int[] nums, int start, List<Integer> curr, List<List<Integer>> res) {
    res.add(new ArrayList<>(curr));

    for (int i = start; i < nums.length; i++) {
        if (i > start && nums[i] == nums[i - 1]) continue; // Prune duplicate branches
        curr.add(nums[i]);
        backtrack(nums, i + 1, curr, res);
        curr.remove(curr.size() - 1); // Undo choice
    }
}
```

---

### 9.2 Permutations with Duplicates
```java
public List<List<Integer>> permuteUnique(int[] nums) {
    Arrays.sort(nums);
    List<List<Integer>> res = new ArrayList<>();
    backtrack(nums, new boolean[nums.length], new ArrayList<>(), res);
    return res;
}

private void backtrack(int[] nums, boolean[] used, List<Integer> curr, List<List<Integer>> res) {
    if (curr.size() == nums.length) {
        res.add(new ArrayList<>(curr));
        return;
    }
    for (int i = 0; i < nums.length; i++) {
        if (used[i]) continue;
        // Skip duplicate if previous identical was NOT chosen in current sequence
        if (i > 0 && nums[i] == nums[i - 1] && !used[i - 1]) continue;

        used[i] = true;
        curr.add(nums[i]);
        backtrack(nums, used, curr, res);
        curr.remove(curr.size() - 1);
        used[i] = false;
    }
}
```

---

### 9.3 In-Place Grid Backtracking (Word Search)
```java
public boolean exist(char[][] board, String word) {
    int m = board.length, n = board[0].length;
    for (int r = 0; r < m; r++) {
        for (int c = 0; c < n; c++) {
            if (dfs(board, word, 0, r, c)) return true;
        }
    }
    return false;
}

private boolean dfs(char[][] b, String w, int idx, int r, int c) {
    if (idx == w.length()) return true;
    if (r < 0 || r >= b.length || c < 0 || c >= b[0].length || b[r][c] != w.charAt(idx)) return false;

    char temp = b[r][c];
    b[r][c] = '#'; // Mark visited in-place

    boolean found = dfs(b, w, idx + 1, r + 1, c) ||
                    dfs(b, w, idx + 1, r - 1, c) ||
                    dfs(b, w, idx + 1, r, c + 1) ||
                    dfs(b, w, idx + 1, r, c - 1);

    b[r][c] = temp; // Backtrack restore
    return found;
}
```

---

## P10: Bit Manipulation & Core Math

### 10.1 Bitwise Operations Matrix
| Operation | Expression | Purpose |
|---|---|---|
| Set bit $i$ | `mask \| (1 << i)` | Mark index $i$ as present |
| Clear bit $i$ | `mask & ~(1 << i)` | Remove index $i$ |
| Toggle bit $i$ | `mask ^ (1 << i)` | Flip status of index $i$ |
| Check bit $i$ | `(mask & (1 << i)) != 0` | Query index $i$ |
| Isolate lowest set bit | `mask & (-mask)` | Extract lowest power of 2 |
| Clear lowest set bit | `mask & (mask - 1)` | Used in Brian Kernighan bit count |
| Check power of 2 | `n > 0 && (n & (n - 1)) == 0` | Single set bit verification |

### 10.2 Iterate All Submasks of Mask
- Iterates strictly over submasks without checking all $2^N$ integers.
```java
for (int sub = mask; sub > 0; sub = (sub - 1) & mask) {
    // sub is a valid non-empty subset of mask
}
```

---

### 10.3 GCD & Reservoir Sampling

#### Greatest Common Divisor (Euclidean)
```java
int gcd(int a, int b) {
    return b == 0 ? a : gcd(b, a % b);
}
// LCM(a, b) = ((long) a * b) / gcd(a, b)
```

#### Reservoir Sampling (Streaming with $1/i$ probability)
- Select $1$ item from dynamic infinite stream with uniform random distribution using $O(1)$ memory.
```java
int reservoir = -1, count = 0;
Random rand = new Random();
for (int item : stream) {
    count++;
    if (rand.nextInt(count) == 0) { // probability 1 / count
        reservoir = item;
    }
}
```

---

## P11: Google L4 Edge Cases & Java Speed Cheat-Sheet

### 11.1 Fast Java Standard Library Constructs

#### Comparators (Prevent Integer Overflow Bug!)
```java
// WRONG: can overflow if b[0] is negative and a[0] is positive
PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> a[0] - b[0]);

// CORRECT: safe against integer overflow
PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[0]));
// Or:
PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> Integer.compare(a[0], b[0]));
```

#### Fast Stack / Queue Initialization
```java
Deque<Integer> stack = new ArrayDeque<>(); // Much faster than java.util.Stack
Queue<int[]> queue = new ArrayDeque<>();
```

#### Map Idioms
```java
// Frequency increment
map.merge(key, 1, Integer::sum);
// Adjacency list creation
adj.computeIfAbsent(u, k -> new ArrayList<>()).add(v);
// Default value
map.getOrDefault(key, 0);
```

#### 2D Coordinate Encoding (Avoid Object Overhead)
```java
// Encode (row, col) into a single 64-bit primitive long
long key = (((long) r) << 32) | (c & 0xFFFFFFFFL);
int r = (int) (key >> 32);
int c = (int) key;
```

---

### 11.2 Google L4 Verification & Edge Case Checklist
Before writing code or announcing completion to the interviewer, run through this 30-second checklist:

1. **Size & Cardinality Boundaries**:
   - $N = 0$ (empty array/string) $\rightarrow$ early return `0`, `""`, or `null`.
   - $N = 1$ (single element, single-node tree).
   - $K > N$ or $K = 0$ in top-K / sliding window problems.
2. **Numeric Limits & Overflow**:
   - Binary Search midpoint: always `l + (r - l) / 2`.
   - Sums & Products: cast to `long` before multiplying: `(long) a * b`.
   - Negatives & Zeros: check `-2^{31}` (`Integer.MIN_VALUE`), because `-Integer.MIN_VALUE == Integer.MIN_VALUE`.
3. **Graph Topologies**:
   - Is graph **directed** or **undirected**?
   - Can graph have **cycles**, **self-loops**, or **disconnected components**?
   - Did I mark `visited = true` **at push/enqueue time** in BFS?
4. **Tree Structures**:
   - Is it guaranteed to be a BST?
   - Can tree degenerate into a linked list (skewed tree: recursion stack depth $O(N)$)?
5. **Time & Space Justification**:
   - $N \le 20 \implies O(2^N)$ or $O(N!)$ (Backtracking, Bitmask DP).
   - $N \le 10^3 \implies O(N^2)$ (2D DP, all pairs).
   - $N \le 10^5 \implies O(N \log N)$ or $O(N)$ (Sorting, Heaps, Two Pointers, Monotonic Stack).
   - $N \le 10^9 \implies O(\log N)$ or $O(1)$ (Binary Search, Math, Bitwise).

---

### 11.3 Google Warsaw L4 Hub Specifics & Core Signal Guide
Google Warsaw is a major European engineering hub heavily concentrated on **Cloud Infrastructure, Distributed Systems (Borg/Kubernetes), and Storage**:
1. **State-Space Modeling**: Warsaw interviewers rarely ask straightforward LeetCode medium questions without a twist. Expect additional dimensions in state (e.g., Shortest path with at most $K$ obstacle eliminations, or BFS with key/lock bitmasks).
2. **Follow-up Instinct**: Always be prepared for:
   - *"What if data doesn't fit in RAM?"* $\rightarrow$ External sort / Two pointers over chunk files / MapReduce.
   - *"What if calls to `update()` are $100\times$ more frequent than `query()`?"* $\rightarrow$ Difference array vs Segment tree trade-offs.
   - *"Can we make this thread-safe or lock-free?"* $\rightarrow$ Atomic references, ConcurrentHashMap, read-write locks.
3. **Clean Code Over Cleanness Tricks**:
   - Write separate, modular helper functions (`isValid(r, c)`, `canAchieve(mid)`).
   - Avoid global variables or mutation of input arguments unless explicitly requested to optimize space in-place.

