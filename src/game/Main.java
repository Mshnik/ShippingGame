package game;

import gui.GUI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.Semaphore;

/** Game starting methods. Also serves as a util holder */
public class Main {
	
	/** False if this code is for personal use and testing, true if for academic use in CS2110 */
	private static final boolean IS_CLASS_RELEASE = false;

	/** Reads args for a string of a ClassName of the Manager to create, then creates
	 * an instance of that manager class, creates the game and the threads, and starts the game.
	 * @param args - a 1x... array containing the name of the class of the user wants as the Manager
	 * 				first argument is manager name, other args are flags.
	 * @throws IllegalArgumentException if args is null or has length 0.
	 */
	public static void main(String[] args) throws IllegalArgumentException{
		//Add intital elements to fibCalc
		fibCalc.add(0);
		fibCalc.add(1);
		
		if(args == null || args.length < 1)
			throw new IllegalArgumentException("Illegal String Array Passed into Args:\n" +
					"expecting length at least 1 array with name of manager class as first element.\n" +
					"recieved " + args + " of length " + (args == null ? "null" : args.length));

		String userManagerClass = "student."+args[0];
		
		if(args[0].startsWith("<s>") && ! IS_CLASS_RELEASE){
			userManagerClass = "solution."+args[0].substring(args[0].indexOf('>') + 1);
		}
		
		//Check if running on gamerunner mode.
		//just one argument - not gamerunner mode (default gui mode. No headless option).
		//multiple - gamerunner mode (may or may not be headless).
		if(args.length > 1){
			ArrayList<String> argsList = new ArrayList<String>();
			for(int i = 0; i < args.length; i++){
				argsList.add(args[i]);
			}
			boolean headless = false;
			if(argsList.contains("-h")) headless = true;
			
			GameRunner gr = new GameRunner(userManagerClass, ! headless);
			
			if((args[1].equals("-r") && ! headless) || (args[2].equals("-r") && headless)){
				int n = -1;
				try{
					if(headless) n = Integer.parseInt(args[3]);
					else n = Integer.parseInt(args[2]);
				}catch(NumberFormatException | ArrayIndexOutOfBoundsException e){
					throw new IllegalArgumentException("Illegal String Array passed into Args:\n" +
							"For headed random mode, expecting\n:" +
							"\t <userManagerClass> -r <numberOfSeeds>.\n" +
							"For headless random mode, expecting\n:" +
							"\t <userManagerClass> -h -r <numberOfSeeds>.\n" +
							"Number of seeds should be an int.\n" +
							"recieved " + args + " of length " + (args == null ? "null" : args.length));
				}
				gr.runRandom(n, true);	
			} else{
				long[] seeds = null;
				if(headless) seeds = new long[args.length - 2];
				else seeds = new long[args.length - 1];
				try{
					int headlessOffset = headless ? 2 : 1;
					for(int i = 0; i < seeds.length; i++){
						seeds[i] = Long.parseLong(args[i+headlessOffset]);
					}
				}catch(NumberFormatException e){
					throw new IllegalArgumentException("Illegal String Array passed into Args:\n" +
							"For headed mode, expecting:\n" +
							"\t <userManagerClass> <seed1> <seed2> <seed3> ...\n" +
							"For headless mode, expecting:\n" +
							"\t <userManagerClass> -h <seed1> <seed2> <seed3> ...\n" +
							"Each seed should be a long.\n" +
							"recieved " + args + " of length " + (args == null ? "null" : args.length));
				}
				gr.runSeeds(seeds, true);
			}
		} else{
			Game g = new Game(userManagerClass, Math.abs((new Random()).nextLong()));
			new GUI(g);
		}
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

		return c.newInstance();//OK because default constructor is only constructor that should be used.
	}

	/** Returns the sum of the natural numbers from {@code 1} to {@code i}, recursively!
	 * Returns 0 for {@code i <= 0} */
	public static int sumTo(int i){
		if(i < 0)
			return 0;
		return sumToHelper(i,0);
	}

	/** Helper for sumTo method such that it's tail recursive */
	private static int sumToHelper(int i, int s){
		if(i == 0) return s;
		return sumToHelper(i-1, s+i);
	}

	/** Part of fib series calculated thus far. Bit of memoization for speed. */
	private static ArrayList<Integer> fibCalc = new ArrayList<Integer>();

	/** Lock that ensures that fibCalc arrayList is added to/accessed correctly. */
	private static Semaphore fibLock = new Semaphore(1);

	/** Returns the ith fibonachi number (0 indexed), starting with 0,1,1,2 ... 
	 * Returns -1 if given number is negative or if the calling thread is interrupted */
	public static int fib(int i){
		try {
			fibLock.acquire();
		} catch (InterruptedException e) {
			return -1;
		}
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
		try {
			fibLock.acquire();
		} catch (InterruptedException e) {
			return -1;
		}
		//Check that we're storing it in the correct place.
		if (fibCalc.size() == i)		
			fibCalc.add(f);
		fibLock.release();
		return f;
	}

	/** Returns the given string with quotes added around it.
	 * Used in JSON creation methods throughout project */
	public static String addQuotes(String s){
		return "\"" + s + "\"";
	}

	/** Returns a random element of the given collection.
	 * Returns null if elms is empty. */
	public static <T> T randomElement(Collection<T> elms){
		if(elms.isEmpty())
			return null;
		T val = null;
		synchronized(elms){
			Iterator<T> it = elms.iterator();
			for(int i = 0; i < (int)(Math.random() * elms.size()) + 1; i++){
				val = it.next();
			}
		}
		return val;
	}

}
