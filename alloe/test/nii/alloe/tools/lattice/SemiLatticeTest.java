/*
 * SemiLatticeTest.java
 * JUnit based test
 *
 * Created on 13 March 2008, 17:19
 */

package nii.alloe.tools.lattice;

import junit.framework.*;
import java.util.*;

/**
 *
 * @author john
 */
public class SemiLatticeTest extends TestCase {
    
    SemiLattice instance;
    
    public SemiLatticeTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        instance = new SemiLattice<TreeSet<Integer>>(new SetSemiLatticeComparator());
        Random r = new Random();
        TreeSet<Integer> e = new TreeSet<Integer>();
        e.add(2);
        e.add(3);
        instance.add(e);
        e = new TreeSet<Integer>();
        e.add(1);
        instance.add(e);
        e = new TreeSet<Integer>();
        e.add(0);
        e.add(1);
        e.add(3);
        e.add(4);
        instance.add(e);
        e = new TreeSet<Integer>();
        e.add(3);
        e.add(4);
        instance.add(e);
        e = new TreeSet<Integer>();
        e.add(0);
        e.add(1);
        e.add(3);
        instance.add(e);
        e = new TreeSet<Integer>();
        e.add(0);
        e.add(1);
        e.add(2);
        instance.add(e);
        e = new TreeSet<Integer>();
        e.add(1);
        e.add(2);
        instance.add(e);
        e = new TreeSet<Integer>();
        e.add(0);
        e.add(2);
        instance.add(e);
        e = new TreeSet<Integer>();
        e.add(0);
        e.add(1);
        e.add(2);
        e.add(4);
        instance.add(e);
        e = new TreeSet<Integer>();
        e.add(1);
        e.add(3);
        e.add(4);
        instance.add(e);
    }
    
    protected void tearDown() throws Exception {
    }
    
    /**
     * Test of iterator method, of class nii.alloe.niceties.lattice.SemiLattice.
     */
    public void testIterator() {
        System.out.println("iterator");
        
    }
    
    /**
     * Test of getInfimumSet method, of class nii.alloe.niceties.lattice.SemiLattice.
     */
    public void testGetInfimumSet() {
        System.out.println("getInfimumSet");
        
        
        Collection<TreeSet<Integer>> result = instance.getInfimumSet();
        for(TreeSet<Integer> sets : result) {
            System.out.println(sets.toString());
        }
        assertEquals(result.size(),3);
        
    }
    
    /**
     * Test of size method, of class nii.alloe.niceties.lattice.SemiLattice.
     */
    public void testSize() {
        System.out.println("size");
        
        int expResult = 7;
        int result = instance.size();
        assertEquals(expResult, result);
    }
    
    /**
     * Test of add method, of class nii.alloe.niceties.lattice.SemiLattice.
     */
    public void testAdd() {
        System.out.println("add");
        
        TreeSet<Integer> e = new TreeSet<Integer>();
        e.add(0);
        e.add(1);
        e.add(3);
        
        boolean expResult = false;
        boolean result = instance.add(e);
        assertEquals(expResult, result);
    }
    
    /**
     * Test of isEmpty method, of class nii.alloe.niceties.lattice.SemiLattice.
     */
    public void testIsEmpty() {
        System.out.println("isEmpty");
        
        boolean expResult = false;
        boolean result = instance.isEmpty();
        assertEquals(expResult, result);
    }
    
// Less than operator given by subset operator, supermum is empty set.
    private class SetSemiLatticeComparator implements SemiLatticeComparator<TreeSet<Integer>> {
        public boolean isLessThan(TreeSet<Integer> e1, TreeSet<Integer> e2) {
            for(Integer i : e2) {
                if(!e1.contains(i))
                    return false;
            }
            return true;
        }
        
        public TreeSet<Integer> join(TreeSet<Integer> e1, TreeSet<Integer> e2) {
            TreeSet<Integer> rval = new TreeSet<Integer>(e1);
            rval.retainAll(e2);
            return rval;
        }
    }
}
