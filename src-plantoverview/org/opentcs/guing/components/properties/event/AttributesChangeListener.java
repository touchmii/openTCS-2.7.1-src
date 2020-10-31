/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.components.properties.event;

/**
 * Interface, das Controller/Views implementieren. �ndert sich das Model, so
 * werden alle Controller/Views, die sich zuvor als Listener registriert haben,
 * �ber diese �nderung benachrichtigt.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @see PropertiesModel
 * @see PropertiesModelChangeEvent
 */
public interface AttributesChangeListener {

  /**
   * Information f�r den View, dass sich die Eigenschaften des Models ge�ndert
   * haben. Der View ist nun selbst daf�r zust�ndig, sich zu aktualisieren.
   *
   * @param e
   */
  void propertiesChanged(AttributesChangeEvent e);
}
