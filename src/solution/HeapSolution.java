package solution;

import game.MinHeap;
import java.util.*;

public class HeapSolution<T> extends ArrayList<T> implements MinHeap<T> {

	private static final long serialVersionUID = 1L;
	
	// Maps items in the heap to an entry that contains their index and priority.
	private HashMap<T, ItemInfo> itemInfoMap;

	/** Constructor: an empty heap. */
	public HeapSolution() {
		itemInfoMap = new HashMap<T, ItemInfo>();
	}

	/** Return a representation of this min heap. */
	@Override
	public String toString(){
		if (isEmpty()) return "[]";
		String s = "[";
		for (T t : this) {
		    s += t.toString() + ":" + itemInfoMap.get(t).priority + ", ";
		}
		return s.substring(0, s.length()-2) +"]";
	}
	
	@Override
	/** Remove and return the min value in this heap.
	 * Precondiiton: The heap is not empty. */
	public T poll() {
		T rtnVal = super.get(0);
		setAt(0, super.get(size() - 1));
		itemInfoMap.remove(rtnVal);
		super.remove(size() - 1);
		bubbleDown(0);
		return rtnVal;
	}

	@Override
	/** Add item with priority p to this heap. */
	public void add(T item, double p) throws IllegalArgumentException {
		if (itemInfoMap.containsKey(item)) {
			throw new IllegalArgumentException("Cannot add duplicate elements to the heap");
		}
		itemInfoMap.put(item, new ItemInfo(p, size()));
		super.add(item);
		bubbleUp(size() - 1);
	}

	@Override
	/** Change the priority of item to p. */
	public void updatePriority(T item, double p) throws IllegalArgumentException {
		ItemInfo entry = itemInfoMap.get(item);
		if(entry == null) 
			throw new IllegalArgumentException("Cannot update priority for element not in heap");
		double oldPriority = entry.priority;
		entry.priority = p;
		if (p > oldPriority) {
			bubbleDown(entry.index);
		} else {
			bubbleUp(entry.index);
		}
	}

	/** Bubble the element at index up until it reaches its correct position in the
	 * heap.
	 * Precondition: the heap satisfies all heap properties except that item
	 * get[index] may be smaller than its parent. */
	private void bubbleUp(int index) {
		if (index == 0) return;
		int parent = parentIndex(index);
		if (comesBefore(parent, index)) return;
		swap(parent, index);
		bubbleUp(parent);
	}

	/** Bubble the element at index down until it reaches its correct position in the
	 * heap.
	 * Precondition: the heap satisfies all heap properties except that item
     * get[index] may be larger than a child. */
	private void bubbleDown(int index) {
		int hiPriChild = leftChildIndex(index);
		if (hiPriChild >= size()) return;
		int rChildIndex = rightChildIndex(index);
		if (rChildIndex < size() && comesBefore(rChildIndex, hiPriChild)) {
			hiPriChild = rChildIndex;
		}
		if (comesBefore(index, hiPriChild)) return;
		swap(index, hiPriChild);
		bubbleDown(hiPriChild);
	}

	/** Return true iff parent <= child (they are in order). */
	private boolean comesBefore(int parent, int child) {
		return itemInfoMap.get(super.get(parent)).priority <= itemInfoMap.get(super.get(child)).priority;
	}

	/** Swap the items at index a and b. */
	private void swap(int a, int b) {
		T temp = super.get(a);
		setAt(a, super.get(b));
		setAt(b, temp);
	}
	
	/** Set the item at index to item, and remap item to index in the indices map. */
	private void setAt(int index, T item) {
		super.set(index, item);
		itemInfoMap.get(item).index = index;
	}

	/** Return the index of the left child of the item at index i. */
	private int leftChildIndex(int i) {
		return 2*i + 1;
	}
	
	/** Return the index of the right child of the item at index i. */
	private int rightChildIndex(int i) {
		return 2*i + 2;
	}

	/** Return the index of the parent of the item at index i. */
	private int parentIndex(int i) {
		return (i - 1)/2;
	}
	
	/** An instance is an item in the heap, with an index and a priority. */
	private static class ItemInfo {
		private double priority;
		private int index;
		
		/** Constructor: an instance with priority p at index i. */
		private ItemInfo(double p, int i) {
			priority = p;
			index = i;
		}
	}
}
