/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.components.properties;

import javax.swing.JComponent;
import org.opentcs.guing.application.action.edit.UndoRedoManager;
import org.opentcs.guing.model.ModelComponent;

/**
 * Ein Inhalt f�r eine Swing-Komponente, der �ber Eigenschaften eines
 * ModelComponent-Objekt Auskunft gibt und M�glichkeiten bietet, diese
 * Eigenschaften zu ver�ndern.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public interface AttributesContent {

  /**
   * @param model Das ModelComponent-Objekt, dessen Eigenschaften dargestellt 
   * werden sollen.
   */
  void setModel(ModelComponent model);

  /**
   * Setzt die Anzeige zur�ck, wenn kein ModelComponente-Objekt mehr dargestellt
   * werden soll.
   */
  void reset();

  /**
   * Liefert den Inhalt, der in eine andere Swing-Komponente eingebunden werden
   * kann.
   *
   * @return
   */
  JComponent getComponent();

  /**
   * Liefert eine Beschreibung des Inhalts, der bei Aktivierung in der
   * �bergeordneten Swing-Komponente angezeigt werden kann.
   *
   * @return
   */
  String getDescription();

  /**
   * Initialisiert den Inhalt mit dem Parent und dem UndoManager.
   *
   * @param undoRedoManager
   */
  void setup(UndoRedoManager undoRedoManager);
}
