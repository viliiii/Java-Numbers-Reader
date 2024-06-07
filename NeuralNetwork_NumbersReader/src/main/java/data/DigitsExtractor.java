package data;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class DigitsExtractor {

    private final int MIN_BOX_SIZE;
    private final int MAX_BOX_SIZE;

    public DigitsExtractor(int MIN_BOX_SIZE, int MAX_BOX_SIZE) {
        this.MIN_BOX_SIZE = MIN_BOX_SIZE;
        this.MAX_BOX_SIZE = MAX_BOX_SIZE;
    }

    public void extractDigitsFromImageFile(String pathBinarizedImageIn, String pathDirOut){

        try {
            File inputFile = new File(pathBinarizedImageIn);
            BufferedImage image = ImageIO.read(inputFile);

            List<BufferedImage> digitImages = extractDigits(image);
            System.out.println(digitImages.size());
            File outputDir = new File(pathDirOut);
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }

            String inputFileName = inputFile.getName();
            String baseName = inputFileName.substring(0, inputFileName.lastIndexOf('.'));
            String extension = "png";  // Change extension to jpg

            for (int i = 0; i < digitImages.size(); i++) {
                BufferedImage img = digitImages.get(i);
                String outputFileName = String.format("EXTRACTED_%s_%d.%s", baseName, i, extension);
                File outputFile = new File(outputDir, outputFileName);
                ImageIO.write(img, extension, outputFile);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Extracts individual digits from a given image.
     *
     * @param image The image from which to extract digits.
     * @return A list of BufferedImage objects, each representing an individual digit.
     */
    public List<BufferedImage> extractDigits(BufferedImage image) {
        List<int[]> boundingBoxes = getBoundingBoxes(image);
        List<BufferedImage> digitImages = new ArrayList<>();
        for (int[] box : boundingBoxes) {
            int x = box[0];
            int y = box[1];
            int width = box[2] - box[0];
            int height = box[3] - box[1];
            if (width > 0 && height > 0) {
                BufferedImage digitImage = image.getSubimage(x, y, width, height);
                digitImages.add(digitImage);
            }
        }

        return digitImages;
    }


    /**
     * Extracts the bounding boxes of black pixels in a binary image.
     *
     * @param binaryImage The binary image to extract bounding boxes from.
     * @return A list of bounding boxes, where each box is represented as an array of four integers: [x-start, y-start, x-end, y-end].
     */
    private List<int[]> getBoundingBoxes(BufferedImage binaryImage) {
        List<int[]> boundingBoxes = new ArrayList<>();

        boolean[][] visited = new boolean[binaryImage.getHeight()][binaryImage.getWidth()];

        for(int y=0; y<binaryImage.getHeight(); y++) {
            for(int x=0; x<binaryImage.getWidth(); x++) {
                // If the pixel is not visited and is black
                if(!visited[y][x] && (binaryImage.getRGB(x, y) & 0xFF) == 0) {
                    // Initialize a new bounding box with the current pixel's coordinates
                    int[] box = new int[] {x, y, x, y};

                    // Find the bounding box for the current black region
                    findBoundingBoxStack(binaryImage, visited, x, y, box);

                    // Check if the bounding box is larger than the minimum required size
                    if ((box[2] - box[0]) >= MIN_BOX_SIZE && (box[3] - box[1]) >= MIN_BOX_SIZE
                        && (box[2] - box[0]) <= MAX_BOX_SIZE && (box[3] - box[1]) <= MAX_BOX_SIZE) {
                        boundingBoxes.add(box);
                    }
                }
            }
        }

        // Return the list of bounding boxes
        return boundingBoxes;
    }

    /** Iteratively finds the bounds of the black pixels surrounding box around the given x, y coordinates.
     * @param binaryImage the image to search the box around the given x, y coordinates
     * @param visited helper array that contains info about the already visited pixels
     * @param x starting x position
     * @param y starting y position
     * @param box [x-start, y-start, x-end, y-end] representation of the bounding box, rectangle.
     */
    private void findBoundingBox(BufferedImage binaryImage, boolean[][] visited, int x, int y, int[] box) {
        /*If reached a white pixel or went out of the picture, the expanding of the bounding box is over.*/
        if(x < 0 || y < 0 || x >= binaryImage.getWidth() || y >= binaryImage.getHeight() || visited[y][x] || (binaryImage.getRGB(x, y) & 0xFF) != 0) {
            return;
        }

        visited[y][x] = true;

        box[0] = Math.min(box[0], x);
        box[1] = Math.min(box[1], y);
        box[2] = Math.max(box[2], x);
        box[3] = Math.max(box[3], y);

        findBoundingBox(binaryImage, visited, x - 1, y, box);
        findBoundingBox(binaryImage, visited, x + 1, y, box);
        findBoundingBox(binaryImage, visited, x, y - 1, box);
        findBoundingBox(binaryImage, visited, x, y + 1, box);
    }


    /** Iteratively finds the bounds of the black pixels surrounding box around the given x, y coordinates.
     * Using Stack to prevent the StackOverflowException from being thrown because of the large images.
     * @param binaryImage the image to search the box around the given x, y coordinates
     * @param visited helper array that contains info about the already visited pixels
     * @param startX starting x position
     * @param startY starting y position
     * @param box [x-start, y-start, x-end, y-end] representation of the bounding box, rectangle.
     */
    private  void findBoundingBoxStack(BufferedImage binaryImage, boolean[][] visited, int startX, int startY, int[] box) {
        int width = binaryImage.getWidth();
        int height = binaryImage.getHeight();

        Stack<int[]> stack = new Stack<>();
        stack.push(new int[]{startX, startY});

        while (!stack.isEmpty()) {
            int[] pos = stack.pop();
            int x = pos[0];
            int y = pos[1];

            if (x < 0 || y < 0 || x >= width || y >= height || visited[y][x] || (binaryImage.getRGB(x, y) & 0xFF) != 0) {
                continue;
            }

            visited[y][x] = true;

            // Update the bounding box
            box[0] = Math.min(box[0], x);
            box[1] = Math.min(box[1], y);
            box[2] = Math.max(box[2], x);
            box[3] = Math.max(box[3], y);

            // Push neighboring pixels onto the stack
            stack.push(new int[]{x - 1, y});
            stack.push(new int[]{x + 1, y});
            stack.push(new int[]{x, y - 1});
            stack.push(new int[]{x, y + 1});
        }
    }
}
