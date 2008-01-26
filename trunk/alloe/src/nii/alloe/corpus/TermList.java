package nii.alloe.corpus;
import java.util.*;
import nii.alloe.corpus.pattern.*;

/**
 *
 * @author John McCrae, National Institute of Informatics
 */
public class TermList extends Vector<String> {
    
    /** Creates a new instance of TermList */
    public TermList() {
        super();
    }
    
    public TermList(Collection<String> c){
        super();
        addAll(c);
    }
    
    public TermList(int initialCapacity) {
        super(initialCapacity);
    }
    
    public TermList(int initialCapacity, int capacityIncrement) {
        super(initialCapacity,capacityIncrement);
    }
    
    public boolean add(String s) {
        return super.add(Pattern.makeSafe(s));
    }
    
    public void add(int index, String s) {
        super.add(index, Pattern.makeSafe(s));
    }
    
    public void addElement(String s) {
        super.addElement(Pattern.makeSafe(s));
    }

    public void addAll(Collection<String> ss) {
        Iterator<String> iter = ss.iterator();
        while(iter.hasNext()) {
            add(iter.next());
        }
    }
    
    public boolean contains(String s) {
        return super.contains(Pattern.makeSafe(s));
    }

    public int indexOf(String s) {
        return super.indexOf(Pattern.makeSafe(s));
    }
    
    public int indexOf(String s, int i) {
        return super.indexOf(Pattern.makeSafe(s),i);
    }
    

    
}
