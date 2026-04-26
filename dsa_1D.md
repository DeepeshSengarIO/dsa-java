## 1. Arrays
---

### 1.1 Swap, Reverse and Rotate

**Swap** is the technique to swap the elements in an array. To achieve this make a copy of first element and replace it with second element
```java
// Swap the elements at index i and j inside the array arr
void swap(int[] arr, int i, int j){
    int temp = arr[i];
    arr[i] = arr[j];
    arr[j] = temp;
}
```

**Reverse** is the technique to reverse the elements. 123 becomes 321.
```java
void reverse(int[] arr){
    int start = 0, end = arr.length-1; // index
    while(start<end){
        swap(arr, start, end);
        start++; end--; // shrink
    }
}
```

**Rotate** is the technique to rotate the array left or right by d places. 
*Example*: 12345 when rotated to left by `2` places, it becomes 34512
Step 1: cut the array in 2 halfes by `2` elements i.e. 12 345. Now, reverse both the halves saperately i.e. 21 543
Step 2: merge the reversed half and now again reverse the full merge i.e. reverse(21543) -> `34512` and you got your answer.

```java
void rotate(int[] arr, int d){
    int n = arr.length;
    //Step 1
    reverse(arr, 0, d-1); // reverse first half
    reverse(arr, d, n-1); // reverse second half
    //Step 2
    reverse(arr, 0, n-1); // reverse complete array
}
```

### 1.2 Kadane Algorithm
Used to find the maximum sum of a contiguous subarray.
**Example**: [-2,-1,3,-1,4] the contiguous subarray which has maximum sum is [3,-1,4] with a total of 3-1+4 = 6.
**Idea** While we proceed the array, We have 2 choices at each index: `Is it better to start a new subarray here, or continue the existing one?` remember this when we use this decision below.

```java
int maxSubarraySum(int[] arr){
    int maxSum = arr[0];
    int currSum = arr[0];
    for(int num: arr){
        // Make a decision
        currSum = Math.max(num, num+currSum); 
        // update the maxSum based on decision you took
        maxSum = Math.max(maxSum, currSum); 
    }
    return maxSum;
}
```

### 1.3 Boyre-Moore Voting Algorithm
A brilliant O(1) space algorithm to find an element that appears more than n/2 times in an array.
**Idea**: You maintain a "candidate" and a "count." If the next element is the candidate, increment count; otherwise, decrement. When count hits zero, pick the next element as the new candidate.
```java
void majorityElement(int[] arr){
    int candidate = 0;
    int count = 0;
    for(int num: arr){
        if(count == 0) candidate = num; // new candidate
        count += (candidate == num) ? 1 : -1;
    }
    return candidate;
}
```

### 1.4 Prefix/Suffix Product
The prefix-suffix approach uses two extra arrays. The prefix array captures the cumulative product of all elements to the left of each index. The suffix array captures the cumulative product of all elements to the right. When we multiply them together, we get the product of everything except the element at that index, because `prefix[i]` skips `nums[i]` (it only goes up to i-1) and `suffix[i]` also skips `nums[i]` (it starts from i+1).
**Question**: Product of Array Except self
**Idea**: Use one pass to store the prefix product in the result array, then a second pass (going backward) to multiply by the suﬃx product.
We will use the prefix-suffix approach but with a twist. we will replace the array with a single variable as we only need the running value i.e. space optimization
```java
int[] productOfArrayExceptSelf(int[] arr){
    int n = arr.length;
    int[] result = new int[n];
    result[0] = 1;
    // one pass to store the prefix product
    for(int i = 1; i < n; i++){
        result[i] = result[i-1] * arr[i-1];
    }
    // then a second pass (going backward) to multiply by the suﬃx product
    int right = 1; // space optimization here
    for(int i = n-1; i >= 0; i--){
        result[i] = result[i] * right;
        right = right * arr[i];
    }
    return result;
}
```

### 1.5 Lexicographical Next State
Systematic way to find the "next" logical arrangement of a set of items based on a specific ordering (usually alphabetical or numerical). If you imagine all possible permutations of a set listed out like words in a dictionary, this pattern allows you to jump from your current "word" to the very next one without generating the entire list.
**Question**: Next Permutation in an integer array
**Idea**: To find the next lexographical state you follow 4 steps:
Step 1: Scan from right to left, find the first element that is smaller than the element to its right. So basically `arr[i] > arr[i+1]`. We call this index as **`pivot`**.
Step 2: Scan again from right to left, this time find the smallest number which is bigger than pivot. clearly speaking, Find the smallest number in the suﬃx `[pivot+1, n-1]` that is larger than `nums[pivot]`. we call this **`successor`**
Step 3: swap the **`pivot`** and **`successor`**.
Step 4: Reverse everything to the right of the pivot's original position. Why? After the swap, the right side is still in descending order. Reversing it turns it into ascending order (the "smallest" it could be), giving us the immediate next permutation.
```java
void nextPermutation(int[] arr){
    int n = nums.length;
    int pivot = n-2;
    while(i>=0 && nums[pivot] > nums[pivot+1]) pivot--; // Step 1: Find the pivot index, from right to left <-
    if(pivot>=0){ // Step 2: Find Successor
        int successor = n-1;
        while(nums[successor] <= nums[pivot]) successor--;
        swap(nums, pivot, successor); // Step 3
    }
    reverse(nums, pivot+1, n-1); // Step 4
}
```

### 1.6 In-Place State Flagging (The "Flag")
When an array contains numbers in a fixed range (like [0,N]), you can use the sign of the numbers at specific indices to store boolean information ("Have I seen this?").
**Question**: Find All Numbers Disappeared in an Array.
**Idea**: For every number x you see, go to index `∣x∣−1` and make the number there negative. Any index
that remains positive at the end corresponds to a missing number.
```java
List<Integer> findAllDisappeared(int[] arr){
    for(int i = 0; i < arr.length; i++){
        int idx = Math.abs(arr[i])-1; 
        if(nums[idx]>0) nums[idx] = -nums[idx]; // flag 
    }
    List<Integer> result = new ArrayList<>();
    for(int i = 0; i < nums.length; i++){
        if(nums[i]>0) res.add(i+1); // add the remaining positive ones to result.
    }
    return res;
}
```

## 2. Strings
---











