package game;
import gui.Circle;
import gui.DraggableCircle;

import java.awt.Color;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.Semaphore;


/** A Node (vertex) on the graph of the game.
 *  Each Node has maintains a set of edges that exit it,
 *  A hashmap of trucks, mapping to either at this node or not at this node,
 *  and a set of Parcels that are on this node (and not loaded on a truck). <br><br>
 *  
 *  All of the methods that modify these collections are protected, but all of the getters
 *  Are public for use by the user. Additionally, convience methods such as isConnectedTo(Node n)
 *  are provided for user use.
 *  
 *  @author MPatashnik
 **/
public class Node implements MapElement, UserData{

	private final Game game;

	/** Threads need to get this to view/edit parcels at this node */
	private Semaphore parcelLock;

	private Semaphore truckLock; //Lock that trucks must acquire in order to make changes to this edge.

	private int truckHereCount; 			   //A count of the trucks here (mappings in truckHere to true)
	private HashMap<Truck, Boolean> truckHere; //Maps truck -> is here

	private String name;
	private HashSet<Edge> exits = new HashSet<Edge>();			//Edges leaving this Node
	private HashSet<Parcel> parcels = new HashSet<Parcel>();	//Parcels currently here and not on truck

	private Object userData;

	private Circle circle;	//Circle that represents this graphically

	/** Constructor for a named Node with no starting exits but with a specific circle drawing
	 * @param g - the Game this Node belongs to
	 * @param name - the name of this Node
	 * @param circle - The (draggable) circle object to draw for this Node*/
	protected Node(Game g, String name, DraggableCircle c){
		this(g, name, c, null);
	}

	/** Constructor for a named Node with starting exits exits.
	 * @param g - the Game this Node belongs to
	 * @param name - the name of this Node
	 * @param exits - the exits of this node
	 */
	protected Node(Game g, String name, DraggableCircle c, Collection<Edge> exits){
		this.game = g;
		this.name = name;
		if(exits != null)
			this.exits.addAll(exits);

		if(c == null)
			circle = new DraggableCircle(this, 0, 0, Circle.DEFAULT_DIAMETER);
		else
			circle = c;

		truckHereCount = 0;
		truckHere = new HashMap<Truck, Boolean>();

		parcelLock = new Semaphore(1);
		truckLock = new Semaphore(1);
	}

	/** Returns the game this Node belongs to */
	public Game getGame(){
		return game;
	}

	/** Returns the name of this Node */
	public String getName(){
		return name;
	}

	/** Sets the name of this Node to newName */
	protected void setName(String newName){
		name = newName;
	}

	/** Returns the exits of this Node */
	protected HashSet<Edge> getTrueExits(){
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
		Iterator<Edge> it = exits.iterator();
		Edge e = null;
		//Move forward along the iterator a random number of times.
		for(int i = 0; i < (int)(Math.random() * exits.size()) + 1; i++)
			e = it.next();
		return e;
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

	/** Returns the number of exits from this node */
	public int getExitsSize(){
		return exits.size();
	}

	/** Returns true if exits contains Edge r (r is an edge connected to this), 
	 * false otherwise */
	public boolean isExit(Edge r){
		return exits.contains(r);
	}

	/** Adds Parcel p to parcels on this Node 
	 * @throws InterruptedException */
	protected void addParcel(Parcel p) throws InterruptedException{
		parcelLock.acquire();
		game.getParcels().add(p);
		parcels.add(p);
		parcelLock.release();
	}

	/** Removes parcel p from parcels 
	 * @throws InterruptedException */
	protected void removeParcel(Parcel p) throws InterruptedException{
		parcelLock.acquire();
		parcels.remove(p);
		parcelLock.release();
	}

	/** Returns the parcels on this Node */
	protected HashSet<Parcel> getTrueParcels(){
		return parcels;
	}

	/** Returns a copy of the HashSet containing the present parcels to prevent its editing 
	 * @throws InterruptedException */
	public HashSet<Parcel> getParcels() throws InterruptedException{
		parcelLock.acquire();
		HashSet<Parcel> parcelClone = new HashSet<Parcel>();
		parcelClone.addAll(parcels);
		parcelLock.release();
		return parcelClone;
	}

	/** Returns a random parcel at this node 
	 * @throws InterruptedException */
	public Parcel getRandomParcel() throws InterruptedException{
		return Main.randomElement(parcels, parcelLock);
	}

	/** Returns true if parcel p is on this node, false otherwise 
	 * @throws InterruptedException */
	public boolean isParcelHere(Parcel p) throws InterruptedException{
		parcelLock.acquire();
		boolean isHere = parcels.contains(p);
		parcelLock.release();
		return isHere;
	}

	/** Creates a new Edge with length length and adds it as an exit
	 * to this Node and other Node
	 * @param other the Node to connect this Node to
	 * @param length the length of the Edge
	 */
	protected void connectTo(Node other, int length){
		Edge r = new Edge(game, this, other, length);
		addExit(r);
		other.addExit(r);
	}

	/** Returns false if destination.equals(this), else  
	 * Returns true if one of the edges in exits leads to Node destination, 
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
	 * @throws InterruptedException */
	public Object getUserData(){
		return userData;
	}

	/** Sets the value of userData to Object uData. To erase the current userData just pass in null */
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

	/** Tells the node if a Truck is currently on it or not. Gets its truckLock to prevent Truck thread collision */
	protected void setTruckHere(Truck t, Boolean isHere) throws InterruptedException{
		truckLock.acquire();
		if(truckHere.containsKey(t) && ! truckHere.get(t) && isHere)
			truckHereCount++;
		else if(! truckHere.containsKey(t) || truckHere.get(t) && ! isHere)
			truckHereCount--;
		truckHere.put(t, isHere);
		truckLock.release();
	}

	/** Updates the circle graphic that represents this truck on the GUI.
	 * Does nothing if threads is null.
	 * Also updates the location of the load if this truck is carrying one 
	 * @param x - the new X location of this Truck in the GUI
	 * @param y - the new Y location of this Truck in the GUI
	 * */
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
		for(Truck t : game.getTrucks()){
			if(t.getLocation() == this)
				t.updateGUILocation(x, y);
		}

	}

	@Override
	/** Two Nodes are equal if they have the same name */
	public boolean equals(Object n){
		if(n == null)
			return false;
		if(! (n instanceof Node) )
			return false;

		return name.equals( ((Node)n).getName());
	}

	@Override
	/** A Node's hashCode is equal to the hashCode of its name */
	public int hashCode(){
		return name.hashCode();
	}

	@Override
	/** Returns true if a truck is currently at this node, false otherwise */
	public boolean isTruckHere(Truck t) throws InterruptedException{
		truckLock.acquire();
		Boolean b = truckHere.get(t);
		if(b == null)
			b = false;
		truckLock.release();
		return b;
	}

	@Override
	/** Returns the number of trucks here */
	public int trucksHere() throws InterruptedException{
		truckLock.acquire();
		int i = truckHereCount;
		truckLock.release();
		return i;
	}

	/** Returns true if any truck is traveling towards this node, false otherwise 
	 * @throws InterruptedException */
	public boolean isTruckTravelingHere() throws InterruptedException{
		for(Truck t : game.getTrucks()){
			if(t.getTravelingTo() != null && t.getTravelingTo().equals(this))
				return true;
		}
		return false;
	}

	/** Returns the color of this Node */
	public Color getColor(){
		return circle.getColor();
	}

	@Override
	/** Returns the name of this Node */
	public String toString(){
		return name;
	}

	@Override
	/** Returns the string that is mapped when this Node is drawn */
	public String getMappedName() {
		return getName();
	}

	@Override
	/** Returns the x location that this Node's string is mapped relative to its top right coordinate */
	public int getRelativeX() {
		return -Circle.DEFAULT_DIAMETER/2;
	}

	@Override
	/** Returns y location that this Node's string is mapped relative to its top right coordinate */
	public int getRelativeY() {
		return 0;
	}

	@Override
	/** Returns just this' name for the JSONString - relies on JSONs of Edges and parcels
	 * to take care of themselves
	 */
	public String toJSONString(){
		return "{\n" + Main.addQuotes(MapElement.NAME_TOKEN) + ":" + Main.addQuotes(name) + "\n}";
	}

}
