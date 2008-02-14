package nii.alloe.niceties;
import java.util.*;

/**
 * Creates a comparator with reverse order to a given comparator
 *
 * @author John McCrae, National Institute of Informatics
 */
public class InverseComparator<E> implements Comparator<E> {
    private Comparator<E> comp;
    /** Creates a new instance of InverseComparator */
    public InverseComparator(Comparator<E> comp) {
        this.comp = comp;
    }
    
    public int compare(E e1, E e2) {
        return -comp.compare(e1,e2);
    }
    
}
