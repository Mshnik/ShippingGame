package game;
import gui.Circle;

import java.awt.Color;
import java.util.concurrent.Semaphore;

/** The Parcel class represents a carriable and deliverable package
 * in the game. Each Parcel has a start location and a destination
 * When dropped off at its desired destination, the parcel will disappear
 * from the game and award the player points. If the parcel matches colors
 * with the Truck carrying it, the point award for delivery is higher <br><br>
 * 
 * Only one truck can carry a parcel at a time, each truck can carry a maximum of one parcel
 * 
 * @author MPatashnik
 *
 */
public class Parcel implements BoardElement{

	/** The board this Parcel belongs to */
	private final Board board;

	/** The node this parcel started the game on */
	public final Node start;
	
	/** This parcel's current location on the board */
	private Node location;
	
	/** The node this parcel wants to be dropped off at to be delivered */
	public final Node destination;

	private Color color; //The color of this Parcel
	private Circle circle; //The GUI Circle that represents this Parcel on the gui

	private Semaphore parcelLock; //A lock for this object, preventing it from being picked up by two trucks, etc.

	private Object userData; //Data (if any) stored by the user in this parcel
	private Truck holder; //The truck (if any) that is carrying this Parcel. null if none

	/** Constructor. Sets start and destination cities, then assigns a random color
	 * @Param m - The Board this parcel belongs to
	 * @param start - the Node where this Parcel starts
	 * @param destionation - the Node where this Parcel goes 
	 * @throws IllegalArgumentException - if start.equals(destination)*/
	protected Parcel(Board m, Node start, Node destination) throws IllegalArgumentException{
		this(m, start, destination, Score.getRandomColor());
	}

	/** Constructor. Sets start and destination cities, and assigns color
	 * @param m - the Board this parcel belongs to
	 * @param start - the Node where this Parcel starts
	 * @param destionation - the Node where this Parcel goes 
	 * @throws IllegalArgumentException - if start.equals(destination)*/
	protected Parcel(Board m, Node start, Node destination, Color color) throws IllegalArgumentException{
		board = m;

		if(start.equals(destination))
			throw new IllegalArgumentException("Illegal Cities passed into Parcel Constructor.");

		this.start = start;
		this.location = start;
		this.destination = destination;
		this.color = color; 
		circle = new Circle(this, start.getCircle().getX1(), start.getCircle().getY1(), Circle.DEFAULT_DIAMETER/2, color, true);

		parcelLock = new Semaphore(1);
	}

	/** Returns the board this Parcel belongs to */
	@Override
	public Board getBoard(){
		return board;
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

	/** Returns the color of this Parcel. Because the color of a parcel has game significance,
	 * this will not be changed after the game starts running. */
	@Override
	public Color getColor() {
		return color;
	}

	/** Returns the userData stored in this Node. May be null if the user has not yet given this Node userData */
	@Override
	public Object getUserData(){
		return userData;
	}

	/** Sets the value of userData to Object uData. To erase the current userData just pass in null */
	@Override
	public void setUserData(Object uData){
		userData = uData;
	}

	/** Sets the color of this Parcel. Must be a color in Score.COLOR 
	 * @throws IllegalArguemntException if color is not in Score.COLOR*/
	protected void setColor(Color color) throws IllegalArgumentException{
		if(!Score.colorContains(color))
			throw new IllegalArgumentException("Illegal Color (" + color.toString() +") passed in");

		this.color = color;
	}

	/** Gets the circle that represents this Parcel on the GUI */
	public Circle getCircle(){
		return circle;
	}

	/** Have truck t pick up this Parcel 
	 * @throws InterruptedException */
	protected void pickedUp(Truck t) throws InterruptedException{
		parcelLock.acquire();
		holder = t;
		int oldParcelOnNodeCount = board.parcelCounts.get(Board.PARCELS_ON_MAP);
		board.parcelCounts.set(Board.PARCELS_ON_MAP, oldParcelOnNodeCount - 1);
		int oldParcelOnTruckCount = board.parcelCounts.get(Board.PARCELS_ON_TRUCK);
		board.parcelCounts.set(Board.PARCELS_ON_TRUCK, oldParcelOnTruckCount + 1);
		board.game.getGUI().updateParcelStats();
		parcelLock.release();
	}

	/** Have holder drop off this parcel at its current location.
	 * If its current location is its destination, use reachedDestination() internally to notify the game.
	 * @throws InterruptedException 
	 * @throws RuntimeException if holder is currently null
	 */
	protected void droppedOff() throws InterruptedException{
		parcelLock.acquire();
		if(holder == null){
			parcelLock.release();
			throw new RuntimeException("Parcel is not currently on a Truck, cannot be dropped off");
		}
		int oldParcelOnTruckCount = board.parcelCounts.get(Board.PARCELS_ON_TRUCK);
		board.parcelCounts.set(Board.PARCELS_ON_TRUCK, oldParcelOnTruckCount - 1);

		if(holder.getLocation().equals(destination)){
			reachedDestination();
			parcelLock.release();
			int oldParcelDeliveredCount = board.parcelCounts.get(Board.PARCELS_DELIVERED);
			board.parcelCounts.set(Board.PARCELS_DELIVERED, oldParcelDeliveredCount + 1);
			board.game.getGUI().updateParcelStats();
			return;
		}

		setLocation(holder.getLocation());
		holder = null;
		int oldParcelOnNodeCount = board.parcelCounts.get(Board.PARCELS_ON_MAP);
		board.parcelCounts.set(Board.PARCELS_ON_MAP, oldParcelOnNodeCount + 1);
		board.game.getGUI().updateParcelStats();
		parcelLock.release();
	}

	/** Used when game notifies this parcel that it has reached its destination */
	private void reachedDestination(){
		board.deliverParcel(this, holder.getLocation(), holder);
		return;
	}

	/** Parcels do not have mapped names. Returns a blank string */
	@Override
	public String getMappedName() {
		return "";
	}

	/** Returns this' start location and its color for JSON string */
	@Override
	public String toJSONString(){
		return "{\n" + Main.addQuotes(BoardElement.LOCATION_TOKEN) + ":" + Main.addQuotes(location.name) + "," +
				"\n" + Main.addQuotes(BoardElement.DESTINATION_TOKEN) + ":" + Main.addQuotes(destination.name) + "," +
				"\n" + Main.addQuotes(BoardElement.COLOR_TOKEN) + ":" + color.getRGB() + 
				"\n}";
	}

	/** Returns 0, since Parcels have no mapped name */
	@Override
	public int getRelativeX() {
		return 0;
	}

	@Override
	/** Returns 0, since Parcels have no mapped name */
	public int getRelativeY() {
		return 0;
	}

	/** Returns true if a truck is currently holding this Parcel, false otherwise */
	@Override
	public boolean isTruckHere(Truck t) {
		return holder == t;
	}

	/** Returns 1 if a truck is currently holding this, 0 otherwise */
	@Override
	public int trucksHere(){
		if(holder == null)
			return 0;
		return 1;
	}

	/** Relies on circle for painting. Parcels have no dependents */
	@Override
	public void updateGUILocation(int x, int y) {
		Circle c = getCircle();
		c.setX1(x);
		c.setY1(y);
		c.repaint();
	}
}
