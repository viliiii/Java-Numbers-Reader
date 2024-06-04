import data.DataReader;
import data.Image;
import data.ImageProcessor;
import network.NetworkBuilder;
import network.NeuralNetwork;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;

import static java.util.Collections.shuffle;

public class Main {
    /*public static void main(String[] args) {

        String imagePath = "C:\\Faks\\numbers_reader\\pre_processed_images\\WhatsApp5.jpg";


        try {
            BufferedImage image = ImageIO.read(new File(imagePath));

            ImageProcessor imp = new ImageProcessor();
            BufferedImage processedImage = imp.processImage(image);

            System.out.println(ImageIO.write(processedImage, "PNG", new File("C:\\Faks\\numbers_reader\\processed_images\\processed1_5.jpg")));
            printImageMatrix(processedImage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }*/

    public static void main(String[] args) {
        DataReader dataReader = new DataReader();

        /*Call this only when some data set (picture directories) were updated with new pictures to process them.*/
        //dataReader.processDigitDirectory("C:\\Faks\\numbers_reader\\pre_processed_images_test", "C:\\Faks\\numbers_reader\\processed_images_test");

        System.out.println("Starting data loading...");

        List<Image> imagesTrain = dataReader.readSubDirectories("C:\\Faks\\numbers_reader\\processed_images");
        List<Image> imagesTest = dataReader.readSubDirectories("C:\\Faks\\numbers_reader\\processed_images_test");

        /*U images su ti sve slike iz processed_images_* fino labelirane.*/

        System.out.println("Training images set size: " + imagesTrain.size());
        System.out.println("Test images set size: " + imagesTest.size());

        long SEED = 123;
        /*builder.addConvolutionLayer(8, 5, 1, 0.1, SEED);
                builder.addMaxPoolLayer(3, 2);
                builder.addFullyConnectedLayer(10, 0.1, SEED);*/
        NetworkBuilder builder = new NetworkBuilder(80, 45, 256*100);

        builder.addConvolutionLayer(32, 5, 1, 0.001, SEED);
        builder.addMaxPoolLayer(2, 2);
        builder.addConvolutionLayer(64, 3, 1, 0.001, SEED);
        builder.addMaxPoolLayer(2, 2);
        builder.addFullyConnectedLayer(128, 0.001, SEED);
        builder.addFullyConnectedLayer(64, 0.001, SEED);
        builder.addFullyConnectedLayer(10, 0.001, SEED);

        NeuralNetwork network = builder.build();

        double withoutTraining_rate = network.testAccuracy(imagesTest);
        System.out.println("Pre training success rate: " + withoutTraining_rate);

        int epochs = 6;

        for (int i = 0; i < epochs; i++) {
            shuffle(imagesTrain);   //so the same digits are not grouped together, incoming one after another
            network.train(imagesTrain);
            double rate = network.testAccuracy(imagesTest);
            System.out.println("Success after epoch " + i + " is:" + rate);
        }





    }


    public static void printImageMatrix(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRGB(x, y);
                int scale = pixel & 0xff; // extracted blue component. Every component is the same because the picture is in greyscale.
                System.out.print(scale + " ");
            }
            System.out.println();
        }
    }

}
