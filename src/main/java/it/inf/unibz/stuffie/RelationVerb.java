package it.inf.unibz.stuffie;

import edu.stanford.nlp.ling.IndexedWord;

public class RelationVerb extends RelationComponent {

	public RelationVerb(IndexedWord headword) {
		super(headword);
	}
	
	public boolean isSynthetic() {
		return headword==null;
	}

	
}
