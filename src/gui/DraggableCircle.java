package gui;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import game.MapElement;

/** A circle that can be dragged by the user on the gui */
public class DraggableCircle extends Circle {

	private static final long serialVersionUID = -3983152780751574074L;

	private MouseListener clickListener; //The listener to mouse press/release
	private Point clickPoint; //The point the user clicked within the circle before dragging began
	private MouseMotionListener motionListener; //The listener for dragging
	
	public DraggableCircle(MapElement represents, int x, int y, int diameter) {
		super(represents, x, y, diameter);
		
		clickListener = new MouseListener(){
			@Override
			public void mouseClicked(MouseEvent e) {}

			@Override
			public void mousePressed(MouseEvent e) {
				clickPoint = e.getPoint();
			}

			@Override
			public void mouseReleased(MouseEvent e) {}

			@Override
			public void mouseEntered(MouseEvent e) {}

			@Override
			public void mouseExited(MouseEvent e) {}	
		};
		
		motionListener = new MouseMotionListener(){

			@Override
			public void mouseDragged(MouseEvent e) {
				DraggableCircle c = (DraggableCircle)e.getSource();
				Point p = e.getPoint();
				c.represents.updateGUILocation(c.getX1() + p.x - clickPoint.x, c.getY1() + p.y - clickPoint.y);
			}

			@Override
			public void mouseMoved(MouseEvent e) {
			}
			
		};
		
		addMouseListener(clickListener);
		addMouseMotionListener(motionListener);
	}

}
