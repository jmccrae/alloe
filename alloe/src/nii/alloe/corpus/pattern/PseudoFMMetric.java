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
    
    ///** The number of hits the pattern has to had to make it worth querying every
    // * single term. May be replaced by automatic benchmarker */
    //public static int PATTERN_HITS_TO_QUERY = 1000;
    
    public PseudoFMMetric(Corpus corpus, TermPairSet termPairs) {
        this.corpus = corpus;
        this.termPairs = termPairs;
    }
    
    public double scorePattern(Pattern pattern) {
        System.out.println(pattern.toString());
        Object query = corpus.prepareQueryPattern(pattern);
        SPSearch sps = new SPSearch(pattern,query);
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
        
        assert N != 0;
        
        double pseudoRecall = (double)sps.spCount / (double)termPairs.size();
        double pseudoPrecision = (double)n / (double)N;
        if(pseudoRecall == 0 && pseudoPrecision == 0)
            return 0;
        else
            return 2 * pseudoRecall * pseudoPrecision / (pseudoPrecision + pseudoRecall);
    }
    
    private class SPSearch implements EachTermPairAction {
        int spCount;
        Pattern pattern;
        Object query;
       // boolean doQuery;
        
        public SPSearch(Pattern pattern, Object query) {
            this.pattern = pattern;
            this.query = query;
            //doQuery = corpus.getPreparedQueryHits(query) > PATTERN_HITS_TO_QUERY;
            spCount = 0;
        }
        public void doAction(String term1, String term2) {
            Iterator<String> contexts;
           // if(doQuery)
                contexts = corpus.getContextsForTermPrepared(
                        term1,term2,query);
           // else
           //     contexts = corpus.getPreparedQuery(query);
            while(contexts.hasNext()) {
                if(pattern.matches(contexts.next(), Pattern.cleanTerm(term1), Pattern.cleanTerm(term2))) {
                    spCount++;
                }
            }
        }
    }
    
    
    public String getName() { return PatternMetricFactory.PSEUDO_FM; }
}
