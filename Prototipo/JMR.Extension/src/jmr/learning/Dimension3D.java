package jmr.learning;

/**
 * Represents a triplet.
 * @param <T> the type of the first value
 * @param <U> the type of the second value
 * @param <V> the type of the third value
 * @author Míriam Mengíbar Rodríguez (mirismr@correo.ugr.es)
 */
public class Dimension3D<T, U, V> {
         
    private T height;
    private U width;
    private V depth;
    
    /**
     * Construct a empty triplet
     */
    public Dimension3D() {
    }
    
    /**
     * Construct a triplet, initializes it from 
     * three elements given by parameter
     * @param first First triplet value
     * @param second Second triplet value 
     * @param third Third triplet value
     */
    public Dimension3D(T first, U second, V third){
        this.height = first;
        this.width = second;
        this.depth= third;
    }

    /**
     * Returns height dimension
     * @return height dimension
     */
    public T getHeight() {
        return height;
    }

    /**
     * Set height dimension
     * @param height dimension
     */
    public void setHeight(T height) {
        this.height = height;
    }
    
    /**
     * Returns width dimension value
     * @return widht dimension value
     */
    public U getWidth() {
        return width;
    }

    /**
     * Set width dimension value
     * @param width dimension value
     */
    public void setWidth(U width) {
        this.width = width;
    }

    /**
     * Returns depth dimension value
     * @return depth dimension value
     */
    public V getDepth() {
        return depth;
    }

    /**
     * Set depth dimension value
     * @param depth dimension value
     */
    public void setDepth(V depth) {
        this.depth = depth;
    }
    
}
