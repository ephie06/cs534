import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;


//TODO: - Add parent back in for back propigation
//      - figure out how LinearModel is ran/used
//      - figure out rl stuff

enum LinearModel {
	INSTANCE;

    //TODO: Change Attributes to fit our part 1
    
    private double[] stateVectorWeights;
    private double bias;//the bias term
    
    private int feature_count; // how many states are learned by this model. We need this to track the model between different runs. 

    private static final int FEATURE_NUMBER = 29;


    /**
     * Randomly assigns staring weights, to begin training
     */
    private LinearModel() {
        Random random = new Random();
        stateVectorWeights  = DoubleStream.generate(() -> random.nextDouble()).limit(28).toArray();
        this.bias = random.nextDouble();
    }

    synchronized void saveWeightsToFile(String filepath) {
    	try {
    		FileOutputStream fos = new FileOutputStream(filepath);
    		ObjectOutputStream oos = new ObjectOutputStream(fos);
    		oos.writeObject(this.stateVectorWeights);
            oos.writeObject(this.bias);
    		oos.writeObject(this.feature_count);
    		oos.close();
    		fos.close();
    	} catch (IOException ioe) {
    		ioe.printStackTrace();
    	}
    }
    
    synchronized void loadWeightsFromFile(String filepath) {
		try {
			FileInputStream fis;
			fis = new FileInputStream(filepath);
			ObjectInputStream ois = new ObjectInputStream(fis);
	
	        this.stateVectorWeights = (double[]) ois.readObject();
            this.bias = (double) ois.readObject();
	        this.feature_count = (int) ois.readObject();
	        ois.close();
		    fis.close();
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

    }
   

    synchronized double getStateVectorValue(Vector<Integer> aStateVector) {
        double ret = 0;
        for(int i = 0; i < aStateVector.size(); i++) {
            ret+= aStateVector.get(i) * this.stateVectorWeights[i];
        }
        ret += bias;
        //System.out.println(ret);
        return ret;
    }

    /**
     * Updates the weights based on the result of the hand rollout
     //* @param move - the card the AI played
     * @param aStateVector - how our AI thought the game would end up
     * @param rolloutResult - the rollout result(how good the game ended up)(exp from above. our actual score)
     */
    synchronized public void updateWeightsSingle(Vector<Integer> aStateVector, double rolloutResult) {

        //Gradient descent: w -= epsilon*gradient
        //Gradient: gradient = x(y-(x*w))/shape_of_y
    	feature_count++;
        double epsilon = 1/(feature_count+1);//step size
        if (epsilon < 0.0001) epsilon = 0.0001;
        double yhat = getStateVectorValue(aStateVector);//get the predicted value
        double difference = yhat - rolloutResult;//get the difference between predicted and result

        for(int i = 0; i < aStateVector.size(); i++) {
            this.stateVectorWeights[i] -= epsilon*(aStateVector.get(i)*difference) / FEATURE_NUMBER;
        }
        this.bias -= epsilon*difference / FEATURE_NUMBER;

    }

    synchronized public String toString() {
        int currentWeightPrinted = 0;
        String[] whatMatrix = new String[]{"playerMatrix", "cardsPlayedMatrix"};
        String[] matriceWeightNames = new String[]{"UNICORNS - numCardsOfSuit", "UNICORNS - meanOfCards",
                                                "ZOMBIE - numCardsOfSuit", "ZOMBIE - meanOfCards",
                                                "TROLLS - numCardsOfSuit", "TROLLS - meanOfCards",
                                                "FAIRIES - numCardsOfSuit", "FAIRIES - meanOfCards"};
        StringBuilder s = new StringBuilder();
        for(int a = 0; a < 2; a++) {
            s.append(whatMatrix[a] + ": \n");
            for(int b = 0; b < 8; b++) {
                s.append(matriceWeightNames[b] + ": " + stateVectorWeights[currentWeightPrinted++] + " ");
                if(b == 1 || b == 3 || b == 5) s.append("\n");
            }
            s.append("\n\n");
        }

        s.append("trickPoints: " + stateVectorWeights[currentWeightPrinted++] + "\n");
        s.append("exhaustTable: ");
        for(int c = 0; c < 8; c++) {
            s.append(stateVectorWeights[currentWeightPrinted++] + " ");
        }
        s.append("\ncanBeat: " + stateVectorWeights[currentWeightPrinted++]);

        s.append("\nleadingBy: " + stateVectorWeights[currentWeightPrinted++]);

        s.append("\nNumberOfZombies: " + stateVectorWeights[currentWeightPrinted]);

        s.append("\n\nBias: " + bias);


        return s.toString();
    }
}

class MCRLGameState extends State {

    //Fields not included from MCTS: visited,total value, numObserved, children, parent, prevStep

    int targetIndex = 1; // the player index that the hand belong to (the player we want it to win)
    double stateValue = 0;
    ArrayList<Card> hand;
    CopyOnWriteArrayList<Card> possibleActions = new CopyOnWriteArrayList<>();//TODO: Terrible fix, find out how to iterate over actions correctly
    ExhaustTable suitExhaustedTable = null;
    MCRLGameState parent = null;
    ArrayList<MCRLGameState> children = new ArrayList<>();
    boolean inSim = false;
    MCRLPlayer parentPlayer;


    MCRLGameState(State secondCopy, ArrayList<Card> hand, MCRLPlayer parentPlayer) {
        super(secondCopy);
        this.hand = new ArrayList<>(hand);
        this.parentPlayer = parentPlayer;
        this.targetIndex = parentPlayer.targetIndex;
        suitExhaustedTable = new ExhaustTable();
        updateExhaustTable();
        rng = ThreadLocalRandom.current();
        fillPossibleActions();
    }


    MCRLGameState(MCRLGameState secondCopy, MCRLGameState parent, MCRLPlayer parentPlayer) {
        super(secondCopy);
        this.hand = new ArrayList<>(secondCopy.hand);
        this.parentPlayer = parentPlayer;
        this.targetIndex = parentPlayer.targetIndex;
        suitExhaustedTable = secondCopy.suitExhaustedTable.deepCopy();
        updateExhaustTable();
        rng = ThreadLocalRandom.current();
        fillPossibleActions();
    }

    Vector<Integer> getStateVector() {
        return new StateVector(this).stateRep;
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

/*    // Get the first suit that was played this round //TODO: ALREADY WRITTEN IN StateVector
    Value getHighestValue(ArrayList<Card> currentRound) {
        if (currentRound.size() == 0) return null;
        Suit fSuit = this.suitOfTrick;
        ArrayList<Card> currentRoundCopy = currentRound;
        currentRoundCopy.removeIf(card -> card.getSuit() != fSuit);
        return currentRoundCopy.stream().max(Comparator.comparing(Card::getValue)).get().getValue();
    }*/

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

    MCRLGameState expansion() {

        if (possibleActions.size()==0) {
            return this;
        }

        var newNode = new MCRLGameState(this, this, this.parentPlayer);
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

    MCRLGameState simulation(LinearModel linearModel) {
        MCRLGameState temp = new MCRLGameState(this, this, this.parentPlayer);
        temp.inSim = true;
        while(temp.possibleActions.size()!=0) {
            temp.makeOneMove(temp.bestPossibleAction(linearModel)); //possibleActions already shuffled. TODO: is action selection in sim Ran
        }
        return temp;
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

    public boolean equals(MCRLGameState obj) {
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

    Card bestPossibleAction(LinearModel linearModel) {
        Card bestMove = null;
        double highestScore = Double.NEGATIVE_INFINITY;
        for(var card : possibleActions) {
            double predictedScore = parentPlayer.makePrediction(card, this); //lookahead to afterstate, choose 'best' afterstate
            if (predictedScore > highestScore) {
                bestMove = card;
                highestScore = predictedScore;
            }
        }
        if (bestMove == null) throw new NullPointerException("no cards in possible actions to select");
        //linearModel.seenStates.add(this);
        return bestMove;
    }
}

public class MCRLPlayer extends Player {

    public long timeLimitInMillis = 1000;
    public static PrintStream log;
    public ArrayList<Integer> lastRoundScore;
    public ExhaustTable lastExhaust = new ExhaustTable();
    public LinearModel linearModel;
    public boolean isTest = false;
    MCRLGameState root = null;
    int targetIndex = 0;
    
    public ArrayList<MCRLGameState> seenStates;
    public ArrayList<Card> actionsTook;
    public ArrayList<Double> imme_reward;
    //public ArrayList<Vector<Integer>> statesVisited;

    @Override
	public void notifyGameOver(int winner) {
    	if (isTest) return;
    	if (winner == targetIndex) {
    		imme_reward.set(imme_reward.size()-1, 100.0);
    	} else {
    		imme_reward.set(imme_reward.size()-1, -100.0);
    	}
		updateWeights();
	}
    
	public void notifyHandOver(ArrayList<Integer> playerScores) {
		if (isTest) return;
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

        double terminalReward = 0;
        if (e.get(targetIndex) == maxV) {
        	terminalReward = maxV - secV;
        } else {
        	terminalReward = e.get(targetIndex) - maxV;
        }
        imme_reward.set(imme_reward.size()-1, terminalReward);
        updateWeights();
        return;
	}

    
    void newVectorSet() {
        seenStates = new ArrayList<>();
        actionsTook = new ArrayList<>();
        imme_reward = new ArrayList<>();
        //statesVisited = new ArrayList<>();
    }
    
    /**
     * Get the predicted value of the game state and move
     * @param move the card the AI played
     * @param state the state we are currently playing
     * @return the predicted value of the state move(how good that move is)(exp. just our score)
     */
    public double makePrediction(Card move, MCRLGameState state) {

        //TODO: Call makeOneMove fcn from NewRolloutPlayer/MCTSPlayer (inputting parameter move)
        MCRLGameState afterState = state;
        afterState.makeOneMove(move);
        Vector<Integer> afterStateVector = afterState.getStateVector();
        //TODO: Simulation call was here, but stateVector values should represent what occurs at end of game
//        System.out.println(afterStateVector);
//        System.out.println(Arrays.toString(this.stateVectorWeights));

        //plug into linear model
        return LinearModel.INSTANCE.getStateVectorValue(afterStateVector);
    }
    
    public void updateWeights() {
        ArrayList<Vector<Integer>> wdwdwindw = new ArrayList<>();
        double discountFactor = 0.9;
        for(int a = seenStates.size()-1; a >= 0 ; a--) {
            MCRLGameState aState = seenStates.get(a);
            aState.makeOneMove(actionsTook.get(a));
            double reward = imme_reward.get(a) + discountFactor * LinearModel.INSTANCE.getStateVectorValue(aState.getStateVector());
            if (a == seenStates.size()-1) {
            	reward = imme_reward.get(a);
            }
            if (imme_reward.get(a)==0) continue;
        	LinearModel.INSTANCE.updateWeightsSingle(seenStates.get(a).getStateVector(), reward);
        }

        newVectorSet();
    }
    
    MCRLPlayer(String id, long timeLimitInMillis, int playerIndex, boolean isTest) {
        super(id);
        linearModel = LinearModel.INSTANCE;
        this.targetIndex = playerIndex;
        // TODO Auto-generated constructor stub
        this.timeLimitInMillis = timeLimitInMillis;
        this.isTest = isTest;
        lastRoundScore = new ArrayList<>();
        lastRoundScore.add(0);
        lastRoundScore.add(0);
        lastRoundScore.add(0);
        newVectorSet();
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
    public void notifyRound(ArrayList<Card> currentRound, int firstPlayer, ArrayList<Integer> rewards) {
        if (currentRound.size()>1) {
            Suit firstSuit = getFirstSuit(currentRound);
            for (int i=1; i<currentRound.size(); i++) {
                if (currentRound.get(i).getSuit() != firstSuit) {
                    lastExhaust.get(firstSuit)[(firstPlayer + i) % 3] = true;
                }
            }
        }
        
        imme_reward.add((double)rewards.get(targetIndex).intValue());
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

        root = new MCRLGameState(masterCopy, hand,this);

        root.parent = null;
        root.suitExhaustedTable.logicOr(lastExhaust);
        lastExhaust = root.suitExhaustedTable;
        //System.out.println(lastExhaust.toString());

        //long start = System.currentTimeMillis();

        Card bestMove = root.bestPossibleAction(linearModel);

//		MCTSDebugger.dump(root, log);
        seenStates.add(root);
        actionsTook.add(bestMove);
        hand.remove(bestMove);
        return bestMove;
    }
}