package jmr.colorspace;

import java.awt.color.*;
import java.awt.Color;

/**
 * The Special Intermediate ColorSpaceJMR (Color Space Java Multimedia Retrieval)
 * contains a serie of tools and the principal method {@link #getInstance(int)}
 *
 * @author SoTiLLo
 *
 * */
public abstract class ColorSpaceJMR extends ColorSpace {

  /**
   * Represents a chromatic zone
   */
  public static final int CHROMATIC_ZONE = 1;

  /**
   * Represents a chromatic zone
   */
  public static final int SEMICHROMATIC_ZONE = 2;

  /**
   * Represents a chromatic zone
   */
  public static final int ACHROMATIC_ZONE = 3;

  /**  Serial ID for serialization in the case that <code>instanceof</code> does not work   */
  protected static final long serialVersionUID = 1L;

  /** YCrCb color spaces defined by {@link ColorSpaceYCbCr}.
   * From the family {@link ColorSpace#TYPE_YCbCr} */
  public static final int CS_YCbCr = ColorSpace.TYPE_YCbCr;

  /** HLS color spaces defined by {@link ColorSpaceHSI}.
   * From the family {@link ColorSpace#TYPE_HLS} */
  public static final int CS_HSI = ColorSpace.TYPE_HLS;

  /** HSV color spaces defined by {@link ColorSpaceHSV}.
   * From the family {@link ColorSpace#TYPE_HSV} */
  public static final int CS_HSV = ColorSpace.TYPE_HSV;

  /** HMMD color spaces defined by {@link ColorSpaceHMMD}.
   * From the family {@link ColorSpace#TYPE_4CLR} */
  public static final int CS_HMMD = ColorSpace.TYPE_4CLR;

  /** L*a*b* color spaces defined by {@link ColorSpaceLab}.
   * From the family {@link ColorSpace#TYPE_Lab} */
  public static final int CS_Lab = ColorSpace.TYPE_Lab;

  /** L*u*v* color spaces defined by {@link ColorSpaceLuv}.
   * From the family {@link ColorSpace#TYPE_Luv} */
  public static final int CS_Luv = ColorSpace.TYPE_Luv;


    public static final int CS_GRAY = ColorSpace.CS_GRAY;
    public static final int CS_RGB = ColorSpace.TYPE_RGB;
    public static final int CS_sRGB = ColorSpace.CS_sRGB;
    public static final int CS_CIEXYZ = ColorSpace.CS_CIEXYZ;
    public static final int CS_LINEAR_RGB = ColorSpace.CS_LINEAR_RGB;


  /** Cache constant during ColorSpace Transformation */
  protected static final double PI2 = Math.PI * 2.0;
  /** Cache constant during ColorSpace Transformation */
  protected static final double PI23 = PI2 / 3.0;
  /** Cache constant during ColorSpace Transformation */
  protected static final double PI43 = PI23 * 2.0;
  /** Cache constant during ColorSpace Transformation */
  protected static final double SQRT3 = Math.sqrt(3.0);
  /** Cache constant during ColorSpace Transformation */
  protected static final double SQRT2 = Math.sqrt(2.0);

  /** Cache constant fot the power value used in XYZ to RGB */
  protected static final double power1 = 1.0 / 2.4;

  /**
   * Constructor of the abstract class ColorSpaceJMR 
   * It is better to use the method {@link #getInstance(int)}
   *
   * @param type 		Which type of color Space
   * @param numCmp	The number of Component
   */
  protected ColorSpaceJMR(int type, int numCmp) {
    super(type, numCmp);

  }

   public abstract int chromaticZone(Color col);

   public abstract float[] chromaticDegree(Color col);


  /**
   * This method can return any type of Instance from
   * the ColorSpace classes.
   *
   * @param ColorSpaceType 	the Type of ColorSpace
   * @return 					a ColorSpace object that is an instance of a
   * herited class (i.e: return new {@link ColorSpaceHSV} )
   * @see <a href="#field_summary">Field Summary</a>
   */
  public static ColorSpace getInstance(int ColorSpaceType) {
    switch (ColorSpaceType) {
      case ColorSpaceJMR.CS_YCbCr:
        return new ColorSpaceYCbCr();
      case ColorSpaceJMR.CS_HSI:
        return new ColorSpaceHSI();
      case ColorSpaceJMR.CS_RGB:
      case ColorSpaceJMR.CS_sRGB:
    	  //return ColorSpace.getInstance(ColorSpaceJMR.CS_sRGB);
    	  return new ColorSpaceRGB();
      case ColorSpaceJMR.CS_HSV:
        return new ColorSpaceHSV();
      case ColorSpaceJMR.CS_HMMD:
        return new ColorSpaceHMMD();
      case ColorSpaceJMR.CS_Lab:
        return new ColorSpaceLab(WhitePoint.getInstance(WhitePoint.WP_TYPE_D50));
      case ColorSpaceJMR.CS_Luv:
        return new ColorSpaceLuv(WhitePoint.getInstance(WhitePoint.WP_TYPE_D50));
      default:
        return ColorSpace.getInstance(ColorSpaceType);
        //return new ICC_ColorSpace(ICC_Profile.getInstance(ColorSpaceType));

    }
  }

  /**
   * This method is used by subclasses to convert XYZ Values to RGB
   * @param 	xyzVec 	float vector of length 3
   * @return 			RGB float vector of length 3 in the same range as XYZ
   * @see java.awt.color.ColorSpace#fromCIEXYZ(float[])
   */
  public float[] fromCIEXYZ(float[] xyzVec) {
    float[] rgbVec = new float[3];
    XYZ2RGB(xyzVec, rgbVec);
    return fromRGB(rgbVec);
  }

  /**
   * This method is used by subclasses to convert a color in this.ColorSpace to XYZ values
   * @param 	csVec 	float vector of length 3
   * @return 			RGB float vector of length 3 in the same range as XYZ
   * @see java.awt.color.ColorSpace#fromCIEXYZ(float[])
   */
  public float[] toCIEXYZ(float[] csVec) {

    float[] rgbVec = toRGB(csVec);
    float[] xyzVec = new float[3];
    RGB2XYZ(rgbVec, xyzVec);

    return xyzVec;
  }

//	/**  Must be defined by subclasses */
//	abstract public float[] fromRGB(float[] arg0);
//	/**  Must be defined by subclasses */
//	abstract public float[] toRGB(float[] arg0);




  /**
   * Compute Euclidean distance dE=[dC0^2 + dC1^2 + dC2^2]^(1/2)
   *
   * @param 	c1 	Color 1 as a vector of length=3
   * @param 	c2 	Color 2 as a vector of length=3
   * @return 		Euclidean Distance
   */
  /*public double getDistance(float[] c1, float[] c2) {
    float d0 = c1[0] - c2[0];
    float d1 = c1[1] - c2[1];
    float d2 = c1[2] - c2[2];

    return Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
  }*/

  /**
   * Tranformation RGB -> XYZ
   * @param 	RGB	 	RGB values in a float vector of length 3
   * @param 	XYZ	 	XYZ values in a float vector of length 3
   */
  static void RGB2XYZ(float[] RGB, float[] XYZ) {
    //my.Debug.printCount("RGB > XYZ : rgb=["+RGB[0]+","+RGB[1]+","+RGB[2]+"];");

    for (int i = 0; i < 3; i++) {
      if (RGB[i] < 0.040449936F) {
        RGB[i] /= 12.92F;
      }
      else {
        RGB[i] = (float) (Math.pow( (RGB[i] + 0.055) / 1.055, 2.4));
      }
    }

    XYZ[0] = 0.436052025f * RGB[0] + 0.385081593f * RGB[1] +
        0.143087414f * RGB[2];
    XYZ[1] = 0.222491598f * RGB[0] + 0.71688606f * RGB[1] +
        0.060621486f * RGB[2];
    XYZ[2] = 0.013929122f * RGB[0] + 0.097097002f * RGB[1] +
        0.71418547f * RGB[2];

  }

  /**
   * Tranformation XYZ -> RGB
   * @param 	XYZ 	XYZ values in a float vector of length 3
   * @param	RGB 	RGB values in a float vector of length 3
   */
  static void XYZ2RGB(float[] XYZ, float[] RGB) {
    //my.Debug.printCount(" - XYZ -> RGB :  xyz=["+XYZ[0]+","+XYZ[1]+","+XYZ[2]+"];");

    RGB[0] = 2.9311227F * XYZ[0] - 1.4111496F * XYZ[1] - 0.6038046F * XYZ[2];
    RGB[1] = -0.87637005F * XYZ[0] + 1.7219844F * XYZ[1] + 0.0502565F * XYZ[2];
    RGB[2] = 0.05038065F * XYZ[0] - 0.187272F * XYZ[1] + 1.280027F * XYZ[2];

    for (int i = 0; i < 3; i++) {
      float v = RGB[i];
      if (v < 0.0F) {
        v = 0.0F;
      }
      if (v < 0.0031308F) {
        RGB[i] = 12.92F * v;
      }
      else {
        if (v > 1.0F) {
          v = 1.0F;
        }
        RGB[i] = (float) (1.055 * Math.pow(v, power1) - 0.055);
      }
    }
  }

  /**
   * Transform a pixel in BGRA order like the one in BufferedImage to RGB pixel
   * @param 	bgra 	BGRA a vector of length 4
   * @return 			RGB a vector of length 3
   */
  static float[] BGRA2RGB(float[] bgra) {
    float[] rgb = new float[3];
    rgb[0] = bgra[2];
    rgb[1] = bgra[1];
    rgb[2] = bgra[0];
    return rgb;
  }

  /**
   * Return the string value of the colorSpaceTypepe
   * @return A String with the name of the Color Space Type
   */
  public String getColorSpaceName() {
    return ColorSpaceJMR.getColorSpaceName(this.getType());
  }

  /**
   * Return the string value of the colorSpaceType
   * @param 	colorSpaceType 	The color space type
   * @return 	A String with the name of the Color Space Type
   */
  public static String getColorSpaceName(int colorSpaceType) {
    switch (colorSpaceType) {
      case CS_HSV:
        return "HSV";
      case CS_Lab:
        return "Lab";
      case CS_Luv:
        return "Luv";
      case CS_YCbCr:
        return "YCbCr";
      case CS_HMMD:
        return "HMMD";
      case CS_HSI:
        return "HSI";
      case CS_CIEXYZ:
        return "CIEXYZ";
      case CS_GRAY:
        return "Gray";
      case CS_sRGB:
        return "sRGB";
      case CS_RGB:
        return "RGB";
      default:
        return "Undefined";
    }
  }

  /**
   * Return the string value of the colorSpaceType
   * @return 	A String with the name of the Color Space Type
   */
  public static String[] getListColorSpaceNames() {

    String[] cad = new String[9];

    cad[0]="HSV";
    cad[1]="HSI";
    cad[2]="HMMD";
    cad[3]="Lab";
    cad[4]="Luv";
    cad[5]="YCbCr";
    cad[6]="RGB";
    cad[7]="CIEXYZ";
    cad[8]="Gray";

    return cad;
  }

  /**
   * Return the int value of the colorSpaceType
   * @param 	name 	The color space name
   * @return 	A int value correspondig to the Color Space Type
   */
  public static int getColorSpaceType(String name) {

    if(name.compareTo("HSV")==0)
      return CS_HSV;

    if(name.compareTo("HSI")==0)
      return CS_HSI;

    if(name.compareTo("Lab")==0)
      return CS_Lab;

    if(name.compareTo("sRGB")==0)
      return CS_sRGB;
    
    //if(name.compareTo("RGB")==0)
    //    return CS_RGB;

    if(name.compareTo("Luv")==0)
      return CS_Luv;

    if(name.compareTo("HMMD")==0)
      return CS_HMMD;

    if(name.compareTo("CIEXYZ")==0)
      return CS_CIEXYZ;

    if(name.compareTo("Gray")==0)
      return CS_GRAY;

    if(name.compareTo("YCbCr")==0)
      return CS_YCbCr;
    return 0;
  }





  /**
   * Extract the name of the component knowing its index
   * @param 	idx		index of the component
   * @return 			A String with the name the component
   */
  public String getName(int idx) {
    int numCmp = getNumComponents();
    if (idx < numCmp) {
      return "Cmp" + idx;
    }
    else if (idx == numCmp) {
      return "Alpha";
    }
    else {
      return "Unknown";
    }
  }

  /**
   * Extract the name of the component knowing its index and the ColorSpace.
   * This method is static in order to obtain a name for component that also
   * come from the ColorSpace instance and not a subclass of ColorSpaceJMR.
   *
   * @param 	idx 	index of the component
   * @param 	cS 		the ColorSpace used
   * @return 			A String with the name the component
   * @see #getName(int)
   */
  public static String getName(ColorSpace cS, int idx) {
    if (cS instanceof ColorSpaceJMR) {
      return cS.getName(idx);
    }
    else {
      int numCmp = cS.getNumComponents();
      if (idx < numCmp) {
        return "Cmp" + idx;
      }
      else if (idx == numCmp) {
        return "Alpha";
      }
      else {
        return "Unknown";
      }
    }
  }

  /**
   * Returns the maximum normalized color component value for the
   * specified component.  The default implementation in this abstract
   * class returns 1.0 for all components.  Subclasses should override
   * this method if necessary.
   *
   * @param component the component index
   * @return the maximum normalized component value
   * @throws IllegalArgumentException if component is less than 0 or
   *         greater than numComponents - 1
   */
  public float getMaxValue(int component) {
    return super.getMaxValue(component);
  }

  /**
   * Returns the minimum normalized color component value for the
   * specified component.  The default implementation in this abstract
   * class returns 0.0 for all components.  Subclasses should override
   * this method if necessary.
   *
   * @param component the component index
   * @return the minimum normalized component value
   * @throws IllegalArgumentException if component is less than 0 or
   *         greater than numComponents - 1
   */
  public float getMinValue(int component) {
    return super.getMinValue(component);
  }

  /**
   * Simple method that permit to convert an object into a string to see all parameters in case of debug
   * @see java.lang.Object#toString()
   */
  public String toString() {
    String str = getColorSpaceName() + " Color Space: (";
    for (int i = 0; i < this.getNumComponents(); i++) {
      str += this.getName(i) + "=[" + this.getMinValue(i) + "," +
          this.getMaxValue(i) + "];";
    }
    str += ")";
    return str;
  }
}
