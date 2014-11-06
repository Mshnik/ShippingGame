package game;

import java.awt.Color;

/** A colorable object is an object that has a color field in the game.
 * Getting the color should be open, but setting it is protected such that 
 * setting the color of the object can be done only by the game files.
 * 
 * Color may or may not have a significance within the game (if not, is just for show).
 * As such, the game-significant color should not be changed while the game is running.
 * @author MPatashnik
 *
 */
public interface Colorable {

	/** Return the color of this object. */
	public Color getColor();
	
	/** Return true iff the color of this object has significance within the game */
	public boolean isColorSignificant();
}
