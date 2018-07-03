package it.unibz.inf.stuffie;

import java.util.Properties;
import java.util.TreeSet;

import com.google.common.collect.TreeMultimap;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.BasicDependenciesAnnotation;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.EnhancedPlusPlusDependenciesAnnotation;
import edu.stanford.nlp.trees.UniversalEnglishGrammaticalRelations;
import edu.stanford.nlp.util.CoreMap;

public class VerbExtractor extends PipelineStep<TreeSet<RelationInstance>, CoreMap> {

	public VerbExtractor(Annotation stAnno, Properties stProp, StanfordCoreNLP stPipe, Mode... relevantModes) {
		super(stAnno, stProp, stPipe, relevantModes);
	}

	public VerbExtractor() {
		super();
	}

	@Override
	protected TreeSet<RelationInstance> run(CoreMap sentence, int sentenceNum,
			TreeMultimap<String, RelationComponent> idToComponentMap) {
		TreeSet<RelationInstance> res = new TreeSet<RelationInstance>();

		SemanticGraph depAnno = sentence.get(BasicDependenciesAnnotation.class);
		SemanticGraph depAnnoEnch = sentence.get(EnhancedPlusPlusDependenciesAnnotation.class);

		if (modes.get(Mode.PrintDependenyTree.class) == Mode.PrintDependenyTree.ENABLED)
			depAnno.prettyPrint();
		for (IndexedWord verb : depAnno.getAllNodesByPartOfSpeechPattern("VB|VBD|VBG|VBN|VBP|VBZ")) {
			if (depAnno.getParentsWithReln(verb, UniversalEnglishGrammaticalRelations.shortNameToGRel.get("aux"))
					.isEmpty()
					&& depAnno.getParentsWithReln(verb,
							UniversalEnglishGrammaticalRelations.shortNameToGRel.get("auxpass")).isEmpty()) {
				RelationVerb rv = new RelationVerb(verb, sentenceNum, depAnno, depAnnoEnch);
				RelationInstance rel = new RelationInstance(rv);
				res.add(rel);
				addComponent(rel, rv, verb, rel::setVerb, idToComponentMap, "v", rv.isContextDependent());
			}

		}

		return res;
	}

}
