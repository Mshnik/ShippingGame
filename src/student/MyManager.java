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
	}

	@Override
	public void truckNotification(Truck t, Notification message){
		if(getGame().isRunning()){
			switch(message){
			case WAITING:
				if(t.getGoingTo() == null)
					t.addToTravel(t.getLocation().getRandomExit());
				else
					t.addToTravel(t.getGoingTo().getRandomExit());
				break;
			case PARCEL_AT_NODE: 
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
				if(t.getLocation().equals(game.getMap().getTruckHome()) && game.getParcels().isEmpty())
					break;

				if(t.getLoad() != null && t.getLoad().getDestination().equals(t.getLocation()))
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
