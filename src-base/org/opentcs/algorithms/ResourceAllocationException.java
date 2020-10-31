/*
 * openTCS copyright information:
 * Copyright (c) 2008 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.algorithms;

import org.opentcs.access.KernelException;

/**
 * Thrown when allocating resources for a {@link ResourceUser ResourceUser} is
 * impossible.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public final class ResourceAllocationException
    extends KernelException {

  /**
   * Creates a new ResourceAllocationException with the given detail message.
   *
   * @param message The detail message.
   */
  public ResourceAllocationException(String message) {
    super(message);
  }

  /**
   * Creates a new ResourceAllocationException with the given detail message and
   * cause.
   *
   * @param message The detail message.
   * @param cause The cause.
   */
  public ResourceAllocationException(String message, Throwable cause) {
    super(message, cause);
  }
}
