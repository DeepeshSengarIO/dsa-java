package arrays;

public class manipulation {

    // LC 27
    public int removeElement(int[] nums, int val) {
        int start = 0;
        for (int i = 0; i < nums.length; i++) {
            if (nums[i]!=val) {
                nums[start++] = nums[i];
            }
        }
        return start+1;
    }

    // LC 1470
    public int[] shuffle(int[] nums, int n) {
        int[] res = new int[2*n];
        for(int i = 0; i < n; i++){
            res[2*i] = nums[i];
            res[2*i+1] = nums[n+i];
        }
        return res;
    }

    // LC 80
    public int removeDuplicates2(int[] nums) {
        int start = 2;
        for (int i = 2; i < nums.length; i++) {
            if(nums[i]!=nums[start-2]){
                nums[start++] = nums[i];
            }
        }
        return start;
    }

    // LC 414
    public int thirdMax(int[] nums) {
        long first = Long.MIN_VALUE, second = Long.MIN_VALUE, third = Long.MIN_VALUE;
        for(int n: nums){
            if(n == first || n == second || n == third) continue;
            if(n > first){
                third = second;
                second = first;
                first = n; 
            }else if(n>second){
                third = second;
                second = n;
            }else if(n > third){
                third = n;
            }
        }
        return (int) (third == Long.MIN_VALUE ? first : third);
    }

}
