package recursion_backtrack.ForLoopBT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ForLoopBT {
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
        permute_helper(nums, res, new ArrayList<>(), new boolean[nums.length]);
        return res;
    }

    private static void permute_helper(int[] nums, List<List<Integer>> res, List<Integer> curr, boolean[] used){
        if (curr.size() == nums.length) {
            res.add(new ArrayList<>(curr));
            return;
        }
        for (int i = 0; i < nums.length; i++) {
            if (used[i]) {
                continue;
            }
            //choose
            used[i] = true;
            curr.add(nums[i]);

            permute_helper(nums, res, curr, used);
            
            //undo
            curr.remove(curr.size()-1);
            used[i] = false;
        }
    }

    /* LC 47
    Given a collection of numbers, nums, that might contain duplicates, return all possible unique permutations in any order.
    Input: nums = [1,1,2]
    Output: [[1,1,2], [1,2,1], [2,1,1]]
    */
    public static List<List<Integer>> permuteUnique(int[] nums) {
        List<List<Integer>> res = new ArrayList<>();
        Arrays.sort(nums);
        permuteUnique_helper(nums, res, new ArrayList<>(), new boolean[nums.length]);
        return res;
    }

    private static void permuteUnique_helper(int[] nums, List<List<Integer>> res, List<Integer> curr, boolean[] used){
        if (curr.size() == nums.length) {
            res.add(new ArrayList<>(curr));
            return;
        }
        for (int i = 0; i < nums.length; i++) {
            if (used[i]) {
                continue;
            }

            //skip duplicates
            if(i>0 && nums[i]==nums[i-1]&&!used[i-1]) continue;

            //choose
            used[i] = true;
            curr.add(nums[i]);

            permuteUnique_helper(nums, res, curr, used);
            
            //undo
            used[i] = false;
            curr.remove(curr.size()-1);
        }
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
        combine_helper(res, new ArrayList<>(), n, k, 1);
        return res;
    }

    private static void combine_helper(List<List<Integer>> res, List<Integer> curr, int n, int k, int idx){
        if (curr.size() == k) {
            res.add(new ArrayList<>(curr));
            return;
        }
        for (int i = idx; i <= n; i++) {
            // choose
            curr.add(i);
            combine_helper(res, curr, n, k, i+1);

            //undo
            curr.remove(curr.size()-1);
        }
    }

    /* LC 17
    Given a string containing digits from 2-9 inclusive, 
    return all possible letter combinations that the number could represent. Return the answer in any order.
    Input: digits = "23"
O   utput: ["ad","ae","af","bd","be","bf","cd","ce","cf"]
    */
    public static List<String> letterCombinations(String digits) {
        Map<Character, String> map = Map.of(
            '2', "abc",
            '3', "def",
            '4', "ghi",
            '5', "jkl",
            '6', "mno",
            '7', "pqrs",
            '8', "tuv",
            '9', "wxyz"
        );
        List<String> res = new ArrayList<>();
        letterCombinations_helper(res, "", digits, 0, map);
        return res;
    }

    private static void letterCombinations_helper(List<String> res, String curr, String digits, int idx, Map<Character, String> map){
        if (idx == digits.length()) {
            res.add(new String(curr));
            return;
        }
        for (Character c : map.get(digits.charAt(idx)).toCharArray()) {
            letterCombinations_helper(res, curr+c, digits, idx+1, map);
        }
    }

}