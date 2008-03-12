/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nii.alloe.runs;

import nii.alloe.corpus.*;
import java.io.*;

/**
 *
 * @author John McCrae
 */
public class RunCorpusLoader {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            if(args.length != 4 && args.length != 5) {
                System.err.println("Usage: command corpusSource termFile indexDir contextSize [sketchSize]");
                System.exit(-1);
            }
            CorpusFile cf;
            if(args[0].matches(".*\\.zip")) {
                cf = new ZipCorpusFile(args[0]);
            } else {
                cf = new TextCorpusFile(args[0]);
            }
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(args[1]));
            TermList terms = (TermList)ois.readObject();
            ois.close();
            CorpusLoader cl = new CorpusLoader(terms, cf, new File(args[2]));
            cl.setContextSize(Integer.parseInt(args[3]));
            if(args.length == 5)
                cl.setMaxSketchSize(Integer.parseInt(args[4]));
            cl.run();
        } catch(Exception x) {
            x.printStackTrace();
            System.exit(-1);
        }
    }   
}
