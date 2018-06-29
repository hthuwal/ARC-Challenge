package it.unibz.inf.stuffie;

import java.util.HashMap;
import java.util.Properties;
import java.util.TreeSet;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.UniversalEnglishGrammaticalRelations;

public class SubjectExtractor extends ComponentExtractor {

	public SubjectExtractor(Annotation stAnno, Properties stProp, StanfordCoreNLP stPipe, Mode... relevantModes) {
		super(stAnno, stProp, stPipe, "resource/subject_arcs.txt", relevantModes);
	}

	public SubjectExtractor() {
		super("resource/subject_arcs.txt");
	}

	@Override
	protected Boolean run(RelationInstance rel, int iteration, HashMap<String, RelationArgument> idToComponentMap) {
		IndexedWord verbSrc = rel.getVerb().headword;
		SemanticGraph depAnno = rel.getVerb().getDepAnno();

		if (traverseOneStep(rel, iteration, verbSrc, depAnno, idToComponentMap)) {
			return true;
		} else {
			TreeSet<IndexedWord> cops = new TreeSet<IndexedWord>(new IndexedWordComparator(verbSrc));
			cops.addAll(depAnno.getParentsWithReln(verbSrc,
					UniversalEnglishGrammaticalRelations.shortNameToGRel.get("cop")));

			if (cops.isEmpty())
				return false;
			else
				return traverseOneStep(rel, iteration, cops.first(), depAnno, idToComponentMap);
		}

	}

	protected Boolean traverseOneStep(RelationInstance rel, int iteration, IndexedWord verbSrc, SemanticGraph depAnno,
			HashMap<String, RelationArgument> idToComponentMap) {
		TreeSet<IndexedWord> candidates = new TreeSet<IndexedWord>(new IndexedWordComparator(verbSrc));
		for (DependencyArc arc : arcs) {
			if (arc.getDir() == DependencyArc.Direction.OUT)
				candidates.addAll(depAnno.getChildrenWithReln(verbSrc, arc.getRel()));
			else
				candidates.addAll(depAnno.getParentsWithReln(verbSrc, arc.getRel()));

			if (candidates.isEmpty())
				continue;

			RelationArgument subj = new RelationArgument(candidates.first(), rel.getVerb().getSentenceID(), depAnno,
					true);
			subj.addChainFromVerb(new TraversalArc(verbSrc, arc.getRel(), subj.headword, arc.getDir()));
			rel.setSubject(subj);
			idToComponentMap.put(subj.id, subj);
			subj.setRelativeID(rel.getId() + "s");
			return true;
		}

		return false;
	}

}
