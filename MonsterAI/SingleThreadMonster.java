public class SingleThreadMonster {

    public static void main(String[] args) {
    	LinearModel.INSTANCE.loadWeightsFromFile("model_1.obj");
    	System.out.println(LinearModel.INSTANCE);
        System.out.println("Welcome to Monster");

        // Initalize the deck of cards
        Deck thing = new Deck();

        // Assume this order is clockwise
        Player p1 = new GDPlayer("GD Player 1");
        Player p2 = new MCRLPlayer("MCRL", 100, 1, LinearModel.INSTANCE);
        Player p3 = new GDPlayer("GD Player 3");

        // at the end of every game, we will have all the cards back in the deck
        // thing.printDeck();

        // Play Multiple Games
        int numberOfGames = 5;
        Game round = new Game(thing, p1, p2, p3);
        while (!round.gameOver()) {
            round.playNewGame();
            numberOfGames++;
        }
        LinearModel.INSTANCE.saveWeightsToFile("model_1.obj");
        System.out.println(LinearModel.INSTANCE);
    }
}