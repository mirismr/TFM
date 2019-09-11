package jmr.video;

import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import jmr.descriptor.Comparator;
import jmr.descriptor.MediaDescriptor;
import jmr.descriptor.MediaDescriptorAdapter;
import jmr.descriptor.MediaDescriptorFactory;
import jmr.descriptor.color.MPEG7ScalableColor;

/**
 * Video descriptor defined as a collection of key frames descriptors.
 *
 * @author Jesús Chamorro Martínez (jesus@decsai.ugr.es)
 */
public class KeyFrameDescriptor extends MediaDescriptorAdapter<Video> implements Serializable {

    /**
     * List of keyframe descriptors
     */
    private ArrayList<MediaDescriptor<BufferedImage>> descriptors;
    /**
     * Default descriptor class used for key frame description.
     */
    public static final Class DEFAULT_FRAME_DESCRIPTOR = MPEG7ScalableColor.class;
    

    /**
     * Constructs a key frame based video descriptor.
     *
     * @param video the source video.
     * @param keyFrameIterator video iterator which produces the keyframes.
     * @param frameDescriptorClass descriptor class for each keyframe.
     */
    public KeyFrameDescriptor(Video video, VideoIterator<BufferedImage> keyFrameIterator, Class<? extends MediaDescriptor> frameDescriptorClass) {
        super(video, new DefaultComparator());
        // The previous call does not initialize the keyframe descriptors. It  
        // will be done in the following setKeyFrameDescriptors() call
        this.setKeyFrameDescriptors(frameDescriptorClass, keyFrameIterator);
    }

    /**
     * Constructs a key frame based video descriptor using a default iterator
     * which goes over all the frames in the video
     *
     * @param video the source video.
     * @param frameDescriptorClass descriptor class for each keyframe.
     */
    public KeyFrameDescriptor(Video video, Class<? extends MediaDescriptor> frameDescriptorClass) {
        this(video, VideoIterator.getDefault(video), frameDescriptorClass);
    }

    /**
     * Constructs a key frame based video descriptor where all the frames in the
     * video are described by means a descriptor of the class
     * {@link #DEFAULT_FRAME_DESCRIPTOR}.
     *
     * @param video the source video.
     */
    public KeyFrameDescriptor(Video video) {
        this(video, VideoIterator.getDefault(video), DEFAULT_FRAME_DESCRIPTOR);
    }

    /**
     * First initialization of the descriptor as an empty list of descriptor.
     *
     * Later, the list should be filled in with the descriptors of each tile (by
     * calling {@link #setKeyFrameDescriptors(java.lang.Class, jmr.video.VideoIterator) }).
     *
     * @param video the media associated to this descriptor
     */
    @Override
    public void init(Video video) {
        descriptors = new ArrayList<>();
        // We also should add to the list the keyframe descriptors, but this 
        // method is call from the superclass constructor so, when this code is
        // executed, the local member data (used for constructing the keyframe 
        // descriptors) are no initialized yet. Thus, after the super() call 
        // in the construtor, we have to initialize the rest of the descriptor.
    }

    /**
     * Set the list of descriptor by calculating a descriptor for each keyframe.
     *
     * @param descriptorClass descriptor class for each keyframe
     * @param keyFrameIterator video iterator which produces the keyframes.
     */
    private void setKeyFrameDescriptors(Class descriptorClass, VideoIterator<BufferedImage> keyFrameIterator) {
        if (keyFrameIterator.getVideo() != source) {
            throw new InvalidParameterException("Different video sources.");
        }
        if (!descriptors.isEmpty()) {
            descriptors.clear();
        }
        BufferedImage keyframe;
        MediaDescriptor descriptor;

        keyFrameIterator.init();
        while (keyFrameIterator.hasNext()) {
            keyframe = keyFrameIterator.next();
            descriptor = MediaDescriptorFactory.getInstance(descriptorClass, keyframe);
            descriptors.add(descriptor);
        }
    }
    
    /**
     * Returns the list of key frame descriptors.
     * 
     * @return the list of key frame descriptors.
     */
    public List getDescriptors(){
        return java.util.Collections.unmodifiableList(descriptors);
    }
    
    /**
     * Returns a string representation of this descriptor
     * .
     * @return a string representation of this descriptor 
     */
    @Override
    public String toString(){
        String output ="";
        for(MediaDescriptor descriptor : descriptors){
            output += descriptor.toString()+"\n";
        }
        return output;
    }
    
    /**
     * Functional (inner) class implementing the default comparator between key
     * frame descriptors. As default comparator, the minimun distance between
     * frames is used.
     */
    static public class DefaultComparator implements Comparator<KeyFrameDescriptor, Double> {

        @Override
        public Double apply(KeyFrameDescriptor t, KeyFrameDescriptor u) {
            Double min_distance = Double.MAX_VALUE;
            try {
                Double item_distance;
                MediaDescriptor m1, m2;
                for (int i = 0; i < t.descriptors.size(); i++) {
                    m1 = t.descriptors.get(i);
                    for (int j = 0; j < u.descriptors.size(); j++) {
                        m2 = u.descriptors.get(j);
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
            return min_distance;
        }
    }

}
