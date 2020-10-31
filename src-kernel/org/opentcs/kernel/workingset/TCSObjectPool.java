/*
 * openTCS copyright information:
 * Copyright (c) 2006 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.workingset;

import java.util.BitSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.inject.Inject;
import org.opentcs.data.ObjectExistsException;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObject;
import org.opentcs.data.TCSObjectEvent;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.util.UniqueStringGenerator;
import org.opentcs.util.eventsystem.CentralEventHub;
import org.opentcs.util.eventsystem.DummyEventListener;
import org.opentcs.util.eventsystem.EventListener;
import org.opentcs.util.eventsystem.TCSEvent;

/**
 * A container for <code>TCSObject</code>s belonging together.
 * It keeps all basic data objects (model data, transport order data and system
 * messages) and ensures these objects have unique IDs and names.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class TCSObjectPool {

  /**
   * This class's Logger.
   */
  private static final Logger log =
      Logger.getLogger(TCSObjectPool.class.getName());
  /**
   * The objects contained in this pool, indexed by their IDs.
   * Note that this has to be an <code>AutoGrowingArrayList</code> because its
   * <code>set()</code> method will be called with indices greater than its
   * <code>size()</code>!
   */
  private final List<TCSObject<?>> objectsById = new AutoGrowingArrayList<>();
  /**
   * The objects contained in this pool, mapped by their names.
   */
  private final Map<String, TCSObject<?>> objectsByName = new LinkedHashMap<>();
  /**
   * A set of bits representing the IDs used in this object pool. Each bit in
   * the set represents the ID equivalent to the bit's index.
   */
  private final BitSet idBits = new BitSet();
  /**
   * The generator providing unique names for objects in this pool.
   */
  private final UniqueStringGenerator objectNameGenerator =
      new UniqueStringGenerator();
  /**
   * A listener for events concerning the stored objects.
   */
  private final EventListener<TCSEvent> objectEventListener;

  /**
   * Creates a new TCSObjectPool.
   */
  public TCSObjectPool() {
    this(new DummyEventListener<TCSEvent>());
  }
  
  /**
   * Creates a new instance that uses the given event listener.
   *
   * @param eventListener The event listener to be used.
   */
  @Inject
  public TCSObjectPool(@CentralEventHub EventListener<TCSEvent> eventListener) {
    log.finer("method entry");
    objectEventListener = Objects.requireNonNull(eventListener,
                                                 "eventListener is null");
  }

  /**
   * Adds a new object to the pool.
   *
   * @param newObject The object to be added to the pool.
   * @throws ObjectExistsException If an object with the same ID or the same
   * name as the new one already exists in this pool.
   */
  public void addObject(TCSObject<?> newObject)
      throws ObjectExistsException {
    log.finer("method entry");
    if (newObject == null) {
      throw new NullPointerException("newObject is null");
    }
    int newObjectId = newObject.getId();
    if (objectsById.get(newObjectId) != null) {
      throw new ObjectExistsException(
          "Object ID " + newObject.getId() + " occupied by object named "
          + objectsById.get(newObjectId).getName());
    }
    if (objectsByName.containsKey(newObject.getName())) {
      throw new ObjectExistsException(
          "Object name " + newObject.getName()
          + " occupied by object with ID "
          + objectsByName.get(newObject.getName()).getId());
    }
    objectsById.set(newObjectId, newObject);
    objectsByName.put(newObject.getName(), newObject);
    idBits.set(newObject.getId());
    objectNameGenerator.addString(newObject.getName());
  }

  /**
   * Returns an object from the pool.
   *
   * @param ref A reference to the object to return.
   * @return The referenced object, or <code>null</code>, if no such object
   * exists in this pool.
   */
  public TCSObject<?> getObject(TCSObjectReference<?> ref) {
    log.finer("method entry");
    return objectsById.get(ref.getId());
  }

  /**
   * Returns an object from the pool.
   *
   * @param <T> The object's type.
   * @param clazz The class of the object to be returned.
   * @param ref A reference to the object to be returned.
   * @return The referenced object, or <code>null</code>, if no such object
   * exists in this pool or if an object exists but is not an instance of the
   * given class.
   */
  public <T extends TCSObject<T>> T getObject(Class<T> clazz,
                                              TCSObjectReference<T> ref) {
    Objects.requireNonNull(clazz, "clazz is null");
    Objects.requireNonNull(ref, "ref is null");

    TCSObject<?> result = objectsById.get(ref.getId());
    if (clazz.isInstance(result)) {
      return clazz.cast(result);
    }
    else {
      return null;
    }
  }

  /**
   * Returns an object from the pool.
   *
   * @param name The name of the object to return.
   * @return The object with the given name, or <code>null</code>, if no such
   * object exists in this pool.
   */
  public TCSObject<?> getObject(String name) {
    log.finer("method entry");
    if (name == null) {
      throw new NullPointerException("name is null");
    }
    return objectsByName.get(name);
  }

  /**
   * Returns an object from the pool.
   *
   * @param <T> The object's type.
   * @param clazz The class of the object to be returned.
   * @param name The name of the object to be returned.
   * @return The named object, or <code>null</code>, if no such object
   * exists in this pool or if an object exists but is not an instance of the
   * given class.
   */
  public <T extends TCSObject<T>> T getObject(Class<T> clazz,
                                              String name) {
    Objects.requireNonNull(clazz, "clazz is null");
    Objects.requireNonNull(name, "name is null");

    TCSObject<?> result = objectsByName.get(name);
    if (clazz.isInstance(result)) {
      return clazz.cast(result);
    }
    else {
      return null;
    }
  }

  /**
   * Returns a set of objects whose names match the given regular expression.
   *
   * @param regexp The regular expression that the names of objects to return
   * must match. If <code>null</code>, all objects contained in this object pool
   * are returned.
   * @return A set of objects whose names match the given regular expression.
   * If no such objects exist, the returned set is empty.
   */
  public Set<TCSObject<?>> getObjects(Pattern regexp) {
    log.finer("method entry");
    Set<TCSObject<?>> result = new HashSet<>();
    if (regexp == null) {
      result.addAll(objectsByName.values());
    }
    else {
      for (TCSObject<?> curObject : objectsByName.values()) {
        if (regexp.matcher(curObject.getName()).matches()) {
          result.add(curObject);
        }
      }
    }
    return result;
  }

  /**
   * Returns a set of objects belonging to the given class.
   *
   * @param <T> The objects' type.
   * @param clazz The class of the objects to be returned.
   * @return A set of objects belonging to the given class.
   */
  public <T extends TCSObject<T>> Set<T> getObjects(Class<T> clazz) {
    return getObjects(clazz, null);
  }

  /**
   * Returns a set of objects belonging to the given class whose names match the
   * given regular expression.
   *
   * @param <T> The objects' type.
   * @param clazz The class of the objects to be returned.
   * @param regexp The regular expression that the names of objects to return
   * must match. If <code>null</code>, all objects contained in this object pool
   * are returned.
   * @return A set of objects belonging to the given class whose names match the
   * given regular expression. If no such objects exist, the returned set is
   * empty.
   */
  public <T extends TCSObject<T>> Set<T> getObjects(Class<T> clazz,
                                                    Pattern regexp) {
    Objects.requireNonNull(clazz, "clazz is null");
    Set<T> result = new HashSet<>();
    for (TCSObject<?> curObject : objectsByName.values()) {
      if (clazz.isInstance(curObject)
          && (regexp == null
              || regexp.matcher(curObject.getName()).matches())) {
        result.add(clazz.cast(curObject));
      }
    }
    return result;
  }

  /**
   * Renames an object.
   *
   * @param ref A reference to the object to be renamed.
   * @param newName The object's new/future name.
   * @throws ObjectUnknownException If the referenced object does not exist.
   * @throws ObjectExistsException If the object cannot be renamed because
   * there is already an object named <code>newName</code>.
   */
  public void renameObject(TCSObjectReference<?> ref, String newName)
      throws ObjectUnknownException, ObjectExistsException {
    log.finer("method entry");
    if (ref == null) {
      throw new NullPointerException("ref is null");
    }
    if (newName == null) {
      throw new NullPointerException("newName is null");
    }
    TCSObject<?> object = objectsById.get(ref.getId());
    if (object == null) {
      throw new ObjectUnknownException("No such object in this pool.");
    }
    // Remember the previous state.
    TCSObject<?> previousState = object.clone();
    // Check if there is not already an object with the given name. Make an
    // exception for objects being reassigned their current names.
    if (!object.getName().equals(newName)
        && objectsByName.containsKey(newName)) {
      throw new ObjectExistsException("old name: '" + object.getName()
          + "', new name: '" + newName + "'");
    }
    // Perform the renaming.
    objectsByName.remove(object.getName());
    objectNameGenerator.removeString(object.getName());
    object.setName(newName);
    objectsByName.put(newName, object);
    objectNameGenerator.addString(newName);

    // Emit an event for the modified object.
    emitObjectEvent(object.clone(),
                    previousState,
                    TCSObjectEvent.Type.OBJECT_MODIFIED);
  }

  /**
   * Checks if this pool contains an object with the given name.
   *
   * @param objectName The name of the object whose existence in this pool is to
   * be checked.
   * @return <code>true</code> if, and only if, this pool contains an object
   * with the given name.
   */
  public boolean contains(String objectName) {
    log.finer("method entry");
    if (objectName == null) {
      throw new NullPointerException("objectName is null");
    }
    return objectsByName.containsKey(objectName);
  }

  /**
   * Removes a referenced object from this pool.
   *
   * @param ref A reference to the object to be removed.
   * @return The object that was removed from the pool.
   * @throws ObjectUnknownException If the referenced object does not exist.
   */
  public TCSObject<?> removeObject(TCSObjectReference<?> ref)
      throws ObjectUnknownException {
    log.finer("method entry");
    if (ref == null) {
      throw new NullPointerException("ref is null");
    }
    TCSObject<?> rmObject = objectsById.get(ref.getId());
    if (rmObject == null) {
      throw new ObjectUnknownException(ref);
    }
    objectsById.set(ref.getId(), null);
    objectsByName.remove(rmObject.getName());
    idBits.clear(ref.getId());
    objectNameGenerator.removeString(rmObject.getName());
    return rmObject;
  }

  /**
   * Removes the objects with the given names from this pool.
   *
   * @param objectNames A set of names of objects to be removed.
   * @return The objects that were removed from the pool. If none were removed,
   * an empty set will be returned.
   */
  public Set<TCSObject<?>> removeObjects(Set<String> objectNames) {
    log.finer("method entry");
    if (objectNames == null) {
      throw new NullPointerException("objectNames is null");
    }
    Set<TCSObject<?>> result = new HashSet<>();
    for (String curName : objectNames) {
      TCSObject<?> removedObject = objectsByName.remove(curName);
      if (removedObject != null) {
        result.add(removedObject);
        objectsById.set(removedObject.getId(), null);
        idBits.clear(removedObject.getId());
        objectNameGenerator.removeString(removedObject.getName());
      }
    }
    return result;
  }

  /**
   * Sets a property for the referenced object.
   *
   * @param ref A reference to the object to be modified.
   * @param key The property's key/name.
   * @param value The property's value. If <code>null</code>, removes the
   * property from the object.
   * @throws ObjectUnknownException If the referenced object does not exist.
   */
  public void setObjectProperty(TCSObjectReference<?> ref,
                                String key,
                                String value)
      throws ObjectUnknownException {
    log.finer("method entry");
    if (ref == null) {
      throw new NullPointerException("ref is null");
    }
    final TCSObject<?> object = objectsById.get(ref.getId());
    if (object == null) {
      throw new ObjectUnknownException("No object with ID " + ref.getId());
    }
    final TCSObject<?> previousState = object.clone();
    log.fine("Setting property: " + ref.getName() + ", " + key + ", " + value);
    object.setProperty(key, value);
    emitObjectEvent(object.clone(),
                    previousState,
                    TCSObjectEvent.Type.OBJECT_MODIFIED);
  }

  /**
   * Clears all of the referenced object's properties.
   *
   * @param ref A reference to the object to be modified.
   * @throws ObjectUnknownException If the referenced object does not exist.
   */
  public void clearObjectProperties(TCSObjectReference<?> ref)
      throws ObjectUnknownException {
    log.finer("method entry");
    if (ref == null) {
      throw new NullPointerException("ref is null");
    }
    final TCSObject<?> object = objectsById.get(ref.getId());
    if (object == null) {
      throw new ObjectUnknownException("No object with ID " + ref.getId());
    }
    final TCSObject<?> previousState = object.clone();
    object.clearProperties();
    emitObjectEvent(object.clone(),
                    previousState,
                    TCSObjectEvent.Type.OBJECT_MODIFIED);
  }

  /**
   * Returns the number of objects kept in this pool.
   *
   * @return The number of objects kept in this pool.
   */
  public int size() {
    log.finer("method entry");
    return objectsByName.size();
  }

  /**
   * Checks if this pool does not contain any objects.
   *
   * @return {@code true} if, and only if, this pool does not contain any
   * objects.
   */
  public boolean isEmpty() {
    log.finer("method entry");
    return objectsByName.isEmpty();
  }

  /**
   * Returns a name that is unique among all known objects in this object pool.
   * The returned name will consist of the given prefix followed by an integer
   * formatted according to the given pattern. The pattern has to be of the form
   * understood by <code>java.text.DecimalFormat</code>.
   *
   * @param prefix The prefix of the name to be generated.
   * @param suffixPattern A pattern describing the suffix of the generated name.
   * Must be of the form understood by <code>java.text.DecimalFormat</code>.
   * @return A name that is unique among all known objects.
   */
  public String getUniqueObjectName(String prefix, String suffixPattern) {
    log.finer("method entry");
    return objectNameGenerator.getUniqueString(prefix, suffixPattern);
  }

  /**
   * Returns an object ID that is unique among all known objects in this object
   * pool.
   *
   * @return An object ID that is unique among all known objects in this pool.
   */
  public int getUniqueObjectId() {
    log.finer("method entry");
    return idBits.nextClearBit(0);
  }

  /**
   * Emits an event for the given object with the given type.
   *
   * @param currentObjectState The current state of the object to emit an event
   * for.
   * @param previousObjectState The previous state of the object to emit an
   * event for.
   * @param evtType The type of event to emit.
   */
  public void emitObjectEvent(TCSObject<?> currentObjectState,
                              TCSObject<?> previousObjectState,
                              TCSObjectEvent.Type evtType) {
    TCSObjectEvent event = new TCSObjectEvent(currentObjectState,
                                              previousObjectState,
                                              evtType);
    objectEventListener.processEvent(event);
  }
}
