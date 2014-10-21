package game;

import gui.GUI;

import java.util.Random;

/** Allows for the running of many games
 * @author MPatashnik
 *
 */
public class GameRunner {

	private final String userManagerClass;
	private GUI gui;
	private static final long UPDATE_FRAME = 1000;
	
	GameRunner(String userManagerClassname){
		this.userManagerClass = userManagerClassname;
	}
	
	/** Runs the userManager on the given seeds */
	GameScore[] runSeeds(long[] seeds, boolean printOutput){
		GameScore[] gs = new GameScore[seeds.length];
		
		if(printOutput){
			System.out.println("Seed\t\t\tScore");
			System.out.println("---------------------------------");
		}
		
		
		for(int i = 0; i < seeds.length; i++){
			Game g = new Game(userManagerClass, seeds[i]);
			if(gui == null) gui = new GUI(g);
			else gui.setGame(g);
			gui.toggleInteractable();
			gs[i] = monitor(g);
			if(printOutput){
				System.out.println(gs[i].game.getSeed() + "\t" + gs[i].score);
			}
		}
		gui.dispose();
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
	
	/** Monitors game g */
	private GameScore monitor(Game g){
		g.start();
		try{
			while(! g.isFinished()){
				Thread.sleep(UPDATE_FRAME);
			}
			Thread.sleep(UPDATE_FRAME);
			return new GameScore(g, g.getManager().getScore(), "Success");
		}catch(InterruptedException e){
			return new GameScore(g,g.getManager().getScore(), "GameRunner Thread Interrupted");
		}
	}
	
	/** A holder for a run on a single game.
	 * Records the game that was run and the score received
	 * @author MPatashnik
	 */
	class GameScore{
		public final Game game;
		public final int score;
		public final String message;
		
		public GameScore(Game g, int s, String message){
			game = g;
			score = s;
			this.message = message;
		}
	}
}
