package it.unibz.inf.stuffie;

import java.util.Comparator;

public class StringIDComparator implements Comparator<String> {

	@Override
	public int compare(String o1, String o2) {
		String[] o1s = o1.split("\\.");
		String[] o2s = o2.split("\\.");
		
		int ret = 0;
		for(int i = 0; i < o1s.length; i++) {
			ret = Integer.compare(Integer.parseInt(o1s[i]), Integer.parseInt(o2s[i]));
			if(ret != 0)
				break;
		}
		return ret;
	}
	
	

}
