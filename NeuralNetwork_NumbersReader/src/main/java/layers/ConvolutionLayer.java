package layers;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static data.MatrixUtility.*;

public class ConvolutionLayer extends Layer{

    private long SEED;  // seed for generating random filters

    private List<double[][]> _filters;
    private int _filterSize;
    private int _stepSize;

    private int _inputLength;
    private int _inputRows;
    private int _inputCols;

    private double _learningRate;

    private List<double[][]> _lastInput;    // the last input into this layer.

    public ConvolutionLayer(int _filterSize, int _stepSize, int _inputLength, int _inputRows, int _inputCols, long SEED, int numberOfFilters, double learningRate) {
        this._filterSize = _filterSize;
        this._stepSize = _stepSize;
        this._inputLength = _inputLength;
        this._inputRows = _inputRows;
        this._inputCols = _inputCols;
        this.SEED = SEED;
        this._learningRate = learningRate;

        generateRandomFilters(numberOfFilters);

    }

    private void generateRandomFilters(int numOfFilters) {
        List<double[][]> filters = new ArrayList<>();
        Random random = new Random(SEED);

        for(int n=0; n<numOfFilters; n++) {
            double[][] filter = new double[_filterSize][_filterSize];

            for(int i = 0; i< _filterSize; i++) {
                for(int j = 0; j< _filterSize; j++) {

                    double value = random.nextGaussian();
                    filter[i][j] = value;
                }
            }

            filters.add(filter);
        }

        _filters = filters;
    }

    /**
     * Filters every input matrix through every convolutional filter matrix.
     * Convolves the input matrix with every filter matrix from this layer.
     * @param list
     * @return
     */
    public List<double[][]> convolutionForwardPass(List<double[][]> list) {
        _lastInput = list;

        List<double[][]> output = new ArrayList<>();

        for(int m=0; m<list.size(); m++) {
            for(var filter: _filters){
                output.add(convolve(list.get(m), filter, _stepSize));
            }
        }
        return output;
    }

    private double[][] convolve(double[][] input, double[][] filter, int stepSize) {
         int outputRows = (input.length - filter.length) / stepSize + 1;
         int outputCols = (input[0].length - filter[0].length) / stepSize + 1;

         int inputRows = input.length;
         int inputCols = input[0].length;

         int filterRows = filter.length;
         int filterCols = filter[0].length;

         double[][] output = new double[outputRows][outputCols];

         int outRow = 0;    //for iterating over convolution result matrix while filling it.
         int outCol;

         for(int i = 0; i <= inputRows - filterRows; i += stepSize) {

             outCol = 0;

             for(int j = 0; j <= inputCols - filterCols; j += stepSize) {
                 double sum = 0;
                 //filter from this i j position.
                 for(int x=0; x < filterRows; x++) {
                     for(int y=0; y < filterCols; y++) {
                         sum += filter[x][y] * input[i+x][j+y];
                     }
                 }

                 output[outRow][outCol++] = sum;

             }
             outRow++;
         }

         return output;

    }

    /** Enlarges the input matrix, moves the values of the input array to the corners and
     * fills the new spaces with zeros. Used for simplifying the process of backpropagation.
     * After this method is applied to the matrix of loss from the upper layer, all this
     * layer needs to do is apply the convolution on the input matrix and spaced matrix of loss
     * and subtract that matrix from the filter(s).
     * @param input matrix to be spaced
     * @return new spaced matrix
     */
    public double[][] spaceMatrix(double[][] input){

        if(_stepSize == 1) return input;

        int outRows = (input.length - 1)*_stepSize + 1;
        int outCols = (input[0].length - 1)*_stepSize + 1;

        double[][] output = new double[outRows][outCols];

        for(int i = 0; i < input.length; i++){
            for(int j = 0; j < input[0].length; j++){
                output[i*_stepSize][j*_stepSize] = input[i][j];
            }
        }
        return output;
    }


    @Override
    public double[] getOutput(List<double[][]> input) {
        List<double[][]> output = convolutionForwardPass(input);
        return _nextLayer.getOutput(output);    //convolution layer is never the last layer so no need for checking if there is a next layer
    }

    @Override
    public double[] getOutput(double[] input) {
        List<double[][]> matrixInput = vectorToMatrix(input, _inputLength, _inputRows, _inputCols);
        return getOutput(matrixInput);
    }

    @Override
    public void backPropagation(List<double[][]> dLdO) {
        /*Each filter could be applied more than once because we have
         a list of matrices as an input in this layer, and every matrix
         needs to be put through every filter.
         So we need to calculate the error for every filter more than once by summing.*/
        List<double[][]> filtersDelta = new ArrayList<>();
        List<double[][]> dLdOPreviousLayer = new ArrayList<>();

        for(int f=0; f<_filters.size(); f++){
            filtersDelta.add(new double[_filterSize][_filterSize]);
        }

        for(int i=0; i<_lastInput.size(); i++){

            double[][] errorForInput = new double[_inputRows][_inputCols];

            for(int f=0; f<_filters.size(); f++){

                double[][] currFilter = _filters.get(f);
                double[][] error = dLdO.get(i*_filters.size() + f); //dL/dO matrix of error for current input and current filter

                double[][] spacedError = spaceMatrix(error);
                double[][] dLdF = convolve(_lastInput.get(i), spacedError, 1);

                /*You need to multiply dLdF by the learning rate and add that matrix
                * to the curr filter. Then the filter is updated. Using custom my custom
                * Matrix utility class for that because there isn't one in Java by default.*/

                double[][] delta = multiply(dLdF, _learningRate*-1);    // It will be added, not subtracted, so the learning rate is multiplied by negative one.
                double[][] newTotalDelta = add(filtersDelta.get(f), delta);
                filtersDelta.set(f, newTotalDelta);

                /*Doing a full convolution on flipped and spaced error matrix (sliding window)
                * and current filter. The list of results should be passed to the previous layer as
                * an input for the backpropagation. */

                double[][] flippedError = flipHorizontal(flipVertical(spacedError));
                errorForInput = add(errorForInput, fullConvolve(currFilter, flippedError));
            }

            dLdOPreviousLayer.add(errorForInput);

        }

        for(int f=0; f< _filters.size(); f++) {
            double[][] modified = add(filtersDelta.get(f), _filters.get(f));
            filtersDelta.set(f, modified);
        }

        /*Now you need to send this layer's error to the layer below.
        * https://www.youtube.com/watch?v=njlyOAiK_yE  the formula is at 23:18
        * and the proof/explanation is from 18:05.
        * Error from our inputs is equivalent to FullConvolution of filter and
        * spaced and mirrored/flipped matrix of our output which we get through this method. */

        if(_previousLayer != null){
            _previousLayer.backPropagation(dLdOPreviousLayer);
        }

    }

    @Override
    public void backPropagation(double[] dLdO) {
        List<double[][]> matrixInput = vectorToMatrix(dLdO, _inputLength, _inputRows, _inputCols);
        backPropagation(matrixInput);
    }

    /**
     *
     * Visual explanation is in the following video at 22:24
     * <a href="https://www.youtube.com/watch?v=njlyOAiK_yE">...</a>
     * @param input the sliding window dL/dO matrix
     * @param filter the filter
     * @return the result of full convolution with the sliding window and the filter
     */
    private double[][] fullConvolve(double[][] input, double[][] filter) {
        int outputRows = (input.length + filter.length)  + 1;
        int outputCols = (input[0].length + filter[0].length)  + 1;

        int inputRows = input.length;
        int inputCols = input[0].length;

        int filterRows = filter.length;
        int filterCols = filter[0].length;

        double[][] output = new double[outputRows][outputCols];

        int outRow = 0;    //for iterating over convolution result matrix while filling it.
        int outCol;

        /*The window of filter size (loss matrix (dL/dOutput) spaced to the size of filter)
        travels through the filter matrix. index i starts from the -filterRows + 1 and j starts
        from the -filterCols + 1 because i and j are the indexes of the sliding window matrix
        that slides through the filter matrix.*/
        for(int i = -filterRows + 1; i <= inputRows; i ++) {

            outCol = 0;

            for(int j = -filterCols + 1; j <= inputCols; j ++) {
                double sum = 0;
                //filter from this i j position.
                for(int x=0; x < filterRows; x++) {
                    for(int y=0; y < filterCols; y++) {
                        int inputRowIndex = i + x;
                        int inputColIndex = j + y;

                        if(inputRowIndex >= 0 && inputColIndex >= 0 && inputRowIndex < inputRows && inputColIndex < inputCols) {
                            sum += filter[x][y] * input[inputRowIndex][inputColIndex];
                        }

                    }
                }

                output[outRow][outCol++] = sum;

            }
            outRow++;
        }

        return output;

    }

    @Override
    public int getOutputLength() {
        return _filters.size() * _inputLength;
    }

    @Override
    public int getOutputRows() {
        return (_inputRows- _filterSize)/_stepSize+1;
    }

    @Override
    public int getOutputCols() {
        return (_inputCols- _filterSize)/_stepSize+1;
    }

    @Override
    public int getOutputElements() {
        return getOutputRows()*getOutputCols()*getOutputLength();
    }
}
