package jmr.descriptor.color;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.Serializable;
import java.util.Arrays;
import jmr.colorspace.ColorConvertTools;
import jmr.media.JMRExtendedBufferedImage;
import jmr.colorspace.ColorSpaceJMR;
import jmr.descriptor.MediaDescriptor;

/**
 * Scalable Color Descriptor from MPEG7 standard.
 *
 *
 * The Scalable Color Descriptor is a Color Histogram in HSV Color Space, which
 * is encoded by a Haar transform. Its binary representation is scalable in
 * terms of bin numbers and bit representation accuracy over a broad range of
 * data rates. The Scalable Color Descriptor is useful for image-to-image
 * matching and retrieval based on color feature. Retrieval accuracy increases
 * with the {@link #nofCoefficients} used in the representation.
 *
 * This class is inspired by ScalableColorImpl.java from
 *  <a href="http://www.semanticmetadata.net">Caliph Emir project</a>
 *
 * @author RAT Benoit
 * @author Jesús Chamorro Martínez (jesus@decsai.ugr.es)
 *
 */
public class MPEG7ScalableColor implements MediaDescriptor<BufferedImage>, Serializable{
    /**
     * The source image of this descriptor
     */
    private transient BufferedImage source = null;
    /**
     * The color space used in this descriptor
     */
    public static final int COLOR_SPACE = ColorSpaceJMR.CS_HSV;
    /**
     * The number of bins in the histogram descriptor. It should be 16, 32, 64,
     * 128 or 256.
     */
    protected int nofCoefficients;
    /**
     * The number of bit planes of the coefficients that are discarded, ranging
     * from 0 to 8. If its value is 0, the coefficients are represented by their
     * magitude and their sign; if its value is 8, the coefficients are
     * represented only by their sign.
     *
     * This property is designed for hardware accelaration or specific code, so
     * it makes less sens in this java implementation.
     */
    protected int nofBitPlanesDiscarded;
    /**
     * Default number of bins in the histogram descriptor
     */
    final public static int DEFAULT_NUM_BINS = 256;
    /**
     * Default number of bit planes of the coefficients that are discarded
     */
    final public static  int DEFAULT_NUM_BITPLANES_DISCARDED = 0;       
    /**
     * Histogram representing this descriptor
     */
    protected int[] histoHaar = null;
    /**
     * Number of bins on the hue component
     */
    final protected int H_BINS = 16;
    /**
     * Number of bins on the saturation component
     */
    final protected int S_BINS = 4;
    /**
     * Number of bins on the intensity component
     */
    final protected int V_BINS = 4;
    /**
     * Scaling factor to transform a hue value into a bin index. In the case of
     * this descriptor, based on the HSV color space, this factor is calculated
     * as (360+epsilon)/{@link #H_BINS}
     * 
     */
    final protected float H_SCALE = (360.0f + 1.0f) / (float)H_BINS;
    /**
     * Scaling factor to transform a saturation value into a bin index. In the
     * case of this descriptor, based on the HSV color space, this factor is
     * calculated as (1+epsilon)/{@link #S_BINS}
     */
    final protected float S_SCALE = (1.0f + 1.0f/255.0f) / (float)S_BINS;
    /**
     * Scaling factor to transform a intensity value into a bin index. In the
     * case of this descriptor, based on the HSV color space, this factor is
     * calculated as (1+epsilon)/{@link #V_BINS}
     */
    final protected float V_SCALE = (1.0f + 1.0f/255.0f) / (float)V_BINS;

    
    /**
     * Constructs a new scalable color descriptor and initializes it from the 
     * image given by parameter
     *
     * @param image the source image
     * @param numCoeffients the number of histogram bins (32,64,128 or 256)
     * @param numBitplanes the number of bitplanes discarded in the histogram 
     */
    public MPEG7ScalableColor(BufferedImage image, int numCoeffients, int numBitplanes) {
        this.nofBitPlanesDiscarded = numBitplanes;
        this.nofCoefficients = numCoeffients;
        this.setSource(image); // Set the source image and init the descriptor
    }
    
    /**
     * Constructs a new scalable color descriptor and initializes it from the 
     * image given by parameter. The number of bins and the number of bitplanes 
     * discarded are set using the default values {@link #DEFAULT_NUM_BINS} and
     * {@link #DEFAULT_NUM_BITPLANES_DISCARDED}
     * 
     * @param image the source image
     */
    public MPEG7ScalableColor(BufferedImage image) {
        this(image, DEFAULT_NUM_BINS, DEFAULT_NUM_BITPLANES_DISCARDED);
    }

      /**
     * Returns the image source associated to this descriptor
     *
     * @return the image source
     */
    @Override
    final public BufferedImage getSource() {
        return source;
    }

    /**
     * Set the image source of this descriptor and updates it on the basis of
     * the new data.
     *
     * @param image the image source
     */
    @Override
    public final void setSource(BufferedImage image) {
        this.source = image;
        init(image);
    }

    /**
     * Initialize the descriptor.
     *
     * @param image the source image
     */
    @Override
    public void init(BufferedImage image) {
        // The MPEG7ColorStructure need a JMRExtendedBufferedImage to be calculated
        JMRExtendedBufferedImage JMRimage = null;
        try {
            JMRimage = (JMRExtendedBufferedImage) image;
        } catch (ClassCastException ex) {
            JMRimage = new JMRExtendedBufferedImage(image);
        }
        // The color space and the image model must been the suitable ones.
        if (!checkImage(JMRimage)) {
            JMRimage = convertImg(JMRimage);
        }
        this.initHistogram(JMRimage);
    }
    
    /**
     * Compares this descriptor to the one given by parameter.
     *
     * This method is valid only for <code>MPEG7ScalableColor</code> image
     * descriptors
     *
     * @param mediaDescriptor descriptor to be compared.
     * @return the result of the descriptor comparision.
     *
     */
    @Override
    public Double compare(MediaDescriptor mediaDescriptor) {
        // Only MPEG7ScalableColor objects can be compared
        if (!(mediaDescriptor instanceof MPEG7ScalableColor)) {
            return (null);
        }
        return (compare((MPEG7ScalableColor) mediaDescriptor));
    }

    /**
     * Compare this descriptor to the one given by parameter using the l1-norm 
     * between each bins of the histograms.
     *
     * @param descriptor descriptor to be compared.
     * @return the distance between descriptors (<code>null</code> if the 
     * descriptors are not comparable)
     */
    public Double compare(MPEG7ScalableColor descriptor) {
        if (descriptor.nofBitPlanesDiscarded != this.nofBitPlanesDiscarded || 
            descriptor.nofCoefficients != this.nofCoefficients) {
            return null;
        }        
        if (descriptor.histoHaar == null || this.histoHaar == null) {
            return null;
        }
        double diffsum = 0;
        for (int i = 0; i < nofCoefficients; i++) {
            diffsum += Math.abs(this.histoHaar[i] - descriptor.histoHaar[i]);
        }
        return diffsum;
    }

    // <editor-fold defaultstate="collapsed" desc="Private methods for calculating the descriptor"> 
  
    /**
     * Quantization matrix. 
     * 
     * From <a href="www.semanticmetadata.net">Caliph and Emir project</a>
     */
    final private int[][] scalableColorQuantValues
            = {
                {217, 9, 255}, {-71, 9, 255}, {-27, 8, 127}, {-54, 9, 255}, {-8, 7, 63}, {-14, 7, 63}, {-22, 7, 63}, {-29, 8, 127},
                {-6, 6, 31}, {-13, 7, 63}, {-11, 6, 31}, {-22, 7, 63}, {-9, 7, 63}, {-14, 7, 63}, {-19, 7, 63}, {-22, 7, 63},
                {0, 4, 7}, {-1, 5, 15}, {0, 3, 3}, {-2, 6, 31}, {1, 5, 15}, {-5, 6, 31}, {0, 5, 15}, {0, 7, 63},
                {2, 5, 15}, {-2, 6, 31}, {-2, 5, 15}, {0, 7, 63}, {3, 5, 15}, {-5, 6, 31}, {-1, 6, 31}, {4, 7, 63},
                {0, 3, 3}, {0, 3, 3}, {0, 3, 3}, {-1, 5, 15}, {0, 3, 3}, {0, 3, 3}, {-1, 5, 15}, {-2, 5, 15},
                {-1, 5, 15}, {-1, 4, 7}, {-1, 5, 15}, {-3, 5, 15}, {-1, 5, 15}, {-2, 5, 15}, {-4, 5, 15}, {-5, 5, 15},
                {-1, 5, 15}, {0, 3, 3}, {-2, 5, 15}, {-2, 5, 15}, {-2, 5, 15}, {-3, 5, 15}, {-3, 5, 15}, {0, 5, 15},
                {0, 5, 15}, {0, 5, 15}, {0, 5, 15}, {2, 5, 15}, {-1, 5, 15}, {0, 5, 15}, {3, 6, 31}, {3, 5, 15},
                {0, 2, 1}, {0, 2, 1}, {0, 3, 3}, {0, 4, 7}, {0, 2, 1}, {0, 2, 1}, {0, 3, 3}, {-1, 4, 7},
                {-1, 4, 7}, {-1, 4, 7}, {-2, 5, 15}, {-1, 5, 15}, {-2, 5, 15}, {-2, 5, 15}, {-2, 5, 15}, {-1, 5, 15},
                {0, 3, 3}, {0, 2, 1}, {0, 3, 3}, {-1, 4, 7}, {0, 2, 1}, {0, 3, 3}, {-1, 4, 7}, {-1, 5, 15},
                {-2, 5, 15}, {-1, 4, 7}, {-2, 5, 15}, {-1, 5, 15}, {-3, 5, 15}, {-3, 5, 15}, {-2, 5, 15}, {0, 5, 15},
                {0, 3, 3}, {0, 3, 3}, {0, 3, 3}, {-1, 4, 7}, {0, 3, 3}, {0, 3, 3}, {-2, 5, 15}, {-2, 5, 15},
                {-2, 5, 15}, {-2, 4, 7}, {-2, 5, 15}, {-1, 5, 15}, {-3, 5, 15}, {-3, 5, 15}, {-1, 5, 15}, {0, 5, 15},
                {1, 4, 7}, {0, 3, 3}, {0, 4, 7}, {-1, 4, 7}, {0, 3, 3}, {0, 4, 7}, {-1, 4, 7}, {0, 4, 7},
                {-1, 4, 7}, {-1, 3, 3}, {-1, 4, 7}, {0, 4, 7}, {-1, 5, 15}, {0, 5, 15}, {1, 5, 15}, {-1, 5, 15},
                {0, 2, 1}, {0, 2, 1}, {0, 3, 3}, {0, 3, 3}, {0, 2, 1}, {0, 2, 1}, {0, 3, 3}, {0, 3, 3},
                {0, 2, 1}, {0, 2, 1}, {0, 3, 3}, {0, 4, 7}, {0, 2, 1}, {0, 2, 1}, {0, 3, 3}, {0, 3, 3},
                {0, 3, 3}, {0, 2, 1}, {0, 3, 3}, {1, 4, 7}, {0, 2, 1}, {0, 3, 3}, {-1, 4, 7}, {1, 4, 7},
                {0, 3, 3}, {0, 3, 3}, {0, 3, 3}, {0, 4, 7}, {0, 3, 3}, {0, 3, 3}, {-1, 4, 7}, {0, 4, 7},
                {0, 3, 3}, {0, 2, 1}, {0, 3, 3}, {0, 3, 3}, {0, 2, 1}, {0, 2, 1}, {0, 3, 3}, {0, 3, 3},
                {0, 3, 3}, {0, 2, 1}, {0, 3, 3}, {1, 4, 7}, {0, 2, 1}, {0, 3, 3}, {0, 4, 7}, {1, 4, 7},
                {0, 3, 3}, {0, 2, 1}, {0, 3, 3}, {1, 5, 15}, {0, 3, 3}, {0, 3, 3}, {-1, 5, 15}, {2, 5, 15},
                {0, 3, 3}, {0, 3, 3}, {0, 3, 3}, {0, 4, 7}, {0, 3, 3}, {0, 3, 3}, {-1, 4, 7}, {1, 5, 15},
                {0, 3, 3}, {0, 2, 1}, {0, 3, 3}, {0, 3, 3}, {0, 2, 1}, {0, 3, 3}, {0, 4, 7}, {0, 4, 7},
                {0, 3, 3}, {0, 2, 1}, {0, 3, 3}, {1, 4, 7}, {0, 3, 3}, {0, 3, 3}, {-1, 5, 15}, {1, 5, 15},
                {0, 3, 3}, {0, 2, 1}, {-1, 3, 3}, {1, 5, 15}, {0, 3, 3}, {-1, 4, 7}, {-1, 5, 15}, {2, 5, 15},
                {0, 3, 3}, {0, 3, 3}, {0, 3, 3}, {0, 4, 7}, {0, 3, 3}, {-1, 3, 3}, {0, 4, 7}, {1, 4, 7},
                {1, 3, 3}, {0, 2, 1}, {-1, 3, 3}, {0, 3, 3}, {0, 3, 3}, {0, 3, 3}, {0, 3, 3}, {1, 4, 7},
                {0, 3, 3}, {0, 2, 1}, {-1, 3, 3}, {0, 4, 7}, {0, 3, 3}, {0, 3, 3}, {0, 4, 7}, {1, 4, 7},
                {0, 3, 3}, {0, 2, 1}, {0, 3, 3}, {0, 4, 7}, {0, 3, 3}, {-1, 3, 3}, {0, 4, 7}, {1, 4, 7},
                {0, 3, 3}, {0, 3, 3}, {0, 3, 3}, {0, 3, 3}, {0, 3, 3}, {-1, 3, 3}, {0, 3, 3}, {-1, 4, 7}
            };
    
    /**
     * Initialize the histogram associated to this descriptor.
     * 
     * @param image the source image
     */
    protected void initHistogram(JMRExtendedBufferedImage image){
        int wImg = image.getWidth();
        int hImg = image.getHeight();
        Raster imRst = image.getRaster();
        float[] pixel = new float[3];
        int i, j, k;
        
        int[][][]histoMx = new int[H_BINS][S_BINS][V_BINS]; // By default, filled with 0
        for(int x=0; x<wImg; x++){
            for(int y=0; y<hImg; y++){
                imRst.getPixel(x, y, pixel);
                i = (int) (pixel[0] / H_SCALE); //H in bin levels
                j = (int) (pixel[1] / S_SCALE); //S in bin levels
                k = (int) (pixel[2] / V_SCALE); //V in bin levels
                histoMx[i][j][k]++;                                
            }
        }
        int[] histoVec = histoMx2histoVec(histoMx);
        QuantizeHistogram(histoVec);
        this.histoHaar = HaarTransform(histoVec);
    }
    
    /**
     * Transform the histogram from a matrix structure to a vector structure 
     */
     protected int[] histoMx2histoVec(int[][][] histoMx) {
        int[] histoVec = new int[H_BINS * V_BINS * S_BINS];
        int count = 0;
        for (int k = 0; k < V_BINS; k++) {
            for (int l = 0; l < S_BINS; l++) {
                for (int m = 0; m < H_BINS; m++) {
                    histoVec[count] = histoMx[m][l][k];
                    count++;
                }
            }
        }
        return histoVec;
    }
    
    /**
     * Quantize the histogram.
     * 
     * @param aHist the source histogram
     * @return the quantized histogram
     */
    protected int[] QuantizeHistogram(int[] aHist) {
        int sumPixels = 0;
        for (int i = 0; i < aHist.length; i++) {
            sumPixels += aHist[i];
        }
        int factor, ibinwert;
        double binwert;
        factor = 0x7ff; 
        for (int i = 0; i < nofCoefficients; i++) {
            binwert = (double) (factor) * (double) (aHist[i] / (double) sumPixels);
            ibinwert = (int) (binwert + 0.49999);
            if (ibinwert > factor) {
                ibinwert = factor; //obsolete
            }
            aHist[i] = ibinwert;
        }
        factor = 15;
        int iwert = 0;
        double wert, potenz = 0.4;
        double arg, maxwert;
        maxwert = (double) 40 * (double) 2047 / (double) 100;
        for (int i = 0; i < nofCoefficients; i++) {
            wert = (double) (aHist[i]);
            if (wert > maxwert) {
                iwert = factor;
            }
            if (wert <= maxwert) {
                arg = wert / maxwert;
                wert = (float) factor * Math.pow(arg, potenz);
                iwert = (int) (wert + 0.5);
            }
            if (iwert > factor) {
                iwert = factor;
            }
            aHist[i] = iwert;
        }
        return aHist;
    }

    /**
     * Haar transform.
     * 
     * From <a href="www.semanticmetadata.net">Caliph-Emir project</a>
     *
     * @param	aHist 256-bin histogram (16H*4S*4V)
     * @return	the histogram after haar tranform and before quantification.
     */
    protected int[] HaarTransform(int[] aHist) {
        // Table for sorting histogram indexes in Harr transform.
        int[] sorttab = new int[]{
            0, 4, 8, 12, 32, 36, 40, 44, 128, 132, 136, 140, 160, 164, 168, 172,
            2, 6, 10, 14, 34, 38, 42, 46, 130, 134, 138, 142, 162, 166, 170, 174,
            64, 66, 68, 70, 72, 74, 76, 78, 96, 98, 100, 102, 104, 106, 108, 110, 192,
            194, 196, 198, 200, 202, 204, 206, 224, 226, 228, 230, 232, 234, 236, 238,
            16, 18, 20, 22, 24, 26, 28, 30, 48, 50, 52, 54, 56, 58, 60, 62, 80, 82,
            84, 86, 88, 90, 92, 94, 112, 114, 116, 118, 120, 122, 124, 126, 144, 146,
            148, 150, 152, 154, 156, 158, 176, 178, 180, 182, 184, 186, 188, 190, 208,
            210, 212, 214, 216, 218, 220, 222, 240, 242, 244, 246, 248, 250, 252, 254,
            1, 3, 5, 7, 9, 11, 13, 15, 17, 19, 21, 23, 25, 27, 29, 31, 33, 35, 37, 39,
            41, 43, 45, 47, 49, 51, 53, 55, 57, 59, 61, 63, 65, 67, 69, 71, 73, 75,
            77, 79, 81, 83, 85, 87, 89, 91, 93, 95, 97, 99, 101, 103, 105, 107, 109,
            111, 113, 115, 117, 119, 121, 123, 125, 127, 129, 131, 133, 135, 137, 139,
            141, 143, 145, 147, 149, 151, 153, 155, 157, 159, 161, 163, 165, 167, 169,
            171, 173, 175, 177, 179, 181, 183, 185, 187, 189, 191, 193, 195, 197, 199,
            201, 203, 205, 207, 209, 211, 213, 215, 217, 219, 221, 223, 225, 227, 229,
            231, 233, 235, 237, 239, 241, 243, 245, 247, 249, 251, 253, 255
        };
        // Table for selecting the input to perform the basic unit function
        int[][] tabelle = new int[][]{
            {0, 2, 4, 6, 8, 10, 12, 14, 0, 2, 4, 6, 8, 10, 12, 14, 0, 2, 4, 6, 8, 10,
             12, 14, 0, 2, 4, 6, 8, 10, 12, 14, 0, 2, 4, 6, 8, 10, 12, 14, 0, 2, 4, 6,
             8, 10, 12, 14, 0, 2, 4, 6, 8, 10, 12, 14, 0, 2, 4, 6, 8, 10, 12, 14, 0, 2,
             4, 6, 8, 10, 12, 14, 0, 2, 4, 6, 8, 10, 12, 14, 0, 2, 4, 6, 8, 10, 12, 14,
             0, 2, 4, 6, 8, 10, 12, 14, 0, 2, 4, 6, 8, 10, 12, 14, 0, 2, 4, 6, 8, 10,
             12, 14, 0, 2, 4, 6, 8, 10, 12, 14, 0, 2, 4, 6, 8, 10, 12, 14, 0, 2, 4, 6,
             8, 10, 12, 14, 0, 2, 4, 6, 8, 10, 12, 14, 0, 2, 4, 6, 8, 10, 12, 14, 0, 2,
             4, 6, 8, 10, 12, 14, 0, 2, 4, 6, 8, 10, 12, 14, 0, 2, 4, 6, 8, 10, 12, 14,
             0, 2, 4, 6, 8, 10, 12, 14, 0, 2, 4, 6, 8, 10, 12, 14, 0, 2, 4, 6, 8, 10,
             12, 14, 0, 2, 4, 6, 8, 10, 12, 14, 0, 2, 4, 6, 8, 10, 12, 14, 0, 2, 4, 6,
             8, 10, 12, 14, 0, 4, 8, 12, 0, 4, 8, 12, 0, 4, 8, 12, 0, 4, 8, 12, 0, 4,
             8, 12, 0, 4, 8, 12, 0, 4, 8, 12, 0, 8, 0}, 
            
            {0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2,
             2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 5,
             5, 5, 5, 5, 6, 6, 6, 6, 6, 6, 6, 6, 7, 7, 7, 7, 7, 7, 7, 7, 8, 8,
             8, 8, 8, 8, 8, 8, 9, 9, 9, 9, 9, 9, 9, 9, 10, 10, 10, 10, 10, 10, 10, 10,
             11, 11, 11, 11, 11, 11, 11, 11, 12, 12, 12, 12, 12, 12, 12, 12, 13, 13,13, 13, 13, 13,
             13, 13, 14, 14, 14, 14, 14, 14, 14, 14, 15, 15, 15, 15, 15, 15, 15, 15, 0, 0, 0, 0,
             0, 0, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 4, 4, 4, 4, 4, 4, 4, 4, 6, 6,
             6, 6, 6, 6, 6, 6, 8, 8, 8, 8, 8, 8, 8, 8, 10, 10, 10, 10, 10, 10, 10, 10,
             12, 12, 12, 12, 12, 12, 12, 12, 14, 14, 14, 14, 14, 14, 14, 14, 0, 0, 0, 0, 0, 0,
             0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 8, 8, 8, 8, 8, 8, 8, 8, 10, 10, 10, 10,
             10, 10, 10, 10, 0, 0, 0, 0, 2, 2, 2, 2, 8, 8, 8, 8, 10, 10, 10, 10, 0, 0,
             0, 0, 8, 8, 8, 8, 0, 0, 0, 0, 0, 0, 0},
            
            {1, 3, 5, 7, 9, 11, 13, 15, 1, 3, 5, 7, 9, 11, 13, 15, 1, 3, 5, 7, 9, 11,
             13, 15, 1, 3, 5, 7, 9, 11, 13, 15, 1, 3, 5, 7, 9, 11, 13, 15, 1, 3, 5, 7,
             9, 11, 13, 15, 1, 3, 5, 7, 9, 11, 13, 15, 1, 3, 5, 7, 9, 11, 13, 15, 1, 3,
             5, 7, 9, 11, 13, 15, 1, 3, 5, 7, 9, 11, 13, 15, 1, 3, 5, 7, 9, 11, 13, 15,
             1, 3, 5, 7, 9, 11, 13, 15, 1, 3, 5, 7, 9, 11, 13, 15, 1, 3, 5, 7, 9, 11,
             13, 15, 1, 3, 5, 7, 9, 11, 13, 15, 1, 3, 5, 7, 9, 11, 13, 15, 0, 2, 4, 6,
             8, 10, 12, 14, 0, 2, 4, 6, 8, 10, 12, 14, 0, 2, 4, 6, 8, 10, 12, 14, 0, 2,
             4, 6, 8, 10, 12, 14, 0, 2, 4, 6, 8, 10, 12, 14, 0, 2, 4, 6, 8, 10, 12, 14,
             0, 2, 4, 6, 8, 10, 12, 14, 0, 2, 4, 6, 8, 10, 12, 14, 0, 2, 4, 6, 8, 10,
             12, 14, 0, 2, 4, 6, 8, 10, 12, 14, 0, 2, 4, 6, 8, 10, 12, 14, 0, 2, 4, 6,
             8, 10, 12, 14, 2, 6, 10, 14, 2, 6, 10, 14, 2, 6, 10, 14, 2, 6, 10, 14, 0, 4,
             8, 12, 0, 4, 8, 12, 0, 4, 8, 12, 4, 12, 8},
            
            {0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2,
             2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 5,
             5, 5, 5, 5, 6, 6, 6, 6, 6, 6, 6, 6, 7, 7, 7, 7, 7, 7, 7, 7, 8, 8,
             8, 8, 8, 8, 8, 8, 9, 9, 9, 9, 9, 9, 9, 9, 10, 10, 10, 10, 10, 10, 10, 10,
             11, 11, 11, 11, 11, 11, 11, 11, 12, 12, 12, 12, 12, 12, 12, 12, 13, 13,13, 13, 13, 13,
             13, 13, 14, 14, 14, 14, 14, 14, 14, 14, 15, 15, 15, 15, 15, 15, 15, 15, 1, 1, 1, 1,
             1, 1, 1, 1, 3, 3, 3, 3, 3, 3, 3, 3, 5, 5, 5, 5, 5, 5, 5, 5, 7, 7,
             7, 7, 7, 7, 7, 7, 9, 9, 9, 9, 9, 9, 9, 9, 11, 11, 11, 11, 11, 11, 11, 11,
             13, 13, 13, 13, 13, 13, 13, 13, 15, 15, 15, 15, 15, 15, 15, 15, 4, 4, 4, 4, 4, 4,
             4, 4, 6, 6, 6, 6, 6, 6, 6, 6, 12, 12, 12, 12, 12, 12, 12, 12, 14, 14, 14,14,
             14, 14, 14, 14, 0, 0, 0, 0, 2, 2, 2, 2, 8, 8, 8, 8, 10, 10, 10, 10, 2, 2,
             2, 2, 10, 10, 10, 10, 8, 8, 8, 8, 0, 0, 0},
            
            {128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128,
             128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128,
             128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128,
             128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128,
             128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128,
             128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 64, 64, 64, 64,
             64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64,
             64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64,
             64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 32, 32, 32, 32, 32, 32,
             32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32,
             32, 32, 32, 32, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16,16, 16, 8, 8,
             8, 8, 8, 8, 8, 8, 4, 4, 4, 4, 2, 2, 1}
        };
        int index;
        int[] histogram_in = new int[256];
        int[] histogram_out = new int[256];

        for (int i = 0; i < nofCoefficients; i++) {
            histogram_in[i] = (int) aHist[i];
        }
        histo_3d_hirarch_5(tabelle, tabelle[0].length, histogram_in, H_BINS, S_BINS, V_BINS);
        for (int j = 0; j < 256; ++j) {
            index = sorttab[j];
            histogram_out[j] = histogram_in[index];
        }
        hsv_hir_quant_lin_5(histogram_out);
        red_bits_pro_bin_5(histogram_out, nofBitPlanesDiscarded);

        return histogram_out;
    }

    /**
     * Method from <a href="www.semanticmetadata.net">Caliph & Emir project</a>
     *
     * @param tabelle represent which input is taken during the haar transform
     * to perform the basic unit function
     * @param tablae second dimension (height) of the tabelle = 254 links
     * corresponding to the
     * @param histogram is the 256 histograms values
     * @param h_size number of bin for H dimension which is normally equal to 16
     * @param s_size number of bin for S dimension which is normally equal to 4
     * @param v_size number of bin for V dimension which is normally equal to 4
     */
    private void histo_3d_hirarch_5(int[][] tabelle, int tablae, int[] histogram, int h_size, int s_size, int v_size) {
        int sum, dif, x1, y1, x2, y2;
        //Matrix is the 2D transformation from the 3D Matrix histogram
        int[][] matrix = new int[16][16];
        //Filling matrix with Mx{H=[1,16]}{S*V=[1,4]*[1,4]}
        for (int i = 0; i < h_size * s_size * v_size; ++i) {
            matrix[i % (h_size)][i / (h_size)] = histogram[i];
        }
        for (int i = 0; i < tablae; ++i) {
            y1 = tabelle[0][i];
            x1 = tabelle[1][i];
            y2 = tabelle[2][i];
            x2 = tabelle[3][i];
            sum = matrix[y1][x1] + matrix[y2][x2];
            dif = matrix[y2][x2] - matrix[y1][x1];
            matrix[y1][x1] = sum;
            matrix[y2][x2] = dif;
        }
        for (int i = 0; i < h_size * s_size * v_size; ++i) {
            histogram[i] = matrix[i % (h_size)][i / (h_size)];
        }
    }

    /**
     * Method from <a href="www.semanticmetadata.net">Caliph & Emir project</a>
     * 
     * @param histogram the histogram
     * @param NumberOfBitplanesDiscarded number of bit planes that are discarded
     */
    private void red_bits_pro_bin_5(int[] histogram, int NumberOfBitplanesDiscarded) {
        int wert, wert1, bits_pro_bin, bits_pro_bild;
        int max_bits_pro_bin, anzkof;
        if (NumberOfBitplanesDiscarded == 0) {
            return;
        }
        bits_pro_bild = 0;
        max_bits_pro_bin = 0;
        anzkof = 0;
        if (NumberOfBitplanesDiscarded > 0) {
            for (int i = 0; i < 256; ++i) {
                bits_pro_bin = scalableColorQuantValues[i][1] - NumberOfBitplanesDiscarded;
                if (bits_pro_bin < 2) {
                    wert = histogram[i];
                    if (wert >= 0) {
                        histogram[i] = 1;
                    }
                    if (wert < 0) {
                        histogram[i] = 0;
                    }
                    bits_pro_bild = bits_pro_bild + 1;
                }
                if (bits_pro_bin >= 2) {
                    wert = histogram[i];
                    wert1 = wert;
                    if (wert < 0) {
                        wert = -wert;
                    }
                    bits_pro_bild = bits_pro_bild + bits_pro_bin;
                    if (bits_pro_bin > max_bits_pro_bin) {
                        max_bits_pro_bin = bits_pro_bin;
                    }
                    anzkof = anzkof + 1;
                    for (int j = 0; j < NumberOfBitplanesDiscarded; ++j) {
                        wert = wert >> 1;
                    }
                    if (wert1 < 0) {
                        wert = -wert;
                    }
                    histogram[i] = wert;
                }
            }
        }
    }

    /**
     * Method from <a href="www.semanticmetadata.net">Caliph & Emir project</a>
     * 
     * @param histogram the histogram
     */
    private void hsv_hir_quant_lin_5(int[] histogram) {
        int i, wert, maxwert;
        for (i = 0; i < 256; ++i) {
            maxwert = scalableColorQuantValues[i][2];
            wert = histogram[i] - scalableColorQuantValues[i][0];
            if (wert > maxwert) {
                wert = maxwert;
            }
            if (wert < -maxwert) {
                wert = -maxwert;
            }
            histogram[i] = wert;
        }
    }

    /**
     * Checks if the given image is this descriptor color space and, in
     * addition, if its type is
     * {@link jmr.media.JMRExtendedBufferedImage#TYPE_JMR_3F_INTERLEAVED}.
     *
     * @param im the image to be checked
     * @return <tt>true</tt> if the image is OK
     */
    protected boolean checkImage(JMRExtendedBufferedImage im) {
        boolean color_space_ok = im.getColorModel().getColorSpace().getType() == COLOR_SPACE;
        boolean image_type_ok = (im.getType() == JMRExtendedBufferedImage.TYPE_JMR_3F_INTERLEAVED);
        return color_space_ok && image_type_ok;
    }

    /**
     * Converts the given image to the colos space of this descriptor.
     *
     * @param imSrc source image
     * @return a new image in the the colos space of this descriptor
     */
    protected JMRExtendedBufferedImage convertImg(JMRExtendedBufferedImage imSrc) {
        return ColorConvertTools.colorConvertOp(imSrc, ColorSpaceJMR.getInstance(COLOR_SPACE));
    }
    
    // </editor-fold>

    /**
     * Returns the coefficient signs of this descriptor.
     * 
     * @return the coefficients signs
     */
    public byte[] getCoefficientSigns() {
        byte[] coeffSign = new byte[nofCoefficients / 8];
        for (int i = 0; i < coeffSign.length; i++) {
            byte tmp = 0;
            int offset = i * 8;
            for (int j = 0; j < 8; j++) {
                if (this.histoHaar[j + offset] < 0) {
                    //The operation ( 1 << j) shift the value 00000001 of j step: (1 << 3)=00001000
                    tmp |= (1 << j); //10000001 OR 10001000 = 10001001 (change only if 1)
                } else {
                    tmp &= ~(1 << j); //10010001 AND NOT 10000000 =  00010001 (replace 1 by zero)
                }
            }
            coeffSign[i] = tmp;
        }
        return coeffSign;
    }

    /**
     * Returns the bit planes of this descriptor.
     * 
     * @return the bit planes
     */
    public byte[] getBitPlane() {
        byte[] bitPlane = new byte[nofCoefficients];
        for (int i = 0; i < this.histoHaar.length; i++) {
            bitPlane[i] = (byte) (Math.abs(this.histoHaar[i]) & 0x000000FF);
        }
        return bitPlane;
    }
    
    /**
     * Set the histogram values from a given vector of values.
     * 
     * @param magnitud
     * @param sign 
     */
    protected void setHistoHaar(byte[] magnitud, byte[] sign) {
        byte tmp = 0;
        if (magnitud.length != sign.length * 8) {
            return;
        }
        histoHaar = new int[magnitud.length];
        for (int i = 0; i < magnitud.length; i++) {
            if (i % 8 == 0) {
                tmp = sign[i / 8]; //Take a byte each 8 incrementation
                //last bit=1 means negative sign
            }
            histoHaar[i] = magnitud[i] & 0x000000FF; //We don't use the sign here.
            if ((tmp & 0x01) == 1) {
                histoHaar[i] *= -1;
            }
            tmp = (byte) (tmp >> 1); //Shift one bit each round
        }
    }
    
    /**
     * Returns a string representation of this descriptor .
     *
     * @return a string representation of this descriptor
     */
    @Override
    public String toString() {
        return "MPEG7ScalableColor: " + Arrays.toString(histoHaar);
    }

}
