/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.components.properties.event;

/**
 * Ein PropertiesModelChangeListener, der ben�tigt wird, wenn ein
 * PropertiesModelChangeEvent erzeugt werden soll, jedoch kein direkter
 * PropertiesModelChangeListener die �nderung verursacht hat.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class NullAttributesChangeListener
    implements AttributesChangeListener {

  /**
   * Creates a new instance of NullPropertiesModelChangeListener
   */
  public NullAttributesChangeListener() {
  }

  @Override
  public void propertiesChanged(AttributesChangeEvent e) {
  }
}
