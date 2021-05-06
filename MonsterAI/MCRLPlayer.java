import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;


//TODO: - Add parent back in for back propigation
//      - figure out how LinearModel is ran/used
//      - figure out rl stuff

class LinearModel {

    //TODO: Change Attributes to fit our part 1
    public double[] suitsMissing_weight;
    public double[] suitHighCard_weight;
    public double suitOfTrick_weight;
    public double highestValueCardPlayed_weight;
    public double valueOfTrick_weight;
    public double roundNumber_weight;
    public double bias;//the bias term

    public static final int FEATURE_NUMBER = 11;


    /**
     * Randomy assigns staring weights, to begin training
     */
    public LinearModel() {
        Random random = new Random();
        this.suitsMissing_weight = new double[]{random.nextDouble(), random.nextDouble(), random.nextDouble()};
        this.suitHighCard_weight = new double[]{random.nextDouble(), random.nextDouble(), random.nextDouble()};
        this.suitOfTrick_weight = random.nextDouble();
        this.highestValueCardPlayed_weight = random.nextDouble();
        this.valueOfTrick_weight = random.nextDouble();
        this.roundNumber_weight = random.nextDouble();
        this.bias = random.nextDouble();
    }

    /**
     * Get the predicted value of the game state and move
     * @param move the card the AI played
     * @param state the state we are currently playing
     * @return the predicted value of the state move(how good that move is)(exp. just our score)
     */
    public double makePrediction(Card move, MCRLState state) {

        //TODO: Call makeOneMove fcn from NewRolloutPlayer/MCTSPlayer (inputting parameter move)
        MCRLState afterState = state;
        afterState.makeOneMove(move);

        double suitOfTrick_double = afterState.currentRound.isEmpty() ? 0 :afterState.getFirstSuit(state.currentRound).ordinal(); //TODO: Fix this it doesnt make sense
        double highestValueCardPlayed_double = afterState.currentRound.isEmpty() ? 0 : (afterState.currentRound.stream().max(Comparator.comparing(i->i.getValue().ordinal())).get()).getValue().ordinal();

        //plug into linear model
        double ret =
                afterState.suitsMissing[0]*this.suitsMissing_weight[0] + afterState.suitsMissing[1]*this.suitsMissing_weight[1] + afterState.suitsMissing[2]*this.suitsMissing_weight[2]
                        + afterState.suitHighCard[0]*this.suitHighCard_weight[0] + afterState.suitHighCard[1]*this.suitHighCard_weight[1] + afterState.suitHighCard[2]*this.suitHighCard_weight[2]
                        + suitOfTrick_double*this.suitOfTrick_weight + highestValueCardPlayed_double*this.highestValueCardPlayed_weight
                        + afterState.valueOfTrick*this.valueOfTrick_weight + afterState.roundNumber*this.roundNumber_weight + bias;
        return ret;
    }

    //Deleted GetState fcn here

    /**
     * Updates the weights based on the result of the hand rollout
     * @param move - the card the AI played
     * @param state - the original game
     * @param rolloutResult - the rollout result(how good the game ended up)(exp from above. our actual score)
     */
    public void updateWeights(Card move, MCRLState state, double rolloutResult) {
        //System.out.println(this);

        //TODO: Figure these values out
        double suitOfTrick_double = 0;
        double highestValueCardPlayed_double = 0;

        //Gradient descent: w -= epsilon*gradient
        //Gradient: gradient = x(y-(x*w))/shape_of_y
        double epsilon = 0.0001;//step size
        double yhat = makePrediction(move, state);//get the predicted value
        double difference = yhat - rolloutResult;//get the difference between predicted and result

        //TODO: Call makeOneMove fcn from NewRolloutPlayer/MCTSPlayer (inputting parameter move)
        MCRLState afterState = state;
        //System.out.println(difference + " = " + yhat + " - " + rolloutResult);

        //update weights to get closer to actual values
        for(int i1 = 0; i1 < 3; i1++) this.suitsMissing_weight[i1] -= epsilon*(afterState.suitsMissing[i1]*difference) / FEATURE_NUMBER;
        for(int i2 = 0; i2 < 3; i2++) this.suitHighCard_weight[i2] -= epsilon*(afterState.suitHighCard[i2]*difference) / FEATURE_NUMBER;

        this.suitOfTrick_weight -= epsilon*(suitOfTrick_double*difference) / FEATURE_NUMBER;
        this.highestValueCardPlayed_weight -= epsilon*(highestValueCardPlayed_double*difference) / FEATURE_NUMBER;
        this.valueOfTrick_weight -= epsilon*(afterState.valueOfTrick*difference) / FEATURE_NUMBER;
        this.roundNumber_weight -= epsilon*(afterState.roundNumber*difference) / FEATURE_NUMBER;

        this.bias -= epsilon*difference / FEATURE_NUMBER;
        //System.out.println(this + "\nend\n\n");
    }
}

class MCRLState extends State {

    //Fields not included from MCTS: visited,total value, numObserved, children, parent, prevStep

    static int targetIndex = 1; // the player index that the hand belong to (the player we want it to win)
    double stateValue = 0;
    ArrayList<Card> hand;
    CopyOnWriteArrayList<Card> possibleActions = new CopyOnWriteArrayList<>();//TODO: Terrible fix, find out how to iterate over actions correctly
    ExhaustTable suitExhaustedTable = null;
    MCRLState parent = null;
    ArrayList<MCRLState> children = new ArrayList<>();
    boolean inSim = false;

    //Fields tested in LinearModel
    //TODO: Figure out how to make these values dynamic
    int[]				suitsMissing; //Trolls, Zombies, Unicorns, Fairies, 0 if it is not missing, 1 if it is
    int[]				suitHighCard; //Trolls, Zombies, Unicorns, Fairies, stores only integer value, 0 if suit is missing
    Suit                suitOfTrick;
    Value               highestValueCardPlayed; //null if AI is beginning suit
    int					valueOfTrick; //set to 0 if unicorn trick and troll was played, slightly redundant
    int					roundNumber; //1-18 for which part of game we are on


    MCRLState(State secondCopy, ArrayList<Card> hand) {
        super(secondCopy);
        this.hand = new ArrayList<>(hand);
        suitExhaustedTable = new ExhaustTable();
        updateExhaustTable();
        rng = ThreadLocalRandom.current();
        fillPossibleActions();

        for(int i : suitsMissing)  i = 0; //TODO: use ExhaustTable to input suit missing values (is this field neccessary?)
        for(int i : suitHighCard)  i = 10; //TODO: create function to iterate through hand for each card
        suitOfTrick = getFirstSuit(currentRound);
        highestValueCardPlayed = getHighestValue(currentRound); //TODO: Check if correct
        valueOfTrick = calculatePoints();
        roundNumber = getRoundNumber();

    }


    MCRLState(MCRLState secondCopy, MCRLState parent) {
        super(secondCopy);
        this.hand = new ArrayList<>(secondCopy.hand);
        suitExhaustedTable = secondCopy.suitExhaustedTable.deepCopy();
        updateExhaustTable();
        rng = ThreadLocalRandom.current();
        fillPossibleActions();
    }

/*    public double UCB95() {
        if (numObserved == 0) {
            return Double.POSITIVE_INFINITY;
        }
        return meanValue() + 2 * Math.sqrt(Math.log(parent.numObserved)/numObserved);
    }*/

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

    // Get the first suit that was played this round
    Value getHighestValue(ArrayList<Card> currentRound) {
        if (currentRound.size() == 0) return null;
        Suit fSuit = this.suitOfTrick;
        ArrayList<Card> currentRoundCopy = currentRound;
        currentRoundCopy.removeIf(card -> card.getSuit() != fSuit);
        return currentRoundCopy.stream().max(Comparator.comparing(Card::getValue)).get().getValue();
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


    private void fillPossibleActions() {
        possibleActions.clear();
        if (!isGameValid()) return;

        Suit firstSuit = getFirstSuit(currentRound);

        if (playerIndex == targetIndex) {
            if (firstSuit != null && checkSuit(firstSuit)) {
                possibleActions = hand.stream().filter(i->(i.getSuit()==firstSuit)).collect(Collectors.toCollection(CopyOnWriteArrayList::new));
            } else {
                possibleActions.addAll(hand);
            }

        } else {
            possibleActions = cardsPlayed.invertDeck.stream().filter(i->!hand.contains(i)).collect(Collectors.toCollection(CopyOnWriteArrayList::new));

            if (inSim && firstSuit!=null && suitExhaustedTable.get(firstSuit)[playerIndex]==false) {
                List<Card> thisSuit = possibleActions.stream().filter(i->(i.getSuit()==firstSuit)).collect(Collectors.toList());
                //With a probability calculated by size of 'hand', 'thisSuit' and 'possibleActions', we filter the possibleActions with the suit.
                int suitCount = thisSuit.size();
                int totalCount = possibleActions.size();
                int handCount = (totalCount+1)/2;
                double pGotACardOtherSuit = 1- (double)suitCount/totalCount;
                double pAllCardOtherSuit = Math.pow(pGotACardOtherSuit, handCount);
                if (rng.nextDouble() < 1 - pAllCardOtherSuit) {
                    possibleActions.clear();
                    possibleActions.addAll(thisSuit);
                }
            }

            CopyOnWriteArrayList<Card> possible = possibleActions.stream().filter(i->suitExhaustedTable.get(i.getSuit())[playerIndex] == false).collect(Collectors.toCollection(CopyOnWriteArrayList::new));
            if (possible.size()!=0 ) {
                possibleActions = possible;
            }
        }
        Collections.shuffle(possibleActions, rng);
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

    protected void makeOneMove(Card c) { //TODO: Protected vs Private?
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
        fillPossibleActions();

    }

/*    MCRLState selection() { //TODO: DO NOT USE
        if (possibleActions.size()!=0) {
            return this;
        }
        if (children.size()==0) {
            return this;
        }
        return children.stream().max(Comparator.comparingDouble(i->i.UCB95())).get().selection();
    }*/

    MCRLState expansion() {

        if (possibleActions.size()==0) {
            return this;
        }

        var newNode = new MCRLState(this, this);
        Card card = possibleActions.get(possibleActions.size()-1);
        possibleActions.remove(possibleActions.size()-1);
        newNode.makeOneMove(card);
        children.add(newNode);
        return newNode;
    }

    void backPropagation(ArrayList<Double> reward) {
//		if (reward==null) return;
        //numObserved += 1;
        if (parent!=null) {

            //TODO: add q(s,a) thing here instead of totalValue
            //totalValue += reward.get(parent.playerIndex);
            parent.backPropagation(reward);
        }
    }

    ArrayList<Double> simulation() {
        MCRLState temp = new MCRLState(this, this);
        temp.inSim = true;
        while(temp.possibleActions.size()!=0) {
            temp.makeOneMove(temp.possibleActions.get(0)); //possibleActions already shuffled. TODO: is action selection in sim Ran
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

    public boolean equals(MCRLState obj) {
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

public class MCRLPlayer extends Player {

    public long timeLimitInMillis = 1000;
    public static PrintStream log;
    public ArrayList<Integer> lastRoundScore;
    public ExhaustTable lastExhaust = new ExhaustTable();
    public LinearModel linearModel = new LinearModel();
    MCRLState root = null;

    MCRLPlayer(String id, long timeLimitInMillis) {
        super(id);
        // TODO Auto-generated constructor stub
        this.timeLimitInMillis = timeLimitInMillis;
        lastRoundScore = new ArrayList<>();
        lastRoundScore.add(0);
        lastRoundScore.add(0);
        lastRoundScore.add(0);
        try {
            log = new PrintStream(new File("log/log.txt"));
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    boolean setDebug() {
        return false;
    }

    @Override
    void notifyRound(ArrayList<Card> currentRound, int firstPlayer) {
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
                root = new MCRLState(masterCopy, hand);
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
                root = new MCRLState(masterCopy, hand);
            }
        }
        root.parent = null;
        root.suitExhaustedTable.logicOr(lastExhaust);
        lastExhaust = root.suitExhaustedTable;
        System.out.println(lastExhaust.toString());

        //long start = System.currentTimeMillis();

        Card bestMove = null;
        double highestScore = 0;
        for(var card : root.possibleActions) {
            double predictedScore = linearModel.makePrediction(card, root);
            if (predictedScore > highestScore) bestMove = card;
            highestScore = predictedScore;
        }

//		MCTSDebugger.dump(root, log);
        hand.remove(bestMove);
        return bestMove;
    }

}
