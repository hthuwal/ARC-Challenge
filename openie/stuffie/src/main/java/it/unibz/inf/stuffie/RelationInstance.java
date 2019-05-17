package it.unibz.inf.stuffie;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class RelationInstance implements Comparable<RelationInstance> {

	protected static final String NULL_CMPNT = "<_>";

	private String id;
	private RelationArgument subject;
	private RelationVerb verb;
	private RelationArgument object;
	private TreeSet<RelationArgument> facets;

	public Set<String> getAllRefs() {
		HashSet<String> ret = new HashSet<>();

		if (subject != null && !subject.isStatic() && subject.isVerb()) {
			ret.add(subject.sentenceID + "." + subject.headword.index());
		}

		if (object != null && !object.isStatic() && object.isVerb()) {
			ret.add(object.sentenceID + "." + object.headword.index());
		}

		for (RelationArgument facet : facets) {
			if (facet != null && !facet.isStatic() && facet.isVerb()) {
				ret.add(facet.sentenceID + "." + facet.headword.index());
			}
		}

		return ret;
	}

	public RelationInstance(RelationVerb verb) {
		super();
		RelationComponentComparator rcc = new RelationComponentComparator(verb);
		facets = new TreeSet<RelationArgument>(rcc::compareByPointOfComparison);
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
			sb.append("\n\t").append(facet.toString());
		}

		return sb.toString();
	}

	@Override
	public int compareTo(RelationInstance o) {
		if (!verb.isSynthetic() && o.verb.isSynthetic())
			return -1;
		if (verb.isSynthetic() && !o.verb.isSynthetic())
			return 1;

		return new StringIDComparator().compare(id, o.id);
//		if (ret == 0 && !verb.isSynthetic()) {
//			ret = Integer.compare(verb.headword.index(), o.verb.headword.index());
//			if (ret != 0)
//				return ret;
//		}
//		if (ret == 0) {
//			if (subject != null && o.subject == null)
//				return -1;
//			else if (subject == null && o.subject != null)
//				return 1;
//			else if (subject == null && o.subject == null) {
//				ret = 0;
//			} else {
//				ret = subject.id.compareTo(o.subject.id);
//			}
//		}
//		if (ret == 0) {
//			if (object != null && o.object == null)
//				return -1;
//			else if (object == null && o.object != null)
//				return 1;
//			else if (object == null && o.object == null) {
//				ret = 0;
//			} else {
//				ret = object.id.compareTo(o.object.id);
//			}
//		}
//		if (ret == 0) {
//			ret = id.compareTo(o.getId());
//		}
//		return ret;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the subject
	 */
	public RelationArgument getSubject() {
		return subject;
	}

	/**
	 * @return the verb
	 */
	public RelationVerb getVerb() {
		return verb;
	}

	/**
	 * @return the object
	 */
	public RelationArgument getObject() {
		return object;
	}

	/**
	 * @return the facets
	 */
	public TreeSet<RelationArgument> getFacets() {
		return facets;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @param subject the subject to set
	 */
	public void setSubject(RelationArgument subject) {
		this.subject = subject;
	}

	public void setSubject(RelationComponent subject) {
		setSubject((RelationArgument) subject);
	}

	/**
	 * @param verb the verb to set
	 */
	public void setVerb(RelationVerb verb) {
		this.verb = verb;
	}
	
	public void setVerb(RelationComponent verb) {
		setVerb((RelationVerb) verb);
	}
	
	public void removeVerb(RelationVerb verb) {
		if(this.verb.compareTo(verb) == 0)
			this.verb = null;
	}
	
	public void removeVerb(RelationComponent verb) {
		removeVerb((RelationVerb) verb);
	}

	/**
	 * @param object the object to set
	 */
	public void setObject(RelationArgument object) {
		this.object = object;
	}

	public void setObject(RelationComponent object) {
		this.object = (RelationArgument) object;
	}

	/**
	 * @param facets the facets to set
	 */
	public void setFacets(TreeSet<RelationArgument> facets) {
		this.facets = facets;
	}

	public void addFacet(RelationArgument facet) {
		facets.add(facet);
	}

	public void addFacet(RelationComponent facet) {
		facets.add((RelationArgument) facet);
	}
	
	public void removeSubject(RelationArgument subject) {
		if(this.subject.compareTo(subject) == 0)
			this.subject = null;
	}
	
	public void removeSubject(RelationComponent subject) {
		removeSubject((RelationArgument) subject);
	}
	
	public void removeObject(RelationArgument object) {
		if(this.object.compareTo(object) == 0)
			this.object = null;
	}
	
	public void removeObject(RelationComponent object) {
		removeObject((RelationArgument) object);
	}
	
	public void removeFacet(RelationArgument facet) {
		facets.remove(facet);
	}
	
	public void removeFacet(RelationComponent facet) {
		facets.remove((RelationArgument) facet);
	}

}
