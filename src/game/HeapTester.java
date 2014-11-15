package game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

/** This class provides method main to test an implementation of game.Min_Heap.
 * It prints its results on the console.
 * The implementation must be in package student.<br><br>
 * 
 * Element args[0] of the parameter to main must be the name of the class.
 * So, Run -> Run Configurations, click pane "Arguments", and type into
 * the Program Aruments field the name or your class that implements
 * game.Min_Heap.  <br><br>
 * 
 * Additional arguments are possible.<br>
     *      -d Print additional debugging information<br>
     *      -p [n] Set the max heap size for polling to n<br>
     *      -s [n] Set the max heap size for update to n<br>
     *      -u [n] Set the number of priorities to update to n<br>
     * Print results of tests on console
 * 
 * 
 *@author davidgries
 */
public class HeapTester{

    public static boolean giveDebugOutput= false;
    public static int maxHeapSizeForPolling= 1000; 
    public static int maxHeapSizeForUpdate= 30;
    public static int prioritiesToUpdate= 6;

    static String userClassName;

    /** Handle program arguments */
    private static void handleArgs(String[] args){
    	int i = 1;
    	while (i < args.length){
    		switch(args[i]){
    		case "-d":
    			giveDebugOutput = true;
    			break;
    		case "-p":
    			if(i + 1 >= args.length){
    				break;
    			}
    			maxHeapSizeForPolling = Integer.parseInt(args[++i]);
    			break;
    			case "-s":
        			if(i + 1 >= args.length){
        				break;
        			}
        			maxHeapSizeForUpdate = Integer.parseInt(args[++i]);
        			break;
        		case "-u":
        			if(i + 1 >= args.length){
        				break;
        			}
        			prioritiesToUpdate = Integer.parseInt(args[++i]);
        			break;
    			}
    			i++;
    		}
    	}
    
    /** Test the user-defined heap. args[0] should be the name of the implementing class.
     * Additional arguments:
     *      -d Print additional debugging information
     *      -p [n] Set the max heap size for polling to n
     *      -s [n] Set the max heap size for update to n
     *      -u [n] Set the number of priorities to update to n
     * Print results of tests on console.
     */
    public static void main(String[] args) {
        userClassName= args[0];
        handleArgs(args);
        System.out.println("Testing heap adding " + (testAdding() ? " Ok" : " err"));
        System.out.println("Testing polling order " + (testPollingOrder() ? " Ok" : " err"));
        System.out.println("Testing update priority " + (testUpdatePriority() ? " Ok" : " err"));
    }

    /** Create and return an instance of the user-defined manager class, 
     * named userHeapClass. */
    @SuppressWarnings("unchecked")
    private static MinHeap<Thingy> createUserManager(String userHeapClass) {
        try {
            String heapClass = "";
            if (userHeapClass.startsWith("<s>"))
                heapClass= "solution." + userHeapClass.substring(4);
            else
                heapClass= "student." + userHeapClass;

            @SuppressWarnings("rawtypes")
            Class c= Class.forName(heapClass);
            if (!MinHeap.class.isAssignableFrom(c))
                throw new IllegalArgumentException("Class " + heapClass + 
                        " Does not Extend Heap Class");

            return (MinHeap<Thingy>) c.newInstance();//OK because default constructor
            // is only constructor that should be used.
        } catch (ClassNotFoundException | InstantiationException |
                IllegalAccessException | IllegalArgumentException e){
            throw new RuntimeException("One of the following exceptions occurred:" +
                    "ClassNotFound, nstantiation, IllegalAccess, IllegalArgumentException");
        }
    }

    /** Objects of this class are placed in the heap, using their dist values
     * for comparison. The field is private but it can be referenced in HeapTester.
     * Note also that there is a field priority, which is also set to dist.
     * In testing method updatePriority, that field is set to the new updated
     * priority so that the update of priority can be tested. */
    private static class Thingy implements Comparable<Thingy>{
        private int dist;
        private double priority;

        /** Constructor: an instance with dist d. */
        public Thingy(int d) {
            dist= d;
            priority= d;
        }

        /** Return neg number, 0, pos number depending on whether this is smaller
         * equal to, or greater than ob. */
        public @Override int compareTo(Thingy ob) {
            return dist - ob.dist;
        }

        /** Return true iff ob is a Thingy with the same value in its dist field. */
        public @Override boolean equals(Object ob) {
            if (!(ob instanceof Thingy)) {
                return false;
            }
            return dist == ((Thingy)ob).dist;
        }

        /** The hashcode is MAX_VALUE - dist. */
        public @Override int hashCode() {
            return Integer.MAX_VALUE - dist;
        }

        /** = a representation of this Thingy - just the dist.*/
        public String toString() {
            return "" + dist;
        }

        /** = a representation of this Thingy - dist and priority.*/
        public String toString1() {
            return "" + dist + ":" + priority;
        }

    }

    /** Test that adding distinct values get added.
     * Also test functions isEmpty() and size(). */
    public static boolean testAdding()  {
        System.out.println("Start of adding test. adding integers 0..7, in order.");
        MinHeap<Thingy> heap= createUserManager(userClassName);

        if (!heap.isEmpty()) {
            System.out.println("Heap should be empty but isn't.");
            return false;
        }

        Thingy[] t = {new Thingy(1), new Thingy(2), new Thingy(3), new Thingy(4),
                new Thingy(5), new Thingy(6), new Thingy(7)};
        for (int i = 0; i < t.length; i++) {
            heap.add(t[i], t[i].dist);
            if (heap.isEmpty()) {
                System.out.println("Heap should not be empty but is empty.");
                return false;
            }
            if (heap.size() != i+1) {
                System.out.println("Heap size is " + heap.size() +  "but should be " + 
                        (i+1));
                return false;
            }
        }

        if (!heap.toString().equals("[1:1.0, 2:2.0, 3:3.0, 4:4.0, 5:5.0, 6:6.0, 7:7.0]")) {
            System.out.println("heap.toString() should be " + 
                    "\n[1:1.0, 2:2.0, 3:3.0, 4:4.0, 5:5.0, 6:6.0, 7:7.0] but is\n" +
                    heap.toString());
        }

        // Try a different order of adding Thingies. */
        t = new Thingy[]{new Thingy(2), new Thingy(3), new Thingy(7), new Thingy(1),
                new Thingy(4), new Thingy(5), new Thingy(6)};

        heap = createUserManager(userClassName);
        for (int i = 0; i < t.length; i= i+1) {
            heap.add(t[i], t[i].dist);
        }
        if (giveDebugOutput) {
            System.out.println("heap should be: " + "[1:1.0, 2:2.0, 5:5.0, 3:3.0, 4:4.0, 7:7.0, 6:6.0]");
            System.out.println("heap is       : " + heap.toString());
         }
        return true;
    }

    /** Return true iff values removed from a heap are in increasing order. */
    public static boolean testPollingOrder() {
        MinHeap<Thingy> heap= createUserManager(userClassName);
        ArrayList<Thingy> arrayList= new ArrayList<Thingy>();

        // inv: heap and arrayList contain the same values, all random
        for (int i= 0; i < maxHeapSizeForPolling; i++) {
            int dist= (int)(Math.random() * 10000);
            try {
                Thingy t= new Thingy(dist);
                heap.add(t, dist);
                arrayList.add(t);
            } catch (IllegalArgumentException e) {
                // the caught illegal arg exception is because we tried to
                // add the same random value to the heap. Just disregard it.
            }
        }
        System.out.println("Tested polling order with a heap of " + heap.size() +
                " random values.");
        // Move values in arrayList to array and sort array.
        Thingy[] array= new Thingy[arrayList.size()];
        array= arrayList.toArray(array);
        Arrays.sort(array);

        // If the elements polled from the heap are not the same as those in array,
        // give a message and return false.
        for (Thingy t : array) {
            Thingy next= heap.poll();
            if (!t.equals(next)) {
                System.out.println("Polled values did not come out in the right order.");
                return false;
            }
        }
        return true;
    }

    /** Return true iff updating priorities functions correctly. */
    public static boolean testUpdatePriority() {
        final Comparator<Thingy> comp= new Comparator<Thingy>() {
            /** Return neg, 0, or pos depending on whether t1's priority <, = 
             * or > t2's priority. */
            public @Override int compare(Thingy t1, Thingy t2) {
                return (int)(t1.priority - t2.priority);
            }
        };

        ArrayList<Thingy> arrayList= new ArrayList<Thingy>();
        MinHeap<Thingy> heap= createUserManager(userClassName); 

        //Add a bunch of elements to both arrayList and heap.
        for (int i= 0; i < maxHeapSizeForUpdate; i++) {
            int dist= (int)(Math.random() * 1000);
            try {Thingy t= new Thingy(dist);
            heap.add(t, dist);
            arrayList.add(t);
            } catch (IllegalArgumentException e){
                // the caught illegal arg exception is because we tried to
                // add the same random value to the heap. Just disregard it.
            }
        }

        //Change some priorities. Each iteration, an index is calculated
        // and the two elements in the array list at index and index+1 have
        // their priorities changed. The first has its priority negated (or 20
        // 60 subtract if it is already negative); the
        // second has 20 subtracted from it. To get the same effect in the
        // the array list, the priority of the Thingy is set to the new priority,
        // and, later, the array list is sorted on the priority instead of dist.
        for (int i = 0; i < prioritiesToUpdate; i++) {
            int index= (int)Math.max(0, (Math.random())*arrayList.size()-1);
            Thingy t= arrayList.get(index);
            if (t.priority >= 0) {
                heap.updatePriority(t, -t.priority);
                t.priority= -t.priority;
            } else {
                heap.updatePriority(t, t.priority+60);
                t.priority= t.priority+60;
            }
            t= arrayList.get(index+1);
            heap.updatePriority(t, t.priority - 20);
            t.priority= t.priority - 20;
        }
        Collections.sort(arrayList, comp);
        if (giveDebugOutput) {
            System.out.println("Testing update. heap is " + heap);
            String res= "[";
            for (Thingy t : arrayList) {
                if (res.length() > 1) res= res + ", ";
                res= res + t.toString1();
            }
            System.out.println("       and arrayList is " + res + "]");
        }

        //See if everything leaves in the same order
        for (Thingy t : arrayList) {
            Thingy t2 = heap.poll();
            if (t != t2) return false;
        }

        return true;
    }
}
