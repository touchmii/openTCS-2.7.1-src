/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.components.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Insets;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.border.EmptyBorder;
import org.opentcs.util.gui.Icons;

/**
 * Ein Dialog mit einem Ok- und einem Cancel-Button, dem im Konstruktor ein
 * JComponent-Objekt als Inhalt �bergeben wird. F�r gew�hnlich handelt es sich
 * bei den Inhalten um PropertiesPane-Objekte.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class StandardDialog
    extends JDialog {

  /**
   * A return status code - returned if Cancel button has been pressed
   */
  public static final int RET_CANCEL = 0;
  /**
   * A return status code - returned if OK button has been pressed
   */
  public static final int RET_OK = 1;
  /**
   * Der Inhalt des Dialogs.
   */
  protected JComponent fContent;

  /**
   * Erzeugt ein neues Exemplar von StandardDialog. Die Gr��e des Dialogs wird
   * an den Inhalt angepasst.
   *
   * @param parent Die Komponente, zu der der Dialog zentriert wird.
   * @param modal True, wenn der Dialog modal sein soll.
   * @param content Der Inhalt des Dialogs neben den beiden Standardbuttons.
   * @param title Der Titel des Dialogs.
   */
  public StandardDialog(Component parent, boolean modal, JComponent content, String title) {
    super(JOptionPane.getFrameForComponent(parent), title, modal);
    initComponents();
    initSize(content);
    setTitle(title);
    setIconImages(Icons.getOpenTCSIcons());
  }

  /**
   * Passt die Gr��e des Dialogs nach dem Hinzuf�gen des Panels an.
   *
   * @param content
   */
  protected final void initSize(JComponent content) {
    fContent = content;
    getContentPane().add(content, BorderLayout.CENTER);
    content.setBorder(new EmptyBorder(new Insets(3, 3, 3, 3)));
    getRootPane().setDefaultButton(okButton);
    pack();
  }

  /**
   * Liefert den Inhalt des Dialogs.
   *
   * @return
   */
  public JComponent getContent() {
    return fContent;
  }

  /**
   * return the return status of this dialog - one of RET_OK or RET_CANCEL
   *
   * @return
   */
  public int getReturnStatus() {
    return returnStatus;
  }

  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonPanel = new javax.swing.JPanel();
        okButton = new javax.swing.JButton();
        cancelButton = new CancelButton();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        okButton.setFont(okButton.getFont().deriveFont(okButton.getFont().getStyle() | java.awt.Font.BOLD));
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/opentcs/guing/res/labels"); // NOI18N
        okButton.setText(bundle.getString("dialog.buttonOk.text")); // NOI18N
        okButton.setOpaque(false);
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });
        buttonPanel.add(okButton);

        cancelButton.setFont(cancelButton.getFont());
        cancelButton.setText(bundle.getString("dialog.buttonCancel.text")); // NOI18N
        cancelButton.setOpaque(false);
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });
        buttonPanel.add(cancelButton);

        getContentPane().add(buttonPanel, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
      doClose(RET_OK);
    }//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
      doClose(RET_CANCEL);
    }//GEN-LAST:event_cancelButtonActionPerformed

  /**
   * Closes the dialog
   */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
      doClose(RET_CANCEL);
    }//GEN-LAST:event_closeDialog

  /**
   * Schlie�t den Dialog.
   */
  private void doClose(int retStatus) {
    returnStatus = retStatus;
    setVisible(false);
    dispose();
  }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton okButton;
    // End of variables declaration//GEN-END:variables
	private int returnStatus = RET_CANCEL;
}