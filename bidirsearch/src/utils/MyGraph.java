package utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.google.common.graph.ImmutableValueGraph;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;

/**
 * Graph in adjacency list format.
 * 
 * @author Madhulika Mohanty (madhulikam@cse.iitd.ac.in)
 *
 */
public class MyGraph {
  ImmutableValueGraph<Node, ArrayList<Integer>> graph;

  public MyGraph(String path) {
    this.graph=readGraph(path);
  }

  public MyGraph() {
    this.graph=readGraph(Config.graphFileName);
  }

  private ImmutableValueGraph<Node, ArrayList<Integer>> readGraph(String path) {
    System.out.println("Reading graph into memory----->");
    MutableValueGraph<Node, ArrayList<Integer>> weightedGraph = ValueGraphBuilder.directed().allowsSelfLoops(true).build();

    BufferedReader br = null;

    try {
      br = new BufferedReader(new FileReader(path));
      String line;

      while((line=br.readLine())!=null)
      {
        String[] nodes = line.split("\t");
        List<String> vals = Arrays.asList(nodes);
        if(vals.size()!=3) continue;
        Node a = new Node(Integer.parseInt(vals.get(0)));
        Node b = new Node(Integer.parseInt(vals.get(2)));
        Optional<ArrayList<Integer>> c1 =  weightedGraph.edgeValue(a, b);
        if(c1.isPresent())
        {
          ArrayList<Integer> c = (ArrayList<Integer>) c1.get();
          c.add(Integer.parseInt(vals.get(1)));
          weightedGraph.putEdgeValue(a, b, c);
        }
        else
        {
          ArrayList<Integer> c = new ArrayList<Integer>();
          c.add(Integer.parseInt(vals.get(1)));
          weightedGraph.putEdgeValue(a, b, c);
        }

        //Putting inverse edge.
        Optional<ArrayList<Integer>> c2 =  weightedGraph.edgeValue(b, a);
        if(c2.isPresent())
        {
          ArrayList<Integer> c = (ArrayList<Integer>) c2.get();
          c.add(Integer.parseInt(vals.get(1)));
          weightedGraph.putEdgeValue(b, a, c);
        }
        else
        {
          ArrayList<Integer> c = new ArrayList<Integer>();
          c.add(Integer.parseInt(vals.get(1)));
          weightedGraph.putEdgeValue(b, a, c);
        }
      }
    } catch (NumberFormatException | IOException e) {
      e.printStackTrace();
    }finally{
      try {
        if (br != null)
          br.close();
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    }
    System.out.println("Graph is in memory now!");
    return ImmutableValueGraph.copyOf(weightedGraph);
  }

  public Iterator<Triple> find(Node sub, Integer pred, Node obj) {
    List<Triple> returnSet = new ArrayList<Triple>();
    if(sub.equals(Node.ANY)){ // Triple with specified obj.
      Set<Node> predSet=graph.predecessors(obj);
      if(predSet.size()==0)
        return returnSet.iterator();;
        returnSet=new ArrayList<Triple>();
        for(Node n:predSet) {
          ArrayList<Integer> edges=graph.edgeValue(n, obj).get();
          for(Integer edge:edges) {
            Triple t = new Triple(n.id,edge,obj.id);
            returnSet.add(t);
          }
        }
    }
    else if (obj.equals(Node.ANY)) { // Triple with specified sub.
      Set<Node> succSet=graph.successors(sub);
      if(succSet.size()==0)
        return returnSet.iterator();;
        returnSet=new ArrayList<Triple>();
        for(Node n:succSet) {
          ArrayList<Integer> edges=graph.edgeValue(sub, n).get();
          for(Integer edge:edges) {
            Triple t = new Triple(sub.id,edge,n.id);
            returnSet.add(t);
          }
        }
    }
    else { // Triple with specified sub and obj.
      Optional<ArrayList<Integer>> edgesOpt=graph.edgeValue(sub, obj);
      if(edgesOpt.isPresent()) {
        returnSet=new ArrayList<Triple>();
        ArrayList<Integer> edges=edgesOpt.get();
        for(Integer edge:edges) {
          Triple t = new Triple(sub.id,edge,obj.id);
          returnSet.add(t);
        }
      }
      else
        return returnSet.iterator();
    }
    return returnSet.iterator();
  }

  public Set<Node> nodes() {
    return this.graph.nodes();
  }
}
