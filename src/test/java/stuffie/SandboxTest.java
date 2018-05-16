package stuffie;

import it.inf.unibz.stuffie.Mode;

public class SandboxTest {
	public static void main(String[] args) {
		for(Class<?> x : Mode.class.getClasses()) {
			System.out.println(x.getSimpleName());
			
			for(Object o : x.getEnumConstants()) {
				System.out.println("\t" + o.toString());
			}
		}
		
		String x = "1-2";
		System.out.println(x.split("-")[1]);
	}
}
