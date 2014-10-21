package game;

import gui.Circle;
import gui.GUI;
import gui.Line;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONString;

/** The Board Class is a container for the HashSets of Edges and Nodes that make up
 * the playing field of the game, as well as the parcels and trucks that are part of the game. 
 * 
 * Each board contains a collection of Parcels that need to be delivered from their
 * starting Node to their desired destination node. In order to do this, each game
 * also has a collection of Trucks that are able to pick up and move Parcels along edges between nodes.
 * 
 * @author MPatashnik
 *
 */
public class Board implements JSONString{

	/** The random seed this Board was generated from: -1 if this was loaded from a non-random file */
	public final long seed;

	private Node truckHome;			//The node at which all trucks start
	protected static final String TRUCK_HOME_NAME = "Truck Depot"; //Name of truckhome

	private HashSet<Edge> edges;    //All edges in this board

	protected int minLength;			//Min length among all edges
	protected int maxLength;			//Max length among all edges

	private HashSet<Node> nodes;    //All nodes in this board

	private ArrayList<Truck> trucks; //The trucks in this board
	private Set<Parcel> parcels; //The parcels in this board - ones that have not been delivered yet

	protected List<Integer> parcelCounts; //A count of [on map, on truck, delivered] parcels
										  //Values are managed by parcels as they are moved
	protected static final int PARCELS_ON_MAP = 0;
	protected static final int PARCELS_ON_TRUCK = 1;
	protected static final int PARCELS_DELIVERED = 2;

	/** The game this board is for */
	public final Game game;

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

	/** Initializes the board from the given serialized version of the board for game g */
	protected Board(Game g, JSONObject obj){
		game = g;
		//Read seed if possible, otherwise use -1.
		if(obj.has(SEED_TOKEN)){
			seed = obj.getLong(SEED_TOKEN);
		} else{
			seed = -1;
		}
		//Read score coefficients
		JSONArray scoreJSON = obj.getJSONArray(Board.SCORE_TOKEN);
		WAIT_COST = scoreJSON.getInt(0);
		PICKUP_COST = scoreJSON.getInt(1);
		DROPOFF_COST = scoreJSON.getInt(2);
		PAYOFF = scoreJSON.getInt(3);
		ON_COLOR_MULTIPLIER = scoreJSON.getInt(4);

		trucks = new ArrayList<Truck>();
		parcels = Collections.synchronizedSet(new HashSet<Parcel>());
		nodes = new HashSet<Node>();
		edges = new HashSet<Edge>();

		//Read in all nodes of board - read all nodes before reading any edges
		for(String key : obj.keySet()){
			if(key.startsWith(Board.NODE_TOKEN)){
				JSONObject nodeJSON = obj.getJSONObject(key);
				Node n = new Node(this, nodeJSON.getString(BoardElement.NAME_TOKEN), null);
				Circle c = n.getCircle();
				c.setX1(nodeJSON.getInt(BoardElement.X_TOKEN));
				c.setY1(nodeJSON.getInt(BoardElement.Y_TOKEN));
				getNodes().add(n);
				if(n.name.equals(Board.TRUCK_HOME_NAME))
					setTruckHome(n);
			}
		}
		//Read in all edges of board. Precondition - all nodes already read in
		for(String key : obj.keySet()){
			if(key.startsWith(Board.EDGE_TOKEN)){
				JSONObject edgeJSON = obj.getJSONObject(key);
				JSONArray exitArr = edgeJSON.getJSONArray(BoardElement.LOCATION_TOKEN);

				int length = edgeJSON.getInt(BoardElement.LENGTH_TOKEN);
				Node firstExit = getNode((String)exitArr.get(0));
				Node secondExit = getNode((String)exitArr.get(1));

				Edge e = new Edge(this, firstExit, secondExit, length);
				getEdges().add(e);
				firstExit.addExit(e);
				secondExit.addExit(e);
			}
		}
		//board reading finished.

		//Read in the trucks and parcels - precondition - all nodes already read
		for(String key : obj.keySet()){
			if (key.startsWith(TRUCK_TOKEN)){
				JSONObject truck = obj.getJSONObject(key);
				Color c = new Color(truck.getInt(BoardElement.COLOR_TOKEN));
				String name = truck.getString(BoardElement.NAME_TOKEN);
				Truck t = new Truck(game, name, c, getTruckHome());
				trucks.add(t);
			} else if( key.startsWith(PARCEL_TOKEN)){
				JSONObject parcel = obj.getJSONObject(key);
				Color c = new Color(parcel.getInt(BoardElement.COLOR_TOKEN));
				Node start = getNode(parcel.getString(BoardElement.LOCATION_TOKEN));
				Node dest = getNode(parcel.getString(BoardElement.DESTINATION_TOKEN));

				Parcel p = new Parcel(this, start, dest, c);
				parcels.add(p);
				start.addParcel(p);
			}
		}
		initParcelTruckCounts();
		updateMinMaxLength();
	}

	/** Initializes the parcelCount list - call during construction */
	private void initParcelTruckCounts(){
		parcelCounts = Collections.synchronizedList(new ArrayList<Integer>(3));
		parcelCounts.add(parcels.size());
		parcelCounts.add(0);
		parcelCounts.add(0);
	}

	/** Returns the number of parcels on this board that are not being caried by trucks */
	public int getOnNodeParcels(){
		return parcelCounts.get(PARCELS_ON_MAP);
	}

	/** Returns the number of parcels on this board being carried by trucks */
	public int getOnTruckParcels(){
		return parcelCounts.get(PARCELS_ON_TRUCK);
	}

	/** Returns the number of parcels from this board that have been successfully delivered */
	public int getDeliveredParcels(){
		return parcelCounts.get(PARCELS_DELIVERED);
	}
	
	/** Returns a random node in this board */
	public Node getRandomNode(){
		return Main.randomElement(nodes);
	}

	/** Returns a random edge in this board */
	public Edge getRandomEdge(){
		return Main.randomElement(edges);
	}

	/** Returns a HashSet containing all the Nodes in this board. Allows addition and removal of Nodes to this board */
	public HashSet<Node> getNodes(){
		return nodes;
	}

	/** Returns the number of Nodes in this board */
	public int getNodesSize(){
		return nodes.size();
	}

	/** Returns the Node named {@code name} in this board if it exists, null otherwise */
	public Node getNode(String name){
		for(Node n : nodes){
			if(n.name.equals(name))
				return n;
		}

		return null;
	}

	/** Returns the TruckHome node in this board that Trucks must return to before the game can be ended. */
	public Node getTruckHome(){
		return truckHome;
	}

	/** Sets the TruckHome node that Trucks must return to before the game can be ended to Node n
	 * @throws IllegalArgumentException - if n is not in this board
	 */
	protected void setTruckHome(Node n) throws IllegalArgumentException{
		if(nodes.contains(n))
			truckHome = n;
		else
			throw new IllegalArgumentException("Can't set Truck Home to a Node that isn't contained in this board.");
	}

	/** Returns the trucks available for use in this board */
	public ArrayList<Truck> getTrucks(){
		return trucks;
	}

	/** Returns the trucks in this board that are currently on the Truck Home node */
	public ArrayList<Truck> getTrucksHome(){
		ArrayList<Truck> homeTrucks = new ArrayList<Truck>();
		for(Truck t : trucks)
			if(t.getLocation() != null && t.getLocation().equals(getTruckHome()))
				homeTrucks.add(t);

		return homeTrucks;
	}

	/** Returns true if any alive Truck in this board is currently on the TruckHome node, false otherwise 
	 */
	public boolean isTruckHome(){
		for(Truck t : getTrucks())
			if(t.getLocation() != null && t.getLocation().equals(getTruckHome()))
				return true;

		return false;
	}

	/** Returns true if all alive Truck in this board are currently on the TruckHome node, false otherwise 
	 */
	public boolean isAllTrucksHome(){
		try{
			for(Truck t : getTrucks()){
				if(t.getLocation() == null || ! t.getLocation().equals(getTruckHome()))
					return false;
			}

			return true;
		}catch(NullPointerException e){
			for(Truck t : getTrucks()){
				System.out.print(t + " : ");
				if(t != null)
					System.out.print(t.getLocation() + ", ");
				if(t != null && t.getLocation() != null)
					System.out.print(t.getLocation().equals(getTruckHome()));
				System.out.println();
			}
			return false;
		}
	}

	/** Returns the parcels in this board. Returned parcels are parcels that have not yet been delivered. */
	public Set<Parcel> getParcels(){
		return parcels;
	}

	/** Called by Trucks to drop off parcels at nodes 
	 * @param p - The Parcel to deliver. Must be currently held by Truck t
	 * @param n - The Node to deliver the parcel to. Must be Parcel p's final destination
	 * @param t - The Truck that is delivering Parcel p. Must currently be holding p and be at n
	 * @throws IllegalArgumentException - if any of the above parameter requirements aren't met*/
	protected void deliverParcel(Parcel p, Node n, Truck t){
		if(p.destination != n)
			throw new IllegalArgumentException("Parcel " + p + "'s final destination is not " + n.name + ". Cannot Deliver Here");
		if(t.getLocation() != n)
			throw new IllegalArgumentException("Truck " + t + "Is not currently at " + n.name + ". Cannot Deliver Here");
		if(t.getLoad() != p)
			throw new IllegalArgumentException("Truck " + t + "Is not currently holding Parcel " + p + ". Cannot Deliver Here");

		if(t.getColor().equals(p.getColor()))
			t.getManager().getScoreObject().changeScore(PAYOFF * ON_COLOR_MULTIPLIER);
		else
			t.getManager().getScoreObject().changeScore(PAYOFF);

		parcels.remove(p);
		n.removeParcel(p);
		if(game.getGUI() != null) game.getGUI().getDrawingPanel().remove(p.getCircle());
	}

	/** Returns a HashSet containing all the Edges in this board. Allows addition and removal of Edges to this board */
	public HashSet<Edge> getEdges(){
		return edges;
	}

	/** Returns the number of Edges in this board */
	public int getEdgesSize(){
		return edges.size();
	}

	/** Returns true if there is any intersection of the lines drawn by the
	 * edges in edges, false otherwise.
	 * 
	 * Used for GUI intersection detection, not useful outside of the GUI context.
	 * Has nothing to say about the non-GUI version of the board
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
			minLength = Math.min(minLength, e.length);
			maxLength = Math.max(maxLength, e.length);
		}
	}

	/** Returns the maximum length of all edges on the board */
	public int getMaxLength(){
		return maxLength;
	}

	/** Returns the minimum length of all edges on the board */
	public int getMinLength(){
		return minLength;
	}

	/** Returns a 2x1 array of edges that have lines that intersect
	 * If no two edges intersect, returns null.
	 * 
	 * Used for GUI intersection detection, not useful outside of the GUI context.
	 * Has nothing to say about the non-GUI version of the board
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


	/** Returns a String representation of this board, including edges and nodes */
	@Override
	public String toString(){
		String output = "";
		Iterator<Node> nodesIterator = nodes.iterator();
		while(nodesIterator.hasNext()){
			Node n = nodesIterator.next();
			output += n + "\t";
			Iterator<Edge> roadsIterator = n.getTrueExits().iterator();
			while(roadsIterator.hasNext()){
				Edge r = roadsIterator.next();
				output += r.getOther(n).name+"-"+r.length;
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

	/** Returns a JSON-compliant version of toString().
	 * A full serialized version of the board, including:
	 * > Seed
	 * > Cost constants
	 * > Nodes
	 * > Edges
	 * > Trucks
	 * > Parcels */
	@Override
	public String toJSONString() {		
		String s = "{\n" + Main.addQuotes(SEED_TOKEN) + ":" + seed +",\n";
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
	/////////////////////////////////////// Random board Generation ////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Library for random board generation.
	 * Implemented inside board class to allow construction based on these methods.
	 * @author eperdew
	 * 
	 * Node placement and Edge connections are done using the Delaunay Triangulation Method:
	 * http://en.wikipedia.org/wiki/Delaunay_triangulation 
	 * 
	 * @author MPatashnik
	 * 
	 */

	private static final int MIN_NODES = 5;
	private static final int MAX_NODES = 20;

	private static final double AVERAGE_DEGREE = 2.5;
	private static final int MIN_EDGE_LENGTH = 5;
	private static final int MAX_EDGE_LENGTH = 70;

	private static final int WIDTH = GUI.DRAWING_BOARD_WIDTH - Circle.DEFAULT_DIAMETER * 3;
	private static final int HEIGHT = GUI.DRAWING_BOARD_HEIGHT - Circle.DEFAULT_DIAMETER * 3;

	private static final int MIN_TRUCKS = 2;
	private static final int MAX_TRUCKS = 50;

	private static final int MIN_PARCELS = 10;
	private static final int MAX_PARCELS = 100;

	private static final int WAIT_COST_MIN = 1;
	private static final int WAIT_COST_MAX = 3;

	private static final int PICKUP_COST_MIN = 0;
	private static final int PICKUP_COST_MAX = 150;

	private static final int DROPOFF_COST_MIN = 0;
	private static final int DROPOFF_COST_MAX = 150;

	private static final int PAYOFF_MIN = 3000;
	private static final int PAYOFF_MAX = 10000;

	private static final int ON_COLOR_MULTIPLIER_MIN = 1;
	private static final int ON_COLOR_MULTIPLIER_MAX = 5;

	/**
	 * Returns a new random board seeded via random seed.
	 */
	public static Board randomBoard(Game g) {
		return randomBoard(g, (long)(Math.random() * Long.MAX_VALUE));
	}

	/** Returns a new random board seeded with {@code seed} */
	public static Board randomBoard(Game g, long seed) {
		return new Board(g, new Random(seed), seed);
	}

	/** Returns a new random board using the {@code Random} parameter {@code r} */
	private Board(Game g, Random r, long seed) {
		this.seed = seed;
		game = g;

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
		//Create nodes and add to board
		for (int i = 0; i < numCities; i++) {
			String name;
			if(i == 0){
				name = Board.TRUCK_HOME_NAME;
			} else{
				name = cities.remove(r.nextInt(cities.size()));
			}
			Node n = new Node(this, name, null);
			Circle c = n.getCircle();
			c.setX1(-Circle.DEFAULT_DIAMETER); 
			c.setY1(-Circle.DEFAULT_DIAMETER);
			while(c.getX1() == -Circle.DEFAULT_DIAMETER || c.getY1() == -Circle.DEFAULT_DIAMETER){
				//Try setting to a new location
				c.setX1(r.nextInt(WIDTH + 1) + Circle.DEFAULT_DIAMETER * 2);
				c.setY1(r.nextInt(HEIGHT + 1) + Circle.DEFAULT_DIAMETER * 2);
				//Check other existing nodes. If too close, re-randomize this node's location
				for(Node n2 : getNodes()){
					if(n2.getCircle().getDistance(c) < Circle.BUFFER_RADUIS){
						c.setX1(-Circle.DEFAULT_DIAMETER);
						c.setY1(-Circle.DEFAULT_DIAMETER);
						break;
					}
				}
			}
			getNodes().add(n);
			if(n.name.equals(Board.TRUCK_HOME_NAME)){
				setTruckHome(n);
			}
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
			start.addParcel(p);
		}

		spiderwebEdges(r);
		initParcelTruckCounts();
		updateMinMaxLength();
	}

	/** Creates an edge with a random length that connects the two given nodes,
	 * and adds to the correct collections. Returns the created edge
	 */
	private Edge addEdge(Random r, Node n1, Node n2){
		int length = r.nextInt(MAX_EDGE_LENGTH - MIN_EDGE_LENGTH + 1) + MIN_EDGE_LENGTH;
		Edge e = new Edge(this, n1, n2, length);
		getEdges().add(e);
		n1.addExit(e);
		n2.addExit(e);
		return e;
	}

	/** Maximum number of attempts to get to average node degree */
	private int MAX_EDGE_ITERATIONS = 1000;

	/** Creates a spiderweb of edges by creating concentric hulls,
	 * then connecting between the hulls.
	 * Creates a connected, planar graph */
	private void spiderwebEdges(Random r){
		HashSet<Node> nodes = new HashSet<Node>();
		nodes.addAll(getNodes());
		ArrayList<HashSet<Node>> hulls = new ArrayList<>();

		//Create hulls, add edges
		while(! nodes.isEmpty()){
			HashSet<Node> nds = addGiftWrapEdges(r, nodes);
			hulls.add(nds);
			for(Node n : nds){
				nodes.remove(n);
			}
		}
		//At this point, there are either 2*n edges or 2*n - 1 edges, depending
		//if the inner most hull had a polygon in it or not.

		//Connect layers w/ random edges - try to connect each node to its closest on the surrounding hull
		//Guarantees that the map is connected after this step
		for(int i = 0; i < hulls.size() - 1; i++){
			for(Node n : hulls.get(i+1)){
				Node c = Collections.min(hulls.get(i), new DistanceComparator(n));
				if(! lineCrosses(n, c)){
					addEdge(r, n, c);
				}
			}
		}

		//Create a hashmap of node -> hull the node is in within hulls.
		HashMap<Node, Integer> hullMap = new HashMap<>();
		for(int i = 0; i < hulls.size(); i++){
			for(Node n : hulls.get(i)){
				hullMap.put(n,i);
			}
		}
		final int maxHull = hulls.size() - 1;

		int iterations = 0;

		while(getEdges().size() < getNodes().size() * AVERAGE_DEGREE){
			//Get random node
			Node n = randomElement(getNodes(), r);
			int hull = hullMap.get(n);
			//Try to connect to a node on the hull beyond this one.
			if(hull < maxHull){
				for(Node c : hulls.get(hull + 1)){
					if(! lineCrosses(n,c) && ! n.isConnectedTo(c)){
						addEdge(r,n,c);
						break;
					}
				}
			}
			//Try to connect to a node on the hull outside this one
			if(hull > 0){
				for(Node c : hulls.get(hull - 1)){
					if(! lineCrosses(n,c) && ! n.isConnectedTo(c)){
						addEdge(r,n,c);
						break;
					}
				}
			}
			iterations++;
			if(iterations == MAX_EDGE_ITERATIONS) break;
		}

		//Fix triangulation such that it's cleaner.
		delunayTriangulate(r);
	}

	/** Gift wraps the nodes - creates a concentric set of edges that surrounds
	 * the set of nodes passed in, with random edge lengths.
	 * Returns a set of nodes that is the nodes involved in the giftwrapping */
	private HashSet<Node> addGiftWrapEdges(Random r, HashSet<Node> nodes){
		HashSet<Node> addedNodes = new HashSet<Node>();
		//Base case - 0 or 1 node. Nothing to do.
		if(nodes.size() <= 1){
			addedNodes.add(nodes.iterator().next());
			return addedNodes;
		}

		//Base case - 2 nodes. Add the one edge connecting them and return.
		if(nodes.size() == 2){
			Iterator<Node> n = nodes.iterator();
			Node n1 = n.next();
			Node n2 = n.next();
			addEdge(r, n1, n2);
			addedNodes.add(n1);
			addedNodes.add(n2);
			return addedNodes;
		}

		//Non base case - do actual gift wrapping alg
		Node first = Collections.min(nodes, xComp);
		Node lastHull = first;
		Node endpoint = null;
		do{
			for(Node n : nodes){
				if(endpoint == null || n != lastHull && isLeftOfLine(lastHull, endpoint, n) 
						&& ! lastHull.isConnectedTo(n)){
					endpoint = n;
				}
			}

			addEdge(r, lastHull, endpoint);
			addedNodes.add(lastHull);

			lastHull = endpoint;
		}while(lastHull != first);

		return addedNodes;
	}

	/** Returns true if e2 is left of the line start -> e1, false otherwise.
	 * Helper for giftwrapping method */
	private boolean isLeftOfLine(Node start, Node e1, Node e2){
		Vector a = start.getCircle().getVectorTo(e1.getCircle());
		Vector b = start.getCircle().getVectorTo(e2.getCircle());
		return Vector.cross(a, b) <= 0;
	}

	/** Returns true if the line that would be formed by connecting the two given nodes
	 * crosses an existing edge, false otherwise.
	 * Helper for giftwrappign and spiderwebbing methods
	 */
	private boolean lineCrosses(Node n1, Node n2){
		Line l = new Line(n1.getCircle(), n2.getCircle(), null);
		for(Edge e : getEdges()){
			if(l.intersects(e.getLine()))
				return true;
		}
		return false;
	}

	/** Fixes (psuedo) triangulation via the delunay method.
	 * Alters the current edge set so that triangles are less skinny */
	private void delunayTriangulate(Random r){

		final double FLIP_CONDITION = Math.PI; //Amount of radians that angle sum necessitates switch

		HashMap<Edge, Node[]> needsFlip = new HashMap<>(); //Edge that should be removed, mapped to its new exits

		for(Node n1 : getNodes()){
			for(Edge e2 : n1.getTrueExits()){
				Node n2 = e2.getOther(n1);
				if(n2 != n1){
					for(Edge e3 : n1.getTrueExits()){
						Node n3 = e3.getOther(n1);
						if(n3 != n2 && n3 != n1){
							for(Edge e4 : n1.getTrueExits()){
								Node n4 = e4.getOther(n1);
								if(n4 != n3 && n4 != n2 && n4 != n1){
									//Check all triangulated quads - n1 connected to n2, n3, n4; n2 and n3 each connected to n4.
									//We already know that n1 is connected to n2, n3, n4.
									//Check other part of condition.
									if(n2.isConnectedTo(n4) && n3.isConnectedTo(n4)){
										//This is a pair of adjacent triangles. 
										//Check angles to see if flip should be made
										Edge e24 = n2.getConnect(n4);
										Edge e34 = n3.getConnect(n4);
										if(e2.getLine().radAngle(e24.getLine())
												+ e3.getLine().radAngle(e34.getLine()) < FLIP_CONDITION){
											//Store the dividing edge as needing a flip
											Node[] newExits = {n2, n3};
											needsFlip.put(e4, newExits);
										}
									}
								}
							}
						}
					}
				}
			}
		}

		for(Entry<Edge, Node[]> e : needsFlip.entrySet()){
			//Remove old edge
			getEdges().remove(e.getKey());

			Node oldFirst = e.getKey().getFirstExit();
			Node oldSecond = e.getKey().getSecondExit();

			oldFirst.removeExit(e.getKey());
			oldSecond.removeExit(e.getKey());

			Node newFirst = e.getValue()[0];
			Node newSecond = e.getValue()[1];

			//Add new edge if it doesn't cross an existing edge
			if(! lineCrosses(newFirst, newSecond)){
				addEdge(r, newFirst, newSecond);
			}
			//Otherwise, put old edge back
			else{
				addEdge(r, oldFirst, oldSecond);
			}
		}
	}

	/** Allows for sorting of Collections of Nodes by their gui distance to
	 * each of the nodes in collection n.
	 * The node that is closest in the collection to the given node is the one that counts.
	 * @author MPatashnik
	 *
	 */
	private static class DistanceComparator implements Comparator<Node> {
		/** The node to which distance is compared */
		protected final Node node;

		@Override
		public int compare(Node n1, Node n2){
			double d = node.getCircle().getDistance(n1.getCircle()) - node.getCircle().getDistance(n2.getCircle());
			if(d < 0) return -1;
			if(d > 0) return 1;
			return 0;
		}

		DistanceComparator(Node node){
			this.node = node;
		}
	}

	/** An instance of the XComparator for sorting nodes. No real need to instantiate another one */
	private final static XComparator xComp = new XComparator();

	/** Allows for sorting a Collection of Nodes by the x coordinate.
	 * No need to instantiate beyond the xcomparator instantiated above */
	private static class XComparator implements Comparator<Node>{
		@Override
		public int compare(Node n1, Node n2){
			return n1.getCircle().getX1() - n2.getCircle().getX1();
		}
	}

	/** Returns a random element from the given collection using the given randomer */
	private static <T> T randomElement(Collection<T> elms, Random r){
		if(elms.isEmpty())
			return null;

		Iterator<T> it = elms.iterator();
		T val = null;
		int rand = r.nextInt(elms.size()) + 1;
		for(int i = 0; i < rand; i++){
			val = it.next();
		}
		return val;
	}

	/** Returns an array of the city names listed in BoardGeneration/cities.txt */
	private static ArrayList<String> cityNames(){
		File f = new File("BoardGeneration/cities.txt");
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
				if(! line.equals(""))
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