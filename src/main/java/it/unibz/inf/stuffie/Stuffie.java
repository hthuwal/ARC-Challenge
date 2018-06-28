package it.unibz.inf.stuffie;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class Stuffie {

	private Annotation stAnno;
	private Properties stProp;
	private StanfordCoreNLP stPline;

	private VerbExtractor vExtr = new VerbExtractor(null, null, null);
	private SubjectExtractor sExtr = new SubjectExtractor(null, null, null);
	private ObjectExtractor oExtr = new ObjectExtractor(null, null, null);
	private LongSubjectExtractor lsExtr = new LongSubjectExtractor(null, null, null);

	private NounExpander nExp = new NounExpander(null, null, null);
	private ConnectorExpander cExp = new ConnectorExpander(null, null, null);
	private VerbExpander vExp = new VerbExpander(null, null, null);

	private SynthRelExtractor srExtr = new SynthRelExtractor(null, null, null);

	private PipelineStep<?, ?> steps[] = new PipelineStep[] { vExtr, sExtr, oExtr, lsExtr, nExp, cExp, vExp, srExtr };

	private Mode[] defaultModes = { Mode.Dependent.SEPARATED, Mode.DependentSubject.TRANSFER_ALL,
			Mode.ClausalConnection.AS_FACET, Mode.FacetConnector.AS_VERB_COMPOUND,
			Mode.SyntheticRelation.ENABLED_NON_REDUNDANT, Mode.VerbGrammarFix.DISABLED, Mode.DanglingRel.HIDDEN,
			Mode.ReferenceAnnotation.ENABLED, Mode.ConjunctionDistribution.PARENTAL_DISTRIBUTION,
			Mode.IndirectObject.AS_VERB_COMPOUND, Mode.MainObject.PROMOTE_FACET_VERB_CONN,
			Mode.PrintDependenyTree.ENABLED, Mode.RelOrdering.INDEX_BASED };

	private HashMap<Class<? extends Mode>, Mode> modes = new HashMap<>();
	private HashMap<Class<? extends PipelineStep<?, ?>>, Class<? extends Mode>[]> relevantPlineModes = new HashMap<>();

	public void setMode(Mode m) {
		modes.put(m.getClass(), m);
		refreshPlines();
	}

	public String currentModesInString() {
		StringBuilder sb = new StringBuilder();

		for (Class<? extends Mode> modeCls : modes.keySet()) {
			sb.append(modeCls.getSimpleName() + "=" + modes.get(modeCls).toString() + ", ");
		}
		sb.deleteCharAt(sb.length() - 1).deleteCharAt(sb.length() - 1);

		return sb.toString();
	}

	@SuppressWarnings("unchecked")
	public Stuffie(Mode... customModes) throws IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException {
		for (Mode m : defaultModes) {
			modes.put(m.getClass(), m);
		}

		for (Mode m : customModes) {
			modes.put(m.getClass(), m);
		}

		relevantPlineModes.put(LongSubjectExtractor.class,
				(Class<? extends Mode>[]) new Class<?>[] { Mode.DependentSubject.class });
		relevantPlineModes.put(VerbExtractor.class,
				(Class<? extends Mode>[]) new Class<?>[] { Mode.PrintDependenyTree.class });
		relevantPlineModes.put(SynthRelExtractor.class,
				(Class<? extends Mode>[]) new Class<?>[] { Mode.VerbGrammarFix.class });

		stProp = new Properties();
		stProp.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,dcoref");
		stAnno = new Annotation("This is a testing sentence.");
		stPline = new StanfordCoreNLP(stProp);
		stPline.annotate(stAnno);

		for (PipelineStep<?, ?> step : steps) {
			Class<? extends PipelineStep<?, ?>> cls = (Class<? extends PipelineStep<?, ?>>) step.getClass();
			Mode[] relevantModes = getCurrentModesFromPline(step);
			if (!relevantPlineModes.containsKey(cls))
				relevantPlineModes.put(cls, (Class<? extends Mode>[]) new Class<?>[] {});

			step.refresh(stAnno, stProp, stPline, relevantModes);

		}
	}

	private Mode[] getCurrentModesFromPline(PipelineStep<?, ?> step) {
		Class<? extends Mode>[] relevantModeClasses = relevantPlineModes.get(step.getClass());
		Mode[] relevantModes = {};
		if (relevantModeClasses != null) {
			relevantModes = new Mode[relevantModeClasses.length];
			int i = 0;
			for (Class<? extends Mode> relModeClass : relevantModeClasses) {
				relevantModes[i] = modes.get(relModeClass);
			}
		}
		return relevantModes;
	}

	private TreeSet<RelationInstance> run(String text) {
		stAnno = new Annotation(text);
		stPline = new StanfordCoreNLP(stProp);
		stPline.annotate(stAnno);
		refreshPlines();

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
		for (CoreMap sentence : sentences) {
			if (modes.get(Mode.SyntheticRelation.class) != Mode.SyntheticRelation.DISABLED)
				rels.addAll(srExtr.run(sentence, iter));
			iter++;
		}

		iter = 1;
		for (RelationInstance relIns : rels) {
			nExp.run(relIns, iter);
			cExp.run(relIns, iter);
			vExp.run(relIns, iter);
			iter++;
		}

		return rels;
	}

	public String parseRelation(String text) {
		StringBuilder sb = new StringBuilder();

		for (RelationInstance relIns : run(text)) {
			sb.append(relIns.toString() + "\n");
		}

		return sb.toString();
	}

	public int[] countReds(String text) {
		int res[] = new int[] { 0, 0 };

		HashMap<String, Set<String>> refMap = new HashMap<>();
		for (RelationInstance relIns : run(text)) {
			Set<String> refs = relIns.getAllRefs();
			res[0] = res[0] + refs.size();

			refMap.put(relIns.getId(), refs);
		}

		return res;
	}

	public int[] countRels(String text) {
		int res[] = new int[] { 0, 0 };

		for (RelationInstance relIns : run(text)) {
			res[0] = res[0] + 1;
			res[1] = res[1] + 1;
			res[1] = res[1] + relIns.getFacets().size();
		}

		return res;
	}

	private void refreshPlines() {
		for (PipelineStep<?, ?> step : steps) {
			step.refresh(stAnno, stProp, stPline, getCurrentModesFromPline(step));
		}
	}

}
