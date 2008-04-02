package nii.alloe.corpus.pattern;

import nii.alloe.corpus.Corpus;
import nii.alloe.corpus.EachTermPairAction;
import nii.alloe.corpus.TermPairSet;
import java.util.*;

/**
 * The Pseudo-F-Measure Metric used to score patterns. Given by <br>
 * Pseudo-Recall = Number of term pairs from set found by this pattern in the corpus / Number of term pairs in set <br>
 * Pseudo-Precision = Number of term pairs found by the pattern in the corpus which are in the set /
 * Number of term pairs found by the pattern <br>
 * Pseudo-F-Measure as usual.
 *
 * @author John McCrae, National Institute of Informatics
 */
public class PseudoFMMetric implements PatternMetric {

    Corpus corpus;
    TermPairSet termPairs;

    public PseudoFMMetric(Corpus corpus, TermPairSet termPairs) {
        this.corpus = corpus;
        this.termPairs = termPairs;
    }

    public double scorePattern(Pattern pattern) {
        int n = 0;
        int N = 0;
        
        TermPairSet foundTerms = new TermPairSet();
        Iterator<Corpus.Hit> contexts = corpus.getContextsForPattern(pattern);
        if(contexts == null)
            return 0;
        LOOP : while(contexts.hasNext()) {
            Corpus.Hit context = contexts.next();
            String text = context.getText();
            String[] terms = context.getTerms();
            boolean goodContext = false;
            for(int i = 0; i < terms.length; i++) {
                for(int j = 0; j < terms.length; j++) {
                    if(termPairs.contains(terms[i],terms[j]) && pattern.matches(text, terms[i],terms[j])) {
                        foundTerms.add(terms[i],terms[j]);
                        if(!goodContext) {
                            n++;
                            goodContext = true;
                        }
                    }
                }
            }
            N++;
        }
        
        double pseudoRecall = (double) foundTerms.size() / (double) termPairs.size();
        double pseudoPrecision = (double) n / (double) N;
        if (pseudoRecall == 0 && pseudoPrecision == 0 || Double.isInfinite(pseudoRecall) || Double.isInfinite(pseudoPrecision)
            || Double.isNaN(pseudoRecall) || Double.isNaN(pseudoPrecision)) {
            return 0;
        } else {
            return 2 * pseudoRecall * pseudoPrecision / (pseudoPrecision + pseudoRecall);
        }
    }

    public String getName() {
        return PatternMetricFactory.PSEUDO_FM;
    }
}
