package nii.alloe.tools.struct;
import java.util.*;

/**
 * Use toString() to compare objects;
 *
 * @author John McCrae, National Institute of Informatics
 */
public class ToStringComparator<E> implements Comparator<E> {
    
    /** Creates a new instance of ToStringComparator */
    public ToStringComparator() {
    }

    public int compare(E e1, E e2) {
        return e1.toString().compareTo(e2.toString());
    }
}
