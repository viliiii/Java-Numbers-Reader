import data.DataReader;
import data.DataReaderMNIST;
import data.Image;
import network.NetworkBuilder;
import network.NeuralNetwork;

import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static java.util.Collections.shuffle;

public class TrainMain {
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
        //dataReader.processDigitsDirectory("C:\\Faks\\numbers_reader\\pre_processed_images_test", "C:\\Faks\\numbers_reader\\processed_images_test");

        //OVO DOLJE JE MAIN ZA TRENIRANJE I TESTIRANJE
        System.out.println("Starting data loading...");

        DataReaderMNIST dataReaderMNIST = new DataReaderMNIST();
        DataReader dataReaderImages = new DataReader();

        //List<Image> imagesTrain = dataReader.readSubDirectories("C:\\Faks\\numbers_reader\\processed_images");
        List<Image> imagesTrain = dataReaderMNIST.readData("C:\\Faks\\numbers_reader\\MNIST_data\\mnist_train.csv");
        List<Image> imagesTest = dataReader.readSubDirectories("C:\\Faks\\numbers_reader\\processed_images_test");

        imagesTrain.forEach(Image::binarizeSimple);
        //imagesTest.forEach(Image::normalize);

        System.out.println("Training images set size: " + imagesTrain.size());
        System.out.println("Test images set size: " + imagesTest.size());

        for(int i = 0; i < 5000; i=i+500){
            System.out.println(imagesTrain.get(i).toString());
        }
        long SEED = 123;
        NetworkBuilder builder = new NetworkBuilder(28, 28, 256*100);   //probaj manji scaling

        builder.addConvolutionLayer(8, 5, 1, 0.1, SEED);
        builder.addMaxPoolLayer(3, 2);
        builder.addFullyConnectedLayer(10, 0.1, SEED, false);

        /*builder.addConvolutionLayer(32, 3, 1, 0.001, SEED);
        builder.addMaxPoolLayer(3, 2);
        builder.addConvolutionLayer(32, 3, 1, 0.001, SEED);
        builder.addMaxPoolLayer(10, 1);
        builder.addFullyConnectedLayer(10, 0.001, SEED, true);*/


        NeuralNetwork network = builder.build();


        double withoutTraining_rate = network.testAccuracy(imagesTest);
        System.out.println("Pre training success rate: " + withoutTraining_rate);

        int epochs = 8;

        for (int i = 0; i < epochs; i++) {
            shuffle(imagesTrain);   //so the same digits are not grouped together, incoming one after another
            network.train(imagesTrain);
            double rate = network.testAccuracy(imagesTest);
            System.out.println("Success after epoch " + (i+1) + " is:" + rate);
        }

        saveNeuralNetwork(network);



    }

    private static void saveNeuralNetwork(NeuralNetwork neuralNetwork) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
            String formattedDateTime = LocalDateTime.now().format(formatter);
            String fileName = "networks/nn_" + formattedDateTime + ".ser";
            FileOutputStream fos = new FileOutputStream(fileName);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(neuralNetwork);
            oos.close();
            fos.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());;
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
