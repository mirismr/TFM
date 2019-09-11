package jmr.query;

import jmr.descriptor.DescriptorList;
import jmr.descriptor.MediaDescriptor;
import jmr.descriptor.MediaDescriptorFactory;

/**
 * Class representing a query.
 * 
 * @param <T> the type of media associated to this query 
 * @author Jesús Chamorro Martínez (jesus@decsai.ugr.es)
 *
 */
public class Query<T> extends DescriptorList<T>{
    
    /**
     * Constructs a query, defined by the given media. 
     * 
     * The list of descriptors associated to this query will be empty and the 
     * comparator will be set to the default one.
     * 
     * @param query the source media of this query
     */
    public Query(T query){
        super(query);
    }
    
    /**
     * Constructs a query, defined by the given media, and initialize the list  
     * of descriptors associated to it. 
     * 
     * It will automatically calculate the descriptors (the number and type will
     * be provided as parameter) and the comparator will be set to the default 
     * one. 
     * 
     * @param query the source media of this query
     * @param descriptorClasses the list of descriptor classes that will determine
     * the set of descriptor associated to this query. Each descriptor class have 
     * to provide a constructor with a single parameter of the query type.
     */
    public Query(T query, Class... descriptorClasses){
        super(query);
        this.initDescriptors(descriptorClasses);
    }
    
    /**
     * Initializes the list of descriptors associated to this query.
     * 
     * @param descriptorClasses the list of descriptor classes that will determine
     * the set of descriptor associated to this query. Each descriptor class have 
     * to provide a constructor with a single parameter of the query type.
     */
    private void initDescriptors(Class... descriptorClasses){
        MediaDescriptor<T> descriptor;
        for(Class c: descriptorClasses){            
            descriptor = MediaDescriptorFactory.getInstance(c,this.source);
            this.add(descriptor);
        }
    }
    
}
