package it.inf.unibz.stuffie;

import java.util.Properties;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class LongSubjectExtractor extends SubjectExtractor {

	public LongSubjectExtractor(Annotation stAnno, Properties stProp, StanfordCoreNLP stPipe) {
		super(stAnno, stProp, stPipe);
	}
	
	@Override
	protected Boolean run(RelationInstance rel, int iteration) {
		
		return true;
	}
	
}
