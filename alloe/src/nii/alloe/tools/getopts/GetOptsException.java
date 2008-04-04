/*
 * GetOptsException.java
 *
 * Created on 11 March 2008, 13:20
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package nii.alloe.tools.getopts;

/**
 *
 * @author john
 */
public class GetOptsException extends java.lang.Exception {
    
    /**
     * Creates a new instance of <code>GetOptsException</code> without detail message.
     */
    public GetOptsException() {
    }
    
    
    /**
     * Constructs an instance of <code>GetOptsException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public GetOptsException(String msg) {
        super(msg);
    }
}
