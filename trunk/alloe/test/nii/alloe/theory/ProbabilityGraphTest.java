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
import nii.alloe.tools.process.Output;

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
        ProbabilityGraph instance = new ProbabilityGraph(4);
        instance.setVal(0, 0, .9);
        instance.setVal(0, 1, .1);
        
        boolean expResult = true;
        boolean result = instance.isConnected(i, j);
        assertEquals(expResult, result);
        assertEquals(instance.isConnected(0,1),false);
    }

    /**
     * Test of mutable method, of class nii.alloe.theory.ProbabilityGraph.
     */
    public void testMutable() {
        System.out.println("mutable");
        
        int i = 0;
        int j = 0;
        ProbabilityGraph instance = new ProbabilityGraph(4);
        
        boolean expResult = true;
        boolean result = instance.mutable(i, j);
        assertEquals(expResult, result);
    }

    /**
     * Test of add method, of class nii.alloe.theory.ProbabilityGraph.
     */
    public void testAdd() {
        System.out.println("add");
        
        int i = 0;
        int j = 0;
        ProbabilityGraph instance = new ProbabilityGraph(4);
        
        instance.add(i, j);
    }

    /**
     * Test of remove method, of class nii.alloe.theory.ProbabilityGraph.
     */
    public void testRemove() {
        System.out.println("remove");
        
        int i = 0;
        int j = 0;
        ProbabilityGraph instance = new ProbabilityGraph(4);
        
        instance.remove(i, j);
    }

    /**
     * Test of linkCount method, of class nii.alloe.theory.ProbabilityGraph.
     */
    public void testLinkCount() {
        System.out.println("linkCount");
        
        ProbabilityGraph instance = new ProbabilityGraph(4);
        instance.setVal(0, 0, .9);
        instance.setVal(0, 1, .1);
        int expResult = 1;
        int result = instance.linkCount();
        assertEquals(expResult, result);
    }

    /**
     * Test of dumpToDot method, of class nii.alloe.theory.ProbabilityGraph.
     */
    public void testDumpToDot() {
        System.out.println("dumpToDot (no test)");
    }

    /**
     * Test of createCopy method, of class nii.alloe.theory.ProbabilityGraph.
     */
    public void testCreateCopy() {
        System.out.println("createCopy (no test)");
    }

    /**
     * Test of posVal method, of class nii.alloe.theory.ProbabilityGraph.
     */
    public void testPosVal() {
        System.out.println("posVal");
        
        int i = 1;
        int j = 3;
        ProbabilityGraph instance = new ProbabilityGraph(4);
        double expResult = 0.9;
        instance.setVal(i, j, expResult);
        
        double result = instance.posVal(i, j);
        assertEquals(expResult, result);
    }

    /**
     * Test of negVal method, of class nii.alloe.theory.ProbabilityGraph.
     */
    public void testNegVal() {
        System.out.println("negVal");

    }

    /**
     * Test of addVal method, of class nii.alloe.theory.ProbabilityGraph.
     */
    public void testAddVal() {
        System.out.println("addVal");

    }

    /**
     * Test of removeVal method, of class nii.alloe.theory.ProbabilityGraph.
     */
    public void testRemoveVal() {
        System.out.println("removeVal");

    }

    /**
     * Test of setPosVal method, of class nii.alloe.theory.ProbabilityGraph.
     */
    public void testSetVal() {
        System.out.println("setVal");
 
    }

    /**
     * Test of setPosNegVal method, of class nii.alloe.theory.ProbabilityGraph.
     */
    public void testSetPosNegVal() {
        System.out.println("setPosNegVal");

    }

    /**
     * Test of setBaseVal method, of class nii.alloe.theory.ProbabilityGraph.
     */
    public void testSetBaseVal() {
        System.out.println("setBaseVal");

    }
}
