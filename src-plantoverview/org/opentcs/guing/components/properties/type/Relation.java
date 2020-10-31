/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.components.properties.type;

import java.io.Serializable;

/**
 * Ein Umwandlungsverh�ltnis zwischen zwei Einheiten.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class Relation<U>
    implements Serializable {

  /**
   * Die Ma�einheit von der umgewandelt wird.
   */
  private final U fUnitFrom;
  /**
   * Die Ma�einheit, in die umgewandelt wird.
   */
  private final U fUnitTo;
  /**
   * Das Umwandlungsverh�ltnis.
   */
  private final double fRelationValue;

  /**
   * Creates a new instance.
   *
   * @param unitFrom
   * @param unitTo
   * @param relationValue
   */
  public Relation(U unitFrom, U unitTo, double relationValue) {
    fUnitFrom = unitFrom;
    fUnitTo = unitTo;
    fRelationValue = relationValue;
  }

  /**
   * Pr�ft, ob das Umwandlungsverh�ltnis f�r die beiden �bergebenen Einheiten
   * passend ist. Liefert <code>true</code> zur�ck, falls ja.
   *
   * @param unitA
   * @param unitB
   * @return
   */
  public boolean fits(U unitA, U unitB) {
    if (fUnitFrom.equals(unitA) && fUnitTo.equals(unitB)) {
      return true;
    }

    if (fUnitFrom.equals(unitB) && fUnitTo.equals(unitA)) {
      return true;
    }

    return false;
  }

  /**
   * Liefert das Umwandlungsverh�ltnis als eine Zahl.
   *
   * @return
   */
  public double relationValue() {
    return fRelationValue;
  }

  /**
   * Liefert die Rechenoperation, die f�r die Umwandlung der ersten Einheit in
   * die zweite Einheit angewendet werden muss. In Frage kommt die
   * Multiplikation und die Division. Zur�ckgeliefert wird nicht die Operation
   * im eigentlichen Sinne, sondern nur ein beschreibender Text aus der Menge
   * {"multiplication", "division"}.
   *
   * @param unitFrom
   * @param unitTo
   * @return
   */
  public Operation getOperation(U unitFrom, U unitTo) {
    if (unitFrom.equals(fUnitFrom)) {
      return Operation.DIVISION;
    }
    else {
      return Operation.MULTIPLICATION;
    }
  }
  
  public static enum Operation {
    DIVISION,
    MULTIPLICATION
  }
}
