import data.DataReader;
import data.DataReaderMNIST;
import data.Image;
import network.NetworkBuilder;
import network.NeuralNetwork;

import java.util.List;

import static java.util.Collections.shuffle;

public class MainMNIST {
    public static void main(String[] args) {
        long SEED = 123;

        System.out.println("Starting data loading...");

        DataReaderMNIST dataReaderMNIST = new DataReaderMNIST();
        DataReader dataReaderImages = new DataReader();

        List<Image> imagesTrain = dataReaderMNIST.readData("C:\\Faks\\numbers_reader\\MNIST_data\\mnist_train.csv");
        List<Image> imagesTest = dataReaderMNIST.readData("C:\\Faks\\numbers_reader\\MNIST_data\\mnist_test.csv");


        System.out.println("Images Train size: " + imagesTrain.size());
        System.out.println("Images Test size: " + imagesTest.size());


        NetworkBuilder builder = new NetworkBuilder(28,28,256*100);
        builder.addConvolutionLayer(8, 5, 1, 0.1, SEED);
        builder.addMaxPoolLayer(3,2);
        builder.addFullyConnectedLayer(10, 0.1, SEED, true);

        /*builder.addConvolutionLayer(32, 3, 1, 0.01, SEED);
        builder.addMaxPoolLayer(3, 3);
        builder.addFullyConnectedLayer(10, 0.01, SEED);*/

        NeuralNetwork net = builder.build();

        double rate = net.testAccuracy(imagesTest);
        System.out.println("Pre training success rate: " + rate);

        int epochs = 5;

        for(int i = 0; i < epochs; i++){
            shuffle(imagesTrain);
            net.train(imagesTrain);
            rate = net.testAccuracy(imagesTest);
            System.out.println("Success rate after round " + (i+1) + ": " + rate);
        }
    }

}
