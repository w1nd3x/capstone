package usna.util;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class Stock {
	
	private String ticker;
	private HashMap<String, List<String>> stockData;
	private String start;
	private String startYear;
	private String startMonth;
	private String startDay;
	private String end;
	private String endYear;
	private String endMonth;
	private String endDay;
	
	
	public Stock(String tick, String start, String end){
		//Constructor
		stockData = new HashMap<String,List<String> >();
		ticker = tick;
		this.start = start;
		startYear = start.substring(0, 4);
		startMonth = Integer.toString(Integer.parseInt(start.substring(4,6))-1);
		startDay = start.substring(6,8);
		this.end = end;
		endYear = end.substring(0, 4);
		endMonth = Integer.toString(Integer.parseInt(end.substring(4,6))-1);
		endDay = end.substring(6,8);
		//GENERATE CSV FILE
		try{
      this.downloadData();
			this.fillData("table1.csv");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void fillData(String fp) throws Exception {
	  BufferedReader CSVFile = new BufferedReader(new FileReader(fp));

		String dataRow = CSVFile.readLine(); // Read first line.
		// The while checks to see if the data is null. If 
		// it is, we've hit the end of the file. If not, 
		// process the data.
		while (dataRow != null){
			String[] dataArray = dataRow.split(",");
			List<String> x = new ArrayList<String>();
			//Open, High, Low, Close, Adj Close
			x.add(dataArray[1]);
			x.add(dataArray[2]);
			x.add(dataArray[3]);
			x.add(dataArray[4]);
			x.add(dataArray[6]);
			   
			stockData.put(dataArray[0].replaceAll("-",""),x); 

			dataRow = CSVFile.readLine(); // Read next line of data.
		}
		  
	  // Close the file once all data has been read.
		CSVFile.close();

		// End the printout with a blank line.
		System.out.println();
	}
  /**
   *  The timestamps of the stock data.
   *  
   *  @return set of keys
   */
  public ArrayList<String> keyList() {
    ArrayList<String> result = new ArrayList<String>();
    int start = Integer.parseInt(this.start);
    int end = Integer.parseInt(this.end);
    for(int i = start; i < end; i++) {
      if(stockData.get(Integer.toString(i)) != null)
        result.add(Integer.toString(i));
    } 
    return result;
  }
	
	public List<String> getData(String date){
		return stockData.get(date);
	}

  public void downloadData(){
    try{
      URL website = new URL("http://ichart.finance.yahoo.com/table.csv?s="
 + ticker + "&a=" + startMonth + "&b=" + startDay + "&c=" + startYear + "&d=" + endMonth + "&e=" + endDay + "&f=" + endYear + "&g=d&ignore=.csv");
      ReadableByteChannel rbc = Channels.newChannel(website.openStream());
      FileOutputStream fos = new FileOutputStream("table1.csv");
      fos.getChannel().transferFrom(rbc, 0, 1 << 24);
    }
    catch(Exception e){ e.printStackTrace(); }
  }


/*
	public static void main(String[] args){
    Scanner s = new Scanner(System.in);
    System.out.println("Enter your stock:");

		Stock aapl = new Stock("AAPL","20120328","20130329");

		System.out.println("Query map: (YYYY-MM-DD):");
		
		System.out.println(aapl.getData("20130328"));
	}
*/	

}



