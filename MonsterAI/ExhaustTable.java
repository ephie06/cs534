import java.util.HashMap;

public class ExhaustTable extends HashMap<Suit, boolean[]> {
    private static final long serialVersionUID = 1L;

    public ExhaustTable() {
        super();
        init();
    }

    void init() {
        put(Suit.FAIRIES, new boolean[3]);
        put(Suit.TROLLS, new boolean[3]);
        put(Suit.ZOMBIES, new boolean[3]);
        put(Suit.UNICORNS, new boolean[3]);
    }

    void copyFrom(HashMap<Suit, boolean[]> table) {
        for (Suit key: keySet()) {
            for (int i=0; i<3; i++) {
                get(key)[i] = table.get(key)[i];
            }
        }
    }

    void logicOr(HashMap<Suit, boolean[]> table) {
        for (Suit key: keySet()) {
            for (int i=0; i<3; i++) {
                get(key)[i] = get(key)[i] || table.get(key)[i];
            }
        }
    }

    ExhaustTable deepCopy() {
        ExhaustTable n = new ExhaustTable();
        n.copyFrom(this);
        return n;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Suit key: keySet()) {
            switch (key) {
                case UNICORNS: sb.append("🦄\t"); break;
                case FAIRIES: sb.append ("🧚\t"); break;
                case TROLLS: sb.append( "👺\t"); break;
                case ZOMBIES: sb.append("\uD83E\uDDDF\t"); break;
            }

            for (int i=0; i<3; i++) {
                sb.append(get(key)[i]).append("\t");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

}