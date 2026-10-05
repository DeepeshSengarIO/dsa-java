# 📖 Comprehensive L4 Google SWE Phone Screen Bible

This document is the exhaustive compilation of all advanced DSA topics extracted from recent Google L4 (SWE-III) Poland phone screens. It covers everything: theory, "Explain Like I'm 5" (ELI5), pros/cons, decision-making, and full Java implementations.

---

## 1. Graph Algorithms

### 1.1 Encoding/Decoding a DAG & Cyclic Graph
*   **Concept**: Graph Serialization.
*   **ELI5**: How do you save a complex network of friends to a text file? Give everyone a unique badge number. Write down "Badge 1 is friends with 2, 3. Badge 2 is friends with 3." To load it back, create all the people first, then re-establish the friendships.
*   **What if it has cycles?**: If a graph has cycles (A -> B -> A), a naive recursive DFS will run forever. We *must* track nodes we have already serialized using a `HashMap<Node, Integer>`.
*   **Java Code**:
```java
// Serialization Strategy
Map<Node, Integer> nodeToId = new HashMap<>();
int idCounter = 0;

public String encode(Node root) {
    if (root == null) return "";
    StringBuilder sb = new StringBuilder();
    Queue<Node> q = new LinkedList<>();
    
    nodeToId.put(root, idCounter++);
    q.add(root);
    
    while (!q.isEmpty()) {
        Node curr = q.poll();
        sb.append(nodeToId.get(curr)).append(":");
        for (Node neighbor : curr.neighbors) {
            if (!nodeToId.containsKey(neighbor)) {
                nodeToId.put(neighbor, idCounter++);
                q.add(neighbor);
            }
            sb.append(nodeToId.get(neighbor)).append(",");
        }
        sb.append("\n");
    }
    return sb.toString();
}
```

### 1.2 Unweighted Shortest Paths & State-based BFS
*   **Concept**: Queue-based BFS for unweighted graphs.
*   **Follow-up 1: Find all nodes belonging to SOME shortest path.**
    *   **Theory**: Water flows from A to B. Any pipe that is perfectly efficient is on the shortest path.
    *   **Implementation**: Run BFS from `start` to get `distA[]`. Run BFS from `end` to get `distB[]`. For any node `u`, if `distA[u] + distB[u] == shortestPathLength`, node `u` belongs to a shortest path.
*   **Follow-up 2: Alice and Bob move simultaneously (Pursuit-Evasion).**
    *   **Theory**: Alice is trying to reach a destination; Bob is trying to catch her.
    *   **Implementation**: This is a multi-source BFS. First, calculate Bob's shortest time to reach every node -> `bobTime[]`. Then, run a BFS for Alice. Alice can only step onto node `v` at time `t` if `t < bobTime[v]` (meaning she gets there strictly before Bob).

### 1.3 Weighted Dijkstra with Priority Queue
*   **Concept**: Shortest path in graphs with positive weights.
*   **Why/When**: Use when edges have costs (distance, time). Do *not* use if weights are uniform (use BFS) or negative (use Bellman-Ford).
*   **ELI5**: Exploring roads by prioritizing the one with the cheapest toll so far.
*   **Code Boilerplate**:
```java
PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[1])); // [node, cost]
pq.add(new int[]{start, 0});
int[] dist = new int[n];
Arrays.fill(dist, Integer.MAX_VALUE);
dist[start] = 0;

while (!pq.isEmpty()) {
    int[] curr = pq.poll();
    int u = curr[0], cost = curr[1];
    if (cost > dist[u]) continue; // Stale state
    for (Edge e : graph.get(u)) {
        if (dist[u] + e.weight < dist[e.to]) {
            dist[e.to] = dist[u] + e.weight;
            pq.add(new int[]{e.to, dist[e.to]});
        }
    }
}
```

### 1.4 Dynamic Connectivity & DSU Rollbacks
*   **Concept**: Grouping connected components.
*   **Base Question**: *Given `timestamp, userA, userB`, find earliest time everyone connects.* -> Sort by timestamp, `union(userA, userB)` until `components == 1`.
*   **Follow-up: Max connections <= k, users UNFRIEND.**
    *   **Theory**: Standard DSU (Union-Find) uses "path compression", completely destroying the history of connections. If users can *unfriend*, we need to hit "Undo".
    *   **DSU with Rollback**: Do not use path compression. Only use Union by Rank/Size. Keep a Stack of the edges added and the old sizes of the components. To unfriend, pop from the stack and reverse the variable assignments.

---

## 2. Dynamic Programming

### 2.1 Matrix DP with Checkpoints
*   **Problem**: Reach `(n-1, m-1)` from `(n-1, 0)`. Moves allowed: right `(r, c+1)`, diag-up-right `(r-1, c+1)`, diag-down-right `(r+1, c+1)`.
*   **ELI5**: A platformer game where you always move forward (right), but can jump up or fall down a level.
*   **Follow-up 1 & 2: Visit checkpoints in order / count paths.**
    *   Since we exclusively move right, we must hit checkpoints in increasing order of their column index. If we have checkpoints `P1, P2, P3`, the total number of paths is `paths(Start -> P1) * paths(P1 -> P2) * paths(P2 -> P3) * paths(P3 -> End)`.
*   **Follow-up 3: Optimize Space.**
    *   **Theory**: To compute column `c`, we only need values from column `c-1`.
    *   **Code**:
```java
// Space O(N), Time O(N*M)
int[] prevCol = new int[n];
prevCol[n-1] = 1; // start pos

for (int c = 1; c < m; c++) {
    int[] currCol = new int[n];
    for (int r = 0; r < n; r++) {
        currCol[r] += prevCol[r]; // From (r, c-1)
        if (r > 0) currCol[r] += prevCol[r-1]; // From (r-1, c-1)
        if (r < n-1) currCol[r] += prevCol[r+1]; // From (r+1, c-1)
    }
    prevCol = currCol;
}
```

---

## 3. Trees

### 3.1 Tree DP: Min Cost to Disconnect Root from Leaves
*   **Problem**: Weighted binary tree. Find min cost to cut edges so no leaf is reachable from the root.
*   **ELI5**: You are trying to block water flowing from the root to the leaves. At every fork, you choose: do I plug this big pipe (cut this edge), or do I plug all the smaller pipes further down? You pick whichever is cheaper.
*   **Code**:
```java
public long minCostToDisconnect(TreeNode node, long weightToNode) {
    if (node.left == null && node.right == null) {
        return weightToNode; // Base case: leaf
    }
    long costToCutSubtrees = 0;
    if (node.left != null) costToCutSubtrees += minCostToDisconnect(node.left, node.leftWeight);
    if (node.right != null) costToCutSubtrees += minCostToDisconnect(node.right, node.rightWeight);
    
    return Math.min(weightToNode, costToCutSubtrees);
}
```

### 3.2 Tree Traversal + Hashmap / Stack + BST
*   **Tree + HashMap**: Used heavily for Vertical Order Traversal (mapping horizontal distance to list of nodes) or caching subtree sums.
*   **Stack + BST**: Used to implement a BST Iterator in $O(1)$ amortized time and $O(H)$ space, maintaining the "next" node in sorted order without storing the entire tree in an array.

---

## 4. Interval Management & Sweep Line

### 4.1 Sweep Line & Difference Events
*   **Concept**: Processing events in chronological order.
*   **Meeting Rooms / Overlapping**: Create two events per interval: `(start, +1)` and `(end, -1)`. Sort events. Iterate and accumulate the values.
*   **Difference Arrays**: Used when adding values to a range `[L, R]`. Do `arr[L] += val`, `arr[R+1] -= val`. Prefix sum of `arr` gives the final values.

### 4.2 Dynamic Intervals (Red-Black Trees / TreeMap)
*   **Problem**: `insert(L, R)` and `contains(number)` while overlapping intervals dynamically arrive.
*   **Why TreeMap?**: `TreeMap` in Java is a Red-Black tree. It keeps keys sorted and allows $O(\log N)$ retrieval of nearest neighbors (`floorKey`, `ceilingKey`).
*   **Code**:
```java
class DynamicIntervals {
    TreeMap<Integer, Integer> map = new TreeMap<>(); // Start -> End

    public void insert(int L, int R) {
        if (L >= R) return;
        Map.Entry<Integer, Integer> floor = map.floorEntry(L);
        if (floor != null && floor.getValue() >= L) {
            L = floor.getKey();
            R = Math.max(R, floor.getValue());
        }
        Map.Entry<Integer, Integer> ceiling = map.ceilingEntry(L);
        while (ceiling != null && ceiling.getKey() <= R) {
            R = Math.max(R, ceiling.getValue());
            map.remove(ceiling.getKey());
            ceiling = map.ceilingEntry(L);
        }
        map.put(L, R);
    }

    public boolean contains(int num) {
        Map.Entry<Integer, Integer> floor = map.floorEntry(num);
        return floor != null && floor.getValue() >= num;
    }
}
```

---

## 5. Arrays, Stacks, & Monotonicity

### 5.1 In-Place Sorting (Fixed Values)
*   **Concept**: Dutch National Flag algorithm. If the array only contains a fixed set of values (e.g., 0, 1, 2), sort it in $O(N)$ time and $O(1)$ space using 3 pointers (`low`, `mid`, `high`).
### 5.2 Modified Sorting
*   **Concept**: Apply an operation to integers, sort based on the output. Create a custom `Comparator` via Lambda: `Arrays.sort(arr, (a, b) -> Integer.compare(operate(a), operate(b)))`.
### 5.3 Unix Path Simplification (Deque)
*   **Concept**: Process `/a/./b/../../c/` to `/c`.
*   **Why Deque**: Use it as a stack to `pollLast()` when seeing `..`, but use it as a queue to build the final string front-to-back.
### 5.4 Distance-Constrained Data Stream (Monotonic Stacks)
*   **Problem**: Stream of ints. Find if a tuple satisfies `abs(x-y) <= d`, `abs(y-z) <= d`, `abs(z-x) <= d`.
*   **Theory**: To find elements with small differences rapidly, you need the active stream in sorted order. While monotonic stacks are great for "next greater element" or maintaining mins/maxes of sliding windows, a `TreeSet` (Red-Black Tree) is the most robust way to insert stream integers and query `floor()` / `ceiling()` to check for elements within distance `d` in $O(\log N)$ time.

---

## 6. System Design, OOP, and Strings

### 6.1 Custom Validating LinkedList
*   **Problem**: Node has `value`, `next`, and `hash`. `hash = string_hash(value + next.hash)`. Validate the list.
*   **ELI5**: A blockchain. If someone changes block 2, the hash of block 1 breaks.
*   **Code**:
```java
public boolean validate(ValidatingNode head) {
    while (head != null) {
        String expectedNextHash = (head.next != null) ? head.next.hash : "";
        String computedHash = computeHash(head.val + expectedNextHash); // Implementation specific
        if (!head.hash.equals(computedHash)) return false;
        head = head.next;
    }
    return true;
}
```

### 6.2 Parking Lot Allocation (OOP)
*   **Concept**: Design given `(vehicleType, slots)` and `(vehicleType, costPerMin)`.
*   **Implementation Details**:
    *   Use an `Enum` for Vehicle Types (COMPACT, LARGE).
    *   Use a `HashMap<TicketId, ParkingSession>` to track entry times.
    *   Use a `PriorityQueue` of available spots to instantly allocate the "closest" or "best" spot in $O(\log S)$.
    *   Cost calculation: `(exitTime - entryTime) * costPerMin`.

### 6.3 String Format Converter & Ambigrams
*   **snake_case to camelCase**:
    *   **Crucial Interview Move**: Clarify assumptions! "Are there trailing underscores?", "Are characters guaranteed lowercase?", "How to handle numbers?"
    *   **Logic**: Split by `_`, lowercase first word, capitalize first letter of subsequent words.
*   **Ambigrams / Interesting Words**:
    *   **Concept**: Words reading the same upside down (e.g., '6' and '9').
    *   **Logic**: Use Two Pointers (`left = 0`, `right = len-1`) validating against a `HashMap` of valid pairs. Use a `HashSet` of the dictionary to achieve $O(1)$ lookups for follow-up optimizations.

---

## Summary of L4 Alternatives
*   **Deque** $\rightarrow$ Often replaceable by **Monotonic Stacks** for scheduling and array optimization.
*   **Prefix Sum Array** $\rightarrow$ Replaceable by **Sliding Window / Two Pointers** for better space efficiency if there are no negative numbers.
*   **Custom Trees** $\rightarrow$ Always leverage Java's **TreeMap** / **TreeSet** for Interval Management and nearest-neighbor scheduling collisions.


---

## 9. Geometry: Sweep Line for Area Bisection
**Question**: You are given several axis-aligned squares. Find a horizontal line $Y = y$ that splits the total area of these squares into two equal halves. 
*(Note: You proposed Binary Search, but the interviewer wanted an exact, deterministic solution.)*

**Why Binary Search fails (or is suboptimal)**:
Binary search on floats $O(N \log(\text{Range}/\epsilon))$ can suffer from precision issues and is considered suboptimal when an exact algebraic solution exists. The interviewer was looking for an $O(N \log N)$ exact Sweep Line algorithm, which is a classic Google L4/L5 geometry pattern.

**Expected Input / Output:**
```text
Squares (Bottom-Left X, Bottom-Left Y, Side Length):
[ (0, 0, 2), (3, 0, 2) ] -> Two 2x2 squares resting on the X-axis. Total Area = 8.
Output: 1.0 
// The horizontal line Y=1 cuts exactly through the middle of both squares, leaving 4 area below and 4 area above.
```

**Approach (Deterministic Sweep Line):**
1. **Find Total Area**: First, calculate the total area of all squares. Target area is `Total / 2.0`.
2. **Create Events**: For every square, create two Y-axis events:
   *   `Event(Y = bottom_edge, delta_width = +side_length)`
   *   `Event(Y = top_edge, delta_width = -side_length)`
3. **Sort Events**: Sort all events by their Y coordinate.
4. **Sweep Line**: 
   Iterate through the events. Maintain a `current_width` (how much of the line intersects squares right now) and a `current_area_below`.
   When you move from `prev_Y` to `curr_Y`, the area added is `current_width * (curr_Y - prev_Y)`.
   If `current_area_below + added_area >= Target`, the bisection line is *between* `prev_Y` and `curr_Y`.
   You can find the exact floating-point $Y$ algebraically:
   `exact_Y = prev_Y + (Target - current_area_below) / current_width`.

**Java Code:**
```java
class Event implements Comparable<Event> {
    double y;
    double deltaWidth;
    
    public Event(double y, double deltaWidth) {
        this.y = y;
        this.deltaWidth = deltaWidth;
    }
    
    public int compareTo(Event other) {
        return Double.compare(this.y, other.y);
    }
}

public double findSplittingLine(int[][] squares) {
    double totalArea = 0;
    List<Event> events = new ArrayList<>();
    
    for (int[] sq : squares) {
        double x = sq[0], y = sq[1], side = sq[2];
        totalArea += side * side;
        // Entering a square increases intersecting width
        events.add(new Event(y, side));
        // Exiting a square decreases intersecting width
        events.add(new Event(y + side, -side));
    }
    
    Collections.sort(events);
    
    double targetArea = totalArea / 2.0;
    double currentArea = 0;
    double currentWidth = 0;
    double prevY = events.get(0).y;
    
    for (Event event : events) {
        double currY = event.y;
        double areaAdded = currentWidth * (currY - prevY);
        
        if (currentArea + areaAdded >= targetArea) {
            // The split line is between prevY and currY!
            double remainingArea = targetArea - currentArea;
            return prevY + (remainingArea / currentWidth);
        }
        
        currentArea += areaAdded;
        currentWidth += event.deltaWidth;
        prevY = currY;
    }
    
    return -1.0; // Should never reach here if inputs are valid
}
```
*(Note: If the squares overlap and you need to bisect the UNION area, calculating `currentWidth` requires a standard Segment Tree overlaid with the Sweep Line to compute the active X-axis coverage at any given Y. However, summing individual widths as shown above is the standard expectation for non-overlapping arrays of shapes).*
