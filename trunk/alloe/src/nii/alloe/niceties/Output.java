package nii.alloe.niceties;

import java.io.PrintStream;

/**
 *
 * @author John McCrae, National Institute of Informatics
 */
public class Output {
    private PrintStream s;
    
    public static Output out;
    public static Output err;

    static {
        out = new Output(System.out);
        err = new Output(System.err);
    }
    
    private Output(PrintStream s) {
        this.s = s;
    }
    
    public void print(String str) {
        s.print(str);
    }
    
    public void println(String str) {
        s.println(str);
    }
    
}
