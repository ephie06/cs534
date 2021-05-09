import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;


class MCTSNode extends State {

	boolean visited = false;
	int targetIndex = 1; // the player index that the hand belong to (the player we want it to win)
	ArrayList<Card> hand;
	double totalValue = 0;
	int numObserved = 0;
	ArrayList<MCTSNode> children = new ArrayList<>();
	ArrayList<Card> possibleMoves = new ArrayList<>();
	MCTSNode parent = null;
	Card prevStep = null;
	ExhaustTable suitExhaustedTable = null;

	MCTSNode(Deck deck, ArrayList<Card> round, ArrayList<Integer> scores, int index, ArrayList<Card> hand, MCTSNode parent, int targetIndex) {
		super(deck, round, scores, index);
		// TODO Auto-generated constructor stub
		this.targetIndex= targetIndex;
		this.hand = new ArrayList<>(hand);
		this.parent = parent;
		suitExhaustedTable = new ExhaustTable();
		updateExhaustTable();
		rng = ThreadLocalRandom.current();
		fillPossibleMoves();
	}

	MCTSNode (State secondCopy, ArrayList<Card> hand, MCTSNode parent, int targetIndex) {
		super(secondCopy);
		this.hand = new ArrayList<>(hand);
		this.parent = parent;
		this.targetIndex = targetIndex;
		suitExhaustedTable = new ExhaustTable();
		updateExhaustTable();
		rng = ThreadLocalRandom.current();
		fillPossibleMoves();
	}


	MCTSNode (MCTSNode secondCopy, MCTSNode parent) {
		super(secondCopy);
		this.hand = new ArrayList<>(secondCopy.hand);
		this.parent = parent;
		this.targetIndex = secondCopy.targetIndex;
		suitExhaustedTable = secondCopy.suitExhaustedTable.deepCopy();
		updateExhaustTable();
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

	ArrayList<Double> terminalValue() {
		ArrayList<Double> e = new ArrayList<>();
		for (int i =0; i<3; i++) {
			e.add((double)playerScores.get(i).intValue());
		}
		double maxV =-10000;
		double secV =-10000;
		for (int i = 0; i<3; i++) {
			if (e.get(i) > secV) {
				secV = e.get(i);
				if (maxV < secV) {
					double a = secV;
					secV = maxV;
					maxV = a;
				}
			}
		}

		for (int i=0; i<e.size(); i++) {
			double reward = 0.8/(maxV - e.get(i) + 1);
			if (Double.compare(e.get(i), maxV)==0) {
				reward += 0.02 * (e.get(i) - secV);
			}
			if (reward>1.4) reward = 1.4;
			e.set(i, reward);
		}
		return e;
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

	//only check currentRound.
	void updateExhaustTable() {
		if (currentRound.size()>1) {
			Suit firstSuit = getFirstSuit(currentRound);
			int firstPlayer = (playerIndex - currentRound.size() + 3) % 3;
			for (int i=1; i<currentRound.size(); i++) {
				if (currentRound.get(i).getSuit() != firstSuit) {
					suitExhaustedTable.get(firstSuit)[(firstPlayer + i) % 3] = true;
				}
			}
		}
	}

	@Override
	boolean isGameValid() {
		return super.isGameValid() && playerScores.stream().allMatch(i->i<200);
	}

	boolean inSim = false;

	private void fillPossibleMoves() {
		possibleMoves.clear();
		if (!isGameValid()) {
			return;
		}

		Suit firstSuit = getFirstSuit(currentRound);

		if (playerIndex == targetIndex) {
			if (firstSuit != null && checkSuit(firstSuit)) {
				possibleMoves = hand.stream().filter(i->(i.getSuit()==firstSuit)).collect(Collectors.toCollection(ArrayList::new));
			} else {
				possibleMoves.addAll(hand);
			}

		} else {
			possibleMoves = cardsPlayed.invertDeck.stream().filter(i->!hand.contains(i)).collect(Collectors.toCollection(ArrayList::new));

			if (inSim && firstSuit!=null && suitExhaustedTable.get(firstSuit)[playerIndex]==false) {
				List<Card> thisSuit = possibleMoves.stream().filter(i->(i.getSuit()==firstSuit)).collect(Collectors.toList());
				//With a probability calculated by size of 'hand', 'thisSuit' and 'possibleMoves', we filter the PossibleMoves with the suit.
				int suitCount = thisSuit.size();
				int totalCount = possibleMoves.size();
				int handCount = (totalCount+1)/2;
				double pGotACardOtherSuit = 1- (double)suitCount/totalCount;
				double pAllCardOtherSuit = Math.pow(pGotACardOtherSuit, handCount);
				if (rng.nextDouble() < 1 - pAllCardOtherSuit) {
					possibleMoves.clear();
					possibleMoves.addAll(thisSuit);
				}
			}

			ArrayList<Card> possible = possibleMoves.stream().filter(i->suitExhaustedTable.get(i.getSuit())[playerIndex] == false).collect(Collectors.toCollection(ArrayList::new));
			if (possible.size()!=0 ) {
				possibleMoves = possible;
			}
		}
		Collections.shuffle(possibleMoves, rng);
	}

	private boolean anyTrolls(ArrayList<Card> check) {
		boolean flag = false;
		for (Card c : check) {
			if (c.getSuit() == Suit.TROLLS) { flag = true; }
		} return flag;
	}

	private int undealtPoints(ArrayList<Card> undealt) {
		int points = 0;
		boolean anyTrolls = anyTrolls(undealt);
		for (Card c : undealt) {
			if (c.getSuit() == Suit.UNICORNS && !anyTrolls) points += 3;
			if (c.getSuit() == Suit.FAIRIES) points+=2;
			if (c.getSuit() == Suit.ZOMBIES) points -= 1;
		}
		return points;
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

			updateExhaustTable();

			// Clear the cards on the table (don't worry, pointers to them are tracked in the cardsPlayed deck)
			currentRound.clear();

			// If game end.
			if (!super.isGameValid()) {
				playerScores.set(taker, playerScores.get(taker)+undealtPoints(cardsPlayed.invertDeck));
			}

			playerIndex = taker;
		}
		fillPossibleMoves();

	}

	MCTSNode selection() { //TODO: DO NOT USE
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

	void backPropagation(ArrayList<Double> reward) {
//		if (reward==null) return;
		numObserved += 1;
		if (parent!=null) {
			totalValue += reward.get(parent.playerIndex);
			parent.backPropagation(reward);
		}
	}

	ArrayList<Double> simulation() {
		MCTSNode temp = new MCTSNode(this, this);
		temp.inSim = true;
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
//                && playerScores.equals(that.playerScores)
				&& cardsPlayed.invertDeck.equals(that.cardsPlayed.invertDeck);

	}

	public boolean equals(MCTSNode obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		State that = obj;
		cardsPlayed.invertDeck.sort(null);
		that.cardsPlayed.invertDeck.sort(null);
		obj.hand.sort(null);
		hand.sort(null);
		return playerIndex == that.playerIndex
				&& currentRound.equals(that.currentRound)
				&& hand.equals(obj.hand)
				&& cardsPlayed.invertDeck.equals(that.cardsPlayed.invertDeck);
	}

}

class MCTSDebugger {
	public static void dump(MCTSNode root, PrintStream output) {
		StringBuilder out = new StringBuilder();
		dump(root, 0, out, 0);
		output.println(out.toString());
	}

	private static void dump(MCTSNode cur, int indent, StringBuilder out, int depth) {
		if (depth > 1) return;
		out.append(" ".repeat(indent)).append(cur.playerIndex).append(':').append(cur.prevStep==null? "null": cur.prevStep.printCard())
				.append(':').append(cur.totalValue).append("/").append(cur.numObserved).append("/").append(String.format("%.3f", cur.meanValue())).append(":players:").append(cur.playerScores).append("\n");
		for (MCTSNode child: cur.children) {
			dump(child, indent+2, out, depth+1);
		}
	}
}

public class MCTSPlayer extends Player {

	public long timeLimitInMillis = 1000;
	public static PrintStream log;
	public ArrayList<Integer> lastRoundScore;
	public ExhaustTable lastExhaust = new ExhaustTable();
	public int targetIndex = 0;

	MCTSPlayer(String id, long timeLimitInMillis, int targetIndex) {
		super(id);
		// TODO Auto-generated constructor stub
		this.timeLimitInMillis = timeLimitInMillis;
		lastRoundScore = new ArrayList<>();
		lastRoundScore.add(0);
		lastRoundScore.add(0);
		lastRoundScore.add(0);
		this.targetIndex = targetIndex;
		try {
			log = new PrintStream(new File("log/log.txt"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	MCTSNode root = null;

	@Override
	boolean setDebug() {
		return false;
	}

	@Override
	public void notifyRound(ArrayList<Card> currentRound, int firstPlayer, ArrayList<Integer> rewards) {
		if (currentRound.size()>1) {
			Suit firstSuit = getFirstSuit(currentRound);
			for (int i=1; i<currentRound.size(); i++) {
				if (currentRound.get(i).getSuit() != firstSuit) {
					lastExhaust.get(firstSuit)[(firstPlayer + i) % 3] = true;
				}
			}
		}
	};

	@Override
	Card performAction(State masterCopy) {
		if (masterCopy.cardsPlayed.size()<3) {
			lastRoundScore = new ArrayList<>(masterCopy.playerScores);
			root = null;
			lastExhaust = new ExhaustTable();
		}

		for (int i=0; i<3; i++) {
			masterCopy.playerScores.set(i, masterCopy.playerScores.get(i) - lastRoundScore.get(i));
		}

		found: {
			if (root == null) {
				root = new MCTSNode(masterCopy, hand, null, targetIndex);
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
				root = new MCTSNode(masterCopy, hand, null, targetIndex);
			}
		}
		root.parent = null;
		root.suitExhaustedTable.logicOr(lastExhaust);
		lastExhaust = root.suitExhaustedTable;
		System.out.println(lastExhaust.toString());

		long start = System.currentTimeMillis();

		while (System.currentTimeMillis() - start < timeLimitInMillis) {
			var tNode = root.selection();
			var cNode = tNode.expansion();
			if (cNode.numObserved>2 || cNode.numObserved > 20) {
				break;
			}
			var rewards = cNode.simulation();
			cNode.backPropagation(rewards);
		}
//		MCTSDebugger.dump(root, log);
		Card card = root.children.stream().max(Comparator.comparingDouble(i->i.meanValue())).get().prevStep;
		hand.remove(card);
		return card;
	}

}
