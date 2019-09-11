/**
 *
 */
package jmr.colorspace;

import java.awt.Color;

/**
 * L*,u*,v* or CIELUV Color Space is an approximate perceptually uniform space.
 *
 * <p> This space is recommended by the CIE for application in additive light condition (Sangwine  Horne 1998).
 * The L* component is the same as for {@link ColorSpaceLab} and the u* and v* coordinates are defined from the xy-diagrams.<br>
 * This space is obtained from CIEXYZ 1931 and it use a Reference White also called {@link WhitePoint}
 * to normalized these XYZ value.</p>
 *
 *
 *
 *
 * @author  SoTiLLo
 * @version 1.0
 * @see <a href="http://www.couleur.org/index.php?page=transformations">www.couleur.org</a>
 *
 */
public class ColorSpaceLuv extends ColorSpaceJMR {

	private static final long serialVersionUID = -4676846889796099725L;

	private WhitePoint wP;

	/** Constant use for the transformation */
	private float epsi = 216.f/24389.f;
	private float kappa = 24389.f/(116.f*27.f);
	private float frac16116 = 16.f/116.f;
	private float ur_;
	private float vr_;

	 /**
     * Constructs an instance of this class with <code>type</code>
     * {@link ColorSpaceJMR#CS_Luv}, 3 components, and used as reference white
     * the {@link WhitePoint#WP_TYPE_D65} (Daylight)
     */

	public ColorSpaceLuv() {
		super(ColorSpaceJMR.CS_Luv,3);
		this.wP = WhitePoint.getInstance(WhitePoint.WP_TYPE_D65);
		ur_ = XYZ2u_(this.wP.getXYZ());
		vr_ = XYZ2v_(this.wP.getXYZ());
	}

	 /**
     * Constructs an instance of this class with <code>type</code>
     * {@link ColorSpaceJMR#CS_Luv}, 3 components, giving a {@link WhitePoint}
     * in parameters.
     *
     * @param 	wp	 The white point to normalize XYZ value
     */
	public ColorSpaceLuv(WhitePoint wp) {
		super(ColorSpaceJMR.CS_Luv,3);
		this.wP=wp;
		ur_ = XYZ2u_(this.wP.getXYZ());
		vr_ = XYZ2v_(this.wP.getXYZ());
	}


	/**
	 * In the case we need to change the white point for this color Space
	 * @param 	type 	the white point you can find in {@link WhitePoint}
	 */
	public void setWhitePoint(int type) {
		wP = WhitePoint.getInstance(type);
		ur_ = XYZ2u_(wP.getXYZ());
		vr_ = XYZ2v_(wP.getXYZ());
	}

	/** Transform a RGB pixel in XYZ pixel then in CIELUV.
	 *
	 * @param	rgbVec 	a vector (length=3) with rgb values normalized R,G,B=[0,1]
	 * @return 			a vector (length=3) with CIELUV values normalized L*,a*,b*=[0,1]
	 * @see <i>The Color Image Processing Book, Sangwine 1998</i>
	 */
	public float[] fromRGB(float[] rgbVec) {
//		my.Debug.printCount("fromRGB (RGB > XYZ > CIELUV):");
//		my.Debug.printCount(" - RGB input :  rgb=["+rgbVec[0]+","+rgbVec[1]+","+rgbVec[2]+"];");
		float[] xyzVec = new float[3];
		RGB2XYZ(rgbVec,xyzVec);
//		my.Debug.printCount(" - XYZ output: 	xyz=["+xyzVec[0]+","+xyzVec[1]+","+xyzVec[2]+"];");
		return fromCIEXYZ(xyzVec);
		//return xyzVec;
	}




	/**
	 * Transform a XYZ pixel in Lab pixel.
	 *
	 * @param 	xyzVec	a vector (length=3) with xyz values normalized X,Y,Z=[0,1]
	 * @return 			a vector (length=3) with luv values normalized L*,a*,b*=[0,1]
	 * @see <i>The Color Image Processing Book, Sangwine 1998</i>
	 * @see <a href="http://www.brucelindbloom.com">BruceLindbloom's website for Kappa and Espilon values</a>
	 */
	public float[] fromCIEXYZ(float[] xyzVec) {
//		my.Debug.printCount("fromRGB (RGB > XYZ > CIELUV):");
//		my.Debug.printCount(" - XYZ input: 	xyz=["+xyzVec[0]+","+xyzVec[1]+","+xyzVec[2]+"];");

		float[] luvVec = new float[3];
		float u, v, u_, v_;
                double threshold = Math.pow(6.0f/29.0f,3.0f);

		float yr = wP.normYVal(xyzVec[1]); //Y Normalized by the WhitePoint for computing the L*
		float L = (yr > (float)threshold)? 116f*(float)Math.pow(yr,1.0f/3.0f)-16 : (float)Math.pow(29.0f/3.0f,3f)*yr; //Obtain the luminance like in CIELAB

		//pure XYZ value to uv_
		u_ = XYZ2u_(xyzVec);
		v_ = XYZ2v_(xyzVec);

		u = 13*L*(u_ -ur_);
		v = 13*L*(v_ -vr_);

		luvVec[0] = L;
		luvVec[1] = u;
		luvVec[2] = v;

//		my.Debug.printCount(" - LUV output: 	luv=["+luvVec[0]+","+luvVec[1]+","+luvVec[2]+"];");

		return luvVec ;
	}

	private float XYZ2u_(float[] XYZ) {
		return 4f*XYZ[0]/(XYZ[0] + 15*XYZ[1] + 3*XYZ[2]); //u_ = 4*X / (X + 15*Y + 3*Z);
	}

	private float XYZ2v_(float[] XYZ) {
		return 9f*XYZ[0]/(XYZ[0] + 15*XYZ[1] + 3*XYZ[2]); //v_ = 9*Y / (X + 15*Y + 3*Z)
	}


	private float func(float x) {
		if ( x > epsi ) return (float)Math.pow(x, 1.f/3.f);
		else  return kappa * x + frac16116;
	}


	/**
	 * Transform a CIELUV pixel in XYZ then in RGB pixel.
	 *
	 *
	 * @param 	luvVec	a vector (length=3) with luv values normalized L*,a*,b*=[0,1]
	 * @return 			a vector (length=3) with rgb values normalized R,G,B=[0,1]
	 */
	public float[] toRGB(float[] luvVec) {
		float[] xyzVec = toCIEXYZ(luvVec);
		float [] rgbVec =  new float[3];

		XYZ2RGB(xyzVec,rgbVec);

		return rgbVec;

	}

	/**
	 * Transform a luv pixel in XYZ pixel.
	 *
	 * <p style="color:red">This function is not implemented and return a <b>zero vector</b></p>
	 *
	 * @param 	luvVec	a vector (length=3) with luv values normalized L*,u*,v*=[0,1]
	 * @return 			a vector (length=3) with xyz values normalized X,Y,Z=[0,1]
	 * @deprecated Return a zero vector.
	 */
	public float[] toCIEXYZ(float[] luvVec) {
		float[] xyzVec = {0f,0f,0f}; //TODO Transformation not implemented
                float l,u,v;
                float u_,v_;
                float[] XYZn = wP.getXYZ();

                l = luvVec[0];
                u = luvVec[1];
                v = luvVec[2];
                u_ = (u/(13.0f*l))+ ur_;
                v_ = (v/(13.0f*l))+ vr_;

                xyzVec[1] = (l>8)? XYZn[1]*(float)Math.pow(((l+16.0f)/116.0f),3.0f) : XYZn[1]*l*(float)Math.pow(3.0f/29.0f,3f);
                xyzVec[0] =(-9.0f*xyzVec[1]*u_)/(((u_-4.0f)*v_)-(u_*v_));
                xyzVec[2] =((9*xyzVec[1])-(15.0f*v_*xyzVec[1])-(v_*xyzVec[0]))/(3*v_);

		return xyzVec;
	}


	/**
     * Returns the maximum normalized color component value for the
     * specified component.
     *
     * <p style="color:red">The value returned may not be correct. This function should be verify</p>
     *
     * @param 	cmp	the component index
     * @return 		the maximum normalized component value
     */
	public float getMaxValue(int cmp) { //TODO: Verify value for u* and v*
		switch(cmp) {
		case 0:
			return 100.f;
		case 1:
			return 400.f;
		case 2:
			return 400.f;
		default:
			return super.getMaxValue(cmp);
		}
	}

	/**
     * Returns the minimum normalized color component value for the
     * specified component.
     *
     * <p style="color:red">The value returned may not be correct. This function should be verify</p>
     *
     * @param 	cmp	the component index
     * @return 		the minimum normalized component value
     */
	public float getMinValue(int cmp) { //TODO: Check value for u*,v*
		switch(cmp) {
		case 1:
			return -128.f;
		case 2:
			return -616.f;
		default:
			return super.getMinValue(cmp);
		}
	}

	public String getName(int cmp) {
		switch(cmp) {
		case 0:
			return "L*";
		case 1:
			return "u*";
		case 2:
			return "v*";
		default:
			return super.getName(cmp);
		}
	}

	public String toString() {
		String str=super.toString();
		str+="/ espilon="+epsi+"; kappa="+kappa;
		return str;
	}

  public int chromaticZone(Color col) {
    //TODO
    return ColorSpaceJMR.CHROMATIC_ZONE;
  }

  public float[] chromaticDegree(Color col) {
    return null;
  }
}
