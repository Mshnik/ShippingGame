package student;
import game.*;

/** A sample implementation of the Manager class - almost random (a tiny bit smarter)
 * Manager.
 * @author MPatashnik
 *
 */
public class MyManager extends Manager {

	private Game game;


	@Override
	public void run() {
		game = getGame();
		for(Truck t : getTrucks()){
			t.setSpeed(Truck.MAX_SPEED);
		}
	}

	@Override
	public void truckNotification(Truck t, Notification message){
		if(getGame().isRunning()){
			switch(message){
			case WAITING:
				if(t.getLocation().equals(game.getBoard().getTruckHome()) && game.getBoard().getParcels().isEmpty()){
					t.clearTravel();
					break;
				}
				if(t.getGoingTo() == null)
					t.addToTravel(t.getLocation().getRandomExit());
				else
					t.addToTravel(t.getGoingTo().getRandomExit());
				break;
			case PARCEL_AT_NODE: 
				if(t.getLoad() == null){

					game.setUpdateMessage("Parcel Picked Up");
					Parcel p = t.getLocation().getRandomParcel();
					try{
						t.pickupLoad(p);
					} catch(Exception e){}
					t.clearTravel();
					if(t.getGoingTo() == null)
						t.addToTravel(t.getLocation().getRandomExit());
					else
						t.addToTravel(t.getGoingTo().getRandomExit());
				}
				break;
			case LOCATION_CHANGED: 
				if(t.getLocation().equals(game.getBoard().getTruckHome()) && game.getBoard().getParcels().isEmpty()){
					t.clearTravel();
					break;
				}

				if(t.getLoad() != null && t.getLoad().destination.equals(t.getLocation()))
					try{
						t.dropoffLoad();
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
	}
}
