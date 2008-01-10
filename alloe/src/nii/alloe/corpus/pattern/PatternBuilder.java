package nii.alloe.corpus.pattern;
import nii.alloe.corpus.Corpus;
import nii.alloe.corpus.EachTermPairAction;
import nii.alloe.corpus.TermPairSet;
import java.util.*;

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
public class PatternBuilder {
    
    private PriorityQueue<Pattern> patternQueue;
    private Map<Pattern, Double> patternScores;
    private LinkedList<Pattern> usedPatterns;
    /** Maximum number of iterations */
    public int maxIterations;
    private Corpus corpus;
    private TermPairSet termPairSet;
    private PatternMetric pm;
    
    /** Creates a new instance of PatternBuilder 
     * @param corpus The corpus
     * @param termPairSet The term pairs
     * @param pm A metric used to score new patterns
     */
    public PatternBuilder(Corpus corpus, TermPairSet termPairSet, PatternMetric pm) {
        this.corpus = corpus;
        this.termPairSet = termPairSet;
        this.pm = pm;
        maxIterations = Integer.MAX_VALUE;
    }
    
    /** Build patterns
     * @return A map whose keys are the patterns and values there score as evaluated by the
     * PatternMetric passed to the constructor */
    public Map<Pattern,Double> buildPatterns() {
        buildBasePatterns();
        
        int iterations = 0;
        usedPatterns = new LinkedList();
        
        while(patternQueue.peek() != null && iterations < maxIterations) {
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
        }
        return patternScores;
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
        patternScores = new TreeMap<Pattern,Double>();
        
        termPairSet.forEachPair(new BaseBuilder());
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
                    pat.concat(patternSplit1[i]);
                } else if(patternSplit1[i].equals("*") ||
                        patternSplit1[i].matches("\\w+")) {
                    pat.concat("*");
                } else {
                    pat.concat(" ");
                }
            }
            rpattern.setVal(pat);
            return rpattern;
        }
    }
    
    private void addPattern(Pattern p) {
        patternScores.put(p,pm.scorePattern(p));
        patternQueue.add(p);
    }
    
    private class BaseBuilder implements EachTermPairAction {
         public void doAction(String term1, String term2) {
            Iterator<String> learnData = corpus.getContextsForTerms(term1,term2);
            while(learnData.hasNext()) {
                String s1 = learnData.next();
                String s2 = s1.replaceAll(term1, "1");
                s2 = s2.replaceAll(term2, "2");
                addPattern(new Pattern(s2));
                s2 = s1.replaceAll(term1, "2");
                s2 = s2.replaceAll(term2, "1");
                addPattern(new Pattern(s2));
            }
            
        }
    }
}
