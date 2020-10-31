/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.transport;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.guing.application.OpenTCSView;
import org.opentcs.guing.components.dialogs.DialogContent;
import org.opentcs.guing.components.dialogs.EditDriveOrderPanel;
import org.opentcs.guing.components.dialogs.StandardContentDialog;
import org.opentcs.guing.model.elements.LocationModel;
import org.opentcs.guing.model.elements.VehicleModel;
import org.opentcs.guing.util.DateUtility;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * Benutzerschnittstelle zur Eingabe eines neuen Transportauftrags.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class CreateTransportOrderPanel
    extends DialogContent {

  /**
   * Die ausgew�hlte Frist.
   */
  private long fSelectedDeadline;
  /**
   * Die anzufahrenden Stationen.
   */
  private final List<LocationModel> fLocationModels;
  /**
   * Die an den Stationen auszuf�hrenden Aktionen.
   */
  private final List<String> fActions;
  /**
   * Die zur Auswahl stehenden Fahrzeuge.
   */
  private final List<VehicleModel> fVehicles;
  /**
   * Der GuiManager.
   */
  private final OpenTCSView fOpenTCSView;
  /**
   * Der Transportauftrag, der als Vorlage dienen soll.
   */
  private TransportOrder fPattern;

  /**
   * Creates new instance..
   *
   * @param openTCSView
   */
  public CreateTransportOrderPanel(OpenTCSView openTCSView) {
    initComponents();
    Object[] columnNames = {ResourceBundleUtil.getBundle().getString("CreateTransportOrderPanel.location"),
                            ResourceBundleUtil.getBundle().getString("CreateTransportOrderPanel.action")};
    DefaultTableModel model = (DefaultTableModel) driveOrdersTable.getModel();
    model.setColumnIdentifiers(columnNames);
    fOpenTCSView = openTCSView;
    fLocationModels = new ArrayList<>();
    fActions = new ArrayList<>();

    driveOrdersTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

      @Override
      public void valueChanged(ListSelectionEvent evt) {
        if (!evt.getValueIsAdjusting()) {
          updateButtons();
        }
      }
    });

    fVehicles = fOpenTCSView.getSystemModel().getVehicleModels();

    Comparator<VehicleModel> c = new Comparator<VehicleModel>() {

      @Override
      public int compare(VehicleModel o1, VehicleModel o2) {
        String s1 = o1.getName().toLowerCase();
        String s2 = o2.getName().toLowerCase();
        return s1.compareTo(s2);
      }
    };

    Collections.sort(fVehicles, c);
  }

  /**
   * Liefert die anzufahrenden Stationen.
   *
   * @return die Stationen
   */
  public List<LocationModel> getLocations() {
    return fLocationModels;
  }

  /**
   * Liefert die auszuf�hrenden Aktionen.
   *
   * @return die Aktionen
   */
  public List<String> getActions() {
    return fActions;
  }

  /**
   * Liefert die ausgew�hlte Frist.
   *
   * @return die Frist
   */
  public long getSelectedDeadline() {
    return fSelectedDeadline;
  }

  /**
   * Liefert das zu verwendende Fahrzeug.
   *
   * @return das Fahrzeug
   */
  public VehicleModel getSelectedVehicle() {
    if (vehicleComboBox.getSelectedIndex() == 0) {
      return null;
    }

    return fVehicles.get(vehicleComboBox.getSelectedIndex() - 1);
  }

  @Override
  public void update() {
    try {
      // Tempor�rer Kalender zum puffern des Datums
      Calendar calDate = new GregorianCalendar();
      SimpleDateFormat f = new SimpleDateFormat("dd.MM.yyyy");
      Date date = f.parse(dateTextField.getText());
      calDate.setTime(date);
      // Tempor�rer Kalender zum puffern der Uhrzeit
      Calendar calTime = new GregorianCalendar();
      f = new SimpleDateFormat("HH:mm");
      Date time = f.parse(timeTextField.getText());
      calTime.setTime(time);
      // Uhrzeit zum Datum "addieren"
      calDate.set(Calendar.HOUR_OF_DAY, calTime.get(Calendar.HOUR_OF_DAY));
      calDate.set(Calendar.MINUTE, calTime.get(Calendar.MINUTE));

      fSelectedDeadline = calDate.getTimeInMillis();

      if (DateUtility.isWithinDaylightSavingTime(fSelectedDeadline)) {
        fSelectedDeadline += 3600 * 1000;
      }
    }
    catch (ParseException e) {
      // XXX Do something here or document why nothing is done.
    }

    fParsingFailed = false;

    if (fLocationModels.isEmpty()) {
      String title = ResourceBundleUtil.getBundle().getString("CreateTransportOrderPanel.inputError");
      String message = ResourceBundleUtil.getBundle().getString("CreateTransportOrderPanel.errorMessage");
      int messageType = JOptionPane.ERROR_MESSAGE;
      JOptionPane.showMessageDialog(this, message, title, messageType);
      fParsingFailed = true;
    }
  }

  @Override
  public void initFields() {
    vehicleComboBox.addItem(ResourceBundleUtil.getBundle().getString("CreateTransportOrderPanel.automatic"));
    Iterator<VehicleModel> e = fVehicles.iterator();

    while (e.hasNext()) {
      VehicleModel v = e.next();
      vehicleComboBox.addItem(v.getName());
    }

    Calendar calendar = GregorianCalendar.getInstance();
    calendar.add(Calendar.HOUR_OF_DAY, 1);
    Date tDeadline = calendar.getTime();

    SimpleDateFormat fDate = new SimpleDateFormat("dd.MM.yyyy");
    SimpleDateFormat fTime = new SimpleDateFormat("HH:mm");
    String sDate = fDate.format(tDeadline);
    String sTime = fTime.format(tDeadline);
    dateTextField.setText(sDate);
    timeTextField.setText(sTime);

    if (fPattern != null) {
      // Frist
      Date deadline = new Date(fPattern.getDeadline());
      sDate = fDate.format(deadline);
      sTime = fTime.format(deadline);
      dateTextField.setText(sDate);
      timeTextField.setText(sTime);

      // Gewolltes Fahrzeug
      if (fPattern.getIntendedVehicle() != null) {
        vehicleComboBox.setSelectedItem(fPattern.getIntendedVehicle().getName());
      }

      // Fahrauftr�ge
      List<DriveOrder> driveOrders = new LinkedList<>();
      driveOrders.addAll(fPattern.getPastDriveOrders());

      if (fPattern.getCurrentDriveOrder() != null) {
        driveOrders.add(fPattern.getCurrentDriveOrder());
      }

      driveOrders.addAll(fPattern.getFutureDriveOrders());

      DefaultTableModel model = (DefaultTableModel) driveOrdersTable.getModel();
      for (DriveOrder o : driveOrders) {
        String location = o.getDestination().getLocation().getName();
        String action = o.getDestination().getOperation();

        String[] row = new String[2];
        row[0] = location;
        row[1] = action;
        model.addRow(row);

        fLocationModels.add(fOpenTCSView.getSystemModel().getLocationModel(location));
        fActions.add(action);
      }
    }

    updateButtons();
  }

  /**
   * Setzt einen Transportauftrag als Vorlage.
   *
   * @param t der Transportauftrag, der als Vorlage, verwendet werden soll
   */
  public void setPattern(TransportOrder t) {
    fPattern = t;
  }

  /**
   * Aktualisiert den Zustand der Schaltfl�chen {enabled | disabled}.
   */
  private void updateButtons() {
    boolean state = driveOrdersTable.getSelectedRow() != -1;

    editButton.setEnabled(state);
    removeButton.setEnabled(state);
    moveUpButton.setEnabled(state);
    moveDownButton.setEnabled(state);

    if (driveOrdersTable.getRowCount() == driveOrdersTable.getSelectedRow() + 1) {
      moveDownButton.setEnabled(false);
    }

    if (driveOrdersTable.getSelectedRow() == 0) {
      moveUpButton.setEnabled(false);
    }
  }

  // CHECKSTYLE:OFF
  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        stationsPanel = new javax.swing.JPanel();
        driveOrdersScrollPane = new javax.swing.JScrollPane();
        driveOrdersTable = new javax.swing.JTable();
        addButton = new javax.swing.JButton();
        editButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        moveUpButton = new javax.swing.JButton();
        moveDownButton = new javax.swing.JButton();
        deadlinePanel = new javax.swing.JPanel();
        dateLabel = new javax.swing.JLabel();
        dateTextField = new javax.swing.JTextField();
        timeLabel = new javax.swing.JLabel();
        timeTextField = new javax.swing.JTextField();
        vehiclePanel = new javax.swing.JPanel();
        vehicleLabel = new javax.swing.JLabel();
        vehicleComboBox = new javax.swing.JComboBox();

        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS));

        stationsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(ResourceBundleUtil.getBundle().getString("CreateTransportOrderPanel.driveOrders")));
        java.awt.GridBagLayout stationsPanelLayout = new java.awt.GridBagLayout();
        stationsPanelLayout.columnWidths = new int[] {0, 5, 0};
        stationsPanelLayout.rowHeights = new int[] {0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0};
        stationsPanel.setLayout(stationsPanelLayout);

        driveOrdersScrollPane.setPreferredSize(new java.awt.Dimension(200, 200));

        driveOrdersTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Station", "Aktion"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        driveOrdersScrollPane.setViewportView(driveOrdersTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 11;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        stationsPanel.add(driveOrdersScrollPane, gridBagConstraints);

        addButton.setFont(addButton.getFont());
        addButton.setText(ResourceBundleUtil.getBundle().getString("CreateTransportOrderPanel.add"));
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
        stationsPanel.add(addButton, gridBagConstraints);

        editButton.setFont(editButton.getFont());
        editButton.setText(ResourceBundleUtil.getBundle().getString("CreateTransportOrderPanel.edit"));
        editButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        stationsPanel.add(editButton, gridBagConstraints);

        removeButton.setFont(removeButton.getFont());
        removeButton.setText(ResourceBundleUtil.getBundle().getString("CreateTransportOrderPanel.delete"));
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        stationsPanel.add(removeButton, gridBagConstraints);

        moveUpButton.setFont(moveUpButton.getFont());
        moveUpButton.setText(ResourceBundleUtil.getBundle().getString("CreateTransportOrderPanel.up"));
        moveUpButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveUpButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        stationsPanel.add(moveUpButton, gridBagConstraints);

        moveDownButton.setFont(moveDownButton.getFont());
        moveDownButton.setText(ResourceBundleUtil.getBundle().getString("CreateTransportOrderPanel.down"));
        moveDownButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveDownButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        stationsPanel.add(moveDownButton, gridBagConstraints);

        add(stationsPanel);

        deadlinePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(ResourceBundleUtil.getBundle().getString("CreateTransportOrderPanel.deadline")));
        java.awt.GridBagLayout deadlinePanelLayout = new java.awt.GridBagLayout();
        deadlinePanelLayout.columnWidths = new int[] {0, 5, 0, 5, 0, 5, 0};
        deadlinePanelLayout.rowHeights = new int[] {0};
        deadlinePanel.setLayout(deadlinePanelLayout);

        dateLabel.setFont(dateLabel.getFont());
        dateLabel.setText(ResourceBundleUtil.getBundle().getString("CreateTransportOrderPanel.date"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        deadlinePanel.add(dateLabel, gridBagConstraints);

        dateTextField.setColumns(10);
        dateTextField.setFont(dateTextField.getFont());
        dateTextField.setText("31.12.2099");
        dateTextField.setToolTipText("Geben Sie das Datum im Format TT.MM.JJJJ ein!");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        deadlinePanel.add(dateTextField, gridBagConstraints);

        timeLabel.setFont(timeLabel.getFont());
        timeLabel.setText(ResourceBundleUtil.getBundle().getString("CreateTransportOrderPanel.time"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        deadlinePanel.add(timeLabel, gridBagConstraints);

        timeTextField.setColumns(10);
        timeTextField.setFont(timeTextField.getFont());
        timeTextField.setText("23:59");
        timeTextField.setToolTipText("Geben Sie die Uhrzeit im Format HH:MM ein!");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        deadlinePanel.add(timeTextField, gridBagConstraints);

        add(deadlinePanel);

        vehiclePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(ResourceBundleUtil.getBundle().getString("CreateTransportOrderPanel.vehicle")));
        java.awt.GridBagLayout vehiclePanelLayout = new java.awt.GridBagLayout();
        vehiclePanelLayout.columnWidths = new int[] {0, 5, 0};
        vehiclePanelLayout.rowHeights = new int[] {0};
        vehiclePanel.setLayout(vehiclePanelLayout);

        vehicleLabel.setFont(vehicleLabel.getFont());
        vehicleLabel.setText(ResourceBundleUtil.getBundle().getString("CreateTransportOrderPanel.vehicle"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        vehiclePanel.add(vehicleLabel, gridBagConstraints);

        vehicleComboBox.setFont(vehicleComboBox.getFont());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        vehiclePanel.add(vehicleComboBox, gridBagConstraints);

        add(vehiclePanel);
    }// </editor-fold>//GEN-END:initComponents

  /**
   * R�ckt den ausgew�hlten Fahrauftrag um eine Position nach unten.
   *
   * @param evt das ausl�sende Ereignis
   */
    private void moveDownButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveDownButtonActionPerformed
      int index = driveOrdersTable.getSelectedRow();

      if (index == -1) {
        return;
      }

      if (index == driveOrdersTable.getRowCount() - 1) {
        return;
      }

      DefaultTableModel model = (DefaultTableModel) driveOrdersTable.getModel();
      model.moveRow(index, index, index + 1);
      driveOrdersTable.getSelectionModel().setSelectionInterval(index + 1, index + 1);

      LocationModel location = fLocationModels.remove(index);
      fLocationModels.add(index + 1, location);

      String action = fActions.remove(index);
      fActions.add(index + 1, action);

      updateButtons();
    }//GEN-LAST:event_moveDownButtonActionPerformed

  /**
   * R�ckt den ausgew�hlten Fahrauftrag um eine Position nach oben.
   *
   * @param evt das ausl�sende Ereignis
   */
    private void moveUpButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveUpButtonActionPerformed
      int index = driveOrdersTable.getSelectedRow();

      if (index <= 0) {
        return;
      }

      DefaultTableModel model = (DefaultTableModel) driveOrdersTable.getModel();
      model.moveRow(index, index, index - 1);
      driveOrdersTable.getSelectionModel().setSelectionInterval(index - 1, index - 1);

      LocationModel location = fLocationModels.remove(index);
      fLocationModels.add(index - 1, location);

      String action = fActions.remove(index);
      fActions.add(index - 1, action);

      updateButtons();
    }//GEN-LAST:event_moveUpButtonActionPerformed

  /**
   * Entfernt den ausgew�hlten Fahrauftrag.
   *
   * @param evt das ausl�sende Ereignis
   */
    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
      int index = driveOrdersTable.getSelectedRow();

      if (index == -1) {
        return;
      }

      fLocationModels.remove(index);
      fActions.remove(index);

      DefaultTableModel model = (DefaultTableModel) driveOrdersTable.getModel();
      model.removeRow(index);
      updateButtons();
    }//GEN-LAST:event_removeButtonActionPerformed

  /**
   * Bearbeitet den ausgew�hlten Fahrauftrag.
   *
   * @param evt das ausl�sende Ereignis
   */
    private void editButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editButtonActionPerformed
      int index = driveOrdersTable.getSelectedRow();

      if (index == -1) {
        return;
      }

      LocationModel location = fLocationModels.get(index);
      String action = fActions.get(index);
      EditDriveOrderPanel contentPanel = new EditDriveOrderPanel(fOpenTCSView.getSystemModel().getLocationModels(), location, action);
      StandardContentDialog dialog = new StandardContentDialog(fOpenTCSView, contentPanel);
      dialog.setVisible(true);

      if (dialog.getReturnStatus() == StandardContentDialog.RET_OK) {
        location = contentPanel.getSelectedLocation();
        action = contentPanel.getSelectedAction();

        driveOrdersTable.setValueAt(location.getName(), index, 0);
        driveOrdersTable.setValueAt(action, index, 1);

        fLocationModels.set(index, location);
        fActions.set(index, action);
      }
    }//GEN-LAST:event_editButtonActionPerformed

  /**
   * F�gt einen Fahrauftrag hinzu.
   *
   * @param evt das ausl�sende Ereignis
   */
    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
      EditDriveOrderPanel contentPanel = new EditDriveOrderPanel(fOpenTCSView.getSystemModel().getLocationModels());
      StandardContentDialog dialog = new StandardContentDialog(fOpenTCSView, contentPanel);
      dialog.setVisible(true);

      if (dialog.getReturnStatus() == StandardContentDialog.RET_OK) {
        int index = driveOrdersTable.getRowCount();

        LocationModel location = contentPanel.getSelectedLocation();
        String action = contentPanel.getSelectedAction();

        String[] row = new String[2];
        row[0] = location.getName();
        row[1] = action;

        DefaultTableModel model = (DefaultTableModel) driveOrdersTable.getModel();
        model.addRow(row);

        fLocationModels.add(location);
        fActions.add(action);

        driveOrdersTable.setRowSelectionInterval(index, index);
        updateButtons();
      }
    }//GEN-LAST:event_addButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JLabel dateLabel;
    private javax.swing.JTextField dateTextField;
    private javax.swing.JPanel deadlinePanel;
    private javax.swing.JScrollPane driveOrdersScrollPane;
    private javax.swing.JTable driveOrdersTable;
    private javax.swing.JButton editButton;
    private javax.swing.JButton moveDownButton;
    private javax.swing.JButton moveUpButton;
    private javax.swing.JButton removeButton;
    private javax.swing.JPanel stationsPanel;
    private javax.swing.JLabel timeLabel;
    private javax.swing.JTextField timeTextField;
    private javax.swing.JComboBox vehicleComboBox;
    private javax.swing.JLabel vehicleLabel;
    private javax.swing.JPanel vehiclePanel;
    // End of variables declaration//GEN-END:variables
  // CHECKSTYLE:ON
}
