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
        String s;
        while(in == null || (s = in.readLine()) == null) {
            if(!zipEntries.hasMoreElements())
                return null;
            ZipEntry entry = (ZipEntry)zipEntries.nextElement();
            bytesRead += entry.getCompressedSize();
            in = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
            zipEntryName = entry.getName();
            linesRead = 1;
        }
        while(s != null) {
            if(!s.matches(".*\\w.*")) {
                s = in.readLine();
                linesRead++;
                continue;
            }
            if(s.length() > 200) {
                String t = in.readLine();
                linesRead++;
                while(t != null && t.length() > 200) {
                    s = s + " " + t;
                    t = in.readLine();
                    linesRead++;
                }
            }
            s.replaceAll("\\s+", " ");
            return s;
        }
        return s;
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
