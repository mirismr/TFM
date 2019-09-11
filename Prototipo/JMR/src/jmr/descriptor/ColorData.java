package jmr.descriptor;


import jmr.result.JMRResult;
import jmr.result.FloatResult;
import java.awt.Color;
import java.security.InvalidParameterException;


/**
 *
 *
 * @author Jesús Chamorro Martínez (jesus@decsai.ugr.es)
 */
/*
   Nota sobre versión de Soto: consideraba otro tipo de distancias además de
   la euclídea, y consideraba casos segun espacio. Realmente esto tendría que
   trasladarse a la clase que represente el color, no tanto a un descriptor.
*/
public class ColorData implements MediaDescriptor {

  /** The color of this descriptor */
  protected Color colorData;

  /**
     * Constructs a <code>ColorData</code> from a color.
     * @param color  The data color
     */
  public ColorData(Color color) { 
    colorData = color;
  }

  
//  public ColorData(Media media) {
//    //TODO  
//    colorData = null;
//  }
  
  
  /** Return the color associated to this ColorData object
   * @return The color data
   */
  public Color getColor() {
    return (colorData);
  }

  /** Set the color associated to this ColorData object
     * @param color
   */
  public void setColor(Color color) {
    colorData = color;
  }


  /** Calculates the Euclidean distance between colors
   * @param c1 fisrt color
   * @param c2 second color
   * @param normalize if <code>true</code>, the distance is normalized between
   * 0 and 1
   * @return A float value corresponding to the euclidean distance between colors
   */
  private float distance(Color c1, Color c2, boolean normalize) {
    if(c1.getColorSpace().getType() != c2.getColorSpace().getType())  {       
        throw new InvalidParameterException("Colors must be in the same color space.");
    }
        
    double dist = 0.0, dc;
    float c1Components[] = c1.getColorComponents(null); // In [0,1]
    float c2Components[] = c2.getColorComponents(null);
    for (int i = 0; i < c1Components.length; i++) {      
      dc = c1Components[i] - c2Components[i];
      dist += (dc * dc);
    }
    dist = Math.sqrt(dist);  //Components are between 0 and 1, so the euclidean
    if(normalize)            //distance is between 0 and sqrt(num_componets)
        dist/=Math.sqrt(c1Components.length); 
    return ( (float) dist);
  }

  /** Compares this ColorData obtect with the ColorData given by parameter
   * @param color ColoData object to be compared
   * @return A float result corresponding to the distance between colors
   */
  public FloatResult compare(ColorData color) {
    float distance = distance(color.colorData, this.colorData,true);
    return ( new FloatResult(distance) );
  }

  /** Compare this ColorData obtect with the ColorData given by parameter
   * <p> This method is valid only for Colodata media descriptors
   * @param mediaDescriptor MediaDescriptor object to be compared
   * @see #compare(ColorData color)
   * @return The difference between descriptors
   */
  @Override
  public JMRResult compare(MediaDescriptor mediaDescriptor) {
    // Only ColorData objects can be compared
    if (! (mediaDescriptor instanceof ColorData)) {
      return (null);
    }
    return ( compare((ColorData) mediaDescriptor) );
  }

    

    @Override
    public void init(Object media) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
