package sliding_window;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class fixed_size {
    public static void main(String[] args) {
        
    }

    public List<Integer> findAnagrams(String s, String p) {
        List<Integer> res = new ArrayList<>();
        int sLen = s.length(), pLen = p.length();
        if (sLen < pLen) return res;

        int[] pCount = new int[26];
        int[] windowCount = new int[26];

        for (int i = 0; i < pLen; i++) {
            pCount[p.charAt(i)-'a']++;
            windowCount[s.charAt(i)-'a']++;
        }

        if (Arrays.equals(pCount, windowCount)) res.add(0);

        // slide the window
        for (int i = pLen; i < sLen; i++) {
            windowCount[s.charAt(i)-'a']++;
            windowCount[s.charAt(i-pLen)-'a']--;
            if (Arrays.equals(pCount, windowCount)) {
                res.add(i-pLen+1);
            }
        }
        return res;
    }
}
