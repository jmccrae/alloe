package nii.alloe.corpus;

/**
 *
 * @author John McCrae, National Institute of Informatics
 */
public interface CorpusFile {
    /** Get the next line in the corpus
     * @return The next line, or null if the file is finished
     */
    public String getNextLine() throws java.io.IOException;
      
    /**
     * Get how far we are through the corpus file
     */
    public double getProgress();
    
    public boolean isEndOfDocument();
}
