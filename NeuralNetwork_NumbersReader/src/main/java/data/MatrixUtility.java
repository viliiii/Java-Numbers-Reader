package data;

public class MatrixUtility {

    public static double[][] add(double[][] a, double[][] b) {

        double[][] result = new double[a.length][b.length];

        for(int i = 0; i < a.length; i++){
            for(int j = 0; j < a[0].length; j++){
                result[i][j] = a[i][j] + b[i][j];
            }
        }

        return result;
    }

    public static double[] add(double[] a, double[] b) {

        double[] result = new double[a.length];

        for(int i = 0; i < a.length; i++) result[i] = a[i] + b[i];

        return result;
    }


    public static double[][] multiply(double[][] a, double scalar) {

        double[][] result = new double[a.length][a[0].length];

        for(int i = 0; i < a.length; i++){
            for(int j = 0; j < a[0].length; j++){
                result[i][j] = a[i][j] * scalar;
            }
        }

        return result;
    }

    public static double[] multiply(double[] a, double scalar) {

        double[] result = new double[a.length];

        for(int i = 0; i < a.length; i++) result[i] = a[i] * scalar;

        return result;
    }

    public static double[][] flipHorizontal(double[][] m){
        int rows = m.length;
        int cols = m[0].length;

        double[][] result = new double[rows][cols];

        for(int i = 0; i < rows; i++) {
            for(int j = 0; j < cols; j++) {
                result[rows - 1 - i][j] = m[i][j];
            }
        }

        return result;
    }

    public static double[][] flipVertical(double[][] m){
        int rows = m.length;
        int cols = m[0].length;

        double[][] result = new double[rows][cols];

        for(int i = 0; i < rows; i++) {
            for(int j = 0; j < cols; j++) {
                result[i][cols - 1 -j] = m[i][j];
            }
        }

        return result;
    }


    public static int getMaxIndex(double[] v){
        double max = 0;
        int ind = 0;

        for(int i = 0; i < v.length; i++){
            if(v[i] >= max){
                max = v[i];
                ind = i;
            }
        }

        return ind;
    }



}
