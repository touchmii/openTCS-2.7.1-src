/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import static java.util.Objects.requireNonNull;
import org.jhotdraw.draw.DefaultDrawing;
import org.jhotdraw.draw.Drawing;
import org.jhotdraw.draw.Figure;

/**
 * Eine Komponente des Systemmodells, die nur Objekte vom Typ FigureComponent
 * enth�lt. Ein FigureComponent besitzt eine Referenz auf ein Figure.
 * FiguresFolder verwaltet ein Drawing, in dem die Figure-Objekte der
 * FigureComponents enthalten sind. Soll im DrawingEditor ein anderes Drawing
 * gesetzt werden, so holt sich die Applikation zun�chst das entsprechende
 * Drawing von einem FiguresFolder.
 * <p>
 * <b>Entwurfsmuster:</b> Kompositum.
 * FiguresFolder ist ein konkretes Kompositum.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @see FigureComponent
 */
public class FiguresFolder
    extends CompositeModelComponent {

  /**
   * Das Drawing, das die in den FigureComponents enthaltenen Figure-Objekte
   * enth�lt.
   */
  private transient Drawing fDrawing;

  /**
   * Creates a new instance of FiguresFolder
   */
  public FiguresFolder() {
    this("Figures");
  }

  /**
   * Erzeugt ein neues Objekt von FiguresFolder mit dem angegebenen Namen. Der
   * Name wird im TreeView angezeigt.
   *
   * @param name
   */
  public FiguresFolder(String name) {
    super(name);
    initDrawing();
  }

  /**
   * Liefert einen Vector mit den enthaltenen Figures zur�ck. Da Blockline
   * prinzipiell nur FigureComponents enth�lt, die wiederum die Figures
   * enthalten, m�ssen erst die Figures aus den FigureComponents
   * herausextrahiert werden.
   *
   * @return
   */
  public Iterator<Figure> figures() {
    List<Figure> figures = new ArrayList<>();

    List<ModelComponent> childComps = getChildComponents();
    synchronized (childComps) {
      for (ModelComponent component : childComps) {
        if (component instanceof FigureComponent) {
          Figure figure = ((FigureComponent) component).getFigure();
          if (figure != null) {
            figures.add(figure);
          }
        }
      }
    }

    return figures.iterator();
  }

  /**
   * Liefert das FigureComponent-Objekt, das eine Referenz auf das �bergebene
   * Figure enth�lt. Liefert null, falls keines der enthaltenen
   * FigureComponent-Objekte eine Referenz auf das �bergebene Figure besitzt.
   * Diese Methode kann daher auch f�r �berpr�fungen nach einem Enthaltensein
   * verwendet werden.
   *
   * @param figure
   * @return
   */
  public ModelComponent getFigureComponent(Figure figure) {
    requireNonNull(figure, "figure");

    for (ModelComponent childComp : getChildComponents()) {
      FigureComponent component = (FigureComponent) childComp;

      if (figure.equals(component.getFigure())) {
        return component;
      }
    }

    return null;
  }

  /**
   * Initialisiert das Drawing.
   */
  private void initDrawing() {
    fDrawing = new DefaultDrawing();
  }

  /**
   * Setzt das Drawing.
   *
   * @param drawing
   */
  public void setDrawing(Drawing drawing) {
    fDrawing = drawing;
  }

  /**
   * Liefert das Drawing.
   *
   * @return
   */
  public Drawing getDrawing() {
    return fDrawing;
  }
}
