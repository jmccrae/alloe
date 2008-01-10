package nii.alloe.theory;
import java.util.*;

class MembershipIterator implements Iterator<Integer> {
    public int n;
    private Iterator<Integer> setIter;
    
    MembershipIterator(int n, Iterator<Integer> setIter) { this.n = n; this.setIter = setIter; }
    
    public Integer next() { Integer i = setIter.next(); return i * n + i; }
    public boolean hasNext() { return setIter.hasNext(); }
    public void remove() { setIter.remove(); }
}

/** A pseudo-graph implementing set membership (\in). Immutable, i is connected 
 * to j iff i == j and i \in S. 
 *
 *  @see Graph
 *
 * @author John McCrae, National Institute of Informatics
 */

public class MembershipGraph implements Graph {
    TreeSet<Integer> set;

    MembershipGraph(TreeSet<Integer> set) { 
	this.set = set;
    }

    public boolean isConnected(int i, int j) {
	return i == j && (set.contains(new Integer(i)) || (i == -1 && !set.isEmpty()));
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
    
    public Iterator<Integer> iterator(int n) { return new MembershipIterator(n,set.iterator()); }
}