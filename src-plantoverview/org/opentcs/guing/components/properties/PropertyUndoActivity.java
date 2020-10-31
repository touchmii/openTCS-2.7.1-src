/**
 * (c): IML, IFAK, JHotDraw.
 *
 */
package org.opentcs.guing.components.properties;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import org.opentcs.guing.components.properties.event.NullAttributesChangeListener;
import org.opentcs.guing.components.properties.type.Property;

/**
 * Ein Undo f�r die �nderung eines Attributs.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class PropertyUndoActivity
    extends javax.swing.undo.AbstractUndoableEdit {

  /**
   * Das Attribut daselbst.
   */
  protected Property fProperty;
  /**
   * Der Zustand des Attributs vor der �nderung.
   */
  protected Property fBeforeModification;
  /**
   * Der Zustand des Attributs nach der �nderung.
   */
  protected Property fAfterModification;
  /**
   * Defaults to true; becomes false if this edit is undone, true
   * again if it is redone.
   */
  boolean hasBeenDone = true;
  /**
   * True if this edit has not received <code>die</code>; defaults
   * to <code>true</code>.
   */
  boolean alive = true;

  /**
   * Creates a new instance of PropertiesUndoActivity
   *
   * @param property
   */
  public PropertyUndoActivity(Property property) {
    fProperty = property;
  }

  /**
   * Erstellt eine Momentaufnahme vor der �nderung des Attributs.
   */
  public void snapShotBeforeModification() {
    fBeforeModification = createMemento();
  }

  /**
   * Erstellt eine Momentaufnahme nach der �nderung des Attributs.
   */
  public void snapShotAfterModification() {
    fAfterModification = createMemento();
  }

  /**
   * Erzeugt eine Momentaufnahme des aktuellen Zustands des Attributs.
   *
   * @return
   */
  protected Property createMemento() {
    return (Property) fProperty.clone();
  }

  @Override
  public String getPresentationName() {
    return fProperty.getDescription();
  }

  @Override
  public void die() {
    alive = false;
  }

  @Override
  public void undo() throws CannotUndoException {
    fProperty.copyFrom(fBeforeModification);
    fProperty.markChanged();
    fProperty.getModel().propertiesChanged(new NullAttributesChangeListener());
    hasBeenDone = false;
  }

  @Override
  public void redo() throws CannotRedoException {
    fProperty.copyFrom(fAfterModification);
    fProperty.markChanged();
    fProperty.getModel().propertiesChanged(new NullAttributesChangeListener());
    hasBeenDone = true;
  }

  @Override
  public boolean canUndo() {
    return alive && hasBeenDone;
  }

  @Override
  public boolean canRedo() {
    return alive && !hasBeenDone;
  }
}
