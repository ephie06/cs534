import java.io.File;
import java.io.PrintStream;

public class Monster {

	static class MultithreadingDemo extends Thread {

		PrintStream data;
		int times;
		LinearModel linearModel = LinearModel.INSTANCE;

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
				Player p1 = new RandomPlayAI("random");
				Player p2 = new MCRLPlayer("MCRL", 100, 1, true);
				Player p3 = new RandomPlayAI("random");

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
				new File("log/gd_accu/").mkdirs();
				data = new PrintStream(new File("log/gd_accu/" + Thread.currentThread().getId() + ".csv"));
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

	public static void main(String[] args) throws InterruptedException {

		int n = 1; // Number of threads
		MultithreadingDemo object = new MultithreadingDemo(1);
		for (int i = 0; i < n; i++) {
			object.start();
			object.join();//TODO: May have to delete when running multiple threads
		}
		System.out.println(object.linearModel.toString());

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
