package game;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

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

	/** Message options from a truck to a manager include the following. Unless
	 * otherwise noted, the truck's status will be WAITING whenever a notification
	 * is fired. Exceptions are TRAVELING_TO_CHANGED (always) and STATUS_CHANGED
	 * (sometimes).
	 * 	<p>	WAITING: sent whenever a truck is waiting without any travel directions.
	 *         Sent every time the truck checks that it still has no travel directions
	 *         (Truck.WAIT_TIME)
	 *  <p> LOCATION_CHANGED: Sent whenever a truck's location field -- what Node
	 *         the truck is currently on -- changes.
	 *  		This occurs whenever a truck reaches a new node
	 *  <p> TRAVELING_TO_CHANGED: Sent whenever a truck's field travelingTo field
	 *          - what Node the truck is currently traveling toward changes.
	 *          This occurs whenever a truck leaves a node towards a new node.
	 *          Thus this will be fired only while a Truck is traveling, not waiting.
	 *  <p> GOING_TO_CHANGED: Sent whenever a truck's goingTo field - what Node the
	 *         truck will end up at when the current travel queue is exhausted - changes.
	 *         This occurs whenever a new edge is added to a truck's travel directions.
	 *  <p> STATUS_CHANGED: Sent whenever a truck's traveling/waiting status changes.
	 *         This occurs whenever a truck begins traveling or stops traveling. Status
	 *         has successfully been changed when the notification is fired,
	 *          so if the truck's status is TRAVELING and the STATUS_CHANGED notification
	 *          is fired, the truck just had its status changed to TRAVELING and is
	 *          now TRAVELING.
	 *  <p> PARCEL_AT_NODE: Sent whenever a truck arrives at a node that contains
	 *         at least one parcel. A PARCEL_AT_NODE notification is fired after a
	 *         LOCATION_CHANGED notification
	 *  <p> PICKED_UP_PARCEL: Sent whenever a truck picks up a parcel
	 *  <p> DROPPED_OFF_PARCEL: Sent whenever a truck drops off up a parcel
	 */
	public enum Notification{
		WAITING,
		LOCATION_CHANGED,
		TRAVELING_TO_CHANGED,
		GOING_TO_CHANGED,
		STATUS_CHANGED,
		PARCEL_AT_NODE,
		PICKED_UP_PARCEL,
		DROPPED_OFF_PARCEL
	}

	/** Allows Trucks to notify the manager that something has occurred. 
	 * Method should provide the calling truck with additional information
	 * pertaining to the message sent.
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
		if (thread != null) {
			try {
				thread.join(1000); //Wait some time
				thread.interrupt(); //Just interrupt it
			} catch (InterruptedException e) {
				thread.interrupt(); //Just interrupt it
			}
		}
	}

}
