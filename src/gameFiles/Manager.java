package gameFiles;
import java.util.ArrayList;
import java.util.HashSet;

/** The Manager Class determines the the behavior of the Trucks.
 * In addition to having a getGame() method, other getters are available for ease of use.
 * <br><br>
 * As the Manager Class is abstract, it is up to the user to extend it and define its behavior,
 * which ultimately is the way the user plays the game.
 * <br><br>
 * The run() method determines what the Manager "does" when the game begins and while it is going on.
 * As the getTrucks() method is available, the run() method is how the Manager communicates with Trucks. <br>
 * The truckNotification(Truck, int) method is called by Trucks whenever one of many situations occurs,
 * and is how Trucks communicate with the Manager.
 * 
 * @author MPatashnik
 *
 */
public abstract class Manager extends Thread{

	private Game game;

	/** Constructor for the Manager class */
	protected Manager(){}

	@Override
	/** Behavior for the Manager and all trucks. To be Overridden in subclasses */
	public abstract void run();

	public static final int WAITING = 0;

	public static final int LOCATION_CHANGED = 1;
	public static final int TRAVELING_TO_CHANGED = 2;
	public static final int GOING_TO_CHANGED = 3;
	public static final int STATUS_CHANGED = 4;
	public static final int LOAD_CHANGED = 5;

	public static final int PARCEL_AT_NODE = 11;
	public static final int PICKED_UP_PARCEL = 12;
	public static final int DROPPED_OFF_PARCEL = 13;

	/** Returns the Manager message name corresponding to int message,
	 * or null if no message matches.
	 * @param message - the number message to decode
	 * @return - the name of the Message Variable that corresponds to message.
	 */
	public static String decodeMessage(int message){
		switch(message){
		case(LOCATION_CHANGED):
			return "LOCATION_CHANGED";
		case(TRAVELING_TO_CHANGED):
			return "TRAVELING_TO_CHANGED";
		case(GOING_TO_CHANGED):
			return "GOING_TO_CHANGED";
		case(STATUS_CHANGED):
			return "STATUS_CHANGED";
		case(LOAD_CHANGED):
			return "LOAD_CHANGED";
		case(PARCEL_AT_NODE):
			return "PARCEL_AT_NODE";
		case(PICKED_UP_PARCEL):
			return "PICKED_UP_PARCEL";
		case(DROPPED_OFF_PARCEL):
			return "DROPPED_OFF_PARCEL";
		default:
			return null;
		}
	}

	/** Allows Trucks to notify the manager that something has occurred. Message options that may be sent include the following:
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
	 *  <p> LOAD_CHANGED - Sent whenever a truck picks up or drops off a parcel
	 *  <p> PARCEL_AT_NODE - Sent whenever a truck arrives at a node that contains at least one parcel. A PARCEL_AT_NODE notification
	 *  		is always fired after a LOCATION_CHANGED notification
	 *  <p> PICKED_UP_PARCEL - A subset of LOAD_CHANGED notifications. Sent whenever a truck picks up a parcel
	 *  <p> DROPPED_OFF_PARCEL - A subset of LOAD_CHANGED notifications. Sent whenever a truck picks up a parcel
	 * 
	 * Notifications may be sent even if the game has not yet started, so consider filtering calls
	 * based on game.isRunning()
	 */
	public abstract void truckNotification(Truck t, int message);

	/** Returns the Game this Manager belongs to */
	public Game getGame(){
		return game;
	}

	/** Returns the Map for this Game */
	public Map getMap(){
		return game.getMap();
	}

	/** Returns the Trucks in this Game */
	public ArrayList<Truck> getTrucks(){
		return game.getTrucks();
	}

	/** Returns the Parcels in this Game */
	public HashSet<Parcel> getParcels(){
		return game.getParcels();
	}

	/** Sets the game this manager is watching to g */
	protected final void setGame(Game g){
		game = g;
	}

	/** Called by the game when the game is over 
	 * @throws InterruptedException - if this Manager is interrupted*/
	protected final void gameOver() throws InterruptedException{
		Thread.currentThread().join();
	}

}
