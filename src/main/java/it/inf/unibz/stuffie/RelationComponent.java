package it.inf.unibz.stuffie;

import java.util.TreeSet;
import java.util.regex.Pattern;

import edu.stanford.nlp.ling.IndexedWord;

public class RelationComponent {

	protected static final Pattern VERB_PATTERN = Pattern.compile("VB|VBD|VBG|VBN|VBP|VBZ");

	protected IndexedWord headword;
	protected TreeSet<IndexedWord> words;

	public RelationComponent(IndexedWord headword) {
		this.headword = headword;
		words = new TreeSet<IndexedWord>(new IndexedWordComparator());
	}
	
	public IndexedWord getHeadword() {
		return headword;
	}

	public String getPOS() {
		return headword.tag();
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
			sb.append(word.originalText()).append(" ");
		}
		
		if(sb.length() > 0)
			sb.deleteCharAt(sb.length()-1);
		
		return sb.toString();
	}

}
