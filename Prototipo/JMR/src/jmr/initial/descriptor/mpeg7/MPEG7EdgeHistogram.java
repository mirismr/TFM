package jmr.initial.descriptor.mpeg7;

import java.awt.image.Raster;
import jmr.media.JMRExtendedBufferedImage;
import jmr.result.FloatResult;

/**
 * Color Structure Descriptor from MPEG7 standard.
 *
 *<p>
 * The edge histogram descriptor represents the spatial distribution of five types of edges,
 * namely four directional edges and one non-directional edge. Since edges play an important
 * role for image perception, it can retrieve images with similar semantic meaning. Thus, it
 * primarily targets image-to-image matching (by example or by sketch), especially for natural
 * images with non-uniform edge distribution. In this context, the image retrieval performance
 * can be significantly improved if the edge histogram descriptor is combined with other
 * Descriptors such as the color histogram descriptor. Besides, the best retrieval performances
 * considering this descriptor alone are obtained by using the semi-global and the global
 * histograms generated directly from the edge histogram descriptor as well as the local ones
 * for the matching process.
 * <a style="font-size:small;font-style:italic" href="http://www.chiariglione.org/mpeg/standards/mpeg-7">
 * Definition from this link</a></p>
 *
 *
 * <p>
 * This class is inspired by EdgeHistogramImplementation.java
 * from  <a href="http://www.semanticmetadata.net">Caliph Emir project</a>
 * </p>
 *
 * @author RAT Benoit 
 * (<a href="http://ivrg.epfl.ch" target="about_blank">IVRG-LCAV-EPFL</a> 
 *  <a href="http://decsai.ugr.es/vip" target="about_blank">VIP-DECSAI-UGR</a>)
 * @version 1.0
 * @since 23 nov. 07
 *
 */
public class MPEG7EdgeHistogram extends MPEG7TextureDescriptor {

  /**  Constant used in case there is no edge in the image.  */
  public static final int EDGE_TYPE_NONE = -1;

  /** Constant used in case there is a vertical edge. */
  public static final int EDGE_TYPE_VERT = 0;

  /** Constant used in case there is a horizontal edge. */
  public static final int EDGE_TYPE_HORI = 1;

  /** Constant used in case there is a diagonal of 45 degree. */
  public static final int EDGE_TYPE_45DEG = 2;

  /** Constant used in case there is a digaonal of 135 degree. */
  public static final int EDGE_TYPE_135DEG = 3;

  /** Constant used in case there is no directional edge. */
  public static final int EDGE_TYPE_ALLDIR = 4;

  /**  If the number of edge is less than 11 we don't count it as an edge.   */
  protected int treshold = 11;

  /** Number of block in the image (this does not correspond to subimage) */
  protected int numBlock = 1100;

  /** <b>Final feature vector</b> set as the histogram for (16 subimage x 5 edges directions bins = 80 bins) */
  protected int[] histo = new int[80];

  /**
   * Quantify to fit on 3 bits by bins. Each column represent the corresponding it's associated
   * value on 3 bits. And each row represent the quantization for a type of edge (vert,hori,45,135,all)
   * @see #histoQuanti()
   */
  private static double[][] QuantTable = {
          {0.010867, 0.057915, 0.099526, 0.144849, 0.195573, 0.260504, 0.358031, 0.530128},
          {0.012266, 0.069934, 0.125879, 0.182307, 0.243396, 0.314563, 0.411728, 0.564319},
          {0.004193, 0.025852, 0.046860, 0.068519, 0.093286, 0.123490, 0.161505, 0.228960},
          {0.004174, 0.025924, 0.046232, 0.067163, 0.089655, 0.115391, 0.151904, 0.217745},
          {0.006778, 0.051667, 0.108650, 0.166257, 0.224226, 0.285691, 0.356375, 0.450972}};

  /**
   * The five classical edge filter representing different type of orientation:
   * f[0]:vertical 	-> |
   * f[1]:horizontal	-> --
   * f[2]:45° diago	-> \
   * f[3]:135° diago	-> /
   * f[4]: all direction.
   */
  private static double edge_filter[][] = {
          {1.0, -1.0, 1.0, -1.0},
          {1.0, 1.0, -1.0, -1.0},
          {Math.sqrt(2), 0.0, 0.0, -Math.sqrt(2)},
          {0.0, Math.sqrt(2), -Math.sqrt(2), 0.0},
          {2.0, -2.0, -2.0, 2.0}};

  /** Array, where the bins are saved before they have been quantized. */
  private double Local_Edge_Histogram[] = new double[80];

  //TODO: For the moment we use the static variable to serialize this object to it has no sense.
  private static Raster imRst = null;

  private static final int NOF_SUBIMAGE_ROW = 4;
  private static final int NOF_SUBIMAGE_COL = 4;
  private static final int NOF_EDGE_TYPE = 5;
  private int blockSize = -1;
  private int subImageWidth = -1, subImageHeight = -1;
  protected static final int DEFAULT_THRESHOLD = 11;
  protected static final int DEFAULT_NUM_BLOCKS = 1100;

  /**
   * Default constructor without parameters.
   * 
   */
  public MPEG7EdgeHistogram() {
    this(DEFAULT_THRESHOLD, DEFAULT_NUM_BLOCKS);
  }

  /**
   * Constructor
   *  @param threshold Threshold
   *  @param numBlock Number of blocks
   */
  public MPEG7EdgeHistogram(int threshold, int numBlock) {
    super(true);
    this.treshold = threshold;
    this.numBlock = numBlock;
  }

  /**
   * Constructs the object for an image using the default parameters.
   * @param im	The image
   */
  public MPEG7EdgeHistogram(JMRExtendedBufferedImage im) {
    this();
    calculate(im);
  }

  /**
   * Constructs the object for an image using the specified parameters.
   * @param im	The image
   * @param numBlock Number of blocks
   */
  public MPEG7EdgeHistogram(JMRExtendedBufferedImage im, int treshold, int numBlock) {
    this(treshold, numBlock);
    calculate(im);
  }

  

  /** Compare two EHD a combination of global,semi-global and local matching
   *
   * 
   * <ul>
   * <li>GLOBAL: it is the accumulation of local histograms keeping the edge type. (5 bins)</li>
   * <li>SEMIGLOBAL: We group some sub-images in different subsets 4 rows, 4 columns and 5 grouping neighbor
   * and accumulate each edge type seperatly ((4+4+5=13) x 5 = 65 bins)</li>
   * <li>LOCAL: We perform the l1-norm on the actual sub-image structure ((4*4=16) x 5 = 80 bins)</li>
   * </ul>
   * This conbination give us up to 150 bins where we use the l1-norm for similarity matching.
   *
   *
   * @param desc <code>MPEG7EdgeHistogram</code> object to be compared
   */
  public FloatResult compare(MPEG7EdgeHistogram desc) {
    int[] f1 = this.histo;
    int[] f2 = desc.histo;
    if (f1 == null || f2 == null) return (null);
    float val = 0;
    val += histoL1Norm(f1, f2); //LocalHisto
    val += histoL1Norm(this.getGlobalHisto(), desc.getGlobalHisto());
    val += histoL1Norm(this.getSemiGlobalHisto(), desc.getSemiGlobalHisto());
    //This is how we normalize the value between 0 and 1.
    val /= 8 * (80 + 5 * 5 + 13 * 5);

    return (new FloatResult(val));
  }


  /** Computes the <code>MPEG7EdgeHistogram</code> descriptor for
    * the media given by parameter
    * @param media The media from which the descriptor is calculated
    */
//  public void calculate(Media media) {
//    // The MPEG7EdgeHistogram can be calculated only from JMRExtendedBufferedImage
//    if (media instanceof JMRExtendedBufferedImage) {
//      calculate( (JMRExtendedBufferedImage) media);
//    }
//  }

  /** Computes the edge histogram descriptor for the image given by parameter
   */
  public void calculate(JMRExtendedBufferedImage im) {
    if (!checkImage(im)) {
      im = convertImg(im);
    }
    imRst = im.getData();
    setSubImageSize(im);
    setBlockSize(im);
    for (int j = 0; j < NOF_SUBIMAGE_COL; j++) {
      for (int i = 0; i < NOF_SUBIMAGE_ROW; i++) {
        //TODO: Maybe change the way to do and do not use subimage to extract block because we loose some pixel at each image (height=150 and block=20 so we can do only to pixel 140.)
        subImageExtract(i, j);
      }
    }
    //Quantification of the histograms.
    histoQuanti();
  }

  /**
   * Compute the histogram using the strongest edge type in a block as element to sum.
   *
   * <p>
   * This method iterate over all the block that can be find i		double addVal=1;n this subimage.
   * Then it compute the strongest edge for each block, and add it to the histogram
   * with 80 bins using only the 5 corresponding to this subimage.
   * </p>
   *
   * @param subIndX 	The index in x-dimension of the current sub-image.
   * @param subIndY   The index in y-dimension of the current sub-image.
   */
  private void subImageExtract(int subIndX, int subIndY) {

    int edgeTypeOfBlock = EDGE_TYPE_NONE;
    double countNofBlock = 0;
    double addVal = 1;

    //Find position of the subimage
    int startX = subIndX * subImageWidth;
    int startY = subIndY * subImageHeight;
    int endX = startX + subImageWidth;
    int endY = startY + subImageHeight;

    //Deduce position in the histogram knowing the subimage
    int histoOffset = subIndX * NOF_EDGE_TYPE; //line index
    histoOffset += subIndY * NOF_SUBIMAGE_ROW * NOF_EDGE_TYPE; //row index

    //Iterate over each block in the subimage(subX,subY).
    for (int yBlock = startY; yBlock < endY; yBlock += blockSize) {
      for (int xBlock = startX; xBlock < endX; xBlock += blockSize) {

        addVal = 1;
        //In case the block has a part inside and outside the subimage
        if (xBlock + blockSize > endX) {

          //We compute which proportion is inside and we add it to the histo.
          addVal *= (double) (endX - xBlock) / (double) blockSize;
        }
        if (yBlock + blockSize > endY) {
          addVal *= (double) (endY - yBlock) / (double) blockSize;

        }
        countNofBlock += addVal;
        edgeTypeOfBlock = getEdgeFeature(xBlock, yBlock);
        if (edgeTypeOfBlock != EDGE_TYPE_NONE) {
          Local_Edge_Histogram[histoOffset + edgeTypeOfBlock] += addVal;
        }
      }
    }

    //Normalized this histo by the number of block find in the sub-image(subX,subY)
    for (int k = histoOffset; k < histoOffset + NOF_EDGE_TYPE; k++) {
      Local_Edge_Histogram[k] /= countNofBlock;
    }
  }

  /**
   * Compute different edge types in a block and return the strongest one.
   *
   * <p>
   * This function divide the image-block in a 2x2 superPixels (macroPixels/subBlocks).
   * The value of each superPixel is computed using {@link #getSuperPixelVal(int, int, int, int)}
   * and then convolved by the classical 2x2 edge filter corresponding to each edge type.
   * The strongest edge type found is returned.
   * </p>
   *
   * @param bPosX 	x start position of the actual block inside the actual subimage.
   * @param bPosY 	y start position of the actual block inside the actual subimae.
   * @return e_index  returns the type of the strongest edge found.
   */
  private int getEdgeFeature(int bPosX, int bPosY) {

    double superPix_2x2[] = new double[4];
    for (int k = 0; k < 2 * 2; k++) {
      superPix_2x2[k] = getSuperPixelVal(bPosX, bPosY, k % 2, k / 2);
    }

    double[] strengths = new double[5];
    int e_index = -1;
    double e_max = 0.0;

    //Iterate over the 5 edge types to find the strongest one.
    for (int e = 0; e < NOF_EDGE_TYPE; e++) {
      //Compute it using simple filter on the 2x2 superPixels
      for (int k = 0; k < 4; k++) {
        strengths[e] += superPix_2x2[k] * edge_filter[e][k];
      }
      strengths[e] = 255.0 * Math.abs(strengths[e]); //Value are normalized between [0,1]
      if (strengths[e] > e_max) {
        e_max = strengths[e];
        e_index = e;
      }
    }
    //If the best edge is lower than a treshold we consider that we have no edges in this block.
    if (e_max < treshold) {
      e_index = EDGE_TYPE_NONE;
    }
    return (e_index);
  }

  /**
   * Compute the superPixel value making the average of all the real pixel
   *  brigthness in this image.
   *
   * @param bPosX 	x-position of the actual block.
   * @param bPosY 	y-position of the actual block.
   * @param spIndX 	Index in X of the super pixel. It can take val=[0,1].
   * @param spIndY	Index in Y of the super pixel. It can take val=[0,1].
   * @return			Average value of all the pixels in the super pixel.
   */
  private double getSuperPixelVal(int bPosX, int bPosY, int spIndX, int spIndY) {
    double avgY = 0; //Average brightness call Y.
    float[] pix = new float[3];

    //Find position of the superPixel (an Image-block is divided in 2x2 superPixels)
    int startX = bPosX + spIndX * blockSize / 2;
    int startY = bPosY + spIndY * blockSize / 2;
    int endX = bPosX + (spIndX + 1) * blockSize / 2;
    int endY = bPosY + (spIndY + 1) * blockSize / 2;

    //Sum all the Y value in a superPixel (or subBlock)
    for (int y = startY; y < endY; y++) {
      for (int x = startX; x < endX; x++) {
        try {
          imRst.getPixel(x, y, pix);
        }
        catch (Exception e) {
          //Brutal Mirror Conditioning
          int yM, xM;
          if (y >= imRst.getHeight()) {
            yM = imRst.getHeight() - (y - imRst.getHeight() + 1);
          }
          else {
            yM = y;
          }
          if (x >= imRst.getWidth()) {
            xM = imRst.getWidth() - (x - imRst.getWidth() + 1);
          }
          else {
            xM = x;
          }
          imRst.getPixel(xM, yM, pix);
        }
        avgY += pix[0];
      }
    }

    //	Normalize value to obtain the average Y for the super pixel.
    avgY /= ( (blockSize * blockSize) / 4.0);

    return avgY;
  }

  /**
   * Quantification of the histogram to fit in 3-bit by bins.
   *
   * <p>
   * As the values are concentrated in a small range [0,0.3] we perform a nonlinear quantification
   * using this table to fit each bins on 3 bit [0-7]. The table used to perform this quantification:
   * {@link #QuantTable}
   * </p>
   */
  private void histoQuanti() {
    double iQuantValue = 0;
    int e; //Edge type between [0,4]

    //Iterate on each bins of the histograms
    for (int i = 0; i < 80; i++) {
      e = i % 5;
      //Find the interval using QuantTable and associate a number between [0-7]
      for (int j = 0; j < 8; j++) {
        histo[i] = (byte) j;
        if (j < 7) {
          iQuantValue = (QuantTable[e][j] + QuantTable[e][j + 1]) / 2.0; //Get the boundary value.
        }
        else {
          iQuantValue = 1.0;
        }
        if (Local_Edge_Histogram[i] <= iQuantValue) {
          break;
        }
      }
    }
  }

  private int histoL1Norm(int[] h1, int[] h2) {
    int val = 0;
    for (int i = 0; i < h1.length; i++) {
      val += Math.abs(h1[i] - h2[i]);
    }
    return val;
  }

  private int[] getGlobalHisto() {
    int[] gHisto = new int[5];
    //Build and return the result
    for (int i = 0; i < histo.length; i++) {
      gHisto[i % NOF_EDGE_TYPE] += histo[i];
    }
    for (int i = 0; i < gHisto.length; i++) {
      gHisto[i] *= 5 / 16; //We have build image 16 by 16 but we put a factor of 5 has suggested in MPEG7.
    }
    return gHisto;
  }

  private int[] getSemiGlobalHisto() {
    //Create the 13 (groups) x 5 (edge type) = 65 bins semi-global histo
    int[] sgHisto = new int[65];

    //Create the 13 groups of 4 neighbor sub-images
    int neighbor[][][] = {
                                {{0,0},{0,1},{0,2},{0,3}},  //Row 1		(#01)
                                {{1,0},{1,1},{1,2},{1,3}},  //Row 2		(#02)
                                {{2,0},{2,1},{2,2},{2,3}},  //Row 3	        (#03)
                                {{3,0},{3,1},{3,2},{3,3}},  //Row 4		(#04)
                                {{0,0},{1,0},{2,0},{3,0}},  //Column 1	 	(#05)
                                {{0,1},{1,1},{2,1},{3,1}},  //Column 2	 	(#06)
                                {{0,2},{1,2},{2,2},{3,2}},  //Column 3	 	(#07)
                                {{0,3},{1,3},{2,3},{3,3}},  //Column 4	 	(#08)
                                {{0,0},{0,1},{1,0},{1,1}},  //Top-Left	 	(#09)
                                {{0,2},{0,3},{1,2},{1,3}},  //Top-Rigth 	(#10)
                                {{2,0},{2,1},{3,0},{3,1}},  //Bottom-Left 	(#11)
                                {{2,2},{2,3},{3,2},{3,3}},  //Bottom-Right	(#12)
                                {{1,1},{1,2},{2,1},{2,2}}   //Center 		(#13)
                };

    int histoOffset = 0;
    //Iterate on each semi-global subset
    for (int g = 0; g < neighbor.length; g++) {
      //Iterate on each subimage in a subset
      for (int j = 0; j < neighbor[g].length; j++) {
        histoOffset = neighbor[g][j][0] * NOF_EDGE_TYPE; //OffsetPosition in X
        histoOffset += neighbor[g][j][1] * NOF_EDGE_TYPE * NOF_SUBIMAGE_ROW;
        for (int e = 0; e < NOF_EDGE_TYPE; e++) {
          sgHisto[g * NOF_EDGE_TYPE + e] += histo[e + histoOffset];
        }
      }
    }
    //Finally normalize value between 0 and 7
    for (int i = 0; i < sgHisto.length; i++) {
      sgHisto[i] /= 4; //Because each group consist of 4 subimages
    }
    return sgHisto;
  }

  /**
   * The image is splitted into 16 local regions, each of them is divided into a fixed number
   * of image_blocks, depending on width and height of the image.
   * The size of this image_block is here computed trying to find the best size to fit more or
   * less the 1100 blocks that we wants. However for an image of 800x600 we have a
   * block size of 20 which gave us in total 1200 block on the image.
   *
   * @param im The image from which we compute the blockSize of this descriptor using im.getWidth and im.getHeight.
   */
  private void setBlockSize(JMRExtendedBufferedImage im) {
    if (blockSize < 0) {
      double a = (int) (Math.sqrt( (im.getWidth() * im.getHeight()) / numBlock));
      blockSize = (int) (Math.floor( (a / 2)) * 2);
      if (blockSize == 0) {
        blockSize = 2;
      }
    }
  }

  private void setSubImageSize(JMRExtendedBufferedImage im) {
    subImageWidth = (int) Math.floor( (double) im.getWidth() / NOF_SUBIMAGE_ROW);
    subImageHeight = (int) Math.floor( (double) im.getHeight() /
                                      NOF_SUBIMAGE_COL);
  }

  protected boolean checkImage(JMRExtendedBufferedImage im) {
    boolean ok = checkColorSpace(im.getColorModel().getColorSpace());
    //if(!ok) System.err.println("This image is not encoded in the correct color space");
    ok = ok &&
        (im.getType() == JMRExtendedBufferedImage.TYPE_JMR_4F_INTERLEAVED);
    //if(!ok) System.err.println("This image does not use the correct sample model");
    return ok;
  }

  public boolean isComputed() {
    return (histo != null);
  }

  /*
   * @see es.ugr.siar.ip.desc.VisualDescriptor#multiply(float)
   */
  public void multiply(float factor) {
    for (int i = 0; i < histo.length; i++) {
      histo[i] *= factor;
    }
  }

  /*
   * @see es.ugr.siar.ip.desc.VisualDescriptor#sum(es.ugr.siar.ip.desc.VisualDescriptor)
   */
  public void sum(MPEG7EdgeHistogram desc) {
    if (desc instanceof MPEG7EdgeHistogram) {
      MPEG7EdgeHistogram ehd = (MPEG7EdgeHistogram) desc;
      for (int i = 0; i < histo.length; i++) {
        this.histo[i] += ehd.histo[i];
      }
    }
  }

  public byte[] getByteHisto() {
    byte[] bHisto = null;
    if (isComputed()) {
      bHisto = new byte[histo.length];
      for (int i = 0; i < histo.length; i++) {
        bHisto[i] = (byte) histo[i]; //No special operation because value are between [0-7]
      }
    }
    return bHisto;
  }

  public void setHisto(byte[] bHisto) {
    if (bHisto != null) {
      if (!isComputed()) {
        histo = new int[bHisto.length];
      }
      for (int i = 0; i < histo.length; i++) {
        histo[i] = (int) (bHisto[i] & 0xFF);
      }
    }
  }

    @Override
    public void init(Object media) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object compare(jmr.descriptor.MediaDescriptor descriptor) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
