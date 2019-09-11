/**
 *
 */
package jmr.colorspace;

import java.awt.Color;

/**
 * The HSV (<i>H</i>ue,<i>S</i>aturation,<i>V</i>alue) color space.
 *
 * <p>
 * The HSV is one of the most popular color space using sector division, also called Hue division.
 * Mainly adopted by the computer graphic community for color selection and representation,
 * this color space has been used in image retrieval (Carson et al. 1997, Depalov et al. 2006b)
 * searching images by semantic color meaning.
 * </p>
 *
 *
 *
 * @author  SoTiLLo
 * @version 1.0
 *
 */
public class ColorSpaceHSV extends ColorSpaceJMR {

  private static final long serialVersionUID = 8258172467685956032L;

  private static float Ts;
  private static float Ti;
  private static float slope;

  private static final float DEFAULT_Ts = 1/7.0f;
  private static final float DEFAULT_Ti = 1/9.0f;
  private static final float DEFAULT_Slope = 4f;


 /**
  * Constructs an instance of this class with <code>type</code>
  * <code>ColorSpace.TYPE_HSV</code>, 3 components, and preferred
  * intermediary space sRGB.
  */
 protected ColorSpaceHSV() {
   super(ColorSpaceJMR.CS_HSV, 3);
   Ts = DEFAULT_Ts;
   Ti = DEFAULT_Ti;
   slope = DEFAULT_Slope;
 }

 public static float getTs(){
   return Ts;
 }

 public static float getTi(){
   return Ti;
 }

 public void setTs(float Ts){
   this.Ts=Ts;
 }

 public void setTi(float Ti){
   this.Ti=Ti;
 }

 public int chromaticZone(Color col){
    float S, I;
    float c[] = col.getColorComponents(null);

    S = c[1];
    I = c[2];

    //Zona
    if (I <= Ti) { //acromatico
      return ColorSpaceJMR.ACHROMATIC_ZONE;
      //System.out.print("A");
    }
    else {
      if ( (Ti < I) && (S > Ts)) {
        return ColorSpaceJMR.CHROMATIC_ZONE;
        //System.out.print("C");
      }
      else {
        return ColorSpaceJMR.SEMICHROMATIC_ZONE;
        //System.out.print("S");
      }
    }
  }



  /** fromRGB : transform a RGB pixel in a HSV pixel.
   *
   * @param 	rgbVec	a vector (length=3) with rgb values normalized R,G,B=[0,1]
   * @return 			a vector (length=3) with hsv values normalized H=[0,360] and S,V=[0,1]
   * @see <i>Introduction to MPEG-7, Manjunath et al. 2001, Section 13.2.1</i>
   */
  public float[] fromRGB(float[] rgbVec) {

//		my.Debug.printCount("fromRGB (RGB > HSV):");
//		my.Debug.printCount(" - RGB input :  rgb=["+rgbVec[0]+","+rgbVec[1]+","+rgbVec[2]+"];");

    float[] hsvVec = new float[3];
    float h = 0f;
    float r = rgbVec[0];
    float g = rgbVec[1];
    float b = rgbVec[2];

    r = (r < 0.0f) ? 0.0f : ( (r > 1.0f) ? 1.0f : r);
    g = (g < 0.0f) ? 0.0f : ( (g > 1.0f) ? 1.0f : g);
    b = (b < 0.0f) ? 0.0f : ( (b > 1.0f) ? 1.0f : b);

    float max = Math.max(Math.max(r, g), Math.max(g, b)); //max(R,G,B);
    float min = Math.min(Math.min(r, g), Math.min(g, b)); //min(R,G,B)
    float diff = (max - min);

    // set value
    hsvVec[2] = max;

    // set saturation
    if (max == 0.0f) {
      hsvVec[1] = 0.0f;
    }
    else {
      hsvVec[1] = diff / max;
    }

    // set hue
    if (max == min) {
      h = 0f;
    }
    else {
      if (r == max) {
        if (g >= b) {
          h = ( (g - b) / diff) * 60.f;
        }
        else {
          h = 360.f + ( (g - b) / diff) * 60.f;
        }
      }
      else if (g == max) {
        h = (2.0f + (b - r) / diff) * 60.f;
      }
      else if (b == max) {
        h = (4.0f + (r - g) / diff) * 60.f;
      }
    }
    hsvVec[0] = h;

//		my.Debug.printCount(" - HSV output: 	hsv=["+hsvVec[0]+","+hsvVec[1]+","+hsvVec[2]+"];");

    return hsvVec;
  }

  /**
   * Transform a HSV pixel in a RGB pixel.
   *
   * @param	hsvVec	a vector (length=3) with hsv values normalized H=[0,360] and S,V=[0,1]
   * @return			a vector (length=3) with rgb values normalized R,G,B=[0,1]
   * @see <a href="http://alvyray.com/Papers/hsv2rgb.htm">HSV2RGB</a>
   */
  /*public float[] toRGB(float[] hsvVec) {
//		my.Debug.printCount("toRGB (HSV -> RGB)");
//		my.Debug.printCount(" - HSV input: 	hsi=["+hsvVec[0]+","+hsvVec[1]+","+hsvVec[2]+"];");

   float[] rgbVec = {0f,0f,0f};
   float h = hsvVec[0] ;
   float s = hsvVec[1] ;
   float v = hsvVec[2] ; // H is given on [0, 6] or UNDEFINED. S and V are given on [0, 1].
   // RGB are each returned on [0, 1].

   h = (h < 0.0f) ? 0.0f : ((h > (float)getMaxValue(0)) ? (float)getMaxValue(0) : h) ;
   s = (s < 0.0f) ? 0.0f : ((s > 1.0f) ? 1.0f : s) ;
   v = (v < 0.0f) ? 0.0f : ((v > 1.0f) ? 1.0f : v) ;

   h=h/60;
   float m, n, f;
   int i;

   i = (int)Math.floor((double)h);
   f = h - i;
   if (i%2== 1) f = 1 - f; // if i is even
   m = v * (1 - s);
   n = v * (1 - s * f);
   switch (i) {
   case 6:
   case 0: toRGBArray(v, n, m,rgbVec);
   case 1: toRGBArray(n, v, m,rgbVec);
   case 2: toRGBArray(m, v, n,rgbVec);
   case 3: toRGBArray(m, n, v,rgbVec);
   case 4: toRGBArray(n, m, v,rgbVec);
   case 5: toRGBArray(v, m, n,rgbVec);
   }

//		my.Debug.printCount(" - RGB : 	rgb=["+rgbVec[0]+","+rgbVec[1]+","+rgbVec[2]+"];");
   return rgbVec ;
    }*/

  /**
   * Transform a HSV pixel in a RGB pixel.
   *
   * @param	hsvVec	a vector (length=3) with hsv values normalized H=[0,360] and S,V=[0,1]
   * @return			a vector (length=3) with rgb values normalized R,G,B=[0,1]
   * @see <a href="http://alvyray.com/Papers/hsv2rgb.htm">HSV2RGB</a>
   */
  public float[] toRGB(float[] hsvVec) {
    float[] rgbVec = {
        0f, 0f, 0f};
    float h = hsvVec[0]*getMaxValue(0);
    float s = hsvVec[1];
    float v = hsvVec[2]; // H is given on [0, 6] or UNDEFINED. S and V are given on [0, 1].
    // RGB are each returned on [0, 1].

    h = (h < 0.0f) ? 0.0f :
        ( (h > (float) getMaxValue(0)) ? (float) getMaxValue(0) : h);
    s = (s < 0.0f) ? 0.0f : ( (s > 1.0f) ? 1.0f : s);
    v = (v < 0.0f) ? 0.0f : ( (v > 1.0f) ? 1.0f : v);

    int hi = (int) (h / 60) % 6;
    float p, q, t, f;

    f = (h / 60.0f) - hi;

    p = v * (1 - s);
    q = v * (1 - (s * f));
    t = v * (1 - (1 - f) * s);

    switch (hi) {
      case 6:
      case 0:
        toRGBArray(v, t, p, rgbVec);
        break;
      case 1:
        toRGBArray(q, v, p, rgbVec);
        break;
      case 2:
        toRGBArray(p, v, t, rgbVec);
        break;
      case 3:
        toRGBArray(p, q, v, rgbVec);
        break;
      case 4:
        toRGBArray(t, p, v, rgbVec);
        break;
      case 5:
        toRGBArray(v, p, q, rgbVec);
        break;
    }

    return rgbVec;
  }

  private void toRGBArray(float r, float g, float b, float[] rgb) {
    rgb[0] = r;
    rgb[1] = g;
    rgb[2] = b;
  }

  public float getMaxValue(int cmp) {
    switch (cmp) {
      case 0:
        return (float) 360.0f;
      case 1:
        return 1.0f;
      case 2:
        return 1.0f;
      default:
        return super.getMaxValue(cmp);
    }
  }

  public float getMinValue(int cmp) {
    switch (cmp) {
      default:
        return super.getMinValue(cmp);
    }
  }

  public String getName(int cmp) {
    switch (cmp) {
      case 0:
        return "H";
      case 1:
        return "S";
      case 2:
        return "V";
      default:
        return super.getName(cmp);
    }
  }

  private float linealMembershipFunction(float p, float c1, float c2, boolean crescent){
         float fp, ini, fin;
         if(crescent){
           ini = 0;
           fin = 1;
         }
         else{
           ini = 1;
           fin = 0;
         }
         if (p <= c1)
             fp = ini;
         else {
             if (c1 <= p && c2 >= p) {
                 float m, b, x1, y1, x2, y2, x;
                 x1 = c1;
                 x2 = c2;
                 y1 = ini;
                 y2 = fin;
                 x = p;
                 m = (y2 - y1) / (x2 - x1);
                 b = y1 - m * x1;
                 fp = m * x + b;
             } else
                 fp = fin;
         }
         return fp;
     }


  /**
 * Returns an array with chromaticity degree.
 * float [0] - achromaticity degree.
 * float [1] - chromaticity degree.
 * @param col Color
 * @return float[]
 */
public float[] chromaticDegree(Color col) {
  float[] mu = new float[3];
  float s,i;
  float mu_a, mu_s, mu_c;

  float components[] = col.getColorComponents(null);
  i = components[2];
  s = components[1];


  mu_a = Math.max(linealMembershipFunction(i,Ti-(Ti/slope),Ti+(Ti/slope),false),linealMembershipFunction(i,1-Ti-(Ti/slope),1-Ti+(Ti/slope),true));
  mu_s = Math.min(linealMembershipFunction(s,Ts-(Ts/slope),Ts+(Ts/slope),false),1-mu_a);
  mu_c = 1 - (mu_a + mu_s);

  mu[0]=mu_a;
  mu[1]=mu_c;
  mu[2]=mu_s;

  return mu;
}

}
