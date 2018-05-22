package stuffie;

import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.BasicDependenciesAnnotation;
import edu.stanford.nlp.util.CoreMap;
import it.unibz.inf.stuffie.Mode;

public class SandboxTest {
	public static void main(String[] args) {
		for (Class<?> x : Mode.class.getClasses()) {
			System.out.println(x.getSimpleName());

			for (Object o : x.getEnumConstants()) {
				System.out.println("\t" + o.toString());
			}
		}

		String x = "1-2";
		System.out.println(x.split("-")[1]);

		Annotation stAnno;
		Properties stProp;
		StanfordCoreNLP stPline;

		stProp = new Properties();
		stProp.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,dcoref");
		stAnno = new Annotation("US President Donald Trump is bald.");
		stPline = new StanfordCoreNLP(stProp);
		stPline.annotate(stAnno);

		List<CoreMap> sents = stAnno.get(SentencesAnnotation.class);
		for (CoreMap sent : sents) {
			SemanticGraph depAnno = sent.get(BasicDependenciesAnnotation.class);
			for(IndexedWord iw : depAnno.descendants(depAnno.getFirstRoot())) {
				System.out.print(iw.originalText() + ' ');
			}

		}
	}
}
