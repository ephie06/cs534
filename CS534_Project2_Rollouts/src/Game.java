import java.util.LinkedList;

public class Game {
	
	protected int playerCount;
	protected int turnCount;
	
	protected Game() {
		playerCount = 3;
		turnCount = 0;
	}		
	
	private static Player currentPlayer;
	
	private static volatile DeckOfCards deck;
	private static Player[] players;
	private static Game monsterGame;
	static boolean playing;
	public LinkedList<Card> undealt;
	
	public static final void play() {
		if(!playing) {
			monsterGame = new Game();
		}
		monsterGame.initialize();
		monsterGame.start();
	}
	
	public final void initialize() {
		players = new Player[3];
		players[0] = new Player(Player.PLAYER_ONE);
		players[1] = new Player(Player.PLAYER_TWO);
		players[2] = new Player(Player.PLAYER_THREE);
		currentPlayer = null;
		playing = true;
	}
	
	public final void start() {
		round();
	}
	
	protected final void round() {
		deck = new DeckOfCards();
		deck.printDeck();
		System.out.println();
		//deal cards
		for(int i=0; i<54; i++) {
			players[i%3].addToHand(deck.drawCard());
		}
		//6 undealt cards
		undealt = new LinkedList<Card>();
		for(int i=0; i<6; i++) {
			undealt.add(deck.drawCard());
		}
		for(int i=0; i<3; i++) {
			players[i].sortHand();
			LinkedList<Card> hand = players[i].getHand();
			for(int j = 0; j<hand.size(); j++) {
				hand.get(j).printCard();
			} System.out.println();
		}

		System.out.print("Undealt: ");
		for(int i=0; i<undealt.size(); i++) {
			undealt.get(i).printCard();
		}
		
	}
	
	public static void main(String[] args){
		Game.play();
	}

}
