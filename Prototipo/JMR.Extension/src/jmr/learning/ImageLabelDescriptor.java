/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jmr.learning;

import java.awt.image.BufferedImage;
import jmr.descriptor.label.LabelDescriptor;

/**
 *
 * @author mirismr
 */
public class ImageLabelDescriptor extends LabelDescriptor<BufferedImage> {

    public ImageLabelDescriptor(BufferedImage media) {
        super(media);
    }
    
    public ImageLabelDescriptor(String label) {
        super(label);
    }

    public ImageLabelDescriptor(String first, String[] queryLabel) {
        super(first, queryLabel);
    }
}
