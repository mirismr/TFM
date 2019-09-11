package jmr.result;

/**
 * Class representing a float number.
 *
 * @author Jesús Chamorro Martínez (jesus@decsai.ugr.es)
 */
public class FloatResult implements JMRResult {

    /**
     * The float value
     */
    private float data;

    /**
     * Constructs a <code>FloatResults</code> from a float number.
     *
     * @param data The float number corresponding to the result
     */
    public FloatResult(float data) {
        this.data = data;

    }

    /**
     * Return the float number representing the result
     *
     * @return The float number representing the result
     */
    public float getValue() {
        return (data);
    }

    /**
     * Set the float number representing the result
     *
     * @param value the newvalue
     */
    public void setValue(float value) {
        this.data = value;
    }

    /**
     * Compares two {@code FloatResult} objects numerically.
     *
     * @param result the {@code FloatResult} to be compared
     * @return a negative integer, zero, or a positive integer as this object is
     * less than, equal to, or greater than the specified object.
     *
     * @see Comparable#compareTo(Object)
     */
    public int compareTo(FloatResult result) {
        return Float.compare(this.data, result.data);
    }

    /**
     * Returns a string representation of this float result.
     *
     * @return a string representation of this float result
     */
    @Override
    public String toString() {
        return Float.toString(data);
    }

    // <editor-fold defaultstate="collapsed" desc="JMRResult interface implementation code">    
    /**
     * Return the result type, in this case TYPE_FLOAT
     *
     * @return The result type
     */
    @Override
    public int getType() {
        return (this.TYPE_FLOAT);
    }

    /**
     * Return a double number representing the result
     *
     * @return A double number representing the result
     */
    @Override
    public double toDouble() {
        return ((double) data);
    }

    /**
     * Return a float number representing the result
     *
     * @return A float number representing the result
     */
    @Override
    public float toFloat() {
        return (data);
    }

    /**
     * Return a integer number representing the result
     *
     * @return A integer number representing the result
     */
    @Override
    public int toInteger() {
        return ((int) data);
    }
    
    /**
     * Compares this {@code FloatResult} object with another {@code JMRResult}.
     *
     * @param result the {@code JMRResult} to be compared
     * @return a negative integer, zero, or a positive integer as this object is
     * less than, equal to, or greater than the specified object.
     */
    @Override
    public int compareTo(JMRResult result) {
        if (result.getClass() == this.getClass()) {
            return this.compareTo((FloatResult) result);
        } else {
            return JMRResult.super.compareTo(result);
        }
    }
    // </editor-fold> 
}
