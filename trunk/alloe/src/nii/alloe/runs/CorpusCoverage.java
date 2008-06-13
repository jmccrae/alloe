/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nii.alloe.runs;

import nii.alloe.corpus.*;
import nii.alloe.classify.*;
import java.io.*;
import java.util.*;
import weka.core.*;

/**
 *
 * @author john
 */
public class CorpusCoverage {

    public static void main(String[] args) {
        try {
            if (args.length != 2 && args.length != 4) {
                System.err.println("Usage: CorpusCoverage corpus termPairs [name afvFile]");
                System.exit(-1);
            }
            Corpus corpus = Corpus.openCorpus(new File(args[0]));
            corpus.initTermsInCorpusCache();
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(args[1]));
            TermPairSet termPairs = (TermPairSet) ois.readObject();
            ois.close();
            DataSet ds = null;
            Vector<String> terms = null;
            if (args.length == 4) {
                ois = new ObjectInputStream(new FileInputStream(args[3]));
                ds = (DataSet) ois.readObject();
                ois.close();
                terms = ds.getTerms(args[2]);
            }

            int trueTerms = 0, allTerms = 0;
            Iterator<Corpus.TermPair> termIter = corpus.getTermsInCorpus();
            Corpus.TermPair tp;
            while (termIter.hasNext()) {
                tp = termIter.next();
                System.out.print(tp.term1.toLowerCase() + "," + tp.term2.toLowerCase() + ",");
                System.out.print(termPairs.contains(tp.term1.toLowerCase(), tp.term2.toLowerCase()) ? 1 : 0);
                System.out.print(",");
                System.out.print(tp.term1.toLowerCase().equals(tp.term2.toLowerCase()) ? 1 : 0);
                System.out.print(",");
                System.out.print(corpus.getHitsFromIterator(corpus.getContextsForTerms(
                        tp.term1.toLowerCase(), tp.term2.toLowerCase())));
                if (ds != null) {
                    Instances is = ds.instances.get(args[2]);
                    int idx = terms.indexOf(tp.term1.toLowerCase() + DataSet.glue + tp.term2.toLowerCase());
                    if (idx != -1) {
                        Instance i = is.instance(idx);
                        double v = 0.0;
                        for(int j = 0; j < i.numAttributes(); j++) {
                            v += i.value(j);
                        }
                        System.out.println("," + v);
                    } else {
                        System.out.println(",0");
                    }

                } else {
                    System.out.println("");
                }
            }
        } catch (Exception x) {
            x.printStackTrace();
        }
    }
}
