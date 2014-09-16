package gui;
import game.MapElement;
import game.Node;
import game.Parcel;
import game.Score;
import game.Truck;
import game.Vector;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;

import javax.swing.JPanel;

/** The Circle class is a graphic class that allows the drawing of circles.
 * Circles use an (x1, y1, diameter) coordinate system of where they are located on the threads.
 * Each Circle is tied to a MapElement that it represents. Circles can be tied instances of the
 * Node class, Truck class, and the Parcel class. While a Circle can technically be tied to an instance
 * of the Edge class, this option is not expected and may result in irregular behavior.
 * @author MPatashnik
 *
 */
public class Circle extends JPanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1250263410666963976L;

	/** The default diameter of Circles in pixels when they are drawn on the GUI */ 
	public static final int DEFAULT_DIAMETER = 20;
	
	/** Extra space to add on diameter when calculating bounds
	 * 
	 */
	public static final int PANEL_BUFFER = DEFAULT_DIAMETER/2;

	/** The minimum amount of distance between Circles in pixels when they are drawn on the GUI */
	public static final int BUFFER_RADUIS = DEFAULT_DIAMETER * 5;

	MapElement represents;

	private int x1;
	private int y1;
	private int diameter;

	private Color color;
	private boolean filled;
	
	public static final Color DEFAULT_TRUCK_COLOR = Color.BLUE;
	public static final Color DEFAULT_NODE_COLOR = Color.BLACK;
	public static final Color DEFAULT_PARCEL_COLOR = Color.RED;

	private static final int LINE_THICKNESS = 2;

	/** Constructor for the Circle class. The circle is colored black.
	 * @param represents - the Object this circle represents
	 * @param x - x coordinate of center
	 * @param y - y coordinate of center
	 * @param diameter - the diameter of the circle
	 */
	public Circle(MapElement represents, int x, int y, int diameter){
		this(represents, x, y, diameter, null, false);
	}

	/** Constructor for the Circle class. The circle is colored black.
	 * @param x - x coordinate of center
	 * @param y - y coordinate of center
	 * @param diameter - the diameter of the circle
	 * @param color - the Color of this circle
	 * @param filled - whether or not this circle is filled when it is drawn
	 */
	public Circle(MapElement represents, int x, int y, int diameter, Color color, boolean filled){
		this.represents = represents;
		
		//Set preliminary bounds
		setBounds(0,0,DEFAULT_DIAMETER + PANEL_BUFFER, DEFAULT_DIAMETER + PANEL_BUFFER);
		//setBorder(new LineBorder(Color.black, 2));
		
		
		this.setDiameter(diameter);
		this.setX1(x);
		this.setY1(y);
		
		if(color != null)
			this.color = color;
		else{
			this.color = Color.black;
			if(represents instanceof Node)
				this.color = DEFAULT_NODE_COLOR;
			if(represents instanceof Truck)
				this.color = DEFAULT_TRUCK_COLOR;
			if(represents instanceof Parcel)
				this.color = DEFAULT_PARCEL_COLOR;
		}
		this.filled = filled;
		setOpaque(false);
	}

	/** Returns the x1 coordinate of this circle*/
	public int getX1() {
		return x1;
	}

	/** Sets the x1 coordinate of this circle.
	 * Also sets the bounds such that this circle will be visible upon drawing */
	public void setX1(int x1) {
		this.x1 = x1;
		fixBounds();
	}

	/** Returns the y1 coordinate of this circle */
	public int getY1() {
		return y1;
	}

	/** Sets the y1 coordinate of this circle */
	public void setY1(int y1) {
		this.y1 = y1;
		fixBounds();
	}

	/** Returns the color of this circle */
	public Color getColor(){
		return color;
	}

	/** Returns the coordinates of this circle as a Point object */
	public Point getPoint(){
		return new Point(x1, y1);
	}
	
	/** Sets the color of this circle.
	 * @throws IllegalArgumentException - if c is not in Score.COLORS */
	public void setColor(Color c){
		if(!Score.colorContains(c))
			throw new IllegalArgumentException("Illegal Color (" + c.toString() +") passed in");
		
		color = c;
	}

	/** Returns the diameter field of this circle */
	public int getDiameter() {
		return diameter;
	}

	/** Returns the diameter field of this circle.
	 * Also updates the bounds (expands outwards from center) */
	protected void setDiameter(int diameter) {
		this.diameter = diameter;
		fixBounds();
	}
	
	/** Changes the color according to the Color Policy */
	public void updateColor(){
		//Do something??
	}
	
	/** Extra height added (either on top or bottom) to bounds to fit text */
	private static final int TEXT_HEIGHT = 15;
	
	/** Extra width added to bounds to fit text */
	private static final int TEXT_WIDTH = 50;
	
	/** Fixes the boundaries so that all drawings will be within the bounds.
	 * Call after x, y, or diameter is changed. */
	private void fixBounds(){
		int x = getX1();
		int y = getY1();
		int d = getDiameter();
		int dP = d + PANEL_BUFFER;
		setBounds(x - dP/2, y - dP/2, dP, dP);
		Rectangle oldBounds = getBounds();
		if(represents instanceof Node){
			setBounds(oldBounds.x, oldBounds.y - TEXT_HEIGHT, oldBounds.width + TEXT_WIDTH, oldBounds.height + TEXT_HEIGHT);
		} else if (represents instanceof Truck){
			setBounds(oldBounds.x, oldBounds.y, oldBounds.width + TEXT_WIDTH, oldBounds.height + TEXT_HEIGHT);
		} 
		//No extra fix necessary for parcel.
	}

	/** Returns the MapElement this object represents */
	protected MapElement getRepresents(){
		return represents;
	}

	/** Switches this Circle's location with the location of circle c */
	protected void switchLocation(Circle c){
		int x2 = c.getX1();
		int y2 = c.getY1();

		c.setX1(x1);
		c.setY1(y1);

		this.setX1(x2);
		this.setY1(y2);
	}

	/** Returns the distance between this Circle and Circle c */
	public double getDistance(Circle c){
		return Math.sqrt( (Math.pow(x1-c.getX1(), 2) + Math.pow(y1 - c.getY1(), 2) ) );
	}
	
	/** Returns the vector from this circle to circle c */
	public Vector getVectorTo(Circle c){
		return new Vector(c.x1 - x1, c.y1 - y1);
	}

	@Override
	/** Returns a string representation of this circle */
	public String toString(){
		return "("+ (getX1()-getDiameter()/2) + "," + (getY1()-getDiameter()/2) + ") , d=" + getDiameter() + " " + represents.getMappedName();
	}

	@Override
	/**Draws the Circle when the component is painted*/
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D)g;

		g2d.setStroke(new BasicStroke(LINE_THICKNESS));
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON);
		
		int heightPlus = 0;
		if(represents instanceof Node)
			heightPlus = TEXT_HEIGHT;
		
		Ellipse2D.Double circle2d = new Ellipse2D.Double(PANEL_BUFFER/2, PANEL_BUFFER/2 + heightPlus, getDiameter(), getDiameter());
		g2d.setColor(getColor());
		if(filled) g2d.fill(circle2d);
		g2d.draw(circle2d);
		g2d.setFont(new Font("Arial", Font.PLAIN, 13));
		g2d.drawString(represents.getMappedName(), represents.getRelativeX() + PANEL_BUFFER, represents.getRelativeY() + PANEL_BUFFER);
	}

	@Override
	/** Returns the size of the circle */
	public Dimension getPreferredSize(){
		return new Dimension(getDiameter(), getDiameter());
	}
}
