package usna.sentiment;

import java.util.*;
import java.io.*;

import usna.util.CommandLineUtils;
import usna.util.Counter;
import usna.util.Counters;
import usna.sentiment.LabeledTweet.SENTIMENT;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;


public class Lexicon {
	private double previousPMI = 0;
	private double newPMI = 1;
	private Datasets datasets;
  private Set englishWords;
	private Set stopwords;
	private String anchor;

  public Lexicon(String anchor) {
    this.datasets = new Datasets("datasets/");
    this.anchor = anchor;
  }

  public void build(ArrayList<String> lexicon) {
		Counter<String> tempWords = new Counter<String>();
		Counter<String> tempPMI = new Counter<String>();
		Counter<String> score = new Counter<String>();
		Counter<String> unigrams = new Counter<String>();
    stopwords = datasets.loadStop();
    englishWords = datasets.loadEnglish();
    
    System.out.println("previous PMI: " + previousPMI);
    System.out.println("new PMI: " + newPMI);
		/* The base condition */
		if(previousPMI > newPMI)
			return;
		/* Recursively call the list */
		if (lexicon.isEmpty())
			lexicon.add(anchor);
		String tweet = datasets.getNextRawTweet();
		String tempArray[];
		while(tweet != null) {
			if(engCheck(tweet)) {
				tempArray = tweet.toLowerCase().split("\t");
				if(tempArray.length > 0) {
					List<String> words;
		    	tweet = tempArray[0];
		    	tweet = datasets.filterTweet(tweet);
		    	words = new ArrayList<String>(Arrays.asList(tweet.split("[\\s]+")));
		    	for(String word : words) {
						if(!stopwords.contains(word)) {
							if(unigrams.containsKey(word)) {
								unigrams.incrementCount(word,1);
							} else {
								unigrams.setCount(word,1);
							}
						}
		    	}
		    	for(String assocWord : lexicon) {
						if(words.contains(assocWord)) {
							for(String word : words) {
								if(!stopwords.contains(word)) {
									if(tempWords.containsKey(word) && word.length() > 3) {
										tempWords.incrementCount(word,1);	
									} else if(word.length() > 3) {
										tempWords.setCount(word,1);
									}
								}
							}
						}
		    	}
				}
	  	}
			tweet = datasets.getNextRawTweet();
		}
    
		for(String anchor : lexicon) {
			for(String word : tempWords.keySet()) {
				if(unigrams.getCount(word) > 10) {
					double P = PMI(unigrams.getCount(anchor),
							unigrams.getCount(word),
							tempWords.getCount(word),
							unigrams.totalCount() );
					if(tempPMI.containsKey(word)) {
						if(P > tempPMI.getCount(word)) {
							tempPMI.setCount(word,P);
						}
					} else {
							tempPMI.setCount(word,P);
					}
				}
			}
		}
		double lambda = .75;
		List<String> sortedWords = Counters.sortedKeys(tempWords);
		for (String s : sortedWords) {
			score.setCount(s, lambda * tempPMI.getCount(s) + (1 - lambda)
				* tempWords.getCount(s));
		}
		List<String> scores = Counters.sortedKeys(score);
		for(String s : scores) {
			if(!lexicon.contains(s)) {
				System.out.println("-----------------");
				System.out.println(">> " + s + "| PMI: "
						+ tempPMI.getCount(s) + "| count: "
						+ tempWords.getCount(s) + "| score: "
						+ score.getCount(s) );
				System.out.println("-----------------");
				previousPMI = newPMI;
				newPMI = tempPMI.getCount(s);
				lexicon.add(s);
				break;
			}
		}
    build(lexicon); 
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


	// **********************************************************//
	// generates the PMI score; //
	// **********************************************************//
	public double PMI(double anchorC, double newWordC, double tweetgram,
			double N) {
		double PMI = Math.log(tweetgram * N / (anchorC * newWordC));
		return PMI;
	}
	
  /*
   *  Main class for debugging purposes only
   */
  public static void main(String args[]) {
    ArrayList<String> lexicon = new ArrayList<String>();
    Lexicon lex = new Lexicon("apple");
    lex.build(lexicon);
    for(String s : lexicon) {
      System.out.println(s);
    }
  }
}
