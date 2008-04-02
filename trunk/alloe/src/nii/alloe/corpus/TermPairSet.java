package nii.alloe.corpus;
import java.io.*;
import java.util.*;
import nii.alloe.niceties.*;

/**
 * A set of term pairs. Implemented via a tree set on the string "term1 => term2"
 *
 * @author John McCrae, National Institute of Informatics
 */
public class TermPairSet extends AbstractCollection<String[]> implements Serializable {
    
    private transient TreeSet<String> termPairs;
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
     * Similar to {@link #forEachPair(EachTermPairAction)} but allows the action to be stopped
     * and resumed at will
     * @param etpa The action to be performed in the doAction method of etpa
     * @param from null to start a new search otherwise, use this value to restart the search
     * @param signal A variable that can be polled to check if this process should be paused
     * @return the next value to be passed to from to resume, null is action complete
     */
    public String forEachPair(EachTermPairAction etpa, String from, PauseSignal signal) {
        Iterator<String> i;
        if(from != null) {
            i = termPairs.tailSet(from).iterator();
        } else {
            i = termPairs.iterator();
        }
        while(i.hasNext() && !signal.shouldPause()) {
            String s = i.next();
            String []ss = s.split(glue);
            etpa.doAction(ss[0],ss[1]);
        }
        return i.hasNext() ? i.next() : null;
    }
    
    /**
     * Get the progress of a forEachPair action
     * @return the ratio of term pairs done over all term pairs */
    public double getForEachPairProgress(String term1, String term2) {
        return getForEachPairProgress(term1 + glue + term2);
    }
    
    /**
     * Get the progress of a forEachPair action
     * @param v Internal representation of a term pair, for example return value of 
     * {@link #forEachPair(EachTermPairAction,String,PauseSignal}
     * @return the ratio of term pairs done over all term pairs */
     public double getForEachPairProgress(String v) {
        return (double)termPairs.headSet(v).size() / (double)termPairs.size();
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
    
    // TODO: Resumable/Progress monitors for forEachRHS&LHS
    
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
    
    private void writeObject(ObjectOutputStream oos) throws IOException  {
        oos.defaultWriteObject();
        oos.writeObject(new Vector<String>(termPairs));
    }
    
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        termPairs = new TreeSet<String>(new Comparator<String>() {
            public int compare(String s1, String s2) {
                return s1.toLowerCase().compareTo(s2.toLowerCase());
            }
            public boolean equals(Object object) {
               if(this == object) return true; return false;
            }
        });
        termPairs.addAll((Vector<String>)ois.readObject());
    }

    public Iterator<String[]> iterator() {
        return new TPSIterator();
    }
    
    private class TPSIterator implements Iterator<String[]> {
        Iterator<String> iter;
        public TPSIterator() { iter = termPairs.iterator(); }
        public String[] next() { return iter.next().split(glue); }
        public boolean hasNext() { return iter.hasNext(); }
        public void remove() { iter.remove(); }
    }

    public boolean add(String[] ss) {
        if(ss.length != 2)
            throw new IllegalArgumentException();
        return add(ss[0],ss[1]);
    }
}
