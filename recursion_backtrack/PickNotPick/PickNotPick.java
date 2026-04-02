package recursion_backtrack.PickNotPick;

import java.util.*;

public class PickNotPick {
    public static void main(String[] args) {
        //System.out.println(subsets(new int[]{1,2,3}));
        //System.out.println(subsetsII(new int[]{1,2,2}));
        //System.out.println(combinationSum(new int[]{2,3,6,7}, 7));
        //System.out.println(combinationSum2(new int[]{10,1,2,7,6,1,5}, 8));
        //System.out.println(canPartition(new int[]{1,5,5,11}));
        //System.out.println(countSubsetsWithSumK(new int[]{1, 2, 3, 4, 5}, 5));
    }

    /* LC 78
    Given an integer array nums of --unique elements--, return all possible subsets (the power set).
    The solution set must not contain duplicate subsets. Return the solution in any order.
    Example 1:
    Input: nums = [1,2,3]
    Output: [[],[1],[2],[1,2],[3],[1,3],[2,3],[1,2,3]]

    Return all subsets
    Power set
    */
    public static List<List<Integer>> subsets(int[] nums) {
        List<List<Integer>> res = new ArrayList<>();
        subsets_helper(nums, new ArrayList<>(), res, 0);
        return res;
    }

    private static void subsets_helper(int[] nums, List<Integer> curr, List<List<Integer>> res, int idx){
        if (idx == nums.length) {
            res.add(new ArrayList<>(curr));
            return;
        }
        //PICK
        curr.add(nums[idx]);
        subsets_helper(nums, curr, res, idx+1);
        curr.remove(curr.size()-1);
        //NOT-PICK
        subsets_helper(nums, curr, res, idx+1);
    }

    /* LC 90
    Given an integer array nums that --may contain duplicates--, return all possible subsets (the power set).
    The solution set must not contain duplicate subsets. Return the solution in any order.
    Example 1:
    Input: nums = [1,2,2]
    Output: [[],[1],[1,2],[1,2,2],[2],[2,2]]

    Array contains duplicate
    Unique subsets required
    */
    public static List<List<Integer>> subsetsII(int[] nums) {
        Arrays.sort(nums);
        List<List<Integer>> res = new ArrayList<>();
        subsetsII_helper(nums, new ArrayList<>(), res, 0);
        return res;
    }

    private static void subsetsII_helper(int[] nums, List<Integer> curr, List<List<Integer>> res, int idx){
        
        if (idx==nums.length) {
            res.add(new ArrayList<>(curr));
            return;
        }
        //PICK
        curr.add(nums[idx]);
        subsetsII_helper(nums, curr, res, idx+1);
        curr.remove(curr.size()-1);

        // skip duplicates
        while (idx+1<nums.length && nums[idx]==nums[idx+1]) {
            idx++;
        }

        //NOT-PICK
        subsetsII_helper(nums, curr, res, idx+1);
    }

    /* LC 39
    Input: candidates = [2,3,6,7], target = 7
    Output: [[2,2,3],[7]]
    Explanation:
    2 and 3 are candidates, and 2 + 2 + 3 = 7. Note that 2 can be used multiple times.
    7 is a candidate, and 7 = 7.
    These are the only two combinations.
    */
    public static List<List<Integer>> combinationSum(int[] candidates, int target) {
        List<List<Integer>> res = new ArrayList<>();
        combinationSum_helper(res, target, new ArrayList<>(), candidates, 0);
        return res;
    }

    private static void combinationSum_helper(List<List<Integer>> res, int target, List<Integer> curr, int[] candidates, int idx){
        if (target == 0) {
            res.add(new ArrayList<>(curr));
            return;
        }
        if (idx == candidates.length) {
            return;
        }

        //PICK
        if (candidates[idx] <= target) {
            curr.add(candidates[idx]);
            combinationSum_helper(res, target-candidates[idx], curr, candidates, idx);
            curr.remove(curr.size()-1);
        }

        //NOT-PICK
        combinationSum_helper(res, target, curr, candidates, idx+1);
    }

    /* LC 40
    Given a collection of candidate numbers (candidates) and a target number (target), 
    find all unique combinations in candidates where the candidate numbers sum to target.
    Each number in candidates may only be used once in the combination.
    Input: candidates = [10,1,2,7,6,1,5], target = 8
    Output: 
    [
    [1,1,6],
    [1,2,5],
    [1,7],
    [2,6]
    ]
    */
    public static List<List<Integer>> combinationSum2(int[] candidates, int target) {
        Arrays.sort(candidates);
        List<List<Integer>> res = new ArrayList<>();
        combinationSum2_helper(res, new ArrayList<>(), candidates, target, 0);
        return res;
    }

    private static void combinationSum2_helper(List<List<Integer>> res, List<Integer> curr, int[] candidates, int target, int idx){
        if (target==0) {
            res.add(new ArrayList<>(curr));
            return;
        }
        if (idx == candidates.length || target < 0) {
            return;
        }

        //PICK
        curr.add(candidates[idx]);
        combinationSum2_helper(res, curr, candidates, target-candidates[idx], idx+1);
        curr.remove(curr.size()-1);

        //skip-duplicates
        while (idx+1<candidates.length && candidates[idx]==candidates[idx+1]) {
            idx++;
        }

        //NOT-PICK
        combinationSum2_helper(res, curr, candidates, target, idx+1);
    }

    /* LC 416
    Given an integer array nums, return true if you can partition the array into two subsets such that 
    the sum of the elements in both subsets is equal or false otherwise.
    Input: nums = [1,5,11,5]
    Output: true
    Explanation: The array can be partitioned as [1, 5, 5] and [11].

    Is there a subset with sum = total/2 therefore becomes a PICK/NOT-PICK Problem
    Although this problem should be done with DP
    */
    public static boolean canPartition(int[] nums) {
        int total = 0;
        for(int n: nums) total+=n;
        if (total % 2 != 0) return false;

        int target = total/2;
        // Memo table: memo[index][current_target]
        Boolean[][] memo = new Boolean[nums.length][target + 1];
        return canPartition_helper(nums, 0, target, memo);
    }

    private static boolean canPartition_helper(int[] nums, int idx, int target, Boolean[][] memo) {
        // Base Cases
        if (target == 0) return true;
        if (idx >= nums.length || target < 0) return false;

        // Check Cache
        if (memo[idx][target] != null) return memo[idx][target];

        // PICK: Move to next index, subtract value from target
        boolean pick = canPartition_helper(nums, idx + 1, target - nums[idx], memo);
        
        // Short circuit: If pick worked, no need to check not-pick
        if (pick) return memo[idx][target] = true;

        // NOT-PICK: Move to next index, target stays same
        boolean notPick = canPartition_helper(nums, idx + 1, target, memo);

        return memo[idx][target] = notPick;
    }

    /* Count Subsets with Sum K
    Given an array arr of n integers and an integer K, 
    count the number of subsets of the given array that have a sum equal to K.
    Input: arr = [1, 2, 2, 3], K = 3
    Output: 3
    Explanation: The subarrays [1,2], [1,2] and [3] have a sum of 3.

    Input: arr = [1, 2, 3, 4, 5], K = 5
    Output: 3
    Explanation: The subsets are [5], [2, 3], and [1, 4]. 
    */
    public static int countSubsetsWithSumK(int[] nums, int k){
        // Integer[][] instead of int[][] to distinguish between 0 and "not calculated"
        Integer[][] memo = new Integer[nums.length][k + 1];
        return countSubsetsWithSumK_helper(nums, k, 0, memo);
    }

    private static int countSubsetsWithSumK_helper(int[] nums, int target, int idx, Integer[][] memo){
        if (idx == nums.length) {
            return (target == 0) ? 1 : 0;
        }
        if (memo[idx][target] != null) return memo[idx][target];
        // PICK (only if target >= nums[idx])
        int pick = 0;
        if (target >= nums[idx]) {
            pick = countSubsetsWithSumK_helper(nums, target - nums[idx], idx + 1, memo);
        }

        // NOT-PICK
        int notPick = countSubsetsWithSumK_helper(nums, target, idx + 1, memo);

        return memo[idx][target] = pick + notPick;
    }

}
