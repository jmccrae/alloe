/*
 * Simulations.java
 *
 * Created on February 5, 2008, 9:55 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package nii.alloe.runs;
import nii.alloe.simulate.*;
import nii.alloe.consist.*;
import nii.alloe.consist.solvers.*;
import nii.alloe.theory.*;
import nii.alloe.niceties.*;
import java.io.*;
import java.util.*;

/**
 *
 * @author john
 */
public class Simulations {
    
    private static final int VEC = 11;
    
    /** Creates a new instance of Simulations */
    public Simulations() {
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter("results"));
            bw.write("connect,time_complete,fm_pre,fm_post_complete,rows,cols,cm1,cm2,cm3,cm4,cm5,cm6\n");
            for(double d = -1; d <= -0.6; d += 0.01) {
                double[] res = new double[VEC];
                double[] resSq = new double[VEC];
                for(int i = 0; i < 500; i++) {
                    double[] res2 = doRun(.8,.8,100,"logics/synonym.logic",d,1);
                    for(int j = 0; j < VEC; j++) {
                        res[j] += res2[j];
                        resSq[j] += res2[j] * 2;
                    }
                }
                bw.write(d + "," + Strings.join(",",res) +","+ Strings.join(",",resSq) + "\n");
                bw.flush();
            }
            bw.close();
        } catch(Exception x) {
            x.printStackTrace();
            System.exit(-1);
        }
        
        //jointImprovement(0.8,0.8,100,-.8,1);
    }
    
    public static double[] doRun(double prec, double recall, int n, String logicFile, double linkDensity, double sparsity) {
        try {
            Simulate s = new Simulate(logicFile,prec,recall,n);
            Iterator<String> relIter = s.l.relationDensity.keySet().iterator();
            while(relIter.hasNext()) {
                s.l.relationDensity.put(relIter.next(),linkDensity);
            }
            s.sparsePercent = sparsity;
            s.createModels();
            /*ConsistSolver cs = new ConsistSolver();
            long time = System.nanoTime();
            int complexity = cs.solve(new Logic(new File(logicFile)),s.probModel);
            time = System.nanoTime() - time;
            Model solvedModel = s.probModel.createSpecificCopy();
            solvedModel.symmDiffAll(cs.soln);
            int[] presolve = s.probModel.computeComparison(s.trueModel);
            int[] postsolve = solvedModel.computeComparison(s.trueModel);*/
            /*GreedySat gs = new GreedySat(new Logic(new File(logicFile)),s.probModel);
            long time = System.nanoTime();
            gs.solve();
            time = System.nanoTime() - time;
            int[] presolve = s.probModel.computeComparison(s.trueModel);
            int[] postsolve = gs.soln.computeComparison(s.trueModel);
            double[] rval = new double[VEC];
            rval[0] = (double)time / 1000000000.0;
            rval[1] = 2.0 * (double)presolve[0] / (double)(2 * presolve[0] + presolve[1] + presolve[2]);
            rval[2] = 2.0 * (double)postsolve[0] / (double)(2 * postsolve[0] + postsolve[1] + postsolve[2]);
            rval[3] = cs.getMatrix().getRowCount();
            rval[4] = cs.getMatrix().getColumnCount();
            rval[5] = complexity;*/
            
            double []rval = new double[VEC];
            GrowingSolver gs = new GrowingSolver(new Logic(new File(logicFile)),s.probModel);
            long time = System.nanoTime();
            gs.solve();
            time = System.nanoTime() - time;
            int[] presolve = s.probModel.computeComparison(s.trueModel);
            int []postsolve = gs.soln.computeComparison(s.trueModel);
            rval[0] = (double)time / 1000000000.0;
            rval[1] = 2.0 * (double)presolve[0] / (double)(2 * presolve[0] + presolve[1] + presolve[2]);
            rval[2] = 2.0 * (double)postsolve[0] / (double)(2 * postsolve[0] + postsolve[1] + postsolve[2]);
            rval[3] = gs.getMatrix().getRowCount();
            rval[4] = gs.getMatrix().getColumnCount();
            rval[5] = presolve[0];
            rval[6] = presolve[1];
            rval[7] = presolve[2];
            rval[8] = postsolve[0];
            rval[9] = postsolve[1];
            rval[10] = postsolve[2];
            /*if(Math.abs(rval[7] - rval[2]) > 0.005) {
                ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("disagreement.model"));
                oos.writeObject(s.probModel);
                oos.close();
                System.err.println("disagreement found");
                System.exit(-1);
            }*/
            
           /* if(rval[4] >= 1 && rval[5]/rval[4] > 10) {
                ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("spike.model"));
                oos.writeObject(s.probModel);
                oos.close();
                System.out.println("spiking model found");
                System.exit(0);
            }*/
            return rval;
        } catch(Exception x) {
            x.printStackTrace();
            System.exit(-1);
        }
        return null;
    }
    
    public static double[] jointImprovement(double prec, double recall, int n, double linkDensity, double sparsity) {
        try {
            Simulate s = new Simulate("logics/sh.logic",prec,recall,n);
            Iterator<String> relIter = s.l.relationDensity.keySet().iterator();
            while(relIter.hasNext()) {
                s.l.relationDensity.put(relIter.next(),linkDensity);
            }
            s.sparsePercent = sparsity;
            s.createModels();
            
            double []rval = new double[18];
            int []tempRes = new int[3];
            
            TreeMap<String,Graph> graphs = new TreeMap<String,Graph>();
            graphs.put("e",s.probModel.getGraphByName("e"));
            graphs.put("r1",s.probModel.getGraphByName("r2"));
            Model synOnlyProbModel = new Model(graphs,n);
            graphs = new TreeMap<String,Graph>();
            graphs.put("e",s.trueModel.getGraphByName("e"));
            graphs.put("r1",s.trueModel.getGraphByName("r2"));
            Model synOnlyTrueModel = new Model(graphs,n);
            tempRes = synOnlyProbModel.computeComparison(synOnlyTrueModel);
            rval[0] = (double)tempRes[0] / (double)(tempRes[0] + tempRes[1]);
            rval[1] = (double)tempRes[0] / (double)(tempRes[0] + tempRes[2]);
            rval[2] = 2.0 * (double)tempRes[0] / (double)(2 * tempRes[0] + tempRes[1] + tempRes[2]);
            graphs = new TreeMap<String,Graph>();
            graphs.put("e",s.probModel.getGraphByName("e"));
            graphs.put("r1",s.probModel.getGraphByName("r1"));
            Model hypOnlyProbModel = new Model(graphs,n);
            graphs = new TreeMap<String,Graph>();
            graphs.put("e",s.trueModel.getGraphByName("e"));
            graphs.put("r1",s.trueModel.getGraphByName("r1"));
            Model hypOnlyTrueModel = new Model(graphs,n);
            tempRes = hypOnlyProbModel.computeComparison(hypOnlyTrueModel);
            rval[3] = (double)tempRes[0] / (double)(tempRes[0] + tempRes[1]);
            rval[4] = (double)tempRes[0] / (double)(tempRes[0] + tempRes[2]);
            rval[5] = 2.0 * (double)tempRes[0] / (double)(2 * tempRes[0] + tempRes[1] + tempRes[2]);
            
            ConsistSolver cs = new ConsistSolver();
            cs.solve(new Logic(new File("logics/sh.logic")),s.probModel);
            Model shSolvedModel = s.probModel.createSpecificCopy();
            shSolvedModel.symmDiffAll(cs.soln);
            
            graphs = new TreeMap<String,Graph>();
            graphs.put("e",shSolvedModel.getGraphByName("e"));
            graphs.put("r1",shSolvedModel.getGraphByName("r2"));
            Model shSynSolved = new Model(graphs,n);
            tempRes = shSynSolved.computeComparison(synOnlyTrueModel);
            rval[6] = (double)tempRes[0] / (double)(tempRes[0] + tempRes[1]);
            rval[7] = (double)tempRes[0] / (double)(tempRes[0] + tempRes[2]);
            rval[8] = 2.0 * (double)tempRes[0] / (double)(2 * tempRes[0] + tempRes[1] + tempRes[2]);
            
            graphs = new TreeMap<String,Graph>();
            graphs.put("e",shSolvedModel.getGraphByName("e"));
            graphs.put("r1",shSolvedModel.getGraphByName("r1"));
            Model shHypSolved = new Model(graphs,n);
            tempRes = shHypSolved.computeComparison(hypOnlyTrueModel);
            rval[9] = (double)tempRes[0] / (double)(tempRes[0] + tempRes[1]);
            rval[10] = (double)tempRes[0] / (double)(tempRes[0] + tempRes[2]);
            rval[11] = 2.0 * (double)tempRes[0] / (double)(2 * tempRes[0] + tempRes[1] + tempRes[2]);
            
            cs = new ConsistSolver();
            cs.solve(new Logic(new File("logics/synonym.logic")), synOnlyProbModel);
            Model synSolvedModel = synOnlyProbModel.createSpecificCopy();
            synSolvedModel.symmDiffAll(cs.soln);
            tempRes = synSolvedModel.computeComparison(synOnlyTrueModel);
            rval[12] = (double)tempRes[0] / (double)(tempRes[0] + tempRes[1]);
            rval[13] = (double)tempRes[0] / (double)(tempRes[0] + tempRes[2]);
            rval[14] = 2.0 * (double)tempRes[0] / (double)(2 * tempRes[0] + tempRes[1] + tempRes[2]);
            
            cs = new ConsistSolver();
            cs.solve(new Logic(new File("logics/hypernym.logic")), hypOnlyProbModel);
            Model hypSolvedModel = hypOnlyProbModel.createSpecificCopy();
            hypSolvedModel.symmDiffAll(cs.soln);
            tempRes = hypSolvedModel.computeComparison(hypOnlyTrueModel);
            rval[15] = (double)tempRes[0] / (double)(tempRes[0] + tempRes[1]);
            rval[16] = (double)tempRes[0] / (double)(tempRes[0] + tempRes[2]);
            rval[17] = 2.0 * (double)tempRes[0] / (double)(2 * tempRes[0] + tempRes[1] + tempRes[2]);
            
            for(int i = 0; i < 18; i += 3) {
                System.out.println(rval[i] + " , " + rval[i+1] + " , " + rval[i+2]);
            }
            
        } catch(Exception x) {
            x.printStackTrace();
            System.exit(-1);
        }
        return null;
    }
    
}
