package nii.alloe.theory;
import java.util.*;
import java.io.*;


/**
 * Represents a matrix that contains probabilities of links
 */
public class ProbabilityGraph implements Graph, Serializable {
    private static int n;
    // TODO sort this out so we can have a sparse prob matrix
    private TreeMap<Integer,Double> pm_pos;
    private TreeMap<Integer,Double> pm_neg;
    private double baseValPos, baseValNeg;
    
    
    /**
     * Create a n x n probability matrix
     */
    ProbabilityGraph(int n) {
        this.n = n;
        pm_pos = new TreeMap<Integer,Double>();
        pm_neg = new TreeMap<Integer,Double>();
        baseValPos = 0;
        baseValPos = Double.NEGATIVE_INFINITY;
    }
    
    public boolean isConnected(int i, int j) {
        return pm_pos.get(i * n + j) > pm_neg.get(i * n + j);
    }
    
    public boolean mutable(int i, int j) {
        return true;
    }
    
    public void add(int i, int j) {
        System.err.println("Attempted add on probability graph, please use setPosNegVal or setPosVal");
    }
    
    public void remove(int i, int j) {
        System.err.println("Attempted remove on probability graph, please use setPosNegVal or setPosVal");
    }
    
    public int linkCount() {
        return pm_pos.size();
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
            return baseValPos;
        else
            return (double)d;
    }
    
    /**
     * @return log(P_ij) - log(1 - P_ij)
     */
    public double addVal(int i, int j) {
        return negVal(i,j) - posVal(i,j);
    }
    
    /**
     * @return log(1 - P_ij) - log(P_ij)
     */
    public double removeVal(int i, int j) {
        return posVal(i,j) - negVal(i,j);
    }
    
    /**
     * @param prob The probability, must be between 0 and 1
     */
    public void setPosVal(int i, int j, double prob) {
        if(prob >= 0 && prob <= 1) {
            pm_pos.put(i*n+j,Math.log(prob));
            pm_neg.put(i*n+j,Math.log(1 - prob));
        } else {
            System.err.println("Invalid probability value!");
        }
    }
    
    /**
     * The two log probability values
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
    
    public void setBaseVal(double prob) {
        setBaseVal(Math.log(prob), Math.log(1-prob));
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
    
    public int len() { return n; }
    
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
                    break;
            }
        }
        
        public Integer next() {
            Integer rv = idx;
            while(baseIter.hasNext()) {
                idx = baseIter.next();
                if(pm_pos.get(idx) > pm_neg.get(idx))
                    break;
            }
            return rv;
        }
        public boolean hasNext() { return baseIter.hasNext(); }
        public void remove() { throw new UnsupportedOperationException("Cannot remove from PGIterator"); }
    }
}
