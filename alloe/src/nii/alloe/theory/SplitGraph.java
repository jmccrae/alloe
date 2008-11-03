package nii.alloe.theory;
import java.util.*;
import nii.alloe.tools.struct.MultiIterator;

/**
 * A meta-graph representing a split between the standard elements and skolem
 * elements. Require three graphs to instantiate, the standard graph represents
 * those connections between standard elements, the skolem graph for those
 * between skolem elements and the inter graph for those between skolem and 
 * standard elements.
 * @see Graph
 * @author John McCrae, National Institute of Informatics
 */
public class SplitGraph implements Graph {
    final Graph standard;
    final Graph inter;
    final Graph skolem;
    final int skolemStart;

    /** Create a new instance */
    public SplitGraph(Graph standard, Graph inter, Graph skolem, int skolemStart) {
	this.standard = standard;
	this.inter = inter;
	this.skolem = skolem;
	this.skolemStart = skolemStart;
    }

    public boolean isConnected(int i, int j) {
	if(i < skolemStart && j < skolemStart)
	    return standard.isConnected(i,j);
	if(i >= skolemStart && j >= skolemStart)
	    return skolem.isConnected(i,j);
	else
	    return inter.isConnected(i,j);
    }

    public boolean mutable(int i, int j) {
	if(i < skolemStart && j < skolemStart)
	    return standard.mutable(i,j);
	if(i >= skolemStart && j >= skolemStart)
	    return skolem.mutable(i,j);
	else
	    return inter.mutable(i,j);
    }

    public void add(int i, int j) {
	if(i < skolemStart && j < skolemStart)
	    standard.add(i,j);
	else if(i >= skolemStart && j >= skolemStart)
	    skolem.add(i,j);
	else
	    inter.add(i,j);
    }

    public void remove(int i, int j) {
	if(i < skolemStart && j < skolemStart)
	    standard.remove(i,j);
	else if(i >= skolemStart && j >= skolemStart)
	    skolem.remove(i,j);
	else
	    inter.remove(i,j);
    }

    public void setVal(int i, int j, double val) {
	if(i < skolemStart && j < skolemStart)
	    standard.setVal(i,j,val);
	else if(i >= skolemStart && j >= skolemStart)
	    skolem.setVal(i,j,val);
	else
	    inter.setVal(i,j,val);
    }

    public double getVal(int i, int j) {
	if(i < skolemStart && j < skolemStart)
	    return standard.getVal(i,j);
	else if(i >= skolemStart && j >= skolemStart)
	    return skolem.getVal(i,j);
	else
	    return inter.getVal(i,j);
    }

    public int linkCount() {
	return standard.linkCount() + skolem.linkCount() + inter.linkCount();
    }

    public void dumpToDot(String dotFile) {
	return;
    }

    /** Iterate through links. Order is standard links, inter links, skolem links */
    public Iterator<Integer> iterator(int n) {
	LinkedList<Iterator<Integer>> iters = new LinkedList<Iterator<Integer>>();
	iters.add(standard.iterator(n));
	iters.add(inter.iterator(n));
	iters.add(skolem.iterator(n));
	return new MultiIterator<Integer>(iters.iterator());
    }

    public Graph createCopy() {
	return new SplitGraph(standard.createCopy(), inter.createCopy(),
			      skolem.createCopy(), skolemStart);
    }
}
