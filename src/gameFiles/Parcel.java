package gameFiles;
import gui.Circle;

import java.awt.Color;
import java.util.concurrent.Semaphore;


public class Parcel implements MapElement{
	
	/** The game this Parcel belongs to */
	private final Game game;
	
	/** The node this parcel started the game on */
	private Node start;
	/** This parcel's current location on the map */
	private Node location;
	/** The node this parcel wants to be dropped off at */
	private Node destination;
	
	/** The color of this Parcel */
	private Color color;
	
	/** The GUI Circle that represents this Parcel on the gui */
	private Circle circle;
	
	/** A lock for this object, preventing it from being picked up by two trucks, etc. */
	private Semaphore parcelLock;
	
	/** Data (if any) stored by the user in this parcel */
	private Object userData;
	
	/** The truck (if any) that is carrying this Parcel. null if none */
	private Truck holder;
	
	/** Constructor. Sets start and destination cities, then assigns a random color
	 * @Param g - The game this parcel belongs to
	 * @param start - the Node where this Parcel starts
	 * @param destionation - the Node where this Parcel goes 
	 * @throws IllegalArgumentException - if start.equals(destination)*/
	protected Parcel(Game g, Node start, Node destination) throws IllegalArgumentException{
		this(g, start, destination, Score.getRandomColor());
	}
	
	/** Constructor. Sets start and destination cities, and assigns color
	 * @param g - the Game this parcel belongs to
	 * @param start - the Node where this Parcel starts
	 * @param destionation - the Node where this Parcel goes 
	 * @throws IllegalArgumentException - if start.equals(destination)*/
	protected Parcel(Game g, Node start, Node destination, Color color) throws IllegalArgumentException{
		game = g;
		
		if(start.equals(destination))
			throw new IllegalArgumentException("Illegal Cities passed into Parcel Constructor.");
	
		this.start = start;
		this.location = start;
		this.destination = destination;
		this.color = color; 
		circle = new Circle(this, start.getCircle().getX1(), start.getCircle().getY1(), Circle.DEFAULT_DIAMETER/2, color, true);

		parcelLock = new Semaphore(1);
	}
	
	/** Returns the start Node of this Parcel */
	public Node getStart(){
		return start;
	}
	
	/** Sets the location of a parcel.
	 * @param start - the new location of this parcel
	 * @throws IllegalArgumentException - if start is equal to the current destination
	 */
	protected void setStart(Node start) throws IllegalArgumentException{
		if(start == null || start.equals(destination))
			throw new IllegalArgumentException("Start Node passed into setStart same as current Destination");
		this.start = start;
	}
	
	/** Returns the current location for this Parcel */
	public Node getLocation() {
		return location;
	}

	/** Sets the location of this parcel.
	 * @param newLocation - the new location of this parcel
	 */
	protected void setLocation(Node newLocation){
		this.location = newLocation;
	}

	/** Returns the destination Node for this Parcel */
	public Node getDestination() {
		return destination;
	}

	/** Sets the destination location of a parcel.
	 * @param destination - the new destination location of this parcel
	 * @throws IllegalArgumentException - if destination is equal to the current start
	 */
	protected void setDestination(Node destination) {
		if(destination == null || location.equals(destination))
			throw new IllegalArgumentException("Destination Node passed into setDestination same as current Start");
		this.destination = destination;
	}

	/** Returns the color of this Parcel */
	public Color getColor() {
		return color;
	}

	/** Returns the userData stored in this Node. May be null if the user has not yet given this Node userData */
	public Object getUserData(){
		return userData;
	}
	
	/** Sets the value of userData to Object uData. To erase the current userData just pass in null */
	public void setUserData(Object uData){
		userData = uData;
	}
	
	/** Sets the color of this Parcel. Must be a color in Score.COLOR 
	 * @throws IllegalArguemntException if color is not in Score.COLOR*/
	protected void setColor(Color color) {
		if(!Score.colorContains(color))
			throw new IllegalArgumentException("Illegal Color (" + color.toString() +") passed in");
		
		this.color = color;
	}
	
	/** Gets the circle that represents this Parcel on the GUI */
	public Circle getCircle(){
		return circle;
	}
	
	/** Handles pickup and dropoff in a fashion to prevent thread collision
	 * @param t - The truck picking up or dropping off this Parcel
	 * @param state - either Truck.LOAD or Truck.UNLOAD
	 * @throws InterruptedException */
	protected void loadUnload(Truck t, boolean state) throws InterruptedException{
		if(state == Truck.LOAD){
			pickedUp(t);
		}
		else if(state == Truck.UNLOAD){
			droppedOff();
		}
	}
	
	/** Have truck t pick up this Parcel 
	 * @throws InterruptedException */
	private void pickedUp(Truck t) throws InterruptedException{
		parcelLock.acquire();
		holder = t;
		parcelLock.release();
	}
	
	/** Have holder drop off this parcel at its current location.
	 * If its current location is its destination, use reachedDestination() internally to notify the game.
	 * @throws InterruptedException 
	 * @throws RuntimeException if holder is currently null
	 */
	private void droppedOff() throws InterruptedException{
		parcelLock.acquire();
		if(holder == null){
			parcelLock.release();
			throw new RuntimeException("Parcel is not currently on a Truck, cannot be dropped off");
		}
		
		if(holder.getLocation().equals(destination)){
			reachedDestination();
			parcelLock.release();
			return;
		}
		
		setLocation(holder.getLocation());
		holder = null;
		parcelLock.release();
	}
	
	/** Used when game notifies this parcel that it has reached its destination */
	private void reachedDestination(){
		game.deliverParcel(this, holder.getLocation(), holder);
	}

	@Override
	/** Parcels do not have mapped names. Returns a blank string */
	public String getMappedName() {
		return "";
	}

	@Override
	public int getRelativeX() {
		return 0;
	}

	@Override
	public int getRelativeY() {
		return 0;
	}

	@Override
	/** Returns true if a truck is currently holding this Parcel, false otherwise */
	public boolean isTruckHere() {
		return holder != null;
	}
	
}