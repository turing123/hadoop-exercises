package turing123.hadoop.exercises;

import org.apache.hadoop.util.ProgramDriver;

/**
 * A description of an example program based on its class and a 
 * human-readable description.
 */
public class ExampleDriver {
  
  public static void main(String argv[]){
    int exitCode = -1;
    ProgramDriver pgd = new ProgramDriver();
    try {      
      pgd.addClass("wordmeanbystartchar", WordMeanByStartChar.class,
                   "A map/reduce program that counts the average length of the words with the same starting character in the input files.");
      exitCode = pgd.run(argv);
    }
    catch(Throwable e){
      e.printStackTrace();
    }
    
    System.exit(exitCode);
  }
}