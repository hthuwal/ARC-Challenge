package it.unibz.inf.stuffie;

import java.util.Comparator;

public class RelationInstanceComparator implements Comparator<RelationInstance> {

	@Override
	public int compare(RelationInstance o1, RelationInstance o2) {
		if (!o1.getVerb().isSynthetic() && o2.getVerb().isSynthetic())
			return -1;
		if (o1.getVerb().isSynthetic() && !o2.getVerb().isSynthetic())
			return 1;

		if (!o1.getVerb().isContextDependent() && o2.getVerb().isContextDependent())
			return -1;
		if (o1.getVerb().isContextDependent() && !o2.getVerb().isContextDependent())
			return 1;

		return new StringIDComparator().compare(o1.getId(), o2.getId());
	}

}
