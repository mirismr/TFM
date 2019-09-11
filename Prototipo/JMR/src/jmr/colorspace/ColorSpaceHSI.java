/**
 *
 */
package jmr.colorspace;

import java.awt.Color;

/**
 * The HSI (<i>H</i>ue,<i>S</i>aturation,<i>I</i>ntensity) color space (also known as IHS or HIS).
 *
 * No more details here but you can find more information in the
 * <a href="http://java.sun.com/products/java-media/jai/forDevelopers/jai-apidocs/javax/media/jai/IHSColorSpace.html">
 * JAI API of IHSColorSpace</a>
 *
 *
 * @author  SoTiLLo
 * @version 1.0
 *
 */
public class ColorSpaceHSI extends ColorSpaceJMR {

  private static final long serialVersionUID = 1L;

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
  protected ColorSpaceHSI() {
    super(ColorSpaceJMR.CS_HSI, 3);
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
    if (I <= Ti || I >= (1 - Ti)) { //acromatico
      return ColorSpaceJMR.ACHROMATIC_ZONE;
      //System.out.print("A");
    }
    else {
      if ( (Ti < I) && (I < (1 - Ti)) && (S > Ts)) {
        return ColorSpaceJMR.CHROMATIC_ZONE;
        //System.out.print("C");
      }
      else {
        return ColorSpaceJMR.SEMICHROMATIC_ZONE;
        //System.out.print("S");
      }
    }
  }


  /** transform a RGB pixel in a HSI pixel.
   *
   * More information about this transformation can be find at
   * <a href="http://java.sun.com/products/java-media/jai/forDevelopers/jai-apidocs/javax/media/jai/IHSColorSpace.html">
   * javax.media.jai.IHSColorSpace
   * </a>
   * @param 	rgbVec	a float vector (length=3) with rgb values normalized R,G,B=[0,1]
   * @return 			a float vector (length=3) with hsv values normalized H=[0,2*PI] and S,I=[0,1]
   */
  /*public float[] fromRGB(float[] rgbVec) {

//		my.Debug.printCount("fromRGB (RGB > HSI):");
//		my.Debug.printCount(" - RGB input :  rgb=["+rgbVec[0]+","+rgbVec[1]+","+rgbVec[2]+"];");

    float[] hsiVec = new float[3];
    float h, s, i = 0.0f;
    float r = rgbVec[0];
    float g = rgbVec[1];
    float b = rgbVec[2];

    //val = (condition)? val_true : val_false
    r = (r < 0.0f) ? 0.0f : ( (r > 1.0f) ? 1.0f : r);
    g = (g < 0.0f) ? 0.0f : ( (g > 1.0f) ? 1.0f : g);
    b = (b < 0.0f) ? 0.0f : ( (b > 1.0f) ? 1.0f : b);

    i = (r + g + b) / 3.0f;
    float drg = r - g;
    float drb = r - b;
    float temp = (float) Math.sqrt(drg * (double) drg +
                                   drb * (double) (drb - drg));

    // when temp is zero, R=G=B. Hue should be NaN. To make
    // numerically consistent, set it to 2PI
    if (temp != 0.0f) {
      temp = (float) Math.acos( (drg + drb) / (double) temp / 2);
      if (g < b) {
        h = (float) (PI2 - temp);
      }
      else {
        h = temp;
      }
    }
    else {
      h = (float) PI2;
    }

    float min = (r < g) ? r : g;
    min = (min < b) ? min : b;

    // when intensity is 0, means R=G=B=0. S can be set to 0 to indicate
    // R=G=B.
    if (i == 0.0f) {
      s = 0.0f;
    }
    else {
      s = 1.0f - min / i;
    }

    hsiVec[0] = h;
    hsiVec[1] = s;
    hsiVec[2] = i;

//		my.Debug.printCount(" - HSI output: 	hsi=["+hsiVec[0]+","+hsiVec[1]+","+hsiVec[2]+"];");

    return hsiVec;
  }*/
  
  public float[] fromRGB(float[] rgbVec) {

  float H=0,S,I;

    float var_R =rgbVec[0];
    float var_G =rgbVec[1];
    float var_B =rgbVec[2];

    float var_Max =(float) Math.max(var_R,Math.max(var_G,var_B));
    float var_Min =(float) Math.min(var_R,Math.min(var_G,var_B));

    //float var_R = (R / 255); //Where RGB values = 0 255
    //float var_G = (G / 255);
    //float var_B = (B / 255);

    //var_Min = min(var_R, var_G, var_B) //Min. value of RGB
    //var_Max = max(var_R, var_G, var_B) //Max. value of RGB
    float del_Max = var_Max - var_Min; //Delta RGB value

    I = (var_Max + var_Min) / 2.0f;

    if (del_Max == 0) { //This is a gray, no chroma...
      H = 0; //HSL results = 0 1
      S = 0;
    }
    else { //Chromatic data...
      if (I < 0.5f)
        S = del_Max / (var_Max + var_Min);
      else
        S = del_Max / (2.0f - var_Max - var_Min);

      float del_R = ( ( (var_Max - var_R) / 6.0f) + (del_Max / 2.0f)) / del_Max;
      float del_G = ( ( (var_Max - var_G) / 6.0f) + (del_Max / 2.0f)) / del_Max;
      float del_B = ( ( (var_Max - var_B) / 6.0f) + (del_Max / 2.0f)) / del_Max;

      if (var_R == var_Max)
        H = del_B - del_G;
      else if (var_G == var_Max) H = (1.0f / 3.0f) + del_R - del_B;
      else if (var_B == var_Max) H = (2.0f / 3.0f) + del_G - del_R;

      if (H < 0.0f)
        H += 1.0f;
      if (H > 1.0f)
        H -= 1.0f;
    }

    float[] hsiVec = new float[3];

    hsiVec[0]=H*(float)this.getMaxValue(0);
    hsiVec[1]=S*(float)this.getMaxValue(1);
    hsiVec[2]=I*(float)this.getMaxValue(2);
    
    //MiColor toRGB = HSItoRGB(hsi);

    return hsiVec;
}


  /**
   * Transform a HSI pixel in a RGB pixel.
   *
   * More information about this transformation can be find at
   * <a href="http://java.sun.com/products/java-media/jai/forDevelopers/jai-apidocs/javax/media/jai/IHSColorSpace.html">
   * javax.media.jai.IHSColorSpace
   * </a>
   *
   * @param	hsiVec	a float vector (length=3) with hsv values normalized H=[0,2*PI] and S,I=[0,1]
   * @return  		a float vector (length=3) with rgb values normalized R,G,B=[0,1]
   */
  /*public float[] toRGB(float[] hsiVec) {
//		my.Debug.printCount("toRGB (HSI -> RGB)");
//		my.Debug.printCount(" - HSI input: 	hsi=["+hsiVec[0]+","+hsiVec[1]+","+hsiVec[2]+"];");

    float h = hsiVec[0];
    float s = hsiVec[1];
    float i = hsiVec[2];

    i = (i < 0.0f) ? 0.0f : ( (i > 1.0f) ? 1.0f : i);
    h = (h < 0.0f) ? 0.0f : ( (h > (float) PI2) ? (float) PI2 : h);
    s = (s < 0.0f) ? 0.0f : ( (s > 1.0f) ? 1.0f : s);

    float[] rgb = new float[3];

    // when the saturation is 0, the color is grey. so R=G=B=I.
    if (s == 0.0f) {
      rgb[0] = rgb[1] = rgb[2] = i;
    }
    else {
      if (h >= PI23 && h < PI43) {
        float r = (1 - s) * i;
        float c1 = 3 * i - r;
        float c2 = (float) (SQRT3 * (r - i) * Math.tan(h));
        rgb[0] = r;
        rgb[1] = (c1 + c2) / 2;
        rgb[2] = (c1 - c2) / 2;
      }
      else if (h > PI43) {
        float g = (1 - s) * i;
        float c1 = 3 * i - g;
        float c2 = (float) (SQRT3 * (g - i) * Math.tan(h - PI23));
        rgb[0] = (c1 - c2) / 2;
        rgb[1] = g;
        rgb[2] = (c1 + c2) / 2;
      }
      else if (h < PI23) {
        float b = (1 - s) * i;
        float c1 = 3 * i - b;
        float c2 = (float) (SQRT3 * (b - i) * Math.tan(h - PI43));
        rgb[0] = (c1 + c2) / 2;
        rgb[1] = (c1 - c2) / 2;
        rgb[2] = b;
      }
    }
//		my.Debug.printCount(" - RGB : 	rgb=["+rgb[0]+","+rgb[1]+","+rgb[2]+"];");
    return rgb;
  }*/
  
  /**
   * hsiVec ha de estar normalizado en [0,1]
   */
  public float[] toRGB(float[] hsiVec) {
//		my.Debug.printCount("toRGB (HSI -> RGB)");
//		my.Debug.printCount(" - HSI input: 	hsi=["+hsiVec[0]+","+hsiVec[1]+","+hsiVec[2]+"];");

    float H = hsiVec[0];
    float S = hsiVec[1];
    float I = hsiVec[2];

    float R,G,B,var_1,var_2;

    float[] rgb = new float[3];

    if ( S == 0 )                       //HSL values = 0 1
    {
      R = I * 255.0f; //RGB results = 0 255
      G = I * 255.0f;
      B = I * 255.0f;
    }
    else{
      if (I < 0.5)
        var_2 = I * (1.0f + S);
      else
        var_2 = (I + S) - (S * I);

      var_1 = 2.0f * I - var_2;

      R = (255.0f * Hue_2_RGB(var_1, var_2, H + (1.0f / 3.0f)));
      G = (255.0f * Hue_2_RGB(var_1, var_2, H));
      B = (255.0f * Hue_2_RGB(var_1, var_2, H - (1.0f / 3.0f)));
    }

    rgb[0]=R/255.0f;
    rgb[1]=G/255.0f;
    rgb[2]=B/255.0f;

    for(int i=0;i<rgb.length;i++)
      if(rgb[i]>1.0f)
        rgb[i]=1.0f;

    return rgb;
  }

  private float Hue_2_RGB(float v1, float v2, float vH )             //Function Hue_2_RGB
  {
     if ( vH < 0.0f ) vH += 1.0f;
     if ( vH > 1.0f ) vH -= 1.0f;
     if ( ( 6.0f * vH ) < 1.0f ) return ( v1 + ( v2 - v1 ) * 6.0f * vH );
     if ( ( 2.0f * vH ) < 1.0f ) return ( v2 );
     if ( ( 3.0f * vH ) < 2.0f ) return ( v1 + ( v2 - v1 ) * ( ( 2.0f / 3.0f ) - vH ) * 6.0f );
     return ( v1 );
  }


  public float getMaxValue(int cmp) {
    switch (cmp) {
      case 0:
        return (float) PI2;
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
        return "I";
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
