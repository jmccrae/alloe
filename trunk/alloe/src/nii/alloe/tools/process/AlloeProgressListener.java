package nii.alloe.tools.process;

/**
 * Listen for change in progress by AlloeProcess
 *
 * @author John McCrae, National Institute of Informatics
 */
public interface AlloeProgressListener {
    /** Called whenever progress is made
     * @param newProgress The new progress percentage (as double between 0 and 1) */
    public void progressChange(double newProgress);
    
    /** Called when the process finishes */
    public void finished();
}
