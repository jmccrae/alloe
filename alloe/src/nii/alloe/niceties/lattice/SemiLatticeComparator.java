package nii.alloe.niceties.lattice;
import java.util.*;

/**
 * A comparator for the SemiLattice object. This extends comparator, that is
 * a comparator that 
 *
 * @author John McCrae, National Institute of Informatics
 */
public interface SemiLatticeComparator<E>  {
    public boolean isLessThan(E e1, E e2);
    public E join(E e1, E e2);
}
