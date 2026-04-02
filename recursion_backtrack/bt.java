package recursion_backtrack;
import java.util.*;
public class bt {
    public static void main(String[] args) {
        //System.out.println(pow(5, 2));
        //System.out.println(generate_parentheses(3));
        //System.out.println(permutations(new int[]{1,2,3}));
        //System.out.println(subsets(new int[]{1,2,3}));
        //System.out.println(combinationSum2(new int[]{2,3,6,7}, 7));
        //System.out.println(letterCombinations("236"));
        //System.out.println(palindromePartition("aab"));
        //System.out.println(n_queens(4));
    }

    private static List<List<String>> n_queens(int n){
        List<List<String>> res = new ArrayList<>();
        Set<Integer> cols = new HashSet<>();
        Set<Integer> posDiags = new HashSet<>();
        Set<Integer> negDiags = new HashSet<>();
        char[][] board = new char[n][n];
        for (int i = 0; i < board.length; i++) {
            Arrays.fill(board[i], '.');
        }
        nq_helper(0, posDiags,cols, negDiags, board, res, n);
        return res;
    }

    private static void nq_helper(int row, Set<Integer> posDiags,Set<Integer> cols, Set<Integer> negDiags, char[][] board, List<List<String>> res, int n) {
        if (row==n) {
            res.add(construct(board));
            return;
        }
        for (int col = 0; col < n; col++) {
            int posDiag = row+col;
            int negDiag = row-col;
            if (cols.contains(col) || posDiags.contains(posDiag) || negDiags.contains(negDiag)) {
                continue;
            }
            cols.add(col);
            posDiags.add(posDiag);
            negDiags.add(negDiag);
            board[row][col] = 'Q';

            nq_helper(row+1, posDiags, cols, negDiags, board, res, negDiag);

            cols.remove(col);
            posDiags.remove(posDiag);
            negDiags.remove(negDiag);
            board[row][col] = '.';
        }
    }

    private static List<String> construct(char[][] board){
        List<String> path = new ArrayList<>();
        for (char[] chars : board) {
            path.add(new String(chars)); // Convert char array to string
        }
        return path;
    }

    private static List<List<String>> palindromePartition(String s){
        List<List<String>> res = new ArrayList<>();
        pp_helper(res, s, 0, new ArrayList<>());
        return res;
    }

    private static void pp_helper(List<List<String>> res, String s, int start, List<String> curr) {
        if (start==s.length()) {
            res.add(new ArrayList<>(curr));
            return;
        }
        for (int i = start; i < s.length(); i++) {
            if (isPalindrome(s, start, i)) {
                curr.add(s.substring(start, i+1));
                pp_helper(res, s, i+1, curr);
                curr.remove(curr.size()-1);
            }
        }
    }

    private static boolean isPalindrome(String s, int left, int right){
        while (left<right) {
            if (s.charAt(left++)!=s.charAt(right--)) {
                return false;
            }
        }
        return true;
    }

    private static List<String> letterCombinations(String digits){
        List<String> res = new ArrayList<>();
        Map<Character, String> map = Map.of('2', "abc", '3', "def", '4', "ghi", '5', "jkl", '6',
         "mno", '7', "pqrs", '8', "tuv", '9', "wxyz");
        lc_helper(res, map, "", digits, 0);
        return res;
    }

    private static void lc_helper(List<String> res, Map<Character, String> map, String curr, String digits, int index){
        if (index == digits.length()) {
            res.add(curr);
            return;
        }
        String letters = map.get(digits.charAt(index));
        for(char c: letters.toCharArray()){
            curr+=c;
            lc_helper(res, map, curr, digits, index+1);
            curr = curr.substring(0, curr.length()-1);
        }
    }

    private static List<List<Integer>> combinationSum2(int[] candidates, int target){
        List<List<Integer>> res = new ArrayList<>();
        cs_helper2(res, new ArrayList<>(), candidates, target, 0);
        return res;
    }

    private static void cs_helper2(List<List<Integer>> res, List<Integer> curr, int[] candidates, int target, int start){
        if (target==0) {
            res.add(new ArrayList<>(curr));
            return;
        }
        for (int i = start; i < candidates.length; i++) {
            if (candidates[i]>target) {
                break;
            }
            if (i>start && candidates[i]==candidates[i-1]) {
                continue;
            }
            curr.add(candidates[i]);
            cs_helper2(res, curr, candidates, target-candidates[i], i+1);
            curr.remove(curr.size()-1);
        }
    }

    private static List<List<Integer>> combinationSum(int[] candidates, int target){
        List<List<Integer>> res = new ArrayList<>();
        cs_helper(res, new ArrayList<>(), candidates, target, 0);
        return res;
    }

    private static void cs_helper(List<List<Integer>> res, List<Integer> curr, int[] candidates, int target, int start){
        if (target==0) {
            res.add(new ArrayList<>(curr));
            return;
        }
        for (int i = start; i < candidates.length; i++) {
            if (candidates[i]>target) {
                continue;
            }
            curr.add(candidates[i]);
            cs_helper(res, curr, candidates, target-candidates[i], i);
            curr.remove(curr.size()-1);
        }
    }

    public static List<List<Integer>> subsets(int[] nums){
        List<List<Integer>> res = new ArrayList<>();
        subsets_helper(res, new ArrayList<>(), nums, 0);
        return res;
    }

    private static void subsets_helper(List<List<Integer>> res, List<Integer> curr, int[] nums, int start) {
        res.add(new ArrayList<>(curr));
        for (int i = start; i < nums.length; i++) {
            curr.add(nums[i]);
            subsets_helper(res, curr, nums, i+1);
            curr.remove(curr.size()-1);
        }
    }

    private static List<List<Integer>> permutations(int[] nums){
        List<List<Integer>> res = new ArrayList<>();
        permutations_helper(nums, res, new ArrayList<>(), new boolean[nums.length]);
        return res;
    }

    private static void permutations_helper(int[] nums, List<List<Integer>> res, List<Integer> curr, boolean[] visited) {
        if (curr.size() == nums.length) {
            res.add(new ArrayList<>(curr));
            return;
        }
        for (int i = 0; i < nums.length; i++) {
            if (visited[i]) {
                continue;
            }
            visited[i] = true;
            curr.add(nums[i]);
            permutations_helper(nums, res, curr, visited);
            visited[i] = false;
            curr.remove(curr.size()-1);
        }
    }

    private static List<String> generate_parentheses(int n){
        List<String> res = new ArrayList<>();
        gp_helper(res, "", n, 0, 0);
        return res;
    }

    private static void gp_helper(List<String> res, String curr, int n, int open, int close){
        if (curr.length() == 2*n) {
            res.add(curr);
            return;
        }
        if (open<n) {
            gp_helper(res, curr+"(", n, open+1, close);
        }
        if (close<open) {
            gp_helper(res, curr+")", n, open, close+1);
        }
    }

    private static double pow(double x, int n){
        if (n==0) {
            return 1;
        }
        if(n<0){
            x = 1/x;
            n=-n;
        }
        return powRecursive(x, n);
    }
    private static double powRecursive(double x, int n){
        if (n==0) {
            return 1;
        }
        double half = powRecursive(x, n/2);
        if (n%2==0) {
            return half*half;
        }else return x*half*half;
    }
}