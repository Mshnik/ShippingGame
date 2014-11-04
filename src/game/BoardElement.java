package game;

import java.awt.Color;

import org.json.JSONString;

/** Interface BoardElement can be implemented by any class that wants to be drawn
 * on the GUI in ShippingGame. Through the implementation of this interface, the
 * implementing class will return values on what string name to draw on the GUI,
 * where to draw that name, what color it is, and if a truck is currently on it.
 * 
 * All board elements must also be JSON-able and override function toJSONString.
 * All board elements must be able to store and return user data stored in them.
 * @author MPatashnik
 */
public interface BoardElement extends JSONString, Colorable, UserData {

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
	
	/** Return the x coordinate of this Object's string drawing relative to the object. */
	public int getRelativeX();
	
	/** Return the y coordinate of this Object's string drawing relative to the object. */
	public int getRelativeY();
	
	/** Return true iff t is currently at/on this BoardElement.
	 * Probably will require a lock maintained internally. */
	public boolean isTruckHere(Truck t);
	
	/** Return the number of trucks here. 
	 * Probably will require a lock maintained internally. */
	public int trucksHere();
	
	/** Return the Color of this BoardElement. */
	@Override
	public Color getColor();
	
	/** Update the location of this on the gui (change it to (x, y). */
	public void updateGUILocation(int x, int y);
	
	/** Return the board to which this BoardElement. */
	public Board getBoard();
	
}
