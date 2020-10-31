/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.components.drawing.figures;

import org.jhotdraw.draw.AttributeKey;
import org.opentcs.guing.components.drawing.course.Origin;
import org.opentcs.guing.model.FigureComponent;

/**
 * Allgemeine Konstanten, die insbesondere Figures betreffen.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public interface FigureConstants {

	/**
	 * �ber diesen Schl�ssel greifen Figures auf ihr Model zu.
	 */
	AttributeKey<FigureComponent> MODEL =
			new AttributeKey<>("Model", FigureComponent.class);
	/**
	 * �ber dieses Attribut erhalten Figures Zugriff auf den Referenzpunkt.
	 */
	AttributeKey<Origin> ORIGIN = new AttributeKey<>("Origin", Origin.class);
}
