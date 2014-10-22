package game;

import gui.GUI;

import java.util.Random;

/** Allows for the running of many games, monitoring them and returning
 * @author MPatashnik
 *
 */
public class GameRunner {

	private final String userManagerClass;
	private GUI gui;
	private static final long UPDATE_FRAME = 1000;
	
	/** Extra time alloted before timeout, for calculation and stuff */
	private static final double TIME_ALLOWANCE = 5000;
	
	/** Number of components of stack trace to include in status message printed to console */
	private final int STACK_TRACE_LENGTH = 3;
	
	/** True if this GameRunnier has a gui, false otherwise */
	private final boolean hasGUI;
	
	/** Creates a new GameRunner to run a set of games using the specified userManagerClassname */
	GameRunner(String userManagerClassname, boolean hasGUI){
		this.userManagerClass = userManagerClassname;
		this.hasGUI = hasGUI;
	}
	
	/** Runs the userManager on the given seeds */
	GameScore[] runSeeds(long[] seeds, boolean printOutput){
		GameScore[] gs = new GameScore[seeds.length];
		
		if(printOutput){
			System.out.println("Seed\t\t\tScore\tStatus");
			System.out.println("----------------------------------------");
		}
		
		for(int i = 0; i < seeds.length; i++){
			Game g = new Game(userManagerClass, seeds[i]);
			if(hasGUI){
				if(gui == null) gui = new GUI(g);
				else gui.setGame(g);
				gui.toggleInteractable();
			}
			gs[i] = monitor(g);
			if(printOutput){
				System.out.println(gs[i].game.getSeed() + "\t" + gs[i].score + "\t" + gs[i].message);
			}
		}
		if(hasGUI) gui.dispose();
		return gs;
	}
	
	/** Runs the userManager on the given number of random seeds. */
	GameScore[] runRandom(int numberTrials, boolean printOutput){
		long[] seeds = new long[numberTrials];
		for(int i = 0; i < seeds.length; i++){
			seeds[i] = new Random().nextLong();
		}
		return runSeeds(seeds, printOutput);
	}
	
	/** Monitors game g.
	 * Caps g's running time based on total number of parcels and total number of trucks. 
	 **/
	private GameScore monitor(Game g){
		
		//Calculate the longest possible time g could take to complete with a deterministic algorithm
		final double parcelTruckRatio = Math.max(1, ((double)g.getBoard().getParcels().size())/((double)g.getBoard().getTrucks().size()));
		final double maxPathLength = g.getBoard().getMaxLength() * g.getBoard().getEdgesSize();
		final long maxTime = (long)(maxPathLength * parcelTruckRatio * 2 + TIME_ALLOWANCE);
		
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
			for(int i = 0; i < Math.min(g.throwable.getStackTrace().length, STACK_TRACE_LENGTH); i++){
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
	enum GameStatus{
		SUCCESS,
		TIMEOUT,
		ERROR
	}
	
	
	/** A holder for a run on a single game.
	 * Records the game that was run and the score received
	 * @author MPatashnik
	 */
	class GameScore{
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
