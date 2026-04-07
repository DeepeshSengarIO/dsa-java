package arrays;

public class finding_counting {
    public static void main(String[] args) {
        
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
