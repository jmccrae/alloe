package nii.alloe.niceties.lattice;
import java.util.*;

/**
 * A SemiLattice is a partially-ordered set, where each pair of elements
 * has a unique least upper bound. This is implemented here by having a
 * structure which stores only the infimums of the lattice. This means that
 * certain elements cannot be added to certain structure (if they are greater
 * than some element and not deducable by some join).
 *
 * @author John McCrae, National Institute of Informatics
 */
public class SemiLattice<E> extends AbstractCollection<E> {
    private LinkedList<E> infimums;
    private SemiLatticeComparator<E> comp;
    private int size;
    
    /** Creates a new instance of SemiLattice */
    public SemiLattice(SemiLatticeComparator<E> comp) {
        this.comp =  comp;
        infimums = new LinkedList<E>();
        size = 0;
    }
    
    /** Iterates on every element in this lattice that can be found by
     * application of the join operation. This is potentially very slow */
    public Iterator<E> iterator() {
        return new SemiLatticeIterator();
    }
    
    /** Get the infimums in this semi-lattice */
    public Collection<E> getInfimumSet() {
        return infimums;
    }
    
    /* Return the size of this lattice. Potentially very, very slow */
    public int size() {
        int i = 0;
        for(E e : this)
            i++;
        return i;
    }
    
    /* Add a new element to the lattice. Note that this new element will not
     * be added if it is not an infimum or a join of existing operators */
    public boolean add(E e) {
        Iterator<E> iter = infimums.iterator();
        while(iter.hasNext()) {
            E e2 = iter.next();
            if(comp.isLessThan(e2,e))
                return false;
            if(comp.isLessThan(e,e2))
                iter.remove();
            if(e2.equals(e))
                return false;
        }
        infimums.add(e);
        return true;
    }
    
    public boolean remove(Object object) {
        return infimums.remove(object);
    }

    public boolean contains(Object object) {
        for(E e : infimums) {
            if(comp.isLessThan((E)object,e) || e.equals(object))
                return true;
        }
        return false;
    }
    
    public void clear() {
        infimums.clear();
    }
    
    public boolean isEmpty() {
        return infimums.isEmpty();
    }
    
    public String toString() { return infimums.toString(); }
    
    class SemiLatticeIterator implements Iterator<E> {
        LinkedList<E> elems;
        SemiLattice<E> nextLayer;
        E last;
        SemiLatticeIterator() {
            elems = new LinkedList<E>(infimums);
            nextLayer = new SemiLattice<E>(comp);
        }
        
        public boolean hasNext() {
            return !elems.isEmpty() || !nextLayer.isEmpty();
        }
        
        public E next() {
            if(elems.isEmpty() && !nextLayer.isEmpty()) {
                elems = new LinkedList<E>(nextLayer.infimums);
                nextLayer.clear();
            } else if(elems.isEmpty() && nextLayer.isEmpty())
                throw new NoSuchElementException();
            E rval = elems.pollFirst();
            for(E e : elems) {
                E newE = comp.join(rval,e);
                if(!nextLayer.contains(newE))
                    nextLayer.add(newE);
            }
            return (last = rval);
        }
        
        public void remove() {
            infimums.remove(last);
        }
    }
}
