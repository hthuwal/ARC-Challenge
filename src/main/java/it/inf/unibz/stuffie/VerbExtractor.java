package it.inf.unibz.stuffie;

import java.util.Properties;
import java.util.TreeSet;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.BasicDependenciesAnnotation;
import edu.stanford.nlp.util.CoreMap;

public class VerbExtractor extends PipelineStep<TreeSet<RelationInstance>, CoreMap> {

	public VerbExtractor(Annotation stAnno, Properties stProp, StanfordCoreNLP stPipe) {
		super(stAnno, stProp, stPipe);
	}

	@Override
	protected TreeSet<RelationInstance> run(CoreMap sentence, int sentenceNum) {
		TreeSet<RelationInstance> res = new TreeSet<RelationInstance>();
		
		SemanticGraph depAnno = sentence.get(BasicDependenciesAnnotation.class);
		for(IndexedWord verb : depAnno.getAllNodesByPartOfSpeechPattern("VB|VBD|VBG|VBN|VBP|VBZ")) {
			res.add(new RelationInstance(new RelationVerb(verb, sentenceNum, depAnno)));
		}
		
		return res;
	}

}
