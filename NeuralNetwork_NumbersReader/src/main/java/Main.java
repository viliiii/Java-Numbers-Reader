import data.DataReader;
import data.Image;
import data.ImageProcessor;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;

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
        List<Image> images = new DataReader().readData("C:\\Faks\\numbers_reader\\processed_images");

        System.out.println(images.get(0));
        System.out.println(images.get(1));
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
