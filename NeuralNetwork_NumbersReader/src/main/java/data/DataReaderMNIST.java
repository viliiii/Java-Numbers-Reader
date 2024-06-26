package data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class DataReaderMNIST {

    private final int rows = 28;
    private final int cols = 28;

    public List<Image> readData(String path){

        List<Image> images = new ArrayList<>();

        try (BufferedReader dataReader = new BufferedReader(new FileReader(path))){

            String line;

            while((line = dataReader.readLine()) != null){
                String[] lineItems = line.split(",");
                /*this was added because some train.csv lines were somehow wrong...*/
                if(lineItems.length < 785) continue;
                double[][] data = new double[rows][cols];
                int label = Integer.parseInt(lineItems[0]);

                int i = 1;

                for(int row = 0; row < rows; row++){
                    for(int col = 0; col < cols; col++){
                        data[row][col] = (double) Integer.parseInt(lineItems[i]);
                        i++;
                    }
                }

                images.add(new Image(data, label));

            }

        } catch (Exception e){
            e.printStackTrace();
            throw new IllegalArgumentException("File not found " + path);
        }

        return images;

    }

}