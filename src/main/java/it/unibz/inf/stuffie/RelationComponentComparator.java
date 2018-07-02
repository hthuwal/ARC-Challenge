package it.unibz.inf.stuffie;

import edu.stanford.nlp.ling.IndexedWord;

public class RelationComponentComparator {

	private RelationComponent pointOfComparison;

	public RelationComponentComparator(RelationComponent pointOfComparison) {
		this.pointOfComparison = pointOfComparison;
	}

	public RelationComponentComparator() {

	}

	public int compareByPointOfComparison(RelationComponent ra1, RelationComponent ra2) {
		IndexedWord head = pointOfComparison.getHeadword();
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

	public int compareByContextDependency(RelationComponent ra1, RelationComponent ra2) {
		if (ra1 instanceof RelationVerb && ra2 instanceof RelationArgument)
			return -1;
		if (ra2 instanceof RelationVerb && ra1 instanceof RelationArgument)
			return 1;
		if (ra1 instanceof RelationVerb && ra2 instanceof RelationVerb)
			return ra1.compareTo(ra2);
		if (ra1 instanceof RelationArgument && ra2 instanceof RelationArgument)
			return new RelationArgumentComparator().compare((RelationArgument) ra1, (RelationArgument) ra2);

		return 0;
	}

}
