/*
 * PairsInCorpus.java
 *
 * Created on 26 February 2008, 13:45
 *
 * To change this template, choose Tools | Template Manager
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
public class PairsInCorpus {
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream("/home/john/scratch/johnm/corpus"));
            Corpus corpus = (Corpus)ois.readObject();
            ois.close();
            ois = new ObjectInputStream(new FileInputStream("/home/john/wpshie/hyps.atps"));
            TermPairSet termPairs = (TermPairSet)ois.readObject();
            ois.close();
            
            corpus.initTermsInCorpusCache();
            
            int trueTerms=0, allTerms=0;
            Iterator<Corpus.TermPair> termIter = corpus.getTermsInCorpus();
            Corpus.TermPair tp;
            while (termIter.hasNext()) {
                tp = termIter.next();
                System.out.print(tp.term1 + " => " + tp.term2);
                if(termPairs.contains(tp.term1.toLowerCase(),tp.term2.toLowerCase()) ||
                        termPairs.contains(tp.term2.toLowerCase(),tp.term1.toLowerCase())) {
                    trueTerms++;
                    System.out.println(" *");
                }
                System.out.println("");
                allTerms++;
            }
            System.out.println(trueTerms + "/" + allTerms);
        } catch(Exception x) {
            x.printStackTrace();
            System.exit(-1);
        }
    }
    
}
