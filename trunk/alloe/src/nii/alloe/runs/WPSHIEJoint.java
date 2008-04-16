/*
 * WPSHIEJoint.java
 *
 * Created on March 19, 2008, 5:58 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package nii.alloe.runs;
import weka.core.*;
import weka.classifiers.functions.*;
import java.io.*;
import java.util.*;
import nii.alloe.classify.*;
import nii.alloe.corpus.*;
import nii.alloe.theory.*;
import nii.alloe.consist.*;

/**
 *
 * @author john
 */
public class WPSHIEJoint {
    
    private static Model probModel, probModelHyp, probModelSyn;
    private static Model trueModel, trueModelHyp, trueModelSyn;
    
    private static final String path = "/home/john/wpshie/";
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            buildTrueModels();
            buildModels();
            solveModels();
        } catch(Exception x) {
            x.printStackTrace();
        }
    }
    
    private static void classifyAllInstances() throws Exception {
        /*String[] names = { "syns","hyps" };
        for(int n = 0; n < names.length; n++) {
            for(int i = 0; i < 5; i++) {
                Instances train = new Instances(new FileReader(names[n]+ "-train-" + i + ".arff"));
                train.setClassIndex(train.numInstances()-1);
                Logistic logistic = new Logistic();
                logistic.buildClassifier(train);
                PrintStream out = new PrintStream(names[n] + "-out-" + i);
                train = null;
                Instances test = new Instances(new FileReader(names[n] + "-test-" + i + ".arff"));
                for(int j = 0; j < test.numInstances(); j++) {
                    double[] dist = logistic.distributionForInstance(test.instance(j));
                    out.println(Strings.join(",",dist));
                }
            }
        }*/
    }
    
    private static final String glue = " => ";
    
    private static void buildModels() throws Exception {
        String[] names = { "syns", "hyps" };
        String[] rels = { "r2", "r1" };
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path+"terms"));
        TermList allTerms = (TermList)ois.readObject();
        ois.close();
        Logic shLogic = new Logic(new File("logics/sh.logic"));
        shLogic.ruleSymbols.setModelSize(allTerms.size());
        probModel = new Model(shLogic);
        probModel.addBasicGraphs(shLogic);
        for(int n = 0; n < 2; n++) {
            System.out.println(names[n]);
            ois = new ObjectInputStream(new FileInputStream(path+names[n] + ".afv"));
            DataSet ds = (DataSet)ois.readObject();
            ois.close();
            Vector<String> termList = ds.getTerms(names[n].substring(0,names[n].length()-1));
            int m = ds.instances.get(names[n].substring(0,names[n].length()-1)).numInstances();
            ds = null;
            
            BufferedReader dists = new BufferedReader(new FileReader(path+names[n] + ".dist"));
            
            ProbabilityGraph pg = probModel.addProbabilityGraph(rels[n]);
            pg.setBaseVal(0.00547);
            String s;
            int i = 0;
            while((s = dists.readLine()) != null) {
                if(s.equals(""))
                    continue;
                String[] ss = s.split(" ");
                String[] terms = termList.get(i).split(glue);
                if(ss.length < 3)
                    System.out.println(s);
                if(terms.length < 2)
                    System.out.println(termList.get(i));
                if(ss[1].equals("1")) {
                    System.out.println(termList.get(i));
                    pg.setPosVal(allTerms.indexOf(terms[0]),allTerms.indexOf(terms[1]),Double.parseDouble(ss[2]));
                } else {
                    pg.setPosVal(allTerms.indexOf(terms[0]),allTerms.indexOf(terms[1]),1 - Double.parseDouble(ss[2]));
                }
                i++;
            }
        }
        Logic hypLogic = new Logic(new File("logics/hyp.logic"));
        hypLogic.ruleSymbols.setModelSize(allTerms.size());
        TreeMap<String,Graph> graphs = new TreeMap<String,Graph>();
        graphs.put("e",probModel.getGraphByName("e"));
        graphs.put("r1",probModel.getGraphByName("r1"));
        probModelHyp = new Model(graphs,hypLogic);
        Logic synLogic = new Logic(new File("logics/syn.logic"));
        synLogic.ruleSymbols.setModelSize(allTerms.size());
        graphs = new TreeMap<String,Graph>();
        graphs.put("e",probModel.getGraphByName("e"));
        graphs.put("r1",probModel.getGraphByName("r2"));
        probModelSyn = new Model(graphs,synLogic);
    }
    
    private static void buildTrueModels() throws Exception  {
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path+"terms"));
        TermList terms = (TermList)ois.readObject();
        ois = new ObjectInputStream(new FileInputStream(path+"syns.atps"));
        TermPairSet synPairs = (TermPairSet)ois.readObject();
        Logic shLogic = new Logic(new File("logics/sh.logic"));
        shLogic.ruleSymbols.setModelSize(terms.size());
        trueModel = new Model(shLogic);
        trueModel.addBasicGraphs(shLogic);
        trueModel.addSpecificGraph("r2");
        trueModel.setGraphAs("r2",synPairs,terms);
        ois = new ObjectInputStream(new FileInputStream(path+"hyps.atps"));
        TermPairSet hypPairs = (TermPairSet)ois.readObject();
        trueModel.addSpecificGraph("r1");
        trueModel.setGraphAs("r1",hypPairs,terms);
        TreeMap<String,Graph> graphs = new TreeMap<String,Graph>();
        graphs.put("e",trueModel.getGraphByName("e"));
        graphs.put("r1",trueModel.getGraphByName("r1"));
        Logic hypLogic = new Logic(new File("logics/hyp.logic"));
        hypLogic.ruleSymbols.setModelSize(terms.size());
        trueModelHyp = new Model(graphs,hypLogic);
        graphs = new TreeMap<String,Graph>();
        graphs.put("e",trueModel.getGraphByName("e"));
        graphs.put("r1",trueModel.getGraphByName("r2"));
        Logic synLogic = new Logic(new File("logics/syn.logic"));
        synLogic.ruleSymbols.setModelSize(terms.size());
        trueModelSyn = new Model(graphs,synLogic);
    }
    
    private static void compareModels(Model actual, Model soln) {
        int[] tempRes = soln.computeComparison(actual);
        System.out.print((double)tempRes[0] / (double)(tempRes[0] + tempRes[1]));
        System.out.print(",");
        System.out.print((double)tempRes[0] / (double)(tempRes[0] + tempRes[2]));
        System.out.print(",");
        System.out.println(2.0 * (double)tempRes[0] / (double)(2 * tempRes[0] + tempRes[1] + tempRes[2]));
    }
    
    private static void solveModels() throws Exception {
        compareModels(trueModelHyp,probModelHyp);
        compareModels(trueModelSyn,probModelSyn);
        compareModels(trueModel,probModel);
        GrowingSolver gs = new GrowingSolver(new Logic(new File("logics/hypernym.logic")),probModelHyp);
        gs.solve();
        compareModels(trueModelHyp,gs.soln);
        gs = new GrowingSolver(new Logic(new File("logics/synonym.logic")),probModelSyn);
        gs.solve();
        compareModels(trueModelSyn,gs.soln);
        gs = new GrowingSolver(new Logic(new File("logics/sh.logic")),probModel);
        gs.solve();
        TreeMap<String,Graph> graphs = new TreeMap<String,Graph>();
        graphs.put("e",gs.soln.getGraphByName("e"));
        graphs.put("r1",gs.soln.getGraphByName("r1"));
        compareModels(trueModelHyp, new Model(graphs,trueModelHyp.logic));
        graphs.put("r1",gs.soln.getGraphByName("r2"));
        compareModels(trueModelSyn, new Model(graphs,trueModelSyn.logic));
        compareModels(trueModel,gs.soln);
    }
}
