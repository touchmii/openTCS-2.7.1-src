/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.components.properties.type;

import java.util.LinkedList;
import java.util.List;
import org.opentcs.guing.model.ModelComponent;

/**
 * Ein Attribut f�r Winkelangaben.
 * Beispiele: 0.1 rad, 30 deg
 *
 * @author Heinz Huber (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class AngleProperty
    extends AbstractQuantity<AngleProperty.Unit> {

  /**
   * Creates a new instance of AngleProperty
   *
   * @param model
   */
  public AngleProperty(ModelComponent model) {
    this(model, Double.NaN, Unit.DEG);
  }

  /**
   * Konstruktor mit Wert und Ma�einheit.
   *
   * @param model
   * @param value
   * @param unit
   */
  public AngleProperty(ModelComponent model, double value, Unit unit) {
    super(model, value, unit, Unit.class, relations());
  }

  @Override // Property
  public Object getComparableValue() {
    return String.valueOf(fValue) + getUnit();
  }

  private static List<Relation<Unit>> relations() {
    List<Relation<Unit>> relations = new LinkedList<>();
    relations.add(new Relation<>(Unit.DEG, Unit.RAD, 180.0 / Math.PI));
    relations.add(new Relation<>(Unit.RAD, Unit.DEG, Math.PI / 180.0));
    return relations;
  }

  @Override
  public void setValue(Object newValue) {
    if (newValue instanceof Double) {
      double value = (double) newValue;
      fValue = value % 360;
    }
    else {
      super.setValue(newValue);
    }
  }

  @Override
  protected void initValidRange() {
    validRange.setMin(0).setMax(360);
  }

  public static enum Unit {

    DEG("deg"),
    RAD("rad");

    private final String displayString;

    private Unit(String displayString) {
      this.displayString = displayString;
    }

    @Override
    public String toString() {
      return displayString;
    }
  }
}
