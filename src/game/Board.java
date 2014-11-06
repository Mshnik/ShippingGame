package game;

import gui.*;

import java.awt.Color;
import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import org.json.*;

/** A Board represents a game state. It is a container for the HashSets of Edges and
 * Nodes that make up the playing field of the game. Because of this, each board
 * belongs uniquely to a Game instance.
 * <br><br>
 * Each board contains a collection of Parcels that need to be delivered from their
 * starting Node to their desired destination node. In order to do this, each game
 * has a collection of Trucks that can pick up and move Parcels along edges between nodes.
 * <br><br>
 * Boards are either randomly generated from a seed or loaded from a file.
 * 
 * @author MPatashnik
 */
public class Board implements JSONString {

    /** The random seed from which this Board was generated: 
     * -1 if loaded from a non-random file. */
    public final long seed;

    private Node truckHome;			//The node at which all trucks start
    protected static final String TRUCK_HOME_NAME = "Truck Depot"; //Name of truckhome

    private HashSet<Edge> edges;    //All edges in this board

    protected int minLength;			//Min length among all edges
    protected int maxLength;			//Max length among all edges

    private HashSet<Node> nodes;    //All nodes in this board

    private ArrayList<Truck> trucks; //The trucks in this board
    private List<Truck> finishedTrucks; //The trucks that have terminated themselves 
    //because they are home and there are no more parcels
    protected final int initialParcelCount;	//Starting number of parcels
    private Set<Parcel> parcels; //The parcels in this board - ones that have not been delivered yet

    /** The game for this board. */
    public final Game game;

    private int waitCost; //Cost of idling, per truck

    private int pickupCost; // One time cost incurred when a truck picks up a parcel

    private int dropoffCost; // One time cost incurred when a truck drops off a parcel

    private int payoff; // The value of successfully delivering one parcel to its destination

    private int onColorMultiplier; // Point multiplier of on-color delivery

    /** Initialize the board from the given serialized version of the board for g */
    protected Board(Game g, JSONObject obj) {
        game = g;

        initCollections();

        //Read seed if possible; otherwise use -1.
        if (obj.has(SEED_TOKEN)) {
            seed = obj.getLong(SEED_TOKEN);
        } else {
            seed = -1;
        }
        //Read score coefficients
        JSONArray scoreJSON = obj.getJSONArray(Board.SCORE_TOKEN);
        waitCost = scoreJSON.getInt(0);
        pickupCost = scoreJSON.getInt(1);
        dropoffCost = scoreJSON.getInt(2);
        payoff = scoreJSON.getInt(3);
        onColorMultiplier = scoreJSON.getInt(4);

        //Read in all nodes of board - read all nodes before reading any edges
        for (String key : obj.keySet()) {
            if (key.startsWith(Board.NODE_TOKEN)) {
                JSONObject nodeJSON = obj.getJSONObject(key);
                Node n = new Node(this, nodeJSON.getString(BoardElement.NAME_TOKEN), null);
                Circle c = n.getCircle();
                c.setX1(nodeJSON.getInt(BoardElement.X_TOKEN));
                c.setY1(nodeJSON.getInt(BoardElement.Y_TOKEN));
                n.x = c.getX1();
                n.y = c.getY1();
                getNodes().add(n);
                if (n.name.equals(Board.TRUCK_HOME_NAME))
                    setTruckHome(n);
            }
        }

        //Scale the locations of the nodes based on the gui size
        scaleComponents();

        //Read in all edges of board. Precondition - all nodes already read in
        for (String key : obj.keySet()) {
            if (key.startsWith(Board.EDGE_TOKEN)) {
                JSONObject edgeJSON = obj.getJSONObject(key);
                JSONArray exitArr = edgeJSON.getJSONArray(BoardElement.LOCATION_TOKEN);

                int length = edgeJSON.getInt(BoardElement.LENGTH_TOKEN);
                Node firstExit = getNode((String)exitArr.get(0));
                Node secondExit = getNode((String)exitArr.get(1));

                Edge e = new Edge(this, firstExit, secondExit, length);
                getEdges().add(e);
                firstExit.addExit(e);
                secondExit.addExit(e);
            }
        }
        //board reading finished.

        //Read in the trucks and parcels - precondition - all nodes already read
        for (String key : obj.keySet()) {
            if (key.startsWith(TRUCK_TOKEN)) {
                JSONObject truck = obj.getJSONObject(key);
                Color c = new Color(truck.getInt(BoardElement.COLOR_TOKEN));
                String name = truck.getString(BoardElement.NAME_TOKEN);
                Truck t = new Truck(game, name, c, getTruckHome());
                trucks.add(t);
            } else if ( key.startsWith(PARCEL_TOKEN)) {
                JSONObject parcel = obj.getJSONObject(key);
                Color c = new Color(parcel.getInt(BoardElement.COLOR_TOKEN));
                Node start = getNode(parcel.getString(BoardElement.LOCATION_TOKEN));
                Node dest = getNode(parcel.getString(BoardElement.DESTINATION_TOKEN));

                Parcel p = new Parcel(this, start, dest, c);
                parcels.add(p);
                start.addParcel(p);
            }
        }
        updateMinMaxLength();

        initialParcelCount = parcels.size();
    }

    /** Initialize collections -- call during construction, not otherwise. */
    private void initCollections() {
        trucks = new ArrayList<Truck>();
        finishedTrucks = Collections.synchronizedList(new ArrayList<Truck>());
        parcels = Collections.synchronizedSet(new HashSet<Parcel>());
        nodes = new HashSet<Node>();
        edges = new HashSet<Edge>();
    }

    /** Return a random node in this board */
    public Node getRandomNode() {
        return Main.randomElement(nodes);
    }

    /** Return a random edge in this board */
    public Edge getRandomEdge() {
        return Main.randomElement(edges);
    }

    /** Return a HashSet containing all the Nodes in this board. 
     * Technically allows addition and removal of Nodes to this board -
     * don't do that while the game is running because things will break. */
    public HashSet<Node> getNodes() {
        return nodes;
    }

    /** Return the number of Nodes in this board */
    public int getNodesSize() {
        return nodes.size();
    }

    /** Return the Node named {@code name} in this board if it exists, null otherwise. */
    public Node getNode(String name) {
        for (Node n : nodes) {
            if (n.name.equals(name))
                return n;
        }

        return null;
    }

    /** Return the unique TruckHome Node in this board that 
     * Trucks must return to before the game can be ended. */
    public Node getTruckHome() {
        return truckHome;
    }

    /** Set the TruckHome node that Trucks must return to before the game can be
     * ended to Node n.
     * @throws IllegalArgumentException -- if n is not in this board
     */
    protected void setTruckHome(Node n) throws IllegalArgumentException {
        if (nodes.contains(n))
            truckHome = n;
        else
            throw new IllegalArgumentException("Can't set Truck Home to " + n + 
                    ", it isn't contained in this board.");
    }

    /** Return the trucks on this board. */
    public ArrayList<Truck> getTrucks() {
        return trucks;
    }

    /** Return the alive trucks on this board that are currently on the Truck Home node
     * (an empty arrayList if there are no such trucks). */
    public ArrayList<Truck> getTrucksHome() {
        ArrayList<Truck> homeTrucks = new ArrayList<Truck>();
        for (Truck t : trucks)
            if (t.isAlive() && t.getLocation() != null && t.getLocation().equals(getTruckHome()))
                homeTrucks.add(t);

        return homeTrucks;
    }

    /** Return true iff an alive Truck in this board is currently on the TruckHome node. 
     */
    public boolean isTruckHome() {
        for (Truck t : getTrucks())
            if (t.isAlive() && t.getLocation() != null && t.getLocation().equals(getTruckHome()))
                return true;

        return false;
    }

    /** Return true iff all alive Trucks in this board are currently on the TruckHome node. 
     */
    public boolean isAllTrucksHome() {
        for (Truck t : getTrucks()) {
            if (t.isAlive() && (t.getStatus().equals(Truck.Status.TRAVELING) 
                    || ! t.getLocation().equals(getTruckHome())))
                return false;
        }

        return true;
    }

    /** Add the given truck to the list of finishedTrucks;
     * then if all trucks are finished, end the game. */
    protected void addTruckToFinished(Truck t) {
        finishedTrucks.add(t);
        if (finishedTrucks.containsAll(trucks)) game.finish();
    }

    /** Return the parcels in this board that have not yet been delivered. */
    public Set<Parcel> getParcels() {
        return parcels;
    }

    /** Called by Trucks to drop off parcels at nodes. 
     * @param p - The Parcel to deliver. Must be currently held by Truck t.
     * @param n - The Node to deliver the parcel to. Must be Parcel p's final destination.
     * @param t - The Truck that is delivering Parcel p. Must currently be holding p and be at n.
     * @throws IllegalArgumentException - if any of the above parameter requirements aren't met.*/
    protected void deliverParcel(Parcel p, Node n, Truck t) {
        if (p.destination != n)
            throw new IllegalArgumentException("Parcel " + p + "'s final destination is not " +
                    n.name + ". Cannot Deliver Here");
        if (t.getLocation() != n)
            throw new IllegalArgumentException("Truck " + t + "Is not currently at " +
                    n.name + ". Cannot Deliver Here");
        if (t.getLoad() != p)
            throw new IllegalArgumentException("Truck " + t +
                    "Is not currently holding Parcel " + p + ". Cannot Deliver Here");

        if (t.getColor().equals(p.getColor()))
            t.getManager().getScoreObject().changeScore(payoff * onColorMultiplier);
        else
            t.getManager().getScoreObject().changeScore(payoff);

        parcels.remove(p);
        n.removeParcel(p);
        if (game.getGUI() != null) game.getGUI().removeParcel(p);
    }

    /** Return the set of Edges in this board. 
     * Technically allows addition and removal of Edges to this board -
     * don't do that while the game is running; things will break. */
    public HashSet<Edge> getEdges() {
        return edges;
    }

    /** Return the number of Edges in this board. */
    public int getEdgesSize() {
        return edges.size();
    }

    /** Return true iff there is any intersection of the lines drawn by the
     * edges in edges.
     * 
     * Used for GUI intersection detection, not useful outside of the GUI context.
     * Has nothing to say about the non-GUI version of the board.
     * Students: not Useful for Game.
     */
    public boolean isIntersection() {
        for (Edge r : edges) {
            for (Edge r2 : edges) {
                if (! r.equals(r2)) {
                    if (r.getLine().intersects(r2.getLine()))
                        return true;
                }
            }
        }

        return false;
    }

    /** Update the Minimum and Maximum lengths of all edge instances.
     * Called internally during processing. 
     * No need to call this after game initialized - it won't do anything. */
    public void updateMinMaxLength() {
        minLength = Edge.DEFAULT_MIN_LENGTH;
        maxLength = Edge.DEFAULT_MAX_LENGTH;

        for (Edge e : edges) {
            minLength = Math.min(minLength, e.length);
            maxLength = Math.max(maxLength, e.length);
        }
    }

    /** Return the maximum length of all edges on the board. */
    public int getMaxLength() {
        return maxLength;
    }

    /** Return the minimum length of all edges on the board. */
    public int getMinLength() {
        return minLength;
    }

    /** Return a 2x1 array of edges that have lines that intersect.
     * If no two edges intersect, return null.
     * 
     * Used for GUI intersection detection, not useful outside of the GUI context.
     * Has nothing to say about the non-GUI version of the board.
     * Students: Not useful
     */
    public Edge[] getAIntersection() {
        for (Edge r : edges) {
            for (Edge r2 : edges) {
                if (!r.equals(r2)) {
                    if (r.getLine().intersects(r2.getLine())) {
                        Edge[] intersectingRoads = {r, r2};
                        return intersectingRoads;
                    }

                }
            }
        }

        return null;
    }

    /** The score cost of idling for a frame, per truck.
     * So you are losing points at a slow rate constantly,
     * you can't compute the optimal solution forever. 
     * Lower than any cost of travel for a frame, but not 0.
     */
    public int getWaitCost() {
        return waitCost;
    }

    /** Return the one-time cost incurred when a truck picks up a parcel. */
    public int getPickupCost() {
        return pickupCost;
    }

    /** Return the one-time cost incurred when a truck drops off a parcel. */
    public int getDropoffCost() {
        return dropoffCost;
    }

    /** Return the value of successfully delivering one parcel to its final destination. */
    public int getPayoff() {
        return payoff;
    }

    /** Return the score multiplier of successfully delivering a parcel using the
     * correct color of Truck. Thus, the score value of an on-color delivery is
     * payoff * onColorMultiplier. */
    public int getOnColorMultiplier() {
        return onColorMultiplier;
    }

    /** Return a String representation of this board, including edges and nodes. */
    @Override
    public String toString() {
        String output = "";
        Iterator<Node> nodesIterator = nodes.iterator();
        while (nodesIterator.hasNext()) {
            Node n = nodesIterator.next();
            output += n + "\t";
            Iterator<Edge> roadsIterator = n.getTrueExits().iterator();
            while (roadsIterator.hasNext()) {
                Edge r = roadsIterator.next();
                output += r.getOther(n).name+"-"+r.length;
                if (roadsIterator.hasNext())
                    output += "\t";
            }
            if (nodesIterator.hasNext())
                output += "\n";
        }
        return output;
    }


    private static final String SCORE_TOKEN = "scoreCoeff";
    private static final String NODE_TOKEN = "node-";
    private static final String EDGE_TOKEN = "edge-";
    private static final String SEED_TOKEN = "seed";
    private static final String TRUCK_TOKEN = "truck-";
    private static final String PARCEL_TOKEN = "parcel-";

    /** Return a JSON-compliant version of toString().
     * A full serialized version of the board, including:
     * > Seed
     * > Cost constants
     * > Nodes
     * > Edges
     * > Trucks
     * > Parcels */
    @Override
    public String toJSONString() {		
        String s = "{\n" + Main.addQuotes(SEED_TOKEN) + ":" + seed +",\n";
        s += Main.addQuotes(SCORE_TOKEN) + ":[" + waitCost + "," 
                + pickupCost + "," + dropoffCost + "," + payoff + "," + onColorMultiplier + "],";
        int i = 0;
        for (Node n : nodes) {
            s += "\n" + Main.addQuotes(NODE_TOKEN + i) + ":" + n.toJSONString() + ",";
            i++;
        }
        i = 0;
        for (Edge e : edges) {
            s += "\n" + Main.addQuotes(EDGE_TOKEN + i) + ":" + e.toJSONString() +",";
            i++;
        }
        i = 0;
        for (Truck t : trucks) {
            s += "\n" + Main.addQuotes(TRUCK_TOKEN + i) + ":" + t.toJSONString() + ",";
            i++;
        }
        i = 0;
        for (Parcel p : parcels) {
            s += "\n" + Main.addQuotes(PARCEL_TOKEN + i) + ":" + p.toJSONString();
            if (i < parcels.size() - 1)
                s += ",";
            i++;
        }	
        return s + "\n}";
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// Random board Generation ////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////

    /** Return a new random board for g seeded via random seed. */
    public static Board randomBoard(Game g) {
        return randomBoard(g, (long)(Math.random() * Long.MAX_VALUE));
    }

    /** Return a new random board for g seeded with {@code seed}. */
    public static Board randomBoard(Game g, long seed) {
        return new Board(g, new Random(seed), seed);
    }

    /** Return a new random board for g seeded with {@code seed} and
     * using the {@code Random} parameter {@code r} */
    private Board(Game g, Random r, long seed) {
        this.seed = seed;
        game = g;

        //Initialize collections
        initCollections();

        //Do board generation
        BoardGeneration.gen(this, r);

        //Finish setting things
        scaleComponents();
        updateMinMaxLength();
        initialParcelCount = parcels.size();
    }

    /** Library for random board generation.
     * Implemented inside board class to allow construction based on these methods.
     * 
     * Node placement and Edge connections are done using the Delaunay Triangulation Method:
     * http://en.wikipedia.org/wiki/Delaunay_triangulation 
     * @author eperdew, MPatashnik
     */
    private static class BoardGeneration{
        private static final int MIN_NODES = 5;
        private static final int MAX_NODES = 100;

        private static final double AVERAGE_DEGREE = 2.5;
        private static final int MIN_EDGE_LENGTH = 5;
        private static final int MAX_EDGE_LENGTH = 60;

        private static final int WIDTH = 1600 - Circle.DEFAULT_DIAMETER * 3;
        private static final int HEIGHT = 1200 - Circle.DEFAULT_DIAMETER * 3;

        private static final int MIN_TRUCKS = 5;
        private static final int MAX_TRUCKS = 50;

        private static final int MIN_PARCELS = 35;
        private static final int MAX_PARCELS = 150;

        private static final int WAIT_COST_MIN = 1;
        private static final int WAIT_COST_MAX = 3;

        private static final int PICKUP_COST_MIN = 0;
        private static final int PICKUP_COST_MAX = 150;

        private static final int DROPOFF_COST_MIN = 0;
        private static final int DROPOFF_COST_MAX = 150;

        private static final int PAYOFF_MIN = 1000;
        private static final int PAYOFF_MAX = 4000;

        private static final int ON_COLOR_MULTIPLIER_MIN = 2;
        private static final int ON_COLOR_MULTIPLIER_MAX = 4;

        /** Generate a full set of random elements for b, using r for all random decisions.
         * @param b - a blank board to put stuff on.
         * @param r - a randomer to use for all random decisions. */
        private static void gen(Board b, Random r) {
            final int numCities = r.nextInt(MAX_NODES - MIN_NODES + 1) + MIN_NODES;
            b.waitCost = -1
                    * (r.nextInt(WAIT_COST_MAX - WAIT_COST_MIN + 1) + WAIT_COST_MIN);
            b.pickupCost = -1
                    * (r.nextInt(PICKUP_COST_MAX - PICKUP_COST_MIN + 1) + PICKUP_COST_MIN);
            b.dropoffCost = -1
                    * (r.nextInt(DROPOFF_COST_MAX - DROPOFF_COST_MIN + 1) + DROPOFF_COST_MIN);
            b.payoff = r.nextInt(PAYOFF_MAX - PAYOFF_MIN + 1) + PAYOFF_MIN;
            b.onColorMultiplier = r.nextInt(ON_COLOR_MULTIPLIER_MAX
                    - ON_COLOR_MULTIPLIER_MIN + 1)
                    + ON_COLOR_MULTIPLIER_MIN;

            ArrayList<String> cities = cityNames();
            //Create nodes and add to board
            for (int i = 0; i < numCities; i++) {
                String name;
                if (i == 0) {
                    name = Board.TRUCK_HOME_NAME;
                } else{
                    name = cities.remove(r.nextInt(cities.size()));
                }
                Node n = new Node(b, name, null);
                Circle c = n.getCircle();
                c.setX1(-Circle.DEFAULT_DIAMETER); 
                c.setY1(-Circle.DEFAULT_DIAMETER);
                while (c.getX1() == -Circle.DEFAULT_DIAMETER || 
                        c.getY1() == -Circle.DEFAULT_DIAMETER) {
                    //Try setting to a new location
                    c.setX1(r.nextInt(WIDTH + 1) + Circle.DEFAULT_DIAMETER * 2);
                    c.setY1(r.nextInt(HEIGHT + 1) + Circle.DEFAULT_DIAMETER * 2);
                    //Check other existing nodes. If too close, re-randomize this node's location
                    for (Node n2 : b.getNodes()) {
                        if (n2.getCircle().getDistance(c) < Circle.BUFFER_RADUIS) {
                            c.setX1(-Circle.DEFAULT_DIAMETER);
                            c.setY1(-Circle.DEFAULT_DIAMETER);
                            break;
                        }
                    }
                }
                n.x = n.getCircle().getX1();
                n.y = n.getCircle().getY1();
                b.getNodes().add(n);
                if (n.name.equals(Board.TRUCK_HOME_NAME)) {
                    b.setTruckHome(n);
                }
            }

            //Add trucks
            final int numb_trucks = r.nextInt(MAX_TRUCKS - MIN_TRUCKS + 1) + MIN_TRUCKS;
            for (int i = 0; i < numb_trucks; i++) {
                Truck t = new Truck(b.game, "TRUCK-" + (i+1), 
                        Score.COLOR[r.nextInt(Score.COLOR.length)], b.getTruckHome());
                b.trucks.add(t);
            }

            //Add parcels
            final int numb_parcels = r.nextInt(MAX_PARCELS - MIN_PARCELS + 1) + MIN_PARCELS;
            for (int i = 0; i < numb_parcels; i++) {
                Node start = randomElement(b.getNodes(), r);
                Node dest = start;
                while(dest == start) {
                    dest = randomElement(b.getNodes(), r);
                }
                Color c = Score.COLOR[r.nextInt(Score.COLOR.length)];
                Parcel p = new Parcel(b, start, dest, c);
                b.parcels.add(p);
                start.addParcel(p);
            }

            spiderwebEdges(b, r);
        }

        /** Create an edge with a random length that connects n1 and n2
         * and add to the correct collections. Return the created edge.
         */
        private static Edge addEdge(Board b, Random r, Node n1, Node n2) {
            int length = r.nextInt(MAX_EDGE_LENGTH - MIN_EDGE_LENGTH + 1) + MIN_EDGE_LENGTH;
            Edge e = new Edge(b, n1, n2, length);
            b.getEdges().add(e);
            n1.addExit(e);
            n2.addExit(e);
            return e;
        }

        /** The maximum number of attempts to get to average node degree */
        private static int MAX_EDGE_ITERATIONS = 1000;

        /** Create a spiderweb of edges by creating concentric hulls,
         * then connecting between the hulls.
         * Create a connected, planar graph. */
        private static void spiderwebEdges(Board b, Random r) {
            HashSet<Node> nodes = new HashSet<Node>();
            nodes.addAll(b.getNodes());
            ArrayList<HashSet<Node>> hulls = new ArrayList<>();

            //Create hulls, add edges
            while (! nodes.isEmpty()) {
                HashSet<Node> nds = addGiftWrapEdges(b, r, nodes);
                hulls.add(nds);
                for (Node n : nds) {
                    nodes.remove(n);
                }
            }
            //At this point, there are either 2*n or 2*n-1 edges, depending
            //if the inner most hull had a polygon in it or not.

            //Connect layers w/ random edges - try to connect each node to its
            //closest on the surrounding hull
            //Guarantee that the map is connected after this step
            for (int i = 0; i < hulls.size() - 1; i++) {
                for (Node n : hulls.get(i+1)) {
                    Node c = Collections.min(hulls.get(i), new DistanceComparator(n));
                    if (! lineCrosses(b, n, c)) {
                        addEdge(b, r, n, c);
                    }
                }
            }

            //Create a hashmap of node -> hull the node is in within hulls.
            HashMap<Node, Integer> hullMap = new HashMap<>();
            for (int i = 0; i < hulls.size(); i++) {
                for (Node n : hulls.get(i)) {
                    hullMap.put(n,i);
                }
            }
            final int maxHull = hulls.size() - 1;

            //If the innermost hull has size 1 or 2, add edges to guarantee that every node
            //has degree at least 2
            HashSet<Node> lastHull = hulls.get(hulls.size() - 1);
            if (lastHull.size() < 3) {
                HashSet<Node> penultimateHull = hulls.get(hulls.size() - 2); //Exists. Just cause.
                int e = 1;
                if (lastHull.size() == 1) e = 2;
                for (Node n : lastHull) {
                    if (n.getExitsSize() < 2) {
                        int i = 0;
                        while (i < e) {
                            Node n2 = randomElement(penultimateHull, r);
                            if (! lineCrosses(b, n, n2) && ! n.isConnectedTo(n2)) {
                                addEdge(b, r, n, n2);
                                i++;
                            }
                        }
                    }
                }
            }

            int iterations = 0;

            while (b.getEdges().size() < b.getNodes().size() * AVERAGE_DEGREE &&
                    iterations < MAX_EDGE_ITERATIONS) {
                //Get random node
                Node n = randomElement(b.getNodes(), r);
                int hull = hullMap.get(n);
                //Try to connect to a node on the hull beyond this one.
                if (hull < maxHull) {
                    for (Node c : hulls.get(hull + 1)) {
                        if (! lineCrosses(b, n,c) && ! n.isConnectedTo(c)) {
                            addEdge(b, r,n,c);
                            break;
                        }
                    }
                }
                //Try to connect to a node on the hull outside this one
                if (hull > 0) {
                    for (Node c : hulls.get(hull - 1)) {
                        if (! lineCrosses(b, n,c) && ! n.isConnectedTo(c)) {
                            addEdge(b, r,n,c);
                            break;
                        }
                    }
                }
                iterations++;
            }

            //Fix triangulation such that it's cleaner.
            delunayTriangulate(b, r);
        }

        /** Gift-wrap the nodes - create a concentric set of edges that surrounds
         * set nodes, with random edge lengths.
         * Return a set of nodes that is the nodes involved in the gift-wrapping. */
        private static HashSet<Node> addGiftWrapEdges(Board b, Random r, HashSet<Node> nodes) {
            HashSet<Node> addedNodes = new HashSet<Node>();
            //Base case - 0 or 1 node. Nothing to do.
            if (nodes.size() <= 1) {
                addedNodes.add(nodes.iterator().next());
                return addedNodes;
            }

            //Base case - 2 nodes. Add the one edge connecting them and return.
            if (nodes.size() == 2) {
                Iterator<Node> n = nodes.iterator();
                Node n1 = n.next();
                Node n2 = n.next();
                addEdge(b, r, n1, n2);
                addedNodes.add(n1);
                addedNodes.add(n2);
                return addedNodes;
            }

            //Non base case - do actual gift wrapping alg
            Node first = Collections.min(nodes, xComp);
            Node lastHull = first;
            Node endpoint = null;
            do {
                for (Node n : nodes) {
                    if (endpoint == null || n != lastHull && isLeftOfLine(lastHull, endpoint, n) 
                            && ! lastHull.isConnectedTo(n)) {
                        endpoint = n;
                    }
                }

                addEdge(b, r, lastHull, endpoint);
                addedNodes.add(lastHull);

                lastHull = endpoint;
            } while (lastHull != first);

            return addedNodes;
        }

        /** Return true iff e2 is left of the line start -> e1.
         * Helper for giftwrapping method */
        private static boolean isLeftOfLine(Node start, Node e1, Node e2) {
            Vector a = start.getCircle().getVectorTo(e1.getCircle());
            Vector b = start.getCircle().getVectorTo(e2.getCircle());
            return Vector.cross(a, b) <= 0;
        }

        /** Return true iff the line that would be formed by connecting the
         * two given nodes crosses an existing edge.
         * Helper for gift-wrapping and spider-webbing methods.
         */
        private static boolean lineCrosses(Board b, Node n1, Node n2) {
            Line l = new Line(n1.getCircle(), n2.getCircle(), null);
            for (Edge e : b.getEdges()) {
                if (l.intersects(e.getLine()))
                    return true;
            }
            return false;
        }

        /** Fix (psuedo) triangulation via the delunay method.
         * Alter the current edge set so that triangles are less skinny. */
        private static void delunayTriangulate(Board b, Random r) {

            //Amount of radians that angle sum necessitates switch
            final double FLIP_CONDITION = Math.PI; 

            //Edge that should be removed, mapped to its new exits
            HashMap<Edge, Node[]> needsFlip = new HashMap<>(); 

            for (Node n1 : b.getNodes()) {
                for (Edge e2 : n1.getTrueExits()) {
                    Node n2 = e2.getOther(n1);
                    if (n2 != n1) {
                        for (Edge e3 : n1.getTrueExits()) {
                            Node n3 = e3.getOther(n1);
                            if (n3 != n2 && n3 != n1) {
                                for (Edge e4 : n1.getTrueExits()) {
                                    Node n4 = e4.getOther(n1);
                                    if (n4 != n3 && n4 != n2 && n4 != n1) {
                                        //Check all triangulated quads - n1 connected to n2,
                                        // n3, n4; n2 and n3 each connected to n4.
                                        //We already know that n1 is connected to n2, n3, n4.
                                        //Check other part of condition.
                                        if (n2.isConnectedTo(n4) && n3.isConnectedTo(n4)) {
                                            //This is a pair of adjacent triangles. 
                                            //Check angles to see if flip should be made
                                            Edge e24 = n2.getConnect(n4);
                                            Edge e34 = n3.getConnect(n4);
                                            if (e2.getLine().radAngle(e24.getLine())
                                                    + e3.getLine().radAngle(e34.getLine()) > FLIP_CONDITION) {
                                                //Store the dividing edge as needing a flip
                                                Node[] newExits = {n2, n3};
                                                needsFlip.put(e4, newExits);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            for (Entry<Edge, Node[]> e : needsFlip.entrySet()) {
                //Remove old edge
                b.getEdges().remove(e.getKey());

                Node oldFirst = e.getKey().getFirstExit();
                Node oldSecond = e.getKey().getSecondExit();

                oldFirst.removeExit(e.getKey());
                oldSecond.removeExit(e.getKey());

                Node newFirst = e.getValue()[0];
                Node newSecond = e.getValue()[1];

                //Add new edge if it doesn't cross an existing edge
                if (! lineCrosses(b, newFirst, newSecond)) {
                    addEdge(b, r, newFirst, newSecond);
                }
                else { //Otherwise, put old edge back
                    addEdge(b, r, oldFirst, oldSecond);
                }  
            }
        }

        /** Allows for sorting of Collections of Nodes by their gui distance to
         * each of the nodes in collection n.
         * The node that is closest in the collection to the given node is the one that counts.
         * @author MPatashnik
         *
         */
        private static class DistanceComparator implements Comparator<Node> {
            /** The node to which distance is compared */
            protected final Node node;

            @Override
            public int compare(Node n1, Node n2) {
                double d = node.getCircle().getDistance(n1.getCircle()) - 
                           node.getCircle().getDistance(n2.getCircle());
                if (d < 0) return -1;
                if (d > 0) return 1;
                return 0;
            }

            DistanceComparator(Node node) {
                this.node = node;
            }
        }

        /** An instance of the XComparator for sorting nodes.
         * No real need to instantiate another one. */
        private final static XComparator xComp = new XComparator();

        /** Allows for sorting a Collection of Nodes by the x coordinate.
         * No need to instantiate beyond the xcomparator instantiated above. */
        private static class XComparator implements Comparator<Node>{
            @Override
            public int compare(Node n1, Node n2) {
                return n1.getCircle().getX1() - n2.getCircle().getX1();
            }
        }

        /** Return a random element from elms using r.
         * (Return null if elms is empty.) */
        private static <T> T randomElement(Collection<T> elms, Random r) {
            if (elms.isEmpty())
                return null;

            Iterator<T> it = elms.iterator();
            T val = null;
            int rand = r.nextInt(elms.size()) + 1;
            for (int i = 0; i < rand; i++) {
                val = it.next();
            }
            return val;
        }

    }

    /** Scale the (x,y) coordinates of circles to fit the gui */
    private void scaleComponents() {
        double heightRatio = (double)(GUI.DRAWING_BOARD_HEIGHT - 2*Circle.DEFAULT_DIAMETER)/ 
                (double)BoardGeneration.HEIGHT;
        double widthRatio = (double)(GUI.DRAWING_BOARD_WIDTH - 2*Circle.DEFAULT_DIAMETER)/ 
                (double)BoardGeneration.WIDTH;

        for (Node n : getNodes()) {
            Circle c = n.getCircle();
            c.setX1((int) (c.getX1() * widthRatio) + Circle.DEFAULT_DIAMETER/2);
            c.setY1((int) (c.getY1() * heightRatio) + Circle.DEFAULT_DIAMETER/2);
        }
    }

    /** Location of files for board generation */
    public static final String BOARD_GENERATION_DIRECTORY = "data/BoardGeneration";

    /** Return the city names listed in BoardGeneration/cities.txt */
    private static ArrayList<String> cityNames() {
        File f = new File(BOARD_GENERATION_DIRECTORY + "/cities.txt");
        BufferedReader read;
        try {
            read = new BufferedReader(new FileReader(f));
        }
        catch (FileNotFoundException e) {
            System.out.println("cities.txt not found. Aborting as empty list of city names...");
            return new ArrayList<String>();
        }
        ArrayList<String> result = new ArrayList<String>();
        try {
            String line;
            while((line = read.readLine()) != null) {
                //Strip non-ascii or null characters out of string
                line = line.replaceAll("[\uFEFF-\uFFFF \u0000]", "");
                result.add(line);
            }
            read.close();
        }
        catch (IOException e) {
            System.out.println("Error in file reading. Aborting as empty list of city names...");
            return new ArrayList<String>();
        }
        return result;
    }
}