package jmr.video;

import java.awt.image.BufferedImage;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Class representing a colletion of video frames.
 *
 * @author Jesús Chamorro Martínez (jesus@decsai.ugr.es)
 */
public class FrameCollection extends ArrayList<BufferedImage> implements Video {

    /**
     * Frame rate expressed in frames per second.
     */
    private int frameRate;
    /**
     * Default frame rate
     */
    public static final int DEFAULT_FPS = 24;

    /**
     * Constructs a frame collection video.
     *
     * @param frameRate the frame rate expressed in frames per second.
     */
    public FrameCollection(int frameRate) {
        this.frameRate = frameRate;
    }
    
    /**
     * Constructs a frame collection video with the default frame rate
     * {@link #DEFAULT_FPS}
     */
    public FrameCollection() {
        this(DEFAULT_FPS);
    }
    
    /**
     * Constructs a frame collection video and initializes it with the images in
     * the specified collection.
     *
     * @param frameRate the frame rate expressed in frames per second.
     * @param c collection containing frames to be added to this video.
     */
    public FrameCollection(int frameRate, Collection<? extends BufferedImage> c) {
        this(frameRate);
        this.addAll(c);
    }

    /**
     * Constructs a frame collection video with the default frame rate
     * {@link #DEFAULT_FPS} and initializes it with the images in the specified
     * collection.
     *
     * @param c collection containing frames to be added to this video.
     */
    public FrameCollection(Collection<? extends BufferedImage> c) {
        this(DEFAULT_FPS,c);
    }

    /**
     * Appends the specified image to the end of this list. The new image must
     * have the same size of this frame collection (is the collestion is empty,
     * the new image will establish the frame size).
     *
     * @param image frame to be appended to this list
     * @return <tt>true</tt> (as specified by {@link java.util.Collection#add})
     * @throws InvalidParameterException if the image size is not compatible
     * with this frame collection.
     */
    @Override
    public boolean add(BufferedImage image) {
        if (!isEmpty() && (image.getWidth() != this.getWidth()
                || image.getHeight() != this.getHeight())) {
            throw new InvalidParameterException("Wrong image size.");
        }
        return super.add(image);
    }

    /**
     * Inserts the specified image at the specified position in this list.
     * Shifts the image currently at that position (if any) and any subsequent
     * images to the right (adds one to their indices).
     *
     * The new image must have the same size of this frame collection (is the
     * collestion is empty, the new image will establish the frame size).
     *
     * @param index index at which the specified image is to be inserted
     * @param image image to be inserted
     * @throws IndexOutOfBoundsException {@inheritDoc}
     * @throws InvalidParameterException if the image size is not compatible
     * with this frame collection.
     */
    @Override
    public void add(int index, BufferedImage image) {
        if (!isEmpty() && (image.getWidth() != this.getWidth()
                || image.getHeight() != this.getHeight())) {
            throw new InvalidParameterException("Wrong image size.");
        }
        super.add(index, image);
    }

    /**
     * Appends all of the images in the specified collection to the end of this
     * list, in the order that they are returned by the specified collection's
     * Iterator.
     *
     * @param c collection containing iamges to be added to this list
     * @return <tt>true</tt> if this list changed as a result of the call
     * @throws NullPointerException if the specified collection is null
     * @throws InvalidParameterException if the specified collection is not
     * compatible with this frame collection.
     */
    @Override
    public final boolean addAll(Collection<? extends BufferedImage> c) {
        if (!isCompatibleCollection(c)) {
            throw new InvalidParameterException("Wrong image collection.");
        }
        return super.addAll(c);
    }

    /**
     * Inserts all of the images in the specified collection into this list,
     * starting at the specified position. Shifts the image currently at that
     * position (if any) and any subsequent images to the right (increases their
     * indices). The new images will appear in the list in the order that they
     * are returned by the specified collection's iterator.
     *
     * @param index index at which to insert the first image from the specified
     * collection
     * @param c collection containing images to be added to this list
     * @return <tt>true</tt> if this list changed as a result of the call
     * @throws IndexOutOfBoundsException {@inheritDoc}
     * @throws NullPointerException if the specified collection is null
     * @throws InvalidParameterException if the specified collection is not
     * compatible with this frame collection.
     */
    @Override
    public boolean addAll(int index, Collection<? extends BufferedImage> c) {
        if (!isCompatibleCollection(c)) {
            throw new InvalidParameterException("Wrong image collection.");
        }
        return super.addAll(index, c);
    }

    /**
     * Checks if the given collecion is compatible with this frame collection,
     * that is, if all the images have the same size of the frames of this
     * collection.
     *
     * @param c the image collection
     * @return <tt>true</tt> if the given collection is compatible with this
     * frame collection
     */
    private boolean isCompatibleCollection(Collection<? extends BufferedImage> c) {
        if (c == null) {
            return false;
        }
        if (!c.isEmpty()) {
            int width, height;
            if (this.isEmpty()) {
                BufferedImage frame = c.iterator().next();
                width = frame.getWidth();
                height = frame.getHeight();
            } else {
                width = this.getWidth();
                height = this.getHeight();
            }
            for (BufferedImage image : c) {
                if (image.getWidth() != width || image.getHeight() != height) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Replaces the image at the specified position in this list with the
     * specified image.
     *
     * @param index index of the image to replace
     * @param image image to be stored at the specified position
     * @return the image previously at the specified position
     * @throws IndexOutOfBoundsException {@inheritDoc}
     * @throws InvalidParameterException if the if the image size is not
     * compatible with this frame collection.
     */
    @Override
    public BufferedImage set(int index, BufferedImage image) {
        if (!isEmpty() && (image.getWidth() != this.getWidth()
                || image.getHeight() != this.getHeight())) {
            throw new InvalidParameterException("Wrong image size.");
        }
        return super.set(index, image);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getHeight() {
        return isEmpty() ? 0 : get(0).getHeight();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getWidth() {
        return isEmpty() ? 0 : get(0).getWidth();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfFrames() {
        return size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BufferedImage getFrame(int frame_index) {
        return get(frame_index);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getFrameRate() {
        return frameRate;
    }

    /**
     * Set the frame rate expressed in frames per second.
     * 
     * @param frameRate the frame rate expressed in frames per second.
     * @throws InvalidParameterException if the frame rate is zero or negative.
     */
    public void setFrameRate(int frameRate){
        if (frameRate<=0) {
            throw new InvalidParameterException("Wrong frame rate.");
        }
        this.frameRate = frameRate;
    }
}
