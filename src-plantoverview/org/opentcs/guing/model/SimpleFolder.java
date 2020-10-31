/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.model;

/**
 * Ist die einfachste Form einer konkreten Komponente im Systemmodell, die
 * Kindelemente enth�lt. SimpleFolder wird f�r schlichte Ordner verwendet.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class SimpleFolder
    extends CompositeModelComponent {

  /**
   * Erzeugt von SimpleFolder einen neues Exemplar mit dem �bergebenen Namen.
   *
   * @param name
   */
  public SimpleFolder(String name) {
    super(name);
  }
}
