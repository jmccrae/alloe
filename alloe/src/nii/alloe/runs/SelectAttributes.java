/*
 * SelectAttributes.java
 *
 * Created on March 19, 2008, 7:33 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package nii.alloe.runs;
import weka.core.*;
import weka.attributeSelection.*;
import java.io.*;

/**
 *
 * @author john
 */
public class SelectAttributes {
     /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
       	try {
	    if(args.length < 3) {
		System.out.println("Usage: java SelectAttributes train-set.arff test-set.arff number");
		System.exit(0);
	    }
	    Instances train_insts = new Instances(new FileReader(args[0]));
	    System.out.println("Loaded Train Set: " + train_insts.numAttributes() + " attributes & " + train_insts.numInstances() + " instances");

	    AttributeSelection as = new AttributeSelection();
	    InfoGainAttributeEval igae = new InfoGainAttributeEval();
	    Ranker rkr = new Ranker();
	    
	    rkr.setNumToSelect(Integer.parseInt(args[2]));
	    as.setSearch(rkr);
	    as.setEvaluator(igae);
	    
	    System.out.println("Selecting Attributes...");
	    as.SelectAttributes(train_insts);
	    
	    System.out.println("Reducing Dimensionality");
	    Instances reduced_train = as.reduceDimensionality(train_insts);
	    PrintStream ps = new PrintStream(args[0].substring(0,args[0].length()-5) + "-sample.arff");
	    ps.println("@relation '" + reduced_train.relationName() + "'");
            for(int i = 0; i < reduced_train.numAttributes(); i++) {
                ps.println(reduced_train.attribute(i).toString());
            }
            ps.println("@data");
            for(int i = 0; i < reduced_train.numInstances(); i++) {
                ps.println(reduced_train.instance(i).toString());
            }
            ps.close();
	    
	    Instances reduced_test = as.reduceDimensionality(new Instances(new FileReader(args[1])));
	    ps = new PrintStream(args[1].substring(0,args[1].length()-5) + "-sample.arff");
	    ps.println("@relation '" + reduced_test.relationName() + "'");
            for(int i = 0; i < reduced_test.numAttributes(); i++) {
                ps.println(reduced_test.attribute(i).toString());
            }
            ps.println("@data");
            for(int i = 0; i < reduced_test.numInstances(); i++) {
                ps.println(reduced_test.instance(i).toString());
            }
            ps.close();
	    
	} catch(Exception x) {
	    x.printStackTrace();
	}
    }
    
}
