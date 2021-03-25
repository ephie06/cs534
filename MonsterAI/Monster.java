public class Monster {
	public static void main(String[] args) {
		System.out.println("Welcome to Monster");

		// Initalize the deck of cards
		Deck thing = new Deck();

		// Assume this order is clockwise
		Player p1 = new RandomPlayAI("Random1");
		Player p2 = new GDPlayer("GD Player");
		Player p3 = new RandomPlayAI("Random3");

		// at the end of every game, we will have all the cards back in the deck
		// thing.printDeck();

		// Play Multiple Games
		int numberOfGames = 1;
		Game round = new Game(thing, p1, p2, p3);
//		while (!round.gameOver()) {
			System.out.println("\n--------------------------------------------");
			System.out.println("--------------------------------------------");
			System.out.println("--------------------------------------------");
			System.out.println("Playing Game #"+numberOfGames);
			System.out.println("--------------------------------------------");
			System.out.println("--------------------------------------------");
			System.out.println("--------------------------------------------\n");
			round.playNewGame();
			numberOfGames++;
//		}
		

	}
}