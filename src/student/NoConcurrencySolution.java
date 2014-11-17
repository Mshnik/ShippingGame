package student;

import java.util.*;
import game.*;

/** A simple solution designed to not have to do any thread safety measures
 * 
 * @author MPatashnik
 *
 */
@SuppressWarnings("unchecked")
public class NoConcurrencySolution extends AbstractSolution {

	private boolean preprocessingDone = false;
	
	@Override
	public void run() {
		
		ArrayList<Truck> trucks = getTrucks();
		for(Truck t : trucks){
			t.setUserData(new LinkedList<Parcel>()); //make the userdata for a truck the queue of parcels
		}
		
		//Rotate through, assign each parcel to a truck by adding it to the end of its queue
		int i = 0;
		for(Parcel p : getParcels()){
			LinkedList<Parcel> q = (LinkedList<Parcel>)trucks.get(i % trucks.size()).getUserData();
			q.add(p);
			i++;
		}
		
		preprocessingDone = true;
	}

	@Override
	public void truckNotification(Truck t, Notification message) {
		if(! preprocessingDone) return; //Take no visitors until preprocessing is done

		//Only take waiting for notifications
		if(message != Notification.WAITING) return;
		
		LinkedList<Parcel> queue = (LinkedList<Parcel>) t.getUserData();
		if(queue.isEmpty() && t.getLoad() == null) t.setTravelPath(dijkstra(t.getLocation(), getBoard().getTruckDepot()));
		else if(t.getLoad() != null){
			if(t.getLoad().destination.equals(t.getLocation())){
				t.dropoffLoad();
				truckNotification(t, Notification.WAITING); //Re-fire to cause new load and new travel
			} else{
				t.setTravelPath(dijkstra(t.getLocation(), t.getLoad().destination));
			}
		}else{
		    Parcel p = queue.peek();
		    if(t.getLocation().isParcelHere(p)){
		    	t.pickupLoad(p);
		    	queue.poll();
		    	truckNotification(t, Notification.WAITING);
		    } else{
		    	t.setTravelPath(dijkstra(t.getLocation(), p.getLocation()));
		    }
		}
		
	}

}
