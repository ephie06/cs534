import java.util.Comparator;

public class CardComparator implements Comparator {
	
	public CardComparator() {}
	
	private void checkComparable(Card c) /*throws InvalidObjectException*/ {
		if (!(c instanceof Card)) {
			//throw new InvalidObjectException("This object is not a PlayingCard. Cannot compare.");
			System.out.println("This object is not a PlayingCard. Cannot compare.");
			System.exit(1);
		}
	}

	/**
	 * 
	 * @param c1
	 * @param c2
	 * @return If cards are the same suit returns true if c1 > c2, otherwise returns true if 
	 * c1 suit is a lower int than c2 (unicorns=0, fairies=1, zombies=2, trolls=3)
	 */
	private boolean isGreater(Card c1, Card c2) {
		
		int face1 = c1.getValue();
		int face2 = c2.getValue();
		
		if (c1.getSuit() == c2.getSuit()) {
			return face1 >= face2;
		} else {
			return (c1.getSuit() < c2.getSuit());
		}
		
	}
	
	private boolean isLessThan(Card c1, Card c2) {
		return !isGreater(c1, c2);
	}
	
	public int compare(Object o1, Object o2) {
		
		checkComparable((Card) o1);
		checkComparable((Card) o2);

		if (isLessThan((Card) o1, (Card) o2)) {
			return -1;
		} else if (isGreater((Card) o1, (Card) o2)) {
			return 1;
		} else {
			return 0;
		}
	}
	
	public boolean greaterValue(Object o1, Object o2) {
		
		checkComparable((Card) o1);
		checkComparable((Card) o2);
		
		int card1 = ((Card) o1).getValue();
		int card2 = ((Card) o2).getValue();
		
		return card1 > card2;
	}
}
