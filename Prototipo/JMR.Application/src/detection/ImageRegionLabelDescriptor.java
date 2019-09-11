/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package detection;

import java.awt.geom.Rectangle2D;
import java.util.List;
import jmr.descriptor.label.Classifier;
import jmr.descriptor.label.LabelDescriptor;


public class ImageRegionLabelDescriptor extends LabelDescriptor<String> {
    
    private List<Rectangle2D> boundingBoxs;

    public ImageRegionLabelDescriptor(String pathMedia, Classifier classifier) {
        super(pathMedia, classifier);         
    }   
    public ImageRegionLabelDescriptor(String pathMedia) {
        this(pathMedia, LabelDescriptor.getDefaultClassifier());
    }
    
    public ImageRegionLabelDescriptor(String first, String[] queryLabel) {
        super(first, queryLabel);
    }
    
    
    /**
     * Initialize the descriptor by using the classifier.
     *
     * @param pathMedia the path media used for initializating this descriptor
     */
    @Override
    public void init(String pathMedia) {   
        if (super.getClassifier() != null) {
            super.init(pathMedia);
            if (!super.getClassifier().getClass().getSimpleName().equals("DefaultClassifier")) {
                LabeledRegionClassification classification = (LabeledRegionClassification) (super.getClassifier().apply(pathMedia));
                this.boundingBoxs = classification.getBoundingBoxs();
            }
        }
    }
    
    public Rectangle2D getBoundingBox(int index){
        return this.boundingBoxs.get(index);
    }
}
