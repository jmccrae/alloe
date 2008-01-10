package nii.alloe.corpus;
import java.io.Serializable;
import java.util.*;

/**
 * A set of term pairs. Implemented via a tree set on the string "term1 => term2"
 *
 * @author John McCrae, National Institute of Informatics
 */
public class TermPairSet implements Serializable {
    
    private TreeSet<String> termPairs;
    static final String glue = " => ";
    
    /** Create a new instance */
    public TermPairSet() {
        termPairs = new TreeSet<String>(new Comparator<String>() {
            public int compare(String s1, String s2) {
                return s1.toLowerCase().compareTo(s2.toLowerCase());
            }
            public boolean equals(Object object) {
               if(this == object) return true; return false;
            }
        });
    }
    
    /** Add term1 => term2 to the set 
     * @return true if the set did not contain the specific
     */
    public boolean add(String term1, String term2) {
        return termPairs.add(term1 + glue + term2);
    }
    
    /** Remove term1 => term2 from the set 
     * @return true if the set contained the specific element
     */
    public boolean remove(String term1, String term2) {
        return termPairs.remove(term1 + glue + term2);
    }
    
    /** @return true if the set contains term1 => term2 */
    public boolean contains(String term1, String term2) {
        return termPairs.contains(term1 + glue + term2);
    }
    
    /**
     * Apply an action to each term pair occuring in this set
     * @param etpa The action to be performed in the doAction method of etpa */
    public void forEachPair(EachTermPairAction etpa) {
        Iterator<String> i = termPairs.iterator();
        while(i.hasNext()) {
            String s = i.next();
            String []ss = s.split(glue);
            etpa.doAction(ss[0],ss[1]);
        }
    }
    
    /**
     * Apply an action to each term pair whose left hand side is term1. If you have a choice
     * this function is faster than forEachLHS.
     * @param etpa The action to be performed in the doAction method of etpa, the term1
     * passed to etpa.doAction(String,String) will be the same as the term1 passed to this
     * method */
    public void forEachRHS(String term1, EachTermPairAction etpa) {
        Iterator<String> i = termPairs.subSet(term1 + " => \0", true, term1 + " =>~",true).iterator();
        while(i.hasNext()) {
            String s = i.next();
            String []ss = s.split(glue);
            etpa.doAction(term1,ss[1]);
        }
    }
    
    /**
     * Apply an action to each term pair whose right hand side is term2. For speed forEachRHS 
     * is generally prefered.
     * @param etpa The action to be performed in the doAction method of etpa, the term2
     * passed to etpa.doAction(String,String) will be the same as the term2 passed to this
     * method */
    public void forEachLHS(String term2, EachTermPairAction etpa) {
        Iterator<String> i = termPairs.iterator();
        while(i.hasNext()) {
            String s = i.next();
            String []ss = s.split(glue);
            if(ss[1].equals(term2)) {
                etpa.doAction(ss[0],ss[1]);
            }
        }
    }
    
    /** number of elements in set */
    public int size() {
        return termPairs.size();
    }
}
