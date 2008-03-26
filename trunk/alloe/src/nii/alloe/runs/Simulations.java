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
        String []newArgs = { "logics/hypernym.logic","test","50" };
        args = newArgs;
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter("results-" + args[1]));
            bw.write("connect,time_complete,fm_pre,fm_post_complete,rows,cols,cm1,cm2,cm3,cm4,cm5,cm6\n");
            for(int n = Integer.parseInt(args[2]); n < 4000; n += 50) {
                double[] res = new double[VEC];
                for(int i = 0; i < 1; i++) {
                    double[] res2 = doRun(.8,.8,n,args[0],-.8,.15);
                    for(int j = 0; j < VEC; j++) {
                        res[j] += res2[j];
                    }
                }
                bw.write(n + "," + Strings.join(",",res) + "\n");
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
    
    
}
