package usna.sentiment;

import java.util.*;
import java.io.*;

import usna.util.CommandLineUtils;
import usna.util.Counter;
import usna.util.Counters;
import usna.sentiment.LabeledTweet.SENTIMENT;

import usna.sentiment.Lexicon;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;


/**
 *  Code to pull words from a lexicon and use them to anaylze a file of tweets
 *
 *
 */
public class Model {
	private Datasets datasets;
	
	private Set englishWords = new HashSet();
	private Set stopwords = new HashSet();
	
	private List<String> lexicon = new ArrayList<String>();
	private List<String> relatedTweets = new ArrayList<String>();

  public Model() {
    this.datasets = new Datasets("./datasets/");
  }

	/*
	 *	Train a model on a set of labeled tweets
	 *
	 */
	public void train() {
		Counter<String> positiveWords = new Counter<String>();
		Counter<String> negativeWords = new Counter<String>();
		Counter<String> objectiveWords = new Counter<String>();
		double posTotal = 0;  
		double negTotal = 0;  
		double objTotal = 0;  
		
		List<LabeledTweet> labeled = datasets.getLabeledTweets();
		Set<String> stopwords = datasets.loadStop();		
		/* 
		 * This loops through all the labeled tweets and gets a count 
		 * of the positive, negative, and objective words in the tweet
		 *
		 */
		for(LabeledTweet t : labeled) {
			String tweet = datasets.filterTweet(t.getText().toLowerCase());
			List<String> words = new ArrayList<String>(Arrays.asList(tweet.split("[\\s]+")));
			for(String word : words) {
				if(!stopwords.contains(word) && word.length() > 3) {
					switch(t.getSentiment()) {
						case POSITIVE:
							if(positiveWords.containsKey(word)) {
								positiveWords.incrementCount(word, 1);
                posTotal++;
							} else {
								positiveWords.setCount(word, 1);
                posTotal++;
							}
							break;
						case NEGATIVE:
							if(negativeWords.containsKey(word)) {
								negativeWords.incrementCount(word, 1);
                negTotal++;
							} else {
								negativeWords.setCount(word, 1);
                negTotal++;
							}
							break;
						case OBJECTIVE:
							if(objectiveWords.containsKey(word)) {
								objectiveWords.incrementCount(word, 1);
                objTotal++;
							} else {
								objectiveWords.setCount(word, 1);
                objTotal++;
							}
							break;
					}
				}
			}
		}
		
		/*
		 * Next the program needs to loop through all of the Counters and
		 * print the probabilities to a file
		 *
		 */
		try {
			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter("positive.txt")));
			for(String word : positiveWords.keySet()) {
				writer.printf("%s\t%f\n",word,positiveWords.getCount(word)/posTotal);
			}
			writer.close();
		} catch(IOException ex) { ex.printStackTrace(); }

		try {
			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter("negative.txt")));
			for(String word : negativeWords.keySet()) {
				writer.printf("%s\t%f\n",word,negativeWords.getCount(word)/negTotal);
			}
			writer.close();
		} catch(IOException ex) { ex.printStackTrace(); }
		
		try {
			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter("objective.txt")));
			for(String word : objectiveWords.keySet()) {
				writer.printf("%s\t%f\n",word,objectiveWords.getCount(word)/objTotal);
			}
			writer.close();
		} catch(IOException ex) { ex.printStackTrace(); }
	}

	/*
	 *	Test a model on a set of labeled tweets
	 *
	 */
	public void test() {

	}

  /**
   *  This program runs a naive bayes on a given tweet.  It takes in a weighted
   *  list of words from training data and applies it to the tweet.  The hardest
   *  part from here will be finding good threshold values for positive, negative,
   *  and objective values.
   *  
   * @param tweets - A single tweet in string form
   * @param positiveWords - A weighted list of words associated with positive tweets
   * @param negativeWords - A weighted list of words associated with negative tweets
   * @param objectiveWords - A weighted list of words associated with objective tweets
   * @return a SENTIMENT specific to the tweet parameter
   *
   */
  public SENTIMENT naiveBayes(String tweet, Counter<String> positiveWords, Counter<String> negativeWords, Counter<String> objectiveWords) {
    String[] strArray = null;
    double pos = 0;
    double neg = 0;
    double obj = 0;
    
    strArray = tweet.split("[\\s]+");

    // add all the probabilities together using logs to smooth some of errors
    for( String word : strArray ) {
      if(positiveWords.containsKey(word))
        pos = pos + Math.abs(Math.log(positiveWords.getCount(word)));
      if(negativeWords.containsKey(word))
        neg = neg + Math.abs(Math.log(negativeWords.getCount(word)));
      if(objectiveWords.containsKey(word))
        obj = obj + Math.abs(Math.log(objectiveWords.getCount(word)));
    }
        
    // convert all the weights into percentages
    obj = Math.exp(obj);
    pos = Math.exp(pos);
    neg = Math.exp(neg);
    obj = obj / (obj + neg + pos);
    pos = pos / (pos + neg + obj);
    neg = neg / (pos + neg + obj);

    // apply a threshold to get values
	  if(obj > .4)
      return SENTIMENT.OBJECTIVE;
	  else if (pos > .4) 
      return SENTIMENT.POSITIVE;
	  else 
      return SENTIMENT.NEGATIVE;
  }

	/*
	 *	Run a model on a set of raw tweets
	 *
	 */
	public List<String> process(String timestamp,String anchor) {
		Counter<String> positiveWords = datasets.getLexiconPositiveWords();
		Counter<String> negativeWords = datasets.getLexiconNegativeWords();
		Counter<String> objectiveWords = datasets.getLexiconObjectiveWords();
	  ArrayList<String> relatedTweets = new ArrayList<String>();
    List<String> results = new ArrayList<String>();
    SENTIMENT sent;
		String[] tweetParts;
    englishWords = datasets.loadEnglish();
    lexicon = datasets.getLexicon(anchor);
    if(lexicon == null) {
      System.err.println("No Lexicon for " + anchor);
      System.exit(1);
    }
		
    double totPos = 0;
		double totNeg = 0;
		double totObj = 0;
    String tweet = datasets.getNextDatedTweet(timestamp);
    while(tweet != null) {
      tweetParts = tweet.toLowerCase().split("\t");
      if(tweetParts.length > 0) {
        tweet = datasets.filterTweet(tweetParts[0]);
        if(engCheck(tweet)) {
          ArrayList<String> words = new ArrayList(Arrays.asList(tweet.split("[\\s]+")));
          for(String anchorWord : lexicon) {
            if(words.contains(anchorWord)) {
              relatedTweets.add(tweet);
              break;
            }
          }
        }
      }
      tweet = datasets.getNextDatedTweet(timestamp);
    }

    for( String t : relatedTweets ) {
      sent = naiveBayes(t,positiveWords,negativeWords,objectiveWords);  
      if(sent.equals(SENTIMENT.POSITIVE)) {
        totPos++;
      } else if(sent.equals(SENTIMENT.NEGATIVE)) {
        totNeg++;
      } else {
        totObj++;
      }
    }
    results.add(Double.toString(totPos));
    results.add(Double.toString(totNeg));
    results.add(Double.toString(totObj));
    results.add(Integer.toString(relatedTweets.size()));
    return results;
  }
  
  /**
	 * Checks to see if English
	 */
	public boolean engCheck(String tweet) {
    String[] tweetArray;
    int tokens = 0;
		// most common words in english alphabet
		if (tweet.matches("^[\\p{ASCII}]*$")) {
      tweetArray = tweet.split("[\\p{P} \\t\\n\\r]+");
      if(tweetArray.length > 0) {
			  for(int i = 0; i < tweetArray.length; i++) {
          if(englishWords.contains(tweetArray[i])) { tokens++; }
			  }
			  if((double)tokens/tweetArray.length > .33)
          return true;
		  }
    }
    return false;
  }


	/*
	 *	Main function purely for debugging purposes
	 *
	 */
	public static void main(String args[]) {
		Model test = new Model();
    test.process("20130317","apple");
	}
}
