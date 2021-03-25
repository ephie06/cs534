import java.util.Random;


public class GDPlayer extends Player {
	
Random rng;
	
	GDPlayer(String name) { super(name); rng = new Random(); System.out.println("Grab and Duck Player ("+name+") initialized.");  }

	boolean setDebug() { return false; }	

	// NOTE: performAction() must REMOVE the card from the hand
	// we would not want this to be the case in the future
	Card performAction (State masterCopy) {		
		
		SuitRange unicorns = getSuitRange(Suit.UNICORNS, hand);
		SuitRange fairies = getSuitRange(Suit.FAIRIES, hand);
		SuitRange trolls = getSuitRange(Suit.TROLLS, hand);
		SuitRange zombies = getSuitRange(Suit.ZOMBIES, hand);
		// For human debugging: print the hand
//		printHand();
		// Get the first suit that was played this round
		Suit firstSuit = getFirstSuit(masterCopy.currentRound);
		
		
		// If no cards were played this round
		// preference for first card in trick: lowest troll, lowest zombie, highest fairy, highest unicorn
		if (firstSuit == null) {
			if (trolls.getRange() != 0) { return hand.remove(trolls.startIndex); }
			else if (zombies.getRange() != 0) { return hand.remove(zombies.startIndex); }
			else if (fairies.getRange() != 0) { return hand.remove(fairies.endIndex-1); }
			else return hand.remove(unicorns.endIndex-1);
		}
		SuitRange range = getSuitRange(firstSuit, hand);
		// grab and duck if first card is unicorns
		int numCards = range.getRange();
		
		if (firstSuit == Suit.UNICORNS) {
			Value highest = masterCopy.currentRound.get(0).getValue();
			for (int j=0; j<masterCopy.currentRound.size(); j++) {
				if (highest.compareTo(masterCopy.currentRound.get(j).getValue()) < 0 && masterCopy.currentRound.get(j).getSuit() == firstSuit)
				{ highest = masterCopy.currentRound.get(j).getValue(); }
			}
			if(unicorns.getRange() != 0) {
				for (int j=0; j<unicorns.getRange(); j++) {
					if (highest.compareTo(hand.get(range.startIndex+j).getValue()) < 0 ) 
						{ return hand.remove(range.startIndex+j); }
					
				} return hand.remove(range.startIndex);
			}	
				
			else if (trolls.getRange() != 0) { return hand.remove(trolls.startIndex); }
			else if (zombies.getRange() != 0) { return hand.remove(zombies.endIndex-1); }
			else { return hand.remove(fairies.startIndex); }
			}		

		// first card is fairies
		if (firstSuit == Suit.FAIRIES) {
			Value highest = masterCopy.currentRound.get(0).getValue();
			for (int j=0; j<masterCopy.currentRound.size(); j++) {
				if (highest.compareTo(masterCopy.currentRound.get(j).getValue()) < 0 && masterCopy.currentRound.get(j).getSuit() == firstSuit)
				{ highest = masterCopy.currentRound.get(j).getValue(); }
			}
			if(range.getRange() == 0) {
				if (zombies.getRange() != 0) { return hand.remove(zombies.endIndex-1); }
				else if (trolls.getRange() != 0) { return hand.remove(trolls.startIndex); }
				else if (unicorns.getRange() != 0) { return hand.remove(unicorns.startIndex); }
			}
			else for (int i=0; i< range.getRange(); i++) {
				if (highest.compareTo(hand.get(range.startIndex+i).getValue()) < 0 ) { return hand.remove(range.startIndex+i); }
				
			} return hand.remove(range.startIndex);
		}	
		// first card is zombies
		if (firstSuit == Suit.ZOMBIES) {
			Value highest = masterCopy.currentRound.get(0).getValue();
			for (int j=0; j<masterCopy.currentRound.size(); j++) {
				if (highest.compareTo(masterCopy.currentRound.get(j).getValue()) < 0 && masterCopy.currentRound.get(j).getSuit() == firstSuit)
				{ highest = masterCopy.currentRound.get(j).getValue(); }
			}
			if(range.getRange() == 0) {
				if (trolls.getRange() != 0) { return hand.remove(trolls.startIndex); }
				else if (fairies.getRange() != 0) { return hand.remove(fairies.startIndex); }
				else if (unicorns.getRange() != 0) { return hand.remove(unicorns.startIndex); }
			}
			else for (int i=1; i<=range.getRange(); i++) {
				if (hand.get(range.endIndex-i).getValue().compareTo(highest) < 0 ) { return hand.remove(range.endIndex-i); }			
			} return hand.remove(range.endIndex-1);
		}
		// first card is trolls
		if (firstSuit == Suit.TROLLS) {
			Value highest = masterCopy.currentRound.get(0).getValue();
			for (int j=0; j<masterCopy.currentRound.size(); j++) {
				if (highest.compareTo(masterCopy.currentRound.get(j).getValue()) < 0 && masterCopy.currentRound.get(j).getSuit() == firstSuit)
				{ highest = masterCopy.currentRound.get(j).getValue(); }
			}
			if(range.getRange() == 0) {
				if (zombies.getRange() != 0) { return hand.remove(zombies.endIndex-1); }
				else if (fairies.getRange() != 0) { return hand.remove(fairies.startIndex); }
				else if (unicorns.getRange() != 0) { return hand.remove(unicorns.startIndex); }
			}
			else for (int i=0; i< range.getRange(); i++) {
				if (highest.compareTo(hand.get(range.startIndex+i).getValue()) < 0 ) { return hand.remove(range.startIndex+i); }
			} return hand.remove(range.startIndex);
		}
		System.out.println("something went wrong");
		return null;
	}
	
	


}
