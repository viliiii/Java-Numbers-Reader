package layers;

import java.util.List;
import java.util.Random;

public class FullyConnectedLayer extends Layer{

    private long SEED;

    private boolean isLast;

    private double[][] _weights;
    private int _inputLength;
    private int _outputLength;

    private double learningRate;

    private double[] lastNets;      //array of net values for each neuron in this layer (used for backpropagation)
    private double[] lastInput;     //array of input values from the layer before. (same usage)

    public FullyConnectedLayer(int _inputLength, int _outputLength, long SEED, double learningRate, boolean isLast) {
        this._inputLength = _inputLength;
        this._outputLength = _outputLength;
        this.SEED = SEED;
        this.learningRate = learningRate;
        this.isLast = isLast;

        _weights = new double[_inputLength][_outputLength];
        setRandomWeights();
    }

    public double[] fullyConnectedLayerPass(double[] input){

        lastInput = input;

        double[] outNets = new double[_outputLength];
        double[] outStep = new double[_outputLength];

        for(int i = 0; i < _inputLength; i++){
            for(int j = 0; j < _outputLength; j++){
                outNets[j] += input[i]*_weights[i][j];
            }
        }

        lastNets = outNets;

        if(!isLast){
            for(int i = 0; i < _outputLength; i++){
                outStep[i] = reLu(outNets[i]);
            }
        }else{
            outStep = softMax(outNets);
        }



        return outStep;

    }

    @Override
    public double[] getOutput(List<double[][]> input) {
        double[] vector = matrixToVector(input);
        return getOutput(vector);
    }

    @Override
    public double[] getOutput(double[] input) {
        double[] forwardPassOutput = fullyConnectedLayerPass(input);

        if(_nextLayer != null) {
            return _nextLayer.getOutput(forwardPassOutput);
        }else {
            return forwardPassOutput;
        }
    }


    @Override
    public void backPropagation(List<double[][]> dLdO) {
        double[] vector = matrixToVector(dLdO);
        backPropagation(vector);
    }

    /**
     * Standard formula used for adjusting weights is at 13:35 in:
     * <a href="https://www.youtube.com/watch?v=JJUlkPFq1q8">...</a>
     * Formula for   is at 23:17.
     * Every weight in this layer is subtracted
     * by the dL/dw calculated using the formula.
     * @param dLdO loss from the next layer (in fact the layer before in the
     * backpropagation process).
     */
    @Override
    public void backPropagation(double[] dLdO) {

        double[] dLdX = new double[_inputLength];   //result to be passed to the previous layer

        double dOdZ;
        double dZdw;
        double dLdw;

        double dZdX;

        for(int k=0; k < _inputLength; k++) {
            double dLdX_sum = 0;
            for(int j=0; j < _outputLength; j++) {

                if(!isLast){
                    dOdZ = derivativeReLu(lastNets[j]);
                }else {
                    dOdZ = lastNets[j];
                }

                dZdw = lastInput[k];
                dZdX = _weights[k][j];

                dLdw = dLdO[j] * dOdZ * dZdw;

                _weights[k][j] -= dLdw * learningRate;

                dLdX_sum += dLdO[j] * dOdZ * dZdX;

            }
            dLdX[k] = dLdX_sum;
        }

        if(_previousLayer != null){
            _previousLayer.backPropagation(dLdX);
        }

    }

    @Override
    public int getOutputLength() {
        return 0;
    }

    @Override
    public int getOutputRows() {
        return 0;
    }

    @Override
    public int getOutputCols() {
        return 0;
    }

    @Override
    public int getOutputElements() {
        return _outputLength;   //number of elements in output vector
    }

    /**
     * This method sets the weights of the fully connected layer to random values.
     * The weights are initialized using a Gaussian distribution with mean 0 and standard deviation 1.
     * The random seed is provided by the 'SEED' field.
     */
    public void setRandomWeights(){
        Random random = new Random(SEED);

        for(int i = 0; i <_inputLength; i++){
            for(int j = 0; j < _outputLength; j++){
                _weights[i][j] = random.nextGaussian();
            }
        }
    }

    public double[] softMax(double[] inputs) {
        double max = Double.NEGATIVE_INFINITY;
        for (double input : inputs) {
            if (input > max) {
                max = input;
            }
        }

        double sum = 0.0;
        double[] expValues = new double[inputs.length];
        for (int i = 0; i < inputs.length; i++) {
            expValues[i] = Math.exp(inputs[i] - max); // Subtract max to avoid overflow
            sum += expValues[i];
        }

        double[] softmax = new double[inputs.length];
        for (int i = 0; i < inputs.length; i++) {
            softmax[i] = expValues[i] / sum;
        }

        return softmax;
    }


    public double reLu(double input){
        return input <= 0 ? 0 : input;
    }

    public double derivativeReLu(double input){
        return input <= 0 ? 0.01 : 1;   //not 0 because that would be too rigid.
    }
}
