package game;
import gui.GUI;
import gui.TextIO;

import java.io.File;
import java.io.IOException;

import org.json.JSONObject;

/** The Game Class is the controlling class for the ShippingGame Project. It manages the go-between
 * between the board, which stores the state of the game, and the gui, which visually displays it.
 * Finally, the game class maintains the managers, the user written classes that fill in the 
 * truck's missing behavior
 * @author MPatashnik
 *
 */
public class Game{

	public static final String MAP_DIRECTORY = "Maps/";
	public static final String MAP_EXTENSION = ".txt";

	private File file;	//The file this game was loaded from. Null if none.
	private String managerClass; //The name of the class the manager was created from.

	private GUI gui;
	private Manager manager;

	private boolean running;  //True if the game is currently in progress
	private boolean finished; //True if the game is over
	private Board board;      //The board for this game
	
	/** Creates a game instance with a set Board, read from File f. Uses Default for all other fields */
	public Game(String managerClassname, File f){
		file = f;
		setManager(managerClassname);
		manager.setGame(this);
		running = false;
		finished = false;
		gui = null;
		try {
			JSONObject obj = new JSONObject(TextIO.read(f));
			board = new Board(this, obj);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	/** Creates a game instance with a random board from the given seed, and the given managerClass */
	public Game(String managerClassname, long seed){
		file = null;
		setManager(managerClassname);
		manager.setGame(this);
		running = false;
		finished = false;
		gui = null;
		board = Board.randomBoard(this, seed);
	}

	/** Allows for setting a manager post construction - only permissible if manager
	 * and mangerClassname are currently null.
	 * Also sets the game of the constructed manager to this.
	 * Returns true if the manager is set this way, false otherwise.
	 * 
	 */
	public boolean setManager(String managerClassname){
		if(managerClass != null || manager != null)
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

	/** Returns true if this game is currently running (in progress, not completed), 
	 * false otherwise */
	public boolean isRunning(){
		return running;
	}
	
	/** Returns true if this game is finished, false otherwise */
	public boolean isFinished(){
		return finished;
	}

	/** Sets the value of running. Also informs gui of changes */
	protected void setRunning(boolean r){
		running = r;
		gui.updateRunning();
	}

	/** Sets the value of finished. Also informs gui of changes */
	protected void setFinished(boolean f){
		finished = f;
		gui.updateRunning();
	}

	/** Returns the board of this game, @see Board for what information is contained therein */
	public Board getBoard(){
		return board;
	}

	/** Returns the manager for this Game. */
	public Manager getManager(){
		return manager;
	}

	/** Returns a String of the class used to load the manager */
	public String getManagerClassname(){
		return managerClass;
	}

	/** Returns the file this game was created from. Returns null if this was created (randomly), 
	 * and thus not loaded */
	public File getFile(){
		return file;
	}
	
	/** Returns the seed this game was generated from. Returns -1 if this game was loaded from
	 * a non-randomly generated file.
	 */
	public long getSeed(){
		return board.seed;
	}

	/** Starts the game by the manager run, then having each truck begin running.
	 * Additional calls to this method after the first call do nothing. */
	public void start(){
		if(! running && !finished){
			setRunning(true);
			
			Thread m = new Thread(manager);
			manager.setThread(m);
			m.start();
			
			for(Truck t : board.getTrucks()){
				Thread th = new Thread(t);
				t.setThread(th);
				th.start();
			}

			
		}
	}

	/** Returns the GUI that represents this game */
	public GUI getGUI(){
		return gui;
	}

	/** Sets the GUI that draws this game to {@code g}.
	 * Students - don't call this */
	public void setGUI(GUI g){
		gui = g;
	}

	/** Returns the current update message shown on the GUI */
	public String getUpdateMessage(){
		return gui.getUpdateMessage();
	}

	/** Updates the GUI to show the given String as an update message for a few seconds */
	public void setUpdateMessage(String newUpdate){
		gui.setUpdateMessage(newUpdate);
	}

	/** Ends this game prematurely by halting trucks and manager.
	 * This will interrupt the manager and truck threads. */
	public void kill(){
		halt(false);
	}
	
	/** Ends this game correctly when the last parcel is delivered. */
	protected void finish(){
		halt(true);
	}
	
	/** Call to end the game. Correct game ending if {@code gameActuallyOver},
	 * premature halting otherwise.
	 * Multiple calls to this method won't do anything additional.
	 */
	private void halt(boolean gameActuallyOver){
		if(! finished){
			setRunning(false);	
			setFinished(true);
			if(gameActuallyOver)
				gui.setUpdateMessage("Game Finished!");
			else
				gui.setUpdateMessage("Game Halted.");
			gui.repaint();
			
			for(Truck t : board.getTrucks()){
				t.gameOver();
			}
	
			manager.gameOver();
		}
	}

	/** Returns a file for the string board filename. Throws an IllegalArgumentException
	 * if this board does not exist. 
	 */
	public static File gameFile(String filename) throws IllegalArgumentException{
		//Check that filename ends with .txt. If not, strip off the bad extension(if any) and add .txt
		if(! filename.endsWith(MAP_EXTENSION)){
			int i = filename.indexOf('.');
			if(i != -1)
				filename = filename.substring(0, i);
			filename += MAP_EXTENSION;
		}

		File f = new File(MAP_DIRECTORY + filename);
		if(! f.exists())
			throw new IllegalArgumentException("File " + f + " for filename " + filename + " Does Not Exist.");

		return f;
	}

	/** Writes this Game to a text file that can be loaded and re-played later.
	 * Can only be used if the game has not yet started or finished.
	 * If either of these is true, throws a runtime exception
	 * @throws IOException - If the file writing goes bad.
	 * @throws RuntimeException if the game is not in its pre-start state when this method is called */
	public void writeGame(String fileName) throws IOException, RuntimeException{
		if(isRunning() || isFinished())
			throw new RuntimeException("Can't Write Game File if the game is running.");

		String n = MAP_DIRECTORY + fileName + ".txt";
		TextIO.write(n, board.toJSONString());
	}
}