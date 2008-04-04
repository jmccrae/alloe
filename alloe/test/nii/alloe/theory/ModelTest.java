/*
 * ModelTest.java
 * JUnit based test
 *
 * Created on November 28, 2007, 5:59 AM
 */

package nii.alloe.theory;

import junit.framework.*;
import java.util.*;
import java.io.*;
import java.io.Serializable;
import nii.alloe.corpus.*;

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
        Logic l;
        try {
            l = new Logic(new File("logics/rooted-hyp.logic"));
            } catch(IOException x) {
            x.printStackTrace();
            System.exit(-1);
            return;
        }
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
        try {
        instance.addBasicGraphs(new Logic(new File("logics/single-hyp.logic")));
        } catch(IOException x) {
            x.printStackTrace();
            System.exit(-1);
            return;
        }
        
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
        try {
        instance.addBasicGraphs(new Logic(new File("logics/hypernym.logic")));
        } catch(IOException x) {
            x.printStackTrace();
            System.exit(-1);
            return;
        }
        
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

    public static Test suite() {
        TestSuite suite = new TestSuite(ModelTest.class);
        
        return suite;
    }

    /**
     * Test of subModel method, of class nii.alloe.theory.Model.
     */
    public void testSubModel() {
        System.out.println("subModel");
        
        TreeSet<Integer> rels = new TreeSet<Integer>();
        rels.add(12);
        rels.add(34);
        Model instance = new Model(10);
        Graph g  = instance.addSpecificGraph("r1");
        g.add(1,2);
        g.add(7,8);
        g.add(3,4);
        
        Model result = instance.subModel(rels);
        assertEquals(2, result.size());
    }

    /**
     * Test of createSpecificCopy method, of class nii.alloe.theory.Model.
     */
    public void testCreateSpecificCopy() {
        System.out.println("createSpecificCopy");
        
        Model instance = null;
        
        Model expResult = null;
        Model result = instance.createSpecificCopy();
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of createProbabilityCopy method, of class nii.alloe.theory.Model.
     */
    public void testCreateProbabilityCopy() {
        System.out.println("createProbabilityCopy");
        
        double posProb = 0.0;
        double negProb = 0.0;
        Model instance = null;
        
        Model expResult = null;
        Model result = instance.createProbabilityCopy(posProb, negProb);
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getCompulsoryCount method, of class nii.alloe.theory.Model.
     */
    public void testGetCompulsoryCount() {
        System.out.println("getCompulsoryCount");
        
        Model instance = null;
        
        int expResult = 0;
        int result = instance.getCompulsoryCount();
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of createCopy method, of class nii.alloe.theory.Model.
     */
    public void testCreateCopy() {
        System.out.println("createCopy");
        
        Model instance = null;
        
        Model expResult = null;
        Model result = instance.createCopy();
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of createBlankSpecificCopy method, of class nii.alloe.theory.Model.
     */
    public void testCreateBlankSpecificCopy() {
        System.out.println("createBlankSpecificCopy");
        
        Model instance = null;
        
        Model expResult = null;
        Model result = instance.createBlankSpecificCopy();
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setGraphAs method, of class nii.alloe.theory.Model.
     */
    public void testSetGraphAs() {
        System.out.println("setGraphAs");
        
        String name = "";
        TermPairSet termPairs = null;
        TermList termList = null;
        Model instance = null;
        
        instance.setGraphAs(name, termPairs, termList);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of createImmutableCopy method, of class nii.alloe.theory.Model.
     */
    public void testCreateImmutableCopy() {
        System.out.println("createImmutableCopy");
        
        Model instance = null;
        
        Model expResult = null;
        Model result = instance.createImmutableCopy();
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of remove method, of class nii.alloe.theory.Model.
     */
    public void testRemove() {
        System.out.println("remove");
        
        Model m = null;
        Model instance = null;
        
        boolean expResult = true;
        boolean result = instance.remove(m);
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of containsAny method, of class nii.alloe.theory.Model.
     */
    public void testContainsAny() {
        System.out.println("containsAny");
        
        Collection<Integer> ids = null;
        Model instance = null;
        
        boolean expResult = true;
        boolean result = instance.containsAny(ids);
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of symmDiffAll method, of class nii.alloe.theory.Model.
     */
    public void testSymmDiffAll() {
        System.out.println("symmDiffAll");
        
        Collection<Integer> c = null;
        Model instance = null;
        
        instance.symmDiffAll(c);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of computeComparison method, of class nii.alloe.theory.Model.
     */
    public void testComputeComparison() {
        System.out.println("computeComparison");
        
        Model m = null;
        Model instance = null;
        
        int[] expResult = null;
        int[] result = instance.computeComparison(m);
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of size method, of class nii.alloe.theory.Model.
     */
    public void testSize() {
        System.out.println("size");
        
        Model instance = null;
        
        int expResult = 0;
        int result = instance.size();
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getGraphIDByName method, of class nii.alloe.theory.Model.
     */
    public void testGetGraphIDByName() {
        System.out.println("getGraphIDByName");
        
        String name = "";
        Model instance = null;
        
        int expResult = 0;
        int result = instance.getGraphIDByName(name);
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getGraphByName method, of class nii.alloe.theory.Model.
     */
    public void testGetGraphByName() {
        System.out.println("getGraphByName");
        
        String name = "";
        Model instance = null;
        
        Graph expResult = null;
        Graph result = instance.getGraphByName(name);
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getGraphNames method, of class nii.alloe.theory.Model.
     */
    public void testGetGraphNames() {
        System.out.println("getGraphNames");
        
        Model instance = null;
        
        Vector<String> expResult = null;
        Vector<String> result = instance.getGraphNames();
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addCompulsorys method, of class nii.alloe.theory.Model.
     */
    public void testAddCompulsorys() {
        System.out.println("addCompulsorys");
        
        Logic logic = null;
        Model instance = null;
        
        instance.addCompulsorys(logic);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of graphNameIterator method, of class nii.alloe.theory.Model.
     */
    public void testGraphNameIterator() {
        System.out.println("graphNameIterator");
        
        Model instance = null;
        
        Iterator<String> expResult = null;
        Iterator<String> result = instance.graphNameIterator();
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of iterator method, of class nii.alloe.theory.Model.
     */
    public void testIterator() {
        System.out.println("iterator");
        
        Model instance = null;
        
        Iterator<Integer> expResult = null;
        Iterator<Integer> result = instance.iterator();
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
