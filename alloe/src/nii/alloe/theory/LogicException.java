/*
 * LogicException.java
 *
 * Created on 23 January 2008, 19:20
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package nii.alloe.theory;

/**
 *
 * @author john
 */
public class LogicException extends java.lang.RuntimeException {
    
    /**
     * Creates a new instance of <code>LogicException</code> without detail message.
     */
    public LogicException() {
    }
    
    
    /**
     * Constructs an instance of <code>LogicException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public LogicException(String msg) {
        super(msg);
    }
}
