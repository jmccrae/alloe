package nii.alloe.corpus.pattern;
/**
 * A scoring metric used to score the patterns
 *
 * @author John McCrae, National Institute of Informatics
 */
public interface PatternMetric {
    public double scorePattern(Pattern pattern);
}
