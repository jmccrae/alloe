package nii.alloe.corpus.pattern;
import nii.alloe.corpus.Corpus;
import nii.alloe.corpus.EachTermPairAction;
import nii.alloe.corpus.TermPairSet;
import nii.alloe.niceties.*;
import java.util.*;
import java.io.*;

/**
 * Build a set of patterns from the corpus. Initially whenever a term pair is found in the same
 * document (context) a new pattern is created with these terms replaced with capturers. We can
 * construct a partial order on patterns by saying that a pattern P1 dominates P2 is P1 matches
 * anything that P2 matches. As such if we have two patterns P1, P2, which are alignable (see
 * {@link Pattern#isAlignableWith(Pattern)}), we can find a minimal pattern P3 which dominates P1 and P2.
 * In fact this is very simply found, we align and split P1 and P2 then for each element of the split
 * we use the simple rule
 * <ol><li> If both blocks are equal P3 is this value </li>
 * <li>If both blocks are non-wildcard but not equal, the block in P3 is set to
 * the appropriate wildcard </li>
 * <li>If one block is a non-wildcard and the other a wildcard, the block in P3
 * is set to the wildcard</li>
 * </ol>
 * The algorithm simply places all initial patterns into a priority queue scored by a
 * {@link PatternMetric}. It then moves patterns one-by-one into a set of used patterns,
 * at every stage it finds all such minimal dominators of the current pattern with any
 * used pattern and if this minimal dominator is new and non-trivial (not all wildcards), it
 * adds it to the queue. This continues until {@link #maxIterations} is reached or the
 * queue is empty
 *
 * @see Pattern
 * @see PatternMetric
 *
 * @author John McCrae, National Institute of Informatics
 */
public class PatternBuilder implements AlloeProcess, Serializable, Runnable {
    
    private PriorityQueue<Pattern> patternQueue;
    /** Same as return value of buildPatterns */
    public PatternSet patternScores;
    private LinkedList<Pattern> usedPatterns;
    /** Maximum number of iterations */
    public int maxIterations;
    private Corpus corpus;
    private TermPairSet termPairSet;
    private int iterations;
    private String relationship;
    private String patternMetricName;
    private String basePatternResume;
    private transient PatternMetric pm;
    private transient int state;
    private transient Thread theThread;
    private static final int STATE_OK = 0;
    private static final int STATE_STOPPING = 1;
    private static final int STATE_BASE = 2;
    
    /** Creates a new instance of PatternBuilder
     * @param corpus The corpus
     * @param termPairSet The term pairs
     * @param pm A metric used to score new patterns
     */
    public PatternBuilder(Corpus corpus, TermPairSet termPairSet, String patternMetric, String relationship) {
        this.corpus = corpus;
        this.termPairSet = termPairSet;
        patternMetricName = patternMetric;
        this.pm = PatternMetricFactory.getPatternMetric(patternMetric, corpus, termPairSet);
        this.relationship = relationship;
        maxIterations = Integer.MAX_VALUE;
        state = STATE_OK;
        iterations = 0;
        basePatternResume = null;
    }
    
    /** Build patterns
     * @return A map whose keys are the patterns and values there score as evaluated by the
     * PatternMetric passed to the constructor */
    public PatternSet buildPatterns() {
        run();
        return patternScores;
    }
    
    public void run() {
        
        state = STATE_BASE;
        if(patternQueue == null || basePatternResume != null)
            buildBasePatterns();
        state = STATE_OK;
        
        if(usedPatterns == null)
            usedPatterns = new LinkedList();
        
        while(patternQueue.peek() != null && iterations < maxIterations && state == STATE_OK) {
            Pattern pattern = patternQueue.poll();
            
            Iterator<Pattern> unifIter = usedPatterns.iterator();
            while(unifIter.hasNext()) {
                Pattern unifier = unifIter.next();
                Pattern unification = unify(pattern, unifier);
                if(unification == null)
                    continue;
                unification.makeMostDominant();
                if(unification != null && patternScores.get(unification) == null) {
                    addPattern(unification);
                }
            }
            usedPatterns.add(pattern);
            iterations++;
            if(maxIterations == Integer.MAX_VALUE)
                fireNewProgressChange((double)iterations / (double)(iterations + patternQueue.size()));
            else
                fireNewProgressChange((double)iterations / (double)maxIterations);
        }
        if(state == STATE_OK)
            fireFinished();
    }
    
    
    private void buildBasePatterns() {
        patternQueue = new PriorityQueue<Pattern>(100,
                new Comparator<Pattern>() {
            public int compare(Pattern p1, Pattern p2) {
                return -patternScores.get(p1).compareTo(patternScores.get(p2));
            }
            public boolean equals(Object object) {
                if(object == this) return true; return false;
            }
        });
        patternScores = new PatternSet();
        patternScores.setRelationship(relationship);
        
        termPairSet.forEachPair(new BaseBuilder(), basePatternResume, new PauseSignal() {
            public boolean shouldPause() {
                return state == STATE_STOPPING;
            }
        });
    }
    
    
    
    private Pattern unify(Pattern pattern1, Pattern pattern2) {
        if(!pattern1.isAlignableWith(pattern2)) {
            return null;
        } else {
            Pattern rpattern = new Pattern();
            String pat = "";
            String[] patternSplit1 = pattern1.getAlignmentWith(pattern2);
            String[] patternSplit2 = pattern2.getAlignmentWith(pattern1);
            
            for(int i = 0; i < patternSplit1.length; i++) {
                if(patternSplit1[i].equals(patternSplit2[i])) {
                    pat = pat.concat(patternSplit1[i]);
                } else if(patternSplit1[i].equals("*") ||
                        patternSplit1[i].matches(Pattern.word + "+")) {
                    pat = pat.concat("*");
                } else {
                    pat = pat.concat(" ");
                }
            }
            rpattern.setVal(pat);
            return rpattern.isTrivial() ? null : rpattern;
        }
    }
    
    private void addPattern(Pattern p) {
        if(patternScores.get(p) != null)
            return;
        if(p.isTrivial())
            return;
        patternScores.put(p,pm.scorePattern(p));
        patternQueue.add(p);
        //System.out.println(p.toString() + " {" + patternScores.get(p) + "}");
        firePatternGenerated(p,patternScores.get(p));
    }
    
    private class BaseBuilder implements EachTermPairAction {
        public void doAction(String term1, String term2) {
            Iterator<String> learnData = corpus.getContextsForTerms(term1,term2);
            while(learnData.hasNext()) {
                String s1 = learnData.next();
                s1 = Pattern.makeSafe(s1);
                String[] splitsByTerm1 = s1.split("\\b" + Pattern.cleanTerm(term1) + "\\b");
                if(splitsByTerm1.length < 2)
                    continue;
                String s2;
                for(int i = 1; i < splitsByTerm1.length; i++) {
                    s2 = "";
                    for(int j = 0; j < i; j++) {
                        s2 = s2 + splitsByTerm1[j];
                        if(j + 1 < i) {
                            s2 = s2 + term1;
                        }
                    }
                    s2 = s2 + "1";
                    for(int j = i; j < splitsByTerm1.length; j++) {
                        s2 = s2 + splitsByTerm1[j];
                        if(j + 1 < splitsByTerm1.length) {
                            s2 = s2 + term1;
                        }
                    }
                    String[] splitsByTerm2 = s2.split("\\b" + Pattern.cleanTerm(term2) + "\\b");
                    if(splitsByTerm2.length < 2)
                    continue;
                    for(int k = 1; k < splitsByTerm2.length; k++) {
                        s2 = "";
                        for(int j = 0; j < k; j++) {
                            s2 = s2 + splitsByTerm2[j];
                            if(j + 1 < k) {
                                s2 = s2 + term2;
                            }
                        }
                        s2 = s2 + "2";
                        for(int j = k; j < splitsByTerm2.length; j++) {
                            s2 = s2 + splitsByTerm2[j];
                            if(j + 1 < splitsByTerm2.length) {
                                s2 = s2 + term2;
                            }
                        }
                        addPattern(new Pattern(s2));
                    }
                }
            }
            fireNewProgressChange(termPairSet.getForEachPairProgress(term1,term2));
        }
    }
    
   
    private transient LinkedList<AlloeProgressListener> listeners;
    
    // AlloeProcess functions
    /** Register a progress listener */
    public void addProgressListener(AlloeProgressListener apl) {
        if(listeners == null)
            listeners = new LinkedList<AlloeProgressListener>();
        listeners.add(apl);
    }
    
    
    private void fireNewProgressChange(double newProgress) {
        Iterator<AlloeProgressListener> apliter = listeners.iterator();
        while(apliter.hasNext()) {
            apliter.next().progressChange(newProgress);
        }
    }
    
    private void fireFinished() {
        Iterator<AlloeProgressListener> apliter = listeners.iterator();
        while(apliter.hasNext()) {
            apliter.next().finished();
        }
    }
    
    private void firePatternGenerated(Pattern p, double score) {
        Iterator<AlloeProgressListener> apliter = listeners.iterator();
        while(apliter.hasNext()) {
            AlloeProgressListener apl = apliter.next();
            if(apl instanceof PatternBuilderListener) {
                ((PatternBuilderListener)apl).patternGenerated(p,score);
            }
        }
    }
    
    
    /** Start process. It is expected that this function should start the progress
     * in a new thread */
    public void start() {
        theThread = new Thread(this);
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
        state = STATE_STOPPING;
        try {
            theThread.join();
        } catch(InterruptedException x) {
            throw new CannotPauseException("The thread was interrupted!");
        }
        
    }
    
    /** Resume the process.
     * @see #pause()
     */
    public void resume() {
        state = STATE_OK;
        theThread = new Thread(this);
        theThread.start();
    }
    
    public String getStateMessage() {
        if(state == STATE_BASE)
            return "Building Base Patterns: ";
        else
            return "Generating Patterns: ";
    }
    
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        state = STATE_OK;
        pm = PatternMetricFactory.getPatternMetric(patternMetricName, corpus, termPairSet);
    }
    
    /** Set the pattern metric
     * @throws PatternMetricUnknown If the pattern metric is not recognissed but PatternMetricFactory
     * @see PatternMetricFactory
     */
    public void setPatternMetric(String patternMetric) {
        patternMetricName = patternMetric;
        this.pm = PatternMetricFactory.getPatternMetric(patternMetric, corpus, termPairSet);
    }
    
    /** @return true is variables like pattern metric should be possible to change */
    public boolean isRunning() { return state == STATE_OK; }
    
    /** Get the relationship name */
    public String getRelationship() { return relationship; }
}
