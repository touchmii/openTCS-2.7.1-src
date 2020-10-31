/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.components.tree;

import java.util.Set;
import org.opentcs.guing.components.tree.elements.UserObject;

/**
 * Ein Interface f�r die Baumansicht auf der linken Seite. Eine Baumansicht
 * verwaltet ein Modell, das eine Menge von DefaultMutableTreeNode-Objekten
 * besitzt. Jeder DefaultMutableTreeNode referenziert ein UserObject. Ein
 * UserObject kapselt ein richtiges Objekt (z.B. Article, Figure, ...). Es wei�,
 * welche Methode der Applikation aufgerufen werden muss, wenn sein Objekt im
 * Baum selektiert, gel�scht oder doppelt angeklickt wird.
 * <p>
 * <b>Entwurfsmuster:</b> Befehl. TreeView (bzw. eine Unterklasse) ist der
 * Ausl�ser f�r die Ausf�hrung eines konkreten UserObjects, welches ja einen
 * Befehl darstellt.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @see UserObject
 */
public interface TreeView {

  /**
   * F�gt dem Baum ein Item hinzu. �bergebenes Elternobjekt ist ein richtiges
   * Objekt, das im Baum gesucht werden muss. Item ist ein UserObject.
   *
   * @param parent
   * @param item
   */
  void addItem(Object parent, UserObject item);

  /**
   * L�scht aus dem Baum ein Item. Item ist ein richtiges Objekt, d.h. kein
   * DefaultMutableTreeNode und kein UserObject sondern beispielsweise ein
   * Article.
   *
   * @param item
   */
  void removeItem(Object item);

  /**
   * Entfernt alle Kindelemente eines Items.
   *
   * @param item
   */
  void removeChildren(Object item);

  /**
   * Selektiert ein Item im Baum. Item ist ein richtiges Objekt, d.h. kein
   * DefaultMutableTreeNode und kein UserObject sondern beispielsweise ein
   * Article.
   *
   * @param item
   */
  void selectItem(Object item);

  /**
   * Selektiert mehrere Items im Baum.
   *
   * @param items
   */
  void selectItems(Set items);

  /**
   * Teilt dem Baum mit, dass sich die Eigenschaften des �bergeben Objekts
   * ge�ndert haben und seine Darstellung aktualisiert werden muss. Item ist ein
   * richtiges Objekt, d.h. kein DefaultMutableTreeNode und kein UserObject
   * sondern beispielsweise ein Article.
   *
   * @param item
   */
  void itemChanged(Object item);

  /**
   * Liefert das selektierte Element.
   *
   * @return
   */
  UserObject getSelectedItem();

  /**
   * Liefert die selektierten Elemente.
   *
   * @return
   */
  Set<UserObject> getSelectedItems();
  
  /**
   * Sorts the root element of the tree.
   */
  void sortRoot();
  
  /**
   * Sorts all children.
   */
  void sortChildren();
}
