package nii.alloe.corpus;
import java.io.*;
import java.util.zip.*;
import java.util.*;

/**
 *
 * @author John McCrae, National Institute of Informatics
 */
public class ZipCorpusFile implements CorpusFile, Serializable {
    private transient Enumeration zipEntries;
    private transient ZipFile zipFile;
    private transient BufferedReader in;
    private long bytesRead;
    private transient long totalBytes;
    private String zipEntryName;
    private int linesRead;
    private String fileName;
    
    /** Creates a new instance of ZipCorpusFile */
    public ZipCorpusFile(String fileName) throws IOException {
        this.fileName = fileName;
        totalBytes = new File(fileName).length();
        zipFile = new ZipFile(fileName);
        zipEntries = zipFile.entries();
    }
    
    public String getNextLine() throws java.io.IOException {
        // Have we initialized?
        if(in == null) {
            nextFile();
            // If still no input stream we must have finished
            if(in == null)
                return null;
        }
        String s = in.readLine();
        linesRead++;
        // Get a line with at least one word character
        while(s == null || !s.matches(".*\\w.*")) {
            if(s == null) {
                nextFile();
                if(in == null)
                    return null;
            }
            s = in.readLine();
            linesRead++;
        }
        // If the lines is over 230 characters we assume it has some continutation
        if(s.length() > 230) {
            String t;
            do {
                t = in.readLine();
                s = s + " " + (t != null ? t : "");
                linesRead++;
            } while(t != null && t.length() > 230);
        }
        s.replaceAll("\\s+", " ");
        return s;
    }
    
    private void nextFile() throws IOException {
        if(!zipEntries.hasMoreElements()) {
            in = null;
            return;
        }
        ZipEntry entry = (ZipEntry)zipEntries.nextElement();
        bytesRead += entry.getCompressedSize();
        in = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
        zipEntryName = entry.getName();
        linesRead = 0;
    }
    
    public double getProgress() {
        return (double)bytesRead / (double)totalBytes;
    }
    
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        
        totalBytes = new File(fileName).length();
        zipFile = new ZipFile(fileName);
        zipEntries = zipFile.entries();
        ZipEntry entry = (ZipEntry)zipEntries.nextElement();
        while(!entry.getName().equals(zipEntryName)) {
            entry = (ZipEntry)zipEntries.nextElement();
        }
        in = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
        for(int i = 0; i < linesRead; i++) {
            in.readLine();
        }
    }
}
