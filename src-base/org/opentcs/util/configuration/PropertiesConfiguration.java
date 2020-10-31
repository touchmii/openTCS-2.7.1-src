/*
 * openTCS copyright information:
 * Copyright (c) 2006 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class realizes configuration management via Java Properties.
 * <p>
 * Note that this realization does not support persisting of configuration item
 * descriptions.
 * </p>
 * <hr>
 * <h4>System properties</h4>
 * <dl>
 * <dt><b>
 * de.fraunhofer.iml.toolbox.configuration.properties.file:
 * </b></dt>
 * <dd>May be set to the name of the configuration file. The default value is
 * {@value #propsFileDefault}.</dd>
 * </dl>
 * <hr>
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class PropertiesConfiguration
    extends Configuration {

  /**
   * The default properties file name.
   */
  public static final String propsFileDefault = "configuration.properties";
  /**
   * This class's Logger instance.
   */
  private static final Logger log =
      Logger.getLogger(PropertiesConfiguration.class.getName());
  /**
   * The name of the property which may contain the name of the desired
   * properties file.
   */
  private static final String propsFileProperty =
      "org.opentcs.util.configuration.properties.file";
  /**
   * The properties file.
   */
  private static final File propsFile;

  /* Initialize static components. */
  static {
    propsFile =
        new File(System.getProperty(propsFileProperty, propsFileDefault));
  }

  /**
   * Creates a new PropertiesConfiguration.
   */
  public PropertiesConfiguration() {
    super();
    log.fine("method entry");
    // Try to read the properties from the file.
    Properties allProperties = new Properties();
    try {
      InputStream inStream = new FileInputStream(propsFile);
      allProperties.load(inStream);
      inStream.close();
    }
    catch (FileNotFoundException exc) {
      log.info("Properties file " + propsFile.getPath()
          + " not found, using empty configuration.");
    }
    catch (IOException exc) {
      log.info("Unable to read from properties file " + propsFile.getPath()
          + ", reverting to empty configuration.");
      allProperties.clear();
    }
    // Split the read properties by their namespaces.
    Enumeration<?> propertyNames = allProperties.propertyNames();
    while (propertyNames.hasMoreElements()) {
      String currentName = propertyNames.nextElement().toString();
      String currentValue = allProperties.getProperty(currentName);
      int splitIndex = currentName.lastIndexOf('.');
      // If there isn't any '.' in the key or the key starts or ends with a '.',
      // dismiss this property as malformed.
      if ((splitIndex < 0) || (splitIndex >= currentName.length())
          || currentName.charAt(0) == '.') {
        log.info("Dismissing property because of malformed key name: "
            + currentName);
      }
      else {
        String namespace = currentName.substring(0, splitIndex);
        String key =
            currentName.substring(splitIndex + 1, currentName.length());
        ConfigurationStore store = getStore(namespace);
        store.setString(key, currentValue);
      }
    }
  }

  @Override
  synchronized void persist() {
    log.fine("method entry");
    Properties allProperties = new SortedProperties();
    for (ConfigurationStore curStore : stores.values()) {
      String namespace = curStore.getNamespace() + ".";
      for (ConfigurationItem configItem : curStore.getConfigurationItems().values()) {
        allProperties.setProperty(namespace + configItem.getKey(),
                                  configItem.getValue());
      }
    }
    try {
      // Write properties to a temporary file first. If successful, rename it
      // to the actual properties file.
      File tempFile =
          File.createTempFile("props", ".tmp", propsFile.getParentFile());
      OutputStream outStream = new FileOutputStream(tempFile);
      allProperties.store(outStream, null);
      outStream.close();
      boolean deleted = propsFile.delete();
      if (!deleted) {
        log.warning("Could not delete original properties file "
            + propsFile.getAbsolutePath());
      }
      boolean renamed = tempFile.renameTo(propsFile);
      if (!renamed) {
        log.warning("Could not rename temporary properties file "
            + tempFile.getAbsolutePath() + " to " + propsFile.getAbsolutePath());
      }
    }
    catch (IOException exc) {
      log.log(Level.WARNING,
              "IOException writing properties to " + propsFile.getPath(), exc);
    }
  }
}
