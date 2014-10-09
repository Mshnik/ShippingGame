package gui;

import game.*;

import java.util.HashMap;
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
	private static final double c3 = 10;
	private static final double c4 = 1;

	/** Guard against too small distance values */
	private static final double MIN_DIST = 10;

	private static final int REPETITIONS = 100;

	protected static void flexNodes(JPanel drawingPanel, HashSet<Node> nodes, 
			int minX, int minY, int maxX, int maxY){
		//Build hashmap of node -> location
		HashMap<Node, Vector> loc = new HashMap<>();
		for(Node n : nodes){
			loc.put(n, new Vector(n.getCircle().getX1(), n.getCircle().getY1()));
		}
		
		randomizeNodes(loc, maxX, maxY);

		for(int i = 0; i < REPETITIONS; i++){
			for(Node n : nodes){
				addForce(n, loc, minX, minY, maxX, maxY);
			}
			
			
			if(i == REPETITIONS/2){
				updateNodes(loc, drawingPanel);
				fixEdgeOverlap(loc);
			}
		}

		updateNodes(loc, drawingPanel);
		drawingPanel.notifyAll();
	}

	/** Assigns locations of all nodes randomly */
	private static void randomizeNodes(HashMap<Node, Vector> nodes, int maxX, int maxY){
		for(Node n : nodes.keySet()){
			nodes.put(n, new Vector((Math.random() * maxX),(Math.random() * maxY)));
		}
	}
	
	/** Update the nodes with the calculated positions */
	private static void updateNodes(HashMap<Node, Vector> nodes, JPanel drawingPanel){
		for(Node n : nodes.keySet()){
			Circle c = n.getCircle();
			Vector v = nodes.get(n);
			c.setX1((int)v.getX());
			c.setY1((int)v.getY());
			//Remove, re-add from drawing panel
			drawingPanel.remove(c);
			drawingPanel.add(c);
		}
	}

	/** Tries to find connected edges that have overlapping other edges and swaps their positions */
	private static void fixEdgeOverlap(HashMap<Node, Vector> nodes){
		for(Node n : nodes.keySet()){
			HashSet<Edge> nExits = n.getExits();
			for(Edge e : nExits){
				Node n2 = e.getOther(n);
				HashSet<Edge> n2Exits = n2.getExits();

				//Check to see if n and n2 each have a different exit that overlap.
				for(Edge e3 : nExits){
					if(e3 != e){
						for(Edge e4 : n2Exits){
							if(e3.getLine().intersects(e4.getLine())){
								n.getCircle().switchLocation(n2.getCircle());
								Vector v = nodes.get(n);
								nodes.put(n, nodes.get(n2));
								nodes.put(n2, v);
							}
						}
					}
				}

			}
		}
	}

	private static void addForce(Node n, HashMap<Node, Vector> otherNodes, double minX, double minY, double maxX, double maxY){
		Vector v = new Vector();
		Vector nVec = otherNodes.get(n);
		for(Node n2 : otherNodes.keySet()){
			if(n != n2){
				Vector n2Vec = otherNodes.get(n2);
				//Vector from this node to other node, of unit length
				Vector a = n2Vec.to(nVec).unit();
				double dist = Math.max(MIN_DIST,nVec.distance(n2Vec));

				//If connected, calculate spring force
				if(n.isConnectedTo(n2)){
					double graphDist = n.getConnect(n2).length;
					double force = c1 * Math.log(Math.pow(graphDist,0.5) * c2 /dist);
					a.mult(force);
					//System.out.println("force " + force);
					v.addVector(a);
				}
				//Otherwise, add uniform repulsion force
				else{
					double repulsion = c3/Math.pow(dist,2);
					a.mult(repulsion);
					v.addVector(a);
				}
			}


		}

		//Multiply the resulting vector by the scaling coefficient
		v.mult(c4);
		
		double x = Math.max(minX, Math.min(maxX, (nVec.getX() + v.getX())));
		double y = Math.max(minY, Math.min(maxY, (nVec.getY() + v.getY())));

		//Apply the vector v to n's circle
		otherNodes.put(n, new Vector(x,y));
	}
}
