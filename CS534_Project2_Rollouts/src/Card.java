import java.util.Comparator;

public class Card {
	
	public final static int UNICORNS = 0;
	public final static int FAIRIES = 1;
	public final static int ZOMBIES = 2;
	public final static int TROLLS = 3;
	
	public final static Comparator CardComparator = new CardComparator();
	
	int cardSuit;
	int cardValue;
	
	//constructor
	public Card(int suit, int val) {
		cardSuit = suit;
		cardValue = val;
	}
	
	/**
	 * returns equality based on suit and value
	 * @param card
	 * @return true if cards have the same suit and value
	 */
	public boolean equals(Card card){
		return cardSuit == card.cardSuit && cardValue == card.cardValue;
	}
	
	/**
	 * 
	 * @return suit of the Card as an int
	 */
	public int getSuit(){	
		return cardSuit;
	}
	
	/**
	 * 
	 * @return value of the card
	 */
	public int getValue(){		
		return cardValue;
	}
	
	/**
	 * 
	 * @param otherCard Card being compared
	 * @return true if cards are the same suit
	 */
	public boolean sameSuit(Card otherCard) {
		return cardSuit == otherCard.cardSuit;
	}
	
	/**
	 * 
	 * @param otherCard Card being compared
	 * @return true if cards are the same suit and this card has a higher value
	 */
	public boolean isHigherSameSuit(Card otherCard) {
		return sameSuit(otherCard) && cardValue > otherCard.cardValue;
	}
	
	/**
	 * 
	 * @return suite as a string
	 */
	public String getSuitString(){
		String str;
		
		switch(cardSuit){
			case UNICORNS:
				str = "Unicorns";
				break;
			case FAIRIES:
				str = "Fairies";
				break;
			case ZOMBIES:
				str = "Zombies";
				break;
			case TROLLS:
				str = "Trolls";
				break;
			default:
				str = "Blank";
				break;
		}
		
		return str;
	}

	/**
	 * prints card to console with it's value and suit as string
	 */
	public void printCard() {
		if(this == null) { System.out.println("null"); }
		System.out.print(getValue() + " of " + getSuitString() + ", ");
	}
}
