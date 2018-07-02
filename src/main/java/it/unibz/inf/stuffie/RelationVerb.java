package it.unibz.inf.stuffie;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;

public class RelationVerb extends RelationComponent {

	private String synthVerb;
	protected SemanticGraph depAnnoEnch;
	
	public RelationVerb(IndexedWord headword, int sentID, SemanticGraph depAnno, SemanticGraph depAnnoEnch) {
		super(headword, sentID, depAnno);
		this.depAnnoEnch = depAnnoEnch;
	}
	
	public RelationVerb(String synthVerb, int sentID, SemanticGraph depAnno, SemanticGraph depAnnoEnch, int synthID) {
		super(null, sentID, depAnno);
		this.synthVerb = synthVerb;
		this.id = sentenceID + "." + synthID;
		this.depAnnoEnch = depAnnoEnch;
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

	public SemanticGraph getEnchDepAnno() {
		return depAnnoEnch;
	}

	
}
