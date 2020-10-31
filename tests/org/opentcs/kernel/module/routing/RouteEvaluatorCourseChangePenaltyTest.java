package org.opentcs.kernel.module.routing;

import java.util.LinkedList;
import java.util.List;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import static org.opentcs.data.ObjectPropConstants.PATH_TRAVEL_ORIENTATION;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.Route;

/**
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class RouteEvaluatorCourseChangePenaltyTest {

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  @Test
  public void shouldSumOnlyPathLengths() {
    RouteEvaluator evaluator = new RouteEvaluatorCourseChangePenalty();

    final String SAME_ORIENTATION = "same-orientation";

    Point startPoint = new Point(1, "startPoint");
    List<Route.Step> steps = new LinkedList<>();

    Point hop1 = new Point(2, "hop1");
    Path path1 = new Path(10, "path1", startPoint.getReference(), hop1.getReference());
    path1.setLength(34);
    path1.setProperty(PATH_TRAVEL_ORIENTATION, SAME_ORIENTATION);
    steps.add(new Route.Step(path1, hop1, Vehicle.Orientation.FORWARD, 0));

    Point hop2 = new Point(3, "hop2");
    Path path2 = new Path(11, "path2", hop1.getReference(), hop2.getReference());
    path2.setLength(77);
    path2.setProperty(PATH_TRAVEL_ORIENTATION, SAME_ORIENTATION);
    steps.add(new Route.Step(path2, hop2, Vehicle.Orientation.FORWARD, 0));
    
    Vehicle vehicle = new Vehicle(21, "dummyvehicle");
    long computedCosts = evaluator.computeCosts(vehicle, startPoint, steps);

    Assert.assertEquals(path1.getLength() + path2.getLength(), computedCosts);
  }

  @Test
  public void shouldAddPanelty() {
    final long PENALTY = 7000;
    RouteEvaluator evaluator = new RouteEvaluatorCourseChangePenalty(PENALTY);

    Point startPoint = new Point(1, "startPoint");
    List<Route.Step> steps = new LinkedList<>();

    Point hop1 = new Point(2, "hop1");
    Path path1 = new Path(10, "path1", startPoint.getReference(), hop1.getReference());
    path1.setLength(34);
    path1.setProperty(PATH_TRAVEL_ORIENTATION, "any orientation");
    steps.add(new Route.Step(path1, hop1, Vehicle.Orientation.FORWARD, 0));

    Point hop2 = new Point(3, "hop2");
    Path path2 = new Path(11, "path2", hop1.getReference(), hop2.getReference());
    path2.setLength(77);
    path2.setProperty(PATH_TRAVEL_ORIENTATION, "another orientation");
    steps.add(new Route.Step(path2, hop2, Vehicle.Orientation.FORWARD, 0));

    Vehicle vehicle = new Vehicle(21, "dummyvehicle");
    long computedCosts = evaluator.computeCosts(vehicle, startPoint, steps);

    Assert.assertEquals(path1.getLength() + path2.getLength() + PENALTY,
                        computedCosts);
  }
}
