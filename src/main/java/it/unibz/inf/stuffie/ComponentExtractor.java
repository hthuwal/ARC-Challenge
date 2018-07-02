package it.unibz.inf.stuffie;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.regex.Pattern;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.UniversalEnglishGrammaticalRelations;

public abstract class ComponentExtractor extends PipelineStep<Boolean, RelationInstance> {

	protected LinkedHashSet<DependencyArc> arcs;

	public ComponentExtractor(Annotation stAnno, Properties stProp, StanfordCoreNLP stPipe, String arcResources,
			Mode... relevantModes) {
		super(stAnno, stProp, stPipe, relevantModes);
		arcs = new LinkedHashSet<DependencyArc>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(arcResources));
			for (String line = br.readLine(); line != null; line = br.readLine()) {
				String x[] = line.split(Pattern.quote(","));
				arcs.add(new DependencyArc(UniversalEnglishGrammaticalRelations.shortNameToGRel.get(x[0]),
						DependencyArc.Direction.valueOf(x[1])));
			}
			br.close();
		} catch (Exception e) {

		}
	}

	public ComponentExtractor(String arcResources) {
		this(null, null, null, arcResources);
	}

}
