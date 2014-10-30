package solution;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;

import game.Edge;
import game.Manager;
import game.Node;

/** AbstractSolution is an abstract class which extends Manager.
 * 	The instructor solutions should all extend this class.
 * 	An implementation of Dijkstra's algorithm is included.
 * 
 * @author Sandra Anderson
 *
 */
public abstract class AbstractSolution extends Manager{

	/** Finds the shortest path from start to end, or the empty list
	 *  if one does not exist. Uses Dijkstra's algorithm for a 
	 *  weighted, bidirectional graph. 
	 * 	
	 * @param start Where the path starts from.
	 * @param end Where the path ends. 
	 * @return A linked list containing every node on the shortest path,
	 * including the start and the end. Returns the empty list
	 * if no path exists. 
	 */
	protected static LinkedList<Node> dijkstra(Node start, Node end){
		PriorityQueue<NodeWrapper> queue = new PriorityQueue<NodeWrapper>();
		NodeWrapper current = new NodeWrapper(start);
		current.setDistance(0);
		current.addToPath(start);
		//a copy of the current contents of the priority queue, indexed by node
		HashMap<Node, NodeWrapper> unprocessed = new HashMap<Node, NodeWrapper>();
		
		//inv: current is the next node to process
		while(!current.node.equals(end)){
			HashSet<Edge> edges = current.node.getExits();
			//process edges out of current
			for(Edge e : edges){
				Node[] exits = e.getExits();
				//process the nodes of that exit
				for(Node n : exits){
					if (!n.equals(current.node)){
						NodeWrapper wrap = unprocessed.get(n);
						if(wrap == null){
							wrap = new NodeWrapper(n, current.getDistance() + e.length, current);
							unprocessed.put(n, wrap);
							queue.add(wrap);
						}else{
							boolean set = wrap.checkDistance(current.getDistance() + e.length);
							if(set){
								wrap.setDistance(current.getDistance() + e.length);
								wrap.resetPath(current);
								queue.remove(wrap);
								queue.add(wrap);
							}
						}
					}
				}
			}
			unprocessed.remove(current.node);
			current = queue.poll();
			if(current == null){
				return new LinkedList<Node>(); //no path was found
			}
		}
		return current.getPath();
	}
	
	/** Returns the collective weight of the given path of nodes
	 * by iterating along it and summing the weight of edges encountered */
	protected int pathLength(LinkedList<Node> path){
		int s = 0;
		Iterator<Node> one = path.iterator();
		Iterator<Node> two = path.iterator();
		two.next(); //Advance two by one link
		
		while(two.hasNext()){
			Node n1 = one.next();
			Node n2 = two.next();
			s += n1.getConnect(n2).length;
		}
		return s;
	}
}
