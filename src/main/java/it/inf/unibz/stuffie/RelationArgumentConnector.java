package it.inf.unibz.stuffie;

import edu.stanford.nlp.ling.IndexedWord;

public class RelationArgumentConnector extends RelationComponent {

	private TraversalPath chainToArgument;
	private boolean expectingConnector;
	private String synthConnector;
	
	public RelationArgumentConnector(IndexedWord headword, int sentID) {
		super(headword, sentID);
	}
	
	public RelationArgumentConnector(IndexedWord headword, int sentID, TraversalPath chainToArgument) {
		super(headword, sentID);
		this.chainToArgument = chainToArgument;
	}
	
	public TraversalPath getChainToArgument() {
		return chainToArgument;
	}
	
	public String toString() {
		if(headword == null && !expectingConnector)
			return "";
		else if(headword == null && expectingConnector) {
			if(synthConnector!=null)
				return "<" + synthConnector + ">";
			else
				return NULL_CMPNT;
		}
			
		return super.toString();
	}

}
