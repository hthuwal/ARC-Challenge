package it.unibz.inf.stuffie;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;

public class RelationVerb extends RelationComponent {

	private String synthVerb;
	
	public RelationVerb(IndexedWord headword, int sentID, SemanticGraph depAnno) {
		super(headword, sentID, depAnno);
	}
	
	public RelationVerb(String synthVerb, int sentID, SemanticGraph depAnno, int synthID) {
		super(null, sentID, depAnno);
		this.synthVerb = synthVerb;
		this.id = sentenceID + "." + synthID;
	}
	
	public boolean isSynthetic() {
		return headword==null;
	}
	
	public void grammaticalFix() {
		
	}
	
	public String toString() {
		if(isSynthetic() && synthVerb!=null)
			return "<" + synthVerb + ">";
		else if (isSynthetic() && synthVerb == null)
			return NULL_CMPNT;
		
		return super.toString();
	}

	public SemanticGraph getDepAnno() {
		return depAnno;
	}

	
}
