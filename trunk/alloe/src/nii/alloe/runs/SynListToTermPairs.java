/*
 * SynListToTermPairs.java
 *
 * Created on March 26, 2008, 1:07 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package nii.alloe.runs;
import java.io.*;
import nii.alloe.corpus.*;

/**
 *
 * @author john
 */
public class SynListToTermPairs {
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if(args.length != 2) {
            System.err.println("Usage: java nii.alloe.runs.SynListToTermPairs synList atpsDest");
            return;
        }
        TermPairSet syns = new TermPairSet();
        try {
            BufferedReader in = new BufferedReader(new FileReader(args[0]));
            String s = in.readLine();
            while(s != null) {
                if(!s.matches(".*\\w.*")) {
                    s = in.readLine();
                    continue;
                }
                String[] ss = s.split(", ");
                for(int i = 0; i < ss.length; i++) {
                    for(int j = 0; j < ss.length; j++) {
                        syns.add(ss[i],ss[j]);
                    }
                }
                s = in.readLine();
            }
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(args[1]));
            oos.writeObject(syns);
            oos.close();
        } catch(IOException x) {
            x.printStackTrace();
            System.exit(-1);
        }
       
    }
    
}
