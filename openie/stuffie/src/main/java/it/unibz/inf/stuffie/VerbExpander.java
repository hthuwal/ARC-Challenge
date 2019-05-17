package it.unibz.inf.stuffie;

import java.util.Properties;
import java.util.TreeSet;

import com.google.common.collect.TreeMultimap;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.UniversalEnglishGrammaticalRelations;

public class VerbExpander extends Expander {

	public VerbExpander(Annotation stAnno, Properties stProp, StanfordCoreNLP stPipe, Mode... relevantModes) {
		super(stAnno, stProp, stPipe, "resource/verb_expand.txt", relevantModes);
	}

	public VerbExpander() {
		super("resource/verb_expand.txt");
	}

	@Override
	protected Boolean run(RelationInstance par, int iteration, TreeMultimap<String, RelationComponent> idToComponentMap) {
		
		IndexedWord verbSrc = par.getVerb().headword;
		SemanticGraph depAnno = par.getVerb().getDepAnno();
		
		TreeSet<IndexedWord> cops = new TreeSet<IndexedWord>(new IndexedWordComparator(verbSrc));
		cops.addAll(
				depAnno.getParentsWithReln(verbSrc, UniversalEnglishGrammaticalRelations.shortNameToGRel.get("cop")));
		
		if(cops.isEmpty()) {
			return expandVerb(par.getVerb(), par.getVerb().getHeadword(), idToComponentMap);
		} else {
			return expandVerb(par.getVerb(), cops.first(), idToComponentMap);
		}
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
