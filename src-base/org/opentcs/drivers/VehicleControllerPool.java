/*
 * openTCS copyright information:
 * Copyright (c) 2008 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.drivers;

/**
 * Maintains associations between vehicles and vehicle controllers.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface VehicleControllerPool {

  /**
   * Returns the vehicle controller associated with the vehicle with the given
   * name, or <code>null</code>, if no vehicle controller is associated with it
   * or if there is no vehicle with the given name.
   *
   * @param vehicleName The name of the vehicle for which to return the vehicle
   * controller.
   * @return the vehicle controller associated with the vehicle with the given
   * name, or <code>null</code>, if no vehicle controller is associated with it
   * or if there is no vehicle with the given name.
   */
  VehicleController getVehicleController(String vehicleName);

  /**
   * Returns the current time factor for simulation.
   *
   * @return The current time factor for simulation.
   */
  double getSimulationTimeFactor();

  /**
   * Sets a time factor for simulation.
   *
   * @param factor The time factor.
   * @throws IllegalArgumentException If the given value is 0.0 or smaller.
   */
  void setSimulationTimeFactor(double factor)
      throws IllegalArgumentException;

  /**
   * Unregisters, unbinds and terminates this pool instance.
   */
  void terminate();
}
