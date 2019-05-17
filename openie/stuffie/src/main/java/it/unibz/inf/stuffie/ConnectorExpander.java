package it.unibz.inf.stuffie;

import java.util.Properties;
import java.util.TreeSet;

import com.google.common.collect.TreeMultimap;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.UniversalEnglishGrammaticalRelations;

public class ConnectorExpander extends Expander {

	public ConnectorExpander(Annotation stAnno, Properties stProp, StanfordCoreNLP stPipe, Mode... relevantModes) {
		super(stAnno, stProp, stPipe, "resource/connector_expand.txt", relevantModes);
	}

	public ConnectorExpander() {
		super("resource/connector_expand.txt");
	}

	@Override
	protected Boolean run(RelationInstance par, int iteration,
			TreeMultimap<String, RelationComponent> idToComponentMap) {
		RelationArgument subj = par.getSubject();
		RelationArgument obj = par.getObject();

		boolean ret = true;

		if (subj != null && !subj.isStatic()) {
			if (!subj.getChainFromVerb().isEmpty())
				ret = ret && extractConnector(subj, subj.getHeadword(),
						subj.getChainFromVerb().get(0).rel.getShortName().equals("acl"), false, "s", idToComponentMap);
			else {
				ret = ret && extractConnector(subj, subj.getHeadword(), false, false, "s", idToComponentMap);
			}
		}
		if (obj != null && !obj.isStatic())
			ret = ret && extractConnector(obj, obj.getHeadword(), false, false, "o", idToComponentMap);

		int facetCount = 1;
		for (RelationArgument arg : par.getFacets()) {
			ret = ret && extractConnector(arg, arg.getHeadword(), false, true, "f" + facetCount, idToComponentMap);
			facetCount++;
		}

		return ret;
	}

	private Boolean extractConnector(RelationArgument arg, IndexedWord headword, boolean isACL, boolean isFacet,
			String relID, TreeMultimap<String, RelationComponent> idToComponentMap) {
		SemanticGraph depAnno = arg.depAnno;

		TreeSet<IndexedWord> copulas = new TreeSet<IndexedWord>(new IndexedWordComparator());
		copulas.addAll(
				depAnno.getParentsWithReln(headword, UniversalEnglishGrammaticalRelations.shortNameToGRel.get("cop")));
		boolean found;
		if (copulas.isEmpty()) {
			found = traverseOneStep(depAnno, arg, headword, isACL, false, relID, idToComponentMap);
		} else {
			found = traverseOneStep(depAnno, arg, copulas.first(), isACL, true, relID, idToComponentMap);
		}

		if (!found) {
			TreeSet<IndexedWord> conjunctions = new TreeSet<IndexedWord>(new IndexedWordComparator(headword));
			conjunctions.addAll(depAnno.getParentsWithReln(headword,
					UniversalEnglishGrammaticalRelations.shortNameToGRel.get("conj")));
			if (!conjunctions.isEmpty()) {
				IndexedWord headOfConj = conjunctions.first();
				IndexedWord cc = depAnno.getChildWithReln(headOfConj,
						UniversalEnglishGrammaticalRelations.COORDINATION);
				RelationArgumentConnector rac = new RelationArgumentConnector(cc, arg.sentenceID, depAnno);
				expandConnector(rac, idToComponentMap);
				addComponent(arg.getOwner(), rac, cc, arg::setConnector, arg::removeConnector, idToComponentMap,
						relID + "c", false);
				found = true;
			}
		}

		if (!found && isFacet) {
			arg.setConnector(new RelationArgumentConnector(null, arg.sentenceID, depAnno));
		}

		return true;
	}

	private Boolean traverseOneStep(SemanticGraph depAnno, RelationArgument arg, IndexedWord headword, boolean isACL,
			boolean isCopular, String relID, TreeMultimap<String, RelationComponent> idToComponentMap) {
		TreeSet<IndexedWord> candidates = new TreeSet<IndexedWord>(
				new IndexedWordComparator(arg.getOwner().getVerb().headword));
		for (ExpansionArc arc : expansionArcs) {
			for (IndexedWord iw : depAnno.getChildrenWithReln(headword, arc.getRel())) {
				if (arc.getT().equals(ExpansionArc.ExpansionType.C) && arc.checkTargetPOS(iw.tag())) {
					if (!isACL && ((!isCopular && iw != null && arg.getOwner().getVerb() != null
							&& arg.getOwner().getVerb().headword != null
							&& iw.index() > arg.getOwner().getVerb().headword.index())
							|| (isCopular && iw != null && iw.tag() != null && !iw.tag().equals("RB")))) {
						candidates.add(iw);
//						if (idToComponentMap.containsKey(arg.getSentenceID() + "." + iw.index())) {
//							NavigableSet<RelationComponent> rcs = idToComponentMap.get(arg.getSentenceID() + "." + iw.index());
//							boolean found = false;
//							for(RelationComponent rc : rcs) {
//								if(rc instanceof RelationArgumentConnector) {
//									found = true;
//									break;
//								}
//							}
//							if(!found)
//								candidates.add(iw);
//						} else {
//							candidates.add(iw);
//						}
					}
				}
			}
		}

		if (!candidates.isEmpty()) {
			IndexedWord iw = candidates.first();
			RelationArgumentConnector rac = new RelationArgumentConnector(iw, arg.sentenceID, depAnno);
			expandConnector(rac, idToComponentMap);
			addComponent(arg.getOwner(), rac, iw, arg::setConnector, arg::removeConnector, idToComponentMap,
					relID + "c", false);
			removeIWFromComponent(arg, iw, idToComponentMap);
			idToComponentMap.put(rac.getSentenceID() + "." + iw.index(), rac);
			return true;
		}
		return false;
	}

	private void expandConnector(RelationArgumentConnector rac,
			TreeMultimap<String, RelationComponent> idToComponentMap) {
		SemanticGraph depAnno = rac.depAnno;
		IndexedWord headword = rac.getHeadword();
		IndexedWord next = depAnno.getNodeByIndex(headword.index() + 1);
		if (next.tag().equals("WRB")) {
			expand(next, rac, idToComponentMap);
		}
	}

	private void expand(IndexedWord head, RelationArgumentConnector rac,
			TreeMultimap<String, RelationComponent> idToComponentMap) {
		rac.addWords(head);
		idToComponentMap.put(rac.getSentenceID() + "." + head.index(), rac);
		SemanticGraph depAnno = rac.depAnno;
		for (IndexedWord par : depAnno.getParentsWithReln(head,
				UniversalEnglishGrammaticalRelations.ADVERBIAL_MODIFIER)) {
			if (!rac.getWords().contains(par))
				expand(par, rac, idToComponentMap);
		}
		for (IndexedWord par : depAnno.getChildrenWithReln(head,
				UniversalEnglishGrammaticalRelations.ADVERBIAL_MODIFIER)) {
			if (!rac.getWords().contains(par))
				expand(par, rac, idToComponentMap);
		}
	}

}
