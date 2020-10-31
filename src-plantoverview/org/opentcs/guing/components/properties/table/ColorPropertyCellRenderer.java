/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.components.properties.table;

import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.Border;
import org.opentcs.guing.components.properties.type.ColorProperty;

/**
 * Ein Renderer f�r ein ColorProperty.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class ColorPropertyCellRenderer
    extends JLabel
    implements javax.swing.table.TableCellRenderer {

  /**
   * Creates a new instance of ColorPropertyCellRenderer
   */
  public ColorPropertyCellRenderer() {
    super();
    setOpaque(true);
    Border insideBorder = BorderFactory.createLineBorder(Color.black);
    Border outsideBorder = BorderFactory.createMatteBorder(5, 10, 5, 10, Color.white);
    setBorder(BorderFactory.createCompoundBorder(outsideBorder, insideBorder));
  }

  @Override
  public java.awt.Component getTableCellRendererComponent(
      JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

    ColorProperty property = (ColorProperty) value;
    setBackground(property.getColor());

    return this;
  }
}
