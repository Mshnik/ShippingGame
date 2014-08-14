import java.util.ArrayList;

import gameFiles.*;

public class MyManager extends Manager {

	private Game game;

	private ArrayList<Truck> trucks = new ArrayList<Truck>();


	@Override
	public void run() {
		game = getGame();
		trucks = game.getTrucks();
		int i = Truck.MIN_SPEED;
		for(Truck t : trucks){
			t.addToTravel(t.getLocation().getRandomExit());
			try {
				t.setSpeed(i);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			i++;
			i++;
		}
	}

	@Override
	public void truckNotification(Truck t, int message){
		switch(message){
		case(Manager.PARCEL_AT_NODE): 
			if(t.getLoad() == null){
				game.setUpdateMessage("Parcel Picked Up");
				Parcel p = null;
				try {
					p = t.getLocation().getRandomParcel();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try{
				  t.loadUnloadParcel(p, Truck.LOAD);
				} catch(Exception e){}
				t.clearTravel();
				if(t.getGoingTo() == null)
					t.addToTravel(t.getLocation().getRandomExit());
				else
					t.addToTravel(t.getGoingTo().getRandomExit());
			}
			break;
		case(Manager.LOCATION_CHANGED): 
			if(t.getLocation().equals(game.getMap().getTruckHome()) && game.getParcels().isEmpty())
				break;
			
			if(t.getLoad() != null && t.getLoad().getDestination().equals(t.getLocation()))
				try{
				    t.loadUnloadParcel(t.getLoad(), Truck.UNLOAD);
				} catch(Exception e){}

			if(t.getGoingTo() == null)
				t.addToTravel(t.getLocation().getRandomExit());
			else
				t.addToTravel(t.getGoingTo().getRandomExit());
			break;
		default:
			break;
		}
	}
	
//	private static final int PICK_DROP = 0;
	//	private static final int PICK_TO_DESTINATION = 1;

	//	/** Picks up and drops off parcels according to the given behavior
	//	 * @param type - either:
	//	 * 			Truck.PICK_DROP - always picks up a parcel when it encounters one and always drops it off at the next node
	//	 * 			Truck.PICK_TO_DESTINATION - always picks up a parcel when it encounters one, then holds on to it until
	//	 * 				it has arrived at that parcel's destination 
	//	 * @throws IllegalArgumentException - If an unknown type is given.
	//	 * */
	//	private void parcelBehavior(int type) throws IllegalArgumentException{
	//		if(type == PICK_DROP){
	//			if(load == null){
	//				if(!location.getTrueParcels().isEmpty()){
	//					Parcel p = location.getRandomParcel();
	//					loadUnloadParcel(p, LOAD);
	//				}
	//			}
	//			else{
	//				loadUnloadParcel(load, UNLOAD);
	//			}	
	//		}
	//		else if (type == PICK_TO_DESTINATION){
	//			if(load == null){
	//				if(!location.getTrueParcels().isEmpty()){
	//					Parcel p = location.getRandomParcel();
	//					loadUnloadParcel(p, LOAD);
	//				}
	//			}
	//			else{
	//				if(location.equals(load.getDestination()))
	//					loadUnloadParcel(load, UNLOAD);
	//			}	
	//		}
	//		else {throw new IllegalArgumentException("Illegal type value put into parcelBehavior. Unknown behavior for Truck " + name);}
	//	}


}
