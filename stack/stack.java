package stack;

import java.util.*;

public class stack {
    public static void main(String[] args) {
        
        //MinStack stack = new MinStack();
        //stack.caller();

        System.out.println(removeDuplicateLetters("cbacdcbc"));

    }

    static class MinStack{

        ArrayDeque<int[]> stack;

        public void caller(){
            MinStack s = new MinStack();
            s.push(-2);
            s.push(0);
            s.push(-3);
            System.out.println(s.getMin()); // -3
            s.pop();
            System.out.println(s.top()); // 0
            System.out.println(s.getMin()); // -2

        }

        public MinStack(){
            stack = new ArrayDeque<>();
        }

        public void push(int val){
            int currMin = stack.isEmpty() ? val : Math.min(val, stack.peek()[1]);
            stack.offerLast(new int[]{val, currMin} );
        }

        public void pop(){
            stack.pollLast();
        }

        public int top(){
            return stack.peekLast()[0];
        }

        public int getMin(){
            return stack.peekLast()[1];
        }
    }

    public static String removeDuplicateLetters(String s) {
        Map<Character, Integer> freq = new HashMap<>();
        for (char c : s.toCharArray()) {
            freq.put(c, freq.getOrDefault(c, 0)+1);
        }
        ArrayDeque<Character> stack = new ArrayDeque<>();
        Set<Character> inStack = new HashSet<>();

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            freq.put(c, freq.get(c)-1);
            if (inStack.contains(c)) {
                continue;
            }
            while (!stack.isEmpty() && stack.peekLast() > c && freq.get(stack.peekLast())>0) {
                char removed = stack.pollLast();
                inStack.add(removed);
            }
            inStack.add(c);
            stack.offerLast(c);
        }

        StringBuilder sb = new StringBuilder();
        for (char c : stack) {
            sb.append(c);
        }
        return sb.toString();
    }
}
