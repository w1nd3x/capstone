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
		
	  String labeled = datasets.getNextLabeledTweet();
		Set<String> stopwords = datasets.loadStop();		
		/* 
		 * This loops through all the labeled tweets and gets a count 
		 * of the positive, negative, and objective words in the tweet
		 *
		 */
    while( labeled != null ) {
      String[] array1;
      array1 = labeled.split("\t");
			String tweet = Datasets.filterTweet(array1[1]);
			List<String> words = new ArrayList<String>(Arrays.asList(tweet.split("[\\s]+")));
			for(String word : words) {
				if(!stopwords.contains(word) && word.length() > 3) {
						if(array1[0].equals("POSITIVE")) {
							if(positiveWords.containsKey(word)) {
								positiveWords.incrementCount(word, 1);
                posTotal++;
							} else {
								positiveWords.setCount(word, 1);
                posTotal++;
							}
            } else if(array1[0].equals("NEGATIVE")) {
							if(negativeWords.containsKey(word)) {
								negativeWords.incrementCount(word, 1);
                negTotal++;
							} else {
								negativeWords.setCount(word, 1);
                negTotal++;
							}
            } else if(array1[0].equals("OBJECTIVE")) {
							if(objectiveWords.containsKey(word)) {
								objectiveWords.incrementCount(word, 1);
                objTotal++;
							} else {
								objectiveWords.setCount(word, 1);
                objTotal++;
							}
            }
					}
				}
      labeled = datasets.getNextLabeledTweet();
		}
		
		/*
		 * Next the program needs to loop through all of the Counters and
		 * print the probabilities to a file
		 *
		 */
		try {
			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter("datasets/lexicon/positive.txt")));
			for(String word : positiveWords.keySet()) {
				writer.printf("%s\t%f\n",word,positiveWords.getCount(word)/positiveWords.size());
			}
			writer.close();
		} catch(IOException ex) { ex.printStackTrace(); }
		try {
			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter("datasets/lexicon/negative.txt")));
			for(String word : negativeWords.keySet()) {
				writer.printf("%s\t%f\n",word,negativeWords.getCount(word)/negativeWords.size());
			}
			writer.close();
		} catch(IOException ex) { ex.printStackTrace(); }
		try {
			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter("datasets/lexicon/objective.txt")));
			for(String word : objectiveWords.keySet()) {
				writer.printf("%s\t%f\n",word,objectiveWords.getCount(word)/objectiveWords.size());
			}
			writer.close();
		} catch(IOException ex) { ex.printStackTrace(); }
	}

	/*
	 *	Test a model on a set of labeled tweets
	 *
	 */
	public void test() {
    List<SENTIMENT> guesses = null;
		guesses = labelWithLexicon(datasets.getLabeledTweets(),
				datasets.getLexiconPositiveWords(),
				datasets.getLexiconNegativeWords(),
        datasets.getLexiconObjectiveWords());
    double accuracy = Evaluator.evaluate(guesses,datasets.getLabeledTweets());
    System.out.printf("Final Accuracy = %.3f%%\n",accuracy);
	}

  private List<SENTIMENT> labelWithLexicon(List<LabeledTweet> tweets, Counter<String> positiveWords,
        Counter<String> negativeWords,Counter<String> objectiveWords) {
    List<SENTIMENT> labels = new ArrayList<SENTIMENT>();
    SENTIMENT sent;
    String tweet;
    for(LabeledTweet t : tweets) {
       tweet = Datasets.filterTweet(t.getText());
      sent = naiveBayes(tweet,positiveWords,negativeWords,objectiveWords);
      labels.add(sent);
    }
    return labels;
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
		stopwords = datasets.loadStop();		

    // encode the priors here
    double pos = 0.0;
    double neg = 0.0;
    double obj = 0.0;
   
    strArray = tweet.split("[\\s]+");
    // add all the probabilities together using logs to smooth some of errors
    for( String word : strArray ) {
      if(!stopwords.contains(word)) {
        if(positiveWords.containsKey(word)) {
          pos = pos + Math.log(positiveWords.getCount(word));
        } else {
          pos = pos + Math.log(0.0000000000000000000001);
        }
        if(negativeWords.containsKey(word)) {
          neg = neg + Math.log(negativeWords.getCount(word));
        } else {
          neg = neg + Math.log(0.0000000000000000000001);
        }
        if(objectiveWords.containsKey(word)) {
          obj = obj + Math.log(objectiveWords.getCount(word));
        } else{
          obj = obj + Math.log(0.0000000000000000000001);
        }
      }
    }
        
    // convert all the weights into percentages
    //obj = Math.exp(obj);
    double temppos = Math.exp(pos + Math.log(0.20));
    double tempneg = Math.exp(neg + Math.log(0.10));
    double tempobj = Math.exp(obj + Math.log(0.70));
    //obj = obj / (pos + neg + obj);
    pos = temppos / (temppos + tempneg + tempobj);
    neg = tempneg / (temppos + tempneg + tempobj);
    obj = tempobj / (temppos + tempneg + tempobj);
    // apply a threshold to get values
    if(Math.max(pos,Math.max(neg,obj)) == pos) 
      return SENTIMENT.POSITIVE;
    else if( Math.max(pos,Math.max(neg,obj)) == neg)
      return SENTIMENT.NEGATIVE;
    else
      return SENTIMENT.OBJECTIVE;
  }

	/*
	 *	Run a model on a set of raw tweets
	 *
	 */
	public List<Integer> process(String timestamp,String anchor) {
		Counter<String> positiveWords = datasets.getLexiconPositiveWords();
		Counter<String> negativeWords = datasets.getLexiconNegativeWords();
		Counter<String> objectiveWords = datasets.getLexiconObjectiveWords();
	  ArrayList<String> relatedTweets = new ArrayList<String>();
    List<Integer> results = new ArrayList<Integer>();
    SENTIMENT sent;
		String[] tweetParts;
    Double totPos = new Double(0.0);
    Double totNeg = new Double(0.0);
    Double totObj = new Double(0.0);
    
    englishWords = datasets.loadEnglish();
    lexicon = datasets.getLexicon(anchor);
    if(lexicon == null) {
      System.err.println("No Lexicon for " + anchor);
      System.exit(1);
    }
		
    String tweet = datasets.getNextDatedTweet(timestamp);
    while(tweet != null) {
      tweetParts = tweet.split("\t");
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
    results.add(new Integer(totPos.intValue()));
    results.add(new Integer(totNeg.intValue()));
    results.add(new Integer(totObj.intValue()));
    results.add(new Integer(relatedTweets.size()));
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
    test.test();
	}
}
