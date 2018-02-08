package it.inf.unibz.stuffie;

import java.util.Properties;
import java.util.TreeSet;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.EnhancedPlusPlusDependenciesAnnotation;
import edu.stanford.nlp.util.CoreMap;

public class VerbExtractor extends PipelineStep<TreeSet<IndexedWord>, CoreMap> {

	public VerbExtractor(Annotation stAnno, Properties stProp, StanfordCoreNLP stPipe) {
		super(stAnno, stProp, stPipe);
	}

	@Override
	protected TreeSet<IndexedWord> run(CoreMap sentence) {
		TreeSet<IndexedWord> res = new TreeSet<IndexedWord>(new IndexedWordComparator());
		
		SemanticGraph depAnno = sentence.get(EnhancedPlusPlusDependenciesAnnotation.class);
		res.addAll(depAnno.getAllNodesByPartOfSpeechPattern("VB|VBD|VBG|VBN|VBP|VBZ"));
		
		return res;
	}

}
