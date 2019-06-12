package utils;

/**
 * The {@link Node} and its activation value.
 * 
 * @author Madhulika Mohanty (madhulikam@cse.iitd.ac.in)
 *
 */
public class NodeActVals{

  Node n;
  Float act;

  public NodeActVals(){
    this.n=new Node(Integer.MIN_VALUE);
    act=new Float(0.0);
  }

  public NodeActVals(Node nn,Float a){
    this.n=nn;
    act=a;
  }
  public void setNode(Node n)
  {
    this.n=n;
  }

  public void setActivationVal(Float v){
    this.act=v;
  }

  public Node getNode(){
    return n;
  }

  public Float getActivationval(){
    return act;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((act == null) ? 0 : act.hashCode());
    result = prime * result + ((n == null) ? 0 : n.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    NodeActVals other = (NodeActVals) obj;
    if (act == null) {
      if (other.act != null)
        return false;
    } else if (!act.equals(other.act))
      return false;
    if (n == null) {
      if (other.n != null)
        return false;
    } else if (!n.equals(other.n))
      return false;
    return true;
  }

}

