package it.unibz.inf.stuffie;

import java.util.Comparator;

public class RelationArgumentComparator implements Comparator<RelationArgument> {

	@Override
	public int compare(RelationArgument o1, RelationArgument o2) {
		
		if(!o1.isContextDependent() && o2.isContextDependent())
			return -1;
		if(o1.isContextDependent() && !o2.isContextDependent())
			return 1;
		
		return o1.getOwner().compareTo(o2.getOwner());
	}

}
