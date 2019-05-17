package it.unibz.inf.stuffie.eval;

import java.util.function.Consumer;

public class LambdaExSandbox {
	
	public static void main(String[] args) {
		testConsume(LambdaExSandbox::test, "A");
	}
	
	public static void test(String x) {
		System.out.println(x.toLowerCase());
	}
	
	public static void testConsume(Consumer<String> c, String x) {
		c.accept(x);
	}
	
}
