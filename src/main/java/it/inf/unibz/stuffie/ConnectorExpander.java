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
		
		boolean ret;
		if (subj == null && obj != null)
			ret = expandConnector(obj, obj.getHeadword());
		else if (subj != null && obj == null) {
			ret = expandConnector(subj, subj.getHeadword());
		} else if (subj == null && obj == null) {
			ret = false; 
		} else {
			ret =  expandConnector(subj, subj.getHeadword()) && expandConnector(obj, obj.getHeadword());
		}
		
		for(RelationArgument arg : par.getFacets()) {
			ret = ret && expandConnector(arg, arg.getHeadword());
		}
		
		return ret;
	}

	private Boolean expandConnector(RelationArgument arg, IndexedWord headword) {
		SemanticGraph depAnno = arg.depAnno;
		
		boolean found = false;
		for (ExpansionArc arc : expansionArcs) {
			for (IndexedWord iw : depAnno.getChildrenWithReln(arg.headword, arc.getRel())) {
				arg.setConnector(new RelationArgumentConnector(iw, arg.sentenceID, depAnno));
				arg.words.remove(iw);
				found =true;
				break;
			}
			if(found)
				break;
		}
		
		return true;
	}

}
