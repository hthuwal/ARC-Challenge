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

	LinkedHashSet<DependencyArc> subjArcs;

	public SubjectExtractor(Annotation stAnno, Properties stProp, StanfordCoreNLP stPipe) {
		super(stAnno, stProp, stPipe);
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

		TreeSet<IndexedWord> candidates = new TreeSet<IndexedWord>(new IndexedWordComparator());
		for (DependencyArc arc : subjArcs) {
			if (arc.getDir() == DependencyArc.Direction.OUT)
				candidates.addAll(depAnno.getChildrenWithReln(verbSrc, arc.getRel()));
			else
				candidates.addAll(depAnno.getParentsWithReln(verbSrc, arc.getRel()));

			if (candidates.isEmpty())
				continue;

			rel.setSubject(new RelationArgument(candidates.first(), rel.getVerb().getSentenceID(), depAnno, true));
		}

		return false;
	}

}
