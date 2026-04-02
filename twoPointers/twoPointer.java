package twoPointers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class twoPointer {
    public static void main(String[] args) {
        System.out.println(threeSum(new int[]{-1,0,1,2,-1,-4}));
    }

    public static List<List<Integer>> threeSum(int[] nums) {
        List<List<Integer>> res = new ArrayList<>();
        Arrays.sort(nums);
        for (int i = 0; i < nums.length-2; i++) {
            if (i>0 && nums[i]==nums[i-1]) {
                continue;
            }
            int left = i+1, right = nums.length-1;
            while (left<right) {
                int sum = nums[i]+nums[left]+nums[right];
                if (sum>0) {
                    right--;
                }else if (sum<0) {
                    left++;
                }
                else{
                    res.add(Arrays.asList(nums[i],nums[left],nums[right]));
                    while (left<right && nums[left]==nums[left+1]) {
                        left++;
                    }
                    while (left<right && nums[right]==nums[right-1]) {
                        right--;
                    }
                    left++;right--;
                }
            }
        }
        return res;
    }

    public static int trappingRainWater(int[] nums){
        int left = 0, right = nums.length-1;
        int leftMax = nums[left], rightMax = nums[right];
        int rain = 0;
        while (left<=right) {
            if (nums[left] <= nums[right]) {
                if (nums[left]>leftMax) {
                    leftMax = nums[left];
                }else rain+=(leftMax-nums[left]);
                left++;
            }else{
                if (nums[right]>=rightMax) {
                    rightMax = nums[right];
                }else rain+=(rightMax-nums[right]);
                right--;
            }
        }
        return rain;
    }
}
