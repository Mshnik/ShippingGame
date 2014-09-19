package game;
import gui.Circle;

import java.awt.Color;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

/** The Truck class is a runnable object that represents a single Truck in the game.
 * Trucks are instantiated by the game and put into their own thread to run.
 * Trucks maintain a queue of travel directions, each of which is an edge to travel.
 * While the queue is empty, trucks idle at their current location.
 * Once the queue is populated, the truck will continue to follow those directions
 * until the queue is empty again. The speed at which a Truck travels can be set
 * using the setSpeed(int i) method, which determines how many units of road
 * the truck travels each frame. Trucks can be told to pick up or drop off parcels
 * when they are not traveling. <br> <br>
 * 
 * Interaction with Trucks is done through the Truck's internal calling of the truckNotification
 * method in the Manager class. Whenever the Truck makes an action, it lets the manager know of 
 * this change, allowing for input at that time.
 * @author MPatashnik
 */
public class Truck implements MapElement, Runnable, Colorable, UserData{

	/** The Two States that a Truck can be in at any time - either Waiting (staying on its location
	 * and awaiting further travel instructions) or Traveling (currently moving from node to node
	 * according to its travel instructions). Every Truck's status field is always one of these
	 * values.
	 * @author MPatashnik
	 */
	public static enum Status {TRAVELING, WAITING};

	private static final int WAIT_TIME = 5;
	protected static final int NUMB_DEFAULT_TRAVEL_DIRECTIONS = 200;

	private String name;			//The name of this truck
	private Circle circle;			//The circle that represents this Graphically
	private Color color = Circle.DEFAULT_TRUCK_COLOR;

	private Queue<Edge> travel; 	//This truck's queue of travel directions, FIFO.

	private Parcel load;			//The Parcel (if any) this truck is currently holding
	private Node location;			//The Node this truck is currently at
	private Node travelingTo;		//The Node at the end of the Edge that this truck is currently on
	private Node goingTo;			//The Node this truck will be at once it finishes its current travel queue.
	private Edge travelingAlong;	//The Edge this Truck is currently traveling along

	private Status status;			//This truck's status, either waiting or traveling

	private long lastTravelTime;    //System time (ms) when this truck last finished travel

	public static final int MAX_SPEED = 10; //Max value for truck's speed
	public static final int EFFICIENT_SPEED = 4; //Speed at which the travel is most efficient
	public static final int MIN_SPEED = 1;  //Min value for truck's speed

	private int speed; //The number of units this moves per frame when traveling. Must be between min and max
	private Semaphore speedLock; 	//Lock for getting/changing speed
	private Semaphore locLock;   	// A lock associated with the changing of locaiton, travelingTo, goingTo, etc.
	                             	//Lock should be released before any notifications are fired.
	private Semaphore statusLock; 	//A Lock for the status of this truck.
	private Semaphore parcelLock;	//A Lock for the parcel this truck is carrying
	
	private Object userData;

	private final Game game;		//The game this truck belongs to

	/** Constructor for the Truck Class. Uses a default color
	 * @param g - the Game this Truck belongs to
	 * @param name - The name of this truck
	 * @param startLocation - The starting location of this truck
	 * 
	 * Speed defaults to EFFICIENT_SPEED
	 */
	protected Truck(Game g, String name, Node startLocation){
		this(g, name, startLocation, Score.getRandomColor());
	}

	/** Constructor for the Truck Class.
	 * @param g - the Game this truck belongs to
	 * @param name - The name of this truck
	 * @param startLocation - The starting location of this truck
	 * @param c - The color of this truck.
	 * 
	 * Speed defaults to EFFICIENT_SPEED
	 */
	protected Truck(Game g, String name, Node startLocation, Color c){
		this.name = name;
		this.game = g;

		speed = Truck.EFFICIENT_SPEED;
		speedLock = new Semaphore(1);
		locLock = new Semaphore(1);
		statusLock = new Semaphore(1);
		parcelLock = new Semaphore(1);

		location = startLocation;
		travelingTo = null;
		goingTo = null;
		status = Status.WAITING;
		travel = new LinkedList<Edge>();
		color = c;
		circle = new Circle(this, 0, 0, (int)((double)Circle.DEFAULT_DIAMETER * 0.8), c, false);

	}

	@Override
	/** The Truck's main running routine. While the travel directions are empty,
	 * Waits for more instructions in WAIT_TIME intervals. While the travel directions
	 * are not empty, pops off the next travel direction 
	 */
	public void run(){
		lastTravelTime = System.currentTimeMillis();
		while(game.isRunning()){

			boolean unset = true;
			while(unset){
				try {
					setGoingTo(null);
					unset = false;
				} catch (InterruptedException e1) {
					unset = true; //Try to setGoingTo again.
					try {
						Thread.sleep(5);
					} catch (InterruptedException e) {}
				}
			}

			while(travel.isEmpty() && game.isRunning()){
				try{
					Thread.sleep(WAIT_TIME);
					game.getManager().truckNotification(this, Manager.Notification.WAITING);
				}
				catch (InterruptedException e){
					e.printStackTrace();
				}
				unset = true;
				while(unset){
					try {
						setGoingTo(null);
						unset = false;
					} catch (InterruptedException e1) {
						unset = true; //Try to setstatus again.
						try {
							Thread.sleep(5);
						} catch (InterruptedException e) {}
					}
				}
				fixLastTravelTime();
			}
			while(!travel.isEmpty() && game.isRunning()){
				Edge r = getTravel();
				try {
					travel(r);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				fixLastTravelTime();
			}
		}
		//Deduct final waiting points
		fixLastTravelTime();
	}

	/** Updates the waitTime to now, and deducts correct number of points for doing this */
	private void fixLastTravelTime(){
		long now = System.currentTimeMillis();
		long diff = now - lastTravelTime;
		game.getScore().changeScore(Score.WAIT_COST * (int)(diff / WAIT_TIME));
		lastTravelTime = now;
	}

	/** Returns the name of this Truck */
	public String getTruckName(){
		return name;
	}

	/** Sets the name of this Truck*/
	public void setTruckName(String newName){
		name = newName;
	}

	/** Returns the Truck's current location. If this.status.equals(Status.TRAVELING), returns null 
	 * @throws InterruptedException */
	public Node getLocation() throws InterruptedException{
		if(status.equals(Status.TRAVELING))
			return null;

		locLock.acquire();
		Node n = location;
		locLock.release();
		
		return n;
	}

	/** Sets this Truck's location to Node l and fires a Manager Notification.
	 * Does not fire a Manager Notification if l.equals(location) 
	 * @throws InterruptedException */
	private void setLocation(Node l) throws InterruptedException{
		if(!l.equals(location)){
			locLock.acquire();
			location = l;
			locLock.release();
			game.getManager().truckNotification(this, Manager.Notification.LOCATION_CHANGED);
		}
	}

	/** Returns the Truck's current destination. If this.status.equals(Status.WAITING), returns null 
	 * @throws InterruptedException */
	public Node getTravelingTo() throws InterruptedException{
		if(status.equals(Status.WAITING))
			return null;

		locLock.acquire();
		Node n = travelingTo;
		locLock.release();
		
		return n;
	}

	/** Sets this Truck's travelingTo to Node t and fires a Manager Notification.
	 * Does not fire a Manager Notification if travelingTo.equals(t) 
	 * @throws InterruptedException */
	private void setTravelingTo(Node t) throws InterruptedException{
		if(travelingTo == null || !travelingTo.equals(t)){
			locLock.acquire();
			travelingTo = t;
			locLock.release();
			game.getManager().truckNotification(this, Manager.Notification.TRAVELING_TO_CHANGED);
		}
	}

	/** Returns the edge this Truck is traveling along. Returns null if status.equals(Status.WAITING) 
	 * @throws InterruptedException */
	public Edge getTravelingAlong() throws InterruptedException{
		locLock.acquire();
		Edge e = travelingAlong;
		locLock.release();
		return e;
	}

	/** Returns the node this truck is coming from, the rear exit of the edge it is currently on.
	 * Returns null if status.equals(Status.WAITING)
	 * @throws InterruptedException 
	 */
	public Node getComingFrom() throws InterruptedException{
		if(status.equals(Status.WAITING))
			return null;

		locLock.acquire();
		Node n = travelingAlong.getOther(travelingTo);
		locLock.release();
		
		return n;
	}

	/** Returns the Truck's eventual destination; the Node this Truck will be at when the current travel
	 * queue is empty. If this.status.equals(Status.WAITING), returns null
	 * @throws InterruptedException 
	 */
	public Node getGoingTo() throws InterruptedException{
		if(status.equals(Status.WAITING))
			return null;

		locLock.acquire();
		Node n = goingTo;
		locLock.release();
		
		return n;
	}

	/** Sets this Truck's goingTo to Node g and fires a Manager Notification.
	 * Does not fire a Manager Notification if goingTo.equals(g)
	 * @throws InterruptedException */
	private void setGoingTo(Node g) throws InterruptedException{
		if(goingTo == null || !goingTo.equals(g)){
			locLock.acquire();
			goingTo = g;
			locLock.release();
			game.getManager().truckNotification(this, Manager.Notification.GOING_TO_CHANGED);
		}
	}

	/** Returns the current status of this Truck, either TRAVELING or WAITING 
	 * @throws InterruptedException */
	public Truck.Status getStatus() throws InterruptedException{
		statusLock.acquire();
		Truck.Status s = status;
		statusLock.release();
		return s;
	}

	/** Sets the current status of this Truck to status s and fires a Manager Notification.
	 * Does not fire a Manager Notification if status.equals(s)
	 * @throws InterruptedException */
	protected void setStatus(Truck.Status s) throws InterruptedException{
		if(!status.equals(s)){
			statusLock.acquire();
			status = s;
			statusLock.release();
			game.getManager().truckNotification(this, Manager.Notification.STATUS_CHANGED);
		}
	}

	/** Returns the parcel this Truck is carrying. Returns null
	 * if no truck is being carried
	 */
	public Parcel getLoad(){
		Parcel p = load;
		return p;
	}

	/** Returns the Color of this Truck */
	public Color getColor(){
		return color;
	}

	/** Sets the Color of this Truck to Color c
	 * @param c - the new Color of this truck
	 * @throws IllegalArgumentException - if c is not in Score.COLORS
	 */
	protected void setColor(Color c){
		if(!Score.colorContains(c))
			throw new IllegalArgumentException("Illegal Color (" + c.toString() +") passed in");

		color = c;
		circle.setColor(c);
	}

	/** Returns the speed this truck will/is traveling 
	 * @throws InterruptedException */
	public int getSpeed() throws InterruptedException{
		speedLock.acquire();
		int i = speed;
		speedLock.release();
		return i;
	}

	/** Sets this trucks speed */
	public void setSpeed(int newSpeed) throws InterruptedException{
		speedLock.acquire();
		speed = newSpeed;
		speedLock.release();
	}

	/** Returns the userData stored in this Node. May be null if the user has not yet given this Node userData */
	public Object getUserData(){
		return userData;
	}

	/** Sets the value of userData to Object uData. To erase the current userData just pass in null */
	public void setUserData(Object uData){
		userData = uData;
	}

	/** The state of loading a parcel - use for loadUnloadParcel(..) */
	public static final boolean LOAD = true;
	/** The state of unloading a parcel - use for loadUnloadParcel(..) */
	public static final boolean UNLOAD = false;

	/** Handles loading and unloading parcels.
	 * Only permit loading/unloading if this is waiting - if it is currently traveling,
	 * don't do any loading/unloading.
	 * 
	 * @param p - The parcel to pick up or drop off
	 * @param state - either Truck.LOAD or Truck.UNLOAD
	 * 
	 * @throws RuntimeException - @see pickupLoad(p)
	 * @throws InterruptedException 
	 */
	public void loadUnloadParcel(Parcel p, boolean state) throws RuntimeException, InterruptedException{
		if(getStatus() == Status.TRAVELING)
			return;
		if(state == LOAD)
			pickupLoad(p);
		else if(state == UNLOAD)
			dropoffLoad();
	}

	/** Picks up parcel p at the current location. If there is, adds it to Truck and waits PARCEL_PICKUP_TIME seconds
	 * @throws RuntimeException  - if load is not null (can't pick up) or if this Truck is currently traveling.
	 * @throws InterruptedException 
	 *  */
	private void pickupLoad(Parcel p) throws RuntimeException, InterruptedException{
		if(load != null)
			throw new RuntimeException("Can't Pickup Parcel with non-null load. Already holding a Parcel.");

		if(location.getTrueParcels().contains(p)){
			parcelLock.acquire();
			location.getTrueParcels().remove(p);
			load = p;
			load.loadUnload(this, Truck.LOAD);
			parcelLock.release();

			game.getScore().changeScore(Score.PICKUP_COST);
			game.getManager().truckNotification(this, Manager.Notification.PICKED_UP_PARCEL);
		}
	}

	/** Drops off load at the current location. Throws a RuntimeException if load is null 
	 * @throws InterruptedException */
	private void dropoffLoad() throws RuntimeException, InterruptedException{
		if(load == null)
			throw new RuntimeException("Can't Drop Off a null parcel. No Parcel to drop off.");

		parcelLock.acquire();
		location.getTrueParcels().add(load);
		load.loadUnload(this, Truck.UNLOAD);
		load = null;
		parcelLock.release();
		game.getScore().changeScore(Score.DROPOFF_COST);
		game.getManager().truckNotification(this, Manager.Notification.DROPPED_OFF_PARCEL);

	}

	/** Returns the circle that represents this truck when it is drawn */
	public Circle getCircle(){
		return circle;
	}

	private static final int GET_TRAVEL = 0;
	private static final int ADD_TO_TRAVEL = 1;
	private static final int CLEAR_TRAVEL = 2;

	/** Adds the road r to this Truck's travel plans, in a fashion that prevents thread collision 
	 * @throws InterruptedException */
	public void addToTravel(Edge r) throws InterruptedException{
		if(goingTo == null)
			setGoingTo(r.getOther(location));
		else
			setGoingTo(r.getOther(goingTo));

		changeTravelQueue(r, ADD_TO_TRAVEL);
	}

	/** Pops the front road r of this Truck's travel plans, in a fashion that prevents thread collision.
	 * Used for traveling within the Truck Class */
	private Edge getTravel(){
		return changeTravelQueue(null, GET_TRAVEL);
	}

	/** Clears the Truck's travel plans, in a fashion that prevents thread collision.
	 * Resets goingTo (The Node the Truck will eventually end up at) to the value of the travelingTo field
	 * (The Node the Truck is currently traveling towards) 
	 * @throws InterruptedException */
	public void clearTravel() throws InterruptedException{
		setGoingTo(travelingTo);
		changeTravelQueue(null, CLEAR_TRAVEL);
	}

	/** Modifies the travel queue in a way to prevent thread collision.
	 * @param r - The Edge that should be added to the queue if state == ADD_TO_TRAVEL. Can be null if used for getting.
	 * @param state - Either ADD_TO_TRAVEL (adding a new Node/Edge), GET_TRAVEL (polling off the top road),
	 * 					or CLEAR_TRAVEL (clearing the Travel queue)
	 * @return - The top Edge in the queue if GET_TRAVEL is used, null otherwise
	 * @throws IllegalArgumentException - if a state other than GET_TRAVEL, ADD_TO_TRAVEL, or CLEAR_TRAVEL is used
	 */
	private synchronized Edge changeTravelQueue(Edge r, int state) throws IllegalArgumentException{
		if(state == GET_TRAVEL){
			return travel.poll();
		}
		else if(state == ADD_TO_TRAVEL){
			travel.add(r);
			return null;
		}
		else if(state == CLEAR_TRAVEL){
			travel.clear();
			return null;
		}
		else{
			throw new IllegalArgumentException("An invalid state given. Cannot interpret use");
		}
	}

	/** Tells the Truck to travel along the given edge.
	 * The Truck will only begin to travel if its status is WAITING.
	 * If status is TRAVELING, the Truck will ignore this call. 
	 * @param r - the Edge for this Truck to travel along
	 * @throws InterruptedException for when the Truck thread sleeps
	 * @throws IllegalArgumentException if the Truck's current location
	 * 		is not one of the exits for Edge r
	 */
	private final void travel(Edge r) throws InterruptedException, IllegalArgumentException{
		if(getStatus().equals(Status.WAITING)){
			if(! r.isExit(location))
				throw new IllegalArgumentException("Truck is not adjacent and cannot travel Edge " + r);

			//Checks OK, start Traveling
			setStatus(Status.TRAVELING);

			setTravelingTo(r.getOther(location));
			travelingAlong = r;

			location.setTruckHere(this, false);
			travelingAlong.setTruckHere(this, true);

			location.getCircle().updateColor();
			travelingAlong.getLine().updateToColorPolicy();

			Circle here = location.getCircle();
			Circle there = r.getOther(location).getCircle();

			int progress = 0;
			long startTravelTime = System.currentTimeMillis();
			while(progress < r.getLength()){
				Thread.sleep(Score.FRAME);

				//Get the speed lock, begin speed and cost computations
				speedLock.acquire();
				//If we can go the full speed's units, do that
				int remaining = r.getLength() - progress;
				if(remaining >= speed){
					progress += speed;
					game.getScore().changeScore(Score.cost(speed));
				}
				//Otherwise, go the remaining fraction, only deduct a correct percent of those points.
				else{
					progress += remaining;
					game.getScore().changeScore(Score.cost(speed) * remaining / speed);
				}
				speedLock.release();
				double percent = (double)progress / (double)r.getLength();
				
				//Update Truck's location on the GUI
				updateGUILocation( (int) (percent * there.getX1() + (1-percent) * here.getX1()), 
						(int) (percent * there.getY1() + (1-percent) * here.getY1()));
			}
			long finishTravelTime = System.currentTimeMillis();
			lastTravelTime += (finishTravelTime - startTravelTime); //Discount the time spent traveling

			//Change the status without firing an update.
			//Status update on waiting should only come when the truck
			//Out of travel directions entirely, which occurs in the run() method.
			statusLock.acquire();
			status = Status.WAITING;
			statusLock.release();

			setLocation(travelingTo);

			updateGUILocation(location.getCircle().getX1(), location.getCircle().getY1());
			travelingAlong.setTruckHere(this, false);
			location.setTruckHere(this, true);

			location.getCircle().updateColor();
			travelingAlong.getLine().updateToColorPolicy();

			if(location.getParcels().size() > 0)
				game.getManager().truckNotification(this, Manager.Notification.PARCEL_AT_NODE);

			if(game.getParcels().isEmpty() && game.isAllTrucksHome())
				game.finish();
		}
	}

	/** Updates the circle graphic that represents this truck on the GUI.
	 * Does nothing if threads is null.
	 * Also updates the location of the load if this truck is carrying one 
	 * @param x - the new X location of this Truck in the GUI
	 * @param y - the new Y location of this Truck in the GUI
	 * */
	public void updateGUILocation(int x, int y){
		if(game.getGUI() != null){
			circle.setX1(x);
			circle.setY1(y);
			circle.repaint();
			if(load != null){
				load.updateGUILocation(x, y);
			}
		}
	}

	@Override
	/**Returns a string representation of this Truck */
	public String toString(){
		return name;
	}

	@Override
	/** Returns the name of this truck to display on the GUI */
	public String getMappedName() {
		return getTruckName();
	}

	@Override
	/** Returns the location of this' name relative to its position on the GUI */
	public int getRelativeX() {
		return -Circle.DEFAULT_DIAMETER/2;
	}

	@Override
	/** Returns the location of this' name relative to its position on the GUI */
	public int getRelativeY() {
		return Circle.DEFAULT_DIAMETER + 10;			
	}

	@Override
	/** Always returns for this, always return false for other trucks, 
	 * because a truck is always "at" itself */
	public boolean isTruckHere(Truck t) {
		return (this == t);
	}

	@Override
	/** Always returns 1 - there is always one truck at itself */
	public int trucksHere(){
		return 1;
	}

	/** Called by the Game when the game is done. Causes this thread to die 
	 * @throws InterruptedException  - if this Truck thread is currently interrupted*/
	protected void gameOver() throws InterruptedException{
		clearTravel();
	}
	
	@Override
	/** Returns a JSON String of this truck.
	 * Just the basic truck info pertaining to map creation - location and load not included.
	 */	
	public String toJSONString() {
		return "{\n" + Main.addQuotes(MapElement.NAME_TOKEN) + ":" + Main.addQuotes(name) + "," +
				"\n" + Main.addQuotes(MapElement.COLOR_TOKEN) + ":" + color.getRGB() + 
				"\n}";
	}
}
