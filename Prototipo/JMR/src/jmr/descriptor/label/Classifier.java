
package jmr.descriptor.label;

import java.io.Serializable;

/**
 * Represents a classifier for given media. This is a specialization of
 * {@link java.util.function.Function}.
 *
 * @param <T> the type of the media to be classified
 * @param <R> the type of the result
 * 
 * * @author Jesús Chamorro Martínez (jesus@decsai.ugr.es)
 */
@FunctionalInterface 
public interface Classifier<T,R> extends java.util.function.Function<T,R>, Serializable{
    /**
     * Applies this classifier to the given argument.
     *
     * @param t the function argument
     * @return the function result
     */
    @Override
    R apply(T t);
    
    /**
     * Returns the class of the the classifier output.
     * 
     * @return the class of the the classifier output.
     */
    default public Class getResultClass(){
        return apply(null).getClass(); // Trick (no specific method for this)
    }
}
