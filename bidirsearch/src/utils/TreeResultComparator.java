package utils;
import java.util.Comparator;
/**
 * Comparator for {@link MyTreeResult}.
 * 
 * @author Madhulika Mohanty (madhulikam@cse.iitd.ac.in)
 *
 */
public class TreeResultComparator implements Comparator<MyTreeResult> {

  @Override
  public int compare(MyTreeResult a1, MyTreeResult a2) {
    if (a1.getNumEdges()<a2.getNumEdges())
    {
      return -1;
    }
    if (a1.getNumEdges()>a2.getNumEdges())
    {
      return 1;
    }
    return 0;
  }

}