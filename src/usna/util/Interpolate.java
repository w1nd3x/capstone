package usna.util;

import java.util.ArrayList;
import java.util.HashMap;

public class Interpolate {
	
	double sent;
	double flow;
	double past; //day before
	double prevScore;
	double maxFlow;
  double maxDiff;
  double minDiff;
  double correct;
  double incorrect;
	
	HashMap<String, Day> pastDays = new HashMap<String, Day>();
	Day today;
  Day yesterday;
	
	public Interpolate(){
		sent = .5;
		flow = .3;
		past = 1 - sent - flow;
    today = new Day();
	}
	
	public void updateInterpolate(){
		if(!pastDays.isEmpty()){
			//Wrong Predictions
			if((prevScore > .5 && today.diffPrice < 0) || (prevScore < .5 && today.diffPrice > 0)){
				//which is most different than our current x y z
				if(sent > flow && sent > past && flow < .9 && sent > .1 && past < .9){
					sent = sent - .05;
					flow = flow + .025;
					past = past + .025;					
				}
				else if (flow > sent && flow > past && sent < .9 && flow > .1 && past > .1){
					sent = sent + .025;
					flow = flow - .05;
					past = past + .025;
				}
				else if (past > flow && past > sent && sent > .1 && flow > .1 && past < .9){
					sent = sent + .025;
					flow = flow + .025;
					past = past - .05;
				}
			}
		} else
			maxFlow = today.TOT;
	}
	
	public double calculateSentimentScore(){
		detMax();
		double equation = sent*((today.POS)/(today.POS + today.NEG)) + flow*(today.TOT / maxFlow) + past*(today.diffSlope);
		return equation;
	}
	
	public void detMax(){
		if(today.TOT > maxFlow){
			maxFlow = today.TOT;
		}
	}
	
	public double newDay(Day t){
    yesterday = today;
		today = t;
    today.diffSlope = calculateDifference();
		updateInterpolate();
		today.setScore(calculateSentimentScore());
		prevScore = yesterday.getScore();
    printInfo(today);
		pastDays.put(today.getDate(), today);
    return today.getScore();
	}

  private void printInfo(Day t) {
		System.out.println("Positive tweets: " + t.POS);
		System.out.println("Negative tweets: " + t.NEG);
		System.out.println("Objective tweets: " + t.OBJ);
		System.out.println("Total tweets: " + t.TOT);
    System.out.println();
    System.out.println("Sent Weight: " + sent);
    System.out.println("Flow Weight: " + flow);
    System.out.println("Past Weight: " + past);
    System.out.println();
    if(prevScore > .5) {
      if(t.diffPrice > 0) {
        System.out.printf("Correct!\nYesterday's Score: %f\nt's Change: %f\n",prevScore,t.diffPrice);
        correct++;
      }
      else  {
        System.out.printf("Incorrect\nYesterday's Score: %f\nt's Change: %f\n",prevScore,t.diffPrice);
        incorrect++;
      }
    } else {
      if(t.diffPrice < 0) {
        System.out.printf("Correct!\nYesterday's Score: %f\nt's Change: %f\n",prevScore,t.diffPrice);
        correct++;
      }
      else {
        System.out.printf("Incorrect\nYesterday's Score: %f\nt's Change: %f\n",prevScore,t.diffPrice);
        incorrect++;
      }
    }
    System.out.println("Current Accuracy: " + correct/(correct+incorrect));
    System.out.println("Score: " + t.getScore());
    System.out.println("---------------------------------------------------------");
  }
	
	public HashMap<String, Day> getDays(){
		return pastDays;
	}
	
	public Day getDay(String x){
		return pastDays.get(x);
	}

  public double calculateDifference() {
      if(today.diffPrice > 0) {
        if(today.diffPrice > maxDiff) {
          maxDiff = today.diffPrice;
        }
        return .5 + ( .5*(today.diffPrice/maxDiff) );
      } else if(today.diffPrice < 0) {
        if(today.diffPrice < minDiff) {
          minDiff = today.diffPrice;
        }
        return .5 - ( .5*(today.diffPrice/minDiff) );
      }
      return .5;
    }

}
