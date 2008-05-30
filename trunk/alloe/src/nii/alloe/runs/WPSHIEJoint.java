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

    private static Model probModel,  probModelHyp,  probModelSyn;
    private static Model trueModel,  trueModelHyp,  trueModelSyn;
    private static final String path = "/home/john/wpshie/";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            buildTrueModels();
            buildModels();
            solveModels();
        } catch (Exception x) {
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
        String[] names = {"mesh-syns", "mesh-hyps"};
        String[] rels = {"r2", "r1"};
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path + "mesh-terms"));
        TermList allTerms = (TermList) ois.readObject();
        ois.close();
        shLogic.ruleSymbols.setModelSize(allTerms.size());
        probModel = new Model(shLogic);
        probModel.addBasicGraphs(shLogic);
        for (int n = 0; n < 2; n++) {
            System.out.println(names[n]);
            ois = new ObjectInputStream(new FileInputStream(path + names[n] + ".afv"));
            DataSet ds = (DataSet) ois.readObject();
            ois.close();
            Vector<String> termList = ds.getTerms(names[n].substring(5, names[n].length() - 1));
            int m = ds.instances.get(names[n].substring(5, names[n].length() - 1)).numInstances();
            ds = null;

            BufferedReader dists = new BufferedReader(new FileReader(path + names[n] + ".dist"));

            ProbabilityGraph pg = probModel.addProbabilityGraph(rels[n]);
            pg.setBaseVal(0.05);
            String s;
            int i = 0;
            while ((s = dists.readLine()) != null) {
                if (s.equals("")) {
                    continue;
                }
                String[] ss = s.split(" ");
                String[] terms = termList.get(i).split(glue);
                if (ss.length < 3) {
                    System.out.println(s);
                }
                if (terms.length < 2) {
                    System.out.println(termList.get(i));
                }
                if (ss[1].equals("1")) {
                    //System.out.println(termList.get(i));
                    pg.setVal(allTerms.indexOf(terms[0]), allTerms.indexOf(terms[1]), Double.parseDouble(ss[2]));
                } else {
                    pg.setVal(allTerms.indexOf(terms[0]), allTerms.indexOf(terms[1]), 1 - Double.parseDouble(ss[2]));
                }
                i++;
            }
        }
        hypLogic.ruleSymbols.setModelSize(allTerms.size());
        TreeMap<String, Graph> graphs = new TreeMap<String, Graph>();
        graphs.put("e", probModel.getGraphByName("e"));
        graphs.put("r1", probModel.getGraphByName("r1"));
        probModelHyp = new Model(graphs, hypLogic);
        synLogic.ruleSymbols.setModelSize(allTerms.size());
        graphs = new TreeMap<String, Graph>();
        graphs.put("e", probModel.getGraphByName("e"));
        graphs.put("r1", probModel.getGraphByName("r2"));
        probModelSyn = new Model(graphs, synLogic);
    }

    private static void buildTrueModels() throws Exception {
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path + "mesh-terms"));
        TermList terms = (TermList) ois.readObject();
        ois = new ObjectInputStream(new FileInputStream(path + "mesh-syns.atps"));
        TermPairSet synPairs = (TermPairSet) ois.readObject();
        shLogic.ruleSymbols.setModelSize(terms.size());
        trueModel = new Model(shLogic);
        trueModel.addBasicGraphs(shLogic);
        trueModel.addSpecificGraph("r2");
        trueModel.setGraphAs("r2", synPairs, terms);
        ois = new ObjectInputStream(new FileInputStream(path + "mesh-hyps.atps"));
        TermPairSet hypPairs = (TermPairSet) ois.readObject();
        trueModel.addSpecificGraph("r1");
        trueModel.setGraphAs("r1", hypPairs, terms);
        TreeMap<String, Graph> graphs = new TreeMap<String, Graph>();
        graphs.put("e", trueModel.getGraphByName("e"));
        graphs.put("r1", trueModel.getGraphByName("r1"));
        hypLogic.ruleSymbols.setModelSize(terms.size());
        trueModelHyp = new Model(graphs, hypLogic);
        graphs = new TreeMap<String, Graph>();
        graphs.put("e", trueModel.getGraphByName("e"));
        graphs.put("r1", trueModel.getGraphByName("r2"));
        synLogic.ruleSymbols.setModelSize(terms.size());
        trueModelSyn = new Model(graphs, synLogic);
    }

    private static void compareModels(Model actual, Model soln) {
        int[] tempRes = soln.computeComparison(actual);
        System.out.print((double) tempRes[0] / (double) (tempRes[0] + tempRes[1]));
        System.out.print(",");
        System.out.print((double) tempRes[0] / (double) (tempRes[0] + tempRes[2]));
        System.out.print(",");
        System.out.println(2.0 * (double) tempRes[0] / (double) (2 * tempRes[0] + tempRes[1] + tempRes[2]));
    }
    static Logic hypLogic,  synLogic, shLogic;

    static {
        try {
            hypLogic = new Logic(new File("logics/hypernym.logic"));
            synLogic = new Logic(new File("logics/synonym.logic"));
            shLogic = new Logic(new File("logics/sh.logic"));
        } catch (IOException x) {
            x.printStackTrace();
        }
    }

    private static void solveModels() throws Exception {
        System.out.print("Hyp pre: ");
        compareModels(trueModelHyp, probModelHyp);
        System.out.print("Syn pre: ");
        compareModels(trueModelSyn, probModelSyn);
        System.out.print("SH  pre: ");
        compareModels(trueModel, probModel);
        GrowingSolver gs = new GrowingSolver(hypLogic, probModelHyp);
        gs.solve();
        System.out.print("Hyp hyp: ");
        compareModels(trueModelHyp, gs.soln);
        Collection<Model> models = probModelSyn.splitByComponents();
        LinkedList<Model> solns = new LinkedList<Model>();
        for (Model m : models) {
            gs = new GrowingSolver(synLogic, m);
            gs.solve();
            solns.add(gs.soln);
        }
        System.out.print("Syn syn: ");
        compareModels(trueModelSyn, Model.joinModels(solns,synLogic));
        models = probModel.splitByComponents();
        solns = new LinkedList<Model>();
        for(Model m : models) {
            gs = new GrowingSolver(shLogic,m);
            gs.solve();
            solns.add(gs.soln);
        }
        Model shSoln = Model.joinModels(solns,shLogic);
        TreeMap<String, Graph> graphs = new TreeMap<String, Graph>();
        graphs.put("e", shSoln.getGraphByName("e"));
        graphs.put("r1", shSoln.getGraphByName("r1"));
        System.out.print("Hyp sh : ");
        compareModels(trueModelHyp, new Model(graphs, trueModelHyp.logic));
        graphs.put("r1", shSoln.getGraphByName("r2"));
        System.out.print("Syn sh : ");
        compareModels(trueModelSyn, new Model(graphs, trueModelSyn.logic));
        System.out.print("SH  sh : ");
        compareModels(trueModel, shSoln);
    }
}
