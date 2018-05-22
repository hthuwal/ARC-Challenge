package it.unibz.inf.stuffie;

import java.util.Properties;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;

public class VerbExpander extends Expander {

	public VerbExpander(Annotation stAnno, Properties stProp, StanfordCoreNLP stPipe, Mode... relevantModes) {
		super(stAnno, stProp, stPipe, "resource/verb_expand.txt", relevantModes);
	}

	@Override
	protected Boolean run(RelationInstance par, int iteration) {
		return expandVerb(par.getVerb(), par.getVerb().getHeadword());
	}
	
	private Boolean expandVerb(RelationVerb arg, IndexedWord current) {
		if(arg.isSynthetic())
			return true;
		
		boolean ret = true;
		SemanticGraph depAnno = arg.depAnno;
		for (ExpansionArc arc : expansionArcs) {
			for (IndexedWord iw : depAnno.getChildrenWithReln(current, arc.getRel())) {
				if (arc.getT().equals(ExpansionArc.ExpansionType.C) && arc.checkTargetPOS(iw.tag())) {
					arg.addWords(iw);
					ret = ret && expandVerb(arg, iw);
				}
			}
		}

		return true;
	}

}
