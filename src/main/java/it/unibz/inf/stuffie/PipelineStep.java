package it.unibz.inf.stuffie;

import java.util.HashMap;
import java.util.Properties;
import java.util.function.Consumer;

import com.google.common.collect.TreeMultimap;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public abstract class PipelineStep<R,P> {

	protected Annotation stAnno;
	protected Properties stProp;
	protected StanfordCoreNLP stPipe;
	protected HashMap<Class<? extends Mode>, Mode> modes = new HashMap<>();
	
	public PipelineStep() {
		this(null,null,null);
	}
	
	public PipelineStep(Annotation stAnno, Properties stProp, StanfordCoreNLP stPipe, Mode... relevantModes) {
		super();
		refresh(stAnno, stProp, stPipe, relevantModes);
		
		for(Mode mode : relevantModes) {
			modes.put(mode.getClass(), mode);
		}
	}
	
	protected void refresh(Annotation stAnno, Properties stProp, StanfordCoreNLP stPipe, Mode... relevantModes) {
		this.stAnno = stAnno;
		this.stProp = stProp;
		this.stPipe = stPipe;
		
		for(Mode mode : relevantModes) {
			modes.put(mode.getClass(), mode);
		}
	}
	
	protected void addComponent(RelationInstance rel, RelationComponent ra, IndexedWord iw, Consumer<RelationComponent> func,
			TreeMultimap<String, RelationComponent> idToComponentMap, String relID, boolean contextDependent) {
		ra.setRelativeID(rel.getId()+relID);
		ra.setOwner(rel);
		ra.setContextDependent(contextDependent);
		func.accept(ra);
		idToComponentMap.put(ra.getSentenceID() + "." + iw.index(), ra);
		idToComponentMap.put(ra.getRelativeID(), ra);
	}
	
	protected abstract R run(P param, int iteration, TreeMultimap<String, RelationComponent> idToComponentMap);
	
}
