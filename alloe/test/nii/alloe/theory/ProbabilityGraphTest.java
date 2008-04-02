/*
 * ProbabilityGraphTest.java
 * JUnit based test
 *
 * Created on April 1, 2008, 11:04 PM
 */

package nii.alloe.theory;

import junit.framework.*;
import java.util.*;
import java.io.*;
import nii.alloe.niceties.Output;

/**
 *
 * @author john
 */
public class ProbabilityGraphTest extends TestCase {
    
    public ProbabilityGraphTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(ProbabilityGraphTest.class);
        
        return suite;
    }

    /**
     * Test of isConnected method, of class nii.alloe.theory.ProbabilityGraph.
     */
    public void testIsConnected() {
        System.out.println("isConnected");
        
        int i = 0;
        int j = 0;
        ProbabilityGraph instance = null;
        
        boolean expResult = true;
        boolean result = instance.isConnected(i, j);
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of mutable method, of class nii.alloe.theory.ProbabilityGraph.
     */
    public void testMutable() {
        System.out.println("mutable");
        
        int i = 0;
        int j = 0;
        ProbabilityGraph instance = null;
        
        boolean expResult = true;
        boolean result = instance.mutable(i, j);
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of add method, of class nii.alloe.theory.ProbabilityGraph.
     */
    public void testAdd() {
        System.out.println("add");
        
        int i = 0;
        int j = 0;
        ProbabilityGraph instance = null;
        
        instance.add(i, j);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of remove method, of class nii.alloe.theory.ProbabilityGraph.
     */
    public void testRemove() {
        System.out.println("remove");
        
        int i = 0;
        int j = 0;
        ProbabilityGraph instance = null;
        
        instance.remove(i, j);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of linkCount method, of class nii.alloe.theory.ProbabilityGraph.
     */
    public void testLinkCount() {
        System.out.println("linkCount");
        
        ProbabilityGraph instance = null;
        
        int expResult = 0;
        int result = instance.linkCount();
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of dumpToDot method, of class nii.alloe.theory.ProbabilityGraph.
     */
    public void testDumpToDot() {
        System.out.println("dumpToDot");
        
        String dotFile = "";
        ProbabilityGraph instance = null;
        
        instance.dumpToDot(dotFile);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of createCopy method, of class nii.alloe.theory.ProbabilityGraph.
     */
    public void testCreateCopy() {
        System.out.println("createCopy");
        
        ProbabilityGraph instance = null;
        
        Graph expResult = null;
        Graph result = instance.createCopy();
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of posVal method, of class nii.alloe.theory.ProbabilityGraph.
     */
    public void testPosVal() {
        System.out.println("posVal");
        
        int i = 0;
        int j = 0;
        ProbabilityGraph instance = null;
        
        double expResult = 0.0;
        double result = instance.posVal(i, j);
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of negVal method, of class nii.alloe.theory.ProbabilityGraph.
     */
    public void testNegVal() {
        System.out.println("negVal");
        
        int i = 0;
        int j = 0;
        ProbabilityGraph instance = null;
        
        double expResult = 0.0;
        double result = instance.negVal(i, j);
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addVal method, of class nii.alloe.theory.ProbabilityGraph.
     */
    public void testAddVal() {
        System.out.println("addVal");
        
        int i = 0;
        int j = 0;
        ProbabilityGraph instance = null;
        
        double expResult = 0.0;
        double result = instance.addVal(i, j);
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of removeVal method, of class nii.alloe.theory.ProbabilityGraph.
     */
    public void testRemoveVal() {
        System.out.println("removeVal");
        
        int i = 0;
        int j = 0;
        ProbabilityGraph instance = null;
        
        double expResult = 0.0;
        double result = instance.removeVal(i, j);
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setPosVal method, of class nii.alloe.theory.ProbabilityGraph.
     */
    public void testSetPosVal() {
        System.out.println("setPosVal");
        
        int i = 0;
        int j = 0;
        double prob = 0.0;
        ProbabilityGraph instance = null;
        
        instance.setPosVal(i, j, prob);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setPosNegVal method, of class nii.alloe.theory.ProbabilityGraph.
     */
    public void testSetPosNegVal() {
        System.out.println("setPosNegVal");
        
        int i = 0;
        int j = 0;
        double p = 0.0;
        double ng = 0.0;
        ProbabilityGraph instance = null;
        
        instance.setPosNegVal(i, j, p, ng);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setBaseVal method, of class nii.alloe.theory.ProbabilityGraph.
     */
    public void testSetBaseVal() {
        System.out.println("setBaseVal");
        
        double prob = 0.0;
        ProbabilityGraph instance = null;
        
        instance.setBaseVal(prob);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of len method, of class nii.alloe.theory.ProbabilityGraph.
     */
    public void testLen() {
        System.out.println("len");
        
        ProbabilityGraph instance = null;
        
        int expResult = 0;
        int result = instance.len();
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of writeToFile method, of class nii.alloe.theory.ProbabilityGraph.
     */
    public void testWriteToFile() {
        System.out.println("writeToFile");
        
        String out_fname = "";
        Graph g = null;
        ProbabilityGraph instance = null;
        
        instance.writeToFile(out_fname, g);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of iterator method, of class nii.alloe.theory.ProbabilityGraph.
     */
    public void testIterator() {
        System.out.println("iterator");
        
        int n = 0;
        ProbabilityGraph instance = null;
        
        Iterator<Integer> expResult = null;
        Iterator<Integer> result = instance.iterator(n);
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
