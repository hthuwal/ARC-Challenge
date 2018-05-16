package it.inf.unibz.stuffie;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
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
	private LongSubjectExtractor lsExtr;

	private NounExpander nExp;
	private ConnectorExpander cExp;
	private VerbExpander vExp;

	private PipelineStep<?, ?> steps[];

	private Mode[] defModes = { Mode.Dependent.SEPARATED, Mode.DependentSubject.TRANSFER_ALL,
			Mode.ClausalConnection.AS_FACET, Mode.FacetConnector.AS_VERB_COMPOUND, Mode.SyntheticRelation.ENABLED,
			Mode.VerbGrammarFix.DISABLED, Mode.DanglingRel.HIDDEN, Mode.ReferenceAnnotation.ENABLED,
			Mode.ConjunctionDistribution.PARENTAL_DISTRIBUTION, Mode.IndirectObject.AS_VERB_COMPOUND };

	private HashMap<Class<? extends Mode>, Mode> modes = new HashMap<>();
	private HashMap<Class<? extends PipelineStep<?,?>>, Class<?>[]> relevantPlineModes = new HashMap<>(); 

	public void setMode(Mode m) {
		modes.put(m.getClass(), m);
		refreshPlines();
	}
	
	public Stuffie(Mode... customModes) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		for (Mode m : defModes) {
			modes.put(m.getClass(), m);
		}

		for (Mode m : customModes) {
			modes.put(m.getClass(), m);
		}
		
		relevantPlineModes.put(LongSubjectExtractor.class, new Class<?>[] {Mode.DependentSubject.class});

		stProp = new Properties();
		stProp.setProperty("annotators", "tokenize,ssplit,pos,lemma,depparse,ner,mention,coref,entitymentions");
		stAnno = new Annotation("This is a testing sentence.");
		stPline = new StanfordCoreNLP(stProp);
		stPline.annotate(stAnno);

		vExtr = new VerbExtractor(stAnno, stProp, stPline);
		sExtr = new SubjectExtractor(stAnno, stProp, stPline);
		oExtr = new ObjectExtractor(stAnno, stProp, stPline);
		lsExtr = new LongSubjectExtractor(stAnno, stProp, stPline, modes.get(Mode.DependentSubject.class));

		nExp = new NounExpander(stAnno, stProp, stPline);
		cExp = new ConnectorExpander(stAnno, stProp, stPline);
		vExp = new VerbExpander(stAnno, stProp, stPline);

		steps = new PipelineStep[] { vExtr, sExtr, oExtr, lsExtr, nExp, cExp, vExp };
		
		for(PipelineStep step : steps) {
			Class cls = step.getClass();
			Class[] cArgs = new Class[]{Annotation.class, Properties.class, StanfordCoreNLP.class, Mode[].class};
			
			step = (PipelineStep) cls.getDeclaredConstructor(cArgs).newInstance(stAnno, stProp, stPline);
		}
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
		for (RelationInstance relIns : rels) {
			sExtr.run(relIns, iter);
			oExtr.run(relIns, iter);
			iter++;
		}

		if (modes.get(Mode.DependentSubject.class) != Mode.DependentSubject.HIDE_ALL) {
			iter = 1;
			for (RelationInstance relIns : rels) {
				if (relIns.getSubject() == null) {
					lsExtr.run(relIns, iter);
				}
				iter++;
			}
		}

		iter = 1;
		for (RelationInstance relIns : rels) {
			nExp.run(relIns, iter);
			cExp.run(relIns, iter);
			vExp.run(relIns, iter);
			iter++;
		}

		for (RelationInstance relIns : rels) {
			sb.append(relIns.toString() + "\n");
		}

		return sb.toString();
	}

	@SuppressWarnings("rawtypes")
	private void refreshPlines() {
		for (PipelineStep step : steps) {
			step.refresh(stAnno, stProp, stPline);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void main(String[] args)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		Mode[] modes = new Mode[args.length];

		int i = 0;
		for (String arg : args) {
			String[] mode = arg.split("=");

			Class<Enum> cls = (Class<Enum>) Class.forName("it.inf.unibz.stuffie.Mode$" + mode[0]);
			Mode m = (Mode) Enum.valueOf(cls, mode[1]);
			modes[i] = m;
			i++;
		}

//		Stuffie stuffie = new Stuffie(modes);z

		Scanner reader = new Scanner(System.in);
		String text = "";
		while (!text.equals("q")) {
			System.out.println("Enter text to extract, or <Mode=VALUE> to change a mode, or q to quit: ");
			text = reader.nextLine();
			if (text.charAt(0) == '<' && text.charAt(text.length() - 1) == '>' && text.contains("=")) {
				text = text.substring(1, text.length()-1);
				String[] mode = text.split("=");
				Class<Enum> cls = (Class<Enum>) Class.forName("it.inf.unibz.stuffie.Mode$" + mode[0]);
				Mode m = (Mode) Enum.valueOf(cls, mode[1]);
				stuffie.setMode(m);
				System.out.println(m.getClass().getSimpleName() + " changed into " + mode[1]);
			} else {
				System.out.println(stuffie.run(text));
			}
		}
		System.out.print("Bye bye.");
		reader.close();
	}

}
