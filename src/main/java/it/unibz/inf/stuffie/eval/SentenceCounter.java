package it.unibz.inf.stuffie.eval;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.TreeSet;
import java.util.stream.Stream;

import it.unibz.inf.stuffie.Stuffie;

public class SentenceCounter implements Comparable<SentenceCounter> {

	int rels, facets, tokens;
	String sent;

	public SentenceCounter(int rels, int facets, String sent) {
		super();
		this.rels = rels;
		this.facets = facets;
		this.sent = sent;
		tokens = sent.split(" ").length;
	}

	@Override
	public int compareTo(SentenceCounter o) {
		int ret = o.facets - facets;
		if (ret == 0)
			ret = rels - o.rels;
		if (ret == 0)
			ret = o.tokens - tokens;
		if (ret == 0)
			ret = sent.compareTo(o.sent);

		return ret;
	}

	public static void main(String[] args) throws IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException, IOException {

		Stuffie stuffie = new Stuffie();
		TreeSet<SentenceCounter> sentences = new TreeSet<>();
		int[] totalRes = new int[] { 0, 0 };
		int[] counter = { 0 };
		try {
			Stream<String> lines = Files.lines(Paths.get("resource/eval/sentences.txt"));
			lines.forEach(line -> {
				int[] res = stuffie.countRels(line);
				totalRes[0] = totalRes[0] + res[0];
				totalRes[1] = totalRes[1] + res[1];
				sentences.add(new SentenceCounter(res[0], res[1], line));
				counter[0]++;
				if (counter[0] % 100 == 0) {
					System.out.println(counter[0]);
				}
			});
			lines.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println(totalRes[0] + " " + totalRes[1]);
		BufferedWriter bw = new BufferedWriter(new FileWriter("resource/eval/sentences-ordered.txt"));

		for (SentenceCounter sentence : sentences) {
			bw.write(sentence.sent + "\n");
		}
		bw.flush();
		bw.close();

	}
}
