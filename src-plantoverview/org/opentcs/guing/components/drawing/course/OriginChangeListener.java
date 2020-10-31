/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.components.drawing.course;

import java.util.EventObject;

/**
 * Interface f�r Klassen, die an �nderungen des Koordinaten-Ursprungs
 * interessiert sind.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public interface OriginChangeListener {

  /**
   * Nachricht, dass sich die Position des Ursprungs ge�ndert hat.
   *
   * @param evt das Ereignis
   */
  void originLocationChanged(EventObject evt);

  /**
   * Nachricht, dass sich der Ma�stab ge�ndert hat.
   *
   * @param evt das Ereignis
   */
  void originScaleChanged(EventObject evt);
}
