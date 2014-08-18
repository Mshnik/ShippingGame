package game;

import java.awt.Color;

/** A colorable object is an object that has a color field in the game.
 * Getting the color should be open, but setting it is protected such that 
 * setting the color of the object can only be done by the game files.
 * @author MPatashnik
 *
 */
public interface Colorable {

	/** Returns the color of this object. */
	public Color getColor();
	
}
