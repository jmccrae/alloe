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
        l.setModelSize(10);
        Model instance = new Model(l);
        
        instance.addBasicGraphs(l);
        
        if(!instance.graphs.containsKey("e")) {
            fail("Oh noes!");
        }
    }
    
    /**
     * Test of addSpecificGraph method, of class nii.aloe.theory.Model.
     */
    public void testAddSpecificGraph() {
        System.out.println("addSpecificGraph");
        
        String name = "r1";
        Logic l = new Logic("");
        l.setModelSize(10);
        Model instance = new Model(l);
        
        
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
        Logic l = new Logic("");
        l.setModelSize(10);
        Model instance = new Model(l);
        
        
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
        Logic l = new Logic("");
        l.setModelSize(10);
        Model instance = new Model(l);
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
        Logic l = new Logic("");
        l.setModelSize(10);
        Model instance = new Model(l);
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
        Logic l = new Logic("");
        l.setModelSize(10);
        Model instance = new Model(l);
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
        Logic l = new Logic("");
        l.setModelSize(10);
        Model instance = new Model(l);
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
        Logic l = new Logic("");
        l.setModelSize(10);
        Model instance = new Model(l);
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
        Logic l = new Logic("");
        l.setModelSize(10);
        Model instance = new Model(l);
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
        Logic l = new Logic("");
        l.setModelSize(10);
        Model instance = new Model(l);
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
        Logic l = new Logic("");
        l.setModelSize(10);
        Model instance = new Model(l);
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
        Logic l = new Logic("");
        l.setModelSize(10);
        Model instance = new Model(l);
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
        
        Logic l = new Logic("r(1,1) ->");
        l.setModelSize(4);
        Model instance = new Model(l);
        instance.addBasicGraphs(l);
        Graph g = instance.addProbabilityGraph("r");
        g.add(0,1);
        g.add(2,3);
        g.add(1,2);
        
        Model expResult = instance;
        Model result = instance.createSpecificCopy();
        assertEquals(expResult, result);
    }

    /**
     * Test of createProbabilityCopy method, of class nii.alloe.theory.Model.
     */
    public void testCreateProbabilityCopy() {
        System.out.println("createProbabilityCopy");
        
         Logic l = new Logic("r(1,1) ->");
        l.setModelSize(4);
        Model instance = new Model(l);
        instance.addBasicGraphs(l);
        Graph g = instance.addSpecificGraph("r");
        g.add(0,1);
        g.add(2,3);
        g.add(1,2);
        
        Model expResult = instance;
        Model result = instance.createProbabilityCopy(0.9,0.1);
        assertEquals(expResult, result);
    }

    /**
     * Test of getCompulsoryCount method, of class nii.alloe.theory.Model.
     */
    public void testGetCompulsoryCount() {
        System.out.println("getCompulsoryCount");
        
        Logic l = new Logic("-> r(1,1)");
        l.setModelSize(4);
        Model instance = new Model(l);
        instance.addBasicGraphs(l);
        instance.addSpecificGraph("r");
        instance.addCompulsorys(l);
        
        int expResult = 4;
        int result = instance.getCompulsoryCount();
        assertEquals(expResult, result);
    }

    /**
     * Test of createCopy method, of class nii.alloe.theory.Model.
     */
    public void testCreateCopy() {
        System.out.println("createCopy");
        
       Logic l = new Logic("-> r(1,1)");
        l.setModelSize(4);
        Model instance = new Model(l);
        instance.addBasicGraphs(l);
      
        
        Model expResult = instance;
        Model result = instance.createCopy();
        assertEquals(expResult, result);

    }

    /**
     * Test of createBlankSpecificCopy method, of class nii.alloe.theory.Model.
     */
    public void testCreateBlankSpecificCopy() {
        System.out.println("createBlankSpecificCopy");
        
         Logic l = new Logic("-> r(1,1)");
        l.setModelSize(4);
        Model instance = new Model(l);
        instance.addBasicGraphs(l);
      
        
        Model expResult = instance;
        Model result = instance.createCopy();
        assertEquals(expResult, result);

    }

    /**
     * Test of setGraphAs method, of class nii.alloe.theory.Model.
     */
    public void testSetGraphAs() {
        System.out.println("setGraphAs");
        
        Logic l = new Logic("r(1,1) ->");
        l.setModelSize(4);
        String name = "r";
        TermPairSet termPairs = new TermPairSet();
        termPairs.add("term1","term2");
        termPairs.add("term2","term3");
        TermList termList = new TermList();
        termList.add("term1");
        termList.add("term2");
        termList.add("term3");
        Model instance = new Model(l);
        
        instance.setGraphAs(name, termPairs, termList);
        assertTrue(instance.isConnected(instance.id("r",0,1)));
    }

    /**
     * Test of createImmutableCopy method, of class nii.alloe.theory.Model.
     */
    public void testCreateImmutableCopy() {
        System.out.println("createImmutableCopy");
        
        Logic l = new Logic("-> r(1,1)");
        l.setModelSize(4);
        Model instance = new Model(l);
        instance.addBasicGraphs(l);
        instance.addSpecificGraph("r");
      
        
        Model expResult = instance;
        Model result = instance.createCopy();
        assertEquals(expResult, result);
        assertTrue(instance.mutable(instance.id("r",1,2)));
    }


     /**
     * Test of containsAny method, of class nii.alloe.theory.Model.
     */
    public void testContainsAny() {
        System.out.println("containsAny");
        
        Collection<Integer> ids = new LinkedList<Integer>();
        Logic l = new Logic("-> r(1,1)");
        l.setModelSize(4);
        Model instance = new Model(l);
        instance.addBasicGraphs(l);
        Graph g = instance.addSpecificGraph("r");
        g.add(1,2);
        ids.add(instance.id("r", 1,2));
        ids.add(instance.id("r",0,2));
       
        
        boolean expResult = true;
        boolean result = instance.containsAny(ids);
        assertEquals(expResult, result);
    }

    /**
     * Test of symmDiffAll method, of class nii.alloe.theory.Model.
     */
    public void testSymmDiffAll() {
        System.out.println("symmDiffAll");
        
        Collection<Integer> c = new TreeSet<Integer>();
        Logic l = new Logic("-> r(1,1)");
        l.setModelSize(4);
        Model instance = new Model(l);
        instance.addBasicGraphs(l);
        Graph g = instance.addSpecificGraph("r");
        g.add(1,2);
        g.add(2,3);
        g.add(0,2);
             
        c.add(instance.id("r",1,2));
        c.add(instance.id("r",2,0));
        
        instance.symmDiffAll(c);
        
        c.add(instance.id("r",2,3));
        c.add(instance.id("r",0,2));
        c.remove(instance.id("r",1,2));
        
        assertTrue(instance.size() == 3);
        assertTrue(instance.containsAll(c));
    }

    /**
     * Test of computeComparison method, of class nii.alloe.theory.Model.
     */
    public void testComputeComparison() {
        System.out.println("computeComparison");
        Logic l = new Logic("-> r(1,1)");
        l.setModelSize(4);
        
        Model m = new Model(l);
        m.addBasicGraphs(l);
        Graph g = m.addSpecificGraph("r");
        g.add(2,1);
        g.add(2,3);
        g.add(0,2);
        Model instance = new Model(l);
        instance.addBasicGraphs(l);
        g = instance.addSpecificGraph("r");
        g.add(1,2);
        g.add(2,3);
        g.add(0,2);
        
        int[] expResult = { 2, 1, 1 };
        int[] result = instance.computeComparison(m);
        for(int i  = 0; i < 3; i++) 
            assertEquals(expResult[i], result[i]);
    }

    /**
     * Test of size method, of class nii.alloe.theory.Model.
     */
    public void testSize() {
        System.out.println("size");
        
        Logic l = new Logic("-> r(1,1)");
        l.setModelSize(4);
        Model instance = new Model(l);
        instance.addBasicGraphs(l);
        Graph g = instance.addSpecificGraph("r");
        g.add(1,2);
        g.add(2,3);
        g.add(0,2);
        
        int expResult = 3;
        int result = instance.size();
        assertEquals(expResult, result);
        
    }

   

    /**
     * Test of getGraphByName method, of class nii.alloe.theory.Model.
     */
    public void testGetGraphByName() {
        System.out.println("getGraphByName");
        
        String name = "r";
        Logic l = new Logic("-> r(1,1)");
        l.setModelSize(4);
        Model instance = new Model(l);
        instance.addBasicGraphs(l);
        Graph g = instance.addSpecificGraph("r");
        g.add(1,2);
        g.add(2,3);
        g.add(0,2);
        
        Graph expResult = g;
        Graph result = instance.getGraphByName(name);
        assertEquals(expResult, result);
    }

    /**
     * Test of getGraphNames method, of class nii.alloe.theory.Model.
     */
    public void testGetGraphNames() {
        System.out.println("getGraphNames");
        
        Logic l = new Logic("-> r(1,1)");
        l.setModelSize(4);
        Model instance = new Model(l);
        instance.addBasicGraphs(l);
        Graph g = instance.addSpecificGraph("r");
        g.add(1,2);
        g.add(2,3);
        g.add(0,2);
        
        Vector<String> expResult = new Vector<String>();
        expResult.add("e");
        expResult.add("r");
        Vector<String> result = instance.getGraphNames();
        assertEquals(expResult, result);
    }    
}
