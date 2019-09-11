package jmr.descriptor;

/**
 * Abstract representation of a media descriptor.
 *
 * @param <T> the type of media described by this object
 *
 * @author Jesús Chamorro Martínez (jesus@decsai.ugr.es)
 */
public interface MediaDescriptor<T> {

    /**
     * Returns the media source associated to this descriptor. The default 
     * implementation returns no source (null).
     *
     * @return the media source (null in the default implementation)
     */
    default public T getSource() {
        return null;
    }

    /**
     * Set the media source of this descriptor and updates it on the basis
     * of the new data.
     * 
     * @param media the media source
     */
    default public void setSource(T media) {
        init(media);
    }
    
    /**
     * Initialize this descriptor from the media given by parameter. 
     * 
     * It is recommended (but not mandatory) to call this method in the 
     * constructors of the subclasses that implement this interface.
     *
     * @param media the media used for initializating this descriptor
     */
    public void init(T media);

    /**
     * Compares this descriptor to the one given by parameter.
     *
     * @param <R> the type of the result.
     * @param descriptor descriptor to be compared.
     * @return the result of the descriptor comparision.
     */
    public <R> R compare(MediaDescriptor descriptor);    
}
