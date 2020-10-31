/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.components.properties.panel;

import java.awt.Dimension;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import javax.swing.JDialog;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import org.opentcs.data.ObjectPropConstants;
import org.opentcs.guing.components.dialogs.DetailsDialogContent;
import org.opentcs.guing.components.dialogs.StandardDetailsDialog;
import org.opentcs.guing.components.properties.type.KeyValueProperty;
import org.opentcs.guing.components.properties.type.KeyValueSetProperty;
import org.opentcs.guing.components.properties.type.Property;
import org.opentcs.guing.model.SystemModel;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * Benutzeroberfl�che zum Bearbeiten einer Menge von Key-Value-Paaren.
 * {
 *
 * @see KeyValueSetProperty}
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class KeyValueSetPropertyEditorPanel
    extends javax.swing.JPanel
    implements DetailsDialogContent {

  /**
   * Das zu bearbeitende Attribut.
   */
  private KeyValueSetProperty fProperty;

  /**
   * Creates a new instance.
   */
  public KeyValueSetPropertyEditorPanel() {
    initComponents();
    setPreferredSize(new Dimension(350, 200));

    itemsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent evt) {
        if (evt.getValueIsAdjusting()) {
          return;
        }

        handleSelectionChanged();
      }
    });
  }

  @Override
  public void setProperty(Property property) {
    fProperty = (KeyValueSetProperty) property;

    DefaultTableModel model = (DefaultTableModel) itemsTable.getModel();

    for (int i = model.getRowCount() - 1; i >= 0; i--) {
      model.removeRow(i);
    }

    Iterator<KeyValueProperty> e = fProperty.getItems().iterator();

    while (e.hasNext()) {
      KeyValueProperty p = e.next();
      String[] row = new String[2];
      row[0] = p.getKey();
      row[1] = p.getValue();
      model.addRow(row);
    }

    sortItems();
    updateView();
  }

  @Override
  public void updateValues() {
    List<KeyValueProperty> items = new LinkedList<>();
    TableModel model = itemsTable.getModel();
    int size = model.getRowCount();

    for (int i = 0; i < size; i++) {
      String key = (String) model.getValueAt(i, 0);
      String value = (String) model.getValueAt(i, 1);
      items.add(new KeyValueProperty(null, key, value));
    }

    fProperty.setItems(items);
  }

  @Override
  public void setSystemModel(SystemModel systemModel) {
    // Do nada.
  }

  @Override
  public String getTitle() {
    return ResourceBundleUtil.getBundle().getString("KeyValueSetPropertyEditorPanel.title");
  }

  @Override
  public Property getProperty() {
    return fProperty;
  }

  /**
   * Liefert das ausgew�hlte KeyValueProperty.
   *
   * @return das ausgew�hlte KeyValueProperty
   */
  private KeyValueProperty getSelectedKeyValueProperty() {
    int i = itemsTable.getSelectedRow();

    if (i == -1) {
      return null;
    }

    String key = (String) itemsTable.getValueAt(i, 0);
    String value = (String) itemsTable.getValueAt(i, 1);

    return new KeyValueProperty(null, key, value);
  }

  /**
   * W�hlt eine bestimmte Tabellezeile (das hei�t ein bestimmtes
   * Schl�ssel-Wert-Paar) aus.
   *
   * @param key der Schl�ssel des auszuw�hlenden Paares
   */
  private void selectItem(String key) {
    for (int i = 0; i < itemsTable.getRowCount(); i++) {
      if (itemsTable.getValueAt(i, 0).equals(key)) {
        itemsTable.getSelectionModel().setSelectionInterval(i, i);
        break;
      }
    }
  }

  /**
   * Sortiert die Schl�ssel-Wert-Paare nach ihrem Schl�ssel.
   */
  private void sortItems() {
    Map<String, String> items = new HashMap<>();

    for (int i = 0; i < itemsTable.getRowCount(); i++) {
      items.put((String) itemsTable.getValueAt(i, 0), (String) itemsTable.getValueAt(i, 1));
    }

    TreeSet<String> sorted = new TreeSet<>(items.keySet());

    int index = 0;
    Iterator<String> i = sorted.iterator();

    while (i.hasNext()) {
      String key = i.next();
      String value = items.get(key);

      itemsTable.setValueAt(key, index, 0);
      itemsTable.setValueAt(value, index, 1);

      index++;
    }
  }

  /**
   * Setzt einen Eintrag in der Tabelle bestehend aus Schl�ssel und Wert. Ist
   * der Schl�ssel bereits vorhanden, wird der zugeh�rige Wert �berschrieben
   * andernfalls wird ein neuer Eintrag erzeugt.
   *
   * @param key der Schl�ssel
   * @param value der Wert
   */
  private void setItem(String key, String value) {
    for (int i = 0; i < itemsTable.getRowCount(); i++) {
      if (itemsTable.getValueAt(i, 0).equals(key)) {
        itemsTable.setValueAt(value, i, 1);
        return;
      }
    }

    DefaultTableModel model = (DefaultTableModel) itemsTable.getModel();
    model.addRow(new Object[] {key, value});
  }

  /**
   * Bearbeitet den ausgew�hlten Eintrag.
   */
  private void edit() {
    KeyValueProperty p = getSelectedKeyValueProperty();

    if (p == null) {
      return;
    }

    JDialog parent = (JDialog) getTopLevelAncestor();
    KeyValuePropertyEditorPanel content = new KeyValuePropertyEditorPanel();
    content.setProperty(p);

    StandardDetailsDialog dialog = new StandardDetailsDialog(parent, true, content);

    dialog.setLocationRelativeTo(parent);
    dialog.setVisible(true);

    if (dialog.getReturnStatus() == StandardDetailsDialog.RET_OK) {
      setItem(p.getKey(), p.getValue());
      sortItems();
      selectItem(p.getKey());

      updateView();
    }
  }

  /**
   * F�gt einen neuen Eintrag hinzu.
   */
  private void add() {
    JDialog parent = (JDialog) getTopLevelAncestor();

    KeyValueProperty p = new KeyValueProperty(null);
    KeyValuePropertyEditorPanel content = new KeyValuePropertyEditorPanel();
    content.setProperty(p);
    StandardDetailsDialog dialog = new StandardDetailsDialog(parent, true, content);
    dialog.setLocationRelativeTo(parent);
    dialog.setVisible(true);

    if (dialog.getReturnStatus() == StandardDetailsDialog.RET_OK) {
      setItem(p.getKey(), p.getValue());
      sortItems();
      selectItem(p.getKey());

      updateView();
    }
  }

  /**
   * Wird aufgerufen, wenn ein anderes Schl�ssel-Wert-Paar ausgew�hlt wurde.
   */
  private void handleSelectionChanged() {
    updateView();
  }

  /**
   * Macht die verschiedenen Schaltfl�chen benutzbar oder unbenutzbar, je
   * nachdem, ob ein Schl�ssel-Wert-Paar ausgew�hlt wurde.
   */
  private void updateView() {
    final TableModel model = itemsTable.getModel();
    boolean selectedAreEditable = true;
    
    for (int selRowIndex : itemsTable.getSelectedRows()) {
      String key = (String) model.getValueAt(selRowIndex, 0);
      // Sonderfall: Dieses Property wird indirekt �ber das Symbol einer Location
      // gesetzt und sollte nicht direkt editierbar sein
      if (key.equals(ObjectPropConstants.LOC_DEFAULT_REPRESENTATION)
          || key.equals(ObjectPropConstants.LOCTYPE_DEFAULT_REPRESENTATION)) {
        selectedAreEditable = false;
        break;
      }
    }

    boolean enableEditing = false;
    boolean enableRemoval = false;
    // Only allow removal of properties if at least one is selected and all of
    // them are editable.
    if (itemsTable.getSelectedRowCount() > 0 && selectedAreEditable) {
      enableRemoval = true;
    }
    // Only allow editing for the selection if exactly one property is selected
    // and it is editable.
    if (itemsTable.getSelectedRowCount() == 1 && selectedAreEditable) {
      enableEditing = true;
    }
    editButton.setEnabled(enableEditing);
    removeButton.setEnabled(enableRemoval);
  }

  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        itemsScrollPane = new javax.swing.JScrollPane();
        itemsTable = new javax.swing.JTable();
        controlPanel = new javax.swing.JPanel();
        addButton = new javax.swing.JButton();
        editButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        itemsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Schl�ssel", "Wert"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        itemsScrollPane.setViewportView(itemsTable);

        add(itemsScrollPane, java.awt.BorderLayout.CENTER);

        controlPanel.setLayout(new java.awt.GridBagLayout());

        addButton.setFont(addButton.getFont());
        addButton.setText("Hinzuf�gen");
        addButton.setOpaque(false);
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        controlPanel.add(addButton, gridBagConstraints);

        editButton.setFont(editButton.getFont());
        editButton.setText("Bearbeiten");
        editButton.setOpaque(false);
        editButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        controlPanel.add(editButton, gridBagConstraints);

        removeButton.setFont(removeButton.getFont());
        removeButton.setText("Entfernen");
        removeButton.setOpaque(false);
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        controlPanel.add(removeButton, gridBagConstraints);

        add(controlPanel, java.awt.BorderLayout.EAST);
    }// </editor-fold>//GEN-END:initComponents

  /**
   * Entfernt das ausgew�hlte Element.
   *
   * @param evt das ausl�sende Ereignis
   */
    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
      int i = itemsTable.getSelectedRow();

      if (i == -1) {
        return;
      }

      DefaultTableModel model = (DefaultTableModel) itemsTable.getModel();
      model.removeRow(i);

      updateView();
    }//GEN-LAST:event_removeButtonActionPerformed

  /**
   * Bearbeitet das ausgew�hlte Element.
   *
   * @param evt das ausl�sende Ereignis
   */
    private void editButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editButtonActionPerformed
      edit();
    }//GEN-LAST:event_editButtonActionPerformed

  /**
   * F�gt ein Element hinzu.
   *
   * @param evt das ausl�sende Ereignis
   */
    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
      add();
    }//GEN-LAST:event_addButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JPanel controlPanel;
    private javax.swing.JButton editButton;
    private javax.swing.JScrollPane itemsScrollPane;
    private javax.swing.JTable itemsTable;
    private javax.swing.JButton removeButton;
    // End of variables declaration//GEN-END:variables
}
