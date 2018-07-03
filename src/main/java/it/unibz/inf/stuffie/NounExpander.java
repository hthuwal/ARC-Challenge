package it.unibz.inf.stuffie;

import java.util.Properties;

import com.google.common.collect.TreeMultimap;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;

public class NounExpander extends Expander {

	public NounExpander(Annotation stAnno, Properties stProp, StanfordCoreNLP stPipe, Mode... relevantModes) {
		super(stAnno, stProp, stPipe, "resource/noun_expand.txt", relevantModes);
	}

	public NounExpander() {
		super("resource/noun_expand.txt");
	}

	@Override
	protected Boolean run(RelationInstance par, int iteration,
			TreeMultimap<String, RelationComponent> idToComponentMap) {
		RelationArgument subj = par.getSubject();
		RelationArgument obj = par.getObject();

		boolean ret = true;
		if (subj == null && obj != null)
			ret = ret && expandObject(par, obj, obj.getHeadword(), idToComponentMap);
		else if (subj != null && obj == null) {
			ret = ret && expandObject(par, subj, subj.getHeadword(), idToComponentMap);
		} else if (subj == null && obj == null) {
			ret = true;
		} else {
			ret = ret && expandObject(par, obj, obj.getHeadword(), idToComponentMap)
					&& expandObject(par, subj, subj.getHeadword(), idToComponentMap);
		}

		for (RelationArgument arg : par.getFacets()) {
			ret = ret && expandObject(par, arg, arg.getHeadword(), idToComponentMap);
		}

		return ret;
	}

	private Boolean expandObject(RelationInstance par, RelationArgument arg, IndexedWord current,
			TreeMultimap<String, RelationComponent> idToComponentMap) {
		if (arg.isVerb() || arg.isStatic())
			return true;

		boolean ret = true;
		SemanticGraph depAnno = arg.depAnno;
		for (ExpansionArc arc : expansionArcs) {
			for (IndexedWord iw : depAnno.getChildrenWithReln(current, arc.getRel())) {
				if (arc.getT().equals(ExpansionArc.ExpansionType.C) && arc.checkTargetPOS(iw.tag())) {
					arg.addWords(iw);
					idToComponentMap.put(arg.getSentenceID() + "." + iw.index(), arg);
					ret = ret && expandObject(par, arg, iw, idToComponentMap);
				} else {
					if (!par.getVerb().isSynthetic() && par.getVerb().getHeadword().index() != iw.index()) {
//						arg.setContextDependent(true);
//						arg.addContext(arg.sentenceID + "." + iw.index());
					}
				}
			}
		}

		return true;
	}

}
