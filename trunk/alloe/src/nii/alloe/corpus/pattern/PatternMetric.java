package nii.alloe.corpus.pattern;
/**
 * A scoring metric used to score the patterns
 *
 * @author John McCrae, National Institute of Informatics
 */
public interface PatternMetric {
    /** Return a score for this particular pattern, higher means better */
    public double scorePattern(Pattern pattern);
    
    /** Return a name so this object can be returned from persistent storage 
     * @see PatternMetricFactory
     */
    public String getName();
}
