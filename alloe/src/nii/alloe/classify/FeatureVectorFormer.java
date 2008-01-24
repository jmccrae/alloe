package nii.alloe.classify;
import java.util.*;
import java.io.*;
import nii.alloe.corpus.Corpus;
import nii.alloe.corpus.TermPairSet;
import nii.alloe.corpus.pattern.*;
import nii.alloe.niceties.*;
import weka.core.*;

/**
 * Form a set of feature vectors using a corpus and set of patterns.
 *
 * @author John McCrae, National Institute of Informatics
 */
public class FeatureVectorFormer implements AlloeProcess, Serializable, Runnable {
    
    
    private String relation;
    private Collection<String> terms;
    private PatternSet patterns;
    private Corpus corpus;
    private TermPairSet termPairs;
    /** The data set that the data should be entered into */
    public DataSet dataSet;
    
    /** Create a new feature vector former
     * @param relation The relation to create data for
     * @param terms A list of term participating in relations
     * @param patterns The pattern set to use to create vectors;
     * @param termPairs The true/false value of a particular term pair set, maybe null, if so all termPairs default to negative class
     * @param corpus The corpus
     **/
    public FeatureVectorFormer(String relation, PatternSet patterns, Corpus corpus, TermPairSet termPairs) {
        this.relation = relation;
        this.terms = corpus.terms;
        this.patterns = patterns;
        this.corpus = corpus;
        this.termPairs = termPairs;
    }
    
    private String term1, term2;
    
    /**
     * Makes a set of feature vectors
     * @param dataSet The dataSet to put data into (if null a new DataSet is created)
     * @return dataSet if the value passed was non-null, else a new DataSet with all information inserted */
    public DataSet makeFeatureVectors() {
        if(dataSet == null)
            dataSet = new DataSet(corpus.terms);
        if(!dataSet.isRelationPrepared(relation))
            dataSet.prepRelation(relation, getAttNames(patterns));
        
        TreeMap<Pattern,Object> preparedQuerys = prepareQuerys();
        
        double termCounts = (double)(terms.size() * terms.size());
        double leftTermCount = 0;
        
        Iterator<String> termIter1 = terms.iterator();
        while(termIter1.hasNext() && state == STATE_OK) {
            if(term1 != null) {
                while(!termIter1.next().equals(term1));
            } else
                term1 = termIter1.next();
            if(!corpus.isTermInCorpus(term1)) {
                leftTermCount++;
                term1 = null;
                continue;
            }
            Iterator<String> termIter2 = terms.iterator();
            double rightTermCount = 0;
            
            while(termIter2.hasNext() && state == STATE_OK) {
                if(term2 != null) {
                    while(!termIter2.next().equals(term2));
                } else {
                    term2 = termIter2.next();
                }
                if(!corpus.areTermsInCorpus(term1, term2)) {
                    rightTermCount++;
                    term2 = null;
                    continue;
                }
                double[] data = new double[patterns.size() + 1];
                // TODO benchmark using corpus.getContextsForTermInPattern()
                
                
                Iterator<Pattern> patIter = patterns.keySet().iterator();
                while(patIter.hasNext()) {
                    int i = 0;
                    Pattern p = patIter.next();
                    Iterator<String> contexts = corpus.getContextsForTermPrepared(term1, term2, preparedQuerys.get(p));
                    while(contexts.hasNext()) {
                        String ctxt = contexts.next();
                        if(p.matches(ctxt,term1,term2))
                            data[i]++;
                        i++;
                        
                    }
                }
                boolean isAllZero = true;
                for(int i = 0; i < patterns.size(); i++) {
                    data[i] = data[i] / corpus.size();
                    if(data[i] != 0.0)
                        isAllZero = false;
                }
                
                fireNewProgressChange((leftTermCount * terms.size() + rightTermCount) / termCounts);
                if(!isAllZero) {
                    if(termPairs != null)
                        data[patterns.size()] = dataSet.getClassVal(termPairs.contains(term1,term2));
                    else
                        data[patterns.size()] = dataSet.getClassVal(false);
                    dataSet.addInstance(new SparseInstance(1.0,data), 
                            relation, term1, term2);
                }
                rightTermCount++;
                term2 = null;
            }
            term1 = null;
            leftTermCount++;
        }
        fireFinished();
        return dataSet;
    }
    
    private TreeMap<Pattern,Object> prepareQuerys() {
        Iterator<Pattern> patIter = patterns.keySet().iterator();
        TreeMap<Pattern,Object> rval = new TreeMap<Pattern,Object>();
        while(patIter.hasNext()) {
            Pattern p = patIter.next();
            rval.put(p,corpus.prepareQueryPattern(p));
        }
        return rval;
    }
    
    public void run() {
        makeFeatureVectors();
    }
    
    private Iterator<String> getAttNames(PatternSet patterns) {
        return new AttNameIterator(patterns);
    }
    
    public final class AttNameIterator implements Iterator<String> {
        Iterator<Pattern> patterns;
        AttNameIterator(PatternSet p) { patterns = p.keySet().iterator(); }
        
        
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
    
    private transient LinkedList<AlloeProgressListener> aplListeners;
    private transient Thread theThread;
    private transient int state;
    private static final int STATE_OK = 0;
    private static final int STATE_STOPPING = 1;
    private static final int STATE_UNPAUSEABLE = 2;
    
    /** Register a progress listener */
    public void addProgressListener(AlloeProgressListener apl) {
        if(aplListeners == null)
            aplListeners = new LinkedList<AlloeProgressListener>();
        if(!aplListeners.contains(apl))
            aplListeners.add(apl);
    }
    
    
    
    private void fireNewProgressChange(double newProgress) {
        Iterator<AlloeProgressListener> apliter = aplListeners.iterator();
        while(apliter.hasNext()) {
            apliter.next().progressChange(newProgress);
        }
    }
    
    private void fireFinished() {
        Iterator<AlloeProgressListener> apliter = aplListeners.iterator();
        while(apliter.hasNext()) {
            apliter.next().finished();
        }
    }
    
    
    
    /** Start process. It is expected that this function should start the progress
     * in a new thread */
    public void start() {
        theThread = new Thread(this);
        state = STATE_OK;
        theThread.start();
    }
    
    /** Pause the process. The assumption is that this will work by changing a variable
     * in the running thread and then wait for this thread to finish by use of join().
     * It is assumed that the this object is Serializable, otherwise it's your problem
     * to assure the object is ok when resume() is called.
     *
     * @throws CannotPauseException If the process is not in a state where it can be resumed
     */
    public void pause() throws CannotPauseException {
        try {
            if(state == STATE_UNPAUSEABLE)
                throw new CannotPauseException("Some reason");
            state = STATE_STOPPING;
            theThread.join();
        } catch(InterruptedException x) {
            throw new CannotPauseException("The thread was interrupted");
        }
    }
    
    
    /** Resume the process.
     * @see #pause()
     */
    public void resume() {
        theThread = new Thread(this);
        state = STATE_OK;
        theThread.start();
    }
    
    /** Get a string representation of the current action being performed */
    public String getStateMessage() { return "Building Feature Vectors: "; }
    
}
