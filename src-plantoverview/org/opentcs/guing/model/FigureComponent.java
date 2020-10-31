/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.model;

import java.util.ArrayList;
import org.jhotdraw.draw.Figure;
import org.opentcs.guing.model.elements.AbstractConnection;

/**
 * Interface f�r solche ModelComponent-Klassen, die jeweils ein Figure als
 * grafische Repr�sentation besitzen.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public interface FigureComponent
    extends ModelComponent {

  /**
   * Setzt das Figure.
   *
   * @param figure
   */
  void setFigure(Figure figure);

  /**
   * Liefert das Figure.
   *
   * @return das Figure
   */
  Figure getFigure();

  /**
   * Adds a Path to another Point or a Link to a Location
   *
   * @param connection The Path or Link to be added.
   */
  void addConnection(AbstractConnection connection);

  /**
   * @return All connected Paths and Links
   */
  ArrayList<AbstractConnection> getConnections();

  /**
   * Removes a connection which is no longer connected to this figure.
   *
   * @param connection The Path or Link to be removed.
   */
  void removeConnection(AbstractConnection connection);
}
