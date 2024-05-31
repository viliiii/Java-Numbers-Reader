package layers;

import java.util.ArrayList;
import java.util.List;

public class MaxPoolLayer extends Layer {

    private int _stepSize;
    private int _windowSize;

    private int _inputLength;
    private int _inputRows;
    private int _inputCols;

    List<int[][]> _lastMaxRow;  // list of matrices that save the information where the maximum values from the input were. (rows)
    List<int[][]> _lastMaxCol;  // -||- (columns)

    public MaxPoolLayer(int _stepSize, int _windowSize, int _inputLength, int _inputRows, int _inputCols) {
        this._stepSize = _stepSize;
        this._windowSize = _windowSize;
        this._inputLength = _inputLength;
        this._inputRows = _inputRows;
        this._inputCols = _inputCols;
    }

    public List<double[][]> maxPoolLayerForwardPass(List<double[][]> input){

        List<double[][]> output = new ArrayList<>();

        _lastMaxRow = new ArrayList<>();
        _lastMaxCol = new ArrayList<>();

        for(int l=0; l<input.size(); l++){
            output.add(pool(input.get(l)));
        }

        return output;

    }

    public double[][] pool(double[][] input){

        double[][] output = new double[getOutputRows()][getOutputCols()];

        int[][] maxRows = new int[getOutputRows()][getOutputCols()];
        int[][] maxCols = new int[getOutputRows()][getOutputCols()];


        for(int r=0; r<getOutputRows(); r += _stepSize){
            for(int c=0; c<getOutputCols(); c += _stepSize) {

                double max = 0;

                maxRows[r][c] = -1; //-1 means no maximum was found at that position
                maxCols[r][c] = -1;

                for(int x=0; x<_windowSize; x++) {
                    for(int y=0; y<_windowSize; y++) {
                        if(input[r+x][c+y] > max) {
                            max = input[r+x][c+y];

                            maxRows[r][c] = r+x;    //so the row of the maximum that lays at [r][c] in the output was r+x
                            maxCols[r][c] = c+y;    //so the column of the maximum that lays at [r][c] in the output was c+y
                        }
                    }
                }

                output[r][c] = max;

            }
        }

        _lastMaxRow.add(maxRows);
        _lastMaxCol.add(maxCols);

        return output;
    }


    @Override
    public double[] getOutput(List<double[][]> input) {
        List<double[][]> poolLayerOutput = maxPoolLayerForwardPass(input);

        return _nextLayer.getOutput(poolLayerOutput);  //pool layer is never the last layer so no need to check if there is a next layer.
    }

    @Override
    public double[] getOutput(double[] input) {
        List<double[][]> matrixList = vectorToMatrix(input, _inputLength, _inputRows, _inputCols);
        return _nextLayer.getOutput(matrixList);
    }

    /**
     * <a href="https://towardsdatascience.com/backpropagation-in-fully-convolutional-networks-fcns-1a13b75fb56a">...</a>
     * <a href="https://www.youtube.com/watch?v=8WrEz-M50oQ&list=PLpcNcOt2pg8k_YsrMjSwVdy3GX-rc_ZgN&index=5">...</a> at 2:08 is the formula
     * @param dLdO
     */
    @Override
    public void backPropagation(List<double[][]> dLdO) {

        List<double[][]> dLdX = new ArrayList<>();  //list of error matrices that are computed through this layer in backpropagation. To pass to the next backpropagation layer.

        int l=0;
        for(var passedMatrix: dLdO){
            double[][] errorMatrix = new double[_inputRows][_inputCols];

            for(int r=0; r<getOutputRows(); r++){
                for(int c=0; c<getOutputCols(); c++){
                    int max_r = _lastMaxRow.get(l)[r][c];
                    int max_c = _lastMaxCol.get(l)[r][c];

                    if(max_r != -1){
                        errorMatrix[max_r][max_c] += passedMatrix[r][c];    //adding the error from upper layer to this layers error matrix at the positions where the pool maximums were.
                    }
                }
            }
            dLdX.add(errorMatrix);
            l++;
        }

        if(_previousLayer != null){
            _previousLayer.backPropagation(dLdX);
        }

    }

    @Override
    public void backPropagation(double[] dLdO) {
        List<double[][]> matrixList = vectorToMatrix(dLdO, getOutputLength(), getOutputRows(), getOutputCols());
        backPropagation(matrixList);
    }

    @Override
    public int getOutputLength() {
        return _inputLength;    //because the pass just pools every matrix from the input list into a smaller matrix
    }

    @Override
    public int getOutputRows() {
        return (_inputRows-_windowSize)/_stepSize + 1;
    }

    @Override
    public int getOutputCols() {
        return (_inputCols-_windowSize)/_stepSize + 1;
    }

    @Override
    public int getOutputElements() {
        return _inputLength * getOutputRows() * getOutputCols();    //number of all the elements from every matrix in the list
    }
}
