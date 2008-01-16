package nii.alloe.niceties;

/**
 * Interface used by some methods to indicate if they should pause
 *
 * @author John McCrae, National Institute of Informatics
 */
public interface PauseSignal {
    /** When this function returns true the current process should pause */
    public boolean shouldPause();
}
