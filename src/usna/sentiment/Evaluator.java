package usna.sentiment;

import java.util.List;

import usna.sentiment.LabeledTweet.SENTIMENT;
import usna.util.Counter;


/**
 * YOU PROBABLY DON'T NEED TO CHANGE OR ADD CODE TO THIS CLASS.
 *
 * Code to compare predicted labels to gold labeled passages. Computes Accuracy.
 * 
 * @author Nate Chambers, US Naval Academy
 */
public class Evaluator {

  /**
   * A simple method that just compares the strings given in the first list to the strings
   * given in the second list. Both lists should be the same length, and the
   * ith index in each should correspond to the same experiment.
   * An accuracy score is computed ( # correct / # passages ) and returned.
   * 
   * @param guesses The list of predicted labels.
   * @param labeledData The list of true gold labels.
   * @return A single accuracy score.
   */
  public static double evaluate(List<SENTIMENT> guesses, List<LabeledTweet> goldTweets) {
    Counter<SENTIMENT> numTypes = new Counter<SENTIMENT>();
    Counter<SENTIMENT> numGuessedRight = new Counter<SENTIMENT>();
    Counter<SENTIMENT> numGuessedWrong = new Counter<SENTIMENT>();
    
    // Number of guesses must be aligned with the actual data.
    if( guesses.size() != goldTweets.size() ) {
      System.out.println("ERROR IN EVALUATE(): you gave me " + guesses.size() + " guessed labels, but " + goldTweets.size() + " gold labels.");
      return 0.0;
    }
    
    // Compare the guesses with the gold labels.
    int numRight = 0, numWrong = 0, xx = 0;
    for( SENTIMENT guess : guesses ) {
      SENTIMENT gold = goldTweets.get(xx).getSentiment();
      numTypes.incrementCount(gold, 1.0);
      if( gold == guess ) {
    	
        numRight++;
        numGuessedRight.incrementCount(guess, 1.0);
      }
      else {
        numGuessedWrong.incrementCount(guess, 1.0);
        numWrong++;
      }
      xx++;
    }

    // Compute accuracy.
    for( SENTIMENT sent : numTypes.keySet() )
      System.out.printf(sent + "\tPrecision=%.1f%%\tRecall=%.1f%%\n", 
          (100*(double)numGuessedRight.getCount(sent) / (numGuessedRight.getCount(sent)+numGuessedWrong.getCount(sent))),
          (100*(double)numGuessedRight.getCount(sent) / (numTypes.getCount(sent))) );
    
    System.out.println("Correct:\t" + numRight);
    System.out.println("Incorrect:\t" + numWrong);
    double accuracy = (double)numRight / ((double)numRight+(double)numWrong);
    return accuracy * 100.0;
  }
}
