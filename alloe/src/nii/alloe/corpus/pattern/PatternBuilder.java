package nii.alloe.corpus.pattern;
import nii.alloe.corpus.Corpus;
import nii.alloe.corpus.EachTermPairAction;
import nii.alloe.corpus.TermPairSet;
import java.util.*;
import java.io.*;
import nii.alloe.tools.process.*;
import nii.alloe.tools.struct.ConcurrentLinkedList;
import nii.alloe.tools.struct.MultiSet;

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
public class PatternBuilder extends AlloeProcessAdapter {
    
    transient PriorityQueue<Pattern> patternQueue;
    /** Same as return value of buildPatterns */
    public PatternSet patternScores;
    ConcurrentLinkedList<Pattern> usedPatterns;
    int maxIterations;
    Corpus corpus;
    TermPairSet termPairSet;
    int iterations;
    String relationship;
    String patternMetricName;
    String basePatternResume;
    transient MultiSet<Pattern> patternCounter;
    transient PatternMetric pm;
    static final int STATE_BASE = 3;
    
    /** Creates a new instance of PatternBuilder
     * @param corpus The corpus
     * @param termPairSet The term pairs
     * @param patternMetric A metric used to score new patterns
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
        //corpus.initTermsInCorpusCache();
        state = STATE_BASE;
        if(patternQueue == null || basePatternResume != null)
            buildBasePatterns();
        if(state == STATE_BASE && !isGenerateBaseOnly())
            state = STATE_OK;
        else {
            if(isGenerateBaseOnly())
                fireFinished();
            return;
        }
        
        if(usedPatterns == null)
            usedPatterns = new ConcurrentLinkedList();
        
        while(patternQueue.peek() != null && iterations < maxIterations && state == STATE_OK) {
            Pattern pattern = patternQueue.poll();
            
            Iterator<Pattern> unifIter = usedPatterns.iterator();
            while(unifIter.hasNext()) {
                Pattern unifier = unifIter.next();
                if(unifier == null)
                    break;
                Pattern unification = unify(pattern, unifier);
                if(unification == null)
                    continue;
                unification.makeMostDominant();
                if(unification != null && patternScores.get(unification) == null) {
                    addPattern(unification, pattern.toString(), unifier.toString());
                }
            }
            usedPatterns.add(pattern);
            iterations++;
            if(maxIterations == Integer.MAX_VALUE)
                fireNewProgressChange((double)iterations / (double)(iterations + patternQueue.size()));
            else
                fireNewProgressChange((double)iterations / (double)maxIterations);
        }
        if(getMaxPatterns() > 0) {
            Iterator<Pattern> psIter = patternScores.keySet().iterator();
            while(psIter.hasNext()) {
                Pattern p2  = psIter.next();
                if(!patternCounter.contains(p2))
                    psIter.remove();
            }
        }
        if(state == STATE_OK)
            fireFinished();
        //corpus.clearTermsInCorpusCache();
    }
    
    
    protected void buildBasePatterns() {
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
    
    
    
    protected Pattern unify(Pattern pattern1, Pattern pattern2) {
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
    
    void addPattern(Pattern p, String f1, String f2) {
        if(patternScores.get(p) != null)
            return;
        if(p.isTrivial())
            return;
        System.out.println(p.toString());
        double d = pm.scorePattern(p);
        if(d < 0 || d > 1 || Double.isInfinite(d) || Double.isNaN(d)) {
            Output.err.println(p.toString());
            Output.err.println("From 1: " + f1);
            Output.err.println("From 2: " + f2);
        }
        if(getMaxPatterns() > 0 && patternCounter.size() == getMaxPatterns()) {
            if(patternScores.get(patternCounter.first()) >= d)
                return;
            else {
                Pattern drop = patternCounter.first();
                if(!patternQueue.remove(drop))
                    usedPatterns.remove(drop);
                firePatternDropped(drop);
                patternCounter.remove(drop);
                patternScores.remove(drop);
            }
        }
        if(d < getScoreFilter())
            return;
        patternScores.put(p,d);
        patternQueue.add(p);
        if(getMaxPatterns() > 0)
            patternCounter.add(p);
        
        //System.out.println(p.toString() + " {" + patternScores.get(p) + "}");
        firePatternGenerated(p,patternScores.get(p));
    }
    
    class BaseBuilder implements EachTermPairAction {
        public void doAction(String term1, String term2) {
            if(isIgnoreReflexives() && term1.equals(term2))
                return;
            Iterator<Corpus.Hit> learnData = corpus.getContextsForTerms(term1,term2);
            while(learnData.hasNext()) {
                String s1 = learnData.next().getText();
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
                            s2 = s2 + Pattern.cleanTerm(term1).toLowerCase();
                        }
                    }
                    s2 = s2 + "1";
                    for(int j = i; j < splitsByTerm1.length; j++) {
                        s2 = s2 + splitsByTerm1[j];
                        if(j + 1 < splitsByTerm1.length) {
                            s2 = s2 + Pattern.cleanTerm(term1).toLowerCase();
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
                                s2 = s2 + Pattern.cleanTerm(term2).toLowerCase();
                            }
                        }
                        s2 = s2 + "2";
                        for(int j = k; j < splitsByTerm2.length; j++) {
                            s2 = s2 + splitsByTerm2[j];
                            if(j + 1 < splitsByTerm2.length) {
                                s2 = s2 + Pattern.cleanTerm(term2).toLowerCase();
                            }
                        }
                        addPattern(new Pattern(s2),term1,term2);
                    }
                }
            }
            fireNewProgressChange(termPairSet.getForEachPairProgress(term1,term2));
        }
    }
    
    
    void firePatternGenerated(Pattern p, double d) {
        Iterator<AlloeProgressListener> apliter = aplListeners.iterator();
        while(apliter.hasNext()) {
            AlloeProgressListener apl = apliter.next();
            if(apl instanceof PatternSetListener) {
                ((PatternSetListener)apl).patternGenerated(p,d);
            }
        }
    }
    
    
    void firePatternDropped(Pattern p) {
        Iterator<AlloeProgressListener> apliter = aplListeners.iterator();
        while(apliter.hasNext()) {
            AlloeProgressListener apl = apliter.next();
            if(apl instanceof PatternSetListener) {
                ((PatternSetListener)apl).patternDropped(p);
            }
        }
    }
    
    void fireClearPatterns() {
        Iterator<AlloeProgressListener> apliter = aplListeners.iterator();
        while(apliter.hasNext()) {
            AlloeProgressListener apl = apliter.next();
            if(apl instanceof PatternSetListener) {
                ((PatternSetListener)apl).clearPatterns();
            }
        }
    }
    
    
    public String getStateMessage() {
        if(state == STATE_BASE)
            return "Building Base Patterns: ";
        else
            return "Generating Patterns: ";
    }
    
    void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        state = STATE_OK;
        pm = PatternMetricFactory.getPatternMetric(patternMetricName, corpus, termPairSet);
        PriorityQueue o = (PriorityQueue)ois.readObject();
        patternQueue = new PriorityQueue<Pattern>(100,
                new Comparator<Pattern>() {
            public int compare(Pattern p1, Pattern p2) {
                return -patternScores.get(p1).compareTo(patternScores.get(p2));
            }
            public boolean equals(Object object) {
                if(object == this) return true; return false;
            }
        });
        patternQueue.addAll(o);
        if(getMaxPatterns() > 0) {
            patternCounter = new MultiSet<Pattern>(new Comparator<Pattern>() {
                public int compare(Pattern obj1, Pattern obj2) {
                    return patternScores.get(obj1).compareTo(patternScores.get(obj2));
                }
            });
            patternCounter.addAll(patternQueue);
        }
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
    
    /**
     * Holds value of property generateBaseOnly.
     */
    boolean generateBaseOnly = false;
    
    /**
     * Getter for property generateBaseOnly.
     * @return Value of property generateBaseOnly.
     */
    public boolean isGenerateBaseOnly() {
        return this.generateBaseOnly;
    }
    
    /**
     * Setter for property generateBaseOnly.
     * @param generateBaseOnly New value of property generateBaseOnly.
     */
    public void setGenerateBaseOnly(boolean generateBaseOnly) {
        this.generateBaseOnly = generateBaseOnly;
    }
    
    /**
     * Holds value of property scoreFilter.
     */
    double scoreFilter = 0;
    
    /**
     * Getter for property scoreFilter.
     * @return Value of property scoreFilter.
     */
    public double getScoreFilter() {
        return this.scoreFilter;
    }
    
    /**
     * Setter for property scoreFilter. Setting this value will cause the system
     * to ignore patterns whose score is less than scoreFilter. Setting zero accepts
     * all patterns (as metric should always report zero or more).
     * @param scoreFilter New value of property scoreFilter.
     */
    public void setScoreFilter(double scoreFilter) {
        this.scoreFilter = scoreFilter;
    }
    
    /**
     * Holds value of property maxPatterns.
     */
    int maxPatterns = -1;
    
    /**
     * Getter for property maxPatterns.
     * @return Value of property maxPatterns.
     */
    public int getMaxPatterns() {
        return this.maxPatterns;
    }
    
    /**
     * Setter for property maxPatterns.
     * @param maxPatterns New value of property maxPatterns.
     */
    public void setMaxPatterns(int maxPatterns) {
        this.maxPatterns = maxPatterns;
        if(maxPatterns > 0) {
            patternCounter = new MultiSet<Pattern>(new Comparator<Pattern>() {
                public int compare(Pattern obj1, Pattern obj2) {
                    return patternScores.get(obj1).compareTo(patternScores.get(obj2));
                }
            });
        } else {
            patternCounter = null;
        }
        
    }
    
    /**
     * Set the base patterns
     */
    public void setBasePatterns(PatternSet patternSet) {
        if(patternSet == null) {
            patternScores = null;
            patternQueue = null;
            fireClearPatterns();
            return;
        }
        if(patternScores == null)
            patternScores = patternSet;
        else {
            patternScores.putAll(patternSet);
        }
        if(patternQueue == null) {
            patternQueue = new PriorityQueue<Pattern>(100,
                    new Comparator<Pattern>() {
                public int compare(Pattern p1, Pattern p2) {
                    return -patternScores.get(p1).compareTo(patternScores.get(p2));
                }
                public boolean equals(Object object) {
                    if(object == this) return true; return false;
                }
            });
        }
        Iterator<Pattern> patIter = patternSet.keySet().iterator();
        while(patIter.hasNext()) {
            Pattern p = patIter.next();
            double d = patternScores.get(p);
            if(getMaxPatterns() > 0 && patternCounter.size() == getMaxPatterns()) {
                if(patternScores.get(patternCounter.first()) >= d)
                    continue;
                else {
                    patternScores.remove(patternCounter.first());
                    if(!patternQueue.remove(patternCounter.first()))
                        usedPatterns.remove(patternCounter.first());
                    firePatternDropped(patternCounter.first());
                    patternCounter.remove(patternCounter.first());
                }
            }
            if(d < getScoreFilter())
                continue;
            patternQueue.add(p);
            firePatternGenerated(p,d);
        }
    }
    
    void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
        oos.writeObject(new PriorityQueue(patternQueue));
    }
    
    public void setMaxIterations(int maxIteration) {
        this.maxIterations = maxIterations;
    }
    
    public void unsetMaxIterations() {
        this.maxIterations = Integer.MAX_VALUE;
    }
    
    /**
     * Holds value of property ignoreReflexives.
     */
    boolean ignoreReflexives;
    
    /**
     * Getter for property ignoreReflexives.
     * @return Value of property ignoreReflexives.
     */
    public boolean isIgnoreReflexives() {
        return this.ignoreReflexives;
    }
    
    /**
     * Setter for property ignoreReflexives.
     * @param ignoreReflexives New value of property ignoreReflexives.
     */
    public void setIgnoreReflexives(boolean ignoreReflexives) {
        this.ignoreReflexives = ignoreReflexives;
    }
}
