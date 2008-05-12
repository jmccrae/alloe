/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nii.alloe.runs;

import java.util.*;
import java.io.*;
import nii.alloe.classify.*;
import nii.alloe.corpus.*;
import nii.alloe.tools.strings.*;
import weka.core.*;

/**
 *
 * @author john
 */
public class AttachZeroesToDataSet {

    public static void main(String[] args) {
        if (args.length != 4) {
            System.err.println("Usage AttachZeroesToDataSet dataSet termPairSet rel noOfZeroes");
            System.exit(-1);
        }
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(args[0]));
            DataSet dataSet = (DataSet) ois.readObject();
            ois.close();
            ois = new ObjectInputStream(new FileInputStream(args[1]));
            TermPairSet tps = (TermPairSet) ois.readObject();
            ois.close();
            String rel = args[2];
            Vector<String> terms = dataSet.getTerms(rel);
            if(terms == null) {
                System.err.println("Invalid relation name, valid names are: " + Strings.join(",", dataSet.instances.keySet()));
                System.exit(-1);
            }
            for (int i = 0; i < Integer.parseInt(args[3]); i++) {
                Random r = new Random();
                int r1, r2;
                String term1, term2;
                do {
                    r1 = r.nextInt(dataSet.termSet.size());
                    r2 = r.nextInt(dataSet.termSet.size());
                    term1 = dataSet.termSet.get(r1);
                    term2 = dataSet.termSet.get(r2);
                } while (terms.contains(term1 + dataSet.glue + term2) || tps.contains(term1, term2));
                dataSet.addInstance(new Instance(1.0, new double[dataSet.instances.get(rel).numAttributes()]), rel, term1, term2);
            }
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(args[0]));
            oos.writeObject(dataSet);
            oos.close();
        } catch (Exception x) {
            x.printStackTrace();
            System.exit(-1);
        }
    }
}
