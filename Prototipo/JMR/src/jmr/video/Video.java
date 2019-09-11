package jmr.video;

import java.awt.image.BufferedImage;

/**
 * Interface representing a video media.
 *
 * @author Jesús Chamorro Martínez (jesus@decsai.ugr.es)
 */
public interface Video {

    /**
     * Returns the height of the video frame.
     *
     * @return the height of the video frame
     */
    public int getHeight();

    /**
     * Returns the width of the video frame.
     *
     * @return the width of the video frame
     */
    public int getWidth();

    /**
     * Returns the number of frames of the video.
     *
     * @return the number of frames of the video.
     */
    public int getNumberOfFrames();

    /**
     * Returns the frame rate expressed in frames per second. It represents the
     * frequency (rate) at which consecutive frames are displayed in an animated
     * display.
     *
     * @return the frame rate expressed in frames per second (FPS)
     */
    public int getFrameRate();

    /**
     * Returns the video duration in seconds.
     * 
     * @return the video duration in seconds.
     */
    default public long getDuration(){
        return getNumberOfFrames() * getFrameRate();
    }
    
    /**
     * Returns the frame at the specified position in this video. Numbering
     * begins with 0.
     *
     * @param frame_index frame index.
     *
     * @return the frame at the specified position
     * @throws IndexOutOfBoundsException {@inheritDoc}
     *
     */
    public BufferedImage getFrame(int frame_index);

}
