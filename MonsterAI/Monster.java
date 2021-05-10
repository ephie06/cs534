import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;

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
//				Player p1 = new RandomPlayAI("R1");
//				Player p2 = new RLRolloutPlayer("Roll1", 1000, 1, true);
//				Player p3 = new RandomPlayAI("R3");
				
				Player p1 = new RLRolloutPlayer("Roll0", 500, 0 ,false);
				Player p2 = new RLRolloutPlayer("Roll1", 500, 1, false);
				Player p3 = new RLRolloutPlayer("Roll2", 500, 2, false);

				// at the end of every
				// game, we will have all the cards back in the deck
				// thing.printDeck();

				// Play Multiple Games
				int numberOfGames = 1;
				Game round = new Game(thing, p1, p2, p3);
				round.debug = true;
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
				new File("log/temp/").mkdirs();
				data = new PrintStream(new File("log/temp/" + Thread.currentThread().getId() + ".csv"));
			} catch (Exception e) {
				e.printStackTrace();
			}

			for (int i=0; i<times; i++) {
				Game g = oneMatch();
				if (g!=null) {
					data.printf("%d, %d, %d, %d, %d\n",
							g.playerOrder.get(0).getPoints(),
							g.playerOrder.get(1).getPoints(),
							g.playerOrder.get(2).getPoints(),
							g.printWinner(),
							g.playerOrder.get(1).getPoints() - Math.max(g.playerOrder.get(0).getPoints(), g.playerOrder.get(2).getPoints()));
				}
			}

			data.close();

		}
	}

	public static void main(String[] args) throws InterruptedException {
        LinearModel.INSTANCE.loadWeightsFromFile("model_rollout_300.obj");
		int n = 10; // Number of threads
		ArrayList<MultithreadingDemo> ths = new ArrayList<>();
		for (int i = 0; i < n; i++) {
			MultithreadingDemo object = new MultithreadingDemo(30);
			object.start();
			ths.add(object);
		}
		for (var t:ths) {
			t.join();
		}
		
		System.out.println(LinearModel.INSTANCE.toString());
		LinearModel.INSTANCE.saveWeightsToFile("model_rollout_600.obj");
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
