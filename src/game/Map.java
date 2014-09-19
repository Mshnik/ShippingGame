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

	private Node truckHome;
	protected static final String TRUCK_HOME_NAME = "Truck Depot";

	private HashSet<Edge> edges;
	private HashSet<Node> nodes;
	
	/** Constructor for use without an attached game. Sets game to null, initializes blank edges and nodes fields */
	protected Map(){
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

	protected static final String NODE_TOKEN = "node-";
	protected static final String EDGE_TOKEN = "edge-";
	
	@Override
	/** Returns a JSON-compliant version of toString() */
	public String toJSONString() {
		String s = "{";
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