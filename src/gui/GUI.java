package gui;

import game.*;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JLabel;

import java.awt.Color;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JMenuItem;
import javax.swing.JSlider;
import javax.swing.JTable;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;

import javax.swing.BoxLayout;
import java.awt.SystemColor;
import javax.swing.JRadioButtonMenuItem;

import java.awt.Font;

/** The GUI Class creates the JFrame that shows the game.
 * The Game class and other classes in game package send updates to the gui
 * to keep it up to date on the board state.
 * The user and the manager have no interaction with the GUI class.
 * @author MPatashnik
 *
 */
public class GUI extends JFrame{

	private static final long serialVersionUID = 2941318999657277463L;

	public static final int DRAWING_BOARD_WIDTH = 1000;	//Default
	public static final int DRAWING_BOARD_HEIGHT = 500; //Default
	
	public static final int UPDATE_PANEL_HEIGHT = 100;
	public static final int SIDE_PANEL_WIDTH = 300;
	
	//Figure out how to get screen size
	static{
//		DRAWING_BOARD_WIDTH = s.get_width() - SIDE_PANEL_WIDTH;
//		DRAWING_BOARD_HEIGHT = s.get_height() - UPDATE_PANEL_HEIGHT;
	}
	
	private int drawingBoardWidth;	//Most recent value of width
	private int drawingBoardHeight; //Most recent value of height
		
	private GUI self;			//A reference to this, for use in anonymous inner classes
	private Game game;			//The game this gui draws
	private boolean interactable;	//True if the user can do input, false otherwise
	private boolean initialized;	//True once the initial construction process is done, false until then
	
	private JPanel drawingPanel; //The main panel on which the board is drawn
	private JPanel sidePanel;	 // The info panel located on the right of the board.

	private JLabel lblUpdate;    //The label that shows the game update string
	private JLabel lblScore;     //The label that paints scores
	
	private JTable statsTable;	//Table on the side panel that holds the stats about the game 
	
	private JMenuBar menuBar;    //The menu bar at the top of the gui
	private JMenuItem mntmReset; //The button that resets the game
	
	private long updateTime;	//How quickly the parcel/truck stats should update (ms)
	private static final long DEFAULT_UPDATE_TIME = 200;
	private Thread updateThread;	//Thread that manages updating of stats

	/** GUI constructor. Creates a window to show a game {@code g} */
	public GUI(Game g) {
		self = this;
		interactable = true;
		updateTime = DEFAULT_UPDATE_TIME;

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		drawingPanel = new JPanel();
		drawingPanel.setBorder(new LineBorder(Color.BLUE));
		drawingPanel.setBackground(Color.WHITE);
		
		drawingBoardWidth = DRAWING_BOARD_WIDTH;
		drawingBoardHeight = DRAWING_BOARD_HEIGHT;

		drawingPanel.setPreferredSize(new Dimension(drawingBoardWidth, drawingBoardHeight));
		drawingPanel.setLayout(null);
		drawingPanel.addComponentListener(new ComponentListener(){
			public void componentResized(ComponentEvent e) {
				drawingPanelResized();
			}
			public void componentMoved(ComponentEvent e) {}
			public void componentShown(ComponentEvent e) {}
			public void componentHidden(ComponentEvent e) {}
		});
		
		getContentPane().add(drawingPanel, BorderLayout.CENTER);
		
		sidePanel = new JPanel();
		sidePanel.setBorder(new LineBorder(Color.BLACK));
		sidePanel.setBackground(new Color(190, 230, 150));
		sidePanel.setPreferredSize(new Dimension(SIDE_PANEL_WIDTH, DRAWING_BOARD_HEIGHT + UPDATE_PANEL_HEIGHT));
		sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
		
		getContentPane().add(sidePanel, BorderLayout.EAST);
		
		JPanel bottomPanel = new JPanel();
		bottomPanel.setPreferredSize(new Dimension(DRAWING_BOARD_WIDTH, UPDATE_PANEL_HEIGHT));
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
						setGame(new Game(game.getManagerClassname(), fil));
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
					if(game.getFile() != null)
						setGame(new Game(game.getManagerClassname(), game.getFile()));
					else{
						setGame(new Game(game.getManagerClassname(), game.getBoard().seed));
					}
				}
			}
		});
		mntmReset.setEnabled(false); //Reset button unenabled until game starts.
		mnGame.add(mntmReset);

		JMenuItem mntmRandom = new JMenuItem("New Random Map...");
		mntmRandom.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				long returnVal = -1;
				String s = "";
				while(returnVal == -1 && s != null){
					try{
						s = JOptionPane.showInputDialog(null, "Enter seed for random game (any long)");
						returnVal = Long.parseLong(s);
					}catch(NumberFormatException e){
					}
				}
				if(s == null){
					return;
				}
				setGame(new Game(game.getManagerClassname(), returnVal));
				
			}
		});
		mnGame.add(mntmRandom);

		JMenuItem mntmPrintJSON = new JMenuItem("Print Game JSON");
		mntmPrintJSON.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println(self.game.getBoard().toJSONString());
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
		
		pack();
		validate();
		repaint();
		setGame(g);
		initialized = true;
		drawingPanelResized();
		setVisible(true);
	}
	
	/** Called internally when the drawing panel is resized */
	private void drawingPanelResized(){
		if(! initialized) return;
		
		Dimension newSize = drawingPanel.getSize();
		double heightRatio = (double)newSize.height / (double)drawingBoardHeight;
		double widthRatio = (double)newSize.width / (double)drawingBoardWidth;
		
		for(Node n : game.getBoard().getNodes()){
			Circle c = n.getCircle();
			n.updateGUILocation((int)Math.round((c.getX1() * widthRatio)), 
								(int)Math.round((c.getY1() * heightRatio)));
		}

		drawingBoardWidth = newSize.width;
		drawingBoardHeight = newSize.height;
	}

	/** Creates and adds to the gui a checkbox with the given text for edge paint style.
	 * Returns a reference to the created checkbox */
	private JRadioButtonMenuItem addEdgeStyleCheckbox(String s, final Line.ColorPolicy k, final JMenu mnGUI, ButtonGroup edgeStyleGroup){
		JRadioButtonMenuItem r = new JRadioButtonMenuItem(s);
		r.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent event) {
				if (event.getStateChange() == ItemEvent.SELECTED) {
					Line.setColorPolicy(k);
					if(game != null && game.getBoard() != null){
						for(Edge ed : game.getBoard().getEdges()){
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

	/** Draws all elements of the game in the drawingPanel. Called as part of
	 * GUI construction, and whenever a new game is loaded */
	private void drawMap(){
		//Put nodes on map
		for(Node n : game.getBoard().getNodes()){
			Circle c = n.getCircle();
			//Remove, re-add from drawing panel
			drawingPanel.remove(c);
			drawingPanel.add(c);
		}

		//Draw the edges on the map
		for(Edge r : game.getBoard().getEdges()){
			Line l = r.getLine();
			l.setC1(r.getExits()[0].getCircle());
			l.setC2(r.getExits()[1].getCircle());
			l.setBounds(drawingPanel.getBounds());
			l.updateToColorPolicy();
			drawingPanel.remove(l);
			drawingPanel.add(l);
		}

		//Set Locations the parcels on the map
		for(Parcel p : game.getBoard().getParcels()){
			p.getCircle().setX1(p.getLocation().getCircle().getX1());
			p.getCircle().setY1(p.getLocation().getCircle().getY1());
			drawingPanel.remove(p.getCircle());
			drawingPanel.add(p.getCircle());
		}

		//Draw the trucks on the map
		for(Truck t : game.getBoard().getTrucks()){
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
		for(Node n : game.getBoard().getNodes()){
			drawingPanel.setComponentZOrder(n.getCircle(), z);
			z++;
		}
		for(Parcel p : game.getBoard().getParcels()){
			drawingPanel.setComponentZOrder(p.getCircle(), z);
			z++;
		}
		for(Edge e : game.getBoard().getEdges()){
			drawingPanel.setComponentZOrder(e.getLine(), z);
			z++;
		}
		for(Truck t : game.getBoard().getTrucks()){
			drawingPanel.setComponentZOrder(t.getCircle(), z);
			z++;
		}

		repaint();
	}

	/** Sets the game to Game {@code g} and redraws the map */
	public void setGame(Game g){
		if(game != null) game.kill();
		drawingPanel.removeAll();
		game = g;
		game.setGUI(this);
		game.getBoard().updateMinMaxLength();
		drawMap();
		updateSidePanel();
		pack();
		validate();
		repaint();
	}
	
	private static final String margin = "    ";
	
	/** Row in the stats table that corresponds to parcels in cities.
	 * Parcels on trucks is in FIRST_PARCEL_ROW + 1,
	 * Delivered parcels is in FIRST_PARCEL_ROW + 2
	 */
	private static final int FIRST_PARCEL_ROW = 10;
	
	/** Row in the stats table that corresponds to trucks that are waiting.
	 * Parcels on trucks is in FIRST_PARCEL_ROW + 1,
	 * Delivered parcels is in FIRST_PARCEL_ROW + 2
	 */
	private static final int FIRST_TRUCK_ROW = 14;
	
	/** Updates the info panel to the new game that was just loaded */
	private void updateSidePanel(){
		sidePanel.removeAll();
		sidePanel.add(new JLabel("  ")); //Line of space at top
		if(game.getFile() != null)
			sidePanel.add(new JLabel("Game from File: " + game.getFile().getName()));
		else if(game.getSeed() != -1)
			sidePanel.add(new JLabel("Game from Seed: " + game.getSeed()));
		else
			sidePanel.add(new JLabel("Custom Game"));
		sidePanel.add(new JLabel("  "));
				
		StatsTableModel basicModel = new StatsTableModel();
		statsTable = new JTable(basicModel);		
		basicModel.addColumn("Info");
		basicModel.addColumn("Value");
		basicModel.addRow(new Object[]{"Cities",game.getBoard().getNodesSize()});
		basicModel.addRow(new Object[]{"Highways",game.getBoard().getEdgesSize()});
		basicModel.addRow(new Object[]{"Trucks",game.getBoard().getTrucks().size()});
		basicModel.addRow(new Object[]{"Parcels",game.getBoard().getParcels().size()});
		basicModel.addRow(new Object[]{"Wait Cost", game.getBoard().WAIT_COST});
		basicModel.addRow(new Object[]{"Pickup Cost", game.getBoard().PICKUP_COST});
		basicModel.addRow(new Object[]{"Dropoff Cost", game.getBoard().DROPOFF_COST});
		basicModel.addRow(new Object[]{"Parcel Payoff", game.getBoard().PAYOFF});
		basicModel.addRow(new Object[]{"On Color Multiplier", game.getBoard().ON_COLOR_MULTIPLIER});
		basicModel.addRow(new Object[]{"","  "});
		basicModel.addRow(new Object[]{"Parcels in Cities",""});
		basicModel.addRow(new Object[]{"Parcels on Trucks",""});
		basicModel.addRow(new Object[]{"Parcels Delivered",""});
		basicModel.addRow(new Object[]{"","  "});
		basicModel.addRow(new Object[]{"Trucks Waiting",""});
		basicModel.addRow(new Object[]{"Trucks Traveling",""});
		basicModel.addRow(new Object[]{"Trucks com w/ Manager",""});
		
		//Set properties of table
		JPanel innerPanel = new JPanel();
		innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.X_AXIS));
		innerPanel.add(new JLabel(margin));
		innerPanel.add(statsTable);
		innerPanel.add(new JLabel(margin));
		innerPanel.setBackground(sidePanel.getBackground());
		sidePanel.add(innerPanel);
		statsTable.setFont(Font.decode("asdf-14")); //System default font with size 14
		statsTable.setBackground(sidePanel.getBackground());
		statsTable.setEnabled(false);
		statsTable.setShowGrid(false);
		statsTable.setSelectionBackground(statsTable.getBackground());
		statsTable.setSelectionForeground(statsTable.getForeground());
		statsTable.setRowHeight(18);
		statsTable.getColumn(statsTable.getColumnName(0)).setPreferredWidth(200);

		JSlider updateSlider = new JSlider();
		updateSlider.setMaximum(1000);
		updateSlider.setMinimum(25);
		updateSlider.setValue((int)updateTime);
		updateSlider.setToolTipText("Update timer for Parcel and Truck stats. Left for faster update, right for slower");
		updateSlider.addChangeListener(new ChangeListener(){
			@Override
			public void stateChanged(ChangeEvent e) {
				updateTime = ((JSlider)e.getSource()).getValue();
			}
		});
		sidePanel.add(updateSlider);
		
		if(updateThread != null) updateThread.interrupt();
		
		updateThread = new Thread(new Runnable(){
			@Override
			public void run(){
				while(true){
					try{
						Thread.sleep(updateTime);
						updateParcelAndTruckStats();
					}catch(InterruptedException e){
						return; //Terminates this thread upon interruption
					}
				}
			}
		});
		updateParcelAndTruckStats();
	}

	/** Sets this gui to the given interactability. When this isn't uninteractable,
	 * doesn't allow user to provide any input
	 */
	public void toggleInteractable(){
		interactable = ! interactable;
		menuBar.setEnabled(interactable);
	}
	
	/** Returns the panel that the map is drawn on */
	public JPanel getDrawingPanel(){
		return drawingPanel;
	}

	/** Updates the gui to reflect the game's running state. Called internally by game. */
	public void updateRunning(){
		boolean running = game.isRunning();
		if(running){
			mntmReset.setEnabled(true);
			updateThread.start();
		}
		else{
			mntmReset.setEnabled(game.isFinished());
		}
	}

	/** Updates the GUI to show the newScore */
	public void updateScore(int newScore){
		lblScore.setText( "" + newScore);
	}
	
	/** Updates the GUI to show the new parcel stats and Truck stats */
	public void updateParcelAndTruckStats(){
		StatsTableModel m = (StatsTableModel)statsTable.getModel();
		int[] parcelStats = game.parcelStats();
		for(int i = 0; i < parcelStats.length; i++){
			m.setValueAt(parcelStats[i], FIRST_PARCEL_ROW + i, 1);
		}
		int[] truckStats = game.truckStats();
		for(int i = 0; i < truckStats.length; i++){
			m.setValueAt(truckStats[i], FIRST_TRUCK_ROW + i, 1);
		}	
	}

	/** Returns the current update message shown on the GUI */
	public String getUpdateMessage(){
		return lblUpdate.getText();
	}

	/** Amount of time to wait after an update message is posted to delete it (in ms) */
	private static final int MESSAGE_DELETE_TIME = 3000; 

	/** The timer thread to clear the update message after a few seconds */
	private Thread messageClearer;

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
	
	/** Disposes of this gui, also interrupts messageClearer thread so that doesn't persist */
	@Override
	public void dispose(){
		updateThread.interrupt();
		messageClearer.interrupt();
		super.dispose();
	}
}
