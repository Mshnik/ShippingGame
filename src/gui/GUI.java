package gui;

import game.*;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JLabel;

import java.awt.Color;
import javax.swing.border.LineBorder;
import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JMenuItem;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;

import javax.swing.BoxLayout;
import java.awt.SystemColor;
import javax.swing.JRadioButtonMenuItem;
import java.awt.Font;

/** The GUI Class creates the JFrame that shows the game.
 * The user and the manager have no interaction with the GUI class.
 * @author MPatashnik
 *
 */
public class GUI extends JFrame{


	/***/
	private static final long serialVersionUID = 2941318999657277463L;

	static final int NODE_BUFFER_SIZE = Circle.DEFAULT_DIAMETER*3;

	static final int MAX_NEIGHBOR_DISTANCE = Circle.DEFAULT_DIAMETER * 5;

	/** The color for menu items when they are not active */
	public static final Color INACTIVE_COLOR = Color.GRAY;

	/** The color for menu items when they are active */
	public static final Color ACTIVE_COLOR = Color.BLACK;

	public static final Dimension MAIN_WINDOW_SIZE = new Dimension(1000, 800);

	private GUI self;
	private Game game;

	private JPanel drawingPanel;

	private JLabel lblUpdate;
	private JLabel lblScore;
	private JMenuBar menuBar;
	private JMenuItem mntmReset;

	/** GUI constructor. Creates a window to show a game */
	public GUI(Game g) {
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
						mntmReset.setEnabled(true);
					}
				}
			}
		});
		mnFile.add(mntmLoadGame);
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

		JMenuItem mntmStart = new JMenuItem("Start");
		mntmStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(game != null && !game.isRunning())
					game.start();
			}
		});
		mnGame.add(mntmStart);

		mntmReset = new JMenuItem("Reset");
		mntmReset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int returnVal = JOptionPane.showConfirmDialog(null, "Are You Sure You Want to Reset?");
				if(returnVal == JOptionPane.YES_OPTION){
					Game oldGame =  game;
					if(oldGame.getFile() != null)
						game = new Game(oldGame.getManagerClassname(), oldGame.getFile());
					else{
						game = Game.randomGame(oldGame.getSeed());
						game.setManager(oldGame.getManagerClassname());
					}
					game.setGUI(self);
					oldGame.kill();
					drawingPanel.removeAll();
					drawMap();
					setUpdateMessage("Game Reset");
					updateScore(game.getScoreValue());
				}
			}
		});
//		if(g.getFile() == null && g.getSeed() == -1){
//			mntmReset.setEnabled(false);
//		}
		mnGame.add(mntmReset);

		JMenuItem mntmRandom = new JMenuItem("New Random Game...");
		mntmRandom.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				long returnVal = -1;
				String s = "";
				while(returnVal == -1 && s != null){
					try{
						s = JOptionPane.showInputDialog(null, "Enter seed for random game (any long)");
						returnVal = Long.parseLong(s);
					}catch(NumberFormatException e){
						System.out.println(s);
					}
				}
				if(s == null){
					return;
				}
				System.out.println("Generating game with seed " + returnVal);
				Game oldGame =  game;
				game = Game.randomGame(returnVal);
				game.setManager(oldGame.getManagerClassname());
				game.setGUI(self);
				oldGame.kill();
				drawingPanel.removeAll();
				drawMap();
				setUpdateMessage("Game Reset");
				updateScore(game.getScoreValue());
				mntmReset.setEnabled(true);
				
			}
		});
		mnGame.add(mntmRandom);

		JMenuItem mntmPrintJSON = new JMenuItem("Print Game JSON");
		mntmPrintJSON.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println(self.game.toJSONString());
			}
		});
		mnGame.add(mntmPrintJSON);

		JMenu mnGUI = new JMenu("GUI");
		menuBar.add(mnGUI);
		JMenuItem mntmRepaint = new JMenuItem("Repaint");
		mntmRepaint.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				drawingPanel.repaint();
			}
		});
		mnGUI.add(mntmRepaint);

		JLabel lblEdgeColoring = new JLabel(" Edge Coloring");
		lblEdgeColoring.setFont(new Font("Lucida Grande", Font.PLAIN, 14));
		mnGUI.add(lblEdgeColoring);

		ButtonGroup edgeStyleGroup = new ButtonGroup();

		JRadioButtonMenuItem d = addEdgeStyleCheckbox("Default", Line.ColorPolicy.DEFAULT, mnGUI, edgeStyleGroup);
		d.setSelected(true);
		addEdgeStyleCheckbox("Highlight Travel", Line.ColorPolicy.HIGHLIGHT_TRAVEL, mnGUI, edgeStyleGroup);
		addEdgeStyleCheckbox("Gradient", Line.ColorPolicy.DISTANCE_GRADIENT, mnGUI, edgeStyleGroup);

		setVisible(true);
		pack();
		repaint();
		
		setGame(g);
	}

	/** Creates and adds to the gui a checkbox with the given text for edge paint style.
	 * Returns a reference to the created checkbox */
	private JRadioButtonMenuItem addEdgeStyleCheckbox(String s, final Line.ColorPolicy k, final JMenu mnGUI, ButtonGroup edgeStyleGroup){
		JRadioButtonMenuItem r = new JRadioButtonMenuItem(s);
		r.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent event) {
				if (event.getStateChange() == ItemEvent.SELECTED) {
					Line.setColorPolicy(k);
					if(game != null && game.getMap() != null){
						for(Edge ed : game.getMap().getEdges()){
							ed.getLine().updateToColorPolicy();
						}
					}
					drawingPanel.repaint();
				}
			}
		});
		edgeStyleGroup.add(r);
		mnGUI.add(r);
		return r;
	}

	/** Draws all elements of the game on the threads. Used when the game is started */
	private void drawMap(){
		//Put nodes on map
		for(Node n : game.getMap().getNodes()){
			Circle c = n.getCircle();
			//Remove, re-add from drawing panel
			drawingPanel.remove(c);
			drawingPanel.add(c);
		}

		//Draw the edges on the map
		for(Edge r : game.getMap().getEdges()){
			Line l = r.getLine();
			l.setC1(r.getExits()[0].getCircle());
			l.setC2(r.getExits()[1].getCircle());
			l.setBounds(drawingPanel.getBounds());
			l.updateToColorPolicy();
			drawingPanel.remove(l);
			drawingPanel.add(l);
		}

		//Set Locations the parcels on the map
		for(Parcel p : game.getParcels()){
			p.getCircle().setX1(p.getLocation().getCircle().getX1());
			p.getCircle().setY1(p.getLocation().getCircle().getY1());
			drawingPanel.remove(p.getCircle());
			drawingPanel.add(p.getCircle());
		}

		//Draw the trucks on the map
		for(Truck t : game.getTrucks()){
			Circle c = t.getCircle();
			c.setBounds(drawingPanel.getBounds());
			c.setX1(t.getLocation().getCircle().getX1());
			c.setY1(t.getLocation().getCircle().getY1());
			drawingPanel.remove(c);
			drawingPanel.add(c);
		}

		//Fix the z-ordering of elements on the panel
		//Higher z painted first -> lower z paint over higher z
		int z = 0;
		for(Node n : game.getMap().getNodes()){
			drawingPanel.setComponentZOrder(n.getCircle(), z);
			z++;
		}
		for(Parcel p : game.getParcels()){
			drawingPanel.setComponentZOrder(p.getCircle(), z);
			z++;
		}
		for(Edge e : game.getMap().getEdges()){
			drawingPanel.setComponentZOrder(e.getLine(), z);
			z++;
		}
		for(Truck t : game.getTrucks()){
			drawingPanel.setComponentZOrder(t.getCircle(), z);
			z++;
		}

		repaint();
	}

	/** Sets the game to Game g */
	private void setGame(Game g){
		game = g;
		game.setGUI(this);
		drawMap();
		game.getMap().updateMinMaxLength();
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

	/** The timer thread to clear the update message after a few seconds */
	private static Thread messageClearer;

	/** Updates the GUI to show the given String as an update message.
	 * Also starts a timer thread to delete the message after a few seconds. */
	public void setUpdateMessage(String newUpdate){
		lblUpdate.setText(newUpdate);
		Runnable r = new Runnable(){
			@Override
			public void run() {
				try {
					Thread.sleep(MESSAGE_DELETE_TIME);
					setUpdateMessage("  ");
				} catch (InterruptedException e) {}
			}
		};
		if(messageClearer != null && messageClearer.isAlive()){
			messageClearer.interrupt();
		}
		messageClearer = new Thread(r);
		messageClearer.start();
	}
}
