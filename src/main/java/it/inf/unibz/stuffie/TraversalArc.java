package it.inf.unibz.stuffie;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.trees.GrammaticalRelation;

public class TraversalArc extends DependencyArc {
	
	private IndexedWord from;
	private IndexedWord to;
	
	public TraversalArc(IndexedWord from, GrammaticalRelation rel, IndexedWord to, Direction dir) {
		super(rel, dir);
		this.from = from;
		this.rel = rel;
	}

	public IndexedWord getFrom() {
		return from;
	}

	public IndexedWord getTo() {
		return to;
	}
	
}
