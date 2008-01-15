/*
 * PatternMetricUnknown.java
 *
 * Created on 15 January 2008, 16:49
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package nii.alloe.corpus.pattern;

/**
 *
 * @author John McCrae, National Institute of Informatics
 */
public class PatternMetricUnknownException extends java.lang.RuntimeException {
    
    /**
     * Creates a new instance of <code>PatternMetricUnknown</code> without detail message.
     */
    public PatternMetricUnknownException() {
    }
    
    
    /**
     * Constructs an instance of <code>PatternMetricUnknown</code> with the specified detail message.
     * @param msg the detail message.
     */
    public PatternMetricUnknownException(String msg) {
        super(msg);
    }
}
