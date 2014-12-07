package game;
import gui.GUI;
import gui.TextIO;

import java.io.*;
import java.util.*;

import org.json.JSONObject;

/** Class Game is the controlling class for the ShippingGame Project. It manages the go-between
 * between the board, which maintains the state of the game, and the gui, which visually displays it.
 * Finally, the class maintains the managers, i.e. the user-written classes that fill in the 
 * truck's missing behavior
 * @author MPatashnik
 *
 */
public class Game {

    /** The directory that contains the map. */
	public static final String MAP_DIRECTORY = "data/Maps/";
	
	/** The extension of the maps' file name --usually .txt . */
	public static final String MAP_EXTENSION = ".txt";

	private File file;	//The file from which this game was loaded. Null if none.
	private String managerClass; //The name of the class from which the manager was created.

	private int frame; // Duration of a frame for this game, in ms. 
	                   // A higher value causes trucks to move slower.
	
	private boolean frameAltered; //True if at any point in running, this game's frame rate
								  //Is anything other than DEFAULT_FRAME.
	private GUI gui;
	private Manager manager;
	private ThreadGroup gameThreads;	//The Truck and Manager threads that are running

	private boolean running;  //True if the game is currently in progress
	private boolean finished; //True if the game is over
	private Board board;      //The board for this game

	protected Throwable throwable;	//The throwable that has been thrown and not caught by this game, if any
	protected Thread monitoringThread;	//The thread that is monitoring this Game - null if none

	/** The default frame value. Other frame values can be used for testing,
	 * but only games run with this frame value are fair for scoring.
	 */
	public static final int DEFAULT_FRAME = 40;
		
	/** Set the manager to managerClassname, make game not running, not finished, with no
	 * gui and a new GameThreadGroup. */
	private Game(String managerClassname) {
		setManager(managerClassname);
		manager.setGame(this);
		running = false;
		finished = false;
		gui = null;
		gameThreads = new GameThreadGroup();
		frame = DEFAULT_FRAME;
	}

	/** Constructor: a game instance with a set Board that is read from File f, using
	 * the manager whose class name is managerClassname. Uses Default for all other fields */
	public Game(String managerClassname, File f) {
		this(managerClassname);
		file = f;
		try {
			JSONObject obj = new JSONObject(TextIO.read(f));
			board = new Board(this, obj);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	/** Constructor: a game instance with a set Board that is read from File f, using
	 * the manager whose class name is managerClassname, and uses the given frame rate */
	public Game(String managerClassname, File f, int frame){
		this(managerClassname, f);
		setFrame(frame);
	}

	/** Constructor: a game instance with a random board from seed seed using
	 * the manager whose class name is managerClassname. */
	public Game(String managerClassname, long seed) {
		this(managerClassname);
		file = null;
		board = Board.randomBoard(this, seed);
	}
	
	/** Constructor: a game instance with a random board from seed seed using
	 * the manager whose class name is managerClassname, and uses the given frame rate. */
	public Game(String managerClassname, long seed, int frame){
		this(managerClassname, seed);
		setFrame(frame);
	}

	/** If the manager and managerClass are null, set the manager to the class whose
	 * name is managerClassname.
	 * Also set the game of the constructed manager to this.
	 * Return true iff the manager is set this way.
	 * 
	 */
	public boolean setManager(String managerClassname) {
		if (managerClass != null || manager != null)
			return false;
		managerClass = managerClassname;
		try {
			manager = (Manager)Main.createUserManager(managerClassname);
			manager.setGame(this);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/** Return the duration of a frame for this game, in milliseconds */
	public int getFrame(){
		return frame;
	}
	
	/** Set the duration of a frame for this game, in milliseconds.
	 * If this value of f is new and the game is currently running,
	 * trips the frameAltered flag. 
	 * A lower value means a faster game -- more penalty for computation.
     * A higher value means a slower game -- less penalty for computation.
     * 
	 * @throws IllegalArgumentException if f <= 0
	 */
	public void setFrame(int f) throws IllegalArgumentException {
		if(f <= 0) throw new IllegalArgumentException("Can't set frame rate for " + this + " to " + f);
		if(isRunning() && (f != DEFAULT_FRAME)) frameAltered = true;
		frame = f;
	}

	/** Return true iff the frame rate of this game was altered in 
	 * such a way that changed the running of the game
	 */
	public boolean isFrameAltered(){
		return frameAltered;
	}
	
	/** Return true iff this game is currently running (in progress, not completed). */
	public boolean isRunning() {
		return running;
	}

	/** Return true iff this game is finished. */
	public boolean isFinished() {
		return finished;
	}

	/** Set the value of running to r. Also inform the gui of changes. */
	protected void setRunning(boolean r) {
		running = r;
		if (gui != null) gui.updateRunning();
	}

	/** Set the value of finished to f. Also inform the gui of changes. */
	protected void setFinished(boolean f) {
		finished = f;
		if (gui != null) gui.updateRunning();
	}

	/** Return the board of this game.
	 *  @see Board for what information is contained therein. */
	public Board getBoard() {
		return board;
	}

	/** Return the manager for this Game. */
	public Manager getManager() {
		return manager;
	}

	/** Return a String of the class name used to load the manager. */
	public String getManagerClassname() {
		return managerClass;
	}

	/** Return the file from which this game was created
	 * (null if created randomly and thus not loaded). */
	public File getFile() {
		return file;
	}

	/** Return the seed from which this game was generated from (-1 if this game
	 * was loaded from a non-randomly generated file.) */
	public long getSeed() {
		return board.seed;
	}

	/** Start the game by the manager run; then have each truck begin running.
	 * Additional calls to this method after the first call do nothing. */
	public void start() {
		if (!running && !finished) {
			if (frame != DEFAULT_FRAME) frameAltered = true;

			setRunning(true);			

			Thread m = new Thread(gameThreads, manager);
			manager.setThread(m);
			m.start();

			for (Truck t : board.getTrucks()) {
				Thread th = new Thread(gameThreads, t);
				t.setThread(th);
				th.start();
			}
		}
	}

	/** Return the exception thrown during the running of this game.
	 * (null if that hasn't happened).
	 */
	public Throwable getThrownThrowable() {
		return throwable;
	}

	/** Return the GUI that represents this game. */
	public GUI getGUI() {
		return gui;
	}

	/** Set the GUI that draws this game to g.
	 * Students: don't call this */
	public void setGUI(GUI g) {
		gui = g;
	}

	/** Return the current update message shown on the GUI. */
	public String getUpdateMessage() {
		return gui.getUpdateMessage();
	}

	/** Update the GUI to show s as an update message for a few seconds. */
	public void setUpdateMessage(String s) {
		gui.setUpdateMessage(s);
	}

	/** Return the parcel stats for the current game in the form
	 * [on city, on truck, delivered] */
	public int[] parcelStats() {
		int[] pArr = new int[3];
		Set<Parcel> parcels = getBoard().getParcels();
		synchronized(parcels) {
			for (Parcel p : parcels) {
				if (p.isHeld())
					pArr[1]++;
				else
					pArr[0]++;
			}
		}
		pArr[2] = getBoard().initialParcelCount - pArr[0] - pArr[1];
		return pArr;
	}

	/** Return the truck stats for the current game in the form
	 * [waiting, traveling, getting manager input] */
	public int[] truckStats() {
		int[] tArr = new int[3];
		ArrayList<Truck> trucks = getBoard().getTrucks();
		synchronized(trucks) {
			for (Truck t : trucks) {
				if (t.isWaitingForManager())
					tArr[2]++;
				else if (t.getStatus().equals(Truck.Status.WAITING))
					tArr[0]++;
				else
					tArr[1]++;
			}
		}
		return tArr;
	}

	/** End this game prematurely by halting trucks and manager.
	 * This will interrupt the manager and truck threads. */
	public void kill() {
		halt(false);
	}

	/** End this game correctly when the last parcel is delivered. */
	protected void finish() {
		halt(true);
	}

	/** Call to end the game. Correct game ending if {@code gameActuallyOver},
	 * premature halting otherwise.
	 * Additional calls to this method won't do anything more.
	 */
	private void halt(boolean gameActuallyOver) {
		boolean wasRunning = running;
		if (!finished) {
			setRunning(false);	
			setFinished(true);
			if (gui != null) {
				if (wasRunning) {
					if (gameActuallyOver)
						gui.setUpdateMessage("Game Finished!");
					else
						gui.setUpdateMessage("Game Halted.");
					gui.repaint();
				}
			}

			for (Truck t : board.getTrucks()) {
				t.gameOver();
			}

			manager.gameOver();
		}
	}

	/** Return a file for the string board filename.
	 * Throw an IllegalArgumentException if this board does not exist. 
	 */
	public static File gameFile(String filename) throws IllegalArgumentException {
		//Check that filename ends with .txt. If not, strip off the bad extension(if any) and add .txt
		if (!filename.endsWith(MAP_EXTENSION)) {
			int i = filename.indexOf('.');
			if (i != -1)
				filename = filename.substring(0, i);
			filename += MAP_EXTENSION;
		}

		File f = new File(MAP_DIRECTORY + filename);
		if (!f.exists())
			throw new IllegalArgumentException("File " + f +
			        " for filename " + filename + " Does Not Exist.");
		return f;
	}

	/** Write this Game to a text file that can be loaded and re-played later.
	 * Can  be used only if the game has not yet started or finished.
	 * If either of these is true, throw a runtime exception.
	 * @throws IOException - If the file writing goes bad.
	 * @throws RuntimeException if the game is not in its pre-start state. */
	public void writeGame(String fileName) throws IOException, RuntimeException{
		if (isRunning() || isFinished())
			throw new RuntimeException("Can't Write Game File if the game is running.");

		String n = MAP_DIRECTORY + File.pathSeparator + fileName + ".txt";
		TextIO.write(n, board.toJSONString());
	}

	/** An extension of ThreadGroup to do custom uncaught error handling
	 * @author MPatashnik
	 */
	private class GameThreadGroup extends ThreadGroup {
		/** Constructor: a new GameThreadGroup (with name "Game Threads"). */
		GameThreadGroup() {
			super("Game Threads");
		}

		/** Called when a thread that is a member of this threadgroup
		 * throws an exception that is not caught.
		 * 
		 * Assuming a monitoring thread, 
		 * store the throwable e in the game and interrupt the monitoring thread.
		 * 
		 * Otherwise just use the superclass' version of uncaught exception - printing to console.
		 */
		@Override
		public void uncaughtException(Thread t, Throwable e) {
			if (monitoringThread != null) {
				throwable = e;
				monitoringThread.interrupt();
			} else {
				super.uncaughtException(t, e);
			}
		}

	}
}
