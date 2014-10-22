package game;

import gui.GUI;

import java.io.File;
import java.util.Random;

/** Allows for the running of many games, monitoring them and returning
 * @author MPatashnik
 *
 */
public class GameRunner {

	/** The user manager class (name) that this is running games for */
	private final String userManagerClass;
	
	private GUI gui;
	private static final long UPDATE_FRAME = 1000;
	
	/** Extra time alloted before timeout, for calculation and stuff */
	private static final double TIME_ALLOWANCE = 5000;
	
	/** Number of components of stack trace to include in status message printed to console */
	private final int STACK_TRACE_LENGTH = 3;
	
	/** True if this GameRunnier has a gui, false otherwise */
	private final boolean hasGUI;
	
	/** True if this GameRunner should print output to the console, false otherwise */
	private final boolean printOutput;

	/** Creates a new GameRunner to run a set of games using the specified userManagerClassname */
	public GameRunner(String userManagerClassname, boolean hasGUI, boolean printOutput){
		this.userManagerClass = userManagerClassname;
		this.hasGUI = hasGUI;
		this.printOutput = printOutput;
	}
	
	/** Runs the userManager on the given files, assuming files in Maps/* */
	public GameScore[] runFiles(String[] fNames){
		Game[] g = new Game[fNames.length];
		for(int i = 0; i < fNames.length; i++){
			g[i] = new Game(userManagerClass, new File(Game.MAP_DIRECTORY + fNames[i] + Game.MAP_EXTENSION));
		}
		return runGames(g);
	}
	
	/** Runs the userManager on the given seeds */
	public GameScore[] runSeeds(long[] seeds){
		Game[] g = new Game[seeds.length];
		for(int i = 0; i < seeds.length; i++){
			g[i] = new Game(userManagerClass, seeds[i]);
		}
		return runGames(g);
	}
	
	/** Runs the userManager on the given number of random seeds. */
	public GameScore[] runRandom(int numberTrials){
		long[] seeds = new long[numberTrials];
		for(int i = 0; i < seeds.length; i++){
			seeds[i] = new Random().nextLong();
		}
		return runSeeds(seeds);
	}
	
	/** Runs the userManager on the given set of games */
	private GameScore[] runGames(Game[] games){
		GameScore[] gs = new GameScore[games.length];
		
		if(printOutput){
			System.out.println("Seed\t\t\tScore\tStatus");
			System.out.println("----------------------------------------");
		}
		
		for(int i = 0; i < games.length; i++){
			Game g = games[i];
			if(hasGUI){
				if(gui == null) gui = new GUI(g);
				else gui.setGame(g);
				gui.toggleInteractable();
			}
			gs[i] = monitor(g);
			if(printOutput){
				System.out.println(String.format("%20d", gs[i].game.getSeed()) + "  " 
						+ String.format("%7d",gs[i].score) + "  " + gs[i].message);
			}
		}
		if(hasGUI && gui != null) gui.dispose();
		return gs;
	}
	
	/** Monitors game g.
	 * Caps g's running time based on total number of parcels and total number of trucks. 
	 **/
	private GameScore monitor(Game g){
		
		//Calculate the longest possible time g could take to complete with a deterministic algorithm
		final double parcelTruckRatio = Math.max(1, ((double)g.getBoard().getParcels().size())/((double)g.getBoard().getTrucks().size()));
		final double maxPathLength = g.getBoard().getMaxLength() * g.getBoard().getEdgesSize();
		final long maxTime = (long)(maxPathLength * parcelTruckRatio * 1.5 + TIME_ALLOWANCE);
		
		//Set the monitoring thread as this thread, start g
		g.monitoringThread = Thread.currentThread();
		g.start();
		
		//Find the time the game should end by
		final long maxFinishTime = System.currentTimeMillis() + maxTime;
		try{
			while(! g.isFinished() && System.currentTimeMillis() < maxFinishTime){
				Thread.sleep(UPDATE_FRAME);
			}
			if(System.currentTimeMillis() < maxFinishTime){
				Thread.sleep(UPDATE_FRAME);
				return new GameScore(g, g.getManager().getScore(), GameStatus.SUCCESS, "Success :-)");
			} else{
				return new GameScore(g, g.getManager().getScore(), GameStatus.TIMEOUT, "Game Timeout after " + maxTime + "ms");
			}
		}catch(InterruptedException e){
			String msg = g.throwable.toString();
			for(int i = 0; i < Math.min(g.throwable.getStackTrace().length - 1, STACK_TRACE_LENGTH); i++){
				msg += " at " + g.throwable.getStackTrace()[i].toString();
			}
			return new GameScore(g,g.getManager().getScore(), GameStatus.ERROR, "Exception Thrown - " + msg);
		}
	}
	
	/** Different results of a game.
	 * SUCCESS - game ended successfully
	 * TIMEOUT - game was terminated by the runner because it ran too long
	 * ERROR - game terminated itself because of internal error.
	 * @author MPatashnik
	 */
	public enum GameStatus{
		SUCCESS,
		TIMEOUT,
		ERROR
	}
	
	
	/** A holder for a run on a single game.
	 * Records the game that was run and the score received
	 * @author MPatashnik
	 */
	public class GameScore{
		public final Game game;
		public final int score;
		public final GameStatus status;
		public final String message;
		
		public GameScore(Game g, int s, GameStatus status, String message){
			game = g;
			score = s;
			this.status = status;
			this.message = message;
		}
	}
}
