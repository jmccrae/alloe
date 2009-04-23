package nii.alloe.simulate;
import java.util.*;
import java.io.*;
import cern.jet.random.*;
import cern.jet.random.engine.*;
import nii.alloe.consist.solvers.GreedySat;
import nii.alloe.consist.GrowingSolver;
import nii.alloe.theory.Graph;
import nii.alloe.theory.Logic;
import nii.alloe.theory.Model;
import nii.alloe.theory.ProbabilityGraph;
import nii.alloe.theory.SpecificGraph;

public class Simulate implements Serializable {
    public int n;
    public double p;
    public double r;
    private String out;
    public Logic l;
    public Model trueModel;
    public Model probModel;
    public double sparsePercent;
    public TreeMap<String,Double> relationDensity;
    
    public Simulate(String logicFile, double prec, double recall, int n) throws IOException {
        l = new Logic(new File(logicFile));
        p = prec;
        r = recall;
        this.n = n;
        sparsePercent = 1;
        relationDensity = new TreeMap<String,Double>();
        for(String s : l.getRelations()) {
            relationDensity.put(s,-1.0);
        }
    }
    
    
    public void createModels() {
        //GrowingSolver gs;
        //do {
        l.ruleSymbols.setModelSize(n);
            trueModel = makeGraphs(l);
            //l.consistCheck(trueModel, new MakeConsistent());
            Model gsModel = trueModel.createProbabilityCopy(.73,.27);
            //gs = new GrowingSolver(l,gsModel);
            //gs.setApproxSolve(true);
        //} while(!gs.solve());
            //gs.solve();
            GreedySat gs  = new GreedySat(l, gsModel);
            gs.solve();
        trueModel = gs.soln;
        probModel = makeProbGraphs(trueModel);
    }
    
    
    private Model makeGraphs(Logic l) {
        Iterator<String> i = relationDensity.keySet().iterator();
        TreeMap<String, Graph> tig = new TreeMap<String, Graph>();
        Model m = new Model(l);
        while(i.hasNext()) {
            String idx = i.next();
            
            SpecificGraph g = m.addSpecificGraph(idx);
            g.makeRandom(Math.pow((double)n,
                    relationDensity.get(idx).doubleValue()));
            //if(l.useLocking.contains(idx)) {
            //    g.enableLocking();
            //}
            
            tig.put(idx, g);
        }
        
        m.addBasicGraphs(l);
        
        return m;
    }
    
    private Model makeProbGraphs(Model spec) {
        Iterator<String> i = relationDensity.keySet().iterator();
        Model rval = new Model(spec);
        while(i.hasNext()) {
            String idx = i.next();
            ProbabilityGraph p = rval.addProbabilityGraph(idx);
            createData(p, spec.graphs.get(idx),idx);
        }
        
        rval.addBasicGraphs(l);
        
        return rval;
    }
    
    
    
    
    //public void format_error(String problem) {
    //    System.err.println("Unexpected " + problem);
    //    System.err.println("Usage:");
    //    System.err.println("   java theory.Simulate -o outdir -n n -p p -r r logic_file");
    //    System.err.println(" outdir: output directory");
    //    System.err.println(" p: desired precision");
    //    System.err.println(" r: desired recall");
    //    System.err.println(" n: size of data set");
    //   System.err.println(" logic_file: logic file");
    //    System.exit(0);
    //}
    
    
    private void createData(ProbabilityGraph rval, Graph g, String graph_name) {
        int pos = g.linkCount();
        double mu_pos = normal_cdf_inverse(r);
        double goal = r * (1 -p) * pos / p;
        double mu_neg = normal_cdf_inverse(goal / (n*(n-1) - pos));
        Normal pos_normal = new Normal(mu_pos, 1, new MersenneTwister());
        Normal neg_normal = new Normal(mu_neg, 1, new MersenneTwister());
        Normal ntr_normal = new Normal(0,1,new MersenneTwister());
        int tp, fp, fn, tn;
        tp = fp = fn = tn = 0;
        rval.setBaseVal(ntr_normal.cdf(mu_neg - 3));
        Random rand = new Random();
        
        for(int i = 0; i < n * n; i++) {
            //if(i % n == i / n)
            //    continue;
            if(g.isConnected(i / n, i % n)) {
                rval.setVal(i/n,i%n,ntr_normal.cdf(pos_normal.nextDouble()));
                if(rval.isConnected(i/n,i%n)) {
                    tp++;
                } else {
                    fn++;
                }
            } else {
                if(rand.nextDouble() < sparsePercent) {
                    rval.setVal(i/n,i%n,ntr_normal.cdf(neg_normal.nextDouble()));
                    if(rval.isConnected(i/n,i%n)) {
                        fp++;
                    } else {
                        tn++;
                    }
                }
            }
        }
        
        System.out.println("Results for relationship: " + graph_name);
        System.out.println("\t" + tp + "\t" + fn);
        System.out.println("\t" + fp + "\t" + tn);
        double recall =  (((double)tp) / ((double)(tp + fn)));
        System.out.println("Recall: " + recall);
        double precision = (((double)tp) / ((double)(tp + fp)));
        System.out.println("Precision: " + precision);
        System.out.println("F-Measure: " + (2 * precision * recall / (precision + recall)));
        
    }
    
    // This function find the inverse of the Normal function
    // (ie the Normal Quotient function) by gradient descent ( :s )
    private double normal_cdf_inverse(double d) {
        double delta = 0.0000001;
        double x = 0;
        Normal ntr_normal = new Normal(0,1,new MersenneTwister());
        
        while(ntr_normal.cdf(x) - d < -delta ||
                ntr_normal.cdf(x) - d > delta) {
            double grad = (ntr_normal.cdf(x + delta) - ntr_normal.cdf(x-delta)) / (2 * delta);
            if(grad == 0)
                grad = delta;
            x -= (ntr_normal.cdf(x) - d) / grad;
        }
        return x;
    }
    
    
    public void output_term_list(String target) {
        try {
            PrintStream syn_file = new PrintStream(new FileOutputStream(target + ".synlist"));
            for(int i = 0; i < n; i++) {
                syn_file.printf("[ \"%d\" ]\n", i);
            }
            syn_file.close();
            
            PrintStream pos_file = new PrintStream(new FileOutputStream(target + ".pairs"));
            for(int j = 0; j < n*n; j++) {
                if(j%n == j/n)
                    continue;
                pos_file.printf("%d => %d\n", j/n, j%n);
            }
            pos_file.close();
            
            File.createTempFile(target, ".nonpairs");
        } catch(Exception x) {
            x.printStackTrace();
            System.exit(0);
        }
    }
}