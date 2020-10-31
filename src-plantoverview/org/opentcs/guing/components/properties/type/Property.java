/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.components.properties.type;

/**
 * Interface f�r Attribute. Die Attribute arbeiten als Wrapper, d.h. sie
 * erlauben den Zugriff auf ihre Daten, ohne dabei jedoch ein neues
 * Attribut-Objekt zu erzeugen. Die Datentypen String, Boolean, Integer usw.
 * bieten dieses nicht, so dass die Ver�nderung eines Attributs zu einem neuen
 * Attribut f�hren w�rde. Ergebnis w�re das st�ndige Setzen der Attribute im
 * Datenobjekt.
 * Vorteil der hier eingesetzten Methode ist, dass das Attribut-Objekt selbst
 * stets dasselbe bleibt, dessen Inhalt sich jedoch �ndern l�sst.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public interface Property
    extends ModelAttribute, Cloneable {

  /**
   * �bernimmt die Werte von dem �bergebenen Attribut. Die Eigenschaften
   * Visibility, Editable usw. werden jedoch nicht �bernommen.
   *
   * @param property
   */
  void copyFrom(Property property);

  /**
   * Returns a comparable represantation of the value of this property.
   *
   * @return A represantation to compare this property to other ones.
   */
  Object getComparableValue();

  /**
   * Klont das Property.
   *
   * @return
   */
  Object clone();
}
