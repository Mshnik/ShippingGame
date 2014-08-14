package gameFiles;

import java.awt.Color;

/** The MapElement interface can be implemented by any class that wants to be drawn on the GUI in ShippingGame.
 * Through the implementation of the MapElement interface, the implementing class will return values on what
 * string name to draw on the GUI, where to draw that name, what color it is, and if a truck is currently on it.
 * @author MPatashnik
 */
public interface MapElement {

	/** The Name this Object has when drawn on the map */
	public String getMappedName();
	
	/** Returns the x coordinate of this Object's string drawing relative to the object */
	public int getRelativeX();
	
	/** Returns the y coordinate of this Object's string drawing relative to the object */
	public int getRelativeY();
	
	/** Returns true if a Truck is currently at/on this MapElement, false otherwise.
	 * Probably will require a lock - throws InterruptedException if lock access is bad. */
	public boolean isTruckHere() throws InterruptedException;
	
	/** Returns the Color of this MapElement */
	public Color getColor();
	
}
