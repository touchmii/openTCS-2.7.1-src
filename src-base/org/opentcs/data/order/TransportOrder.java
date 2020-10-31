/*
 * openTCS copyright information:
 * Copyright (c) 2006 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.data.order;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import javax.xml.bind.annotation.XmlType;
import org.opentcs.data.TCSObject;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Vehicle;
import org.opentcs.util.UniqueTimestampGenerator;

/**
 * Represents a set of movements and operations that are to be executed by a
 * {@link org.opentcs.data.model.Vehicle Vehicle}.
 * <p>
 * A TransportOrder basically encapsulates a list of {@link DriveOrder
 * DriveOrders}.
 * </p>
 * <p>
 * Transport orders may depend on other transport orders in the systems, which
 * means they may not be processed before the orders they depend on have been
 * processed. Furthermore, the priority of each transport order is measured by
 * its deadline; orders for which the deadline is closer in the future
 * implicitly have higher priority than others.
 * </p>
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public final class TransportOrder
    extends TCSObject<TransportOrder>
    implements Serializable, Cloneable {

  /**
   * A comparator for sorting transport orders by their priority, with those
   * with the highest priority coming first.
   * Transport orders are sorted by their deadlines first; if two orders have
   * exactly the same deadline, they are sorted by their (unique) creation
   * times, with the older one coming first.
   */
  public static final Comparator<TransportOrder> priorityComparator =
      new PriorityComparator();
  /**
   * A comparator for sorting transport orders by their age, with the oldest
   * ones coming first.
   */
  public static final Comparator<TransportOrder> ageComparator =
      new AgeComparator();
  /**
   * The timestamp generator for order creation times.
   */
  private static final UniqueTimestampGenerator timestampGenerator =
      new UniqueTimestampGenerator();
  /**
   * This class's Logger.
   */
  private static final Logger log =
      Logger.getLogger(TransportOrder.class.getName());
  /**
   * A set of TransportOrders that must have been finished before this one may
   * be processed.
   */
  private Set<TCSObjectReference<TransportOrder>> dependencies =
      new LinkedHashSet<>();
  /**
   * A list of rejections for this transport order.
   */
  private List<Rejection> rejections = new LinkedList<>();
  /**
   * A list of drive orders that have been finished already.
   */
  private List<DriveOrder> pastDriveOrders = new LinkedList<>();
  /**
   * A list of drive orders that still have to be processed as part of this
   * transport order (in the order they have to be processed in).
   */
  private List<DriveOrder> futureDriveOrders = new LinkedList<>();
  /**
   * The drive order that is currently being processed.
   */
  private DriveOrder currentDriveOrder;
  /**
   * This transport order's current state.
   */
  private State state = State.RAW;
  /**
   * The point of time at which this TransportOrder was created.
   */
  private final long creationTime;
  /**
   * The point of time at which this TransportOrder must have been finished.
   */
  private long deadline = Long.MAX_VALUE;
  /**
   * The point of time at which this transport order was finished.
   */
  private long finishedTime = Long.MIN_VALUE;
  /**
   * A reference to the vehicle that is intended to process this transport
   * order. If this order is free to be processed by any vehicle, this is
   * <code>null</code>.
   */
  private TCSObjectReference<Vehicle> intendedVehicle;
  /**
   * A reference to the vehicle currently processing this transport order. If
   * this transport order is not being processed at the moment, this is
   * <code>null</code>.
   */
  private TCSObjectReference<Vehicle> processingVehicle;
  /**
   * The order sequence this transport order belongs to. May be
   * <code>null</code> in case this order isn't part of any sequence.
   */
  private TCSObjectReference<OrderSequence> wrappingSequence;
  /**
   * Whether this order is dispensable (may be withdrawn automatically).
   */
  private boolean dispensable;

  /**
   * Creates a new TransportOrder.
   *
   * @param objectID This transport order's ID.
   * @param name This transport order's name.
   * @param destinations A list of destinations that are to be travelled to
   * when processing this transport order.
   */
  public TransportOrder(int objectID, String name,
                        List<DriveOrder.Destination> destinations) {
    super(objectID, name);
    if (destinations == null) {
      throw new NullPointerException("destinations is null");
    }
    if (destinations.isEmpty()) {
      throw new IllegalArgumentException("destinations is empty");
    }
    for (DriveOrder.Destination curDest : destinations) {
      DriveOrder driveOrder = new DriveOrder(curDest);
      driveOrder.setTransportOrder(this.getReference());
      futureDriveOrders.add(driveOrder);
    }
    creationTime = timestampGenerator.getNextTimestamp();
  }

  // Methods not declared in any interface start here
  /**
   * Returns this transport order's current state.
   *
   * @return This transport order's current state.
   */
  public State getState() {
    return state;
  }

  /**
   * Checks if this transport order's current state is equal to the given one.
   *
   * @param otherState The state to compare to this transport order's one.
   * @return <code>true</code> if, and only if, the given state is equal to this
   * transport order's one.
   */
  public boolean hasState(State otherState) {
    if (otherState == null) {
      throw new NullPointerException("otherState is null");
    }
    return this.state.equals(otherState);
  }

  /**
   * Changes this transport order's current state.
   *
   * @param newState The new state this transport order is supposed to be in.
   */
  public void setState(State newState) {
    if (newState == null) {
      throw new NullPointerException("newState is null");
    }
    if (state.isFinalState()) {
      throw new IllegalStateException("trying to modify state " + state + " to "
          + newState);
    }
    state = newState;
    if (state.equals(State.FINISHED)) {
      finishedTime = System.currentTimeMillis();
    }
  }

  /**
   * Returns this transport order's creation time.
   *
   * @return This transport order's creation time.
   */
  public long getCreationTime() {
    return creationTime;
  }

  /**
   * Returns this transport order's deadline. If the value of transport order's
   * deadline was not changed, the initial value <code>Long.MAX_VALUE</code>
   * is returned.
   *
   * @return This transport order's deadline or the initial deadline value.
   * <code>Long.MAX_VALUE</code>, if the deadline was not changed.
   */
  public long getDeadline() {
    return deadline;
  }

  /**
   * Sets this transport order's deadline.
   *
   * @param newDeadline This transport order's new deadline.
   */
  public void setDeadline(long newDeadline) {
    deadline = newDeadline;
  }

  /**
   * Returns the point of time at which this transport order was finished.
   * If the transport order has not been finished, yet,
   * <code>Long.MIN_VALUE</code> is returned.
   *
   * @return The point of time at which this transport order was finished, or
   * <code>Long.MIN_VALUE</code>, if the transport order has not been finished,
   * yet.
   */
  public long getFinishedTime() {
    return finishedTime;
  }

  /**
   * Returns a reference to the vehicle that is intended to process this
   * transport order.
   *
   * @return A reference to the vehicle that is intended to process this
   * transport order. If this order is free to be processed by any vehicle,
   * <code>null</code> is returned.
   */
  public TCSObjectReference<Vehicle> getIntendedVehicle() {
    return intendedVehicle;
  }

  /**
   * Sets a reference to the vehicle that is intended to process this transport
   * order.
   *
   * @param vehicle The reference to the vehicle intended to process this order.
   */
  public void setIntendedVehicle(TCSObjectReference<Vehicle> vehicle) {
    intendedVehicle = vehicle;
  }

  /**
   * Returns a reference to the vehicle currently processing this transport
   * order.
   *
   * @return A reference to the vehicle currently processing this transport
   * order. If this transport order is not currently being processed,
   * <code>null</code> is returned.
   */
  public TCSObjectReference<Vehicle> getProcessingVehicle() {
    return processingVehicle;
  }

  /**
   * Sets a reference to the vehicle currently processing this transport order.
   *
   * @param vehicle The reference to the vehicle currently processing this
   * transport order.
   */
  public void setProcessingVehicle(TCSObjectReference<Vehicle> vehicle) {
    processingVehicle = vehicle;
  }

  /**
   * Returns the set of transport orders this order depends on.
   *
   * @return The set of transport orders this order depends on.
   */
  public Set<TCSObjectReference<TransportOrder>> getDependencies() {
    return dependencies;
  }

  /**
   * Adds a dependency on another transport order.
   *
   * @param newDep A reference to the transport order that must be finished
   * before this one may be started.
   * @return <code>true</code> if, and only if, the given transport order was
   * not already a dependency for this one.
   */
  public boolean addDependency(TCSObjectReference<TransportOrder> newDep) {
    if (newDep == null) {
      throw new NullPointerException("newDep is null");
    }
    return dependencies.add(newDep);
  }

  /**
   * Removes a dependency on another transport order.
   *
   * @param rmDep A reference to the transport order that is no longer a
   * dependency for this one.
   * @return <code>true</code> if, and only if, the given transport order was
   * a dependency for this one.
   */
  public boolean removeDependency(TCSObjectReference<TransportOrder> rmDep) {
    if (rmDep == null) {
      throw new NullPointerException("rmDep is null");
    }
    return dependencies.remove(rmDep);
  }

  /**
   * Returns a list of rejections for this transport order.
   *
   * @return A list of rejections for this transport order.
   */
  public List<Rejection> getRejections() {
    return new ArrayList<>(rejections);
  }

  /**
   * Adds a rejection for this transport order.
   *
   * @param newRejection The new rejection.
   */
  public void addRejection(Rejection newRejection) {
    if (newRejection == null) {
      throw new NullPointerException("newRejection is null");
    }
    rejections.add(newRejection);
  }

  /**
   * Returns a list of DriveOrders that have been processed already.
   *
   * @return A list of DriveOrders that have been processed already.
   */
  public List<DriveOrder> getPastDriveOrders() {
    return new ArrayList<>(pastDriveOrders);
  }

  /**
   * Returns a list of DriveOrders that still need to be processed.
   *
   * @return A list of DriveOrders that still need to be processed.
   */
  public List<DriveOrder> getFutureDriveOrders() {
    return new ArrayList<>(futureDriveOrders);
  }

  /**
   * Copies drive order data from a list of drive orders to this transport
   * order's future drive orders.
   *
   * @param newOrders The drive orders containing the data to be copied into
   * this transport order's drive orders.
   * @throws IllegalArgumentException If the destinations of the given drive
   * orders do not match the destinations of the drive orders in this transport
   * order.
   */
  public void setFutureDriveOrders(List<DriveOrder> newOrders)
      throws IllegalArgumentException {
    if (newOrders == null) {
      throw new NullPointerException("newOrders is null");
    }
    int orderCount = newOrders.size();
    if (orderCount != futureDriveOrders.size()) {
      throw new IllegalArgumentException("newOrders has wrong size");
    }
    // Check if the destinations of the given drive orders are equivalent to the
    // ones we have.
    for (int i = 0; i < orderCount; i++) {
      DriveOrder myOrder = futureDriveOrders.get(i);
      DriveOrder newOrder = newOrders.get(i);
      if (!myOrder.getDestination().equals(newOrder.getDestination())) {
        throw new IllegalArgumentException(
            "newOrders' destinations do not equal mine");
      }
    }
    // Copy the given drive orders' data to ours.
    for (int i = 0; i < orderCount; i++) {
      DriveOrder myOrder = futureDriveOrders.get(i);
      DriveOrder newOrder = newOrders.get(i);
      myOrder.setRoute(newOrder.getRoute());
      myOrder.setState(newOrder.getState());
    }
  }

  /**
   * Returns the current drive order, or <code>null</code>, if no drive order is
   * currently being processed.
   *
   * @return the current drive order, or <code>null</code>, if no drive order is
   * currently being processed.
   */
  public DriveOrder getCurrentDriveOrder() {
    return currentDriveOrder;
  }

  /**
   * Returns a list of all drive orders, i.e. the past, current and future drive
   * orders.
   *
   * @return A list of all drive orders, i.e. the past, current and future drive
   * orders. If no drive orders exist, the returned list is empty.
   */
  public List<DriveOrder> getAllDriveOrders() {
    List<DriveOrder> result = new LinkedList<>();
    result.addAll(pastDriveOrders);
    if (currentDriveOrder != null) {
      result.add(currentDriveOrder);
    }
    result.addAll(futureDriveOrders);
    return result;
  }

  /**
   * Makes the first of the future drive orders the current one.
   * Fails if there already is a current drive order or if the list of future
   * drive orders is empty.
   *
   * @throws IllegalStateException If there already is a current drive order or
   * if the list of future drive orders is empty.
   */
  public void setInitialDriveOrder()
      throws IllegalStateException {
    if (currentDriveOrder != null) {
      throw new IllegalStateException("currentDriveOrder is not null");
    }
    if (futureDriveOrders.isEmpty()) {
      throw new IllegalStateException("futureDriveOrders is empty");
    }
    currentDriveOrder = futureDriveOrders.remove(0);
    currentDriveOrder.setState(DriveOrder.State.TRAVELLING);
  }

  /**
   * Marks the current drive order as finished, adds it to the list of past
   * drive orders and sets the current drive order to the next one of the list
   * of future drive orders (or <code>null</code>, if that list is empty).
   * If the current drive order is <code>null</code> because all drive orders
   * have been finished already or none has been started, yet, nothing happens.
   */
  public void setNextDriveOrder() {
    // Mark the current drive order as finished and push it to the list of past
    // drive orders.
    if (currentDriveOrder != null) {
      pastDriveOrders.add(currentDriveOrder);
    }
    else {
      log.warning("Cannot finish current drive order as it is null.");
    }
    // Pull in the next drive order, if there is any.
    if (futureDriveOrders.isEmpty()) {
      currentDriveOrder = null;
    }
    else {
      currentDriveOrder = futureDriveOrders.remove(0);
    }
  }

  /**
   * Sets the current drive order's state to the given one.
   *
   * @param newState The current drive order's new state.
   */
  public void setCurrentDriveOrderState(DriveOrder.State newState) {
    if (newState == null) {
      throw new NullPointerException("newState is null");
    }
    if (currentDriveOrder == null) {
      log.warning("currentDriveOrder is null");
      return;
    }
    currentDriveOrder.setState(newState);
  }

  /**
   * Returns the order sequence this order belongs to, or <code>null</code>, if
   * it doesn't belong to any sequence.
   *
   * @return The order sequence this order belongs to, or <code>null</code>, if
   * it doesn't belong to any sequence.
   */
  public TCSObjectReference<OrderSequence> getWrappingSequence() {
    return wrappingSequence;
  }

  /**
   * Sets the order sequence this transport order belongs to.
   *
   * @param sequence The order sequence this order belongs to. May be
   * <code>null</code> to indicate that this order does not belong to any
   * sequence.
   */
  public void setWrappingSequence(TCSObjectReference<OrderSequence> sequence) {
    wrappingSequence = sequence;
  }

  /**
   * Checks if this order is dispensable.
   *
   * @return <code>true</code> if, and only if, this order is dispensable.
   */
  public boolean isDispensable() {
    return dispensable;
  }

  /**
   * Sets this order's <em>dispensable</em> flag.
   *
   * @param dispensable This order's new <em>dispensable</em> flag.
   */
  public void setDispensable(boolean dispensable) {
    this.dispensable = dispensable;
  }

  @Override
  public TransportOrder clone() {
    TransportOrder clone = (TransportOrder) super.clone();
    clone.dependencies = new LinkedHashSet<>();
    for (TCSObjectReference<TransportOrder> curRef : dependencies) {
      clone.dependencies.add(curRef.clone());
    }
    clone.rejections = new LinkedList<>();
    clone.rejections.addAll(this.rejections);
    clone.pastDriveOrders = new LinkedList<>();
    for (DriveOrder curDriveOrder : pastDriveOrders) {
      clone.pastDriveOrders.add(curDriveOrder.clone());
    }
    clone.futureDriveOrders = new LinkedList<>();
    for (DriveOrder curDriveOrder : futureDriveOrders) {
      clone.futureDriveOrders.add(curDriveOrder.clone());
    }
    clone.currentDriveOrder =
        (currentDriveOrder == null) ? null : currentDriveOrder.clone();
    return clone;
  }

  /**
   * This enumeration defines the various states a transport order may be in.
   */
  @XmlType(name = "transportOrderState")
  public static enum State {

    /**
     * A transport order's initial state.
     * A transport order remains in this state until its parameters have been
     * set up completely.
     */
    RAW,
    /**
     * Set (by a user/client) when a transport order's parameters have been set
     * up completely and the kernel should dispatch it when possible.
     */
    ACTIVE,
    /**
     * Marks a transport order as ready to be dispatched to a vehicle (i.e. all
     * its dependencies have been finished).
     */
    DISPATCHABLE,
    /**
     * Marks a transport order as being processed by a vehicle.
     */
    BEING_PROCESSED,
    /**
     * Indicates the transport order is withdrawn from a processing vehicle but
     * not yet in its final state (which will be FAILED), as the vehicle has not
     * yet finished/cleaned up.
     */
    WITHDRAWN,
    /**
     * Marks a transport order as successfully completed.
     */
    FINISHED,
    /**
     * General failure state that marks a transport order as failed.
     */
    FAILED,
    /**
     * Failure state that marks a transport order as unroutable, i.e. it is
     * impossible to find a route that would allow a vehicle to process the
     * transport order completely.
     */
    UNROUTABLE;

    /**
     * Checks if this state is a final state for a transport order.
     *
     * @return <code>true</code> if, and only if, this state is a final state
     * for a transport order - i.e. FINISHED, FAILED or UNROUTABLE.
     */
    public boolean isFinalState() {
      return this.equals(FINISHED)
          || this.equals(FAILED)
          || this.equals(UNROUTABLE);
    }
  }

  /**
   * A comparator for sorting transport orders by their priority, with those
   * with the highest priority coming first.
   * Transport orders are sorted by their deadlines first; if two orders have
   * exactly the same deadline, they are sorted by their (unique) creation
   * times, with the older one coming first.
   */
  private static class PriorityComparator
      implements Comparator<TransportOrder> {

    /**
     * Creates a new PriorityComparator.
     */
    public PriorityComparator() {
    }

    @Override
    public int compare(TransportOrder o1, TransportOrder o2) {
      int result;
      long difference = o1.getDeadline() - o2.getDeadline();
      if (difference < 0) {
        result = -1;
      }
      else if (difference > 0) {
        result = 1;
      }
      else {
        long ageDifference = o1.getCreationTime() - o2.getCreationTime();
        if (ageDifference < 0) {
          result = -1;
        }
        else if (ageDifference > 0) {
          result = 1;
        }
        else {
          result = 0;
        }
      }
      return result;
    }
  }

  /**
   * A comparator for sorting transport orders by their age, with the oldest
   * ones coming first.
   */
  private static class AgeComparator
      implements Comparator<TransportOrder> {

    /**
     * Creates a new AgeComparator.
     */
    public AgeComparator() {
    }

    @Override
    public int compare(TransportOrder o1, TransportOrder o2) {
      int result;
      long ageDifference = o1.getCreationTime() - o2.getCreationTime();
      if (ageDifference < 0) {
        result = -1;
      }
      else if (ageDifference > 0) {
        result = 1;
      }
      else {
        int idDifference = o1.getId() - o2.getId();
        if (idDifference < 0) {
          result = -1;
        }
        else if (idDifference > 0) {
          result = 1;
        }
        else {
          result = 0;
        }
      }
      return result;
    }
  }
}
