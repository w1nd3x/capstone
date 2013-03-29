package usna.util;

import java.util.List;

public class Day {
	
	String date;
	
	int OBJ;
	int POS;
	int NEG;
	int TOT;
	double openPrice;
	double closePrice;
	double diffPrice;
	Counter<String> unigram = new Counter<String>();
	Counter<String> bigram = new Counter<String>();
	Counter<String> trigram = new Counter<String>();
	
	private double score = 0;
	
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
	
	public String getDate(){
		return date;
	}

}
