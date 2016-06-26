
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * This is a startup project for imageJ plug-in. It is based on NetBeans IDE
 * 8.1. It uses Apache Maven to build. Make sure the internet connection is
 * working while build the project.
 *
 * @author Luke Chang, 25-02-2016
 *
 */
public class Unimodal_Thresholding_ implements PlugInFilter{

    public static final int PORE_VALUE = 255;

    // image properties
    private ImagePlus image;
    private Rectangle rec;  // contains width and height for the original image
    private int numBins;    // should be 256
    private int numPixels;  // width * height

    @Override
    public int setup(String string, ImagePlus ip){
        this.image = ip;
        IJ.hideProcessStackDialog = true;
        // add DOES_16, if you want process 16bit greyscale image.
        return DOES_8G + SUPPORTS_MASKING + NO_UNDO;
    }

    @Override
    public void run(ImageProcessor ip){
        // the properties should same in entire stack
        int bitDepth = ip.getBitDepth();
        numBins = (int)Math.pow(2, bitDepth);
        rec = ip.getRoi();
        numPixels = rec.height * rec.width;
        // process each slice
        IJ.showStatus("2D kriging is running...");
        for(int i = 1; i <= image.getStackSize(); i++){
            IJ.showProgress(i, image.getStackSize());
            process(image.getStack().getProcessor(i));
            // update results
            image.updateAndDraw();
        }
    }

    /**
     * Put your filter here
     *
     * @param ip
     */
    private void process(ImageProcessor ip){
        FloatProcessor fp = null;
        fp = ip.toFloat(0, fp);
        // Get original image
        float[] pixels = (float[])fp.getPixels();
        double[] h = Util.histogram(pixels, numBins, Boolean.TRUE);
        RosinUnimodalThreshold unimodal = new RosinUnimodalThreshold(h, false);
        unimodal.solve();
        int threshold = unimodal.threshold0;
        System.out.println("Threshold(Unimodal) = " + threshold);
        IJ.log("Threshold(Unimodal) = " + threshold);
        // compute T0, T1

        int[] segmentImg = new int[numPixels];
        for(int i = 0; i < numPixels; i++){
            if(pixels[i] < threshold){
                segmentImg[i] = Unimodal_Thresholding_.PORE_VALUE;
            }
        }
        double porosity = computePorosity(segmentImg);
        System.out.println("porosity = " + porosity);
//        IJ.log("porosity = " + porosity);
        updateImage(ip, segmentImg);

    }

    public void updateImage(ImageProcessor ip, int[] segmented){
        for(int y = 0; y < rec.height; y++){
            for(int x = 0; x < rec.width; x++){
                ip.putPixel(x, y, segmented[y * rec.width + x]);
            }
        }
    }

    public static double computePorosity(int[] segmented){
        double sum = segmented.length;
        double count = 0;

        for(int i = 0; i < sum; i++){
            if(segmented[i] == Unimodal_Thresholding_.PORE_VALUE){
                count++;
            }
        }
        double porosity = count / sum;
        return porosity;
    }

    /**
     * Main method for IDE to run. Not required for imageJ to compile. Do not
     * alter.
     *
     * @param args
     */
    public static void main(String[] args){
        // set the plugins.dir property to make the plugin appear in the Plugins menu
        Class<?> clazz = Unimodal_Thresholding_.class;
        String url = clazz.getResource("/" + clazz.getName().replace('.', '/') + ".class").toString();
        String pluginsDir = url.substring(5, url.length() - clazz.getName().length() - 6);
        System.setProperty("plugins.dir", pluginsDir);

        // start ImageJ
        new ImageJ();
    }
}
