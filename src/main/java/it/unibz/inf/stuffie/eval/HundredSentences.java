package it.unibz.inf.stuffie.eval;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

import it.unibz.inf.stuffie.Stuffie;

public class HundredSentences {

	public static void main(String[] args) throws IOException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		Stuffie stuffie = new Stuffie();
		StringBuilder sb = new StringBuilder();
		Stream<String> lines = Files.lines(Paths.get("resource/eval/100sentences.txt"));
		lines.forEach(line -> {
			sb.append(line).append("\n");
			sb.append(stuffie.parseRelation(line));
			sb.append("\n");
		});
		lines.close();
		
		BufferedWriter bw = new BufferedWriter(new FileWriter("resource/eval/stuffie-res.txt"));
		bw.write(sb.toString());
		bw.flush();
		bw.close();
	}
	
}
