package it.unibz.inf.stuffie;

import java.util.Properties;

import com.google.common.collect.TreeMultimap;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;

public class VerbExpander extends Expander {

	public VerbExpander(Annotation stAnno, Properties stProp, StanfordCoreNLP stPipe, Mode... relevantModes) {
		super(stAnno, stProp, stPipe, "resource/verb_expand.txt", relevantModes);
	}

	public VerbExpander() {
		super("resource/verb_expand.txt");
	}

	@Override
	protected Boolean run(RelationInstance par, int iteration, TreeMultimap<String, RelationComponent> idToComponentMap) {
		return expandVerb(par.getVerb(), par.getVerb().getHeadword(), idToComponentMap);
	}
	
	private Boolean expandVerb(RelationVerb arg, IndexedWord current, TreeMultimap<String, RelationComponent> idToComponentMap) {
		if(arg.isSynthetic())
			return true;
		
		boolean ret = true;
		SemanticGraph depAnno = arg.depAnno;
		for (ExpansionArc arc : expansionArcs) {
			for (IndexedWord iw : depAnno.getChildrenWithReln(current, arc.getRel())) {
				if (arc.getT().equals(ExpansionArc.ExpansionType.C) && arc.checkTargetPOS(iw.tag())) {
					arg.addWords(iw);
					idToComponentMap.put(arg.getSentenceID() + "." + iw.index(), arg);
					ret = ret && expandVerb(arg, iw, idToComponentMap);
				}
			}
		}

		return true;
	}

}
