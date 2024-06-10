import data.DataReader;
import data.DigitsExtractor;
import data.Image;
import data.ImageProcessor;
import network.NeuralNetwork;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class NumbersReader {

    private NeuralNetwork model;

    /*Trebam metodu koja prima String putanju do jmbag slike i vraća String brojeva koji su na slici. */

    /*public NumbersReader(NeuralNetwork model) {
        this.model = model;
    }*/

    public String readNumbers(String inputImagePath, String nnPath) {


        String processedImagePath = "processed_jmbag/"+ "processed_" + getImageName(inputImagePath);
        DataReader dataReader = new DataReader();
        dataReader.processDigitsImage(inputImagePath, processedImagePath);


        /*Those box sizes work great with pictures from iPhone 12, 13, 14, 15.
        * If the original picture resolution is significantly smaller than 2K,
        * adjust box sizes by lowering them.*/
        DigitsExtractor digitsExtractor = new DigitsExtractor(20, 600);
        deleteAllFilesInDirectory("processed_jmbag/extracted_digits");
        digitsExtractor.extractDigitsFromImageFile(processedImagePath, "processed_jmbag/extracted_digits");

        ImageProcessor processor = new ImageProcessor();
        List<BufferedImage> extractedDigitsList = dataReader.readDirectory("processed_jmbag/extracted_digits");
        List<BufferedImage> scaled = new ArrayList<>();
        extractedDigitsList.forEach(image -> {scaled.add(processor.scale(image));});

        deleteAllFilesInDirectory("processed_jmbag/extracted_digits_scaled");
        for(int i=0; i<scaled.size(); i++){
            try {
                ImageIO.write(scaled.get(i), "png", new File("processed_jmbag/extracted_digits_scaled/" + i + ".png"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        List<Image> digits = dataReader.readData_unknownLabel("processed_jmbag/extracted_digits_scaled");


        NeuralNetwork network = loadNN(nnPath);
        StringBuilder output = new StringBuilder();
        for (var digit: digits) {
            System.out.println(digit);
            output.append(network.guess(digit));
        }

        return output.toString();
    }

    public int guessDigitFromFile(String inputImagePath, String nnPath) {
        DataReader dataReader = new DataReader();
        Image digitImage = dataReader.readImageFromFile(inputImagePath);
        NeuralNetwork network = loadNN(nnPath);

        return network.guess(digitImage);
    }


    public static String getImageName(String inputImagePath) {
        Path path = Paths.get(inputImagePath);
        return path.getFileName().toString();
    }

    public static void deleteAllFilesInDirectory(String directoryPath) {
        File directory = new File(directoryPath);

        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        boolean deleted = file.delete();
                        if (!deleted) {
                            System.err.println("Failed to delete file: " + file.getAbsolutePath());
                        }
                    }
                }
            } else {
                System.err.println("Failed to list files in directory: " + directoryPath);
            }
        } else {
            System.err.println("Directory does not exist or is not a directory: " + directoryPath);
        }
    }


    private NeuralNetwork loadNN(String pathToNN) {
        NeuralNetwork neuralNetwork;
        try {
            FileInputStream fis = new FileInputStream(pathToNN);
            ObjectInputStream ois = new ObjectInputStream(fis);
            neuralNetwork = (NeuralNetwork) ois.readObject();
            ois.close();
            fis.close();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        return neuralNetwork;
    }


    public static void main(String[] args) {
        NumbersReader reader = new NumbersReader();

        String jmbag = reader.readNumbers("C:\\Faks\\numbers_reader\\pre_processed_jmbag\\tara4.jpg", "networks/nn_2024-06-08_23-25-32.ser");
        System.out.println(jmbag);

        //System.out.println(reader.guessDigitFromFile("C:\\Faks\\numbers_reader\\NeuralNetwork_NumbersReader\\processed_jmbag\\extracted_digits_scaled\\0.png", "networks/nn_2024-06-08_23-25-32.ser"));
    }
}
