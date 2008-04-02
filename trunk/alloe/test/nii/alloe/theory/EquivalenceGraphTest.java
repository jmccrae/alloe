/*
 * EquivalenceGraphTest.java
 * JUnit based test
 *
 * Created on April 1, 2008, 11:04 PM
 */

package nii.alloe.theory;

import junit.framework.*;
import java.util.*;
import java.io.*;

/**
 *
 * @author john
 */
public class EquivalenceGraphTest extends TestCase {
    
    public EquivalenceGraphTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(EquivalenceGraphTest.class);
        
        return suite;
    }

    /**
     * Test of isConnected method, of class nii.alloe.theory.EquivalenceGraph.
     */
    public void testIsConnected() {
        System.out.println("isConnected");
        
        int i = 17;
        int j = 17;
        EquivalenceGraph instance = new EquivalenceGraph();
        
        boolean expResult = true;
        boolean result = instance.isConnected(i, j);
        assertEquals(expResult, result);
    }

    /**
     * Test of mutable method, of class nii.alloe.theory.EquivalenceGraph.
     */
    public void testMutable() {
        System.out.println("mutable");
        
        int i = 0;
        int j = 0;
        EquivalenceGraph instance = new EquivalenceGraph();
        
        boolean expResult = false;
        boolean result = instance.mutable(i, j);
        assertEquals(expResult, result);
    }

    /**
     * Test of add method, of class nii.alloe.theory.EquivalenceGraph.
     */
    public void testAdd() {
        System.out.println("add");
        
        int i = 0;
        int j = 0;
        EquivalenceGraph instance = new EquivalenceGraph();
        
        try {
            instance.add(i, j);
        } catch(UnsupportedOperationException x) {
            return;
        }
        fail();
    }

    /**
     * Test of remove method, of class nii.alloe.theory.EquivalenceGraph.
     */
    public void testRemove() {
        System.out.println("remove");
        
        int i = 0;
        int j = 0;
        EquivalenceGraph instance = new EquivalenceGraph();
        
        try {
            instance.remove(i, j);
         } catch(UnsupportedOperationException x) {
            return;
        }
        fail();  
    }

    /**
     * Test of linkCount method, of class nii.alloe.theory.EquivalenceGraph.
     */
    public void testLinkCount() {
        System.out.println("linkCount");
        
        EquivalenceGraph instance = new EquivalenceGraph();
        
        int expResult = 0;
        int result = instance.linkCount();
        assertEquals(expResult, result);
    }   
}
