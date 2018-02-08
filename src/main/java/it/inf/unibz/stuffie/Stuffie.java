package it.inf.unibz.stuffie;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.TreeSet;

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class Stuffie {

	private Annotation stAnno;
	private Properties stProp;
	private StanfordCoreNLP stPline;
	
	private VerbExtractor vExt;
	
	
	@SuppressWarnings("rawtypes")
	ArrayList<PipelineStep> steps;
	
	@SuppressWarnings("rawtypes")
	public Stuffie() {
		stProp = new Properties();
		stProp.setProperty("annotators",
				"tokenize,ssplit,pos,lemma,depparse");
		//,ner,mention,coref,entitymentions,natlog,openie
		stAnno = new Annotation("This is a testing sentence.");
		stPline = new StanfordCoreNLP(stProp);
		stPline.annotate(stAnno);
		
		vExt = new VerbExtractor(stAnno, stProp, stPline);
		steps = new ArrayList<PipelineStep>();
		steps.add(vExt);
	}
	
	public String run(String text) {
		stAnno = new Annotation(text);
		stPline = new StanfordCoreNLP(stProp);
		stPline.annotate(stAnno);
		refreshPlines();
		
		StringBuilder sb = new StringBuilder();
		List<CoreMap> sentences = stAnno.get(SentencesAnnotation.class);
		int sentId = 1;
		TreeSet<RelationInstance> rels = new TreeSet<RelationInstance>();
		for (CoreMap sentence : sentences) {
			TreeSet<IndexedWord> verbs = vExt.run(sentence);
			for(IndexedWord verb : verbs) {
				rels.add(new RelationInstance(new RelationVerb(verb, sentId)));
			}
			sentId++;
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
