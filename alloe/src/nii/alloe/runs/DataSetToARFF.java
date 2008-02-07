/*
 * DataSetToARFF.java
 *
 * Created on February 1, 2008, 7:08 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package nii.alloe.runs;
import nii.alloe.classify.*;
import nii.alloe.corpus.TermPairSet;
import weka.core.*;
import java.io.*;
import java.util.*;

/**
 *
 * @author john
 */
public class DataSetToARFF {
    
    /** Creates a new instance of DataSetToARFF */
    public DataSetToARFF() {
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            /*ObjectInputStream ois = new ObjectInputStream(new FileInputStream("/home/john/wpshie/syns.atps"));
            Object o = ois.readObject();
            TermPairSet tps = (TermPairSet)o;
            System.out.println(tps.size());*/
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream("/home/john/wpshie/hyps.afv"));
            Object o = ois.readObject();
            if(!(o instanceof DataSet)) {
                System.err.println("Not a dataset");
                System.exit(-1);
            }
            ois.close();
            DataSet ds = (DataSet)o;
            Instances is = ds.instances.get("hyp");
            if(is == null) {
                System.err.println("No syn in dataset");
                System.exit(-1);
            }
            BufferedWriter bw = new BufferedWriter(new FileWriter("/home/john/wpshie/hyps.arff"));
            bw.write(is.toString());
            bw.close();
            /*Instances is = new Instances(new FileReader("/home/john/wpshie/syns2.arff"));
            ds.instances.put("syn",is);
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("/home/john/wpshie/syns2.afv"));
            oos.writeObject(ds);
            oos.close();*/
        } catch(Exception x) {
            x.printStackTrace();
            System.exit(-1);
        }
    }
    
}
