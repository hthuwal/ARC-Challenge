package it.inf.unibz.stuffie;

import java.util.TreeSet;

public class RelationInstance {
	
	private String id;
	private RelationArgument subject;
	private RelationVerb verb;
	private RelationArgument object;
	private TreeSet<RelationArgument> facets; 
	
	public RelationInstance(RelationVerb verb) {
		super();
		facets = new TreeSet<RelationArgument>(new RelationArgumentComparator(verb));
	}
	
	public RelationInstance(String id, RelationArgument subject, RelationVerb verb, RelationArgument object) {
		super();
		this.id = id;
		this.subject = subject;
		this.verb = verb;
		this.object = object;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(id).append(":\t");
		sb.append(subject.toString());
		sb.append(verb.toString());
		sb.append(object.toString());
		
		for(RelationArgument facet : facets) {
			sb.append("\n\t").append(facet);
		}
		
		return sb.toString();
	}
}
