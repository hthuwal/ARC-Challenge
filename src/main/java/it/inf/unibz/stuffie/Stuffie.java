package it.inf.unibz.stuffie;

import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.TreeSet;

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class Stuffie {

	private Annotation stAnno;
	private Properties stProp;
	private StanfordCoreNLP stPline;
	
	private VerbExtractor vExtr;
	private SubjectExtractor sExtr;
	private ObjectExtractor oExtr;
	
	private NounExpander nExp;
	private ConnectorExpander cExp;
	private VerbExpander vExp;
	
	enum DepdendentMode {
		COMBINED, SEPARATED_SUBJ_TRANSFERED, SEPARATED_SUBJ_HIDDEN
	}
	
	enum ClausalConnectorMode {
		AS_FACET, AS_RELATION
	}
	
	enum SyntheticRelationMode {
		ENABLED, DISABLED
	}
	
	enum VerbGrammarFix {
		ENABLED, DISABLED
	}
	
	enum DanglingRelMode {
		HIDDEN, SHOWN
	}
	
	enum ReferenceAnnotation {
		ENABLED, DISABLED
	}
	
	enum ConjunctionDistribution {
		AS_FACET, PARENTAL_DISTRIBUTION
	}
	
	@SuppressWarnings("rawtypes")
	private PipelineStep steps[];
	public Stuffie() {
		stProp = new Properties();
		stProp.setProperty("annotators",
				"tokenize,ssplit,pos,lemma,depparse,ner,mention,coref,entitymentions");
		stAnno = new Annotation("This is a testing sentence.");
		stPline = new StanfordCoreNLP(stProp);
		stPline.annotate(stAnno);
		
		vExtr = new VerbExtractor(stAnno, stProp, stPline);
		sExtr = new SubjectExtractor(stAnno, stProp, stPline);
		oExtr = new ObjectExtractor(stAnno, stProp, stPline);
		
		nExp = new NounExpander(stAnno, stProp, stPline);
		cExp = new ConnectorExpander(stAnno, stProp, stPline);
		vExp = new VerbExpander(stAnno, stProp, stPline);
		
		steps = new PipelineStep[] {vExtr, sExtr, oExtr, nExp, cExp, vExp};
	}
	
	public String run(String text) {
		stAnno = new Annotation(text);
		stPline = new StanfordCoreNLP(stProp);
		stPline.annotate(stAnno);
		refreshPlines();
		
		StringBuilder sb = new StringBuilder();
		List<CoreMap> sentences = stAnno.get(SentencesAnnotation.class);
		int iter = 1;
		TreeSet<RelationInstance> rels = new TreeSet<RelationInstance>();
		for (CoreMap sentence : sentences) {
			rels.addAll(vExtr.run(sentence, iter));
			iter++;
		}
		
		iter = 1;
		for(RelationInstance relIns : rels) {
			sExtr.run(relIns, iter);
			oExtr.run(relIns, iter);
			nExp.run(relIns, iter);
			cExp.run(relIns, iter);
			vExp.run(relIns, iter);
			iter++;
		}
		
		for(RelationInstance relIns : rels) {
			sb.append(relIns.toString() + "\n");
		}
		
		return sb.toString();
	}
	
	@SuppressWarnings("rawtypes")
	private void refreshPlines() {
		for(PipelineStep step : steps) {
			step.refresh(stAnno, stProp, stPline);
		}
	}
	
	public static void main(String[] args) {
		Stuffie stuffie = new Stuffie();
		
		Scanner reader = new Scanner(System.in); 
		String text = "";
		while (!text.equals("q")) {
			System.out.println("Enter text to extract (or q to quit): ");
			text = reader.nextLine();
			System.out.println(stuffie.run(text));
		}
		System.out.print("Bye bye.");
		reader.close();
	}
	
	
	
}
