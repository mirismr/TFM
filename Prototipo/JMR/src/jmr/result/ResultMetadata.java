package jmr.result;

/**
 * Metadata information about a result
 * 
 * In addition to a <code>JMRResult</code> object, extra information about the 
 * result is stored as a <code>T</code> obtect (with T the metadata type -if it 
 * is not specified, Object class is used bay default-).
 * 
 * For example, if we use <code>BufferedImage</code> as metadata type, we can 
 * add as addictional information the image associated to the result (i.e., the
 * one used in the comparision procedure). Another clases, like <code>String</code>,
 * or user designed classes, may be also useful. 
 * 
 * @author Jesús Chamorro Martínez (jesus@decsai.ugr.es)
 * @param <R> result type
 * @param <T> metadata type 
 */
public class ResultMetadata<R,T> implements Comparable<ResultMetadata> { //implements JMRResult{
    private R result;
    private T metadata;
    
    /**
     * Constructs a result metadata.
     * 
     * @param result the result to be extended with metadata
     * @param metadata the metadata associated to the result
     */
    public ResultMetadata(R result, T metadata){
        this.result = result;
        this.metadata = metadata;
    }
    
    /**
     * Return the result associated to this object
     *
     * @return the result associated to this object
     */
    public R getResult(){
        return result;
    }
    
    /**
     * Set the specified  <code>JMRResult</code> objetc as result.
     * 
     * @param result the new result
     */
    public void setResult(R result){
        this.result = result;     
    }
    
    /**
     * Return the metadata associated to the the result
     *
     * @return the metadata associated to the the result
     */
    public T getMetadata() {
        return metadata;
    }
    
    /**
     * Set the metadata of the result associated to this object.
     * 
     * @param metadata the metadata associated to the result
     */
    public void setMetadata(T metadata) {
        this.metadata = metadata;
    }
    
    // <editor-fold defaultstate="collapsed" desc="JMRResult interface implementation code"> 
//    @Override
//    public int getType() {
//        return result.getType();
//    }
//
//    @Override
//    public double toDouble() {
//        return result.toDouble();
//    }
//
//    @Override
//    public float toFloat() {
//        return result.toFloat();
//    }
//
//    @Override
//    public int toInteger() {
//        return result.toInteger();
//    }
//
//    @Override
//    public int compareTo(JMRResult o) {
//        if (o.getClass() == this.getClass()) {
//            return result.compareTo(((ResultMetadata)o).result);
//        } else {
//            return result.compareTo(o);
//        }
//    }
    // </editor-fold> 
    
    @Override
    public int compareTo(ResultMetadata o) {       
        return ((Comparable)result).compareTo(o.result);
    }
    
}
