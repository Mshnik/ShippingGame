package game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class HeapTester{

	/** Tests the user defined heap. args[0] should be the name of the implementing class.
	 * Prints results tests to console.
	 */
	public static void main(String[] args){
		System.out.println("Testing heap adding " + (testAdding() ? " Ok" : " err"));
		System.out.println("Testing polling order " + (testPollingOrder() ? " Ok" : " err"));
		System.out.println("Testing update priority " + (testUpdatePriority() ? " Ok" : " err"));

	}

	/** Creates and returns an instance of the user defined manager class
	 * @param userHeapClass - the String Name of the class to define
	 * @return - An instance of the user defined class
	 * @throws ClassNotFoundException - If the string class is not found
	 * @throws InstantiationException - If there is an error in creating the instance
	 * @throws IllegalAccessException - If the constructor for the class is not visible
	 * @throws IllegalArgumentException - If the given class is not a subclass of Manager.
	 */
	@SuppressWarnings("unchecked")
	private static MinHeap<Integer> createUserManager(String userHeapClass) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException{
		String heapClass = "";
		if(userHeapClass.startsWith("<s>"))
			heapClass = "solution." + userHeapClass.substring(4);
		else
			heapClass = "student." + userHeapClass;

		@SuppressWarnings("rawtypes")
		Class c= Class.forName(heapClass);
		if(!MinHeap.class.isAssignableFrom(c))
			throw new IllegalArgumentException("Class " + heapClass + " Does not Extend MinHeap Class");

		return (MinHeap<Integer>) c.newInstance();//OK because default constructor is only constructor that should be used.
	}

	private static class Thingy{
		private int dist;
		Thingy(int d){
			dist = d;
		}
	}

	/** Tests adding non-distinct values don't get added.
	 * Also tests the isEmpty() and size() functions. */
	public static boolean testAdding(){
		MinHeap<Thingy> heap = null; /////TODO

		Thingy[] t = {new Thingy(1), new Thingy(2), new Thingy(3), new Thingy(4), new Thingy(5)};
		if(! heap.isEmpty()) return false;

		for(int i = 0; i < t.length; i++){
			heap.add(t[i], t[i].dist);
			if(heap.isEmpty()) return false;
			if(heap.size() != i) return false;
		}
		
		for(int i = 0; i < t.length; i++){
			heap.add(t[i], t[i].dist);
			if(heap.isEmpty()) return false;
			if(heap.size() != t.length) return false;
		}
		return true;
	}

	/** Returns true if values removed from a heap are always in increasing order */
	public static boolean testPollingOrder() {
		MinHeap<Thingy> heap = null;/////TODO

		for (int i = 0; i < 100000; i++) {
			int dist = (int) (Math.random() * 1000);
			heap.add(new Thingy(dist), dist);
		}
		int lowest = Integer.MIN_VALUE;
		while (!heap.isEmpty()) {
			Thingy next = heap.poll();
			if(lowest > next.dist){
				return false;
			}
			lowest = next.dist;
		}
		return true;
	}

	/** Returns true if updating priorities functions correctly */
	public static boolean testUpdatePriority(){
		
		ArrayList<Thingy> arr = new ArrayList<Thingy>();
		
		final Comparator<Thingy> comp = new Comparator<Thingy>(){
			@Override
			public int compare(Thingy t1, Thingy t2){
				return t1.dist - t2.dist;
			}
		};
		
		MinHeap<Thingy> heap = null; //TODO
		
		//Add a buncha elements
		for (int i = 0; i < 100000; i++) {
			int dist = (int) (Math.random() * 1000);
			Thingy t = new Thingy(dist);
			heap.add(t, dist);
			arr.add(t);
		}
		
		//Change some priorities
		for(int i = 0; i < 1000; i++){
			Thingy t = arr.get((int)(Math.random())*arr.size());
			heap.updatePriority(t, (int)(Math.random()*1000));
		}
		
		Collections.sort(arr, comp);
		//See if everything leaves in the same order
		
		for(Thingy t : arr){
			Thingy t2 = heap.poll();
			if(t != t2) return false;
		}
		return true;
	}
}
