package game;
import gui.Line;

import java.awt.Color;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** The Edge Class in ShippingGame allows creation of the connections between Nodes that Trucks can travel along.
 * Each Edge is bidirectional and is connected to exactly two Nodes. 
 * The getFirstExit() and getSecondExit() methods allow access to these Nodes. 
 * Each Edge is weighted (has a length), which is the amount of time (in units that are converted to milliseconds) it takes
 * for a Truck to cross this Edge. One useful method to highlight is the getOther(Node) method, which returns the other exit
 * of the Edge given either exit. <br><br>
 * 
 * The Edge Class implements BoardElement and is represented on the threads by an instance of the Line class. This implementation
 * requires the creation of the four BoardElement methods, which are relatively unused outside of the graphical representation of this Edge.
 * <br><br>
 * 
 * There are no public Edge constructors, so classes outside of the game package will not be able to construct additional Edge objects
 * 
 * @author MPatashnik
 *
 */
public class Edge implements BoardElement{

	public static final int DEFAULT_MIN_LENGTH = Integer.MAX_VALUE;	//Max val an edge can have for length
	public static final int DEFAULT_MAX_LENGTH = 0;					//Min val an edge can have for length

	private Node[] exits;	//The Nodes this edge connects. Always has length 2.
	
	/** Returns the length (weight) of this Edge. Uncorrelated with its graphical length on the GUI */
	public final int length;		

	private Map<Truck, Boolean> truckHere; //Maps truck -> is here
	
	private Object userData; //User data (if any) stored in this edge

	private Line line; //Graphical representation of this Edge
	
	private final Board board;	//The board this Edge belongs to

	/** Constructor. Accepts a non-null Node array of length 2 to be exits and an integer length
	 * lengthOfRoad must be positive and non-zero
	 * @throws IllegalArgumentException:
	 * 		if exits is null or has length not equal to 2
	 * 		if lengthOfRoad is less than 1 and not equal to Edge.DUMMY_LENGTH
	 * 		if either of the nodes are null
	 * 		if exits[0] and exits[1] are the same node*/
	protected Edge(Board m, Node exits[], int lengthOfRoad) throws IllegalArgumentException{
		this(m, exits[0], exits[1], lengthOfRoad);

		if(exits.length != 2)
			throw new IllegalArgumentException("Incorrectly sized Node Array Passed into Edge Constructor");
	}

	/** Constructor. Accepts two non-null nodes to be exits and an integer length
	 * lengthOfRoad must be positive and non-zero
	 * @throws IllegalArgumentException:
	 * 		if lengthOfRoad is less than 1 and not equal to Edge.DUMMY_LENGTH
	 * 		if either of the nodes are null
	 * 		if firstExit and secondExit are the same node*/
	protected Edge(Board m, Node firstExit, Node secondExit, int lengthOfRoad) throws IllegalArgumentException{
		Node[] e = new Node[2];
		e[0] = firstExit;
		e[1] = secondExit;
		setExits(e);
		
		board = m;
		truckHere = Collections.synchronizedMap(new HashMap<Truck, Boolean>());

		if(lengthOfRoad <= 0)
			throw new IllegalArgumentException("lengthOfRoad value " + lengthOfRoad + " is an illegal value.");
		
		length = lengthOfRoad;
		line = new Line(firstExit.getCircle(), secondExit.getCircle(), this);
	}

	/** Returns the Board this Edge belongs to. */
	@Override
	public Board getBoard(){
		return board;
	}
	
	/** Returns the exits of this line, a length 2 array of Nodes */
	protected Node[] getTrueExits(){
		return exits;
	}

	/** Returns the first exit of this Edge */
	public Node getFirstExit(){
		return exits[0];
	}

	/** Returns the second exit of this Edge */
	public Node getSecondExit(){
		return exits[1];
	}

	/** Returns the exits of this line, a length 2 array of Nodes. Copies the nodes into a new array to
	 * prevent interference with the exits of this node. (Setting the values of the return of this method
	 * will not alter the Edge object).
	 */
	public Node[] getExits(){
		Node[] newExits = new Node[2];
		newExits[0] = exits[0];
		newExits[1] = exits[1];
		return newExits;
	}

	/** Checks that the following are satisfied, then sets the value of
	 * the exits field to newExits. Only used in edge construction
	 * @param newExits - the new exits to set.
	 * @throws IllegalArgumentException:
	 * 		if exits is null or has length not equal to 2
	 * 		if either of the nodes are null
	 * 		if exits[0] and exits[1] are the same node
	 **/
	private void setExits(Node[] newExits) throws IllegalArgumentException{
		if(newExits == null)
			throw new IllegalArgumentException("Null Node Array Passed into Edge Constructor");

		if(newExits.length != 2)
			throw new IllegalArgumentException("Incorrectly sized Node Array Passed into Edge Constructor");

		if(newExits[0] == null)
			throw new IllegalArgumentException("First Node Passed into Edge constructor is null");

		if(newExits[1] == null)
			throw new IllegalArgumentException("Second Node Passed into Edge constructor is null");

		if(newExits[0].equals(newExits))
			throw new IllegalArgumentException("Two Nodes Passed into Edge constructor refer to the same node");

		exits = newExits;
	}

	/** Returns true if Node {@code node} is one of the exits of this Edge, false otherwise */
	public boolean isExit(Node node){
		if(exits[0].equals(node) || exits[1].equals(node))
			return true;
		else
			return false;
	}

	/** Returns true if this Edge and Edge r share a Node exit, false otherwise. */
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

	/** Returns the other exit that is not equal to Node {@code n}.
	 *  Returns null if {@code n} is neither of the nodes in exits */
	public Node getOther(Node n){
		if(exits[0].equals(n))
			return exits[1];
		if(exits[1].equals(n))
			return exits[0];

		return null;
	}

	/** Returns the Line that represents this edge graphically. */
	public Line getLine(){
		return line;
	}

	/** Returns the userData stored in this edge. May be null if the user has not yet given this Node userData */
	@Override
	public Object getUserData(){
		return userData;
	}

	/** Sets the value of userData to Object uData. To erase the current userData just pass in null */
	@Override
	public void setUserData(Object uData){
		userData = uData;
	}

	/** Returns the color of this Edge, as it is pained on the GUI. Color
	 * of edges has no game significance, thus this value may be changed during gameplay.
	 */
	@Override
	public Color getColor(){
		return line.getColor();
	}

	/** Two Edges are equal if they have the same exits, even if they have different lengths.
	 * This makes sure that only one edge connects each pair of nodes in duplicate-free collections  */
	@Override
	public boolean equals(Object e){
		if(e == null)
			return false;
		if(! (e instanceof Edge) )
			return false;

		return ( exits[0].equals( ((Edge)e).getTrueExits()[1]) && exits[1].equals( ((Edge)e).getTrueExits()[0]) )
				|| ( exits[0].equals( ((Edge)e).getTrueExits()[0]) && exits[1].equals( ((Edge)e).getTrueExits()[1]) );
	}

	/** An Edge's hashCode is equal to the sum of the hashCodes of its first exit and its second exit.
	 * {@code getFirstExit().hashCode() + getSecondExit().hashCode()}.
	 * Notably this means the ordering of the exits for an edge doesn't matter for hashing */
	@Override
	public int hashCode(){
		return exits[0].hashCode() + exits[1].hashCode();
	}

	/** Returns a String representation of this object:
	 * {@code getFirstExit().name + " to " + getSecondExit().name} */
	@Override
	public String toString(){
		return exits[0].name + " to " + exits[1].name; 
	}
	
	/** Returns exits of this and the length for its JSON string */
	@Override
	public String toJSONString(){
		return "{\n" + Main.addQuotes(BoardElement.LOCATION_TOKEN) + ":[" 
				     + Main.addQuotes(exits[0].name) + "," + Main.addQuotes(exits[1].name) + "]," +
				"\n" + Main.addQuotes(BoardElement.LENGTH_TOKEN) + ":" + length + 
				"\n}";
	}

	/** Tells the node if a Truck is currently on it or not. 
	 * Gets its truckLock to prevent Truck thread collision */
	protected void setTruckHere(Truck t, Boolean isHere){
		truckHere.put(t, isHere);
	}

	/** Returns a String to print when this object is drawn on a GUI */
	@Override
	public String getMappedName() {
		return "" + length;
	}

	/** Returns the x location the boardped name of this Edge relative to the top left corner of the line */
	@Override
	public int getRelativeX() {
		return line.getXMid() - line.getX1() + Line.LINE_THICKNESS;
	}

	/** Returns the y location the boardped name of this Edge relative to the top left corner of the line */
	@Override
	public int getRelativeY() {
		return line.getYMid() - line.getY1() + Line.LINE_THICKNESS*3;
	}

	/** Returns true if the given truck is currently traveling this edge, false otherwise. */
	@Override
	public boolean isTruckHere(Truck t){
		return truckHere.get(t);
	}
	
	/** Returns the number of trucks here - currently traveling this edge. */
	@Override
	public int trucksHere(){
		int i = 0;
		for(Boolean b : truckHere.values()){
			if(b) i++;
		}
		return i;
	}

	/** Repaints itself. Values of x and y unused, but included to comply with interface. */
	@Override
	public void updateGUILocation(int x, int y) {
		getLine().repaint();
	}
}
