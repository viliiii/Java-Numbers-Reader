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
     * Reads all images from a given directory and returns them as a list of BufferedImage objects.
     *
     * @param dirPath The path to the directory containing the images.
     * @return A list of BufferedImage objects representing the images in the directory.
     */
    public List<BufferedImage> readDirectory(String dirPath) {
        Path path = Paths.get(dirPath);

        List<BufferedImage> images = new ArrayList<>();

        try(DirectoryStream<Path> imagesStream = Files.newDirectoryStream(path)){
            for(Path imgPath : imagesStream){
                BufferedImage image = ImageIO.read(new File(String.valueOf(imgPath)));
                images.add(image);
            }
        }catch(IOException e){
            System.err.println(e.getMessage());
        }
        return images;
    }

    /**
     * Creates a list of Image images from the given folder.
     * Image label is the last character from the file name.
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
     * Creates a list of Image images from the given folder.
     * Image label is -1 because the labels are unknown.
     * Used for reading extracted digits which are to be classified.
     * @param dirPath the path to the folder containing images
     * @return List of Image objects created of the given folder with images
     */
    public List<Image> readData_unknownLabel(String dirPath){
        Path path = Paths.get(dirPath);

        List<Image> images = new ArrayList<>();

        try(DirectoryStream<Path> imagesStream = Files.newDirectoryStream(path)){
            for(Path imgPath : imagesStream){
                BufferedImage image = ImageIO.read(new File(String.valueOf(imgPath)));

                String imgName = imgPath.getFileName().toString();
                int labelIndex = imgName.lastIndexOf('.') - 1;
                int label = -1;
                double data[][] = readImageMatrix(image);

                images.add(new Image(data, label));
            }
        }catch(IOException e){
            System.err.println(e.getMessage());
        }
        return images;
    }

    /** Creates Image object with label from the last character of the input directory Path.
     * @param dirPath the directory containing all the images of one digit
     * @return List of Image objects created of the given folder with images
     */
    public List<Image> readLabeledDirectory(String dirPath){
        Path path = Paths.get(dirPath);

        List<Image> images = new ArrayList<>();

        int label = Integer.parseInt(String.valueOf(dirPath.charAt(dirPath.length()-1)));

        try(DirectoryStream<Path> imagesStream = Files.newDirectoryStream(path)){
            for(Path imgPath : imagesStream){
                BufferedImage image = ImageIO.read(new File(String.valueOf(imgPath)));

                double[][] data = readImageMatrix(image);

                images.add(new Image(data, label));
            }
        }catch(IOException e){
            System.err.println(e.getMessage());
        }
        return images;
    }

    /** Visits all the subdirectories in the given directory and calls
     * readLabeledDirectory on each of them to make one concatenation of all the
     * pictures into a List of Images.
     * @param dirPath the directory whose digit labeled subdirectories will be visited
     * @return List of Images from every subdirectory together
     */
    public List<Image> readSubDirectories(String dirPath){
        List<Image> allImages = new ArrayList<>();

        // Loop through all subdirectories named 0-9
        for (int i = 0; i <= 9; i++) {
            String subDirPath = dirPath + File.separator + i;
            File subDir = new File(subDirPath);

            if (subDir.isDirectory()) {
                List<Image> images = readLabeledDirectory(subDirPath);
                allImages.addAll(images);
            }
        }
        return allImages;
    }

    /**
     * Creates a double matrix representation of the given image.
     * @param image to be read as a Matrix
     * @return the Matrix of the image
     */
    public double[][] readImageMatrix(BufferedImage image) {
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


    /** Enters every labeled subdirectory in dirFrom, calls ImageProcessor.process
     * for each image in the subdirectory and saves the processed BufferImage into the
     * corresponding labeled subdirectory of dirTo.
     * dirFrom subdirectories and dirTo subdirectories must be called:
     * 0 1 2 3 4 5 6 7 8 9. Each subdirectory for preprocessed and processed pictures
     * of one digit.
     * CALL ONLY IF SOME DATASET HAS BEEN MANUALLY UPDATED TO PROCESS THE NEW IMAGES.
     * @param dirFrom directory containing labeled directories with images to be processed
     * @param dirTo directory containing labeled directories to save the processed images
     */
    public void processDigitsDirectory(String dirFrom, String dirTo){
        // Loop through all subdirectories named 0-9
        for (int i = 0; i <= 9; i++) {
            String subDirFrom = dirFrom + File.separator + i;
            String subDirTo = dirTo + File.separator + i;

            File inputDir = new File(subDirFrom);
            File outputDir = new File(subDirTo);

            // Create the output directory if it does not exist
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }

            // List all image files in the input directory
            File[] imageFiles = inputDir.listFiles((dir, name) -> {
                String lowercaseName = name.toLowerCase();
                return lowercaseName.endsWith(".jpg") || lowercaseName.endsWith(".jpeg") || lowercaseName.endsWith(".png");
            });

            int totalImages = imageFiles.length;
            int counter = 1;
            ImageProcessor imageProcessor = new ImageProcessor();

            if (imageFiles != null) {
                for (File imageFile : imageFiles) {
                    try {
                        // Read the image
                        BufferedImage image = ImageIO.read(imageFile);

                        // Process the image using ImageProcessor
                        BufferedImage processedImage = imageProcessor.processImage(image);

                        // Save the processed image to the output directory
                        File outputImageFile = new File(outputDir, imageFile.getName());
                        ImageIO.write(processedImage, "png", outputImageFile);
                        System.out.println(i + ": " + counter++  + "/" + totalImages);
                    } catch (IOException e) {
                        System.err.println(e.getMessage());
                    }
                }
            }
        }
    }

    /** Processes every image in the dirFrom directory using ImageProcessor, and
     * saves the processed images into the dirTo directory.
     * @param dirFrom directory containing images to process
     * @param dirTo directory to save the processed images into
     */
    public void processImagesDirectory(String dirFrom, String dirTo, boolean scaling){
        File inputDir = new File(dirFrom);
        File outputDir = new File(dirTo);

        // Create the output directory if it does not exist
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        // List all image files in the input directory
        File[] imageFiles = inputDir.listFiles((dir, name) -> {
            String lowercaseName = name.toLowerCase();
            return lowercaseName.endsWith(".jpg") || lowercaseName.endsWith(".jpeg") || lowercaseName.endsWith(".png");
        });

        ImageProcessor imageProcessor = new ImageProcessor();

        if (imageFiles != null) {
            for (File imageFile : imageFiles) {
                try {
                    // Read the image
                    BufferedImage image = ImageIO.read(imageFile);

                    // Process the image using ImageProcessor
                    BufferedImage processedImage;
                    if(scaling){
                        processedImage = imageProcessor.processImage(image);
                    }else{
                        processedImage = imageProcessor.processImage_noScaling(image);
                    }

                    // Save the processed image to the output directory
                    File outputImageFile = new File(outputDir, imageFile.getName());
                    ImageIO.write(processedImage, "png", outputImageFile);
                } catch (IOException e) {
                    System.err.println(e.getMessage());
                }
            }
        }

    }

    /** Processes the input JMBAG image. After this method, the image is ready
     * for extracting digits.
     * @param imageInputPath the JMBAG image to process
     * @param imageOutputPath processed JMBAG image
     */
    public void processDigitsImage(String imageInputPath, String imageOutputPath) {
        try {
            BufferedImage imageInput = ImageIO.read(new File(imageInputPath));
            ImageProcessor processor = new ImageProcessor();
            BufferedImage processedImage = processor.processImage_noScaling(imageInput);

            File outputFile = new File(imageOutputPath);
            ImageIO.write(processedImage, "png", outputFile);
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
}

