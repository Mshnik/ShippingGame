package student;


import java.util.ArrayList;
import java.util.HashMap;

/** An instance is a heap of elements of type T. */
public class MyHeap<T> implements MinHeap<T> {
    
	/** An ArrayList that maintains the elements of the heap in a binary tree bfs order. */
    private ArrayList<T> heap;
    
    /** A HashMap that maintains the priority and index information of the elements in the heap. */
    private HashMap<T, info> infoMap;
	
	/** Constructor: an empty heap. */
    public MyHeap() {
    	heap = new ArrayList<T>();
    	infoMap = new HashMap<T, info>();
    }

    /** Return a representation of this heap. */
    public @Override String toString(){
        //TODO  
    	int l = size();
    	// Initialization
    	T curr = heap.get(0);
    	double prio = infoMap.get(curr).getPriority();
    	
    	StringBuffer output = new StringBuffer("[" + curr + ":" + prio);
    	// Loop through the entire heap
    	for (int i = 1; i < l; i++){
    		curr = heap.get(i);
    		prio = infoMap.get(curr).getPriority();
    		
    		output.append(", " + curr + ":" + prio);
    	}
    	
    	output.append("]");
    	
        return output.toString();
    }

    /** Remove and return the min value in this heap.
     * Precondition: The heap is not empty. */
    public @Override T poll() {
        //TODO 
    	// Get the minimum value
    	T root = heap.get(0);
    	// Replace the first element with the last element and remove the former first element
    	int l = size()- 1;
    	swapEle(0, l);
    	heap.remove(l);
    	infoMap.remove(root);
    	// Bubble down the new first element to its rightful place
    	bubbleDown(0);
    	
        return root;
    }

    /** Add item with priority p to this heap.
     * Throw IllegalArgumentException if an equal item is already in the heap. */
    public @Override void add(T item, double p) throws IllegalArgumentException {
        //TODO 
    	if (heap.contains(item)) // heap doesn't contain this item
    		throw new IllegalArgumentException("This item is already in the heap");
    	else
    	{
    		// Add the new element to the end of the heap (ArrayList)
    		heap.add(item);
    		// Add the new element to the information map
    		int l = size() - 1;
    		info newInfo = new info(p, l);
    		infoMap.put(item, newInfo);
    		// Bubble up the new element to its rightful place
    		bubbleUp(l);
    	}
    }

    /** Change the priority of item to p. */
    public @Override void updatePriority(T item, double p) {
        //TODO
    	// If the item is not in the heap, do nothing
    	if (!infoMap.containsKey(item))
    		return;
    	else // item is in the heap
    	{
    		// Update item to the new priority
    		infoMap.get(item).setPriority(p);
    		// Find index of item
    		int k = infoMap.get(item).getIndex();
    		// If item is the first element
    		if (k == 0)
    		{
    			if (size()==1) // item is the only element in the heap, do nothing
    				return;
    			// item has lower priority than the one and only child, swap them
    			else if (k+1 == size()-1 && p > infoMap.get(heap.get(k+1)).getPriority())
    				swapEle(k, k+1);
    			// item has lower priority than one of its two children, bubble item down
    			else if (k+2 == size()-1 && (p > infoMap.get(heap.get(k+1)).getPriority() || 
    					p > infoMap.get(heap.get(k+2)).getPriority()))
    				bubbleDown(k);
    		}
    		
    		// k is not 0, get its parent index c
    		int c = (int) Math.floor((k-1)/2);
    		// if item has higher priority than its parent, bubble it up
    		if (p < infoMap.get(heap.get(c)).getPriority())
    			bubbleUp(k);
    		else // item has the same or lower priority than its parent, so bubble it down
    			bubbleDown(k);
    	}
    }

    /** Return the size of this heap. */
    public @Override int size() {
        //TODO
        return heap.size();
    }

    /** Return true iff the heap is empty. */
    public @Override boolean isEmpty() {
        //TODO
        return heap.isEmpty();
    }
    
    /** Bubble element k up the binary tree backed by this heap until the right place
    Precondition: k is in the interval [0, N], where N is the number of element in heap */
    private void bubbleUp(int k){
    	if (k == 0)
    		return;
    	// Get the priority of element k
    	T thisK = heap.get(k);
    	double prioK = infoMap.get(thisK).getPriority();
    	
    	// p is the index of the parent of the element k
    	int p = (int) Math.floor((k - 1)/2);
    	T currP = heap.get(p);
    	double prioP = infoMap.get(currP).getPriority();
    	
    	// while element k is not the first element of the heap and it has a higher priority
    	// than its parent, continue to swap them
    	while (k > 0 && prioK < prioP)
    	{
    		swapEle(k, p);
    		// Update index of the former kth element
    		k = p;
    		// Update index of its  parent
    		p = (int) Math.floor((k - 1)/2);
    		if (p >= 0) // if the new kth element has a parent
            {
    			// update the parent
    			currP = heap.get(p);
    			prioP = infoMap.get(currP).getPriority();
            }
        	
    	}
    	
    }
 
    /** Bubble element p down the binary tree backed by this heap until the right place
    Precondition: p is in the interval [0, N], where N is the number of element in heap */
    private void bubbleDown(int p){
    	// Initialize k to the second child of element at p,
    	// where k is the index of the higher priority child of element at p
    	int k = p * 2 + 2;
    	// If k is out of bound or priority of the first child is higher than the second child,
    	// update k to the first child of element at p
    	if (k > size()-1 || (infoMap.get(heap.get(k)).getPriority() > infoMap.get(heap.get(k-1)).getPriority()))
    		k--;
    	
    	// While k is not out of bound and p has a lower priority than its child with the lower priority
    	while (k < size() && (infoMap.get(heap.get(p)).getPriority() > infoMap.get(heap.get(k)).getPriority()))
    	{
    		swapEle(k, p);
    		// Update index of the former pth element
    		p = k;
    		// Update index of its child
    		k = p * 2 + 2;
    		if (k > size()-1 || (infoMap.get(heap.get(k)).getPriority() > infoMap.get(heap.get(k-1)).getPriority()))
        		k--;
    	}
    	
    }
    
    // Swap the element at index k with the one at index p in the heap
    private void swapEle(int k, int p){
    	// Swap elements in the heap
    	T tempK = heap.get(k);
    	T tempP = heap.get(p);
    	heap.set(k, tempP);
    	heap.set(p, tempK);
    	
    	// Update indices
    	infoMap.get(tempK).setIndex(p);
    	infoMap.get(tempP).setIndex(k);
    }
    
    /** An inner class of MyHeap that keeps the priority and index information
    of each element in the heap */
    public class info{
    	/** Priority of the element.*/
    	private double priority; 
    	
    	/** Index of the element in the ArrayList. */
    	private int index;
    	
    	/** Constructor: an instance with priority prio and index ind (ind non-negative). */
    	private info(double prio, int ind){
    		priority = prio;
    		index = ind;
    	}
    	
    	/** Return the priority of the element. */
    	public double getPriority(){
    		return priority;
    	}
    	
    	/** Return the index of the element. */
    	public int getIndex(){
    		return index;
    	}
    	
    	/** Set the priority of the element to prio. */
    	private void setPriority(double prio){
    		priority = prio;
    	}
    	
    	/** Set the index of the element to ind.
    	 Precondition: ind is non-negative. */
    	private void setIndex(int ind){
    		index = ind;
    	}
    }
}