package utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * The result tree generated from keyword search.
 * 
 * @author Madhulika Mohanty (madhulikam@cse.iitd.ac.in)
 *
 */
public class MyTreeResult implements Comparable<MyTreeResult>{
  HashMap<Node,ArrayList<EdgeNode>> tree; // Map of nodes to its edges and neighbouring nodes.
  Node root; // The root of the tree.
  Integer numEdges;
  Set<Node> nodes;
  Set<Integer> edges;
  public MyTreeResult(){
    this.tree=new HashMap<Node,ArrayList<EdgeNode>>();
    this.root=null;
    this.numEdges=0;
    this.nodes=new HashSet<Node>();
    this.edges=new HashSet<Integer>();
  }
  
  public void addEdge(Node n1, Integer ann,Node n2){
    if(tree.containsKey(n1)){
      ArrayList<EdgeNode> en=this.tree.get(n1);
      for(int i=0;i<en.size();i++){
        EdgeNode enTmp=en.get(i);
        if(enTmp.getNode().equals(n2)&&enTmp.getAnnotation().equals(ann))
          return;
      }
      en.add(new EdgeNode(n2,ann));
      numEdges++;
    }
    else{
      ArrayList<EdgeNode> en=new ArrayList<EdgeNode>();
      en.add(new EdgeNode(n2,ann));
      this.tree.put(n1, en);
      numEdges++;
    }
    this.nodes.add(n1);
    this.nodes.add(n2);
    this.edges.add(ann);
  }

  public void setRoot(Node n){
    this.root=n;
  }
  
  public void setNumEdges(Integer n){
    this.numEdges=n;
  }
  
  public Integer getNumEdges(){
    return this.numEdges;
  }
  
  public Node getRoot(){
    return this.root;
  }
  public HashMap<Node,ArrayList<EdgeNode>> getResult() {
    return this.tree;
  }

  @Override
  public int compareTo(MyTreeResult o) {
    if (this.getNumEdges()<o.getNumEdges())
    {
      return -1;
    }
    if (this.getNumEdges()>o.getNumEdges())
    {
      return 1;
    }
    return 0;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((numEdges == null) ? 0 : numEdges.hashCode());
    result = prime * result + ((root == null) ? 0 : root.hashCode());
    result = prime * result + ((tree == null) ? 0 : tree.hashCode());
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
    MyTreeResult other = (MyTreeResult) obj;
    if (numEdges == null) {
      if (other.numEdges != null)
        return false;
    } else if (!numEdges.equals(other.numEdges))
      return false;
    if (root == null) {
      if (other.root != null)
        return false;
    } else if (!root.equals(other.root))
      return false;
    if (tree == null) {
      if (other.tree != null)
        return false;
    } else if (!tree.equals(other.tree))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "MyTreeResult [root=" + root + ", tree=" + tree + ", numEdges=" + numEdges + "]";
  }

  public Set<Node> getNodes() {
    return nodes;
  }

  public Set<Integer> getEdges() {
    return edges;
  }

}
