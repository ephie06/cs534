import java.util.LinkedList;

public class Player {
	
	private Card chosenCard;
	private int score;
	private Card played;
	private final int playerNum;

	public static final int PLAYER_ONE = 1;
	public static final int PLAYER_TWO = 2;
	public static final int PLAYER_THREE = 3;
	
	private static final Thread lock = new Thread();
	
	private final LinkedList<Card> hand;
	private final LinkedList<Card>  collected;
	
	/**
	 * Constructor that takes an int 1-3 for the player number
	 * @param num 
	 */
	public Player(int num){
		played = null;
		playerNum = num;
		hand = new LinkedList<Card>();
		collected = new LinkedList<Card>();
	}
	
	/**
	 * Sorts hand by suit and value low to high.
	 */
	public void sortHand(){
		hand.sort(Card.CardComparator);
	}
	
	/**
	 *  Getter for players hand
	 * @return The hand of the player
	 */
	public LinkedList<Card> getHand(){
		return hand;
	}
	
	/**
	 * 
	 * @param played
	 * @return list of legal moves in player's hand
	 */
	public LinkedList<Card> legalMoves(Card played) {
		LinkedList<Card> legal = new LinkedList<Card>();
		if (noneOfSuit(played.getSuit())) { 
			return hand;
		}
		else {
			for(int i=0; i<hand.size(); i++) {
				if(hand.get(i).getSuit()==played.getSuit()) {
					legal.add(hand.get(i));
				}
			} return legal;
		}
	}
	/**
	 * 
	 * @param suit
	 * @return True if there are no cards of given suit in player's hand
	 */
	public boolean noneOfSuit(int suit){
		
		for(int i=0; i<hand.size(); i++){
			if(hand.get(i).getSuit() == suit){
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Adds a card to collected for a won trick
	 * @param card The card to be added
	 */
	public void addToCollected(Card card){
		collected.add(card);		
	}
	
	/**
	 * Getter for collected cards in a won trick
	 * @return the collected cards
	 */
	public LinkedList<Card> getCollected(){
		return collected;
	}
	
	/**
	 * Clears the collected cards of the player
	 */
	public void clearCollected(){
		collected.clear();
	}
	
	/**
	 * Assigns a card from hand to played and removes the card from hand
	 * @param card
	 */
	public void playCard(Card card){
		played = hand.remove(hand.indexOf(card));		
	}
	
	/**
	 * Clears the most recently played card
	 */
	public void clearPlayed(){
		played = null;
	}
	
	/**
	 * 
	 * @return Most recently played card
	 */
	public Card getPlayed(){
		return played;
	}
	
	/**
	 * Adds to the players score
	 * @param n
	 */
	public void addScore(int n){
		score += n;
	}
	
	/**
	 * Score getter
	 * @return the score of the player
	 */
	public int getScore(){
		return score;
	}	
	
	public void printHand(){
		System.out.println("Player number " + playerNum + "'s hand: ");
		for(int i=0; i<hand.size(); i++){
			System.out.print(i+1 + ".\t");
			hand.get(i).printCard();
		}
	}

	public void addToHand(Card card) {
		hand.add(card);
	}
}
