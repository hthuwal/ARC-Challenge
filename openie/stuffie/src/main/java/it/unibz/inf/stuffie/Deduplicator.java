package it.unibz.inf.stuffie;

import java.util.TreeSet;

import com.google.common.collect.TreeMultimap;

import edu.stanford.nlp.ling.IndexedWord;

public class Deduplicator extends PipelineStep<Boolean, RelationInstance> {

	public void run(TreeMultimap<String, RelationComponent> idToComponentMap) {

	}

	@Override
	protected Boolean run(RelationInstance rel, int iteration,
			TreeMultimap<String, RelationComponent> idToComponentMap) {

		if (iteration == 1) {
			TreeSet<RelationComponent> rcs = new TreeSet<>();
			for (String x : idToComponentMap.keySet()) {
				int i = 0;
				for (RelationComponent rc : idToComponentMap.get(x)) {
					if (i == 0) {
						if (!(rc instanceof RelationArgumentConnector))
							break;
					}
					if (i > 0) {
						rcs.add(rc);
					}
					i++;
				}
			}

			for (RelationComponent rc : rcs) {
				if (rc.isVerb())
					continue;

				try {
					for (IndexedWord iw : rc.getWords()) {
						idToComponentMap.remove(rc.getSentenceID() + "." + iw.index(), rc);
					}
					rc.getOwnershipRemovalFunc().accept(rc);
				} catch (Exception e) {

				}
			}
		}

		for (IndexedWord vw : rel.getVerb().getWords()) {
			if (vw.tag().contains("RB") && rel.getObject().getWords().contains(vw)) {
				removeIWFromComponent(rel.getVerb(), vw, idToComponentMap);
			}
		}

		for (RelationArgument facet : rel.getFacets()) {
			for (IndexedWord fw : facet.getWords()) {
				if (fw.tag().contains("RB") && rel.getObject().getWords().contains(fw)) {
					try {
						removeComponentFromOwner(facet, idToComponentMap);
						break;
					} catch (Exception e) {

					}
				}

				for (IndexedWord connw : facet.getConnector().getWords()) {
					if (connw.tag().contains("RB") && rel.getObject().getWords().contains(connw)) {
						try {
							removeComponentFromOwner(facet, idToComponentMap);
							break;
						} catch (Exception e) {

						}
					}
				}
			}
		}

		return true;
	}

}
