package game;
import gui.GUI;
import gui.TextIO;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

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
	
	private static final int DEFAULT_NUMB_ROADS = 19;
	private static final int DEFAULT_MIN_ROAD_LENGTH = 2;
	private static final int DEFAULT_AVG_ROAD_LENGTH = 25;
	private static final int DEFAULT_MAX_ROAD_LENGTH = 60;
	private static final String[] DEFAULT_NODES = {"Chicago", "DC", "New York", "Los Angeles", "Dallas", "San Francisco", 
		"Atlanta", "Miami", "Seattle", "Portland", "Albany", "Boston", "Richmond", "Orlando"};
	private static final String[] DEFAULT_TRUCKS = {"Truck_A", "Truck_B", "Truck_C"};
	private static final int DEFAULT_NUMB_PARCELS = 5;

	private GUI gui;
	private Manager manager;
	
	private boolean running;
	private Score score;
	private HashSet<Parcel> parcels;
	private Map map;
	private ArrayList<Truck> trucks;

	/** Creates a default (test) game instance. Uses default values for all fields */
	public Game(Manager m){
		manager = m;
		score = new Score(this);
		map = new Map(this, DEFAULT_NODES, DEFAULT_NUMB_ROADS, DEFAULT_MIN_ROAD_LENGTH, DEFAULT_AVG_ROAD_LENGTH, DEFAULT_MAX_ROAD_LENGTH);
		trucks = new ArrayList<Truck>(DEFAULT_TRUCKS.length);
		for(int i = 0; i < DEFAULT_TRUCKS.length; i++){
			Truck t = new Truck(this, DEFAULT_TRUCKS[i], map.getTruckHome(), Score.getRandomColor());
			try {
				map.getTruckHome().setTruckHere(t, true);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			trucks.add(t);
		}

		parcels = new HashSet<Parcel>();
		for(int i = 0; i < DEFAULT_NUMB_PARCELS; i++){
			Node start = map.getRandomNode();
			while(start.equals(map.getTruckHome()))
				start = map.getRandomNode();
			Node dest = map.getRandomNode();
			while(dest.equals(map.getTruckHome()))
				dest = map.getRandomNode();
			while(start.equals(dest)){
				dest = map.getRandomNode();
			}
			try {
				start.addParcel(new Parcel(this, start, dest));
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			//			System.out.println("Parcel traveling from " + start.getName() + " to " + dest.getName());
		}
	}
	
	/** Creates a game instance with a set Map, read from File f. Uses Default for all other fields */
	public Game(Manager m, File f){
		manager = m;
		score = new Score(this);
		try {
			readGame(f);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	/** Returns true if this game is currently running, false otherwise */
	public boolean isRunning(){
		return running;
	}
	
	/** Returns true if this game is currently paused, false otherwise */
	public boolean isPaused(){
		return gui.isPaused();
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

	/** Starts the game by having each truck begin running, then having the manager begin running.
	 * Make sure not to do this twice. */
	public void start(){
		if(! running){
			running = true;
			for(Truck t : trucks){
				Thread th = new Thread(t);
				th.start();
			}
	
			Thread m = new Thread(manager);
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
		if(!p.getDestination().equals(n))
			throw new IllegalArgumentException("Parcel " + p + "'s final destination is not " + n.getName() + ". Cannot Deliver Here");
		try {
			if(!t.getLocation().equals(n))
				throw new IllegalArgumentException("Truck " + t + "Is not currently at " + n.getName() + ". Cannot Deliver Here");
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		if(!t.getLoad().equals(p))
			throw new IllegalArgumentException("Truck " + t + "Is not currently holding Parcel " + p + ". Cannot Deliver Here");

		if(t.getColor().equals(p.getColor()))
			score.changeScore(Score.PAYOFF * Score.ON_COLOR_MULTIPLIER);
		else
			score.changeScore(Score.PAYOFF);

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
			running = false;
			gui.dispose();
	
			for(Truck t : getTrucks()){
				try {
					t.gameOver();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			try {
				manager.gameOver();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/** Called when the game is finished, all the parcels are delivered.
	 * Cleans up the game and announces the score and time.
	 * Halts all trucks
	 */
	protected void finish(){
		running = false;
		gui.getCheckBoxPaused().setSelected(true);		
		gui.setUpdateMessage("Game Finished!");
		gui.getCheckBoxPaused().setEnabled(false);
		gui.repaint();

		for(Truck t : getTrucks()){
			try {
				t.gameOver();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		try {
			manager.gameOver();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private static final String MAP_TOKEN = "map";
	private static final String TRUCK_TOKEN = "truck-";
	private static final String PARCEL_TOKEN = "parcel-";
	
	@Override
	/** Returns a JSON representation of this game.
	 * Specifically JSON-ifies the parts needed to recreate it later.
	 * JSONs the map, the parcels, and the trucks.
	 */
	public String toJSONString() {
		String s = "{\n" + Main.addQuotes(MAP_TOKEN) + ":" + map.toJSONString() + ",";
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
		map = new Map();
		trucks = new ArrayList<Truck>();
		parcels = new HashSet<Parcel>();
		
		JSONObject obj = new JSONObject(TextIO.read(f));
		
		//First process map - under key with value of MAP_TOKEN.
		JSONObject mapJSON = obj.getJSONObject(MAP_TOKEN);
		//Read in all nodes of map
		for(String key : mapJSON.keySet()){
			if(key.startsWith(Map.NODE_TOKEN)){
				JSONObject nodeJSON = mapJSON.getJSONObject(key);
				Node n = new Node(this, nodeJSON.getString(MapElement.NAME_TOKEN), null);
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

				Edge e = new Edge(firstExit, secondExit, length);
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
				Truck t = new Truck(this, name, map.getTruckHome(), c);
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
}
