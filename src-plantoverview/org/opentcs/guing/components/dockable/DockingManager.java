package org.opentcs.guing.components.dockable;

import bibliothek.gui.dock.common.CContentArea;
import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CGrid;
import bibliothek.gui.dock.common.CLocation;
import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.SingleCDockable;
import bibliothek.gui.dock.common.event.CVetoClosingEvent;
import bibliothek.gui.dock.common.event.CVetoClosingListener;
import bibliothek.gui.dock.common.group.CGroupBehavior;
import bibliothek.gui.dock.common.mode.ExtendedMode;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JFrame;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * Utility class for working with dockables.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 */
public class DockingManager {

  /**
   * ID for the tab pane, that contains the course, transport orders and
   * order sequences.
   */
  public static final String COURSE_TAB_PANE_ID = "course_tab_pane";
  /**
   * ID for the tab pane, that contains the components, blocks and groups.
   */
  public static final String TREE_TAB_PANE_ID = "tree_tab_pane";
  /**
   * ID for the dockable, that contains the VehiclePanel.
   */
  public static final String VEHICLES_DOCKABLE_ID = "vehicles_dock";
  /**
   * PropertyChangeEvent when a floating dockable closes.
   */
  public static final String DOCKABLE_CLOSED = "DOCKABLE_CLOSED";
  /**
   * Tab pane that contains the components, blocks and groups.
   */
  private CStack treeTabPane;
  /**
   * Tab pane that contains the course, transport orders and
   * order sequences.
   */
  private CStack courseTabPane;
  /**
   * Map that contains all tab panes. They are stored by their id.
   */
  private final Map<String, CStack> tabPanes = new HashMap<>();
  /**
   * Control for the dockable panels.
   */
  private CControl control;
  /**
   * The listeners for closing events.
   */
  private final List<PropertyChangeListener> listeners = new ArrayList<>();
  /**
   * File for saving the docking configuration.
   */
  private static final String DOCKING_CONFIG
      = System.getProperty("opentcs.dockinglayout");

  /**
   * Creates a new instance.
   */
  public DockingManager() {

  }

  /**
   * Adds a PropertyChangeListener.
   *
   * @param listener The new listener.
   */
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    if (!listeners.contains(listener)) {
      listeners.add(listener);
    }
  }

  /**
   * Creates a new dockable.
   *
   * @param id The unique id for this dockable.
   * @param title The title text of this new dockable.
   * @param comp The JComponent wrapped by the new dockable.
   * @param closeable If the dockable can be closeable or not.
   * @return The newly created dockable.
   */
  public DefaultSingleCDockable createDockable(String id,
                                               String title,
                                               JComponent comp,
                                               boolean closeable) {
    Objects.requireNonNull(id, "id is null");
    Objects.requireNonNull(title, "title is null");
    Objects.requireNonNull(comp, "comp is null");
    if (control == null) {
      return null;
    }
    DefaultSingleCDockable dockable = new DefaultSingleCDockable(id, title);
    dockable.setCloseable(closeable);
    dockable.add(comp);
    return dockable;
  }

  /**
   * Creates a new floating dockable.
   *
   * @param id The unique id for this dockable.
   * @param title The title text of this new dockable.
   * @param comp The JComponent wrapped by the new dockable.
   * @return The newly created dockable.
   */
  public DefaultSingleCDockable createFloatingDockable(String id,
                                                       String title,
                                                       JComponent comp) {
    if (control == null) {
      return null;
    }
    final DefaultSingleCDockable dockable = new DefaultSingleCDockable(id, title);
    dockable.setCloseable(true);
    dockable.setFocusComponent(comp);
    dockable.add(comp);
    dockable.addVetoClosingListener(new CVetoClosingListener() {

      @Override
      public void closing(CVetoClosingEvent event) {
      }

      @Override
      public void closed(CVetoClosingEvent event) {
        fireFloatingDockableClosed(dockable);
      }
    });
    control.addDockable(dockable);
    dockable.setExtendedMode(ExtendedMode.EXTERNALIZED);
    Rectangle centerRectangle = control.getContentArea().getCenter().getBounds();
    dockable.setLocation(CLocation.external((centerRectangle.width - comp.getWidth()) / 2,
                                            (centerRectangle.height - comp.getHeight()) / 2,
                                            comp.getWidth(),
                                            comp.getHeight()));
    return dockable;
  }

  /**
   * Loads the last layout (position of undocked dockables etc).
   */
  public void loadLayout() {
    // not working currently @ 24.01.14
//    try {
//      File file = new File(DOCKING_CONFIG);
//      if (file.exists()) {
//        control.readXML(file);
//      }
//    }
//    catch (IOException ex) {
//      Logger.getLogger(DockingManager.class.getName()).log(Level.WARNING, null, ex);
//    }
  }

  /**
   * Saves the current layout (position of undocked dockables etc).
   */
  public void saveLayout() {
    try {
      control.writeXML(new File(DOCKING_CONFIG));
    }
    catch (IOException ex) {
      Logger.getLogger(DockingManager.class.getName()).log(Level.WARNING, null, ex);
    }
  }

  /**
   * Adds a dockable as tab to the tab pane identified by the given id.
   *
   * @param newTab The new dockable that shall be added.
   * @param id The ID of the tab pane.
   * @param index Index where to insert the dockable in the tab pane.
   */
  public void addTabTo(DefaultSingleCDockable newTab, String id, int index) {
    Objects.requireNonNull(newTab, "newTab is null.");
    Objects.requireNonNull(id, "id is null");
    CStack tabPane = tabPanes.get(id);
    if (tabPane != null) {
      control.addDockable(newTab);
      newTab.setWorkingArea(tabPane);
      tabPane.getStation().add(newTab.intern(), index);
      tabPane.getStation().setFrontDockable(newTab.intern());
    }
  }

  /**
   * Removes a dockable from the CControl.
   *
   * @param dockable The dockable that shall be removed.
   */
  public void removeDockable(SingleCDockable dockable) {
    Objects.requireNonNull(dockable, "dockable is null");
    if (control != null) {
      control.removeDockable(dockable);
    }
  }

  /**
   * Returns the CControl.
   *
   * @return The CControl.
   */
  public CControl getCControl() {
    return control;
  }

  /**
   * Returns the whole component with all dockables, tab panes etc.
   *
   * @return The CContentArea of the CControl.
   */
  public CContentArea getContentArea() {
    if (control != null) {
      return control.getContentArea();
    }
    else {
      return null;
    }
  }

  /**
   * Returns the tab pane with the given id.
   *
   * @param id ID of the tab pane.
   * @return The tab pane or null if there is no tab pane with this id.
   */
  public CStack getTabPane(String id) {
    if (control != null) {
      return tabPanes.get(id);
    }
    else {
      return null;
    }
  }

  /**
   * Wraps all given JComponents into a dockable and deploys them on the CControl.
   *
   * @param frame
   * @param vehiclesPanel
   * @param fTreeView
   * @param fBlocksView
   * @param fGroupsView
   * @param fPropertiesComponent
   * @param statusScrollPane
   */
  public void initializeDockables(JFrame frame,
                                  JComponent vehiclesPanel,
                                  JComponent fTreeView,
                                  JComponent fBlocksView,
                                  JComponent fGroupsView,
                                  JComponent fPropertiesComponent,
                                  JComponent statusScrollPane) {
    Objects.requireNonNull(frame, "frame is null");
    Objects.requireNonNull(vehiclesPanel, "vehiclesPane is null");
    Objects.requireNonNull(fTreeView, "fTreeView is null");
    Objects.requireNonNull(fBlocksView, "fBlocksView is null");
    Objects.requireNonNull(fGroupsView, "fGroupsView is null");
    Objects.requireNonNull(fPropertiesComponent, "fPropertiesComponent is null");
    Objects.requireNonNull(statusScrollPane, "statusScrollPane is null");

    control = new CControl(frame);
    control.setGroupBehavior(CGroupBehavior.TOPMOST);

    // Disable keyboard shortcuts to avoid collisions.
    control.putProperty(CControl.KEY_GOTO_NORMALIZED, null);
    control.putProperty(CControl.KEY_GOTO_EXTERNALIZED, null);
    control.putProperty(CControl.KEY_GOTO_MAXIMIZED, null);
    control.putProperty(CControl.KEY_MAXIMIZE_CHANGE, null);

    ResourceBundleUtil bundle = ResourceBundleUtil.getBundle();
    CGrid grid = new CGrid(control);
    courseTabPane = new CStack(COURSE_TAB_PANE_ID);
    tabPanes.put(COURSE_TAB_PANE_ID, courseTabPane);
    DefaultSingleCDockable vehiclesDockable
        = createDockable(VEHICLES_DOCKABLE_ID,
                         bundle.getString("dockable.vehicles"),
                         vehiclesPanel,
                         false);
    treeTabPane = new CStack(TREE_TAB_PANE_ID);
    tabPanes.put(TREE_TAB_PANE_ID, treeTabPane);
    DefaultSingleCDockable treeViewDock
        = createDockable("treeView",
                         bundle.getString("dockable.treeView"),
                         fTreeView,
                         false);
    DefaultSingleCDockable treeBlocks
        = createDockable("blocksView",
                         bundle.getString("tree.blocks.text"),
                         fBlocksView,
                         false);
    DefaultSingleCDockable treeGroups
        = createDockable("groupsView",
                         bundle.getString("tree.groups.text"),
                         fGroupsView,
                         false);
    grid.add(0, 0, 1, 150, treeTabPane);
    grid.add(0, 150, 1, 100, createDockable("properties",
                                            bundle.getString("dockable.properties"),
                                            fPropertiesComponent,
                                            false));
    grid.add(0, 250, 1, 50, createDockable("status",
                                           bundle.getString("dockable.status"),
                                           statusScrollPane,
                                           false));
    grid.add(1, 0, 3.5, 252, courseTabPane);
    grid.add(1, 230, 1, 48, vehiclesDockable);

    control.getContentArea().deploy(grid);

    // init tab panes
    addTabTo(treeViewDock, TREE_TAB_PANE_ID, 0);
    addTabTo(treeBlocks, TREE_TAB_PANE_ID, 1);
    addTabTo(treeGroups, TREE_TAB_PANE_ID, 2);
    treeTabPane.getStation().setFrontDockable(treeViewDock.intern());
  }

  /**
   * Hides a dockable (by actually removing it from its station).
   *
   * @param station The CStackDockStation the dockable belongs to.
   * @param dockable The dockable to hide.
   */
  public void hideDockable(CStackDockStation station, DefaultSingleCDockable dockable) {
    int index = station.indexOf(dockable.intern());

    if (index <= -1) {
      station.add(dockable.intern(), station.getDockableCount());
      index = station.indexOf(dockable.intern());
    }
    station.remove(index);
  }

  /**
   * Shows a dockable (by actually adding it to its station).
   *
   * @param station The CStackDockStation the dockable belongs to.
   * @param dockable The dockable to show.
   * @param index Where to add the dockable.
   */
  public void showDockable(CStackDockStation station,
                           DefaultSingleCDockable dockable,
                           int index) {
    if (station.indexOf(dockable.intern()) <= -1) {
      station.add(dockable.intern(), index);
    }
  }

  /**
   * Sets the visibility status of a dockable with the given id.
   *
   * @param id The id of the dockable.
   * @param visible If it shall be visible or not.
   */
  public void setDockableVisibility(String id, boolean visible) {
    if (control != null) {
      SingleCDockable dockable = control.getSingleDockable(id);
      if (dockable != null) {
        dockable.setVisible(visible);
      }
    }
  }

  /**
   * Checks if the given dockable is docked to its CStackDockStation.
   *
   * @param station The station the dockable should be docked in.
   * @param dockable The dockable to check.
   * @return True if it is docked, false otherwise.
   */
  public boolean isDockableDocked(CStackDockStation station, DefaultSingleCDockable dockable) {
    return station.indexOf(dockable.intern()) <= -1;
  }

  /**
   * Fires a <code>PropertyChangeEvent</code> when a floatable dockable is closed
   * (eg a plugin panel).
   *
   * @param dockable The dockable that was closed.
   */
  private void fireFloatingDockableClosed(DefaultSingleCDockable dockable) {
    for (PropertyChangeListener listener : listeners) {
      listener.propertyChange(
          new PropertyChangeEvent(this, DOCKABLE_CLOSED, dockable, dockable));
    }
  }
}
