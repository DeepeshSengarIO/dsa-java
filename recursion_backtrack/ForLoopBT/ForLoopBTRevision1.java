package recursion_backtrack.ForLoopBT;

import java.util.ArrayList;
import java.util.List;

public class ForLoopBTRevision1 {
    public static void main(String[] args) {
        //System.out.println(permute(new int[]{1,2,3}));
        //System.out.println(permuteUnique(new int[]{1,1,2}));
        //System.out.println(combine(4, 2));
        //System.out.println(letterCombinations("23"));
    }

    /* LC 46
    Given an array nums of distinct integers, return all the possible permutations. You can return the answer in any order.
    Example 1:
    Input: nums = [1,2,3]
    Output: [[1,2,3],[1,3,2],[2,1,3],[2,3,1],[3,1,2],[3,2,1]]
    */
    public static List<List<Integer>> permute(int[] nums) {
        List<List<Integer>> res = new ArrayList<>();
        return res;
    }

    /* LC 47
    Given a collection of numbers, nums, that might contain duplicates, return all possible unique permutations in any order.
    Input: nums = [1,1,2]
    Output: [[1,1,2], [1,2,1], [2,1,1]]
    */
    public static List<List<Integer>> permuteUnique(int[] nums) {
        List<List<Integer>> res = new ArrayList<>();
        return res;
    }

    /* LC 77
    Given two integers n and k, return all possible combinations of k numbers chosen from the range [1, n].
    You may return the answer in any order.
    Input: n = 4, k = 2
    Output: [[1,2],[1,3],[1,4],[2,3],[2,4],[3,4]]
    Explanation: There are 4 choose 2 = 6 total combinations.
    Note that combinations are unordered, i.e., [1,2] and [2,1] are considered to be the same combination.
    */
    public static List<List<Integer>> combine(int n, int k) {
        List<List<Integer>> res = new ArrayList<>();
        
        return res;
    }

    /* LC 17
    Given a string containing digits from 2-9 inclusive, 
    return all possible letter combinations that the number could represent. Return the answer in any order.
    Input: digits = "23"
O   utput: ["ad","ae","af","bd","be","bf","cd","ce","cf"]
    */
    public static List<String> letterCombinations(String digits) {
        List<String> res = new ArrayList<>();   
        return res;
    }
}
