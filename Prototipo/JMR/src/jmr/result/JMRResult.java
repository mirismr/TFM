package jmr.result;

/**
 * Class representing an abstract result obtained in some way using the JMR.
 * 
 * @author Jesús Chamorro Martínez (jesus@decsai.ugr.es)
 */
public interface JMRResult extends Comparable<JMRResult> {

  /**
   * Represents an undefined result type
   */
  public static final int TYPE_UNDEFINED = -1;

  /**
   * Represents a custom result type
   */
  public static final int TYPE_CUSTOM = 0;

  /**
   * Represents float result
   */
  public static final int TYPE_FLOAT = 1;

  /**
   * Represents a double result
   */
  public static final int TYPE_DOUBLE = 2;

  /**
   * Represents a integer result
   */
  public static final int TYPE_INTEGER = 3;
  
  /**
   * Represents a a point in a n-dimesnional vector space
   */
  public static final int TYPE_VECTOR = 4;

  /** Return the result type
     * @see #TYPE_UNDEFINED
     * @see #TYPE_CUSTOM
     * @see #TYPE_FLOAT
     * @see #TYPE_DOUBLE
     * @see #TYPE_INTEGER
     * @see #TYPE_VECTOR
     * @return The result type
     */
  public int getType();

  /**
   * Return a double number representing the result
   * <p> This representation will depend on the characteristics of the <code>JMRResult</code> objects.
   * In fact, for some structured <code>JMRResult</code> objects, summarizing the information into a number
   * will have no sense.
   * @return A double number representing the result
   */
  public double toDouble();

  /**
  * Return a float number representing the result
  * <p> This representation will depend on the characteristics of the <code>JMRResult</code> objects.
  * In fact, for some structured <code>JMRResult</code> objects, summarizing the information into a number
  * will have no sense.
  * @return A float number representing the result
  */
  public float toFloat();

  /**
    * Return a integer number representing the result
    * <p> This representation will depend on the characteristics of the <code>JMRResult</code> objects.
    * In fact, for some structured <code>JMRResult</code> objects, summarizing the information into a number
    * will have no sense.
    * @return A integer number representing the result
    */
  public int toInteger();
  
    /**
     * Compares this result object with the specified object for order. Returns
     * a negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     * 
     * The default implementation is based on the double representation of the
     * <code>JMRResult</code> objects.
     *
     * @param result the objtect to be compared
     * @return a negative integer, zero, or a positive integer as this object is 
     * less than, equal to, or greater than the specified object.
     */
    @Override
    default public int compareTo(JMRResult result) {
        Double r1 = this.toDouble();
        Double r2 = result.toDouble();
        return r1.compareTo(r2);
    }
}
