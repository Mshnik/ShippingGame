package solution;

import java.util.LinkedList;

import game.Node;

/**	An instance wraps a Node, including that node's path
 * 	from some starting node, and the distance of that path. 
 * 
 * @author Sandra Anderson
 *
 */
public class NodeWrapper implements  Comparable<NodeWrapper> {

	final Node node;
	//by convention, this contains both the starting node and this node
	private LinkedList<Node> path; 
	private int distance;
	
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
	
	/** Returns the path to this node. */
	LinkedList<Node> getPath(){
		return path;
	}
	
	/** Adds the specified node to this node's path. */
	void addToPath(Node n){
		path.add(n);
	}
	
	/** Adds the contents of the given list to this node's path, in order. */
	void addToPath(LinkedList<Node> l){
		path.addAll(l);
	}
	
	/** Resets this node's path to the empty list. */
	void setPath(){
		path = new LinkedList<Node>();
	}
	
	/** Resets the path to "reset" so that it passes through "current"
	 * Preconditions: an edge exists between "reset" and "current"
	 */
	void resetPath(NodeWrapper through){
		path = new LinkedList<Node>();
		path.addAll(through.getPath());
		path.add(node);
	}
	
	/** Returns the distance to this node. */
	int getDistance(){
		return distance;
	}
	
	/** Returns true if the given distance is shorter
	 * 	than the distance currently in this NodeWrapper,
	 * 	and false otherwise. 
	 */
	boolean checkDistance(int d){
		return (d < distance);
	}
	
	/** Sets the distance of this node to d */
	void setDistance(int d){
		distance = d;
	}
	
	/**	Returns a negative, zero, or positive integer
	 * 	as this NodeWrapper is less than, equal to, or
	 * 	greater than the given NodeWrapper. Comparison is 
	 * 	made on distance.  
	 */
	public int compareTo(NodeWrapper w){
		return distance - w.distance; 
	}
	
	/**	Returns true if and only if this node equals the 
	 * 	given object. That object must also be a node-wrapper,
	 * 	and the nodes they wrap must also be equal. 
	 */
	public @Override boolean equals(Object obj){
		if(obj instanceof NodeWrapper){
			return node.equals(((NodeWrapper) obj).node);
		}
		return false;
	}

}
