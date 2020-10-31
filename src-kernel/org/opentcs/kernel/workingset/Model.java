/*
 * openTCS copyright information:
 * Copyright (c) 2006 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.workingset;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.inject.Inject;
import org.opentcs.data.ObjectExistsException;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObject;
import org.opentcs.data.TCSObjectEvent;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Block;
import org.opentcs.data.model.Group;
import org.opentcs.data.model.Layout;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.StaticRoute;
import org.opentcs.data.model.TCSResource;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.model.visualization.LayoutElement;
import org.opentcs.data.model.visualization.ViewBookmark;
import org.opentcs.data.model.visualization.VisualLayout;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.drivers.CommunicationAdapter;
import org.opentcs.drivers.LoadHandlingDevice;

/**
 * Instances of this class present a view on the complete static topology of an
 * openTCS model, i.e. Points, Paths etc., and Vehicless, contained
 * in a {@link TCSObjectPool TCSObjectPool}.
 * <p>
 * Note that no synchronization is done inside this class. Concurrent access of
 * instances of this class must be synchronized externally.
 * </p>
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class Model {

  /**
   * This class's Logger.
   */
  private static final Logger log = Logger.getLogger(Model.class.getName());
  /**
   * The system's global object pool.
   */
  private final TCSObjectPool objectPool;
  /**
   * This model's name.
   */
  private String name = "";

  /**
   * Creates a new model.
   *
   * @param globalPool The object pool serving as the container for this model's
   * data.
   */
  @Inject
  public Model(TCSObjectPool globalPool) {
    this.objectPool = Objects.requireNonNull(globalPool);
  }

  /**
   * Returns the <code>TCSObjectPool</code> serving as the container for this
   * model's data.
   *
   * @return The <code>TCSObjectPool</code> serving as the container for this
   * model's data.
   */
  public TCSObjectPool getObjectPool() {
    log.finer("method entry");
    return objectPool;
  }

  /**
   * Returns this model's name.
   *
   * @return This model's name.
   */
  public String getName() {
    log.finer("method entry");
    return name;
  }

  /**
   * Sets this model's name.
   *
   * @param newName This model's new name.
   */
  public void setName(String newName) {
    log.finer("method entry");
    if (newName == null) {
      throw new NullPointerException("newName is null");
    }
    name = newName;
  }

  /**
   * Removes all model objects from this model and the object pool by which it
   * is backed.
   */
  public void clear() {
    log.finer("method entry");
    for (TCSObject<?> curObject : objectPool.getObjects((Pattern) null)) {
      if (curObject instanceof Point
          || curObject instanceof Path
          || curObject instanceof Vehicle
          || curObject instanceof LocationType
          || curObject instanceof Location
          || curObject instanceof Block
          || curObject instanceof Group
          || curObject instanceof Layout
          || curObject instanceof StaticRoute
          || curObject instanceof VisualLayout) {
        objectPool.removeObject(curObject.getReference());
        objectPool.emitObjectEvent(null,
                                   curObject,
                                   TCSObjectEvent.Type.OBJECT_REMOVED);
      }
    }
  }

  /**
   * Creates a new layout with a unique name and all other attributes set to
   * default values.
   *
   * @param objectID The object ID of the newly created layout. If
   * <code>null</code>, a new, unique one will be generated.
   * @param layoutData The actual data the layout is supposed to contain.
   * @return The newly created layout.
   */
  public Layout createLayout(Integer objectID, byte[] layoutData) {
    log.finer("method entry");
    // Get a unique ID and name for the new point and create an instance.
    int layoutID =
        objectID != null ? objectID : objectPool.getUniqueObjectId();
    String layoutName = objectPool.getUniqueObjectName("Layout-", "00");
    Layout newLayout = new Layout(layoutID, layoutName, layoutData);
    // Store the instance in the global object pool.
    try {
      objectPool.addObject(newLayout);
    }
    catch (ObjectExistsException exc) {
      log.log(Level.SEVERE, "Allegedly unique object ID/name already exists",
              exc);
      throw new IllegalStateException(
          "Allegedly unique object ID/name already exists", exc);
    }
    objectPool.emitObjectEvent(newLayout.clone(),
                               null,
                               TCSObjectEvent.Type.OBJECT_CREATED);
    // Return the newly created layout.
    return newLayout;
  }

  /**
   * Returns the layout belonging to the given reference.
   *
   * @param ref A reference to the layout to return.
   * @return The referenced layout, if it exists, or <code>null</code>, if it
   * doesn't.
   */
  public Layout getLayout(TCSObjectReference<Layout> ref) {
    log.finer("method entry");
    return objectPool.getObject(Layout.class, ref);
  }

  /**
   * Returns the layout with the given name.
   *
   * @param layoutName The name of the layout to return.
   * @return The layout with the given name, if it exists, or <code>null</code>,
   * if it doesn't.
   */
  public Layout getLayout(String layoutName) {
    log.finer("method entry");
    return objectPool.getObject(Layout.class, layoutName);
  }

  /**
   * Returns a set of layouts whose names match the given regular expression.
   *
   * @param regexp The regular expression selecting the layouts returned. If
   * <code>null</code>, all layouts will be returned.
   * @return A set of points whose names match the given regular expression. If
   * no such layouts exist, the returned set is empty.
   */
  public Set<Layout> getLayouts(Pattern regexp) {
    log.finer("method entry");
    return objectPool.getObjects(Layout.class, regexp);
  }

  /**
   * Sets the layout data of a given layout.
   *
   * @param ref A reference to the layout to be modified.
   * @param newData The layout's new data.
   * @return The modified layout.
   * @throws ObjectUnknownException If the referenced layout does not exist.
   */
  public Layout setLayoutData(TCSObjectReference<Layout> ref, byte[] newData)
      throws ObjectUnknownException {
    log.finer("method entry");
    Layout layout = objectPool.getObject(Layout.class, ref);
    if (layout == null) {
      throw new ObjectUnknownException(ref);
    }
    Layout previousState = layout.clone();
    layout.setData(newData);
    objectPool.emitObjectEvent(layout.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return layout;
  }

  /**
   * Removes a layout.
   *
   * @param ref A reference to the layout to be removed.
   * @return The removed layout.
   * @throws ObjectUnknownException If the referenced layout does not exist.
   */
  public Layout removeLayout(TCSObjectReference<Layout> ref)
      throws ObjectUnknownException {
    log.finer("method entry");
    Layout layout = objectPool.getObject(Layout.class, ref);
    if (layout == null) {
      throw new ObjectUnknownException(ref);
    }
    // Remove the layout.
    objectPool.removeObject(ref);
    objectPool.emitObjectEvent(null,
                               layout.clone(),
                               TCSObjectEvent.Type.OBJECT_REMOVED);
    return layout;
  }

  /**
   * Creates a new visual layout with a unique name and all other attributes set
   * to default values.
   *
   * @param objectID The object ID of the newly created layout. If
   * <code>null</code>, a new, unique one will be generated.
   * @return The newly created layout.
   */
  public VisualLayout createVisualLayout(Integer objectID) {
    log.finer("method entry");
    // Get a unique ID and name for the new point and create an instance.
    int layoutID =
        objectID != null ? objectID : objectPool.getUniqueObjectId();
    String layoutName = objectPool.getUniqueObjectName("VLayout-", "00");
    VisualLayout newLayout = new VisualLayout(layoutID, layoutName);
    // Store the instance in the global object pool.
    try {
      objectPool.addObject(newLayout);
    }
    catch (ObjectExistsException exc) {
      log.log(Level.SEVERE, "Allegedly unique object ID/name already exists",
              exc);
      throw new IllegalStateException(
          "Allegedly unique object ID/name already exists", exc);
    }
    objectPool.emitObjectEvent(newLayout.clone(),
                               null,
                               TCSObjectEvent.Type.OBJECT_CREATED);
    // Return the newly created layout.
    return newLayout;
  }
  

  /**
   * Sets the layout's scale on the X axis.
   *
   * @param ref A reference to the layout to be modified.
   * @param scaleX The layout's new scale on the X axis.
   * @return The modified layout.
   * @throws ObjectUnknownException If the referenced object does not exist.
   */
  public VisualLayout setVisualLayoutScaleX(
      TCSObjectReference<VisualLayout> ref,
      double scaleX)
      throws ObjectUnknownException {
    log.finer("method entry");
    VisualLayout layout = objectPool.getObject(VisualLayout.class, ref);
    if (layout == null) {
      throw new ObjectUnknownException(ref);
    }
    VisualLayout previousState = layout.clone();
    layout.setScaleX(scaleX);
    objectPool.emitObjectEvent(layout.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return layout;
  }

  /**
   * Sets the layout's scale on the Y axis.
   *
   * @param ref A reference to the layout to be modified.
   * @param scaleY The layout's new scale on the Y axis.
   * @return The modified layout.
   * @throws ObjectUnknownException If the referenced object does not exist.
   */
  public VisualLayout setVisualLayoutScaleY(
      TCSObjectReference<VisualLayout> ref,
      double scaleY)
      throws ObjectUnknownException {
    log.finer("method entry");
    VisualLayout layout = objectPool.getObject(VisualLayout.class, ref);
    if (layout == null) {
      throw new ObjectUnknownException(ref);
    }
    VisualLayout previousState = layout.clone();
    layout.setScaleY(scaleY);
    objectPool.emitObjectEvent(layout.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return layout;
  }

  /**
   * Sets the layout's colors.
   *
   * @param ref A reference to the point to be modified.
   * @param colors The layout's new colors.
   * @return The modified point.
   * @throws ObjectUnknownException If the referenced object does not exist.
   */
  public VisualLayout setVisualLayoutColors(
      TCSObjectReference<VisualLayout> ref,
      Map<String, Color> colors)
      throws ObjectUnknownException {
    log.finer("method entry");
    VisualLayout layout = objectPool.getObject(VisualLayout.class, ref);
    if (layout == null) {
      throw new ObjectUnknownException(ref);
    }
    VisualLayout previousState = layout.clone();
    layout.setColors(colors);
    objectPool.emitObjectEvent(layout.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return layout;
  }

  /**
   * Sets the layout's elements.
   *
   * @param ref A reference to the point to be modified.
   * @param elements The layout's new elements.
   * @return The modified point.
   * @throws ObjectUnknownException If the referenced object does not exist.
   */
  public VisualLayout setVisualLayoutElements(
      TCSObjectReference<VisualLayout> ref,
      Set<LayoutElement> elements)
      throws ObjectUnknownException {
    log.finer("method entry");
    VisualLayout layout = objectPool.getObject(VisualLayout.class, ref);
    if (layout == null) {
      throw new ObjectUnknownException(ref);
    }
    VisualLayout previousState = layout.clone();
    layout.setLayoutElements(elements);
    objectPool.emitObjectEvent(layout.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return layout;
  }

  /**
   * Sets the layout's view bookmarks.
   *
   * @param ref A reference to the point to be modified.
   * @param bookmarks The layout's new bookmarks.
   * @return The modified point.
   * @throws ObjectUnknownException If the referenced object does not exist.
   */
  public VisualLayout setVisualLayoutViewBookmarks(
      TCSObjectReference<VisualLayout> ref,
      List<ViewBookmark> bookmarks)
      throws ObjectUnknownException {
    log.finer("method entry");
    VisualLayout layout = objectPool.getObject(VisualLayout.class, ref);
    if (layout == null) {
      throw new ObjectUnknownException(ref);
    }
    VisualLayout previousState = layout.clone();
    layout.setViewBookmarks(bookmarks);
    objectPool.emitObjectEvent(layout.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return layout;
  }

  /**
   * Creates a new point with a unique name and all other attributes set to
   * default values.
   *
   * @param objectID The object ID of the newly created point. If
   * <code>null</code>, a new, unique one will be generated.
   * @return The newly created point.
   */
  public Point createPoint(Integer objectID) {
    log.finer("method entry");
    // Get a unique ID and name for the new point and create an instance.
    int pointID = objectID != null ? objectID : objectPool.getUniqueObjectId();
    String pointName = objectPool.getUniqueObjectName("Point-", "0000");
    Point newPoint = new Point(pointID, pointName);
    // Store the instance in the global object pool.
    try {
      objectPool.addObject(newPoint);
    }
    catch (ObjectExistsException exc) {
      log.log(Level.SEVERE, "Allegedly unique object ID/name already exists",
              exc);
      throw new IllegalStateException(
          "Allegedly unique object ID/name already exists", exc);
    }
    objectPool.emitObjectEvent(newPoint.clone(),
                               null,
                               TCSObjectEvent.Type.OBJECT_CREATED);
    // Return the newly created point.
    return newPoint;
  }

  /**
   * Returns the point belonging to the given reference.
   *
   * @param ref A reference to the point to return.
   * @return The referenced point, if it exists, or <code>null</code>, if it
   * doesn't.
   */
  public Point getPoint(TCSObjectReference<Point> ref) {
    log.finer("method entry");
    return objectPool.getObject(Point.class, ref);
  }

  /**
   * Returns the point with the given name.
   *
   * @param pointName The name of the point to return.
   * @return The point with the given name, if it exists, or <code>null</code>,
   * if it doesn't.
   */
  public Point getPoint(String pointName) {
    log.finer("method entry");
    return objectPool.getObject(Point.class, pointName);
  }

  /**
   * Returns a set of points whose names match the given regular expression.
   *
   * @param regexp The regular expression selecting the points returned. If
   * <code>null</code>, all points will be returned.
   * @return A set of points whose names match the given regular expression. If
   * no such points exist, the returned set is empty.
   */
  public Set<Point> getPoints(Pattern regexp) {
    log.finer("method entry");
    return objectPool.getObjects(Point.class, regexp);
  }

  /**
   * Sets the physical coordinates of a given point.
   *
   * @param ref A reference to the point to be modified.
   * @param position The point's new coordinates.
   * @return The modified point.
   * @throws ObjectUnknownException If the referenced point does not exist.
   */
  public Point setPointPosition(TCSObjectReference<Point> ref, Triple position)
      throws ObjectUnknownException {
    log.finer("method entry");
    Point point = objectPool.getObject(Point.class, ref);
    if (point == null) {
      throw new ObjectUnknownException(ref);
    }
    Point previousState = point.clone();
    point.setPosition(position);
    objectPool.emitObjectEvent(point.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return point;
  }

  /**
   * Sets the vehicle's (assumed) orientation angle at the given position.
   *
   * @param ref A reference to the point to be modified.
   * @param angle The new angle.
   * @return The modified point.
   * @throws ObjectUnknownException If the referenced point does not exist.
   */
  public Point setPointVehicleOrientationAngle(TCSObjectReference<Point> ref,
                                               double angle)
      throws ObjectUnknownException {
    log.finer("method entry");
    Point point = objectPool.getObject(Point.class, ref);
    if (point == null) {
      throw new ObjectUnknownException(ref);
    }
    Point previousState = point.clone();
    point.setVehicleOrientationAngle(angle);
    objectPool.emitObjectEvent(point.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return point;
  }

  /**
   * Sets the type of a given point.
   *
   * @param ref A reference to the point to be modified.
   * @param newType The point's new type.
   * @return The modified point.
   * @throws ObjectUnknownException If the referenced point does not exist.
   */
  public Point setPointType(TCSObjectReference<Point> ref, Point.Type newType)
      throws ObjectUnknownException {
    log.finer("method entry");
    if (newType == null) {
      throw new NullPointerException("newType is null");
    }
    Point point = objectPool.getObject(Point.class, ref);
    if (point == null) {
      throw new ObjectUnknownException(ref);
    }
    Point previousState = point.clone();
    point.setType(newType);
    objectPool.emitObjectEvent(point.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return point;
  }

  /**
   * Adds an incoming path to a point.
   *
   * @param pointRef A reference to the point to be modified.
   * @param pathRef A reference to the path.
   * @return The modified point.
   * @throws ObjectUnknownException If the referenced point or path do not
   * exist.
   */
  public Point addPointIncomingPath(TCSObjectReference<Point> pointRef,
                                    TCSObjectReference<Path> pathRef)
      throws ObjectUnknownException {
    log.finer("method entry");
    Point point = objectPool.getObject(Point.class, pointRef);
    if (point == null) {
      throw new ObjectUnknownException(pointRef);
    }
    Path path = objectPool.getObject(Path.class, pathRef);
    if (path == null) {
      throw new ObjectUnknownException(pathRef);
    }
    // Check if the point really is the path's destination point.
    if (!path.getDestinationPoint().equals(point.getReference())) {
      throw new IllegalArgumentException(
          "Point is not the path's destination.");
    }
    Path previousState = path.clone();
    point.addIncomingPath(path.getReference());
    objectPool.emitObjectEvent(point.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return point;
  }

  /**
   * Removes an incoming path from a point.
   *
   * @param pointRef A reference to the point to be modified.
   * @param pathRef A reference to the path.
   * @return The modified point.
   * @throws ObjectUnknownException If the referenced point or path do not
   * exist.
   */
  public Point removePointIncomingPath(TCSObjectReference<Point> pointRef,
                                       TCSObjectReference<Path> pathRef)
      throws ObjectUnknownException {
    log.finer("method entry");
    Point point = objectPool.getObject(Point.class, pointRef);
    if (point == null) {
      throw new ObjectUnknownException(pointRef);
    }
    Path path = objectPool.getObject(Path.class, pathRef);
    if (path == null) {
      throw new ObjectUnknownException(pathRef);
    }
    Path previousState = path.clone();
    point.removeIncomingPath(path.getReference());
    objectPool.emitObjectEvent(point.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return point;
  }

  /**
   * Adds an outgoing path to a point.
   *
   * @param pointRef A reference to the point to be modified.
   * @param pathRef A reference to the path.
   * @return The modified point.
   * @throws ObjectUnknownException If the referenced point or path do not
   * exist.
   */
  public Point addPointOutgoingPath(TCSObjectReference<Point> pointRef,
                                    TCSObjectReference<Path> pathRef)
      throws ObjectUnknownException {
    log.finer("method entry");
    Point point = objectPool.getObject(Point.class, pointRef);
    if (point == null) {
      throw new ObjectUnknownException(pointRef);
    }
    Path path = objectPool.getObject(Path.class, pathRef);
    if (path == null) {
      throw new ObjectUnknownException(pathRef);
    }
    // Check if the point really is the path's source.
    if (!path.getSourcePoint().equals(point.getReference())) {
      throw new IllegalArgumentException("Point is not the path's source.");
    }
    Path previousState = path.clone();
    point.addOutgoingPath(path.getReference());
    objectPool.emitObjectEvent(point.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return point;
  }

  /**
   * Removes an outgoing path from a point.
   *
   * @param pointRef A reference to the point to be modified.
   * @param pathRef A reference to the path.
   * @return The modified point.
   * @throws ObjectUnknownException If the referenced point or path do not
   * exist.
   */
  public Point removePointOutgoingPath(TCSObjectReference<Point> pointRef,
                                       TCSObjectReference<Path> pathRef)
      throws ObjectUnknownException {
    log.finer("method entry");
    Point point = objectPool.getObject(Point.class, pointRef);
    if (point == null) {
      throw new ObjectUnknownException(pointRef);
    }
    Path path = objectPool.getObject(Path.class, pathRef);
    if (path == null) {
      throw new ObjectUnknownException(pathRef);
    }
    Path previousState = path.clone();
    point.removeOutgoingPath(path.getReference());
    objectPool.emitObjectEvent(point.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return point;
  }

  /**
   * Removes a point.
   *
   * @param ref A reference to the point to be removed.
   * @return The removed point.
   * @throws ObjectUnknownException If the referenced point does not exist.
   */
  public Point removePoint(TCSObjectReference<Point> ref)
      throws ObjectUnknownException {
    log.finer("method entry");
    Point point = objectPool.getObject(Point.class, ref);
    if (point == null) {
      throw new ObjectUnknownException(ref);
    }
    // Remove any links to locations attached to this point.
    for (Location.Link curLink : point.getAttachedLinks()) {
      disconnectLocationFromPoint(curLink.getLocation(), ref);
    }
    // Remove any paths starting or ending in the removed point.
    for (TCSObjectReference<Path> curPathRef
        : new ArrayList<>(point.getOutgoingPaths())) {
      removePath(curPathRef);
    }
    for (TCSObjectReference<Path> curPathRef
        : new ArrayList<>(point.getIncomingPaths())) {
      removePath(curPathRef);
    }
    // Remove the point.
    objectPool.removeObject(ref);
    objectPool.emitObjectEvent(null,
                               point.clone(),
                               TCSObjectEvent.Type.OBJECT_REMOVED);
    return point;
  }

  /**
   * Creates a new path with a unique name and all other attributes set to
   * default values.
   *
   * @param objectID The ID of the newly created path. If <code>null</code>, a
   * new, unique one will be generated.
   * @param srcRef A reference to the point which the new path originates in.
   * @param destRef A reference to the point which the new path ends in.
   * @return The newly created path.
   * @throws ObjectUnknownException If the referenced point does not exist.
   */
  public Path createPath(Integer objectID,
                         TCSObjectReference<Point> srcRef,
                         TCSObjectReference<Point> destRef)
      throws ObjectUnknownException {
    log.finer("method entry");
    Point srcPoint = objectPool.getObject(Point.class, srcRef);
    if (srcPoint == null) {
      throw new ObjectUnknownException(srcRef);
    }
    Point destPoint = objectPool.getObject(Point.class, destRef);
    if (destPoint == null) {
      throw new ObjectUnknownException(destRef);
    }
    // Get a unique ID and name for the new path and create an instance.
    int pathID =
        objectID != null ? objectID : objectPool.getUniqueObjectId();
    String pathName = objectPool.getUniqueObjectName("Path-", "0000");
    Path newPath = new Path(pathID, pathName, srcPoint.getReference(),
                            destPoint.getReference());
    // Store the instance in the global object pool.
    try {
      objectPool.addObject(newPath);
    }
    catch (ObjectExistsException exc) {
      log.log(Level.SEVERE, "Allegedly unique object ID/name already exists",
              exc);
      throw new IllegalStateException(
          "Allegedly unique object ID/name already exists", exc);
    }
    objectPool.emitObjectEvent(newPath.clone(),
                               null,
                               TCSObjectEvent.Type.OBJECT_CREATED);
    addPointOutgoingPath(srcRef, newPath.getReference());
    addPointIncomingPath(destRef, newPath.getReference());
    // Return the newly created point.
    return newPath;
  }

  /**
   * Returns a referenced path.
   *
   * @param ref A reference to the path to be returned.
   * @return The path with the given name, if it exists, or <code>null</code>,
   * if it doesn't.
   */
  public Path getPath(TCSObjectReference<Path> ref) {
    log.finer("method entry");
    return objectPool.getObject(Path.class, ref);
  }

  /**
   * Returns the path with the given name.
   *
   * @param pathName The name of the path to be returned.
   * @return The path with the given name, if it exists, or <code>null</code>,
   * if it doesn't.
   */
  public Path getPath(String pathName) {
    log.finer("method entry");
    return objectPool.getObject(Path.class, pathName);
  }

  /**
   * Returns a set of paths whose names match the given regular expression.
   *
   * @param regexp The regular expression which the returned path's names must
   * match. If <code>null</code>, all paths are returned.
   * @return A set of paths whose names match the given regular expression. If
   * no such paths exist, the returned set is empty.
   */
  public Set<Path> getPaths(Pattern regexp) {
    log.finer("method entry");
    return objectPool.getObjects(Path.class, regexp);
  }

  /**
   * Sets the length of a given path.
   *
   * @param ref A reference to the path to be modified.
   * @param newLength The path's new length.
   * @return The modified path.
   * @throws ObjectUnknownException If the referenced path does not exist.
   */
  public Path setPathLength(TCSObjectReference<Path> ref, long newLength)
      throws ObjectUnknownException {
    log.finer("method entry");
    Path path = objectPool.getObject(Path.class, ref);
    if (path == null) {
      throw new ObjectUnknownException(ref);
    }
    Path previousState = path.clone();
    path.setLength(newLength);
    objectPool.emitObjectEvent(path.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return path;
  }

  /**
   * Sets the routing cost of a given path.
   *
   * @param ref A reference to the path to be modified.
   * @param newCost The path's new cost.
   * @return The modified path.
   * @throws ObjectUnknownException If the referenced path does not exist.
   */
  public Path setPathRoutingCost(TCSObjectReference<Path> ref, long newCost)
      throws ObjectUnknownException {
    log.finer("method entry");
    Path path = objectPool.getObject(Path.class, ref);
    if (path == null) {
      throw new ObjectUnknownException(ref);
    }
    Path previousState = path.clone();
    path.setRoutingCost(newCost);
    objectPool.emitObjectEvent(path.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return path;
  }

  /**
   * Sets the maximum allowed velocity for a given path.
   *
   * @param ref A reference to the path to be modified.
   * @param newVelocity The path's new maximum allowed velocity.
   * @return The modified path.
   * @throws ObjectUnknownException If the referenced path does not exist.
   */
  public Path setPathMaxVelocity(TCSObjectReference<Path> ref, int newVelocity)
      throws ObjectUnknownException {
    log.finer("method entry");
    Path path = objectPool.getObject(Path.class, ref);
    if (path == null) {
      throw new ObjectUnknownException(ref);
    }
    Path previousState = path.clone();
    path.setMaxVelocity(newVelocity);
    objectPool.emitObjectEvent(path.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return path;
  }

  /**
   * Sets the maximum allowed reverse velocity for a given path.
   *
   * @param ref A reference to the path to be modified.
   * @param newVelocity The path's new maximum allowed reverse velocity.
   * @return The modified path.
   * @throws ObjectUnknownException If the referenced path does not exist.
   */
  public Path setPathMaxReverseVelocity(TCSObjectReference<Path> ref,
                                        int newVelocity)
      throws ObjectUnknownException {
    log.finer("method entry");
    Path path = objectPool.getObject(Path.class, ref);
    if (path == null) {
      throw new ObjectUnknownException(ref);
    }
    Path previousState = path.clone();
    path.setMaxReverseVelocity(newVelocity);
    objectPool.emitObjectEvent(path.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return path;
  }

  /**
   * Locks/Unlocks a path.
   *
   * @param ref A reference to the path to be modified.
   * @param newLocked If <code>true</code>, this path will be locked when the
   * method call returns; if <code>false</code>, this path will be unlocked.
   * @return The modified path.
   * @throws ObjectUnknownException If the referenced path does not exist.
   */
  public Path setPathLocked(TCSObjectReference<Path> ref, boolean newLocked)
      throws ObjectUnknownException {
    log.finer("method entry");
    Path path = objectPool.getObject(Path.class, ref);
    if (path == null) {
      throw new ObjectUnknownException(ref);
    }
    Path previousState = path.clone();
    path.setLocked(newLocked);
    objectPool.emitObjectEvent(path.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return path;
  }

  /**
   * Removes a path.
   *
   * @param ref A reference to the path to be removed.
   * @return The removed path.
   * @throws ObjectUnknownException If the referenced path does not exist.
   */
  public Path removePath(TCSObjectReference<Path> ref)
      throws ObjectUnknownException {
    log.finer("method entry");
    Path path = objectPool.getObject(Path.class, ref);
    if (path == null) {
      throw new ObjectUnknownException(ref);
    }
    Path previousState = path.clone();
    removePointOutgoingPath(path.getSourcePoint(), ref);
    removePointIncomingPath(path.getDestinationPoint(), ref);
    objectPool.removeObject(ref);
    objectPool.emitObjectEvent(null,
                               previousState,
                               TCSObjectEvent.Type.OBJECT_REMOVED);
    return path;
  }

  /**
   * Creates a new location type with a unique name and all other attributes set
   * to their default values.
   *
   * @param objectID The new location type's ID. If <code>null</code>, a new,
   * unique one will be generated.
   * @return The newly created location type.
   */
  public LocationType createLocationType(Integer objectID) {
    log.finer("method entry");
    int typeID = objectID != null ? objectID : objectPool.getUniqueObjectId();
    String typeName = objectPool.getUniqueObjectName("LType-", "00");
    LocationType newType = new LocationType(typeID, typeName);
    try {
      objectPool.addObject(newType);
    }
    catch (ObjectExistsException exc) {
      log.log(Level.SEVERE, "Allegedly unique object ID/name already exists",
              exc);
      throw new IllegalStateException(
          "Allegedly unique object ID/name already exists", exc);
    }
    objectPool.emitObjectEvent(newType.clone(),
                               null,
                               TCSObjectEvent.Type.OBJECT_CREATED);
    return newType;
  }

  /**
   * Returns the referenced location type.
   *
   * @param ref A reference to the location type to be returned.
   * @return The referenced location type, or <code>null</code>, if no such
   * location type exists.
   */
  public LocationType getLocationType(TCSObjectReference<LocationType> ref) {
    log.finer("method entry");
    return objectPool.getObject(LocationType.class, ref);
  }

  /**
   * Returns the location type with the given name.
   *
   * @param typeName The name of the location type to return.
   * @return The location type with the given name, or <code>null</code>, if no
   * such location type exists.
   */
  public LocationType getLocationType(String typeName) {
    log.finer("method entry");
    return objectPool.getObject(LocationType.class, typeName);
  }

  /**
   * Returns a set of location types whose names match the given regular
   * expression.
   *
   * @param regexp The regular expression describing the names of the
   * location types to return. If <code>null</code>, all location types are
   * returned.
   * @return A set of location types whose names match the given regular
   * expression. If no such location types exist, the returned set is empty.
   */
  public Set<LocationType> getLocationTypes(Pattern regexp) {
    log.finer("method entry");
    return objectPool.getObjects(LocationType.class, regexp);
  }

  /**
   * Adds an allowed operation to a location type.
   *
   * @param ref A reference to the location type to be modified.
   * @param operation The operation to be allowed.
   * @return The modified location type.
   * @throws ObjectUnknownException If the referenced location type does not
   * exist.
   */
  public LocationType addLocationTypeAllowedOperation(
      TCSObjectReference<LocationType> ref, String operation)
      throws ObjectUnknownException {
    log.finer("method entry");
    LocationType type = objectPool.getObject(LocationType.class, ref);
    if (type == null) {
      throw new ObjectUnknownException(ref);
    }
    LocationType previousState = type.clone();
    type.addAllowedOperation(operation);
    objectPool.emitObjectEvent(type.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return type;
  }

  /**
   * Removes an allowed operation from a location type.
   *
   * @param ref A reference to the location type to be modified.
   * @param operation The operation to be disallowed.
   * @return The modified location type.
   * @throws ObjectUnknownException If the referenced location type does not
   * exist.
   */
  public LocationType removeLocationTypeAllowedOperation(
      TCSObjectReference<LocationType> ref, String operation)
      throws ObjectUnknownException {
    log.finer("method entry");
    LocationType type = objectPool.getObject(LocationType.class, ref);
    if (type == null) {
      throw new ObjectUnknownException(ref);
    }
    LocationType previousState = type.clone();
    type.removeAllowedOperation(operation);
    objectPool.emitObjectEvent(type.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return type;
  }

  /**
   * Removes a location type.
   *
   * @param ref A reference to the location type to be removed.
   * @return The removed location type.
   * @throws ObjectUnknownException If the referenced location type does not
   * exist.
   */
  public LocationType removeLocationType(TCSObjectReference<LocationType> ref)
      throws ObjectUnknownException {
    log.finer("method entry");
    LocationType type = objectPool.getObject(LocationType.class, ref);
    if (type == null) {
      throw new ObjectUnknownException(ref);
    }
    // XXX Check if any locations of this type still exist, first.
    objectPool.removeObject(ref);
    objectPool.emitObjectEvent(null,
                               type.clone(),
                               TCSObjectEvent.Type.OBJECT_REMOVED);
    return type;
  }

  /**
   * Creates a new location with a unique name and all other attributes set to
   * default values.
   *
   * @param objectID The ID of the newly created location. If <code>null</code>,
   * a new, unique one will be generated.
   * @param typeRef The location type the location will belong to.
   * @return The newly created location.
   * @throws ObjectUnknownException If the referenced location type does not
   * exist.
   */
  public Location createLocation(Integer objectID,
                                 TCSObjectReference<LocationType> typeRef)
      throws ObjectUnknownException {
    log.finer("method entry");
    LocationType type = objectPool.getObject(LocationType.class, typeRef);
    if (type == null) {
      throw new ObjectUnknownException(typeRef);
    }
    // Get a unique ID and name for the new location and create an instance.
    int locID = objectID != null ? objectID : objectPool.getUniqueObjectId();
    String locationName = objectPool.getUniqueObjectName("Location-", "0000");
    Location newLocation =
        new Location(locID, locationName, type.getReference());
    // Store the instance in the global object pool.
    try {
      objectPool.addObject(newLocation);
    }
    catch (ObjectExistsException exc) {
      log.log(Level.SEVERE, "Allegedly unique object ID/name already exists",
              exc);
      throw new IllegalStateException(
          "Allegedly unique object ID/name already exists", exc);
    }
    objectPool.emitObjectEvent(newLocation.clone(),
                               null,
                               TCSObjectEvent.Type.OBJECT_CREATED);
    // Return the newly created point.
    return newLocation;
  }

  /**
   * Returns the referenced location.
   *
   * @param ref A reference to the location to be returned.
   * @return The referenced location, or <code>null</code>, if no such location
   * exists in this pool.
   */
  public Location getLocation(TCSObjectReference<Location> ref) {
    log.finer("method entry");
    return objectPool.getObject(Location.class, ref);
  }

  /**
   * Returns the location with the given name.
   *
   * @param locName The name of the location to return.
   * @return The location with the given name, or <code>null</code>, if no such
   * location exists.
   */
  public Location getLocation(String locName) {
    log.finer("method entry");
    return objectPool.getObject(Location.class, locName);
  }

  /**
   * Returns a set of locations whose names match the given regular expression.
   *
   * @param regexp The regular expression describing the names of the locations
   * to return. If <code>null</code>, all locations are returned.
   * @return A set of locations whose names match the given regular expression.
   * If no such locations exist, the returned set is empty.
   */
  public Set<Location> getLocations(Pattern regexp) {
    log.finer("method entry");
    return objectPool.getObjects(Location.class, regexp);
  }

  /**
   * Sets the physical coordinates of a given location.
   *
   * @param ref A reference to the location to be modified.
   * @param position The location's new coordinates.
   * @return The modified location.
   * @throws ObjectUnknownException If the referenced location does not exist.
   */
  public Location setLocationPosition(TCSObjectReference<Location> ref,
                                      Triple position)
      throws ObjectUnknownException {
    log.finer("method entry");
    Location location = objectPool.getObject(Location.class, ref);
    if (location == null) {
      throw new ObjectUnknownException(ref);
    }
    Location previousState = location.clone();
    location.setPosition(position);
    objectPool.emitObjectEvent(location.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return location;
  }

  /**
   * Sets a location's type.
   *
   * @param ref A reference to the location to be modified.
   * @param typeRef The location's new type.
   * @return The modified location.
   * @throws ObjectUnknownException If the referenced location or name do not
   * exist.
   */
  public Location setLocationType(TCSObjectReference<Location> ref,
                                  TCSObjectReference<LocationType> typeRef)
      throws ObjectUnknownException {
    log.finer("method entry");
    Location location = objectPool.getObject(Location.class, ref);
    if (location == null) {
      throw new ObjectUnknownException(ref);
    }
    LocationType type = objectPool.getObject(LocationType.class, typeRef);
    if (type == null) {
      throw new ObjectUnknownException(typeRef);
    }
    Location previousState = location.clone();
    location.setType(type.getReference());
    objectPool.emitObjectEvent(location.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return location;
  }

  /**
   * Connects a location to a point.
   *
   * @param locRef A reference to the location.
   * @param pointRef A reference to the point.
   * @return The modified location.
   * @throws ObjectUnknownException If any of the referenced objects does not
   * exist.
   */
  public Location connectLocationToPoint(TCSObjectReference<Location> locRef,
                                         TCSObjectReference<Point> pointRef)
      throws ObjectUnknownException {
    log.finer("method entry");
    Location location = objectPool.getObject(Location.class, locRef);
    if (location == null) {
      throw new ObjectUnknownException(locRef);
    }
    Location previousLocationState = location.clone();
    Point point = objectPool.getObject(Point.class, pointRef);
    if (point == null) {
      throw new ObjectUnknownException(pointRef);
    }
    Point previousPointState = point.clone();
    Location.Link newLink =
        new Location.Link(location.getReference(), point.getReference());
    location.attachLink(newLink);
    point.attachLink(newLink);
    objectPool.emitObjectEvent(location.clone(),
                               previousLocationState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    objectPool.emitObjectEvent(point.clone(),
                               previousPointState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return location;
  }

  /**
   * Disconnects a location from a point.
   *
   * @param locRef A reference to the location.
   * @param pointRef A reference to the point.
   * @return The modified location.
   * @throws ObjectUnknownException If any of the referenced objects does not
   * exist.
   */
  public Location disconnectLocationFromPoint(
      TCSObjectReference<Location> locRef, TCSObjectReference<Point> pointRef)
      throws ObjectUnknownException {
    log.finer("method entry");
    Location location = objectPool.getObject(Location.class, locRef);
    if (location == null) {
      throw new ObjectUnknownException(locRef);
    }
    Location previousLocationState = location.clone();
    Point point = objectPool.getObject(Point.class, pointRef);
    if (point == null) {
      throw new ObjectUnknownException(pointRef);
    }
    Point previousPointState = point.clone();
    location.detachLink(point.getReference());
    point.detachLink(location.getReference());
    objectPool.emitObjectEvent(location.clone(),
                               previousLocationState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    objectPool.emitObjectEvent(point.clone(),
                               previousPointState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return location;
  }

  /**
   * Adds an allowed operation to a link between a location and a point.
   *
   * @param locRef A reference to the location end of the link to be modified.
   * @param pointRef A reference to the point end of the link to be modified.
   * @param operation The operation to be added.
   * @throws ObjectUnknownException If any of the referenced objects does not
   * exist.
   */
  public void addLocationLinkAllowedOperation(
      TCSObjectReference<Location> locRef, TCSObjectReference<Point> pointRef,
      String operation)
      throws ObjectUnknownException {
    log.finer("method entry");
    Location location = objectPool.getObject(Location.class, locRef);
    if (location == null) {
      throw new ObjectUnknownException(locRef);
    }
    Location previousLocationState = location.clone();
    Point point = objectPool.getObject(Point.class, pointRef);
    if (point == null) {
      throw new ObjectUnknownException(pointRef);
    }
    Point previousPointState = point.clone();
    // Get the link between the point and location, if any exists.
    Location.Link referredLink = null;
    for (Location.Link curLink : location.getAttachedLinks()) {
      if (point.getReference().equals(curLink.getPoint())) {
        referredLink = curLink;
        break;
      }
    }
    if (referredLink == null) {
      throw new ObjectUnknownException("Described link not in this model");
    }
    referredLink.addAllowedOperation(operation);
    // Emit an event for both the location and the point end of the link.
    objectPool.emitObjectEvent(location.clone(),
                               previousLocationState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    objectPool.emitObjectEvent(point.clone(),
                               previousPointState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    // XXX Do we want to return anything here?
  }

  /**
   * Removes an allowed operation from a link between a location and a point.
   *
   * @param locRef A reference to the location end of the link to be modified.
   * @param pointRef A reference to the point end of the link to be modified.
   * @param operation The operation to be removed.
   * @throws ObjectUnknownException If any of the referenced objects does not
   * exist.
   */
  public void removeLocationLinkAllowedOperation(
      TCSObjectReference<Location> locRef, TCSObjectReference<Point> pointRef,
      String operation)
      throws ObjectUnknownException {
    log.finer("method entry");
    Location location = objectPool.getObject(Location.class, locRef);
    if (location == null) {
      throw new ObjectUnknownException(locRef);
    }
    Location previousLocationState = location.clone();
    Point point = objectPool.getObject(Point.class, pointRef);
    if (point == null) {
      throw new ObjectUnknownException(pointRef);
    }
    Point previousPointState = point.clone();
    // Get the link between the point and location, if any exists.
    Location.Link referredLink = null;
    for (Location.Link curLink : location.getAttachedLinks()) {
      if (curLink.getPoint().equals(point.getReference())) {
        referredLink = curLink;
        break;
      }
    }
    if (referredLink == null) {
      throw new ObjectUnknownException("Described link not in this model");
    }
    // Emit an event for both the location and the point end of the link.
    objectPool.emitObjectEvent(location.clone(),
                               previousLocationState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    objectPool.emitObjectEvent(point.clone(),
                               previousPointState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    // XXX Do we want to return anything here?
  }

  /**
   * Removes all allowed operations (for all vehicle types) from a link between
   * a location and a point.
   *
   * @param locRef A reference to the location end of the link to be modified.
   * @param pointRef A reference to the point end of the link to be modified.
   * @throws ObjectUnknownException If any of the referenced objects does not
   * exist.
   */
  public void clearLocationLinkAllowedOperations(
      TCSObjectReference<Location> locRef, TCSObjectReference<Point> pointRef)
      throws ObjectUnknownException {
    log.finer("method entry");
    Location location = objectPool.getObject(Location.class, locRef);
    if (location == null) {
      throw new ObjectUnknownException(locRef);
    }
    Location previousLocationState = location.clone();
    Point point = objectPool.getObject(Point.class, pointRef);
    if (point == null) {
      throw new ObjectUnknownException(pointRef);
    }
    Point previousPointState = point.clone();
    // Get the link between the point and location, if any exists.
    Location.Link referredLink = null;
    for (Location.Link curLink : location.getAttachedLinks()) {
      if (curLink.getPoint().equals(point.getReference())) {
        referredLink = curLink;
        break;
      }
    }
    if (referredLink == null) {
      throw new ObjectUnknownException("Described link not in this model");
    }
    referredLink.clearAllowedOperations();
    // Emit an event for both the location and the point end of the link.
    objectPool.emitObjectEvent(location.clone(),
                               previousLocationState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    objectPool.emitObjectEvent(point.clone(),
                               previousPointState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    // XXX Do we want to return anything here?
  }

  /**
   * Removes a location.
   *
   * @param ref A reference to the location to be removed.
   * @return The removed location.
   * @throws ObjectUnknownException If the referenced location does not exist.
   */
  public Location removeLocation(TCSObjectReference<Location> ref)
      throws ObjectUnknownException {
    log.finer("method entry");
    Location location = objectPool.getObject(Location.class, ref);
    if (location == null) {
      throw new ObjectUnknownException(ref);
    }
    // XXX Check if there are links pointing to this location, first.
    objectPool.removeObject(ref);
    objectPool.emitObjectEvent(null,
                               location.clone(),
                               TCSObjectEvent.Type.OBJECT_REMOVED);
    return location;
  }

  /**
   * Creates a new vehicle with a unique name and all other attributes set to
   * their default values.
   *
   * @param objectID The ID of the newly created vehicle. If <code>null</code>,
   * a new, unique one will be generated.
   * @return The newly created vehicle.
   * @throws ObjectUnknownException If the referenced vehicle type is not in
   * this model.
   */
  public Vehicle createVehicle(Integer objectID)
      throws ObjectUnknownException {
    log.finer("method entry");
    int vehicleID =
        objectID != null ? objectID : objectPool.getUniqueObjectId();
    String vehicleName = objectPool.getUniqueObjectName("Vehicle-", "00");
    Vehicle newVehicle = new Vehicle(vehicleID, vehicleName);
    try {
      objectPool.addObject(newVehicle);
    }
    catch (ObjectExistsException exc) {
      log.log(Level.SEVERE, "Allegedly unique object ID/name already exists",
              exc);
      throw new IllegalStateException(
          "Allegedly unique object ID/name already exists", exc);
    }
    objectPool.emitObjectEvent(newVehicle.clone(),
                               null,
                               TCSObjectEvent.Type.OBJECT_CREATED);
    return newVehicle;
  }

  /**
   * Returns the referenced vehicle.
   *
   * @param ref A reference to the vehicle to be returned.
   * @return The referenced vehicle, or <code>null</code>, if no such vehicle
   * exists.
   */
  public Vehicle getVehicle(TCSObjectReference<Vehicle> ref) {
    log.finer("method entry");
    return objectPool.getObject(Vehicle.class, ref);
  }

  /**
   * Returns the vehicle with the given name.
   *
   * @param vehicleName The name of the vehicle to return.
   * @return The vehicle with the given name, or <code>null</code>, if no
   * such vehicle exists.
   */
  public Vehicle getVehicle(String vehicleName) {
    log.finer("method entry");
    return objectPool.getObject(Vehicle.class, vehicleName);
  }

  /**
   * Returns a set of vehicles whose names match the given regular expression.
   *
   * @param regexp The regular expression describing the names of the
   * vehicles to return. If <code>null</code>, all vehicles are returned.
   * @return A set of Vehicles whose names match the given regular expression.
   * If no such vehicles exist, the returned set is empty.
   */
  public Set<Vehicle> getVehicles(Pattern regexp) {
    log.finer("method entry");
    return objectPool.getObjects(Vehicle.class, regexp);
  }

  /**
   * Sets a vehicle's energy level.
   *
   * @param ref A reference to the vehicle to be modified.
   * @param energyLevel The vehicle's new energy level.
   * @return The modified vehicle.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   */
  public Vehicle setVehicleEnergyLevel(TCSObjectReference<Vehicle> ref,
                                       int energyLevel)
      throws ObjectUnknownException {
    log.finer("method entry");
    Vehicle vehicle = objectPool.getObject(Vehicle.class, ref);
    if (vehicle == null) {
      throw new ObjectUnknownException(ref);
    }
    Vehicle previousState = vehicle.clone();
    vehicle.setEnergyLevel(energyLevel);
    objectPool.emitObjectEvent(vehicle.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return vehicle;
  }

  /**
   * Sets a vehicle's critical energy level.
   *
   * @param ref A reference to the vehicle to be modified.
   * @param energyLevel The vehicle's new critical energy level.
   * @return The modified vehicle.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   */
  public Vehicle setVehicleEnergyLevelCritical(TCSObjectReference<Vehicle> ref,
                                               int energyLevel)
      throws ObjectUnknownException {
    log.finer("method entry");
    Vehicle vehicle = objectPool.getObject(Vehicle.class, ref);
    if (vehicle == null) {
      throw new ObjectUnknownException(ref);
    }
    Vehicle previousState = vehicle.clone();
    vehicle.setEnergyLevelCritical(energyLevel);
    objectPool.emitObjectEvent(vehicle.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return vehicle;
  }

  /**
   * Sets a vehicle's good energy level.
   *
   * @param ref A reference to the vehicle to be modified.
   * @param energyLevel The vehicle's new good energy level.
   * @return The modified vehicle.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   */
  public Vehicle setVehicleEnergyLevelGood(TCSObjectReference<Vehicle> ref,
                                           int energyLevel)
      throws ObjectUnknownException {
    log.finer("method entry");
    Vehicle vehicle = objectPool.getObject(Vehicle.class, ref);
    if (vehicle == null) {
      throw new ObjectUnknownException(ref);
    }
    Vehicle previousState = vehicle.clone();
    vehicle.setEnergyLevelGood(energyLevel);
    objectPool.emitObjectEvent(vehicle.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return vehicle;
  }

  /**
   * Sets a vehicle's recharge operation.
   *
   * @param ref A reference to the vehicle to be modified.
   * @param rechargeOperation The vehicle's new recharge operation.
   * @return The modified vehicle.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   */
  public Vehicle setVehicleRechargeOperation(TCSObjectReference<Vehicle> ref,
                                             String rechargeOperation)
      throws ObjectUnknownException {
    log.finer("method entry");
    Vehicle vehicle = objectPool.getObject(Vehicle.class, ref);
    if (vehicle == null) {
      throw new ObjectUnknownException(ref);
    }
    Vehicle previousState = vehicle.clone();
    vehicle.setRechargeOperation(rechargeOperation);
    objectPool.emitObjectEvent(vehicle.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return vehicle;
  }

  /**
   * Sets a vehicle's load handling devices.
   *
   * @param ref A reference to the vehicle to be modified.
   * @param devices The vehicle's new load handling devices.
   * @return The modified vehicle.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   */
  public Vehicle setVehicleLoadHandlingDevices(TCSObjectReference<Vehicle> ref,
                                               List<LoadHandlingDevice> devices)
      throws ObjectUnknownException {
    log.finer("method entry");
    Vehicle vehicle = objectPool.getObject(Vehicle.class, ref);
    if (vehicle == null) {
      throw new ObjectUnknownException(ref);
    }
    Vehicle previousState = vehicle.clone();
    vehicle.setLoadHandlingDevices(devices);
    objectPool.emitObjectEvent(vehicle.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return vehicle;
  }

  /**
   * Sets a vehicle's maximum velocity (in mm/s).
   *
   * @param ref A reference to the vehicle to be modified.
   * @param velocity The vehicle's new maximum velocity.
   * @return The modified vehicle.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   */
  public Vehicle setVehicleMaxVelocity(TCSObjectReference<Vehicle> ref,
                                       int velocity)
      throws ObjectUnknownException {
    log.finer("method entry");
    Vehicle vehicle = objectPool.getObject(Vehicle.class, ref);
    if (vehicle == null) {
      throw new ObjectUnknownException(ref);
    }
    Vehicle previousState = vehicle.clone();
    vehicle.setMaxVelocity(velocity);
    objectPool.emitObjectEvent(vehicle.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return vehicle;
  }

  /**
   * Sets a vehicle's maximum reverse velocity.
   *
   * @param ref A reference to the vehicle to be modified.
   * @param velocity The vehicle's new maximum reverse velocity.
   * @return The modified vehicle.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   */
  public Vehicle setVehicleMaxReverseVelocity(TCSObjectReference<Vehicle> ref,
                                              int velocity)
      throws ObjectUnknownException {
    log.finer("method entry");
    Vehicle vehicle = objectPool.getObject(Vehicle.class, ref);
    if (vehicle == null) {
      throw new ObjectUnknownException(ref);
    }
    Vehicle previousState = vehicle.clone();
    vehicle.setMaxReverseVelocity(velocity);
    objectPool.emitObjectEvent(vehicle.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return vehicle;
  }

  /**
   * Sets a vehicle's state.
   *
   * @param ref A reference to the vehicle to be modified.
   * @param newState The vehicle's new state.
   * @return The modified vehicle.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   */
  public Vehicle setVehicleState(TCSObjectReference<Vehicle> ref,
                                 Vehicle.State newState)
      throws ObjectUnknownException {
    log.finer("method entry");
    Vehicle vehicle = objectPool.getObject(Vehicle.class, ref);
    if (vehicle == null) {
      throw new ObjectUnknownException(ref);
    }
    Vehicle previousState = vehicle.clone();
    vehicle.setState(newState);
    objectPool.emitObjectEvent(vehicle.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return vehicle;
  }

  /**
   * Sets a vehicle's processing state.
   *
   * @param ref A reference to the vehicle to be modified.
   * @param newState The vehicle's new processing state.
   * @return The modified vehicle.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   */
  public Vehicle setVehicleProcState(TCSObjectReference<Vehicle> ref,
                                     Vehicle.ProcState newState)
      throws ObjectUnknownException {
    log.finer("method entry");
    Vehicle vehicle = objectPool.getObject(Vehicle.class, ref);
    if (vehicle == null) {
      throw new ObjectUnknownException(ref);
    }
    Vehicle previousState = vehicle.clone();
    vehicle.setProcState(newState);
    objectPool.emitObjectEvent(vehicle.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return vehicle;
  }

  /**
   * Sets a vehicle's communication adapter's state.
   *
   * @param ref A reference to the vehicle to be modified.
   * @param newState The vehicle's communication adapter's new state.
   * @return The modified vehicle.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   */
  public Vehicle setVehicleAdapterState(TCSObjectReference<Vehicle> ref,
                                        CommunicationAdapter.State newState)
      throws ObjectUnknownException {
    log.finer("method entry");
    Vehicle vehicle = objectPool.getObject(Vehicle.class, ref);
    if (vehicle == null) {
      throw new ObjectUnknownException(ref);
    }
    Vehicle previousState = vehicle.clone();
    vehicle.setAdapterState(newState);
    objectPool.emitObjectEvent(vehicle.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return vehicle;
  }

  /**
   * Sets a vehicle's length.
   *
   * @param ref A reference to the vehicle to be modified.
   * @param length The vehicle's new length.
   * @return The modified vehicle.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   */
  public Vehicle setVehicleLength(TCSObjectReference<Vehicle> ref, int length)
      throws ObjectUnknownException {
    log.finer("method entry");
    Vehicle vehicle = objectPool.getObject(Vehicle.class, ref);
    if (vehicle == null) {
      throw new ObjectUnknownException(ref);
    }
    Vehicle previousState = vehicle.clone();
    vehicle.setLength(length);
    objectPool.emitObjectEvent(vehicle.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return vehicle;
  }

  /**
   * Sets a vehicle's position.
   *
   * @param ref A reference to the vehicle to be modified.
   * @param newPosRef A reference to the point the vehicle is occupying.
   * @return The modified vehicle.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   */
  public Vehicle setVehiclePosition(TCSObjectReference<Vehicle> ref,
                                    TCSObjectReference<Point> newPosRef)
      throws ObjectUnknownException {
    log.finer("method entry");
    Vehicle vehicle = objectPool.getObject(Vehicle.class, ref);
    if (vehicle == null) {
      throw new ObjectUnknownException(ref);
    }
    Vehicle previousVehicleState = vehicle.clone();
    // If the vehicle was occupying a point before, clear it and send an event.
    if (vehicle.getCurrentPosition() != null) {
      Point oldPos = objectPool.getObject(Point.class,
                                          vehicle.getCurrentPosition());
      Point previousPointState = oldPos.clone();
      oldPos.setOccupyingVehicle(null);
      objectPool.emitObjectEvent(oldPos.clone(),
                                 previousPointState,
                                 TCSObjectEvent.Type.OBJECT_MODIFIED);
    }
    // If the vehicle is occupying a point now, set that and send an event.
    if (newPosRef != null) {
      Point newPos = objectPool.getObject(Point.class, newPosRef);
      Point previousPointState = newPos.clone();
      newPos.setOccupyingVehicle(ref);
      objectPool.emitObjectEvent(newPos.clone(),
                                 previousPointState,
                                 TCSObjectEvent.Type.OBJECT_MODIFIED);
    }
    vehicle.setCurrentPosition(newPosRef);
    objectPool.emitObjectEvent(vehicle.clone(),
                               previousVehicleState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);

    return vehicle;
  }

  /**
   * Sets a vehicle's next position.
   *
   * @param ref A reference to the vehicle to be modified.
   * @param newPosition A reference to the point the vehicle is expected to
   * occupy next.
   * @return The modified vehicle.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   */
  public Vehicle setVehicleNextPosition(TCSObjectReference<Vehicle> ref,
                                        TCSObjectReference<Point> newPosition)
      throws ObjectUnknownException {
    log.finer("method entry");
    Vehicle vehicle = objectPool.getObject(Vehicle.class, ref);
    if (vehicle == null) {
      throw new ObjectUnknownException(ref);
    }
    Vehicle previousState = vehicle.clone();
    vehicle.setNextPosition(newPosition);
    objectPool.emitObjectEvent(vehicle.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return vehicle;
  }

  /**
   * Sets a vehicle's precise position.
   *
   * @param ref A reference to the vehicle to be modified.
   * @param newPosition The vehicle's precise position.
   * @return The modified vehicle.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   */
  public Vehicle setVehiclePrecisePosition(TCSObjectReference<Vehicle> ref,
                                           Triple newPosition)
      throws ObjectUnknownException {
    log.finer("method entry");
    Vehicle vehicle = objectPool.getObject(Vehicle.class, ref);
    if (vehicle == null) {
      throw new ObjectUnknownException(ref);
    }
    Vehicle previousState = vehicle.clone();
    vehicle.setPrecisePosition(newPosition);
    objectPool.emitObjectEvent(vehicle.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return vehicle;
  }

  /**
   * Sets a vehicle's current orientation angle.
   *
   * @param ref A reference to the vehicle to be modified.
   * @param angle The vehicle's orientation angle.
   * @return The modified vehicle.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   */
  public Vehicle setVehicleOrientationAngle(TCSObjectReference<Vehicle> ref,
                                            double angle)
      throws ObjectUnknownException {
    log.finer("method entry");
    Vehicle vehicle = objectPool.getObject(Vehicle.class, ref);
    if (vehicle == null) {
      throw new ObjectUnknownException(ref);
    }
    Vehicle previousState = vehicle.clone();
    vehicle.setOrientationAngle(angle);
    objectPool.emitObjectEvent(vehicle.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return vehicle;
  }

  /**
   * Sets a vehicle's transport order.
   *
   * @param vehicleRef A reference to the vehicle to be modified.
   * @param orderRef A reference to the transport order the vehicle processes.
   * @return The modified vehicle.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   */
  public Vehicle setVehicleTransportOrder(
      TCSObjectReference<Vehicle> vehicleRef,
      TCSObjectReference<TransportOrder> orderRef)
      throws ObjectUnknownException {
    log.finer("method entry");
    Vehicle vehicle = objectPool.getObject(Vehicle.class, vehicleRef);
    if (vehicle == null) {
      throw new ObjectUnknownException(vehicleRef);
    }
    Vehicle previousState = vehicle.clone();
    if (orderRef == null) {
      vehicle.setTransportOrder(null);
    }
    else {
      TransportOrder order = objectPool.getObject(TransportOrder.class,
                                                  orderRef);
      if (order == null) {
        throw new ObjectUnknownException(orderRef);
      }
      vehicle.setTransportOrder(order.getReference());
    }
    objectPool.emitObjectEvent(vehicle.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return vehicle;
  }

  /**
   * Sets a vehicle's order sequence.
   *
   * @param vehicleRef A reference to the vehicle to be modified.
   * @param seqRef A reference to the order sequence the vehicle processes.
   * @return The modified vehicle.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   */
  public Vehicle setVehicleOrderSequence(
      TCSObjectReference<Vehicle> vehicleRef,
      TCSObjectReference<OrderSequence> seqRef)
      throws ObjectUnknownException {
    log.finer("method entry");
    Vehicle vehicle = objectPool.getObject(Vehicle.class, vehicleRef);
    if (vehicle == null) {
      throw new ObjectUnknownException(vehicleRef);
    }
    Vehicle previousState = vehicle.clone();
    if (seqRef == null) {
      vehicle.setOrderSequence(null);
    }
    else {
      OrderSequence seq = objectPool.getObject(OrderSequence.class, seqRef);
      if (seq == null) {
        throw new ObjectUnknownException(seqRef);
      }
      vehicle.setOrderSequence(seq.getReference());
    }
    objectPool.emitObjectEvent(vehicle.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return vehicle;
  }

  /**
   * Sets a vehicle's index of the last route step travelled for the current
   * drive order of its current transport order.
   *
   * @param vehicleRef A reference to the vehicle to be modified.
   * @param index The new index.
   * @return The modified vehicle.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   */
  public Vehicle setVehicleRouteProgressIndex(
      TCSObjectReference<Vehicle> vehicleRef,
      int index)
      throws ObjectUnknownException {
    log.finer("method entry");
    Vehicle vehicle = objectPool.getObject(Vehicle.class, vehicleRef);
    if (vehicle == null) {
      throw new ObjectUnknownException(vehicleRef);
    }
    Vehicle previousState = vehicle.clone();
    vehicle.setRouteProgressIndex(index);
    objectPool.emitObjectEvent(vehicle.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return vehicle;
  }

  /**
   * Removes a vehicle.
   *
   * @param ref A reference to the vehicle to be removed.
   * @return The removed vehicle.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   */
  public Vehicle removeVehicle(TCSObjectReference<Vehicle> ref)
      throws ObjectUnknownException {
    log.finer("method entry");
    Vehicle vehicle = objectPool.getObject(Vehicle.class, ref);
    if (vehicle == null) {
      throw new ObjectUnknownException(ref);
    }
    objectPool.removeObject(ref);
    objectPool.emitObjectEvent(null,
                               vehicle.clone(),
                               TCSObjectEvent.Type.OBJECT_REMOVED);
    return vehicle;
  }

  /**
   * Creates a new block with a unique name and all other attributes set to
   * default values.
   *
   * @param objectID The object ID of the newly created block. If
   * <code>null</code>, a new, unique one will be generated.
   * @return The newly created block.
   */
  public Block createBlock(Integer objectID) {
    log.finer("method entry");
    // Get a unique ID and name for the new point and create an instance.
    int blockID = objectID != null ? objectID : objectPool.getUniqueObjectId();
    String blockName = objectPool.getUniqueObjectName("Block-", "0000");
    Block newBlock = new Block(blockID, blockName);
    // Store the instance in the global object pool.
    try {
      objectPool.addObject(newBlock);
    }
    catch (ObjectExistsException exc) {
      log.log(Level.SEVERE, "Allegedly unique object ID/name already exists",
              exc);
      throw new IllegalStateException(
          "Allegedly unique object ID/name already exists", exc);
    }
    objectPool.emitObjectEvent(newBlock.clone(),
                               null,
                               TCSObjectEvent.Type.OBJECT_CREATED);
    // Return the newly created block.
    return newBlock;
  }

  /**
   * Returns the block belonging to the given reference.
   *
   * @param ref A reference to the block to return.
   * @return The referenced block, if it exists, or <code>null</code>, if it
   * doesn't.
   */
  public Block getBlock(TCSObjectReference<Block> ref) {
    log.finer("method entry");
    return objectPool.getObject(Block.class, ref);
  }

  /**
   * Returns the block with the given name.
   *
   * @param blockName The name of the block to return.
   * @return The block with the given name, if it exists, or <code>null</code>,
   * if it doesn't.
   */
  public Block getBlock(String blockName) {
    log.finer("method entry");
    return objectPool.getObject(Block.class, blockName);
  }

  /**
   * Returns a set of blocks whose names match the given regular expression.
   *
   * @param regexp The regular expression selecting the blocks returned. If
   * <code>null</code>, all points will be returned.
   * @return A set of blocks whose names match the given regular expression. If
   * no such blocks exist, the returned set is empty.
   */
  public Set<Block> getBlocks(Pattern regexp) {
    log.finer("method entry");
    return objectPool.getObjects(Block.class, regexp);
  }

  /**
   * Adds a member to a block.
   *
   * @param ref A reference to the block to be modified.
   * @param newMemberRef A reference to the new member.
   * @return The modified block.
   * @throws ObjectUnknownException If any of the referenced block or member do
   * not exist.
   */
  public Block addBlockMember(TCSObjectReference<Block> ref,
                              TCSResourceReference<?> newMemberRef)
      throws ObjectUnknownException {
    Block block = objectPool.getObject(Block.class, ref);
    if (block == null) {
      throw new ObjectUnknownException(ref);
    }
    Block previousState = block.clone();
    TCSObject<?> object = objectPool.getObject(newMemberRef);
    if (!(object instanceof TCSResource)) {
      throw new ObjectUnknownException(ref);
    }
    TCSResourceReference<?> memberRef = ((TCSResource) object).getReference();
    block.addMember(memberRef);
    objectPool.emitObjectEvent(block.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return block;
  }

  /**
   * Removes a member from a block.
   *
   * @param ref A reference to the block to be modified.
   * @param rmMemberRef A reference to the member to be removed.
   * @return The modified block.
   * @throws ObjectUnknownException If the referenced block does not exist.
   */
  public Block removeBlockMember(TCSObjectReference<Block> ref,
                                 TCSResourceReference<?> rmMemberRef)
      throws ObjectUnknownException {
    Block block = objectPool.getObject(Block.class, ref);
    if (block == null) {
      throw new ObjectUnknownException(ref);
    }
    Block previousState = block.clone();
    block.removeMember(rmMemberRef);
    objectPool.emitObjectEvent(block.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return block;
  }

  /**
   * Removes a block.
   *
   * @param ref A reference to the block to be removed.
   * @return The removed block.
   * @throws ObjectUnknownException If the referenced block does not exist.
   */
  public Block removeBlock(TCSObjectReference<Block> ref)
      throws ObjectUnknownException {
    log.finer("method entry");
    Block block = objectPool.getObject(Block.class, ref);
    if (block == null) {
      throw new ObjectUnknownException(ref);
    }
    // Remove the block.
    objectPool.removeObject(ref);
    objectPool.emitObjectEvent(null,
                               block.clone(),
                               TCSObjectEvent.Type.OBJECT_REMOVED);
    return block;
  }

  /**
   * Creates a new group with a unique name and all other attributes set to
   * default values.
   *
   * @param objectID The object ID of the newly created group. If
   * <code>null</code>, a new, unique one will be generated.
   * @return The newly created group.
   */
  public Group createGroup(Integer objectID) {
    log.finer("method entry");
    // Get a unique ID and name for the new point and create an instance.
    int groupID = objectID != null ? objectID : objectPool.getUniqueObjectId();
    String groupName = objectPool.getUniqueObjectName("Group-", "0000");
    Group newGroup = new Group(groupID, groupName);
    // Store the instance in the global object pool.
    try {
      objectPool.addObject(newGroup);
    }
    catch (ObjectExistsException exc) {
      log.log(Level.SEVERE, "Allegedly unique object ID/name already exists",
              exc);
      throw new IllegalStateException(
          "Allegedly unique object ID/name already exists", exc);
    }
    objectPool.emitObjectEvent(newGroup.clone(),
                               null,
                               TCSObjectEvent.Type.OBJECT_CREATED);
    // Return the newly created group.
    return newGroup;
  }

  /**
   * Returns a set of groups whose names match the given regular expression.
   *
   * @param regexp The regular expression selecting the groups returned. If
   * <code>null</code>, all points will be returned.
   * @return A set of groups whose names match the given regular expression. If
   * no such groups exist, the returned set is empty.
   */
  public Set<Group> getGroups(Pattern regexp) {
    log.finer("method entry");
    return objectPool.getObjects(Group.class, regexp);
  }

  /**
   * Adds a member to a group.
   *
   * @param ref A reference to the group to be modified.
   * @param newMemberRef A reference to the new member.
   * @return The modified group.
   * @throws ObjectUnknownException If any of the referenced group or member do
   * not exist.
   */
  public Group addGroupMember(TCSObjectReference<Group> ref,
                              TCSObjectReference<?> newMemberRef)
      throws ObjectUnknownException {
    Group group = objectPool.getObject(Group.class, ref);
    if (group == null) {
      throw new ObjectUnknownException(ref);
    }
    Group previousState = group.clone();
    TCSObject<?> object = objectPool.getObject(newMemberRef);
    if (object == null) {
      throw new ObjectUnknownException(newMemberRef);
    }
    group.addMember(object.getReference());
    objectPool.emitObjectEvent(group.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return group;
  }

  /**
   * Removes a member from a group.
   *
   * @param ref A reference to the group to be modified.
   * @param rmMemberRef A reference to the member to be removed.
   * @return The modified group.
   * @throws ObjectUnknownException If the referenced group does not exist.
   */
  public Group removeGroupMember(TCSObjectReference<Group> ref,
                                 TCSObjectReference<?> rmMemberRef)
      throws ObjectUnknownException {
    Group group = objectPool.getObject(Group.class, ref);
    if (group == null) {
      throw new ObjectUnknownException(ref);
    }
    Group previousState = group.clone();
    group.removeMember(rmMemberRef);
    objectPool.emitObjectEvent(group.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return group;
  }

  /**
   * Removes a group.
   *
   * @param ref A reference to the group to be removed.
   * @return The removed group.
   * @throws ObjectUnknownException If the referenced group does not exist.
   */
  public Group removeGroup(TCSObjectReference<Group> ref)
      throws ObjectUnknownException {
    log.finer("method entry");
    Group group = objectPool.getObject(Group.class, ref);
    if (group == null) {
      throw new ObjectUnknownException(ref);
    }
    // Remove the block.
    objectPool.removeObject(ref);
    objectPool.emitObjectEvent(null,
                               group.clone(),
                               TCSObjectEvent.Type.OBJECT_REMOVED);
    return group;
  }

  /**
   * Creates a new static route with a unique name and all other attributes set
   * to default values.
   *
   * @param objectID The object ID of the newly created route. If
   * <code>null</code>, a new, unique one will be generated.
   * @return The newly created static route.
   */
  public StaticRoute createStaticRoute(Integer objectID) {
    log.finer("method entry");
    // Get a unique ID and name for the new object and create an instance.
    int routeID = objectID != null ? objectID : objectPool.getUniqueObjectId();
    String routeName = objectPool.getUniqueObjectName("Route-", "0000");
    StaticRoute newRoute = new StaticRoute(routeID, routeName);
    // Store the instance in the global object pool.
    try {
      objectPool.addObject(newRoute);
    }
    catch (ObjectExistsException exc) {
      log.log(Level.SEVERE, "Allegedly unique object ID/name already exists",
              exc);
      throw new IllegalStateException(
          "Allegedly unique object ID/name already exists", exc);
    }
    objectPool.emitObjectEvent(newRoute.clone(),
                               null,
                               TCSObjectEvent.Type.OBJECT_CREATED);
    // Return the newly created route.
    return newRoute;
  }

  /**
   * Returns the route belonging to the given reference.
   *
   * @param ref A reference to the route to return.
   * @return The referenced route, if it exists, or <code>null</code>, if it
   * doesn't.
   */
  public StaticRoute getStaticRoute(TCSObjectReference<StaticRoute> ref) {
    log.finer("method entry");
    return objectPool.getObject(StaticRoute.class, ref);
  }

  /**
   * Returns the route with the given name.
   *
   * @param routeName The name of the route to return.
   * @return The route with the given name, if it exists, or <code>null</code>,
   * if it doesn't.
   */
  public StaticRoute getStaticRoute(String routeName) {
    log.finer("method entry");
    return objectPool.getObject(StaticRoute.class, routeName);
  }

  /**
   * Returns a set of routes whose names match the given regular expression.
   *
   * @param regexp The regular expression selecting the routes returned. If
   * <code>null</code>, all routes will be returned.
   * @return A set of routes whose names match the given regular expression. If
   * no such routes exist, the returned set is empty.
   */
  public Set<StaticRoute> getStaticRoutes(Pattern regexp) {
    log.finer("method entry");
    return objectPool.getObjects(StaticRoute.class, regexp);
  }

  /**
   * Adds a static route hop.
   * 
   * @param routeRef A reference to the route
   * @param newHopRef A reference to the new hop
   * @return The new static route
   * @throws ObjectUnknownException If a parameter is unknown 
   */
  public StaticRoute addStaticRouteHop(TCSObjectReference<StaticRoute> routeRef,
                                       TCSObjectReference<Point> newHopRef)
      throws ObjectUnknownException {
    log.finer("method entry");
    StaticRoute route = objectPool.getObject(StaticRoute.class, routeRef);
    if (route == null) {
      throw new ObjectUnknownException(routeRef);
    }
    StaticRoute previousState = route.clone();
    Point point = objectPool.getObject(Point.class, newHopRef);
    if (point == null) {
      throw new ObjectUnknownException(newHopRef);
    }
    route.addHop(point.getReference());
    objectPool.emitObjectEvent(route.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return route;
  }

  /**
   * Clears all static route hops in the given route.
   * 
   * @param routeRef A reference to the route to be cleared
   * @return The modifired static route
   * @throws ObjectUnknownException If a parameter is unknown
   */
  public StaticRoute clearStaticRouteHops(
      TCSObjectReference<StaticRoute> routeRef)
      throws ObjectUnknownException {
    log.finer("method entry");
    StaticRoute route = objectPool.getObject(StaticRoute.class, routeRef);
    if (route == null) {
      throw new ObjectUnknownException(routeRef);
    }
    StaticRoute previousState = route.clone();
    route.clearHops();
    objectPool.emitObjectEvent(route.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return route;
  }

  /**
   * Removes a static route.
   *
   * @param ref A reference to the block to be removed.
   * @return The removed block.
   * @throws ObjectUnknownException If the referenced block does not exist.
   */
  public StaticRoute removeStaticRoute(TCSObjectReference<StaticRoute> ref)
      throws ObjectUnknownException {
    log.finer("method entry");
    StaticRoute route = objectPool.getObject(StaticRoute.class, ref);
    if (route == null) {
      throw new ObjectUnknownException(ref);
    }
    StaticRoute previousState = route.clone();
    // Remove the block.
    objectPool.removeObject(ref);
    objectPool.emitObjectEvent(route.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_REMOVED);
    return route;
  }

  /**
   * Expands a set of resources <em>A</em> to a set of resources <em>B</em>.
   * <em>B</em> contains the resources in <em>A</em> with blocks expanded to
   * their actual members.
   * The given set is not modified.
   *
   * @param resources The set of resources to be expanded.
   * @return The given set with resources expanded.
   * @throws ObjectUnknownException If an object referenced in the given set
   * does not exist.
   */
  public Set<TCSResource> expandResources(Set<TCSResourceReference> resources)
      throws ObjectUnknownException {
    log.finer("method entry");
    Set<TCSResource> result = new HashSet<>();
    Set<Block> blocks = getBlocks(null);
    for (TCSResourceReference curRef : resources) {
      TCSObject object = objectPool.getObject(curRef);
      if (object == null) {
        throw new ObjectUnknownException(curRef);
      }
      TCSResource resource = (TCSResource) object;
      result.add(resource);
      for (Block curBlock : blocks) {
        // If the current block contains the resource, add all of the block's
        // members to the result.
        if (curBlock.containsMember(resource.getReference())) {
          for (TCSResourceReference<?> curResRef : curBlock.getMembers()) {
            TCSResource member = (TCSResource) objectPool.getObject(curResRef);
            result.add(member);
          }
        }
      }
    }
    return result;
  }

  /**
   * Returns an informational string describing this model's topology.
   *
   * @return An informational string describing this model's topology.
   */
  public String getInfo() {
    log.finer("method entry");
    StringBuilder result = new StringBuilder();
    Set<Point> points = new TreeSet<>(TCSObject.idComparator);
    Set<Path> paths = new TreeSet<>(TCSObject.idComparator);
    Set<LocationType> locationTypes = new TreeSet<>(TCSObject.idComparator);
    Set<Location> locations = new TreeSet<>(TCSObject.idComparator);
    Set<Vehicle> vehicles = new TreeSet<>(TCSObject.idComparator);
    Set<TCSObject<?>> objects = objectPool.getObjects((Pattern) null);
    for (TCSObject<?> curObject : objects) {
      if (curObject instanceof Point) {
        points.add((Point) curObject);
      }
      else if (curObject instanceof Path) {
        paths.add((Path) curObject);
      }
      else if (curObject instanceof LocationType) {
        locationTypes.add((LocationType) curObject);
      }
      else if (curObject instanceof Location) {
        locations.add((Location) curObject);
      }
      else if (curObject instanceof Vehicle) {
        vehicles.add((Vehicle) curObject);
      }
    }
    result.append("Model data:\n");
    result.append(" Name: " + name + "\n");
    result.append("Points:\n");
    for (Point curPoint : points) {
      result.append(" Point:\n");
      result.append("  ID: " + curPoint.getId() + "\n");
      result.append("  Name: " + curPoint.getName() + "\n");
      result.append("  Type: " + curPoint.getType() + "\n");
      result.append("  X: " + curPoint.getPosition().getX() + "\n");
      result.append("  Y: " + curPoint.getPosition().getY() + "\n");
      result.append("  Z: " + curPoint.getPosition().getZ() + "\n");
    }
    result.append("Paths:\n");
    for (Path curPath : paths) {
      result.append(" Path:\n");
      result.append("  ID: " + curPath.getId() + "\n");
      result.append("  Name: " + curPath.getName() + "\n");
      result.append("  Source: " + curPath.getSourcePoint().getId()
          + " (" + curPath.getSourcePoint().getName() + ")\n");
      result.append("  Destination: " + curPath.getDestinationPoint().getId()
          + " (" + curPath.getDestinationPoint().getName() + ")\n");
      result.append("  Length: " + curPath.getLength() + "\n");
    }
    result.append("LocationTypes:\n");
    for (LocationType curType : locationTypes) {
      result.append(" LocationType:\n");
      result.append("  ID: " + curType.getId() + "\n");
      result.append("  Name: " + curType.getName() + "\n");
      result.append("  Operations: "
          + curType.getAllowedOperations().toString() + "\n");
    }
    result.append("Locations:\n");
    for (Location curLocation : locations) {
      result.append(" Location:\n");
      result.append("  ID: " + curLocation.getId() + "\n");
      result.append("  Name: " + curLocation.getName() + "\n");
      result.append("  Type: " + curLocation.getType().getId()
          + " (" + curLocation.getType().getName() + ")\n");
      for (Location.Link curLink : curLocation.getAttachedLinks()) {
        result.append("  Link:\n");
        result.append("   Point: " + curLink.getPoint().getId()
            + " (" + curLink.getPoint().getName() + ")\n");
        result.append("   Allowed operations:" + curLink.getAllowedOperations()
            + "\n");
      }
    }
    result.append("Vehicles:\n");
    for (Vehicle curVehicle : vehicles) {
      result.append(" Vehicle:\n");
      result.append("  ID: " + curVehicle.getId() + "\n");
      result.append("  Name: " + curVehicle.getName() + "\n");
      result.append("  Length: " + curVehicle.getLength());
    }
    return result.toString();
  }

  /**
   * Attaches a resource to another one.
   *
   * @param resRef A reference to the resource that is to receive the
   * attachment.
   * @param newResRef A reference to the resource to be attached.
   * @return The modified resource.
   * @throws ObjectUnknownException If any of the referenced resources does not
   * exist.
   */
  public TCSResource<?> attachResource(TCSResourceReference<?> resRef,
                                       TCSResourceReference<?> newResRef)
      throws ObjectUnknownException {
    log.finer("method entry");
    TCSObject object = objectPool.getObject(resRef);
    if (!(object instanceof TCSResource)) {
      throw new ObjectUnknownException(resRef);
    }
    TCSResource<?> resource = (TCSResource<?>) object;
    object = objectPool.getObject(newResRef);
    if (!(object instanceof TCSResource)) {
      throw new ObjectUnknownException(newResRef);
    }
    TCSResource<?> newResource = (TCSResource<?>) object;
    resource.attachResource(newResource.getReference());
    return resource;
  }

  /**
   * Detaches a resource from another one.
   *
   * @param resRef A reference to the resource from which the attached resource
   * is to be removed.
   * @param rmResRef A reference to the resource to be detached.
   * @return The modified resource.
   * @throws ObjectUnknownException If any of the referenced resources does not
   * exist.
   */
  public TCSResource<?> detachResource(TCSResourceReference<?> resRef,
                                       TCSResourceReference<?> rmResRef)
      throws ObjectUnknownException {
    log.finer("method entry");
    TCSObject object = objectPool.getObject(resRef);
    if (!(object instanceof TCSResource)) {
      throw new ObjectUnknownException(resRef);
    }
    TCSResource<?> resource = (TCSResource<?>) object;
    object = objectPool.getObject(rmResRef);
    if (!(object instanceof TCSResource)) {
      throw new ObjectUnknownException(rmResRef);
    }
    TCSResource<?> newResource = (TCSResource<?>) object;
    resource.detachResource(newResource.getReference());
    return resource;
  }

  /**
   * Expands a set of resources, adding to it all resources attached to those
   * that it initially contains.
   *
   * @param initialSet A set of references to the initial resources.
   * @return A new set containing both the references to the initial resources
   * and references to the resources attached to them.
   */
  public Set<TCSResourceReference<?>> getEffectiveResources(
      Set<TCSResourceReference<?>> initialSet) {
    log.finer("method entry");
    if (initialSet == null) {
      throw new NullPointerException("initialSet is null");
    }
    Set<TCSResourceReference<?>> result = new HashSet<>();
    for (TCSResourceReference<?> initialRef : initialSet) {
      // XXX Should throw ObjectUnknownException if resource doesn't exist
      TCSResource<?> initialResource =
          (TCSResource<?>) objectPool.getObject(initialRef);
      result.add(initialResource.getReference());
      for (TCSResourceReference<?> curRef :
           initialResource.getAttachedResources()) {
        // XXX Should throw ObjectUnknownException if resource doesn't exist
        result.add((TCSResourceReference<?>) objectPool.getObject(curRef).getReference());
      }
    }
    return result;
  }
}
