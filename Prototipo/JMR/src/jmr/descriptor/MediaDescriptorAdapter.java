package jmr.descriptor;

import java.io.Serializable;

/**
 * An abstract adapter class for media descriptors.
 * 
 * This class, in addition to implement the interface {@link MediaDescriptor},  
 * introduces a {@link Comparator} object associated to the descriptor (i.e., it 
 * is not a "simple" adapter where all the abstract methods from 
 * {@link MediaDescriptor} are implemented with empty body). The main role of 
 * this class is:
 * 
 * <ul>
 * <li> To define a standard constructor where the two main properties of a 
 * descriptor are required: the media and the comparator. This contructor will
 * set both properties and will call the init method in order to calculate the
 * descriptor data. The idea is to force all subclases to call this (unique) 
 * constructor.
 * 
 * <li> To implement the compare method. This method will use the comparator in
 * order to give the result. Note that you can use different comparators for the
 * same descriptor just chaning them using the 
 * {@link MediaDescriptorAdapter#setComparator(jmr.descriptor.Comparator) } method. 
 * This will allow to compare descriptors in different ways without overloading
 * any method, just to defining a {@link Comparator} class for each type of 
 * comparision and setting the corresponding object.
 * 
 * <li> To implement the get/set source methods.
 * </ul>
 * 
 * The init method is kept as abstract: how a descriptor is calculated from 
 * the media must be implemented in each subclass.
 * 
 * Therefore, and summarizing, the descriptor class implementing this abstract 
 * class should:
 * <ul>
 * <li> Implement the method 
 * {@link MediaDescriptorAdapter#init(java.lang.Object)}, where the 
 * descriptor data is initialized from the media, and
 * <li> Implement at least one constructor. This constructor should call the
 * super one which required two parameters: a media and a comparator.
 * </ul>
 * And, for that, it is recommended to:
 * <ul>
 * <li> Implement a default (inner) comparator class extending the interface 
 * {@link Comparator}. The method 
 * {@link Comparator#apply(jmr.descriptor.MediaDescriptor, jmr.descriptor.MediaDescriptor) } 
 * of the class will codes how to compare two given descriptors.
 * <li> Define a constructor with a single parameter of the media type, which 
 * will call the super one passing them (a) the media (b) and a new object built
 * using the default comparator class.
 * </ul>
 * 
 * @param <T> the type of media described by this object
 * 
 * @author Jesús Chamorro Martínez (jesus@decsai.ugr.es)
 */
public abstract class MediaDescriptorAdapter<T> implements MediaDescriptor<T>, Serializable{
    /**
     * The source media of this descriptor
     */
    protected transient T source = null;
    /**
     * A comparator for the descriptor. It uses a standard functional interface, 
     * allowing lambda expressions 
     */
    protected Comparator comparator = null;

    /**
     * Constructs a descriptor from the given media and set as comparator the
     * one given by parameter
     * 
     * @param media the media ssociated to this descriptor
     * @param comparator the comparator associated to this descriptor
     */
    protected MediaDescriptorAdapter(T media, Comparator comparator){
        setSource(media);
        setComparator(comparator);
    }
    
    /**
     * Only for serialization tasks. It should not be used in subclasses
     */
    protected MediaDescriptorAdapter(){
        System.out.println("Serilazing: empty constructor in MediaDescriptorAdapter");
    }
    
    /**
     * Set the comparator for this descriptor. It allows the use of lambda 
     * expressions
     * 
     * @param comparator the new comparator
     */
    final public void setComparator(Comparator comparator){
        this.comparator = comparator;
    }
        
    /**
     * Returns the media source associated to this descriptor
     *
     * @return the media source
     */
    @Override
    final public T getSource() {
        return source;
    }
    
    /**
     * Set the media source of this descriptor and updates it on the basis
     * of the new source.
     * 
     * @param media the media source
     */
    @Override
    final public void setSource(T media){
        this.source = media;
        this.init(media);
    }
    
    /**
     * Inherited method to be implemented in the subclass. Its goal is to
     * initialize the descriptor from the media given by parameter.
     *
     * @param media the media used for initializating this descriptor
     */
    @Override
    abstract public void init(T media);
    
    /**
     * Compares this descriptor to the one given by parameter.
     *
     * @param <R> the type of the result. It will be given by the output type of 
     * the comparator associated to this descriptor.
     * @param descriptor descriptor to be compared.
     * @return the result of the descriptor comparision.
     */
    @Override
    public <R> R compare(MediaDescriptor descriptor){
        if (comparator == null) {
            throw new NullPointerException("Comparator is null.");
        }
        return (R)comparator.apply(this, descriptor);
    }
}
