package jmr.descriptor.label;

import java.io.Serializable;
import jmr.descriptor.Comparator;
import jmr.descriptor.MediaDescriptorAdapter;

/**
 * A descriptor representing a single label associated to a visual media.
 * 
 * @param <T> the type of media described by this descriptor.
 * 
 * @author Jesús Chamorro Martínez (jesus@decsai.ugr.es)
 */
public class SingleLabelDescriptor<T> extends MediaDescriptorAdapter<T> implements Serializable {   
    /**
     * Label associated to this descriptor.
     */
    private String label;
    /**
     * Weight associated to this descriptor.
     */
    private Double weight;
    /**
     * A classifier used for labeling a given media. It uses a standard
     * functional interface, allowing lambda expressions.
     */
    private Classifier<T, String> classifier = null;
    /**
     * Comparator used by defayult.
     */
    static private Comparator DEFAULT_COMPARATOR = new DefaultWeightedComparator();
    /**
     * Classifier used by default.
     */
    static private Classifier DEFAULT_CLASSIFIER = new DefaultClassifier();
    /**
     * The output of the default comparator when the labes are equals.
     */
    static private Double DEFAULT_MAX_DIFFERENCE = 1.0;
    

    /**
     * Constructs a single label descriptor using the default classifier to 
     * label the given image, and set as comparator the default one. The weight 
     * is set to 1.0.
     * 
     * @param media the media source
     */
    public SingleLabelDescriptor(T media) {
        this(media, DEFAULT_CLASSIFIER); 
    }   
    
    /**
     * Constructs a single label descriptor using the given classifier to label
     * the given image, and set as comparator the default one. The weight is set 
     * to 1.0.
     *
     * @param media the media source
     * @param classifier the classifier used for labeling a given media. The
     * result type of the classifier must be <code>String</code>.
     */
    public SingleLabelDescriptor(T media, Classifier classifier) {
        super(media, DEFAULT_COMPARATOR); //Implicit call to init 
        // The previous call does not initialize the label since the classifier
        // has not been assigned yet. Therefore, in the following sentences the
        // classifier data member is initialize and then used for obtaining the
        // label of this descriptor
        this.classifier = classifier;
        this.weight = 1.0;
        this.init(media); //Second call, but needed (see init method)
    }
    
    /**
     * Constructs a single label descriptor, initializes it with the given 
     * label and weight, and set as comparator and classifier the default ones.
     * 
     * @param label the label to be set
     * @param weight the weight to be set.
     */
    public SingleLabelDescriptor(String label, Double weight) {
        this((T)null); //Default comparator and classifier; null source
        this.label = label;
        this.weight = weight;
    }
    
    /**
     * Constructs a single label descriptor, initializes it with the given 
     * label and set as comparator and classifier the default ones. The weight
     * is set to 1.0.
     * 
     * @param label the label to be set
     */
    public SingleLabelDescriptor(String label) {
        this(label,1.0);
    }
    
    /**
     * Initialize the descriptor by using the classifier.
     *
     * @param media the media used for initializating this descriptor
     */
    @Override
    public void init(T media) {
        label = media!=null && classifier!=null ? classifier.apply(media) : null;
        // When this method is called from the superclass constructor, the local
        // member data, and particularly the classifier, are not initialized 
        // yet. Thus, in the construction process, the previous code always 
        // initializes the label to null. For this reason, after the super() 
        // call in the constructor, we have to (1) initialize the rest of the 
        // descriptor (particularly the classifier) and (2) to calculte the
        // label again (for example, calling this init method again).
        //
        // Note that this method is not only called from the constructor, it is 
        // also called from the setSource method (which allow to chage de media
        // and, consequently, it changes the label using the current classidier
    }
       
    /**
     * Returns the label associated to this descriptor
     * @return the label associated to this descriptor
     */
    public String getLabel(){
        return label;
    } 
    
    /**
     * Set the classifier for this descriptor.
     *
     * @param classifier the new classifier. The result type of the classifier
     * must be <code>String</code>
     */
    public void setClassifier(Classifier<T, String> classifier){
        this.classifier = classifier;
    }
    
    /**
     * Returns the classifier of this descriptor. 
     * 
     * @return the classifier of this descriptor. 
     */
    public Classifier getClassifier(){
        return classifier;
    }
    
    
    /**
     * Set the weight for this descriptor.
     *
     * @param weight the new weight. 
     */
    public void setWeight(Double weight){
        this.weight = weight;
    }
    
    /**
     * Returns the weight of this descriptor. 
     * 
     * @return the weight of this descriptor. 
     */
    public Double getWeight(){
        return weight;
    }
    
    
    /**
     * Returns a string representation of this descriptor.
     * 
     * @return a string representation of this descriptor 
     */
    @Override
    public String toString(){
        return this.getClass().getSimpleName()+": ["+label+"]";
    }
    
    /**
     * Functional (inner) class implementing a comparator between single label
     * descriptors. It returns 1.0 if the labels are different and 0.0 if they 
     * are equals (ignoring upper cases).
     */
    static public class DefaultComparator implements Comparator<SingleLabelDescriptor, Double> {
        @Override
        public Double apply(SingleLabelDescriptor t, SingleLabelDescriptor u) {
            int equal = t.label.compareToIgnoreCase(u.label);
            return equal == 0 ? 0.0 : DEFAULT_MAX_DIFFERENCE;
        }
    } 
    
    /**
     * Functional (inner) class implementing a comparator between single label
     * descriptors. It returns 1.0 if the labels are different and the weight
     * (positive) difference if they are equals (ignoring upper cases).
     */
    static public class DefaultWeightedComparator implements Comparator<SingleLabelDescriptor, Double> {
        @Override
        public Double apply(SingleLabelDescriptor t, SingleLabelDescriptor u) {
            int equal = t.label.compareToIgnoreCase(u.label);
            return equal == 0 ? Math.abs(t.weight-u.weight) : DEFAULT_MAX_DIFFERENCE;
        }
    }
    
    /**
     * Functional (inner) class implementing a default classifier. This
     * implementation labels the media by the (simple) name of its class.
     */
    static private class DefaultClassifier<T> implements Classifier<T, String> {
        @Override
        public String apply(T t) {
            return (t!=null) ? t.getClass().getSimpleName() : "";
        }
    }
}
