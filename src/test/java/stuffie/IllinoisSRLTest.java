package stuffie;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.PredicateArgumentView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotationUtilities;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.prepsrl.PrepSRLAnnotator;
import edu.illinois.cs.cogcomp.prepsrl.PrepSRLConfigurator;
import edu.illinois.cs.cogcomp.prepsrl.data.Preprocessor;

public class IllinoisSRLTest {

	public static void main(String[] args) throws AnnotatorException, IOException {
		Annotator annotator = new PrepSRLAnnotator();
		Preprocessor preprocessor = new Preprocessor(PrepSRLConfigurator.defaults());

		 String longSentence =
	                "introducing genes into an afflicted individual for therapeutic purposes : "
	                        + "holds great potential for treating the relatively small number of disorders "
	                        + "traceable to a single defective gene .";
		TextAnnotation ta1 = TextAnnotationUtilities.createFromTokenizedString(longSentence);
		preprocessor.annotate(ta1);
		View view1 = annotator.getView(ta1);
		PredicateArgumentView paView1 = (PredicateArgumentView) view1;
		for (Constituent x : paView1.getPredicates()) {
			for(String key : x.getAttributeKeys()) {
				System.out.println(key + " -> " + x.getAttribute(key) + "\n");
			}
			System.out.println(x.getSurfaceForm() + " -> " + x.getLabel() + "\n");
		}
		
		StringBuilder sb = new StringBuilder();
		Stream<String> lines = Files.lines(Paths.get("resource/eval/100sentences.txt"));
		lines.forEach(line -> {
			sb.append(line).append("\n");
			TextAnnotation ta = TextAnnotationUtilities.createFromTokenizedString(line);
			try {
				preprocessor.annotate(ta);

				View view = annotator.getView(ta);
				PredicateArgumentView paView = (PredicateArgumentView) view;
				for (Constituent x : paView.getPredicates()) {
					if(x.getSurfaceForm().split(" ").length == 1)
						sb.append(x.getSurfaceForm() + " -> " + x.getLabel() + "\n");
				}
			} catch (Exception e) {
				
			} catch (Error e) {
				
			} finally {
				sb.append("\n");
			}
		});
		lines.close();
		
		BufferedWriter bw = new BufferedWriter(new FileWriter("resource/eval/illinois-SRL-res.txt"));
		bw.write(sb.toString());
		bw.flush();
		bw.close();
	}

}
