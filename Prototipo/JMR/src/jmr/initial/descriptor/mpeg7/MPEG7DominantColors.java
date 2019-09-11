package jmr.initial.descriptor.mpeg7;

import jmr.result.JMRResult;
import jmr.result.FloatResult;
import jmr.descriptor.MediaDescriptor;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.util.ArrayList;
import jmr.colorspace.ColorConvertTools;
import jmr.colorspace.ColorSpaceJMR;
import jmr.media.JMRExtendedBufferedImage;
import jmr.tools.JMRImageTools;

/**
 * The MPG7 Dominant Color Descriptor (DCD)
 *
 * @author Jose Manuel Soto Hidalgo (jmsoto@uco.es)
 * @author Jesús Chamorro Martínez (jesus@decsai.ugr.es)
 */
public class MPEG7DominantColors implements MediaDescriptor<BufferedImage> {
    /**
     * The source media of this descriptor
     */
    protected transient BufferedImage source = null;
    /**
     * A vector of <code>MPEG7SingleDominatColor</code>
     */
    private ArrayList<MPEG7SingleDominatColor> dominantColors;

    /**
     * Mean distance threshold between clusters. 
     */
    private float ro;
    /**
     * Processing Pixels in clustering calculate. If L[x,y] == 0 means pixel
     * (x,y) is not processed yet
     */
    private int[][] L;
    /**
     * A reference cluster. If couple[x,y]=2 means cluster 2 is the closest
     * cluster to pixel(x,y)
     */
    private int[][] couple;
    /**
     * A reference matrix. The i,j pixel corresponds to the cluster
     * referenceMatrix[i,j].
     */
    private int[][] referenceMatrix;
    /**
     * An instance of ColorSpace that descriptor uses
     */
    private ColorSpace cs;
    /**
     * An array with the color component corresponding to a pixel
     */
    private float[] pixel;
    /**
     * A vector of <code>MPEG7SingleDominatColor</code>
     */
    private ArrayList<jmr.descriptor.ColorData> dcd;

    /**
     * The minimum percentage to consider a cluster as a cluster
     */
    private float minPercentage;

    private FloatResult thresholdDist;
    private int distanceType;

    public static final int COMPARE_ALL_TO_ALL = 1;
    public static final int COMPARE_CONTAIN_COLORS = 2;
    public static final int COMPARE_ONLY_SAME_NUMBER_OF_COLORS = 3;
    public static final int COMPARE_CONTAIN_COLORS_PERCENTAJE = 4;
    public static final int COMPARE_ONLY_COLORS_PERCENTAJE = 5;
    public static int DEFAULT_COMPARISON = COMPARE_CONTAIN_COLORS;

    /**
     * Represents the default color space
     */
    public static int DEFAULT_CS = ColorSpaceJMR.CS_sRGB;

    /**
     * Represents a default value for parameter ro
     */
    public static float DEFAULT_RO = 0.3f;

    /**
     * Represents a default value for parameter minPercentage
     */
    public static float DEFAULT_MIN_PERCENTAGE = 0.01f;

    private float td = Float.MAX_VALUE;


  /**
   * Constructs an empty DCD descriptor with the default values for parameters
   * <code>ro</code> (the distance threshold for clusters spliting) and the minimum 
   * size of a cluster (measured in %).
   */
  public MPEG7DominantColors() {
    this.ro = DEFAULT_RO;
    this.minPercentage = DEFAULT_MIN_PERCENTAGE;
    this.cs = ColorSpaceJMR.getInstance(DEFAULT_CS);
  }

  /**
   * Constructs an empty DCD descriptor
   * 
   * @param ro the distance threshold for clusters spliting
   * @param minPercentaje the minimum size of a cluster to take it into account 
   * (measured in %)
   */
  public MPEG7DominantColors(float ro, float minPercentaje) {
    this.ro = ro;
    this.minPercentage = minPercentaje;
    this.cs = ColorSpaceJMR.getInstance(DEFAULT_CS);
  }

  /**
   * Constructs the DCD descriptor for the given image
   * 
   * @param img the image 
   */
  public MPEG7DominantColors(BufferedImage img) {
    this();
    this.source = img;
    this.init(img);
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
  
    public float getMinPercentage() {
        return minPercentage;
    }

    public void setMinPercentage(float minPercentage) {
        this.minPercentage = minPercentage;
    }
  
  /** Compare this <code>MPEG7DominantColors</code> obtect with the
   * <code>MPEG7DominantColors</code> given by parameter
   * <p> This method is valid only for <code>MPEG7DominantColors</code>
   *  media descriptors
   * @param mediaDescriptor MediaDescriptor object to be compared
   * @see #compare(MPEG7DominantColors desc)
   * @return The difference between descriptors
   */
  @Override
  public JMRResult compare(MediaDescriptor mediaDescriptor) {
    // Only MPEG7DominantColors objects can be compared
    if (! (mediaDescriptor instanceof MPEG7DominantColors)) {
      return (null);
    }
    return (compare( (MPEG7DominantColors) mediaDescriptor));
  }

  /** Compare this <code>MPEG7DominantColors</code> obtect with the
   * <code>MPEG7DominantColors</code> given by parameter using the
   * default distance (#DEFAULT_DISTANCE)
   * @param desc <code>MPEG7DominantColors</code> object to be compared
   * @return The difference between descriptors as float result
   */
  public FloatResult compare(MPEG7DominantColors desc) {
    return compare(desc, DEFAULT_COMPARISON);
  }

  /** Compare this <code>MPEG7DominantColors</code> obtect with the
   * <code>MPEG7DominantColors</code> given by parameter using the distance
   * indicated by parameter
   * @param desc <code>MPEG7DominantColors</code> object to be compared
   * @param typeDistance Distance to be applied
   * @return The difference between descriptors as float result
   */
    public FloatResult compare(MPEG7DominantColors desc, int typeDistance) {
        switch (typeDistance) {
            case COMPARE_ALL_TO_ALL:
                return compareAllToAll(desc);
            case COMPARE_CONTAIN_COLORS:
                return compareContainColors(desc);
            case COMPARE_ONLY_SAME_NUMBER_OF_COLORS:
                return compareOnlySameNumberOfColors(desc);
            default:
                return null;
        }
  }

  
  


  
  
  
  /*******************************************************************
   * 
   * 
   * 
   */
  
  
  /**
	 * Computes the <code>MPEG7DominantColors</code> descriptor for the media
	 * given by parameter
	 * 
	 * @param media
	 *            The media from which the descriptor is calculated
	 */
//    @Override
//	public void init(Object media) {
//		// The MPEG7DominantColors only can be calculated from
//		// JMRExtendedBufferedImage
//		if (media instanceof JMRExtendedBufferedImage) {
//			init((JMRExtendedBufferedImage) media);
//		}
//	}
	

	/**
	 * Computes the structured histogram descriptor for the image given by
	 * parameter
	 * 
	 * @param img
	 *            The image from which the descriptor is calculated
	 */
	public void init(JMRExtendedBufferedImage img) {		
                JMRExtendedBufferedImage imgSource = img;
		// We convert the JMRBufferedImage to the same MPEGDominantColor
		// descriptor color space
                
//		imgSource = ColorConvertTools.convertColor(img, this.colorSpaceType);
		imgSource = ColorConvertTools.convertColor(img, DEFAULT_CS);

		// r is the Raster of source JMRBufferedImage
		Raster r = imgSource.getData();
		// pixel stores a whole pixel
		pixel = new float[r.getNumBands()];
		batchelor_Wilkins(r);
	}

        
    @Override
        public void init(BufferedImage img) {		
            JMRExtendedBufferedImage imgJMR = new JMRExtendedBufferedImage(img);
            this.init(imgJMR);
	}
        
        
        public BufferedImage calculate(BufferedImage img, boolean resize) {
            BufferedImage imgIn = img;
            if(resize){
                double rate = ((float)img.getHeight())/img.getWidth();
                if(img.getWidth()>200){                
                    imgIn = JMRImageTools.resize(img,new Dimension(200,(int)(200.0*rate)));
                }
            }          
            this.init(imgIn);
            return imgIn;
	}
        

	private jmr.descriptor.ColorData getColorData(Raster r, int x, int y) {
		r.getPixel(x, y, pixel);

		// creates a ColorData 'c' with the array pixel
		// IMPORTANT... the components array must to be in the 0.0 to 1.0
		// range...
		// in the Color constructor so the components in any color space must to
		// be normalized
		// we need to normalize to the 0.0 to 1.0 range
		for (int i = 0; i < pixel.length; i++)
			pixel[i] = ColorConvertTools.domainTransform(pixel[i], cs.getMinValue(i), cs.getMaxValue(i), 0.0f, 1.0f);

		return (new jmr.descriptor.ColorData(new Color(cs, pixel, 1.0f)));
		
		//new ColorData(new Color(this.getRGB(x, y)));
	}

	private void batchelor_Wilkins(Raster r) {
		boolean terminar = false;
		Point n;

		L = new int[r.getWidth()][r.getHeight()];
		couple = new int[r.getWidth()][r.getHeight()];
		referenceMatrix = new int[r.getWidth()][r.getHeight()];

		initialitation(r);
		do {
			createGrouping(r);
			n = theMostDiferentPixel(r);
			if (thresholdDist.getValue() > calculateThreshold(ro)) {
				dcd.add(getColorData(r, n.x, n.y));
				referenceMatrix[n.x][n.y] = dcd.size() - 1;
				L[n.x][n.y] = 1;
				terminar = false;
			} else
				terminar = true;
		} while (!terminar);

		putIntoGroups(r);
	}

	private void initialitation(Raster r) {
		Point m;

		dcd = new ArrayList();

		// z1<-x1
		dcd.add(getColorData(r, 0, 0));

		// s1<-x1
		referenceMatrix[0][0] = dcd.size() - 1;

		// L<-L-{x1}
		L[0][0] = 1;

		m = theMostDiferentPixel(r);

		// z2<-Xm
		dcd.add(getColorData(r, m.x, m.y));

		// S2<-{Xm}
		referenceMatrix[m.x][m.y] = dcd.size() - 1;

		// L<-L-{Xm}
		L[m.x][m.y] = 1;
	}

	/**
	 * Calculates the most distance pixel
	 * 
	 * @param r
	 *            raster with image data
	 * @return The pixel coords
	 */
	private Point theMostDiferentPixel(Raster r) {
		Point n = null;
		FloatResult maxDist = new FloatResult(-1);
		FloatResult currentDist;

		for (int i = 0; i < r.getWidth(); i++) {
			for (int j = 0; j < r.getHeight(); j++) {
				if (L[i][j] == 0) {

					currentDist = (FloatResult) ((jmr.descriptor.ColorData) dcd.get(couple[i][j])).compare(getColorData(r, i, j));

					if (currentDist.getValue() > maxDist.getValue()) {
						maxDist = currentDist;
						n = new Point(i, j);
					}
				}
			}
		}
		thresholdDist = maxDist;
		return n;
	}

	private void updateAccordingToPercentaje(float total, int[] numElem,
			double p) {
		int tamano = dcd.size();
		for (int i = 0; i < tamano; i++) {
			if ((double) numElem[i] / total < p) {
				// dcd.removeElementAt(i);
				// dcd.setElementAt(null, i);
				dcd.set(i, null);
				// numElem[i] = -1000;
				// L[posDCD[i]]=0;
			}
		}
		for (int i = 0; i < dcd.size(); i++)
			if (dcd.get(i) == null) {
				dcd.remove(i);
				i--;
			}
	}

	private void createGrouping(Raster r) {
		int m;

		for (int i = 0; i < r.getWidth(); i++) {
			for (int j = 0; j < r.getHeight(); j++) {
				if (L[i][j] == 0) {
					m = closerGroup(getColorData(r, i, j), dcd);
					couple[i][j] = m;
				}
			}
		}
	}

	private int closerGroup(jmr.descriptor.ColorData x, ArrayList dcd) {
		int m = 0;
		FloatResult minDist = new FloatResult(Float.MAX_VALUE);
		FloatResult currentDist;
		for (int i = 0; i < dcd.size(); i++) {
			currentDist = (FloatResult) ((jmr.descriptor.ColorData) dcd.get(i)).compare(x);
			if (currentDist.getValue() < minDist.getValue()) {
				minDist = currentDist;
				m = i;
			}
		}
		return m;
	}

	private float calculateThreshold(float alfa) {
		 //FloatResult sum = new FloatResult(0);
		float sum = 0;
		for (int i = 0; i < dcd.size() - 1; i++){
			FloatResult fr =(FloatResult) ((jmr.descriptor.ColorData) dcd.get(i)).compare((jmr.descriptor.ColorData) dcd.get(i + 1));
			sum += fr.getValue(); 
		}
		return (alfa * (sum / (float) (dcd.size() - 1)));
	}

	private void putIntoGroups(Raster r) {
		int m;

		if (minPercentage != 0) { // If minimum percentage of the cluster is
									// considered
			int[] numElem = new int[dcd.size()];
			for (int i = 0; i < r.getWidth(); i++) {
				for (int j = 0; j < r.getHeight(); j++) {
					if (L[i][j] == 0) {
						m = closerGroup(getColorData(r, i, j), dcd);
						numElem[m]++;
					}
				}
			}
			updateAccordingToPercentaje(r.getHeight() * r.getWidth(), numElem,
					minPercentage);
			for (int i = 0; i < r.getWidth(); i++) {
				for (int j = 0; j < r.getHeight(); j++) {
					m = closerGroup(getColorData(r, i, j), dcd);
					referenceMatrix[i][j] = m;
				}
			}
		} else {
			for (int i = 0; i < r.getWidth(); i++) {
				for (int j = 0; j < r.getHeight(); j++) {
					if (L[i][j] == 0) {
						m = closerGroup(getColorData(r, i, j), dcd);
						referenceMatrix[i][j] = m;
					}
				}
			}
		}

		dominantColors = calculateCentroids(r, referenceMatrix);
		// varianza = Distorsion(matriz,matrizRef, dcd, A-1);
	}

	private ArrayList calculateCentroids(Raster cluster, int[][] mRef) {
		ArrayList c;
		float PI = (float) Math.PI;
		float PI2 = PI * 2;
		// float maxC1 = cs.getMaxValue(0);
		int numComponents = cs.getNumComponents();

		int[] num = new int[dcd.size()];
		float[][] centroid = new float[dcd.size()][numComponents];
		float[] meanColorComponents = new float[numComponents];
		float[][] meanAngle = new float[dcd.size()][2];

		// almacenamos el n�mero de elementos del cluster correspondiente
		c = new ArrayList<MPEG7SingleDominatColor>();

		// calculamos el centroide (centro de masas-media) del cluster
		for (int i = 0; i < cluster.getWidth(); i++) {
			for (int j = 0; j < cluster.getHeight(); j++) {
				// vamos haciendo el sumatorio para cada centroide
				if (cs.getType() == ColorSpaceJMR.CS_HSI
						|| cs.getType() == ColorSpaceJMR.CS_HSV) {
					float h = (getColorData(cluster, i, j)).getColor()
							.getColorComponents(null)[0];
					meanAngle[mRef[i][j]][0] += Math.sin((double) h * PI2);
					meanAngle[mRef[i][j]][1] += Math.cos((double) h * PI2);
					centroid[mRef[i][j]][1] += (getColorData(cluster, i, j))
							.getColor().getColorComponents(null)[1];
					centroid[mRef[i][j]][2] += (getColorData(cluster, i, j))
							.getColor().getColorComponents(null)[2];
				} else { // If not HSI or HSV color space
					for (int k = 0; k < numComponents; k++)
						centroid[mRef[i][j]][k] += (getColorData(cluster, i, j))
								.getColor().getColorComponents(null)[k];
				}
				// number of elements of each cluster
				num[mRef[i][j]]++;
			}
		}

		for (int i = 0; i < dcd.size(); i++) {
			if (cs.getType() == ColorSpaceJMR.CS_HSI
					|| cs.getType() == ColorSpaceJMR.CS_HSV) {
				float c1y = meanAngle[i][0] / num[i];
				float c1x = meanAngle[i][1] / num[i];

				float c1 = (float) Math.atan(c1y / c1x);
				// centroid[i][0]=(float) Math.atan2(c1x,c1y)/(float)PI;
				// if(centroid[i][0]<0)
				// centroid[i][0] += PI2;

				if (c1y >= 0) {
					if (c1x >= 0)
						c1 /= PI2; // 1er cuadrante
					else
						c1 = (PI2 / 2.0f + c1) / PI2; // 2� cuadrante
				} else {
					if (c1x >= 0)
						c1 = (PI2 + c1) / PI2; // 4� cuadrante
					else
						c1 = (PI2 / 2.0f + c1) / PI2; // 3er cuadrante
				}
				centroid[i][0] = num[i] * c1;
			}

			for (int k = 0; k < numComponents; k++)
				meanColorComponents[k] = (centroid[i][k] / num[i]);

			c.add(new MPEG7SingleDominatColor(new Color(cs,
					meanColorComponents, 1.0f), 0, num[i]
					/ (float) (cluster.getWidth() * cluster.getHeight()), 0));
		}

		return c;
	}


	private FloatResult compareContainColors(MPEG7DominantColors desc) {
		// TODO Auto-generated method stub
		int i,j,numColores=0;
	    double sum=0;
	    double distMin=Double.MAX_VALUE;
	    FloatResult dist;
	    
	    MPEG7DominantColors c1 = this;
		MPEG7DominantColors c2 = desc;

	    // si c1 es mayor que c2
	    if(c1.getNumberOfDominantColors() >= c2.getNumberOfDominantColors()){
	      for (i = 0; i < c1.getNumberOfDominantColors(); i++){
	        distMin=Double.MAX_VALUE;
	        for (j = 0; j < c2.getNumberOfDominantColors(); j++) {
	          //dist = c.getDistancia( (MiColor) c1.elementAt(i),(MiColor) c2.elementAt(j));
	          dist = c1.getDominantColor(i).compare(c2.getDominantColor(j));
	            if (dist.toFloat() < distMin)
	              distMin = dist.toFloat();
	        }
	        if(distMin<td){
	          numColores++;
	          sum += distMin;
	        }
	      }
	      //sum /= c1.size();
	      if(numColores==c1.getNumberOfDominantColors())
	        sum/=numColores;
	      else
	        sum=1;
	    }
	    else{ // si c2 es mayor que c1
	      for (i = 0; i < c2.getNumberOfDominantColors(); i++) {
	        distMin = Double.MAX_VALUE;
	        for (j = 0; j < c1.getNumberOfDominantColors(); j++) {
	        	dist = c2.getDominantColor(i).compare(c1.getDominantColor(j));
	
	        	if (dist.toFloat() < distMin)
		              distMin = dist.toFloat();
	          
	        }
	        if (distMin < td) {
	          numColores++;
	          sum += distMin;
	        }
	      }
	      if (numColores == c1.getNumberOfDominantColors())
	        sum /= numColores;
              else {
	        sum = 1;
                //¿Si el número de colores del segundo es mayor, considera 1 la distancia?  
              }
	    }

	    return (new FloatResult((float)sum));
	  
	}

	private FloatResult compareOnlySameNumberOfColors(MPEG7DominantColors desc) {	
            int i,j,numColores=0,colMin=-1;
	    double sum=0;
	    double distMin=Double.MAX_VALUE;
	    FloatResult dist;

	    MPEG7DominantColors c1 = this;
		MPEG7DominantColors c2 = desc;

	    // si c1 es igual que c2 -> mismo n�mero de colores
		if (c1.getNumberOfDominantColors() == c2.getNumberOfDominantColors()){
	      boolean[] selec = new boolean[c2.getNumberOfDominantColors()];
	      for(i=0;i<c2.getNumberOfDominantColors();i++)
	        selec[i]=false;
	      for (i = 0; i < c1.getNumberOfDominantColors(); i++){
	        distMin=Double.MAX_VALUE;
	        for (j = 0; j < c2.getNumberOfDominantColors(); j++) {
	        	dist = c1.getDominantColor(i).compare(c2.getDominantColor(j));
	            
	            if (dist.toFloat() < distMin){
	              distMin = dist.toFloat();
	              colMin = j;
	            }
	        }
	        if(distMin<td && !selec[colMin]){
	          numColores++;
	          sum += distMin;
	          selec[colMin] = true;
	        }
	      }
	      if(numColores==c1.getNumberOfDominantColors())
	        sum/=numColores;
	      else
	        sum=1;
	    }
	    else
	      sum=1;
	    return (new FloatResult((float)sum));
	}

	/**
	 * Compare this <code>MPEG7DominantColors</code> object with the
	 * <code>MPEG7DominantColors</code> given by parameter. To calculate the
	 * distance....
	 * 
	 * @param desc
	 *            <code>MPEG7DominantColors</code> object to be compared
	 * @return The difference between descriptors as float result
	 */
	private FloatResult compareAllToAll(MPEG7DominantColors desc) {
		// TODO
		return (null);
	}

    /**
     * ��Inserts the specified <code>MPEG7SingleDominatColor</code> at the
     * specified position in the dominant color vector. Shifts the element
     * currently at that position (if any) and any subsequent elements to the
     * right (adds one to their indices).
     *
     * @param index Index at which the specified element is to be inserted
     * @param dColor The dominant color to be inserted
     */
    public void addDominantColor(int index, MPEG7SingleDominatColor dColor) {
        dominantColors.add(index, dColor);
    }

    /**
     * Appends the specified <code>MPEG7SingleDominatColor</code> to the end of
     * the dominant color vector
     *
     * @param dColor The dominant color to be inserted
     * @return <code>True</code> if the color is appended or <code>False</code>
     * otherwise
     */
    public boolean addDominantColor(MPEG7SingleDominatColor dColor) {
        return dominantColors.add(dColor);
    }

    /**
     * Returns the element at the specified position in the dominant color
     * vector
     *
     * @param index Index of element to be returned
     * @return Returns the element at the specified position in the dominant
     * color vector
     */
    public MPEG7SingleDominatColor getDominantColor(int index) {
        return (MPEG7SingleDominatColor) (dominantColors.get(index));
    }

    public int getNumberOfDominantColors() {
        return dominantColors.size();
    }

    public ArrayList<MPEG7SingleDominatColor> getDominantColors() {
        return dominantColors;
    }

    /**
     * Nested class representing a single dominant color
     */
    public class MPEG7SingleDominatColor extends jmr.descriptor.ColorData {

        /**
         * The variance of the dominant color
         */
        float variance;

        /**
         * The percentage of the dominant color
         */
        float percentage;

        /**
         * The spatial coherence of the dominant color
         */
        float spatialCoherence;

        /**
         * Represents a non calculated variance data
         */
        public static final float NO_VARIANCE = 0.0f;

        /**
         * Represents a non calculated variance data
         */
        public static final float NO_PERCENTAGE = 0.0f;

        /**
         * Represents a non calculated variance data
         */
        public static final float NO_SPATIAL_COHERENCE = 0.0f;

        /**
         * Constructs a <code>MPEG7SingleDominatColor</code> from a color
         *
         * @param color The data color
         */
        public MPEG7SingleDominatColor(Color color) {
            this(color, NO_VARIANCE, NO_PERCENTAGE, NO_SPATIAL_COHERENCE);
        }

        /**
         * Constructs a <code>MPEG7SingleDominatColor</code> from a color with
         * the specified properties
         *
         * @param color The data color
         * @param variance The variance of the color
         * @param percentage The percent of the color
         * @param spatialCoherence The spatial coherence of the color
         */
        public MPEG7SingleDominatColor(Color color, float variance, float percentage, float spatialCoherence) {
            super(color);
            this.variance = variance;
            this.percentage = percentage;
            this.spatialCoherence = spatialCoherence;
        }

        public float getVariance() {
            return variance;
        }

        public float getPercentage() {
            return percentage;
        }

        public float getSpatialCoherence() {
            return spatialCoherence;
        }

        public Color getColorData() {
            return this.colorData;
        }

    } // End of inner class

  
}
