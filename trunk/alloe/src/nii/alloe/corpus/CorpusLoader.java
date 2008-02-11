package nii.alloe.corpus;

import nii.alloe.niceties.*;
import java.util.*;
import java.io.*;

/**
 *
 * @author John McCrae, National Institute of Informatics
 */
public class CorpusLoader implements AlloeProcess, Runnable, Serializable {
    /** The Corpus, when it is loaded */
    public Corpus corpus;
    /** The set of terms */
    public TermList terms;
    /** Filename where the terms start */
    public File fileName;
    
    private int linesRead;
    private long bytesRead;
    
    private transient long fileSize;
    private transient BufferedReader in;
    private transient int state;
    private transient Thread theThread;
    private transient LinkedList<AlloeProgressListener> listeners;
    private transient File indexFile;
    private static final int STATE_OK = 0;
    private static final int STATE_STOPPING = 1;
    
    /** Creates a new instance of CorpusLoader */
    public CorpusLoader(TermList terms, File fileName, File indexFile) {
        this.terms = terms;
        this.fileName = fileName;
        this.indexFile = indexFile;
        linesRead = 0;
        listeners = new LinkedList<AlloeProgressListener>();
        contextSize = 3;
        maxSketchSize = -1;
    }
    
    /** Register a progress listener */
    public void addProgressListener(AlloeProgressListener apl) {
        if(!listeners.contains(apl))
            listeners.add(apl);
    }
    
    /** Start process. It is expected that this function should start the progress
     * in a new thread */
    public void start() {
        corpus = new Corpus(terms,indexFile);
        corpus.setMaxSketchSize(getMaxSketchSize());
        try {
            corpus.openIndex(true);
            fileSize = fileName.length();
            in = new BufferedReader(new FileReader(fileName),256);
            state = STATE_OK;
            theThread = new Thread(this);
            theThread.start();
        } catch(IOException x) {
            throw new RuntimeException("File Not Found or other disk error");
        }
    }
    
    /** Pause the process. The assumption is that this will work by changing a variable
     * in the running thread and then wait for this thread to finish by use of join().
     * It then returns an object (generally this), which is Serializable and can be written
     * to a file.
     *
     * @throws CannotPauseException If the process is not in a state where it can be resumed
     */
    public void pause() throws CannotPauseException {
        state = STATE_STOPPING;
        try {
            theThread.join();
        } catch(InterruptedException x) {
            throw new CannotPauseException("The running thread was interrupted");
        }
    }
    
    /** Resume the process. */
    public void resume() {
        try {
            corpus.openIndex(false);
            fileSize = fileName.length();
            in = new BufferedReader(new FileReader(fileName),256);
            for(int i = 0; i < linesRead; i++) {
                in.readLine();
            }
            state = STATE_OK;
            theThread = new Thread(this);
            theThread.start();
        } catch(IOException x) {
            throw new RuntimeException("File not found or other disk error");
        }
    }
    
    private void readObject(ObjectInputStream ois)  throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        listeners = new LinkedList<AlloeProgressListener>();
    }
    
    public void run() {
        try {
            String s = readLine();
            while(s != null && state == STATE_OK) {
                if(!s.matches(".*\\w.*")) {
                    s = readLine();
                    continue;
                }
                String[] ss = s.split("[\\.;]");
                for(int i = 0; i < ss.length; i++) {
                    Vector<String> ss2 = corpus.getContexts(ss[i],getContextSize(),(double)bytesRead/(double)fileSize);
                    Iterator<String> siter = ss2.iterator();
                    while(siter.hasNext()) {
                        String s2 = siter.next();
                        corpus.addDoc(s2);
                    }
                }
                s = readLine();
                if(s == null) break;
                if(s.length() > 200) {
                    String t = readLine();
                    s = s + " " + (t != null ? t : "");
                    while(t != null && t.length() > 200) {
                        t = readLine();
                        s = s + " " + (t != null ? t : "");
                    }
                }
                s.replaceAll("\\s\\s+", " ");
                fireNewProgressChange((double)bytesRead / (double)fileSize);
            }
            corpus.closeIndex();
        } catch(IOException x) {
            throw new RuntimeException(x.getMessage());
        }
        if(state == STATE_OK)
            fireFinished();
    }
    
    private String readLine() throws IOException {
        String s = in.readLine();
        if(s == null)
            return null;
        bytesRead += s.length() + 1;
        linesRead++;
        return s;
    }
    
    private void fireNewProgressChange(double newProgress) {
        Iterator<AlloeProgressListener> apliter = listeners.iterator();
        while(apliter.hasNext()) {
            apliter.next().progressChange(newProgress);
        }
    }
    
    private void fireFinished() {
        Iterator<AlloeProgressListener> apliter = listeners.iterator();
        while(apliter.hasNext()) {
            apliter.next().finished();
        }
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
