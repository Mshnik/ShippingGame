package gui;
import game.*;

import java.awt.*;
import java.awt.geom.Line2D;

import javax.swing.JPanel;

/** This graphics class  allows the drawing of lines.
 * Lines use a (c1, c2) coordinate system, where c1 and c2 are circle objects
 * that denote the endpoints of this line.
 * Each circle has (x, y) coordinates, so a Line can be though of as having
 * (x1, y1, x2, y2).
 * Each Line is tied to a BoardElement (most likely an Edge) that it represents.
 * While a Line can be tied to a Node, Parcel, or Truck, this behavior is unspecified.
 * @author MPatashnik
 *
 */
public class Line  extends JPanel{

	private static final long serialVersionUID = -1688624827819736589L;

	/** Default thickness of lines when they are drawn on the GUI */
	public static final int LINE_THICKNESS = 2;

	/** Default color of lines when they are drawn on the GUI */
	public static final Color DEFAULT_COLOR = Color.DARK_GRAY;

	/** Color of lines when they are being traveled by a truck, if HIGHLIGHT_TRAVEL is selected */
	public static final Color TRAVELING_COLOR = Color.RED;

	/** Color of the lines representing the shortest edges, if DISTANCE_GRADIENT is selected */
	public static final Color GRADIENT_SHORT_COLOR = Color.CYAN;

	/** Color of the lines representing the longest edges, if DISTANCE_GRADIENT is selected */
	public static final Color GRADIENT_LONG_COLOR = Color.BLACK;

	/** The different ways to draw the edges of the graph.
	 * @author MPatashnik
	 */
	public enum ColorPolicy{
		/** All edges drawn as a single dark-gray color */
		DEFAULT,
		/** Edges that trucks are currently traveling drawn red, others drawn dark-gray */
		HIGHLIGHT_TRAVEL,
		/** Edges drawn as a linear interpolation of their weights
		 * relative to other edges in the graph. Lighter edges are shorter.
		 */
		DISTANCE_GRADIENT
	}

	private static ColorPolicy colorPolicy = ColorPolicy.DEFAULT; //The color policy to follow

	private Circle c1;  //Endpoint one of this line
	private Circle c2;  //Endpoint two of this line

	private Color color; //The color to draw this line; should stay in sync with the color policy

	private BoardElement represents; //The BoardElement (probably Edge) that this represents

	/** Constructor: a line from c1 to c2 representing r and colored according
	 * to the color policy.
	 * @param c1 - the Circle that marks the first end of this line
	 * @param c2 - the Circle that marks the second end of this line
	 * @param r - the MapElement this Line represents when drawn on the GUI
	 */
	public Line(Circle c1, Circle c2, BoardElement r) {
		setC1(c1);
		setC2(c2);
		represents = r;
		setOpaque(false);
		fixBounds();
		updateToColorPolicy();
	}

	/** Return the first end of this line. */
	public Circle getC1() {
		return c1;
	}

	/** Sets the first end of this line to c */
	protected void setC1(Circle c) {
		c1 = c;
	}

	/** Return the second end of this line. */
	public Circle getC2() {
		return c2;
	}

	/** Sets the second end of this line to c. */
	protected void setC2(Circle c) {
		c2 = c;
	}

	/** Return the x coordinate of the first end of this line. */
	public int getX1() {
		return c1.getX1();
	}

	/** Return the y coordinate of the first end of this line. */
	public int getY1() {
		return c1.getY1();
	}

	/** Return the x coordinate of the second end of this line. */
	public int getX2() {
		return c2.getX1();
	}

	/** Return the y coordinate of the second end of this line. */
	public int getY2() {
		return c2.getY1();
	}

	/** Return the midpoint of this line. */
	public Point getMid() {
		return new Point(getXMid(), getYMid());
	}

	/** Return the x value of the midpoint of this line. */
	public int getXMid() {
		return (c1.getX1() + c2.getX1()) / 2;
	}

	/** Return the y value of the midpoint of this line. */
	public int getYMid() {
		return (c1.getY1() + c2.getY1()) / 2;
	}

	/** Return the width (x diff) of the line. Always positive. */
	public int getLineWidth() {
		return Math.abs(getX1() - getX2());
	}

	/** Return the height (y diff) of the line. Always positive. */
	public int getLineHeight() {
		return Math.abs(getY1() - getY2());
	}

	/** Dynamically resize the drawing boundaries of this line based on the
	 * height and width of the line, with a minimum sized box of (40,40).
	 * Call whenever circles move to fix the drawing boundaries of this. */
	public void fixBounds() {
		int minX = Math.min(getX1(), getX2());
		int minY = Math.min(getY1(), getY2());
		int width = Math.max(Math.abs(getX1() - getX2()), 40);
		int height = Math.max(Math.abs(getY1() - getY2()), 40);

		setBounds(minX, minY, width + 2, height + 2);
	}

	/** Return the current color of this line, which is determined by the color policy. */
	public Color getColor() {
		return color;
	}

	/** Update the Color of this Line according to the currently selected color policy. */
	public void updateToColorPolicy() {
		switch (colorPolicy) {
			case DEFAULT:
				color = DEFAULT_COLOR;
				break;
			case HIGHLIGHT_TRAVEL:
				if (represents != null && represents.trucksHere() > 0)
					color = TRAVELING_COLOR;
				else
					color = DEFAULT_COLOR;
				break;
			case DISTANCE_GRADIENT:
				if (represents != null)
					color = getDistGradientColor();
				else
					color = DEFAULT_COLOR;
				break;
		}

		repaint();
	}

	/** Return the color for this line using the distance gradient.
	 * It compares the length of the edge this line to the max and min values
	 * of edge length. */
	private Color getDistGradientColor() {
		Board m = represents.getBoard();
		if (m.getMaxLength() == Edge.DEFAULT_MAX_LENGTH || 
				m.getMinLength() == Edge.DEFAULT_MIN_LENGTH ||
				! (represents instanceof Edge))
			return DEFAULT_COLOR;

		Edge e = (Edge)represents;
		int max = m.getMaxLength();
		int min = m.getMinLength();
		double v = (double)(e.length - min) / ((double)(max - min));

		return new Color( 
				(int)( (double)GRADIENT_LONG_COLOR.getRed()    *    v) + 
				(int)( (double)GRADIENT_SHORT_COLOR.getRed()   * (1-v)),
				(int)( (double)GRADIENT_LONG_COLOR.getGreen()  *    v) + 
				(int)( (double)GRADIENT_SHORT_COLOR.getGreen() * (1-v)),
				(int)( (double)GRADIENT_LONG_COLOR.getBlue()   *    v) + 
				(int)( (double)GRADIENT_SHORT_COLOR.getBlue()  * (1-v))
				);
	}

	/** Return the color policy for painting roads.
	 * @see Line.ColorPolicy
	 */
	public static ColorPolicy getColorPolicy() {
		return colorPolicy;
	}

	/** Set the colorPolicy to policy.
	 * @see Line.ColorPolicy
	 */
	public static void setColorPolicy(ColorPolicy policy) {
		colorPolicy = policy;
	}

	/** Return the BoardElement that this object represents. */
	protected BoardElement getRepresents() {
		return represents;
	}

	/** Number of pixels of tolerance for a point to be considered on the line */
	public static final int ON_LINE_TOLERANCE = 20;

	/** Return true iff Point p is within ON_LINE_TOLERANCE pixels of this line. */
	public boolean isOnLine(Point p) {
		double dist = distanceTo(p);
		return dist <= (double)ON_LINE_TOLERANCE;
	}

	/** Return the distance from p to this line. */
	public double distanceTo(Point p) {
		return Line2D.ptLineDist(c1.getX1(), c1.getY1(), c2.getX1(), c2.getY1(), p.getX(), p.getY());
	}

	/** Return the angle between this line and line l, in radians.
	 * Return is in the range 0 .. PI.
	 * Throw an illegalArgumentException the two lines don't share an endpoint.
	 */
	public double radAngle(Line l) throws IllegalArgumentException{
		Circle commonEndpoint;
		Circle otherPoint1;
		Circle otherPoint2;
		if (c1.locationEquals(l.c1)) {
			commonEndpoint = c1;
			otherPoint1 = c2;
			otherPoint2 = l.c2;
		} else if (c1.locationEquals(l.c2)) {
			commonEndpoint = c1;
			otherPoint1 = c2;
			otherPoint2 = l.c1;
		} else if (c2.locationEquals(l.c1)) {
			commonEndpoint = c2;
			otherPoint1 = c1;
			otherPoint2 = c2;
		} else if (c2.locationEquals(l.c2)) {
			commonEndpoint = c2;
			otherPoint1 = c1;
			otherPoint2 = l.c1;
		} else {
			throw new IllegalArgumentException("Can't measure angle between " + this + " and " + l
					+ " because they don't share an endpoint");
		}

		Vector v = new Vector(otherPoint1.getX1() - commonEndpoint.getX1(), 
				otherPoint1.getY1() - commonEndpoint.getY1());
		Vector v2 = new Vector(otherPoint2.getX1() - commonEndpoint.getX1(), 
				otherPoint2.getY1() - commonEndpoint.getY1());		
		return Vector.radAngle(v, v2);
	}

	/** Return true iff l intersects this line.
	 * (Return false if they share an endpoint.) */
	public boolean intersects(Line l) {
		return !c1.locationEquals(l.getC1()) && !c1.locationEquals(l.getC2()) &&
				!c2.locationEquals(l.getC1()) && !c2.locationEquals(l.getC2()) &&
				Line2D.linesIntersect(c1.getX1(), c1.getY1(), c2.getX1(), c2.getY1(),
						l.getX1(), l.getY1(), l.getX2(), l.getY2());
	}

	/** Return a String representation of this line */
	@Override
	public String toString() {
		return "(" + c1.getX1() +"," + c1.getY1() + "), (" + 
				c2.getX1() + "," + c2.getY1() + ")";
	}

	/** Paint this line */
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setStroke(new BasicStroke(LINE_THICKNESS));
		Line2D line2d = null;
		if (getX1() < getX2() && getY1() < getY2() || getX2() < getX1() && getY2() < getY1())
			line2d = new Line2D.Double(1, 1, getLineWidth(), getLineHeight());
		else
			line2d = new Line2D.Double(1, getLineHeight(), getLineWidth(), 1);
		g2d.setColor(getColor());
		g2d.draw(line2d);
		g2d.drawString(represents.getMappedName(), represents.getRelativeX(), represents.getRelativeY());
	}


	/** Return the size of the line, as a rectangular bounding box (x2 - x1, y2 - y1). */
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(Math.abs(getX2()-getX1()), Math.abs(getY2()- getY1()));
	}
}
