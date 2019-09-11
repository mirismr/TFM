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
 * Color Structure Descriptor from MPEG7 standard.
 *
 * The Color structure descriptor is a color feature descriptor that captures
 * both color content (similar to a color histogram) and information about the
 * structure of this content. Its main functionality is image-to-image matching
 * and its intended use is for still-image retrieval, where an image may consist
 * of either a single rectangular frame or arbitrarily shaped, possibly
 * disconnected, regions. The extraction method embeds color structure
 * information into the descriptor by taking into account all colors in a
 * structuring element of 8x8 pixels that slides over the image, instead of
 * considering each pixel separately. Unlike the color histogram, this
 * descriptor can distinguish between two images in which a given color is
 * present in identical amounts but where the structure of the groups of pixels
 * having that color is different in the two images. Color values are given by
 * the ColorSpaceHMMD which is quantized non-uniformly into 32, 64, 128 or 256
 * bins {@link #qLevels}. Each bin amplitude value is represented by an 8-bit
 * code. The Color Structure descriptor provides additional functionality and
 * improved similarity-based image retrieval performance for natural images
 * compared to the ordinary color histogram.
 * <a style="font-size:small;font-style:italic"
 * href="http://www.chiariglione.org/mpeg/standards/mpeg-7">
 * Definition from this link</a>
 *
 *
 * This class is inspired by ColorStructureImplementation.java from
 *  <a href="http://www.semanticmetadata.net">Caliph Emir project</a>
 *
 *
 * @author RAT Benoit
 * @author Jesús Chamorro Martínez (jesus@decsai.ugr.es)
 *
 */
public class MPEG7ColorStructure implements MediaDescriptor<BufferedImage>, Serializable {
    /**
     * The source media of this descriptor
     */
    protected transient BufferedImage source = null;
    /**
     * The color space used in this descriptor
     */
    public static final int COLOR_SPACE = ColorSpaceJMR.CS_HMMD;
    /**
     * Quantization Level of the HMMD ColorSpace
     */
    protected int qLevels = 256;
    /**
     * Histogram representing this descriptor
     */
    protected int[] histo = null;
    /**
     * Index for the 'hue' component in the HMMD color space
     */
    private static final int HUE = 0;
    /**
     * Index for the 'max' component in the HMMD color space
     */
    private static final int MAX = 1;
    /**
     * Index for the 'min' component in the HMMD color space
     */
    private static final int MIN = 2;
    /**
     * Index for the 'diff' component in the HMMD color space
     */
    private static final int DIFF = 3;
    /**
     * Quantization table for 256, 128, 64 and 32 quantization bins
     */
    private static final int[][][] QUANTIZATION_TABLE = {
        // Hue, Sum - subspace 0,1,2,3,4
        {{1, 8}, {4, 4}, {4, 4}, {4, 1}, {4, 1}}, // 32 levels
        {{1, 8}, {4, 4}, {4, 4}, {8, 2}, {8, 1}}, // 64 levels
        {{1, 16}, {4, 4}, {8, 4}, {8, 4}, {8, 4}}, // 128 level
        {{1, 32}, {4, 8}, {16, 4}, {16, 4}, {16, 4}}};  // 256 levels
    /**
     * Offset
     */
    private int offset = 0;
    /**
     * Default number of levels.
     */
    protected static final int DEFAULT_NUM_LEVELS = 256;
    
        
    /**
     * Constructs a new color structure descriptor and initializes it from the 
     * image given by parameter
     *
     * @param image the source image
     * @param qLevels the number of levels associated to this descriptor
     */
    public MPEG7ColorStructure(BufferedImage image, int qLevels) {
        this.source = image;
        this.init(image, qLevels);
    }

    /**
     * Constructs a new descriptor color structure descriptor and initializes it
     * from the image given by parameter. The number of levels are set to the
     * default value {@link #DEFAULT_NUM_LEVELS}
     *
     * @param image the source image
     */
    public MPEG7ColorStructure(BufferedImage image) {
        this(image, DEFAULT_NUM_LEVELS);
    }

    /**
     * Initializes the quantization level taking into account that the valid
     * values are 32, 64, 128 or 256.
     */
    protected void setLevels(int qLevels) {
        if (qLevels <= 32) {
            this.qLevels = 32;
        } else if (qLevels <= 64) {
            this.qLevels = 64;
        } else if (qLevels <= 128) {
            this.qLevels = 128;
        } else {
            this.qLevels = 256;
        }
        this.offset = (int) log2(qLevels) - 5; // 2^5=32 => log2(32)-5 = 0
    }

    /**
     * Initialize the descriptor.
     *
     * @param image the source image
     * @param qLevels the quantization levels (32, 64, 128 or 256)
     */
    public void init(BufferedImage image, int qLevels) {
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
        this.setLevels(qLevels);
        byte[][] imQ = quantHMMDImage(JMRimage);
        float[] histo = structuredHisto(imQ, image.getWidth(), image.getHeight());
        this.histo = reQuantization(histo);
    }
    
     /**
     * Initialize the descriptor.
     *
     * @param image the source image
     */
    @Override
    public void init(BufferedImage image) {
        init(image,DEFAULT_NUM_LEVELS);
    }
    
    /**
     * Compares this descriptor to the one given by parameter.
     *
     * This method is valid only for <code>MPEG7ColorStructure</code> image
     * descriptors
     *
     * @param mediaDescriptor descriptor to be compared.
     * @return the result of the descriptor comparision.
     *
     */
    @Override
    public Double compare(MediaDescriptor mediaDescriptor) {
        // Only MPEG7ColorStructure objects can be compared
        if (!(mediaDescriptor instanceof MPEG7ColorStructure)) {
            return (null);
        }
        return (compare((MPEG7ColorStructure) mediaDescriptor));
    }

    /**
     * Compare two CSD using the l1-norm between each bins of the histograms.
     *
     * <p>
     * In the case that the two ColorStructure Descriptor don't have the same
     * {@link #qLevels} numbers we perform a resize operation on the biggest
     * FeatureVec using the {@link #resizeCSD(MPEG7ColorStructure, int)} method.
     * </p>
     *
     * @param desc <code>MPEG7ColorStructure</code> object to be compared
     * @return the distance between descriptors
     */
    public Double compare(MPEG7ColorStructure desc) {
        int[] f1, f2;
        if (desc.histo == null || this.histo == null) {
            return (null);
        }
        if (this.qLevels == desc.qLevels) {
            f1 = this.histo;
            f2 = desc.histo;
        } else if (this.qLevels < desc.qLevels) {
            f1 = this.histo;
            f2 = resizeCSD(desc, this.qLevels);
        } else {
            f1 = resizeCSD(this, desc.qLevels);
            f2 = desc.histo;
        }
        Double distance = 0.0;
        for (int i = 0; i < f1.length; i++) {
            distance += Math.abs(f1[i] - f2[i]);
        }
        distance /= (256 * f1.length); //Normalization

        return distance;
    }
    
    /**
     * Returns the media source associated to this descriptor
     *
     * @return the media source
     */
    @Override
    final public BufferedImage getSource() {
        return source;
    }
  
    /**
     * Set the media source of this descriptor and updates it on the basis
     * of the new data.
     * 
     * @param image the media source
     */
    @Override
    public void setSource(BufferedImage image) {
        this.source = image;
        init(image);
    }
    
    // <editor-fold defaultstate="collapsed" desc="Private methods for calculating the descriptor">  

    /**
     * Convert (and quantize) the image to the HMMD color space.
     *
     * It first look in which subspace a HMMD value is by looking at the DIFF
     * component. Then it used the {@link #QUANTIZATION_TABLE} to obtain the bin
     * value for HUE and SUM and finally each bins value for each component is
     * merge to be put in the qLevels quantification.
     *
     * @param imSrc an image in the HMMD Color Space
     * {@link jmr.colorspace.ColorSpaceHMMD}
     * @return	a byte matrix representing the quantifized values between
     * [0,qLevels] This matrix is transposed. It is in the form :
     * imgQuant[height][width].
     */
    protected byte[][] quantHMMDImage(JMRExtendedBufferedImage imSrc) {
        //Source image variable
        int wImg = imSrc.getWidth();
        int hImg = imSrc.getHeight();
        Raster imRst = imSrc.getRaster();
        float[] pix = new float[4];
        //Destination image array
        byte[][] imDst = new byte[hImg][wImg];
        int subspace, hue_bin, sum_bin, v;
        int[] startSubSpacePos = getStartSubspacePos();
        for (int y = 0; y < hImg; y++) {
            for (int x = 0; x < wImg; x++) {
                imRst.getPixel(x, y, pix);
                //Define the subspace along the Diff axis
                subspace = getSubspace(pix[DIFF]);
                //Obtain the value of the hue in this quantization space
                hue_bin = (int) ((pix[HUE] / 361.0f) * QUANTIZATION_TABLE[offset][subspace][0]);
                //Obtain the value of the sum and multiply it by the hue value
                float tmp = ((pix[MIN] + pix[MAX]) / 2 - 1 / 255);
                sum_bin = (int) (tmp * QUANTIZATION_TABLE[offset][subspace][1]);
                if (sum_bin != 0) {
                    tmp = 0;
                    //Shift until the start position for this subspace in the histogram
                }
                v = startSubSpacePos[subspace] + sum_bin * QUANTIZATION_TABLE[offset][subspace][0] + hue_bin;
                //Check if value is not bigger than qLevels
                if (v >= qLevels) {
                    // Value computed is bigger than qLevels.
                    throw new RuntimeException("Error in  HMMD color space conversion");
                }
                //Set the value of the float
                imDst[y][x] = (byte) (v);
            }
        }
        return imDst;
    }

    /**
     * Returns the CSD histograms with value between 0 and 1.
     *
     * It creates a structuring block elements according to the size of the
     * quantified HMMD image. Then it computes a local histogram with the 8x8
     * structuring elements in the "sliding windows" block element. If one color
     * is present at least once on the local histogram of the sliding windows,
     * fill the CSD histogram with this color.
     *
     * @param imQ a byte matrix representing the quantifized values between
     * [0,qLevels] (heigh x width)
     * @param wImg width of the image
     * @param hImg height of the image
     * @return	a {@link #qLevels} histograms
     */
    protected float[] structuredHisto(byte[][] imQ, int wImg, int hImg) {
        int m = 0;
        double hw = Math.sqrt(hImg * wImg);
        double p = Math.floor(Math.log(hw) / Math.log(2) - 7.5); //Formula by Manjunath2002
        if (p < 0) {
            p = 0; //Minimum size of the division factor to have K=1
        }
        double K = Math.pow(2, p); //Determine the space between each structuring element
        double E = 8 * K; //Determine the size of the moving windows
        // Setting the local temporary and the CDS histograms
        float histo[] = new float[qLevels]; // CSD histograms
        int winHisto[] = new int[qLevels]; // local histo for a specific windows
        for (int i = 0; i < qLevels; i++) {
            histo[i] = 0.0f;
        }
        for (int y = 0; y < hImg - E; y += K) {
            for (int x = 0; x < wImg - E; x += K) {
                // Reinitialize the local windows histogram t[m]
                for (m = 0; m < qLevels; m++) {
                    winHisto[m] = 0;
                }
                for (int yy = y; yy < y + E; yy += K) {
                    for (int xx = x; xx < x + E; xx += K) {
                        //Obtain the pixel values of the HMMD quantized image
                        m = (int) (imQ[yy][xx] & 0x000000FF); //WARNING imQ is signed byte
                        //pixel value correspond to the bin value in qLevels CSD Histo
                        winHisto[m]++;
                    }
                } //End of local histogram for a local windows
                // Increment the color structure histogram for each color present in the structuring element
                for (m = 0; m < qLevels; m++) {
                    if (winHisto[m] > 0) {
                        histo[m]++;
                    }
                }
            }
        }
        //Normalize the histograms by the number of times the windows was shift
        int winShift_X = ((wImg - 1) - (int) E + (int) K);
        int winShift_Y = ((hImg - 1) - (int) E + (int) K);
        int S = (winShift_X / (int) K) * (winShift_Y / (int) K);
        for (m = 0; m < qLevels; m++) {
            histo[m] = histo[m] / S;
        }
        return histo;
    }

    /**
     * Calculates the subspace start positions (depending on the qLevels)
     * 
     * @return an array with the 5 start position
     */
    private int[] getStartSubspacePos() {
        return getStartSubspacePos(this.offset);
    }
    
    /**
     * Calculates the subspace start positions (depending on the qLevels)
     * 
     * @param offset the offset
     * @return an array with the 5 start position
     */
    private static int[] getStartSubspacePos(int offset) {
        int[] startP = new int[5];
        startP[0] = 0;
        for (int i = 1; i < startP.length; i++) {
            startP[i] = startP[i - 1]; //Set the position of the previous subspace start
            startP[i] += QUANTIZATION_TABLE[offset][i - 1][0] * QUANTIZATION_TABLE[offset][i - 1][1]; //Add the length of the previous subspace
        }
        return startP;
    }

    /**
     * Re-quantize the histogram (following Caliph-Emir code).
     *
     * @param	colorHistogramTemp a {@link #qLevels} non uniform CSD histograms
     * containing values between [0-1]
     * @return	a {@link #qLevels} uniform histograms.
     */
    protected int[] reQuantization(float[] colorHistogramTemp) {
        int[] uniformCSD = new int[colorHistogramTemp.length];
        for (int i = 0; i < colorHistogramTemp.length; i++) {
            uniformCSD[i] = quantFunc((double) colorHistogramTemp[i]);
        }
        return uniformCSD;
    }

    /**
     * Quantize the given value
     * 
     * @param x the value to be quantized
     * @return the quantized value
     */
    static public int quantFunc(double x) {
        double[] stepIn = {0.000000001, 0.037, 0.08, 0.195, 0.32, 1};
        int[] stepOut = {-1, 0, 25, 45, 80, 115};
        int y = 0;
        if (x <= 0) {
            y = 0;
        } else if (x >= 1) {
            y = 255;
        } else {
            y = (int) Math.round(((x - 0.32) / (1 - 0.32)) * 140.0);
            for (int i = 0; i < stepIn.length; i++) {
                if (x < stepIn[i]) {
                    y += stepOut[i];
                    break;
                }
            }
            // Since there is a bug in Caliph & emir version the data 
            // are between -66 and 255
            y = (int) (255.0 * ((double) y + 66) / (255.0 + 66.0));
        }
        return y;
    }

    /**
     * Resizes the descrptor to the given size.
     * 
     * @param c the descriptor
     * @param qSizeDst the new size
     * @return the resized descriptor
     */
    protected static int[] resizeCSD(MPEG7ColorStructure c, int qSizeDst) {
        int qSizeSrc = c.getQuantLevels();
        int[] dstHisto = new int[qSizeDst];
        int[] srcHisto = c.histo;
        if (qSizeSrc > qSizeDst) {
            int offsetSrc = (int) log2(qSizeSrc);
            int offsetDst = (int) log2(qSizeDst) - 5;
            int[] subStartPosSrc = getStartSubspacePos(offsetSrc);
            int[] subStartPosDst = getStartSubspacePos(offsetDst);
            int tmp = 0, sVal;
            double sumBinSrc, hueBinSrc, hueBinDst, sumBinDst;
            //We resize this descriptors
            for (int i = 0; i < qSizeSrc; i++) {
                tmp = 0;
                //Obtain the subspace Value or DiffBin looking at starting position
                for (sVal = 1; sVal < 5; sVal++) {
                    if (i < subStartPosSrc[sVal]) {
                        break;
                    }
                }
                sVal--;
                //Obtain the sum value
                tmp = i - subStartPosSrc[sVal];
                hueBinSrc = tmp % QUANTIZATION_TABLE[offsetSrc][sVal][0];
                sumBinSrc = Math.floor(tmp / QUANTIZATION_TABLE[offsetSrc][sVal][0]);
                //Compute their analog value in destination histograms
                hueBinDst = QUANTIZATION_TABLE[offsetDst][sVal][0] * (hueBinSrc / QUANTIZATION_TABLE[offsetSrc][sVal][0]);
                sumBinDst = QUANTIZATION_TABLE[offsetDst][sVal][1] * (sumBinSrc / QUANTIZATION_TABLE[offsetSrc][sVal][1]);
                //Then compute find the exact position in the destination histogram and increment
                tmp = subStartPosDst[sVal]  + QUANTIZATION_TABLE[offsetDst][sVal][0] * (int) sumBinDst + (int) hueBinDst;
                dstHisto[tmp] += srcHisto[i];
            }
        }
        return dstHisto;
    }

    /**
     * Calculates log(x)/log(2)
     * @param x the value
     * @return log(x)/log(2)
     */
    private static double log2(int x) {
        return Math.log(x) / Math.log(2);
    }

    /**
     * Checks if the given image is this descriptor color space and, in addition, 
     * if its type is {@link jmr.media.JMRExtendedBufferedImage#TYPE_JMR_4F_INTERLEAVED}.
     * 
     * @param im the image to be checked
     * @return <tt>true</tt> if the image is OK
     */
    protected boolean checkImage(JMRExtendedBufferedImage im) {
        boolean color_space_ok = im.getColorModel().getColorSpace().getType() == COLOR_SPACE;       
        boolean image_type_ok = (im.getType() == JMRExtendedBufferedImage.TYPE_JMR_4F_INTERLEAVED);
        return color_space_ok && image_type_ok;
    }

    /**
     * Returns the number of levels of this descriptor.
     * 
     * @return the number of levels
     */
    public int getQuantLevels() {
        return qLevels;
    }

    /**
     * Returns the type of subspace used in this descriptor.
     * 
     * @param diff the 'diff' component in the HMMD color space
     * @return the type of subspace
     */
    private int getSubspace(float diff) {
        if (diff < 7f / 255f) {
            return 0;
        } else if (diff < 21f / 255f) {
            return 1;
        } else if (diff < 61f / 255f) {
            return 2;
        } else if (diff < 111f / 255f) {
            return 3;
        } else {
            return 4;
        }
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
     * Set the histogram values from a given vector of values.
     *
     * @param bHisto a vector of histogram values
     */
    protected void setHisto(byte[] bHisto) {
        if (bHisto != null) {
            if (histo != null) {
                histo = new int[bHisto.length];
            }
            for (int i = 0; i < histo.length; i++) {
                histo[i] = (int) (bHisto[i] & 0xFF);
            }
        }
    }

    /**
     * Returns a string representation of this descriptor .
     *
     * @return a string representation of this descriptor
     */
    @Override
    public String toString() {
        return "MPEG7ColorStructure: " + Arrays.toString(histo);
    }

}
