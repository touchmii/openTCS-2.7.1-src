/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.configuration;

/**
 * Item Constraint for Boolean type value.
 *
 * @author Preity Gupta (Fraunhofer IML)
 */
public class ItemConstraintBoolean
    extends ItemConstraint {

  /**
   * Creates a constraint of type Boolean .
   * 
   */
  public ItemConstraintBoolean() {
    super(ConfigurationDataType.BOOLEAN,0,0,null);
  }

  @Override
  public boolean accepts(String value) {
    return true;

  }
}
