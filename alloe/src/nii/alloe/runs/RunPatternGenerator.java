/*
 * RunPatternGenerator.java
 *
 * Created on February 29, 2008, 1:59 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package nii.alloe.runs;
import nii.alloe.corpus.*;
import nii.alloe.corpus.pattern.*;
import java.io.*;
import java.util.*;
import nii.alloe.tools.process.AlloeProgressListener;

/**
 *
 * @author john
 */
public class RunPatternGenerator {
    
    public static String file;
    public static PatternBuilder pb;
    
    /** Creates a new instance of RunPatternGenerator */
    public RunPatternGenerator() {
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
         try {
            if(args.length != 5) {
                System.err.println("Invalid arguments\n corpusFile termPairFile relationship outputFile bias");
                System.exit(-1);
            }
            Corpus c = Corpus.openCorpus(new File(args[0]));
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(args[1]));
            TermPairSet tps = (TermPairSet)ois.readObject();
            ois.close();
            file = args[3];
            pb = new PatternBuilder(c,tps,PatternMetricFactory.PSEUDO_FM,args[2]);
            pb.setMaxPatterns(500);
            //pb = new PatternBuilder(c,tps,args[2],500);
            pb.setMetricAlpha(Math.pow(1.15,Double.parseDouble(args[4])));
            pb.setIgnoreReflexives(true);
            pb.setMaxIterations(500000);
            pb.addProgressListener(new AlloeProgressListener() {
                public void progressChange(double newProgress) {
                    System.out.println(newProgress);
                }
                
                public void finished() {
                    try {
                        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
                        oos.writeObject(pb.patternScores);
                        oos.close();
                    } catch(IOException x) {
                        x.printStackTrace();
                        System.exit(-1);
                    }
                }
            });
            pb.start();
        } catch(Exception x) {
            x.printStackTrace();
            System.exit(-1);
        }
    }
    
}
