package it.unibz.inf.stuffie;

import java.util.HashMap;
import java.util.Properties;
import java.util.function.Consumer;

import com.google.common.collect.TreeMultimap;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public abstract class PipelineStep<R, P> {

	protected Annotation stAnno;
	protected Properties stProp;
	protected StanfordCoreNLP stPipe;
	protected HashMap<Class<? extends Mode>, Mode> modes = new HashMap<>();

	public PipelineStep() {
		this(null, null, null);
	}

	public PipelineStep(Annotation stAnno, Properties stProp, StanfordCoreNLP stPipe, Mode... relevantModes) {
		super();
		refresh(stAnno, stProp, stPipe, relevantModes);

		for (Mode mode : relevantModes) {
			modes.put(mode.getClass(), mode);
		}
	}

	protected void refresh(Annotation stAnno, Properties stProp, StanfordCoreNLP stPipe, Mode... relevantModes) {
		this.stAnno = stAnno;
		this.stProp = stProp;
		this.stPipe = stPipe;

		for (Mode mode : relevantModes) {
			modes.put(mode.getClass(), mode);
		}
	}

	protected void addComponent(RelationInstance rel, RelationComponent ra, IndexedWord iw,
			Consumer<RelationComponent> adderFunction, Consumer<RelationComponent> removalFunction,
			TreeMultimap<String, RelationComponent> idToComponentMap, String relID, boolean contextDependent) {
		ra.setRelativeID(rel.getId() + relID);
		ra.setOwner(rel);
		ra.setOwnershipRemovalFunc(removalFunction);
		ra.setContextDependent(contextDependent);
		adderFunction.accept(ra);
		idToComponentMap.put(ra.getSentenceID() + "." + iw.index(), ra);
		idToComponentMap.put(ra.getRelativeID(), ra);
	}

	protected void removeIWFromComponent(RelationComponent ra, IndexedWord iw,
			TreeMultimap<String, RelationComponent> idToComponentMap) {
		ra.removeWord(iw);
		idToComponentMap.remove(ra.getSentenceID() + "." + iw.index(), ra);
	}

	protected void removeComponentFromOwner(RelationComponent ra,
			TreeMultimap<String, RelationComponent> idToComponentMap) {
		for (IndexedWord iw : ra.getWords()) {
			idToComponentMap.remove(ra.getSentenceID() + "." + iw.index(), ra);
		}
		ra.getOwnershipRemovalFunc().accept(ra);
	}

	protected abstract R run(P param, int iteration, TreeMultimap<String, RelationComponent> idToComponentMap);

}
