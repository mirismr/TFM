package jmr.tools;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 *
 * @author Jesús Chamorro Martínez (jesus@decsai.ugr.es)
 */
public class JMRImageTools {
    
    public static BufferedImage resize(BufferedImage img, Dimension new_size){
        BufferedImage output = new BufferedImage(new_size.width,new_size.height,img.getType());
        Graphics2D gImg = output.createGraphics();
        gImg.drawImage(img,0,0,new_size.width,new_size.height,null);
        return output;
    }
}
