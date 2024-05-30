package data;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Class used for reading a training or testing data set (folder of pictures)
 * into an Image classes that are convenient for input in the NN.
 */
public class DataReader {

    private final int rows = 80;
    private final int columns = 45;

    /**
     * Creates a list of Image images from the given folder.
     * @param dirPath the path to the folder containing images
     * @return List of Image objects created of the given folder with images
     */
    public List<Image> readData(String dirPath){
        Path path = Paths.get(dirPath);

        List<Image> images = new ArrayList<>();

        try(DirectoryStream<Path> imagesStream = Files.newDirectoryStream(path)){
            for(Path imgPath : imagesStream){
                BufferedImage image = ImageIO.read(new File(String.valueOf(imgPath)));

                String imgName = imgPath.getFileName().toString();
                int labelIndex = imgName.lastIndexOf('.') - 1;
                int label;
                if(labelIndex > 0){
                    label = Integer.parseInt(String.valueOf(imgName.charAt(labelIndex)));
                }else{
                    label = Integer.parseInt(String.valueOf(imgName.charAt(imgName.length()-1)));
                }
                double data[][] = readImageMatrix(image);

                images.add(new Image(data, label));
            }
        }catch(IOException e){
            System.err.println(e.getMessage());
        }
        return images;
    }

    /**
     * Creates a float matrix representation of the given image.
     * @param image to be read as a Matrix
     * @return the Matrix of the image
     */
    private double[][] readImageMatrix(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        double[][] imageMatrix = new double[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRGB(x, y);
                int scale = pixel & 0xff; // extracted blue component. Every component is the same because the picture is in greyscale.
                imageMatrix[y][x] = (double) scale;
            }
        }

        return imageMatrix;
    }
}
