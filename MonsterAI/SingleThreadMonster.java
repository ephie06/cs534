import java.io.File;
import java.io.PrintStream;

public class SingleThreadMonster {

    public static void main(String[] args) {

        long start = System.currentTimeMillis();
        System.out.println("Welcome to Monster");
        //while (System.currentTimeMillis() - start < 1000000000) {

        LinearModel.INSTANCE.loadWeightsFromFile("model_100000.obj");
        System.out.println(LinearModel.INSTANCE);

        // Initalize the deck of cards

        // Assume this order is clockwise 

        // at the end of every game, we will have all the cards back in the deck
        // thing.printDeck();

        // Play Multiple Games
        PrintStream data;
        try {
			new File("log/RL_results/").mkdirs();
			data = new PrintStream(new File("log/RL_results/" + Thread.currentThread().getId() + ".csv"));
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
        data.println(LinearModel.INSTANCE.toString());
        
	     for (int i = 0; i < 100000; i++) {
	    	 
	    	 Player p1 = new RandomPlayAI("f");
	    	 Player p2 = new MCRLPlayer("MCRL 1", 100, 1, false);
//	    	 Player p2 = new RandomPlayAI("Random Player 2");
	    	 Player p3 = new RandomPlayAI("h");

	    	 Deck thing = new Deck();
	        Game round = new Game(thing, p1, p2, p3);
	        round.debug = false;
	        while (!round.gameOver()) {
	            round.playNewGame();
	        }
	        data.println(round.getWinner());
	     
	     }
	     data.close();
	     LinearModel.INSTANCE.saveWeightsToFile("model_200000.obj");
	        //}
	     System.out.println(LinearModel.INSTANCE);
	    }
}