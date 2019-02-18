package it.unibz.inf.stuffie;

import java.util.ArrayList;
import java.util.HashSet;

public class TraversalPath extends ArrayList<TraversalArc> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6965849574058346307L;

	private HashSet<Integer> fromIndexes;

	public TraversalPath() {
		super();
		fromIndexes = new HashSet<Integer>();
	}

	public TraversalPath(TraversalArc initArc) {
		this();
		add(initArc);
	}

	public boolean add(TraversalArc arc) throws DisconnectedTraversalPathException {
		if (this.isEmpty()) {
			fromIndexes.add(arc.getFrom().index());
			return super.add(arc);
		}

		TraversalArc last = this.get(this.size() - 1);
		if (last.getTo().index() != arc.getFrom().index())
			throw new DisconnectedTraversalPathException("Attempting to connect path ending with #" + last.getTo().index() + " to an arc starting with #" + arc.getFrom().index());
		if (fromIndexes.contains(last.getTo().index()))
			throw new CircularTraversalPathException("Circular path on index #" + last.getTo().index());

		return super.add(arc);

	}

	private class DisconnectedTraversalPathException extends IllegalArgumentException {

		/**
		 * 
		 */
		private static final long serialVersionUID = 511615848591971704L;
		
		public DisconnectedTraversalPathException(String string) {
			super(string);
		}

	}

	private class CircularTraversalPathException extends IllegalArgumentException {

		/**
		 * 
		 */
		private static final long serialVersionUID = 4089752874563889998L;
		
		public CircularTraversalPathException(String string) {
			super(string);
		}

	}

}
