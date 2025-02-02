// This AI will look at the first suit played this round
// If no cards have been played, will pick a random card out of the hand
// Otherwise, pick a random card in the suit first played

import java.util.ArrayList;
import java.util.*;

class RandomPlayAI extends Player {

	Random rng;
	
	RandomPlayAI(String name) { super(name); rng = new Random(); System.out.println("Random Play AI ("+name+") initialized.");  }

	boolean setDebug() { return false; }

	// NOTE: performAction() must REMOVE the card from the hand
	// we would not want this to be the case in the future
	Card performAction (State masterCopy) {
		// For human debugging: print the hand
//		printHand();
		// Get the first suit that was played this round
		Suit firstSuit = getFirstSuit(masterCopy.currentRound);
		// If no cards were played this round, play a random card 
		if (firstSuit == null) {
			int index = rng.nextInt(hand.size());
			return hand.remove(index);
		}

		// Remove a random card of the correct suit
		SuitRange range = getSuitRange(firstSuit, hand);
		if (range.getRange() == 0) return hand.remove(rng.nextInt(hand.size()));
		int index = rng.nextInt(range.getRange());
		return hand.remove(range.startIndex+index);
	}

}