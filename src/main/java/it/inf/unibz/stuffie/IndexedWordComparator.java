package it.inf.unibz.stuffie;

import java.util.Comparator;

import edu.stanford.nlp.ling.IndexedWord;

public class IndexedWordComparator implements Comparator<IndexedWord> {

	IndexedWord head = null;

	public IndexedWordComparator() {}
	
	public IndexedWordComparator(IndexedWord head) {
		this.head = head;
	}

	public int compare(IndexedWord o1, IndexedWord o2) {
		if (head == null)
			return Integer.compare(o1.index(), o2.index());
		else {
			int ret = Integer.compare(Math.abs(head.index() - o1.index()), Math.abs(head.index() - o2.index()));
			if (ret == 0) {
				if (o1.index() > head.index() && o2.index() < head.index())
					ret = -1;
				else if (o1.index() < head.index() && o2.index() > head.index())
					ret = 1;
			}
			return ret;
		}
	}

}
