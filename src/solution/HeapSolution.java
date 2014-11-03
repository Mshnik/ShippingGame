package solution;

import game.MinHeap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class HeapSolution<T> extends ArrayList<T> implements MinHeap<T> {

	private static final long serialVersionUID = 1L;
	
	// Maps items in the heap to an entry that stores their index and priority.
	private HashMap<T, ItemInfo> itemInfoMap;

	public HeapSolution() {
		itemInfoMap = new HashMap<T, ItemInfo>();
	}

	@Override
	public String toString(){
		if(isEmpty()) return "[]";
		String s = "[";
		Iterator<T> iter = iterator();
		while(iter.hasNext()){
			T t = iter.next();
			s += t.toString() + ":" + itemInfoMap.get(t).priority + ", ";
		}
		return s.substring(0, s.length()-2) +"]";
	}
	
	@Override
	public T poll() {
		T rtnVal = super.get(0);
		setAt(0, super.get(size() - 1));
		itemInfoMap.remove(rtnVal);
		super.remove(size() - 1);
		rotateDown(0);
		return rtnVal;
	}

	@Override
	public void add(T item, double priority) throws IllegalArgumentException {
		if (itemInfoMap.containsKey(item)) {
			throw new IllegalArgumentException("Cannot add duplicate elements to the heap");
		}
		itemInfoMap.put(item, new ItemInfo(priority, size()));
		super.add(item);
		rotateUp(size() - 1);
	}

	@Override
	public void updatePriority(T item, double priority) {
		ItemInfo entry = itemInfoMap.get(item);
		if (priority > entry.priority) {
			throw new IllegalArgumentException("Cannot increase priority with the decreaesePriority method");
		}
		entry.priority = priority;
		rotateUp(entry.index);
	}

	/** Bubbles the element at index up until it reaches its correct position in the
	 * heap. Precondition: index is correctly ordered with respect to its children. */
	private void rotateUp(int index) {
		if (index == 0) return;
		int parent = parentIndex(index);
		if (comesBefore(parent, index)) return;
		swap(parent, index);
		rotateUp(parent);
	}

	/** Bubbles the element at index down until it reaches its correct position in the
	 * heap. Precondition: index is correctly ordered with respect to its parent. */
	private void rotateDown(int index) {
		int hiPriChild = leftChildIndex(index);
		if (hiPriChild >= size()) return;
		int rChildIndex = rightChildIndex(index);
		if (rChildIndex < size() && comesBefore(rChildIndex, hiPriChild)) {
			hiPriChild = rChildIndex;
		}
		if (comesBefore(index, hiPriChild)) return;
		swap(index, hiPriChild);
		rotateDown(hiPriChild);
	}

	/** Returns true iff parent is less than or equal to child (they are in order). */
	private boolean comesBefore(int parent, int child) {
		return itemInfoMap.get(super.get(parent)).priority <= itemInfoMap.get(super.get(child)).priority;
	}

	/** Swaps the items at index a and b. */
	private void swap(int a, int b) {
		T temp = super.get(a);
		setAt(a, super.get(b));
		setAt(b, temp);
	}
	
	/** Sets the item at index to item, and remaps item to index in the indices map. */
	private void setAt(int index, T item) {
		super.set(index, item);
		itemInfoMap.get(item).index = index;
	}

	/** Returns the index of the left child of the item at index i. */
	private int leftChildIndex(int i) {
		return 2 * i + 1;
	}
	
	/** Returns the index of the right child of the item at index i. */
	private int rightChildIndex(int i) {
		return 2 * i + 2;
	}

	/** Returns the index of the parent of the item at index i. */
	private int parentIndex(int i) {
		return (i - 1) / 2;
	}
	
	private static class ItemInfo {
		private double priority;
		private int index;
		
		private ItemInfo(double p, int i) {
			priority = p;
			index = i;
		}
	}
}
