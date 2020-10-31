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
import static java.util.Objects.requireNonNull;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.Route;

/**
 * Computes costs for routes based on the sum of the lengths of its paths.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
class RouteEvaluatorDistance
    extends RouteEvaluator {

  public RouteEvaluatorDistance() {
    super();
  }

  @Override
  long computeCosts(Vehicle vehicle, Point startPoint, List<Route.Step> steps) {
    requireNonNull(startPoint, "startPoint");
    requireNonNull(steps, "steps");

    long result = 0;
    for (Route.Step step : steps) {
      result += step.getPath().getLength();
    }
    return result;
  }
}
