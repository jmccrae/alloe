/* 
 * Copyright (c) 2008, National Institute of Informatics
 *
 * This file is part of SRL, and is free
 * software, licenced under the GNU Library General Public License,
 * Version 2, June 1991.
 *
 * A copy of this licence is included in the distribution in the file
 * licence.html, and is also available at http://www.fsf.org/licensing/licenses/info/GPLv2.html.
 */
package nii.alloe.corpus.pattern;

import edu.smu.tspell.wordnet.NounSynset;
import edu.smu.tspell.wordnet.Synset;
import edu.smu.tspell.wordnet.WordNetDatabase;
import java.util.Iterator;
import nii.alloe.corpus.Corpus;

/**
 * @author John McCrae, National Institute of Informatics
 */
public class WordNetMetric implements PatternMetric {

    private Corpus corpus;
    private double alpha;
    private WordNetDatabase wndb;

    public WordNetMetric(Corpus corpus) {
        this.corpus = corpus;
        System.setProperty("wordnet.database.dir", "/home/john/Desktop/WordNet-3.0/dict/");
        wndb = WordNetDatabase.getFileInstance();
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    public double scorePattern(Pattern pattern) {
        return returnBoth(pattern)[0];
    }

    public double[] returnBoth(Pattern pattern) {
        Iterator<Corpus.Hit> hits = corpus.getContextsForPattern(pattern);
        int positives = 0;
        int hitCount = 0;

        LOOP:
        while (hits.hasNext()) {
            Corpus.Hit hit = hits.next();
            String[] ss = pattern.getTermMatch(hit.getText());
            if(ss == null || ss.length != 2) {
                continue;
            }
            hitCount++;
            if (pattern.matches(hit.getText(), ss[0], ss[1]) &&
                    isHyp(ss[0], ss[1])) {
                //System.out.println(hit.getText() + " --- " + s + " >>> " + s2);
                positives++;
                continue LOOP;
            }
        }
        /*
        if(hitCount > 0)
        return (double)positives / (double)hitCount;
        else
        return 0;
         * */

        double[] rval = new double[2];
        rval[0] = positives;
        rval[1] = hitCount;
        return rval;
    }

    private boolean isHyp(String term1, String term2) {
        Synset[] s1sets = wndb.getSynsets(term1);
        Synset[] s2sets = wndb.getSynsets(term2);

        int rv = 0;
        for (int i = 0; i < s1sets.length; i++) {
            for (int j = 0; j < s2sets.length; j++) {
                rv = Math.max(rv, isHyp(s1sets[i], s2sets[j], 0));
            }
        }

        return rv > 0;
    }

    private static int isHyp(Synset s1, Synset s2, int depth) {
        if (s1.equals(s2)) {
            return depth;
        }
        if (!(s2 instanceof NounSynset)) {
            return 0;
        }
        Synset[] ss = ((NounSynset) s2).getHypernyms();
        int rv = 0;
        for (int i = 0; i < ss.length; i++) {
            rv = Math.max(rv, isHyp(s1, ss[i], depth + 1));
        }
        return rv;
    }

    public String getName() {
        return PatternMetricFactory.WORDNET;
    }
}
