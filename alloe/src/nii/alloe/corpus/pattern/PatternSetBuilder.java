package nii.alloe.corpus.pattern;
import nii.alloe.corpus.*;
import nii.alloe.tools.struct.*;
import java.util.*;

/**
 *
 * @author John McCrae, National Institute of Informatics
 */
public class PatternSetBuilder extends PatternBuilder {
    
    TreeMap<Pattern,TermPairSet> positives;
    TreeMap<Pattern,TermPairSet> negatives;
    TreeMap<String, Integer> posCounts;
    TreeMap<String, Integer> negCounts;
    
    /** Creates a new instance of PatternSetBuilder */
    public PatternSetBuilder(Corpus corpus, TermPairSet termPairSet, String relationship) {
        super(corpus,termPairSet,PatternMetricFactory.PSEUDO_FM,relationship);
        positives = new TreeMap<Pattern,TermPairSet>();
        negatives = new TreeMap<Pattern,TermPairSet>();
        patternCounter = new MultiSet<Pattern>();
    }
    
  
    private static final String glue = " => ";
    
    void addPattern(Pattern pattern, String f1, String f2) {
        System.out.print(pattern.toString());
        if(patternScores.get(pattern) != null)
            return;
        if(pattern.isTrivial())
            return;
        TermPairSet patternPositives = new TermPairSet();
        TermPairSet patternNegatives = new TermPairSet();
        Iterator<Corpus.Hit> contexts = corpus.getContextsForPattern(pattern);
        if(contexts == null)
            return;
        LOOP : while(contexts.hasNext()) {
            Corpus.Hit context = contexts.next();
            String text = context.getText();
            String[] terms = context.getTerms();
            for(int i = 0; i < terms.length; i++) {
                for(int j = 0; j < terms.length; j++) {
                    if(pattern.matches(text, terms[i],terms[j])) {
                        if(termPairSet.contains(terms[i],terms[j])) {
                            patternPositives.add(terms[i],terms[j]);
                        } else {
                            patternNegatives.add(terms[i],terms[j]);
                        }
                    }
                }
            }
        }
        double patternScore = 2.0 * ((double)patternPositives.size() / (double)(patternPositives.size() + patternNegatives.size() + termPairSet.size()));
        if(patternCounter.size() < getMaxPatterns()) {
            System.out.println("");
            patternScores.put(pattern,patternScore);
            patternQueue.add(pattern);
            patternCounter.add(pattern);
            positives.put(pattern,patternPositives);
            negatives.put(pattern,patternNegatives);
            firePatternGenerated(pattern,patternScore);
        } else {
            if(posCounts == null) {
                initCounts();
            }
            Pattern bestPattern = findBestReplacement(patternPositives, patternNegatives);
            if(bestPattern != null) {
                System.out.println("... replacing " + bestPattern.toString());
                boolean b = patternCounter.remove(bestPattern);
                firePatternDropped(bestPattern);
                updateCounts(bestPattern, patternPositives, patternNegatives);
                patternScores.put(pattern,patternScore);
                patternQueue.add(pattern);
                patternCounter.add(pattern);
                positives.put(pattern,patternPositives);
                negatives.put(pattern,patternNegatives);
                firePatternGenerated(pattern,patternScore);
            } else {
                System.out.println("... no good");
                patternScores.put(pattern,patternScore);
                patternQueue.add(pattern);
            }
        }
    }
    
    void updateCounts(Pattern bestPattern, TermPairSet patternPositives, TermPairSet patternNegatives) {
        TermPairSet bestPositives = positives.get(bestPattern);
        for(String[] terms : bestPositives) {
            int i = posCounts.get(terms[0] + glue + terms[1]);
            if(i == 1)
                posCounts.remove(terms[0]+ glue + terms[1]);
            else
                posCounts.put(terms[0] + glue + terms[1],i-1);
        }
        for(String[] terms : patternPositives) {
            if(posCounts.containsKey(terms[0] + glue + terms[1])) {
                posCounts.put(terms[0] + glue + terms[1], posCounts.get(terms[0] + glue + terms[1])+1);
            } else {
                posCounts.put(terms[0] + glue + terms[1],1);
            }
        }
        TermPairSet bestNegatives = negatives.get(bestPattern);
        for(String[] terms : bestNegatives) {
            int i = negCounts.get(terms[0] + glue + terms[1]);
            if(i == 1)
                negCounts.remove(terms[0]+ glue + terms[1]);
            else
                negCounts.put(terms[0] + glue + terms[1],i-1);
        }
        for(String[] terms : patternNegatives) {
            if(negCounts.containsKey(terms[0] + glue + terms[1])) {
                negCounts.put(terms[0] + glue + terms[1], negCounts.get(terms[0] + glue + terms[1])+1);
            } else {
                negCounts.put(terms[0] + glue + terms[1],1);
            }
        }
    }
    
    static Integer one = new Integer(1);
    
    Pattern findBestReplacement(TermPairSet patternPositives, TermPairSet patternNegatives) {
        double bestImprov = 0;
        Pattern bestPattern = null;
        double baseFM = 2.0 * ((double)posCounts.size() / (double)(posCounts.size() + negCounts.size() + termPairSet.size()));
        int posGain = 0;
        int negGain = 0;
        for(String[] terms : patternPositives) {
            if(posCounts.get(terms[0] + glue + terms[1]) == null)
                posGain++;
        }
        for(String[] terms : patternNegatives) {
            if(negCounts.get(terms[0] + glue + terms[1]) == null) {
                negGain++;
            }
        }
        for(Pattern p2 : patternCounter) {
            TermPairSet patternPositives2 = positives.get(p2);
            int posGain2 = posGain;
            
            for(String[] terms : patternPositives2) {
                if(posCounts.get(terms[0] + glue + terms[1]).equals(one)) {
                    posGain2--;
                }
            }
            patternPositives2 = null;
            int negGain2 = negGain;
            TermPairSet patternNegatives2 = negatives.get(p2);
            
            for(String[] terms : patternNegatives2) {
                if(negCounts.get(terms[0] + glue + terms[1]).equals(one)) {
                    negGain2--;
                }
            }
            double newFM = 2.0 * (((double)posCounts.size() + posGain2) / (double)
            (posCounts.size() + posGain2 + negCounts.size() + negGain2 + termPairSet.size()));
            if(newFM - baseFM > bestImprov) {
                bestImprov = newFM - baseFM;
                bestPattern = p2;
            }
        }
        return bestPattern;
    }
    
    void initCounts() {
        posCounts = new TreeMap<String,Integer>();
        for(TermPairSet pos : positives.values()) {
            for(String[] terms : pos) {
                if(posCounts.containsKey(terms[0] + glue + terms[1])) {
                    posCounts.put(terms[0] + glue + terms[1], posCounts.get(terms[0] + glue + terms[1])+1);
                } else {
                    posCounts.put(terms[0] + glue + terms[1],1);
                }
            }
        }
        negCounts = new TreeMap<String,Integer>();
        for(TermPairSet neg : negatives.values()) {
            for(String[] terms : neg) {
                if(negCounts.containsKey(terms[0] + glue + terms[1])) {
                    negCounts.put(terms[0] + glue + terms[1], negCounts.get(terms[0] + glue + terms[1])+1);
                } else {
                    negCounts.put(terms[0] + glue + terms[1],1);
                }
            }
        }
    }
    
}
