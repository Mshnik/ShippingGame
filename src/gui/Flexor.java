package gui;

import game.*;

import java.util.HashSet;

import javax.swing.JPanel;
/** Force-directed graph layout algorithm.
 * Takes in a graph (with the nodes having no cartesian location)
 * And attempts to place them in a way that is relatively untangled.
 * 
 * See also: http://cs.brown.edu/~rt/gdhandbook/chapters/force-directed.pdf
 * @author MPatashnik
 */
public class Flexor {

	/** Parameters for the force-directed graph. See above link for explanation thereof. */
	private static final double c1 = 3;
	private static final double c2 = 30; //Repurposed a bit, not exactly as in algorithm. Now a conversion from graph dist to edge length
	private static final double c3 = 1000;
	private static final double c4 = 0.2;

	/** Guard against 0 distance values */
	private static final double MIN_DIST = 1;
	
	private static final int REPETITIONS = 100;

	public static void flexNodes(JPanel drawingPanel, HashSet<Node> nodes, 
								int minX, int minY, int maxX, int maxY){
		//Assign all initial locations randomly
		for(Node n : nodes){
			n.updateGUILocation((int)(Math.random() * maxX),(int)(Math.random() * maxY)) ;
			//Remove, re-add from drawing panel
			drawingPanel.remove(n.getCircle());
			drawingPanel.add(n.getCircle());
		}

		for(int i = 0; i < REPETITIONS; i++){
			for(Node n : nodes){
				addForce(drawingPanel, n, nodes, minX, minY, maxX, maxY);
			}
		}
		
		drawingPanel.notifyAll();
	}

	private static void addForce(JPanel panel, Node n, HashSet<Node> otherNodes, int minX, int minY, int maxX, int maxY){
		Vector v = new Vector();
		for(Node n2 : otherNodes){
			if(n != n2){
				//Vector from this node to other node, of unit length
				Vector a = n2.getCircle().getVectorTo(n.getCircle()).unit();
				double dist = Math.max(MIN_DIST,n.getCircle().getDistance(n2.getCircle()));

				//If connected, calculate spring force
				if(n.isConnectedTo(n2)){
					double graphDist = n.getConnect(n2).getLength();
					double force = c1 * Math.log(graphDist * c2 /dist);
					a.mult(force);
					//System.out.println("force " + force);
					v.addVector(a);
				}
				//Otherwise, add uniform repulsion force
				else{
					double repulsion = c3/Math.pow(Math.max(MIN_DIST, dist),2);
					a.mult(repulsion);
					//System.out.println("repulsion " + repulsion);
					v.addVector(a);
				}
			}
		}
		
		//Multiply the resulting vector by the scaling coefficient
		v.mult(c4);

		int x = Math.max(minX, Math.min(maxX, (int)(n.getCircle().getX1() + v.getX())));
		int y = Math.max(minY, Math.min(maxY, (int)(n.getCircle().getY1() + v.getY())));
		
		//Apply the vector v to n's circle
		n.updateGUILocation(x, y);
		
		//Repaint to see the changes
		panel.repaint();
	}
}
