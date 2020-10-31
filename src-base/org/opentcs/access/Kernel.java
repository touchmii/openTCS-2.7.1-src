/*
 * openTCS copyright information:
 * Copyright (c) 2005 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access;

import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.opentcs.access.queries.Query;
import org.opentcs.data.ObjectExistsException;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObject;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.message.Message;
import org.opentcs.data.model.Block;
import org.opentcs.data.model.Group;
import org.opentcs.data.model.Layout;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.StaticRoute;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.model.visualization.LayoutElement;
import org.opentcs.data.model.visualization.ViewBookmark;
import org.opentcs.data.model.visualization.VisualLayout;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.DriveOrder.Destination;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.data.user.UserExistsException;
import org.opentcs.data.user.UserPermission;
import org.opentcs.data.user.UserUnknownException;
import org.opentcs.util.eventsystem.EventSource;
import org.opentcs.util.eventsystem.TCSEvent;

/**
 * Declares the methods the openTCS kernel must implement which are accessible
 * both to internal components and remote peers (like graphical user
 * interfaces).
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface Kernel
    extends EventSource<TCSEvent> {

  /**
   * The default name used for the empty model created on startup.
   */
  String DEFAULT_MODEL_NAME = "unnamed";

  /**
   * Returns the permissions the calling client is granted.
   *
   * @return The permissions the calling client is granted.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  Set<UserPermission> getUserPermissions()
      throws CredentialsException;

  /**
   * Creates a new user account.
   *
   * @param userName The new user's name.
   * @param userPassword The new user's password.
   * @param userPermissions The new user's permissions.
   * @throws UserExistsException If a user with the given name exists already.
   * @throws UnsupportedKernelOpException If user management is not
   * implemented.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  void createUser(String userName, String userPassword,
                  Set<UserPermission> userPermissions)
      throws UserExistsException, UnsupportedKernelOpException,
             CredentialsException;

  /**
   * Changes a user's password.
   *
   * @param userName The name of the user for which the password is to be
   * changed.
   * @param userPassword The user's new password.
   * @throws UserUnknownException If a user with the given name does not exist.
   * @throws UnsupportedKernelOpException If user management is not
   * implemented.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  void setUserPassword(String userName, String userPassword)
      throws UserUnknownException, UnsupportedKernelOpException,
             CredentialsException;

  /**
   * Changes a user's permissions.
   *
   * @param userName The name of the user for which the permissions are to be
   * changed.
   * @param userPermissions The user's new permissions.
   * @throws UserUnknownException If a user with the given name does not exist.
   * @throws UnsupportedKernelOpException If user management is not
   * implemented.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  void setUserPermissions(String userName, Set<UserPermission> userPermissions)
      throws UserUnknownException, UnsupportedKernelOpException,
             CredentialsException;

  /**
   * Removes a user account.
   *
   * @param userName The name of the user whose account is to be removed.
   * @throws UserUnknownException If a user with the given name does not exist.
   * @throws UnsupportedKernelOpException If user management is not
   * implemented.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  void removeUser(String userName)
      throws UserUnknownException, UnsupportedKernelOpException,
             CredentialsException;

  /**
   * Returns the current state of the kernel.
   *
   * @return The current state of the kernel.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  State getState()
      throws CredentialsException;

  /**
   * Sets the current state of the kernel.
   *
   * @param newState The state the kernel is to be set to.
   * @throws IllegalArgumentException If setting the new state is not possible,
   * e.g. because a transition from the current to the new state is not allowed.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  void setState(State newState)
      throws IllegalArgumentException, CredentialsException;

  /**
   * Returns a set of names of models that are available.
   *
   * @return A set of names of available models. If no models are available, the
   * returned set is empty.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  Set<String> getModelNames()
      throws CredentialsException;

  /**
   * Returns the name of the currently loaded model.
   *
   * @return The name of the currently loaded model.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  String getCurrentModelName()
      throws CredentialsException;

  /**
   * Replaces the kernel's current model with an empty one.
   *
   * @param modelName The newly created model's name.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  void createModel(String modelName)
      throws CredentialsException;

  /**
   * Loads the model with the specified name into the kernel.
   *
   * @param modelName The name of the model to be loaded.
   * @throws IOException If a model with the given name could not be found or
   * loaded.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  void loadModel(String modelName)
      throws IOException, CredentialsException;

  /**
   * Saves the current model under the given name.
   *
   * @param modelName The name under which the current model is to be saved. If
   * <code>null</code>, the model's current name will be used, otherwise the
   * model will be renamed accordingly.
   * @param overwrite If <code>true</code>, any existing model with the same
   * name will be overwritten.
   * @throws IOException If a model with the given name exists and
   * <code>overwrite</code> is <code>false</code> or if the model could not be
   * persisted for some reason. Note that for consistency reasons and to avoid
   * confusion and possible data loss with case insensitive file systems,
   * implementations of this method also throw an exception if a model with the
   * given name exists but the name differs in case. This effectively means that
   * the model's name must either not exist at all, yet, or must match an
   * existing name exactly, including the case of the spelling.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  void saveModel(String modelName, boolean overwrite)
      throws IOException, CredentialsException;

  /**
   * Removes a saved model.
   *
   * @param rmName The name of the model to be removed.
   * @throws FileNotFoundException If a model with the given name does not
   * exist.
   * @throws IOException If deleting the model was not possible for some reason.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  void removeModel(String rmName)
      throws FileNotFoundException, IOException, CredentialsException;

  /**
   * Returns a single TCSObject of the given class.
   *
   * @param <T> The TCSObject's actual type.
   * @param clazz The class of the object to be returned.
   * @param ref A reference to the object to be returned.
   * @return A copy of the referenced object, or <code>null</code> if no such
   * object exists or if an object exists but is not an instance of the given
   * class.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  <T extends TCSObject<T>> T getTCSObject(Class<T> clazz,
                                          TCSObjectReference<T> ref)
      throws CredentialsException;

  /**
   * Returns a single TCSObject of the given class.
   *
   * @param <T> The TCSObject's actual type.
   * @param clazz The class of the object to be returned.
   * @param name The name of the object to be returned.
   * @return A copy of the named object, or <code>null</code> if no such
   * object exists or if an object exists but is not an instance of the given
   * class.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  <T extends TCSObject<T>> T getTCSObject(Class<T> clazz,
                                          String name)
      throws CredentialsException;

  /**
   * Returns all existing TCSObjects of the given class.
   *
   * @param <T> The TCSObjects' actual type.
   * @param clazz The class of the objects to be returned.
   * @return Copies of all existing objects of the given class.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  <T extends TCSObject<T>> Set<T> getTCSObjects(Class<T> clazz)
      throws CredentialsException;

  /**
   * Returns all existing TCSObjects of the given class whose names match the
   * given pattern.
   *
   * @param <T> The TCSObjects' actual type.
   * @param clazz The class of the objects to be returned.
   * @param regexp A regular expression describing the names of the objects to
   * be returned; if <code>null</code>, all objects of the given class are
   * returned.
   * @return Copies of all existing objects of the given class whose names match
   * the given pattern. If no such objects exist, the returned set will be
   * empty.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  <T extends TCSObject<T>> Set<T> getTCSObjects(Class<T> clazz, Pattern regexp)
      throws CredentialsException;

  /**
   * Rename a TCSObject.
   *
   * @param ref A reference to the object to be renamed.
   * @param newName The object's new name.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @throws ObjectUnknownException If the referenced object does not exist.
   * @throws ObjectExistsException If the object cannot be renamed because there
   * is already an object with the given new name.
   */
  void renameTCSObject(TCSObjectReference<?> ref, String newName)
      throws CredentialsException, ObjectUnknownException, ObjectExistsException;

  /**
   * Sets an object's property.
   *
   * @param ref A reference to the object to be modified.
   * @param key The property's key.
   * @param value The property's (new) value. If <code>null</code>, removes the
   * property from the object.
   * @throws ObjectUnknownException If the referenced object does not exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  void setTCSObjectProperty(TCSObjectReference<?> ref, String key, String value)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Clears all of an object's properties.
   *
   * @param ref A reference to the object to be modified.
   * @throws ObjectUnknownException If the referenced object does not exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  void clearTCSObjectProperties(TCSObjectReference<?> ref)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Remove a TCSObject.
   *
   * @param ref A reference to the object to be removed.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @throws ObjectUnknownException If the referenced object does not exist.
   */
  void removeTCSObject(TCSObjectReference<?> ref)
      throws CredentialsException, ObjectUnknownException;
  
  /**
   * Creates a new message with the given content and type.
   *
   * @param message The message's content.
   * @param type The message's type.
   * @return A copy of the newly created message.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  Message publishMessage(String message, Message.Type type)
      throws CredentialsException;

  /**
   * Adds a layout to the current model.
   * A new layout is created with a unique ID and name and all other attributes
   * set to default values. A copy of the newly created layout is then returned.
   *
   * @param layoutData The layout's actual data.
   * @return A copy of the newly created layout.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Don't use {@link Layout} any more. Use {@link VisualLayout}
   * instead.
   */
  Layout createLayout(byte[] layoutData)
      throws CredentialsException;

  /**
   * Sets the layout data of a layout.
   *
   * @param ref A reference to the layout to be modified.
   * @param newData The layout's new data.
   * @throws ObjectUnknownException If the referenced layout does not exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Don't use {@link Layout} any more. Use {@link VisualLayout}
   * instead.
   */
  void setLayoutData(TCSObjectReference<Layout> ref, byte[] newData)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Adds a visual layout to the current model.
   * A new layout is created with a unique ID and name and all other attributes
   * set to default values. A copy of the newly created layout is then returned.
   *
   * @return A copy of the newly created layout.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  VisualLayout createVisualLayout()
      throws CredentialsException;

  /**
   * Sets a layout's scale on the X axis.
   *
   * @param ref A reference to the layout to be modified.
   * @param scaleX The layout's new scale on the X axis.
   * @throws ObjectUnknownException If the referenced layout does not exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  void setVisualLayoutScaleX(TCSObjectReference<VisualLayout> ref,
                             double scaleX)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Sets a layout's scale on the Y axis.
   *
   * @param ref A reference to the layout to be modified.
   * @param scaleY The layout's new scale on the Y axis.
   * @throws ObjectUnknownException If the referenced layout does not exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  void setVisualLayoutScaleY(TCSObjectReference<VisualLayout> ref,
                             double scaleY)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Sets a layout's colors.
   *
   * @param ref A reference to the layout to be modified.
   * @param colors The layout's new colors.
   * @throws ObjectUnknownException If the referenced layout does not exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  void setVisualLayoutColors(TCSObjectReference<VisualLayout> ref,
                             Map<String, Color> colors)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Sets a layout's elements.
   *
   * @param ref A reference to the layout to be modified.
   * @param elements The layout's new elements.
   * @throws ObjectUnknownException If the referenced layout does not exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  void setVisualLayoutElements(TCSObjectReference<VisualLayout> ref,
                               Set<LayoutElement> elements)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Sets a layout's view bookmarks.
   *
   * @param ref A reference to the layout to be modified.
   * @param bookmarks The layout's new bookmarks.
   * @throws ObjectUnknownException If the referenced layout does not exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  void setVisualLayoutViewBookmarks(TCSObjectReference<VisualLayout> ref,
                                    List<ViewBookmark> bookmarks)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Adds a point to the current model.
   * A new point is created with a unique ID and name and all other attributes
   * set to default values. A copy of the newly created point is then returned.
   *
   * @return A copy of the newly created point.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  Point createPoint()
      throws CredentialsException;

  /**
   * Sets the physical coordinates of a point.
   *
   * @param ref A reference to the point to be modified.
   * @param position The point's new coordinates.
   * @throws ObjectUnknownException If the referenced point does not exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  void setPointPosition(TCSObjectReference<Point> ref, Triple position)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Sets the vehicle's (assumed) orientation angle at the given position.
   * The allowed value range is [-360.0..360.0], and <code>Double.NaN</code>, to
   * indicate that the vehicle's orientation at the point is unspecified.
   *
   * @param ref A reference to the point to be modified.
   * @param angle The new angle.
   * @throws ObjectUnknownException If the referenced point does not exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  void setPointVehicleOrientationAngle(TCSObjectReference<Point> ref,
                                       double angle)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Sets the type of a point.
   *
   * @param ref A reference to the point to be modified.
   * @param newType The point's new type.
   * @throws ObjectUnknownException If the referenced point does not exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  void setPointType(TCSObjectReference<Point> ref, Point.Type newType)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Adds a path to the current model.
   * A new path is created with a generated unique ID and name and ending in the
   * point specified by the given name; all other attributes set to default
   * values. Furthermore, the point is registered with the point which it
   * originates in and with the one it ends in. A copy of the newly created path
   * is then returned.
   *
   * @param srcRef A reference to the point which the newly created path
   * originates in.
   * @param destRef A reference to the point which the newly created path ends
   * in.
   * @return A copy of the newly created path.
   * @throws ObjectUnknownException If any of the referenced points does not
   * exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  Path createPath(TCSObjectReference<Point> srcRef,
                  TCSObjectReference<Point> destRef)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Sets the length of a path.
   *
   * @param ref A reference to the path to be modified.
   * @param length The new length of the path, in millimetres.
   * @throws ObjectUnknownException If the referenced path does not exist.
   * @throws IllegalArgumentException If <code>length</code> is zero or
   * negative.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  void setPathLength(TCSObjectReference<Path> ref, long length)
      throws ObjectUnknownException, IllegalArgumentException, CredentialsException;

  /**
   * Sets the routing cost of a path.
   *
   * @param ref A reference to the path to be modified.
   * @param cost The new routing cost of the path (unitless).
   * @throws ObjectUnknownException If the referenced path does not exist.
   * @throws IllegalArgumentException If <code>cost</code> is zero or negative.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  void setPathRoutingCost(TCSObjectReference<Path> ref, long cost)
      throws ObjectUnknownException, IllegalArgumentException, CredentialsException;

  /**
   * Sets the maximum allowed velocity for a path.
   *
   * @param ref A reference to the path to be modified.
   * @param velocity The new maximum allowed velocity in mm/s.
   * @throws ObjectUnknownException If the referenced path does not exist.
   * @throws IllegalArgumentException If <code>velocity</code> is negative.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  void setPathMaxVelocity(TCSObjectReference<Path> ref, int velocity)
      throws ObjectUnknownException, IllegalArgumentException, CredentialsException;

  /**
   * Sets the maximum allowed reverse velocity for a path.
   *
   * @param ref A reference to the path to be modified.
   * @param velocity The new maximum reverse velocity, in mm/s.
   * @throws ObjectUnknownException If the referenced path does not exist.
   * @throws IllegalArgumentException If <code>velocity</code> is negative.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  void setPathMaxReverseVelocity(TCSObjectReference<Path> ref, int velocity)
      throws ObjectUnknownException, IllegalArgumentException, CredentialsException;

  /**
   * Locks/Unlocks a path.
   *
   * @param ref A reference to the path to be modified.
   * @param locked Indicates whether the path is to be locked
   * (<code>true</code>) or unlocked (<code>false</code>).
   * @throws ObjectUnknownException If the referenced path does not exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  void setPathLocked(TCSObjectReference<Path> ref, boolean locked)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Creates a new vehicle.
   * A new vehicle is created with a generated unique ID and name and all other
   * attributes set to default values. A copy of the newly created vehicle is
   * then returned.
   *
   * @return A copy of the newly created vehicle type.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  Vehicle createVehicle()
      throws CredentialsException;

  /**
   * Sets a vehicle's critical energy level.
   *
   * @param ref A reference to the vehicle to be modified.
   * @param energyLevel The vehicle's new critical energy level.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  void setVehicleEnergyLevelCritical(TCSObjectReference<Vehicle> ref,
                                     int energyLevel)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Sets a vehicle's good energy level.
   *
   * @param ref A reference to the vehicle to be modified.
   * @param energyLevel The vehicle's new good energy level.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  void setVehicleEnergyLevelGood(TCSObjectReference<Vehicle> ref,
                                 int energyLevel)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Sets a vehicle's length.
   *
   * @param ref A reference to the vehicle to be modified.
   * @param length The vehicle's new length.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  void setVehicleLength(TCSObjectReference<Vehicle> ref, int length)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Creates a new location type.
   * A new location type is created with a generated unique ID and name and all
   * other attributes set to default values. A copy of the newly created
   * location type is then returned.
   *
   * @return A copy of the newly created location type.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  LocationType createLocationType()
      throws CredentialsException;

  /**
   * Adds an allowed operation to a location type.
   *
   * @param ref A reference to the location type to be modified.
   * @param operation The operation to be allowed.
   * @throws ObjectUnknownException If the referenced location type does not
   * exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  void addLocationTypeAllowedOperation(TCSObjectReference<LocationType> ref,
                                       String operation)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Removes an allowed operation from a location type.
   *
   * @param ref A reference to the location type to be modified.
   * @param operation The operation to be disallowed.
   * @throws ObjectUnknownException If the referenced location type does not
   * exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  void removeLocationTypeAllowedOperation(TCSObjectReference<LocationType> ref,
                                          String operation)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Creates a new location.
   * A new location of the given type is created with a generated unique ID and
   * name and all other attributes set to default values. A copy of the newly
   * created location is then returned.
   *
   * @param typeRef A reference to the location type of which the newly created
   * location is supposed to be.
   * @return A copy of the newly created location.
   * @throws ObjectUnknownException If the reference location type does not
   * exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  Location createLocation(TCSObjectReference<LocationType> typeRef)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Sets the physical coordinates of a location.
   *
   * @param ref A reference to the location to be modified.
   * @param position The location's new coordinates.
   * @throws ObjectUnknownException If the referenced location does not exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  void setLocationPosition(TCSObjectReference<Location> ref, Triple position)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Sets a location's type.
   *
   * @param ref A reference to the location.
   * @param typeRef A reference to the location's new type.
   * @throws ObjectUnknownException If the referenced location or type do not
   * exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  void setLocationType(TCSObjectReference<Location> ref,
                       TCSObjectReference<LocationType> typeRef)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Connects a location to a point (expressing that the location is reachable
   * from that point).
   *
   * @param locRef A reference to the location.
   * @param pointRef A reference to the point.
   * @throws ObjectUnknownException If the referenced location or point does not
   * exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  void connectLocationToPoint(TCSObjectReference<Location> locRef,
                              TCSObjectReference<Point> pointRef)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Disconnects a location from a point (expressing that the location isn't
   * reachable from the point any more).
   *
   * @param locRef A reference to the location.
   * @param pointRef A reference to the point.
   * @throws ObjectUnknownException If the referenced location or point does not
   * exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  void disconnectLocationFromPoint(TCSObjectReference<Location> locRef,
                                   TCSObjectReference<Point> pointRef)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Adds an allowed operation to a link between a location and a point.
   *
   * @param locRef A reference to the location end of the link to be modified.
   * @param pointRef A reference to the point end of the link to be modified.
   * @param operation The operation to be added.
   * @throws ObjectUnknownException If any of the referenced objects does not
   * exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  void addLocationLinkAllowedOperation(TCSObjectReference<Location> locRef,
                                       TCSObjectReference<Point> pointRef,
                                       String operation)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Removes an allowed operation from a link between a location and a point.
   *
   * @param locRef A reference to the location end of the link to be modified.
   * @param pointRef A reference to the point end of the link to be modified.
   * @param operation The operation to be removed.
   * @throws ObjectUnknownException If any of the referenced objects does not
   * exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  void removeLocationLinkAllowedOperation(TCSObjectReference<Location> locRef,
                                          TCSObjectReference<Point> pointRef,
                                          String operation)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Removes all allowed operations (for all vehicle types) from a link between
   * a location and a point.
   *
   * @param locRef A reference to the location end of the link to be modified.
   * @param pointRef A reference to the point end of the link to be modified.
   * @throws ObjectUnknownException If any of the referenced objects does not
   * exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  void clearLocationLinkAllowedOperations(TCSObjectReference<Location> locRef,
                                          TCSObjectReference<Point> pointRef)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Adds a block to the current model.
   * A new block is created with a unique ID and name and all other attributes
   * set to default values. A copy of the newly created block is then returned.
   *
   * @return A copy of the newly created block.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  Block createBlock()
      throws CredentialsException;

  /**
   * Adds a member to a block.
   *
   * @param ref A reference to the block to be modified.
   * @param newMemberRef A reference to the new member.
   * @throws ObjectUnknownException If any of the referenced block and member do
   * not exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  void addBlockMember(TCSObjectReference<Block> ref,
                      TCSResourceReference<?> newMemberRef)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Removes a member from a block.
   *
   * @param ref A reference to the block to be modified.
   * @param rmMemberRef A reference to the member to be removed.
   * @throws ObjectUnknownException If the referenced block does not exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  void removeBlockMember(TCSObjectReference<Block> ref,
                         TCSResourceReference<?> rmMemberRef)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Adds a group to the current model.
   * A new group is created with a unique ID and name and all other attributes
   * set to default values. A copy of the newly created block is then returned.
   *
   * @return A copy of the newly created group.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  Group createGroup()
      throws CredentialsException;

  /**
   * Adds a member to a group.
   *
   * @param ref A reference to the group to be modified.
   * @param newMemberRef A reference to the new member.
   * @throws ObjectUnknownException If any of the referenced group and member do
   * not exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  void addGroupMember(TCSObjectReference<Group> ref,
                      TCSObjectReference<?> newMemberRef)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Removes a member from a group.
   *
   * @param ref A reference to the group to be modified.
   * @param rmMemberRef A reference to the member to be removed.
   * @throws ObjectUnknownException If the referenced group does not exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  void removeGroupMember(TCSObjectReference<Group> ref,
                         TCSObjectReference<?> rmMemberRef)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Adds a static route to the current model.
   * A new route is created with a unique ID and name and all other attributes
   * set to default values. A copy of the newly created route is then returned.
   *
   * @return A copy of the newly created route.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  StaticRoute createStaticRoute()
      throws CredentialsException;

  /**
   * Adds a hop to a route.
   *
   * @param ref A reference to the route to be modified.
   * @param newHopRef A reference to the new hop.
   * @throws ObjectUnknownException If any of the referenced route and hop do
   * not exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  void addStaticRouteHop(TCSObjectReference<StaticRoute> ref,
                         TCSObjectReference<Point> newHopRef)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Removes all hops from a route.
   *
   * @param ref A reference to the route to be modified.
   * @throws ObjectUnknownException If the referenced route does not exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  void clearStaticRouteHops(TCSObjectReference<StaticRoute> ref)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Attaches a resource to another one.
   *
   * @param resource A reference to the resource that is to receive the
   * attachment.
   * @param newResource A reference to the resource to be attached.
   * @throws ObjectUnknownException If any of the referenced resources does not
   * exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  void attachResource(TCSResourceReference<?> resource,
                      TCSResourceReference<?> newResource)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Detaches a resource from another one.
   *
   * @param resource A reference to the resource from which the attached
   * resource is to be removed.
   * @param rmResource A reference to the resource to be detached.
   * @throws ObjectUnknownException If any of the referenced resources does not
   * exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  void detachResource(TCSResourceReference<?> resource,
                      TCSResourceReference<?> rmResource)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Creates a new transport order.
   * A new transport order is created with a generated unique ID and name,
   * containing the given <code>DriveOrder</code>s and with all other attributes
   * set to their default values. A copy of the newly created transport order
   * is then returned.
   *
   * @param destinations The list of destinations that have to be travelled to
   * when processing this transport order.
   * @return A copy of the newly created transport order.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  TransportOrder createTransportOrder(List<Destination> destinations)
      throws CredentialsException;

  /**
   * Sets a transport order's deadline.
   *
   * @param ref A reference to the transport order to be modified.
   * @param deadline The transport order's new deadline.
   * @throws ObjectUnknownException If the referenced transport order does not
   * exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  void setTransportOrderDeadline(TCSObjectReference<TransportOrder> ref,
                                 long deadline)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Activates a transport order and makes it available for processing by a
   * vehicle.
   *
   * @param ref A reference to the transport order to be modified.
   * @throws ObjectUnknownException If the referenced transport order does not
   * exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  void activateTransportOrder(TCSObjectReference<TransportOrder> ref)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Sets a transport order's intended vehicle.
   *
   * @param orderRef A reference to the transport order to be modified.
   * @param vehicleRef A reference to the vehicle intended to process the order.
   * @throws ObjectUnknownException If the referenced transport order does not
   * exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  void setTransportOrderIntendedVehicle(
      TCSObjectReference<TransportOrder> orderRef,
      TCSObjectReference<Vehicle> vehicleRef)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Copies drive order data from a list of drive orders to the given transport
   * order's future drive orders.
   *
   * @param orderRef A reference to the transport order to be modified.
   * @param newOrders The drive orders containing the data to be copied into
   * this transport order's drive orders.
   * @throws ObjectUnknownException If the referenced transport order is not
   * in this pool.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @throws IllegalArgumentException If the destinations of the given drive
   * orders do not match the destinations of the drive orders in this transport
   * order.
   */
  void setTransportOrderFutureDriveOrders(
      TCSObjectReference<TransportOrder> orderRef,
      List<DriveOrder> newOrders)
      throws ObjectUnknownException, CredentialsException, IllegalArgumentException;

  /**
   * Adds a dependency to a transport order on another transport order.
   *
   * @param orderRef A reference to the order that the dependency is to be added
   * to.
   * @param newDepRef A reference to the order that is the new dependency.
   * @throws ObjectUnknownException If any of the referenced transport orders
   * does not exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  void addTransportOrderDependency(TCSObjectReference<TransportOrder> orderRef,
                                   TCSObjectReference<TransportOrder> newDepRef)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Removes a dependency from a transport order on another transport order.
   *
   * @param orderRef A reference to the order that the dependency is to be
   * removed from.
   * @param rmDepRef A reference to the order that is not to be depended on any
   * more.
   * @throws ObjectUnknownException If any of the referenced transport orders
   * does not exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  void removeTransportOrderDependency(
      TCSObjectReference<TransportOrder> orderRef,
      TCSObjectReference<TransportOrder> rmDepRef)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Creates a new order sequence.
   * A new order sequence is created with a generated unique ID and name. A copy
   * of the newly created order sequence is then returned.
   *
   * @return A copy of the newly created order sequence.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  OrderSequence createOrderSequence()
      throws CredentialsException;

  /**
   * Adds a transport order to an order sequence.
   *
   * @param seqRef A reference to the order sequence to be modified.
   * @param orderRef A reference to the transport order to be added.
   * @throws ObjectUnknownException If the referenced order sequence or
   * transport order is not in this pool.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @throws IllegalArgumentException If the sequence is already marked as
   * <em>complete</em>, if the sequence already contains the given order or
   * if the given transport order has already been activated.
   */
  void addOrderSequenceOrder(TCSObjectReference<OrderSequence> seqRef,
                             TCSObjectReference<TransportOrder> orderRef)
      throws ObjectUnknownException, CredentialsException, IllegalArgumentException;

  /**
   * Removes a transport order from an order sequence.
   *
   * @param seqRef A reference to the order sequence to be modified.
   * @param orderRef A reference to the transport order to be removed.
   * @throws ObjectUnknownException If the referenced order sequence or
   * transport order is not in this pool.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  void removeOrderSequenceOrder(TCSObjectReference<OrderSequence> seqRef,
                                TCSObjectReference<TransportOrder> orderRef)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Sets an order sequence's complete flag.
   *
   * @param seqRef A reference to the order sequence to be modified.
   * @throws ObjectUnknownException If the referenced order sequence does not
   * exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  void setOrderSequenceComplete(TCSObjectReference<OrderSequence> seqRef)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Sets an order sequence's <em>failureFatal</em> flag.
   *
   * @param seqRef A reference to the order sequence to be modified.
   * @param fatal The sequence's new <em>failureFatal</em> flag.
   * @throws ObjectUnknownException If the referenced order sequence does not
   * exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  void setOrderSequenceFailureFatal(TCSObjectReference<OrderSequence> seqRef,
                                    boolean fatal)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Sets an order sequence's intended vehicle.
   *
   * @param seqRef A reference to the order sequence to be modified.
   * @param vehicleRef A reference to the vehicle intended to process the order
   * sequence.
   * @throws ObjectUnknownException If the referenced order sequence or vehicle
   * does not exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  void setOrderSequenceIntendedVehicle(TCSObjectReference<OrderSequence> seqRef,
                                       TCSObjectReference<Vehicle> vehicleRef)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Withdraw the referenced order, set its state to FAILED and stop the vehicle
   * that might be processing it.
   * Calling this method once will initiate the withdrawal, leaving the
   * transport order assigned to the vehicle until it has finished the movements
   * that it has already been ordered to execute. The transport order's state
   * will change to WITHDRAWN. Calling this method a second time for the same
   * vehicle/order will withdraw the order from the vehicle without further
   * waiting.
   * 
   * @param ref A reference to the transport order to be withdrawn.
   * @param disableVehicle Whether setting the processing state of the vehicle
   * currently processing the transport order to
   * {@link org.opentcs.data.model.Vehicle.ProcState#UNAVAILABLE} to prevent
   * immediate redispatching of the vehicle.
   * @throws ObjectUnknownException If the referenced transport order does not
   * exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  void withdrawTransportOrder(TCSObjectReference<TransportOrder> ref,
                              boolean disableVehicle)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Withdraw any order that a vehicle might be processing, set its state to
   * FAILED and stop the vehicle.
   * Calling this method once will initiate the withdrawal, leaving the
   * transport order assigned to the vehicle until it has finished the movements
   * that it has already been ordered to execute. The transport order's state
   * will change to WITHDRAWN. Calling this method a second time for the same
   * vehicle/order will withdraw the order from the vehicle without further
   * waiting.
   * 
   * @param vehicleRef A reference to the vehicle to be modified.
   * @param disableVehicle Whether setting the processing state of the vehicle
   * currently processing the transport order to
   * {@link org.opentcs.data.model.Vehicle.ProcState#UNAVAILABLE} to prevent
   * immediate redispatching of the vehicle.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  void withdrawTransportOrderByVehicle(TCSObjectReference<Vehicle> vehicleRef,
                                       boolean disableVehicle)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Explicitly trigger dispatching of the referenced idle vehicle.
   * 
   * @param vehicleRef A reference to the vehicle to be dispatched.
   * @param setIdleIfUnavailable Whether to set the vehicle's processing state
   * to IDLE before dispatching if it is currently UNAVAILABLE. If the vehicle's
   * processing state is UNAVAILABLE and this flag is not set, an
   * IllegalArgumentException will be thrown.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @throws IllegalArgumentException If the referenced vehicle is not in a
   * dispatchable state (IDLE or, if the corresponding flag is set, UNAVAILABLE).
   */
  void dispatchVehicle(TCSObjectReference<Vehicle> vehicleRef,
                       boolean setIdleIfUnavailable)
      throws ObjectUnknownException, CredentialsException, IllegalArgumentException;

  /**
   * Sends a message to the communication adapter associated with the referenced
   * vehicle.
   * This method provides a generic one-way communication channel to the
   * communication adapter of a vehicle. Note that there is no return value and
   * no guarantee that the communication adapter will understand the message;
   * clients cannot even know which communication adapter is attached to a
   * vehicle, so it's entirely possible that the communication adapter receiving
   * the message does not understand it.
   *
   * @param vehicleRef The vehicle whose communication adapter shall receive the
   * message.
   * @param message The message to be delivered.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  void sendCommAdapterMessage(TCSObjectReference<Vehicle> vehicleRef,
                              Object message)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Creates and returns a list of transport orders defined in a script file.
   *
   * @param fileName The name of the script file defining the transport orders
   * to be created.
   * @return The list of transport orders created. If none were created, the
   * returned list is empty.
   * @throws ObjectUnknownException If any object referenced in the script file
   * does not exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @throws IOException If there was a problem reading or parsing the file with
   * the given name.
   */
  List<TransportOrder> createTransportOrdersFromScript(String fileName)
      throws ObjectUnknownException, CredentialsException, IOException;

  /**
   * Returns the costs for travelling from one location to a given set of
   * others.
   *
   * @param vRef A reference to the vehicle that shall be used for calculating
   * the costs. If it's <code>null</code> a random vehicle will be used.
   * @param srcRef A reference to the source Location
   * @param destRefs A set containing all destination locations
   * @return A list containing tuples of a location and the costs to travel there
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @throws ObjectUnknownException If something is not known.
   */
  List<TravelCosts> getTravelCosts(TCSObjectReference<Vehicle> vRef,
                                   TCSObjectReference<Location> srcRef,
                                   Set<TCSObjectReference<Location>> destRefs)
      throws CredentialsException, ObjectUnknownException;

  /**
   * Returns the result of the query defined by the given class.
   *
   * @param <T> The result's actual type.
   * @param clazz Defines the query and the class of the result to be returned.
   * @return The result of the query defined by the given class, or
   * <code>null</code>, if the defined query is not supported in the kernel's
   * current state.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  <T extends Query<T>> T query(Class<T> clazz)
      throws CredentialsException;

  /**
   * Returns the current time factor for simulation.
   *
   * @return The current time factor for simulation.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  double getSimulationTimeFactor()
      throws CredentialsException;

  /**
   * Sets a time factor for simulation.
   * 
   * @param factor The new time factor.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  void setSimulationTimeFactor(double factor)
      throws CredentialsException;

  /**
   * Returns all configuration items existing in the kernel.
   * 
   * @return All configuration items existing in the kernel.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  Set<ConfigurationItemTO> getConfigurationItems()
      throws CredentialsException;

  /**
   * Sets a single configuration item in the kernel.
   *
   * @param itemTO The configuration item to be set.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  void setConfigurationItem(ConfigurationItemTO itemTO)
      throws CredentialsException;

  /**
   * The various states a kernel instance may be running in.
   */
  public enum State {

    /**
     * The state in which the model/topology is created and parameters are set.
     */
    MODELLING,
    /**
     * The normal mode of operation in which transport orders may be accepted
     * and dispatched to vehicles.
     */
    OPERATING,
    /**
     * A transitional state the kernel is in while shutting down.
     */
    SHUTDOWN
  }
}
