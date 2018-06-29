package it.unibz.inf.stuffie;

import java.util.HashMap;
import java.util.Properties;
import java.util.TreeSet;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.UniversalEnglishGrammaticalRelations;

public class LongSubjectExtractor extends SubjectExtractor {
	
	public LongSubjectExtractor(Annotation stAnno, Properties stProp, StanfordCoreNLP stPipe, Mode... relevantModes) {
		super(stAnno, stProp, stPipe, relevantModes);
	}
	
	public LongSubjectExtractor() {
		super();
	}

	@Override
	protected Boolean run(RelationInstance rel, int iteration,HashMap<String, RelationArgument> idToComponentMap) {
		IndexedWord verbSrc = rel.getVerb().headword;
		SemanticGraph depAnno = rel.getVerb().getDepAnno();

		do {
			TreeSet<IndexedWord> advcls = new TreeSet<IndexedWord>(new IndexedWordComparator(verbSrc));
			if(modes.get(Mode.DependentSubject.class) == Mode.DependentSubject.TRANSFER_ALL) {
				advcls.addAll(depAnno.getParentsWithReln(verbSrc, UniversalEnglishGrammaticalRelations.shortNameToGRel.get("advcl")));
				advcls.addAll(depAnno.getParentsWithReln(verbSrc, UniversalEnglishGrammaticalRelations.shortNameToGRel.get("xcomp")));
			} else if(modes.get(Mode.DependentSubject.class) == Mode.DependentSubject.TRANSFER_ADVCL) {
				advcls.addAll(depAnno.getParentsWithReln(verbSrc, UniversalEnglishGrammaticalRelations.shortNameToGRel.get("advcl")));
			} else if(modes.get(Mode.DependentSubject.class) == Mode.DependentSubject.TRANSFER_XCOMP) {
				advcls.addAll(depAnno.getParentsWithReln(verbSrc, UniversalEnglishGrammaticalRelations.shortNameToGRel.get("xcomp")));
			}
			
			if(advcls.isEmpty())
				return false;
			else {
				verbSrc = advcls.first();
				boolean res = traverseOneStep(rel, iteration, verbSrc, depAnno,idToComponentMap);
				if(res)
					return true;
			}
			
		} while (true);
	}

}
