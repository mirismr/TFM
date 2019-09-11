/**
 *
 */
package jmr.colorspace;

import java.awt.Color;

/**
 * The HMMD (<i>H</i>ue,<i>M</i>ax,<i>M</i>in,<i>D</i>iff) Color Space used by MPEG7 Standard.
 *
 *
 * <p>
 * It has been created because the quantification of the HSV space is not uniform and introduce some error
 * (<a href="http://www.lg-elite.com/MIGR/cmip/hmmd/hmmd.html">More details</a>)
 * To deal with this inaccuracy, the MPEG-7 recently developed an adaptation of the HSV
 * called the Hue-Max-Min-Diff (HMMD) color space (Manjunath et al. 2001). First, the
 * HMMD's dimensions meaning are more intuitive than the HSV: the Min dimension stand
 * for whiteness, the Max for blackness and the Diff for colorfulness. In other words,
 * Max indicates how much black color it has, giving the flavor of shade, Min indicates
 * how much white color it has, giving the flavor of tint. The Diff indicates how much
 * gray it contains and how close to the pure color, giving the flavor of tone. It is defined
 * as the difference between max and min.
 *  (This space is similar to {@link ColorSpaceHSI}).</p>
 *
 *
 * <p>
 * For more details about the quantification and extremum value of this space
 * see {@link #fromRGB(float[])}.
 * The inverse function to go from HMMD space to sRGB/CIEXYZ called {@link #toRGB(float[])}
 *  is not implemented.
 * </p>
 *
 * @author  	SoTiLLo
 *
 * */
public class ColorSpaceHMMD extends ColorSpaceJMR {


	 /**  Serial ID for serialization in the case that <code>instanceof</code> does not work   */
	private static final long serialVersionUID = 5148059083034064637L;

	/**
     * Constructs an instance of this class with <code>type</code>
     * <code>ColorSpace.TYPE_HMMD</code>, 3 components, and preferred
     * intermediary space sRGB.
     */

	protected ColorSpaceHMMD() {
		super(ColorSpaceJMR.CS_HMMD,4);
	}


	/** transform a RGB pixel in a HMMD pixel.
	 *
	 *
	 * @param 	rgbVec 	a float vector (length=3) with rgb values normalized R,G,B=[0,1]
	 * @return 			a float vector (length=4) with hmmd values normalized H=[0,360] and Max,Min,Diff=[0,1]
	 * @see 			<a href="http://www.lg-elite.com/MIGR/cmip/hmmd/hmmd.html">HMMD quantification</a>
	 *
	 */
	public float[] fromRGB(float[] rgbVec) {

		//my.Debug.printCount("fromRGB (RGB > HMMD):");
		//my.Debug.printCount(" - RGB input  :rgb=["+rgbVec[0]+","+rgbVec[1]+","+rgbVec[2]+"];");

		float[] hmmdVec = new float[4] ;
		float r = rgbVec[0] ;
		float g = rgbVec[1] ;
		float b = rgbVec[2] ;

		r = (r < 0.0f)? 0.0f : ((r > 1.0f) ? 1.0f: r) ;
		g = (g < 0.0f)? 0.0f : ((g > 1.0f) ? 1.0f: g) ;
		b = (b < 0.0f)? 0.0f : ((b > 1.0f) ? 1.0f: b) ;

		float max = Math.max(Math.max(r,g), Math.max(g,b));
		float min = Math.min(Math.min(r,g), Math.min(g,b));
		float diff = (max - min);
		//	float sum = (float) ((max + min)/2.);

		float hue = 0;
		if (diff == 0)
			hue = 0;
		else if (r == max && (g - b) > 0)
			hue = 60*(g-b)/(max-min);
		else if (r == max && (g - b) <= 0)
			hue = 60*(g-b)/(max-min) + 360;
		else if (g == max)
			hue = (float) (60*(2.+(b-r)/(max-min)));
		else if (b == max)
			hue = (float) (60*(4.+(r-g)/(max-min)));

                // set hue

		hmmdVec[0] = hue;
		hmmdVec[1] = max;
		hmmdVec[2] = min;
		hmmdVec[3] = diff;

		//my.Debug.printCount(" - HMMD output: hmmd=["+hmmdVec[0]+","+hmmdVec[1]+","+hmmdVec[2]+","+hmmdVec[3]+"];");

		return hmmdVec ;
	}


	/**
	 * Transform a HMMD pixel in a RGB pixel.
	 *
	 *  <p style="color:red">This function is not correctly implemented and use strictly the HSI to RGB convertion</p>
	 *
	 * @param	hmmdVec	a float vector (length=3) with hsv values normalized H=[0,360] and Max,Min,Diff=[0,1]
	 * @return 			a float vector (length=3) with rgb values normalized R,G,B=[0,1]
	 * @see <a href="http://java.sun.com/products/java-media/jai/forDevelopers/jai-apidocs/javax/media/jai/IHSColorSpace.html">
	 * javax.media.jai.IHSColorSpace</a>
	 * @deprecated Use the same transformation as {@link ColorSpaceHSI#toRGB(float[])}
	 */
	public float[] toRGB(float[] hmmdVec) {
//		my.Debug.printCount("toRGB (HMMD -> RGB)");
//		my.Debug.printCount(" - HMMD input: 	hmmd=["+hmmdVec[0]+","+hmmdVec[1]+","+hmmdVec[2]+","+hmmdVec[3]+"];");

		float h = hmmdVec[0]*((float)PI2/360f);
		float s = hmmdVec[3] ;
		float i = (hmmdVec[1] + hmmdVec[2])/2f;

		i = (i < 0.0f) ? 0.0f : ((i > 1.0f) ? 1.0f : i) ;
		h = (h < 0.0f) ? 0.0f : ((h > (float)PI2) ? (float)PI2 : h) ;
		s = (s < 0.0f) ? 0.0f : ((s > 1.0f) ? 1.0f : s) ;

		float[] rgb = new float[3] ;

		// when the saturation is 0, the color is grey. so R=G=B=I.
		if (s == 0.0f) {
			rgb[0] = rgb[1] = rgb[2] = i ;
		}
		else {
			if (h >= PI23 && h < PI43) {
				float r = (1 - s) * i ;
				float c1 = 3 * i - r ;
				float c2 = (float) (SQRT3 * (r - i) * Math.tan(h)) ;
				rgb[0] = r ;
				rgb[1] = (c1 + c2) / 2 ;
				rgb[2] = (c1 - c2) / 2 ;
			}
			else if (h >PI43) {
				float g = (1 - s) * i ;
				float c1 = 3 * i - g ;
				float c2 = (float) (SQRT3 * (g - i) * Math.tan(h - PI23)) ;
				rgb[0] = (c1 - c2) / 2 ;
				rgb[1] = g ;
				rgb[2] = (c1 + c2) / 2 ;
			}
			else if (h < PI23) {
				float b = (1 - s) * i ;
				float c1 = 3 * i - b ;
				float c2 = (float) (SQRT3 * (b - i) * Math.tan(h - PI43)) ;
				rgb[0] = (c1 + c2) / 2 ;
				rgb[1] = (c1 - c2) / 2 ;
				rgb[2] = b ;
			}
		}
//		my.Debug.printCount(" - RGB : 	rgb=["+rgb[0]+","+rgb[1]+","+rgb[2]+"];");
		return rgb ;

	}


	public float getMaxValue(int cmp) {
		switch(cmp) {
		case 0:
			return 360.0f;
		default:
			return super.getMaxValue(cmp);
		}
	}

	public float getMinValue(int cmp) {
		switch(cmp) {
		default:
			return super.getMinValue(cmp);
		}
	}

	public String getName(int cmp) {
		switch(cmp) {
		case 0:
			return "Hue";
		case 1:
			return "Max";
		case 2:
			return "Min";
		case 3:
			return "Diff";
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
