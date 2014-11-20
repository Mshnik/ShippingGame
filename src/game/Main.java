package game;

import gui.GUI;

import java.util.*;
import java.util.concurrent.Semaphore;

/** Game starting methods. Also serves as a util holder */
public class Main {

	/** Student directory */
	public static final String studentDirectory = "student";
	
	/** Read args for a string of a ClassName of the Manager to create, then create
	 * an instance of that manager class, create the game and the threads, and start the game.
	 * @param args - a 1x... array containing the name of the class of the user wants as the Manager
	 * 				first argument is manager name, other args are flags.
	 *              If null or empty, uses {"MyManager"} as a 1x1 array of args.
	 * @throws IllegalArgumentException if args is null or has length 0.
	 */
	public static void main(String[] args) throws IllegalArgumentException {
		//Add initial elements to fibCalc
		fibCalc.add(0);
		fibCalc.add(1);
		
		if (args == null || args.length < 1)
			args = new String[]{"MyManager"};

		String userManagerClass = studentDirectory + "." +args[0];
		
		if (args[0].startsWith("<s>")) {
			userManagerClass = "solution."+args[0].substring(args[0].indexOf('>') + 1);
		}
		
		//Check if running in gamerunner mode.
		//just one argument - not gamerunner mode (default gui mode. No headless option).
		//multiple - gamerunner mode (may or may not be headless).
		if (args.length > 1) {
			ArrayList<String> argsList = new ArrayList<String>();
			for (int i = 0; i < args.length; i++) {
				argsList.add(args[i]);
			}
			boolean headless = false;
			if (argsList.contains("-h")) headless = true;
			
			GameRunner gr = new GameRunner(userManagerClass, ! headless, true);
			
			if ((args[1].equals("-r") && ! headless) || (args[2].equals("-r") && headless)) {
				int n = -1;
				try {
					if (headless) n = Integer.parseInt(args[3]);
					else n = Integer.parseInt(args[2]);
				} catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
					throw new IllegalArgumentException("Illegal String Array passed into Args:\n" +
							"For headed random mode, expecting\n:" +
							"\t <userManagerClass> -r <numberOfSeeds>.\n" +
							"For headless random mode, expecting\n:" +
							"\t <userManagerClass> -h -r <numberOfSeeds>.\n" +
							"Number of seeds should be an int.\n" +
							"recieved " + args + " of length " + (args == null ? "null" : args.length));
				}
				gr.runRandom(n);	
			} else {
				long[] seeds = null;
				if (headless) seeds = new long[args.length - 2];
				else seeds = new long[args.length - 1];
				try {
					int headlessOffset = headless ? 2 : 1;
					for (int i = 0; i < seeds.length; i++) {
						seeds[i] = Long.parseLong(args[i+headlessOffset]);
					}
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException("Illegal String Array passed into Args:\n" +
							"For headed mode, expecting:\n" +
							"\t <userManagerClass> <seed1> <seed2> <seed3> ...\n" +
							"For headless mode, expecting:\n" +
							"\t <userManagerClass> -h <seed1> <seed2> <seed3> ...\n" +
							"Each seed should be a long.\n" +
							"recieved " + args + " of length " + (args == null ? "null" : args.length));
				}
				gr.runSeeds(seeds);
			}
		} else {
			Game g = new Game(userManagerClass, Math.abs((new Random()).nextLong()));
			new GUI(g);
		}
	}

	/** Create and return an instance of the user-defined manager class
	 * @param userManagerClass - the String Name of the class to define
	 * @return - An instance of the user defined class
	 * @throws ClassNotFoundException - If the string class is not found
	 * @throws InstantiationException - If there is an error in creating the instance
	 * @throws IllegalAccessException - If the constructor for the class is not visible
	 * @throws IllegalArgumentException - If the given class is not a subclass of Manager.
	 */
	public static Object createUserManager(String userManagerClass)
	        throws ClassNotFoundException, InstantiationException, IllegalAccessException,
	        IllegalArgumentException{
		@SuppressWarnings("rawtypes")
		Class c= Class.forName(userManagerClass);
		if (!Manager.class.isAssignableFrom(c))
			throw new IllegalArgumentException("Class " + userManagerClass + 
			        " Does not Extend Manager Class");

		return c.newInstance();//OK because default constructor is only constructor that should be used.
	}

	/** Return the sum of the natural numbers in 0..i, recursively!
	 * (mathematicially, that's 0 if i < 0) */
	public static int sumTo(int i) {
		if (i < 0) return 0;
		return sumToHelper(i,0);
	}

	/** Helper for sumTo method such that it's tail recursive */
	private static int sumToHelper(int i, int s) {
		if (i == 0) return s;
		return sumToHelper(i-1, s+i);
	}

	/** Part of fib series calculated thus far. Bit of memoization for speed. */
	private static ArrayList<Integer> fibCalc = new ArrayList<Integer>();

	/** Lock that ensures that fibCalc arrayList is added to/accessed correctly. */
	private static Semaphore fibLock = new Semaphore(1);

	/** Return fibonachi number i (0 indexed), starting with 0,1,1,2 ... 
	 * Returns -1 if i is negative or if the calling thread is interrupted */
	public static int fib(int i) {
		try {
			fibLock.acquire();
		} catch (InterruptedException e) {
			return -1;
		}
		if (i < 0) {
			fibLock.release();
			return -1;
		}
		else if (i < fibCalc.size()) {
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

	/** Return s with quotes added around it.
	 * Used in JSON creation methods throughout project. */
	public static String addQuotes(String s) {
		return "\"" + s + "\"";
	}

	/** Return a random element of elms (null if elms is empty).
	 * Synchronizes on {@code elms} to prevent concurrent modification. */
	public static <T> T randomElement(Collection<T> elms) {
		T val = null;
		synchronized(elms) {
			if (elms.isEmpty())
				return null;
			Iterator<T> it = elms.iterator();
			for (int i = 0; i < (int)(Math.random() * elms.size()) + 1; i++) {
				val = it.next();
			}
		}
		return val;
	}

}
