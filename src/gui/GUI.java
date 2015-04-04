package gui;

import game.*;

import javax.swing.*;

import java.awt.*;

import java.awt.event.*;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;

import org.json.JSONException;

import java.io.File;


/** An instance is the JFrame that shows the game.
 * Class Game and other classes in package game send updates to the gui
 * to keep it up to date on the board state.
 * The user and the manager have no interaction with the GUI class.
 * @author MPatashnik
 *
 */
public class GUI extends JFrame{

	private static final long serialVersionUID = 2941318999657277463L;

	public static final int X_OFFSET = 100;
	public static final int Y_OFFSET = 50;

	public static final int DRAWING_BOARD_WIDTH_MIN = 400;
	public static final int DRAWING_BOARD_HEIGHT_MIN = 400;

	public static final int DRAWING_BOARD_WIDTH;	//Default
	public static final int DRAWING_BOARD_HEIGHT; 	//Default

	public static final int UPDATE_PANEL_HEIGHT = 100;
	public static final int SIDE_PANEL_WIDTH = 300;

	static {
		Dimension s = Toolkit.getDefaultToolkit().getScreenSize();
		DRAWING_BOARD_WIDTH = (int)(s.width) - SIDE_PANEL_WIDTH - 2 * X_OFFSET;
		DRAWING_BOARD_HEIGHT = (int)(s.height * 0.8) - UPDATE_PANEL_HEIGHT - 2 * Y_OFFSET;
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

	private JLabel frameLabel;   //Label that shows the current frame rate. Red if it's been altered
	private JSlider frameSlider; //Slider for the frame rate.

	private long updateTime;	//How quickly the parcel/truck stats should update (ms)
	private static final long DEFAULT_UPDATE_TIME = 200;
	private Thread updateThread;	//Thread that manages updating of stats


	/** A simple extension of the DefaultTableModel that 
	 * doesn't allow editing. Used to show stats on the side panel of the gui.
	 * @author MPatashnik
	 */
	private static class StatsTableModel extends DefaultTableModel {
		private static final long serialVersionUID = 1L;

		/** No cells are editable in a stats table - always returns false */
		@Override
		public boolean isCellEditable(int row, int col) {
			return false;
		}

	}

	/** Constructor: a window to show a game g. */
	public GUI(Game g) {		
		self = this;
		interactable = true;
		updateTime = DEFAULT_UPDATE_TIME;

		setMinimumSize(new Dimension(SIDE_PANEL_WIDTH + DRAWING_BOARD_WIDTH_MIN,
				UPDATE_PANEL_HEIGHT + DRAWING_BOARD_HEIGHT_MIN));
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		createDrawingPanel();

		createSidePanel();

		createBottomPanel();

		createMenuBar();

		pack();
		validate();
		repaint();
		drawingBoardHeight = drawingPanel.getHeight();
		drawingBoardWidth = drawingPanel.getWidth();
		setGame(g);
		initialized = true;
		setLocation(X_OFFSET, Y_OFFSET);
		drawingPanelResized();
		setVisible(true);
	}

	/** Creates and re-adds the drawingPanel to this gui. Used as part of construction */
	private void createDrawingPanel(){
		drawingPanel = new JPanel();
		drawingPanel.setBorder(new LineBorder(new Color(131,155,255)));
		drawingPanel.setBackground(Color.WHITE);

		drawingBoardWidth = DRAWING_BOARD_WIDTH;
		drawingBoardHeight = DRAWING_BOARD_HEIGHT;

		drawingPanel.setPreferredSize(new Dimension(drawingBoardWidth, drawingBoardHeight));
		drawingPanel.setLayout(null);
		drawingPanel.addComponentListener(new ComponentListener() {
			public void componentResized(ComponentEvent e) {
				drawingPanelResized();
			}
			public void componentMoved(ComponentEvent e) {}
			public void componentShown(ComponentEvent e) {}
			public void componentHidden(ComponentEvent e) {}
		});

		getContentPane().add(drawingPanel, BorderLayout.CENTER);
	}

	/** Creates and adds the sidePanel to this gui. Used as part of construction */
	private void createSidePanel(){
		sidePanel = new JPanel();
		sidePanel.setBorder(new LineBorder(new Color(131,155,255)));
		sidePanel.setBackground(new Color(203, 255, 181));
		sidePanel.setPreferredSize(new Dimension(SIDE_PANEL_WIDTH,
				DRAWING_BOARD_HEIGHT + UPDATE_PANEL_HEIGHT));
		sidePanel.setLayout(new BorderLayout());

		getContentPane().add(sidePanel, BorderLayout.EAST);
	}

	/** Creates and adds the bottomPanel to this gui. Used as part of construciton */
	private void createBottomPanel(){
		JPanel bottomPanel = new JPanel();
		bottomPanel.setPreferredSize(new Dimension(DRAWING_BOARD_WIDTH, UPDATE_PANEL_HEIGHT));
		bottomPanel.setBackground(new Color(181,255,252));
		getContentPane().add(bottomPanel, BorderLayout.SOUTH);
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));

		JPanel updatePanel = new JPanel();
		updatePanel.setBackground(bottomPanel.getBackground());
		bottomPanel.add(updatePanel);

		lblUpdate = new JLabel("  ");
		updatePanel.add(lblUpdate);

		JPanel scorePanel = new JPanel();
		scorePanel.setBackground(bottomPanel.getBackground());
		bottomPanel.add(scorePanel);

		JLabel lblScoreTitle = new JLabel("Score:");
		scorePanel.add(lblScoreTitle);

		lblScore = new JLabel("0");
		scorePanel.add(lblScore);

		JLabel lblSpace = new JLabel("\t\t");
		bottomPanel.add(lblSpace);

	}

	/** Creates and adds the menuBar to this GUI. Used during construction */
	private void createMenuBar(){
		menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);

		JMenuItem mntmLoadGame = new JMenuItem("Open...");
		mntmLoadGame.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (game == null || !game.isRunning()) {
					JFileChooser f = new JFileChooser(new File(Game.MAP_DIRECTORY));
					f.setDialogTitle("Select Game to Load");
					f.setDialogType(JFileChooser.OPEN_DIALOG);
					f.setFileSelectionMode(JFileChooser.FILES_ONLY);
					f.showOpenDialog(null);
					File fil = f.getSelectedFile();
					if (fil != null && fil.exists()) {
						try{
							setGame(new Game(game.getManagerClassname(), fil));
						} catch(JSONException j){
							showJSONParseError(j, fil);
						}
					}
				}
			}
		});
		mnFile.add(mntmLoadGame);
		JMenuItem mntmQuit = new JMenuItem("Quit");
		mntmQuit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int returnVal = JOptionPane.showConfirmDialog(null, 
						"Are You Sure You Want to Quit?");
				if (returnVal == JOptionPane.YES_OPTION) {
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
				if (game != null && game.isFinished()) {
					messageClearer.interrupt();
					int frame = game.getFrame();
					if (game.getFile() != null){
						File fil = game.getFile();
						try{
							setGame(new Game(game.getManagerClassname(), fil));
						} catch(JSONException j){
							showJSONParseError(j, fil);
						}
					}else{
						setGame(new Game(game.getManagerClassname(), game.getBoard().seed));
					}
					game.setFrame(frame);
					frameSlider.setValue(frame);
					game.start();
					setUpdateMessage("Game Started");
				}
				else if (game != null && !game.isRunning()) {
					game.start();
					setUpdateMessage("Game Started");
				}
			}
		});
		mnGame.add(mntmStart);

		mntmReset = new JMenuItem("Reset");
		mntmReset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int returnVal = JOptionPane.showConfirmDialog(null, "Are You Sure You Want to Reset?");
				if (returnVal == JOptionPane.YES_OPTION) {
					if(messageClearer != null) messageClearer.interrupt();
					if (game.getFile() != null){
						File fil = game.getFile();
						try{
							setGame(new Game(game.getManagerClassname(), fil));
						} catch(JSONException j){
							showJSONParseError(j, fil);
						}
					}
					else{
						setGame(new Game(game.getManagerClassname(), game.getBoard().seed));
					}
					setUpdateMessage("Game Reset");
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
				while(returnVal == -1 && s != null) {
					try{
						s = JOptionPane.showInputDialog(null, "Enter seed for random game (any long)");
						returnVal = Long.parseLong(s);
					}catch(NumberFormatException e) {
					}
				}
				if (s == null) {
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
	}

	/** Call to show the message for a json parsing error */
	private void showJSONParseError(JSONException j, File fil){
		String msg = "Err with reading board " + fil.getName() + " : " 
				+ j.getMessage() + "\n" +
				"Try pasting the contents of " + fil.getName() + 
				" into a JSON validator online.\n"
				+ "Ex: jsonlint.com";
		JOptionPane.showMessageDialog(self, msg);
	}

	/** Resize the drawing panel.
	 *  Called internally when the drawing panel is resized */
	private void drawingPanelResized() {
		if (!initialized) return;

		Dimension newSize = drawingPanel.getSize();
		double heightRatio = (double)newSize.height / (double)drawingBoardHeight;
		double widthRatio = (double)newSize.width / (double)drawingBoardWidth;

		for (Node n : game.getBoard().getNodes()) {
			Circle c = n.getCircle();
			n.updateGUILocation((int)Math.round((c.getX1() * widthRatio)), 
					(int)Math.round((c.getY1() * heightRatio)));
		}

		drawingBoardWidth = newSize.width;
		drawingBoardHeight = newSize.height;
	}

	/** Create and add to the gui a checkbox with text s for edge paint style.
	 * Returns a reference to the created checkbox. */
	private JRadioButtonMenuItem addEdgeStyleCheckbox(String s, final Line.ColorPolicy k,
			final JMenu mnGUI, ButtonGroup edgeStyleGroup) {
		JRadioButtonMenuItem r = new JRadioButtonMenuItem(s);
		r.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent event) {
				if (event.getStateChange() == ItemEvent.SELECTED) {
					Line.setColorPolicy(k);
					if (game != null && game.getBoard() != null) {
						for (Edge ed : game.getBoard().getEdges()) {
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

	/** Draw all elements of the game in the drawingPanel. Called as part of
	 * GUI construction and whenever a new game is loaded. */
	private void drawMap() {
		//Put nodes on map
		for (Node n : game.getBoard().getNodes()) {
			Circle c = n.getCircle();
			//Remove and re-add from drawing panel
			drawingPanel.remove(c);
			drawingPanel.add(c);
		}

		//Draw the edges on the map
		for (Edge r : game.getBoard().getEdges()) {
			Line l = r.getLine();
			l.setC1(r.getExits()[0].getCircle());
			l.setC2(r.getExits()[1].getCircle());
			l.setBounds(drawingPanel.getBounds());
			l.updateToColorPolicy();
			drawingPanel.remove(l);
			drawingPanel.add(l);
		}

		//Set Locations the parcels on the map
		for (Parcel p : game.getBoard().getParcels()) {
			p.getCircle().setX1(p.getLocation().getCircle().getX1());
			p.getCircle().setY1(p.getLocation().getCircle().getY1());
			drawingPanel.remove(p.getCircle());
			drawingPanel.add(p.getCircle());
		}

		//Draw the trucks on the map
		for (Truck t : game.getBoard().getTrucks()) {
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
		for (Node n : game.getBoard().getNodes()) {
			drawingPanel.setComponentZOrder(n.getCircle(), z);
			z++;
		}
		for (Parcel p : game.getBoard().getParcels()) {
			drawingPanel.setComponentZOrder(p.getCircle(), z);
			z++;
		}
		for (Edge e : game.getBoard().getEdges()) {
			drawingPanel.setComponentZOrder(e.getLine(), z);
			z++;
		}
		for (Truck t : game.getBoard().getTrucks()) {
			drawingPanel.setComponentZOrder(t.getCircle(), z);
			z++;
		}

		repaint();
	}

	/** Set the game to Game g and redraw the map. */
	public void setGame(Game g) {
		if (game != null) game.kill();
		drawingPanel.removeAll();
		game = g;
		game.setGUI(this);
		game.getBoard().updateMinMaxLength();
		drawMap();

		Dimension newSize = drawingPanel.getSize();
		double heightRatio = (double)newSize.height / (double)DRAWING_BOARD_HEIGHT;
		double widthRatio = (double)newSize.width / (double)DRAWING_BOARD_WIDTH;

		for (Node n : game.getBoard().getNodes()) {
			Circle c = n.getCircle();
			n.updateGUILocation((int)Math.round((c.getX1() * widthRatio)), 
					(int)Math.round((c.getY1() * heightRatio)));
		}

		updateSidePanel();
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

	/** Update the info panel to the new game that was just loaded. */
	private void updateSidePanel() {
		sidePanel.removeAll();

		JLabel gameLabel = null;
		if (game.getFile() != null)
			gameLabel = new JLabel("Game from File:" + game.getFile().getName());
		else if (game.getSeed() != -1)
			gameLabel = new JLabel("Game from Seed: " +  game.getSeed());
		else
			gameLabel = new JLabel("Custom Game");
		gameLabel.setFont(Font.decode("asdf-14"));
		sidePanel.add(gameLabel, BorderLayout.NORTH);

		StatsTableModel basicModel = new StatsTableModel();
		statsTable = new JTable(basicModel);		
		basicModel.addColumn("Info");
		basicModel.addColumn("Value");
		basicModel.addRow(new Object[]{"Cities",game.getBoard().getNodesSize()});
		basicModel.addRow(new Object[]{"Highways",game.getBoard().getEdgesSize()});
		basicModel.addRow(new Object[]{"Trucks",game.getBoard().getTrucks().size()});
		basicModel.addRow(new Object[]{"Parcels",game.getBoard().getParcels().size()});
		basicModel.addRow(new Object[]{"Wait Cost", game.getBoard().getWaitCost()});
		basicModel.addRow(new Object[]{"Pickup Cost", game.getBoard().getPickupCost()});
		basicModel.addRow(new Object[]{"Dropoff Cost", game.getBoard().getDropoffCost()});
		basicModel.addRow(new Object[]{"Parcel Payoff", game.getBoard().getPayoff()});
		basicModel.addRow(new Object[]{"On Color Multiplier", game.getBoard().getOnColorMultiplier()});
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
		sidePanel.add(innerPanel, BorderLayout.CENTER);
		statsTable.setFont(Font.decode("asdf-14")); //System default font with size 14
		statsTable.setBackground(sidePanel.getBackground());
		statsTable.setEnabled(false);
		statsTable.setShowGrid(false);
		statsTable.setSelectionBackground(statsTable.getBackground());
		statsTable.setSelectionForeground(statsTable.getForeground());
		statsTable.setRowHeight(18);
		statsTable.getColumn(statsTable.getColumnName(0)).setPreferredWidth(160);

		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
		sidePanel.add(bottomPanel, BorderLayout.SOUTH);

		//Frame Panel
		JPanel innerPanel3 = new JPanel();
		innerPanel3.setLayout(new BoxLayout(innerPanel3, BoxLayout.X_AXIS));
		innerPanel3.add(new JLabel(margin));
		frameLabel = new JLabel("Frame: " + fixNumber(game.getFrame(), 4, "") + "ms  ");
		frameLabel.setFont(Font.decode("asdf-14")); //System default font with size 14
		innerPanel3.add(frameLabel);
		frameSlider = new JSlider();
		frameSlider.setMajorTickSpacing(1);
		frameSlider.setMaximum(2000);
		frameSlider.setMinimum(1);
		frameSlider.setValue((int)game.getFrame());
		frameSlider.setToolTipText("Frame rate (ms) for Truck movement");
		frameSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				int f = ((JSlider)e.getSource()).getValue();
				game.setFrame(f);
				frameLabel.setText("Frame: " + fixNumber(f, 4, "") + "ms  ");
				if(game.isFrameAltered()){
					frameLabel.setForeground(Color.RED);
				}
			}
		});		
		if(game.isFrameAltered()){
			frameLabel.setForeground(Color.RED);
		}
		innerPanel3.add(frameSlider);
		innerPanel3.add(new JLabel(margin));
		innerPanel3.setBackground(sidePanel.getBackground());
		bottomPanel.add(innerPanel3);


		//Update Panel
		JPanel innerPanel2 = new JPanel();
		innerPanel2.setLayout(new BoxLayout(innerPanel2, BoxLayout.X_AXIS));
		innerPanel2.add(new JLabel(margin));
		final JLabel sliderLabel = new JLabel("Update Time: " + 
				fixNumber((int)updateTime, 4, "") + "ms  ");
		sliderLabel.setFont(Font.decode("asdf-14")); //System default font with size 14
		innerPanel2.add(sliderLabel);
		JSlider updateSlider = new JSlider();
		updateSlider.setMajorTickSpacing(25);
		updateSlider.setMaximum(2000);
		updateSlider.setMinimum(25);
		updateSlider.setValue((int)updateTime);
		updateSlider.setToolTipText("Update timer for Parcel and Truck stats. " +
				"Left for faster update, right for slower");
		updateSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				updateTime = ((JSlider)e.getSource()).getValue();
				sliderLabel.setText("Update Time: " + fixNumber((int)updateTime, 4, "") + "ms ");
			}
		});		
		innerPanel2.add(updateSlider);
		innerPanel2.add(new JLabel(margin));
		innerPanel2.setBackground(sidePanel.getBackground());
		bottomPanel.add(innerPanel2);

		if (updateThread != null) updateThread.interrupt();

		updateThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep(updateTime);
						updateParcelAndTruckStats();
					} catch (InterruptedException e) {
						return; //Terminates this thread upon interruption
					}
				}
			}
		});
		updateParcelAndTruckStats();
	}

	/** Recursively pad zeroes on left such that the resulting string has digits characters */
	private static String fixNumber(int x, int digits, String s){
		int a = 0;
		if(x == 1){
			a = 1;
		} else if(x > 1){
			a = (int)Math.ceil(Math.log10(x));
		}
		if(digits == a) return s + x;
		return fixNumber(x, digits-1, s + "0");
	}

	/** Remove the given parcel from the gui - called internally by game
	 * when a parcel is successfully delivered. Puts the event on the AWT
	 * event thread to correctly handle multi-threaded parcel removal.
	 * Students - Do Not Call
	 */
	public void removeParcel(final Parcel p){
		try {
			SwingUtilities.invokeAndWait(new Runnable(){
				@Override
				public void run(){
					getDrawingPanel().remove(p.getCircle());
				}
			});
		} catch (InterruptedException e) {
			//Do nothing - interrupted because the game was reset, thus no handling necessary
		} catch(Exception e){
			e.printStackTrace(); //Other exception - print trace
		}
	}

	/** Toggle the interactability. When this isn't uninteractable,
	 * doesn't allow user to provide any input
	 */
	public void toggleInteractable() {
		interactable = !interactable;
		menuBar.setEnabled(interactable);
	}

	/** Return the panel on which the map is drawn. */
	public JPanel getDrawingPanel() {
		return drawingPanel;
	}

	/** Update the gui to reflect the game's running state.
	 * Called internally by game. */
	public void updateRunning() {
		boolean running = game.isRunning();
		if (running) {
			mntmReset.setEnabled(true);
			updateThread.start();
			if(game.isFrameAltered()){
				frameLabel.setForeground(Color.RED);
			}
		}
		else {
			mntmReset.setEnabled(game.isFinished());
			updateParcelAndTruckStats();
			updateThread.interrupt();
		}
	}

	/** Update the GUI to show the newScore. */
	public void updateScore(int newScore) {
		lblScore.setText( "" + newScore);
	}

	/** Update the GUI to show the new parcel stats and Truck stats. */
	public void updateParcelAndTruckStats() {
		StatsTableModel m = (StatsTableModel)statsTable.getModel();
		int[] parcelStats = game.parcelStats();
		for (int i = 0; i < parcelStats.length; i++) {
			m.setValueAt(parcelStats[i], FIRST_PARCEL_ROW + i, 1);
		}
		int[] truckStats = game.truckStats();
		for (int i = 0; i < truckStats.length; i++) {
			m.setValueAt(truckStats[i], FIRST_TRUCK_ROW + i, 1);
		}	
	}

	/** Return the current update message shown on the GUI. */
	public String getUpdateMessage() {
		return lblUpdate.getText();
	}

	/** Amount of time to wait after posting an update message to delete it (in ms) */
	private static final int MESSAGE_DELETE_TIME = 3000; 

	/** The timer thread to clear the update message after a few seconds */
	private Thread messageClearer;

	/** Update the GUI to show newUpdate as an update message and
	 * start a timer thread to delete the message after a few seconds. */
	public void setUpdateMessage(String newUpdate) {
		lblUpdate.setText(newUpdate);
		Runnable r = new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(MESSAGE_DELETE_TIME);
					setUpdateMessage("  ");
				} catch (InterruptedException e) {}
			}
		};
		if (messageClearer != null && messageClearer.isAlive()) {
			messageClearer.interrupt();
		}
		messageClearer = new Thread(r);
		messageClearer.start();
	}

	/** Dispose of this gui and interrupt the messageClearer thread so that
	 * it doesn't persist. */
	@Override
	public void dispose() {
		updateThread.interrupt();
		messageClearer.interrupt();
		super.dispose();
	}
}
