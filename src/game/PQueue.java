package game;
/** An implementation implements a priority queue whose elements are of type E.
 *  Below, N is used as the number of elements currently in the priority queue.
 *  Duplicate elements are not allowed.
 *  The priorities are doubles values. */
public interface PQueue<E> {

	/** Return a string that represents this priority queue, in the format:
	 * [item0:priority0, item1:priority1, ..., item(N-1):priority(N-1)]
	 * Thus, the list is delimited by '['  and ']' and ", " (i.e. a
	 * comma and a space char) separate adjacent items. */
	String toString();

	/** Return the number of elements in the priority queue. */
	int size();

	/** Return true iff the priority queue is empty. */
	boolean isEmpty();

	/** Add e with priority p to the priority queue.
	 *  Throw an illegalArgumentException if e is already in the queue. */ 
	void add(E e, double priority) throws IllegalArgumentException;

	/** Return the element of the priority queue with lowest priority, without
	 *  changing the priority queue.
	 *  Precondition: the priority queue is not empty. */
	E peek();

	/** Remove and return the element of the priority queue with lowest priority.
	 *  Precondition: the priority queue is not empty. */
	E poll();

	/** Change the priority of element e to p.
	 *  Precondition: e is in the priority queue */
	void updatePriority(E e, double p);
}
