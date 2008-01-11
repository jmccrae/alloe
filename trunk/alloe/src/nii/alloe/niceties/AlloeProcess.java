package nii.alloe.niceties;

import java.io.Serializable;

/**
 *
 * @author John McCrae, National Institute of Informatics
 */
public interface AlloeProcess {
   /** Register a progress listener */
    public void addProgressListener(AlloeProgressListener apl);
    
    /** Start process. It is expected that this function should start the progress
     * in a new thread */
    public void start();
   
    /** Pause the process. The assumption is that this will work by changing a variable
     * in the running thread and then wait for this thread to finish by use of join().
     * It is assumed that the this object is Serializable, otherwise it's your problem
     * to assure the object is ok when resume() is called.
     *
     * @throws CannotPauseException If the process is not in a state where it can be resumed
     */
    public void pause() throws CannotPauseException;
    
    /** Resume the process. 
     * @see #pause()
     */
    public void resume();
    
    /** Get a string representation of the current action being performed */
    public String getStateMessage();
    
}
