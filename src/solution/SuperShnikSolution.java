package solution;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import game.*;

public class SuperShnikSolution extends AbstractSolution {

	private Map<Tuple2<Node, Node>, Tuple2<Integer,List<Node>>> paths;
	private Node truckDepot;
	private boolean preprocessingDone;

	@Override
	public void run() {
		//long startTime = System.currentTimeMillis();

		truckDepot = getBoard().getTruckDepot();

		//Determine which nodes are important
		HashSet<Node> importantLocations = new HashSet<Node>();
		importantLocations.add(getBoard().getTruckDepot());
		for(Parcel p : getParcels()){
			importantLocations.add(p.start);
			importantLocations.add(p.destination);
		}

		paths = new HashMap<>();

		//Calculate all paths among important nodes
		for(Node n : importantLocations){
			Map<Node, List<Node>> pths = dijkstra(n);
			for(Map.Entry<Node, List<Node>> e : pths.entrySet()){
				Node m = e.getKey();
				List<Node> path = e.getValue();
				if(importantLocations.contains(m)){
					paths.put(new Tuple2<Node, Node>(n, m), 
							new Tuple2<Integer, List<Node>>(pathLength(path), path));
				}
			}
		}

		//Assign all trucks a user data
		for(Truck t : getTrucks()){
			t.setUserData(new TruckData());
		}


		//Divy parcels among trucks
		LinkedList<Truck> trucksRotation = new LinkedList<Truck>(getTrucks());
		for(Parcel p : getParcels()){
			Truck getter = null;

			for(Truck t : trucksRotation){
				if(p.getColor().equals(t.getColor())){
					getter = t;
					break;
				}
			}

			if(getter == null) getter = trucksRotation.getFirst();

			TruckData td = (TruckData) getter.getUserData();
			td.toCollect.add(p);

			trucksRotation.remove(getter);
			trucksRotation.add(getter);
		}

		//Start each truck
		for(Truck t : getTrucks()){
			TruckData td = (TruckData) t.getUserData();
			if(td.toCollect.isEmpty()){
				td.done = true;
			} else{
				pickNextLoad(t);
				routeTo(t, td.toPickup.start);
			}
		}

		//System.out.println("Preprocessing took " + (System.currentTimeMillis() - startTime) + "ms");
		preprocessingDone = true;
	}

	private static class TruckData{
		boolean done = false;
		boolean needsInstruction = true;
		Node destination = null;
		Parcel toPickup = null;
		LinkedList<Parcel> toCollect = new LinkedList<>();
	}

	/** Returns {@code n1, n2} as a tuple */
	private static Tuple2<Node,Node> asTuple(Node n1, Node n2){
		return new Tuple2<Node,Node>(n1,n2);
	}

	@Override
	public void truckNotification(Truck t, Notification message) {
		if(! preprocessingDone || message.equals(Notification.PARCEL_AT_NODE)){
			return;
		}

		TruckData td = (TruckData)t.getUserData();

		if(message.equals(Notification.LOCATION_CHANGED)){
			td.needsInstruction = !td.done && t.getLocation() == td.destination;
		}

		if(! td.needsInstruction){
			return;
		}

		//State 4 - At assigned parcel's location
		if(t.getLoad() == null && td.toPickup != null && t.getLocation() == td.toPickup.start){
			t.pickupLoad(td.toPickup);
			routeTo(t, td.toPickup.destination);
			return;
		}

		//State 5 - At held parcel's location. Check this to go straight to state 1 or 6
		if(t.getLoad() != null && t.getLocation() == t.getLoad().destination){
			td.toPickup = null;
			t.dropoffLoad();			
		}

		//State 6 - no current parcel, no assigned parcel
		// Go home if not already there
		if(t.getLoad() == null && td.toCollect.isEmpty()){
			routeTo(t, truckDepot);
			td.done = true;
			return;
		}

		if(t.getLoad() == null && ! td.toCollect.isEmpty()){
			pickNextLoad(t);
			routeTo(t, td.toPickup.start);
			return;
		}
	}

	/** Searches among assigned parcels for best to pickup */
	private void pickNextLoad(Truck t){
		Tuple2<Integer, List<Node>> closest = new Tuple2<>(Integer.MAX_VALUE, null);
		Parcel best = null;
		TruckData td = (TruckData)t.getUserData();
		for(Parcel p : td.toCollect){
			Tuple2<Integer, List<Node>> c = paths.get(asTuple(t.getLocation(), p.start));
			if(c._1 < closest._1){
				closest = c;
				best = p;
			}
		}

		td.toPickup = best;
		td.toCollect.remove(best);
	}

	/** Causes t to travel towards dest, updates fields as needed */
	private void routeTo(Truck t, Node dest){
		TruckData td = (TruckData)t.getUserData();
		td.destination = dest;
		if(dest != t.getLocation()){
			t.setTravelPath(paths.get(asTuple(t.getLocation(), dest))._2);
			td.needsInstruction = false;
		}
		else{
			td.needsInstruction = true;
		}
	}

}
