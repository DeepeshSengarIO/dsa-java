package hashmap;

public class DesignHashmap {
    private static final int SIZE = 769;
    private ListNode[] buckets;

    private static class ListNode{
        int key, value;
        ListNode next;
        ListNode(int k, int val, ListNode next){
            this.key = k;
            this.value = val;
            this.next = next;
        }
    }

    public DesignHashmap(){
        buckets = new ListNode[SIZE];
    }

    private int hash(int key){
        return key%SIZE;
    }

    public void put(int key, int val){
        int bucket = hash(key);
        ListNode curr = buckets[bucket];
        while (curr!=null) {
            if (curr.key == key) {
                curr.value = val;
                return;
            }
            curr = curr.next;
        }
        buckets[bucket] = new ListNode(key, val, buckets[bucket]);
    }

    public int get(int key){
        int bucket = hash(key);
        ListNode curr = buckets[bucket];
        while (curr!=null) {
            if (curr.key == key) {
                return curr.value;
            }
            curr=curr.next;
        }
        return -1;
    }

    public void remove(int key){
        int bucket = hash(key);
        if (buckets[bucket]!=null && buckets[bucket].key==key) {
            buckets[bucket] = buckets[bucket].next;
            return;
        }
        ListNode curr = buckets[bucket];
        while (curr!=null && curr.next!=null) {
            if (curr.next.key == key) {
                curr.next = curr.next.next;
                return;
            }
            curr = curr.next;
        }
    }
}
