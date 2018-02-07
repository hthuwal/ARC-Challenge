package it.inf.unibz.stuffie;

import edu.stanford.nlp.ling.IndexedWord;

public class RelationArgumentConnector extends RelationComponent {

	private TraversalPath chainToArgument;
	
	public RelationArgumentConnector(IndexedWord headword, TraversalPath chainToArgument) {
		super(headword);
		this.chainToArgument = chainToArgument;
	}
	
	public TraversalPath getChainToArgument() {
		return chainToArgument;
	}

}
