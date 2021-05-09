import java.util.ArrayList;
import java.util.Scanner;

class Game {

	boolean 			debug;					// set to true for debug if some player wishes for it
	ArrayList<Player> 	playerOrder;			// think of this as a circular queue of the 3 players
	int 				firstPlayer;			// the index of the first player for this round
	Deck 				cardsPlayed;			// cards that have already been played -- replace them into the deck
	ArrayList<Card> 	currentRound;   		// cards currently played on the table
	ArrayList<Card> 	undealt; 				// 6 undealt cards
	ArrayList<Integer> 	playerScores; 			// keep track of the player scores within this game
	ArrayList<Integer>  zombieCount;			// keep track of zombies players collect
	Scanner 			in;						// For scanner input
	String 				s;						// To store scanner input
	boolean 			zArmy;					// Keep track so can only happen once per round

	// Every game must have three players and one deck!
	// Note: This WILL NOT shuffle the deck or deal the cards here
	// We ONLY do that upon playing a new game
	Game (Deck deck, Player p1, Player p2, Player p3) {
		debug = false;
		playerOrder = new ArrayList<Player>();
		playerOrder.add(p1);
		playerOrder.add(p2);
		playerOrder.add(p3);
		for (Player p : playerOrder) if (p.setDebug()) debug = true;
		firstPlayer = 0;
		cardsPlayed = deck;
		currentRound = new ArrayList<Card>();
		undealt = new ArrayList<Card>();
		playerScores = new ArrayList<Integer>();
		zombieCount = new ArrayList<Integer>();
		zArmy = false;
		in = new Scanner(System.in);
	}

	// Call this every time a new game is played to shuffle the deck and clear player hands
	void initNewGame () {
		cardsPlayed.shuffleDeck();
		// cardsPlayed.printDeck(); // debugging to make sure the deck is correct
		// cardsPlayed.checkDeck(); // we need a way to check that all 60 cards are here correctly
		// clear the hands of all the players (to make sure they're not holding anything already!)
		for (Player p : playerOrder) { p.clearHand(); }
		// pass out 18 cards each to the 3 players
		for (int i = 0; i < 18; i++) { for (Player p : playerOrder) { p.addToHand ( cardsPlayed.drawTop() ); } }
		//6 undealt cards
		for(int i=0; i<6; i++) { undealt.add(cardsPlayed.drawTop()); }
//		System.out.print("Undealt: ");
//		for(int i=0; i<undealt.size(); i++) {
//			System.out.print(undealt.get(i).printCard() + ", ");}
//		System.out.println();
		
		// sort all hands
		for (Player p : playerOrder) { p.sortHand(); }
//		for (Player p : playerOrder) { p.printHand(); }		// for debugging to check all the hands are valid
		cardsPlayed.printDeck();								// for debugging to check all cards have been dealt
		// pick first player
		firstPlayer = 0;
		// print message to say who plays first
//		System.out.println(playerOrder.get(firstPlayer).getName() + " will play first.\n");
		// just to be safe, clear the arraylist of cards on the table
		currentRound.clear();
		// clear scores for this game
		playerScores.clear();
		playerScores.add(0);
		playerScores.add(0);
		playerScores.add(0);
		zombieCount.clear();
		zombieCount.add(0);
		zombieCount.add(0);
		zombieCount.add(0);
		zArmy = false;

	}

	// Print the cards that were played so far this round
	// be sure to pass in the index of the first player to get the names right
	void printRound(int firstPlayer) {
		System.out.println("\nCards played this round:");
		System.out.println("------------------------");
		if (currentRound.size() == 0) {
			System.out.println("No cards have been played this round.");
		}
		for (int i = 0; i < currentRound.size(); i++) {
			int index = (i+firstPlayer) % playerOrder.size();
			// be careful with the format length -- potentially check for longest name length
			System.out.format("%15s", playerOrder.get(index).getName());
			System.out.print(" played ");
			System.out.format("%3s\n", currentRound.get(i).printCard());
		}
		//System.out.println();
	}
	
	// Return a bool based on whether any player reached 200 points
	boolean gameOver() {
		boolean flag = false;
		for (int i = 0; i < playerOrder.size(); i++) {
			if ( playerOrder.get(i).getPoints() >= 200) { flag = true; }
		} return flag;
	}
	

	// Return a bool based on whether the played card was valid or not
	boolean checkRound (Card playedCard, int index) {

		if (currentRound.size() == 0) {
			return true;
		}

		// next, check the first card on the table and check the hand of the player playing
		// we can only get to this step if there already is a card on the table!
		Suit firstSuit = currentRound.get(0).getSuit();
		if (playerOrder.get(index).checkSuit(firstSuit) && playedCard.getSuit() != firstSuit) {
			System.out.println("You still have a card that is " + firstSuit + ". You must play that first.");
			return false;
		}
		return true;
	}

	// Return the index of the next player who will play // the player who takes this round
	// Pass in the index of the current first player (to check who played what card)
	// NOTE: This MUST return an int from 0 to 3! ALWAYS DO % playerOrder.size();
	int findTaker (int firstPlayer) {
		Suit firstSuit = currentRound.get(0).getSuit();
		Value largestValue = currentRound.get(0).getValue();
		int taker = firstPlayer;

		// go through all 3 cards that were played this round
		for (int i = 0; i < playerOrder.size(); i++) {
			// keep track of the index of who played it
			int index = (firstPlayer+i) % playerOrder.size();
			// if this card is the same suit as the first card, proceed
			if (currentRound.get(i).getSuit() == firstSuit) {
				// if this card is the largest played of the right suit this round, this player takes the round
				if (largestValue.compareTo(currentRound.get(i).getValue()) < 0) {
					taker = index;
					largestValue = currentRound.get(i).getValue();
				}
			}
		}

		return taker % playerOrder.size();
	}
	
	//Count zombies in an array of cards
	int countZombies(ArrayList<Card> check) {
		int count = 0;
		for (Card c : check) {
			if (c.getSuit() == Suit.ZOMBIES) { count++; }
		} return count;
	}

	//Check if any Trolls in currentRound
	boolean anyTrolls(ArrayList<Card> check) {
		boolean flag = false;
		for (Card c : check) {
			if (c.getSuit() == Suit.TROLLS) { flag = true; }
		} return flag;
	}	
	
	// Go through the cards from the currentRound and calculate their point values
	int calculatePoints() {
		int points = 0;
		boolean anyTrolls = anyTrolls(currentRound);
		for (Card c : currentRound) {
			if (c.getSuit() == Suit.UNICORNS && !anyTrolls) points += 3;
			if (c.getSuit() == Suit.FAIRIES) points+=2;
			if (c.getSuit() == Suit.ZOMBIES) points -= 1;
		}
		return points;
	}
	
	// Go through the undealt cards and calculate their point values
		int undealtPoints() {
			int points = 0;
			boolean anyTrolls = anyTrolls(undealt);
			for (Card c : undealt) {
				if (c.getSuit() == Suit.UNICORNS && !anyTrolls) points += 3;
				if (c.getSuit() == Suit.FAIRIES) points+=2;
				if (c.getSuit() == Suit.ZOMBIES) points -= 1;
			}
			return points;
		}
	

	// Print out how many points each player currently has within this game
	void printPoints() {
//		System.out.println("Points received this game:");
//		System.out.println("--------------------------");
//		for (int i = 0; i < playerOrder.size(); i++) {
//			System.out.println(playerOrder.get(i).getName() + " has " + playerScores.get(i) + " points.");
//		}
//		System.out.println();
	}
	int getWinner() {
		int highestScore = playerOrder.get(0).getPoints();
		int index = 0;
		for (int i = 0; i < playerOrder.size(); i++) {
			if (highestScore < playerOrder.get(i).getPoints()) {
				index = i;
				highestScore = playerOrder.get(i).getPoints();
			}
		}
		return index;
	}
	
	// Print the person who is in the lead after this game
	int printWinner() {
		int highestScore = playerOrder.get(0).getPoints();
		int index = 0;
		for (int i = 0; i < playerOrder.size(); i++) {
			if (highestScore < playerOrder.get(i).getPoints()) {
				index = i;
				highestScore = playerOrder.get(i).getPoints();
			}
		}
		System.out.println("\n" + playerOrder.get(index).getName() + " is the winner after this round.\n");
		return index;
	}

	// Print out how many points each player currently has between all games
	void printTotalPoints() {
		if (!debug) return;
		System.out.println("Total cumulative points between all games:");
		System.out.println("------------------------------------------");
		for (Player p : playerOrder) {
			System.out.println(p.getName() + " has " + p.getPoints() + " points.");
		}
		System.out.println();
	}

	// Rnd-game functionality for zombie army
	// subtracts 20 points from the other players for the round and game.
	void zombieArmy () {
		int index = -1;
		for (int i = 0; i < playerOrder.size(); i++) {
			if (zombieCount.get(i) >= 12) {
				//System.out.println("\n" + playerOrder.get(i).getName() + " has a Zombie Army! Opponents score -20");
				index = i;
				zArmy = true; // stops from being called in subsequent tricks
			}
		}
		
		if (index > -1) {
			for (int i = 0; i < playerOrder.size(); i++) {
				if (i != index) {
					playerOrder.get(i).addPoints(-20);
					playerScores.set(i, playerScores.get(i)-20);
				}
				
			}
		}
	}

	// Call this whenever you want to start a completely new game and play through it
	void playNewGame() {
		// We must call this to shuffle the deck and deal cards to all the players
		initNewGame();
		// For all 18 rounds of the game...
		for (int i = 1; i < 19; i++) {
//			System.out.println("--------------------------------------------");
//			System.out.println("Round #" +i+":");
//			System.out.println("--------------------------------------------");
			// clear the table for this round
			currentRound.clear();
			// go through actions for all three players (ordered based on firstPlayer)
			for (int j = 0; j < 3; j++) {
				// use index to determine the index of the player currently playing
				int index = (j+firstPlayer) % playerOrder.size();
//				if (debug)
//					printRound(firstPlayer); // for debugging: print the cards that were played this round
				boolean validPlay = false;
				Card playedCard = null;

				// Create a copy of the current game state, to be passed in to the Player
				// So that the Player may potentially do playouts of the game
				// Note that these three parameters are copied in the State constructor
				State gameCopy = new State(cardsPlayed, currentRound, playerScores, index);

				// Loop: Allow player to pick a play, but reject if invalid selection
				while (!validPlay) {
					// Allow the Player to pick a move to play next
					playedCard = playerOrder.get(index).performAction(gameCopy);
					// Check if the playedCard is valid, given this currentRound
					validPlay = checkRound(playedCard, index);
					// If the card was not valid, put it back in the hand and sort the hand (this might be SLOW)
					if (!validPlay) {
						//System.out.println("This was an invalid play. Please pick a valid card.");
						playerOrder.get(index).addToHand(playedCard);
						playerOrder.get(index).sortHand();
						//break;
					}
				}

				// Standard output message to notify what card was officially played
//				System.out.println(playerOrder.get(index).getName() + " played " + playedCard.printCard() + ".");
				// Add the played card to the currentRound (put the card on the table for all to see)
				// BE CAREFUL! We will be adding a direct pointer to the card here!
				currentRound.add(playedCard);
				// Take the card that is played and add it back to the deck as well
				cardsPlayed.restockDeck(playedCard);

        		
			}

			if (debug) {
				System.out.println("--------------------------------------------");
				System.out.println("Round " + i + " Summary:");
				System.out.println("--------------------------------------------");
				printRound(firstPlayer); 	// for debugging: use this method to see what cards were played this round
			}

			// 1. findTaker() will update who took the cards this round
			// 2. calculatePoints() will calculate how many points this round consisted of
			// 3. addPoints() will add those points to the correct player
			int oldFirstPlayer = firstPlayer;
			firstPlayer = findTaker(firstPlayer);
			ArrayList<Integer> rewards = new ArrayList<>();
			for (int j=0; j<3; j++) {
				rewards.add(-playerOrder.get(j).points);
			}
			
			int points = calculatePoints();
			playerScores.set(firstPlayer,playerScores.get(firstPlayer)+points);
			playerOrder.get(firstPlayer).addPoints(points);
			//count zombies and add to taker
			int zombies = countZombies(currentRound);
			zombieCount.set(firstPlayer, zombieCount.get(firstPlayer)+zombies);
			if (!zArmy) { zombieArmy(); }
			int thisScore = playerScores.get(firstPlayer);
			
			for (int j=0; j<3; j++) {
				rewards.set(j, rewards.get(j) + playerOrder.get(j).points);
			}
			
			for (int j=0; j<3; j++) {
				playerOrder.get(j).notifyRound(currentRound, oldFirstPlayer, rewards);
			}
			
			
			if (debug) {
				System.out.println("\n" + playerOrder.get(firstPlayer).getName() + " played the highest card "
					+ "and took " + points + " points this round.\n");
				printPoints();
			}
			
			if (gameOver()) {
				int winner = printWinner();
				for (var p: playerOrder) {
					p.notifyGameOver(winner);
				}
				break;
			}

		}
		if(!zArmy) { zombieArmy(); }
		if (!gameOver()) {
			//add points and zombies from the undealt cards to the winner of the last trick
			int undealtPoints = undealtPoints();
			int undealtZombies = countZombies(undealt);
			playerScores.set(firstPlayer,playerScores.get(firstPlayer)+undealtPoints);
			playerOrder.get(firstPlayer).addPoints(undealtPoints);
			zombieCount.set(firstPlayer, zombieCount.get(firstPlayer)+undealtZombies);
			if(!zArmy) { zombieArmy(); }
		
			if (debug) {
				System.out.println("\n" + playerOrder.get(firstPlayer).getName() + " won the last trick "
						+ "and took " + undealtPoints + " points from the undealt cards.\n");
				printPoints();
				for (int i=0; i < zombieCount.size(); i++) { System.out.println("Player " + (i+1) + " has " 
						+ zombieCount.get(i) + " zombies."); }
				System.out.println();			
			}
			
			for(var p:playerOrder) {
				p.notifyHandOver(playerScores);
			}
				
			if (gameOver()) {
				printWinner();
			}
		}

		printTotalPoints();
				
		for (int i = 0; i < undealt.size(); i++) { cardsPlayed.restockDeck(undealt.get(i)); }
		undealt.clear();


//		System.out.println("------------------------------------------");
//		System.out.println("Game Summary:");
//		System.out.println("------------------------------------------\n");
//		printPoints();
//		printWinner();
//		printTotalPoints();
//		System.out.println("Press ENTER to start the next game.");
//	    s = in.nextLine();
//		final String ANSI_CLS = "\u001b[2J";
//        final String ANSI_HOME = "\u001b[H";
//        System.out.print(ANSI_CLS + ANSI_HOME);
//        System.out.println();
//        System.out.flush();
//		//cardsPlayed.printDeck(); 		// debugging to make sure that all cards have returned to the deck

	}
}	


