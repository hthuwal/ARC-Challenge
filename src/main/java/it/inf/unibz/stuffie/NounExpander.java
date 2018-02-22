package it.inf.unibz.stuffie;

import java.util.Properties;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;

public class NounExpander extends Expander {

	public NounExpander(Annotation stAnno, Properties stProp, StanfordCoreNLP stPipe) {
		super(stAnno, stProp, stPipe, "resource/noun_expand.txt");
	}

	@Override
	protected Boolean run(RelationInstance par, int iteration) {
		RelationArgument subj = par.getSubject();
		RelationArgument obj = par.getObject();

		if (subj == null && obj != null)
			return expandObject(par, obj, obj.getHeadword());
		else if (subj != null && obj == null) {
			return expandObject(par, subj, subj.getHeadword());
		} else if (subj == null && obj == null) {
			return false; 
		}
		return expandObject(par, subj, subj.getHeadword()) && expandObject(par, obj, obj.getHeadword());
	}

	private Boolean expandObject(RelationInstance par, RelationArgument arg, IndexedWord current) {
		if (arg.isVerb())
			return true;

		boolean ret = true;
		SemanticGraph depAnno = arg.depAnno;
		for (ExpansionArc arc : expansionArcs) {
			for (IndexedWord iw : depAnno.getChildrenWithReln(current, arc.getRel())) {
				if (arc.getT().equals(ExpansionArc.ExpansionType.C)) {
					arg.addWords(iw);
					ret = ret && expandObject(par, arg, iw);
				} else {
					if(par.getVerb().getHeadword().index() != iw.index())
						arg.addContext(arg.getSentenceID() + "." + iw.index());
				}
			}
		}

		return true;
	}

}
