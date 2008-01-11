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
    public Vector<String> terms;
    /** Filename where the terms start */
    public String fileName;
    
    private int linesRead;
    private long bytesRead;
    
    private transient long fileSize;
    private transient BufferedReader in;
    private transient int state;
    private transient Thread theThread;
    private transient LinkedList<AlloeProgressListener> listeners;
    private transient String indexFile;
    private static final int STATE_OK = 0;
    private static final int STATE_STOPPING = 1;
    
    /** Creates a new instance of CorpusLoader */
    public CorpusLoader(Vector<String> terms, String fileName, String indexFile) {
        this.terms = terms;
        this.fileName = fileName;
        this.indexFile = indexFile;
        linesRead = 0;
        listeners = new LinkedList<AlloeProgressListener>();
    }
    
    /** Register a progress listener */
    public void addProgressListener(AlloeProgressListener apl) {
        if(!listeners.contains(apl))
            listeners.add(apl);
    }
    
    /** Start process. It is expected that this function should start the progress
     * in a new thread */
    public void start() {
        corpus = new Corpus(new Vector<String>(terms),indexFile);
        try {
            corpus.openIndex(true);
            fileSize = (new File(fileName)).length();
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
            fileSize = (new File(fileName)).length();
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
         linesRead = 0;
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
                    Vector<String> ss2 = corpus.getContexts(ss[i],3);
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
                    s = s + " " + t;
                    while(t.length() > 200) {
                        t = readLine();
                        s = s + " " + t;
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
}
