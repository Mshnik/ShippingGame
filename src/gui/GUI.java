package gui;

import game.*;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;

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

	static final int NODE_BUFFER_SIZE = Circle.DEFAULT_DIAMETER*3;

	static final int MAX_NEIGHBOR_DISTANCE = Circle.DEFAULT_DIAMETER * 5;

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

	/** Draws all elements of the game on the threads. Used when the game is started */
	private void drawMap(){
		final int maxX = drawingPanel.getBounds().width - NODE_BUFFER_SIZE*2;
		final int maxY = drawingPanel.getBounds().height - NODE_BUFFER_SIZE*2;

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
		
		//Fix the positions of the nodes on the panel using the Force model.
		//See the Flexor class for how this is done.
		//Done in seperate thread so progress can be seen. drawingPanel is notified when this is done.
		new Thread(new Runnable(){
			@Override
			public void run() {
				synchronized(drawingPanel){
					Flexor.flexNodes(drawingPanel, game.getMap().getNodes(), 20, 20, maxX, maxY);		
				}
			}
		}).start();
		
		try {
			synchronized(drawingPanel){
				drawingPanel.wait();
			}
		} catch (InterruptedException e2) {
			e2.printStackTrace();
		}
		
		//Draw the parcels on the map
		for(Parcel p : game.getParcels()){
			p.getCircle().setX1(p.getLocation().getCircle().getX1());
			p.getCircle().setY1(p.getLocation().getCircle().getY1());
			//p.getCircle().setBounds(drawingPanel.getBounds());
			drawingPanel.remove(p.getCircle());
			drawingPanel.add(p.getCircle());
		}

		//Draw the trucks on the map
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
