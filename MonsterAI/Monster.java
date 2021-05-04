import java.io.File;
import java.io.PrintStream;

public class Monster {

	
		
		static PrintStream data;
		static int times = 1;
		
		
		
		public static Game oneMatch() {
			try {
				// Displaying the thread that is running
				//System.out.println("Thread " + Thread.currentThread().getId() + " is running");

				// Initalize the deck of cards
				Deck thing = new Deck();

				// Assume this order is clockwise
				Player p1 = new RandomPlayAI("rand1");
				Player p2 = new RLPlayer("rl2", 10);
				Player p3 = new RandomPlayAI("rand3");

				// at the end of every
				// game, we will have all the cards back in the deck
				// thing.printDeck();

				// Play Multiple Games
				int numberOfGames = 1;
				Game round = new Game(thing, p1, p2, p3);
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
		
		public static void main(String[] args)
		{
			try {
				new File("log/rdt/").mkdirs();
				data = new PrintStream(new File("log/rdt/" + "mctsVgd" + ".csv"));
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

