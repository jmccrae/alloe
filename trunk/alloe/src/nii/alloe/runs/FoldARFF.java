/*
 * FoldARFF.java
 *
 * Created on March 19, 2008, 4:17 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package nii.alloe.runs;
import weka.core.*;
import java.io.*;
import java.util.*;
import nii.alloe.classify.*;

/**
 *
 * @author john
 */
public class FoldARFF {
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if(args.length != 2) {
            System.err.print("Usage java nii.alloe.runs.FoldARFF arffOrAfvFile foldCount");
            System.exit(-1);
        }
        try {
            Instances is;
            String fileName;
            if(args[0].matches(".*\\.arff")) {
                fileName = args[0].substring(0,args[0].length() -5);
                is = new Instances(new FileReader(args[0]));
            } else if(args[0].matches(".*\\.afv")) {
                fileName = args[0].substring(0,args[0].length() - 4);
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(args[0]));
                DataSet ds = (DataSet) ois.readObject();
                is = ds.instances.get(ds.instances.keySet().iterator().next());
            } else {
                System.err.println("First parameter not arff or afv");
                return;
            }
            int n = Integer.parseInt(args[1]);
            int[] counts = new int[n];
            double d = (double)is.numInstances() / (double)n;
            for(double i = 0; i < n; i++) {
                counts[(int)i] = (int)(i * d);
            }
            PrintStream[] testout = new PrintStream[n];
            PrintStream[] trainout = new PrintStream[n];
            PrintStream members = new PrintStream(fileName + "-members");
            for(int i = 0; i < n; i++) {
               testout[i] = new PrintStream(fileName + "-test-" + i + ".arff");
               trainout[i] = new PrintStream(fileName + "-train-" + i + ".arff");
               testout[i].println("@relation '" + fileName + "-test-" + i + "'");
               for(int j = 0; j < is.numAttributes(); j++) {
                   testout[i].println(is.attribute(j).toString());
               }
               testout[i].println("@data");
               trainout[i].println("@relation '" + fileName + "-train-" + i + "'");
               for(int j = 0; j < is.numAttributes(); j++) {
                   trainout[i].println(is.attribute(j).toString());
               }
               trainout[i].println("@data");
            }
            for(int i = 0; i < is.numInstances(); i++) {
                int set = select(counts,is.numInstances()-i);
                for(int j = 0; j < n; j++) {
                    if(j == set) {
                        testout[j].println(is.instance(i).toString());
                    } else {
                        trainout[j].println(is.instance(i).toString());
                    }
                }
                members.println(i + "," + set);
            }
            members.close();
            for(int i = 0; i < n; i++) {
                testout[i].close();
                trainout[i].close();
            }
        } catch(Exception x) {
            x.printStackTrace();
        }
    }
    
    private static Random r = new Random();
    
    private static int select(int[] counts, int n) {
        assert(n >= counts[counts.length -1]);
        
        int selected = r.nextInt(n);
        int i=0;
        while(!(i + 1 == counts.length && counts[i] <= selected) && 
                !(counts[i] <= selected && counts[i+1] > selected)) { i++; }
        for(int j = i+1; j < counts.length; j++) {
            counts[j]--;
        }
        return i;
    }
    
}
