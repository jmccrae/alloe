package nii.alloe.niceties;

/**
 *
 * @author John McCrae, National Institute of Informatics
 */
public class CannotPauseException extends Exception {
    
    /** Creates a new instance of CannotPauseException */
    public CannotPauseException() {
        super();
    }
    
    public CannotPauseException(String str) {
        super(str);
    }
    
    public CannotPauseException(Throwable thr) {
        super(thr);
    }
    
    public CannotPauseException(String str, Throwable thr) {
        super(str,thr);
    }
    
}
