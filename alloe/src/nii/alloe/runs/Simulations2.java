package nii.alloe.runs;
import java.text.DecimalFormat;
import javax.swing.text.html.CSS;
import nii.alloe.consist.*;
import nii.alloe.consist.solvers.*;
import nii.alloe.theory.*;
import nii.alloe.simulate.*;
import java.io.*;
import java.util.*;
import nii.alloe.tools.strings.Strings;

/**
 *
 * @author John McCrae, National Institute of Informatics
 */
public class Simulations2 {
    
    static Model probModel;
    static Model trueModel;
    
    static final String logic = "logics/hypernym.logic";
    
    public static void main(String[] args) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter("results-gsat"));
            for(int i = 0; i < 130; i++) {
                loadModel(i);
                bw.write(Strings.join(",",evalGSat()));
                bw.write("\n");
                bw.flush();
            }
        } catch(Exception x) {
            x.printStackTrace();
            System.exit(-1);
        }
    }
    
    private static double[] evalGrowing() throws IOException {
        GrowingSolver gs = new GrowingSolver(new Logic(new File(logic)),probModel);
        long time = System.nanoTime();
        gs.solve();
        time = System.nanoTime() - time;
        double []rv = new double[2];
        rv[0] = (double)time / 1000000000.0;
        rv[1] = gs.cost;
        return rv;           
    }
    
    private static double[] evalSinglePass() throws IOException {
        ConsistSolver cs = new ConsistSolver();
        Logic l = new Logic(new File(logic));
        long time = System.nanoTime();
        cs.solve(l,probModel);
        time = System.nanoTime() - time;
        double []rv = new double[2];
        rv[0] = (double)time / 1000000000.0;
        rv[1] = cs.cost;
        return rv;
    }
    
    private static double[] evalConstructAstar() throws IOException {
        Constructor cs = new Constructor(Constructor.ADD_NODES_LEAST_CENTRAL, Constructor.METHOD_ASTAR);
        Logic l = new Logic(new File(logic));
        long time = System.nanoTime();
        cs.solve(probModel,l);
        time = System.nanoTime() - time;
        double []rv = new double[2];
        rv[0] = (double)time / 1000000000.0;
        rv[1] = cs.cost;
        return rv;
    }
    
    private static double[] evalConstructGreedy() throws IOException {
        Constructor cs = new Constructor(Constructor.ADD_NODES_LEAST_CENTRAL, Constructor.METHOD_GREEDY);
        Logic l = new Logic(new File(logic));
        long time = System.nanoTime();
        cs.solve(probModel,l);
        time = System.nanoTime() - time;
        double []rv = new double[2];
        rv[0] = (double)time / 1000000000.0;
        rv[1] = cs.cost;
        return rv;
    }
    
    private static double[] evalGSat() throws IOException {
         GreedySat gs = new GreedySat(new Logic(new File(logic)),probModel);
        long time = System.nanoTime();
        gs.solve();
        time = System.nanoTime() - time;
        double []rv = new double[2];
        rv[0] = (double)time / 1000000000.0;
        rv[1] = gs.cost;
        return rv;  
    }
    
    private static void loadModel(int i) {
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream("/home/john/models/" + i + ".model"));
            ois.readObject();
            probModel = (Model)ois.readObject();
            trueModel = (Model)ois.readObject();
        } catch(Exception x) {
            x.printStackTrace();
            System.exit(-1);
        }
    }
    
    private static void dumpAllModel(String logicFile) {
        try {
            for(double d = -1.5; d < -0.2; d += 0.01) {
                Simulate s = new Simulate(logicFile,.8,.8,100);
                Iterator<String> relIter = s.relationDensity.keySet().iterator();
                while(relIter.hasNext()) {
                    s.relationDensity.put(relIter.next(),d);
                }
                s.sparsePercent = 1;
                s.createModels();
                int r = (int)((d + 1.5) / 0.01);
                ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("/home/john/models/" + r + ".model"));
                oos.writeObject(d);
                oos.writeObject(s.probModel);
                oos.writeObject(s.trueModel);
                oos.close();
            }
        } catch(Exception x) {
            x.printStackTrace();
            System.exit(-1);
        }
    }
}
