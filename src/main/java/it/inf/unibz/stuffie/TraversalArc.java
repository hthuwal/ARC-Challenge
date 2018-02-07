package it.inf.unibz.stuffie;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.trees.GrammaticalRelation;

public class TraversalArc {
	
	enum Direction {
		NORMAL, INVERSE;
	}
	
	private IndexedWord from;
	private GrammaticalRelation rel;
	private IndexedWord to;
	private Direction dir;
	
	public TraversalArc(IndexedWord from, GrammaticalRelation rel, IndexedWord to, Direction dir) {
		super();
		this.from = from;
		this.rel = rel;
		this.to = to;
		this.dir = dir;
	}

	public IndexedWord getFrom() {
		return from;
	}

	public GrammaticalRelation getRel() {
		return rel;
	}

	public IndexedWord getTo() {
		return to;
	}

	public Direction getDir() {
		return dir;
	}
	
	
}
