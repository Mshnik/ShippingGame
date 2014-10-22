package game;

/** An extension of ThreadGroup to do custom uncaught error handling
 * 
 * @author MPatashnik
 *
 */
public class GameThreadGroup extends ThreadGroup {

	/** The game this is maintains the threads for */
	private final Game game;
	
	/** Constructs a new GameThreadGroup (with name "Game Threads")
	 * Assigned to the game g
	 */
	public GameThreadGroup(Game g) {
		super("Game Threads");
		game = g;
	}
	
	/** Called when a thread that is a member of this threadgroup
	 * throws an exception that is not caught.
	 * 
	 * Stores the throwable e in the game and interrupts the monitoring thread.
	 */
	@Override
	public void uncaughtException(Thread t, Throwable e){
		game.throwable = e;
		game.monitoringThread.interrupt();
	}

}
