package nii.alloe.theory;
import java.util.*;
import javax.naming.OperationNotSupportedException;

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

public class EquivalenceGraph implements Graph { 
    EquivalenceGraph() {  }

    public boolean isConnected(int i, int j) {
	return i == j || i == -1 || j == -1;
    }

    public boolean mutable(int i , int j) {
	return false;
    }

    public void add(int i, int j) {
	System.err.println("Attempted add on non-mutable equivalence graph");
    }

    public void remove(int i, int j) {
	System.err.println("Attempted remove on non-mutable equivalence graph");
    }

    public int linkCount() { return 0; }

    public void dumpToDot(String dotFile) {
	return;
    }
    
    public Iterator<Integer> iterator(int n) { return new EQIterator(n); }
};