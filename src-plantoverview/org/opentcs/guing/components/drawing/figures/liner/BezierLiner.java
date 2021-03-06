/**
 * (c): IML.
 *
 */
package org.opentcs.guing.components.drawing.figures.liner;

import java.util.Collection;
import java.util.Collections;
import org.jhotdraw.draw.ConnectionFigure;
import org.jhotdraw.draw.LineConnectionFigure;
import org.jhotdraw.draw.handle.Handle;
import org.jhotdraw.draw.liner.Liner;
import org.jhotdraw.geom.BezierPath;
import org.jhotdraw.xml.DOMInput;
import org.jhotdraw.xml.DOMOutput;

/**
 * A {@link Liner} that constrains a connection to a quadratic or cubic curved
 * line.
 *
 * @author Heinz Huber (Fraunhofer IML)
 */
public class BezierLiner
		implements 
		org.jhotdraw.draw.liner.Liner, 
		org.jhotdraw.xml.DOMStorable {

	/**
	 * Creates a new instance.
	 */
	public BezierLiner() {
	}

	@Override	// Liner
	public Collection<Handle> createHandles(BezierPath path) {
		return Collections.emptyList();
	}

	@Override	// Liner
	public void lineout(ConnectionFigure figure) {
		BezierPath path = ((LineConnectionFigure) figure).getBezierPath();

		if (path != null) {
			path.invalidatePath();
		}
	}

	@Override // DOMStorable
	public void read(DOMInput in) {
	}

	@Override // DOMStorable
	public void write(DOMOutput out) {
	}

	@Override // Object
	public Liner clone() {
		try {
			return (Liner) super.clone();
		}
		catch (CloneNotSupportedException ex) {
			InternalError error = new InternalError(ex.getMessage());
			error.initCause(ex);
			throw error;
		}
	}
}
