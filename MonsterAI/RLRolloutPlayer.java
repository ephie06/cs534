import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Vector;

public class RLRolloutPlayer extends MCRLPlayer {
    
	public void OnOneSimulationDone(ArrayList<Integer> playerScores) {
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
    
    
    RLRolloutPlayer(String id, long timeLimitInMillis, int playerIndex, boolean isTest) {
        super(id, timeLimitInMillis, playerIndex, isTest);
    }

    @Override
    public void notifyRound(ArrayList<Card> currentRound, int firstPlayer, ArrayList<Integer> rewards) {
        return;
    };
    
    @Override
    public void notifyHandOver(ArrayList<Integer> playerScores) {
    	return;
    }

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
        while (root.possibleActions.size() != 0) {
            root.expansion();
        }

        long start = System.currentTimeMillis();

        //Dynamic Time based on how many child nodes exist
        while ((System.currentTimeMillis() - start < timeLimitInMillis) && (root.children.size() > 1)) {
            int randNode = (int) (root.children.size() * Math.random());
            var rewards = root.children.get(randNode).simulation();
            OnOneSimulationDone(rewards.playerScores);
            root.children.get(randNode).backPropagation(rewards.terminalValue());
        }

        Card card = root.children.stream()
                .max(Comparator.comparingDouble(i->i.meanValue()))
                .get().prevStep;
        hand.remove(card);
        
        return card;
    }

}
