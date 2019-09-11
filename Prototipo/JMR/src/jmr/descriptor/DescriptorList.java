package jmr.descriptor;

import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.ArrayList;

/**
 * Class representing a list of descriptors calculated from the same media. 
 * 
 * There are not restrictions about the type of descriptors (the list even
 * may contain descriptors of different classes, but from the same source).
 * 
 * @param <T> the type of media described by this object
 * 
 * @author Jesús Chamorro Martínez (jesus@decsai.ugr.es)
 */
public class DescriptorList<T> extends MediaDescriptorAdapter<T> implements Serializable{
    /**
     * List of descriptors
     */
    protected ArrayList<MediaDescriptor<T>> descriptors;

    /**
     * Constructs the descriptor as an empty list and set as comparator the
     * default one.
     * 
     * @param media the source media
     */
    public DescriptorList(T media) {
        super(media, new DefaultComparator());
    }
    
    /**
     * Initialize the descriptor as an empty list.
     *
     * @param media the media associated to this descriptor
     */
    @Override
    public void init(T media) {
        descriptors = new ArrayList<>();
    }
    
    /**
     * Appends the specified descriptor to the end of this list.
     *
     * @param descriptor descriptor to be appended to this list
     * @return <tt>true</tt> (as specified by 
     * {@link java.util.Collection#add(java.lang.Object) })
     * @throws InvalidParameterException if the new descriptor does not share 
     * the list media source
     */
    public boolean add(MediaDescriptor<T> descriptor) {
        if (descriptor.getSource() != this.getSource()) {
            throw new InvalidParameterException("The new descriptor does not share this list media source.");
        }
        return descriptors.add(descriptor);
    }
    
    /**
     * Inserts the specified descriptor at the specified position in this
     * list. Shifts the element currently at that position (if any) and
     * any subsequent elements to the right (adds one to their indices).
     *
     * @param index index at which the specified descriptor is to be inserted
     * @param descriptor descriptor to be inserted
     * @throws IndexOutOfBoundsException {@inheritDoc}
     * @throws InvalidParameterException if the new descriptor does not share 
     * the list media source
     */
    public void add(int index, MediaDescriptor<T> descriptor) {
        if (descriptor.getSource() != this.getSource()) {
            throw new InvalidParameterException("The new descriptor does not share this list media source.");
        }
        descriptors.add(index, descriptor);
    }
    
    /**
     * Replaces the descriptor at the specified position in this list with
     * the specified descriptor.
     *
     * @param index index of the descriptor to replace
     * @param descriptor descriptor to be stored at the specified position
     * @return the descriptor previously at the specified position
     * @throws IndexOutOfBoundsException {@inheritDoc}
     * @throws InvalidParameterException if the new descriptor does not share 
     * the list media source
     */
    public MediaDescriptor<T> set(int index, MediaDescriptor<T> descriptor) {
        if (descriptor.getSource() != this.getSource()) {
            throw new InvalidParameterException("The new descriptor does not share this list media source.");
        }
        return descriptors.set(index, descriptor);
    }
    
    /**
     * Returns the descriptor at the specified position in this list.
     *
     * @param  index index of the descriptor to return
     * @return the descriptor at the specified position in this list
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public MediaDescriptor<T> get(int index) {
        return descriptors.get(index);
    }
    
    /**
     * Removes the descriptor at the specified position in this list.
     * Shifts any subsequent descriptor to the left (subtracts one from their
     * indices).
     *
     * @param index the index of the descriptor to be removed
     * @return the descriptor that was removed from the list
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public MediaDescriptor<T> remove(int index) {
        return descriptors.remove(index);
    }
    
    /**
     * Removes all of the descriptors from this list.  The list will
     * be empty after this call returns.
     */
    public void clear() {
        descriptors.clear();
    }
   
    /**
     * Returns the number of descriptors in this list.
     *
     * @return the number of descriptors in this list
     */
    public int size() {
        return descriptors.size();
    }
    
    /**
     * Returns <tt>true</tt> if this list contains no descriptors.
     *
     * @return <tt>true</tt> if this list contains no descriptors
     */
    public boolean isEmpty() {
        return descriptors.isEmpty();
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
     * Functional (inner) class implementing a comparator between list
     * descriptors.
     * 
     * The difference between list descriptors is calculated as the Euclidean
     * distance. Both lists must have the same size and all the descriptors in 
     * the list must be comparables (at a given position) with a double value 
     * as result.
     */
    static class DefaultComparator implements Comparator<DescriptorList, Double> {
        @Override
        /**
         * Calculates the difference between list descriptors by means a
         * Euclidean distance. Both lists must have the same size and all the
         * descriptors in the list must be comparables (at a given position)
         * with a double value as result.
         *
         * @param t the first descriptor list.
         * @param u the second descriptor list.
         * @return the difference between descriptors.
         * @throws InvalidParameterException if the descriptor lists have 
         * different size, or if the descriptors at a given position are not 
         * comparables with a double value as result.
         */
        public Double apply(DescriptorList t, DescriptorList u) {
            if(t.size() != u.size()){
                throw new InvalidParameterException("The descriptor lists must have the same size.");
            }
            Double item_distance, sum = 0.0;
            for(int i=0; i<t.size(); i++){
                try{
                    item_distance = (Double)t.get(i).compare(u.get(i));
                    sum += item_distance*item_distance;
                }
                catch(ClassCastException e){
                    throw new InvalidParameterException("The comparision between descriptors at position '"+i+"' is not interpetrable as a double value.");
                }
                catch(Exception e){
                    throw new InvalidParameterException("The descriptors at position '"+i+"' are not comparables.");
                }                
            }
            return Math.sqrt(sum);
        }    
    }
    
}
