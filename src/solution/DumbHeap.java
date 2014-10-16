package solution;

import java.util.ArrayList;

import game.Heap;

/** A dumb (O(n)) implementation of the Heap interface.
 * @author MPatashnik
 */
public class DumbHeap implements Heap<NodeWrapper> {

	private ArrayList<NodeWrapper> arr;
	
	public DumbHeap(){
		arr = new ArrayList<>();
	}
	
	/** Returns the minimum value in the heap by iterating through it.
	 * Returns null if the heap is currently empty
	 * Does this in O(N) time.
	 */
	@Override
	public NodeWrapper poll() {
		if(arr.isEmpty())
			return null;
		
		NodeWrapper min = null;
		for(NodeWrapper n : arr){
			if(min == null || n.compareTo(min) < 0)
				min = n;
		}
		
		arr.remove(min);
		return min;
	}

	/** Called when a value is updated. For this implementation, do nothing */
	@Override
	public void updateValue(NodeWrapper val) {
	}

	/** Adds the given value to the array. For this implementation, do this dumbly - back of arraylist */
	@Override
	public void add(NodeWrapper val) {
		arr.add(val);
	}

	/** Returns the size of the Heap */
	@Override
	public int size() {
		return arr.size();
	}

	/** Returns true if this Heap is empty (size == 0), false otherwise. */
	@Override
	public boolean isEmpty() {
		return arr.isEmpty();
	}

}
