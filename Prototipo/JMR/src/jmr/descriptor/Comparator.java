
package jmr.descriptor;

import java.io.Serializable;
import java.util.function.BiFunction;

/**
 * Represents a function that compares two descriptors (the arguments) and 
 * produces a result. This is a specialization of {@link BiFunction}.
 * 
 * @param <T> the type of the argument descriptors
 * @param <R> the type of the result
 * 
 * * @author Jesús Chamorro Martínez (jesus@decsai.ugr.es)
 */
@FunctionalInterface 
public interface Comparator<T extends MediaDescriptor, R> extends java.util.function.BiFunction<T, T, R>, Serializable{
    /**
     * Applies this comparator to the given arguments.
     *
     * @param t the first function argument
     * @param u the second function argument
     * @return the function result
     */
    @Override
    R apply(T t, T u);
}
