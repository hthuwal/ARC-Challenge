package stuffie;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

public class RandomizerTest {
	
	public static void main(String[] args) throws IOException {
		
		List<Integer> rands = new ArrayList<Integer>();
		ThreadLocalRandom.current().ints(0, 11000).distinct().limit(100).forEach(rands::add);
		
		List<String> sents = new ArrayList<String>();
		Stream<String> lines = Files.lines(Paths.get("resource/eval/sentences-ordered.txt"));
		lines.forEach(line -> {
			sents.add(line);
		});
		lines.close();
		
		BufferedWriter bw = new BufferedWriter(new FileWriter("resource/eval/100sentences.txt"));
		
		for(Integer index : rands) {
			bw.write(sents.get(index) + "\n");
		}
		bw.flush();
		bw.close();
	}
	
}
