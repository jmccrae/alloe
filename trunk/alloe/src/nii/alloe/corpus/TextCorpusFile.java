package nii.alloe.corpus;
import java.io.*;

/**
 *
 * @author John McCrae, National Institute of Informatics
 */
public class TextCorpusFile implements CorpusFile, Serializable {
    private transient long fileSize;
    private transient BufferedReader in;
    private int linesRead;
    private long bytesRead;
    private String fileName;
    
    /** Creates a new instance of TextCorpusFile */
    public TextCorpusFile(String fileName) throws IOException {
        this.fileName = fileName;
        fileSize = new File(fileName).length();
        in = new BufferedReader(new FileReader(fileName),256);
    }
    
    public String getNextLine() throws IOException {
        String s = readLine();
        while(s != null) {
            if(!s.matches(".*\\w.*")) {
                s = readLine();
                continue;
            }
            if(s.length() > 200) {
                String t = readLine();
                while(t != null && t.length() > 200) {
                    s = s + " " + t;
                    t = readLine();
                }
            }
            s.replaceAll("\\s+", " ");
            return s;
        }
        return null;
    }
    
    private String readLine() throws IOException {
        String s = in.readLine();
        if(s == null)
            return null;
        bytesRead += s.length() + 1;
        linesRead++;
        return s;
    }
    
    public double getProgress() {
        return (double)bytesRead / (double)fileSize;
    }
      
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        fileSize = new File(fileName).length();
        in = new BufferedReader(new FileReader(fileName),256);
        for(int i = 0; i < linesRead; i++) {
            in.readLine();
        }
    }
}
