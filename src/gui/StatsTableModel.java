package gui;

import javax.swing.table.DefaultTableModel;

/** A simple extension of the DefaultTableModel that 
 * doesn't allow editing. Used to show stats on the side panel of the gui
 * @author MPatashnik
 *
 */
public class StatsTableModel extends DefaultTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/** No cells are editable in a stats table - always returns false */
	@Override
	public boolean isCellEditable(int row, int col){
		return false;
	}
	
}
