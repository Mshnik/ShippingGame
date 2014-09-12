package gui;
import game.Edge;
import game.Game;
import game.Node;
import game.Parcel;
import game.Truck;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;

import javax.swing.JLabel;

import java.awt.Color;
import javax.swing.border.LineBorder;
import javax.swing.JFileChooser;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JMenuItem;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import javax.swing.BoxLayout;
import java.awt.SystemColor;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

/** The GUI Class creates the JFrame that shows the game.
 * The user and the manager have no interaction with the GUI class.
 * @author MPatashnik
 *
 */
public class GUI extends JFrame{


	/***/
	private static final long serialVersionUID = 2941318999657277463L;

	private static final int NODE_BUFFER_SIZE = Circle.DEFAULT_DIAMETER*3;

	private static final int MAX_NEIGHBOR_DISTANCE = Circle.DEFAULT_DIAMETER * 5;

	/** The color for menu items when they are not active */
	public static final Color INACTIVE_COLOR = Color.GRAY;

	/** The color for menu items when they are active */
	public static final Color ACTIVE_COLOR = Color.BLACK;

	private static final Dimension MAIN_WINDOW_SIZE = new Dimension(1000, 800);

	private GUI self;
	private Game game;

	private JPanel drawingPanel;

	private boolean editMode;

	private JLabel lblUpdate;
	private JLabel lblScore;
	private JLabel editLbl;
	private JMenuBar menuBar;

	/** GUI constructor. Creates a window to show a game */
	protected GUI() {
		self = this;

		editMode = false;

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

		menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);

		JMenuItem mntmLoadGame = new JMenuItem("Open...");
		mntmLoadGame.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(game == null || !game.isRunning()){
					JFileChooser f = new JFileChooser(new File(Game.MAP_DIRECTORY));
					f.setDialogTitle("Select Game to Load");
					f.setDialogType(JFileChooser.OPEN_DIALOG);
					f.setFileSelectionMode(JFileChooser.FILES_ONLY);
					f.showOpenDialog(null);
					File fil = f.getSelectedFile();
					if(fil != null && fil.exists()){
						Game oldGame =  game;
						game = new Game(oldGame.getManagerClassname(), fil);
						game.setGUI(self);
						oldGame.getManager().setGame(game);
						oldGame.kill();
						drawingPanel.removeAll();
						drawMap();
					}
				}
			}
		});
		mnFile.add(mntmLoadGame);

		JMenuItem mntmStart = new JMenuItem("Start");
		mntmStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(game != null && !game.isRunning() && ! editMode)
					game.start();
			}
		});
		mnFile.add(mntmStart);

		JMenuItem mntmReset = new JMenuItem("Reset");
		mntmReset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int returnVal = JOptionPane.showConfirmDialog(null, "Are You Sure You Want to Reset?");
				if(returnVal == JOptionPane.YES_OPTION){
					Game oldGame =  game;
					game = new Game(oldGame.getManagerClassname(), oldGame.getFile());
					game.setGUI(self);
					oldGame.kill();
					drawingPanel.removeAll();
					drawMap();
					setUpdateMessage("Game Reset");
					updateScore(game.getScoreValue());
				}
			}
		});
		mnFile.add(mntmReset);

		JMenuItem mntmQuit = new JMenuItem("Quit");
		mntmQuit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int returnVal = JOptionPane.showConfirmDialog(null, "Are You Sure You Want to Quit?");
				if(returnVal == JOptionPane.YES_OPTION){
					System.exit(0);
				}
			}
		});
		mnFile.add(mntmQuit);

		JMenu mnGame = new JMenu("Game");
		menuBar.add(mnGame);

		JMenuItem mntmNew = new JMenuItem("New");
		mntmNew.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int returnVal = JOptionPane.YES_OPTION;
				if(game != null){
					returnVal = JOptionPane.showConfirmDialog(null, "Are You Sure You Want to Create a New Game?");
				}
				if(returnVal == JOptionPane.YES_OPTION){
					if(game != null){
						Game oldGame =  game;
						game = new Game(oldGame.getManagerClassname());
						game.setGUI(self);
						oldGame.getManager().setGame(game);
						oldGame.kill();
					}
					drawingPanel.removeAll();
					drawMap();
				}
			}
		});
		mnGame.add(mntmNew);

		JCheckBoxMenuItem chckbxmntmShowEditToolbar = new JCheckBoxMenuItem("Show Edit Toolbar");
		chckbxmntmShowEditToolbar.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if(game != null && ! game.isRunning()){
					JCheckBoxMenuItem source = (JCheckBoxMenuItem)e.getSource();
					editMode = source.isSelected();
					if(editMode){
						editLbl.setText("Editing");
					} else {
						editLbl.setText("");
					}
				}
			}
		});
		mnGame.add(chckbxmntmShowEditToolbar);

		JMenuItem mntmSaveMap = new JMenuItem("Save");
		mntmSaveMap.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(game != null && !game.isRunning()){
					String mapName = JOptionPane.showInputDialog("Enter New Map Name");
					if(mapName != null && !mapName.equals("")){
						try {
							game.writeGame(mapName);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		});
		mnGame.add(mntmSaveMap);

		JMenuItem mntmPrintJSON = new JMenuItem("Print Game JSON");
		mntmPrintJSON.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println(self.game.toJSONString());
			}
		});
		mnGame.add(mntmPrintJSON);

		editLbl = new JLabel("");
		editLbl.setForeground(Color.RED);
		menuBar.add(editLbl);

		setVisible(true);
		pack();
		repaint();
	}

	/** GUI Constructor. Creates a window to show game g */
	public GUI(Game g){
		this();
		setGame(g);
	}

	class MapUserData{
		Point loc;
		double neighborsPlaced;

		MapUserData(){
			loc = null;
			neighborsPlaced = 0;
		}
	}

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
			n.setUserData(new MapUserData());
		}

		//Nodes that have successfully been put on map
		LinkedList<Node> toPlace = new LinkedList<Node>();
		HashSet<Node> placed = new HashSet<Node>();
		toPlace.add(game.getMap().getNodes().iterator().next()); //Make an arbitrary node the first to place.
		boolean first = true;
		while(! toPlace.isEmpty()){
			Node n = toPlace.poll();
			if(! placed.contains(n)){
				MapUserData nData = (MapUserData)n.getUserData();
				Circle c = n.getCircle();
				if(first){
					c.setX1(maxX/2 + NODE_BUFFER_SIZE);
					c.setY1(maxY/2 + NODE_BUFFER_SIZE);
					nData.loc = new Point(c.getX1(), c.getY1());
					first = false;
				}
				else{
					Node neighbor = null;
					for(Node possibleNeighbor : placed){
						if(possibleNeighbor.isConnectedTo(n))
							neighbor = possibleNeighbor;
					}
					if(neighbor == null){
						throw new RuntimeException("Disjoint graph?");
					}
					else{
						MapUserData neighborData = (MapUserData)neighbor.getUserData();
						boolean flag = false;
						int loopCount = 0;
						do{
							flag = false;
							double rot = (loopCount / 4) * 0.25;
							c.setX1(neighborData.loc.x + (int)(Math.cos(Math.PI/2.0 * (neighborData.neighborsPlaced + rot)) * MAX_NEIGHBOR_DISTANCE));
							c.setY1(neighborData.loc.y + (int)(Math.sin(Math.PI/2.0 * (neighborData.neighborsPlaced + rot)) * MAX_NEIGHBOR_DISTANCE));
							nData.loc = new Point(c.getX1(), c.getY1());
							for(Node each : placed){
								if(each.getCircle().getX1() == c.getX1() && each.getCircle().getY1() == c.getY1()){
									neighborData.neighborsPlaced++;
									flag = true;
									break;
								}

							}
							loopCount++;
						}while(flag);
						neighborData.neighborsPlaced++;
					}
				}
				//c.setBounds(drawingPanel.getBounds());
				drawingPanel.remove(c);
				drawingPanel.add(c);
				placed.add(n);
				for(Edge e : n.getExits()){
					if(! placed.contains(e.getOther(n)))
						toPlace.add(e.getOther(n));
				}
			}
		}

		//Extend nodes for distance and buffer area
		boolean flag = true;
		while(flag){
			flag = false;
			for(Node n : game.getMap().getNodes()){
				for(Node n2 : game.getMap().getNodes()){
					Circle c = n.getCircle();
					Circle c2 = n2.getCircle();
					if(!c.equals(c2) && (c.getDistance(c2) < Circle.BUFFER_RADUIS)){
						c.setX1((int)(Math.random()*maxX) + NODE_BUFFER_SIZE);
						c.setY1((int)(Math.random()*maxY) + NODE_BUFFER_SIZE);
						flag = true;
					}
				}
			}
		}

		for(Edge r : game.getMap().getEdges()){
			Line l = r.getLine();
			l.setC1(r.getExits()[0].getCircle());
			l.setC2(r.getExits()[1].getCircle());
			l.setBounds(drawingPanel.getBounds());
			l.updateToColorPolicy();
			drawingPanel.remove(l);
			drawingPanel.add(l);
		}

		//Shift map to move it to center screen
		int shiftX = 0;
		int shiftY = 0;

		for(Node n : game.getMap().getNodes()){
			if(n.getCircle().getX1() < NODE_BUFFER_SIZE){
				shiftX = NODE_BUFFER_SIZE - n.getCircle().getX1();
			}
			if(n.getCircle().getX1() > maxX - NODE_BUFFER_SIZE){
				shiftX = (maxX - NODE_BUFFER_SIZE) - n.getCircle().getX1();
			}
			if(n.getCircle().getY1() < NODE_BUFFER_SIZE){
				shiftY = NODE_BUFFER_SIZE - n.getCircle().getY1();
			}
			if(n.getCircle().getY1() > maxY - NODE_BUFFER_SIZE){
				shiftY = (maxY - NODE_BUFFER_SIZE) - n.getCircle().getY1();
			}
		}

		for(Node n : game.getMap().getNodes()){
			n.getCircle().setX1(n.getCircle().getX1() + shiftX);
			n.getCircle().setY1(n.getCircle().getY1() + shiftY);
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
			//p.getCircle().setBounds(drawingPanel.getBounds());
			drawingPanel.remove(p.getCircle());
			drawingPanel.add(p.getCircle());
			repaint();
		}


		for(Truck t : game.getTrucks()){
			Circle c = t.getCircle();
			c.setBounds(drawingPanel.getBounds());
			boolean unset = true;
			while(unset){
				try {
					c.setX1(t.getLocation().getCircle().getX1());
					c.setY1(t.getLocation().getCircle().getY1());
					unset = false;
				} catch (InterruptedException e) {
					try {
						Thread.sleep(50);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
			}
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

	/** Updates the gui to reflect the game's running state. Called internally by game. */
	public void updateRunning(){
		boolean running = game.isRunning();
		if(running){
			//TODO
		}
		else{
			//TODO
		}
	}

	/** Updates the GUI to show the newScore */
	public void updateScore(int newScore){
		lblScore.setText( "" + newScore);
	}

	/** Returns the current update message shown on the GUI */
	public String getUpdateMessage(){
		return lblUpdate.getText();
	}

	/** Amount of time to wait after an update message is posted to delete it (in ms) */
	private static final int MESSAGE_DELETE_TIME = 3000; 
	
	/** Updates the GUI to show the given String as an update message.
	 * Also starts a timer thread to delete the message after a few seconds. */
	public void setUpdateMessage(String newUpdate){
		lblUpdate.setText(newUpdate);
		Runnable r = new Runnable(){
			@Override
			public void run() {
				try {
					Thread.sleep(MESSAGE_DELETE_TIME);
				} catch (InterruptedException e) {}
				setUpdateMessage("  ");
			}
		};
		Thread t = new Thread(r);
		t.start();
	}
}
