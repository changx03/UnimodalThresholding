
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author xcha011
 */
public class RosinUnimodalThreshold{

    private final int numBin;
    private double[] hist;
    private double peakIdx;
    private double peakVal;
    private double minIdx;
    private double maxIdx;
    public int threshold0;

    public RosinUnimodalThreshold(double[] histogram, Boolean isSmoothHist){
        this.hist = histogram;
        this.numBin = histogram.length;
        this.threshold0 = 0;

        // get peak and value
        peakVal = hist[0] = hist[this.numBin - 1] = 0;
        peakIdx = 0;
        for(int i = 1; i < this.numBin; i++){
            if(hist[i] != 0){
                minIdx = i;
                break;
            }
        }
        for(int i = this.numBin - 1; i >= 0; i--){
            if(hist[i] != 0){
                maxIdx = i;
                break;
            }
        }

        double range = maxIdx - minIdx;
        for(int i = (int)minIdx + (int)Math.round(range * 0.1); i < (int)maxIdx; i++){
            if(peakVal <= hist[i]){
                peakIdx = i;
                peakVal = hist[i];
            }
        }

        if(isSmoothHist){
            smoothHistogram();
        }
    }

    public void solve(){
        double minVal = hist[(int)minIdx];
        double d = euclideanDistant(peakIdx, peakVal, minIdx, minVal);
        double triL = peakIdx - minIdx;
        double triH = peakVal - minVal;

        int threshold = (int)minIdx;
        double h = 0;
        if(d != 0){
            for(int i = (int)minIdx; i < peakIdx; i++){
                if(hist[i] != 0.0){
                    double tempH = Math.abs(triL * (minVal - hist[i]) + triH * ((double)i - minIdx)) / d;
                    if(tempH >= h){
                        threshold = i;
                        h = tempH;
                    }
                }
            }
        }
        threshold0 = threshold;
    }

    private double euclideanDistant(double y0, double x0, double y1, double x1){
        return Math.sqrt((y0 - y1) * (y0 - y1) + (x0 - x1) * (x0 - x1));
    }

    /**
     * Linear estimation with smooth
     */
    private void smoothHistogram(){
        double[] tempH = this.hist.clone();
        // Step 1: remove outliers
        double minVal = Math.min(Math.min(hist[(int)minIdx], hist[(int)minIdx + 1]), hist[(int)minIdx + 2]);
        double range = Math.abs(peakIdx - minIdx);
        double step = (peakVal - minVal) / range;
        double modY, empY;
        for(int i = 0; i <= range; i++){
            modY = minVal + i * step;
            empY = hist[(int)minIdx + i];
            if(empY > (modY * 1.1)){
                tempH[(int)minIdx + i] = modY;
            }
        }
        double maxVal = Math.min(Math.min(hist[(int)maxIdx], hist[(int)maxIdx - 1]), hist[(int)maxIdx - 2]);
        range = Math.abs(maxIdx - peakIdx);
        step = (peakVal - maxVal) / range;
        for(int i = 0; i <= range; i++){
            modY = peakVal + i * step;
            empY = hist[(int)peakIdx + i];
            if(i == range){
                modY = maxVal;
            }
            if(empY > (modY * 1.1)){
                tempH[(int)peakIdx + i] = modY;
            }
        }
        // Step 2 fill empty bins
        double countN, countP, count;
        double valL, valR;
        double est;
        for(int i = ((int)minIdx + 1); i < maxIdx; i++){
            if(hist[i] != 0){
                continue;
            }
            valL = valR = countN = countP = 0;
            for(int j = (i - 1); minIdx <= j; j--){
                countN++;
                if(tempH[j] != 0){
                    valL = tempH[j];
                    break;
                }
            }
            for(int j = (i + 1); j <= maxIdx; j++){
                countP++;
                if(tempH[j] != 0){
                    valR = tempH[j];
                    break;
                }
            }
            count = countN + countP;
            step = Math.abs(valL - valR) / count;
            est = (i <= peakIdx) ? (valL + (step * countN)) : (valL - (step * countN));
            est = Math.round(est);
            tempH[i] = est;
        }
        this.hist = tempH.clone();
    }
}
