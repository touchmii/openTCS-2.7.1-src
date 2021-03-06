/*
 * openTCS copyright information:
 * Copyright (c) 2008 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.xmlorders;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A set of orders to be processed by the kernel.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
@XmlRootElement
public class TCSOrderSet {
  /**
   * The orders to be processed.
   */
  private List<TCSOrder> orders = new LinkedList<>();

  /**
   * Creates a new instance.
   */
  public TCSOrderSet() {
    // Do nada.
  }

  /**
   * Returns the orders to be processed.
   *
   * @return The orders to be processed.
   */
  @XmlElement(name="order", required=true)
  public List<TCSOrder> getOrders() {
    return orders;
  }

  /**
   * Sets the orders to be processed.
   *
   * @param orders The orders to be processed.
   */
  public void setOrders(List<TCSOrder> orders) {
    if (orders == null) {
      throw new NullPointerException("orders is null");
    }
    this.orders = orders;
  }
  
  /**
   * Marshals this instance to its XML representation and returns that in a
   * string.
   *
   * @return A <code>String</code> containing the XML representation of this
   * instance.
   */
  public String toXml() {
    StringWriter stringWriter = new StringWriter();
    try {
      // Als XML in eine Datei schreiben.
      JAXBContext jc = JAXBContext.newInstance(TCSOrderSet.class,
                                               Transport.class,
                                               TransportScript.class);
      Marshaller marshaller = jc.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      marshaller.marshal(this, stringWriter);
    }
    catch (JAXBException exc) {
      throw new IllegalStateException("Exception marshalling data", exc);
    }
    return stringWriter.toString();
  }
  
  /**
   * Unmarshals an instance of this class from the given XML representation.
   *
   * @param xmlData A <code>String</code> containing the XML representation.
   * @return The status instance unmarshalled from the given String.
   */
  public static TCSOrderSet fromXml(String xmlData) {
    if (xmlData == null) {
      throw new NullPointerException("xmlData is null");
    }
    StringReader stringReader = new StringReader(xmlData);
    try {
      JAXBContext jc = JAXBContext.newInstance(TCSOrderSet.class,
                                               Transport.class,
                                               TransportScript.class);
      Unmarshaller unmarshaller = jc.createUnmarshaller();
      Object o = unmarshaller.unmarshal(stringReader);
      return (TCSOrderSet) o;
    }
    catch (JAXBException exc) {
      throw new IllegalStateException("Exception unmarshalling data", exc);
    }
  }
}
