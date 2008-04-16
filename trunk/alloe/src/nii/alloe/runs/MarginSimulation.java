/*
 * MarginSimulation.java
 *
 * Created on 05 March 2008, 04:52
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package nii.alloe.runs;
import java.io.*;
import java.util.*;
import nii.alloe.consist.*;
import nii.alloe.theory.*;
import nii.alloe.simulate.*;
import nii.alloe.tools.strings.Strings;

/**
 *
 * @author john
 */
public class MarginSimulation {
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter("margin-results"));
            bw.write("connect,syninprec,syninrecall,syninfm,hypinprec,hypinrecall,hypinfm,synonlyprec,synonlyrecal,synonlyfm,hyponlyprec,hyponlyrecall,hyponlyfm,synshprec,synshrecall,synshfm,hypshprec,hypshrecall,hypsshfm\n");
            for(double d = -2 ; d < -.2; d += 0.01) {
                double[] res = jointImprovement(.8,0.8,100,d,1);
                bw.write(d + "," + Strings.join(",",res) + "\n");
                bw.flush();
            }
        } catch(Exception x) {
            x.printStackTrace();
        }
    }
    public static double[] jointImprovement(double prec, double recall, int n, double linkDensity, double sparsity) {
        try {
            Simulate s = new Simulate("logics/sh.logic",prec,recall,n);
            Iterator<String> relIter = s.relationDensity.keySet().iterator();
            while(relIter.hasNext()) {
                s.relationDensity.put(relIter.next(),linkDensity);
            }
            s.sparsePercent = sparsity;
            s.createModels();
            
            double []rval = new double[18];
            int []tempRes = new int[3];
            
            Logic shLogic = new Logic(new File("logics/sh.logic"));
            shLogic.setModelSize(n);
            Logic hypLogic = new Logic(new File("logics/hypernym.logic"));
            hypLogic.setModelSize(n);
            Logic synLogic = new Logic(new File("logics/synonym.logic"));
            synLogic.setModelSize(n);
            
            TreeMap<String,Graph> graphs = new TreeMap<String,Graph>();
            graphs.put("e",s.probModel.getGraphByName("e"));
            graphs.put("r1",s.probModel.getGraphByName("r2"));
            Model synOnlyProbModel = new Model(graphs,synLogic);
            graphs = new TreeMap<String,Graph>();
            graphs.put("e",s.trueModel.getGraphByName("e"));
            graphs.put("r1",s.trueModel.getGraphByName("r2"));
            Model synOnlyTrueModel = new Model(graphs,synLogic);
            tempRes = synOnlyProbModel.computeComparison(synOnlyTrueModel);
            rval[0] = (double)tempRes[0] / (double)(tempRes[0] + tempRes[1]);
            rval[1] = (double)tempRes[0] / (double)(tempRes[0] + tempRes[2]);
            rval[2] = 2.0 * (double)tempRes[0] / (double)(2 * tempRes[0] + tempRes[1] + tempRes[2]);
            graphs = new TreeMap<String,Graph>();
            graphs.put("e",s.probModel.getGraphByName("e"));
            graphs.put("r1",s.probModel.getGraphByName("r1"));
            Model hypOnlyProbModel = new Model(graphs,hypLogic);
            graphs = new TreeMap<String,Graph>();
            graphs.put("e",s.trueModel.getGraphByName("e"));
            graphs.put("r1",s.trueModel.getGraphByName("r1"));
            Model hypOnlyTrueModel = new Model(graphs,hypLogic);
            tempRes = hypOnlyProbModel.computeComparison(hypOnlyTrueModel);
            rval[3] = (double)tempRes[0] / (double)(tempRes[0] + tempRes[1]);
            rval[4] = (double)tempRes[0] / (double)(tempRes[0] + tempRes[2]);
            rval[5] = 2.0 * (double)tempRes[0] / (double)(2 * tempRes[0] + tempRes[1] + tempRes[2]);
            
            GrowingSolver gs = new GrowingSolver(new Logic(new File("logics/sh.logic")),s.probModel);
            gs.solve();
            Model shSolvedModel = gs.soln;
            
            graphs = new TreeMap<String,Graph>();
            graphs.put("e",shSolvedModel.getGraphByName("e"));
            graphs.put("r1",shSolvedModel.getGraphByName("r2"));
            Model shSynSolved = new Model(graphs,shLogic);
            tempRes = shSynSolved.computeComparison(synOnlyTrueModel);
            rval[6] = (double)tempRes[0] / (double)(tempRes[0] + tempRes[1]);
            rval[7] = (double)tempRes[0] / (double)(tempRes[0] + tempRes[2]);
            rval[8] = 2.0 * (double)tempRes[0] / (double)(2 * tempRes[0] + tempRes[1] + tempRes[2]);
            
            graphs = new TreeMap<String,Graph>();
            graphs.put("e",shSolvedModel.getGraphByName("e"));
            graphs.put("r1",shSolvedModel.getGraphByName("r1"));
            Model shHypSolved = new Model(graphs,shLogic);
            tempRes = shHypSolved.computeComparison(hypOnlyTrueModel);
            rval[9] = (double)tempRes[0] / (double)(tempRes[0] + tempRes[1]);
            rval[10] = (double)tempRes[0] / (double)(tempRes[0] + tempRes[2]);
            rval[11] = 2.0 * (double)tempRes[0] / (double)(2 * tempRes[0] + tempRes[1] + tempRes[2]);
            
            gs = new GrowingSolver(new Logic(new File("logics/synonym.logic")), synOnlyProbModel);
            gs.solve();
            Model synSolvedModel = gs.soln;
            tempRes = synSolvedModel.computeComparison(synOnlyTrueModel);
            rval[12] = (double)tempRes[0] / (double)(tempRes[0] + tempRes[1]);
            rval[13] = (double)tempRes[0] / (double)(tempRes[0] + tempRes[2]);
            rval[14] = 2.0 * (double)tempRes[0] / (double)(2 * tempRes[0] + tempRes[1] + tempRes[2]);
            
            gs = new GrowingSolver(new Logic(new File("logics/hypernym.logic")), synOnlyProbModel);
            gs.solve();
            Model hypSolvedModel = gs.soln;
            tempRes = hypSolvedModel.computeComparison(hypOnlyTrueModel);
            rval[15] = (double)tempRes[0] / (double)(tempRes[0] + tempRes[1]);
            rval[16] = (double)tempRes[0] / (double)(tempRes[0] + tempRes[2]);
            rval[17] = 2.0 * (double)tempRes[0] / (double)(2 * tempRes[0] + tempRes[1] + tempRes[2]);
            
            for(int i = 0; i < 18; i += 3) {
                System.out.println(rval[i] + " , " + rval[i+1] + " , " + rval[i+2]);
            }
            return rval;
        } catch(Exception x) {
            x.printStackTrace();
            System.exit(-1);
        }
        return null;
    }
    
}
