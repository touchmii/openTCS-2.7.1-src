/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.transport;

import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

/**
 * Eine Tabelle f�r Transportauftr�ge und Fahrauftr�ge, die weder das Editieren
 * noch das Selektieren von Zellen erlaubt.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class OrdersTable
		extends JTable {

	/**
	 * Creates a new instance of OrdersTable.
	 *
	 * @param tableModel das Tabellenmodell
	 */
	public OrdersTable(TableModel tableModel) {
		super(tableModel);

		setRowSelectionAllowed(true);
		setFocusable(false);
	}

  @Override
	public boolean isCellEditable(int row, int column) {
		return false;
	}
	
  @Override
	public TableCellEditor getCellEditor(int row, int column) {
		TableModel tableModel = getModel();
		Object value = tableModel.getValueAt(row, column);
		TableCellEditor editor = getDefaultEditor(value.getClass());

		return editor;
	}

  @Override
	public TableCellRenderer getCellRenderer(int row, int column) {
		TableModel tableModel = getModel();
		Object value = tableModel.getValueAt(row, column);
		TableCellRenderer renderer = getDefaultRenderer(value.getClass());
	
		return renderer;
	}
}
