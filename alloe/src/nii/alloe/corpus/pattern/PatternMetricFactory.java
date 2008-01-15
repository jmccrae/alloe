package nii.alloe.corpus.pattern;
import nii.alloe.corpus.*;

/**
 * Used to create PatternMetrics. The reason for implementing this as a factory
 * is to allow PatternMetrics to be recreated after an object has been obtained from
 * persistent storage (ie deserialized).
 *
 * @see PatternMetric
 *
 * @author John McCrae, National Institute of Informatics
 */
public class PatternMetricFactory {
    public static final String PSEUDO_FM = "Pseudo F-Measure";
    
    /** Create a new pattern metric object */
    public static PatternMetric getPatternMetric(String id, Corpus corpus, TermPairSet termPairs) 
        throws PatternMetricUnknownException {
        if(id.equals(PSEUDO_FM)) {
            return new PseudoFMMetric(corpus, termPairs);
        } else {
            throw new PatternMetricUnknownException("Invalid Pattern Metric: " + id);
        }
    }

    /**
     * Holds value of property patternMetrics.
     */
    private static String[] patternMetrics = { PSEUDO_FM };

    /**
     * Indexed getter for property patternMetrics.
     * @param index Index of the property.
     * @return Value of the property at <CODE>index</CODE>.
     */
    public static String getPatternMetrics(int index) {
        return patternMetrics[index];
    }

    /**
     * Getter for property patternMetrics.
     * @return Value of property patternMetrics.
     */
    public static String[] getPatternMetrics() {
        return patternMetrics;
    }
    

}
