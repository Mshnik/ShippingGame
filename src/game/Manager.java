package game;
import java.util.ArrayList;
import java.util.HashSet;

/** The Manager Class determines the the behavior of the Trucks.
 * In addition to having a getGame() method, other getters are available for ease of use.
 * <br><br>
 * As the Manager Class is abstract, it is up to the user to extend it and define its behavior,
 * which ultimately is the way the user plays the game.
 * <br><br>
 * The run() method determines what the Manager "does" when the game begins and while it is going on.
 * As the getTrucks() method is available, the run() method is how the Manager communicates with Trucks
 * of its own volition. <br>
 * The truckNotification(Truck, int) method is called by Trucks whenever one of many situations occurs,
 * and is how Trucks communicate with the Manager, how the Manager can give instructions in reponse to
 * a change in the Truck's situation.
 * 
 * @author MPatashnik
 *
 */
public abstract class Manager implements Runnable{

	private Game game;
	
	private Thread thread; //The thread this manager is running in.

	/** Constructor for the Manager class.
	 * Written to prevent public construction of Managers. */
	protected Manager(){}

	@Override
	/** Behavior for the Manager and all trucks. To be Overridden in subclasses */
	public abstract void run();

	/** Message options from a truck to a manager include the following:
	 * 	<p>	WAITING - sent whenever a truck is waiting without any travel directions. Sent every time the
	 * 			truck checks that it still has no travel directions (Truck.WAIT_TIME)
	 *  <p> LOCATION_CHANGED - Sent whenever a truck's location field - what Node the truck is currently on - changes.
	 *  		This occurs whenever a truck reaches a new node
	 *  <p> TRAVELING_TO_CHANGED - Sent whenever a truck's travelingTo field - what Node the truck is currently traveling towards
	 *  		- changes. This occurs whenever a truck leaves a node towards a new node.
	 *  <p> GOING_TO_CHANGED - Sent whenever a truck's goingTo field - what Node the truck will end up at when the current travel
	 *  		queue is exhausted - changes. This occurs whenever a new edge is added to a truck's travel directions.
	 *  <p> STATUS_CHANGED - Sent whenever a truck's traveling/waiting status changes. This occurs whenever a truck begins traveling
	 *  		or stops traveling
	 *  <p> PARCEL_AT_NODE - Sent whenever a truck arrives at a node that contains at least one parcel. A PARCEL_AT_NODE notification
	 *  		is always fired after a LOCATION_CHANGED notification
	 *  <p> PICKED_UP_PARCEL - A subset of LOAD_CHANGED notifications. Sent whenever a truck picks up a parcel
	 *  <p> DROPPED_OFF_PARCEL - A subset of LOAD_CHANGED notifications. Sent whenever a truck picks up a parcel
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
	 * Notifications may be sent even if the game has not yet started, so consider filtering calls
	 * based on game.isRunning().
	 * 
	 * @see Manager.Notification The notification enum for types of messages
	 */
	public abstract void truckNotification(Truck t, Notification message);
	
	/** Returns the Game this Manager belongs to */
	public final Game getGame(){
		return game;
	}

	/** Returns the Map for this Game */
	public final Map getMap(){
		return game.getMap();
	}

	/** Returns the Trucks in this Game */
	public final ArrayList<Truck> getTrucks(){
		return game.getTrucks();
	}

	/** Returns the Parcels in this Game */
	public final HashSet<Parcel> getParcels(){
		return game.getParcels();
	}

	/** Sets the game this manager is watching to g */
	public final void setGame(Game g){
		game = g;
	}
	
	/** Sets the Thread this manager is being run in */
	final void setThread(Thread t){
		t.setName("MANAGER-THREAD");
		thread = t;
	}

	/** Called by the game when the game is over */
	final void gameOver(){
		try {
			thread.join(1000); //Wait some time
			thread.interrupt(); //Just interrupt it
		} catch (InterruptedException e) {}
	}

}
