package it.unibz.inf.stuffie;

import com.google.common.collect.TreeMultimap;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;

public class EnhancedSubjectExtractor extends SubjectExtractor {

	public EnhancedSubjectExtractor() {
		super("resource/enhc_subject_arcs.txt");
	}

	@Override
	protected Boolean run(RelationInstance rel, int iteration, TreeMultimap<String, RelationComponent> idToComponentMap) {
		IndexedWord verbSrc = rel.getVerb().headword;
		SemanticGraph depAnno = rel.getVerb().getEnchDepAnno();
		return traverseOneStep(rel, iteration, verbSrc, depAnno, idToComponentMap);
	}
	
}
