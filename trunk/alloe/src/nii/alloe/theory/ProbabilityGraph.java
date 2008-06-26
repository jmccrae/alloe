package nii.alloe.theory;
import java.util.*;
import java.io.*;
import nii.alloe.tools.process.Output;

/**
 * Represents a matrix that contains probabilities of links
 */
public class ProbabilityGraph implements Graph, Serializable {
    static int n;
    // TODO sort this out so we can have a sparse prob matrix
    private TreeMap<Integer,Double> pm_pos;
    private TreeMap<Integer,Double> pm_neg;
    private double baseValPos, baseValNeg;
    /** If attempts are made to set a prob value to 1 or zero, this value is used to avoid
     * -Infinitys appearing.*/
    public static final double MIN_PROB = -1e+99;
    
    /**
     * Create a n x n probability matrix
     */
    ProbabilityGraph(int n) {
        this.n = n;
        pm_pos = new TreeMap<Integer,Double>();
        pm_neg = new TreeMap<Integer,Double>();
        baseValPos = 0;
        baseValNeg = MIN_PROB;
    }
    
    private ProbabilityGraph(int n, TreeMap<Integer,Double> pm_pos, TreeMap<Integer,Double> pm_neg, double baseValPos, double baseValNeg) {
        this.n = n;
        this.pm_pos = pm_pos;
        this.pm_neg = pm_neg;
        this.baseValPos = baseValPos;
        this.baseValNeg = baseValNeg;
    }
    
    public boolean isConnected(int i, int j) {
        return posVal(i,j) > negVal(i,j);
    }
    
    public boolean mutable(int i, int j) {
        return true;
    }
    
    public void add(int i, int j) {
        Output.err.println("WARNING: Adding link (" + i + " -> " + j + " with absolute probability!");
        setVal(i,j,1);
    }
    
    public void remove(int i, int j) {
        Output.err.println("WARNING: Adding link (" + i + " -> " + j + " with absolute probability!");
        setVal(i,j,0);
    }

    
    public int linkCount() {
        int rval = 0;
        Iterator<Double> posIter = pm_pos.values().iterator();
        Iterator<Double> negIter = pm_neg.values().iterator();
        while(posIter.hasNext()) {
            if(posIter.next() > negIter.next())
                rval++;
        }
        if(baseValPos < baseValNeg) {
            rval = rval + n * n  - pm_pos.size();
        }
        return rval;
    }
    
    public void dumpToDot(String dotFile) {
        try {
            PrintStream dot = new PrintStream(new FileOutputStream(dotFile));
            dot.println("digraph G {");
            Iterator<Map.Entry<Integer,Double>> posIter = pm_pos.entrySet().iterator();
            Iterator<Double> negIter = pm_neg.values().iterator();
            while(posIter.hasNext()) {
                Map.Entry<Integer,Double> e = posIter.next();
                int i = e.getKey() % n;
                int j = e.getKey() / n;
                
                dot.println("\tn"+i+" -> n"+j+" .get(label=\"" +
                        (e.getValue() - negIter.next()) + "\");");
                
            }
            dot.println("}");
            dot.close();
        } catch(IOException x) {
            x.printStackTrace();
            System.exit(0);
        }
    }
    
    public Graph createCopy() {
        return new ProbabilityGraph(n,new TreeMap<Integer,Double>(pm_pos), new TreeMap<Integer,Double>(pm_neg), baseValPos,baseValNeg);
    }
    
    /**
     * @return log(P_ij)
     */
    public double posVal(int i, int j) {
        Double d = pm_pos.get(i*n+j);
        if(d == null)
            return baseValPos;
        else 
            return (double)d;
    }
    
    /**
     * @return log(1 - P_ij)
     */
    public double negVal(int i, int j) {
        Double d = pm_neg.get(i*n+j);
        if(d == null)
            return baseValNeg;
        else
            return (double)d;
    }
    
    /**
     * @return log(1 - P_ij) - log(P_ij)
     */
    public double addVal(int i, int j) {
        return negVal(i,j) - posVal(i,j);
    }
    
    /**
     * @return log(P_ij) - log(1 - P_ij)
     */
    public double removeVal(int i, int j) {
        return posVal(i,j) - negVal(i,j);
    }
    
    /**
     * Set the probablity value. Setting the value to zero will not remove a
     * value, neither will setting it to the base value.
     * @param prob The probability, must be between 0 and 1
     */
    public void setVal(int i, int j, double prob) {
        if(prob > 0 && prob < 1) {
            pm_pos.put(i*n+j,Math.log(prob));
            pm_neg.put(i*n+j,Math.log(1 - prob));
        } else if(prob == 1) {
            pm_pos.put(i*n+j,0.0);
            pm_neg.put(i*n+j,MIN_PROB);
        } else if(prob == 0) {
            pm_pos.put(i*n+j,MIN_PROB);
            pm_neg.put(i*n+j,0.0);
        } else {
	    throw new IllegalArgumentException("Non-probability value");
        }
    }

    /**
     * Get the probability value.
     * Note it is stored as a logarithm, so there is no guarantee of an exact
     * conversion.
     */
    public double getVal(int i, int j) {
	return Math.exp(pm_pos.get(i * n + j));
    }
    
    /**
     * Set the two log probability values directly
     * @param p The positive value i.e. log(P_ij)
     * @param ng The negative value i.e. log(1 - P_ij)
     */
    public void setPosNegVal(int i, int j, double p, double ng) {
        if(Math.exp(p) + Math.exp(ng) < 0.99 ||
                Math.exp(p) + Math.exp(ng) > 1.01 ||
                p > 0 || ng > 0) {
            System.err.println("Values specified to ProbabilityGraph for ("
                    + i + "," + j + ") do not sum to 1: p=" + p +
                    " n=" + ng + " sum to " + (Math.exp(p) + Math.exp(ng)));
        }
        pm_pos.put(i*n+j,p);
        pm_neg.put(i*n+j,ng);
    }
    
    public double getBaseValPos() {
        return baseValPos;
    }
    
    public double getBaseValNeg() {
        return baseValNeg;
    }
    
    public void setBaseVal(double prob) {
        if(prob > 0 && prob < 1) {
            setBaseVal(Math.log(prob), Math.log(1-prob));
        } else if(prob == 1) {
            setBaseVal(0,MIN_PROB);
        } else if(prob == 0) {
            setBaseVal(MIN_PROB,0);
        } else {
            throw new IllegalArgumentException("Probability value should be between 0 and 1");
        }
    }
    
    public void setBaseVal(double p, double ng) {
        if(Math.exp(p) + Math.exp(ng) < 0.99 ||
                Math.exp(p) + Math.exp(ng) > 1.01 ||
                p > 0 || ng > 0) {
            System.err.println("Values specified to ProbabilityGraph for (base) do not sum to 1: p=" + p +
                    " n=" + ng + " sum to " + (Math.exp(p) + Math.exp(ng)));
        }
        baseValPos = p;
        baseValNeg = ng;
    }
    
    /**
     * Deprecated prefer serialization
     */
    public void writeToFile(String out_fname, Graph g) {
        try {
            PrintStream p = new PrintStream(new FileOutputStream(out_fname));
            for(int i = 0; i < n*n; i++) {
                if(i%n == i/n)
                    continue;
                p.printf("x %+d %1.9f %+d\n", (pm_pos.get(i) > Math.log(0.5) ? 1 : -1),
                        Math.exp(pm_pos.get(i)), (g.isConnected(i/n,i%n) ? 1 : -1));
            }
            p.close();
        } catch(Exception x) {
            x.printStackTrace();
            System.exit(0);
        }
    }
    
    
    public Iterator<Integer> iterator(int n) {
        if(this.n != n)
            throw new IllegalArgumentException();
        return new PGIterator(pm_pos.keySet().iterator());
    }
    
    private class PGIterator implements Iterator<Integer> {
        int idx;
        Iterator<Integer> baseIter;
        PGIterator(Iterator<Integer> baseIter) {
            this.baseIter = baseIter;
            while(baseIter.hasNext()) {
                idx = baseIter.next();
                if(pm_pos.get(idx) > pm_neg.get(idx))
                    return;
            }
            idx = -1;
        }
        
        public Integer next() {
            Integer rv = idx;
            while(baseIter.hasNext()) {
                idx = baseIter.next();
                if(pm_pos.get(idx) > pm_neg.get(idx))
                    return rv;
            }
            idx = -1;
            return rv;
        }
        public boolean hasNext() { return idx >= 0; }
        public void remove() { throw new UnsupportedOperationException("Cannot remove from PGIterator"); }
    }
}
