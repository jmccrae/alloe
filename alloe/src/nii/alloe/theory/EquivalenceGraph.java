package nii.alloe.theory;
import java.util.*;
import java.io.*;

class EQIterator implements Iterator<Integer> {
    int i = 0;
    int n;
    
    EQIterator(int n) { this.n = n; }
    
    public Integer next() { return n * i + i++;  }
    public boolean hasNext() { return i < n; }
    public void remove() { throw new UnsupportedOperationException(); }
}

/**
 * A pseudo-graph to represent the equivalence. Totally immutable, i is connected to j iff i == j
 *
 * @see Graph
 *
 * @author John McCrae, National Institute of Informatics
 */

public class EquivalenceGraph implements Graph, Serializable { 
    EquivalenceGraph() {  }

    public boolean isConnected(int i, int j) {
	return i == j || i == -1 || j == -1;
    }

    public boolean mutable(int i , int j) {
	return false;
    }

    public void add(int i, int j) {
	throw new UnsupportedOperationException("Attempted add on non-mutable equivalence graph");
    }

    public void remove(int i, int j) {
	throw new UnsupportedOperationException("Attempted remove on non-mutable equivalence graph");
    }
    
    public void setVal(int i, int j, double val) {
	throw new UnsupportedOperationException("Attempted setVal on non-mutable graph");
    }

    public double getVal(int i, int j) {
	return isConnected(i,j) ? 1.0 : 0.0;
    }

    public int linkCount() { return 0; }

    public void dumpToDot(String dotFile) {
	return;
    }
    
    public Iterator<Integer> iterator(int n) { return new EQIterator(n); }
    
    public Graph createCopy() { return this; }
};