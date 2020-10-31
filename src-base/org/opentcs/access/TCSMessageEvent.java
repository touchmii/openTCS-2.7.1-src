/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access;

import java.io.Serializable;
import java.util.Objects;
import org.opentcs.data.message.Message;
import org.opentcs.util.eventsystem.TCSEvent;

/**
 * Instances of this class represent events emitted by/for messages being
 * published.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public final class TCSMessageEvent
    extends TCSEvent
    implements Serializable {

  /**
   * The published message.
   */
  private final Message message;

  /**
   * Creates a new instance.
   *
   * @param message The message being published.
   */
  public TCSMessageEvent(Message message) {
    this.message = Objects.requireNonNull(message, "message is null");
  }

  /**
   * Returns the message being published.
   *
   * @return The message being published.
   */
  public Message getMessage() {
    return message;
  }

  @Override
  public String toString() {
    return "TCSMessageEvent(" + message + ")";
  }
}
