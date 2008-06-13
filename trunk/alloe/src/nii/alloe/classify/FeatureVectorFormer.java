package nii.alloe.classify;

import java.util.*;
import java.io.*;
import nii.alloe.corpus.Corpus;
import nii.alloe.corpus.TermPairSet;
import nii.alloe.corpus.pattern.*;
import nii.alloe.tools.process.AlloeProcess;
import nii.alloe.tools.process.AlloeProgressListener;
import nii.alloe.tools.process.CannotPauseException;
import weka.core.*;

/**
 * Form a set of feature vectors using a corpus and set of patterns.
 *
 * @author John McCrae, National Institute of Informatics
 */
public class FeatureVectorFormer implements AlloeProcess, Serializable, Runnable {

    private Vector<String> relation;
    private Collection<String> terms;
    private Vector<PatternSet> patterns;
    private Corpus corpus;
    private Vector<TermPairSet> termPairs;
    /** The data set that the data should be entered into */
    public DataSet dataSet;
    private HashMap<String, SparseInstance> instances;
    private static final String glue = " => ";

    /** Create a new feature vector former
     * @param relation The relation to create data for
     * @param patterns The pattern set to use to create vectors;
     * @param termPairs The true/false value of a particular term pair set, maybe null, if so all termPairs default to negative class
     * @param corpus The corpus
     **/
    public FeatureVectorFormer(Vector<String> relation, Vector<PatternSet> patterns, Corpus corpus, Vector<TermPairSet> termPairs) {
        this.relation = relation;
        this.terms = corpus.terms;
        this.patterns = patterns;
        this.corpus = corpus;
        this.termPairs = termPairs;
        instances = new HashMap<String, SparseInstance>();
        i = 0;
    }
    private String term1,  term2;
    private int i;

    /**
     * Makes a set of feature vectors
     * @return dataSet if the value passed was non-null, else a new DataSet with all information inserted */
    public DataSet makeFeatureVectors() {
        for (int ri = 0; ri < relation.size(); ri++) {
            if (dataSet == null) {
                dataSet = new DataSet(corpus.terms);
            }
            if (!dataSet.isRelationPrepared(relation.get(ri))) {
                dataSet.prepRelation(relation.get(ri), getAttNames(patterns.get(ri)));
            }

            fireNewProgressChange((double) i / (double) patterns.get(ri).size());


            Iterator<Pattern> patIter = patterns.get(ri).keySet().iterator();
            for (int j = 0; j < i; j++) {
                patIter.next();
            }
            double corpusSize = corpus.size();
            while (patIter.hasNext() && state == STATE_OK) {
                Pattern p = patIter.next();
                Iterator<Corpus.Hit> contexts = corpus.getContextsForPattern(p);
                if (contexts == null) {
                    continue;
                }
                while (contexts.hasNext()) {
                    Corpus.Hit context = contexts.next();
                    String text = context.getText();
                    String[] terms = context.getTerms();
                    for (int j = 0; j < terms.length; j++) {
                        for (int k = 0; k < terms.length; k++) {
                            if (p.matches(text, terms[j], terms[k], isLazyMatching())) {
                                SparseInstance inst = instances.get(terms[j] + glue + terms[k]);
                                if (inst == null) {
                                    instances.put(terms[j] + glue + terms[k], inst = new SparseInstance(1.0, new double[patterns.size() + 1]));
                                }
                                inst.setValue(i, inst.value(i) + 1.0); // (double)corpusSize);
                            }
                        }
                    }
                }
                i++;
                fireNewProgressChange((double) i / (double) patterns.get(ri).size() * (ri + 1) / relation.size());
            }
            corpus.clearTermsInCorpusCache();

            Iterator<Map.Entry<String, SparseInstance>> entryIter = instances.entrySet().iterator();
            while (entryIter.hasNext()) {
                Map.Entry<String, SparseInstance> entry = entryIter.next();
                String terms = entry.getKey();
                SparseInstance inst = entry.getValue();
                String[] ss = terms.split(glue);
                assert ss.length == 2;
                term1 = ss[0];
                term2 = ss[1];
                if (termPairs != null) {
                    inst.setValue(patterns.get(ri).size(), dataSet.getClassVal(termPairs.get(ri).contains(term1, term2)));
                } else {
                    inst.setValue(patterns.get(ri).size(), dataSet.getClassVal(false));
                }
                dataSet.addInstance(inst,
                        relation.get(ri), term1, term2);
                entryIter.remove();
            }

        }
        
            fireFinished();

        return dataSet;
    }

    public void run() {
        makeFeatureVectors();
    }

    private Iterator<String> getAttNames(PatternSet patterns) {
        return new AttNameIterator(patterns);
    }

    public final class AttNameIterator implements Iterator<String> {

        Iterator<Pattern> patterns;

        AttNameIterator(PatternSet p) {
            patterns = p.keySet().iterator();
        }

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
        if (aplListeners == null) {
            aplListeners = new LinkedList<AlloeProgressListener>();
        }
        if (!aplListeners.contains(apl)) {
            aplListeners.add(apl);
        }
    }

    private void fireNewProgressChange(double newProgress) {
        Iterator<AlloeProgressListener> apliter = aplListeners.iterator();
        while (apliter.hasNext()) {
            apliter.next().progressChange(newProgress);
        }
    }

    private void fireFinished() {
        Iterator<AlloeProgressListener> apliter = aplListeners.iterator();
        while (apliter.hasNext()) {
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
            if (state == STATE_UNPAUSEABLE) {
                throw new CannotPauseException("Some reason");
            }
            state = STATE_STOPPING;
            theThread.join();
        } catch (InterruptedException x) {
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
    public String getStateMessage() {
        return "Building Feature Vectors: ";
    }
    /**
     * Holds value of property lazyMatching.
     */
    private boolean lazyMatching;

    /**
     * Getter for property lazyMatching.
     * @return Value of property lazyMatching.
     */
    public boolean isLazyMatching() {
        return this.lazyMatching;
    }

    /**
     * Setter for property lazyMatching.
     * @param lazyMatching New value of property lazyMatching.
     */
    public void setLazyMatching(boolean lazyMatching) {
        this.lazyMatching = lazyMatching;
    }
}
