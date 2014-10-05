package game;
import gui.GUI;
import gui.TextIO;

import java.io.File;
import java.io.IOException;

import org.json.JSONObject;

/** The Game Class is the controlling class for the ShippingGame Project. It manages the go-between
 * between the map, which stores the state of the game, and the gui, which visually displays it.
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

	private boolean running; //True if the game is currently in progress
	private boolean finished; //True if the game is over
	private Map map;

	/** Creates a game instance with a set Map, read from File f. Uses Default for all other fields */
	public Game(String managerClassname, File f){
		file = f;
		setManager(managerClassname);
		manager.setGame(this);
		running = false;
		finished = false;
		gui = null;
		try {
			JSONObject obj = new JSONObject(TextIO.read(f));
			map = new Map(this, obj);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	/** Creates a game instance with a random map, and the given managerClass */
	public Game(String managerClassname, long seed){
		file = null;
		setManager(managerClassname);
		manager.setGame(this);
		running = false;
		finished = false;
		gui = null;
		map = Map.randomMap(this, seed);
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

	/** Sets the value of finished. */
	protected void setFinished(boolean f){
		finished = f;
	}
	
	/** Sets the map to map m */
	protected void setMap(Map m){
		map = m;
	}

	/** Returns the map of this game, including all of the nodes and all of the edges */
	public Map getMap(){
		return map;
	}

	/** Returns the manager for this Game */
	public Manager getManager(){
		return manager;
	}

	/** Returns a String of the class used to load the manager */
	public String getManagerClassname(){
		return managerClass;
	}

	/** Returns the file this game was created from. Returns null if this was created (randomly), 
	 * not loaded */
	public File getFile(){
		return file;
	}

	/** Starts the game by having each truck begin running, then having the manager begin running.
	 * Make sure not to do this twice. */
	public void start(){
		if(! running && !finished){
			setRunning(true);
			for(Truck t : map.getTrucks()){
				Thread th = new Thread(t);
				t.setThread(th);
				th.start();
			}

			Thread m = new Thread(manager);
			manager.setThread(m);
			m.start();
		}
	}

	/** Returns the threads for this game */
	protected GUI getGUI(){
		return gui;
	}

	/** Sets the threads of this game to GUI g */
	public void setGUI(GUI g){
		gui = g;
	}

	/** Returns the current update message shown on the GUI */
	public String getUpdateMessage(){
		return gui.getUpdateMessage();
	}

	/** Updates the GUI to show the given String as an update message */
	public void setUpdateMessage(String newUpdate){
		gui.setUpdateMessage(newUpdate);
	}

	/** Ends this game prematurely by halting trucks and manager */
	public void kill(){
		if(running && !finished){
			setRunning(false);
			setFinished(true);
			for(Truck t : map.getTrucks()){
				t.gameOver();
			}

			manager.gameOver();
		}
	}

	/** Called when the game is finished, all the parcels are delivered.
	 * Cleans up the game and announces the score and time.
	 * Halts all trucks
	 */
	protected void finish(){
		setRunning(false);	
		setFinished(true);
		gui.setUpdateMessage("Game Finished!");
		gui.repaint();
		
		for(Truck t : map.getTrucks()){
			t.gameOver();
		}

		manager.gameOver();
	}

	/** Returns a file for the string map filename. Throws an IllegalArgumentException
	 * if this map does not exist. 
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
			throw new IllegalArgumentException("File " + f + " for filename " + filename + " DNE!");

		return f;
	}

	/** Writes this Game to a text file that can be loaded and re-played later.
	 * Can only be used if the game has not yet started or finished.
	 * If either of these is true, throws a runtime exception
	 * @throws IOException
	 * @throws RuntimeException if the game is not in its pre-start state when this method is called */
	public void writeGame(String fileName) throws IOException, RuntimeException{
		if(isRunning() || isFinished())
			throw new RuntimeException("Can't Write Game File if the game is running.");


		String n = MAP_DIRECTORY + fileName + ".txt";
		TextIO.write(n, map.toJSONString());
	}
}