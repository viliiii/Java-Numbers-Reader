package data;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

/**
 * Class with set of methods used for simplifying images. Each method
 * represents one "filter" for the image.
 */
public class ImageProcessor {


    /**
     * Filters the given image using all the methods in this class.
     * @param image to be filtered
     * @return the filtered BufferedImage image
     */
    public BufferedImage processImage(BufferedImage image){
        BufferedImage scaledImage = scale(image);

        BufferedImage greyScaledImage = greyscale(scaledImage);

        int threshold = otsuTreshold(greyScaledImage);

        BufferedImage binarizedImage = binarize(greyScaledImage, threshold);

        return binarizedImage;
    }

    /**
     * Scales the image to 80:45 pixels.
     * Scales from whatever 16:9 format size
     * to height of 80 and width of 45 pixels which is also a 16:9 format.
     * @param image BufferedImage to be scaled.
     * @return scaled image in BufferedImage object.
     */
    public BufferedImage scale(BufferedImage image){
        try{

            int width = image.getWidth();
            int height = image.getHeight();

            BufferedImage scaledImage = new BufferedImage(28, 28, BufferedImage.TYPE_INT_ARGB);

            AffineTransform affineTransform = AffineTransform.getScaleInstance(28.0/width, 28.0/height);
            AffineTransformOp affineTransformOp = new AffineTransformOp(affineTransform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);

            scaledImage = affineTransformOp.filter(image, scaledImage);
            int swidth = scaledImage.getWidth();
            int swheight = scaledImage.getHeight();

            //System.out.println("width: " + width + ", height: " + height);
            //System.out.println("swidth: " + swidth + ", swheight: " + swheight);

            return scaledImage;

        } catch (Exception e) {
            System.err.println("Error scaling: " + image);
            System.err.println(e.getMessage());
        }

        return null;
    }

    /**
     * Converts the given image into a greyscale representation of that image.
     * Greyscale value for each pixel is the average value of red, green, and blue. Alpha
     * value is the same.
     * @param image to be greyscaled
     * @return greyscaled BufferedImage
     */
    public BufferedImage greyscale(BufferedImage image){

        int width = image.getWidth();
        int height = image.getHeight();

        int[] pixels = image.getRGB(0, 0, width, height, null, 0, width);

        for(int i = 0; i < pixels.length; i++){

            int p = pixels[i];

            int a = (p >> 24) & 0xFF;   //transparency is the highest 8 bits

            int r = (p >> 16) & 0xFF;   //red is the next 8 bits [0, 255]
            int g = (p >> 8) & 0xFF;
            int b = p & 0xFF;

            int avg = (r + g + b) / 3;
            //int avg = (int)(0.299*r + 0.587*g + 0.144*b) / 3; //alternative for greyscale value
            avg = 255 - avg;
            p = (a << 24) | (avg << 16) | (avg << 8) | avg; //making a new pixel with same transparency
                                                            // but every color has the same value so the pixel is in the notes of grey
            pixels[i] = p;
        }

        image.setRGB(0, 0, width, height, pixels, 0, width);

        return image;
    }


/*    private int adjustContrastRigid(int value) {
        double scaleFactor = 1.5;

        if (value > 200) {
            // Pikseli koji su bili svetliji od 180 postaju beli (255)
            return 255;
        } else if (value < 110) {
            // Pikseli koji su bili tamniji od 70 postaju crni (0)
            return 0;
        } else {
            // Pikseli između 70 i 180 se skaliraju sa faktorom kontrasta
            int newValue = (int) ((value - 128) * scaleFactor + 128);

            // Ograničavamo vrednosti na opseg [0, 255]
            return Math.max(0, Math.min(255, newValue));
        }
    }*/

/*    private int adjustContrastRigidMine(int value) {
        double scaleFactor = 1.5;

        int newValue = (int) ((value - 128) * scaleFactor + 128);

        if (newValue > 190) {
            return 255;
        } else if (newValue < 160) {
            return 0;
        } else {


            return Math.max(0, Math.min(255, newValue));
        }
    }*/


    /**
     * Calculates the 'Otsu threshold' value for the given greyscaled image.
     * The 'Otsu threshold' is the value above which the pixels should be considered white, and above which
     * should be considered black.
     * <a href="https://www.youtube.com/watch?v=jUUkMaNuHP8">...</a>
     * @param image the image to calculate the value on
     * @return calculated Otsu threshold value
     */
    public int otsuTreshold(BufferedImage image) {
        int[] histogram = new int[256]; //histogram of greyscale values
        int width = image.getWidth();
        int height = image.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRGB(x, y);
                int grey = pixel & 0xff;
                histogram[grey]++;
            }
        }

        int total = width * height;
        float sum = 0;

        for(int i=0; i<256; i++) sum += i*histogram[i];

        float sumB = 0; //background greyscale sum
        int wB = 0;     //weight background
        int wF = 0;     //weight foreground
        float varianceMax = 0;
        int threshold = 0;

        for(int t=0; t<256; t++) {      //try to maximize variance for every possible threshold in the picture
            wB += histogram[t];
            if(wB == 0) continue;

            wF = total - wB;
            if(wF == 0) continue;

            sumB += (float) (t * histogram[t]);
            float mB = sumB / wB;       //niB from the linked video, mean background
            float mF = (sum - sumB) / wF;   //niF from the linked video, mean foreground

            float variance = (float) wB * (float) wF * (mB-mF)*(mB-mF);

            if(variance > varianceMax) {
                varianceMax = variance;
                threshold = t;
            }
        }

        return threshold;
    }


    /**
     * Binarizes the image around the given threshold. Pixels above the threshold will be
     * white (255), and pixels below the threshold will be black (0).
     * @param image to be binarized
     * @param threshold for determining
     * @return the binarized BufferImage image
     */
    public BufferedImage binarize(BufferedImage image, int threshold) {
        int width = image.getWidth();
        int height = image.getHeight();

        BufferedImage binaryImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRGB(x, y);
                int gray = pixel & 0xFF;
                int a = pixel >> 24 & 0xFF;

                int binary = gray > threshold ? 255 : 0;
                int newPixel = a << 24 | binary << 16 | binary << 8 | binary;
                binaryImage.setRGB(x, y, newPixel);
            }
        }

        return binaryImage;
    }

}
