package solution;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import game.Manager;
import game.Node;
import game.MinHeap;

/** AbstractSolution is an abstract class which extends Manager.
 * 	The instructor solutions should all extend this class.
 * 	An implementation of Dijkstra's algorithm is included.
 * 
 * @author Sandra Anderson
 *
 */
public abstract class AbstractSolution extends Manager {

	private static class NodeInfo {
		private Node previous;
		private int distFromStart;

		private NodeInfo(Node previous, int distFromStart) {
			this.previous = previous;
			this.distFromStart = distFromStart;
		}

		private NodeInfo() {}
	}

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
	protected static LinkedList<Node> dijkstra(Node start, Node end) {
		MinHeap<Node> queue = new HeapSolution<Node>();
		HashMap<Node, NodeInfo> nodeInfo = new HashMap<Node, NodeInfo>();

		queue.add(start, 0);
		nodeInfo.put(start, new NodeInfo());
		
		while (!queue.isEmpty()) {
			Node current = queue.poll();
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
					queue.add(neighbor, newDistToNeighbor);
				} else if (needToUpdate) {
					queue.updatePriority(neighbor, newDistToNeighbor);
				}

				if (neverSeen || needToUpdate) {
					neighborInfo.previous = current;
					neighborInfo.distFromStart = newDistToNeighbor;
				}
			}
		}
		return new LinkedList<Node>(); //no path was found
	}

	private static LinkedList<Node> reconstructPath(Node end, HashMap<Node, NodeInfo> nodeInfo) {
		LinkedList<Node> path = new LinkedList<Node>();
		Node current = end;
		while (current != null) {
			path.addFirst(current);
			current = nodeInfo.get(current).previous;
		}
		return path;
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
