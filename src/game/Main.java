package game;

import gui.GUI;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

/** Game starting methods. Also serves as a util holder */
public class Main {
	
	/** Prompts the User for a string of a ClassName of the Manager to create, then creates
	 * an instance of that manager class, creates the game and the threads, and starts the game.
	 * @param args - a 1x1 array containing the name of the class of the user wants as the Manager
	 * @throws IllegalArgumentException if args is null or has length 0.
	 */
	public static void main(String[] args) throws IllegalArgumentException{
		
		if(args == null || args.length != 1)
			throw new IllegalArgumentException("Illegal String Array Passed into Main:\n" +
												"expecting length 1 array of name of manager class.\n" +
												"recieved " + args + " of length " + (args == null ? "null" : args.length));
		
		String userManagerClass = args[0];
		
		Game g = new Game(userManagerClass, Game.gameFile("JSONMap1.txt"));
		
		new GUI(g);
	}
	
	/** Creates and returns an instance of the user defined manager class
	 * @param userManagerClass - the String Name of the class to define
	 * @return - An instance of the user defined class
	 * @throws ClassNotFoundException - If the string class is not found
	 * @throws InstantiationException - If there is an error in creating the instance
	 * @throws IllegalAccessException - If the constructor for the class is not visible
	 * @throws IllegalArgumentException - If the given class is not a subclass of Manager.
	 */
	public static Object createUserManager(String userManagerClass) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException{
		@SuppressWarnings("rawtypes")
		Class c= Class.forName(userManagerClass);
		if(!Manager.class.isAssignableFrom(c))
			throw new IllegalArgumentException("Class " + userManagerClass + " Does not Extend Manager Class");
		
		return c.newInstance();//assuming you aren't worried about constructor .
	}
	
	/** Returns the sum of the natural numbers from 1 to i */
	public static int sumTo(int i){
		if(i <= 0)
			return 0;
		return i + sumTo(i-1);
	}
	
	//Part of fib series calculated thus far. Bit of memoization for speed.
	private static ArrayList<Integer> fibCalc;
	
	//Lock that ensures that fibCalc arrayList is added to/accessed correctly.
	private static Semaphore fibLock;
	
	/** Returns the ith fibonachi number (0 indexed), starting with 0,1,1,2 ... Returns -1 if given number is negative 
	 * @throws InterruptedException */
	public static int fib(int i) throws InterruptedException{
		if(fibLock == null || fibCalc == null){
			fibCalc = new ArrayList<Integer>();
			fibLock = new Semaphore(1);
			fibLock.acquire();
			fibCalc.add(0); //First number
			fibCalc.add(1); //Second number
			fibLock.release();
		}
		fibLock.acquire();
		if (i < 0){
			fibLock.release();
			return -1;
		}
		else if( i < fibCalc.size()){
			int k = fibCalc.get(i);
			fibLock.release();
			return k;
		}
		
		//Release the lock before recursive calls.
		fibLock.release();
		//Calculate next number
		int f = fib(i-2) + fib(i-1);
		
		//Acquire before checking size/adding.
		fibLock.acquire();
		//Check that we're storing it in the correct place.
		if (fibCalc.size() != i)
			throw new RuntimeException("Wut. Trying to calculate fib number " + i + " but precalced is " + fibCalc.toString());
		
		fibCalc.add(f);
		fibLock.release();
		return f;
	}
	
	/** Returns the given string with quotes added around it */
	public static String addQuotes(String s){
		return "\"" + s + "\"";
	}
	
}
