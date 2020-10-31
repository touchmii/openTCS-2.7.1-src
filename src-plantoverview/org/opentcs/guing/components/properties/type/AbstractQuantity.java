/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.components.properties.type;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import static java.util.Objects.requireNonNull;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * Basisimplementierung f�r Properties, die einen Wert und eine Einheit
 * besitzen. Beispiele hierf�r sind 1 s, 200 m, 30 km/h. Zudem werden
 * Umwandlungsverh�ltnisse (siehe {
 *
 * @see Relation}) zwischen verschiedenen Ma�einheiten festgelegt. Mit Hilfe
 * dieser Umwandlungsverh�ltnisse k�nnen Konvertierungen der Werte in andere
 * Einheiten vorgenommen werden.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @author Stefan Walter (Fraunhofer IML)
 */
public abstract class AbstractQuantity<U extends Enum<U>>
    extends AbstractProperty {

  /**
   * This class's logger.
   */
  private static final Logger log
      = Logger.getLogger(AbstractQuantity.class.getName());
  /**
   * The unit's enum class;
   */
  private final Class<U> fUnitClass;
  /**
   * Die m�glichen Einheiten.
   */
  private final List<U> fPossibleUnits;
  /**
   * Die Verh�ltnisse zwischen den Einheiten.
   */
  private final List<Relation<U>> fRelations;
  /**
   * Die aktuelle Einheit zum Zahlenwert.
   */
  private U fUnit;
  /**
   * Ob es sich um einen ganzzahligen Wert handeln soll.
   */
  private boolean fIsInteger;
  /**
   * Ob es sich um einen vorzeichenlosen Wert handeln soll.
   */
  private boolean fIsUnsigned;
  /**
   * A {@link ValidRangePair} indicating the range of the valid values
   * for this quantity.
   */
  protected ValidRangePair validRange = new ValidRangePair();

  /**
   * Konstruktor mit Wert und Ma�einheit.
   *
   * @param model
   * @param value
   * @param unit
   */
  public AbstractQuantity(ModelComponent model, double value, U unit, Class<U> unitClass, List<Relation<U>> relations) {
    super(model);
    setValue(value);
    fUnit = requireNonNull(unit, "unit");
    fUnitClass = requireNonNull(unitClass, "unitClass");
    fPossibleUnits = Arrays.asList(unitClass.getEnumConstants());
    fRelations = requireNonNull(relations, "relations");
    fIsInteger = false;
    fIsUnsigned = false;
    initValidRange();
  }

  /**
   * Sets the new valid range.
   *
   * @param newRange The new {@link ValidRangePair}.
   */
  public void setValidRangePair(ValidRangePair newRange) {
    validRange = Objects.requireNonNull(newRange, "newRange is null");
  }

  /**
   * Returns the valid range for this quantity.
   *
   * @return The {@link ValidRangePair}.
   */
  public ValidRangePair getValidRange() {
    return validRange;
  }

  /**
   * Initializes the valid range of values.
   */
  protected abstract void initValidRange();

  /**
   * Setzt den Wert auf ganzzahlig oder gebrochen.
   *
   * @param isInteger
   */
  public void setInteger(boolean isInteger) {
    fIsInteger = isInteger;
  }

  /**
   * Zeigt an, ob eine ganzzahlige Darstellung erfolgen soll.
   *
   * @return
   */
  public boolean isInteger() {
    return fIsInteger;
  }

  /**
   * Setzt den Wert auf vorzeichenbehaftet oder vorzeichenlos.
   *
   * @param isUnsigned
   */
  public void setUnsigned(boolean isUnsigned) {
    this.fIsUnsigned = isUnsigned;
  }

  /**
   *
   * @return
   */
  public boolean isUnsigned() {
    return fIsUnsigned;
  }

  @Override
  public Object getValue() {
    try {
      double value = Double.parseDouble(fValue.toString());

      if (isInteger()) {
        return (int) (value + 0.5);
      }
      else {
        return value;
      }
    }
    catch (NumberFormatException nfe) {
      log.log(Level.INFO, "Error parsing value", nfe);
      return fValue;
    }
  }

  /**
   * Liefert den Wert, der der �bergebenen Einheit entspricht, ohne eine feste
   * Umwandlung vorzunehmen.
   *
   * @param unit
   * @return
   */
  public double getValueByUnit(U unit) {
    try {
      AbstractQuantity<U> property = (AbstractQuantity<U>) clone();
      property.setValueAndUnit((double) getValue(), getUnit());
      property.convertTo(unit);

      return (double) property.getValue();
    }
    catch (IllegalArgumentException e) {
      log.log(Level.SEVERE, "Exception: ", e);
    }

    return Double.NaN;
  }

  /**
   * Konvertiert den aktuellen Wert mit der aktuellen Einheit in einen neuen
   * Wert mit der �bergebenen
   *
   * @param unit Einheit.
   */
  public void convertTo(U unit) {
    if (!fPossibleUnits.contains(unit)) {
      return;
    }

    if (fUnit.equals(unit)) {
      return;
    }

    int indexUnitA = fPossibleUnits.indexOf(fUnit);
    int indexUnitB = fPossibleUnits.indexOf(unit);

    int lowerIndex;
    int upperIndex;
    if (indexUnitA < indexUnitB) {
      lowerIndex = indexUnitA;
      upperIndex = indexUnitB;
    }
    else {
      lowerIndex = indexUnitB;
      upperIndex = indexUnitA;
    }

    double relationValue = 1.0;

    for (int i = lowerIndex; i < upperIndex; i++) {
      U unitA = fPossibleUnits.get(i);
      U unitB = fPossibleUnits.get(i + 1);

      Relation<U> relation = findFittingRelation(unitA, unitB);
      relationValue *= relation.relationValue();
    }

    U lowerUnit = fPossibleUnits.get(lowerIndex);
    U upperUnit = fPossibleUnits.get(upperIndex);

    Relation<U> relation = new Relation<>(lowerUnit, upperUnit, relationValue);

    Relation.Operation operation = relation.getOperation(fUnit, unit);

    switch (operation) {
      case DIVISION:
        setValue((double) fValue / relation.relationValue());
        break;
      case MULTIPLICATION:
        setValue((double) fValue * relation.relationValue());
        break;
      default:
        throw new IllegalArgumentException("Unhandled operation: " + operation);
    }

    fUnit = unit;
  }

  /**
   * Liefert die Ma�einheit.
   *
   * @return
   */
  public U getUnit() {
    return fUnit;
  }

  /**
   * Pr�ft, ob es sich bei der �bergebenen Ma�einheit um eine m�gliche Einheit
   * handelt. Gibt in diesem Fall
   * <code>true
   * </code> zur�ck.
   *
   * @param unit
   * @return
   */
  public boolean isPossibleUnit(U unit) {
    return fPossibleUnits.contains(unit);
  }

  /**
   * Setzt f�r das Attribut einen neuen Wert und eine neue Ma�einheit. Eine
   * Ausnahme wird ausgel�st, wenn es sich bei der Ma�einheit um keine m�gliche
   * Einheit handelt.
   *
   * @param value
   * @param unit
   * @throws IllegalArgumentException If the given unit is not usable.
   */
  public void setValueAndUnit(double value, U unit)
      throws IllegalArgumentException {
    if (!isPossibleUnit(unit)) {
      throw new IllegalArgumentException(
          ResourceBundleUtil.getBundle().getString("AbstractQuantity.errorWrongUnit"));
    }
    if (!Double.isNaN(value)) {
      if (fValue instanceof Double) {
        if ((double) fValue != value) {
          markChanged();
        }
      }
      else {
        // Wenn fValue vorher "<different values>" war
        markChanged();
      }
    }

    setValue(value);
    fUnit = unit;

    if (fIsUnsigned) {
      setValue(Math.abs(value));
    }
  }

  public void setValueAndUnit(double value, String unitString)
      throws IllegalArgumentException {
    requireNonNull(unitString);

    for (U unit : fUnitClass.getEnumConstants()) {
      if (unitString.equals(unit.toString())) {
        setValueAndUnit(value, unit);
        return;
      }
    }
    throw new IllegalArgumentException("Unknown unit \"" + unitString + "\"");
  }

  @Override
  public String toString() {
    if (fValue instanceof Integer) {
      return ((int) fValue) + " " + fUnit;
    }
    else if (fValue instanceof Double) {
      return fValue + " " + fUnit;
    }
    else {
      return fValue.toString();
    }
  }

  /**
   * Liefert die m�glichen Ma�einheiten.
   *
   * @return
   */
  public List<U> getPossibleUnits() {
    return fPossibleUnits;
  }

  @Override
  public void copyFrom(Property property) {
    AbstractQuantity<U> quantity = (AbstractQuantity<U>) property;

    try {
      if (quantity.getValue() instanceof Double) {
        setValueAndUnit((double) quantity.getValue(), quantity.getUnit());
      }
      else if (quantity.getValue() instanceof Integer) {
        setValueAndUnit(((Integer) quantity.getValue()).doubleValue(), quantity.getUnit());
      }
    }
    catch (IllegalArgumentException e) {
      log.log(Level.SEVERE, "Exception: ", e);
    }
  }

  /**
   * Findet aus der Menge der Umwandlungsverh�ltnisse das heraus, welches f�r
   * die beiden �bergebenen Einheiten passend ist.
   */
  private Relation<U> findFittingRelation(U unitFrom, U unitTo) {
    for (Relation<U> relation : fRelations) {
      if (relation.fits(unitFrom, unitTo)) {
        return relation;
      }
    }

    return null;
  }

  public class ValidRangePair {

    private double min = Double.NEGATIVE_INFINITY;
    private double max = Double.MAX_VALUE;

    public ValidRangePair() {
    }

    public ValidRangePair(double min, double max) {
      this.min = min;
      this.max = max;
    }

    /**
     * Returns whether the given value is in the valid range.
     *
     * @param value The value to test.
     * @return <code>true</code> if the value is in range, <code>false</code>
     * otherwise.
     */
    public boolean isValueValid(double value) {
      return value >= min && value <= max;
    }

    public double getMin() {
      return min;
    }

    public ValidRangePair setMin(double min) {
      this.min = min;
      return this;
    }

    public double getMax() {
      return max;
    }

    public ValidRangePair setMax(double max) {
      this.max = max;
      return this;
    }
  }
}
