package usna.util;

import java.util.List;
import java.io.*;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

public class Day {
	
	String date;
	
	double OBJ;
	double POS;
	double NEG;
	double TOT;
	double openPrice;
	double closePrice;
	double diffPrice;
	Counter<String> unigram = new Counter<String>();
	Counter<String> bigram = new Counter<String>();
	Counter<String> trigram = new Counter<String>();

  	double diffSlope;
	
	private double score = 0;


  public Day() {
		date = null;
		POS = 0;
		NEG = 0;
		OBJ = 0;
		TOT = 0;
		openPrice = 0;
		closePrice = 0;
		diffPrice = 0;
    score = 0;
  }
	public Day(List<String> stockData, List<Integer> tweetData, String d){
		
		date = d;
		
		POS = tweetData.get(0);
		NEG = tweetData.get(1);
		OBJ = tweetData.get(2);
		TOT = tweetData.get(3);
		
		openPrice = Double.parseDouble(stockData.get(0));
		closePrice = Double.parseDouble(stockData.get(3));
		
		diffPrice = closePrice - openPrice;
	}
	
	public void setScore(double x){
		score = x;
	}
	
	public double getScore(){
		return score;
	}

	public double getdiffSlope(){
		return diffSlope;
	}
	
	public String getDate(){
		return date;
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

}
