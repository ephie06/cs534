class Card implements Comparable<Card> {

	Suit suit;
	Value value;
	String shorthand;

	// All cards MUST be constructed with a suit and value!
	Card (Suit thisSuit, Value thisValue) { 
		suit = thisSuit; 
		value = thisValue;
		setShorthand();
	}

	// Getter functions for the suit, value, and identity of this card
	Suit getSuit() { return suit; }
	Value getValue() { return value; }
//	String printCard() { return getValue() + " of " + getSuit(); }
	String printCard() { return shorthand; }

	// Overriden method for comparing cards by suit (for sorting hands)
	public int compareTo (Card other) {
		if (suit.compareTo(other.getSuit()) == 0) 
			return value.compareTo(other.getValue());
		return suit.compareTo(other.getSuit());
	}

	// Overriden method to check if cards are the same
	public boolean equals(Card other) {
		return suit.equals(other.getSuit()) && value.equals(other.getValue());
	}

	// Allow a copy function for cards
	Card copy() { return new Card(suit, value); } 
	
	void setShorthand () {
		shorthand = "";
		switch(value) {
			case ZERO: shorthand += "0"; break;
			case ONE: shorthand += "1"; break;
			case TWO: shorthand += "2"; break;
			case THREE: shorthand += "3"; break;
			case FOUR: shorthand += "4"; break;
			case FIVE: shorthand += "5"; break;
			case SIX: shorthand += "6"; break;
			case SEVEN: shorthand += "7"; break;
			case EIGHT: shorthand += "8"; break;
			case NINE: shorthand += "9"; break;
			case TEN: shorthand += "10"; break;
			case ELEVEN: shorthand += "11"; break;
			case TWELVE: shorthand += "12"; break;
			case THIRTEEN: shorthand += "13"; break;
			case FOURTEEN: shorthand += "14"; break;
		}
		switch(suit) {
			case UNICORNS: shorthand += "🦄"; break;
			case FAIRIES: shorthand += "🧚"; break;
			case TROLLS: shorthand += "👺"; break;
			case ZOMBIES: shorthand += "\uD83E\uDDDF"; break;
		}
	}

}
