/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.components.properties.panel;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import org.opentcs.guing.components.dialogs.DetailsDialogContent;
import org.opentcs.guing.components.properties.type.ModelAttribute;
import org.opentcs.guing.components.properties.type.Property;
import org.opentcs.guing.components.properties.type.SelectionProperty;
import org.opentcs.guing.model.SystemModel;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * Benutzerschnittstelle zum Ausw�hlen eines Wertes aus einer ComboBox.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class SelectionPropertyEditorPanel
		extends JPanel
		implements DetailsDialogContent {

	/**
	 * Das Attribut.
	 */
	private SelectionProperty fProperty;

	/**
	 * Creates new form SelectionPropertyEditorPanel
	 */
	public SelectionPropertyEditorPanel() {
		initComponents();
	}

  @Override // DetailsDialogContent
	public void setProperty(Property property) {
		fProperty = (SelectionProperty) property;

		DefaultComboBoxModel model = new DefaultComboBoxModel(fProperty.getPossibleValues().toArray());
		valueComboBox.setModel(model);

		Object value = fProperty.getValue();
		valueComboBox.setSelectedItem(value);
	}

  @Override // DetailsDialogContent
	public void updateValues() {
		Object selectedItem = valueComboBox.getSelectedItem();
		fProperty.setValue(selectedItem);
		fProperty.setChangeState(ModelAttribute.ChangeState.DETAIL_CHANGED);
	}

  @Override // DetailsDialogContent
	public String getTitle() {
    return ResourceBundleUtil.getBundle().getString("SelectionPropertyEditorPanel.title");
	}

  @Override // DetailsDialogContent
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

        valueLabel = new javax.swing.JLabel();
        valueComboBox = new javax.swing.JComboBox();

        setLayout(new java.awt.GridBagLayout());

        valueLabel.setFont(valueLabel.getFont());
        valueLabel.setText("Wert:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        add(valueLabel, gridBagConstraints);

        valueComboBox.setFont(valueComboBox.getFont());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 4);
        add(valueComboBox, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox valueComboBox;
    private javax.swing.JLabel valueLabel;
    // End of variables declaration//GEN-END:variables
  // CHECKSTYLE:ON
}