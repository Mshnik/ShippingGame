package game;
import gui.Circle;

import java.awt.Color;
import java.util.*;
import java.util.concurrent.Semaphore;

/** CLass Truck is a runnable object that represents a single Truck in the game.
 * Trucks are instantiated by the game and put into their own thread to run.
 * Trucks maintain a queue of travel directions, each of which is an edge to travel.
 * While the queue is empty, trucks idle at their current location.
 * Once the queue is populated, the truck will continue to follow those directions
 * until the queue is empty again. The speed at which a Truck travels can be set
 * using method setSpeed(int i), which determines how many units of road
 * the truck travels each frame. Trucks can be told to pick up or drop off parcels
 * when they are not traveling. <br><br>
 * 
 * Interaction with Trucks is done through the Truck's internal calling of method
 * truckNotification in class Manager. Whenever the Truck makes an action or reaches
 * a point of decision, it lets the manager know of this change, allowing for input
 * at that time.

 * @author MPatashnik
 */
public final class Truck implements BoardElement, Runnable {

    /** The Two states that a Truck can be in at any time - either Waiting (staying
     * on its location and awaiting further travel instructions) or Traveling
     * (currently moving from node to node according to its travel instructions).
     * Every Truck's status field is always one of these values.
     * @author MPatashnik
     */
    public static enum Status {
    	/** Status while a truck is traveling.
    	 * While traveling, Parcel operations will not function correctly.
    	 * The get methods for traveling (travelingTo, travelingAlong, comingFrom, goingTo)
    	 * are available. Additional travel instructions can still be provided.
    	 */
    	TRAVELING, 
    	/** Status while a truck is waiting.
    	 * While waiting, Parcel operations will function. The getLocation() method
    	 * is the only valid location getting method. 
    	 * Travel instructions can be provided to make this truck start traveling
    	 */
    	WAITING};

    /** Milliseconds between wait updates - a truck calling
     * Manager.truckNotification(this, Notification.WAITING) .*/
    public static final int WAIT_TIME = 5;

    /** Maximum length/frame speed that a truck can travel. */
    public static final int MAX_SPEED = 10;

    /** Most efficient length/frame speed that a truck can travel, in terms of
     * total cost for traveling a given length. */
    public static final int EFFICIENT_SPEED = 4;

    /** Minimum length/frame speed that a truck can travel. */
    public static final int MIN_SPEED = 1;

    private String name;			//The name of this truck
    private Circle circle;			//The circle that represents this graphically
    private Color color;			//The color of this truck

    private List<Edge> travel; 	//This truck's queue of travel directions, FIFO.

    private Parcel load;			//The Parcel (if any) this truck is currently holding
    private Node location;			//The Node this truck is currently at
    private Node travelingTo;		//The Node at the end of the Edge that this
    // truck is currently on
    private Node goingTo;			//The Node this truck will be at once it finishes 
    // its current travel queue.
    private Edge travelingAlong;	//The Edge this Truck is currently traveling along

    private boolean alive;			//True iff this truck is executing its run loop
    private Status status;			//This truck's status, either waiting or traveling
    private boolean waitingForManager;	//True iff this is waiting for manager input

    private long lastTravelTime;    //System time (ms) when this truck last finished travel

    private int speed; //The number of units this moves per frame when traveling.
    //Must be between min and max
    private Semaphore speedLock; 	//Lock for getting/changing speed
    private Semaphore locLock;   	// A lock associated with the changing of location,
    // travelingTo, goingTo, etc.
    //Lock should be released before any notifications are fired.
    private Semaphore statusLock; 	//A Lock for the status of this truck.
    private Semaphore parcelLock;	//A Lock for the parcel this truck is carrying

    private Object userData;

    /** The game to which this truck belongs. */
    public final Game game;

    private Thread thread;	//The thread this truck is running in. Should have TRUCK in its name

    /** Constructor: An instance with name name running in game g starting on
     *  node start with a random color and speed EFFICIENT_SPEED.
     * @param g - the Game this Truck belongs to.
     * @param name - The name of this truck.
     */
    protected Truck(Game g, String name, Node start) {
        this(g, name, Score.getRandomColor(), start);
    }

    /** Constructor:  An instance with name name running in game g starting on
     *  node start with color c and speed EFFICIENT_SPEED.
     * @param g - the Game this truck belongs to
     * @param name - The name of this truck
     * @param c - The color of this truck.
     */
    protected Truck(Game g, String name, Color c, Node start) {
        this.name = name;
        this.game = g;

        speed = Truck.EFFICIENT_SPEED;
        speedLock = new Semaphore(1);
        locLock = new Semaphore(1);
        statusLock = new Semaphore(1);
        parcelLock = new Semaphore(1);

        location = start;
        travelingTo = null;
        goingTo = null;
        status = Status.WAITING;
        travel = Collections.synchronizedList(new LinkedList<Edge>());
        color = c;
        circle = new Circle(this, 0, 0, (int)((double)Circle.DEFAULT_DIAMETER * 0.8), c, false);
        alive = false;
    }

    /** The Truck's main running routine. While the travel directions are empty,
     * Waits for more instructions in WAIT_TIME intervals. While the travel
     * directions are not empty, pops off the next travel direction.<br><br>
     * 
     * Called and on loop until the game ends. Terminates itself when there are
     * no more parcels and this truck is at the Truck Depot. Once that occurs,
     * doesn't take any more instructions.
     * Students: don't call this procedure!
     */
    @Override
    public void run() {
        try {
            lastTravelTime = System.currentTimeMillis();
            alive = true;
            while (alive) {
                locLock.acquire();
                if (getBoard().getParcels().isEmpty() && location.equals(getBoard().getTruckDepot())) {
                    locLock.release();
                    getBoard().addTruckToFinished(this);
                    //Deduct final waiting points
                    fixLastTravelTime();
                    alive = false;
                    return;
                }
                locLock.release();

                Thread.sleep(WAIT_TIME);
                preManagerNotification();
                game.getManager().truckNotification(this, Manager.Notification.WAITING);
                postManagerNotification();

                setGoingTo(null);
                fixLastTravelTime();

                while (!travel.isEmpty() && game.isRunning()) {
                    try {
                        Edge r = getTravel();
                        travel(r);
                    } catch (IllegalArgumentException e) {
                        clearTravel(); //If traveling isn't valid, clear the queue
                    }
                    fixLastTravelTime();
                }
            }
        }
        //If interrupted exception occurs anywhere within run, just kill the truck
        catch (InterruptedException e) {
            alive = false;
            return;
        }
    }

    /** Set this as waiting for manager input. Must be called before any manager
     * notification. */
    private void preManagerNotification() {
        waitingForManager= true;
    }

    /** Set this as finishing receiving manager input. Must be called after any
     * manager notification. */
    private void postManagerNotification() {
        waitingForManager = false;
    }

    /** Update the waitTime to now and deduct correct number of points for doing this. */
    private void fixLastTravelTime() {
        long now = System.currentTimeMillis();
        long diff = now - lastTravelTime;
        getManager().getScoreObject().changeScore(getBoard().getWaitCost() * (int)(diff / WAIT_TIME));
        lastTravelTime = now;
    }

    /** Return the manager that is managing this truck. */
    public Manager getManager() {
        return game.getManager();
    }

    /** Return the board to which this Truck belongs. */
    @Override
    public Board getBoard() {
        return game.getBoard();
    }

    /** Set the thread this truck is running in to t. */
    void setThread(Thread t) {
        t.setName("TRUCK-THREAD:"+getTruckName());
        thread = t;
    }

    /** Return the name of this Truck. */
    public String getTruckName() {
        return name;
    }

    /** Set the name of this Truck to newName and, if it is not yet assigned
     * to this thread, set it to this thread. */
    public void setTruckName(String newName) {
        name = newName;
        if (thread != null) setThread(thread);
    }

    /** Return true iff this Truck is alive (executing its run loop). */
    public boolean isAlive() {
        return alive;
    }

    /** Return the Truck's current location.  (Return null if
     *  this.status.equals(Status.TRAVELING) or the calling thread is interrupted.) 
     */
    public Node getLocation() {
        if (status.equals(Status.TRAVELING))
            return null;

        Node n;
        try {
            locLock.acquire();
            n = location;
            locLock.release();
        } catch (InterruptedException e) {
            return null;
        }
        return n;
    }

    /** Set this Truck's location to Node l and fires a Manager Notification.
     * Does not fire a Manager Notification if l.equals(location). 
     * @throws InterruptedException */
    private void setLocation(Node l) throws InterruptedException{
        if (!l.equals(location)) {
            locLock.acquire();
            location = l;
            locLock.release();
            preManagerNotification();
            game.getManager().truckNotification(this, Manager.Notification.LOCATION_CHANGED);
            postManagerNotification();
        }
    }

    /** Return the Truck's current destination. (Return null if
     * this.status.equals(Status.WAITING) or if the calling thread is interrupted). */
    public Node getTravelingTo() {
        if (status.equals(Status.WAITING))
            return null;

        try {
            locLock.acquire();
        } catch (InterruptedException e) {
            return null;
        }
        Node n = travelingTo;
        locLock.release();

        return n;
    }

    /** Set this Truck's travelingTo to t and fire a Manager Notification.
     * Does not fire a Manager Notification if travelingTo.equals(t) 
     * @throws InterruptedException */
    private void setTravelingTo(Node t) throws InterruptedException {
        if (travelingTo == null || !travelingTo.equals(t)) {
            locLock.acquire();
            travelingTo = t;
            locLock.release();
        }
    }

    /** Return the edge this Truck is traveling along. (Return null if
     * status.equals(Status.WAITING) or if the calling thread is interrupted.) */
    public Edge getTravelingAlong() {
        try {
            locLock.acquire();
        } catch (InterruptedException e1) {
            return null;
        }
        Edge e = travelingAlong;
        locLock.release();
        return e;
    }

    /** Return the node this truck is coming from, the rear exit of the edge it
     * is currently on. (Return null if
     * status.equals(Status.WAITING) or if the calling thread is interrupted.)
     */
    public Node getComingFrom() {
        if (status.equals(Status.WAITING))
            return null;

        try {
            locLock.acquire();
        } catch (InterruptedException e) {
            return null;
        }
        Node n = travelingAlong.getOther(travelingTo);
        locLock.release();

        return n;
    }

    /** Return the Truck's eventual destination; the Node this Truck will be at
     * when the current travel queue is empty. (Return null if
     * this.status.equals(Status.WAITING) or if the calling thread is interrupted.)
     */
    public Node getGoingTo() {
        if (status.equals(Status.WAITING))
            return null;

        try {
            locLock.acquire();
        } catch (InterruptedException e) {
            return null;
        }
        Node n = goingTo;
        locLock.release();

        return n;
    }

    /** Set this Truck's goingTo to g and fire a Manager Notification.
     * Does not fire a Manager Notification if goingTo.equals(g).
     * Does nothing if the calling thread is interrupted. */
    private void setGoingTo(Node g) {
        if ((goingTo == null && g != null) || (goingTo != null && !goingTo.equals(g))) {
            try {
                locLock.acquire();
            } catch (InterruptedException e) {
                return;
            }
            goingTo = g;
            locLock.release();
        }
    }

    /** Return the current status of this Truck, either TRAVELING or WAITING.
     * Return null if the thread is interrupted. */
    public Truck.Status getStatus() {
        try {
            statusLock.acquire();
        } catch (InterruptedException e) {
            return null;
        }
        Truck.Status s = status;
        statusLock.release();
        return s;
    }

    /** Set the current status of this Truck to s and fire a Manager Notification.
     * Does not fire a Manager Notification if status.equals(s).
     * @throws InterruptedException */
    private void setStatus(Truck.Status s) throws InterruptedException {
        if (!status.equals(s)){
            statusLock.acquire();
            status = s;
            statusLock.release();
        }
    }

    /** Return true iff this truck is waiting for manager input.
     * Useful for debugging purposes. If manager is recursing forever, trucks
     * wait forever. */
    public boolean isWaitingForManager() {
        return waitingForManager;
    }

    /** Return the parcel this Truck is carrying. (null if none).
     * Return null if the calling thread is interrupted */
    public Parcel getLoad() {
    	try{
    		parcelLock.acquire();
    	}catch(InterruptedException e){
    		return null;
    	}
    	Parcel p = load;
    	parcelLock.release();
        return p;
    }

    /** Return the Color of this Truck. Because the color of a truck has game
     * significance, this will not be changed while the game is running. */
    @Override
    public Color getColor() {
        return color;
    }
    
    /** Return true - the color of Trucks is significant */
    @Override
    public boolean isColorSignificant(){
    	return true;
    }

    /** Set the Color of this Truck to c.
     * @param c - the new Color of this truck
     * @throws IllegalArgumentException - if c is not in Score.COLORS
     */
    protected void setColor(Color c) {
        if(!Score.colorContains(c))
            throw new IllegalArgumentException("Illegal Color (" + c.toString() +") passed in");

        color = c;
        circle.setColor(c);
    }

    /** Return the speed this truck will/is traveling.
     * Return -1 if the calling thread is interrupted */
    public int getSpeed() {
        try {
            speedLock.acquire();
        } catch (InterruptedException e) {
            return -1;
        }
        int i = speed;
        speedLock.release();
        return i;
    }

    /** Set this trucks speed.
     * Does not change the speed if the calling thread is interrupted */
    public void setSpeed(int newSpeed) {
        try {
            speedLock.acquire();
        } catch (InterruptedException e) {
            return;
        }
        speed = newSpeed;
        speedLock.release();
    }

    /** Return the userData stored in this Node. May be null if the user has not
     * yet given this Node userData. */
    @Override
    public Object getUserData() {
        return userData;
    }

    /** Set the value of userData to uData.
     * To erase the current userData, use argument null. */
    @Override
    public void setUserData(Object uData) {
        userData = uData;
    }

    /** Pick up parcel p at the current location. If there is, add it to Truck
     * and wait PARCEL_PICKUP_TIME seconds. Do nothing (does not pick up) if the
     * calling thread is interrupted.
     * @throws RuntimeException  - if load is not null (can't pick up) or
     * if this Truck is currently traveling.
     * 
     */
    public void pickupLoad(Parcel p) throws RuntimeException {
        if (getStatus() == Status.TRAVELING)
            throw new RuntimeException("Can't Pickup Parcel while traveling");

        if (load != null)
            throw new RuntimeException("Can't Pickup Parcel with non-null load. " +
                    "Already holding a Parcel - " + load);

        if (location.getTrueParcels().contains(p)) {
            try {
                parcelLock.acquire();
            } catch (InterruptedException e) {
                parcelLock.release();
                return;
            }
            location.getTrueParcels().remove(p);
            load = p;
            try {
                load.pickedUp(this);
            } catch (InterruptedException e) {
                //Undo changes thus far to make it so that no pickup happened.
                location.getTrueParcels().add(p);
                load = null;
                parcelLock.release();
                return;
            }
            parcelLock.release();

            getManager().getScoreObject().changeScore(getBoard().getPickupCost());
        }
    }

    /** Drop off load at the current location. Throw a RuntimeException if load is null 
     * Do nothing (don't drop off) if the calling thread is interrupted. */
    public void dropoffLoad() throws RuntimeException {
        if (getStatus() == Status.TRAVELING)
            throw new RuntimeException("Can't Drop Off Parcel while traveling");

        if (load == null)
            throw new RuntimeException("Can't Drop Off a null parcel. No Parcel to drop off.");

        try {
            parcelLock.acquire();
        } catch (InterruptedException e) {
            return;
        }
        location.getTrueParcels().add(load);
        parcelLock.release();
        try {
            load.droppedOff();
        } catch (InterruptedException e) {
            //Undo drop off
            location.getTrueParcels().remove(load);
            return;
        }
        load = null;
        getManager().getScoreObject().changeScore(getBoard().getDropoffCost());

    }

    /** Return the circle that represents this truck when it is drawn */
    public Circle getCircle() {
        return circle;
    }

    /** Add road r to this Truck's travel plans, in a fashion that prevents
     * thread collision. Do nothing if the thread is interrupted. */
    public void addToTravel(Edge r) {
        if (goingTo == null)
            setGoingTo(r.getOther(location));
        else
            setGoingTo(r.getOther(goingTo));
        travel.add(r);
    }

    /** Clear the Travel queue, then 
     *  Set the travel queue to travel the given list of edges, in order. */
    public void setTravelQueue(List<Edge> path) {
    	clearTravel();
        for (Edge e : path) {
            addToTravel(e);
        }
    }

    /** Clear the travel queue, then
     * Set the travel queue to travel the given path.
     * First element is the truck's current location, and the last
     * is the expected destination.
     * @throws RuntimeException if the truck isn't currently at the first node in the path.
     */
    public void setTravelPath(List<Node> path) throws RuntimeException {
        if (status == Status.WAITING && path.get(0) != getLocation()
        	|| status == Status.TRAVELING && path.get(0) != getTravelingTo())
            throw new RuntimeException("Can't start travel at " + path.get(0) +
                    " because " + this + " is currently at " + getLocation());
        clearTravel();
        Node prev = null;
        for (Node n : path){
            if (prev != null){
                addToTravel(prev.getConnect(n));
            }
            prev = n;
        }
    }

    /** Pop the front road r of this Truck's travel plans, in a fashion that
     * prevents thread collision.
     * Return null if the calling thread is interrupted. */
    private Edge getTravel(){
        return travel.remove(0);
    }

    /** Clear the Truck's travel plans, in a fashion that prevents thread collision.
     * Reset goingTo (the Node the Truck will eventually end up at) to the value
     * of the travelingTo field (the Node the Truck is currently traveling toward). 
     * Do nothing (don't clear) if the calling thread is interrupted. */
    public void clearTravel() {
        setGoingTo(travelingTo);
        travel.clear();
    }

    /** Make the Truck travel along edge r.
     * The Truck will begin to travel only if its status is WAITING.
     * If status is TRAVELING, the Truck will ignore this call. 
     * @param r - the Edge for this Truck to travel along.
     * @throws InterruptedException for when the Truck thread sleeps.
     * @throws IllegalArgumentException if the Truck's current location
     * 		is not one of the exits for Edge r.
     */
    private final void travel(Edge r) throws InterruptedException, IllegalArgumentException {
        if (getStatus().equals(Status.WAITING)) {
            if (! r.isExit(location))
                throw new IllegalArgumentException("Truck is not adjacent and cannot travel Edge " + r);

            //Check OK, start Traveling
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
            while (progress < r.length) {
                Thread.sleep(getBoard().game.getFrame());

                //Get the speed lock, begin speed and cost computations
                speedLock.acquire();
                //If we can go the full speed's units, do that
                int remaining = r.length - progress;
                if(remaining >= speed){
                    progress += speed;
                    getManager().getScoreObject().changeScore(Score.cost(speed));
                }
                //Otherwise, go the remaining fraction, only deduct a correct percent of those points.
                else{
                    progress += remaining;
                    getManager().getScoreObject().changeScore(Score.cost(speed) * remaining / speed);
                }
                speedLock.release();
                double percent = (double)progress / (double)r.length;

                //Update Truck's location on the GUI
                updateGUILocation( (int) (percent * there.getX1() + (1-percent) * here.getX1()), 
                        (int) (percent * there.getY1() + (1-percent) * here.getY1()));
            }
            long finishTravelTime = System.currentTimeMillis();
            lastTravelTime += (finishTravelTime - startTravelTime); //Discount the time spent traveling

            //Done with this travel
            setStatus(Status.WAITING);

            setLocation(travelingTo);

            updateGUILocation(location.getCircle().getX1(), location.getCircle().getY1());
            travelingAlong.setTruckHere(this, false);
            location.setTruckHere(this, true);

            location.getCircle().updateColor();
            travelingAlong.getLine().updateToColorPolicy();

            if (location.getParcels().size() > 0) {
                preManagerNotification();
                game.getManager().truckNotification(this, Manager.Notification.PARCEL_AT_NODE);
                postManagerNotification();
            }
        }
    }

    /** Update the circle graphic that represents this truck on the GUI.
     * Do nothing if threads is null.
     * Also update the location of the load, if this truck is carrying one.
     * @param x - the new X location of this Truck in the GUI
     * @param y - the new Y location of this Truck in the GUI
     * */
    @Override
    public void updateGUILocation(int x, int y) {
        if (game.getGUI() != null) {
            circle.setX1(x);
            circle.setY1(y);
            circle.repaint();
            if (load != null) {
                load.updateGUILocation(x, y);
            }
        }
    }

    /** Return a string representation of this Truck --its name. */
    @Override
    public String toString(){
        return name;
    }

    /** Return the name of this truck to display on the GUI. */
    @Override
    public String getMappedName() {
        return getTruckName();
    }

    /** Return the location of this' name relative to its position on the GUI. */
    @Override
    public int getRelativeX() {
        return -Circle.DEFAULT_DIAMETER/2;
    }

    /** Return the location of this' name relative to its position on the GUI. */
    @Override
    public int getRelativeY() {
        return Circle.DEFAULT_DIAMETER + 10;			
    }

    /** Return true for this and return false for other trucks, 
     * because a truck is always "at" itself. */
    @Override
    public boolean isTruckHere(Truck t) {
        return this == t;
    }

    /**Return 1 -- there is always one truck at itself */
    @Override
    public int trucksHere() {
        return 1;
    }

    /** Called by the Game when the game is done. Cause this thread to die.
     * If thread is null, do nothing because this truck was never started. */
    protected void gameOver(){
        clearTravel();
        if (thread != null) thread.interrupt(); 
        alive = false;
    }

    /** Return a JSON String of this truck. This is just the basic truck
     * info pertaining to map creation - location and load not included.
     */	
    @Override
    public String toJSONString() {
        return "{\n" + Main.addQuotes(BoardElement.NAME_TOKEN) + ":" + Main.addQuotes(name) + "," +
                "\n" + Main.addQuotes(BoardElement.COLOR_TOKEN) + ":" + color.getRGB() + 
                "\n}";
    }
}
