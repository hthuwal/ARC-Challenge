package main;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import search.BidirSearch;
import utils.MyTreeResult;

/**
 * An example use of the {@link BidirSearch} class.
 * 
 * @author Madhulika Mohanty (madhulikam@cse.iitd.ac.in)
 *
 */
public class BidirSearchExample {

  public static int numAnswers = 5;
  
  public static void main(String[] args) {
    
    Scanner sc = new Scanner(System.in);
    System.out.println("Enter your keyword query:");
    String query = sc.nextLine();//Eg. Angelina_Jolie Brad_Pitt;
    sc.close();
    
    BidirSearch ks=new BidirSearch(query);
    List<MyTreeResult> heap=new ArrayList<MyTreeResult>();
    int i=0;
    while(ks.hasNext()){
      MyTreeResult res = ks.next();
      heap.add(res);
      i++;
      if(i==numAnswers){
        ks.close();
        break;
      }
    }
    System.out.println("Returned "+heap.size()+" answers!");

    Iterator<MyTreeResult> iter=heap.iterator();
    List<MyTreeResult> listAnswers = new ArrayList<MyTreeResult>();
    while(iter.hasNext()){
      MyTreeResult tmp=iter.next();
      listAnswers.add(tmp);
      System.out.println("\nResult:"+"\n"+tmp.toString());
    }

  }

}
