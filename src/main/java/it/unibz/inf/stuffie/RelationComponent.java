package it.unibz.inf.stuffie;

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
	protected boolean isStatic;

	public boolean isStatic() {
		return isStatic;
	}

	public void setStatic(boolean isStatic) {
		this.isStatic = isStatic;
	}

	public RelationComponent(IndexedWord headword, int sentenceID, SemanticGraph depAnno) {
		this.headword = headword;
		this.sentenceID = sentenceID;
		if (headword != null)
			id = sentenceID + "." + headword.index();
		else
			id = sentenceID + "." + 0;
		words = new TreeSet<IndexedWord>(new IndexedWordComparator());
		if (headword != null)
			words.add(headword);
		this.depAnno = depAnno;
		isStatic = false;
	}

	public RelationComponent(IndexedWord headword, TreeSet<IndexedWord> words, int sentenceID, SemanticGraph depAnno) {
		this.headword = headword;
		this.sentenceID = sentenceID;
		if (headword != null)
			id = sentenceID + "." + headword.index();
		else
			id = sentenceID + "." + 0;
		this.words = words;
		isStatic = true;
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

		for (IndexedWord word : words) {
			sb.append(word.originalText()).append(' ');
		}

		if (sb.length() > 0)
			sb.deleteCharAt(sb.length() - 1);
		else
			return NULL_CMPNT;

		return sb.toString();
	}

	public void addWords(IndexedWord iw) {
		words.add(iw);
	}

}
