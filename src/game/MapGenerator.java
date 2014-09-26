package game;

import java.util.Random;

/**
 * The MapGenerator class is a library of static functions for randomly
 * generating maps from seeds.
 * 
 * @author eperdew
 * 
 */
public class MapGenerator {

	private static final int MIN_NODES = 5;
	private static final int MAX_NODES = 100;
	private static final double AVERAGE_DEGREE = 2;

	private static final int WIDTH = 100;
	private static final int HEIGHT = 100;
	private static final int MIN_EDGE_LENGTH = 5;

	private static final int WAIT_COST_MIN = 1;
	private static final int WAIT_COST_MAX = 3;

	private static final int PICKUP_COST_MIN = 50;
	private static final int PICKUP_COST_MAX = 150;

	private static final int DROPOFF_COST_MIN = 50;
	private static final int DROPOFF_COST_MAX = 150;

	private static final int PAYOFF_MIN = 3000;
	private static final int PAYOFF_MAX = 10000;

	private static final int ON_COLOR_MULTIPLIER_MIN = 1;
	private static final int ON_COLOR_MULTIPLIER_MAX = 5;

	private MapGenerator() {
	}

	/**
	 * Returns a new random map seeded using the default {@code Random}
	 * constructor
	 */
	public static Map randomMap() {
		return randomMap(new Random());
	}

	/** Returns a new random map seeded with {@code seed} */
	public static Map randomMap(long seed) {
		return randomMap(new Random(seed));
	}

	/** Returns a new random map using the {@code Random} parameter {@code r} */
	private static Map randomMap(Random r) {
		int numCities = r.nextInt(MAX_NODES - MIN_NODES + 1) + MIN_NODES;
		int WAIT_COST = -1
				* (r.nextInt(WAIT_COST_MAX - WAIT_COST_MIN + 1) + WAIT_COST_MIN);
		int PICKUP_COST = -1
				* (r.nextInt(PICKUP_COST_MAX - PICKUP_COST_MIN + 1) + PICKUP_COST_MIN);
		int DROPOFF_COST = -1
				* (r.nextInt(DROPOFF_COST_MAX - DROPOFF_COST_MIN + 1) + DROPOFF_COST_MIN);
		int PAYOFF = r.nextInt(PAYOFF_MAX - PAYOFF_MIN + 1) + PAYOFF_MIN;
		int ON_COLOR_MULTIPLIER = r.nextInt(ON_COLOR_MULTIPLIER_MAX
				- ON_COLOR_MULTIPLIER_MIN + 1)
				+ ON_COLOR_MULTIPLIER_MIN;
		int[] scoreCoeffs = {WAIT_COST,PICKUP_COST,DROPOFF_COST,PAYOFF,ON_COLOR_MULTIPLIER};
		Point[] points = new Point[numCities];
		for (int i = 0; i < numCities; i++) {
			points[i] = new Point(r.nextInt(WIDTH + 1), r.nextInt(HEIGHT + 1));
		}
		for (int i = 0; i < AVERAGE_DEGREE / 2 * numCities; i++) {

		}
	}

	private class Point {
		int x, y;

		Point(int x, int y) {
			this.x = x;
			this.y = y;
		}

		int distanceTo(Point n) {
			return Math.max(Math.abs(x - n.x) + Math.abs(y - n.y),
					MIN_EDGE_LENGTH);
		}

	}

}
