/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.components.properties.panel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import org.opentcs.data.model.visualization.LocationRepresentation;
import org.opentcs.guing.components.dialogs.DetailsDialogContent;
import org.opentcs.guing.components.properties.type.Property;
import org.opentcs.guing.components.properties.type.SymbolProperty;
import org.opentcs.guing.model.SystemModel;
import org.opentcs.guing.util.DefaultLocationThemeManager;
import org.opentcs.guing.util.IconToolkit;
import org.opentcs.guing.util.LocationThemeManager;
import org.opentcs.guing.util.ResourceBundleUtil;
import org.opentcs.util.gui.plugins.LocationTheme;

/**
 * Grafische Benutzeroberfl�che zur Bearbeitung eines Attributs, das ein
 * grafisches Symbol repre�sentiert.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class SymbolPropertyEditorPanel
    extends JPanel
    implements DetailsDialogContent {

  /**
   * Das Attribut.
   */
  private SymbolProperty fProperty;
  /**
   * Die Enum-Namen der Symbole.
   */
  private final List<LocationRepresentation> fRepresentations
      = new LinkedList<>();
  /**
   * Die Symbole.
   */
  private final List<ImageIcon> fSymbols = new ArrayList<>();
  /**
   * Der Index des angezeigten Symbols.
   */
  private int fIndex;
  /**
   * Die LocationThemeRegistry.
   */
  private final LocationThemeManager locationThemeManager
      = DefaultLocationThemeManager.getInstance();
  /**
   * The factory used for the images.
   */
  private LocationTheme locationTheme;

  /**
   * Creates new form SymbolPropertyEditorPanel
   */
  public SymbolPropertyEditorPanel() {
    initComponents();
    init();
  }

  @Override // DetailsDialogContent
  public void setProperty(Property property) {
    fProperty = (SymbolProperty) property;
    fIndex = fRepresentations.indexOf(fProperty.getLocationRepresentation());

    if (fIndex == -1) {
      fIndex = 0;
    }

    updateView();
  }

  @Override // DetailsDialogContent
  public void updateValues() {
    if (fIndex < 0) {
      fProperty.setLocationRepresentation(null);
    }
    else {
      fProperty.setLocationRepresentation(fRepresentations.get(fIndex));
    }
  }

  @Override // DetailsDialogContent
  public String getTitle() {
    ResourceBundleUtil bundle = ResourceBundleUtil.getBundle();
    return bundle.getString("SymbolPropertyEditorPanel.title");
  }

  @Override // DetailsDialogContent
  public Property getProperty() {
    return fProperty;
  }

  @Override
  public void setSystemModel(SystemModel systemModel) {
    // Do nada.
  }

  private void init() {
    Collections.addAll(fRepresentations, LocationRepresentation.values());

    locationTheme = locationThemeManager.getDefaultTheme();

    for (LocationRepresentation cur : LocationRepresentation.values()) {
      String filename = locationTheme.getImagePathFor(cur);

      if (filename != null) {
        fSymbols.add(IconToolkit.instance().getImageIconByFullPath(filename));
      }
      else {
        fRepresentations.remove(cur);
      }
    }
  }

  /**
   * Aktualisiert die Ansicht.
   */
  private void updateView() {
    fSymbols.clear();
    fRepresentations.clear();
    init();
    labelSymbol.setIcon(fSymbols.get(fIndex));
    labelSymbolName.setText(fRepresentations.get(fIndex).name());
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

        previousSymbolButton = new javax.swing.JButton();
        nextSymbolButton = new javax.swing.JButton();
        labelSymbol = new javax.swing.JLabel();
        labelSymbolName = new javax.swing.JLabel();
        removeButton = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        previousSymbolButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/opentcs/guing/res/symbols/panel/back.24x24.gif"))); // NOI18N
        previousSymbolButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        previousSymbolButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                previousSymbolButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        add(previousSymbolButton, gridBagConstraints);

        nextSymbolButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/opentcs/guing/res/symbols/panel/forward.24x24.gif"))); // NOI18N
        nextSymbolButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        nextSymbolButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextSymbolButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        add(nextSymbolButton, gridBagConstraints);

        labelSymbol.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelSymbol.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)), javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        labelSymbol.setMaximumSize(new java.awt.Dimension(200, 100));
        labelSymbol.setMinimumSize(new java.awt.Dimension(100, 60));
        labelSymbol.setPreferredSize(new java.awt.Dimension(100, 60));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        add(labelSymbol, gridBagConstraints);

        labelSymbolName.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelSymbolName.setText("Name");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 4, 6);
        add(labelSymbolName, gridBagConstraints);

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/opentcs/guing/res/labels"); // NOI18N
        removeButton.setText(bundle.getString("dialog.buttonRemove.text")); // NOI18N
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        add(removeButton, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

  /**
   * Bl�ttert weiter zum n�chsten Symbol.
   *
   * @param evt das ausl�sende Ereignis
   */
    private void nextSymbolButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextSymbolButtonActionPerformed
      if (fIndex >= fSymbols.size() - 1 || fIndex < 0) {
        fIndex = 0;
      }
      else {
        fIndex++;
      }

      updateView();
    }//GEN-LAST:event_nextSymbolButtonActionPerformed

  /**
   * Bl�ttert zur�ck zum vorherigen Symbol.
   *
   * @param evt das ausl�sende Ereignis
   */
    private void previousSymbolButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_previousSymbolButtonActionPerformed
      if (fIndex <= 0 || fIndex >= fSymbols.size()) {
        fIndex = fSymbols.size() - 1;
      }
      else {
        fIndex--;
      }

      updateView();
    }//GEN-LAST:event_previousSymbolButtonActionPerformed

  /**
   * Remove the symbol for the Location or LocationType
   * @param evt
   */
  private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
    labelSymbol.setIcon(null);
    labelSymbolName.setText("-");
    fIndex = -2;  // Invalid index, so in updateValues() no Icon will be loaded
  }//GEN-LAST:event_removeButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel labelSymbol;
    private javax.swing.JLabel labelSymbolName;
    private javax.swing.JButton nextSymbolButton;
    private javax.swing.JButton previousSymbolButton;
    private javax.swing.JButton removeButton;
    // End of variables declaration//GEN-END:variables
  // CHECKSTYLE:ON
}
