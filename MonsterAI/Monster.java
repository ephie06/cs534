import java.io.File;
import java.io.PrintStream;

public class Monster {

	static class MultithreadingDemo extends Thread {
		
		PrintStream data;
		int times;
		
		public MultithreadingDemo (int times) {
			super();
			this.times = times;
		}
		
		public Game oneMatch() {
			try {
				// Displaying the thread that is running
				System.out.println("Thread " + Thread.currentThread().getId() + " is running");

				// Initalize the deck of cards
				Deck thing = new Deck();

				// Assume this order is clockwise
				Player p1 = new GDPlayer("gd1");
				Player p2 = new MCTSPlayer("MCTS", 1000);
				Player p3 = new GDPlayer("gd3");

				// at the end of every
				// game, we will have all the cards back in the deck
				// thing.printDeck();

				// Play Multiple Games
				int numberOfGames = 1;
				Game round = new Game(thing, p1, p2, p3);
//				round.debug = true;
				while (!round.gameOver()) {
/*					System.out.println("\n--------------------------------------------");
					System.out.println("--------------------------------------------");
					System.out.println("--------------------------------------------");
					System.out.println("Playing Game #"+numberOfGames);
					System.out.println("--------------------------------------------");
					System.out.println("--------------------------------------------");
					System.out.println("--------------------------------------------\n");*/
					round.playNewGame();
					numberOfGames++;

				}
				return round;
			}
			catch (Exception e) {
				// Throwing an exception
				e.printStackTrace();
				System.out.println("Exception is caught");
			}
			return null;
		}
		
		public void run()
		{
			try {
				new File("log/mcts_no_table/").mkdirs();
				data = new PrintStream(new File("log/mcts_no_table/" + Thread.currentThread().getId() + ".csv"));
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			for (int i=0; i<times; i++) {
				Game g = oneMatch();
				if (g!=null) {
					data.printf("%d, %d, %d, %s\n", 
							g.playerOrder.get(0).getPoints(),
							g.playerOrder.get(1).getPoints(),
							g.playerOrder.get(2).getPoints(),
							g.printWinner());
				}
			}
			
			data.close();

		}
	}

	public static void main(String[] args) {

		int n = 10; // Number of threads
		for (int i = 0; i < n; i++) {
			MultithreadingDemo object = new MultithreadingDemo(20);
			object.start();
		}

/*		System.out.println("Welcome to Monster");

		// Initalize the deck of cards
		Deck thing = new Deck();

		// Assume this order is clockwise
		Player p1 = new RandomPlayAI("Random1");
		Player p2 = new NewRolloutPlayer("NRP", 100);
		Player p3 = new RandomPlayAI("Random3");

		// at the end of every
		// game, we will have all the cards back in the deck
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
		
		}*/
		

	}
}