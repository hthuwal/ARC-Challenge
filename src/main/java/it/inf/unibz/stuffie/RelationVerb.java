package it.inf.unibz.stuffie;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;

public class RelationVerb extends RelationComponent {

	private String synthVerb;
	
	public RelationVerb(IndexedWord headword, int sentID, SemanticGraph depAnno) {
		super(headword, sentID, depAnno);
	}
	
	public boolean isSynthetic() {
		return headword==null;
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
