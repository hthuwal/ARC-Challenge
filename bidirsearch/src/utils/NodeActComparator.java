package utils;
import java.util.Comparator;

/**
 * Comparator for {@link NodeActVals} activation values.
 * 
 * @author Madhulika Mohanty (madhulikam@cse.iitd.ac.in)
 *
 */
public class NodeActComparator implements Comparator<NodeActVals> {

  @Override
  public int compare(NodeActVals a1, NodeActVals a2) {
    if (a1.getActivationval()>a2.getActivationval())
        {
            return -1;
        }
        if (a1.getActivationval()<a2.getActivationval())
        {
            return 1;
        }
    return 0;
  }

}
