package hashmap;
import java.util.*;
public class hashmap {
    public static void main(String[] args) {
        System.out.println(longestConsecutiveSubsequence(new int[]{ 100,4,200,1,3,2}));
    }

    public static String reorganizeString(String s){
        int[] freq = new int[26];
        for (char c : s.toCharArray()) {
            freq[c-'a']++;
        }

        int maxFreq = 0, maxChar = 0;
        for(int i = 0; i < 26; i++){
            if (freq[i]>maxFreq) {
                maxFreq = freq[i];
                maxChar = i;
            }
        }

        int n = s.length();
        if (maxFreq > (n+1)/2) {
            return "";
        }

        char[] res = new char[n];

        int idx = 0;
        while (freq[maxChar]>0) {
            res[idx] = (char)(maxChar+'a');
            freq[maxChar]--;
            idx+=2;
        }

        for(int i = 0; i < 26; i++){
            while (freq[i]>0) {
                if (idx>=n) {
                    idx=1;
                }
                res[idx] = (char)(freq[i]+'a');
                idx+=2;
            }
        }

        return new String(res);
    }

    public static class Codec{
        private Map<Integer, String> map = new HashMap<>();
        public String encode(String url){
            int key = url.hashCode();
            while (map.containsKey(key) && !map.get(key).equals(url)) {
                key++;
            }
            map.put(key, url);
            return "https//tinyurl.com/"+key;
        }
        public String decode(String shortUrl){
            int key = Integer.parseInt(shortUrl.replace("https//tinyurl.com/", ""));
            return map.get(key);
        }
    } 

    public static List<List<String>> groupAnagrams(String[] strs) {
        Map<String, List<String>> map = new HashMap<>(); 
        for (String s : strs) {
            int[] freq = new int[26];
            for (char c : s.toCharArray()) {
                freq[c-'a']++;
            }
            StringBuilder sb = new StringBuilder();
            for(int n: freq){
                sb.append('#').append(n);
            }
            map.computeIfAbsent(s, k -> new ArrayList<>()).add(s);
        }
        return new ArrayList<>(map.values());
    }

    public static int longestConsecutiveSubsequence(int[] nums){
        HashSet<Integer> set = new HashSet<>();
        for(int n: nums) set.add(n);

        int res = 0;

        for(int n: nums){
            if(!set.contains(n-1)){
                int curr = 1;
                int currElement = n;
                while (set.contains(currElement+1)) {
                    currElement+=1;
                    curr++;
                }
                res=Math.max(res, curr);
            }
        }
        return res;
    }

    public static int maxNumberOfBalloons(String str) {
        Map<Character, Integer> textMap = new HashMap<>();
        Map<Character, Integer> targetMap = new HashMap<>();

        for (char c : str.toCharArray()) {
            textMap.put(c, textMap.getOrDefault(c, 0)+1);
        }

        String target = "balloon";
        for (char c : target.toCharArray()) {
            targetMap.put(c, targetMap.getOrDefault(c, 0)+1);
        }

        int res = Integer.MAX_VALUE;
        for(Map.Entry<Character, Integer> entry : targetMap.entrySet()){
            int supply = textMap.getOrDefault(entry.getKey(), 0);
            int demand = entry.getValue();
            res = Math.min(res, supply/demand);
        }
        return res;
    }

    public static int numIdenticalPairs(int[] nums) {
        int[] freq = new int[101];
        int count = 0;
        for(int num: nums){
            count+=freq[num];
            freq[num]++;
        }
        return count;
    }

    public static boolean isIsomorphic(String s, String t) {
        Map<Character, Character> mapStoT = new HashMap<>();
        Map<Character, Character> mapTtoS = new HashMap<>();

        for (int i = 0; i < s.length(); i++) {
            char charS = s.charAt(i), charT = t.charAt(i);
            if (mapStoT.containsKey(charS) && mapStoT.get(charS)!=charT) {
                return false;
            }
            if (mapTtoS.containsKey(charT) && mapTtoS.get(charT)!=charS) {
                return false;
            }

            mapStoT.put(charS, charT);
            mapTtoS.put(charT, charS);
        }
        return true;
    }

    public boolean canConstruct(String ransomNote, String magazine) {
        int[] arr = new int[26];
        for (char c : magazine.toCharArray()) {
            arr[c-'a']++;
        }
        for (char c : ransomNote.toCharArray()) {
            arr[c-'a']--;
            if (arr[c-'a']<0) {
                return false;
            }
        }
        return true;
    }

    public boolean containsNearbyDuplicate(int[] nums, int k) {
        Map<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < nums.length; i++) {
            if (map.containsKey(nums[i]) && i-map.get(nums[i])<=k) {
                return true;
            }
            map.put(nums[i], i);
        }
        return false;
    }

    
}
