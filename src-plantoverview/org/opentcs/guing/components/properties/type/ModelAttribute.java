/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.components.properties.type;

import java.io.Serializable;
import org.opentcs.guing.model.ModelComponent;

/**
 * Interface f�r Eigenschaften (Property) von ModelComponent-Objekten.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public interface ModelAttribute
    extends Serializable {

  public static enum ChangeState {

    NOT_CHANGED,
    CHANGED, // Das Attribut wurde in der Tabelle ge�ndert
    DETAIL_CHANGED, // Das Attribut wurde �ber den Popup-Dialog ge�ndert
  };

  /**
   * Liefert das Model, zu dem dieses Attribut geh�rt.
   *
   * @return
   */
  public ModelComponent getModel();

  /**
   *
   * @param model
   */
  public void setModel(ModelComponent model);

  /**
   * Kennzeichnet das Attribut als ge�ndert. Wird vom Controller/View
   * aufgerufen, der die �nderung vorgenommen hat.
   */
  void markChanged();

  /**
   * Kennzeichnet ein Attribut als nicht ge�ndert. Hebt damit markChanged() auf.
   * Wird vom Model aufgerufen, nachdem sich alle Views aktualisiert haben.
   */
  void unmarkChanged();

  /**
   *
   * @param changeState
   */
  void setChangeState(AbstractModelAttribute.ChangeState changeState);

  /**
   * Gibt zur�ck, ob sich der Zustand ge�ndert hat oder nicht. Damit wissen
   * Views und Fahrkurselemente von Fahrzeugtypen, ob �berhaupt eine
   * �bernehmenswerte �nderung vorliegt.
   *
   * @return
   */
  boolean hasChanged();

  /**
   * Setzt die Bezeichnung dieser Zustandsrepr�sentation.
   *
   * @param description
   */
  void setDescription(String description);

  /**
   * Liefert die Bezeichnung eines Zustands.
   *
   * @return
   */
  String getDescription();

  /**
   * Setzt den Hilfetext f�r einen Zustand.
   *
   * @param helptext
   */
  void setHelptext(String helptext);

  /**
   * Liefert den Hilfetext f�r diesen Zustand.
   *
   * @return
   */
  String getHelptext();

  /**
   * Sagt, ob das Attribut gleichzeitig mit den gleichnamigen Attributen anderer
   * Fahrkurselemente bearbeitet werden kann.
   *
   * @param collectiveEditable
   */
  void setCollectiveEditable(boolean collectiveEditable);

  /**
   * Zeigt an, ob das Attribut gleichzeitig mit den gleichnamigen Attributen
   * anderer Fahrkurselemente desselben Fahrzeugtyps bearbeitet werden kann.
   *
   * @return
   */
  boolean isCollectiveEditable();

  /**
   * @param editable true, wenn der Benutzer das Attribut im Kernel-Modus
   * "Modelling" ver�ndern kann.
   */
  void setModellingEditable(boolean editable);

  /**
   * @return true, wenn der Benutzer das Property im Kernel-Modus "Modelling"
   * ver�ndern kann, ansonsten false.
   */
  boolean isModellingEditable();

  /**
   * @param editable true, wenn der Benutzer das Attribut im Kernel-Modus
   * "Operating" ver�ndern kann.
   */
  void setOperatingEditable(boolean editable);

  /**
   * @return true, wenn der Benutzer das Property im Kernel-Modus "Operating"
   * ver�ndern kann, ansonsten false.
   */
  boolean isOperatingEditable();
}
