package it.inf.unibz.stuffie;

import java.util.HashMap;
import java.util.Properties;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public abstract class PipelineStep<R,P> {

	protected Annotation stAnno;
	protected Properties stProp;
	protected StanfordCoreNLP stPipe;
	protected HashMap<Class<? extends Mode>, Mode> modes = new HashMap<>();
	
	public PipelineStep(Annotation stAnno, Properties stProp, StanfordCoreNLP stPipe, Mode... relevantModes) {
		super();
		refresh(stAnno, stProp, stPipe);
		
		for(Mode mode : relevantModes) {
			modes.put(mode.getClass(), mode);
		}
	}
	
	protected void refresh(Annotation stAnno, Properties stProp, StanfordCoreNLP stPipe) {
		this.stAnno = stAnno;
		this.stProp = stProp;
		this.stPipe = stPipe;
	}
	
	protected abstract R run(P par, int iteration);
}
