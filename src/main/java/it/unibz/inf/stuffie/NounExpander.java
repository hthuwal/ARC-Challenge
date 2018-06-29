package it.unibz.inf.stuffie;

import java.util.HashMap;
import java.util.Properties;

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
	protected Boolean run(RelationInstance par, int iteration, HashMap<String, RelationArgument> idToComponentMap) {
		RelationArgument subj = par.getSubject();
		RelationArgument obj = par.getObject();

		boolean ret = true;
		if (subj == null && obj != null)
			ret = ret && expandObject(par, obj, obj.getHeadword());
		else if (subj != null && obj == null) {
			ret = ret && expandObject(par, subj, subj.getHeadword());
		} else if (subj == null && obj == null) {
			ret = true;
		} else {
			ret = ret && expandObject(par, obj, obj.getHeadword()) && expandObject(par, subj, subj.getHeadword());
		}

		for (RelationArgument arg : par.getFacets()) {
			ret = ret && expandObject(par, arg, arg.getHeadword());
		}

		return ret;
	}

	private Boolean expandObject(RelationInstance par, RelationArgument arg, IndexedWord current) {
		if (arg.isVerb() || arg.isStatic())
			return true;

		boolean ret = true;
		SemanticGraph depAnno = arg.depAnno;
		for (ExpansionArc arc : expansionArcs) {
			for (IndexedWord iw : depAnno.getChildrenWithReln(current, arc.getRel())) {
				if (arc.getT().equals(ExpansionArc.ExpansionType.C)) {
					arg.addWords(iw);
					ret = ret && expandObject(par, arg, iw);
				} else {
					if (!par.getVerb().isSynthetic() && par.getVerb().getHeadword().index() != iw.index())
						arg.addContext(arg.getSentenceID() + "." + iw.index());
				}
			}
		}

		return true;
	}

}
