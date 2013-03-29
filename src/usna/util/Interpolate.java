package usna.util;

import java.util.ArrayList;
import java.util.HashMap;

public class Interpolate {
	
	double sent;
	double flow;
	int maxFlow;
	
	HashMap<String, ArrayList<Double>> vars = new HashMap<String, ArrayList<Double>>();
	HashMap<String, Day> pastDays = new HashMap<String, Day>();
	Day today;
	
	public Interpolate(Day one){
		sent = .6;
		flow = 1 - sent;
		
		today = one;
	}
	
	public void updateInterpolate(){
		
		if(!pastDays.isEmpty()){
			
			double prevSent = vars.get(Integer.getInteger(today.getDate()) - 1).get(0);
			double prevFlow = vars.get(Integer.getInteger(today.getDate()) - 1).get(1);
			double prevScore = pastDays.get(Integer.getInteger(today.getDate()) - 1).getScore();
			double todayChange = today.getScore();
			
			//Wrong Predictions
			if((prevScore > .5 && todayChange < .5) || (prevScore < .5 && todayChange > .5)){
				//which is most different than our current x y z
				if(prevFlow > prevSent && flow > .1 && sent < .9){
					flow = flow - .025;
					sent = sent + .025;
				}
				else if (prevFlow < prevSent && sent > .1 && flow < .9){
					sent = sent - .025;
					flow = flow + .025;
				}
			}
		}
	}
	
	public double calculateSentimentScore(){
		detMax();
		double equation = sent*((today.POS - today.NEG)/today.TOT) + flow*(today.TOT / maxFlow);
		return equation;
	}
	
	public void detMax(){
		if(today.TOT > maxFlow){
			maxFlow = today.TOT;
		}
	}
	
	public double newDay(Day t){
		today = t;
		updateInterpolate();
		today.setScore(calculateSentimentScore());
		pastDays.put(today.getDate(), today);
		return calculateSentimentScore();
	}
	
	public HashMap<String, Day> getDays(){
		return pastDays;
	}

}
