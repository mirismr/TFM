/**
 *
 */
package jmr.colorspace;


/**
 * A white point is a set of tristimulus values coordinates (Xref,Yref,Zref) that serve
 * to define the color "white" in image capture or reproduction.
 *
 * <p>This class contains a list of standardized illuminants with their CIE chromaticity
 * coordinates (x,y) of a perfect reflecting (or transmitting) white diffuser.
 * The CIE chromaticity coordinates are given for both the 2 degree field of view {@link #WP_CIE1931}
 *  and the 10 degree field of view {@link #WP_CIE1964}.
 *  </p>
 *
 * @author  SoTiLLo
 * @version 1.0
 *
 * @see <a href="http://en.wikipedia.org/wiki/White_point#White_points_for_some_illuminants">Wikipedia</a>
 *
 */
public class WhitePoint {

	/** A user defined white point */
	public static final int WP_TYPE_CUSTOM = 0;

	/**  Incandescent tungsten */
	public static final int WP_TYPE_A = 1;

	/** Obsolete, direct sunlight at noon */
	public static final int WP_TYPE_B = 2;

	/** Cool White Fluorescent (CWF) */
	public static final int WP_TYPE_F2 = 3;

	public static final int WP_TYPE_D50 = 4;
	public static final int WP_TYPE_D55 = 5;

	/** Broad-Band Daylight Fluorescent */
	public static final int WP_TYPE_F7 = 6;

	/** Daylight. It is used by Television, sRGB color space */
	public static final int WP_TYPE_D65 = 7;

	/** Equal energy */
	public static final int WP_TYPE_E = 10;

	/** Correspond to the 2 degree field of view measures by CIE1931 */
	public static final int WP_CIE1931 =1;

	/** Correspond to the 10 degree field of view measures by CIE1964 */
	public static final int WP_CIE1964 =2;

	private float Xr;
	private float Yr;
	private float Zr;
	private int illuType;
	private int illuCIE;

	/**
	 * The constructor which can't be call with only the <code>illuType</code> due to the fact that the
	 * value X,Y,Z need to be calculated before calling the instance of the object.
	 * You should use the {@link #getInstance(int, boolean)} method to call the constructor.
	 *
	 * @param X
	 * @param Y
	 * @param Z
	 */

	public WhitePoint(float X,float Y,float Z) {
		this.Xr = X;
		this.Yr = Y;
		this.Zr = Z;
		this.illuType = WP_TYPE_CUSTOM;
		this.illuCIE = WP_TYPE_CUSTOM;
	}

	private WhitePoint(float [] XYZ, int type, int illuCIE) {
		this.Xr = XYZ[0];
		this.Yr = XYZ[1];
		this.Zr = XYZ[2];
		this.illuType = type;
		this.illuCIE = illuCIE;
	}


	/**
	 * Create an instance of <code>WhitePoint</code> using the illuminant {@link #WP_TYPE_D65} as default and the
	 * Tristimulus value with the 2° observer {@link #WP_CIE1931}.
	 * @return a WhitePoint.
	 */
	public static WhitePoint getInstance() {
		return getInstance(WP_TYPE_D65);
	}

	/**
	 * Create an instance of <code>WhitePoint</code> given an illuminant type.
	 * The tristimulus value are using the 2° observer {@link #WP_CIE1931}.
	 * @param type 	The Illuminant type
	 * @return a WhitePoint.
	 */
	public static WhitePoint getInstance(int type) {
		return getInstance(type,false);
	}

	/**
	 * Create an instance of <code>WhitePoint</code> given an illuminant type, and the type of observer.
	 * @param type		 The Illuminant type
	 * @param isCIE1964  if it's true use the 10° observer {@link #WP_CIE1964} else use the 2° observer {@link #WP_CIE1931}.
	 * @return 			 A WhitePoint.
	 */
	public static WhitePoint getInstance(int type, boolean isCIE1964) {
		if(isCIE1964)
			return new WhitePoint(xy2XYZ(getCIE1964xy(type)),type,WP_CIE1964);
		else
			return new WhitePoint(xy2XYZ(getCIE1931xy(type)),type,WP_CIE1931);
	}


	/**
	 * @param typeIlluminant
	 * @return an array of two element containing CIE1964xy values according to the illumant type.
	 * @see <a href="http://en.wikipedia.org/wiki/White_point">Wikepedia Table</a>
	 */
	private static float[] getCIE1964xy(int typeIlluminant) {
		switch(typeIlluminant) {
		case WP_TYPE_A:
			return new float[]{ 0.45117f , 	0.40594f }; 	//Incandescent tungsten
		case WP_TYPE_B:
			return new float[]{	0.3498f  , 	0.3527f  }; 	//Obsolete, direct sunlight at noon
		case WP_TYPE_F2:
			return new float[]{	0.37928f , 	0.36723f }; 	//Cool White Fluorescent (CWF)
		case WP_TYPE_D50:
			return new float[]{	0.34773f , 	0.35952f };
		case WP_TYPE_D55:
			return new float[]{	0.33411f , 	0.34877f };
		case WP_TYPE_F7:
			return new float[]{	0.31565f , 	0.32951f }; 	//Broad-Band Daylight Fluorescent
		case WP_TYPE_E:
			return new float[]{	1/3      ,	1/3}; 			//Equal energy
		case WP_TYPE_D65:
			//We assume that image are in sRGB so the white reference is D65.
		default:
			return new float[]{ 0.31382f , 	0.33100f }; 	//Television, sRGB color space
		}
	}

	/**
	 * @param 	typeIlluminant 	The illuminant type listed <a href="#field_summary">here</a>
	 * @return an array of two element containing CIE1964xy values according to the illumant type.
	 * @see <a href="http://en.wikipedia.org/wiki/White_point">Wikepedia Table</a>
	 */
	private static float[] getCIE1931xy(int typeIlluminant) {
		switch(typeIlluminant) {
		case WP_TYPE_A:
			return new float[]{ 0.44757f, 0.40745f }; 	//Incandescent tungsten
		case WP_TYPE_B:
			return new float[]{	0.34842f, 0.35161f }; 	//Obsolete, direct sunlight at noon
		case WP_TYPE_F2:
			return new float[]{	0.37207f ,	0.37512f}; 	//Cool White Fluorescent (CWF)
		case WP_TYPE_D50:
			return new float[]{	0.34567f , 	0.35850f};
		case WP_TYPE_D55:
			return new float[]{	0.33242f, 	0.34743f};
		case WP_TYPE_F7:
			return new float[]{	0.31285f, 	0.32918f}; 	//Broad-Band Daylight Fluorescent
		case WP_TYPE_E:
			return new float[]{	1/3 ,	1/3}; 			//Equal energy
		case WP_TYPE_D65:
			//We assume that image are in sRGB so the white reference is D65.
		default:
			return new float[]{0.31271f , 	0.32902f}; 	//Television, sRGB color space
		}
	}

	/** Transform xyValue that we have in our table in XYZ value */
	private static float[] xy2XYZ(float [] xy) {
		float [] XYZ = new float[3];
		XYZ[1] = 1f; //Y is defined to be 1
		XYZ[0] = xy[0]*(XYZ[1]/xy[1]); // X=x*(Y/y)
		XYZ[2] = (1-xy[0]-xy[1])*(XYZ[1]/xy[1]); //Z=(1-x-y)*(Y/y)
		return XYZ;
	}

	public float[] getXYZ() {
		float[] XYZr = {Xr,Yr,Zr};
		return XYZr;
	}

	public void setXYZ(float X,float Y,float Z) {
		this.Xr = X;
		this.Yr = Y;
		this.Zr = Z;
	}

	public void setXYZ(float [] XYZ) {
		this.Xr = XYZ[0];
		this.Yr = XYZ[1];
		this.Zr = XYZ[2];
	}

	public void normVal(float[] XYZ) {
		XYZ[0] /= Xr;
		XYZ[1] /= Yr;
		XYZ[2] /= Zr;
	}

	public float normYVal(float Y) {
		return Y/Yr;
	}

	public String toString() {
		String str="WhitePoint "+getName();
		if(illuCIE == WP_CIE1931) str+=" - CIE1931 ";
		if(illuCIE == WP_CIE1964) str+=" - CIE1964 ";
		str+=" XYZ = ["+this.Xr+","+this.Yr+","+this.Zr+"];\n";
		return str;
	}

	public String getName() {
		String str="";
		switch(this.illuType) {
		case WP_TYPE_CUSTOM: str = "WP_TYPE_CUSTOM"; break;
		case WP_TYPE_A: str = "WP_TYPE_A"; break;
		case WP_TYPE_B: str = "WP_TYPE_B"; break;
		case WP_TYPE_F2: str = "WP_TYPE_F2"; break;
		case WP_TYPE_D50: str = "WP_TYPE_D50"; break;
		case WP_TYPE_D55: str = "WP_TYPE_D55"; break;
		case WP_TYPE_F7: str = "WP_TYPE_F7"; break;
		case WP_TYPE_D65: str = "WP_TYPE_D65"; break;
		case WP_TYPE_E: str = "WP_TYPE_E"; break;
		}
	return str;

	}
;
}
