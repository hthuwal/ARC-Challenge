package it.unibz.inf.stuffie;

import edu.stanford.nlp.trees.GrammaticalRelation;

public class DependencyArc {

	protected enum Direction {
		IN, OUT;
	}
	
	protected GrammaticalRelation rel;
	protected Direction dir;
	
	public DependencyArc(GrammaticalRelation rel, Direction dir) {
		super();
		this.rel = rel;
		this.dir = dir;
	}
	
	public GrammaticalRelation getRel() {
		return rel;
	}
	
	public Direction getDir() {
		return dir;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dir == null) ? 0 : dir.hashCode());
		result = prime * result + ((rel == null) ? 0 : rel.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DependencyArc other = (DependencyArc) obj;
		if (dir != other.dir)
			return false;
		if (rel == null) {
			if (other.rel != null)
				return false;
		} else if (!rel.equals(other.rel))
			return false;
		return true;
	}
	
	
}
