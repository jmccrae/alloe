package nii.alloe.corpus.pattern;
import nii.alloe.corpus.*;
import java.util.*;

/**
 *
 * @author John McCrae, National Institute of Informatics
 */
public class MinSupportPrecisionMetric implements PatternMetric {
    Corpus corpus;
    TermPairSet termPairs;
    
    /** Creates a new instance of MinSupportPrecisionMetric */
    public MinSupportPrecisionMetric(Corpus corpus, TermPairSet termPairs) {
        this.corpus = corpus;
        this.termPairs = termPairs;
    }
    
    public double scorePattern(Pattern pattern) {
        int n = 0;
        int N = 0;
        
        Iterator<Corpus.Hit> contexts = corpus.getContextsForPattern(pattern);
        if(contexts == null)
            return 0;
        LOOP : while(contexts.hasNext()) {
            Corpus.Hit context = contexts.next();
            String text = context.getText();
            String[] terms = context.getTerms();
            for(int i = 0; i < terms.length; i++) {
                for(int j = 0; j < terms.length; j++) {
                    if(termPairs.contains(terms[i],terms[j]) && pattern.matches(text, terms[i],terms[j])) {
                        n++;
                        N++;
                        continue LOOP;
                    }
                }
            }
            N++;
        }
        
        double precision = (double) n / (double) N;
        if(n < minSupport) {
            return n;
        } else {
            return minSupport + precision;
        }
    }
    
    public String getName() {
        return PatternMetricFactory.MIN_SUP_PREC;
    }
    
    /**
     * Holds value of property minSupport.
     */
    private int minSupport = 3;
    
    /**
     * Getter for property minSupport.
     * @return Value of property minSupport.
     */
    public int getMinSupport() {
        return this.minSupport;
    }
    
    /**
     * Setter for property minSupport.
     * @param minSupport New value of property minSupport.
     */
    public void setMinSupport(int minSupport) {
        if(minSupport < 0)
            throw new IllegalArgumentException();
        this.minSupport = minSupport;
    }
}
