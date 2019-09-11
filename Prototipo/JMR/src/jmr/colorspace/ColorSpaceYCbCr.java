/**
 *
 */
package jmr.colorspace;

import java.awt.Color;

/**
 * YCbCr or Y'CbCr is a family of opponent color spaces mainly used in video systems.
 *
 * <p>
 * Y' is the luma component and Cb and Cr are the blue and red chroma components.
 * For more details about the quantification and extremum value of this space
 * see {@link #fromRGB(float[])}. </p>
 *
 *
 *
 * @author SoTiLLo
 * @version 1.0
 * @since 29 nov. 07
 *
 */
public class ColorSpaceYCbCr extends ColorSpaceJMR {

	private static final long serialVersionUID = 1L;


	 /**
     * Constructs an instance of this class with <code>type</code>
     * {@link ColorSpaceJMR#CS_YCbCr}, 3 components, and preferred
     * intermediary space sRGB.
     */

	protected ColorSpaceYCbCr() {
		super(ColorSpaceJMR.CS_YCbCr,3);
	}


	/**
	 * fromRGB : transform a RGB pixel in a YCrCb pixel.
	 *
	 * <code>
	 * YCbCr (ITU-R BT.601)										<br>
	 * ========================================================	<br>
	 * Y' = + 0.299 * R' + 0.587 * G' + 0.114 * B' 				<br>
	 * Cb = - 0.168736 * R' - 0.331264 * G' + 0.5 * B'			<br>
	 * Cr = + 0.5 * R' - 0.418688 * G' - 0.081312 * B'			<br>
	 * ........................................................	<br>
	 * R', G', B' in [0; 1]										<br>
	 * Y' in [0; 1] and Cb,Cr [-0.5; 0.5]							<br>
	 * </code>
	 *
	 * @param	rgbVec	a vector (length=3) with rgb values normalized R,G,B=[0,1]
	 * @return 			a vector (length=3) with hsv values normalized Y,Cr,Cb=[0,1]
	 *
	 *
	 * @see <a href="http://www.f4.fhtw-berlin.de/~barthel/ImageJ/ColorInspector">ColorInspector 3D v2.0</a>
	 * @see <a href="http://en.wikipedia.org/wiki/Ycbcr">Wikipedia</a>
	 */
	public float[] fromRGB(float[] rgbVec) {

//		my.Debug.printCount("fromRGB (RGB > YCbCr):");
//		my.Debug.printCount(" - RGB input :  rgb=["+rgbVec[0]+","+rgbVec[1]+","+rgbVec[2]+"];");

		float[] ybrVec = new float[3] ;
		float r = rgbVec[0] ;
		float g = rgbVec[1] ;
		float b = rgbVec[2] ;

		ybrVec[0] = ( 0.299f   * r + 0.587f   * g + 0.114f   * b);
		ybrVec[1] = (-0.16874f * r - 0.33126f * g + 0.50000f * b);
		ybrVec[2] = ( 0.50000f * r - 0.41869f * g - 0.08131f * b);


//  Method with input in [0,1] and output in [0,255]
//      ybrVec[0] =  77.0f   * r + 150.0f   * g + 29.0f   * b;
//		ybrVec[1] = -44.0f * r - 87.0f * g + 131.0f * b +128;
//		ybrVec[2] =  131.0f * r - 110f * g - 21f * b +128;
//

//		my.Debug.printCount(" - YCbCr output:  YCbCr=["+ybrVec[0]+","+ybrVec[1]+","+ybrVec[2]+"];");

		return ybrVec ;
	}


	/**
	 * transform a YCrCb pixel to a RGB pixel.
	 *
	 * @param 	ybrVec	a vector (length=3) with YCbCr values normalized  Y in [0; 1] and Cb,Cr [-0.5; 0.5]
	 * @return 			a vector (length=3) with rgb values normalized R,G,B=[0,1]
	 *
	 * @see <a href="http://www.f4.fhtw-berlin.de/~barthel/ImageJ/ColorInspector">ColorInspector 3D v2.0</a>
	 * @see <a href="http://en.wikipedia.org/wiki/Ycbcr">Wikipedia</a>
	 */
	public float[] toRGB(float[] ybrVec) {
//		my.Debug.printCount("toRGB (HSI -> RGB)");
//		my.Debug.printCount(" - HSI input: 	hsi=["+ybrVec[0]+","+ybrVec[1]+","+ybrVec[2]+"];");

		float Y = ybrVec[0] ;
		float Cb = ybrVec[1] ;
		float Cr = ybrVec[2] ;
		float[] rgb = new float[3] ;

		rgb[0] = Y + 1.4020f*Cr;
		rgb[1] = Y - 0.3441f*Cb - 0.7141f*Cr;
		rgb[2] = Y + 1.7720f*Cb;

//		my.Debug.printCount(" - RGB : 	rgb=["+rgb[0]+","+rgb[1]+","+rgb[2]+"];");
		return rgb ;

	}


	public float getMaxValue(int cmp) {
		switch(cmp) {
		case 0:
			return (float)1.0f;
		case 1:
			return 0.5f;
		case 2:
			return 0.5f;
		default:
			return super.getMaxValue(cmp);
		}
	}

	public float getMinValue(int cmp) {
		switch(cmp) {
		case 0:
			return (float)0.0f;
		case 1:
			return -0.5f;
		case 2:
			return -0.5f;
		default:
			return super.getMinValue(cmp);
		}
	}

	public String getName(int cmp) {
		switch(cmp) {
		case 0:
			return "Y";
		case 1:
			return "Cb";
		case 2:
			return "Cr";
		default:
			return super.getName(cmp);
		}
	}

  public int chromaticZone(Color col) {
    //TODO
    return ColorSpaceJMR.CHROMATIC_ZONE;
  }

  public float[] chromaticDegree(Color col) {
    return null;
  }
}
