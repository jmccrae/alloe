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
import java.io.*;
import java.util.*;
import nii.alloe.classify.ProbModelBuilder;
import nii.alloe.tools.strings.Strings;

/**
 *
 * @author john
 */
public class Simulations {
    
    private static final int VEC = 13;
    
    /** Creates a new instance of Simulations */
    public Simulations() {
    }
    
    public static boolean includeConstruct = false;
    public static boolean includeSyn =false;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String []newArgs = { "logics/sh.logic","test","5", "101", "yes", "yes" };
        args = newArgs;
        includeConstruct = args[4].equals("yes");
        includeSyn = args[5].equals("yes");
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter("results-" + args[1]));
            bw.write("n,pre,null,null2,gsat-time,gsat-fm,gs-time,gs-fm,rfs-time,rfs-fm,const-time,const-fm,sets-time,sets-fm,bnb-time,bnb-fm\n");
            for(int n = Integer.parseInt(args[2]); n < Integer.parseInt(args[3]); n += Integer.parseInt(args[2])) {
                double[] res = new double[VEC];
                for(int i = 0; i < 10; i++) {
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
            System.out.println("Building simulate data");
            double []rval = new double[VEC];
            
            /*Simulate s = new Simulate(logicFile,prec,recall,n);
            Iterator<String> relIter = s.relationDensity.keySet().iterator();
            while(relIter.hasNext()) {
                s.relationDensity.put(relIter.next(),linkDensity);
            }
            s.sparsePercent = sparsity;
            s.createModels();
            writeSimulation(s,"sim-temp");*/
            Simulate s = readSimulation("sim-temp");
            
            //printModel(s.probModel);
            
            ConsistSolver cs = new ConsistSolver();
            System.out.println("consist");
            long time = System.nanoTime();
            //int complexity = cs.solve(new Logic(new File(logicFile)),s.probModel);
            time = System.nanoTime() - time;
            //Model solvedModel = s.probModel.createSpecificCopy();
            //solvedModel.symmDiffAll(cs.soln);
            int[] presolve = s.probModel.computeComparison(s.trueModel);
            int[] postsolve ;//= solvedModel.computeComparison(s.trueModel);
            //printModel(solvedModel);
            //rval[1] = (double)time / 1000000000.0;
            rval[0] = 2.0 * (double)presolve[0] / (double)(2 * presolve[0] + presolve[1] + presolve[2]);
            //rval[2] = 2.0 * (double)postsolve[0] / (double)(2 * postsolve[0] + postsolve[1] + postsolve[2]);
            
            GreedySat gs2 = new GreedySat(new Logic(new File(logicFile)),s.probModel);
            System.out.println("gsat");
            time = System.nanoTime();
            gs2.solve();
            time = System.nanoTime() - time;
            presolve = s.probModel.computeComparison(s.trueModel);
            postsolve = gs2.soln.computeComparison(s.trueModel);
            //printModel(gs2.soln);
            rval[1] = (double)time / 1000000000.0;
            rval[2] = 2.0 * (double)postsolve[0] / (double)(2 * postsolve[0] + postsolve[1] + postsolve[2]);
            
            GrowingSolver gs = new GrowingSolver(new Logic(new File(logicFile)),s.probModel);
            System.out.println("grow");
            time = System.nanoTime();
            gs.solve();
            time = System.nanoTime() - time;
            presolve = s.probModel.computeComparison(s.trueModel);
            postsolve = gs.soln.computeComparison(s.trueModel);
            //printModel(gs.soln);
            rval[3] = (double)time / 1000000000.0;
            rval[4] = 2.0 * (double)postsolve[0] / (double)(2 * postsolve[0] + postsolve[1] + postsolve[2]);
            
            ResFreeSolver rfs = new ResFreeSolver(new Logic(new File(logicFile)),s.probModel);
            System.out.println("resfree");
            time = System.nanoTime();
            rfs.solve();
            time = System.nanoTime() - time;
            postsolve = rfs.soln.computeComparison(s.trueModel);
            //printModel(rfs.soln);
            rval[5] = (double)time / 1000000000.0;
            rval[6] = 2.0 * (double)postsolve[0] / (double)(2 * postsolve[0] + postsolve[1] + postsolve[2]);
            
            if(includeConstruct) {
                Constructor c  = new Constructor(Constructor.ADD_NODES_MOST_CENTRAL, Constructor.METHOD_GREEDY);
                Logic logic = new Logic(new File(logicFile));
                System.out.println("construct");
                time = System.nanoTime();
                c.solve(s.probModel, logic);
                time = System.nanoTime() - time;
                //printModel(c.solvedModel);
                c.solvedModel.addCompulsorys(s.probModel.logic);
                postsolve = c.solvedModel.computeComparison(s.trueModel);
                rval[7] = (double)time / 1000000000.0;
                rval[8] = 2.0 * (double)postsolve[0] / (double)(2 * postsolve[0] + postsolve[1] + postsolve[2]);
            }
            
            if(includeSyn) {
            GreedySets gs3 = new GreedySets(s.probModel, "r1");
            System.out.println("greedy-sets");
            time = System.nanoTime();
            gs3.solve();
            time = System.nanoTime() - time;
            postsolve = gs3.soln.computeComparison(s.trueModel);
            //printModel(gs3.soln);
            rval[9] = (double)time / 1000000000.0;
            rval[10] = 2.0 * (double)postsolve[0] / (double)(2 * postsolve[0] + postsolve[1] + postsolve[2]);
            
            SetBnB sbnb = new SetBnB(s.probModel, "r1");
            System.out.println("set-b-n-b");
            time = System.nanoTime();
            sbnb.solve();
            time = System.nanoTime() - time;
            postsolve = sbnb.soln.computeComparison(s.trueModel);
            //printModel(sbnb.soln);
            rval[11] = (double)time / 1000000000.0;
            rval[12] = 2.0 * (double)postsolve[0] / (double)(2 * postsolve[0] + postsolve[1] + postsolve[2]);
            }
            return rval;
        } catch(Exception x) {
            x.printStackTrace();
            System.exit(-1);
        }
        return null;
    }
    
    private static void printModel(Model model) {
        Iterator<Integer> i = model.iterator();
        int val = model.getModelSize();
        while(i.hasNext()) {
            int j = i.next();
            System.out.print((j / val) + " -> " + (j % val) + ", ");
        }
        System.out.println();
    }
    
    private static void writeSimulation(Simulate s, String fileName) {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName));
            oos.writeObject(s);
        } catch(IOException x) {
            x.printStackTrace();
            System.exit(-1);
        }
    }
    
    private static Simulate readSimulation(String fileName) {
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName));
            return (Simulate)ois.readObject();
        } catch(IOException x) {
            x.printStackTrace();
            System.exit(-1);
            return null;
        } catch(ClassNotFoundException x) {
            x.printStackTrace();
            System.exit(-1);
            return null;
        }
    }
    
}
