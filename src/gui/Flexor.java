package gui;

import game.*;

import java.util.HashSet;

import javax.swing.JPanel;
/**
 * 
 * 
 * See also: http://cs.brown.edu/~rt/gdhandbook/chapters/force-directed.pdf
 * @author MPatashnik
 */
public class Flexor {

	private static final double c1 = 2;
	private static final double c2 = 1;
	private static final double c3 = 1000;
	private static final double c4 = 0.1;

	private static final int REPETITIONS = 200;

	public static void flexNodes(JPanel drawingPanel, HashSet<Node> nodes, int maxX, int maxY){
		//Assign all initial locations randomly
		for(Node n : nodes){
			Circle c = n.getCircle();
			c.setX1((int)(Math.random() * maxX));
			c.setY1((int)(Math.random() * maxY));

			//Remove from drawing panel
			drawingPanel.remove(c);
		}

		for(int i = 0; i < REPETITIONS; i++){
			for(Node n : nodes){
				addForce(n, nodes, maxX, maxY);
			}
		}

		//Re-add to drawing panel
		for(Node n : nodes){
			drawingPanel.add(n.getCircle());
		}
	}

	private static void addForce(Node n, HashSet<Node> otherNodes, int maxX, int maxY){
		Vector v = new Vector();
		for(Node n2 : otherNodes){
			if(n != n2){
				//Vector from this node to other node, of unit length
				Vector a = n.getCircle().getVectorTo(n2.getCircle()).unit();

				//If connected, calculate spring force
				if(n.isConnectedTo(n2)){
					double dist = n.getConnect(n2).getLength() / 10.0;
					double force = -c1 * Math.log10(dist/c2);
					a.mult(force);
					System.out.println("force " + force);
					v.addVector(a);
				}
				//Otherwise, add uniform repulsion force
				else{
					double dist = n.getCircle().getDistance(n2.getCircle());
					double repulsion = c3/Math.pow(dist,2);
					a.mult(repulsion);
					System.out.println("repulsion " + repulsion);
					v.addVector(a);
				}
			}
		}
		
		//Multiply the resulting vector by the scaling coefficient
		v.mult(c4);

		int x = Math.max(15, Math.min(maxX, (int)(n.getCircle().getX1() + v.getX())));
		int y = Math.max(15, Math.min(maxY, (int)(n.getCircle().getY1() + v.getY())));
		
		//Apply the vector v to n's circle
		n.getCircle().setX1(x);
		n.getCircle().setY1(y);
	}
}
