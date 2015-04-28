package game;
import java.awt.Color;
import java.util.Arrays;

/** Class Score monitors the score of a Game for a given Manager. <br>
 * The method that allows changing the value of the score is protected, so only
 * files in package gameFiles are able to change the score. This prevents the user
 * from altering the score, though function getScore() is public. <br><br>
 * Also maintains methods for computing the cost of traveling at a given speed
 * for trucks and validates colors for parcels and trucks.
 * @author MPatashnik
 */
public final class Score {

	/** The Colors in the game. Colors of Trucks and Parcels are chosen from this Array.
	 * Other colors are not allowed in the game, so setColor(Color c) methods must
	 * check for c being contained in COLOR before setting. */
	private static final Color[] COLOR = {
		new Color(198, 0, 0),	//Mid Red
		new Color(198, 0, 144),	//Mid Pink
		new Color(102, 0, 198),	//Mid Purple-Blue
		new Color(0, 113, 198),	//Sea Blue
		new Color(0, 198, 107),	//Mid Green
		new Color(198, 196, 0),	//Mid Yellow
		new Color(255, 79, 79),	//Bright Red
		new Color(251, 79, 255),//Bright Purple
		new Color(79, 220, 255),//Turquoise
		new Color(79, 255, 168),//Bright Green
		new Color(12, 3, 79),	//Something else
		new Color(255, 178, 79),//Bright Orange
		new Color(109, 43, 140)//Dark Purple
	};	
	
	public static Color[] colorArr(){
		return Arrays.copyOf(COLOR, COLOR.length);
	}

	/** Return a random Color from Score.Color. */
	public static Color getRandomColor() {
		return COLOR[(int)(Math.random() * (double)COLOR.length)];
	}


	/** Return true iff c is in Score.COLOR.
	 * Use to check validity of a color assignment to any colorable thing.
	 * @param c - the Color to check
	 * @return true if c in COLOR, false otherwise.
	 */
	public static boolean colorContains(Color c) {
		for (Color c2 : COLOR) {
			if (c.equals(c2))
				return true;
		}

		return false;
	}

	/** Return the color represented by s (its toString format), 
	 * that color's toString() output.
	 * (null if no such color exits, or if the color isn't in COLOR).
	 */
	public static Color getColor(String s) {
		for (Color c : COLOR) {
			if (c.toString().equals(s))
				return c;
		}
		return null;
	}

	/** Return the cost of traveling one frame at rate of speed s.
	 * The most efficient speed is in middle.
	 * See Truck.MIN_SPEED, Truck.MAX_SPEED for boundary values,
	 * Truck.EFFICIENT_SPEED for most cost-effective speed to travel.
	 * <br><br>
	 * {@code Cost =} <br>
	 *        {@code if (s < Efficient_Speed) -> s + 1} <br>
	 *        {@code else if (s > Efficient_Speed) -> s + fib(s - Efficient_Speed)} <br>
	 *        {@code else -> s }
	 * <br> 1 : 2
	 * <br> 2 : 3
	 * <br> 3 : 4
	 * <br> 4 : 4 
	 * <br> 5 : 6
	 * <br> 6 : 8
	 * <br> 7 : 10
	 * <br> 8 : 13
	 * <br> 9 : 17
	 * <br> 10 : 22   
	 * @throws IllegalArgumentException if s is out of the range min..max.
	 * @return -Cost, calculated using the above equation. Sample values are in table. 
	 */
	public static int cost(int s) throws IllegalArgumentException{
		if (s < Truck.MIN_SPEED || s > Truck.MAX_SPEED)
			throw new IllegalArgumentException("Can't calculated cost for speed " + s);

		if (s < Truck.EFFICIENT_SPEED)
			return -(s + 1);
		else if (s > Truck.EFFICIENT_SPEED)
			return -(s + Main.fib(s - Truck.EFFICIENT_SPEED));
		else
			return -s;
	}

	private int score; //The score maintained by this score object
	
	/** The manager for which this Score object keeps track. */
	public final Manager manager;

	/** Constructor: an instance for manager m and with initial score 0. */
	protected Score(Manager m) {
		manager = m;
		score = 0;
	}

	/** Constructor: an instance for manager m and with initial score s.
	 * @param s - the Initial score of this Game
	 */
	protected Score(Manager m, int s) {
		this(m);
		this.score = s;
	}

	/** Add s to the score. */
	protected void changeScore(int s) {
		score += s;
		if (manager.getGame().getGUI() != null) manager.getGame().getGUI().updateScore(score);
	}

	/** Return the current score. */
	public int getScore() {
		return score;
	}

	/** Return the current score. */
	public int value() {
		return getScore();
	}

	/** Return the current score value as a String. */
	@Override
	public String toString() {
		return "" + getScore();
	}
}
