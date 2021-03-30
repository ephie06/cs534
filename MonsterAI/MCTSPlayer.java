import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

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
	
	//This is filter list for estimate the hand that can be played by player i, every filter function will filter the possible cards by one rule.
	//TODO Not Implemented.
	ArrayList<Function<Card, Boolean>> HandFilterPlayer1 = new ArrayList<>(), HandFilterPlayer3 = new ArrayList<>();
	
	MCTSNode(Deck deck, ArrayList<Card> round, ArrayList<Integer> scores, int index, ArrayList<Card> hand, MCTSNode parent) {
		super(deck, round, scores, index);
		// TODO Auto-generated constructor stub
		this.hand = new ArrayList<>(hand);
		rng = ThreadLocalRandom.current();
	}
	
	MCTSNode (State secondCopy, ArrayList<Card> hand, MCTSNode parent) {
		super(secondCopy);
		this.hand = new ArrayList<>(hand);
		this.targetIndex = playerIndex;
		rng = ThreadLocalRandom.current();
	}
	
	
	MCTSNode (MCTSNode secondCopy, MCTSNode parent) {
		super(secondCopy);
		rng = ThreadLocalRandom.current();
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
	
	MCTSNode selection() {
		if (!visited || possibleMoves.size()!=0) {
			return this;
		}
		return children.stream().max(Comparator.comparingDouble(i->i.UCB95())).get().selection();
	}
	
	private void fillPossibleMoves() {
		//TODO
	}
	
	public void makeOneMove(Card c) {
		prevStep = c;
		//TODO
	}
	
	MCTSNode expansion() {
		if (visited!=false) {
			fillPossibleMoves();
			visited = true;
		}
		
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
			backPropagation(reward);
		}
	}
	
	ArrayList<Integer> simulation() {
		//TODO
		return playerScores;
	}
	
	public boolean equals(State obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        MCTSNode that = (MCTSNode) obj;
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
