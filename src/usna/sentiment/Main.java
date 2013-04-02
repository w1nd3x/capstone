package usna.sentiment;

import java.util.Map;
import java.util.ArrayList;
import java.util.List;

// usna packages
import usna.sentiment.Lexicon;
import usna.sentiment.Datasets;
import usna.sentiment.Model;
import usna.util.Day;
import usna.util.Interpolate;
import usna.util.Stock;
import usna.util.CommandLineUtils;


/**
 *  Class to build up a model of Stocks against data from Twitter
 *
 *
 */
public class Main {

  public static void main(String args[]) {
    Map<String, String> flags = CommandLineUtils.simpleCommandLineParser(args);
    Datasets datasets = new Datasets("./datasets/");
    boolean train = false;
    boolean test = false;
    boolean interpolate = false;
    boolean constructLex = false;
    String build = null;
    String anchor = null;
    String stock = null;
    String start = null;
    String end = null;


    if (flags.containsKey("-test")&&flags.containsKey("-train")) {
      System.out.println("Error: cannot test and train in the same run");
      //Main.usage();
      System.exit(1);
    }
    if (flags.containsKey("-train")) {
      train = true;
    }
    if (flags.containsKey("-test")) {
      test = true;
    }
    if (flags.containsKey("-anchor")&&flags.containsKey("-start")&&flags.containsKey("-end")) {
      anchor = flags.get("-anchor");
      start = flags.get("-start");
      end = flags.get("-end");
      interpolate = true;
      System.out.println("INTERPOLATE");
    }
    if (flags.containsKey("-build")&&flags.containsKey("-anchor")) {
      build = flags.get("-build");
      anchor = flags.get("-anchor");
      constructLex = true;
    }
    
    // Test the model
    if(test){
      Model testModel = new Model();
      testModel.test();
      return;
    }

    // Train the model
    if(train) {
      Model trainModel = new Model();
      trainModel.train();
      return;
    }

    if(constructLex) {
      ArrayList<String> lexical = new ArrayList<String>();
      Lexicon lexicon = new Lexicon(anchor);
      lexicon.build(lexical);
      return;
    }

    if(interpolate) {
      Interpolate Mod = new Interpolate();
      // build a range of days based on some input
      Stock aapl = new Stock("AAPL",start,end);
      for ( String timestamp : aapl.keyList() ) {
        Model apple = new Model();
        Day tempDay = new Day(aapl.getData(timestamp),apple.process(timestamp,anchor),timestamp);
        Mod.newDay(tempDay);
      }
      for(String lex : datasets.getLexicon(anchor)) {
        System.out.println("\t" + lex);
      }
    }
  }
}
