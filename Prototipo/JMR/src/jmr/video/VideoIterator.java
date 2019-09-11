package jmr.video;

import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Interface representing an iterator over a video. In each iteration, it
 * produces a an object of type <tt>T</tt>.
 * 
 * A default iterator is provided which goes over all the frames in the video
 * and, in each iteration, produces a video frame of type
 * {@link java.awt.image.BufferedImage}.
 *
 * @param <T> the type of elements returned by this iterator.
 * 
 * @author Jesús Chamorro Martínez (jesus@decsai.ugr.es)

 */
public interface VideoIterator<T> extends Iterator<T>{
    /**
     * Set the video source of this iterator.
     * 
     * @param video the video source.
     */
    public void setVideo(Video video);
    
    /**
     * Returns the video source of this iterator.
     * 
     * @return the video source of this iterator.
     */
    public Video getVideo();
    
    /**
     * Sets iterator position to the initial one.
     */
    public void init();
    
    /**
     * Returns a default video iterator that goes over all the frames in the
     * image
     *
     * @param video the video source of the iterator.
     * @return a default video iterator.
     */
    static public VideoIterator getDefault(Video video){
        return new AllFrames(video);
    }
    
    /**
     * Inner class representing video iterator that goes over all the frames in
     * the video.
     */
    public static final class AllFrames implements VideoIterator<BufferedImage>{
        /**
         * The source video associated to this iterator.
         */
        Video source;
        /**
         * The length of the video (that is, the number of frames)
         */
        private int length=0;
        /**
         * Current position in the iteration.
         */
        int index;
        
        /**
         * Constructs a new iterator for the given video.
         *
         * @param video the source video associated to this iterator.
         */
        public AllFrames(Video video){
            setVideo(video);
        }
        
        /**
         * Set the source video and initializes the local parameters.
         *
         * @param video the source video.
         */
        @Override
        public void setVideo(Video video) {
            this.source = video;            
            if (video != null) {                
                index = 0;
                length = video.getNumberOfFrames();
            } else {
                index = length = 0;
            }
        }

        /**
         * Returns the source video associated to this iterator.
         * 
         * @return the source video associated to this iterator.
         */
        @Override
        public Video getVideo() {
            return source;
        }

        /**
         * Sets iterator position to the initial one.
         */
        public void init(){
            index = 0;
        }
        
        /**
         * Returns <code>true</code> if the iteration has more elements (in
         * other words, returns <code>true</code> if {@link #next} would return
         * an element rather than throwing an exception).
         *
         * @return <code>true</code> if the iteration has more elements
         */
        @Override
        public boolean hasNext() {
            return (index < length);
        }

        /**
         * Returns the next element in the iteration.
         *
         * @return the next element in the iteration. 
         * 
         * @throws NoSuchElementException if the iteration has no more elements.
         */
        @Override
        public BufferedImage next() {
            if (index >= length) {
                throw new NoSuchElementException("No more frames");
            }
            return source.getFrame(index++);
        }
    }
}
