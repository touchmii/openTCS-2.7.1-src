/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.components.properties.table;

import java.util.LinkedList;
import java.util.List;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import org.opentcs.guing.application.GuiManager.OperationMode;
import org.opentcs.guing.components.properties.event.TableChangeListener;
import org.opentcs.guing.components.properties.event.TableSelectionChangeEvent;
import org.opentcs.guing.components.properties.type.ModelAttribute;

/**
 * Eine Tabelle, in der Attribute dargestellt und ver�ndert werden k�nnen. Sie
 * besteht aus zwei Spalten: die erste enth�lt die Namen der Attribute, die
 * zweite die Werte der Attribute.
 * <p>
 * Die Tabelle ist Teil der {
 *
 * @see PropertiesComponent}. Diese besitzt unterhalb der Tabelle einen Bereich
 * f�r attributspezifische Hilfetexte. PropertiesComponent muss deshalb wissen,
 * welche Tabellenzeile der Benutzer gerade selektiert, um den entsprechenden
 * Hilfetext anzeigen zu k�nnen. Daher registriert sich PropertiesComponent als
 * {
 * @see TableSelectionChangeListener} bei der Tabelle und wird dann �ber jede
 * Ver�nderung informiert.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class AttributesTable
    extends JTable {

  /**
   * Eine Liste von Objekten, die daran interessiert sind, welche Tabellenzeile
   * gerade selektiert ist.
   */
  private final List<TableChangeListener> fTableChangeListeners
      = new LinkedList<>();

  /**
   * Creates a new instance.
   */
  public AttributesTable() {
    super();
    setStyle();
    putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
  }

  /**
   * Konstruktor f�r ein TableModel.
   *
   * @param tableModel
   */
  public AttributesTable(TableModel tableModel) {
    super(tableModel);
    setStyle();
  }

  /**
   * Konfiguriert das Erscheinungsbild der Tabelle.
   */
  protected final void setStyle() {
    setRowHeight(20);
    setCellSelectionEnabled(false);
    getTableHeader().setReorderingAllowed(false);
    setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    ListSelectionModel model = getSelectionModel();
    model.addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) {
          return;
        }

        ListSelectionModel l = (ListSelectionModel) e.getSource();

        if (l.isSelectionEmpty()) {
          fireSelectionChanged(null);
        }
        else {
          int selectedRow = l.getMinSelectionIndex();
          fireSelectionChanged(getModel().getValueAt(selectedRow, 1));
        }
      }
    });
  }

  /**
   * F�gt einen TableSelectionChangeListener hinzu.
   *
   * @param l
   */
  public void addTableChangeListener(TableChangeListener l) {
    fTableChangeListeners.add(l);
  }

  /**
   * Entfernt einen TableSelectionChangeListener.
   *
   * @param l
   */
  public void removeTableChangeListener(TableChangeListener l) {
    fTableChangeListeners.remove(l);
  }

  /**
   * Benachrichtigt alle registrierten TableChangeListener, dass der Benutzer
   * eine Tabellenzeile (und damit ein Attribut) selektiert hat.
   *
   * @param selectedValue
   */
  protected void fireSelectionChanged(Object selectedValue) {
    for (TableChangeListener l : fTableChangeListeners) {
      l.tableSelectionChanged(new TableSelectionChangeEvent(this, selectedValue));
    }
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

  /**
   * Zeigt an, ob die �bergebene Zeile editierbar ist. Dies ist dann der Fall,
   * wenn das Attribut in seiner
   * <code>isEditable()
   * </code> Methode
   * <code>true</code> liefert. Diese Methode wird von CellRenderern benutzt, um
   * nicht ver�nderbare Zeilen anders darzustellen.
   *
   * @param row
   * @return
   */
  public boolean isEditable(int row) {
    AttributesTableModel tableModel = (AttributesTableModel) getModel();
    ModelAttribute attribute = (ModelAttribute) tableModel.getValueAt(row, 1);
    OperationMode operationMode = tableModel.getView().getOperationMode();

    if (operationMode == OperationMode.MODELLING) {
      return attribute.isModellingEditable();
    }

    return attribute.isOperatingEditable();
  }

  @Override
  public void tableChanged(TableModelEvent event) {
    super.tableChanged(event);

    if (fTableChangeListeners != null) {
      for (TableChangeListener listener : fTableChangeListeners) {
        listener.tableModelChanged();
      }
    }
  }
}
