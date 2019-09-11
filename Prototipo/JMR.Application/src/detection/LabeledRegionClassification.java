/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package detection;

import java.awt.geom.Rectangle2D;
import java.util.List;
import jmr.descriptor.label.LabeledClassification;

/**
 *
 * @author mirismr
 */
public interface LabeledRegionClassification extends LabeledClassification{
    
    /**
    * 
    * @return 
    */
    public List<Rectangle2D> getBoundingBoxs();
    
}
