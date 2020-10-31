/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.components.tree.elements;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import org.opentcs.guing.model.ModelComponent;

/**
 * Ein UserObject hat die Funktion eines Stellvertreters f�r ein Datenobjekt
 * (ModelComponent) im TreeView. Es kennt sein Datenobjekt und ist daf�r
 * zust�ndig, Aktionen des Nutzer wie Selektieren, L�schen, Doppelklicken usw.
 * auszuwerten. In der Regel wird es jeweils eine bestimmte Methode der
 * Applikation aufrufen, in der dann das Ereignis behandelt wird. <br> Neben der
 * Auswertung von Nutzereingaben kann ein UserObject ein Popup-Men� sowie ein
 * Icon bereitstellen. <br> Im TreeView sind prinzipiell alle Objekte vom Typ
 * DefaultMutableTreeNode. Ein DefaultMutableTreeNode besitzt eine Referenz auf
 * ein UserObject. Der TreeView kennt bei einer Aktion des Nutzers nur den
 * DefaultMutableTreeNode, auf dem die Aktion ausgef�hrt wurde. Dadurch, dass
 * aber jeder DefaultMutableTreeNode ein UserObject besitzt, k�nnen auf dem
 * UserObject die jeweiligen Aktionen ausgef�hrt werden.
 * <p>
 * <b>Entwurfsmuster:
 * </b>Befehl. UserObject ist der abstrakte Befehl. Klient ist der TreeView und
 * Empf�nger ist die Applikation.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @see ModelComponent
 */
public interface UserObject {

  /**
   * Liefert das gekapselte Datenobjekt (ModelComponent).
   *
   * @return
   */
  ModelComponent getModelComponent();

  /**
   * Liefert ein passendes Popup-Men�.
   *
   * @return
   */
  JPopupMenu getPopupMenu();

  /**
   * Liefert das zugeh�rige Icon.
   *
   * @return
   */
  ImageIcon getIcon();

  /**
   * Wird aufgerufen, wenn das Objekt im Baum selektiert wurde.
   */
  void selected();

  /**
   * Wird aufgerufen, wenn das Objekt aus dem Baum entfernt wurde (aufgrund
   * einer Nutzereingabe).
   *
   * @return
   */
  boolean removed();

  /**
   * Wird aufgerunfen, wenn das Objekt in der Baumansicht mit der rechten
   * Maustaste angeklickt wird.
   *
   * @param component
   * @param x
   * @param y
   */
  void rightClicked(JComponent component, int x, int y);

  /**
   * Wird aufgerufen, wenn das Objekt in der Baumansicht doppelt angeklickt
   * wird.
   */
  void doubleClicked();
  
  /**
   * Returns the parent component that contains this user object.
   * (Typically a <code>SimpleFolder</code>
   * 
   * @return The parent <code>ModelComponent</code>.
   */
  ModelComponent getParent();
  
  /**
   * Sets the parent component.
   * 
   * @param parent The parent.
   */
  void setParent(ModelComponent parent);
}
