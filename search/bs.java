package search;
import java.util.*;
public class bs {
    public static void main(String[] args) {
        //System.out.println(search_insert_position(new int[]{1,3,5,6}, 2));
        //System.out.println(Arrays.toString(first_n_last_pos(new int[]{5,7,7,8,8,10}, 8)));
        //System.out.println(search_in_rotated_sorted(new int[] {4,5,6,7,0,1,2}, 0));
        //System.out.println(fins_peak_element(new int[]{1,2,1,6}));
        //System.out.println(find_min_in_rorated_sorted(new int[]{2,4,5,6,0,1}));
        //System.out.println(search_in_2d(new int[][]{{1,3,5,7}, {10,11,16,20}, {23,30,34,60}}, 3));
        //System.out.println(find_in_mountain_Arr(new MountainArray(), 3));
        
    }

    

    public static class MountainArray{
        private int[] arr;
        public MountainArray(){
            this.arr = new int[]{1,2,3,4,5,3,1};
        }
        public int length(){
            return arr.length;
        }
        public int get(int idx){
            return arr[idx];
        }
    }

    private static int find_in_mountain_Arr(MountainArray mountainArray, int target){
        int n = mountainArray.length();
        int left = 0, right = n-1;
        while (left<right) {
            int mid = left + (right-left)/2;
            if (mountainArray.get(mid)<mountainArray.get(mid+1)) {
                left = mid+1;
            }else right = mid-1;
        }
        int peak = left;
        int idx = bs_mountain_arr(mountainArray, target, peak, 0, true);
        if (idx!=-1) {
            return idx;
        }
        return bs_mountain_arr(mountainArray, target, n-1, peak+1, false);

    }

    private static int bs_mountain_arr(MountainArray mountainArray, int target, int right, int left, boolean isAsc){
        while (left<=right) {
            int mid = left+(right-left)/2;
            int midVal = mountainArray.get(mid);
            if (target == midVal) {
                return mid;
            }
            if (isAsc) {
                if (midVal<target) {
                    left = mid+1;
                }else right = mid-1;
            }else{
                if (midVal<target) {
                    right = mid-1;
                }else left = mid+1;
            }
        }
        return -1;
    }

    private static boolean search_in_2d(int[][] grid, int target){
        int m = grid.length, n = grid[0].length;
        int left = 0, right = m*n-1;
        while (left<=right) {
            int mid = left + (right-left)/2;
            int midElement = grid[mid/n][mid%n];
            if (midElement==target) {
                return true;
            }else if (midElement<target) {
                left = mid+1;
            }else right = mid-1;
        }
        return false;
    }

    private static int find_min_in_rorated_sorted(int[] nums){
        int left = 0, right = nums.length-1;
        while (left<right) {
            int mid = left + (right-left)/2;
            if (nums[mid]>nums[left]) {
                left = mid+1;
            }else right = mid;
        }
        return nums[left];
    }

    private static int fins_peak_element(int[] nums){
        int left = 0, right = nums.length-1;
        while (left<right) {
            int mid = left+(right-left)/2;
            if (nums[mid]>nums[mid+1]) {
                right = mid;
            }else left = mid+1;
        }
        return left;
    }

    private static int search_in_rotated_sorted(int[] nums, int target){
        int left = 0, right = nums.length-1;
        while (left<=right) {
            int mid = left+(right-left)/2;
            if (nums[mid] == target) {
                return mid;
            }else if (nums[left]<=nums[mid]) { // left part is sorted
                if (nums[left]<=target && target<nums[mid]) {
                    right = mid-1;
                }else left = mid+1;
            }else{
                if (nums[right]>=target && target > nums[mid]) {
                    left = mid+1;
                }else right = mid-1;
            }
        }
        return -1;
    }

    private static int[] first_n_last_pos(int[] nums, int target){
        int start = -1, end = -1;
        int left = 0, right = nums.length-1;
        while (left<=right) {
            int mid = left+(right-left)/2;
            if (nums[mid]>target) {
                right = mid-1;
            }else if(nums[mid]<target){
                left = mid+1;
            }else{
                start = mid;
                end = mid;
                while (start>0 && target==nums[start-1]) {
                    start--;
                }
                while (end<nums.length && target==nums[end+1]) {
                    end++;
                }
                return new int[]{start, end};
            }
        }
        return new int[]{start, end};
    }

    private static int search_insert_position(int[] nums, int target){
        int left = 0, right = nums.length-1;
        while (left<=right) {
            int mid = left + (right-left)/2;
            if (nums[mid] == target) {
                return mid;
            }else if (nums[mid] < target) {
                left = mid+1;
            }else right = mid-1;
        }
        return left;
    }
}


