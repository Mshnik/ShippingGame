package gameFiles;

import gui.GUI;

import java.io.File;
import java.util.ArrayList;

/** Game starting methods. Also serves as a util holder */
public class Main {
	
	/** Prompts the User for a string of a ClassName of the Manager to create, then creates
	 * an instance of that manager class, creates the game and the threads, and starts the game.
	 * @param args - a 1x1 array containing the name of the class of the user wants as the Manager
	 * @throws IllegalArgumentException if args is null or has length 0.
	 */
	public static void main(String[] args) throws IllegalArgumentException{
		
		if(args == null || args.length == 0)
			throw new IllegalArgumentException("Illegal String Array Passed into Main");
		
		String userManagerClass = args[0];
		
		Manager m = null;
		try {
			m = (Manager) createUserManager(userManagerClass);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Game g = new Game(m);
		m.setGame(g);
		
		new GUI(g);
//		new GUI();
	}
	
	/** Creates and returns an instance of the user defined manager class
	 * @param userManagerClass - the String Name of the class to define
	 * @return - An instance of the user defined class
	 * @throws ClassNotFoundException - If the string class is not found
	 * @throws InstantiationException - If there is an error in creating the instance
	 * @throws IllegalAccessException - If the constructor for the class is not visible
	 * @throws IllegalArgumentException - If the given class is not a subclass of Manager.
	 */
	private static Object createUserManager(String userManagerClass) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException{
		@SuppressWarnings("rawtypes")
		Class c= Class.forName(userManagerClass);
		if(!Manager.class.isAssignableFrom(c))
			throw new IllegalArgumentException("Class " + userManagerClass + " Does not Extend Manager Class");
		
		return c.newInstance();//assuming you aren't worried about constructor .
	}
	
	/** Returns the sum of the natural numbers from 1 to n */
	public static int sumTo(int i){
		if(i <= 0)
			return 0;
		return i + sumTo(i-1);
	}
	
	//Part of fib series calculated thus far. Bit of memoization
	private static ArrayList<Integer> fibCalc;
	
	/** Returns the ith fibonachi number (0 indexed), starting with 0,1,1,2 ... Returns -1 if given number is negative */
	public static int fib(int i){
		if(fibCalc == null){
			fibCalc = new ArrayList<Integer>();
			fibCalc.add(0); //First number
			fibCalc.add(1); //Second number
		}
		if (i < 0)
			return -1;
		else if( i < fibCalc.size())
			return fibCalc.get(i);
		
		//Calculate next number
		int f = fib(i-2) + fib(i-1);
		//Check that we're storing it in the correct place.
		if (fibCalc.size() != i)
			throw new RuntimeException("Wut. Trying to calculate fib number " + i + " but precalced is " + fibCalc.toString());
		
		fibCalc.add(f);
		return f;
	}
	
	/** Returns the given string with quotes added around it */
	public static String addQuotes(String s){
		return "\"" + s + "\"";
	}
	
}
