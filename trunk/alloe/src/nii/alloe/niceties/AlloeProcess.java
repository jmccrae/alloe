package nii.alloe.niceties;

import java.io.Serializable;

/**
 * A Pausble and resumable process which fires progress messages
 *
 * @see nii.alloe.gui.ProcessMonitor
 *
 * @author John McCrae, National Institute of Informatics
 */
public interface AlloeProcess {
    /* private transient LinkedList<AlloeProgressListener> aplListeners; 
      private transient Thread theThread;
      private transient int state;
      private static final int STATE_OK = 0;
      private static final int STATE_STOPPING = 1;
      private static final int STATE_UNPAUSEABLE = 2; */
    
   /** Register a progress listener */
    public void addProgressListener(AlloeProgressListener apl);
    /* if(aplListeners == null) 
       aplListeners = new LinkedList<AlloeProgressListener>();
      if(!aplListeners.contains(apl))
       aplListeners.add(apl);
     }
     */
    
    /*
     private void fireNewProgressChange(double newProgress) {
      if(aplListeners != null) {
        Iterator<AlloeProgressListener> apliter = aplListeners.iterator();
        while(apliter.hasNext()) {
            apliter.next().progressChange(newProgress);
        }
     }
    }
    
    private void fireFinished() {
     if(aplListeners != null) {
        Iterator<AlloeProgressListener> apliter = aplListeners.iterator();
        while(apliter.hasNext()) {
            apliter.next().finished();
        }
     }
    }
    */
    
    
    /** Start process. It is expected that this function should start the progress
     * in a new thread */
    public void start();
    /* theThread = new Thread(this);
      state = STATE_OK;
      theThread.start();
     */
   
    /** Pause the process. The assumption is that this will work by changing a variable
     * in the running thread and then wait for this thread to finish by use of join().
     * It is assumed that the this object is Serializable, otherwise it's your problem
     * to assure the object is ok when resume() is called.
     *
     * @throws CannotPauseException If the process is not in a state where it can be resumed
     */
    public void pause() throws CannotPauseException;
    /*
     try { 
     if(state == STATE_UNPAUSEABLE)
      throw new CannotPauseException("Some reason");
      state = STATE_STOPPING;
      theThread.join();
        } catch(InterruptedException x) {
            throw new CannotPauseException("The thread was interrupted");
        }
     }
     */
    
    /** Resume the process. 
     * @see #pause()
     */
    public void resume();
    /*
      theThread = new Thread(this);
      state = STATE_OK;
      theThread.start();
     }
     */
    
    /** Get a string representation of the current action being performed */
    public String getStateMessage();
    
}
