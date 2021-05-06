import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

public class CardMatrix extends HashMap<Suit, double[]>{
	
	private static final long serialVersionUID = 1L;
	Vector<Double> cardVector;
	ArrayList<Card> cards;
	
	public CardMatrix(ArrayList<Card> cards) {
		super();
		this.cards = cards;
	}		
	
		void initHigh() {		
			put(Suit.ZOMBIES, new double[3]);
			put(Suit.TROLLS, new double[3]);
			put(Suit.FAIRIES, new double[3]);
			put(Suit.UNICORNS, new double[3]);
		}
		
		void initNoHigh() {		
			put(Suit.ZOMBIES, new double[2]);
			put(Suit.TROLLS, new double[2]);
			put(Suit.FAIRIES, new double[2]);
			put(Suit.UNICORNS, new double[2]);
		}
		
		void copyFrom(HashMap<Suit, double[]> table) {
			for (Suit key: keySet()) {
				for (int i=0; i<3; i++) {
					get(key)[i] = table.get(key)[i];
				}
			}
		}
		
//		void logicOr(HashMap<Suit, int[]> table) {
//			for (Suit key: keySet()) {
//				for (int i=0; i<3; i++) {
//					get(key)[i] = get(key)[i] || table.get(key)[i];
//				}
//			}
//		}
//		
//		CardMatrix deepCopy() {
//			CardMatrix n = new CardMatrix();
//			n.copyFrom(this);
//			return n;
//		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (Suit key: keySet()) {
				switch (key) {
					case UNICORNS: sb.append("🦄\t"); break;
					case FAIRIES: sb.append ("🧚\t"); break;
					case TROLLS: sb.append( "👺\t"); break;
					case ZOMBIES: sb.append("\uD83E\uDDDF\t"); break;
				}

				for (int i=0; i<3; i++) {
					sb.append(get(key)[i]).append("\t");
				}
				sb.append("\n");
			}
			return sb.toString();
		}
	
	//counts the number of cards of a suit a list of cards
	int numCardsOfSuit(ArrayList<Card> cards, Suit suit) {
		int numCards = 0;
		for (Card c : cards) {
			if (c.getSuit() == suit) numCards++;
		}
		return numCards;
	}
	
	double meanOfCards(ArrayList<Card> cards, Suit suit) {
		double total = 0;
		int numCards = 0;
		for (Card c : cards) {
			if(c.getSuit() == suit) {
				total += c.getValue().val;
				numCards++;
			}
		}
		return total/numCards;
	}
	
//	double variance(ArrayList<Card> cards, Suit suit) {
//		double mean = meanOfCards(cards, suit);
//		double sumOfSqDf = 0;
//		int numCards = 0;
//		for (Card c : cards) {
//			if(c.getSuit() == suit) {
//			int val = c.getValue().val;
//			sumOfSqDf += (val - mean)*(val - mean);	
//			numCards++;
//			}
//		}
//		return sumOfSqDf/numCards;
//	}
	
	int highCard(ArrayList<Card> cards, Suit suit) {
		int largestValue = -1;
		for (Card c : cards) {
			if(c.getSuit() == suit && c.getValue().val > largestValue) largestValue = c.getValue().val;
		}
		return largestValue;
	}
	
	void buildMatrixWithHigh() {	
		this.get(Suit.ZOMBIES)[0] = numCardsOfSuit(cards, Suit.ZOMBIES);
		this.get(Suit.ZOMBIES)[1] = meanOfCards(cards, Suit.ZOMBIES);
		this.get(Suit.ZOMBIES)[2] = highCard(cards, Suit.ZOMBIES);
		this.get(Suit.TROLLS)[0] = numCardsOfSuit(cards, Suit.TROLLS);
		this.get(Suit.TROLLS)[1] = meanOfCards(cards, Suit.TROLLS);
		this.get(Suit.TROLLS)[2] = highCard(cards, Suit.TROLLS);
		this.get(Suit.FAIRIES)[0] = numCardsOfSuit(cards, Suit.FAIRIES);
		this.get(Suit.FAIRIES)[1] = meanOfCards(cards, Suit.FAIRIES);
		this.get(Suit.FAIRIES)[2] = highCard(cards, Suit.FAIRIES);
		this.get(Suit.UNICORNS)[0] = numCardsOfSuit(cards, Suit.UNICORNS);
		this.get(Suit.UNICORNS)[1] = meanOfCards(cards, Suit.UNICORNS);
		this.get(Suit.UNICORNS)[2] = highCard(cards, Suit.UNICORNS);	
	}
	
	void buildMatrixWithoutHigh() {	
		this.get(Suit.ZOMBIES)[0] = numCardsOfSuit(cards, Suit.ZOMBIES);
		this.get(Suit.ZOMBIES)[1] = meanOfCards(cards, Suit.ZOMBIES);
		this.get(Suit.TROLLS)[0] = numCardsOfSuit(cards, Suit.TROLLS);
		this.get(Suit.TROLLS)[1] = meanOfCards(cards, Suit.TROLLS);
		this.get(Suit.FAIRIES)[0] = numCardsOfSuit(cards, Suit.FAIRIES);
		this.get(Suit.FAIRIES)[1] = meanOfCards(cards, Suit.FAIRIES);
		this.get(Suit.UNICORNS)[0] = numCardsOfSuit(cards, Suit.UNICORNS);
		this.get(Suit.UNICORNS)[1] = meanOfCards(cards, Suit.UNICORNS);	
	}
	
	Vector<Double> toVector() {
		Vector<Double> cardsVector = new Vector<Double>();
		Iterator<Map.Entry<Suit, double[]>> entrySet = this.entrySet().iterator();
		while (entrySet.hasNext()){
            Map.Entry<Suit, double[]> entry = entrySet.next();
            double[] thisEntry = entry.getValue();
            for (double d : thisEntry) { cardsVector.add(d); }
        }
         return cardsVector;   
    }
	

}
