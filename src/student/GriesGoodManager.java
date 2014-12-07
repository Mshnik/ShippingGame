package student;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import game.Board;
import game.Edge;
import game.Game;
import game.Manager;
import game.Node;
import game.Parcel;
import game.Truck;

public class GriesGoodManager extends Manager {
    // True iff preprocessing in run() is done and truck notifications can be handled
    boolean preprocessingDone;

    // Number of truckNotification calls before preprocessingDone is done
    Integer preCalls= 0;

    Game game;    // The game
    Board board; // The board on which the game is played
    Set<Parcel> parcels; // The parcels in the cities.
    ArrayList<Truck> trucks; // The trucks at the Truck Depot initially

    // Parcels that are not assigned to a Truck. It is a synchronized set
    Set<Parcel> unassignedParcels;

    /** The Truck is responsible for delivering the parcel it is carrying (if is
     * carrying one) AND all the Parcels in the LinkedList<Parcel> that is its
     * UserData. The Parcel it is carrying (if it is carrying one) is not in the
     * LinkedList.

     * A Truck has a Parcel only if it has a travel path or is at the Parcel's
     * destination --the travel path to get the Parcel to its destination.

     * If the LinkedList is not empty and the Truck has no Parcel, its
     * destination is the city of the first Parcel in the LinkedList. */


    /** An instance supervises Trucks picking up Parcels. */
    public GriesGoodManager() {
    }

    /** Supervise the Trucks in delivering parcels. This implementation attempts
     * to be more efficient than the basic way. 
     * 1. There is a collection of unassignedParcels.
     * 2. run() runs Dijkstra's algorithm until it has found ONE Parcel for
     *    EACH Truck to deliver (if possible)--these are the Parcel's closest to
     *    the Truck Depot. The Parcel is stored in the Truck's UserData as a
     *    LinkedList, a Truck is never assigned more than 1 Parcel at a time.
     *    This initial assignment attempts to assign each Parcel to Truck of
     *    the same color.
     * 3. In truckNotification(), when a Truck drops off a Parcel, Dijkstra's
     *    algorithm is called again to find the nearest unassigned Parcel and
     *    assign it to the Truck.
     */
    public @Override void run() {
        game= getGame();
        board= game.getBoard();

        unassignedParcels= Collections.synchronizedSet(board.getParcels());
        trucks= board.getTrucks();
        ArrayList<Truck> ts= new ArrayList<Truck>(); // local copy of trucks

        // Make each Truck's user data an empty list of Parcels
        // and add all Trucks to trucks
        for (Truck t : trucks) {
            ts.add(t);
            t.setUserData(new LinkedList<Parcel>());
        }

        //Assign Parcels to trucks.
        Dijkstra(board.getTruckDepot(), board.getTruckDepot(), ts);
        int sameColor= 0; //# Trucks assigned a Parcel of same Color

        //Store in sameColor the # of trucks carrying a Parcel of same color
        for (Truck t : trucks) {
            LinkedList<Parcel> p= (LinkedList<Parcel>)(t.getUserData());
            if (p.size() > 0  &&  t.getColor().equals(p.get(0).getColor())) {
                sameColor= sameColor + 1;
            }
        }
        System.out.println("GriesGoodManager: Number of trucks: " + trucks.size() +
                ". # assigned Parcels of same color: " + sameColor +
                ". " + "unassigned Parcels: " + unassignedParcels.size());

        if (trucks.size() > 0) {
            Truck t= trucks.get(0);
            LinkedList<Parcel> p= (LinkedList<Parcel>)(t.getUserData());

            System.out.println("End run() with Truck " + t.getTruckName() + 
                    " holding " + p.size() + " parcels");
        }

        preprocessingDone= true;
        System.out.println("Number of preprocessing waiting calls: " + preCalls);

    }

    /** Print out all shortest paths from Truck Depot. */
    public void testDijkstraAll() {
        Game game= getGame();
        Board board= game.getBoard();
        Node truckDepot= board.getTruckDepot();
        Set<Node> nodes= board.getNodes();
        for (Node node : nodes) {
            List<Node> path= Dijkstra(truckDepot, node, null);
            System.out.println("Path length: " + path.size() + ". " +
                    truckDepot + " to " + node  + ": " +
                    toString(path));
        }
    }

    /** Test Dijkstra for path length of 0 and 1, printing out paths and lengths. */
    public void testDijkstraSimple() {
        Game game= getGame();
        Board board= game.getBoard();
        Node truckDepot= board.getTruckDepot();
        List<Node> path= Dijkstra(truckDepot, truckDepot, null);
        Set<Edge> edges= truckDepot.getExits();
        //System.out.println("Path length: " + path.size() + ". " +
        //        truckDepot + " to " + truckDepot  + ": " + toString(path));
        for (Edge e : edges) {
            Node w= e.getOther(truckDepot);
            path= Dijkstra(truckDepot, w, null);
        }
    }


    public @Override void truckNotification(Truck t, Notification message) {
        if (!preprocessingDone) {
            synchronized(preCalls) {
                preCalls= preCalls + 1;
            }
            return;
        }
        if (message == Manager.Notification.LOCATION_CHANGED) {
            if (t.getLoad() != null  && t.getLocation().equals(t.getLoad().destination)) {
                System.out.println("Truck " + t.getTruckName() + " dropping " + 
                        t.getLoad() + " at " + t.getLocation());
                t.dropoffLoad();
                beginTravelToFirstParcelOrDepot(t);
                return;
            }
            if (t.getLoad() != null) { //Truck has a Parcel but is not at Parcel's destination
                return;
            }
            // Truck is not carrying a Parcel. Its destination is the start node
            // of first Parcel in its list --or the Truck Depot if the list is empty
            if (!isAtDestination(t)) {
                return;
            }
            // Truck is not carrying a Parcel and is at its destination
            LinkedList<Parcel> p= (LinkedList<Parcel>)t.getUserData();
            if (p.size() == 0) {// Destination is Truck Depot
                t.clearTravel();
                return;
            }
            pickUpParcel(t);
            return;
        }

        if (message != Manager.Notification.WAITING) return;

        if (t.getLoad() == null) {
            beginTravelToFirstParcelOrDepot(t);
            return;
        }

        System.out.println("At end of truckNotification");
    }

    /** If t's city has a Parcel of t's Color and t doesn't have a Parcel of t's
     *  color, then drop t's Parcel (if it has one) and pick up one with right color.
     */
    public boolean PossiblyChangeParcels(Truck t) {
        // TODO.  Will need synchronization.
        return false;
    }

    /** If t has no more parcel's to pick up, fix it to travel to the Truck Depot.
     *  Otherwise, fix it to travel to the first Parcel's start city.
     * Precondition: t does not have a travel path or a load. */
    public void beginTravelToFirstParcelOrDepot(Truck t) {
        //case 0: Truck has a parcel assigned to it.
        //case 1: Truck has no parcel assigned and there are no unassignedParcels
        //case 2: Truck has no parcel assigned and there are unassignedParcels
        LinkedList<Parcel> parcels= (LinkedList<Parcel>)t.getUserData();

        // Do case 0
        if (parcels.size() > 0) {
            Parcel p= parcels.peek();
            if (t.getLocation().equals(p.start)) { //Truck is at Parcel start
                p= parcels.poll();
                t.pickupLoad(p);
                List<Node> path= Dijkstra(p.start, p.destination, null);
                t.setTravelPath(path);
                System.out.println("In beginTravelToFirstParcelOrDepot Truck " + 
                        t.getTruckName() + " picked up " +
                        p + " at " + p.start + " heading for " + p.destination);
                return;
            }
            // Truck is not at parcel's start
            List<Node> path= Dijkstra(t.getLocation(), p.start, null);
            t.setTravelPath(path);
            System.out.println("In beginTravelToFirstParcelOrDepot Truck " + 
            t.getTruckName() + " at " + t.getLocation() + " going to get parcel " + p);
            return;
        }

        // Do case 1
        if (parcels.size() == 0  && unassignedParcels.size() == 0) {
            goToDepot(t);
            System.out.println("In beginTravelToFirstParcelOrDepot Truck " + 
            t.getTruckName() + " at : " + t.getLocation() + " going to home to Truck Depot");
            return;
        }

        // Do case 2: Truck has no parcel assigned and there may be unassignedParcels
        ArrayList<Truck> ts= new ArrayList<Truck>();
        ts.add(t);
        Dijkstra(t.getLocation(), null, ts);
        if (parcels.size() == 0) {
            goToDepot(t);
            System.out.println("In beginTravelToFirstParcelOrDepot Truck " + 
                    t.getTruckName() + " at : " + t.getLocation() + " going to home to Truck Depot");
                    return;
        }
        
        System.out.println("In beginTravelToFirstParcelOrDepot End. Truck " + t.getTruckName() + " parcel size " + parcels.size());
    }

    /** Return "t is at its destination". */
    public boolean isAtDestination(Truck t) {
        Parcel p= t.getLoad();
        if (p != null) {
            return t.getLocation().equals(p.destination);
        }
        LinkedList<Parcel> pl= (LinkedList<Parcel>)t.getUserData();
        if (pl.size() == 0)
            return t.getLocation().equals(board.getTruckDepot());
        return t.getLocation().equals(pl.peek().start);
    }

    /** Pick up the first Parcel in t's list of Parcels and set t to travel
     * to the Parcel's destination.
     * Precondition. That Parcel is at t's current location.
     * Throw RuntimeException if this is not the case. */
    public void pickUpParcel(Truck t) {
        LinkedList<Parcel> pl= (LinkedList<Parcel>)t.getUserData();
        if (pl.size() == 0) {
            throw new RuntimeException("In pickUpParcel for Truck " + t +
                    ". The Truck has no parcels to pick up.");
        }
        Parcel p= pl.poll();
        Node loc= t.getLocation();
        if (!p.start.equals(loc)) {
            throw new RuntimeException("In pickUpParcel for Truck " + t +
                    "'s first Parcel, " + p + ", is not at this location");
        }
        t.pickupLoad(p);
        List<Node> path= Dijkstra(p.start, p.destination, null);
        t.setTravelPath(path);
        System.out.println("1 Truck " + t.getTruckName() + " picked up " +
                p + " at " + p.start + " heading for " + p.destination);
    }

    /** If t has no more Parcels to pick up, return false.
     * Otherwise, let p be first Parcel in t's list.  
     * If t is at p's location: pick p up, remove from t's list, tell t
     *   to travel to p's destination.
     * If t not at p's location, tell t to travel to the city where p located.
     * 
     * Precondition: This is executed in t's Thread, and t's status is WAITING.
     *               t has no travel instructions and does not have a Parcel.
     *  */
    public boolean goGetFirstParcel(Truck t) {
        LinkedList<Parcel> pl= (LinkedList<Parcel>)t.getUserData();
        if (pl.size() == 0) {
            return false;
        }
        Parcel p= pl.peek();
        if (t.getLocation().equals(p.start)) {
            p= pl.poll();
            t.pickupLoad(p);
            List<Node> path= Dijkstra(p.start, p.destination, null);
            t.setTravelPath(path);

            System.out.println("In goGetFirstParcel Truck " + t.getTruckName() + " picked up " +
                    p + " at " + p.start + " heading for " + p.destination);
        }

        List<Node> path= Dijkstra(t.getLocation(), p.destination, null);
        t.setTravelPath(path);
        System.out.println("In goGetFirstParcel Truck " + t.getTruckName() + 
                " at " + p.start + " heading for " + p.destination + " to pick up a Parcel");
    
        return true;
    }

    /** Tell t to go to the Truck Depot. */
    public void goToDepot(Truck t) {
        List<Node> path= Dijkstra(t.getLocation(), board.getTruckDepot(), null);
        if (path.size() > 1) {
            t.setTravelPath(path);
        }
    }


    /** Return the shortest path from from startN to endN (include both ends).
     * Precondition. There is a path from startN to endN, and neither is null.
     * 
     * Note: if endN = null, calculate shortest path to all nodes.
     * 
     * Note: if trucks is not null, do depth-first search and assign Parcels
     * on the nodes to trucks until each Truck has a Parcel, removing each truck
     * from trucks as it is assigned a Parcel. Try to match colored Parcels to
     * same-colored trucks. In this case, return null. */
    public List<Node> Dijkstra(Node startN, Node endN, ArrayList<Truck> trucks) {

        GriesHeap<NodeDistBack> frontier= new GriesHeap<NodeDistBack>();
        HashMap<Node, NodeDistBack> all= new HashMap<Node, NodeDistBack>();

        NodeDistBack f= new NodeDistBack(startN, 0, null, 0);
        frontier.add(f, 0);
        all.put(startN, f);
        // inv: As in CS2110 slides for lecture on shortest path. Importantly,
        //      the frontier set is frontier. Secondly, the keys in all are
        //      the nodes in the settled and frontier sets.
        while (!frontier.isEmpty()) {
            f= frontier.poll();
            if (trucks != null  &&  trucks.size() > 1) {
                assignParcels(f.u, trucks);
                if (trucks.size() == 0) return null;
            } else if (trucks != null  &&  trucks.size() == 1) {
                boolean assigned= assignParcel(f.u, trucks.get(0));
                if (assigned) {
                    trucks.remove(0);
                }
                if (trucks.size() == 0) return null;
            } else if (trucks != null  &&  trucks.size() == 0) {
                System.out.println("In Dijkstra. Truck list should not be empty");
            } else if (endN == null) {
                // Nothing to do --calculating shortest path to all nodes
            } else if (endN.equals(f.u)) { // return shortest path
                return shortestPath(f);
            }

            Set<Edge> edges= f.u.getExits();  //edges leaving f.u
            for (Edge e : edges) {
                Node w= e.getOther(f.u);
                int newDistance= f.distance + e.length;
                NodeDistBack wbkptr= all.get(w);
                if (wbkptr == null) { // w is in the far out set
                    wbkptr= new NodeDistBack(w, newDistance, f, f.pathLength + 1);
                    frontier.add(wbkptr, newDistance);
                    all.put(w, wbkptr);
                } else { // w is not in far out set
                    if (newDistance < wbkptr.distance) {
                        wbkptr.bkptr= f;
                        wbkptr.distance= newDistance;
                        wbkptr.pathLength= f.pathLength + 1;
                        frontier.updatePriority(wbkptr, newDistance);
                    }
                }
            }
        }
        return null; // should not happen.
    }

    /** Assign Parcels at node n to trucks in ts, removing those
     * trucks from ts. Try to give a Truck a Parcel of its color.
     * This is called in run(), and synchronization is probably not necessary.
     */
    public void assignParcels(Node n, ArrayList<Truck> ts) {
        synchronized(n.getParcels()) {
            HashSet<Parcel> parcels= n.getParcels();
            for (Parcel parcel : parcels) {
                if (ts.size() == 0) return;
                synchronized(unassignedParcels) {
                    if (unassignedParcels.contains(parcel)) {
                        unassignedParcels.remove(parcel);
                    }
                    int k= getLast(ts, parcel.getColor());
                    Truck t= ts.get(k);
                    LinkedList<Parcel> truckParcels= (LinkedList<Parcel>)(t.getUserData());
                    truckParcels.add(parcel);
                    ts.remove(k);
                }
            }
        }
    }


    /**= index of last Truck in ts that has color c (ts.size()-1 if none).
     * Precondition: ts contains at least one Truck. */
    public int getLast(ArrayList<Truck> ts, Color c) {
        for (int k= ts.size()-1; 0 <= k; k= k-1) {
            if (ts.get(k).getColor().equals(c)) 
                return k;
        }
        return ts.size()-1;
    }

    /** Assign an unassigned Parcel at node n to t (choosing one with t's color)
     * if possible. Return true if Parcel was assigned, false if not */
    public boolean assignParcel(Node n, Truck t) {
        synchronized(n) {
            HashSet<Parcel> parcels= n.getParcels();
            LinkedList<Parcel> truckParcels= (LinkedList<Parcel>)(t.getUserData());
            for (Parcel parcel : parcels) { //Try to assign one of same color
                synchronized (unassignedParcels) {
                    if (unassignedParcels.contains(parcel)  &&
                            parcel.getColor().equals(t.getColor())) {
                        unassignedParcels.remove(parcel);
                        truckParcels.add(parcel);
                        System.out.println("In assignParcel. Node " + n + ". Assigning Parcel: " + parcel+ " at " + parcel.start);
                        return true;
                    }
                }
            }
            for (Parcel parcel : parcels) { // Try to assign any one
                synchronized (unassignedParcels) {
                    if (unassignedParcels.remove(parcel)) {
                        truckParcels.add(parcel);
                        System.out.println("In assignParcel. Node " + n + ". Assigning Parcel: " + parcel+ " at " + parcel.start);
                        return true;
                    }
                }
            }
            return false;
        }
    }

    /** An instance contains a node u, the current shortest-path
     * distance to it from a start node, an instance of this class
     * u's backpointer, and the shortest-path length (number of edges). */
    public static class NodeDistBack {
        /** This node. */
        public Node u;

        /** Shortest path distance thus far from start to u*/
        public int distance;  

        /** Back pointer on shortest path from start to u. (null if start is u)*/
        public NodeDistBack bkptr;

        /** Path length to u (number of edges). */
        public int pathLength;

        /** Constructor: instance for node u with distance d, backpointer b,
         * and path length pl */
        public NodeDistBack(Node u, int d, NodeDistBack b, int pl) {
            distance= d;
            this.u= u;
            bkptr= b;
            pathLength= pl;
        }
    }


    /** Return the shortest path to c.u using the backpointers. */
    public static List<Node> shortestPath(NodeDistBack c) {
        LinkedList path= new LinkedList<Node>();
        // invariant: path[k+1..] contains the path from c.u to original c.u
        for (int k= c.pathLength; k >= 0; k= k-1) {
            path.addFirst(c.u);
            //path[k]= c.u;
            c= c.bkptr;
        }
        return path;
    }

    /** Return the list of nodes in p, separated by ", " */
    public static String toString(List<Node> p) {
        String res= "";
        for (Node n : p) {
            if (res.length() > 0) res= res + ", ";
            res= res + n;
        }

        return res;

    }
}
