/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.module.routing;

import java.util.List;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.Route;

/**
 * Implementations of this interface compute costs for routes based on their
 * respective implementation criteria.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
abstract class RouteEvaluator {

  /**
   * Computes the costs for the given vehicle travelling from the given starting
   * position via the given list of route steps.
   *
   * @param vehicle The vehicle.
   * @param startPoint The starting position.
   * @param steps The list of steps to travel.
   * @return The costs for the given vehicle travelling from the given starting
   * position via the given list of route steps.
   */
  abstract long computeCosts(Vehicle vehicle, Point startPoint, List<Route.Step> steps);
}
