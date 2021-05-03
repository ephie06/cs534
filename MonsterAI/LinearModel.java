import java.util.Random;



public class LinearModel {

    //TODO: Change Attributes to fit our part 1
    public double[] suitsMissing_weight;
    public double[] suitHighCard_weight;
    public double suitOfTrick_weight;
    public double highestValueCardPlayed_weight;
    public double valueOfTrick_weight;
    public double roundNumber_weight;
    public double bias;//the bias term

    public static final int FEATURE_NUMBER = 11;


    /**
     * Randomy assigns staring weights, to begin training
     */
    public LinearModel() {
        Random random = new Random();
        this.suitsMissing_weight = new double[]{random.nextDouble(), random.nextDouble(), random.nextDouble()};
        this.suitHighCard_weight = new double[]{random.nextDouble(), random.nextDouble(), random.nextDouble()};
        this.suitOfTrick_weight = random.nextDouble();
        this.highestValueCardPlayed_weight = random.nextDouble();
        this.valueOfTrick_weight = random.nextDouble();
        this.roundNumber_weight = random.nextDouble();
        this.bias = random.nextDouble();
    }

    /**
     * Get the predicted value of the game state and move
     * @param move the card the AI played
     * @param state the state we are currently playing
     * @return the predicted value of the state move(how good that move is)(exp. just our score)
     */
    public double makePrediction(Card move, State state) {

        //TODO: Call makeOneMove fcn from NewRolloutPlayer/MCTSPlayer (inputting parameter move)
        State afterState = state;

        double suitOfTrick_double = 0;
        double highestValueCardPlayed_double = 0;

        //plug into linear model
        double ret =
                afterState.suitsMissing[0]*this.suitsMissing_weight[0] + afterState.suitsMissing[1]*this.suitsMissing_weight[1] + afterState.suitsMissing[2]*this.suitsMissing_weight[2]
                + afterState.suitHighCard[0]*this.suitHighCard_weight[0] + afterState.suitHighCard[1]*this.suitHighCard_weight[1] + afterState.suitHighCard[2]*this.suitHighCard_weight[2]
                + suitOfTrick_double*this.suitOfTrick_weight + highestValueCardPlayed_double*this.highestValueCardPlayed_weight
                + afterState.valueOfTrick*this.valueOfTrick_weight + afterState.roundNumber*this.roundNumber_weight + bias;
        return ret;
    }

    //Deleted GetState fcn

    /**
     * Updates the weights based on the result of the hand rollout
     * @param move - the card the AI played
     * @param state - the original game
     * @param rolloutResult - the rollout result(how good the game ended up)(exp from above. our actual score)
     */
    public void updateWeights(Card move, State state, double rolloutResult) {
        //System.out.println(this);

        //TODO: Figure these values out
        double suitOfTrick_double = 0;
        double highestValueCardPlayed_double = 0;

        //Gradient descent: w -= epsilon*gradient
        //Gradient: gradient = x(y-(x*w))/shape_of_y
        double epsilon = 0.0001;//step size
        double yhat = makePrediction(move, state);//get the predicted value
        double difference = yhat - rolloutResult;//get the difference between predicted and result

        //TODO: Call makeOneMove fcn from NewRolloutPlayer/MCTSPlayer (inputting parameter move)
        State afterState = state;
        //System.out.println(difference + " = " + yhat + " - " + rolloutResult);

        //update weights to get closer to actual values
        this.suitsMissing_weight[0] -= epsilon*(afterState.suitsMissing[0]*difference) / FEATURE_NUMBER;
        this.suitsMissing_weight[1] -= epsilon*(afterState.suitsMissing[1]*difference) / FEATURE_NUMBER;
        this.suitsMissing_weight[2] -= epsilon*(afterState.suitsMissing[2]*difference) / FEATURE_NUMBER;

        this.suitHighCard_weight[0] -= epsilon*(afterState.suitHighCard[0]*difference) / FEATURE_NUMBER;
        this.suitHighCard_weight[1] -= epsilon*(afterState.suitHighCard[1]*difference) / FEATURE_NUMBER;
        this.suitHighCard_weight[2] -= epsilon*(afterState.suitHighCard[2]*difference) / FEATURE_NUMBER;
        
        this.suitOfTrick_weight -= epsilon*(suitOfTrick_double*difference) / FEATURE_NUMBER;
        this.highestValueCardPlayed_weight -= epsilon*(highestValueCardPlayed_double*difference) / FEATURE_NUMBER;
        this.valueOfTrick_weight -= epsilon*(afterState.valueOfTrick*difference) / FEATURE_NUMBER;
        this.roundNumber_weight -= epsilon*(afterState.roundNumber*difference) / FEATURE_NUMBER;

        this.bias -= epsilon*difference / FEATURE_NUMBER;
        //System.out.println(this + "\nend\n\n");
    }




}