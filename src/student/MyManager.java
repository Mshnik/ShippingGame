package student;
import java.util.ArrayList;
import game.*;

/** A sample implementation of the Manager class - almost random (a tiny bit smarter)
 * Manager.
 * @author MPatashnik
 *
 */
public class MyManager extends Manager {

	private Game game;

	private ArrayList<Truck> trucks = new ArrayList<Truck>();


	@Override
	public void run() {
		game = getGame();
		trucks = game.getTrucks();
		for(Truck t : trucks){
			Node n = game.getMap().getTruckHome();
			for(int i = 0; i < 10; i++){
				try {
					Edge e = n.getRandomExit();
					t.addToTravel(e);
					n = e.getOther(n);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	@Override
	public void truckNotification(Truck t, Notification message){
				if(game.isRunning()){
					switch(message){
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
							try{
								t.clearTravel();
								if(t.getGoingTo() == null)
									t.addToTravel(t.getLocation().getRandomExit());
								else
									t.addToTravel(t.getGoingTo().getRandomExit());
							} catch(InterruptedException e){
		
							}
						}
						break;
					case LOCATION_CHANGED: 
						try{
							if(t.getLocation().equals(game.getMap().getTruckHome()) && game.getParcels().isEmpty())
								break;
						} catch(InterruptedException e){ break; }
		
						try {
							if(t.getLoad() != null && t.getLoad().getDestination().equals(t.getLocation()))
								try{
									t.dropoffLoad();
								} catch(Exception e){}
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
		
						try{
							if(t.getGoingTo() == null)
								t.addToTravel(t.getLocation().getRandomExit());
							else
								t.addToTravel(t.getGoingTo().getRandomExit());
						} catch(InterruptedException e){}
						break;
					default:
						break;
					}
				}
	}
}
