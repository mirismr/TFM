/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jmr.video;

import java.security.InvalidParameterException;
import jmr.descriptor.Comparator;
import jmr.descriptor.MediaDescriptor;

/**
 * Example of a KeyFrameDescriptor comparator for teaching purposes (it is the
 * default one implemented in the KeyFrameDescriptor, but as an independent 
 * class)
 * 
 * @author Jesús Chamorro Martínez (jesus@decsai.ugr.es)
 */
public class MinMinComparator implements Comparator<KeyFrameDescriptor, Double> {
        @Override
        public Double apply(KeyFrameDescriptor t, KeyFrameDescriptor u) {
            Double min_distance = Double.MAX_VALUE;
            try {
                Double item_distance;
                MediaDescriptor m1, m2;
                for (int i = 0; i < t.getDescriptors().size(); i++) {
                    m1 = (MediaDescriptor)t.getDescriptors().get(i);
                    for (int j = 0; j < u.getDescriptors().size(); j++) {
                        m2 = (MediaDescriptor)u.getDescriptors().get(j);
                        item_distance = (Double) m1.compare(m2);
                        if (item_distance < min_distance) {
                            min_distance = item_distance;
                        }
                    }
                }
            } catch (ClassCastException e) {
                throw new InvalidParameterException("The comparision between descriptors is not interpetrable as a double value.");
            } catch (Exception e) {
                throw new InvalidParameterException("The descriptors are not comparables.");
            }
            return min_distance*min_distance;
        }

}
