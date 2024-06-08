import data.DataReader;
import data.DigitsExtractor;
import data.Image;
import data.ImageProcessor;
import network.NetworkBuilder;
import network.NeuralNetwork;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;
import javax.xml.crypto.Data;

import static java.util.Collections.shuffle;

public class SandboxMain {

    public static void main(String[] args) {
        DataReader dataReader = new DataReader();
        /*Call only when pictures are added or removed.*/
        //dataReader.processImagesDirectory("C:\\Faks\\numbers_reader\\pre_processed_jmbag", "C:\\Faks\\numbers_reader\\processed_jmbag", false);

        DigitsExtractor digitsExtractor = new DigitsExtractor(150, 600);

        digitsExtractor.extractDigitsFromImageFile("C:\\Faks\\numbers_reader\\processed_jmbag\\tara2.jpg", "C:\\Faks\\numbers_reader\\processed_jmbag\\extracted_digits");

    }
}
