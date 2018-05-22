package it.unibz.inf.stuffie;

import java.util.Comparator;

import edu.stanford.nlp.ling.IndexedWord;

public class IndexedWordComparator implements Comparator<IndexedWord> {

	IndexedWord head = null;
	boolean preferAfterHead = false;

	public IndexedWordComparator() {}
	
	public IndexedWordComparator(IndexedWord head) {
		this.head = head;
	}
	
	public IndexedWordComparator(IndexedWord head, boolean pah) {
		this.head = head;
		preferAfterHead = pah;
	}

	public int compare(IndexedWord o1, IndexedWord o2) {
		if (head == null)
			return Integer.compare(o1.index(), o2.index());
		else if (!preferAfterHead){
			int ret = Integer.compare(Math.abs(head.index() - o1.index()), Math.abs(head.index() - o2.index()));
			if (ret == 0) {
				if (o1.index() > head.index() && o2.index() < head.index())
					ret = -1;
				else if (o1.index() < head.index() && o2.index() > head.index())
					ret = 1;
			}
			return ret;
		} else {
			if(o1.index() > head.index() && o2.index() < head.index()) {
				return -1;
			} else if(o1.index() < head.index() && o2.index() > head.index()) {
				return 1;
			} else {
				return Integer.compare(o1.index(), o2.index());
			}
			
		}
	}

}
