package game;

import java.awt.Color;

import org.json.JSONString;

/** The BoardElement interface can be implemented by any class that wants to be drawn on the GUI in ShippingGame.
 * Through the implementation of the BoardElement interface, the implementing class will return values on what
 * string name to draw on the GUI, where to draw that name, what color it is, and if a truck is currently on it.
 * 
 * All board elements must also be JSON-able, and override the toJSONString method.
 * All board elements must be able to store and return user data stored in them.
 * @author MPatashnik
 */
public interface BoardElement extends JSONString, Colorable, UserData{

	/** The key for the x field for JSON implementations. */
	public static final String X_TOKEN = "x";
	/** The key for the y field for JSON implementations. */
	public static final String Y_TOKEN = "y";
	/** The key for the name field for JSON implementations. */
	public static final String NAME_TOKEN = "name";
	/** The key for the color field for JSON implementations.  */
	public static final String COLOR_TOKEN = "color";
	/** The key for the location field for JSON implementations. 
	 * May be a single value (in parcel), or an array (in edge) */
	public static final String LOCATION_TOKEN = "location";
	/** The key for the destination field for JSON implementations. */
	public static final String DESTINATION_TOKEN = "destination";
	/** The key for the length field for JSON implementations. */
	public static final String LENGTH_TOKEN = "length";
	
	/** The Name this Object has when drawn on the board */
	public String getMappedName();
	
	/** Returns the x coordinate of this Object's string drawing relative to the object */
	public int getRelativeX();
	
	/** Returns the y coordinate of this Object's string drawing relative to the object */
	public int getRelativeY();
	
	/** Returns true if t is currently at/on this BoardElement, false otherwise.
	 * Probably will require a lock maintained internally. */
	public boolean isTruckHere(Truck t);
	
	/** Returns a count of the trucks here 
	 * Probably will require a lock maintained internally. */
	public int trucksHere();
	
	/** Returns the Color of this BoardElement */
	public Color getColor();
	
	/** Update the location of this on the gui */
	public void updateGUILocation(int x, int y);
	
	/** Returns the board this BoardElement belongs to */
	public Board getBoard();
	
}
