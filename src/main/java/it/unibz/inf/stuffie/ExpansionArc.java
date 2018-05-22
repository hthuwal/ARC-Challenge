package it.unibz.inf.stuffie;

import java.util.HashSet;

import edu.stanford.nlp.trees.GrammaticalRelation;

public class ExpansionArc {
	
	protected enum ExpansionType {
		C, R; // Compound or Relation 
	}
	
	protected enum TargetPosType{
		E, R; // Exclusion or Restriction
	}
	
	private ExpansionType t;
	private GrammaticalRelation rel;
	private HashSet<String> targetPOS;
	private TargetPosType targetPOStype;
	
	public ExpansionArc(GrammaticalRelation rel, ExpansionType et) {
		this(rel,et,TargetPosType.E);
	}
	
	public ExpansionArc(GrammaticalRelation rel, ExpansionType et, TargetPosType tpt) {
		this.rel = rel;
		this.t= et;
		targetPOS = new HashSet<String>();
		this.targetPOStype = tpt;
	}

	public ExpansionType getT() {
		return t;
	}

	public GrammaticalRelation getRel() {
		return rel;
	}
	
	public void addTargetPOS(String pos) {
		targetPOS.add(pos);
	}
	
	public boolean checkTargetPOS(String pos) {
		if(targetPOStype == TargetPosType.R) {
			return targetPOS.contains(pos);
		} else
			return !targetPOS.contains(pos);
	}
}
