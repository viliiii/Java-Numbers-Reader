package network;

import layers.ConvolutionLayer;
import layers.FullyConnectedLayer;
import layers.Layer;
import layers.MaxPoolLayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that is like a Builder design pattern for creating a NeuralNetwork.
 * Use this class with simple methods to build a network for later use.
 * First add all wanted layers in wanted order by calling methods in certain order, then
 * call the method build().
 */
public class NetworkBuilder {

    private NeuralNetwork network;
    private int _inputRows;
    private int _inputCols;
    private double _scalingFactor;
    List<Layer> _layers;

    public NetworkBuilder(int _inputRows, int _inputCols, double scalingFactor) {
        this._inputRows = _inputRows;
        this._inputCols = _inputCols;
        this._scalingFactor = scalingFactor;

        _layers = new ArrayList<>();
    }

    public void addConvolutionLayer(int numFilters, int filterSize, int stepSize, double learningRate, long SEED){
         if(_layers.isEmpty()){
             /*Input length is 1 because this layer is the first layer, and input is one image representation matrix.*/
             _layers.add(new ConvolutionLayer(filterSize, stepSize, 1, _inputRows, _inputCols, SEED, numFilters, learningRate));
         } else {
             Layer prev = _layers.getLast();
             _layers.add(new ConvolutionLayer(filterSize, stepSize, prev.getOutputLength(), prev.getOutputRows(), prev.getOutputCols(), SEED, numFilters, learningRate));
         }
    }

    public void addMaxPoolLayer(int windowSize, int stepSize){
        if(_layers.isEmpty()){
            /*Input length is 1 because this layer is the first layer, and input is one image representation matrix.*/
            _layers.add(new MaxPoolLayer(stepSize, windowSize, 1, _inputRows, _inputCols));
        } else {
            Layer prev = _layers.getLast();
            _layers.add(new MaxPoolLayer(stepSize, windowSize, prev.getOutputLength(), prev.getOutputRows(), prev.getOutputCols()));
        }
    }

    public void addFullyConnectedLayer(int outLength, double learningRate, long SEED, boolean isLast){
        if(_layers.isEmpty()){
            /*Input length is 1 because this layer is the first layer, and input is one image representation matrix.*/
            _layers.add(new FullyConnectedLayer(_inputRows*_inputCols, outLength, SEED, learningRate, isLast));
        } else {
            Layer prev = _layers.getLast();
            _layers.add(new FullyConnectedLayer(prev.getOutputElements(), outLength, SEED, learningRate, isLast));
        }
    }

    public NeuralNetwork build() {
        network = new NeuralNetwork(_layers, _scalingFactor);
        return network;
    }



}
