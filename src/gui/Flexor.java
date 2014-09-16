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
	private static final double c1 = 1;
	private static final double c2 = 100; //Repurposed a bit, not exactly as in algorithm. Now a conversion from graph dist to edge length
	private static final double c3 = 1000;
	private static final double c4 = 0.8;

	/** Guard against too small distance values */
	private static final double MIN_DIST = 100;

	private static final int REPETITIONS = 250;

	public static void flexNodes(JPanel drawingPanel, HashSet<Node> nodes, 
			int minX, int minY, int maxX, int maxY){
		randomizeNodes(nodes, maxX, maxY, drawingPanel);

		for(int i = 0; i < REPETITIONS; i++){
			for(Node n : nodes){
				addForce(drawingPanel, n, nodes, minX, minY, maxX, maxY);
			}
			
			if(i == REPETITIONS/2)
				fixEdgeOverlap(nodes);
		}

		drawingPanel.notifyAll();
	}

	/** Assigns locations of all nodes randomly */
	private static void randomizeNodes(HashSet<Node> nodes, int maxX, int maxY, JPanel drawingPanel){
		for(Node n : nodes){
			n.updateGUILocation((int)(Math.random() * maxX),(int)(Math.random() * maxY)) ;
			//Remove, re-add from drawing panel
			drawingPanel.remove(n.getCircle());
			drawingPanel.add(n.getCircle());
		}
	}

	/** Tries to find connected edges that have overlapping other edges and swaps their positions */
	private static void fixEdgeOverlap(HashSet<Node> nodes){
		for(Node n : nodes){
			HashSet<Edge> nExits = n.getExits();
			for(Edge e : nExits){
				Node n2 = e.getOther(n);
				HashSet<Edge> n2Exits = n2.getExits();

				//Check to see if n and n2 each have a different exit that overlap.
				for(Edge e3 : nExits){
					if(e3 != e){
						for(Edge e4 : n2Exits){
							if(e3.getLine().intersects(e4.getLine()))
								n.getCircle().switchLocation(n2.getCircle());
						}
					}
				}

			}
		}
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
					double force = c1 * Math.log(Math.pow(graphDist,0.5) * c2 /dist);
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
