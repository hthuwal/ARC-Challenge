package it.unibz.inf.stuffie;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;

import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class Stuffie {

	private Annotation stAnno;
	private Properties stProp;
	private StanfordCoreNLP stPline;

	private VerbExtractor vExtr = new VerbExtractor();
	private SubjectExtractor sExtr = new SubjectExtractor();
	private ObjectExtractor oExtr = new ObjectExtractor();
	private EnhancedSubjectExtractor esExtr = new EnhancedSubjectExtractor();
	private LongSubjectExtractor lsExtr = new LongSubjectExtractor();

	private SynthRelExtractor srExtr = new SynthRelExtractor();
	
	private NounExpander nExp = new NounExpander();
	private VerbExpander vExp = new VerbExpander();
	private ConnectorExpander cExp = new ConnectorExpander();

	private PipelineStep<?, ?> steps[] = new PipelineStep[] { vExtr, sExtr, oExtr, esExtr, lsExtr, srExtr, nExp, vExp, cExp };

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

		TreeMultimap<String, RelationComponent> idToComponentMap = TreeMultimap.create(Ordering.natural(),
				new RelationComponentComparator()::compareByContextDependency);
		TreeSet<RelationInstance> rels;
		if (modes.get(Mode.RelOrdering.class) == Mode.RelOrdering.INDEX_BASED)
			rels = new TreeSet<RelationInstance>();
		else
			rels = new TreeSet<RelationInstance>(new RelationInstanceComparator());
		List<CoreMap> sentences = stAnno.get(SentencesAnnotation.class);

		runPipeline(true, o -> true, sentences, rels, idToComponentMap, vExtr);
		runPipeline(true, o -> true, sentences, rels, idToComponentMap, sExtr, oExtr);
		runPipeline(modes.get(Mode.DependentSubject.class) != Mode.DependentSubject.HIDE_ALL,
				o -> ((RelationInstance) o).getSubject() == null, sentences, rels, idToComponentMap, esExtr, lsExtr);
		runPipeline(modes.get(Mode.SyntheticRelation.class) != Mode.SyntheticRelation.DISABLED, o -> true, sentences,
				rels, idToComponentMap, srExtr);
		runPipeline(true, o -> true, sentences, rels, idToComponentMap, nExp, vExp, cExp);

		return rels;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	void runPipeline(boolean outerCond, Function<Object, Boolean> innerCond, List<CoreMap> sentences,
			TreeSet<RelationInstance> rels, TreeMultimap<String, RelationComponent> idToComponentMap,
			PipelineStep... steps) {

		Collection col;
		if (steps[0] instanceof Expander || steps[0] instanceof ComponentExtractor)
			col = rels;
		else
			col = sentences;

		int iter = 1;
		if (outerCond) {
			for (Object obj : col) {
				for (PipelineStep step : steps) {
					if (innerCond.apply(obj)) {
						Object ret = step.run(obj, iter, idToComponentMap);
						if (ret instanceof Collection)
							rels.addAll((Collection) ret);
					}
				}
				iter++;
			}
		}
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
