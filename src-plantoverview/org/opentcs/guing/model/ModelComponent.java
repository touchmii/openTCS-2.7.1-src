/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.model;

import java.util.List;
import java.util.Map;
import org.opentcs.guing.components.properties.event.AttributesChangeListener;
import org.opentcs.guing.components.properties.type.Property;
import org.opentcs.guing.components.tree.TreeViewManager;
import org.opentcs.guing.components.tree.elements.UserObject;

/**
 * Interface f�r alle Komponenten des Systemmodells. Konkrete Implementierungen
 * sind entweder Komposita oder Bl�tter. Eine Komponente ist f�r folgende Dinge
 * zust�ndig:
 * - Bereitstellung einer JComponent (in der Regel eines JPanels), auf dem
 * die Eigenschaften der Komponente ver�nderbar sind
 * - Bereitstellung eines passenden UserObjects, das f�r die Anzeige der
 * Komponente im TreeView eingesetzt wird
 * - Verwaltung der Kindelemente, wenn es sich um ein Kompositum handelt
 * - Wiederherstellung des TreeViews nach dem Laden von der Festplatte
 *
 * <b>Entwurfsmuster:</b> Kompositum. ModelComponent ist die Komponente.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public interface ModelComponent
    extends Cloneable {

  /**
   * Der Schl�ssel f�r das Namensattribut.
   */
  String NAME = "Name";
  /**
   * Der Schl�ssel f�r sonstige Eigenschaften.
   */
  String MISCELLANEOUS = "Miscellaneous";

  /**
   * Erzeugt das UserObject, das im TreeView eingesetzt wird.
   *
   * @return
   */
  UserObject createUserObject();

  /**
   * Liefert das UserObject, das im TreeView eingesetzt wird.
   *
   * @return
   */
  UserObject getUserObject();

  /**
   * F�gt das eigene Objekt dem TreeView hinzu und ruft die restore()- Methode
   * auf alle Kindobjekte auf. Wird f�r die Wiederherstellung der Baumansicht
   * nach dem Laden von der Festplatte verwendet.
   *
   * @param parent
   * @param treeViewManager
   */
  void treeRestore(ModelComponent parent, TreeViewManager treeViewManager);

  /**
   * F�gt ein Kindobjekt hinzu.
   *
   * @param component
   */
  void add(ModelComponent component);

  /**
   * Entfernt ein Kindobjekt.
   *
   * @param component
   */
  void remove(ModelComponent component);

  /**
   * Liefert die Kindobjekte.
   *
   * @return
   */
  List<ModelComponent> getChildComponents();

  /**
   * Liefert einen String, der in der Baumansicht angezeigt wird.
   *
   * @return
   */
  String getTreeViewName();

  /**
   * Gibt an, ob die �bergebene Komponente eine direkte Komponente ist.
   *
   * @param component
   * @return
   */
  boolean contains(ModelComponent component);

  /**
   * Liefert die direkte Elternkomponente.
   *
   * @return die direkte Elternkomponente
   */
  ModelComponent getParent();

  /**
   * Returns the actual parent of this component. PropertiesCollection e.g.
   * overwrites it. May be null.
   *
   * @return The actual parent.
   */
  ModelComponent getActualParent();

  /**
   * Setzt die direkte Elternkomponente.
   *
   * @param parent die direkte Elternkomponente
   */
  void setParent(ModelComponent parent);

  /**
   * Liefert true zur�ck, wenn die Komponente im TreeView dargestellt werden
   * soll, ansonsten false.
   *
   * @return
   */
  boolean isTreeViewVisible();

  /**
   * Setzt die TreeView-Sichtbarkeit der Komponente.
   *
   * @param visibility true, wenn die Komponente im TreeView angezeigt werden
   * soll; false, wenn die Komponente nicht angezeigt werden soll
   */
  void setTreeViewVisibility(boolean visibility);

  /**
   * Liefert eine ganz kurze Beschreibung, um was f�r ein Objekt es sich
   * handelt.
   *
   * @return
   */
  String getDescription();

  /**
   * Liefert den Namen des Objekts.
   *
   * @return
   */
  String getName();

  /**
   * Liefert zum aktuellen Schl�ssel das Attribut mit dem �bergebenen Namen.
   *
   * @param name
   * @return
   */
  Property getProperty(String name);

  /**
   * Liefert eine Hashtable mit den Attributen des aktuell gesetzten Schl�ssels.
   *
   * @return
   */
  Map<String, Property> getProperties();

  /**
   * F�gt unter dem �bergebenen Namen einen Beutel mit Attributen hinzu.
   *
   * @param name
   * @param property
   */
  void setProperty(String name, Property property);

  /**
   * F�gt den �bergebenen AttributesChangeListener hinzu und informiert diesen
   * fortan, wenn sich die Eigenschaften oder Zust�nde des ModelComponent
   * ge�ndert haben.
   *
   * @param l der hinzuzuf�gende AttributesChangeListener
   */
  void addAttributesChangeListener(AttributesChangeListener l);

  /**
   * Entfernt den �bergebenen AttributesChangeListener und informiert diesen
   * fortan nicht mehr, wenn sich die Eigenschaften oder Zust�nde des
   * ModelComponent ge�ndert haben.
   *
   * @param l der zu entfernende AttributesChangeListener
   */
  void removeAttributesChangeListener(AttributesChangeListener l);

  /**
   * Pr�ft, ob ein bestimmter AttributesChangeListener vorhanden ist.
   *
   * @param l der zu pr�fende AttributesChangeListener
   * @return
   * <code> true </code>, wenn der AttributesChangeListener vorhanden ist
   */
  boolean containsAttributesChangeListener(AttributesChangeListener l);

  /**
   * Benachrichtigt alle registrierten AttributesChangeListener, dass sich die
   * Eigenschaften des Models ge�ndert haben.
   *
   * @param l Der Initiator der �nderung.
   */
  void propertiesChanged(AttributesChangeListener l);

  /**
   * Clones this ModelComponent.
   *
   * @return A clone of this ModelComponent.
   * @throws java.lang.CloneNotSupportedException
   */
  ModelComponent clone() throws CloneNotSupportedException;
}
