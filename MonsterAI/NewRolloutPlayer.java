import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

class RolloutNode extends State {

    boolean visited = false;
    int targetIndex = 1; // the player index that the hand belong to (the player we want it to win)
    ArrayList<Card> hand;
    int totalValue = 0;
    int numObserved = 0;
    ArrayList<RolloutNode> children = new ArrayList<>();
    ArrayList<Card> possibleMoves = new ArrayList<>();
    RolloutNode parent = null;
    Card prevStep = null;

    RolloutNode(Deck deck, ArrayList<Card> round, ArrayList<Integer> scores, int index, ArrayList<Card> hand, RolloutNode parent) {
        super(deck, round, scores, index);
        // TODO Auto-generated constructor stub
        this.hand = new ArrayList<>(hand);
        this.parent = parent;
        rng = ThreadLocalRandom.current();
        fillPossibleMoves();
    }

    RolloutNode (State secondCopy, ArrayList<Card> hand, RolloutNode parent) {
        super(secondCopy);
        this.hand = new ArrayList<>(hand);
        this.targetIndex = playerIndex;
        this.parent = parent;
        rng = ThreadLocalRandom.current();
        fillPossibleMoves();
    }


    RolloutNode (RolloutNode secondCopy, RolloutNode parent) {
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

    void playCard(Card c, int index) { //TODO: USE
        playCard(c);
        if (index == targetIndex) {
            hand.remove(c);
        }
    }

    ArrayList<Integer> terminalValue() { //TODO: USE
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
    boolean isGameValid() { //TODO: USE
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

    RolloutNode selection() {
        if (possibleMoves.size()!=0) return this;

        return children.stream()
                .skip((int) (children.size() * Math.random()))
                .findFirst()
                .orElse(this);
    }


    void expansion(int count) {

        if (possibleMoves.size()==0) {
            return;
        }

        var newNode = new RolloutNode(this, this);
        Card card = possibleMoves.get(count);
        possibleMoves.remove(count);
        newNode.makeOneMove(card);
        children.add(newNode);
    }

    void backPropagation(ArrayList<Integer> reward) {
        RolloutNode no = this;
        while (no != null) {
            no.numObserved++;
            no.totalValue += reward.get(playerIndex);
            no = no.parent;
        }
    }

    ArrayList<Integer> simulation() {
        RolloutNode temp = new RolloutNode(this, this);
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

public class NewRolloutPlayer extends Player {

    public long timeLimitInMillis = 1000;

    NewRolloutPlayer(String id, long timeLimitInMillis) {
        super(id);
        // TODO Auto-generated constructor stub
        this.timeLimitInMillis = timeLimitInMillis;
    }

    RolloutNode root = null;

    @Override
    boolean setDebug() {
        return false;
    }

    @Override
    Card performAction(State masterCopy) {

        found: {
            if (root == null) {
                root = new RolloutNode(masterCopy, hand, null);
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
                root = new RolloutNode(masterCopy, hand, null);
            }
        }
        root.parent = null;

        //Populate root with potential moves it can take
        for(int count = 0; count < root.possibleMoves.size(); count++) root.expansion(count);

        long start = System.currentTimeMillis();

        //Dynamic Time based on how many child nodes exist
        while ((System.currentTimeMillis() - start < root.children.size()* 100L) && (root.children.size() > 1)) {
            //var tNode = root.selection();
            int randNode = (int) (root.children.size() * Math.random());
            var rewards = root.children.get(randNode).simulation();
            root.children.get(randNode).backPropagation(rewards);
        }

        Card card = root.children.stream()
                .max(Comparator.comparingDouble(i->i.meanValue()))
                .get().prevStep;
        hand.remove(card);
        return card;
    }

}