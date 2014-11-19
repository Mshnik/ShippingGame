package student;

import game.MinHeap;
import java.util.ArrayList;
import java.util.HashMap;

/** An instance is a heap of elements of type T. */
public class GriesHeap<T> implements MinHeap<T> {

    private static final long serialVersionUID = 1L;
    
    // The heap consists of the b.size() elements in b.
    // They satisfy the heap properties:
    // 1. For each b[i], 1 <= i < b.size(), its parent is b[(i-1)/2].
    // 2. For each b[i], 0 <= i < b.size(), its children are b[2i + 1]
    //    and b[2i + 2] (if the indices are < b.size()).
    // 3. The priority of each b[i] >= the priority of its parent.
    private ArrayList<T> b= new ArrayList<T>();
    
    // Maps items in the heap to entries that contain their index and priority.
    private HashMap<T, ItemInfo> itemMap= new HashMap<T, ItemInfo>();

    /** Constructor: an empty heap. */
    public GriesHeap() { }

    /** Return a representation of this heap. */
    public @Override String toString(){
        String s = "[";
        for (T t : b) {
            if (s.length() > 1)  s= s + ", ";
            s= s + t  +  ":"  +  itemMap.get(t).priority;
        }
        return s +"]";
    }

    /** Remove and return the min value in this heap.
     * Precondition: The heap is not empty. */
    public @Override T poll() {
        T val= b.get(0);
        setAt(0, b.get(b.size()-1));
        itemMap.remove(val);
        b.remove(b.size()-1);
        bubbleDown(0);
        return val;
    }

    /** Add item with priority p to this heap.
     * Throw IllegalArgumentException if an equal item is already in the heap. */
    public @Override void add(T item, double p) throws IllegalArgumentException {
        if (itemMap.containsKey(item)) {
            throw new IllegalArgumentException("Duplicate heap elements not allowed");
        }
        itemMap.put(item, new ItemInfo(p, b.size()));
        b.add(item);
        bubbleUp(b.size()-1);
    }

    /** Change the priority of item to p. */
    public @Override void updatePriority(T item, double p) {
        ItemInfo entry = itemMap.get(item);
        if (p > entry.priority) {
			entry.priority = p;
			bubbleDown(entry.index);
        } else {
			entry.priority = p;
			bubbleUp(entry.index);
		}
    }

    /** Bubble b[k] up until it reaches its correct position in the heap.
     * Precondition: b satisfies the heap properties except perhaps b[k]
     * is smaller than its parent. */
    private void bubbleUp(int k) {
        int parent = (k-1)/2;
        // inv: b satisfies the heap properties except perhaps b[k]
        // is smaller than its parent, which is b[parent].
        while (k != 0  &&  comesBefore(k, parent)) {
            swap(parent, k);
            k= parent;
            parent= (k-1)/2;
        }
    }

    /** Bubble b[k] down until it reaches its correct position in the heap.
     * Precondition: b satisfies the heap properties except perhaps b[k]
     * is greater than one or both of its children. */
    private void bubbleDown(int k) {
        int c= getSmallerChild(k);
        // inv: b satisfies the heap properties except that b[k] may have
        //      a smaller a child  AND  b[c] is b[k]'s smaller child.
        while (c < size()  &&  comesBefore(c, k)) {
            swap(c, k);
            k= c;
            c= getSmallerChild(k);
        }
    }

    /** Return the index of the smaller of b[k]'s children. */
    public int getSmallerChild(int k) {
        int c= 2*k + 2; // index of k's right child
        if (c > size()-1  ||  comesBefore(c-1, c))
            return c-1;
        return c;
    }

    /** Return true iff b[h] <= b[k]. */
    private boolean comesBefore(int h, int k) {
        return itemMap.get(b.get(h)).priority <= itemMap.get(b.get(k)).priority;
    }

    /** Swap b[h] and b[k]. */
    private void swap(int h, int k) {
        T temp= b.get(h);
        setAt(h, b.get(k));
        setAt(k, temp);
    }

    /** Store item in b[k] and remap item to k in itemMap. */
    private void setAt(int k, T item) {
        b.set(k, item);
        itemMap.get(item).index= k;
    }

    /** An instance contains a priority and an index into b. */
    private static class ItemInfo {
        private double priority;  // Priority of this heap element
        private int index;        // heap element b[index].

        /** Constructor: an instance with priority p at index i. */
        private ItemInfo(double p, int i) {
            priority= p;
            index= i;
        }
    }

    /** Return the size of this heap. */
    public @Override int size() {
        return b.size();
    }

    /** Return true iff the heap is empty. */
    public @Override boolean isEmpty() {
        return b.size() == 0;
    }
}
