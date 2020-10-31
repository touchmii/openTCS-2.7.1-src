/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.components.dialogs;

/**
 * Interface f�r Dialoge, die das komfortable Bearbeiten von Attributen
 * erm�glichen. Der Dialog selbst gibt nur den Rahmen vor, z.B. einen Ok- und
 * einen Cancel-Button. Die Komponente, mit der die eigentliche Bearbeitung
 * eines Attributs erfolgt, ist vom Typ DialogContent und wird dem Dialog 
 * hinzugef�gt.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public interface DetailsDialog {

  /**
   * Liefert die Komponente, mit der die Einstellungen des Attributs komfortabel
   * vorgenommen werden k�nnen.
   *
   * @return
   */
  DetailsDialogContent getDialogContent();

  /**
   * Aktiviert den Dialog. Das sollte durch einen Klienten immer dann ausgel�st
   * werden, wenn der Dialog sichtbar gemacht wird. Wird ben�tigt, um die
   * Funktion "�nderungen f�r alle Fahrzeugtypen �bernehmen" realisieren zu
   * k�nnen.
   */
  void activate();
}
