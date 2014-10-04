package game;

import gui.Circle;
import gui.GUI;

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

/** The Map Class is a container for the HashSets of Edges and Nodes that make up
 * the playing field of the game. It also allows for easy access to random nodes and edges.
 * @author MPatashnik
 *
 */
public class Map implements JSONString{

	private long seed;		//The seed this map was generated from. -1 if none/custom.

	private Node truckHome;			//The node at which all trucks start
	protected static final String TRUCK_HOME_NAME = "Truck Depot"; //Name of truckhome

	private HashSet<Edge> edges;    //All edges in this map

	protected int minLength;			//Min length among all edges
	protected int maxLength;			//Max length among all edges

	private HashSet<Node> nodes;    //All nodes in this map

	private ArrayList<Truck> trucks; //The trucks in this map
	private HashSet<Parcel> parcels; //The parcels in this map

	private Score score; //The score for this map
	private final Game game; //The game this map belongs to

	/** The score cost of idling for a frame, per truck.
	 * So you are losing points at a slow rate constantly,
	 * you can't compute the optimal solution forever. 
	 * Lower than any cost of travel for a frame, but not 0.
	 */
	public final int WAIT_COST;

	/** One time cost incurred when a truck picks up a parcel */
	public final int PICKUP_COST;

	/** One time cost incurred when a truck drops off a parcel */
	public final int DROPOFF_COST;

	/** The score value of successfully delivering one parcel to its final destination */
	public final int PAYOFF;

	/** The score multiplier of successfully delivering a parcel using the correct color of Truck.
	 * Thus, the score value of  an on-color delivery is PAYOFF * ON_COLOR_MULTIPLIER
	 */
	public final int ON_COLOR_MULTIPLIER;

	/** Intializes the map from the given serialized version of the map for game g */
	protected Map(Game g, JSONObject obj){
		game = g;
		//Read seed if possible, otherwise use -1.
		if(obj.has(SEED_TOKEN)){
			seed = obj.getLong(SEED_TOKEN);
		} else{
			seed = -1;
		}
		//Read score coefficients
		JSONArray scoreJSON = obj.getJSONArray(Map.SCORE_TOKEN);
		int[] coeffs = new int[scoreJSON.length()];
		WAIT_COST = coeffs[0];
		PICKUP_COST = coeffs[1];
		DROPOFF_COST = coeffs[2];
		PAYOFF = coeffs[3];
		ON_COLOR_MULTIPLIER = coeffs[4];

		trucks = new ArrayList<Truck>();
		parcels = new HashSet<Parcel>();

		//Read in all nodes of map
		for(String key : obj.keySet()){
			if(key.startsWith(Map.NODE_TOKEN)){
				JSONObject nodeJSON = obj.getJSONObject(key);
				Node n = new Node(this, nodeJSON.getString(MapElement.NAME_TOKEN), null);
				Circle c = n.getCircle();
				c.setX1(nodeJSON.getInt(MapElement.X_TOKEN));
				c.setY1(nodeJSON.getInt(MapElement.Y_TOKEN));
				getNodes().add(n);
				if(n.getName().equals(Map.TRUCK_HOME_NAME))
					setTruckHome(n);
			}
		}
		//Read in all edges of map
		for(String key : obj.keySet()){
			if(key.startsWith(Map.EDGE_TOKEN)){
				JSONObject edgeJSON = obj.getJSONObject(key);
				JSONArray exitArr = edgeJSON.getJSONArray(MapElement.LOCATION_TOKEN);

				int length = edgeJSON.getInt(MapElement.LENGTH_TOKEN);
				Node firstExit = getNode((String)exitArr.get(0));
				Node secondExit = getNode((String)exitArr.get(1));

				Edge e = new Edge(this, firstExit, secondExit, length);
				getEdges().add(e);
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
				Truck t = new Truck(game, name, c, getTruckHome());
				trucks.add(t);
			} else if( key.startsWith(PARCEL_TOKEN)){
				JSONObject parcel = obj.getJSONObject(key);
				Color c = new Color(parcel.getInt(MapElement.COLOR_TOKEN));
				Node start = getNode(parcel.getString(MapElement.LOCATION_TOKEN));
				Node dest = getNode(parcel.getString(MapElement.DESTINATION_TOKEN));

				Parcel p = new Parcel(this, start, dest, c);
				parcels.add(p);
				try {
					start.addParcel(p);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		updateMinMaxLength();
	}

	/** Returns the seed this game was generated from. If non-random, returns -1 */
	public long getSeed(){
		return seed;
	}

	/** Returns a random node in this map */
	public Node getRandomNode(){
		return Main.randomElement(nodes, null);
	}

	/** Returns a random edge in this map */
	public Edge getRandomEdge(){
		return Main.randomElement(edges, null);
	}

	/** Returns a HashSet containing all the Nodes in this map. Allows addition and removal of Nodes to this map */
	public HashSet<Node> getNodes(){
		return nodes;
	}

	/** Returns the number of Nodes in this map */
	public int getNodesSize(){
		return nodes.size();
	}

	/** Returns the Node named name in this map if it exists, null otherwise */
	public Node getNode(String name){
		for(Node n : nodes){
			if(n.getName().equals(name))
				return n;
		}

		return null;
	}

	/** Returns the TruckHome node that Trucks must return to before the game can be ended. */
	public Node getTruckHome(){
		return truckHome;
	}

	/** Sets the TruckHome node that Trucks must return to before the game can be ended to Node n
	 * @throws IllegalArgumentException - if n is not in this map
	 */
	protected void setTruckHome(Node n) throws IllegalArgumentException{
		if(nodes.contains(n))
			truckHome = n;
		else
			throw new IllegalArgumentException("Can't set Truck Home to a Node that isn't contained in this map.");
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
			if(t.getLocation() != null && t.getLocation().equals(getTruckHome()))
				homeTrucks.add(t);

		return homeTrucks;
	}

	/** Returns true if any alive Truck in this game is currently on the TruckHome node, false otherwise 
	 * @throws InterruptedException */
	public boolean isTruckHome() throws InterruptedException{
		for(Truck t : getTrucks())
			if(t.getLocation() != null && t.getLocation().equals(getTruckHome()))
				return true;

		return false;
	}

	/** Returns true if all alive Truck in this game are currently on the TruckHome node, false otherwise 
	 * @throws InterruptedException */
	public boolean isAllTrucksHome() throws InterruptedException{
		for(Truck t : getTrucks())
			if(t.getLocation() == null || ! t.getLocation().equals(getTruckHome()))
				return false;

		return true;
	}

	/** Returns the parcels in this Game */
	public HashSet<Parcel> getParcels(){
		return parcels;
	}

	/** Returns the score object */
	protected Score getScore(){
		return score;
	}

	/** Returns the value of the score*/
	public int getScoreValue(){
		return score.value();
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
			score.changeScore(PAYOFF * ON_COLOR_MULTIPLIER);
		else
			score.changeScore(PAYOFF);

		parcels.remove(p);
		try {
			n.removeParcel(p);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		game.getGUI().getDrawingPanel().remove(p.getCircle());
	}

	/** Returns a HashSet containing all the Edges in this map. Allows addition and removal of Edges to this map */
	public HashSet<Edge> getEdges(){
		return edges;
	}

	/** Returns the number of Edges in this map */
	public int getEdgesSize(){
		return edges.size();
	}

	/** Returns true if there is any intersection of the lines drawn by the
	 * edges in edges, false otherwise.
	 * 
	 * Used for GUI intersection detection, not useful outside of the GUI context.
	 * Has nothing to say about the non-GUI version of the map
	 */
	public boolean isIntersection(){
		for(Edge r : edges){
			for(Edge r2 : edges){
				if(! r.equals(r2)){
					if(r.getLine().intersects(r2.getLine()))
						return true;

				}
			}
		}

		return false;
	}

	/** Updates the Minimum and Maximum lengths of all edge instances.
	 * Called internally during processing, no need to call this after game initialized. */
	public void updateMinMaxLength(){
		minLength = Edge.DEFAULT_MIN_LENGTH;
		maxLength = Edge.DEFAULT_MAX_LENGTH;

		for(Edge e : edges){
			if(e.getLength() != Edge.DUMMY_LENGTH){
				minLength = Math.min(minLength, e.getLength());
				maxLength = Math.max(maxLength, e.getLength());
			}
		}
	}

	/** Returns the maximum length of all edges on the map */
	public int getMaxLength(){
		return maxLength;
	}

	/** Returns the minimum length of all edges on the map */
	public int getMinLength(){
		return minLength;
	}

	/** Returns a 2x1 array of edges that have lines that intersect
	 * If no two edges intersect, returns null.
	 * 
	 * Used for GUI intersection detection, not useful outside of the GUI context.
	 * Has nothing to say about the non-GUI version of the map
	 */
	public Edge[] getAIntersection(){
		for(Edge r : edges){
			for(Edge r2 : edges){
				if(! r.equals(r2)){
					if(r.getLine().intersects(r2.getLine())){
						Edge[] intersectingRoads = {r, r2};
						return intersectingRoads;
					}

				}
			}
		}

		return null;
	}


	@Override
	/** Returns a String representation of this Map, with all edges and Nodes */
	public String toString(){
		String output = "";
		Iterator<Node> nodesIterator = nodes.iterator();
		while(nodesIterator.hasNext()){
			Node n = nodesIterator.next();
			output += n + "\t";
			Iterator<Edge> roadsIterator = n.getTrueExits().iterator();
			while(roadsIterator.hasNext()){
				Edge r = roadsIterator.next();
				output += r.getOther(n).getName()+"-"+r.getLength();
				if(roadsIterator.hasNext())
					output += "\t";
			}
			if(nodesIterator.hasNext())
				output += "\n";
		}
		return output;
	}


	private static final String SCORE_TOKEN = "scoreCoeff";
	private static final String NODE_TOKEN = "node-";
	private static final String EDGE_TOKEN = "edge-";
	private static final String SEED_TOKEN = "seed";
	private static final String TRUCK_TOKEN = "truck-";
	private static final String PARCEL_TOKEN = "parcel-";

	@Override
	/** Returns a JSON-compliant version of toString() */
	public String toJSONString() {		
		String s = "{\n" + Main.addQuotes(SEED_TOKEN) + ":" + seed +",";
		s += Main.addQuotes(SCORE_TOKEN) + ":[" + WAIT_COST + "," 
				+ PICKUP_COST + "," + DROPOFF_COST + "," + PAYOFF + "," + ON_COLOR_MULTIPLIER + "],";
		int i = 0;
		for(Node n : nodes){
			s += "\n" + Main.addQuotes(NODE_TOKEN + i) + ":" + n.toJSONString() + ",";
			i++;
		}
		i = 0;
		for(Edge e : edges){
			s += "\n" + Main.addQuotes(EDGE_TOKEN + i) + ":" + e.toJSONString() +",";
			i++;
		}
		i = 0;
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
	
	////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////// Random Map Generation //////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Library for random map generation.
	 * Implemented inside map class to allow construction based on these methods.
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
	public static Map randomMap(Game g) {
		return randomMap(g, (long)(Math.random() * Long.MAX_VALUE));
	}

	/** Returns a new random map seeded with {@code seed} */
	public static Map randomMap(Game g, long seed) {
		return new Map(g, new Random(seed), seed);
	}

	/** Returns a new random map using the {@code Random} parameter {@code r} */
	private Map(Game g, Random r, long seed) {
		this.seed = seed;
		game = g;
		//Create new score object
		score = new Score();

		final int numCities = r.nextInt(MAX_NODES - MIN_NODES + 1) + MIN_NODES;
		WAIT_COST = -1
				* (r.nextInt(WAIT_COST_MAX - WAIT_COST_MIN + 1) + WAIT_COST_MIN);
		PICKUP_COST = -1
				* (r.nextInt(PICKUP_COST_MAX - PICKUP_COST_MIN + 1) + PICKUP_COST_MIN);
		DROPOFF_COST = -1
				* (r.nextInt(DROPOFF_COST_MAX - DROPOFF_COST_MIN + 1) + DROPOFF_COST_MIN);
		PAYOFF = r.nextInt(PAYOFF_MAX - PAYOFF_MIN + 1) + PAYOFF_MIN;
		ON_COLOR_MULTIPLIER = r.nextInt(ON_COLOR_MULTIPLIER_MAX
				- ON_COLOR_MULTIPLIER_MIN + 1)
				+ ON_COLOR_MULTIPLIER_MIN;

		//Initialize collections
		nodes = new HashSet<Node>();
		edges = new HashSet<Edge>();
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
			getNodes().add(n);
			if(n.getName().equals(Map.TRUCK_HOME_NAME)){
				setTruckHome(n);
			}
		}
		//Add intial edges, make sure every node has degree at least 2.
		//Do this by connecting every edge in order, creating an outer loop
		Iterator<Node> i1 = getNodes().iterator();
		Iterator<Node> i2 = getNodes().iterator();
		Node first = i2.next(); //First node in collection
		while(i2.hasNext()){
			Node from = i1.next();
			Node to = i2.next();
			int length = r.nextInt(MAX_EDGE_LENGTH - MIN_EDGE_LENGTH + 1) + MIN_EDGE_LENGTH;
			Edge e = new Edge(this, from, to, length);
			getEdges().add(e);
			from.addExit(e);
			to.addExit(e);
		}
		//Add final edge connecting the circle
		Node last = i1.next();
		Edge e = new Edge(this, last, first, r.nextInt(MAX_EDGE_LENGTH - MIN_EDGE_LENGTH + 1) + MIN_EDGE_LENGTH);
		getEdges().add(e);
		first.addExit(e);
		last.addExit(e);

		//Add edges to the map to satisfy the average degree constraint
		while (getEdges().size() < (getNodes().size()*AVERAGE_DEGREE)/2){
			Node from = randomElement(getNodes(), r);
			Node to = from;
			while (from == to || from.isConnectedTo(to)){
				from = randomElement(getNodes(), r);
			}
			int length = r.nextInt(MAX_EDGE_LENGTH - MIN_EDGE_LENGTH + 1) + MIN_EDGE_LENGTH;
			e = new Edge(this, from, to, length);
			getEdges().add(e);
			from.addExit(e);
			to.addExit(e);
		}

		//Add trucks
		final int numb_trucks = r.nextInt(MAX_TRUCKS - MIN_TRUCKS + 1) + MIN_TRUCKS;
		for(int i = 0; i < numb_trucks; i++){
			Truck t = new Truck(game, "TRUCK-" + (i+1), Score.COLOR[r.nextInt(Score.COLOR.length)], getTruckHome());
			trucks.add(t);
		}

		//Add parcels
		final int numb_parcels = r.nextInt(MAX_PARCELS - MIN_PARCELS + 1) + MIN_PARCELS;
		for(int i = 0; i < numb_parcels; i++){
			Node start = randomElement(getNodes(), r);
			Node dest = start;
			while(dest == start){
				dest = randomElement(getNodes(), r);
			}
			Color c = Score.COLOR[r.nextInt(Score.COLOR.length)];
			Parcel p = new Parcel(this, start, dest, c);
			parcels.add(p);
			try {
				start.addParcel(p);
			} catch (InterruptedException e1) {}
		}
		updateMinMaxLength();
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