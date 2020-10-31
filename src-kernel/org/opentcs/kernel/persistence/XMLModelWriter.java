/*
 * openTCS copyright information:
 * Copyright (c) 2006 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.persistence;

import java.io.IOException;
import java.io.OutputStream;
import org.opentcs.kernel.workingset.Model;

/**
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
interface XMLModelWriter {
  /**
   * Returns the version an implementation supports as a String.
   *
   * @return The version an implementation supports as a String.
   */
  String getVersionString();
  
  /**
   * Writes a model to an output stream as XML.
   *
   * @param model The model to be written.
   * @param outStream The output stream the model is to be written to.
   * @throws IOException If there was a problem writing the model.
   */
  void writeXMLModel(Model model, OutputStream outStream)
  throws IOException;
}
