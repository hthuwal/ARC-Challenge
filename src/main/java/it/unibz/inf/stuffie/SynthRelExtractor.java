package it.unibz.inf.stuffie;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Properties;
import java.util.TreeSet;
import java.util.stream.Stream;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.BasicDependenciesAnnotation;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.util.CoreMap;

public class SynthRelExtractor extends PipelineStep<TreeSet<RelationInstance>, CoreMap> {

	protected HashSet<String> NPSequencePOS = new HashSet<>();

	public SynthRelExtractor(Annotation stAnno, Properties stProp, StanfordCoreNLP stPipe, Mode... relevantModes) {
		super(stAnno, stProp, stPipe, relevantModes);

		try {
			Stream<String> lines = Files.lines(Paths.get("resource/np_pos.txt"));
			lines.forEach(line -> {
				NPSequencePOS.add(line);
			});
			lines.close();
		} catch (Exception e) {

		}
	}

	@Override
	protected TreeSet<RelationInstance> run(CoreMap sentence, int sentenceNum) {
		TreeSet<RelationInstance> res = new TreeSet<RelationInstance>();

		SemanticGraph depAnno = sentence.get(BasicDependenciesAnnotation.class);
		TreeSet<SemanticGraphEdge> apposEdges = new TreeSet<>();
		TreeSet<IndexedWord> headNouns = new TreeSet<>();

		for (IndexedWord root : depAnno.getRoots()) {
			collectApposesAndNouns(null, root, apposEdges, headNouns, depAnno);
		}

		for (SemanticGraphEdge e : apposEdges) {
			RelationInstance ri = new RelationInstance(
					new RelationVerb("be", sentenceNum, depAnno, e.getTarget().index()));
			RelationArgument subj = new RelationArgument(e.getSource(), sentenceNum, depAnno, true);
			RelationArgument obj = new RelationArgument(e.getTarget(), sentenceNum, depAnno, false);
			ri.setSubject(subj);
			ri.setObject(obj);
			res.add(ri);
		}

		OUTER:
		for (IndexedWord noun : headNouns) {
			TreeSet<IndexedWord> subtrees = new TreeSet<IndexedWord>(new IndexedWordComparator());
			for(IndexedWord iw : depAnno.descendants(noun)) {
				if(!NPSequencePOS.contains(iw.tag()))
					continue OUTER;
				subtrees.add(iw);
			}
			TreeSet<IndexedWord> subjects = new TreeSet<IndexedWord>(new IndexedWordComparator());
			TreeSet<IndexedWord> objects = new TreeSet<IndexedWord>(new IndexedWordComparator());

			IndexedWord last = subtrees.pollLast();
			String lastNER = last.ner();
			if (lastNER.equals("0"))
				continue;
			subjects.add(last);
			while (!subtrees.isEmpty()) {
				IndexedWord cur = subtrees.pollLast();
				if (cur.ner().equals(lastNER))
					subjects.add(cur);
				else {
					lastNER = "";
					if(cur.tag().equals("DT") && cur.originalText().equals("a"))
						continue;
					objects.add(cur);
				}
			}
			
			
			
			if (!subjects.isEmpty() && !objects.isEmpty()) {
	
				if(subjects.first().tag().startsWith("IN") || subjects.last().tag().startsWith("IN"))
					continue;
				if(objects.first().tag().startsWith("IN") || objects.last().tag().startsWith("IN"))
					continue;
				
				RelationArgument subj = new RelationArgument(last, subjects, sentenceNum, depAnno, true);
				RelationArgument obj = new RelationArgument(objects.last(), objects, sentenceNum, depAnno, false);

				RelationInstance ri = new RelationInstance(
						new RelationVerb("be", sentenceNum, depAnno, objects.last().index()));
				ri.setSubject(subj);
				ri.setObject(obj);
				res.add(ri);
			}
		}

		return res;
	}

	private void collectApposesAndNouns(IndexedWord prevNode, IndexedWord curNode,
			TreeSet<SemanticGraphEdge> apposEdges, TreeSet<IndexedWord> headNouns, SemanticGraph depAnno) {
		for (IndexedWord child : depAnno.getChildren(curNode)) {
			SemanticGraphEdge edge = depAnno.getEdge(curNode, child);
			if (edge.getRelation().getShortName().equals("appos")) {
				apposEdges.add(edge);
			}

			if (curNode.tag().startsWith("NN")) {
				if (prevNode == null || !prevNode.tag().startsWith("NN")) {
					headNouns.add(curNode);
				}
			}

			collectApposesAndNouns(curNode, child, apposEdges, headNouns, depAnno);
		}
	}

}
