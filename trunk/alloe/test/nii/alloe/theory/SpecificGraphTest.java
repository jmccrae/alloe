/*
 * SpecificGraphTest.java
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
public class SpecificGraphTest extends TestCase {
    
    public SpecificGraphTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(SpecificGraphTest.class);
        
        return suite;
    }

    /**
     * Test of makeRandom method, of class nii.alloe.theory.SpecificGraph.
     */
    public void testMakeRandom() {
        System.out.println("makeRandom (note this test fails 0.01% of the time)");
        
        double prob = 0.5;
        SpecificGraph instance = new SpecificGraph(100,"r");
        
        instance.makeRandom(prob);
        
        
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of mutable method, of class nii.alloe.theory.SpecificGraph.
     */
    public void testMutable() {
        System.out.println("mutable");
        
        int i = 0;
        int j = 0;
        SpecificGraph instance = null;
        
        boolean expResult = true;
        boolean result = instance.mutable(i, j);
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isConnected method, of class nii.alloe.theory.SpecificGraph.
     */
    public void testIsConnected() {
        System.out.println("isConnected");
        
        int i = 0;
        int j = 0;
        SpecificGraph instance = null;
        
        boolean expResult = true;
        boolean result = instance.isConnected(i, j);
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of add method, of class nii.alloe.theory.SpecificGraph.
     */
    public void testAdd() {
        System.out.println("add");
        
        int i = 0;
        int j = 0;
        SpecificGraph instance = null;
        
        instance.add(i, j);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of remove method, of class nii.alloe.theory.SpecificGraph.
     */
    public void testRemove() {
        System.out.println("remove");
        
        int i = 0;
        int j = 0;
        SpecificGraph instance = null;
        
        instance.remove(i, j);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of linkCount method, of class nii.alloe.theory.SpecificGraph.
     */
    public void testLinkCount() {
        System.out.println("linkCount");
        
        SpecificGraph instance = null;
        
        int expResult = 0;
        int result = instance.linkCount();
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of dumpToDot method, of class nii.alloe.theory.SpecificGraph.
     */
    public void testDumpToDot() {
        System.out.println("dumpToDot");
        
        String dotFile = "";
        SpecificGraph instance = null;
        
        instance.dumpToDot(dotFile);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of iterator method, of class nii.alloe.theory.SpecificGraph.
     */
    public void testIterator() {
        System.out.println("iterator");
        
        int n = 0;
        SpecificGraph instance = null;
        
        Iterator<Integer> expResult = null;
        Iterator<Integer> result = instance.iterator(n);
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of createCopy method, of class nii.alloe.theory.SpecificGraph.
     */
    public void testCreateCopy() {
        System.out.println("createCopy");
        
        SpecificGraph instance = null;
        
        Graph expResult = null;
        Graph result = instance.createCopy();
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
