/**
 * (c): IML, IFAK, JHotDraw.
 *
 */
package org.opentcs.guing.components.drawing;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.Serializable;

/**
 * Ein Punkt, der eine exakte Position wiedergibt, die sich auch durch das
 * Zoomen nicht ver�ndert. Insbesondere ist diese Art von Punkt f�r das Zoomen
 * n�tig, bei dem die Positionen der Figures ge�ndert werden, nicht jedoch deren
 * Gr��e. <p> i. Auftretende F�lle bei symbolischer Zeichenweise: <br> (a)
 * �nderung des x- oder y-Attributes: keine Auswirkungen (b) Bewegung des
 * Punktes mit der Maus: genaue Position ist in dem Zoompunkt zu speichern (c)
 * Zoomen: die neue Position des Punktes wird anhand der genauen Werte des
 * Zoompunkts ermittelt <p> ii. Auftretende F�lle bei ma�stabsgetreuer
 * Zeichenweise: <br> (a) �nderung des x- oder y-Attributes: der Punkt muss der
 * Zoomstufe entsprechend m�glichst genau platziert werden; der exakte Wert ist
 * zu ermitteln und im Zoompunkt abzulegen (b) Bewegung des Punktes mit der
 * Maus: genaue Position ist in dem Zoompunkt zu speichern (c) Zoomen: die neue
 * Position des Punktes wird anhand der genauen Werte des Zoompunkts ermittelt;
 * das x- und y-Attribut darf davon nicht beeinflusst werden
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class ZoomPoint implements Serializable {

	/**
	 * Der x-Wert in Pixel bei Zoomfaktor 1.
	 */
	protected double fX;
	/**
	 * Der y-Wert in Pixel bei Zoomfaktor 1.
	 */
	protected double fY;
	/**
	 * Die momentane Zoomstufe.
	 */
	protected double fScale;

	/**
	 * Creates a new instance of ZoomPoint
	 */
	public ZoomPoint() {
		this(0, 0);
	}

	/**
	 * Konstruktor mit �bergabe des x- und y-Werts.
	 */
	public ZoomPoint(double x, double y) {
		this(x, y, 1.0);
	}

	/**
	 * Konstruktor mit �bergabe des x-, y-Wertes und des momentanten Zoomfaktors.
	 */
	public ZoomPoint(double x, double y, double scale) {
		fX = x / scale;
		fY = y / scale;
		fScale = scale;
	}

	/**
	 * Liefert die aktuelle Zoomstufe.
	 */
	public double scale() {
		return fScale;
	}

	/**
	 * Setzt den Wert f�r x.
	 */
	public void setX(double x) {
		fX = x;
	}

	/**
	 * Setzt den y-Wert.
	 */
	public void setY(double y) {
		fY = y;
	}

	/**
	 * Liefert den x-Wert.
	 */
	public double getX() {
		return fX;
	}

	/**
	 * Liefert den y-Wert.
	 */
	public double getY() {
		return fY;
	}

	/**
	 * Liefert die Position des Punktes in Pixel bei der aktuellen Zoomstufe.
	 *
	 * @return die Position in Pixel
	 */
	public Point getPixelLocation() {
		int x = (int) (getX() * scale());
		int y = (int) (getY() * scale());
		
		return new Point(x, y);
	}

	/**
	 * Liefert die exakte Position des Punktes in Pixel bei der aktuellen
	 * Zoomstufe.
	 *
	 * @return die Position in Pixel
	 */
	public Point2D getPixelLocationExactly() {
		double x = getX() * scale();
		double y = getY() * scale();
	
		return new Point2D.Double(x, y);
	}

	/**
	 * Mitteilung, dass sich die Zoomstufe ge�ndert hat.
	 *
	 * @param scale Der neue Zoomfaktor.
	 */
	public void scaleChanged(double scale) {
		fScale = scale;
	}

	/**
	 * Nachricht, dass der Punkt direkt durch den Benutzer mit der Maus bewegt
	 * wurde. Der Zoompunkt muss daraus die genaue Position ermittelt.
	 *
	 * @param x Die x-Koordinate in Pixel.
	 * @param y Die y-Koordinate in Pixel.
	 */
	public void movedByMouse(int x, int y) {
		fX = x / scale();
		fY = y / scale();
	}
}
