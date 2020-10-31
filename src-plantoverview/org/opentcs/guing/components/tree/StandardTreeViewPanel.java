/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.components.tree;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import org.jhotdraw.draw.Figure;
import org.opentcs.guing.application.OpenTCSView;
import org.opentcs.guing.application.action.edit.DeleteAction;
import org.opentcs.guing.application.action.edit.UndoRedoManager;
import org.opentcs.guing.components.EditableComponent;
import org.opentcs.guing.components.tree.elements.AbstractUserObject;
import org.opentcs.guing.components.tree.elements.BlockUserObject;
import org.opentcs.guing.components.tree.elements.FigureUserObject;
import org.opentcs.guing.components.tree.elements.LayoutUserObject;
import org.opentcs.guing.components.tree.elements.LocationUserObject;
import org.opentcs.guing.components.tree.elements.PathUserObject;
import org.opentcs.guing.components.tree.elements.PointUserObject;
import org.opentcs.guing.components.tree.elements.SimpleFolderUserObject;
import org.opentcs.guing.components.tree.elements.UserObject;
import org.opentcs.guing.model.AbstractFigureComponent;
import org.opentcs.guing.model.FigureComponent;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.SimpleFolder;
import org.opentcs.guing.model.elements.GroupModel;
import org.opentcs.guing.model.elements.LayoutModel;
import org.opentcs.guing.model.elements.StaticRouteModel;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * Standardimplementierung einer Baumansicht zur Darstellung der Modellelemente
 * in einem einzigen Baum.
 * <p>
 * <b>Entwurfsmuster:</b> Befehl.
 * StandardTreeViewPanel ist der Ausl�ser f�r die Ausf�hrung eines konkreten
 * UserObjects, welches ja einen Befehl darstellt.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @see UserObject
 */
public class StandardTreeViewPanel
    extends JPanel
    implements TreeView, EditableComponent {

  /**
   * Key for indicating this panel displays the group view.
   */
  public static final String GROUP_VIEW = "groups";
  /**
   * Der Wurzelknoten.
   */
  protected DefaultMutableTreeNode fRootNode;
  /**
   * Das Modell hinter dem JTree.
   */
  protected TreeModel fTreeModel;
  /**
   * Flag indicating if this panel displays the group view.
   */
  protected boolean isGroupView = false;
  /**
   *
   */
  private final UndoRedoManager fUndoRedoManager;
  /**
   *
   */
  private List<UserObject> bufferedUserObjects;
  /**
   *
   */
  private final List<Figure> bufferedFigures = new ArrayList<>();

  /**
   *
   * @param undoRedoManager
   */
  public StandardTreeViewPanel(UndoRedoManager undoRedoManager) {
    this(undoRedoManager, null);
  }

  /**
   * Creates new form StandardTreeView
   *
   * @param undoRedoManager
   * @param id
   */
  public StandardTreeViewPanel(UndoRedoManager undoRedoManager, String id) {
    initComponents();
    objectTree.setCellRenderer(new TreeViewCellRenderer());
    objectTree.addMouseListener(createMouseListener());
    // Remove JTree's standard keyboard actions to enable the actions defined
    // in ActionManager
    ActionMap treeActionMap = objectTree.getActionMap();
    treeActionMap.getParent().remove("cut");  // <Ctrl> + X
    treeActionMap.getParent().remove("copy");  // <Ctrl> + C
    treeActionMap.getParent().remove("paste");  // <Ctrl> + V
    treeActionMap.getParent().remove("duplicate");  // <Ctrl> + D
    treeActionMap.getParent().remove("selectAll");  // <Ctrl> + A
    // Add a keyboard handler for the "Delete" action
    InputMap inputMap = objectTree.getInputMap();
    inputMap.getParent().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), DeleteAction.ID);
    inputMap.getParent().put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), DeleteAction.ID);
    treeActionMap.getParent().put(DeleteAction.ID, new DeleteAction());

    if (id != null && id.equals(GROUP_VIEW)) {
      isGroupView = true;
    }

    this.fUndoRedoManager = undoRedoManager;
    this.bufferedUserObjects = new ArrayList<>();
  }

  /**
   * Liefert den Baum als Swing-Komponte.
   *
   * @return den Baum
   */
  public JTree getTree() {
    return objectTree;
  }

  @Override // EditableComponent
  public void cutSelectedItems() {
    bufferSelectedItems(true);
  }

  @Override // EditableComponent
  public void copySelectedItems() {
    bufferSelectedItems(false);
  }

  @Override // EditableComponent
  public void pasteBufferedItems() {
    restoreItems(bufferedUserObjects, bufferedFigures);
    // Auch das "Paste" per Undo r�ckg�ngig machen
    fUndoRedoManager.addEdit(new PasteEdit(bufferedUserObjects, bufferedFigures));
  }

  @Override // EditableComponent
  public void duplicate() {
    bufferSelectedItems(false);
    restoreItems(bufferedUserObjects, bufferedFigures);
    fUndoRedoManager.addEdit(new PasteEdit(bufferedUserObjects, bufferedFigures));
  }

  @Override // EditableComponent
  public void delete() {
    bufferSelectedItems(true);

    if (bufferedUserObjects.isEmpty() && bufferedFigures.isEmpty()) {
      return; // nothing to undo/redo
    }

    fUndoRedoManager.addEdit(new DeleteEdit(bufferedUserObjects, bufferedFigures));
  }

  @Override // EditableComponent
  public void selectAll() {
    // Sample implementation (HH 2014-04-08): 
    // Select all components in the currently focused tree folder
    // TODO: select all components except folders
    UserObject selectedItem = getSelectedItem();

    if (selectedItem != null) {
      ModelComponent parent = selectedItem.getParent();

      if (parent != null) {
        selectItems(new HashSet<>(parent.getChildComponents()));
      }
    }
  }

  @Override // EditableComponent
  public void clearSelection() {
    // Not used for our tree:
    // JTree's default action for <Ctrl> + <Shift> + A already does the job.
  }

  @Override // EditableComponent
  public boolean isSelectionEmpty() {
    // Not used for tree ?
    return true;
  }

  /**
   * Copy the selected tree components and the associated figures in the drawing
   * to abuffer.
   *
   * @param doDelete true if the original object will be deleted after
   * copying it to the buffer
   */
  private void bufferSelectedItems(boolean doDelete) {
    if (objectTree.getSelectionPaths() != null) {
      bufferedUserObjects.clear();
      bufferedFigures.clear();

      for (TreePath treePath : objectTree.getSelectionPaths()) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) treePath.getLastPathComponent();
        UserObject userObject = (UserObject) node.getUserObject();
        boolean removed = false;
        // Let the user object decide how to remove the item from the tree.
        if (doDelete) {
          removed = userObject.removed();
        }

        if (removed || !doDelete) {
          bufferedUserObjects.add(userObject);
        }
        // Save deleted figures to allow undo-ing the delete operation
        if (doDelete) {
          ModelComponent modelComponent = userObject.getModelComponent();

          if (modelComponent instanceof AbstractFigureComponent) {
            Figure figure = ((AbstractFigureComponent) modelComponent).getFigure();
            bufferedFigures.add(figure);
          }
        }
      }
    }
  }

  public boolean hasBufferedObjects() {
    return !bufferedFigures.isEmpty() || !bufferedUserObjects.isEmpty();
  }

  private void restoreItems(List<UserObject> userObjects, List<Figure> figures) {
    // Restore deleted model components
    bufferedUserObjects = OpenTCSView.instance().restoreModelComponents(userObjects);
    // Restore the figures associated with these model components
    bufferedFigures.clear();

    for (Figure figure : figures) {
      OpenTCSView.instance().getDrawingView().getDrawing().add(figure);
      bufferedFigures.add(figure);
    }
  }

  /**
   * Sortiert die Kindelemente eines Knotens.
   *
   * @param treeNode Der Knoten, dessen Kindelemente sortiert werden sollen.
   */
  public synchronized void sortItems(TreeNode treeNode) {
    if (!(treeNode instanceof SortableTreeNode)) {
      // Der Parent des "Layout"-Folders ist nicht sortierbar
      for (int i = 0; i < treeNode.getChildCount(); i++) {
        DefaultMutableTreeNode child = (DefaultMutableTreeNode) treeNode.getChildAt(i);

        if (child.getUserObject() instanceof LayoutUserObject) {
          // Den Namen aktualisieren
          treeModel().reload(child);
          return;
        }
      }

      return;
    }

    SortableTreeNode sortable = (SortableTreeNode) treeNode;
    // Alle Kinder rekursiv sortieren
    Enumeration<TreeNode> en = sortable.children();

    while (en.hasMoreElements()) {
      TreeNode node = en.nextElement();

      if (node.getChildCount() > 0) {
        sortItems(node);
      }
    }

    if (sortable.isSorting()) {
      if (sortable.getChildCount() > 0) {
        sortable.sort(createSortComparator());
      }
    }

    int size = treeNode.getChildCount();
    boolean[] expanded = new boolean[size];

    for (int i = 0; i < size; i++) {
      DefaultMutableTreeNode child = (DefaultMutableTreeNode) treeNode.getChildAt(i);
      expanded[i] = objectTree.isExpanded(new TreePath(child.getPath()));
    }

    treeModel().reload(sortable);

    for (int i = 0; i < expanded.length; i++) {
      DefaultMutableTreeNode child = (DefaultMutableTreeNode) treeNode.getChildAt(i);
      objectTree.expandPath(new TreePath(child.getPath()));
    }
  }

  /**
   * Traversiert den gesamten Baum, um den Knoten zu finden, der das �bergebene
   * Datenobjekt in seinem UserObject h�lt.
   *
   * @param dataObject
   * @return
   */
  public DefaultMutableTreeNode findFirst(Object dataObject) {
    DefaultMutableTreeNode searchNode = null;
    Enumeration e = fRootNode.preorderEnumeration();
    // boolean to prevent selecting the model in an other folder than "Static routes"
    // when selecting in element in a static route
    boolean selectStaticRouteComponent = false;

    while (e.hasMoreElements()) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
      UserObject userObject = (UserObject) node.getUserObject();
      ModelComponent modelComponent = (ModelComponent) Objects.requireNonNull(dataObject);
      ModelComponent parentComponent = modelComponent.getParent();

      if (parentComponent instanceof StaticRouteModel) {
        selectStaticRouteComponent = true;
      }

      // Select point and path "directly", not the entries in a block area.
      if (dataObject != null && dataObject.equals(userObject.getModelComponent())) {
        // if we aren't in the static routes folder and looking for a 
        // static route model continue
        if (selectStaticRouteComponent) {
          if (node.getUserObject() instanceof SimpleFolder) {
            SimpleFolder sf = (SimpleFolder) node.getUserObject();

            if (!sf.getTreeViewName().equals(
                ResourceBundleUtil.getBundle().getString("tree.staticRoutes.text"))) {
              continue;
            }
          }
          else {
            continue;
          }
        }

        // pseifert @ 20.05.14: Is this still neccessary?
//        DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) node.getParent();
//        if (parentNode != null) {
//          UserObject parentUserObject = (UserObject) parentNode.getUserObject();
//
//          if (parentUserObject != null && parentUserObject instanceof BlockUserObject) {
//            continue;
//          }
//        }
        searchNode = node;
        break;
      }
    }

    return searchNode;
  }

  /**
   * Mauslistener f�r Links-, Rechts- und Doppelklicks.
   */
  private MouseListener createMouseListener() {
    return new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        int selRow = objectTree.getRowForLocation(e.getX(), e.getY());
        TreePath selPath = objectTree.getPathForLocation(e.getX(), e.getY());
        Set<UserObject> oldSelection = getSelectedItems();

        if (selRow != -1) {
          if (!e.isControlDown()) {
            objectTree.setSelectionPath(selPath);
          }

          UserObject userObject = getUserObject(selPath);

          if (userObject == null) {
            return;
          }

          if (e.getClickCount() == 1) {
            if (e.getModifiers() == MouseEvent.BUTTON3_MASK) {
              if (isGroupView) {
                // Tree "Groups"
                if (userObject instanceof PointUserObject
                    || userObject instanceof LocationUserObject
                    || userObject instanceof PathUserObject) {
                  if (e.isControlDown()) {
                    oldSelection.add(userObject);
                    Set<ModelComponent> dataObjects = new HashSet<>();

                    for (UserObject userObj : oldSelection) {
                      dataObjects.add(userObj.getModelComponent());
                    }

                    selectItems(dataObjects);
                  }

                  FigureUserObject pub = (FigureUserObject) userObject;
                  pub.rightClicked(objectTree, e.getX(), e.getY(), isGroupView);

                  return;
                }
                else {
                  showPopupMenuGroup(userObject.getModelComponent(), e.getX(), e.getY());
                }
              }
              else if (userObject instanceof BlockUserObject
                  || userObject instanceof PointUserObject
                  || userObject instanceof LocationUserObject
                  || userObject instanceof PathUserObject) {
                // Tree "Components" / "Blocks"
                oldSelection.add(userObject);
                Set<ModelComponent> dataObjects = new HashSet<>();

                for (UserObject userObj : oldSelection) {
                  dataObjects.add(userObj.getModelComponent());
                }

                selectItems(dataObjects);
              }

              userObject.rightClicked(objectTree, e.getX(), e.getY());
            }
            else {
              // Einfach-Click mit linker Maus-Taste
              if (e.isControlDown()) {
                ((AbstractUserObject) userObject).selectMultipleObjects();
              }
              else {
                userObject.selected();
              }
            }
          }
          else if (e.getClickCount() == 2) {
            userObject.doubleClicked();
          }
        }
        else if (e.getModifiers() == MouseEvent.BUTTON3_MASK) {
          showPopupMenu(e.getX(), e.getY());
        }
      }
    };
  }

  /**
   * Returns the user object the user clicked on.
   *
   * @param e The mouse event.
   * @return The clicked user object or null, if none was found.
   */
  public UserObject getDraggedUserObject(MouseEvent e) {
    int selRow = objectTree.getRowForLocation(e.getX(), e.getY());

    if (selRow != -1) {
      TreePath selPath = objectTree.getPathForLocation(e.getX(), e.getY());

      return getUserObject(selPath);
    }

    return null;
  }

  /**
   * A special popup menu when clicking on a group.
   *
   * @param folder Folder containing the group.
   * @param x x position.
   * @param y y position.
   */
  private void showPopupMenuGroup(Object folder, int x, int y) {
    final GroupModel groupFolder = (GroupModel) folder;
    JPopupMenu menu = new JPopupMenu();
    ResourceBundleUtil labels = ResourceBundleUtil.getBundle();

    final OpenTCSView openTCSView = OpenTCSView.instance();

    final JCheckBoxMenuItem cbItemAll = new JCheckBoxMenuItem(
        labels.getString("tree.group.showInAll"), groupFolder.isGroupVisible());
    cbItemAll.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        openTCSView.setGroupVisibilityInAllDrawingViews(groupFolder,
                                                        cbItemAll.isSelected());
      }
    });
    menu.add(cbItemAll);

    for (final String title : openTCSView.getDrawingViewNames()) {
      final JCheckBoxMenuItem cbItem = new JCheckBoxMenuItem(
          labels.getFormatted("tree.group.show", title),
          groupFolder.isGroupInDrawingViewVisible(title));
      cbItem.addActionListener(new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
          openTCSView.setGroupVisibilityInDrawingView(title,
                                                      groupFolder,
                                                      cbItem.isSelected());
        }
      });
      menu.add(cbItem);
    }

    menu.addSeparator();

    JMenuItem item = new JMenuItem(labels.getString("tree.group.add"));
    item.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        OpenTCSView.instance().addSelectedItemsToGroup(groupFolder);
      }
    });
    menu.add(item);

    item = new JMenuItem(labels.getString("tree.group.delete"));
    item.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        OpenTCSView.instance().deleteGroup(groupFolder);
      }
    });

    menu.add(item);
    menu.show(objectTree, x, y);
  }

  /**
   * Shows a popup menu with options for the JTree.
   */
  private void showPopupMenu(int x, int y) {
    JPopupMenu menu = new JPopupMenu();
    ResourceBundleUtil labels = ResourceBundleUtil.getBundle();

    JMenuItem item = new JMenuItem();
    labels.configureMenu(item, "tree.expandAll");

    item.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent event) {
        for (int i = 0; i < objectTree.getRowCount(); i++) {
          objectTree.expandRow(i);
        }
      }
    });

    menu.add(item);

    item = new JMenuItem();
    labels.configureMenu(item, "tree.closeAll");

    item.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent event) {
        for (int i = 0; i < objectTree.getRowCount(); i++) {
          objectTree.collapseRow(i);
        }
      }
    });

    menu.add(item);

    menu.addSeparator();

    item = new JMenuItem();
    labels.configureMenu(item, "tree.sortAll");

    item.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent event) {
        Enumeration<TreeNode> eTreeNodes
            = ((TreeNode) objectTree.getModel().getRoot()).children();

        while (eTreeNodes.hasMoreElements()) {
          TreeNode node = eTreeNodes.nextElement();
          sortItems(node);
        }
      }
    });

    menu.add(item);

    menu.show(objectTree, x, y);
  }

  @Override
  public void sortRoot() {
    sortItems((TreeNode) objectTree.getModel().getRoot());
  }

  @Override
  public void sortChildren() {
    Enumeration<TreeNode> eTreeNodes
        = ((TreeNode) objectTree.getModel().getRoot()).children();

    while (eTreeNodes.hasMoreElements()) {
      TreeNode node = eTreeNodes.nextElement();
      sortItems(node);
    }
  }

  @Override // TreeView
  public void addItem(Object parent, UserObject item) {
    if (parent == null) {
      setRoot(item);
    }
    else {
      ResourceBundleUtil bundle = ResourceBundleUtil.getBundle();
      String treeViewName;
      boolean sorting = true;	// Die Kindelemente des Knotens sollen sortiert werden
      // Statische Routen sollen nicht sortiert werden
      if (parent instanceof SimpleFolder) {
        SimpleFolder folder = (SimpleFolder) parent;
        treeViewName = folder.getTreeViewName();

        if (treeViewName.equals(bundle.getString("tree.staticRoutes.text"))) {
          sorting = false;
        }
      }

      if (item instanceof LayoutUserObject) {
        sorting = false; // Kindelemente des Layouts nicht sortieren
      }

      SortableTreeNode treeItem = createTreeNode(item, sorting);
      DefaultMutableTreeNode parentItem = findFirst(parent);

      if (parent instanceof LayoutModel) {
        treeViewName = ((SimpleFolder) ((SimpleFolderUserObject) item).getModelComponent()).getTreeViewName();

        if (treeViewName.equals(bundle.getString("tree.points.text"))) {
          treeModel().insertNodeInto(treeItem, parentItem, 0);
        }
        else if (treeViewName.equals(bundle.getString("tree.paths.text"))) {
          insertElementAt(treeItem, parentItem, 1);
        }
        else if (treeViewName.equals(bundle.getString("tree.locations.text"))) {
          insertElementAt(treeItem, parentItem, 2);
        }
        else if (treeViewName.equals(bundle.getString("tree.locationTypes.text"))) {
          insertElementAt(treeItem, parentItem, 3);
        }
        else if (treeViewName.equals(bundle.getString("tree.links.text"))) {
          insertElementAt(treeItem, parentItem, 4);
        }
        else if (treeViewName.equals(bundle.getString("tree.blocks.text"))) {
          insertElementAt(treeItem, parentItem, 5);
        }
        else if (treeViewName.equals(bundle.getString("tree.staticRoutes.text"))) {
          insertElementAt(treeItem, parentItem, 6);
        }
        else if (treeViewName.equals(bundle.getString("tree.otherGraphicals.text"))) {
          insertElementAt(treeItem, parentItem, 7);
        }
      }
      else {
        if (parentItem == null) {
          return;
        }
        treeModel().insertNodeInto(treeItem, parentItem, parentItem.getChildCount());
        if (parent instanceof ModelComponent) {
          item.setParent((ModelComponent) parent);
        }
      }

      objectTree.scrollPathToVisible(new TreePath(treeItem.getPath()));
    }
  }

  @Override // TreeView
  public void removeItem(Object item) {
    List<DefaultMutableTreeNode> myList = (item instanceof UserObject)
        ? findAll(((UserObject) item))
        : findAll(item);

    for (DefaultMutableTreeNode node : myList) {
      treeModel().removeNodeFromParent(node);
    }
  }

  @Override // TreeView
  public void removeChildren(Object item) {
    DefaultMutableTreeNode node = findFirst(item);
    int size = node.getChildCount();

    for (int i = size - 1; i > -1; i--) {
      treeModel().removeNodeFromParent((DefaultMutableTreeNode) node.getChildAt(i));
    }
  }

  @Override // TreeView
  public void selectItem(Object item) {
    DefaultMutableTreeNode itemToSelect = findFirst(item);

    if (itemToSelect == null) {
      return;
    }

    TreePath treePath = new TreePath(itemToSelect.getPath());
    objectTree.setSelectionPath(treePath);
    objectTree.scrollPathToVisible(treePath);
  }

  @Override
  public void selectItems(Set items) {
    objectTree.removeSelectionPaths(objectTree.getSelectionPaths());

    if (items != null) {
      for (Object item : items) {
        DefaultMutableTreeNode itemToSelect = findFirst(item);

        if (itemToSelect == null) {
          break;
        }

        TreePath treePath = new TreePath(itemToSelect.getPath());
        objectTree.addSelectionPath(treePath);
      }
    }
  }

  @Override // TreeView
  public void itemChanged(Object item) {
    for (DefaultMutableTreeNode node : findAll(item)) {
      sortItems(node.getParent());
    }
  }

  @Override // TreeView
  public UserObject getSelectedItem() {
    TreePath treePath = objectTree.getSelectionPath();

    if (treePath != null) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) treePath.getLastPathComponent();
      return (UserObject) node.getUserObject();
    }
    else {
      return null;
    }
  }

  @Override
  public Set<UserObject> getSelectedItems() {
    Set<UserObject> objects = new HashSet<>();
    TreePath[] selectionPaths = objectTree.getSelectionPaths();

    if (selectionPaths != null) {
      for (TreePath path : selectionPaths) {
        if (path != null) {
          DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
          objects.add((UserObject) node.getUserObject());
        }
      }
    }

    return objects;
  }

  /**
   * Updates the model name.
   *
   * @param text The new name.
   */
  public void updateText(String text) {
    textFieldModelName.setText(text);
  }

  /**
   * F�gt ein Element am �bergebenen Index bzw an letzter Stelle ein, falls
   * das Elternobjekt weniger Elemente besitzt.
   *
   * @param treeItem Das einzuf�gende Item.
   * @param parentItem Das Elternobjekt.
   * @param index Die Position.
   */
  private void insertElementAt(SortableTreeNode treeItem,
                               DefaultMutableTreeNode parentItem,
                               int index) {
    if (parentItem.getChildCount() < index) {
      treeModel().insertNodeInto(treeItem, parentItem, parentItem.getChildCount());
    }
    else {
      treeModel().insertNodeInto(treeItem, parentItem, index);
    }
  }

  private DefaultTreeModel treeModel() {
    return (DefaultTreeModel) fTreeModel;
  }

  private void setRoot(UserObject root) {
    fRootNode = new SortableTreeNode(root);
    fTreeModel = createTreeModel(fRootNode);
    objectTree.setModel(fTreeModel);
  }

  /**
   * Fabrikmethode zum Erzeugen des TreeModels.
   *
   * @param root
   * @return
   */
  private TreeModel createTreeModel(MutableTreeNode root) {
    return new DefaultTreeModel(root);
  }

  /**
   * Fabrikmethode zur Erzeugung eines Knotens.
   *
   * @param item Das UserObject.
   * @param sorting Ob die Kindelemente des Knotens sortiert werden sollen.
   * @return Der Knoten.
   */
  private SortableTreeNode createTreeNode(UserObject item, boolean sorting) {
    SortableTreeNode treeNode = new SortableTreeNode(item);
    treeNode.setSorting(sorting);

    return treeNode;
  }

  /**
   * Fabrikmethode zur Erzeugung eines Comparators, mit dessen Hilfe die
   * Sortierung durchgef�hrt wird.
   *
   * @return
   */
  private Comparator createSortComparator() {
    return new AscendingTreeViewNameComparator();
  }

  /**
   * Traversiert den gesamten Baum, um alle Knoten zu finden, die das �bergebene
   * Datenobjekt in ihrem UserObject halten.
   *
   * @param dataObject
   * @return
   */
  private List<DefaultMutableTreeNode> findAll(Object dataObject) {
    List<DefaultMutableTreeNode> searchNodes = new ArrayList<>();
    Enumeration e = fRootNode.preorderEnumeration();

    while (e.hasMoreElements()) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
      UserObject userObject = (UserObject) node.getUserObject();

      if (dataObject.equals(userObject.getModelComponent())) {
        searchNodes.add(node);
      }
    }

    return searchNodes;
  }

  /**
   * Looks for nodes that contain the given user object.
   *
   * @param o The user object to look for.
   * @return All nodes that contain this user object.
   */
  private List<DefaultMutableTreeNode> findAll(UserObject o) {
    List<DefaultMutableTreeNode> searchNodes = new ArrayList<>();
    Enumeration e = fRootNode.preorderEnumeration();

    while (e.hasMoreElements()) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
      UserObject userObject = (UserObject) node.getUserObject();

      if (userObject == o) {
        searchNodes.add(node);
      }
    }

    return searchNodes;
  }

  /**
   * Liefert das DefaultMutableTreeNode-Objekt zu dem angegeben Pfad.
   *
   * @param selPath
   * @return
   */
  private UserObject getUserObject(TreePath selPath) {
    DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) objectTree.getLastSelectedPathComponent();

    return treeNode != null ? (UserObject) treeNode.getUserObject() : null;
  }

  /**
   * Called by delete(): Undo/Redo the "Delete" action
   */
  private class DeleteEdit
      extends AbstractUndoableEdit {

    private final List<UserObject> userObjects = new ArrayList<>();
    private final List<Figure> figures = new ArrayList<>();

    public DeleteEdit(List<UserObject> userObjects, List<Figure> figures) {
      this.userObjects.addAll(userObjects);
      this.figures.addAll(figures);
    }

    @Override
    public String getPresentationName() {
      return ResourceBundleUtil.getBundle().getString("edit.delete.text");
    }

    @Override
    public void undo() throws CannotUndoException {
      super.undo();
      restoreItems(userObjects, figures);
    }

    @Override
    public void redo() throws CannotRedoException {
      super.redo();
      // TODO: Delete again ...
      for (UserObject userObject : userObjects) {
        userObject.removed();
      }
    }
  }

  /**
   * Called by paste(): Undo/Redo the "Paste" action
   */
  private class PasteEdit
      extends AbstractUndoableEdit {

    private final List<UserObject> userObjects = new ArrayList<>();
    private final List<Figure> figures = new ArrayList<>();

    public PasteEdit(List<UserObject> userObjects, List<Figure> figures) {
      this.userObjects.addAll(userObjects);
      this.figures.addAll(figures);
    }

    @Override
    public String getPresentationName() {
      return ResourceBundleUtil.getBundle().getString("edit.paste.text");
    }

    @Override
    public void undo() throws CannotUndoException {
      super.undo();

      for (UserObject userObject : userObjects) {
        userObject.removed();

        if (userObject.getModelComponent() instanceof FigureComponent) {
          Figure figure = ((FigureComponent) userObject.getModelComponent()).getFigure();
          figures.add(figure);
        }
      }
    }

    @Override
    public void redo() throws CannotRedoException {
      super.redo();
      restoreItems(userObjects, figures);
    }
  }

  // CHECKSTYLE:OFF
  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    scrollPaneTree = new javax.swing.JScrollPane();
    objectTree = new StandardActionTree(this);
    textFieldModelName = new javax.swing.JTextField();

    setLayout(new java.awt.BorderLayout());

    scrollPaneTree.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
    scrollPaneTree.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

    objectTree.setRootVisible(false);
    scrollPaneTree.setViewportView(objectTree);

    add(scrollPaneTree, java.awt.BorderLayout.CENTER);

    textFieldModelName.setEditable(false);
    textFieldModelName.setBackground(new java.awt.Color(153, 153, 255));
    textFieldModelName.setFont(textFieldModelName.getFont().deriveFont(textFieldModelName.getFont().getStyle() | java.awt.Font.BOLD));
    textFieldModelName.setForeground(new java.awt.Color(255, 255, 255));
    textFieldModelName.setText("Model");
    add(textFieldModelName, java.awt.BorderLayout.PAGE_START);
  }// </editor-fold>//GEN-END:initComponents
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JTree objectTree;
  private javax.swing.JScrollPane scrollPaneTree;
  private javax.swing.JTextField textFieldModelName;
  // End of variables declaration//GEN-END:variables
  // CHECKSTYLE:ON
}
