/*
 * MembershipGraphTest.java
 * JUnit based test
 *
 * Created on April 1, 2008, 11:04 PM
 */

package nii.alloe.theory;

import junit.framework.*;
import java.util.*;
import java.io.Serializable;

/**
 *
 * @author john
 */
public class MembershipGraphTest extends TestCase {
    
    TreeSet<Integer> set;
    
    public MembershipGraphTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        set = new TreeSet<Integer>();
        set.add(0);
        set.add(1);
        set.add(3);
    }

    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(MembershipGraphTest.class);
        
        return suite;
    }

    /**
     * Test of isConnected method, of class nii.alloe.theory.MembershipGraph.
     */
    public void testIsConnected() {
        System.out.println("isConnected");
        
        int i = 0;
        int j = 0;
        
        MembershipGraph instance = new MembershipGraph(set);
        
        boolean expResult = true;
        boolean result = instance.isConnected(i, j);
        assertEquals(expResult, result);
        assertEquals(false, instance.isConnected(2,0));
    }

    /**
     * Test of mutable method, of class nii.alloe.theory.MembershipGraph.
     */
    public void testMutable() {
        System.out.println("mutable");
        
        int i = 0;
        int j = 0;
        MembershipGraph instance = new MembershipGraph(set);
        
        boolean expResult = false;
        boolean result = instance.mutable(i, j);
        assertEquals(expResult, result);
    }

    /**
     * Test of add method, of class nii.alloe.theory.MembershipGraph.
     */
    public void testAdd() {
        System.out.println("add");
        
        int i = 0;
        int j = 0;
        MembershipGraph instance = new MembershipGraph(set);
        try {
            instance.add(i, j);
        } catch(UnsupportedOperationException x) {
            return;
        }
        fail();
    }

    /**
     * Test of remove method, of class nii.alloe.theory.MembershipGraph.
     */
    public void testRemove() {
        System.out.println("remove");
        
        int i = 0;
        int j = 0;
        MembershipGraph instance = null;
        
        try {
            instance.remove(i, j);
        } catch(UnsupportedOperationException x) {
            return;
        }
        fail();
    }

    /**
     * Test of linkCount method, of class nii.alloe.theory.MembershipGraph.
     */
    public void testLinkCount() {
        System.out.println("linkCount");
        
        MembershipGraph instance = null;
        
        int expResult = 3;
        int result = instance.linkCount();
        assertEquals(expResult, result);
        
    }
}
