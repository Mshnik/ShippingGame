package game;

import java.util.HashSet;
import java.util.Iterator;

import org.json.JSONString;

/** The Map Class is a container for the HashSets of Edges and Nodes that make up
 * the playing field of the game. It also allows for easy access to random nodes and edges.
 * @author MPatashnik
 *
 */
public class Map implements JSONString{

	private Node truckHome;			//The node at which all trucks start
	protected static final String TRUCK_HOME_NAME = "Truck Depot"; //Name of truckhome

	private HashSet<Edge> edges;    //All edges in this map
	
	private int minLength;			//Min length among all edges
	private int maxLength;			//Max length among all edges
	
	private HashSet<Node> nodes;    //All nodes in this map
	
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
	
	/** Constructor. initializes blank edges and nodes fields.
	 * Uses given scoreCosts array to initialize final score fields.
	 * Input must have length 5 and be in order wait_cost, pickup_cost, dropoff_cost,
	 * payoff, on_color_multiplier */
	protected Map(int[] scoreCosts) throws IllegalArgumentException{
		if(scoreCosts == null || scoreCosts.length != 5)
			throw new IllegalArgumentException("Bad ScoreCosts Array for Map Construction:" + scoreCosts);
		
		WAIT_COST = scoreCosts[0];
		PICKUP_COST = scoreCosts[1];
		DROPOFF_COST = scoreCosts[2];
		PAYOFF = scoreCosts[3];
		ON_COLOR_MULTIPLIER = scoreCosts[4];
		
		edges = new HashSet<Edge>();
		nodes = new HashSet<Node>();
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

	
	protected static final String SCORE_TOKEN = "scoreCoeff";
	protected static final String NODE_TOKEN = "node-";
	protected static final String EDGE_TOKEN = "edge-";
	
	@Override
	/** Returns a JSON-compliant version of toString() */
	public String toJSONString() {
		String s = "{\n";
		s += Main.addQuotes(SCORE_TOKEN) + ":[" + WAIT_COST + "," 
			+ PICKUP_COST + "," + DROPOFF_COST + "," + PAYOFF + "," + ON_COLOR_MULTIPLIER + "],";
		int i = 0;
		for(Node n : nodes){
			s += "\n" + Main.addQuotes(NODE_TOKEN + i) + ":" + n.toJSONString() + ",";
			i++;
		}
		i = 0;
		for(Edge e : edges){
			s += "\n" + Main.addQuotes(EDGE_TOKEN + i) + ":" + e.toJSONString();
			if(i < edges.size() - 1)
				s += ",";
			i++;
		}	
		return s + "\n}";
	}
}