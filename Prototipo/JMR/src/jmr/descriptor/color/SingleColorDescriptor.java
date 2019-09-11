package jmr.descriptor.color;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import jmr.descriptor.Comparator;
import jmr.descriptor.MediaDescriptorAdapter;

/**
 * A descriptor representing a single color associated to a visual media.
 * 
 * @author Jesús Chamorro Martínez (jesus@decsai.ugr.es)
 */
public class SingleColorDescriptor extends MediaDescriptorAdapter<BufferedImage> implements Serializable {
    
    /**
     * Main color associated to this descriptor 
     */
    public Color color;
    
    /**
     * Constructs a single color descriptor, initializes it from the image 
     * given by parameter and set as comparator the default one.
     * 
     * @param image the image source
     */
    public SingleColorDescriptor(BufferedImage image) {
        super(image, new DefaultComparator()); //Implicit call to init       
    }   
    
    /**
     * Constructs a single color descriptor, initializes it with the given 
     * color and set as comparator the default one.
     * 
     * @param color the color to be set
     */
    public SingleColorDescriptor(Color color) {
        super(null, new DefaultComparator()); //Implicit call to init
        this.color = color;
    }
        
    /**
     * Initialize the descriptor as the mean color of the given image.
     *
     * @param image the media used for initializating this descriptor
     */
    @Override
    public void init(BufferedImage image) {
        color = image != null ? mean(image) : null;
    }
       
    /**
     * Returns the color associated to this descriptor
     * @return the color associated to this descriptor
     */
    public Color getColor(){
        return color;
    } 

    /**
     * Calculates the mean color of the given image.
     *
     * @param media the image. It must be not null and not empty
     * @return the mean color
     */
    private Color mean(BufferedImage image) {
        Color pixelColor;
        float mean[] = {0.0f, 0.0f, 0.0f}; //RGB
        double imageSize = image.getWidth() * image.getHeight();

        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                // Color conversion takes place in getRGB method, if necessary
                pixelColor = new Color(image.getRGB(x, y));
                mean[0] += pixelColor.getRed();
                mean[1] += pixelColor.getGreen();
                mean[2] += pixelColor.getBlue();
            }
        }
        mean[0] /= imageSize;
        mean[1] /= imageSize;
        mean[2] /= imageSize;
        
        return new Color((int) mean[0], (int) mean[1], (int) mean[2]);
    }
    
    /**
     * Returns a string representation of this descriptor.
     * 
     * @return a string representation of this descriptor 
     */
    @Override
    public String toString(){
        return "SingleColorDescriptor: [" + color.getRed() + "," + color.getGreen() + "," + color.getBlue()+"]";
    }
    
    /**
     * Functional (inner) class implementing a comparator between single color descriptors
     */
    static class DefaultComparator implements Comparator<SingleColorDescriptor, Double> {
        @Override
        public Double apply(SingleColorDescriptor t, SingleColorDescriptor u) {
            Color c1 = t.color, c2 = u.color;
            double rDif = Math.pow(c1.getRed()-c2.getRed(),2);
            double gDif = Math.pow(c1.getGreen()-c2.getGreen(),2);
            double bDif = Math.pow(c1.getBlue()-c2.getBlue(),2);
            return Math.sqrt(rDif+gDif+bDif);
        }    
    }
    
}
