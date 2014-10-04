package game;
import gui.Circle;
import gui.GUI;
import gui.TextIO;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONString;

/** The Game Class is the controlling class for the ShippingGame Project.
 * Each game contains a collection of Parcels that need to be delivered from their
 * starting Node to their desired destination node. In order to do this, each game
 * also has a collection of Trucks that are able to pick up and move Parcels along edges between nodes.
 * <br><br>
 * Games also contain an instance of a Map that holds all of the Nodes and Edges in this game. The manager
 * may use this map to determine where trucks should go and what edges should be used to get there.
 * <br><br>
 * Finally, each Game contains a Score object, that manages the points in the Game and holds the current value
 * when the final score is announced
 * @author MPatashnik
 *
 */
public class Game implements JSONString{

	public static final String MAP_DIRECTORY = "Maps/";
	public static final String MAP_EXTENSION = ".txt";

	private long seed;		//The seed this game was generated from. -1 if none/custom.
	private File file;	//The file this game was loaded from. Null if none.
	private String managerClass; //The name of the class the manager was created from.

	private GUI gui;
	private Manager manager;

	private boolean running;
	private Score score;
	private HashSet<Parcel> parcels;
	private Map map;
	private ArrayList<Truck> trucks;

	/** Creates a game instance with a set Map, read from File f. Uses Default for all other fields */
	public Game(String managerClassname, File f){
		file = f;
		setManager(managerClassname);
		manager.setGame(this);
		score = new Score(this);
		try {
			readGame(f);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
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

	/** Returns the seed this game was generated from. If non-random, returns -1 */
	public long getSeed(){
		return seed;
	}

	/** Returns true if this game is currently running, false otherwise */
	public boolean isRunning(){
		return running;
	}

	/** Sets the value of running. Also informs gui of changes */
	protected void setRunning(boolean r){
		running = r;
		gui.updateRunning();
	}

	/** Sets the map to map m */
	protected void setMap(Map m){
		map = m;
	}

	/** Returns the map of this game, including all of the nodes and all of the edges */
	public Map getMap(){
		return map;
	}

	/** Returns the score object */
	protected Score getScore(){
		return score;
	}

	/** Returns the trucks in this game */
	public ArrayList<Truck> getTrucks(){
		return trucks;
	}

	/** Returns the trucks in this game that are currently on the Truck Home node 
	 * @throws InterruptedException */
	public ArrayList<Truck> getTrucksHome() throws InterruptedException{
		ArrayList<Truck> homeTrucks = new ArrayList<Truck>();
		for(Truck t : trucks)
			if(t.getLocation() != null && t.getLocation().equals(map.getTruckHome()))
				homeTrucks.add(t);

		return homeTrucks;
	}

	/** Returns true if any alive Truck in this game is currently on the TruckHome node, false otherwise 
	 * @throws InterruptedException */
	public boolean isTruckHome() throws InterruptedException{
		for(Truck t : getTrucks())
			if(t.getLocation() != null && t.getLocation().equals(map.getTruckHome()))
				return true;

		return false;
	}

	/** Returns true if all alive Truck in this game are currently on the TruckHome node, false otherwise 
	 * @throws InterruptedException */
	public boolean isAllTrucksHome() throws InterruptedException{
		for(Truck t : getTrucks())
			if(t.getLocation() == null || ! t.getLocation().equals(map.getTruckHome()))
				return false;

		return true;
	}

	/** Returns the value of the score*/
	public int getScoreValue(){
		return score.value();
	}

	/** Returns the parcels in this Game */
	public HashSet<Parcel> getParcels(){
		return parcels;
	}

	/** Returns the manager for this Game */
	public Manager getManager(){
		return manager;
	}

	/** Returns a String of the class used to load the manager */
	public String getManagerClassname(){
		return managerClass;
	}

	/** Returns the file this game was created from. Returns null if this was created, not loaded */
	public File getFile(){
		return file;
	}

	/** Starts the game by having each truck begin running, then having the manager begin running.
	 * Make sure not to do this twice. */
	public void start(){
		if(! running){
			setRunning(true);
			for(Truck t : trucks){
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

	/** Called by Trucks to drop off parcels at nodes 
	 * @param p - The Parcel to deliver. Must be currently held by Truck t
	 * @param n - The Node to deliver the parcel to. Must be Parcel p's final destination
	 * @param t - The Truck that is delivering Parcel p. Must currently be holding p and be at n
	 * @throws IllegalArgumentException - if any of the above parameter requirements aren't met*/
	protected void deliverParcel(Parcel p, Node n, Truck t){
		if(p.getDestination() != n)
			throw new IllegalArgumentException("Parcel " + p + "'s final destination is not " + n.getName() + ". Cannot Deliver Here");
		if(t.getLocation() != n)
			throw new IllegalArgumentException("Truck " + t + "Is not currently at " + n.getName() + ". Cannot Deliver Here");
		if(t.getLoad() != p)
			throw new IllegalArgumentException("Truck " + t + "Is not currently holding Parcel " + p + ". Cannot Deliver Here");

		if(t.getColor().equals(p.getColor()))
			score.changeScore(map.PAYOFF * map.ON_COLOR_MULTIPLIER);
		else
			score.changeScore(map.PAYOFF);

		parcels.remove(p);
		try {
			n.removeParcel(p);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		gui.getDrawingPanel().remove(p.getCircle());
	}

	/** Ends this game prematurely by halting trucks and manager */
	public void kill(){
		if(running){
			setRunning(false);
			for(Truck t : getTrucks()){
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
		gui.setUpdateMessage("Game Finished!");
		gui.repaint();

		for(Truck t : getTrucks()){
			t.gameOver();
		}

		manager.gameOver();
	}

	private static final String SEED_TOKEN = "seed";
	private static final String MAP_TOKEN = "map";
	private static final String TRUCK_TOKEN = "truck-";
	private static final String PARCEL_TOKEN = "parcel-";

	@Override
	/** Returns a JSON representation of this game.
	 * Specifically JSON-ifies the parts needed to recreate it later.
	 * JSONs the map, the parcels, and the trucks.
	 */
	public String toJSONString() {
		String s = "{\n" + Main.addQuotes(SEED_TOKEN) + ":" + seed +",";
		s += "\n" + Main.addQuotes(MAP_TOKEN) + ":" + map.toJSONString() + ",";
		int i = 0;
		for(Truck t : trucks){
			s += "\n" + Main.addQuotes(TRUCK_TOKEN + i) + ":" + t.toJSONString() + ",";
			i++;
		}
		i = 0;
		for(Parcel p : parcels){
			s += "\n" + Main.addQuotes(PARCEL_TOKEN + i) + ":" + p.toJSONString();
			if(i < parcels.size() - 1)
				s += ",";
			i++;
		}	
		return s + "\n}";
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

	/** Reads the Game in File f, creates it, and returns it.
	 * Also sets the trucks and parcels of this game while reading the map
	 * @param g - the Game this map will belong to
	 * @param f - the Text file with the map in it
	 * @throws IOException if End_Node_Identifier is not found in the file, or if TextIO.read(f) throws an exception*/
	protected void readGame(File f) throws IOException{
		JSONObject obj = new JSONObject(TextIO.read(f));

		//Read seed if possible, otherwise use -1.
		if(obj.has(SEED_TOKEN)){
			seed = obj.getLong(SEED_TOKEN);
		} else{
			seed = -1;
		}
		//First process map - under key with value of MAP_TOKEN.
		JSONObject mapJSON = obj.getJSONObject(MAP_TOKEN);
		//Read score coefficients
		JSONArray scoreJSON = mapJSON.getJSONArray(Map.SCORE_TOKEN);
		int[] coeffs = new int[scoreJSON.length()];
		for(int i = 0; i < coeffs.length; i++){
			coeffs[i] = scoreJSON.getInt(i);
		}
		map = new Map(coeffs);

		trucks = new ArrayList<Truck>();
		parcels = new HashSet<Parcel>();

		//Read in all nodes of map
		for(String key : mapJSON.keySet()){
			if(key.startsWith(Map.NODE_TOKEN)){
				JSONObject nodeJSON = mapJSON.getJSONObject(key);
				Node n = new Node(this, nodeJSON.getString(MapElement.NAME_TOKEN), null);
				Circle c = n.getCircle();
				c.setX1(nodeJSON.getInt(MapElement.X_TOKEN));
				c.setY1(nodeJSON.getInt(MapElement.Y_TOKEN));
				map.getNodes().add(n);
				if(n.getName().equals(Map.TRUCK_HOME_NAME))
					map.setTruckHome(n);
			}
		}
		//Read in all edges of map
		for(String key : mapJSON.keySet()){
			if(key.startsWith(Map.EDGE_TOKEN)){
				JSONObject edgeJSON = mapJSON.getJSONObject(key);
				JSONArray exitArr = edgeJSON.getJSONArray(MapElement.LOCATION_TOKEN);

				int length = edgeJSON.getInt(MapElement.LENGTH_TOKEN);
				Node firstExit = map.getNode((String)exitArr.get(0));
				Node secondExit = map.getNode((String)exitArr.get(1));

				Edge e = new Edge(this, firstExit, secondExit, length);
				map.getEdges().add(e);
				firstExit.addExit(e);
				secondExit.addExit(e);
			}
		}
		//Map reading finished.

		//Read in the trucks and parcels
		for(String key : obj.keySet()){
			if (key.startsWith(TRUCK_TOKEN)){
				JSONObject truck = obj.getJSONObject(key);
				Color c = new Color(truck.getInt(MapElement.COLOR_TOKEN));
				String name = truck.getString(MapElement.NAME_TOKEN);
				Truck t = new Truck(this, name, c);
				trucks.add(t);
			} else if( key.startsWith(PARCEL_TOKEN)){
				JSONObject parcel = obj.getJSONObject(key);
				Color c = new Color(parcel.getInt(MapElement.COLOR_TOKEN));
				Node start = map.getNode(parcel.getString(MapElement.LOCATION_TOKEN));
				Node dest = map.getNode(parcel.getString(MapElement.DESTINATION_TOKEN));

				Parcel p = new Parcel(this, start, dest, c);
				parcels.add(p);
				try {
					start.addParcel(p);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/** Writes this Game to a text file that can be loaded and re-played later.
	 * Can only be used if the game has not yet started or finished.
	 * If either of these is true, throws a runtime exception
	 * @throws IOException
	 * @throws RuntimeException if the game is not in its pre-start state when this method is called */
	public void writeGame(String fileName) throws IOException, RuntimeException{
		if(isRunning())
			throw new RuntimeException("Can't Write Game File if the game is running.");


		String n = MAP_DIRECTORY + fileName + ".txt";
		TextIO.write(n, toJSONString());
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////// Random Game Generation //////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Library for random game generation.
	 * Implemented inside game class to allow construction based on these methods.
	 * 
	 * @author eperdew
	 * @author MPatashnik
	 * 
	 */

	private static final int MIN_NODES = 5;
	private static final int MAX_NODES = 20;

	private static final double AVERAGE_DEGREE = 3;
	private static final int MIN_EDGE_LENGTH = 15;
	private static final int MAX_EDGE_LENGTH = 100;

	private static final int WIDTH = GUI.MAIN_WINDOW_SIZE.width;
	private static final int HEIGHT = GUI.MAIN_WINDOW_SIZE.height;

	private static final int MIN_TRUCKS = 1;
	private static final int MAX_TRUCKS = 1;

	private static final int MIN_PARCELS = 3;
	private static final int MAX_PARCELS = 10;

	private static final int WAIT_COST_MIN = 1;
	private static final int WAIT_COST_MAX = 3;

	private static final int PICKUP_COST_MIN = 50;
	private static final int PICKUP_COST_MAX = 150;

	private static final int DROPOFF_COST_MIN = 50;
	private static final int DROPOFF_COST_MAX = 150;

	private static final int PAYOFF_MIN = 3000;
	private static final int PAYOFF_MAX = 10000;

	private static final int ON_COLOR_MULTIPLIER_MIN = 1;
	private static final int ON_COLOR_MULTIPLIER_MAX = 5;

	/**
	 * Returns a new random map seeded via random seed.
	 */
	public static Game randomGame() {
		return randomGame((long)(Math.random() * Long.MAX_VALUE));
	}

	/** Returns a new random map seeded with {@code seed} */
	public static Game randomGame(long seed) {
		return new Game(new Random(seed), seed);
	}

	/** Returns a new random map using the {@code Random} parameter {@code r} */
	private Game(Random r, long seed) {
		//Set unused fields in a random game - either unused or set later
		file = null;
		managerClass = null;
		manager = null;
		gui = null;
		running = false;
		this.seed = seed;
		//Create new score object
		score = new Score(this);

		final int numCities = r.nextInt(MAX_NODES - MIN_NODES + 1) + MIN_NODES;
		final int WAIT_COST = -1
				* (r.nextInt(WAIT_COST_MAX - WAIT_COST_MIN + 1) + WAIT_COST_MIN);
		final int PICKUP_COST = -1
				* (r.nextInt(PICKUP_COST_MAX - PICKUP_COST_MIN + 1) + PICKUP_COST_MIN);
		final int DROPOFF_COST = -1
				* (r.nextInt(DROPOFF_COST_MAX - DROPOFF_COST_MIN + 1) + DROPOFF_COST_MIN);
		final int PAYOFF = r.nextInt(PAYOFF_MAX - PAYOFF_MIN + 1) + PAYOFF_MIN;
		final int ON_COLOR_MULTIPLIER = r.nextInt(ON_COLOR_MULTIPLIER_MAX
				- ON_COLOR_MULTIPLIER_MIN + 1)
				+ ON_COLOR_MULTIPLIER_MIN;
		final int[] scoreCoeffs = {WAIT_COST,PICKUP_COST,DROPOFF_COST,PAYOFF,ON_COLOR_MULTIPLIER};

		//Initialize collections
		map = new Map(scoreCoeffs);
		parcels = new HashSet<Parcel>();
		trucks = new ArrayList<Truck>();

		ArrayList<String> cities = cityNames();
		//Create nodes and add to map
		for (int i = 0; i < numCities; i++) {
			String name;
			if(i == 0){
				name = Map.TRUCK_HOME_NAME;
			} else{
				name = cities.remove((int)(Math.random()*cities.size()));
			}
			Node n = new Node(this, name, null);
			Circle c = n.getCircle();
			c.setX1(r.nextInt(WIDTH + 1));
			c.setY1(r.nextInt(HEIGHT + 1));
			map.getNodes().add(n);
			if(n.getName().equals(Map.TRUCK_HOME_NAME)){
				map.setTruckHome(n);
			}
		}
		//Add intial edges, make sure every node has degree at least 2.
		//Do this by connecting every edge in order, creating an outer loop
		Iterator<Node> i1 = map.getNodes().iterator();
		Iterator<Node> i2 = map.getNodes().iterator();
		Node first = i2.next(); //First node in collection
		while(i2.hasNext()){
			Node from = i1.next();
			Node to = i2.next();
			int length = r.nextInt(MAX_EDGE_LENGTH - MIN_EDGE_LENGTH + 1) + MIN_EDGE_LENGTH;
			Edge e = new Edge(this, from, to, length);
			map.getEdges().add(e);
			from.addExit(e);
			to.addExit(e);
		}
		//Add final edge connecting the circle
		Node last = i1.next();
		Edge e = new Edge(this, last, first, r.nextInt(MAX_EDGE_LENGTH - MIN_EDGE_LENGTH + 1) + MIN_EDGE_LENGTH);
		map.getEdges().add(e);
		first.addExit(e);
		last.addExit(e);

		//Add edges to the map to satisfy the average degree constraint
		while (map.getEdges().size() < (map.getNodes().size()*AVERAGE_DEGREE)/2){
			Node from = randomElement(map.getNodes(), r);
			Node to = from;
			while (from == to || from.isConnectedTo(to)){
				from = randomElement(map.getNodes(), r);
			}
			int length = r.nextInt(MAX_EDGE_LENGTH - MIN_EDGE_LENGTH + 1) + MIN_EDGE_LENGTH;
			e = new Edge(this, from, to, length);
			map.getEdges().add(e);
			from.addExit(e);
			to.addExit(e);
		}

		//Add trucks
		final int numb_trucks = r.nextInt(MAX_TRUCKS - MIN_TRUCKS + 1) + MIN_TRUCKS;
		for(int i = 0; i < numb_trucks; i++){
			Truck t = new Truck(this, "TRUCK-" + (i+1), Score.COLOR[r.nextInt(Score.COLOR.length)]);
			trucks.add(t);
		}

		//Add parcels
		final int numb_parcels = r.nextInt(MAX_PARCELS - MIN_PARCELS + 1) + MIN_PARCELS;
		for(int i = 0; i < numb_parcels; i++){
			Node start = randomElement(map.getNodes(), r);
			Node dest = start;
			while(dest == start){
				dest = randomElement(map.getNodes(), r);
			}
			Color c = Score.COLOR[r.nextInt(Score.COLOR.length)];
			Parcel p = new Parcel(this, start, dest, c);
			parcels.add(p);
			try {
				start.addParcel(p);
			} catch (InterruptedException e1) {}
		}
	}
	
	/** Returns a random element from the given collection using the given randomer */
	private static <T> T randomElement(Collection<T> elms, Random r){
		Iterator<T> it = elms.iterator();
		T val = null;
		int rand = r.nextInt(elms.size()) + 1;
		for(int i = 0; i < rand; i++){
			val = it.next();
		}
		return val;
	}

	/** Returns an array of the city names listed in MapGeneration/cities.txt */
	private static ArrayList<String> cityNames(){
		File f = new File("MapGeneration/cities.txt");
		BufferedReader read;
		try {
			read = new BufferedReader(new FileReader(f));
		}
		catch (FileNotFoundException e){
			System.out.println("cities.txt not found. Aborting as empty list of city names...");
			return new ArrayList<String>();
		}
		ArrayList<String> result = new ArrayList<String>();
		String line;
		try{
			while((line = read.readLine()) != null){
				result.add(line);
			}
			read.close();
		}
		catch(IOException e){
			System.out.println("Error in file reading. Aborting as empty list of city names...");
			return new ArrayList<String>();
		}
		return result;
	}
}