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
				ret = ret && expandConnector(subj, subj.getHeadword(),
						subj.getChainFromVerb().get(0).rel.getShortName().equals("acl"), false, idToComponentMap);
			else {
				ret = ret && expandConnector(subj, subj.getHeadword(), false, false, idToComponentMap);
			}
		}
		if (obj != null && !obj.isStatic())
			ret = ret && expandConnector(obj, obj.getHeadword(), false, false, idToComponentMap);

		for (RelationArgument arg : par.getFacets()) {
			ret = ret && expandConnector(arg, arg.getHeadword(), false, true, idToComponentMap);
		}

		return ret;
	}

	private Boolean expandConnector(RelationArgument arg, IndexedWord headword, boolean isACL, boolean isFacet,
			TreeMultimap<String, RelationComponent> idToComponentMap) {
		SemanticGraph depAnno = arg.depAnno;

		boolean found = traverseOneStep(depAnno, arg, headword, isACL, idToComponentMap);
		if (!found) {
			TreeSet<IndexedWord> copulas = new TreeSet<IndexedWord>(new IndexedWordComparator());
			copulas.addAll(depAnno.getParentsWithReln(headword,
					UniversalEnglishGrammaticalRelations.shortNameToGRel.get("cop")));
			if (!copulas.isEmpty()) {
				traverseOneStep(depAnno, arg, copulas.first(), isACL, idToComponentMap);
			}
		}

		if (!found && isFacet) {
			arg.setConnector(new RelationArgumentConnector(null, arg.sentenceID, depAnno));
		}

		return true;
	}

	private Boolean traverseOneStep(SemanticGraph depAnno, RelationArgument arg, IndexedWord headword, boolean isACL,
			TreeMultimap<String, RelationComponent> idToComponentMap) {
		for (ExpansionArc arc : expansionArcs) {
			for (IndexedWord iw : depAnno.getChildrenWithReln(headword, arc.getRel())) {
				if (arc.getT().equals(ExpansionArc.ExpansionType.C) && arc.checkTargetPOS(iw.tag())) {
					if (!isACL) {
						RelationArgumentConnector rac = new RelationArgumentConnector(iw, arg.sentenceID, depAnno);
						StringBuilder relID = new StringBuilder();
						char last = arg.getRelativeID().charAt(arg.getRelativeID().length() - 1);
						if (Character.isDigit(last)) {
							relID.append(arg.getRelativeID().charAt(arg.getRelativeID().length() - 2));
							relID.append(last);
						} else
							relID.append(last);

						addComponent(arg.getOwner(), rac, iw, arg::setConnector, idToComponentMap, relID.toString(),
								false);

					}
					return true;
				}
			}
		}
		return false;
	}

}
