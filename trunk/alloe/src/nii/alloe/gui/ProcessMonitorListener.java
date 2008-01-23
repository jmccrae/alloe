package nii.alloe.gui;

/**
 * Allows containers for ProcessMonitor to react to start and resume buttons
 *
 * @author John McCrae, National Institute of Informatics
 */
public interface ProcessMonitorListener {
    /** Called when the start button is pressed 
     * @param pm The ProcessMonitor calling
     * @return If rval is false, process monitor will not start
     */
    public boolean onStart(ProcessMonitor pm);
    
    /** Called when the resume button is pressed
     * @param pm The ProcessMonitor calling
     * @return If rval is false, process monitor will not resume
     */
    public boolean onResume(ProcessMonitor pm);
}
