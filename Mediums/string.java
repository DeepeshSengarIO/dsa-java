package Mediums;

public class string {
    public static void main(String[] args) {
        
    }

    // LC 6
    public String ZigZagConversion(String s, int n) {
        if(s.length() <= n || n==1) return s;
        StringBuilder[] arr = new StringBuilder[n];
        for(int i = 0; i < n; i++) arr[i] = new StringBuilder();
        int curr = 0, isDown = -1;
        for(char c: s.toCharArray()){
            arr[curr].append(c);
            if(curr==0 || curr == n-1) isDown = -isDown;
            curr+=isDown;
        }
        StringBuilder res = new StringBuilder();
        for(StringBuilder row: arr){
            res.append(row);
        }
        return res.toString();
    }

    

}
