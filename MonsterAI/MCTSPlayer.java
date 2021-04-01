import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

class MCTSNode extends State {
	
	boolean visited = false;
	int targetIndex = 2; // the player index that the hand belong to (the player we want it to win) 
	ArrayList<Card> hand;
	int totalValue = 0;
	int numObserved = 0;
	ArrayList<MCTSNode> children = new ArrayList<>();
	ArrayList<Card> possibleMoves = new ArrayList<>();
	MCTSNode parent = null;
	Card prevStep = null;
	
	MCTSNode(Deck deck, ArrayList<Card> round, ArrayList<Integer> scores, int index, ArrayList<Card> hand, MCTSNode parent) {
		super(deck, round, scores, index);
		// TODO Auto-generated constructor stub
		this.hand = new ArrayList<>(hand);
		this.parent = parent;
		rng = ThreadLocalRandom.current();
		fillPossibleMoves();
	}
	
	MCTSNode (State secondCopy, ArrayList<Card> hand, MCTSNode parent) {
		super(secondCopy);
		this.hand = new ArrayList<>(hand);
		this.targetIndex = playerIndex;
		this.parent = parent;
		rng = ThreadLocalRandom.current();
		fillPossibleMoves();
	}
	
	
	MCTSNode (MCTSNode secondCopy, MCTSNode parent) {
		super(secondCopy);
		this.hand = new ArrayList<>(secondCopy.hand);
		this.targetIndex = playerIndex;
		this.parent = parent;
		rng = ThreadLocalRandom.current();
		fillPossibleMoves();
	}
	
	public double meanValue() {
		if (numObserved==0) return 0;
		return (double)totalValue/numObserved;
		
	}
	
	public double UCB95() {
		if (numObserved == 0) {
			return Double.POSITIVE_INFINITY;
		}
		return meanValue() + 2 * Math.sqrt(Math.log(parent.numObserved)/numObserved); 
	}
	
	void playCard(Card c, int index) {
		playCard(c);
		if (index == targetIndex) {
			hand.remove(c);
		}
	}
	
	ArrayList<Integer> terminalValue() {
		return playerScores;
	}
	
	// Given a suit, check if the hand has that suit
	boolean checkSuit(Suit check) {
		boolean flag = false;
		if (check == null) return false;
		for (Card c: hand) { if (c.getSuit() == check) flag = true; }
		return flag;
	}

	// Get the first suit that was played this round
	Suit getFirstSuit(ArrayList<Card> currentRound) {
		if (currentRound.size() == 0) return null;
		return currentRound.get(0).getSuit();
	}
	
	@Override
	boolean isGameValid() {
		return super.isGameValid() && playerScores.stream().allMatch(i->i<200);
	}
	
	private void fillPossibleMoves() {
		possibleMoves.clear();
		if (!isGameValid()) {
			return; 
		}
		
		if (playerIndex == targetIndex) {
			Suit firstSuit = getFirstSuit(currentRound);
			// If no cards were played this round, play a random card 
			if (firstSuit == null) {
				possibleMoves.addAll(hand);
			} else {
				if (checkSuit(firstSuit)) {
					possibleMoves.addAll(hand.stream().filter(i->(i.getSuit()==firstSuit)).collect(Collectors.toList()));
				} else {
					possibleMoves.addAll(hand);
				}
			}
		} else {
			possibleMoves.addAll(cardsPlayed.invertDeck);
			for (var c:hand) {
				possibleMoves.remove(c);
			}
		}
		Collections.shuffle(possibleMoves, rng);
	}
	
	private void makeOneMove(Card c) {
		prevStep = c;
		playCard(c, this.playerIndex);
		if (validRound()) {
			playerIndex = (playerIndex+1) % playerScores.size();
		} else {
			// Round has ended -- check what points have gone where and determine who goes next (use playerScores)
			int firstPlayer = (playerIndex + 1) % playerScores.size();
			int points = calculatePoints();
			int taker = findTaker(firstPlayer);
			playerScores.set(taker, playerScores.get(taker)+points);
			
			// Clear the cards on the table (don't worry, pointers to them are tracked in the cardsPlayed deck)
			currentRound.clear();

			// If game end.
			if (!isGameValid()) {
				playerScores.set(taker, playerScores.get(taker)+3); // give a expectation of the score of undealt cards.
			}
			
			playerIndex = taker;
		}
		fillPossibleMoves();
		
	}
	
	MCTSNode selection() {
		if (possibleMoves.size()!=0) {
			return this;
		}
		if (children.size()==0) {
			return this;
		}
		return children.stream().max(Comparator.comparingDouble(i->i.UCB95())).get().selection();
	}
	
	MCTSNode expansion() {

		if (possibleMoves.size()==0) {
			return this;
		}
		
		var newNode = new MCTSNode(this, this);
		Card card = possibleMoves.get(possibleMoves.size()-1);
		possibleMoves.remove(possibleMoves.size()-1);
		newNode.makeOneMove(card);
		children.add(newNode);
		return newNode;
	}
	
	void backPropagation(ArrayList<Integer> reward) {
		totalValue += reward.get(playerIndex);
		numObserved += 1;
		if (parent!=null) {
			parent.backPropagation(reward);
		}
	}
	
	ArrayList<Integer> simulation() {
		MCTSNode temp = new MCTSNode(this, this);
		while(temp.possibleMoves.size()!=0) {
			temp.makeOneMove(temp.possibleMoves.get(0)); //possibleMoves already shuffled.
		}
		return temp.terminalValue();
	}
	
	public boolean equals(State obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        State that = obj;
        cardsPlayed.invertDeck.sort(null);
        that.cardsPlayed.invertDeck.sort(null);
        return playerIndex == that.playerIndex
                && currentRound.equals(that.currentRound)
                && playerScores.equals(that.playerScores)
                && cardsPlayed.invertDeck.equals(that.cardsPlayed.invertDeck);

    }
	
}

public class MCTSPlayer extends Player {

	public long timeLimitInMillis = 1000; 
	
	MCTSPlayer(String id, long timeLimitInMillis) {
		super(id);
		// TODO Auto-generated constructor stub
		this.timeLimitInMillis = timeLimitInMillis;
	}

	MCTSNode root = null;
	
	@Override
	boolean setDebug() {
		return false;
	}

	@Override
	Card performAction(State masterCopy) {
		
		found: {
			if (root == null) {
				root = new MCTSNode(masterCopy, hand, null);
			} else {
				for (var c: root.children) {
					if (c.equals(masterCopy)) {
						root = c;
						break found;
					}
					for (var d: c.children) {
						if (d.equals(masterCopy)) {
							root = d;
							break found;
						}
					}
				}
				root = new MCTSNode(masterCopy, hand, null);
			}
		}
		root.parent = null;
		
		long start = System.currentTimeMillis();
		
		while (System.currentTimeMillis() - start < timeLimitInMillis) {
			var tNode = root.selection();
			var cNode = tNode.expansion();
			var rewards = cNode.simulation();
			cNode.backPropagation(rewards);
		}
		
		Card card = root.children.stream().max(Comparator.comparingDouble(i->i.meanValue())).get().prevStep;
		hand.remove(card);
		return card;
	}

}
