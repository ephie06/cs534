
import java.util.ArrayList;
import java.util.Vector;
import java.util.stream.Stream;

public class StateVector {


/**
	 * State Vector takes a state and outputs the following stateRep of 27 Integers: How many cards of each
	 * suit the player has and their mean value rounded down [8], how many cards of each suit have been
	 * played and their mean value rounded down [8], point value of the current trick [1], exhaust table
	 * binaries for p1 and p3 [8], binary can the player beat the current high card in the trick [1], point
	 * difference between the player and the highest of the p1 and p3 [1]. Suit exhaust tables and point difference
	 * are currently dependent on the player being in position 2 in the game...
	 */


	private static final long serialVersionUID = 1L;

	Vector<Integer> 	stateRep;
	Deck 				cardsPlayed;
	ArrayList<Card> 	currentRound;
	ArrayList<Integer> 	playerScores;
	int 				playerIndex;
	static int 			targetIndex = 1;
	ArrayList<Card> 	hand;
	ExhaustTable 		suitExhaustedTable = null;
	int 				trickPoints;
	int 				highCardTrick;
	Vector<Integer> 	exhausts;
//	int 				roundNumber;

	//TO DO ZOMBIES

	StateVector(MCRLGameState state) {

		cardsPlayed = state.cardsPlayed;
		currentRound = state.currentRound;
		playerIndex = state.playerIndex;
		hand = state.hand;
		suitExhaustedTable = state.suitExhaustedTable;
		trickPoints = state.calculatePoints();
		exhausts = suitExhaustedTable.toVector();
		playerScores = state.playerScores;
//		this.roundNumber = state.getRoundNumber();

		highCardTrick();
		buildVector();

	}

	private void buildVector() {

		stateRep = new Vector<Integer>();

		CardMatrix playerMatrix = new CardMatrix(hand);
		playerMatrix.init();
		playerMatrix.buildMatrix();
		Vector<Integer> playerCards = playerMatrix.toVector();

		// how many cards of each suit the player has, mean value of cards in each suit
		stateRep.addAll(playerCards);

		CardMatrix cardsPlayedMatrix = new CardMatrix(cardsPlayed.allCards);
		cardsPlayedMatrix.init();
		cardsPlayedMatrix.buildMatrix();
		Vector<Integer> playedCards = cardsPlayedMatrix.toVector();

		//how many cards of each suit have already been played, mean value of cards in each suit that have been played
		stateRep.addAll(playedCards);

		//point value of current trick
		stateRep.add(trickPoints); //TODO: do we want this?

		//exhaust binaries for p1 and p3: this is a problem when player is not in middle position
		stateRep.addAll(exhausts);

		//binary if player can beat high card of suit in trick
		stateRep.add(canBeat());

		//round number: I think this might be redundant
//		stateRep.add(roundNumber);

		//player scores
//		stateRep.addAll(playerScores);

		//diff between middle player and highest of other two players
		stateRep.add(leadingBy());


		int zombieCount = (int) cardsPlayed.allCards.stream().filter(i -> i.getSuit() == Suit.ZOMBIES).count();
		stateRep.add(zombieCount);

	}

	int highCardTrick () { //highest value & valid card currently in trick
		int largestValue = -1;
		if(currentRound.size() > 0) {
			Suit firstSuit = currentRound.get(0).getSuit();
			for (Card c: currentRound) {
				if (c.getSuit() == firstSuit) {
					if (largestValue < c.getValue().val) {
						largestValue = c.getValue().val;
					}
				}
			} return largestValue;
		}
		return 0;

	}

	int highCardInSuit() { //Highest value & valid card in hand that can be played in trick
		int largestValue = -1;
		if(currentRound.size() > 0) {
			Suit firstSuit = currentRound.get(0).getSuit();
			for (Card c: hand) {
				if (c.getSuit() == firstSuit) {
					if (largestValue < c.getValue().val) {
						largestValue = c.getValue().val;
					}
				}
			}return largestValue;
		}
		return 1;

	}

	int canBeat() { //can MCRL win the trick?
		if (highCardInSuit() > highCardTrick()) return 1;
		else return 0;
	}

	int leadingBy() {
		int rlScore = playerScores.get(1);
		int high = Math.max(playerScores.get(0), playerScores.get(2));
		return rlScore - high;
	}



//	void playerScores(ArrayList<Integer> intScore) {
//		ArrayList<Integer> scores = new ArrayList<Integer>();
//		for(Integer i : intScore){
//		    scores.add(i);
//		}
//		playerScores = scores;
//	}


}

