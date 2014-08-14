package gui;
import gameFiles.Edge;
import gameFiles.Game;
import gameFiles.Node;
import gameFiles.Parcel;
import gameFiles.Truck;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JLabel;

import java.awt.Color;
import javax.swing.border.LineBorder;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JMenuItem;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import javax.swing.BoxLayout;
import java.awt.SystemColor;
import javax.swing.JRadioButton;
import javax.swing.JCheckBox;

/** The GUI Class creates the JFrame that shows the game.
 * The user and the manager have no interaction with the GUI class.
 * @author MPatashnik
 *
 */
public class GUI extends JFrame{


	/***/
	private static final long serialVersionUID = 2941318999657277463L;

	private static final int NODE_BUFFER_SIZE = Circle.DEFAULT_DIAMETER*3;

	private static final int MAX_NEIGHBOR_DISTANCE = Circle.DEFAULT_DIAMETER * 15;

	private static final Dimension MAIN_WINDOW_SIZE = new Dimension(1000, 800);

	private GUI self;
	private Game game;

	private JPanel drawingPanel;

	private JLabel lblUpdate;
	private JLabel lblScore;
	private JCheckBox chckBoxPaused;
	private JMenuBar menuBar;
	private ButtonGroup speedGroup;

	/** GUI constructor. Creates a window to show a game */
	protected GUI() {
		self = this;

		setMinimumSize(MAIN_WINDOW_SIZE);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setResizable(false);

		drawingPanel = new JPanel();
		drawingPanel.setBorder(new LineBorder(Color.BLUE));
		drawingPanel.setBackground(Color.WHITE);
		drawingPanel.setLayout(null);

		getContentPane().add(drawingPanel, BorderLayout.CENTER);

		JPanel bottomPanel = new JPanel();
		bottomPanel.setBackground(SystemColor.textHighlight);
		getContentPane().add(bottomPanel, BorderLayout.SOUTH);
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));

		JPanel updatePanel = new JPanel();
		updatePanel.setBackground(SystemColor.textHighlight);
		bottomPanel.add(updatePanel);

		lblUpdate = new JLabel("  ");
		updatePanel.add(lblUpdate);

		JPanel scorePanel = new JPanel();
		scorePanel.setBackground(SystemColor.textHighlight);
		bottomPanel.add(scorePanel);

		JLabel lblScoreTitle = new JLabel("Score:");
		scorePanel.add(lblScoreTitle);

		lblScore = new JLabel("0");
		scorePanel.add(lblScore);

		JLabel lblSpace = new JLabel("\t\t");
		bottomPanel.add(lblSpace);

		JPanel speedPanel = new JPanel();
		speedPanel.setBackground(SystemColor.textHighlight);
		bottomPanel.add(speedPanel);

		chckBoxPaused = new JCheckBox("Paused");
		speedPanel.add(chckBoxPaused);

		JRadioButton rdbtnVerySlow = new JRadioButton(VERY_SLOW_NAME);
		speedPanel.add(rdbtnVerySlow);

		JRadioButton rdbtnSlow = new JRadioButton(SLOW_NAME);
		speedPanel.add(rdbtnSlow);

		JRadioButton rdbtnReg = new JRadioButton(REGULAR_NAME);
		rdbtnReg.setSelected(true);
		speedPanel.add(rdbtnReg);

		JRadioButton rdbtnFast = new JRadioButton(FAST_NAME);
		speedPanel.add(rdbtnFast);

		JRadioButton rdbtnVeryFast = new JRadioButton(VERY_FAST_NAME);
		speedPanel.add(rdbtnVeryFast);

		speedGroup = new ButtonGroup();
		speedGroup.add(rdbtnVerySlow);
		speedGroup.add(rdbtnSlow);
		speedGroup.add(rdbtnReg);
		speedGroup.add(rdbtnFast);
		speedGroup.add(rdbtnVeryFast);

		menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu mnGame = new JMenu("Game");
		menuBar.add(mnGame);

		JMenuItem mntmStart = new JMenuItem("Start");
		mntmStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(game != null && !game.isRunning())
					game.start();
			}
		});
		mnGame.add(mntmStart);

		JMenuItem mntmNew = new JMenuItem("New");
		mntmNew.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int returnVal = JOptionPane.YES_OPTION;
				if(game != null){
					returnVal = JOptionPane.showConfirmDialog(null, "Are You Sure You Want to Start a New Game?");
				}
				if(returnVal == JOptionPane.YES_OPTION){
					if(game != null){
						game.kill();
						drawingPanel.removeAll();
					}

					//					game = GUI.showCreateNewGame();
					//					game.setGUI(self);

					drawMap();
				}

			}
		});
		mnGame.add(mntmNew);

		JMenuItem mntmQuit = new JMenuItem("Quit");
		mntmQuit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int returnVal = JOptionPane.showConfirmDialog(null, "Are You Sure You Want to Quit?");
				if(returnVal == JOptionPane.YES_OPTION){
					System.exit(0);
				}
			}
		});
		mnGame.add(mntmQuit);

		JMenu mnMap = new JMenu("Map");
		menuBar.add(mnMap);

		JMenuItem mntmSaveMap = new JMenuItem("Save Map");
		mntmSaveMap.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(game != null && !game.isRunning()){
					String mapName = JOptionPane.showInputDialog("Enter New Map Name");
					if(mapName != null && !mapName.equals("")){
						try {
							game.writeGame(mapName);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		});
		mnMap.add(mntmSaveMap);

		JMenuItem mntmRearrangeMap = new JMenuItem("Re-Arrange Map");
		mntmRearrangeMap.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(!game.isRunning())
					drawMap();
			}
		});
		mnMap.add(mntmRearrangeMap);

		setVisible(true);
		pack();
		repaint();
	}

	/** GUI Constructor. Creates a window to show game g */
	public GUI(Game g){
		this();
		setGame(g);
	}
	//
	//	private static Game showCreateNewGame(){
	//		
	//	}

	/** Draws all elements of the game on the threads. Used when the game is started */
	private void drawMap(){
		int maxX = drawingPanel.getBounds().width - NODE_BUFFER_SIZE*2;
		int maxY = drawingPanel.getBounds().height - NODE_BUFFER_SIZE*2;

		//Hashmap of user data before this operation. Temporarily comandeers that field.
		HashMap<Node, Object> userData = new HashMap<Node, Object>();
		for(Node n : game.getMap().getNodes()){
			userData.put(n, n.getUserData());
		}

		//Put an integer in the object field. For map purposes.
		for(Node n : game.getMap().getNodes()){
			n.setUserData(new Integer(0));
		}

		//Nodes that have successfully been put on map
		LinkedList<Node> toPlace = new LinkedList<Node>();
		HashSet<Node> placed = new HashSet<Node>();
		toPlace.add(game.getMap().getNodes().iterator().next()); //Make an arbitrary node the first to place.
		boolean first = true;
		//Move the nodes around randomly
		while(! toPlace.isEmpty()){
			Node n = toPlace.poll();
			Circle c = n.getCircle();
			if(first){
				c.setX1(maxX/2 + NODE_BUFFER_SIZE);
				c.setY1(maxY/2 + NODE_BUFFER_SIZE);
				first = false;
			}
			else{
				Node neighbor = null;
				for(Node possibleNeighbor : placed){
					if(possibleNeighbor.isConnectedTo(n))
						neighbor = possibleNeighbor;
				}
				if(neighbor == null){
					c.setX1((int)(Math.random()*maxX) + NODE_BUFFER_SIZE);
					c.setY1((int)(Math.random()*maxY) + NODE_BUFFER_SIZE); 
				}
				else{
					c.setX1((int)(Math.random()*MAX_NEIGHBOR_DISTANCE) - MAX_NEIGHBOR_DISTANCE/2 + neighbor.getCircle().getX1());
					c.setY1((int)(Math.random()*MAX_NEIGHBOR_DISTANCE) - MAX_NEIGHBOR_DISTANCE/2 + neighbor.getCircle().getY1());
				}
			}
			c.setBounds(drawingPanel.getBounds());
			drawingPanel.remove(c);
			drawingPanel.add(c);
			placed.add(n);
			for(Edge e : n.getExits()){
				if(! placed.contains(e.getOther(n)))
					toPlace.add(e.getOther(n));
			}
		}

		//		//Extend nodes for distance and buffer area
		//		boolean flag = true;
		//		while(flag){
		//			flag = false;
		//			for(Node n : game.getMap().getNodes()){
		//				for(Node n2 : game.getMap().getNodes()){
		//					Circle c = n.getCircle();
		//					Circle c2 = n2.getCircle();
		//					if(!c.equals(c2) && (c.getDistance(c2) < Circle.BUFFER_RADUIS)){
		//						c.setX1((int)(Math.random()*maxX) + NODE_BUFFER_SIZE);
		//						c.setY1((int)(Math.random()*maxY) + NODE_BUFFER_SIZE);
		//						flag = true;
		//					}
		//				}
		//			}
		//		}

		for(Edge r : game.getMap().getEdges()){
			Line l = r.getLine();
			l.setC1(r.getExits()[0].getCircle());
			l.setC2(r.getExits()[1].getCircle());
			l.setBounds(drawingPanel.getBounds());
			l.updateToColorPolicy();
			drawingPanel.remove(l);
			drawingPanel.add(l);
		}

		//		//Move lines and nodes to untangle
		//		int attempts = 0;
		//		int maxAttempts = (int)Math.pow(game.getMap().getEdges().size(), 2);
		//		while(game.getMap().isIntersection() && attempts < maxAttempts){
		//			Edge[] intersectingRoads = game.getMap().getAIntersection();
		//
		//			if(intersectingRoads == null)
		//				break;
		//
		//			Edge r = intersectingRoads[0];
		//			Edge r2 = intersectingRoads[1];
		//
		//			Circle c1 = r.getLine().getC1();
		//			Circle c2 = r2.getLine().getC1();
		//
		//			c1.switchLocation(c2);
		//
		//			attempts++;
		//		}

		repaint();

		for(Parcel p : game.getParcels()){
			p.getCircle().setX1(p.getLocation().getCircle().getX1());
			p.getCircle().setY1(p.getLocation().getCircle().getY1());
			p.getCircle().setBounds(drawingPanel.getBounds());
			drawingPanel.remove(p.getCircle());
			drawingPanel.add(p.getCircle());
			repaint();
		}


		for(Truck t : game.getTrucks()){
			t.setGUI(this);
			Circle c = t.getCircle();
			c.setBounds(drawingPanel.getBounds());
			c.setX1(t.getLocation().getCircle().getX1());
			c.setY1(t.getLocation().getCircle().getY1());
			drawingPanel.remove(c);
			drawingPanel.add(c);
		}

		repaint();

		//Reset userData fields to what they were before.
		for(Node n : game.getMap().getNodes()){
			n.setUserData(userData.get(n));
		}
	}

	/** Sets the game to Game g */
	private void setGame(Game g){
		game = g;
		game.setGUI(this);
		drawMap();
		Edge.updateMinMaxLength();
	}

	/** Returns the panel that the map is drawn on */
	public JPanel getDrawingPanel(){
		return drawingPanel;
	}

	/** returns the menuCheckBoxPaused check box */
	public JCheckBox getCheckBoxPaused(){
		return chckBoxPaused;
	}

	/** Returns true if the GUI is currently paused false otherwise */
	public boolean getPaused(){
		return chckBoxPaused.isSelected();
	}

	/** Updates the GUI to show the newScore */
	public void updateScore(int newScore){
		lblScore.setText( "" + newScore);
	}

	/** Returns the current update message shown on the GUI */
	public String getUpdateMessage(){
		return lblUpdate.getText();
	}

	/** Updates the GUI to show the given String as an update message */
	public void setUpdateMessage(String newUpdate){
		lblUpdate.setText(newUpdate);
	}

	protected static final int VERY_SLOW_SPEED = 200;
	private static final String VERY_SLOW_NAME = "Very Slow";
	protected static final int SLOW_SPEED = 80;
	private static final String SLOW_NAME = "Slow";
	protected static final int REGULAR_SPEED = 40;
	private static final String REGULAR_NAME = "Regular";
	protected static final int FAST_SPEED = 10;
	private static final String FAST_NAME = "Fast";
	protected static final int VERY_FAST_SPEED = 1;
	private static final String VERY_FAST_NAME = "Very Fast";

	/** Returns the speed of the game based on the selected radiobutton at the south of the GUI.
	 * @return - either SLOW_SPEED, REGULAR_SPEED, or FAST_SPEED, based on the selected button.
	 * 				returns REGULAR_SPEED if no button is selected.
	 */
	public int getGameSpeed(){
		for(Enumeration<AbstractButton> e = speedGroup.getElements(); e.hasMoreElements(); ){
			AbstractButton button = e.nextElement();
			if(button.isSelected()){
				String t = button.getText();
				if(t.equals(VERY_SLOW_NAME))
					return VERY_SLOW_SPEED;
				if(t.equals(SLOW_NAME))
					return SLOW_SPEED;
				if(t.equals(REGULAR_NAME))
					return REGULAR_SPEED;
				if(t.equals(FAST_NAME))
					return FAST_SPEED;
				if(t.equals(VERY_FAST_NAME))
					return VERY_FAST_SPEED;
			}
		}

		return REGULAR_SPEED;
	}
}
