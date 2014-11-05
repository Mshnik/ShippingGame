package game;

/** Specifies a class that is able to act as a dynamically sized Min Heap.
 * @author MPatashnik
 * @param <T> - type of elements in the implementing class.
 */
public interface MinHeap<T> {

	/** Returns a string that represents this heap, in format:
	 * [item1:priority1, item2:priority2, ... itemN:priorityN]
	 * Use the toString() method for strings for item1, item2, etc. 
	 */
	public String toString();
	
	/** Removes and returns the minimum valued element from the Heap.
	 *  Precondition: the heap is not empty.
	 *  Ideal - O(log(N)). Minimum - O(N) */
	public T poll();
	
	/** Change the priority of item.
	 *  Implement in at worst O(log(N)) time.
	 */
	public void updatePriority(T item, double priority);
	
	/** Adds item to the Heap. Min valued priority is at top of heap
	 *  Precondition: item is not already in the heap.
	 *  Throw an illegalArgumentException if {@code item} is already in the heap
	 *  Implement in at worst O(log(N)) time. */ 
	public void add(T item, double priority) throws IllegalArgumentException;
	
	/** Returns the size of the Heap. Implement in O(1) time. */
	public int size();
	
	/** Returns true if the Heap is empty. false otherwise. Implement in O(1) time */
	public boolean isEmpty();
	
}
