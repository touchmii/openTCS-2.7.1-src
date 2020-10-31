/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.components.tree;

import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import org.opentcs.guing.components.tree.elements.UserObject;

/**
 * Ein CellRenderer f�r die Knoten der Baumansicht, die �ber spezielle Icons
 * verf�gen.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class TreeViewCellRenderer
    extends DefaultTreeCellRenderer {

  /**
   * Creates a new instance of TreeViewCellRenderer
   */
  public TreeViewCellRenderer() {
    super();
  }

  @Override
  public Component getTreeCellRendererComponent(
      JTree tree, Object value, boolean sel, boolean expanded, boolean leaf,
      int row, boolean hasFocus) {

    super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

    DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
    Object userObject = node.getUserObject();

    if (userObject instanceof UserObject) {
      ImageIcon icon = ((UserObject) userObject).getIcon();

      if (icon != null) {
        setIcon(icon);
      }
    }

    return this;
  }
}
