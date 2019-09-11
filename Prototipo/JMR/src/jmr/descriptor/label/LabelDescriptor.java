package jmr.descriptor.label;

import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;
import jmr.descriptor.Comparator;
import jmr.descriptor.MediaDescriptorAdapter;

/**
 * A descriptor representing a list of labels associated to a visual media.
 *
 * @param <T> the type of media described by this descriptor.
 *
 * @author Jesús Chamorro Martínez (jesus@decsai.ugr.es)
 */
public class LabelDescriptor<T> extends MediaDescriptorAdapter<T> implements Serializable {

    /**
     * List of labels associated to this descriptor.
     */
    private List<String> labels;
    /**
     * List of weights associated to this descriptor.
     */
    private List<Double> weights;
    /**
     * A classifier used for labeling a given media. It uses a standard
     * functional interface, allowing lambda expressions.
     */
    private transient Classifier<T, ? extends LabeledClassification> classifier = null;
    /**
     * Comparator used by default.
     */
    static private Comparator DEFAULT_COMPARATOR = new InclusionComparator();
    /**
     * Comparator used by default.
     */
    static private Comparator DEFAULT_WEIGHTED_COMPARATOR = new WeightBasedComparator(WeightBasedComparator.TYPE_MIN, true);
    /**
     * Classifier used by default.
     */
    static private Classifier DEFAULT_CLASSIFIER = new DefaultClassifier();

    /**
     * Constructs a multiple label descriptor using the default classifier to
     * label the given media, and set as comparator the default one.
     *
     * @param media the media source
     */
    public LabelDescriptor(T media) {
        this(media, DEFAULT_CLASSIFIER);
        // Particular case. If a new object is constructed by passing a single 
        // String, this constructor is called instead of the specific one for 
        // String type. Only works as expected if the 'new' operation is made 
        // by indicating explicitly the type (new LabelDescriptor<T>), but
        // probably this will no be the case. In order to avoid confusing 
        // results, this particular case is considered
        /*if (media != null && media.getClass() == String.class) {
            setSource(null); // Source and labels are set to null, but not the 
            // comparator and classifier 
            this.labels = new ArrayList();
            this.labels.add((String) media);
            this.weights = null;
        }*/
    }

    /**
     * Constructs a multiple label descriptor using the given classifier to
     * label the given image, and set as comparator the default one.
     *
     * @param media the media source
     * @param classifier the classifier used for labeling a given media. The
     * result type of the classifier must be <code>List&lt;String&gt;</code>
     */
    public LabelDescriptor(T media, Classifier classifier) {
        super(media, DEFAULT_COMPARATOR); //Implicit call to init 
        // The previous call does not initialize the label since the classifier
        // has not been assigned yet. Therefore, in the following sentences the
        // classifier data member is initialize and then used for obtaining the
        // label of this descriptor
        this.setClassifier(classifier);
        this.init(media); //Second call, but needed (see init method)
    }

    /**
     * Constructs a multiple label descriptor, initializes it with the given
     * labels and set as comparator and classifier the default ones. No weights
     * are set by default.
     *
     * @param label the first label of this descriptor.
     * @param labels the second and following labels of this descriptor.
     */
    public LabelDescriptor(String label, String... labels) {
        this((T) null); //Default comparator and classifier; null source
        this.labels = new ArrayList(Arrays.asList(labels));
        this.labels.add(0, label);
        this.weights = null;
    }

    /**
     * Initialize the descriptor by using the classifier.
     *
     * @param media the media used for initializating this descriptor
     */
    @Override
    public void init(T media) {
        if (media != null && classifier != null) {
            LabeledClassification classification = classifier.apply(media);
            labels = classification.getLabels();
            weights = classification.getWeights();
            if (weights != null) {
                this.setComparator(DEFAULT_WEIGHTED_COMPARATOR);
            }
        } else {
            labels = null;
            weights = null;
        }
        // When this method is called from the superclass constructor, the local
        // member data, and particularly the classifier, are not initialized 
        // yet. Thus, in the construction process, the previous code always 
        // initializes the labels and weights to null. For this reason, after the
        // super() call in the constructor, we have to (1) initialize the rest 
        // of the descriptor (particularly the classifier) and (2) to calculte 
        // the labels and weights again (for example, calling this init method 
        // again).
        //
        // Note that this method is not only called from the constructor, it is 
        // also called from the setSource method (which allow to chage de media
        // and, consequently, it changes the label using the current classidier
    }

    /**
     * Returns the number of labels in this descriptor.
     *
     * @return the number of labels in this descriptor.
     */
    public int size() {
        return labels != null ? labels.size() : 0;
    }

    /**
     * Returns <tt>true</tt> if this descriptor contains no labels.
     *
     * @return <tt>true</tt> if this descriptor contains no labels.
     */
    public boolean isEmpty() {
        return labels != null ? labels.isEmpty() : true;
    }

    /**
     * Returns <tt>true</tt> if this descriptor is weighted.
     *
     * @return <tt>true</tt> if this descriptor is weighted, <tt>false</tt> in
     * other case.
     */
    public boolean isWeighted() {
        return weights != null;
    }

    /**
     * Returns the label at the specified position in this descriptor.
     *
     * @param index index of the label to return
     * @return the label at the specified position in this descriptor
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public String getLabel(int index) {
        return labels.get(index);
    }

    /**
     * Returns the weight at the specified position in this descriptor.
     *
     * @param index index of the weight to return
     * @return the weight at the specified position in this descriptor
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public Double getWeight(int index) {
        return weights != null ? this.weights.get(index) : null;
    }

    /**
     * Returns the weight of the given label.
     *
     * @param label the label to serach for in this descriptor.
     * @return the weight of the label, <code>null</code> if the label is not in
     * this descriptor or if there are not weigts associated to this descriptor.
     */
    public Double getWeight(String label) {
        int index = labels.indexOf(label);
        return index != -1 && weights != null ? weights.get(index) : null;
    }

    /**
     * Set the weights associated to the labels of this descriptor.
     *
     * @param weights the weights associated to this descriptor.
     */
    public void setWeights(Double... weights) {
        if (weights.length != this.labels.size()) {
            throw new InvalidParameterException("The number of weight must be " + this.labels.size());
        }
        this.weights = new ArrayList(Arrays.asList(weights));
    }

    /**
     * Set the classifier for this descriptor.
     *
     * @param classifier the new classifier. The result type of the classifier
     * must be of type {@link jmr.descriptor.label.LabeledClassification}. If
     * the given parameter is null, the default clasifier is assigned.
     */
    public void setClassifier(Classifier<T, ? extends LabeledClassification> classifier) {
        this.classifier = classifier != null ? classifier : new DefaultClassifier();
        // No null classifier is allowed. If the given parameter is null, the 
        // default one is used.
    }

    /**
     * Returns the classifier of this descriptor.
     *
     * @return the classifier of this descriptor.
     */
    public Classifier getClassifier() {
        return classifier;
    }

    /**
     * Set the default classifier for this class. This classifer is used when a
     * specific one is not provided in the object construction.
     *
     * @param classifier the new classifier. The result type of the classifier
     * must be of type {@link jmr.descriptor.label.LabeledClassification}. If
     * the given parameter is null, a {@link #DEFAULT_CLASSIFIER} clasifier is
     * assigned.
     */
    static public void setDefaultClassifier(Classifier classifier) {
        DEFAULT_CLASSIFIER = classifier != null ? classifier : new DefaultClassifier();
        // No null classifier is allowed. If the given parameter is null, the 
        // default one is used.
    }
    
    static public void setDefaultWeightComparator (Comparator comparator) {
        DEFAULT_WEIGHTED_COMPARATOR = comparator != null ? comparator : new WeightBasedComparator(WeightBasedComparator.TYPE_MIN, true);
        // No null classifier is allowed. If the given parameter is null, the 
        // default one is used.
    }

    static public Classifier getDefaultClassifier() {
        return DEFAULT_CLASSIFIER;
    }

    /**
     * Set the default comparator for this class. This comparator is used when a
     * specific one is not provided in the object construction.
     *
     * @param comparator the new comparator. If the given parameter is null, a
     * {@link #DEFAULT_COMPARATOR} comparator is assigned.
     */
    static public void setDefaultComparator(Comparator comparator) {
        DEFAULT_COMPARATOR = comparator != null ? comparator : new EqualComparator();
        // No null comparator is allowed. If the given parameter is null, the 
        // default one is used.
    }

    /**
     * Returns a string representation of this descriptor.
     *
     * @return a string representation of this descriptor
     */
    @Override
    public String toString() {
        return weights == null ? labels.toString() : toStringWeighted();
    }

    /**
     * Returns a string representation of this descriptor with both the labels
     * and the weights.
     *
     * @return a string representation of this descriptor
     */
    private String toStringWeighted() {
        String output = "[";
        for (int i = 0; i < size(); i++) {
            output += "(" + labels.get(i) + "," + weights.get(i) + ")";
        }

        return output + "]";
    }

    /**
     * Returns <tt>true</tt> if the labels of this descriptor are included in
     * the one given by parameter.
     *
     * @param u the second label descriptor.
     * @return <tt>true</tt> if the descriptor <tt>t</tt> is included in the
     * descriptor <tt>u</tt>, <tt>false</tt> in other case.
     */
    public boolean isIncluded(LabelDescriptor u) {
        int equal;
        String label_i;
        for (int i = 0; i < this.size(); i++) {
            label_i = this.getLabel(i);
            equal = 1;
            // We search the nearest one
            for (int j = 0; j < u.size() && equal != 0; j++) {
                equal = label_i.compareToIgnoreCase(u.getLabel(j)); // 0 if equals
            }
            if (equal != 0) {
                return false; //Same label not found
            }
        }
        return true; //If this descriptor has not labels, it is included in u
    }

    /**
     * Returns <tt>true</tt> if at least one label of this descriptor is
     * included in the descriptor given by parameter.
     *
     * @param u the second label descriptor.
     * @return <tt>true</tt> if the descriptor <tt>t</tt> has at least one label
     * included in the descriptor <tt>u</tt>, <tt>false</tt> in other case.
     */
    public boolean isSoftIncluded(LabelDescriptor u) {
        int equal;
        String label_i;
        if (this.size() == 0) {
            return true; //If this descriptor has not labels, it is included in u
        }
        for (int i = 0; i < this.size(); i++) {
            label_i = this.getLabel(i);
            for (int j = 0; j < u.size(); j++) {
                equal = label_i.compareToIgnoreCase(u.getLabel(j)); // 0 if equals
                if (equal == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns a value related to the distance in which this descriptor is
     * included in the one given by parameter. This method is used in comparator
     * inner classes.
     *
     * @param u the second label descriptor.
     * @param op_init the unary operator used to initialize the distance
     * accumulator (as a function of the fisrt distance).
     * @param op_aggregation the binary operator used to aggregate a new
     * distance to the previous ones.
     * @return a value related to the degree in which the first descriptor is
     * included in the second one (Double.POSITIVE_INFINITY if some label is not
     * included)
     */
    private Double inclusionDistance(LabelDescriptor u,
            UnaryOperator<Double> op_init, BinaryOperator<Double> op_aggregation) {
        int equal;
        String label_i;
        Double dist = null, dist_ij = null;
        for (int i = 0; i < this.size(); i++) {
            label_i = this.getLabel(i);
            equal = 1;
            // We search the same label
            for (int j = 0; j < u.size() && equal != 0; j++) {
                equal = label_i.compareToIgnoreCase(u.getLabel(j)); // 0 if equals                  
                if (equal == 0) {
                    //We assume that the distance is given by the first coincidence
                    dist_ij = Math.abs(this.getWeight(i) - u.getWeight(j));
                }
            }
            if (equal != 0) {
                return Double.POSITIVE_INFINITY; //Same label not found
            } else {
                dist = dist == null ? op_init.apply(dist_ij) : op_aggregation.apply(dist, dist_ij);
            }
        }
        return dist != null ? dist : op_init.apply(0.0);  //If this descriptor has not labels, it is included in u 
    }

    /**
     * Functional (inner) class implementing the inclusion comparator between
     * label descriptors.
     */
    static public class InclusionComparator implements Comparator<LabelDescriptor, Double> {

        @Override
        public Double apply(LabelDescriptor t, LabelDescriptor u) {

            return t.isIncluded(u) ? 0.0 : Double.POSITIVE_INFINITY;
        }
    }

    /**
     * Functional (inner) class implementing the equal comparator between label
     * descriptors. It returns 0.0 if the labels are the same in both
     * descriptors without repetitions (ignoring upper cases and position),
     * {@link java.lang.Double.POSITIVE_INFINITY} in other case.
     */
    static public class EqualComparator implements Comparator<LabelDescriptor, Double> {

        @Override
        public Double apply(LabelDescriptor t, LabelDescriptor u) {
            System.out.println(t);
            System.out.println(u);
            // If the number of labels is not the same, the descriptors are 
            // assumed to be different
            if (t.size() != u.size()) {
                return Double.POSITIVE_INFINITY;
            }
            return (t.isIncluded(u) && u.isIncluded(t)) ? 0.0 : Double.POSITIVE_INFINITY;
        }
    }

    /**
     * Functional (inner) class implementing the 'soft' equal comparator between
     * label descriptors. It returns 0.0 if the labels of one descriptor are
     * included in the other, or viceversa (ignoring upper cases and position),
     * {@link java.lang.Double.POSITIVE_INFINITY} in other case.
     */
    static public class SoftEqualComparator implements Comparator<LabelDescriptor, Double> {

        @Override
        public Double apply(LabelDescriptor t, LabelDescriptor u) {
            return (t.isIncluded(u) || u.isIncluded(t)) ? 0.0 : Double.POSITIVE_INFINITY;
        }
    }

    /**
     * Functional (inner) class implementing the weighted comparator between
     * label descriptors. This comparator uses the weights associated to the
     * labels to return a distance between descriptors. It tests equality (by
     * default) or inclusion.
     *
     * The comparator returns Double.POSITIVE_INFINITY if the labels are
     * diferent (in the test based on equality) or if some label is not included
     * (in the test based on inclusion).
     */
    static public class WeightBasedComparator implements Comparator<LabelDescriptor, Double> {

        /**
         * Type of distance aggregation based on the maximum.
         */
        static public final int TYPE_MAX = 1;
        /**
         * Type of distance aggregation based on the minimum.
         */
        static public final int TYPE_MIN = 2;
        /**
         * Type of distance aggregation based on the mean.
         */
        static public final int TYPE_MEAN = 3;
        /**
         * Type of distance aggregation based on the euclidean distance.
         */
        static public final int TYPE_EUCLIDEAN = 4;
        /**
         * The tye of aggregation used in this comparator
         */
        private int type;
        /**
         * If true, only inclusion is tested (not equality)
         */
        boolean only_inclusion;
        /**
         * The unary operator used to initialize the distance accumulator (as a
         * function of the fisrt distance).
         */
        private transient UnaryOperator<Double> op_init;  // Class non serializable 
        /**
         * The binary operator used to aggregate a new distance to the previous
         * ones.
         */
        private transient BinaryOperator<Double> op_aggregation; // Class non serializable

        /**
         * Constructs a new comparator based on the given type of distance
         * aggregation.
         *
         * @param type the type of distance aggregation.
         * @param only_inclusion if <tt>true</tt>, only inclusion is tested; if
         * <tt>false</tt>, equality is tested;
         */
        public WeightBasedComparator(int type, boolean only_inclusion) {
            switch (type) {
                case TYPE_MAX:
                    op_init = (a) -> a;
                    op_aggregation = (a, b) -> Math.max(a, b);
                    break;
                case TYPE_MIN:
                    op_init = (a) -> a;
                    op_aggregation = (a, b) -> Math.min(a, b);
                    break;
                case TYPE_MEAN:
                    op_init = (a) -> a;
                    op_aggregation = (a, b) -> (a + b);
                    break;
                case TYPE_EUCLIDEAN:
                    op_init = (a) -> (a * a);
                    op_aggregation = (a, b) -> (a + b * b);
                    break;
                default:
                    throw new InvalidParameterException("Invalid distance aggregator type");
            }
            this.type = type;
            this.only_inclusion = only_inclusion;
        }

        /**
         * Constructs a new comparator based on the given type of distance
         * aggregation and using by default the equality test.
         *
         * @param type the type of distance aggregation.
         */
        public WeightBasedComparator(int type) {
            this(type, false);
        }

        /**
         * Constructs a new comparator using by default the
         * {@link #TYPE_EUCLIDEAN} type for distance aggregation, and the
         * equality test.
         */
        public WeightBasedComparator() {
            this(TYPE_EUCLIDEAN, false);
        }

        /**
         * Applies this comparator to the given arguments.
         *
         * @param t the first function argument
         * @param u the second function argument
         * @return the function result
         */
        @Override
        public Double apply(LabelDescriptor t, LabelDescriptor u) {
            if (!only_inclusion && t.size() != u.size()) {
                return Double.POSITIVE_INFINITY;
            }
            // If the size is equal, and the labels are the same, the distance 
            // between t and u will be given by the inclusion of t in u (which 
            // will be the same that the inclusion of u in t). If the labels are
            // different, the inclusion will be Double.POSITIVE_INFINITY
            Double output = t.inclusionDistance(u, op_init, op_aggregation);
            if (type == TYPE_MEAN) {
                return output / t.size();
            }
            if (type == TYPE_EUCLIDEAN) {
                return Math.sqrt(output);
            }
            return output;
        }
    }

    /**
     * Functional (inner) class implementing a default classifier. This
     * implementation labels the media with only one label equals to the
     * (simple) name of its class.
     */
    static private class DefaultClassifier<T> implements Classifier<T, LabeledClassification> {

        @Override
        public LabeledClassification apply(T t) {
            ArrayList<String> list = new ArrayList();
            if (t != null) {
                list.add(t.getClass().getSimpleName());
            }
            return new LabeledClassification() {
                public List<String> getLabels() {
                    return list;
                }

                public boolean isWeighted() {
                    return false;
                }

                public List<Double> getWeights() {
                    return null;
                }
            };
        }
    }

    /**
     * Particular case of a {@link jmr.descriptor.label.LabelDescriptor} when
     * the media is a {@link java.awt.image.BufferedImage}. Although the
     * standard <code>LabelDescriptor</code> could be used, sometimes it is
     * useful a specific class for a given type (for example, when the
     * {@link jmr.descriptor.MediaDescriptorFactory} class is used for building
     * objetcs -as in the {@link jmr.descriptor.GriddedDescriptor} descriptor-)
     */
    static public class ImageLabelDescriptor extends LabelDescriptor<BufferedImage> {

        /**
         * Constructs a multiple label descriptor using the default classifier
         * to label the given image, and set as comparator the default one.
         *
         * @param img the image source
         */
        public ImageLabelDescriptor(BufferedImage img) {
            super(img);
        }

        /**
         * Constructs a multiple label descriptor, initializes it with the given
         * labels and set as comparator and classifier the default ones. No
         * weights are set by default.
         *
         * @param label the first label of this descriptor.
         * @param labels the second and following labels of this descriptor.
         */
        public ImageLabelDescriptor(String label, String... labels) {
            super(label, labels);
        }
    }
}
