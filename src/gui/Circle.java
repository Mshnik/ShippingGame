package gui;
import game.BoardElement;
import game.Node;
import game.Parcel;
import game.Score;
import game.Truck;
import game.Vector;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;

import javax.swing.JPanel;

/** Graphics class Circle  allows the drawing of circles.
 * Circles use an (x1, y1, diameter) coordinate system of where they are located
 * on the board.
 * Each Circle is tied to and represents a BoardElement. Circles can be tied to
 * instances of classes Node, Truck, and  Parcel. While a Circle can technically
 * be tied to an instance of class Edge, this option is not expected and may
 * result in irregular behavior.
 * @author MPatashnik
 *
 */
public class Circle extends JPanel {

	private static final long serialVersionUID = 1250263410666963976L;

	/** The default diameter of Circles in pixels when drawn on the GUI */ 
	public static final int DEFAULT_DIAMETER = 25;

	/** Extra space to add on diameter when calculating bounds*/
	public static final int PANEL_BUFFER = DEFAULT_DIAMETER/2;

	/** The minimum amount of distance between Circles in pixels when drawn on the GUI */
	public static final int BUFFER_RADUIS = DEFAULT_DIAMETER * 5;

	/** The game piece that this instance represents on the GUI */
	protected BoardElement represents;

	private int x1;
	private int y1;
	private int diameter;

	private Color color;
	private boolean filled;

	public static final Color DEFAULT_TRUCK_COLOR = Color.BLUE;
	public static final Color DEFAULT_NODE_COLOR = Color.BLACK;
	public static final Color DEFAULT_PARCEL_COLOR = Color.RED;

	private static final int LINE_THICKNESS = 2;

	/** Constructor: an instance at (x, y) of diameter d and colored black
	 * that represents r and is not filled.
	 * @param r - the Object this circle represents
	 * @param x - x coordinate of center
	 * @param y - y coordinate of center
	 * @param d - the diameter of the circle
	 */
	public Circle(BoardElement r, int x, int y, int d) {
		this(r, x, y, d, null, false);
	}

	/** Constructor: an instance at (x, y) of diameter d and colored c
     * that represents r and is filled iff filled.
	 * @param x - x coordinate of center
	 * @param y - y coordinate of center
	 * @param d - the diameter of the circle
	 * @param c - the Color of this circle - if null, uses the default color for represents
	 * @param filled - whether or not this circle is filled when it is drawn
	 */
	public Circle(BoardElement r, int x, int y, int d, Color c, boolean filled) {
		represents = r;

		//Set preliminary bounds
		setBounds(0,0,DEFAULT_DIAMETER + PANEL_BUFFER, DEFAULT_DIAMETER + PANEL_BUFFER);

		setDiameter(d);
		setX1(x);
		setY1(y);

		if (c != null)
			color = c;
		else {
			color = Color.black;
			if (r instanceof Node)
				color = DEFAULT_NODE_COLOR;
			if (r instanceof Truck)
				color = DEFAULT_TRUCK_COLOR;
			if (r instanceof Parcel)
				color = DEFAULT_PARCEL_COLOR;
		}
		this.filled = filled;
		setOpaque(false);
	}

	/** Return the x coordinate of this circle. */
	public int getX1() {
		return x1;
	}

	/** Set the x coordinate of this circle to x.
	 * Also set the bounds such that this circle will be visible upon drawing. */
	public void setX1(int x) {
		x1 = x;
		fixBounds();
	}

	/** Return the y coordinate of this circle. */
	public int getY1() {
		return y1;
	}

	/** Set the y coordinate of this circle to y*/
	public void setY1(int y) {
		y1 = y;
		fixBounds();
	}

	/** Return the color of this circle. */
	public Color getColor() {
		return color;
	}

	/** Return the coordinates of this circle as a Point. */
	public Point getPoint() {
		return new Point(x1, y1);
	}

	/** Set the color of this circle to c.
	 * @throws IllegalArgumentException - if c is not in Score.COLORS. */
	public void setColor(Color c) {
		if (!Score.colorContains(c))
			throw new IllegalArgumentException("Illegal Color (" + c.toString() +") passed in");

		color = c;
	}

	/** Return the diameter field of this circle. */
	public int getDiameter() {
		return diameter;
	}

	/** Return the diameter field of this circle to d and
	 * update the bounds (expands outward from center) */
	protected void setDiameter(int d) {
		diameter = d;
		fixBounds();
	}

	/** Change the color according to the Color Policy */
	public void updateColor() {
		//HERP
	}

	/** Extra height added (either on top or bottom) to bounds to fit text */
	private static final int TEXT_HEIGHT = 15;

	/** Extra width added to bounds to fit text */
	private static final int TEXT_WIDTH = 50;

	/** Fixe the boundaries so that all drawings will be within the bounds.
	 * Call after x, y, or diameter is changed. */
	private void fixBounds() {
		int x = getX1();
		int y = getY1();
		int d = getDiameter();
		int dP = d + PANEL_BUFFER;
		setBounds(x - dP/2, y - dP/2, dP, dP);
		Rectangle oldBounds = getBounds();
		if (represents instanceof Node) {
			setBounds(oldBounds.x, oldBounds.y - TEXT_HEIGHT,
			        oldBounds.width + TEXT_WIDTH, oldBounds.height + TEXT_HEIGHT);
		} else if (represents instanceof Truck) {
			setBounds(oldBounds.x, oldBounds.y, oldBounds.width + TEXT_WIDTH,
			        oldBounds.height + TEXT_HEIGHT);
		} 
		//No extra fix necessary for parcel.
	}

	/** Return the BoardElement that this Circle represents. */
	protected BoardElement getRepresents() {
		return represents;
	}

	/** Switch this Circle's location with the location of c */
	protected void switchLocation(Circle c) {
		int x2 = c.getX1();
		int y2 = c.getY1();

		c.setX1(x1);
		c.setY1(y1);

		setX1(x2);
		setY1(y2);
	}

	/** Return the distance between the centers of this Circle and c. */
	public double getDistance(Circle c) {
		return Math.sqrt( (Math.pow(x1-c.getX1(), 2) + Math.pow(y1 - c.getY1(), 2) ) );
	}
	
	/** Return true iff the this circle has the same center as c */
	public boolean locationEquals(Circle c) {
		return x1 == c.x1 && y1 == c.y1;
	}

	/** Return the vector from the center of this circle to the center of c. */
	public Vector getVectorTo(Circle c) {
		return new Vector(c.x1 - x1, c.y1 - y1);
	}

	/** Return a string representation of this circle. */
	@Override
	public String toString() {
		return "("+ (getX1()-getDiameter()/2) + "," + (getY1()-getDiameter()/2) + 
		        ") , d=" + getDiameter() + " " + represents.getMappedName();
	}

	/**Draw the Circle when the component is painted. */
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D)g;

		g2d.setStroke(new BasicStroke(LINE_THICKNESS));
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		int heightPlus = 0;
		if (represents instanceof Node)
			heightPlus = TEXT_HEIGHT;

		Ellipse2D.Double circle2d = new Ellipse2D.Double(PANEL_BUFFER/2, 
		        PANEL_BUFFER/2 + heightPlus, getDiameter(), getDiameter());
		g2d.setColor(getColor());
		if (filled) g2d.fill(circle2d);
		g2d.draw(circle2d);
		g2d.drawString(represents.getMappedName(), represents.getRelativeX() + PANEL_BUFFER,
		        represents.getRelativeY() + PANEL_BUFFER);

		if (represents instanceof Node) {
			g2d.setColor(Color.BLACK);
			Node n = (Node)represents;

			if (! n.getParcels().isEmpty()) {
				g2d.drawString(n.getParcels().size() + "", diameter/2 + 2,
				        diameter/2 + PANEL_BUFFER + heightPlus);
			}
		}

	}

	/** Return a bounding square (of size diameter * diameter) of the circle. */
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(getDiameter(), getDiameter());
	}
}
