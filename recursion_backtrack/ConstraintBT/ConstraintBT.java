package recursion_backtrack.ConstraintBT;
import java.util.*;
public class ConstraintBT {
    public static void main(String[] args) {
        //System.out.println(solveNQueens(4));
        //solveSudoku(new char[][]{{'5','3','.','.','7','.','.','.','.'},{'6','.','.','1','9','5','.','.','.'},{'.','9','8','.','.','.','.','6','.'},{'8','.','.','.','6','.','.','.','3'},{'4','.','.','8','.','3','.','.','1'},{'7','.','.','.','2','.','.','.','6'},{'.','6','.','.','.','.','2','8','.'},{'.','.','.','4','1','9','.','.','5'},{'.','.','.','.','8','.','.','7','9'}});
        System.out.println(exist(new char[][]{{'A','B','C','E'},{'S','F','C','S'},{'A','D','E','E'}}, "ABCCED"));
    }

    /* LC 51
    The n-queens puzzle is the problem of placing n queens on an n x n chessboard such that no two queens attack each other.
    Given an integer n, return all distinct solutions to the n-queens puzzle. You may return the answer in any order.
    Input: n = 4
    Output: [[".Q..","...Q","Q...","..Q."],["..Q.","Q...","...Q",".Q.."]]
    Explanation: There exist two distinct solutions to the 4-queens puzzle as shown above
    */
    public static List<List<String>> solveNQueens(int n){
        char[][] grid = new char[n][n];
        boolean[] cols = new boolean[n];
        boolean[] diag1 = new boolean[2*n];
        boolean[] diag2 = new boolean[2*n];
        for (char[] cs : grid) {
            Arrays.fill(cs, '.');
        }
        List<List<String>> res = new ArrayList<>();
        solveNQueens_helper(res, 0, n, grid, cols, diag1, diag2);
        return res;
    }

    private static void solveNQueens_helper(List<List<String>> res, int row, int n, 
        char[][] grid, boolean[] cols, boolean[] diag1, boolean[] diag2){
        if (row == n) {
            List<String> list = new ArrayList<>();
            for (char[] r : grid) {
                list.add(new String(r));
            }
            res.add(list);
            return;
        }

        for (int col = 0; col < n; col++) {
            if (cols[col] || diag1[col+row] || diag2[row-col+n]) {
                continue;
            }

            //place queen
            cols[col] = diag1[col+row] = diag2[row - col + n] = true;
            grid[row][col] = 'Q';

            solveNQueens_helper(res, row+1, n, grid, cols, diag1, diag2);

            cols[col] = diag1[col+row] = diag2[row - col + n] = false;
            grid[row][col] = '.';
        }
    }

    /* LC 37
    Write a program to solve a Sudoku puzzle by filling the empty cells.
    A sudoku solution must satisfy all of the following rules:
    Each of the digits 1-9 must occur exactly once in each row.
    Each of the digits 1-9 must occur exactly once in each column.
    Each of the digits 1-9 must occur exactly once in each of the 9 3x3 sub-boxes of the grid.
    Input: board = [["5","3",".",".","7",".",".",".","."],["6",".",".","1","9","5",".",".","."],[".","9","8",".",".",".",".","6","."],["8",".",".",".","6",".",".",".","3"],["4",".",".","8",".","3",".",".","1"],["7",".",".",".","2",".",".",".","6"],[".","6",".",".",".",".","2","8","."],[".",".",".","4","1","9",".",".","5"],[".",".",".",".","8",".",".","7","9"]]
    Output: [["5","3","4","6","7","8","9","1","2"],["6","7","2","1","9","5","3","4","8"],["1","9","8","3","4","2","5","6","7"],["8","5","9","7","6","1","4","2","3"],["4","2","6","8","5","3","7","9","1"],["7","1","3","9","2","4","8","5","6"],["9","6","1","5","3","7","2","8","4"],["2","8","7","4","1","9","6","3","5"],["3","4","5","2","8","6","1","7","9"]]
    */
    public static void solveSudoku(char[][] board) {
        solveSudoku_helper(board);
        for(char[] c: board){
            System.out.println(Arrays.toString(c));
        }
    }

    private static boolean solveSudoku_helper(char[][] board) {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (board[i][j]=='.') {
                    for (char c = '1'; c <= '9'; c++) {
                        if (isValidSudoku(board, i, j, c)) {
                            board[i][j] = c;
                            if (solveSudoku_helper(board)) {
                                return true;
                            }
                            board[i][j] = '.';
                        }
                    }
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean isValidSudoku(char[][] board, int row, int col, char c) {
        for (int i = 0; i < 9; i++) {
            if (board[row][i] == c) {
                return false;
            }
            if (board[i][col] == c) {
                return false;
            }
            int r = 3*(row/3)+i/3;
            int cBox = 3*(col/3)+i%3;
            if (board[r][cBox]==c) {
                return false;
            }
        }
        return true;
    }

    /* LC 79
    Given an m x n grid of characters board and a string word, return true if word exists in the grid.
    Input: board = [["A","B","C","E"],["S","F","C","S"],["A","D","E","E"]], word = "ABCCED"
    Output: true
    */
    public static boolean exist(char[][] board, String word) {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                if (exist_helper(board, i, j, word, 0)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean exist_helper(char[][] board, int i, int j, String word, int idx) {
        if (idx==word.length()) {
            return true;
        }
        if (i<0||j<0||i>=board.length||j>=board[0].length||board[i][j]!=word.charAt(idx)) {
            return false;
        }

        char temp = board[i][j];
        board[i][j] = '#';
        boolean found = exist_helper(board, i+1, j, word, idx+1) ||
                        exist_helper(board, i, j+1, word, idx+1) ||
                        exist_helper(board, i-1, j, word, idx+1) ||
                        exist_helper(board, i, j-1, word, idx+1);
        board[i][j] = temp;
        return found;
    }

}
