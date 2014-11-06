package solution;

import java.util.*;
import game.*;

/** Abstract class AbstractSolution extends Manager.
 * 	The instructor solutions should all extend this class.
 * 	An implementation of Dijkstra's algorithm is included.
 * 
 * @author Sandra Anderson
 */
public abstract class AbstractSolution extends Manager {

	/** An instance contains information about a node: the previous
	 * node on a path from the start node to this node and the distance
	 * of this node from the start node. */
	private static class NodeInfo {
		private Node previous;
		private int distFromStart;

		/** Constructor: an instance with previous node p and distance d from
		 * the start node.*/
		private NodeInfo(Node p, int d) {
			previous = p;
			distFromStart = d;
		}

		/** Constructor: an instance with a null previous node and distance 0. */
		private NodeInfo() {}
	}

	/** Find the shortest path from start to end, or the empty list
	 *  if one does not exist. Uses Dijkstra's algorithm for a 
	 *  weighted, bidirectional graph. 
	 * 	
	 * @param start The path start node.
	 * @param end The end node. 
	 * @return A linked list containing every node on the shortest path,
	 * including the start and the end. Return the empty list
	 * if no path exists. 
	 */
	protected static LinkedList<Node> dijkstra(Node start, Node end) {
		MinHeap<Node> frontier = new HeapSolution<Node>();
		HashMap<Node, NodeInfo> nodeInfo = new HashMap<Node, NodeInfo>();

		frontier.add(start, 0);
		nodeInfo.put(start, new NodeInfo());

		while (!frontier.isEmpty()) {
			Node current = frontier.poll();
			if (current.equals(end)) {
				return reconstructPath(current, nodeInfo);
			}

			NodeInfo currentInfo = nodeInfo.get(current);
			HashMap<Node,Integer> neighbors = current.getNeighbors();

			for (Map.Entry<Node, Integer> entry : neighbors.entrySet()) {
				Node neighbor = entry.getKey();
				int edgeWeight = entry.getValue();
				int newDistToNeighbor = currentInfo.distFromStart + edgeWeight;

				NodeInfo neighborInfo = nodeInfo.get(neighbor);
				boolean neverSeen = neighborInfo == null;
				boolean needToUpdate = neighborInfo != null && newDistToNeighbor < neighborInfo.distFromStart;

				if (neverSeen) {
					neighborInfo = new NodeInfo();
					nodeInfo.put(neighbor, neighborInfo);
					frontier.add(neighbor, newDistToNeighbor);
				} else if (needToUpdate) {
					frontier.updatePriority(neighbor, newDistToNeighbor);
				}

				if (neverSeen || needToUpdate) {
					neighborInfo.previous = current;
					neighborInfo.distFromStart = newDistToNeighbor;
				}
			}
		}
		return new LinkedList<Node>(); //no path was found
	}

	/** Return the path from the start node to end.
	 * Precondition: nodeInfo contains all the necessary information about
	 * the path. */
	private static LinkedList<Node> reconstructPath(Node end, HashMap<Node, NodeInfo> nodeInfo) {
		LinkedList<Node> path = new LinkedList<Node>();
		Node current = end;
		while (current != null) {
			path.addFirst(current);
			current = nodeInfo.get(current).previous;
		}
		return path;
	}

	/** Return the collective weight of the given path of nodes
	 * by iterating along it and summing the weight of edges encountered. */
	protected int pathLength(LinkedList<Node> path) {
		synchronized(path){
			int s = 0;
			Iterator<Node> one = path.iterator();
			Iterator<Node> two = path.iterator();
			two.next(); //Advance two by one link

			while (two.hasNext()) {
				Node n1 = one.next();
				Node n2 = two.next();
				s += n1.getConnect(n2).length;
			}
			return s;
		}
	}
}
