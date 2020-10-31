/**
 * (c) 2014 Fraunhofer IML.
 *
 */
package org.opentcs.guing.components.tree;

import org.opentcs.guing.components.EditableComponent;

/**
 *
 * @author Heinz Huber (Fraunhofer IML)
 */
public class StandardActionTree
    extends javax.swing.JTree
    implements EditableComponent {

  private final EditableComponent parent;

  public StandardActionTree(EditableComponent parent) {
    this.parent = parent;
  }

  @Override
  public void cutSelectedItems() {
    parent.cutSelectedItems();
  }

  @Override
  public void copySelectedItems() {
    parent.copySelectedItems();
  }

  @Override
  public void pasteBufferedItems() {
    parent.pasteBufferedItems();
  }

  @Override
  public void delete() {
    parent.delete();
  }

  @Override
  public void duplicate() {
    parent.duplicate();
  }

  @Override
  public void selectAll() {
    parent.selectAll();
  }

  /**
   * Note: EditableComponent.clearSelection() must _not_ be overridden
   * since the method JTree.clearSelection() is called in JTree's constructor.
   */
}