import java.util.Random;

public class DeckOfCards {

	private Card[] deck;
	private int size;


/**
 * Constructor for new shuffled deck.	
 */
public DeckOfCards() {
	startDeck();
	shuffleDeck();
}
	
//constructor helper
public void startDeck() {
	deck = new Card[60];
	for (int i=0; i<4; i++) {
		for (int j=0; j<15; j++) {
			deck[i*15 + j] = new Card(i, j);
		}
		size = 60;
	}
}

/**
 * Shuffles the deck of cards
 */
public final void shuffleDeck() {
	Random r = new Random();
	int index;
	Card temp;
	int size = deck.length;
	
	for (int i=0; i<5; i++) {
		for (int j=0; j<size; j++) {
			index = r.nextInt(size);
			temp = deck[j];
			deck[j] = deck[index];
			deck[index] = temp;
		}
	}
}
/**
 * Deck setter
 * @param deck Array of Card
 */
public void setDeck(Card[] deck) {
	this.deck = deck;
}

/** 
 * Deck getter
 * @return Array of Card
 */
public Card[] getDeck() {
	return this.deck;
}

/**
 * prints cards in deck to console
 */
public void printDeck() {
	int size = deck.length;
	for(int i=size-1; i>=0; i--) {
		deck[i].printCard();
	}
}

public Card drawCard() {
	if(size == 0){
		throw new DeckException("Cannot draw card from empty deck.");
	}
	
	Card card = deck[--size];
	deck[size] = null;
	return card;
}

}

