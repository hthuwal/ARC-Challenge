package it.inf.unibz.stuffie;

import java.util.TreeSet;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;

public class RelationArgument extends RelationComponent {

	private TraversalPath chainFromVerb;
	private RelationArgumentConnector connector;
	private TreeSet<RelationVerb> contexts;
	private boolean isSubject;

	public RelationArgument(IndexedWord headword, int sentID, SemanticGraph depAnno, boolean isSubject) {
		super(headword, sentID, depAnno);
		this.isSubject = isSubject;
		contexts = new TreeSet<RelationVerb>(new RelationComponentComparator(this));
	}

	public void setChainFromVerb(TraversalPath p) {
		chainFromVerb = p;
	}

	public TraversalPath getChainFromVerb() {
		return chainFromVerb;
	}

	public RelationArgumentConnector getConnector() {
		return connector;
	}

	public String toString() {

		StringBuilder sbWords = new StringBuilder();
		StringBuilder sbConn = new StringBuilder();

		if (isVerb())
			sbWords.append("#" + headword.index());
		else {
			for (IndexedWord word : words) {
				sbWords.append(word.originalText()).append(' ');
			}
			if (sbWords.length() > 0)
				sbWords.deleteCharAt(sbWords.length() - 1);
			else
				sbWords.append(NULL_CMPNT);
		}

		if (contexts.size() > 0) {
			sbWords.append("<ctx");
			for (RelationVerb ctx : contexts) {
				sbWords.append("#" + ctx.getHeadword().index() + ",");
			}
			sbWords.deleteCharAt(sbWords.length() - 1);
			sbWords.append(">");
		}

		if (connector != null)
			sbConn.append(connector.toString());

		if (isSubject && sbConn.length() > 0) {
			return sbWords.toString() + "; " + sbConn.toString();
		}
		if (!isSubject && sbConn.length() > 0) {
			return sbConn.toString() + "; " + sbWords.toString() + ";";
		}
		if (isSubject) {
			return sbWords.toString() + "; ";
		}
		if (!isSubject) {
			return "; " + sbWords.toString() + ";";
		}

		return sbWords.toString();
	}

}
