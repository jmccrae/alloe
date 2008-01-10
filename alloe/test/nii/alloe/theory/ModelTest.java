/*
 * ModelTest.java
 * JUnit based test
 *
 * Created on November 28, 2007, 5:59 AM
 */

package nii.alloe.theory;

import junit.framework.*;
import java.util.*;

/**
 *
 * @author john
 */
public class ModelTest extends TestCase {
    
    public ModelTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
    }
    
    protected void tearDown() throws Exception {
    }
    
    /**
     * Test of addBasicGraphs method, of class nii.aloe.theory.Model.
     */
    public void testAddBasicGraphs() {
        System.out.println("addBasicGraphs");
        
        Logic l = new Logic("logics/rooted-hyp.logic");
        Model instance = new Model(10);
        
        instance.addBasicGraphs(l);
        
        if(!instance.graphs.containsKey("e") || !instance.graphs.containsKey("in_1")) {
            fail("Oh noes!");
        }
    }
    
    /**
     * Test of addSpecificGraph method, of class nii.aloe.theory.Model.
     */
    public void testAddSpecificGraph() {
        System.out.println("addSpecificGraph");
        
        String name = "r1";
        Model instance = new Model(10);
        
        
        SpecificGraph result = instance.addSpecificGraph(name);
        
        if(instance.graphs.get("r1") != result) {
            fail("Oh noes!");
        }
    }
    
    /**
     * Test of addProbabilityGraph method, of class nii.aloe.theory.Model.
     */
    public void testAddProbabilityGraph() {
        System.out.println("addProbabilityGraph");
        
        String name = "r1";
        Model instance = new Model(10);
        
        
        ProbabilityGraph result = instance.addProbabilityGraph(name);
        
        assertTrue(instance.graphs.get("r1") == result);
    }
    
    /**
     * Test of id method, of class nii.aloe.theory.Model.
     */
    public void testId() {
        System.out.println("id");
        
        String relation = "r1";
        int i = 5;
        int j = 6;
        Model instance = new Model(10);
        instance.addSpecificGraph("r1");
        
        int expResult = 56;
        int result = instance.id(relation, i, j);
        assertEquals(expResult, result);
    }
    
    /**
     * Test of relationByID method, of class nii.aloe.theory.Model.
     */
    public void testRelationByID() {
        System.out.println("relationByID");
        
        int id = 56;
        Model instance = new Model(10);
        instance.addSpecificGraph("r1");
        instance.addBasicGraphs(new Logic("logics/single-hyp.logic"));
        
        String expResult = "r1";
        String result = instance.relationByID(id);
        assertEquals(expResult, result);
        expResult = "e";
        result = instance.relationByID(156);
        assertEquals(expResult, result);
        
    }
    
    /**
     * Test of iByID method, of class nii.aloe.theory.Model.
     */
    public void testIByID() {
        System.out.println("iByID");
        
        int id = 56;
        Model instance = new Model(10);
        instance.addSpecificGraph("r1");
        
        int expResult = 5;
        int result = instance.iByID(id);
        assertEquals(expResult, result);
        
    }
    
    /**
     * Test of jByID method, of class nii.aloe.theory.Model.
     */
    public void testJByID() {
        System.out.println("jByID");
        
        int id = 56;
        Model instance = new Model(10);
        instance.addSpecificGraph("r1");
        
        int expResult = 6;
        int result = instance.jByID(id);
        assertEquals(expResult, result);
    }
    
    /**
     * Test of getGraphByID method, of class nii.aloe.theory.Model.
     */
    public void testGetGraphByID() {
        System.out.println("getGraphByID");
        
        int id = 56;
        Model instance = new Model(10);
        SpecificGraph graph = instance.addSpecificGraph("r1");
        
        Graph result = instance.getGraphByID(id);
        assertTrue(result == graph);
    }
    
    /**
     * Test of isConnected method, of class nii.aloe.theory.Model.
     */
    public void testIsConnected() {
        System.out.println("isConnected");
        
        Integer id = 56;
        Model instance = new Model(10);
        SpecificGraph graph = instance.addSpecificGraph("r1");
        graph.add(5,6);
        
        boolean expResult = true;
        boolean result = instance.isConnected(id);
        assertEquals(expResult, result);
        expResult = false;
        result = instance.isConnected(43);
        assertEquals(expResult, result);
    }
    
    /**
     * Test of mutable method, of class nii.aloe.theory.Model.
     */
    public void testMutable() {
        System.out.println("mutable");
        
        Integer id = 56;
        Model instance = new Model(10);
        instance.addSpecificGraph("r1");
        instance.addBasicGraphs(new Logic("logics/hypernym.logic"));
        
        boolean expResult = true;
        boolean result = instance.mutable(id);
        assertEquals(expResult, result);
        expResult = false;
        result = instance.mutable(156);
        assertEquals(expResult, result);
    }
    
    /**
     * Test of add method, of class nii.aloe.theory.Model.
     */
    public void testAdd() {
        System.out.println("add");
        
        Integer id = 56;
        Model instance = new Model(10);
        instance.addSpecificGraph("r1");
        
        instance.add(id);
        
        assertTrue(instance.isConnected(id));
    }
    
}
