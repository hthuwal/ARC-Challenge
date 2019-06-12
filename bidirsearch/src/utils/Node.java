package utils;
/**
 * Class for a node in the graph having an {@link Integer} id.
 * 
 * @author Madhulika Mohanty (madhulikam@cse.iitd.ac.in)
 *
 */
public class Node implements Comparable<Node>{
  
  public static final Node ANY = new Node(Integer.MIN_VALUE);
  Integer id;
  
  public Node(int id) {
    this.id = id;
  }

  public Integer getId() {
    return id;
  }

  @Override
  public int compareTo(Node nOther) {
    return this.id.compareTo(nOther.id);
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
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
    Node other = (Node) obj;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "Node [id=" + id + ", label=" + NametoNumMap.numToName.get(id) + "]";
  }
}
