package jmr.application;


/**
 *
 * @author Míriam Mengíbar Rodríguez
 */
public class SettingsClassifier {
    static private int widthWS = 200;
    static private int heigthWS = 200;
    static private int stepSizeWS = 60;
    static private int thresholdHeatmap = 130;
    
    
    static public void setParameters(int width, int heigth, int stepSize, int threshold) {
        SettingsClassifier.widthWS = width;
        SettingsClassifier.heigthWS = heigth;
        SettingsClassifier.stepSizeWS = stepSize;
        SettingsClassifier.thresholdHeatmap = threshold;        
    }    

    public static int getWidthWS() {
        return widthWS;
    }

    public static void setWidthWS(int widthWS) {
        SettingsClassifier.widthWS = widthWS;
    }

    public static int getHeigthWS() {
        return heigthWS;
    }

    public static void setHeigthWS(int heigthWS) {
        SettingsClassifier.heigthWS = heigthWS;
    }

    public static int getStepSizeWS() {
        return stepSizeWS;
    }

    public static void setStepSizeWS(int stepSizeWS) {
        SettingsClassifier.stepSizeWS = stepSizeWS;
    }

    public static int getThresholdHeatmap() {
        return thresholdHeatmap;
    }

    public static void setThresholdHeatmap(int thresholdHeatmap) {
        SettingsClassifier.thresholdHeatmap = thresholdHeatmap;
    }
}
