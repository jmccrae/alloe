package nii.alloe.corpus.pattern;

import nii.alloe.corpus.*;
import nii.alloe.tools.struct.*;
import java.util.*;

/**
 *
 * @author John McCrae, National Institute of Informatics
 */
public class PatternSetBuilder extends PatternBuilder {

    TreeMap<Pattern, TermPairSet> positives;
    TreeMap<Pattern, TermPairSet> negatives;
    TreeMap<String, Integer> posCounts;
    TreeMap<String, Integer> negCounts;
    Map<Pattern, Integer> posLoss;
    Map<Pattern, Integer> negLoss;
    Map<Pattern, Integer> posBothLoss;
    Map<Pattern, Integer> negBothLoss;
    int posGain;
    int negGain;

    /** Creates a new instance of PatternSetBuilder */
    public PatternSetBuilder(Corpus corpus, TermPairSet termPairSet, String relationship, int maxPatterns) {
        super(corpus, termPairSet, PatternMetricFactory.PSEUDO_FM, relationship);
        positives = new TreeMap<Pattern, TermPairSet>();
        negatives = new TreeMap<Pattern, TermPairSet>();
        patternCounter = new MultiSet<Pattern>();
        setMaxPatterns(maxPatterns);
        posLoss = new TreeMap<Pattern, Integer>();
        negLoss = new TreeMap<Pattern, Integer>();
    }
    private static final String glue = " => ";
    double sketchAmount;
    TermPairSet patternPositives;
    TermPairSet patternNegatives;
    Iterator<Corpus.Hit> contexts;

    void addPattern(Pattern pattern, String f1, String f2) {
        if (patternScores.get(pattern) != null) {
            return;
        }
        if (pattern.isTrivial()) {
            return;
        }

        calculateEnoughPositivesNegatives(pattern);

        double patternScore;
        if (patternCounter.size() < getMaxPatterns()) {
            System.out.println("");
            finishCalculatingPositivesNegatives(pattern);
            patternScore = 2.0 * ((double) patternPositives.size() / (double) (patternPositives.size() + patternNegatives.size() + termPairSet.size()));
            patternScores.put(pattern, patternScore);
            patternQueue.add(pattern);
            patternCounter.add(pattern);
            positives.put(pattern, patternPositives);
            negatives.put(pattern, patternNegatives);
            firePatternGenerated(pattern, patternScore);
        } else {
            if (posCounts == null) {
                initCounts();
            }
            Pattern bestPattern = findBestReplacement();
            if (bestPattern != null) {
                System.out.println("... replacing " + bestPattern.toString());
                finishCalculatingPositivesNegatives(pattern);
                patternScore = 2.0 * ((double) patternPositives.size() / (double) (patternPositives.size() + patternNegatives.size() + termPairSet.size()));
                boolean b = patternCounter.remove(bestPattern);
                firePatternDropped(bestPattern);
                updateCounts(pattern, bestPattern, patternPositives, patternNegatives);
                patternScores.put(pattern, patternScore);
                patternQueue.add(pattern);
                patternCounter.add(pattern);
                positives.put(pattern, patternPositives);
                negatives.put(pattern, patternNegatives);
                firePatternGenerated(pattern, patternScore);
            } else {
                System.out.println("... no good");
                patternScore = 2.0 * ((double) patternPositives.size() / sketchAmount / (double) ((patternPositives.size() + patternNegatives.size()) / sketchAmount + termPairSet.size()));
                patternScores.put(pattern, patternScore);
                patternQueue.add(pattern);
            }
        }
    }

    /** Calculate positives and negatives up to the maximum number of sketches
     */
    void calculateEnoughPositivesNegatives(Pattern pattern) {
        patternPositives = new TermPairSet();
        patternNegatives = new TermPairSet();
        posBothLoss = new HashMap<Pattern, Integer>(patternCounter.size());
        negBothLoss = new HashMap<Pattern, Integer>(patternCounter.size());
        for (Pattern p : patternCounter) {
            posBothLoss.put(p, 0);
            negBothLoss.put(p, 0);
        }
        posGain = negGain = 0;
        contexts = corpus.getContextsForPattern(pattern);
        if (contexts == null) {
            return;
        }
        sketchAmount = 1.0;
        int contextsSeen = 0;
        System.out.print(pattern.toString());
        while (contexts.hasNext()) {
            Corpus.Hit context = contexts.next();
            String text = context.getText();
            String[] terms = context.getTerms();
            for (int i = 0; i < terms.length; i++) {
                for (int j = 0; j < terms.length; j++) {
                    if (pattern.matches(text, terms[i], terms[j])) {
                        if (termPairSet.contains(terms[i], terms[j])) {
                            patternPositives.add(terms[i], terms[j]);
                            if (posCounts != null &&
                                    !posCounts.containsKey(terms[i] + glue + terms[j])) {
                                posGain++;
                            } else if (posCounts != null) {
                                for (Pattern p : patternCounter) {
                                    if (positives.get(p).contains(terms)) {
                                        posBothLoss.put(p, posBothLoss.get(p) + 1);
                                    }
                                }
                            }
                        } else {
                            patternNegatives.add(terms[i], terms[j]);
                            if (negCounts != null &&
                                    !negCounts.containsKey(terms[i] + glue + terms[j])) {
                                negGain++;
                            } else if (negCounts != null) {
                                for (Pattern p : patternCounter) {
                                    if (negatives.get(p).contains(terms)) {
                                        negBothLoss.put(p, negBothLoss.get(p) + 1);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (contextsSeen == getSketchSize()) {
                sketchAmount = (double) corpus.getHitsFromIterator(contexts) / (double) contextsSeen;
                break;
            }
            contextsSeen++;
        }
    }

    /** Complete the calculation of positives and negatives */
    void finishCalculatingPositivesNegatives(Pattern pattern) {
        while (contexts.hasNext()) {
            Corpus.Hit context = contexts.next();
            String text = context.getText();
            String[] terms = context.getTerms();
            for (int i = 0; i < terms.length; i++) {
                for (int j = 0; j < terms.length; j++) {
                    if (pattern.matches(text, terms[i], terms[j])) {
                        if (termPairSet.contains(terms[i], terms[j])) {
                            patternPositives.add(terms[i], terms[j]);
                        } else {
                            patternNegatives.add(terms[i], terms[j]);
                        }
                    }
                }
            }
        }
    }

    void updateCounts(Pattern pattern, Pattern bestPattern, TermPairSet patternPositives, TermPairSet patternNegatives) {
        TermPairSet bestPositives = positives.get(bestPattern);
        for (String[] terms : bestPositives) {
            int i = posCounts.get(terms[0] + glue + terms[1]);
            if (i == 1) {
                posCounts.remove(terms[0] + glue + terms[1]);
            } else if (i == 2) {
                posCounts.put(terms[0] + glue + terms[1], 1);
                for (Pattern p : patternCounter) {
                    if (positives.get(p).contains(terms)) {
                        posLoss.put(p, posLoss.get(p) + 1);
                    }
                }
            } else {
                posCounts.put(terms[0] + glue + terms[1], i - 1);
            }
        }
        posLoss.put(pattern, 0);
        for (String[] terms : patternPositives) {
            if (posCounts.containsKey(terms[0] + glue + terms[1])) {
                posCounts.put(terms[0] + glue + terms[1], posCounts.get(terms[0] + glue + terms[1]) + 1);
            } else {
                posCounts.put(terms[0] + glue + terms[1], 1);
                posLoss.put(pattern, posLoss.get(pattern) + 1);
            }
        }
        TermPairSet bestNegatives = negatives.get(bestPattern);
        for (String[] terms : bestNegatives) {
            int i = negCounts.get(terms[0] + glue + terms[1]);
            if (i == 1) {
                negCounts.remove(terms[0] + glue + terms[1]);
            } else if (i == 2) {
                negCounts.put(terms[0] + glue + terms[1], 1);
                for (Pattern p : patternCounter) {
                    if (negatives.get(p).contains(terms)) {
                        negLoss.put(p, negLoss.get(p) + 1);
                    }
                }
            } else {
                negCounts.put(terms[0] + glue + terms[1], i - 1);
            }
        }
        negLoss.put(pattern, 0);
        for (String[] terms : patternNegatives) {
            if (negCounts.containsKey(terms[0] + glue + terms[1])) {
                negCounts.put(terms[0] + glue + terms[1], negCounts.get(terms[0] + glue + terms[1]) + 1);
            } else {
                negCounts.put(terms[0] + glue + terms[1], 1);
                negLoss.put(pattern, negLoss.get(pattern) + 1);
            }
        }
    }
    /** The current score of the pattern set */
    public double currentFM = 0;

    Pattern findBestReplacement() {
        double bestImprov = 0;
        Pattern bestPattern = null;
        currentFM = 2.0 * ((double) posCounts.size() / (double) (posCounts.size() + negCounts.size() + termPairSet.size()));
        for (Pattern p2 : patternCounter) {
            double posGain2 = (double) posGain / sketchAmount - (double) posLoss.get(p2) + (double) posBothLoss.get(p2) / sketchAmount;
            double negGain2 = (double) negGain / sketchAmount - (double) negLoss.get(p2) + (double) negBothLoss.get(p2) / sketchAmount;
            double newFM = 2.0 * (((double) posCounts.size() + posGain2) / (double) (posCounts.size() + posGain2 + negCounts.size() + negGain2 + termPairSet.size()));
            if (newFM - currentFM > bestImprov) {
                bestImprov = newFM - currentFM;
                bestPattern = p2;
            }
        }
        return bestPattern;
    }

    void initCounts() {
        HashMap<String, Pattern> termPairToPattern = new HashMap<String, Pattern>();
        posCounts = new TreeMap<String, Integer>();
        for (Map.Entry<Pattern, TermPairSet> entry : positives.entrySet()) {
            TermPairSet pos = entry.getValue();
            for (String[] terms : pos) {
                if (posCounts.containsKey(terms[0] + glue + terms[1])) {
                    posCounts.put(terms[0] + glue + terms[1], posCounts.get(terms[0] + glue + terms[1]) + 1);
                    termPairToPattern.remove(terms[0] + glue + terms[1]);
                } else {
                    posCounts.put(terms[0] + glue + terms[1], 1);
                    termPairToPattern.put(terms[0] + glue + terms[1],
                            entry.getKey());
                }
            }
            posLoss.put(entry.getKey(), 0);
        }
        negCounts = new TreeMap<String, Integer>();
        for (Map.Entry<Pattern, TermPairSet> entry : negatives.entrySet()) {
            TermPairSet neg = entry.getValue();
            for (String[] terms : neg) {
                if (negCounts.containsKey(terms[0] + glue + terms[1])) {
                    negCounts.put(terms[0] + glue + terms[1], negCounts.get(terms[0] + glue + terms[1]) + 1);
                    termPairToPattern.remove(terms[0] + glue + terms[1]);
                } else {
                    negCounts.put(terms[0] + glue + terms[1], 1);
                    termPairToPattern.put(terms[0] + glue + terms[1],
                            entry.getKey());
                }
            }
            negLoss.put(entry.getKey(), 0);
        }



        for (Map.Entry<String, Integer> entry : posCounts.entrySet()) {
            if (entry.getValue() == 1) {
                Pattern p = termPairToPattern.get(entry.getKey());
                posLoss.put(p, posLoss.get(p) + 1);
            }
        }

        for (Map.Entry<String, Integer> entry : negCounts.entrySet()) {
            if (entry.getValue() == 1) {
                Pattern p = termPairToPattern.get(entry.getKey());
                negLoss.put(p, negLoss.get(p) + 1);
            }
        }
    }
    private int sketchSize = 1000;

    public int getSketchSize() {
        return sketchSize;
    }

    public void setSketchSize(int sketchSize) {
        this.sketchSize = sketchSize;
    }
    
    public double getSetScore() {
        return currentFM;
    }
}
