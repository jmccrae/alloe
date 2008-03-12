/*
 * BuildFeatureVectors.java
 *
 * Created on 08 March 2008, 18:20
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package nii.alloe.runs;

import nii.alloe.classify.*;
import nii.alloe.corpus.*;
import nii.alloe.corpus.pattern.*;
import nii.alloe.niceties.*;
import java.io.*;

/**
 *
 * @author john
 */
public class BuildFeatureVectors {
    
    private static String outputFile;
    private static FeatureVectorFormer fvf;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            if(args.length != 6 && args.length != 5) {
                System.err.println("Usage: command corpus patternSet termPairSet relationName output [-lazy]");
                return;
            }
            Corpus corpus = Corpus.openCorpus(new File(args[0]));
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(args[1]));
            PatternSet patternSet = (PatternSet)ois.readObject();
            ois.close();
            ois = new ObjectInputStream(new FileInputStream(args[2]));
            TermPairSet termPairs = (TermPairSet)ois.readObject();
            ois.close();
            fvf = new FeatureVectorFormer(args[3],patternSet,corpus,termPairs);
            if(args.length == 6) {
                fvf.setLazyMatching(true);
            }
            outputFile = args[4];
            fvf.addProgressListener(new AlloeProgressListener() {
                public void finished() {
                    try {                    
                        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(outputFile));
                        oos.writeObject(fvf.dataSet);
                        oos.close();
                    } catch(IOException x) {
                        x.printStackTrace();
                    }
                }
                double lastProgress = 0;
                public void progressChange(double newProgress) {
                    if(newProgress - lastProgress >= 0.0001) {
                        System.out.println(newProgress);
                        lastProgress = newProgress;
                    }
                }
            });
            fvf.start();
        } catch(Exception x) {
            x.printStackTrace();
        }
    }
    
}
