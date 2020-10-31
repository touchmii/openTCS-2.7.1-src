/*
 * openTCS copyright information:
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.module.recharging;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.inject.Inject;
import org.opentcs.access.Kernel;
import org.opentcs.access.LocalKernel;
import org.opentcs.algorithms.RechargeStrategy;
import org.opentcs.algorithms.Router;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Block;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;

/**
 * Tries to find recharge locations for vehicles that are off the route of other
 * vehicles.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
class SimpleRechargeStrategy
    implements RechargeStrategy {

  /**
   * Our kernel.
   */
  private final Kernel kernel;
  /**
   * Our router.
   */
  private final Router router;
  /**
   * Maps locations' access points to points sharing the same block(s).
   */
  private final Map<Point, Set<Point>> accessPoints = new HashMap<>();

  /**
   * Creates a new instance.
   * 
   * @param kernel The kernel we're working for.
   * @param router The router to use.
   */
  @Inject
  public SimpleRechargeStrategy(final LocalKernel kernel, final Router router) {
    this.kernel = Objects.requireNonNull(kernel, "kernel is null");
    this.router = Objects.requireNonNull(router, "router is null");

    Set<Block> allBlocks = kernel.getTCSObjects(Block.class);
    // Get all locations from the kernel, and for each of their access points,
    // build a set of points that share the same block(s).
    for (Location curLoc : kernel.getTCSObjects(Location.class)) {
      for (Point accessPoint : getAccessPoints(curLoc)) {
        accessPoints.put(accessPoint, getBlockedPoints(accessPoint, allBlocks));
      }
    }
  }

  @Override
  public Location getRechargeLocation(final Vehicle vehicle) {
    Objects.requireNonNull(vehicle, "vehicle is null");
    Objects.requireNonNull(vehicle.getCurrentPosition(),
                           "vehicle's current position is null");

    String rechargeOp = vehicle.getRechargeOperation();
    Point curPos = kernel.getTCSObject(Point.class,
                                       vehicle.getCurrentPosition());
    Set<Point> targetedPoints = router.getTargetedPoints();
    Location bestLocation = null;
    long bestCosts = Long.MAX_VALUE;
    for (Location curLoc : kernel.getTCSObjects(Location.class)) {
      LocationType lType = kernel.getTCSObject(LocationType.class,
                                               curLoc.getType());
      // If the location provides the vehicle's recharge operation
      // AND has an untargeted access point
      // AND that access point is closer than the best we found so far,
      // then the location is our new candidate.
      if (lType.isAllowedOperation(rechargeOp)) {
        long costs = getBestUntargetedAccessPointCosts(vehicle,
                                                       curPos,
                                                       curLoc,
                                                       targetedPoints,
                                                       rechargeOp);
        if (costs < bestCosts) {
          bestCosts = costs;
          bestLocation = curLoc;
        }
      }
    }
    return bestLocation;
  }

  /**
   * Returns the lowest possible costs for the given  vehicle travelling to an
   * untargeted access point of the given location.
   *
   * @param vehicle The vehicle for which to compute the routes.
   * @param srcPosition The position from which the vehicle would travel.
   * @param location The location the vehicle would travel to.
   * @param targetedPoints All points currently targeted by vehicles.
   * @param rechargeOp The vehicle's recharge operation.
   * @return The lowest possible costs for the given  vehicle travelling to an
   * untargeted access point of the given location.
   */
  private long getBestUntargetedAccessPointCosts(Vehicle vehicle,
                                                 Point srcPosition,
                                                 Location location,
                                                 Set<Point> targetedPoints,
                                                 String rechargeOp) {
    assert vehicle != null;
    assert srcPosition != null;
    assert location != null;
    assert targetedPoints != null;

    long bestLinkCosts = Long.MAX_VALUE;
    for (Location.Link curLink : location.getAttachedLinks()) {
      // This link is only interesting if it either does not define any allowed
      // operations at all or, if it does, allows the required recharge
      // operation.
      if (curLink.getAllowedOperations().isEmpty()
          || curLink.hasAllowedOperation(rechargeOp)) {

        Point accessPoint = kernel.getTCSObject(Point.class,
                                                curLink.getPoint());
        Set<Point> blockedPoints = accessPoints.get(accessPoint);

        boolean linkUsable = true;
        for (Point blockedPoint : blockedPoints) {
          Point blockedPointActu = kernel.getTCSObject(Point.class,
                                                       blockedPoint.getReference());
          // If the point is occupied by another vehicle, give up this link.
          if (blockedPointActu.getOccupyingVehicle() != null
              && !blockedPointActu.getOccupyingVehicle().equals(vehicle.getReference())) {
            linkUsable = false;
            break;
          }
          // If the point is targeted by another vehicle, give up this link.
          else if (targetedPoints.contains(blockedPointActu)) {
            linkUsable = false;
            break;
          }
        }

        if (linkUsable) {
          long linkCosts = router.getCostsByPointRef(vehicle,
                                                     srcPosition.getReference(),
                                                     curLink.getPoint());
          bestLinkCosts = Math.min(linkCosts, bestLinkCosts);
        }
      }
    }
    return bestLinkCosts;
  }

  /**
   * Gathers a set of all points from all given blocks that the given point is a
   * member of.
   *
   * @param point The point to check.
   * @param blocks The blocks to scan for the point.
   * @return A set of all points from all given blocks that the given point is a
   * member of.
   */
  @SuppressWarnings("unchecked")
  private Set<Point> getBlockedPoints(Point point, Set<Block> blocks) {
    assert point != null;
    assert blocks != null;
    Set<Point> result = new HashSet<>();

    // The point itself is always required.
    result.add(point);

    // Check for every block if the given point is part of it.
    for (Block curBlock : blocks) {
      if (curBlock.containsMember(point.getReference())) {
        // Check for every member of the block if it's a point. If it is, add it
        // to the resulting set.
        for (TCSObjectReference<?> memberRef : curBlock.getMembers()) {
          if (Point.class.equals(memberRef.getReferentClass())) {
            result.add(kernel.getTCSObject(Point.class,
                                           (TCSObjectReference<Point>) memberRef));
          }
        }
      }
    }
    return result;
  }

  /**
   * Returns a set of points from which the given location can be accessed.
   *
   * @param location The location.
   * @return A set of points from which the given location can be accessed.
   */
  private Set<Point> getAccessPoints(Location location) {
    assert location != null;
    Set<Point> result = new HashSet<>();

    for (Location.Link curLink : location.getAttachedLinks()) {
      result.add(kernel.getTCSObject(Point.class, curLink.getPoint()));
    }

    return result;
  }
}
