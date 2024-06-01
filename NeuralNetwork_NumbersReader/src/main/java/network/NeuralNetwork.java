package network;

import data.Image;
import data.MatrixUtility;
import layers.Layer;

import java.util.ArrayList;
import java.util.List;

import static data.MatrixUtility.add;
import static data.MatrixUtility.multiply;
import static data.MatrixUtility.getMaxIndex;

public class NeuralNetwork {

    List<Layer> _layers;

    /*Used for scaling the input numbers to some smaller number rather than,
    * for example, 255 for white pixel value. Helps the network to learn
    * faster and more efficient because the sums won't be huge numbers
    * anymore.*/
    double scalingFactor;

    public NeuralNetwork(List<Layer> _layers, double scalingFactor) {
        this._layers = _layers;
        this.scalingFactor = scalingFactor;
        linkLayers();
    }


    private void linkLayers(){
        if(_layers.size() <= 1){
            return;
        }

        for(int i=0; i<_layers.size(); i++) {
            if(i==0){
                _layers.get(i).set_nextLayer(_layers.get(i+1));
            } else if (i == _layers.size() - 1){
                _layers.get(i).set_previousLayer(_layers.get(i-1));
            } else {
                _layers.get(i).set_previousLayer(_layers.get(i-1));
                _layers.get(i).set_nextLayer(_layers.get(i+1));
            }
        }


    }

    /**
     * Calculates the error between the network output and the expected output.
     *
     * @param networkOutput The output of the neural network.
     * @param correctAnswer The correct class label represented as an integer.
     * @return The error vector between the network output and the expected output.
     */
    public double[] getErrors(double[] networkOutput, int correctAnswer) {
        int numClasses = networkOutput.length;

        double[] expected = new double[numClasses];

        expected[correctAnswer] = 1;

        return add(networkOutput, multiply(expected, -1));

    }


    public int guess(Image image){
        List<double[][]> inputList = new ArrayList<>(); //CNN works with list of matrices, so that is the input even tho it's one picture. Throughout the layers, List will be used.
        inputList.add(multiply(image.getData(), 1.0/scalingFactor));

        double[] networkOutput = _layers.getFirst().getOutput(inputList);
        int guess = getMaxIndex(networkOutput);

        return guess;
    }

    public double testAccuracy(List<Image> images){
        int correct = 0;

        for(var image: images){
            int guess = guess(image);

            if(guess == image.getLabel()){
                correct++;
            }
        }

        return((double)correct/ images.size());
    }

    public void train(List<Image> images){

        for(var image: images){
            List<double[][]> inputList = new ArrayList<>();
            inputList.add(multiply(image.getData(), 1.0/scalingFactor));

            double[] networkOutput = _layers.getFirst().getOutput(inputList);
            double[] dLdO = getErrors(networkOutput, image.getLabel());

            _layers.getLast().backPropagation(dLdO);

        }
    }

}
