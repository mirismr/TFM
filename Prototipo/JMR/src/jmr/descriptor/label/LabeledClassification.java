
package jmr.descriptor.label;

import java.io.Serializable;
import java.util.List;

/**
 * Represents a classifier output based on labels.
 *
 */

public interface LabeledClassification extends Serializable{
    /**
     * 
     * @return 
     */
    public List<String> getLabels();
    /**
     * 
     * @return 
     */
    public boolean isWeighted();
    /**
     * 
     * @return 
     */
    public List<Double> getWeights();
}
