package nii.alloe.classify;
import java.util.*;
import nii.alloe.corpus.Corpus;
import nii.alloe.corpus.TermPairSet;
import nii.alloe.corpus.pattern.Pattern;
import weka.core.*;

/**
 * Form a set of feature vectors using a corpus and set of patterns.
 *
 * @author John McCrae, National Institute of Informatics
 */
public class FeatureVectorFormer {
    
    /** Creates a new instance of FeatureVectorFormer */
    public FeatureVectorFormer() {
    }
    
    /**
     * Makes a set of feature vectors
     * @param relation The relation to create data for
     * @param dataSet The dataSet to put data into (if null a new DataSet is created)
     * @param terms A list of term participating in relations
     * @param corpus The corpus
     * @param termPairs The true/false values of the relations
     * @return dataSet if the value passed was non-null, else a new DataSet with all information inserted
     */
    public DataSet makeFeatureVectors(String relation,
            DataSet dataSet,
            Iterable<String> terms, 
            List<Pattern> patterns, 
            Corpus corpus, 
            TermPairSet termPairs) {
        if(dataSet == null)
            dataSet = new DataSet();
        dataSet.prepRelation(relation, getAttNames(patterns));
        
        Iterator<String> termIter1 = terms.iterator();
        while(termIter1.hasNext()) {
            String term1 = termIter1.next();
            if(!corpus.isTermInCorpus(term1))
                continue;
            Iterator<String> termIter2 = terms.iterator();
            while(termIter2.hasNext()) {
                String term2 = termIter2.next();
                if(!corpus.areTermsInCorpus(term1, term2))
                    continue;
                double[] data = new double[patterns.size()];
                // TODO benchmark using corpus.getContextsForTermInPattern()
                Iterator<String> contexts = corpus.getContextsForTerms(term1, term2);
                while(contexts.hasNext()) {
                    String ctxt = contexts.next();
                    Iterator<Pattern> patIter = patterns.iterator();
                    int i = 0;
                    while(patIter.hasNext()) {
                        Pattern p = patIter.next();
                        if(p.matches(ctxt,term1,term2)) 
                            data[i]++;
                        i++;
                    }
                }
                for(int i = 0; i < patterns.size(); i++) {
                    data[i] = data[i] / corpus.size();
                }
                
                dataSet.addInstance(new SparseInstance(1.0,data),
                        relation, term1, term2);
            }
        }
        return dataSet;
    }
    
    private Iterator<String> getAttNames(List<Pattern> patterns) {
        return new AttNameIterator(patterns);
    }

    public final class AttNameIterator implements Iterator<String> {
        Iterator<Pattern> patterns;
        AttNameIterator(List<Pattern> p) { patterns = p.iterator(); }

        
        public String next() {
            return patterns.next().getVal();
        }

        
        public boolean hasNext() {
            return patterns.hasNext();
        }
        
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
