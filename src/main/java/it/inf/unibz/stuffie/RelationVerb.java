package it.inf.unibz.stuffie;

import edu.stanford.nlp.ling.IndexedWord;

public class RelationVerb extends RelationComponent {

	private String synthVerb;
	
	public RelationVerb(IndexedWord headword) {
		super(headword);
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

	
}
