public class Monster {

	static class MultithreadingDemo extends Thread {
		public void run()
		{
			try {
				// Displaying the thread that is running
				System.out.println("Thread " + Thread.currentThread().getId() + " is running");

				// Initalize the deck of cards
				Deck thing = new Deck();
//				thing.printDeck();
				// Assume this order is clockwise
				Player p1 = new GDPlayer("Random1");
				Player p2 = new MCTSPlayer("MCTS", 500);
				Player p3 = new GDPlayer("Random3");

				// at the end of every
				// game, we will have all the cards back in the deck
				// thing.printDeck();

				// Play Multiple Games
				int numberOfGames = 1;
				Game round = new Game(thing, p1, p2, p3);
				round.debug=true;
				while (!round.gameOver()) {
///*					System.out.println("\n--------------------------------------------");
//					System.out.println("--------------------------------------------");
//					System.out.println("--------------------------------------------");
//					System.out.println("Playing Game #"+numberOfGames);
//					System.out.println("--------------------------------------------");
//					System.out.println("--------------------------------------------");
//					System.out.println("--------------------------------------------\n");*/
					round.playNewGame();
					numberOfGames++;
					round.printTotalPoints();
				}
			}
			catch (Exception e) {
				// Throwing an exception
				System.out.println("Exception is caught");
			}
		}
	}

	public static void main(String[] args) {

		int n = 1; // Number of threads
		for (int i = 0; i < n; i++) {
			MultithreadingDemo object = new MultithreadingDemo();
			object.start();
		}
/*		System.out.println("Welcome to Monster");

		// Initalize the deck of cards
		Deck thing = new Deck();

		// Assume this order is clockwise
		Player p1 = new RandomPlayAI("Random1");
		Player p2 = new MCTSPlayer("MCTS Player", 1000);
		Player p3 = new RandomPlayAI("Random3");

		// at the end of every game, we will have all the cards back in the deck
		// thing.printDeck();

		// Play Multiple Games
		int numberOfGames = 1;
		Game round = new Game(thing, p1, p2, p3);
		while (!round.gameOver()) {
			System.out.println("\n--------------------------------------------");
			System.out.println("--------------------------------------------");
			System.out.println("--------------------------------------------");
			System.out.println("Playing Game #"+numberOfGames);
			System.out.println("--------------------------------------------");
			System.out.println("--------------------------------------------");
			System.out.println("--------------------------------------------\n");
			round.playNewGame();
			numberOfGames++;
		
		}
		

	}
}