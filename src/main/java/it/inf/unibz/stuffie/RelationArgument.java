package it.inf.unibz.stuffie;

import edu.stanford.nlp.ling.IndexedWord;

public class RelationArgument extends RelationComponent {

	private TraversalPath chainToVerb;
	private RelationArgumentConnector connector;

	public RelationArgument(IndexedWord headword, TraversalPath chainToVerb, RelationArgumentConnector connector) {
		super(headword);
		this.chainToVerb = chainToVerb;
		this.connector = connector;
	}

	public TraversalPath getChainToVerb() {
		return chainToVerb;
	}
	
	public RelationArgumentConnector getConnector() {
		return connector;
	}

}
