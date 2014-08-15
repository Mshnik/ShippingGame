package gui;
import gameFiles.Edge;
import gameFiles.MapElement;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;

import javax.swing.JPanel;

public class Line  extends JPanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1688624827819736589L;

	public static final int LINE_THICKNESS = 2;
	public static final Color DEFAULT_COLOR = Color.DARK_GRAY;
	public static final Color TRAVELING_COLOR = Color.RED;

	public static final int COLOR_POLICY_DEFAULT = 0;
	public static final int COLOR_POLICY_HIGHLIGHT_TRAVEL = 1;
	public static final int COLOR_POLICY_DISTANCE_GRADIENT = 2;

	private static final Color GRADIENT_SHORT_COLOR = Color.BLACK;
	private static final Color GRADIENT_LONG_COLOR = Color.CYAN;

	private static final int[] ACCEPTABLE_COLOR_POLICIES = {COLOR_POLICY_DEFAULT, COLOR_POLICY_HIGHLIGHT_TRAVEL, 
		COLOR_POLICY_DISTANCE_GRADIENT};

	private static int colorPolicy = COLOR_POLICY_HIGHLIGHT_TRAVEL;

	private Circle c1;
	private Circle c2;

	private Color color;

	private MapElement represents;

	/** Constructor for the Line class. The line is colored black.
	 * @param c1 - the Circle that marks the first end of this line
	 * @param c2 - the Circle that marks the second end of this line
	 * @param represents - the MapElement this Line represents when drawn on the GUI
	 */
	public Line(Circle c1, Circle c2, MapElement represents){
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

	/** Returns the x1 coordinate of this line*/
	public int getX1() {
		return c1.getX1();
	}

	/** Returns the y1 coordinate of this line */
	public int getY1() {
		return c1.getY1();
	}

	/** Returns the x2 coordinate of this line */
	public int getX2() {
		return c2.getX1();
	}


	/** Returns the y2 coordinate of this line */
	public int getY2() {
		return c2.getY1();
	}

	/** Returns a point that is the midpoint of this line */
	public Point getMid(){
		return new Point(getXMid(), getYMid());
	}

	/** Returns the x midpoint of this line */
	public int getXMid(){
		return (c1.getX1() + c2.getX1()) / 2;
	}

	/** Returns the y midpoint of this line */
	public int getYMid(){
		return (c1.getY1() + c2.getY1()) / 2;
	}

	/** Returns the current color of this line, which is determined by the color policy */
	public Color getColor(){
		return color;
	}

	/** Updates the Color according to the color policy
	 * @throws RuntimeException if the colorPolicy is an illegal value*/
	public void updateToColorPolicy() throws RuntimeException{
		switch(colorPolicy){
		case COLOR_POLICY_DEFAULT:
			color = DEFAULT_COLOR;
			break;
		case COLOR_POLICY_HIGHLIGHT_TRAVEL:
			boolean b = false;
			try{
				b = represents.trucksHere() > 0;
			}catch(InterruptedException e){}
			if(b)
				color = TRAVELING_COLOR;
			else
				color = DEFAULT_COLOR;
			break;
		case COLOR_POLICY_DISTANCE_GRADIENT:
			color = getDistGradientColor();
			break;
		default:
			throw new RuntimeException("Invalid Color Policy; cannot update color");
		}

		repaint();
	}

	private Color getDistGradientColor(){
		if(Edge.getMaxLength() == Edge.DEFAULT_MAX_LENGTH && Edge.getMinLength() == Edge.DEFAULT_MIN_LENGTH)
			return DEFAULT_COLOR;
		
		Edge e = (Edge)represents;
		int max = Edge.getMaxLength();
		int min = Edge.getMinLength();
		double v = (double)(e.getLength() - min)/ ((double)(max - min));

		return new Color( (int)( (double)GRADIENT_LONG_COLOR.getRed() * v) + (int)( (double)GRADIENT_SHORT_COLOR.getRed() * (1-v)),
				(int)( (double)GRADIENT_LONG_COLOR.getGreen() * v) + (int)( (double)GRADIENT_SHORT_COLOR.getGreen() * (1-v)),
				(int)( (double)GRADIENT_LONG_COLOR.getBlue() * v) + (int)( (double)GRADIENT_SHORT_COLOR.getBlue() * (1-v)));
	}

	/** Returns the color policy for painting roads. May be one of the following:
	 * 	<br> COLOR_POLICY_DEFAULT - paint all roads the default color
	 * 	<br> COLOR_POLICY_HIGHLIGHT_TRAVEL - highlight the roads that are currently being traveled
	 *  <br> COLOR_POLICY_DISTANCE_GRADIENT - paint roads according to their length value
	 */
	public static int getColorPolicy(){
		return colorPolicy;
	}

	/** Sets the colorPolicy to one of the following:
	 * 	<br> COLOR_POLICY_DEFAULT - paint all roads the default color
	 * 	<br> COLOR_POLICY_HIGHLIGHT_TRAVEL - highlight the roads that are currently being traveled
	 *  <br> COLOR_POLICY_DISTANCE_GRADIENT - paint roads according to their length value
	 *  @throws IllegalArgumentException - if an unrecognized policy is given
	 */
	public static void setColorPolicy(int policy){
		for(int i = 0; i < ACCEPTABLE_COLOR_POLICIES.length; i++)
			if(policy == ACCEPTABLE_COLOR_POLICIES[i]){
				colorPolicy = policy;
				return;
			}

		throw new IllegalArgumentException("Unrecognized ColorPolicy given to Line Class");
	}

	/** Returns the MapElement this object represents */
	protected MapElement getRepresents(){
		return represents;
	}

	private static final int ON_LINE_TOLERANCE = 5;	//Number of pixels of tolerance for a point to be on the line

	/** Returns true if Point p is within ON_LINE_TOLERANCE pixels of this line */
	public boolean isOnLine(Point p){
		double dist = distanceTo(p);
		return dist <= (double)ON_LINE_TOLERANCE;
	}
	
	/** Returns the distance from Point p to this line */
	public double distanceTo(Point p){
		return Line2D.ptLineDist(c1.getX1(), c1.getY1(), c2.getX1(), c2.getY1(), p.getX(), p.getY());
	}

	/** Returns true if Line l intersects this line */
	public boolean intersects(Line l){
		return Line2D.linesIntersect(c1.getX1(), c1.getY1(), c2.getX1(), c2.getY1(), l.getX1(), l.getY1(), l.getX2(), l.getY2());
	}

	@Override
	/** Returns a String representation of this line */
	public String toString(){
		return "(" + c1.getX1() +"," + c1.getY1() + "), (" + c2.getX1() + "," + c2.getY1() + ")";
	}

	@Override
	/**Allows the line object to draw itself */
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

	@Override
	/** Returns the size of the line */
	public Dimension getPreferredSize(){
		return new Dimension(Math.abs(getX2()-getX1()), Math.abs(getY2()- getY1()));

	}
}
