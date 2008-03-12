package nii.alloe.corpus;

import nii.alloe.niceties.*;
import java.util.*;
import java.io.*;

/**
 *
 * @author John McCrae, National Institute of Informatics
 */
public class CorpusLoader extends AlloeProcessAdapter {
    /** The Corpus, when it is loaded */
    public Corpus corpus;
    /** The set of terms */
    public TermList terms;
    /** Corpus file */
    public CorpusFile corpusFile;  
      
    private transient int state;
    private transient Thread theThread;
    private transient LinkedList<AlloeProgressListener> listeners;
    private transient File indexFile;
    private static final int STATE_OK = 0;
    private static final int STATE_STOPPING = 1;
    
    /** Creates a new instance of CorpusLoader */
    public CorpusLoader(TermList terms, CorpusFile corpusFile, File indexFile) {
        this.terms = terms;
        this.corpusFile = corpusFile;
        this.indexFile = indexFile;
        listeners = new LinkedList<AlloeProgressListener>();
        contextSize = 3;
        maxSketchSize = -1;
    }
    
    public void pause() throws CannotPauseException {
        try {
            super.pause();
            corpus.closeIndex();
        } catch(IOException x) {
            throw new CannotPauseException(x.getMessage());
        }
    }
        
    /** Resume the process. */
    public void resume() {
        try {
            corpus.openIndex(false);
            super.resume();
        } catch(IOException x) {
            throw new RuntimeException(x.getMessage());
        }
    }
    
    public void run() {
        try {
            corpus = new Corpus(terms,indexFile);
            corpus.setMaxSketchSize(getMaxSketchSize());
            corpus.openIndex(true);
            
            String s = corpusFile.getNextLine();
            while(s != null && state == STATE_OK) {
              
                String[] ss = s.split("[\\.;]");
                for(int i = 0; i < ss.length; i++) {
                    Vector<String> ss2 = corpus.getContexts(ss[i],getContextSize(),corpusFile.getProgress());
                    Iterator<String> siter = ss2.iterator();
                    while(siter.hasNext()) {
                        String s2 = siter.next();
                        corpus.addDoc(s2);
                    }
                }
                
                s = corpusFile.getNextLine();
                fireNewProgressChange(corpusFile.getProgress());
            }
            corpus.closeIndex();
        } catch(IOException x) {
            throw new RuntimeException(x.getMessage());
        }
        if(state == STATE_OK)
            fireFinished();
    }
    
   
    
    public String getStateMessage() { return "Indexing corpus: "; }

    /**
     * Holds value of property contextSize.
     */
    private int contextSize;

    /**
     * Getter for property contextSize.
     * @return Value of property contextSize.
     */
    public int getContextSize() {
        return this.contextSize;
    }

    /**
     * Setter for property contextSize.
     * @param contextSize New value of property contextSize.
     */
    public void setContextSize(int contextSize) {
        this.contextSize = contextSize;
    }

    /**
     * Holds value of property maxSketchSize.
     */
    private int maxSketchSize;

    /**
     * Getter for property maxSketchSize.
     * @return Value of property maxSketchSize.
     */
    public int getMaxSketchSize() {
        return this.maxSketchSize;
    }

    /**
     * Setter for property maxSketchSize.
     * @param maxSketchSize New value of property maxSketchSize.
     */
    public void setMaxSketchSize(int maxSketchSize) {
        this.maxSketchSize = maxSketchSize;
        if(corpus != null)
            corpus.setMaxSketchSize(maxSketchSize);
    }
}
