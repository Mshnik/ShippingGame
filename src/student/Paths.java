/* Time spent on a7:  4 hours and 30 minutes.

 * Names: Shreya Sitaraman, Colin Budd
 * Netids: ss2643, cmb434
 * What I thought about this assignment: We expected this assignment to be easier than it actually was
 * because we thought we understood Dijkstra's algorithm pretty well, but it turns out that understanding
 * the algorithm and actually implementing it are two different beasts. We struggled a bit to figure out
 * what was going on at first, but once we got the grasp of that, it became a bit easier.
 */


package student;
import game.PQueue;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import game.Edge;
import game.Node;

/** This class contains Dijkstra's shortest-path algorithm and some other methods. */
@SuppressWarnings("unused")
public class Paths {


    /** Return a list of the nodes on the shortest path from start to end, or
     * the empty list (a list of size 0) if a path from start to end does not exist. */
    public static LinkedList<Node> dijkstra(Node start, Node end) {
        /*Implement Dijkstras's shortest-path algorithm presented
         in the slides titled "Final Algorithm" in the slides for lecture 19.
         In particular, a min-heap (as implemented in assignment A6) should be
         used for the frontier set. We provide a declaration of the frontier.

         Maintaining information about shortest paths will require maintaining
         for each node in the settled and frontier sets the backpointer for
         that node (as described in the handout) along with the length of the
         shortest path (thus far, for nodes in the frontier set). For this
         purpose, we provide static class NodeInfo. We leave it to you to
         declare the HashMap variable for this and describe carefully what it
         means. 

         Note 1: Do not attempt to create a data structure to contain the
             far-off set.
         Note 2: Read the list of notes on pages 2..3 of the handout carefully.
         */

        // The frontier set, as discussed in lecture
        MinHeap<Node> frontier= new MinHeap<Node>();
        
        HashMap<Node, NodeInfo> nodeInfo = new HashMap<Node, NodeInfo>();
        nodeInfo.put(start, new NodeInfo(null,0));
        frontier.add(start, 0);
        while (!frontier.isEmpty() && frontier.peek()!=end){
        	Node f = frontier.poll();
        	for (Edge e : f.getExits()){
        		if (!nodeInfo.containsKey(e.getOther(f))){
        			frontier.add(e.getOther(f), nodeInfo.get(f).distance + e.length);
        			nodeInfo.put(e.getOther(f), new NodeInfo(f, nodeInfo.get(f).distance + e.length));
        		}
        		else{
        			Node n = e.getOther(f);
        			int flength = Math.min(nodeInfo.get(n).distance, nodeInfo.get(f).distance + e.length);
        			if (nodeInfo.get(n).distance > flength){
        				nodeInfo.put(n, new NodeInfo(f, flength));
        				frontier.updatePriority(n, flength);
        			}	
        		}
        	}
        }
        return buildPath(end, nodeInfo);
    }

    /** Return the path from the start node to end.
     * Precondition: nodeInfo contains all the necessary information about
     * the path. */
    public static LinkedList<Node> buildPath(Node end, HashMap<Node, NodeInfo> nodeInfo) {
        LinkedList<Node> path= new LinkedList<Node>();
        Node p= end;
        while (p != null) {
            path.addFirst(p);
            p= nodeInfo.get(p).backPointer;
            
        }
        return path;
    }

    /** Return the sum of the weight of the edges on path p. */
    public static int pathLength(LinkedList<Node> path) {
        synchronized(path){
            if (path.size() == 0) return 0;

            Iterator<Node> iter= path.iterator();
            Node p= iter.next();  // First node on path
            int s= 0;
            // invariant: s = sum of weights of edges from start up to p
            while (iter.hasNext()) {
                Node q= iter.next();
                s= s + p.getConnect(q).length;
                p= q;
            }
            return s;
        }
    }

    /** An instance contains information about a node: the previous
     * node on a shortest path from the start node to this node and the distance
     * of this node from the start node. */
    private static class NodeInfo {
        private Node backPointer;
        private int distance;

        /** Constructor: an instance with distance d from the start node and
         * backpointer p.*/
        private NodeInfo(Node p, int d) {
            backPointer= p;  // Backpointer on the path (null if start node)
            distance= d; //Distance from start node to this one.
        }

        /** Constructor: an instance with a null previous node and distance 0. */
        private NodeInfo() {}

        /** return a representation of this instance. */
        public String toString() {
            return "distance " + distance + ", bckptr " + backPointer;
        }
    }


}
