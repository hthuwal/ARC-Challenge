package it.inf.unibz.stuffie;

import java.util.TreeSet;
import java.util.regex.Pattern;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;

public class RelationComponent {

	protected static final Pattern VERB_PATTERN = Pattern.compile("VB|VBD|VBG|VBN|VBP|VBZ");
	protected static final String NULL_CMPNT = "<_>";

	protected IndexedWord headword;
	protected TreeSet<IndexedWord> words;
	protected Integer sentenceID;
	protected String id;
	protected SemanticGraph depAnno;

	public RelationComponent(IndexedWord headword, int sentenceID, SemanticGraph depAnno) {
		this.headword = headword;
		this.sentenceID = sentenceID;
		id = sentenceID + "." + headword.index();
		words = new TreeSet<IndexedWord>(new IndexedWordComparator());
		words.add(headword);
		this.depAnno = depAnno;
	}
	
	public IndexedWord getHeadword() {
		return headword;
	}

	public String getPOS() {
		return headword.tag();
	}
	
	public int getSentenceID() {
		return sentenceID;
	}

	public Boolean isVerb() {
		if (headword.tag() == null)
			return null;
		else
			return (VERB_PATTERN.matcher(headword.tag()).matches());
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		for(IndexedWord word : words) {
			sb.append(word.originalText()).append(' ');
		}
		
		if(sb.length() > 0)
			sb.deleteCharAt(sb.length()-1);
		else
			return NULL_CMPNT;
		
		return sb.toString();
	}
	
	public void addWords(IndexedWord iw) {
		words.add(iw);
	}

}
