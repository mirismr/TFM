package jmr.result;

import java.util.LinkedList;

/**
 * A list of <code>JMRResult</code> object.
 * 
 * @author Jesús Chamorro Martínez (jesus@decsai.ugr.es)
 * @param <T> the result type
 */
public class ResultList<T extends JMRResult> extends LinkedList<T>{
    /**
     * Sorts this list into ascending order.
     */
    public void sort() {
        this.sort(null);
    }
}

//NOTA: Realmente es un alias para List<JMRResult>. Analizar si realmente es 
//      útil o si es preferible usar List donde se precise