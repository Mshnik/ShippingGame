package game;
import gui.Circle;
import gui.DraggableCircle;

import java.awt.Color;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


/** A Node (vertex) on the board of the game.
 *  Each Node has maintains a set of edges that exit it,
 *  A hashmap of trucks, mapping to either at this node or not at this node,
 *  and a set of Parcels that are on this node (and not loaded on a truck). <br><br>
 *  
 *  All of the methods that modify these collections are protected, but all of the getters
 *  Are public for use by the user. Additionally, convenience methods such as isConnectedTo(Node n)
 *  are provided for user use.
 *  
 *  @author MPatashnik
 **/
public class Node implements BoardElement{

	private final Board board;		//The board this Node is contained in
	
	private HashMap<Truck, Boolean> truckHere; //Maps truck -> is here

	/** The name of this node. Set during construction */
	public final String name;
	private Set<Edge> exits; 				//Edges leaving this Node
	private Set<Parcel> parcels; 		//Parcels currently here and not on truck
	
	private Object userData;

	private Circle circle;	//Circle that represents this graphically

	/** Constructor for a named Node with no starting exits but with a specific circle drawing
	 * @param m - the Board this Node belongs to
	 * @param name - the name of this Node
	 * @param circle - The (draggable) circle object to draw for this Node*/
	protected Node(Board m, String name, DraggableCircle c){
		this(m, name, c, null);
	}

	/** Constructor for a named Node with starting exits exits.
	 * @param m - the Board this Node belongs to
	 * @param name - the name of this Node
	 * @param exits - the exits of this node
	 */
	protected Node(Board m, String name, DraggableCircle c, Set<Edge> exits){
		board = m;
		this.name = name;

		if(c == null)
			circle = new DraggableCircle(this, 0, 0, Circle.DEFAULT_DIAMETER);
		else
			circle = c;

		truckHere = new HashMap<Truck, Boolean>();

		if(exits !=  null){
			this.exits = Collections.synchronizedSet(exits);
		} else{
			this.exits = Collections.synchronizedSet(new HashSet<Edge>());
		}
		parcels = Collections.synchronizedSet(new HashSet<Parcel>());
	}

	/** Returns the board this Node belongs to */
	public Board getBoard(){
		return board;
	}

	/** Returns the exits of this Node */
	protected Set<Edge> getTrueExits(){
		return exits;
	}

	/** Returns a HashSet that contains the same edges as this Node
	 * This is to prevent the addition of edges to exits without proper clearance.
	 */
	public HashSet<Edge> getExits(){
		HashSet<Edge> newExits = new HashSet<Edge>();
		newExits.addAll(exits);
		return newExits;
	}

	/** Returns a random exit from exits */
	public Edge getRandomExit(){
		return Main.randomElement(exits);
	}

	/** Sets the value of exits to newExits */
	protected void setExits(HashSet<Edge> newExits){
		exits = newExits;
	}

	/** Adds newExit to this Node's set of exits */
	protected void addExit(Edge newExit){
		exits.add(newExit);
	}

	/** Removes badExit from this Node's set of exits */
	protected void removeExit(Edge badExit){
		exits.remove(badExit);
	}

	/** Adds the Collection of edges to exits */
	protected void addExits(Collection<Edge> newExits){
		exits.addAll(newExits);
	}

	/** Returns the number of exits from this node. */
	public int getExitsSize(){
		return exits.size();
	}

	/** Returns true if exits contains Edge r (r is an edge connected to this), 
	 * false otherwise */
	public boolean isExit(Edge r){
		return exits.contains(r);
	}

	/** Adds Parcel p to parcels on this Node  */
	protected void addParcel(Parcel p){
		board.getParcels().add(p);
		parcels.add(p);
	}

	/** Removes parcel p from parcels  */
	protected void removeParcel(Parcel p){
		parcels.remove(p);
	}

	/** Returns the parcels located this Node, not held by any truck */
	protected Set<Parcel> getTrueParcels(){
		return parcels;
	}

	/** Returns a copy of the HashSet containing the present parcels to prevent its editing */
	public HashSet<Parcel> getParcels(){
		HashSet<Parcel> parcelClone = new HashSet<Parcel>();
		synchronized(parcels){
			parcelClone.addAll(parcels);
		}
		return parcelClone;
	}

	/** Returns a random parcel at this node.*/
	public Parcel getRandomParcel(){
		return Main.randomElement(parcels);
	}

	/** Returns true if parcel p is on this node, false otherwise */
	public boolean isParcelHere(Parcel p){
		return parcels.contains(p);
	}

	/** Creates a new Edge with length length and adds it as an exit
	 * to this Node and other Node. Also adds it to the board.
	 * @param other the Node to connect this Node to
	 * @param length the length of the Edge
	 */
	protected void connectTo(Node other, int length){
		Edge r = new Edge(board, this, other, length);
		addExit(r);
		other.addExit(r);
		board.getEdges().add(r);
	}

	/** Returns false if destination.equals(this), else  
	 * Returns true if one of the edges in exits leads to Node destination, 
	 * (this is connected to destination via a single edge)
	 * false otherwise. */
	public boolean isConnectedTo(Node destination){
		if(destination.equals(this))
			return false;

		for(Edge r : exits){
			if(r.isExit(destination))
				return true;
		}

		return false;
	}

	/** Returns the road that this node shares with node other, or null if not connected */
	public Edge getConnect(Node other){
		for(Edge r : exits)
			if(r.getOther(this).equals(other))
				return r;

		return null;
	}

	/** Returns the userData stored in this Node. May be null if the user has not yet given this Node userData 
	 */
	@Override
	public Object getUserData(){
		return userData;
	}

	/** Sets the value of userData to Object uData. To erase the current userData just pass in null */
	@Override
	public void setUserData(Object uData){
		userData = uData;
	}

	/** Returns the Circle that represents this node graphically */
	public Circle getCircle(){
		return circle;
	}

	/** Sets the Circle for this Node to circle c */
	public void setCircle(Circle c){
		circle = c;
	}

	/** Tells the node if a Truck is currently on it or not.*/
	protected void setTruckHere(Truck t, Boolean isHere){
		truckHere.put(t, isHere);
	}

	/** Updates the circle graphic that represents this truck on the GUI.
	 * Does nothing if threads is null.
	 * Also updates the location of the load if this truck is carrying one 
	 * @param x - the new X location of this Truck in the GUI
	 * @param y - the new Y location of this Truck in the GUI
	 * */
	@Override
	public void updateGUILocation(int x, int y){
		circle.setX1(x);
		circle.setY1(y);
		circle.repaint();
		for(Edge e : exits){
			e.updateGUILocation(x, y);
		}
		for(Parcel p : parcels){
			p.updateGUILocation(x, y);
		}
		for(Truck t : board.getTrucks()){
			if(t.getLocation() == this)
				t.updateGUILocation(x, y);
		}

	}

	/** Two Nodes are equal if they have the same name - guaranteed to be unique
	 * within the context of a single game */
	@Override
	public boolean equals(Object n){
		if(n == null)
			return false;
		if(! (n instanceof Node) )
			return false;

		return name.equals( ((Node)n).name);
	}

	/** A Node's hashCode is equal to the hashCode of its name.
	 * This is guaranteed to be unique within the context of a single game. */
	@Override
	public int hashCode(){
		return name.hashCode();
	}

	/** Returns true if a truck is currently at this node, false otherwise. */
	@Override
	public boolean isTruckHere(Truck t){
		return truckHere.get(t);
	}

	/** Returns the number of trucks here */
	@Override
	public int trucksHere(){
		int i = 0;
		for(Boolean b : truckHere.values()){
			if(b) i++;
		}
		return i;
	}

	/** Returns true if any truck is traveling towards this node, false otherwise */
	public boolean isTruckTravelingHere(){
		for(Truck t : board.getTrucks()){
			if(t.getTravelingTo() != null && t.getTravelingTo().equals(this))
				return true;
		}
		return false;
	}

	/** Returns the color of this Node */
	public Color getColor(){
		return circle.getColor();
	}

	/** Returns the name of this Node */
	@Override
	public String toString(){
		return name;
	}

	/** Returns the string that is mapped when this Node is drawn */
	@Override
	public String getMappedName() {
		return name;
	}

	/** Returns the x location that this Node's string is mapped relative to its top right coordinate */
	@Override
	public int getRelativeX() {
		return -Circle.DEFAULT_DIAMETER/2;
	}

	/** Returns y location that this Node's string is mapped relative to its top right coordinate */
	@Override
	public int getRelativeY() {
		return 0;
	}

	/** Returns just this' name for the JSONString - relies on JSONs of Edges and parcels
	 * to take care of themselves.
	 */
	@Override
	public String toJSONString(){
		return "{\n" + Main.addQuotes(BoardElement.NAME_TOKEN) + ":" + Main.addQuotes(name) + ",\n"
				+ Main.addQuotes(X_TOKEN) + ":"+ circle.getX1() + ",\n"
				+ Main.addQuotes(Y_TOKEN) + ":" + circle.getY1() + "\n}";
	}

}
