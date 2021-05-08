public class SingleThreadMonster {

    public static void main(String[] args) {
        LinearModel linearModel = LinearModel.getInstance();
        System.out.println("Welcome to Monster");

        // Initalize the deck of cards
        Deck thing = new Deck();

        // Assume this order is clockwise
        Player p1 = new GDPlayer("GD Player 1");
        Player p2 = new MCRLPlayer("MCRL", 100, linearModel);
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
            linearModel.updateWeights();
            System.out.println(linearModel);
    }
}