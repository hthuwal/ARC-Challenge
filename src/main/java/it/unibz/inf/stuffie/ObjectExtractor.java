package it.unibz.inf.stuffie;

import java.util.HashMap;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.UniversalEnglishGrammaticalRelations;

public class ObjectExtractor extends ComponentExtractor {


	public ObjectExtractor(Annotation stAnno, Properties stProp, StanfordCoreNLP stPipe, Mode... relevantModes) {
		super(stAnno, stProp, stPipe, "resource/object_arcs.txt", relevantModes);
	}

	public ObjectExtractor() {
		super("resource/object_arcs.txt");
	}

	@Override
	protected Boolean run(RelationInstance rel, int iteration, HashMap<String, RelationArgument> idToComponentMap) {
		IndexedWord verbSrc = rel.getVerb().headword;
		SemanticGraph depAnno = rel.getVerb().getDepAnno();

		TreeSet<IndexedWord> candidates = new TreeSet<IndexedWord>(new IndexedWordComparator(verbSrc, true));
		HashMap<Integer, DependencyArc> arcTempStorage = new HashMap<Integer, DependencyArc>();
		for (DependencyArc arc : arcs) {
			Set<IndexedWord> temp;
			if (arc.getDir() == DependencyArc.Direction.OUT) {
				temp = depAnno.getChildrenWithReln(verbSrc, arc.getRel());

				if (!temp.isEmpty() && arc.getRel().getShortName().equals("ccomp")) {
					Set<IndexedWord> shiftingSet = depAnno.getChildrenWithReln(verbSrc, arc.getRel());
					for (IndexedWord iw : shiftingSet) {
						if (iw.tag().equals("JJ")) {
							TreeSet<IndexedWord> realVBs = new TreeSet<IndexedWord>(new IndexedWordComparator(iw));
							realVBs.addAll(depAnno.getChildrenWithReln(iw,
									UniversalEnglishGrammaticalRelations.shortNameToGRel.get("cop")));
							if (!realVBs.isEmpty()) {
								temp.remove(iw);
								temp.add(realVBs.first());
							}
						}
					}
				}
			} else
				temp = depAnno.getParentsWithReln(verbSrc, arc.getRel());

			for (IndexedWord iw : temp) {
				arcTempStorage.put(iw.index(), arc);
			}
			candidates.addAll(temp);
		}

		if (candidates.isEmpty())
			return false;

		boolean first = true;
		for (IndexedWord candidate : candidates) {
			DependencyArc arc = arcTempStorage.get(candidate.index());
			RelationArgument obj = new RelationArgument(candidate, rel.getVerb().getSentenceID(), depAnno, false);
			obj.addChainFromVerb(new TraversalArc(verbSrc, arc.getRel(), obj.headword, arc.getDir()));

			if (first) {
				rel.setObject(obj);
				first = false;
			} else {
				rel.addFacet(obj);
			}
		}

		return true;
	}

}
