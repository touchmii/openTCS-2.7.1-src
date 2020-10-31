/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.transport;

import org.opentcs.data.order.TransportOrder;

/**
 * Klassen, die an �nderungen der Liste der Transportauftr�ge interessiert sind,
 * implementieren dieses Interface.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public interface TransportOrderListener {

	/**
	 * Botschaft, dass ein Transportauftrag hinzugef�gt wurde.
	 *
	 * @param t der Transportauftrag
	 */
	void transportOrderAdded(TransportOrder t);

	/**
	 * Botschaft, dass ein Transportauftrag entfernt wurde.
	 *
	 * @param t der Transportauftrag
	 */
	void transportOrderRemoved(TransportOrder t);

	/**
	 * Botschaft, dass sich ein Transportauftrag ge�ndert hat.
	 *
	 * @param t der Transportauftrag
	 */
	void transportOrderChanged(TransportOrder t);
}
