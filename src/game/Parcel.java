package game;
import gui.Circle;

import java.awt.Color;
import java.util.concurrent.Semaphore;

/** Class Parcel  represents a carriable and deliverable package
 * in the game. Each Parcel has a start location and a destination.
 * When dropped off at its desired destination, the parcel disappears
 * from the game and the player is awarded points. If the parcel matches colors
 * with the Truck carrying it, the point award for delivery is higher <br><br>
 * 
 * Only one truck can carry a parcel at a time, each truck can carry a maximum
 * of one parcel.
 * 
 * @author MPatashnik
 *
 */
public final class Parcel implements BoardElement {

	/** The board to which this Parcel belongs. */
	private final Board board;

	/** The node on which this this parcel started. */
	public final Node start;

	/** This parcel's current location. */
	private Node location;

	/** The node where this parcel is to be delivered. */
	public final Node destination;

	private Color color; //The color of this Parcel.
	private Circle circle; //The GUI Circle that represents this Parcel on the gui.

	private Semaphore parcelLock; //A lock for this object, preventing it from
	//being picked up by two trucks, etc.

	private Object userData; //Data (if any) stored by the user in this parcel
	private Truck holder; //The truck (if any) that is carrying this Parcel. null if none

	/** Constructor. A parcel on m that starts on s and has destination d.
	 * It has a random color.
	 * @Param m - The Board to which this parcel belongs
	 * @param s - the Node where this Parcel starts
	 * @param d - the Node where this Parcel goes 
	 * @throws IllegalArgumentException - if s.equals(d). */
	protected Parcel(Board m, Node s, Node d) throws IllegalArgumentException{
		this(m, s, d, Score.getRandomColor());
	}

	/** Constructor. A parcel on m that starts on s and has destination d and color c.
	 * @param m - the Board to which this parcel belongs
	 * @param s - the Node where this Parcel starts
	 * @param d - the Node where this Parcel goes 
	 * @throws IllegalArgumentException - if s.equals(d)*/
	protected Parcel(Board m, Node s, Node d, Color c) throws IllegalArgumentException{
		board = m;

		if (s.equals(d))
			throw new IllegalArgumentException("Illegal Cities passed into Parcel Constructor.");

		start = s;
		location = s;
		destination = d;
		color = c; 
		circle = new Circle(this, s.getCircle().getX1(), s.getCircle().getY1(),
				Circle.DEFAULT_DIAMETER/2, c, true);

		parcelLock = new Semaphore(1);
	}

	/** Return the board on which this Parcel belongs. */
	@Override
	public Board getBoard() {
		return board;
	}

	/** Return the current location for this Parcel */
	public Node getLocation() {
		return location;
	}

	/** Set the location of this parcel to loc.
	 * @param loc - the new location of this parcel
	 */
	protected void setLocation(Node loc) {
		location = loc;
	}

	/** Return the color of this Parcel. Because the color of a parcel has 
	 * game significance, it will not be changed after the game starts running. */
	@Override
	public Color getColor() {
		return color;
	}

	/** Return true - the color of Parcels is significant */
	@Override
	public boolean isColorSignificant(){
		return true;
	}

	/** Return the userData stored in this Node (may be null). */
	@Override
	public Object getUserData() {
		return userData;
	}

	/** Set the userData to uData. To erase the current userData, use argument null). */
	@Override
	public void setUserData(Object uData) {
		userData = uData;
	}

	/** Set the color of this Parcel to c. 
	 * @throws IllegalArguemntException if c is not in Score.COLOR. */
	protected void setColor(Color c) throws IllegalArgumentException {
		if (!Score.colorContains(c))
			throw new IllegalArgumentException("Illegal Color (" + c.toString() +") passed in");

		this.color = c;
	}

	/** Return the circle that represents this Parcel on the GUI */
	public Circle getCircle() {
		return circle;
	}

	/** Have t pick up this Parcel 
	 * @throws InterruptedException */
	protected void pickedUp(Truck t) throws InterruptedException{
		parcelLock.acquire();
		holder = t;
		parcelLock.release();
	}

	/** Have holder drop off this parcel at its current location.
	 * If its current location is its destination, use reachedDestination()
	 * internally to notify the game.
	 * @throws InterruptedException 
	 * @throws RuntimeException if holder is currently null
	 */
	protected void droppedOff() throws InterruptedException{
		parcelLock.acquire();
		if (holder == null) {
			parcelLock.release();
			throw new RuntimeException("Parcel is not on a Truck, cannot be dropped off");
		}

		if (holder.getLocation().equals(destination)) {
			parcelLock.release();
			reachedDestination();
			return;
		}

		setLocation(holder.getLocation());
		holder = null;
		parcelLock.release();
	}

	/** Used when game notifies this parcel that it has reached its destination. */
	private void reachedDestination() {
		board.deliverParcel(this, holder.getLocation(), holder);
		return;
	}

	/** Return an empty string --Parcels do not have mapped names. */
	@Override
	public String getMappedName() {
		return "";
	}

	/** Return this parcel's start location and its color for JSON string. */
	@Override
	public String toJSONString() {
		return "{\n" + Main.addQuotes(BoardElement.LOCATION_TOKEN) + ":" + Main.addQuotes(location.name) + "," +
				"\n" + Main.addQuotes(BoardElement.DESTINATION_TOKEN) + ":" + Main.addQuotes(destination.name) + "," +
				"\n" + Main.addQuotes(BoardElement.COLOR_TOKEN) + ":" + color.getRGB() + 
				"\n}";
	}

	/** Return 0, since Parcels have no mapped name. */
	@Override
	public int getRelativeX() {
		return 0;
	}

	@Override
	/** Return 0, since Parcels have no mapped name. */
	public int getRelativeY() {
		return 0;
	}

	/** Return true iff t currently holds this Parcel. */
	@Override
	public boolean isTruckHere(Truck t) {
		return holder == t;
	}

	/** Returns true iff some truck currently holds this Parcel. */
	public boolean isHeld() {
		return holder != null;
	}

	/** Return 1 if a truck is currently holding this Parcel, 0 otherwise */
	@Override
	public int trucksHere() {
		if (holder == null)
			return 0;
		return 1;
	}

	/** Update the circle position to (x, y).
	 * Relies on the circle for painting. Parcels have no dependents */
	@Override
	public void updateGUILocation(int x, int y) {
		Circle c = getCircle();
		c.setX1(x);
		c.setY1(y);
		c.repaint();
	}
}
