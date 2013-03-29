package usna.sentiment;

import java.util.Map;


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

    // Here we have a basic outline for how to add in command line arguments from
    // Chamber's code.
    Map<String, String> flags = CommandLineUtils.simpleCommandLineParser(args);
    if (!flags.containsKey("-data")) {
      System.out.println("TrainTest -data <data-dir> [-learnmylex] [-usemylex]");
			System.exit(1);
		}
		if (flags.containsKey("-learnmylex") && flags.containsKey("-usemylex")) {
			System.out.println("Hmm, can't learn a lexicon and use the lexicon at the same time.");
			System.exit(1);
		} else if (flags.containsKey("-learnmylex")) {
			anchor = flags.get("-learnmylex");
			learnLexicon = true;
		} else if (flags.containsKey("-usemylex"))
			testWithLearned = true;

		datasets = new Datasets(flags.get("-data"));

    // After done whatever we're going to do with the commandline we need to 
    // sdet up and run the model.
	}

}
