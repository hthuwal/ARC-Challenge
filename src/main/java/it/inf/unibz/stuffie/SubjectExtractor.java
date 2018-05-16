package it.inf.unibz.stuffie;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.TreeSet;
import java.util.regex.Pattern;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.UniversalEnglishGrammaticalRelations;

public class SubjectExtractor extends PipelineStep<Boolean, RelationInstance> {

	protected LinkedHashSet<DependencyArc> subjArcs;

	public SubjectExtractor(Annotation stAnno, Properties stProp, StanfordCoreNLP stPipe, Mode... relevantModes) {
		super(stAnno, stProp, stPipe, relevantModes);
		subjArcs = new LinkedHashSet<DependencyArc>();
		try {
			BufferedReader br = new BufferedReader(new FileReader("resource/subject_arcs.txt"));
			for (String line = br.readLine(); line != null; line = br.readLine()) {
				String x[] = line.split(Pattern.quote(","));
				subjArcs.add(new DependencyArc(UniversalEnglishGrammaticalRelations.shortNameToGRel.get(x[0]),
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

		if(traverseOneStep(rel, iteration, verbSrc, depAnno)) {
			return true;
		} else {
			TreeSet<IndexedWord> cops = new TreeSet<IndexedWord>(new IndexedWordComparator(verbSrc));
			cops.addAll(depAnno.getParentsWithReln(verbSrc, UniversalEnglishGrammaticalRelations.shortNameToGRel.get("cop")));
			
			if(cops.isEmpty())
				return false;
			else
				return traverseOneStep(rel, iteration, cops.first(), depAnno);
		}

	}
	
	protected Boolean traverseOneStep(RelationInstance rel, int iteration, IndexedWord verbSrc, SemanticGraph depAnno) {
		TreeSet<IndexedWord> candidates = new TreeSet<IndexedWord>(new IndexedWordComparator(verbSrc));
		for (DependencyArc arc : subjArcs) {
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
			
			return true;
		}
		
		return false;
	}

}
