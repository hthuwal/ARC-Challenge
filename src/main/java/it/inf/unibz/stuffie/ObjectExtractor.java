package it.inf.unibz.stuffie;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.UniversalEnglishGrammaticalRelations;

public class ObjectExtractor extends PipelineStep<Boolean, RelationInstance> {

	private LinkedHashSet<DependencyArc> objArcs;

	public ObjectExtractor(Annotation stAnno, Properties stProp, StanfordCoreNLP stPipe) {
		super(stAnno, stProp, stPipe);

		objArcs = new LinkedHashSet<DependencyArc>();
		try {
			BufferedReader br = new BufferedReader(new FileReader("resource/object_arcs.txt"));
			for (String line = br.readLine(); line != null; line = br.readLine()) {
				String x[] = line.split(Pattern.quote(","));
				objArcs.add(new DependencyArc(UniversalEnglishGrammaticalRelations.shortNameToGRel.get(x[0]),
						DependencyArc.Direction.valueOf(x[1])));
			}
			br.close();
		} catch (Exception e) {

		}
	}

	@Override
	protected Boolean run(RelationInstance rel, int iteration) {
		IndexedWord verbSrc = rel.getVerb().headword;
		SemanticGraph depAnno = rel.getVerb().getDepAnno();

		TreeSet<IndexedWord> candidates = new TreeSet<IndexedWord>(new IndexedWordComparator(verbSrc, true));
		HashMap<Integer, DependencyArc> arcTempStorage = new HashMap<Integer, DependencyArc>();
		for (DependencyArc arc : objArcs) {
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
			RelationArgument obj = new RelationArgument(candidates.first(), rel.getVerb().getSentenceID(), depAnno,
					true);
			obj.setChainFromVerb(
					new TraversalPath(new TraversalArc(verbSrc, arc.getRel(), obj.headword, arc.getDir())));

			if (first) {
				rel.setObject(new RelationArgument(candidate, rel.getVerb().getSentenceID(), depAnno, false));
				first = false;
			} else {
				rel.addFacet(new RelationArgument(candidate, rel.getVerb().getSentenceID(), depAnno, false));
			}
		}

		return true;
	}

}
