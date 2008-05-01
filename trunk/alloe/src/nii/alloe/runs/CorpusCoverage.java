/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nii.alloe.runs;

import nii.alloe.corpus.*;
import java.io.*;
import java.util.*;

/**
 *
 * @author john
 */
public class CorpusCoverage {

    public static void main(String[] args) {
        try {
            if (args.length != 2) {
                System.err.println("Usage: CorpusCoverage corpus termPairs");
                System.exit(-1);
            }
            Corpus corpus = Corpus.openCorpus(new File(args[0]));
            corpus.initTermsInCorpusCache();
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(args[1]));
            TermPairSet termPairs = (TermPairSet) ois.readObject();
            ois.close();

            int trueTerms = 0, allTerms = 0;
            Iterator<Corpus.TermPair> termIter = corpus.getTermsInCorpus();
            Corpus.TermPair tp;
            while (termIter.hasNext()) {
                tp = termIter.next();
                System.out.print(termPairs.contains(tp.term1.toLowerCase(),tp.term2.toLowerCase())?1:0);
                System.out.print(",");
                System.out.print(tp.term1.toLowerCase().equals(tp.term2.toLowerCase())?1:0);
                System.out.print(",");
                System.out.println(corpus.getHitsFromIterator(corpus.getContextsForTerms(
                        tp.term1.toLowerCase(),tp.term2.toLowerCase())));
            }
        } catch (Exception x) {
            x.printStackTrace();
        }
    }
}
