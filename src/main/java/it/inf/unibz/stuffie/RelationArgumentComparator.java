package it.inf.unibz.stuffie;

import java.util.Comparator;

import edu.stanford.nlp.ling.IndexedWord;

public class RelationArgumentComparator implements Comparator<RelationArgument> {

	private RelationVerb verb;
	
	public RelationArgumentComparator(RelationVerb verb) {
		this.verb = verb;
	}

	@Override
	public int compare(RelationArgument ra1, RelationArgument ra2) {
		IndexedWord head = verb.getHeadword();
		IndexedWord o1 = ra1.getHeadword();
		IndexedWord o2 = ra2.getHeadword();
		
		int ret = Integer.compare(Math.abs(head.index() - o1.index()), Math.abs(head.index() - o2.index()));
		if (ret == 0) {
			if (o1.index() > head.index() && o2.index() < head.index())
				ret = -1;
			else if (o1.index() < head.index() && o2.index() > head.index())
				ret = 1;
		}
		return ret;
	}

}
