package gameFiles;
import gui.Line;

import java.awt.Color;
import java.util.HashSet;
import java.util.concurrent.Semaphore;

/** The Edge Class in ShippingGame allows creation of the connections between Nodes that Trucks can Travel along
 * Each Edge is connected to exactly two Nodes, and the getFirstExit() and getSecondExit() methods allow access to these
 * Nodes. Each Edge also has a length, which is the amount of time (in units that are converted to milliseconds) it takes
 * for a Truck to cross this Edge. One useful method to highlight is the getOther(Node) method, which returns the other exit
 * of the Edge given either exit. <br><br>
 * 
 * The Edge Class implements MapElement and is represented on the threads by an instance of the Line class. This implementation
 * requires the creation of the four MapElement methods, which are relatively unused outside of the graphical representation of this Edge.
 * <br><br>
 * 
 * There are no public Edge constructors, so classes outside of the gameFiles package will not be able to construct additional Edge objects
 * <br><br>
 * 
 * The Edge Class also has public static methods, which pertian to the minimum and maximum lengths of all Edges.
 * @author MPatashnik
 *
 */
public class Edge implements MapElement{

	/** The length of "Blank" Edges. Should be later changed to an actual length value */
	protected static final int DUMMY_LENGTH = Integer.MIN_VALUE;

	private static HashSet<Edge> edges = new HashSet<Edge>();

	public static final int DEFAULT_MIN_LENGTH = Integer.MAX_VALUE;
	public static final int DEFAULT_MAX_LENGTH = 0;

	private static int minLength = DEFAULT_MIN_LENGTH;
	private static int maxLength = DEFAULT_MAX_LENGTH;

	private Node[] exits;	//The Nodes this edge connects. Always has length 2.
	private int length;		//The length of this line

	private Semaphore truckLock; //Lock that trucks must acquire in order to make changes to this edge.

	private Object userData;

	private Line line;
	private boolean truckHere;

	/** Constructor. Accepts a non-null Node array of length 2 to be exits and an integer length
	 * lengthOfRoad must be positive and non-zero
	 * @throws IllegalArgumentException:
	 * 		if exits is null or has length not equal to 2
	 * 		if lengthOfRoad is less than 1 and not equal to Edge.DUMMY_LENGTH
	 * 		if either of the nodes are null
	 * 		if exits[0] and exits[1] are the same node*/
	protected Edge(Node exits[], int lengthOfRoad) throws IllegalArgumentException{
		this(exits[0], exits[1], lengthOfRoad);

		if(exits.length != 2)
			throw new IllegalArgumentException("Incorrectly sized Node Array Passed into Edge Constructor");
	}

	/** Constructor. Accepts two non-null nodes to be exits and an integer length
	 * lengthOfRoad must be positive and non-zero
	 * @throws IllegalArgumentException:
	 * 		if lengthOfRoad is less than 1 and not equal to Edge.DUMMY_LENGTH
	 * 		if either of the nodes are null
	 * 		if firstExit and secondExit are the same node*/
	protected Edge(Node firstExit, Node secondExit, int lengthOfRoad) throws IllegalArgumentException{

		if(firstExit == null)
			throw new IllegalArgumentException("First Node Passed into Edge constructor is null");

		if(secondExit == null)
			throw new IllegalArgumentException("Second Node Passed into Edge constructor is null");

		if(firstExit.equals(secondExit))
			throw new IllegalArgumentException("Two Nodes Passed into Edge constructor refer to the same node");

		if(lengthOfRoad <= 0 && lengthOfRoad != DUMMY_LENGTH)
			throw new IllegalArgumentException("lengthOfRoad value " + lengthOfRoad + " is an illegal value.");


		exits = new Node[2];
		exits[0] = firstExit;
		exits[1] = secondExit;
		
		truckLock = new Semaphore(1);

		length = lengthOfRoad;
		updateMinMaxLength();

		line = new Line(null, null, this);
		edges.add(this);
	}

	/** Returns the exits of this line, a length 2 array of Nodes */
	protected Node[] getTrueExits(){
		return exits;
	}

	/** Returns the first exit of this line */
	public Node getFirstExit(){
		return exits[0];
	}

	/** Returns the second exit of this line */
	public Node getSecondExit(){
		return exits[1];
	}

	/** Returns the exits of this line, a length 2 array of Nodes. Copies the nodes into a new array to
	 * prevent interference with the exits of this node
	 */
	public Node[] getExits(){
		Node[] newExits = new Node[2];
		newExits[0] = exits[0];
		newExits[1] = exits[1];
		return newExits;
	}

	/** Checks that the following are satisfied, then sets the value of
	 * the exits field to newExits
	 * @param newExits - the new exits to set.
	 * @throws IllegalArgumentException:
	 * 		if exits is null or has length not equal to 2
	 * 		if either of the nodes are null
	 * 		if exits[0] and exits[1] are the same node
	 **/
	protected void setExits(Node[] newExits) throws IllegalArgumentException{
		if(exits == null)
			throw new IllegalArgumentException("Null Node Array Passed into Edge Constructor");

		if(exits.length != 2)
			throw new IllegalArgumentException("Incorrectly sized Node Array Passed into Edge Constructor");

		if(exits[0] == null)
			throw new IllegalArgumentException("First Node Passed into Edge constructor is null");

		if(exits[1] == null)
			throw new IllegalArgumentException("Second Node Passed into Edge constructor is null");

		if(exits[0].equals(exits[1]))
			throw new IllegalArgumentException("Two Nodes Passed into Edge constructor refer to the same node");

		exits = newExits;
	}

	/** Returns the length of this line */
	public int getLength(){
		return length;
	}

	/** Sets the length of this line.
	 * @throws IllegalArgumentException if lengthOfRoad is less than 1 and not equal to DUMMY_LENGTH
	 */
	protected void setLength(int lengthOfRoad) throws IllegalArgumentException{
		if(lengthOfRoad <= 0 && lengthOfRoad != DUMMY_LENGTH)
			throw new IllegalArgumentException("lengthOfRoad value " + lengthOfRoad + " is an illegal value.");

		length = lengthOfRoad;
		updateMinMaxLength();
	}

	/** Updates the Minimum and Maximum lengths of all edge instances */
	public static void updateMinMaxLength(){
		minLength = DEFAULT_MIN_LENGTH;
		maxLength = DEFAULT_MAX_LENGTH;

		for(Edge e : edges){
			if(e.length != DUMMY_LENGTH){
				minLength = Math.min(minLength, e.length);
				maxLength = Math.max(maxLength, e.length);
			}
		}
	}

	/** Returns the maximum length of all edges */
	public static int getMaxLength(){
		return maxLength;
	}

	/** Returns the minimum length of all edges */
	public static int getMinLength(){
		return minLength;
	}

	/** Returns true if Node node is one of the exits of this Edge, false otherwise */
	public boolean isExit(Node node){
		if(exits[0].equals(node) || exits[1].equals(node))
			return true;
		else
			return false;
	}

	/** Returns true if this Edge and Edge r share a Node exit, false otherwise */
	public boolean sharesExit(Edge r){
		if(exits[0].equals(r.getTrueExits()[0]))
			return true;
		if(exits[0].equals(r.getTrueExits()[1]))
			return true;
		if(exits[1].equals(r.getTrueExits()[0]))
			return true;
		if(exits[1].equals(r.getTrueExits()[1]))
			return true;

		return false;
	}

	/** Returns the other exit that is not equal to Node n.
	 *  Returns null if n is neither of the nodes in exits */
	public Node getOther(Node n){
		if(exits[0].equals(n))
			return exits[1];
		if(exits[1].equals(n))
			return exits[0];

		return null;
	}

	/** Returns the Line that represents this node graphically */
	public Line getLine(){
		return line;
	}

	/** Returns the userData stored in this Node. May be null if the user has not yet given this Node userData */
	public Object getUserData(){
		return userData;
	}

	/** Sets the value of userData to Object uData. To erase the current userData just pass in null */
	public void setUserData(Object uData){
		userData = uData;
	}

	public Color getColor(){
		return line.getColor();
	}

	@Override
	/** Two Edges are equal if they have the same exits */
	public boolean equals(Object e){
		if(e == null)
			return false;
		if(! (e instanceof Edge) )
			return false;

		return ( exits[0].equals( ((Edge)e).getTrueExits()[1]) && exits[1].equals( ((Edge)e).getTrueExits()[0]) )
				|| ( exits[0].equals( ((Edge)e).getTrueExits()[0]) && exits[1].equals( ((Edge)e).getTrueExits()[1]) );
	}

	@Override
	/** An Edge's hashCode is equal to the sum of the hashCodes of its first exit and its second exit */
	public int hashCode(){
		return exits[0].hashCode() + exits[1].hashCode();
	}

	@Override
	/** Returns a String representation of this object */
	public String toString(){
		return exits[0].getName() + " to " + exits[1].getName(); 
	}

	/** Tells the edge whether or not a Truck is currently on it. Requires truck lock. 
	 * @throws InterruptedException */
	protected void setTruckHere(boolean truckHere) throws InterruptedException{
		truckLock.acquire();
		this.truckHere = truckHere;
		truckLock.release();
	}

	@Override
	/** Returns a String to map when this object is drawn on a GUI */
	public String getMappedName() {
		return "" + length;
	}

	@Override
	/** Returns the x location the mapped name of this Edge relative to the top left corner of the line */
	public int getRelativeX() {
		return line.getXMid() - line.getX1() + Line.LINE_THICKNESS;
	}

	@Override
	/** Returns the y location the mapped name of this Edge relative to the top left corner of the line */
	public int getRelativeY() {
		return line.getYMid() - line.getY1() + Line.LINE_THICKNESS*3;
	}

	@Override
	/** Returns true if a Truck is currently on this Edge, false otherwise. */
	public boolean isTruckHere() throws InterruptedException {
		truckLock.acquire();
		boolean b = truckHere;
		truckLock.release();
		return b;
	}
}
