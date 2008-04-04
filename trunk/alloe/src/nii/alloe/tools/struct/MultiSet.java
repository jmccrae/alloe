package nii.alloe.tools.struct;
import java.util.*;

/**
 * Implements the idea of a multi-set, that is a set where elements can have 
 * several different values. I'm not sure how well this works
 * but the basic idea is that it is a TreeSet which converts an comparator to a
 * version where equality only exists for identical objects.
 *
 * @author John McCrae, National Institute of Informatics
 */
public class MultiSet<E extends Comparable> extends TreeSet<E> {
    
    /** Creates a new instance of SortedSet */
    public MultiSet() {
        super(new StandardComparator<E>());
    }
    
    public MultiSet(Collection<? extends E> c) {
        super(new StandardComparator<E>());
        addAll(c);
    }
    
    public MultiSet(Comparator<? extends E> c) {
        super(new DerivedComparator(c));
    }
    
    public MultiSet(SortedSet<? extends E> c) {
        super(new DerivedComparator(c.comparator()));
        addAll(c);
    }
}

class StandardComparator<E extends Comparable> implements Comparator<E> {
    public int compare(E o1, E o2) {
        if(o1 == o2)
            return 0;
        if(o1.compareTo(o2) > 0)
            return 1;
        else
            return -1;
    }   
}

class DerivedComparator<E> implements Comparator<E> {
    Comparator<E> base;
    public DerivedComparator(Comparator<E> base) {
        this.base = base;
    }

    public int compare(E o1, E o2) {
        if(o1 == o2)
            return 0;
        else if(base.compare(o1,o2) > 0)
            return 1;
        else
            return -1;
    }

}

