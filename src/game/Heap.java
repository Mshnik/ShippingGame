package game;

/** Specifies a class that is able to act as a dynamically sized
 * Min Heap.
 * @author MPatashnik
 * @param <T> - type of elements in the implementing class.
 */
public interface Heap<T extends Comparable<T>> {

	/** Gets and removes the minimum valued element from the Heap.
	 * Ideal - O(log(N)). Minimum - O(N)
	 */
	public T poll();
	
	/** Call when the value that is used to determine the ordering of an
	 *  element changes within the Heap.
	 *  Takes as an argument the value which currently an element of this Heap
	 *  that has its value changed. 
	 *  May be unused in less complex implementations.
	 *  Implement in at worst O(log(N)) time.
	 */
	public void updateValue(T val);
	
	/** Adds the value val to the Heap. Do not add val if it is already
	 * contained in this Heap. Implement in at worst O(log(N)) time. */ 
	public void add(T val);
	
	/** Returns the size of the Heap. Implement in O(1) time. */
	public int size();
	
	/** Returns true if the Heap is empty. false otherwise. Implement in O(1) time */
	public boolean isEmpty();
	
}
