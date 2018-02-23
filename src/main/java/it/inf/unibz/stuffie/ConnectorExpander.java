package it.inf.unibz.stuffie;

import java.util.Properties;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;

public class ConnectorExpander extends Expander {

	public ConnectorExpander(Annotation stAnno, Properties stProp, StanfordCoreNLP stPipe) {
		super(stAnno, stProp, stPipe, "resource/connector_expand.txt");
	}

	@Override
	protected Boolean run(RelationInstance par, int iteration) {
		RelationArgument subj = par.getSubject();
		RelationArgument obj = par.getObject();

		boolean ret = true;

		if (subj != null) {
			ret = ret && expandConnector(subj, subj.getHeadword(),
					subj.getChainFromVerb().get(0).rel.getShortName().equals("acl"), false);
		}
		if (obj != null)
			ret = ret && expandConnector(obj, obj.getHeadword(), false, false);

		for (RelationArgument arg : par.getFacets()) {
			ret = ret && expandConnector(arg, arg.getHeadword(), false, true);
		}

		return ret;
	}

	private Boolean expandConnector(RelationArgument arg, IndexedWord headword, boolean isACL, boolean isFacet) {
		SemanticGraph depAnno = arg.depAnno;

		boolean found = false;
		for (ExpansionArc arc : expansionArcs) {
			for (IndexedWord iw : depAnno.getChildrenWithReln(arg.headword, arc.getRel())) {
				if(!isACL)
					arg.setConnector(new RelationArgumentConnector(iw, arg.sentenceID, depAnno));
				arg.words.remove(iw);
				found = true;
				break;
			}
			if (found)
				break;
		}
		
		if(!found && isFacet) {
			arg.setConnector(new RelationArgumentConnector(null, arg.sentenceID, depAnno));
		}

		return true;
	}

}
