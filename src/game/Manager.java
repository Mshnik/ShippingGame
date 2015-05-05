package game;
import java.util.*;

/** Class Manager determines the the behavior of the Trucks.
 * In addition to function getGame(), other getters are available for ease of use.
 * These methods are all final, so they cannot be overwritten in subclasses.
 * <br><br>
 * Since class Manager is abstract, it is up to the user to extend it and define
 * its behavior, which ultimately is the way the user plays the game.
 * <br><br>
 * Method run() determines what the Manager "does" when the game begins and while
 * it is going on. Since function getTrucks() is available, run() is how the Manager
 * communicates with Trucks of its own volition. <br>
 * Method truckNotification(Truck, Notification) is called by Trucks whenever one of
 * many situations occurs; that is how Trucks communicate with the Manager, how the
 * Manager can give instructions in response to a change in the Truck's situation.
 * 
 * @author MPatashnik
 */
public abstract class Manager implements Runnable{

	private Game game;     //The game in which this manager is running.
	private final Score score;   //The score for this manager.
	private Thread thread; //The thread in which this manager is running.

	/** Constructor: an instance with a score but no game or thread.
	 * Written to prevent public construction of Managers. */
	protected Manager() {
		score = new Score(this);
	}

	@Override
	/** Behavior for the Manager and all trucks. To be overridden in subclasses */
	public abstract void run();

	/** Message options from a truck to a manager include the following. 
	 * The truck's status will be WAITING whenever a notification
	 * is fired. 
	 */
	public enum Notification{
		/** Notification sent whenever a truck is waiting (doing nothing) 
		 *  without any travel directions. <br>
		 *  Sent every time the truck checks that it still has no travel directions
		 *  - Every Truck.WAIT_TIME ms. */
		WAITING,

		/** Notification sent whenever a truck's location field -- what Node the truck is 
		 *  currently on -- changes. This occurs whenever a truck reaches a new node */
		LOCATION_CHANGED,

		/** Notification sent whenever a truck arrives at a node that contains
		 *  at least one parcel. A PARCEL_AT_NODE notification is fired after a
		 *  LOCATION_CHANGED notification */
		PARCEL_AT_NODE
	}

	/** Called by Truck t to notify the manager that something has occurred. 
	 * Method should provide t with additional information pertaining
	 * to the message sent.
	 * 
	 * @see Manager.Notification The notification enum for types of messages
	 */
	public abstract void truckNotification(Truck t, Notification message);

	/** Return the score object - default to prevent access in subclasses. */
	final Score getScoreObject() {
		return score;
	}

	/** Return the current value of the score. */
	public final int getScore() {
		return score.value();
	}

	/** Return the current value of the score. */
	public final int getScoreValue() {
		return getScore();
	}

	/** Return the Game to which this Manager belongs. */
	public final Game getGame() {
		return game;
	}

	/** Return the Board for this Game. */
	public final Board getBoard() {
		return game.getBoard();
	}

	/** Return the Nodes in this Game. */
	public final HashSet<Node> getNodes() {
		return getBoard().getNodes();
	}

	/** Return the Edges in this Game, */
	public final HashSet<Edge> getEdges() {
		return getBoard().getEdges();
	}

	/** Return the Trucks in this Game. */
	public final ArrayList<Truck> getTrucks() {
		return getBoard().getTrucks();
	}

	/** Return the undelivered Parcels in this Game. */
	public final Set<Parcel> getParcels() {
		return getBoard().getParcels();
	}

	/** Set the game this manager is watching to g.
	 * Students: don't call this. */
	public final void setGame(Game g) {
		game = g;
	}

	/** Set the Thread on which this manager is being run. */
	final void setThread(Thread t) {
		t.setName("MANAGER-THREAD");
		thread = t;
	}

	/** Called by the game when the game is over.
	 * If thread is null, do nothing because it was never started in the first place. */
	final void gameOver() {
		if (thread != null) thread.interrupt();
	}

}
