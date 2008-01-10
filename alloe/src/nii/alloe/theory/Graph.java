package nii.alloe.theory;
import java.util.*;
import java.io.*;

/**
 * This class models relations. Most relations should be
 * instantiated as {@link SpecificGraph}s or {@link ProbabilityGraph}s, 
 * however we also allow for compatability
 * with the special relationships equality and set membership and these
 * are implemented via the classes {@link EquivalenceGraph} and {@link MembershipGraph}
 */
public interface Graph {
    /** 
     * Checks if i is connected to j. If i or j is -1 this checks if
     * any connection exists
     */
    public boolean isConnected(int i, int j);

    /**
     * Checks if the graph is mutable at link i -&gt; j. If i or j is -1
     * checks if all connections are mutable
     */
    public boolean mutable(int i, int j);

    /**
     * Adds a link if the graph is mutable at link i -&gt; j. If i or j is -1
     * adds all values
     */
    public void add(int i, int j);

    /**
     * Removes a link from the graph at linke i -&gt; j. If i or j is -1 
     * removes all values
     */
    public void remove(int i, int j);

    /**
     * Returns the number of positive links in the graphs
     */
    public int linkCount();

    /**
     * Prints the graph to DOT file capable of being viewed by GraphViz
     */
    public void dumpToDot(String dotFile);
    
    /**
     * Returns the links in order, the index should be i * n + j
     * generally better to use Model.iterator();
     */
    public Iterator<Integer> iterator(int n);

};