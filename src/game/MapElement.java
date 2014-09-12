package game;

import java.awt.Color;

import org.json.JSONString;

/** The MapElement interface can be implemented by any class that wants to be drawn on the GUI in ShippingGame.
 * Through the implementation of the MapElement interface, the implementing class will return values on what
 * string name to draw on the GUI, where to draw that name, what color it is, and if a truck is currently on it.
 * 
 * All map elements must also be JSON-able, and override the toJSONString method.
 * @author MPatashnik
 */
public interface MapElement extends JSONString, Colorable{

	/** The key for the name field */
	public static final String NAME_TOKEN = "name";
	/** The key for the color field */
	public static final String COLOR_TOKEN = "color";
	/** The key for the location field. May be a single value (in parcel), or an array (in edge) */
	public static final String LOCATION_TOKEN = "location";
	/** The key for the destination field. */
	public static final String DESTINATION_TOKEN = "destination";
	/** The key for the length field. */
	public static final String LENGTH_TOKEN = "length";
	
	/** The Name this Object has when drawn on the map */
	public String getMappedName();
	
	/** Returns the x coordinate of this Object's string drawing relative to the object */
	public int getRelativeX();
	
	/** Returns the y coordinate of this Object's string drawing relative to the object */
	public int getRelativeY();
	
	/** Returns true if t is currently at/on this MapElement, false otherwise.
	 * Probably will require a lock - throws InterruptedException if lock access is bad. */
	public boolean isTruckHere(Truck t) throws InterruptedException;
	
	/** Returns a count of the trucks here 
	 * Probably will require a lock*/
	public int trucksHere() throws InterruptedException;
	
	/** Returns the Color of this MapElement */
	public Color getColor();
	
	/** Update the location of this on the gui */
	public void updateGUILocation(int x, int y);
	
}
