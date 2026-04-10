package Mediums;

import java.util.ArrayList;
import java.util.List;

public class arrays {
    public static void main(String[] args) {
        
    }

    // LC 80 Remove Duplicates 2
    // Two-Pointers
    public int removeDuplicates2(int[] arr){
        if (arr.length <= 2) {
            return arr.length;
        }
        int writePos = 2;
        for (int i = 2; i < arr.length; i++) {
            if (arr[i]!=arr[writePos-2]) {
                arr[writePos++] = arr[i];
            }
        }
        return writePos;
    }

    // LC 229 Majority Element II
    // Boyr-Moore Voting Algo
    public List<Integer> majorityElement(int[] nums) {
        int n = nums.length;
        int cand1 = 0, cand2 = 0;
        int count1 = 0, count2 = 0;
        // First pass: find candidates
        for(int num: nums){
            if (num == cand1) {
                count1++;
            }else if (num == cand2) {
                count2++;
            }else if (count1 == 0) {
                cand1 = num;
                count1 = 1;
            }else if(count2 == 0){
                cand2 = num;
                count2 = 1;
            }else{
                // Cancel: three distinct elements found
                count1--;
                count2--;
            }
        }

        //verify
        count1 = 0; count2 = 0;
        for (int num : nums) {
            if (num==cand1) {
                count1++;
            }else if (num==cand2) {
                count2++;
            }
        }

        List<Integer> res = new ArrayList<>();
        if (count1>n/3) {
            res.add(cand1);
        }
        if (count2>n/3) {
            res.add(cand2);
        }

        return res;
    }

    // LC 122 Best Time to Buy and Sell Stock II
    // Find and return the maximum profit you can achieve.
    // Greedy
    public int maxProfit2(int[] prices) {
        int res = 0;
        for (int i = 0; i < prices.length; i++) {
            // Collect every upward price movement
            if (prices[i] > prices[i-1]) {
                res+=prices[i]-prices[i-1];
            }
        }
        return res;
    }

    // LC 2348
    // Counting Consecutive Zeros
    public long zeroFilledSubarray(int[] nums) {
        long count = 0;
        int consecutiveZeros = 0;
        for (int num : nums) {
            if (num == 0) {
                consecutiveZeros++;
            }else{
                consecutiveZeros = 0;
            }
            count+=consecutiveZeros;
        }
        return count;
    }

    // LC 334
    public boolean increasingTriplet(int[] nums) {
        int first = Integer.MAX_VALUE, second = Integer.MAX_VALUE;
        for (int num : nums) {
            if (num>first) {
                // Smallest value so far
                first = num;
            }else if (num>second) {
                // Second smallest value (greater than some earlier first)
                second = num;
            }else{
                // Found a value greater than both first and second
                return true;
            }
        }
        return false;
    }

    // LC 238
    public int[] productExceptSelf(int[] nums) {
        int[] res = new int[nums.length];
        res[0] = 1;
        // First pass: store prefix products in result
        for (int i = 1; i < res.length; i++) {
            res[i] = res[i-1]*nums[i];
        }
        // Second pass: multiply suffix products in-place
        int suffixProduct = 1;
        for (int i = res.length-1; i >= 0; i--) {
            res[i] = res[i]*suffixProduct;
            suffixProduct*=nums[i];
        }
        return res;
    }

    // LC 31
    public void nextPermutation(int[] nums) {
        int n = nums.length;
        
        // Step 1: find the pivot (rightmost i where nums[i]<nums[i+1])
        int i = n-2;
        while (i>=0 && nums[i]>=nums[i-1]) {
            i--;
        }

        // Step 2: If pivot exists, find the successor and swap
        if (i>=0) {
            int j = n-1;
            while (nums[j]<=nums[i]) {
                j--;
            }
            // Step 3: find the successor and swap
            int temp = nums[i];
            nums[i] = nums[j];
            nums[j] = temp;
        }

        // Step 4: reverse the suffix starting at i+1
        int left = i+1;
        int right = n-1;
        while (left<right) {
            int temp = nums[left];
            nums[left++] = nums[right];
            nums[right--] = temp;
        }

    }
}
