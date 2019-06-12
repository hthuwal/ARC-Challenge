package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * Class having values for all the configuration parameters. 
 * The properties are read from {@value #configFile} file.
 * 
 * @author Madhulika Mohanty (madhulikam@cse.iitd.ac.in)
 *
 */
public class Config {
  public static final String configFile = "conf/config.properties";
  public static String nodesMapFile;
  public static String graphFileName;

  static{
    Config.loadProperties();
  }

  public static void loadProperties(){
    Properties props = new Properties();
    try {
      props.load(new FileInputStream(new File(configFile)));
      Config.graphFileName = props.getProperty("graphFileName");
      Config.nodesMapFile = props.getProperty("nodesMapFile");
    } catch (FileNotFoundException e) {
      System.out.println("Please put the config file in:"+new File(configFile).getAbsolutePath());
      System.out.println("See conf/config.example.properties for more information.");
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
