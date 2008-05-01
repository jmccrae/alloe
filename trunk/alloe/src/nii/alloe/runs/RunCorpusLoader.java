/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nii.alloe.runs;

import nii.alloe.corpus.*;
import java.io.*;
import nii.alloe.tools.process.*;

/**
 *
 * @author John McCrae
 */
public class RunCorpusLoader extends TerminalProcess {
    public RunCorpusLoader(String []args) {
        super(args);
        name = "corpus";
    }
    public static void main(String []args) {
        new RunCorpusLoader(args);
    }
    
    protected void start(String[]args) {
        Class clasz = CorpusLoader.class;
        Class[] constructorParams = { TermList.class, CorpusFile.class, File.class };
        String[] constructorNames = { "terms", "corpus", "index" };
        try {
            registerLoader(CorpusFile.class, this.getClass().getMethod("loadCorpusFile", String.class));
        } catch(NoSuchMethodException x) {
            x.printStackTrace();
            return;
        }
        try {
            autoInit(args, clasz, constructorParams, constructorNames);
        } catch(nii.alloe.tools.getopts.GetOptsException x) {
            System.err.println(x.getMessage());
            System.exit(-1);
        }
    }
    
    public static CorpusFile loadCorpusFile(String fileName) throws IOException {
        if(fileName.matches(".*\\.zip")) {
            return new ZipCorpusFile(fileName);
        } else {
            return new TextCorpusFile(fileName);
        }
    }
}
