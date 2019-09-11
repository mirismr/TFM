/**
 *
 */
package jmr.colorspace;

import java.awt.Color;

/**
 * The HSV (<i>H</i>ue,<i>S</i>aturation,<i>V</i>alue) color space.
 * 
 * <p>
 * The HSV is one of the most popular color space using sector division, also
 * called Hue division. Mainly adopted by the computer graphic community for
 * color selection and representation, this color space has been used in image
 * retrieval (Carson et al. 1997, Depalov et al. 2006b) searching images by
 * semantic color meaning.
 * </p>
 * 
 * 
 * @author SoTiLLo
 * @version 1.0
 * 
 */
@SuppressWarnings("serial")
public class ColorSpaceRGB extends ColorSpaceJMR {

	/**
	 */
	protected ColorSpaceRGB() {
		super(ColorSpaceJMR.CS_RGB, 3);
		// TODO Auto-generated constructor stub
	}

	public int chromaticZone(Color col) {
		// TODO
		return ColorSpaceJMR.CHROMATIC_ZONE;
	}

	/**
	 *  Transforms a color value assumed to be in the default CS_sRGB color space into this ColorSpace.
	 * 
	 * @return a vector (length=3) with srgb values normalized RGB=[0,1]
	 */
	public float[] fromRGB(float[] srgb) {
		/* PROBANDOOOO! ... 10-11-2010 ... getMaxValue = 255 ???
		float[] rgbVec = new float[srgb.length];
		for(int i=0;i<srgb.length;i++)
			rgbVec[i]=srgb[i]/getMaxValue(i);
		
		return rgbVec;*/
		return srgb;
	}
	
	public int getType(){
		return ColorSpaceJMR.CS_sRGB;
	}

	/**
	 * Transforms a color value normalized [0,1] assumed to be in this ColorSpace into a value in the default CS_sRGB color space.
	 * 
	 * @param rgb a vector (length=3) with rgb values normalized [0,1]
	 * @return a vector (length=3) with values normalized R,G,B=[0,255]
	 */
	public float[] toRGB(float[] rgb) {
		/* PROBANDOOOO! ... 10-11-2010 ... getMaxValue = 255 ???
		float[] rgbVec = new float[rgb.length];
		for(int i=0;i<rgb.length;i++)
			rgbVec[i]=rgb[i]*getMaxValue(i);
		
		return rgbVec;*/
		return rgb;
		
	}

	public float getMaxValue(int cmp) {
		//PROBANDOOO ... 20-01-2012 ... MAXIMO 255 NO va FCS
		//return 255.0f;
		return 1.0f;
	}

	public float getMinValue(int cmp) {
		return 0.0f;
	}

	public String getName(int cmp) {
		switch (cmp) {
		case 0:
			return "R";
		case 1:
			return "G";
		case 2:
			return "B";
		default:
			return super.getName(cmp);
		}
	}

	/**
	 * Returns an array with chromaticity degree. float [0] - achromaticity
	 * degree. float [1] - chromaticity degree.
	 * 
	 * @param col
	 *            Color
	 * @return float[]
	 */
	public float[] chromaticDegree(Color col) {
		// TODO
		return null;
	}

}
