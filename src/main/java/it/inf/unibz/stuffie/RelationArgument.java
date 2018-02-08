package it.inf.unibz.stuffie;

import java.util.TreeSet;

import edu.stanford.nlp.ling.IndexedWord;

public class RelationArgument extends RelationComponent {

	private TraversalPath chainToVerb;
	private RelationArgumentConnector connector;
	private TreeSet<RelationVerb> contexts;
	private boolean isSubject;

	public RelationArgument(IndexedWord headword, int sentID, TraversalPath chainToVerb, RelationArgumentConnector connector, boolean isSubject) {
		super(headword, sentID);
		this.chainToVerb = chainToVerb;
		this.connector = connector;
		this.isSubject = isSubject;
		contexts = new TreeSet<RelationVerb>(new RelationComponentComparator(this));
	}

	public TraversalPath getChainToVerb() {
		return chainToVerb;
	}
	
	public RelationArgumentConnector getConnector() {
		return connector;
	}
	
	public String toString() {
		if(isVerb())
			return "#" + headword.index() +";";
		
		StringBuilder sbWords = new StringBuilder();
		StringBuilder sbConn = new StringBuilder();
		
		for(IndexedWord word : words) {
			sbWords.append(word.originalText()).append(' ');
		}
		if(sbWords.length() > 0)
			sbWords.deleteCharAt(sbWords.length()-1);
		else
			sbWords.append(NULL_CMPNT);
		
		if(contexts.size() > 0) {
			sbWords.append("<ctx");
			for(RelationVerb ctx : contexts) {
				sbWords.append("#"+ctx.getHeadword().index()+",");
			}
			sbWords.deleteCharAt(sbWords.length()-1);
			sbWords.append(">");
		}
		
		sbConn.append(connector.toString());
		
		if(isSubject && sbConn.length() > 0) {
			return sbWords.toString() + "; " + sbConn.toString();
		}
		if(!isSubject && sbConn.length() > 0) {
			return sbConn.toString() + "; " + sbWords.toString() + ";";
		}
		if(isSubject) {
			return sbWords.toString() + ";";
		}
		if(!isSubject) {
			return ";" + sbWords.toString() + ";";
		}
			
		
		return sbWords.toString();
	}

}
