package usna.sentiment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

import java.util.zip.GZIPInputStream;
import usna.util.Counter;
import usna.sentiment.LabeledTweet.SENTIMENT;


/**
 * YOU PROBABLY DON'T NEED TO CHANGE OR ADD CODE TO THIS CLASS.
 * 
 * Reads the tweets and the Opinion Lexicon from disk for you.
 * Use the lexicon to label tweets with sentiment, and use the tweets to learn a new lexicon.
 * 
 * @author Nate Chambers, US Naval Academy
 */
public class Datasets {
  private String rootDirectory = "./datasets/";
  BufferedReader rawReader = null;
  String currentRawFile = null;
	  
  /**
   * Creates the object, and requires the path to the base data directory "lab4/data/"
   * @param rootPath
   */
  public Datasets(String rootPath) {
    rootDirectory = rootPath;
  }

  /**
   * This function gives you a single tweet. It will read the next tweet in the currently
   * opened file. If the file reaches the end, it opens the next unread file and gives you
   * its first tweet. This function is amazing. Almost no memory is used.
   * @return A String which is the text of one tweet.
   */
  public String getNextRawTweet() {
	  if( rawReader != null ) {
		  try {
			  String line = rawReader.readLine();
			  if( line != null )
				  return line;
		  } catch( IOException ex ) { ex.printStackTrace(); }
	  }
	  if( advanceRawFile() )
		  return getNextRawTweet();
	  else return null;
  }
  
  public String getNextDatedTweet(String timestamp) {
	  if( rawReader != null ) {
		  try {
			  String line = rawReader.readLine();
			  if( line != null )
				  return line;
		  } catch( IOException ex ) { ex.printStackTrace(); }
	  }
    if( openRawDatedFile(timestamp) )
      return getNextDatedTweet(timestamp);
	  else return null;
  }
  
  private boolean openRawDatedFile(String timestamp) {
	  String dir = rootDirectory + "/tweets";
	  
	  // Make sure it is a valid directory.
	  File filedir = new File(dir);
	  if( !filedir.isDirectory() ) {
		  System.err.println("ERROR: path is not a directory: " + dir);
		  System.exit(1);
	  }
	  
    // already read the datefile
    if(currentRawFile == null)
      currentRawFile = timestamp;
    else
      return false; 

	  // Open the date file from this directory.
	  System.out.println("opening file " + timestamp + ".txt.gz");
	  try {
		  if( rawReader != null ) rawReader.close();
		  InputStream in = new GZIPInputStream(new FileInputStream(new File(dir + "/" + timestamp + ".txt.gz")));
		  rawReader = new BufferedReader(new InputStreamReader(in));
	  } catch( IOException ex ) { 
		  System.err.println("Error opening file: " + timestamp + ".txt.gz");
		  ex.printStackTrace();
    }
    return true;
  }
  
  private boolean advanceRawFile() {
	  String dir = rootDirectory + "/tweets";
	  
	  // Make sure it is a valid directory.
	  File filedir = new File(dir);
	  if( !filedir.isDirectory() ) {
		  System.err.println("ERROR: path is not a directory: " + dir);
		  System.exit(1);
	  }

	  // Read the author files from this directory.
	  String[] files = getZipFilesSorted(dir);
	  String nextFile = null;
	  if( currentRawFile == null )
		  nextFile = files[0];
	  else {
		  for( int ii = 0; ii < files.length; ii++ ) {
			  if( files[ii].equalsIgnoreCase(currentRawFile) && ii+1 < files.length )
				  nextFile = files[ii+1];
		  }
	  }

	  // No more files to read.
	  if( nextFile == null ) {
          currentRawFile = null;
            return false; 
      }
      else
          currentRawFile = nextFile;

	  // Open the file to be read later.	  
	  System.out.println("opening file " + currentRawFile);
	  try {
		  if( rawReader != null ) rawReader.close();
		  InputStream in = new GZIPInputStream(new FileInputStream(new File(dir + "/" + currentRawFile)));
		  rawReader = new BufferedReader(new InputStreamReader(in));
	  } catch( IOException ex ) { 
		  System.err.println("Error opening file: " + currentRawFile);
		  ex.printStackTrace();
	  }
	  return true;
  }
  
  
  /**
   * Read the labeled tweets from disk, put them in a list of tweet objects.
   * @return A list of tweet objects with sentiment labels.
   */
  public List<LabeledTweet> getLabeledTweets() {
    List<LabeledTweet> tweets = new ArrayList<LabeledTweet>();
    //String path = "/tmp/sentiment-data/lexicon/new-labeled-tweets.txt";
    String path = "./datasets/combined-labeled-tweets.txt"; 
    try {
      BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(new File(path))));
      String line = in.readLine();
      while( line != null ) {
        String parts[] = line.split("\t");
        tweets.add(new LabeledTweet(SENTIMENT.valueOf(parts[0].toUpperCase()), parts[1]));
        line = in.readLine();
      }
      in.close();
    } catch( IOException ex ) { 
      System.err.println("Error opening file: " + path);
      ex.printStackTrace();
    }
    
    return tweets;
  }

  /**
   * Grab all the positive words from the Opinion Lexicon.
   * @return A list of words.
   */
  public Counter<String> getLexiconPositiveWords() {
	  return getWordsFromFile(rootDirectory + File.separator + "lexicon/positive.txt");
  }

  /**
   * Grab all the negative words from the Opinion Lexicon.
   * @return A list of words.
   */
  public Counter<String> getLexiconNegativeWords() {
	  return getWordsFromFile(rootDirectory + File.separator + "lexicon/negative.txt");
  }

  /**
   * Grab all the objective words from the Opinion Lexicon.
   * @return A list of words.
   */
    public Counter<String> getLexiconObjectiveWords() {
        return getWordsFromFile(rootDirectory + File.separator + "lexicon/objective.txt");     
    }

  
  /**
   * Opens the given file path and assumes one word per line. Returns those words as a List.
   * @param path The path to the word file.
   * @return A list of strings, the words in the file.
   */
    public Counter<String> getWordsFromFile(String path) {
        Counter<String> words = new Counter<String>();
        String[] strArray = null;
        try {
		  BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(new File(path))));
		  String line = in.readLine();
        while( line != null ) {
              strArray = line.split("\\t");
			  if( strArray.length > 1 )
				  words.setCount(strArray[0],Double.valueOf(strArray[1]));
			  line = in.readLine();
		  }
		  in.close();
	  } catch( IOException ex ) { 
		  System.err.println("Error opening file: " + path);
		  ex.printStackTrace();
		  return null;
	  }
	  return words;	
  }
  
  public ArrayList<String> getLexicon(String anchor) {
    ArrayList<String> lexicon = new ArrayList<String>();
    try {
      BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(new File("" + anchor + ".lex"))));
      String line = in.readLine();
      while( line != null ) {
        lexicon.add(line);
        line = in.readLine();
      }
    } catch( IOException ex ) {
      System.out.println("Lexicon for " + anchor + " does not exist.  Building...");
      return null;
    }
    return lexicon;
  }
 
	/**
   *  gets all the stop words from the text document
   */
	public Set<String> loadStop() {
		Set<String> words = new HashSet<String>();
		String path = rootDirectory + "/stopwords.txt";
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(new File(path))));
			String line = in.readLine();
      while( line != null ) {
				words.add(line);
			  line = in.readLine();
			}
			in.close();
		} catch(IOException ex) { 
			System.err.println("Error opening file: " + path);
			ex.printStackTrace(); 
			return null;
		}
		return words;
	}
	
  /**
   *  gets all the stop words from the text document
   */
	public Set<String> loadEnglish() {
		Set<String> words = new HashSet<String>();
		String path = rootDirectory + "/dict.txt";
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(new File(path))));
			String line = in.readLine();
      while( line != null ) {
				words.add(line);
			  line = in.readLine();
			}
			in.close();
		} catch(IOException ex) { 
			System.err.println("Error opening file: " + path);
			ex.printStackTrace(); 
			return null;
		}
		return words;
  }
	
	/**
	 *  Filters stuff out of a tweet
   */
	public static String filterTweet(String tweet) {
		tweet = tweet.replaceAll("http://[^\\s]+", "");
		return tweet.toLowerCase().replaceAll("[\\,\\.\\!\\@\\#\\$\\%\\^\\&\\*\\_\\-\\+\\=\\?\\\"\\/\\\\\\)\\(]", "");
	}
  
  /**
   * Read a directory and return all files in sorted order.
   */
  private static String[] getZipFilesSorted(String dirPath) {
    List<String> unsorted = getZipFiles(new File(dirPath));

    if( unsorted == null )
      System.out.println("ERROR: Directory.getFilesSorted() path is not known: " + dirPath);

    String[] sorted = new String[unsorted.size()];
    sorted = unsorted.toArray(sorted);
    Arrays.sort(sorted);

    return sorted;
  }

  /**
   * Get all regular files in a directory.
   */
  private static List<String> getZipFiles(File dir) {
    if( dir.isDirectory() ) {
      List<String> files = new ArrayList<String>();
      for( String file : dir.list() ) {
        if( !file.startsWith(".") && file.endsWith(".gz") )
          files.add(file);
      }
      return files;
    }
    return null;
  }
    
  // Debugging only.
  public static void main(String[] args) {
    Datasets data = new Datasets(args[0]);
//    for( LabeledTweet tweet : data.getLabeledTweets() )
//      System.out.println(tweet);
    for( int xx = 0; xx < 4000000; xx++ ) {
    	String tweet = data.getNextRawTweet();
    	if( tweet == null ) {
    		System.out.println("Done at " + xx);
    		break;
    	}
    }
//    System.out.println("tweet: " + data.getRawTweet());
//    System.out.println("tweet: " + data.getRawTweet());
//    System.out.println("tweet: " + data.getRawTweet());
  }
}
