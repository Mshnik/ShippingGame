package game;

import gui.GUI;

import java.io.File;
import java.util.Random;

/**
 * Allows for the running of many games, monitoring them and returning.
 * 
 * @author MPatashnik
 *
 */
public class GameRunner {

	/** The user manager class (name) that this is running games for */
	private final String userManagerClass;

	private GUI gui;
	private static final long UPDATE_FRAME = 10;

	/** Extra time alloted before timeout, for calculation and stuff */
	private static final double TIME_ALLOWANCE = 10000;

	/**
	 * Number of components of stack trace to include in status message printed
	 * to console
	 */
	private final int STACK_TRACE_LENGTH = 3;

	/** True if this GameRunnier has a gui, false otherwise */
	private final boolean hasGUI;

	/**
	 * True if this GameRunner should print output to the console, false
	 * otherwise
	 */
	private final boolean printOutput;
	
	/** True if gameRunners should print extra debugging output (time allotted) */
	private static final boolean PRINT_DEBUG = false;

	/**
	 * Create a new GameRunner to run a set of games using userManagerClassname.
	 * A gui will be used iff hasGUI. Output will be printed iff printOutput
	 */
	public GameRunner(String userManagerClassname, boolean hasGUI,
			boolean printOutput) {
		this.userManagerClass = userManagerClassname;
		this.hasGUI = hasGUI;
		this.printOutput = printOutput;
	}

	/**
	 * Run the userManager on the files in fNames, assuming they are in
	 * directory Maps/*
	 */
	public GameScore[] runFiles(String[] fNames) {
		Game[] g = new Game[fNames.length];
		for (int i = 0; i < fNames.length; i++) {
			g[i] = new Game(userManagerClass, new File(Game.MAP_DIRECTORY
					+ fNames[i] + Game.MAP_EXTENSION));
		}
		return runGames(g);
	}

	/** Run the userManager on seeds seeds */
	public GameScore[] runSeeds(long[] seeds) {
		Game[] g = new Game[seeds.length];
		for (int i = 0; i < seeds.length; i++) {
			g[i] = new Game(userManagerClass, seeds[i]);
		}
		return runGames(g);
	}

	/** Run the userManager on n random seeds. */
	public GameScore[] runRandom(int n) {
		long[] seeds = new long[n];
		for (int i = 0; i < seeds.length; i++) {
			seeds[i] = new Random().nextLong();
		}
		return runSeeds(seeds);
	}

	/** Run the userManager on the games in games */
	private GameScore[] runGames(Game[] games) {
		GameScore[] gs = new GameScore[games.length];

		if (printOutput) {
			System.out.println("Seed\t\t\tScore\tStatus");
			System.out.println("----------------------------------------");
		}

		for (int i = 0; i < games.length; i++) {
			Game g = games[i];
			if (hasGUI) {
				if (gui == null)
					gui = new GUI(g);
				else
					gui.setGame(g);
				gui.toggleInteractable();
			}
			try {
				gs[i] = monitor(g);
			} catch (Exception e) {
				String msg = "" + e;
				gs[i] = new GameScore(g, g.getManager().getScore(),
						GameStatus.ERROR, "Exception Thrown - " + msg);
			}
			if (printOutput) {
				System.out.println(String.format("%20d", gs[i].game.getSeed())
						+ "  " + String.format("%7d", gs[i].score) + "  "
						+ gs[i].message);
			}
			if(PRINT_DEBUG) System.err.println("Finished board " + (i + 1));
		}
		return gs;
	}

	/**
	 * Monitors game g. Caps g's running time based on total number of parcels
	 * and total number of trucks.
	 **/
	private GameScore monitor(Game g) {

		// Calculate the longest possible time g could take to complete with a
		// deterministic algorithm
		final double parcelTruckRatio = Math.max(1, ((double) g.getBoard()
				.getParcels().size())
				/ ((double) g.getBoard().getTrucks().size()));
		final double maxPathLength = g.getBoard().getMaxLength()
				* g.getBoard().getEdgesSize();
		final long maxTime = (long) (maxPathLength * parcelTruckRatio / 5 + TIME_ALLOWANCE);
		if(PRINT_DEBUG) 
			System.err.println(">> Time Allowed : " + maxTime);

		// Set the monitoring thread as this thread, start g
		g.monitoringThread = Thread.currentThread();
		g.start();

		// Find the time the game should end by
		final long maxFinishTime = System.currentTimeMillis() + maxTime;
		try {
			while (!g.isFinished()
					&& System.currentTimeMillis() < maxFinishTime) {
				synchronized(g.endCondition){
					g.endCondition.wait(maxTime);
				}
			}
			if (System.currentTimeMillis() < maxFinishTime) {
				Thread.sleep(UPDATE_FRAME);
				return new GameScore(g, g.getManager().getScore(),
						GameStatus.SUCCESS, "Success :-)");
			} else {
				g.forceKill();
				return new GameScore(g, g.getManager().getScore(),
						GameStatus.TIMEOUT, "Game Timeout after " + maxTime
								+ "ms");
			}
		} catch (InterruptedException e) {
			String msg = "" + g.throwable;
			if (g.throwable != null) {
				for (int i = 0; i < Math.min(
						g.throwable.getStackTrace().length - 1,
						STACK_TRACE_LENGTH); i++) {
					msg += " at " + g.throwable.getStackTrace()[i];
				}
			}
			g.forceKill();
			return new GameScore(g, g.getManager().getScore(),
					GameStatus.ERROR, "Exception Thrown - " + msg);
		}
	}

	/**
	 * Different results of a game.
	 * 
	 * @author MPatashnik
	 */
	public enum GameStatus {
		/** Game ended successfully */
		SUCCESS,
		/** Game was terminated by the runner because it ran too long */
		TIMEOUT,
		/** Game terminated itself because of an internal error. */
		ERROR
	}

	/**
	 * A holder for a run on a single game. Records the game that was run and
	 * the score received, along with other information about the game.
	 * 
	 * @author MPatashnik
	 */
	public static class GameScore {
		/** the game. */
		public final Game game;

		/** the score. */
		public final int score;

		/** The initial number of parcels */
		public final double initialParcels;

		/** The number of delivered parcels remaining */
		public final double deliveredParcels;

		/** The total number of trucks */
		public final double trucks;

		/** The number of trucks who made it home */
		public final double homeTrucks;

		/** the status. */
		public final GameStatus status;

		/** the message. */
		public final String message;

		/**
		 * Constructor: an instance for game g with score s, status status, and
		 * message m.
		 * 
		 * @param g
		 * @param s
		 * @param status
		 * @param m
		 */
		public GameScore(Game g, int s, GameStatus status, String m) {
			game = g;
			initialParcels = g.getBoard().initialParcelCount;
			deliveredParcels = initialParcels
					- g.getBoard().getParcels().size();
			trucks = g.getBoard().getTrucks().size();
			homeTrucks = g.getBoard().getTruckDepot().trucksHere();
			score = s;
			this.status = status;
			message = m;
		}
	}
}
