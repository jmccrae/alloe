/*
 * SketchCorpus.java
 *
 * Created on 04 March 2008, 18:45
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
public class SketchCorpus {
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            if(args.length != 4) {
                System.err.println("Usage java nii.alloe.runs.SketchCorpus corpus sketchSize newCorpusDirectory newCorpus");
                return;
            }
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(args[0]));
            Corpus corpus = (Corpus)ois.readObject();
            ois.close();
            Corpus corpus2 = corpus.sketchCorpus(Integer.parseInt(args[1]),args[2]);
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(args[3]));
            oos.writeObject(corpus2);
            oos.close();
        } catch(Exception x) {
            x.printStackTrace();
        }
    }
    
}
