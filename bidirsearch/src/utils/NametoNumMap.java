package utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
/**
 * The map of node names to their IDs and vice versa.
 * 
 * @author Madhulika Mohanty (madhulikam@cse.iitd.ac.in)
 *
 */
public class NametoNumMap {
  public static Map<String,Integer> nameToNum;
  public static Map<Integer,String> numToName;

  static{
    loadMapOfNodes();
  }

  private static void loadMapOfNodes() {
    System.out.println("Loading map of nodes---->");
    nameToNum = new HashMap<String,Integer>();
    numToName = new HashMap<Integer,String>();
    BufferedReader br = null;
    try {
      br = new BufferedReader(new FileReader(Config.nodesMapFile));
      String line;
      while((line=br.readLine())!=null){
        String[] vals = line.split("\t");
        if(vals.length>2)
          throw new IllegalArgumentException("Number of values > 2 when 2 args expected.");
        Integer id = Integer.parseInt(vals[0]);
        String node = vals[1].toLowerCase();  // toLowerCase() to ensure compatibility.
        nameToNum.put(node, id);
        numToName.put(id, node);
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }finally{
      if(br!=null)
        try {
          br.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
    }
    System.out.println("Loaded!!");
  }
}
