package it.inf.unibz.stuffie;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.regex.Pattern;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.UniversalEnglishGrammaticalRelations;

public abstract class Expander extends PipelineStep<Boolean, RelationInstance> {

	protected LinkedHashSet<ExpansionArc> expansionArcs;

	public Expander(Annotation stAnno, Properties stProp, StanfordCoreNLP stPipe, Mode... relevantModes) {
		super(stAnno, stProp, stPipe, relevantModes);
	}

	public Expander(Annotation stAnno, Properties stProp, StanfordCoreNLP stPipe, String ruleSrc, Mode... relevantModes) {
		super(stAnno, stProp, stPipe, relevantModes);
		expansionArcs = new LinkedHashSet<ExpansionArc>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(ruleSrc));
			for (String line = br.readLine(); line != null; line = br.readLine()) {
				String x[] = line.split(Pattern.quote(","));
				ExpansionArc ea;
				if (x.length <= 2)
					ea = new ExpansionArc(UniversalEnglishGrammaticalRelations.shortNameToGRel.get(x[0]),
							ExpansionArc.ExpansionType.valueOf(x[1]));
				else {
					ea = new ExpansionArc(UniversalEnglishGrammaticalRelations.shortNameToGRel.get(x[0]),
							ExpansionArc.ExpansionType.valueOf(x[1]), ExpansionArc.TargetPosType.valueOf(x[2]));

					for (int i = 3; i < x.length; i++) {
						ea.addTargetPOS(x[i]);
					}
				}

				expansionArcs.add(ea);

			}
			br.close();
		} catch (Exception e) {

		}
	}

}
