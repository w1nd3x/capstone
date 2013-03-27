package usna.sentiment;

/**
 * A handy-dandy class to hold a single piece of text, and a sentiment label for it.
 * @author Chambers, USNA
 *
 */
public class LabeledTweet {
  public static enum SENTIMENT { POSITIVE, NEGATIVE, OBJECTIVE };

  // The sentiment label of this particular tweet.
  private SENTIMENT sentiment = null;
  // The entire text of this passage, may include newlines.
  private String text = null;

  /**
   * Constructor ... give it some text and a sentiment type.
   */
  public LabeledTweet(SENTIMENT sent, String text) {
    this.text = text;
    this.sentiment = sent;
  }

  /**
   * Accessor functions.
   */
  public String getText() { return text; }
  public SENTIMENT getSentiment() { return sentiment; }

  public void setSentiment(SENTIMENT sent) { sentiment = sent; }

  public String toString() {
    return sentiment.toString() + "\t" + text;
  }
}
