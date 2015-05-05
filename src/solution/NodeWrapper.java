package solution;

import game.Node;
import java.util.LinkedList;

/**	An instance wraps a Node, including that node's path
 * 	from some start node and the distance of that path. 
 * 
 * @author Sandra Anderson
 *
 */
public class NodeWrapper implements  Comparable<NodeWrapper> {

	final Node node;
	//by convention, this contains both the starting node and this node
	private LinkedList<Node> path; 
	private int distance;

	/** Constructor: an instance for node n. The distance is MAX_VALUE and
	 * the path contains only the starting node. */
	NodeWrapper(Node n) {
		node = n;
		distance = Integer.MAX_VALUE;
		path = new LinkedList<Node>();
	}

	NodeWrapper(Node n, int d, NodeWrapper pathThrough){
		node = n;
		distance = d;
		resetPath(pathThrough);
	}

	/** Return the path to this node. */
	LinkedList<Node> getPath(){
		return path;
	}

	/** Add n to this node's path. */
	void addToPath(Node n){
		path.add(n);
	}

	/** Add the contents of list to this node's path, in order. */
	void addToPath(LinkedList<Node> list){
		path.addAll(list);
	}

	/** Reset this node's path to the empty list. */
	void setPath(){
		path = new LinkedList<Node>();
	}

	/** Reset the path to "reset" so that it passes through "current"
	 * Preconditions: an edge exists between "reset" and "current"
	 */
	void resetPath(NodeWrapper through){
		path = new LinkedList<Node>();
		path.addAll(through.getPath());
		path.add(node);
	}

	/** Return the distance to this node. */
	int getDistance(){
		return distance;
	}

	/** Return true iff d is shorter than the distance currently in
	 * this NodeWrapper.  */
	boolean checkDistance(int d){
		return d < distance;
	}

	/** Set the distance of this node to d */
	void setDistance(int d){
		distance = d;
	}

	/**	Return a negative, zero, or positive integer as this NodeWrapper
	 * distance is less than, equal to, or greater than w's distance. */
	public int compareTo(NodeWrapper w){
		return distance - w.distance; 
	}

	/**	Return true iff obj is a NodeWrapper and this and obj wrap
	 * equal nodes. 
	 */
	public @Override boolean equals(Object obj){
		if (!(obj instanceof NodeWrapper)) return false;
		return node.equals(((NodeWrapper) obj).node);
	}

}
