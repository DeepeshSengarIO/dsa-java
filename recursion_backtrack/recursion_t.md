## 1. PICK / NOT PICK (Subset Pattern)
---
At every index we have two choices:
Pick the element
Dont pick the element

### template

```java
void solve(int idx, List<Integer> curr){
    if(idx == n){
        result.add(new ArrayList<>(curr));
        return;
    }
    // PICK
    curr.add(arr[idx]);
    solve(idx+1, curr);
    curr.reomve(curr.size()-1);

    // NOT PICK
    solve(idx+1, curr);
}
```
This generates 2^n possibilities


## 2. FOR-LOOP BACKTRACKING
---
Instead of:
“Pick or don’t pick”

You now think:
“At this position, I can pick ANY of the remaining choices”

### template

```java
void backtrack(List<Integer> curr, boolean[] used) {
    if (curr.size() == n) {
        res.add(new ArrayList<>(curr));
        return;
    }

    for (int i = 0; i < n; i++) {
        if (used[i]) continue;

        // choose
        used[i] = true;
        curr.add(nums[i]);

        backtrack(curr, used);

        // undo
        curr.remove(curr.size() - 1);
        used[i] = false;
    }
}
```
This builds permutations / arrangements

👉 Pick/Not Pick = decision tree
👉 For-loop BT = choice tree


## 3. CONSTRAINT BACKTRACKING
---
Earlier:
Pick/Not Pick → no restrictions
For-loop → multiple choices

You now think:
You can choose, BUT only if it’s valid

### template

```java
void solve(...) {
    if (goal reached) {
        save answer;
        return;
    }

    for (each possible choice) {
        if (isValid(choice)) {
            make move;

            solve(...);

            undo move;
        }
    }
}
```

