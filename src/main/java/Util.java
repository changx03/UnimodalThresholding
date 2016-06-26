
import ij.IJ;
import ij.ImagePlus;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Luke Chang
 */
public class Util{

    public static double[] histogram(float[] image, int numBins, Boolean ignoreMinMax){
        double[] histo = new double[numBins];
        for(int i = 0; i < image.length; i++){
            int idx = (int)image[i];
            // ignore salt and pepper noise (No 0 and 255)
            if(!ignoreMinMax || (ignoreMinMax && idx != 0 && idx != 255)){
                histo[idx]++;
            }
        }
        return histo;
    }

    public static double[] probabilityHistogram(double[] histogram){
        int numBin = histogram.length;
        double[] outHist = new double[numBin];
        double sum = 0.0;
        for(int i = 0; i < numBin; i++){
            sum += histogram[i];
        }
        for(int i = 0; i < numBin; i++){
            outHist[i] = histogram[i] / sum;
        }
        return outHist;
    }

    public static double[] cumulativeHistogram(double[] histogram){
        int numBin = histogram.length;
        double[] outHist = new double[numBin];
        outHist[0] = histogram[0];
        for(int i = 1; i < numBin; i++){
            outHist[i] = outHist[i - 1] + histogram[i];
        }
        return outHist;
    }

    /**
     *
     * @param histogram
     * @param min include minimum
     * @param max not include maximum
     * @return
     */
    public static double solveMean(double[] histogram, int min, int max){
        double mu = 0;
        double sum = 0;
        for(int i = min; i <= max; i++){
            mu += histogram[i] * (double)i;
            sum += histogram[i];
        }
        mu /= sum;
        return mu;
    }

    /**
     *
     * @param histogram
     * @param min include minimum
     * @param max not include maximum
     * @param mu mean
     * @return
     */
    public static double solveStdDev(double[] histogram, int min, int max, double mu){
        double sum = 0;
        double sigma2 = 0;
        for(int i = min; i <= max; i++){
            sigma2 += histogram[i] * sqr((double)i - mu);
            sum += histogram[i];
        }
        return Math.sqrt(sigma2 / sum);
    }

    public static double sqr(double value){
        return value * value;
    }

    public static double cube(double value){
        return value * value * value;
    }

    public static double euclideanDistance(Point a, Point b){
        return Math.sqrt((double)((a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y)));
    }

    public static double euclideanDistance3D(Voxel vc, Voxel vp){
        return Math.sqrt(Util.sqr(vc.z - vp.z) + Util.sqr(vc.y - vp.y) + Util.sqr(vc.x - vp.x));
    }

    /**
     * Draw 8-bit image based on input array
     *
     * @param title the image title which shows on the new window
     * @param pixels the image data
     * @param rec it provides the width and height of the image
     */
    public static void createImage(String title, int[] pixels, Rectangle rec){
        ImageProcessor imgPro = new ByteProcessor(rec.width, rec.height);
        ImagePlus thresholdImp = new ImagePlus(title, imgPro);
        for(int y = 0; y < rec.height; y++){
            for(int x = 0; x < rec.width; x++){
                imgPro.putPixel(x, y, pixels[y * rec.width + x]);
            }
        }
        thresholdImp.show();
        IJ.selectWindow(title);
    }

    public static void saveTxtFile(String fileName, String content){
        try{
            File file = new File(fileName + ".txt");
            FileWriter fw = new FileWriter(file);
            fw.write(content + "\n");
            fw.flush();
            fw.close();
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    public static double findMax(double[] array){
        double max = array[0];
        for(double i : array){
            if(i > max){
                max = i;
            }
        }
        return max;
    }

    public static BufferedImage getGreysacleImageFromIntArray(int[] pixels, int width, int height){
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        int pixel, rgb;
        for(int y = 0; y < height; y++){
            for(int x = 0; x < width; x++){
                pixel = pixels[y * width + x];
                rgb = (pixel << 16) | (pixel << 8) | pixel;
                image.setRGB(x, y, rgb);
            }
        }
        return image;
    }

    public static void saveImage(String imgTitle, String dir, BufferedImage img){
        try{
            String fullPath = dir + imgTitle + ".png";
            System.out.println(fullPath);
            ImageIO.write(img, "png", new File(fullPath));
        } catch(IOException e){
            System.err.printf(e.toString());
        }
    }
    
    public static String buildDataToStr(ArrayList data){
        StringBuilder sb = new StringBuilder();
        for(Object d : data){
            sb.append(d).append(",");
        }
        return sb.toString();
    }
    
    public static void save2dKrigingDataToCsv(String data, String dir, String title) {
        String titleContent = "t0,t1,p_uncertain,p_pore";
        BufferedWriter bw;
        try {
            File file = new File(dir + title + ".csv");
            if (!file.exists()) {
                file.createNewFile();
                bw = new BufferedWriter(new FileWriter(file));
                bw.write(titleContent);
                bw.newLine();
                bw.flush();
                bw.close();
            }
            bw = new BufferedWriter(new FileWriter(file, true));
            bw.write(data);
            bw.newLine();
            bw.flush();
            bw.close();
        } catch (IOException e) {
            System.err.println(e.toString());
        }
    }
}
