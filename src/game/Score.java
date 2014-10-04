package game;
import java.awt.Color;

/** The Score Class is a convenient way to monitor the score of a Game. <br>
 * All expected score increases and decreases are specified as public static final variables in the
 * Score class. All variables are meant to be added - costs are represented as negative numbers. <br>
 * The method that allows changing the value of the score is protected, so only files in the gameFiles
 * package are able to change the score. This prevents the user from altering the score, though the
 * getScore() method is public.
 * @author MPatashnik
 */
public class Score {

	/** Milliseconds per frame. Wait time between travel updates*/
	public static final int FRAME = 40;

	/** The Colors that are in the game. Colors of Trucks and of Parcels are chosen from this Array.
	 * Other colors are not allowed in the game, as setColor(Color c) methods must check for c being
	 * contained in COLOR before setting */
	public static final Color[] COLOR = {
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

	/** Returns a random Color from Score.Color */
	public static Color getRandomColor(){
		int i = (int)(Math.random()*(double)COLOR.length);
		return COLOR[i];
	}


	/** Returns true if Color c is in Score.COLOR, false otherwise.
	 * Use to check validity of a color assignment to any colorable thing
	 * @param c - the Color to check
	 * @return true if c in COLOR, false otherwise.
	 */
	public static boolean colorContains(Color c){
		for(Color c2 : COLOR){
			if(c.equals(c2))
				return true;
		}

		return false;
	}

	/** Returns the color represented by String s, that color's toString() output.
	 * Returns null if no such color exits, or if the color isn't in COLOR.
	 */
	public static Color getColor(String color){
		for(Color c : COLOR){
			if(c.toString().equals(color))
				return c;
		}
		return null;
	}

	/** Returns the cost of traveling one frame at a rate of speed.
	 * Most efficient speed is in middle.
	 * <br><br>
	 * Cost = if speed < Efficient_Speed, speed + 1
	 *        else if speed > Efficient_Speed, speed + fib(speed - Efficient_Speed)
	 *        else speed
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
	 * @throws IllegalArgumentException if speed is out of the range [min,max] speed.
	 * @return -Cost, calculated using the above table  
	 */
	public static int cost(int speed) throws IllegalArgumentException{
		if (speed < Truck.MIN_SPEED || speed > Truck.MAX_SPEED)
			throw new IllegalArgumentException("Can't calculated cost for speed " + speed);

		if(speed < Truck.EFFICIENT_SPEED)
			return -(speed + 1);
		else if (speed > Truck.EFFICIENT_SPEED)
			while(true){
				try {
					return -(speed + Main.fib(speed - Truck.EFFICIENT_SPEED));
				} catch (InterruptedException e) {
					try {
						Thread.sleep(5); //Give a short break before trying again.
					} catch (InterruptedException e1) {}	
				}
			}
		else
			return -(speed);
	}

	private int score; //The score maintained by this score object

	/** Constructor. Creates an instance to keep track of the score */
	protected Score(){
		score = 0;
	}

	/** Constructor. Creates an instance to keep track of the score.
	 * @param score - the Initial score of this Game
	 */
	protected Score(int score){
		this.score = score;
	}

	/** Adds addToScore to score */
	protected void changeScore(int addToScore){
		score += addToScore;
	}

	/** Returns the current score */
	public int getScore(){
		return score;
	}

	/** Returns the current score */
	public int value(){
		return getScore();
	}
}
