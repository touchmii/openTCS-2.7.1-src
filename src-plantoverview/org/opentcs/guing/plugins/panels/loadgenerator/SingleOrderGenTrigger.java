/*
 * openTCS copyright information:
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.plugins.panels.loadgenerator;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.opentcs.access.KernelRuntimeException;

/**
 * Triggers creation of transport orders only once.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 */
class SingleOrderGenTrigger
    implements OrderGenerationTrigger {

  /**
   * This class's Logger.
   */
  private static final Logger log
      = Logger.getLogger(SingleOrderGenTrigger.class.getName());
  /**
   * The instance actually creating the new orders.
   */
  private final OrderBatchCreator orderBatchCreator;

  /**
   * Creates a new SingleOrderGenTrigger.
   *
   * @param orderBatchCreator The order batch creator
   */
  public SingleOrderGenTrigger(final OrderBatchCreator orderBatchCreator) {
    this.orderBatchCreator = Objects.requireNonNull(orderBatchCreator,
                                                    "orderBatchCreator is null");
  }

  @Override
  public void setTriggeringEnabled(boolean enabled) {
    if (enabled) {
      triggerOrderGeneration();
    }
  }

  @Override
  public void triggerOrderGeneration()
      throws KernelRuntimeException {
    try {
      if (orderBatchCreator != null) {
        orderBatchCreator.createOrderBatch();
      }
    }
    catch (KernelRuntimeException exc) {
      log.log(Level.WARNING,
              "Exception triggering order generation, terminating triggering",
              exc);
    }
  }
}
