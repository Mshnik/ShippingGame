package student;

import game.*;

import java.util.*;

/** A more complex solution.
 * Works on colorizing and having trucks pick up the parcels closest to them.
 * @author MPatashnik
 *
 */
public class MyManager extends AbstractSolution {

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

        //Start with all parcels unassigned, assign to each truck
        unassignedParcels.addAll(getParcels());
        for (Truck t : getTrucks()) {
            assignParcelTo(t);
        }

        preprocessingDone = true;
    }

    /** Assign a currently unassigned parcel to truck t.
     * If possible, assign a same-color parcel; either way, assign the closest parcel.
     * If unassignedParcels is empty, do nothing (return).
     */
    private void assignParcelTo(final Truck t) {
        //If possible, build same color list of parcels
        synchronized(unassignedParcels) {
            if (unassignedParcels.isEmpty()) return;

            HashSet<Parcel> sameColorParcels = new HashSet<Parcel>();
            for (Parcel p : unassignedParcels) {
                if (p.getColor().equals(t.getColor())) {
                    sameColorParcels.add(p);
                }
            }

            //If that wasn't possible, choose from all unassigned parcels
            if (sameColorParcels.isEmpty()) sameColorParcels.addAll(unassignedParcels);

            //Choose the closest parcel to t to assign it to
            Parcel parcel = Collections.min(sameColorParcels, new Comparator<Parcel>() {
                @Override
                public int compare(Parcel o1, Parcel o2) {
                    LinkedList<Node> p1 = dijkstra(t.getLocation(), o1.getLocation());
                    int l1 = pathLength(p1);
                    LinkedList<Node> p2 = dijkstra(t.getLocation(), o2.getLocation());
                    int l2 = pathLength(p2);
                    return l1 - l2;
                }
            });

            parcelsAssigned.put(t, parcel);
            unassignedParcels.remove(parcel);
        }
    }

    /** Allows Trucks to notify the manager that something has occurred. 
     * Method should provide the calling truck with additional information
     * pertaining to the message sent.
     * 
     * @see Manager.Notification The notification enum for types of messages
     */
    @Override
    public void truckNotification(Truck t, Notification message) {
        if (!preprocessingDone) return;

        //Wait frame notification - logic branches a bit.
        //if t is holding a load, travel to that location.
        if (message.equals(Notification.WAITING)) {
            if (t.getLoad() != null && t.getLoad().destination.equals(t.getLocation())) {
                t.dropoffLoad();

                //Check if there are more parcels to handle. If not, remove from assignment and go home.
                //If so, pick one at random and assign this truck to that.
                if (unassignedParcels.isEmpty()) {
                    parcelsAssigned.remove(t);
                    t.setTravelPath(dijkstra(t.getLocation(), getBoard().getTruckDepot()));
                } else {
                    assignParcelTo(t);
                }
            }
            else if (t.getLoad() != null) {
                t.setTravelPath(dijkstra(t.getLocation(), t.getLoad().destination));
            } else if (parcelsAssigned.containsKey(t) && t.getLocation().isParcelHere(parcelsAssigned.get(t))) {
                t.pickupLoad(parcelsAssigned.get(t));
            } else if (parcelsAssigned.containsKey(t) && getParcels().contains(parcelsAssigned.get(t))) {
                t.setTravelPath(dijkstra(t.getLocation(), parcelsAssigned.get(t).getLocation()));
            } else if (! parcelsAssigned.containsKey(t) || getParcels().isEmpty()) {
                t.setTravelPath(dijkstra(t.getLocation(), getBoard().getTruckDepot()));
            } else if (parcelsAssigned.containsKey(t) && ! getParcels().contains(parcelsAssigned.get(t))) {
                assignParcelTo(t);
            }
        }
    }
}
