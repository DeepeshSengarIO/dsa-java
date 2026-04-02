package recursion_backtrack.PickNotPick;

import java.util.ArrayList;
import java.util.List;

public class PickNotPickRevision1 {
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
        
        return res;
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
        List<List<Integer>> res = new ArrayList<>();
        
        return res;
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
        
        return res;
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
        List<List<Integer>> res = new ArrayList<>();
        return res;
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
        return false;
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
        return 0;
    }

}
