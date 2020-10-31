/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.components.dialogs;

import java.util.Iterator;
import java.util.List;
import org.jhotdraw.draw.DrawingView;
import org.opentcs.guing.components.drawing.OpenTCSDrawingView;
import org.opentcs.guing.components.drawing.figures.VehicleFigure;
import org.opentcs.guing.model.elements.VehicleModel;

/**
 * Panel zur Auswahl eines Fahrzeugs, das dann im View gesucht werden kann.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class FindVehiclePanel
    extends javax.swing.JPanel {

  /**
   * Die Liste mit den vorhandenen Fahrzeugen.
   */
  protected List fVehicles;
  /**
   * Die Applikation.
   */
  protected DrawingView fDrawingView;

  /**
   * Creates new form FindVehiclePanel
   */
  public FindVehiclePanel(List vehicles, DrawingView drawingView) {
    initComponents();
    fVehicles = vehicles;
    fDrawingView = drawingView;
    Iterator e = vehicles.iterator();

    while (e.hasNext()) {
      VehicleModel vehicle = (VehicleModel) e.next();
      comboBoxVehicles.addItem(vehicle.getName());
    }
  }

  /**
   * Liefert das ausgewählte Fahrzeug.
   */
  public VehicleModel getSelectedVehicle() {
    int index = comboBoxVehicles.getSelectedIndex();

    if (index == -1) {
      return null;
    }

    return (VehicleModel) fVehicles.get(index);
  }

  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        labelVehicles = new javax.swing.JLabel();
        comboBoxVehicles = new javax.swing.JComboBox();
        buttonFind = new javax.swing.JButton();

        labelVehicles.setFont(labelVehicles.getFont());
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/opentcs/guing/res/labels"); // NOI18N
        labelVehicles.setText(bundle.getString("findVehiclePanel.labelVehicles.text")); // NOI18N
        add(labelVehicles);

        comboBoxVehicles.setFont(comboBoxVehicles.getFont());
        add(comboBoxVehicles);

        buttonFind.setFont(buttonFind.getFont());
        buttonFind.setText(bundle.getString("findVehiclePanel.buttonFind.text")); // NOI18N
        buttonFind.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonFindActionPerformed(evt);
            }
        });
        add(buttonFind);
    }// </editor-fold>//GEN-END:initComponents

  /**
   * Startet die Suche nach einem Fahrzeug. Wird aufgerufen, wenn der Benutzer
   * den Button "Fahrzeug finden" anklickt.
   */
    private void buttonFindActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonFindActionPerformed
      VehicleModel vehicle = getSelectedVehicle();

      if (vehicle != null) {
        VehicleFigure figure = (VehicleFigure) vehicle.getFigure();

        if (figure != null) {
          if (fDrawingView != null) {
            ((OpenTCSDrawingView) fDrawingView).scrollTo(figure);
          }
        }
      }
    }//GEN-LAST:event_buttonFindActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonFind;
    private javax.swing.JComboBox comboBoxVehicles;
    private javax.swing.JLabel labelVehicles;
    // End of variables declaration//GEN-END:variables
}
