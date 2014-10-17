package gui;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import game.BoardElement;

/** A circle that can be dragged by the user on the gui */
public class DraggableCircle extends Circle {

	private static final long serialVersionUID = -3983152780751574074L;
	private Point clickPoint; //The point the user clicked within the circle before dragging began
	private int maxX;   //Boundary for dragging on the x
	private int maxY;   //Boundary for dragging on the y
	
	
	/** Constructs a DraggableCircle
	 * @param represents - the game pice that this circle is drawn for
	 * @param x - the starting x coordinate (center point)
	 * @param y - the starting y coordinate (center point)
	 * @param diameter - the diameter of the circle
	 */
	public DraggableCircle(final BoardElement represents, int x, int y, int diameter) {
		super(represents, x, y, diameter);
		
		MouseListener clickListener = new MouseListener(){
			
			/** When this is clicked, store the initial point at which this is clicked */
			@Override
			public void mousePressed(MouseEvent e) {
				maxX = represents.getBoard().game.getGUI().getDrawingPanel().getWidth();
				maxY = represents.getBoard().game.getGUI().getDrawingPanel().getHeight();
				clickPoint = e.getPoint();
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {}

			@Override
			public void mouseReleased(MouseEvent e) {}

			@Override
			public void mouseEntered(MouseEvent e) {}

			@Override
			public void mouseExited(MouseEvent e) {}	
		};
		
		MouseMotionListener motionListener = new MouseMotionListener(){

			/** When this is dragged, perform the translation from the point where this was clicked
			 * to the new dragged point
			 */
			@Override
			public void mouseDragged(MouseEvent e) {
				DraggableCircle c = (DraggableCircle)e.getSource();
				Point p = e.getPoint();
				c.represents.updateGUILocation(
					    Math.min(maxX, Math.max(0, c.getX1() + p.x - clickPoint.x)), 
						Math.min(maxY, Math.max(0, c.getY1() + p.y - clickPoint.y)));
			}

			@Override
			public void mouseMoved(MouseEvent e) {}
			
		};
		
		addMouseListener(clickListener);
		addMouseMotionListener(motionListener);
	}

}
