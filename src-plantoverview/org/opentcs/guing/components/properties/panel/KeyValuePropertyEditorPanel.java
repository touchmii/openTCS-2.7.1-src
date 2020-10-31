/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.components.properties.panel;

import org.opentcs.guing.components.dialogs.DetailsDialogContent;
import org.opentcs.guing.components.properties.type.KeyValueProperty;
import org.opentcs.guing.components.properties.type.Property;
import org.opentcs.guing.model.SystemModel;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * Benutzeroberfläche zum Bearbeiten eines Key-Value-Paares.
 *
 * {
 *
 * @see KeyValueProperty}
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class KeyValuePropertyEditorPanel
    extends javax.swing.JPanel
    implements DetailsDialogContent {

  /**
   * Das zu bearbeitende Attribut.
   */
  private KeyValueProperty fProperty;

  /**
   * Creates new form StringSetPropertyEditorPanel
   */
  public KeyValuePropertyEditorPanel() {
    initComponents();
  }

  @Override
  public void setProperty(Property property) {
    fProperty = (KeyValueProperty) property;

    keyTextField.setText(fProperty.getKey());
    valueTextField.setText(fProperty.getValue());
  }

  @Override
  public void updateValues() {
    fProperty.setKeyAndValue(keyTextField.getText(), valueTextField.getText());
  }

  @Override
  public String getTitle() {
    return ResourceBundleUtil.getBundle().getString("KeyValuePropertyEditorPanel.title");
  }

  @Override
  public Property getProperty() {
    return fProperty;
  }

  @Override
  public void setSystemModel(SystemModel systemModel) {
    // Do nada.
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

        keyLabel = new javax.swing.JLabel();
        keyTextField = new javax.swing.JTextField();
        valueLabel = new javax.swing.JLabel();
        valueTextField = new javax.swing.JTextField();

        setLayout(new java.awt.GridBagLayout());

        keyLabel.setFont(keyLabel.getFont());
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/opentcs/guing/res/labels"); // NOI18N
        keyLabel.setText(bundle.getString("KeyValuePropertyEditorPanel.key.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
        add(keyLabel, gridBagConstraints);

        keyTextField.setColumns(15);
        keyTextField.setFont(keyTextField.getFont());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        add(keyTextField, gridBagConstraints);

        valueLabel.setFont(valueLabel.getFont());
        valueLabel.setText(bundle.getString("KeyValuePropertyEditorPanel.value.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
        add(valueLabel, gridBagConstraints);

        valueTextField.setColumns(15);
        valueTextField.setFont(valueTextField.getFont());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        add(valueTextField, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel keyLabel;
    private javax.swing.JTextField keyTextField;
    private javax.swing.JLabel valueLabel;
    private javax.swing.JTextField valueTextField;
    // End of variables declaration//GEN-END:variables
  // CHECKSTYLE:ON
}
