package student;


/** An implementation implements a dynamically sized min Heap of elements
 * of type T. Below, N is used as the number of elements currently in the heap.
 * @author MPatashnik
 * @param <T> - type of elements in the implementing class.
 */
public interface MinHeap<T> {

	/** Return a string that represents this heap, in the format:
	 * [item1:priority1, item2:priority2, ..., itemN:priorityN]
	 * Use the toString() function of the items. 
	 */
	public String toString();
	
	/** Remove and return the minimum-valued element from the Heap,
	 * in worst-case time no more than O(log N).
	 *  Precondition: the heap is not empty. */
	public T poll();
	
	/** Change the priority of t to p.
	 *  Must take worst-case time no more than O(log N).
	 */
	public void updatePriority(T t, double p);
	
	/** Add t with priority p to the Heap.
	 *  Throw an illegalArgumentException if t is already in the heap
	 *  Implement in worst-case time no more than O(log N). */ 
	public void add(T t, double priority) throws IllegalArgumentException;
	
	/** Return the size of the Heap. Implement in O(1) time. */
	public int size();
	
	/** Return true iff the Heap is empty. Implement in O(1) time */
	public boolean isEmpty();
	
}
