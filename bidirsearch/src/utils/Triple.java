package utils;


/**
 * A Triple of the Knowledge Graph.
 * 
 * @author Madhulika Mohanty (madhulikam@cse.iitd.ac.in)
 *
 */
public class Triple {
  Integer subject, predicate, object;

  public Triple (Integer sub, Integer pred, Integer obj) {
    this.subject = sub;
    this.predicate = pred;
    this.object = obj;
  }

  public Integer getSubject() {
    return this.subject;
  }

  public Integer getObject() {
    return this.object;
  }

  public Integer getPredicate() {
    return this.predicate;
  }

  @Override
  public String toString() {
    return "Triple [subject=" + subject + ", predicate=" + predicate + ", object=" + object + "]";
  }
}
