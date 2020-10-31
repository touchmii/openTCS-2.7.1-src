/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.drivers.messages;

import java.io.Serializable;

/**
 * A message that informs a communication adapter that it/the vehicle should
 * reset currently active errors if possible.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public final class ClearError
    implements Serializable {

  /**
   * Creates a new instance.
   */
  public ClearError() {
    // Do nada.
  }
}
