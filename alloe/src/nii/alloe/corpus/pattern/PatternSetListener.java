package nii.alloe.corpus.pattern;

/**
 *
 * @author John McCrae, National Institute of Informatics
 */
public interface PatternSetListener extends nii.alloe.tools.process.AlloeProgressListener {
    public void patternGenerated(Pattern p, double score);
    public void patternDropped(Pattern p);
    public void clearPatterns();
}
