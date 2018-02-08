package it.inf.unibz.stuffie;

import java.util.TreeSet;

public class RelationInstance implements Comparable<RelationInstance> {

	protected static final String NULL_CMPNT = "<_>";

	private String id;
	private RelationArgument subject;
	private RelationVerb verb;
	private RelationArgument object;
	private TreeSet<RelationArgument> facets;

	public RelationInstance(RelationVerb verb) {
		super();
		facets = new TreeSet<RelationArgument>(new RelationComponentComparator(verb));
		this.verb = verb;
		this.id = verb.id;
	}

	public RelationInstance(RelationArgument subject, RelationVerb verb, RelationArgument object) {
		super();
		this.subject = subject;
		this.verb = verb;
		this.object = object;
		this.id = verb.id;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append(id).append(":\t");
		if (subject != null)
			sb.append(subject.toString());
		else
			sb.append(NULL_CMPNT).append("; ");

		sb.append(verb.toString());

		if (object != null)
			sb.append(object.toString());
		else
			sb.append("; ").append(NULL_CMPNT).append("; ");

		for (RelationArgument facet : facets) {
			sb.append("\n\t").append(facet);
		}

		return sb.toString();
	}

	@Override
	public int compareTo(RelationInstance o) {
		int ret = id.compareTo(o.id);
		if (ret == 0) {
			if (subject != null && o.subject == null)
				return -1;
			else if (subject == null && o.subject != null)
				return 1;
			else if (subject == null && o.subject == null) {
				ret = 0;
			} else {
				ret = subject.id.compareTo(o.subject.id);
			}
		}
		if (ret == 0) {
			if (object != null && o.object == null)
				return -1;
			else if (object == null && o.object != null)
				return 1;
			else if (object == null && o.object == null) {
				ret = 0;
			} else {
				ret = object.id.compareTo(o.object.id);
			}
		}
		return ret;
	}
}
