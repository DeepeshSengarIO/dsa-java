package prefixSum;

import java.util.*;

public class prefix_sum {
    public static void main(String[] args) {
        
    }

    public int subarraySumEqualK(int[] nums, int k){
        int res = 0;
        Map<Integer, Integer> map = new HashMap<>();
        int prefix_sum = 0;
        map.put(0, 1);
        for(int n: nums){
            prefix_sum+=n;
            res+=map.getOrDefault(prefix_sum-k, 0);
            map.put(prefix_sum, map.getOrDefault(prefix_sum, 0)+1);
        }
        return res;
    }

    public int subarraySumDivisibleByK(int[] nums, int k){
        Map<Integer, Integer> map = new HashMap<>();
        map.put(0, 1);
        int prefix_sum = 0;
        int res = 0;
        for (int n : nums) {
            prefix_sum+=n;

            int r = ((prefix_sum%k)+k)%k;
            res+=map.getOrDefault(r, 0);
            map.put(r, map.getOrDefault(r, 0)+1);
        }
        return res;
    }

    public static boolean continuousSubarraySum(int[] nums, int k){
        Map<Integer, Integer> map = new HashMap<>();
        map.put(0, -1);
        int sum = 0;
        for (int i = 0; i < nums.length; i++) {
            sum+=nums[i];
            int r = sum%k;
            if (map.containsKey(r)) {
                if (i-map.get(r)>=2) {
                    return true;
                }
            }else{
                map.put(r, i);
            }
        }
        return false;
    } 
}
