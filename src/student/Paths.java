/* Time spent on a7:  hh hours and mm minutes.

 * Name:
 * Netid: 
 * What I thought about this assignment:
 *
 *
 */


package student;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import game.Edge;
import game.Node;

/** This class contains Dijkstra's shortest-path algorithm and some other methods. */
public class Paths {


    /** Return a list of the nodes on the shortest path from start to end,
     * or the empty list if one does not exist. */
    public static LinkedList<Node> dijkstra(Node start, Node end) {
        /* TODO Implement Dijkstras's shortest-path algorithm presented
         in the slides titled "Final Algorithm" in the slides for lecture 19.
         In particular, a min-heap (as implemented in assignment A6) should be
         used for the frontier set. We provide a declaration of the frontier.
         
         Maintaining information about shortest paths will require maintaining
         for each node in the settled and frontier sets the backpointer for
         that node, as described in the handout, along with the length of the
         shortest path (thus far, for nodes in the frontier set). For this
         purpose, we provide static class NodeInfo. We leave it to you to
         declare the HashMap variable for this and describe carefully what it
         means. 
         
         Note that the  */
        
        // The frontier set, as discussed in lecture
        MinHeap<Node> frontier= new MinHeap<Node>();

        // Each node in the Settled and Frontier sets has an entry
        // that gives its shortest distance from node start and the backpointer
        // of the node on a shortest path from node start.
        HashMap<Node, NodeInfo> nodeInfo= new HashMap<Node, NodeInfo>();

        frontier.add(start, 0);
        nodeInfo.put(start, new NodeInfo());
        
        // invariant: As presented in notes for Lecture 19
        while (!frontier.isEmpty()  &&  frontier.peek() != end) {
            Node f= frontier.poll();

            NodeInfo fInfo= nodeInfo.get(f);

            HashSet<Edge> edges= f.getExits();
            for (Edge edge : edges) {
                Node w= edge.getOther(f);
                NodeInfo wInfo= nodeInfo.get(w);
                int wDistance= fInfo.distance + edge.length;
                if (wInfo == null) {
                    frontier.add(w, wDistance);
                    nodeInfo.put(w, new NodeInfo(f, wDistance));
                } else 
                    if (wDistance < wInfo.distance) {
                        frontier.updatePriority(w, wDistance);
                        wInfo.distance= wDistance;
                        wInfo.backPointer= f;
                    }
            }
        }
        if (frontier.isEmpty()) {
            return new LinkedList<Node>(); //no path was found
        }
        Node p= frontier.peek();
        return buildPath(frontier.peek(), nodeInfo);
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
    public int pathLength(LinkedList<Node> path) {
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
