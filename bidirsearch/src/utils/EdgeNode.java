package utils;

/**
 * Class for defining an edge and neighbouring node for any given node.
 * 
 * @author Madhulika Mohanty (madhulikam@cse.iitd.ac.in)
 *
 */
public class EdgeNode {
  Node n;
  Integer annotation;
  
  public EdgeNode(Node n2, Integer ann) {
    this.n=n2;
    this.annotation=ann;
  }

  public void setNode(Node n){
    this.n=n;
  }
  
  public void setAnnotation(Integer ann){
    this.annotation=ann;
  }
  
  public Node getNode(){
    return this.n;
  }
  
  public Integer getAnnotation(){
    return this.annotation;
  }

  @Override
  public String toString() {
    return "EdgeNode [n=" + n + ", annotation=" + annotation + "]";
  }
}
