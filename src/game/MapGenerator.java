package game;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Random;
import java.util.HashSet;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONString;

/**
 * The MapGenerator class is a library of static functions for randomly
 * generating maps from seeds.
 * 
 * @author eperdew
 * 
 */
public class MapGenerator {
	
	protected static final String SCORE_TOKEN = "scoreCoeff";
	protected static final String NODE_TOKEN = "node-";
	protected static final String EDGE_TOKEN = "edge-";

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

	private MapGenerator() { }

	/**
	 * Returns a new random map seeded using the default {@code Random}
	 * constructor
	 */
	public static JSONObject randomMap() {
		return randomMap(new Random());
	}

	/** Returns a new random map seeded with {@code seed} */
	public static JSONObject randomMap(long seed) {
		return randomMap(new Random(seed));
	}

	/** Returns a new random map using the {@code Random} parameter {@code r} */
	private static JSONObject randomMap(Random r) {
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
		
		HashSet<Pair> edges = new HashSet<Pair>();
		HashSet<Point> nodes = new HashSet<Point>();
		ArrayList<String> cities = cityNames();
		Point[] points = new Point[numCities];
		
		for (int i = 0; i < numCities; i++) {
			points[i] = new Point(r.nextInt(WIDTH + 1), r.nextInt(HEIGHT + 1),
					cities.remove((int)(Math.random()*cities.size())));
			nodes.add(points[i]);
		}
		for (int i = 1; i < numCities; i++) {
			edges.add(new Pair(points[i-1],points[i]));
		}
		while (edges.size() < (nodes.size()*AVERAGE_DEGREE)/2){
			int i = (int) (Math.random() * points.length);
			int j = i;
			while (i == j){
				j = (int) (Math.random() * points.length);
			}
			edges.add(new Pair(points[i],points[j]));
		}
		JSONObject map = new JSONObject();
		JSONArray scores = new JSONArray();
		scores.put(scoreCoeffs);
	}
	
	private static ArrayList<String> cityNames(){
		File f = new File("MapGeneration/Cities.txt");
		Scanner read = null;
		try {
			read = new Scanner(f);
		}
		catch (FileNotFoundException e){
			System.out.println("Cities.txt not found. Aborting.");
			return new ArrayList<String>();
		}
		ArrayList<String> result = new ArrayList<String>();
		while(read.hasNext()){
			result.add(read.nextLine());
		}
		read.close();
		return result;
	}

	private class Point {
		int x, y;
		String name;

		Point(int x, int y, String name) {
			this.x = x;
			this.y = y;
			this.name = name;
		}

		int distanceTo(Point n) {
			return Math.max(Math.abs(x - n.x) + Math.abs(y - n.y),
					MIN_EDGE_LENGTH);
		}

	}
	
	private class Pair {
		Point p1, p2;
		
		Pair(Point p1, Point p2){
			this.p1 = p1;
			this.p2 = p2;
		}
		
		@Override
		public boolean equals(Object o){
			if (!(o instanceof Pair)){
				return false;
			}
			else{
				Pair p = (Pair) o;
				return (p.p1 == p1 && p.p2 == p2) || (p.p2 == p1 && p.p1 == p2); 
			}
		}

	}

}
