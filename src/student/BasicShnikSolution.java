package student;

import java.util.*;
import game.*;

/** A simple solution to the shipping game problem.
 * Greedy solution: assign each parcel to a truck, that truck gets it, 
 * delivers it, gets new parcel to work on, etc. etc.
 * Very simple: no consideration for current location, color, etc.
 * @author MPatashnik
 */
public class BasicShnikSolution extends AbstractSolution {

	/** A HashMap of parcel -> truck that is assigned to deliver it */
	private Map<Truck, Parcel> parcelsAssigned;
	
	/** Unassigned parcels */
	private Set<Parcel> unassignedParcels;
	
	/** True once the run step is done, false until then */
	private boolean preprocessingDone;
	
	@Override
	public void run() {
		parcelsAssigned = Collections.synchronizedMap(new HashMap<Truck, Parcel>());
		unassignedParcels = Collections.synchronizedSet(new HashSet<Parcel>());
		
		ArrayList<Truck> trucks = getTrucks();
		int i = 0;
		
		//Assign every parcel to a truck as possible, or put it in the unassigned list
		for (Parcel p : getParcels()) {
			if (i >= trucks.size()) {
				unassignedParcels.add(p);
			}
			else {
				parcelsAssigned.put(trucks.get(i), p);
				i++;
			}
		}
		preprocessingDone = true;
	}

	@Override
	public void truckNotification(Truck t, Notification message) {
		if (! preprocessingDone) return;
		
		//Base case - at new node. Check if has parcel and should drop off.
		if (message.equals(Notification.LOCATION_CHANGED)) {
			if (t.getLoad() != null && t.getLoad().destination.equals(t.getLocation())) {
				t.dropoffLoad();
				
				//Check if there are more parcels to handle. If not, remove from
				// assignment and go home.
				//If so, assign this truck to a random one.
				if (unassignedParcels.isEmpty()) {
					parcelsAssigned.remove(t);
					t.setTravelPath(dijkstra(t.getLocation(), getBoard().getTruckHome()));
				} else {
					Parcel p = Main.randomElement(unassignedParcels);
					unassignedParcels.remove(p);
					parcelsAssigned.put(t, p);
				}
			}
		}
		//Wait frame notification - logic branches a bit.
		//if t is holding a load, travel to that location.
		else if (message.equals(Notification.WAITING)) {
			if (t.getLoad() != null) {
				t.setTravelPath(dijkstra(t.getLocation(), t.getLoad().destination));
			} else if (parcelsAssigned.containsKey(t)) {
				t.pickupLoad(parcelsAssigned.get(t));
				t.setTravelPath(dijkstra(t.getLocation(), parcelsAssigned.get(t).getLocation()));
			}
		}
		
	}
}