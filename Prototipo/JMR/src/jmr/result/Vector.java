package jmr.result;

import java.security.InvalidParameterException;
import java.util.Arrays;

/**
 * Class representing a point in a n-dimesnional vector space
 * 
 * @author Jesús Chamorro Martínez (jesus@decsai.ugr.es)
 */
public class Vector implements JMRResult{
    /**
     * Store the coordinates of the vector
     */
    private Double data[];
    
    /**
     * Constructs a new vector of a given dimension initialized as the origin
     * 
     * @param dimension the vector dimension 
     */
    public Vector(int dimension){
        if(dimension<=0){
            throw new InvalidParameterException("Dimesnion must be positive.");
        }
        data = new Double[dimension];        
    }
    
    /**
     * Returns the vector dimension.
     * 
     * @return the vector dimension
     */
    public int dimension(){
        return data.length;
    }
    
    /**
     * Set the value for a given coordinate of the vector
     * 
     * @param index index of the coordinate to replace
     * @param value value to be stored at the specified position
     */
    public void setCoordinate(int index, double value){
        data[index] = value;
    }
    
    /**
     * Returns the coordinate value of the given index
     * 
     * @param index index of the coordinate 
     * @return the coordinate value of the given index
     */
    double coordinate(int index){        
        return data[index];
    }
    
    /**
     * Returns the magnitude of the vector.
     * 
     * @return the magnitude of the vector
     */
    public double magnitude(){
        double magnitude = 0.0;
        for(Double v : data){
            magnitude += v*v;
        }
        return Math.sqrt(magnitude);
    }

    /**
     * Euclidean distance between vectors.
     * 
     * @param v vector to be compared
     * @return the Eucilean distance
     */
    public double distance(Vector v){
        if(this.dimension()!=v.dimension()){
            throw new InvalidParameterException("Vector have different dimesnion: "+v.dimension());
        }
        double distance = 0.0;
        for(int i=0;i<this.dimension();i++){
            distance += Math.pow(this.coordinate(i)-v.coordinate(i),2.0);
        }
        return Math.sqrt(distance);
    }
    
    
    /**
     * Compares two vectors on the basis of their magnitudes.
     * 
     * @param v vector to be compared
     * @return a negative integer, zero, or a positive integer as this object is 
     * less than, equal to, or greater than the specified object.
     * 
     * @see Comparable#compareTo(Object)
     */
    public int compareTo(Vector v) {
        return Double.compare(this.magnitude(), v.magnitude());
    }
    
    @Override
    public String toString(){
        return Arrays.toString(data)+" Magnitude: "+this.magnitude();
    }
    
    // <editor-fold defaultstate="collapsed" desc="JMRResult interface implementation code"> 
    @Override
    public int getType() {
        return (this.TYPE_VECTOR);
    }

    @Override
    public double toDouble() {
        return this.magnitude();
    }

    @Override
    public float toFloat() {
        return (float)this.magnitude();
    }

    @Override
    public int toInteger() {
        return (int)this.magnitude();
    }

    @Override
    public int compareTo(JMRResult o) {
        if (o.getClass() == this.getClass()) {
            return this.compareTo((Vector)o);
        } else {
            return JMRResult.super.compareTo(o);
        }
    }
    // </editor-fold> 
}
