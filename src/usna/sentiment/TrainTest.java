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

/**
 * Code to label tweets with sentiment in two different ways. The first simply
 * uses a sentiment lexicon of fixed words. The second learns a lexicon from raw
 * tweets.
 * 
 * TrainTest -data <data-dir> [-learnmylex] [-usemylex]
 * 
 * @author Nate Chambers, US Naval Academy
 */
public class TrainTest {
	private static final String Char = null;
	Datasets datasets;
	boolean learnLexicon = false; // if true, runs your lexicon learning code.
	boolean testWithLearned = false; // if true, loads your already learned
										// lexicon.
	Set englishWords = new HashSet();
	List<String> Lexicon = new ArrayList<String>();
	Set stopwords = new HashSet();
	List<String> relatedTweets = new ArrayList<String>();

	String mypositive = "mypositive.txt";
	String mynegative = "mynegative.txt";

	String anchor;
	double previousPMI = 0;
	double newPMI = 1;
	
	/**
	 * YOU MUST WRITE THIS METHOD. This method will run your algorithm that uses
	 * a Sentiment Lexicon to label tweets with their sentiment. The method
	 * receives a list of tweets, and should return a list of predicted labels.
	 * 
	 * @param passages
	 *            A list of tweets without labels.
	 * @return A list of Sentiment labels, one for each given tweet.
	 */
	public List<SENTIMENT> labelWithLexicon(List<LabeledTweet> tweets,
			Counter<String> positiveWords, Counter<String> negativeWords, Counter<String> objectiveWords) {
		System.out.println("labelWithLexicon...");
		List<SENTIMENT> labels = new ArrayList<SENTIMENT>();
        SENTIMENT sent;

		for (LabeledTweet t : tweets) {
            sent = naiveBayes(t.getText(),positiveWords,negativeWords,objectiveWords);
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
  public SENTIMENT naiveBayes(String tweet, Counter<String> positiveWords, Counter<String> negativeWords,           
      Counter<String> objectiveWords) {
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
    if(obj > .40)
      return SENTIMENT.OBJECTIVE;
    if( pos > .40)
      return SENTIMENT.POSITIVE;
    else
      return SENTIMENT.NEGATIVE;
  }


    /**
     *  Filters stuff out of a tweet
     */
    public static String filterTweet(String tweet) {
        tweet = tweet.replaceAll("http://[^\\s]+", "");
        return tweet.toLowerCase().replaceAll("[\\,\\.\\!\\@\\#\\$\\%\\^\\&\\*\\_\\-\\+\\=\\?\\\"]", "");
    }


	// THE BEGINNING OF OUR PROJECT...
	//
	// **********************************************************//
	// Adds sentiment labels to the already determined related //
	// tweets //
	// **********************************************************//
  public void addSentiment() {
	  Counter<String> positiveWords = datasets.getLexiconPositiveWords();
		Counter<String> negativeWords = datasets.getLexiconNegativeWords();
		Counter<String> objectiveWords = datasets.getLexiconObjectiveWords();
		List<LabeledTweet> labeled = new ArrayList<LabeledTweet>();
    SENTIMENT sent;
		String[] word;

		double totPos = 0;
		double totNeg = 0;
		double totObj = 0;

    try {
			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter("microTweets.txt")));
		  for (String t : relatedTweets) {
        sent = naiveBayes(t,positiveWords,negativeWords,objectiveWords);  
			  LabeledTweet tweet = new LabeledTweet(sent, t);
			  labeled.add(tweet);
        writer.printf("%s\n",tweet.toString());
        if(sent.equals(SENTIMENT.POSITIVE))
          totPos++;
        else if(sent.equals(SENTIMENT.NEGATIVE))
          totNeg++;
        else
          totObj++;
		  }
      writer.close();
		} catch (IOException ex) { ex.printStackTrace(); }
		
    System.out.println("---------------------------------");
		System.out.println("TOTAL POSITIVES: " + totPos + " 	|| percent: "
				+ totPos / relatedTweets.size() * 100 + "%");
		System.out.println("TOTAL NEGATIVES: " + totNeg + " 	|| percent: "
				+ totNeg / relatedTweets.size() * 100 + "%");
		System.out.println("TOTAL OBJECTIVES: " + totObj + " 	|| percent: "
				+ totObj / relatedTweets.size() * 100 + "%");
		System.out.println("---------------------------------");
	}

	// **********************************************************//
	// **********************************************************//

	public void printTweets(List<LabeledTweet> labeled) {
		for (LabeledTweet t : labeled) {
			System.out.println("---------------------------------");
			System.out.println(t);
			System.out.println("---------------------------------");
		}
	}

	// **********************************************************//
	// Learns all the associated words and adds them to the //
	// lexicon //
	// **********************************************************//
	public void learnAssocLexicon() {
		Counter<String> tempWords = new Counter<String>();
		Counter<String> tempPMI = new Counter<String>();
		Counter<String> score = new Counter<String>();
		Counter<String> unigrams = new Counter<String>();

		// add the ticker
		if (Lexicon.isEmpty())
			Lexicon.add(anchor);

		// numbers for the loops
		String tweet = datasets.getNextRawTweet();
		String tempArray[];

		// collecting the unigrams and bigrams for the future PMI scores
		// increments all the counts of every word
		while (tweet != null) {
			if (engCheck(tweet)) {
				tempArray = tweet.toLowerCase().split("\t");
				if (tempArray.length > 0) {
                    List<String> words;  
					tweet = tempArray[0];
                    tweet = filterTweet(tweet);
					words = new ArrayList<String>(Arrays.asList(tweet.split("[\\s]+")));
					for(String word : words) { // for each word in the tweet
						if (isGood(word)) {
							if (unigrams.containsKey(word)) {
								unigrams.incrementCount(word, 1);
							} else {
								unigrams.setCount(word, 1);
							}
						}
					}
					for (String assocWord : Lexicon) {
						if (words.contains(assocWord)) {
							for (String word : words) {
                                if(isGood(word)) {
									if (tempWords.containsKey(word)
                                            && word.length() > 3) {
										tempWords.incrementCount(word, 1);
									} else if (word.length() > 3) {
										tempWords.setCount(word, 1);
									}
                                }
							}
						}
					}
				}
			}
			tweet = datasets.getNextRawTweet();
		}

		// loops through the lexicon
		for (String anchor : Lexicon) {
			// storing the PMI values of the words
			for (String word : tempWords.keySet()) {
				if (unigrams.getCount(word) > 10) {
					// example anchor = apple
					double P = PMI(unigrams.getCount(anchor),
							unigrams.getCount(word), tempWords.getCount(word),
							unigrams.totalCount());
					if (tempPMI.containsKey(word)) {
						if (P > tempPMI.getCount(word)) {
							tempPMI.setCount(word, P);
						}
					} else
						tempPMI.setCount(word, P);
				}
			}
		}

		double lambda = .75;
		// print
		System.out.println("Saving our new lexicon");
		// Output your positive words to file.
		List<String> sortedWords = Counters.sortedKeys(tempWords);
		for (String s : sortedWords) {
			score.setCount(s, lambda * tempPMI.getCount(s) + (1 - lambda)
				* tempWords.getCount(s));
		}
		List<String> scores = Counters.sortedKeys(score);
		for (String s : scores) {
			if (!Lexicon.contains(s)) {
				// adds the "best" word after any words in the lexicon
				System.out.println("----------------------");
				System.out.println(">> " + s + "| PMI: "
					+ tempPMI.getCount(s) + "| count: "
					+ tempWords.getCount(s) + "| score: "
					+ score.getCount(s));
			    System.out.println("----------------------");
				// fixes the iterator outside this function
				previousPMI = newPMI;
				newPMI = tempPMI.getCount(s);
				// -----------------------
				Lexicon.add(s);
				break;
			}
		}
        System.out.println(Lexicon);
		System.out.println("----------------------");
	}

	// **********************************************************//
	// generates the PMI score; //
	// **********************************************************//
	public double PMI(double anchorC, double newWordC, double tweetgram,
			double N) {
		double PMI = Math.log(tweetgram * N / (anchorC * newWordC));
		// if(PMI > 7)
		// {
		// System.out.println("PMI = " + PMI + "   ||| tweetgram: " + tweetgram
		// + " N: " + N + " / (anchorC: " + anchorC + " newWordC: " + newWordC +
		// ")");
		// }
		return PMI;
	}

	// **********************************************************//
	// Finds the related tweets for later sentiment analysis //
	// **********************************************************//
	public void findRelated() {
		String[] tweetParts;
		String tweet = datasets.getNextRawTweet();
		System.out.println("Finding related tweets!");
		while (tweet != null) {
			tweetParts = tweet.toLowerCase().split("\t");
			if (tweetParts.length > 0) {
				tweet = filterTweet(tweetParts[0]);
				if (engCheck(tweet)) {
					List<String> twit = new ArrayList(Arrays.asList(tweet.split("[\\s]+")));
					for (String l : Lexicon) {
						if (twit.contains(l)) {
							relatedTweets.add(tweet);
              break;
						}
					}
				}
			}
			tweet = datasets.getNextRawTweet();
		}

    // uncomment to get an output of the tweets
    /*
		try {
			// Create file
			FileWriter fstream = new FileWriter("outTweets.txt");
			BufferedWriter out = new BufferedWriter(fstream);
			for (int i = 0; i < relatedTweets.size(); i++) {
				out.write(relatedTweets.get(i).toString());
				out.newLine();
			}
			// Close the output stream
			out.close();
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		} */
	}

	// **********************************************************//
	// **********************************************************/

	/**
	 * THE ENTIRE OPERATIONAL PART OF THE PROJECT START HERE
	 * 
	 * @throws IOException
	 */
	public void learnTheLexicon() throws IOException {

		// loads all the stop words from the text documents
		loadStop();

		// call BootStrapping		
		while (previousPMI < newPMI) {
			learnAssocLexicon();
		}
		
		System.out.println("----------------------");
		System.out.println("Removing last word in Lexicon due to lower PMI value");
		// take out the last term in the list of related terms
		Lexicon.remove(Lexicon.size()-1);
		System.out.println(Lexicon);
		System.out.println("----------------------");


		// ***********************
		findRelated(); // gets related tweets
		// ***********************


		// ***********************
		addSentiment(); // gets related tweets
		// ***********************

	}


    public void writeToXMLFile() {
        try {
            Element sentiment = new Element("sentiment");
            Document doc = new Document(sentiment);
            Element company = new Element("company");
            sentiment.addContent(company);
            Element name = new Element("name");
            name.setText("apple");
            company.addContent(name);
            XMLOutputter xmlOutput = new XMLOutputter();
            xmlOutput.output(doc, new FileWriter("sentiment.xml"));
        } catch (IOException io) {
            System.out.println(io.getMessage());
        }
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

	// gets all the stop words from the text document
	public void loadStop() throws IOException {
		Scanner s = null;
		try {
			s = new Scanner(new BufferedReader(new FileReader("stopwords.txt")));
			while (s.hasNextLine()) { stopwords.add(s.nextLine()); }
            s.close();
		} catch(IOException ex) { ex.printStackTrace(); }
	}

	// simples boolean return deterimned it containing a stop word
	public boolean isGood(String tweet) {
		if (stopwords.contains(tweet)) {
			return false;
		}
		return true;
	}

	// checks to see if a tweet contains a very positive word
	public boolean pnCheck(String[] tweet, ArrayList<String> list) {
		for (String s : list) {
			for (String str : tweet) {
				if (s.compareTo(str) == 1)
					return true;
			}
		}
		return false;
	}

	/**
	 * ***************************************************
	 * 
	 * The following methods shouldn't need to be changed.
	 * *still had to change them
	 * ***************************************************
	 */

	/**
	 * DO NOT CHANGE THIS METHOD. This runs the three-step process of
	 * train/test/evaluate.
	 * 
	 * @throws IOException
	 */
	public void execute() throws IOException {
		// Learn a lexicon from raw tweets!
		if (learnLexicon) {
			System.out.println("Learn...");
			learnTheLexicon();
		}
		// No lexicon learning. Use a lexicon on disk and label tweets!
		else {
			System.out.println("Test...");
			List<SENTIMENT> guesses = null;
			// Label tweets.
			if (testWithLearned)
				guesses = labelWithLexicon(datasets.getLabeledTweets(),
						datasets.getWordsFromFile(mypositive),
						datasets.getWordsFromFile(mynegative),
                        datasets.getLexiconObjectiveWords());
			else
				guesses = labelWithLexicon(datasets.getLabeledTweets(),
						datasets.getLexiconPositiveWords(),
						datasets.getLexiconNegativeWords(),
                        datasets.getLexiconObjectiveWords());

			// Evaluate the model.
			double accuracy = Evaluator.evaluate(guesses,
					datasets.getLabeledTweets());
			System.out.printf("Final Accuracy = %.3f%%\n", accuracy);
		}
	}

	/**
	 * YOU PROBABLY DON'T WANT TO CHANGE THIS UNLESS YOU ARE ADDING COMMAND-LINE
	 * FLAGS. (make sure the current flags still work)
	 */
	public TrainTest(String[] args) {
        Scanner scanner = null;
		// read in dict.txt
		try {
		    scanner = new Scanner(new BufferedReader(new FileReader("dict.txt")));
		} catch(IOException ex) { ex.printStackTrace(); }
		while( scanner.hasNextLine() ) {
		    englishWords.add(scanner.nextLine());
		}
		
		Map<String, String> flags = CommandLineUtils
				.simpleCommandLineParser(args);

		if (!flags.containsKey("-data")) {
			System.out
					.println("TrainTest -data <data-dir> [-learnmylex] [-usemylex]");
			System.exit(1);
		}

		if (flags.containsKey("-learnmylex") && flags.containsKey("-usemylex")) {
			System.out
					.println("Hmm, can't learn a lexicon and use the lexicon at the same time.");
			System.exit(1);
		} else if (flags.containsKey("-learnmylex")) {
			anchor = flags.get("-learnmylex");
			learnLexicon = true;
		} else if (flags.containsKey("-usemylex"))
			testWithLearned = true;

		datasets = new Datasets(flags.get("-data"));
	}

	/**
	 * DO NOT CHANGE THIS MAIN FUNCTION.
	 * 
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		TrainTest program = new TrainTest(args);
		program.execute();
        program.writeToXMLFile();
	}
}
