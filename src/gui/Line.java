package gui;
import game.Edge;
import game.Board;
import game.BoardElement;
import game.Vector;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;

import javax.swing.JPanel;

/** The Line class is a graphic class that allows the drawing of lines.
 * Lines use a (c1, c2) coordinate system of where they are located on the board, where
 * c1 and c2 are each circle objects that denote the endpoints of this line.
 * Each circle has (x,y) coordinates, so a Line can be though of as having (x1, y1, x2, y2)
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
	 * <br>Default - all single color
	 * <br>Highlight color - all default color, but highlighted when a truck is traveling it
	 * <br>Distance gradient - shorter edges lighter, longer edges darker
	 * @author MPatashnik
	 *
	 */
	public enum ColorPolicy{
		DEFAULT,
		HIGHLIGHT_TRAVEL,
		DISTANCE_GRADIENT
	}

	private static ColorPolicy colorPolicy = ColorPolicy.DEFAULT; //The color policy to follow

	private Circle c1;  //Endpoint one of this line
	private Circle c2;  //Endpoint two of this line

	private Color color; //The color to draw this line - should stay in sync with the color policy

	private BoardElement represents; //The BoardElement (probably Edge) that this represents

	/** Constructor for the Line class. The line is colored according to the color policy.
	 * @param c1 - the Circle that marks the first end of this line
	 * @param c2 - the Circle that marks the second end of this line
	 * @param represents - the MapElement this Line represents when drawn on the GUI
	 */
	public Line(Circle c1, Circle c2, BoardElement represents){
		this.setC1(c1);
		this.setC2(c2);
		this.represents = represents;
		setOpaque(false);

		updateToColorPolicy();
	}

	/** Returns the circle that is first end of this line */
	public Circle getC1(){
		return c1;
	}

	/** Sets the first end of this line to be at Circle c */
	protected void setC1(Circle c){
		c1 = c;
	}

	/** Returns the circle that is the second end of this line */
	public Circle getC2(){
		return c2;
	}

	/** Sets the second end of this line to be at Circle c */
	protected void setC2(Circle c){
		c2 = c;
	}

	/** Returns the x1 coordinate of this line, getC1().getX1()*/
	public int getX1() {
		return c1.getX1();
	}

	/** Returns the y1 coordinate of this line, getC1().getY1() */
	public int getY1() {
		return c1.getY1();
	}

	/** Returns the x2 coordinate of this line, getC2().getX1() */
	public int getX2() {
		return c2.getX1();
	}

	/** Returns the y2 coordinate of this line, getC2().getY1() */
	public int getY2() {
		return c2.getY1();
	}

	/** Returns a point that is the midpoint of this line */
	public Point getMid(){
		return new Point(getXMid(), getYMid());
	}

	/** Returns the x value of the midpoint of this line */
	public int getXMid(){
		return (c1.getX1() + c2.getX1()) / 2;
	}

	/** Returns the y value of the midpoint of this line */
	public int getYMid(){
		return (c1.getY1() + c2.getY1()) / 2;
	}

	/** Returns the current color of this line, which is determined by the color policy */
	public Color getColor(){
		return color;
	}

	/** Updates the Color according to the color policy */
	public void updateToColorPolicy(){
		switch(colorPolicy){
		case DEFAULT:
			color = DEFAULT_COLOR;
			break;
		case HIGHLIGHT_TRAVEL:
			if(represents.trucksHere() > 0)
				color = TRAVELING_COLOR;
			else
				color = DEFAULT_COLOR;
			break;
		case DISTANCE_GRADIENT:
			color = getDistGradientColor();
			break;
		}

		repaint();
	}

	/** Returns the color for this line using the distance gradient.
	 * Compares the length of the edge this represents to the max and min values of edge length.
	 */
	private Color getDistGradientColor(){
		Board m = represents.getBoard();
		if(m.getMaxLength() == Edge.DEFAULT_MAX_LENGTH || m.getMinLength() == Edge.DEFAULT_MIN_LENGTH)
			return DEFAULT_COLOR;
		
		Edge e = (Edge)represents;
		int max = m.getMaxLength();
		int min = m.getMinLength();
		double v = (double)(e.length - min)/ ((double)(max - min));

		return new Color( (int)( (double)GRADIENT_LONG_COLOR.getRed() * v) + (int)( (double)GRADIENT_SHORT_COLOR.getRed() * (1-v)),
				(int)( (double)GRADIENT_LONG_COLOR.getGreen() * v) + (int)( (double)GRADIENT_SHORT_COLOR.getGreen() * (1-v)),
				(int)( (double)GRADIENT_LONG_COLOR.getBlue() * v) + (int)( (double)GRADIENT_SHORT_COLOR.getBlue() * (1-v)));
	}

	/** Returns the color policy for painting roads.
	 * @see Line.ColorPolicy
	 */
	public static ColorPolicy getColorPolicy(){
		return colorPolicy;
	}

	/** Sets the colorPolicy to one of the colorPolicies.
	 * @see Line.ColorPolicy
	 */
	public static void setColorPolicy(ColorPolicy policy){
		colorPolicy = policy;
	}

	/** Returns the BoardElement this object represents */
	protected BoardElement getRepresents(){
		return represents;
	}

	/** Number of pixels of tolerance for a point to be considered on the line */
	public static final int ON_LINE_TOLERANCE = 20;

	/** Returns true if Point p is within ON_LINE_TOLERANCE pixels of this line */
	public boolean isOnLine(Point p){
		double dist = distanceTo(p);
		return dist <= (double)ON_LINE_TOLERANCE;
	}
	
	/** Returns the distance from Point p to this line */
	public double distanceTo(Point p){
		return Line2D.ptLineDist(c1.getX1(), c1.getY1(), c2.getX1(), c2.getY1(), p.getX(), p.getY());
	}
	
	/** Returns the angle between this line and line l, in radians.
	 * Return is in the range [0 .. PI]
	 */
	public double radAngle(Line l){
		Vector v = new Vector(getX2() - getX1(), getY2() - getY1());
		Vector v2 = new Vector(l.getX2() - l.getX1(), l.getY2() - l.getY1());
		return Vector.radAngle(v, v2);
	}

	/** Returns true if Line l intersects this line. Returns false if they share an endpoint though. */
	public boolean intersects(Line l){
		return c1 != l.getC1() && c2 != l.getC2() && c2 != l.getC1() && c2 != l.getC2() &&
				Line2D.linesIntersect(c1.getX1(), c1.getY1(), c2.getX1(), c2.getY1(), l.getX1(), l.getY1(), l.getX2(), l.getY2());
	}

	/** Returns a String representation of this line */
	@Override
	public String toString(){
		return "(" + c1.getX1() +"," + c1.getY1() + "), (" + c2.getX1() + "," + c2.getY1() + ")";
	}
	
	/** Paints this line */
	@Override
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setStroke(new BasicStroke(LINE_THICKNESS));
		Line2D line2d = new Line2D.Double(getX1(), getY1(), getX2(), getY2());
		g2d.setColor(getColor());
		g2d.draw(line2d);
		g2d.drawString(represents.getMappedName(), represents.getRelativeX() + getX1(), represents.getRelativeY() + getY1());
	}

	
	/** Returns the size of the line, as a rectangular bounding box (x2 - x1, y2 - y1). */
	@Override
	public Dimension getPreferredSize(){
		return new Dimension(Math.abs(getX2()-getX1()), Math.abs(getY2()- getY1()));

	}
}
