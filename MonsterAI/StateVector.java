import java.util.ArrayList;
import java.util.Vector;

public class StateVector extends Vector<Double> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	Vector<Double> 		stateRep;
	Deck 				cardsPlayed;		
	ArrayList<Card> 	currentRound;
	ArrayList<Double> 	playerScores;
	int 				playerIndex;
	static int 			targetIndex = 1; 
	ArrayList<Card> 	hand;
	RLExhaustTable 		suitExhaustedTable = null; 
	double 				trickPoints;
	double 				highCardTrick;
	Vector<Double> 		exhausts;
	double 				roundNumber;
	
	//TO DO ZOMBIES
	
	StateVector(RLNode state) {
		
		this.cardsPlayed = state.cardsPlayed;	
		this.currentRound = state.currentRound;
		this.playerIndex = state.playerIndex;
		this.hand = state.hand;
		this.suitExhaustedTable = state.suitExhaustedTable;
		this.trickPoints = state.calculatePoints();
		this.exhausts = suitExhaustedTable.toVector();
		this.roundNumber = state.getRoundNumber();
		
		highCardTrick();
		playerScores(state.playerScores);
		
		buildVector();
		
	}

	private void buildVector() {
		
		stateRep = new Vector<Double>();
		
		CardMatrix playerMatrix = new CardMatrix(hand);
		playerMatrix.initHigh();
		playerMatrix.buildMatrixWithHigh();
		Vector<Double> playerCards = playerMatrix.toVector();
		
		// how many cards of each suit the player has, mean value of cards in each suit, high card in each suit
		stateRep.addAll(playerCards);
		
		CardMatrix cardsPlayedMatrix = new CardMatrix(cardsPlayed.allCards);
		cardsPlayedMatrix.initNoHigh();
		cardsPlayedMatrix.buildMatrixWithoutHigh();
		Vector<Double> playedCards = cardsPlayedMatrix.toVector();
		
		//how many cards of each suit have already been played, mean value of cards in each suit that have been played
		stateRep.addAll(playedCards);
		
		//highest value of in-suit card in current trick
		stateRep.add(highCardTrick);	
		
		//point value of current trick
		stateRep.add(trickPoints);
		
		//exhaust binaries for p1 and p3
		stateRep.addAll(exhausts);
		
		//round number
		stateRep.add(roundNumber);
		
		//player scores
		stateRep.addAll(playerScores);
		
		
									
	}
	
	void highCardTrick () {	
		int largestValue = -1;
		if(currentRound.size() > 0) {
		Suit firstSuit = currentRound.get(0).getSuit();
		for (Card c: currentRound) {
			if (c.getSuit() == firstSuit) {
				if (largestValue < c.getValue().val) {
					largestValue = c.getValue().val;
				}
			}
		}
		}

		highCardTrick = largestValue;
	}
	
	void playerScores(ArrayList<Integer> intScore) {
		ArrayList<Double> scores = new ArrayList<Double>();
		for(Integer i : intScore){
		    scores.add(i.doubleValue());
		}
		playerScores = scores;				
	}
}
