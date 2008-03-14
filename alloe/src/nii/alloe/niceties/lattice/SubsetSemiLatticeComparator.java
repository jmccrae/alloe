package nii.alloe.niceties.lattice;
import java.util.*;

/**
 *
 * @author John McCrae, National Institute of Informatics
 */
public class SubsetSemiLatticeComparator<E> implements SemiLatticeComparator<TreeSet<E>> {
    public boolean isLessThan(TreeSet<E> e1, TreeSet<E> e2) {
        for(E i : e2) {
            if(!e1.contains(i))
                return false;
        }
        return true;
    }
    
    public TreeSet<E> join(TreeSet<E> e1, TreeSet<E> e2) {
        TreeSet<E> rval = new TreeSet<E>(e1);
        rval.retainAll(e2);
        return rval;
    }
}
