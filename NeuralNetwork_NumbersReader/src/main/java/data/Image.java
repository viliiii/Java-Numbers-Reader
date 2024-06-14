package data;

public class Image {

    private final double[][] data;
    private final int label;

    public Image(double[][] data, int label) {
        this.data = data;
        this.label = label;
    }

    public double[][] getData() {
        return data;
    }

    public int getLabel() {
        return label;
    }

    @Override
    public String toString() {
        String s = label + ":\n";
        for(int r=0; r< data.length; r++) {
            for(int c=0; c< data[0].length; c++) {
                s+=data[r][c]+" ";
            }
            s+="\n";
        }

        return s;
    }

    public void normalize() {
        for(int r = 0; r < data.length; r++) {
            for(int c = 0; c < data[0].length; c++) {
                if(data[r][c] == 0) {
                    data[r][c] = -1;
                } else {
                    data[r][c] = 1;
                }
            }
        }
    }

    public void binarizeSimple(){
        for(int r = 0; r < data.length; r++) {
            for(int c = 0; c < data[0].length; c++) {
                if(data[r][c] > 100) {
                    data[r][c] = 255.0;
                } else {
                    data[r][c] = 0.0;
                }
            }
        }
    }
}
