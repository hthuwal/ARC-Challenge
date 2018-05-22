package it.unibz.inf.stuffie;

public interface Mode {
	
	// DONE
	enum DependentSubject implements Mode {
		TRANSFER_ALL, HIDE_ALL, TRANSFER_ADVCL, TRANSFER_XCOMP
	}

	enum Dependent implements Mode {
		COMBINE_CONNECTOR, COMBINE_ALL, SEPARATED
	}
	
	enum ClausalConnection implements Mode {
		AS_FACET, AS_RELATION
	}

	enum FacetConnector implements Mode {
		AS_VERB_COMPOUND, AS_OBJECT_COMPOUND, NO_OBJECT
	}

	enum SyntheticRelation implements Mode {
		ENABLED_NON_REDUNDANT, ENABLED_REDUNDANT, DISABLED
	}

	enum VerbGrammarFix implements Mode {
		ENABLED, DISABLED
	}

	enum DanglingRel implements Mode {
		HIDDEN, SHOWN
	}

	enum ReferenceAnnotation implements Mode {
		ENABLED, DISABLED
	}

	enum ConjunctionDistribution implements Mode {
		AS_FACET_OR_COMPOUND, PARENTAL_DISTRIBUTION
	}

	enum IndirectObject implements Mode {
		AS_VERB_COMPOUND, AS_FACET
	}	
	
	enum MainObject implements Mode {
		STRICT_DOBJ, PROMOTE_FACET_VERB_CONN, PROMOTE_FACET_OBJ_CONN
	}
	
	// DONE
	enum PrintDependenyTree implements Mode {
		DISABLED, ENABLED
	}
	
	enum RelOrdering implements Mode {
		INDEX_BASED, DEPENDENCY_BASED
	}
	
}
