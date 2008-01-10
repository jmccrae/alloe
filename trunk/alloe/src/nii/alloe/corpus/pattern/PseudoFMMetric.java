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
        SPSearch sps = new SPSearch(pattern);
        termPairs.forEachPair(sps);
        
        int n = 0;
        int N = 0;
        Iterator<String> contexts = corpus.getContextsForPattern(pattern);
        while(contexts.hasNext()) {
            String[] ss = pattern.getTermMatch(contexts.next());
            if(ss != null && termPairs.contains(ss[0],ss[1])) { // Maybe search among pairs ??
                n++;
            }
            N++;
        }
        
        double pseudoRecall = sps.spCount / termPairs.size();
        double pseudoPrecision = n / N;
        return 2 * pseudoRecall * pseudoPrecision / (pseudoPrecision + pseudoRecall);
    }
    
    private class SPSearch implements EachTermPairAction {
        int spCount;
        Pattern pattern;
        public SPSearch(Pattern pattern) { this.pattern = pattern; spCount = 0; }
        public void doAction(String term1, String term2) {
            Iterator<String> contexts = corpus.getContextsForTerms(term1,term2);
            while(contexts.hasNext()) {
                if(pattern.matches(contexts.next(), term1, term2)) {
                    spCount++;
                }
            }
        }
    }
}
