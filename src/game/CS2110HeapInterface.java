package game;
/** Specifies a class that is able to act as a dynamically sized Min Heap.
 * @author MPatashnik
 * @param <T> - type of elements in the implementing class.
 */
public interface CS2110HeapInterface<T> {

	/** Removes and returns the minimum valued element from the Heap.
	 *  Precondition: the heap is not empty.
	 *  Ideal - O(log(N)). Minimum - O(N) */
	public T poll();
	
	/** Change the priority of item.
	 *  Precondition: priority is less than or equal to item's previous priority.
	 *  May be unused in less complex implementations.
	 *  Implement in at worst O(log(N)) time.
	 */
	public void updatePriority(T item, double priority);
	
	/** Adds item to the Heap.
	 *  Precondition: item is not already in the heap.
	 *  Implement in at worst O(log(N)) time. */ 
	public void add(T item, double priority);
	
	/** Returns the size of the Heap. Implement in O(1) time. */
	public int size();
	
	/** Returns true if the Heap is empty. false otherwise. Implement in O(1) time */
	public boolean isEmpty();
	
}
