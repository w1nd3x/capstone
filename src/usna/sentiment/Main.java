package usna.sentiment;


public class Main {




  public static void main(String args[]) {
    Map<String, String> flags = CommandLineUtils.simpleCommandLineParser(args);
    if (!flags.containsKey("-data")) {
      System.out.println("TrainTest -data <data-dir> [-learnmylex] [-usemylex]");
			System.exit(1);
		}

		if (flags.containsKey("-learnmylex") && flags.containsKey("-usemylex")) {
			System.out.println("Hmm, can't learn a lexicon and use the lexicon at the same time.");
			System.exit(1);
		} else if (flags.containsKey("-learnmylex")) {
			anchor = flags.get("-learnmylex");
			learnLexicon = true;
		} else if (flags.containsKey("-usemylex"))
			testWithLearned = true;

		datasets = new Datasets(flags.get("-data"));
	}

}
