package nii.alloe.niceties;
import java.util.*;

/**
 * Implements an iterator which combines a number of other iterators.
 *
 * @author John McCrae, National Institute of Informatics
 */
public class MultiIterator<E> implements Iterator<E> {
    private Iterator<E> currentIter;
    private Iterator<E> lastIter;
    private Iterator<Iterator<E>> allIters;
    private int iterNumber;
    
    protected MultiIterator() {}
    
    /** Creates a new instance of MultiIterator */
    public MultiIterator(Iterator<Iterator<E>> allIterators) {
        init(allIterators);
    }
    
    /** Same as the constructor but allows for easier inheritance */
    protected void init(Iterator<Iterator<E>> allIterators) {    
        allIters = allIterators;
        currentIter = allIters.hasNext() ? allIters.next() : null;
        iterNumber = 0;
        lastIter = null;
    }
    

    public boolean hasNext() {
        if(currentIter == null)
            return false;
        if(currentIter.hasNext())
            return true;
        if(!allIters.hasNext())
            return false;
        lastIter = currentIter;
        do {
            currentIter = allIters.next();
            iterNumber++;
        } while(!currentIter.hasNext() && allIters.hasNext());
        return currentIter.hasNext();
    }

    public E next() {
        lastIter = null;
        if(currentIter.hasNext())
            return returnVal(currentIter.next(),iterNumber);
        do {
            currentIter = allIters.next();
            iterNumber++;
        } while(!currentIter.hasNext() && allIters.hasNext());
        if(currentIter.hasNext()) 
            return returnVal(currentIter.next(),iterNumber);
        else
            throw new NoSuchElementException();
    }

    /** Override this to change the value based on which iterator it comes from
     * @param iterNumber This will be zero for the first iterator, 1 for the next etc.
     */
    protected E returnVal(E e, int iterNumber) { return e; }
    
    public void remove() {
        if(currentIter == null)
            throw new IllegalStateException();
        if(lastIter == null)
            currentIter.remove();
        else
            lastIter.remove();
    }
}
