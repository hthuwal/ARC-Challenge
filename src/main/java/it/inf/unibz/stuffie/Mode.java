package it.inf.unibz.stuffie;

public interface Mode {

	enum Dependent implements Mode {
		COMBINE_CONNECTOR, COMBINE_ALL, SEPARATED
	}
	
	enum DependentSubject implements Mode {
		TRANSFER_ALL, HIDE_ALL, TRANSFER_ADVCL, TRANSFER_XCOMP
	}

	enum ClausalConnection implements Mode {
		AS_FACET, AS_RELATION
	}

	enum FacetConnector implements Mode {
		AS_VERB_COMPOUND, AS_OBJECT_COMPOUND, NO_OBJECT
	}

	enum SyntheticRelation implements Mode {
		ENABLED, DISABLED
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
		
	}
	
}
