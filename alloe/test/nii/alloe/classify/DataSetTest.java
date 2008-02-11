/*
 * DataSetTest.java
 * JUnit based test
 *
 * Created on 08 January 2008, 12:52
 */

package nii.alloe.classify;

import junit.framework.*;
import nii.alloe.theory.Logic;
import nii.alloe.theory.Model;
import nii.alloe.corpus.TermList;
import weka.core.*;
import weka.classifiers.*;
import java.util.*;
import weka.classifiers.*;
import weka.classifiers.functions.*;
import java.io.*;

/**
 *
 * @author john
 */
public class DataSetTest extends TestCase {
    LinkedList<String> atts;
    TermList termList;
    
    public DataSetTest(String testName) {
        super(testName);
        termList = new TermList();
        termList.add("term1");
        termList.add("term2");
    }

    protected void setUp() throws Exception {
        atts = new LinkedList<String>();
        atts.add("att1");
        atts.add("att2");
        atts.add("att3");
    }

    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(DataSetTest.class);
        
        return suite;
    }

    /**
     * Test of prepRelation method, of class nii.aloe.classify.DataSet.
     */
    public void testPrepRelation() {
        System.out.println("prepRelation");
        
        String relation = "relation";
        Iterator<String> attNames = atts.iterator();
        DataSet instance = new DataSet(termList);
        
        instance.prepRelation(relation, attNames);
    }

    /**
     * Test of addInstance method, of class nii.aloe.classify.DataSet.
     */
    public void testAddInstance() {
        System.out.println("addInstance");
        
        Instance i = new Instance(3);
        String relation = "relation";
        String term1 = "term1";
        String term2 = "term2";
        DataSet instance = new DataSet(termList);
        instance.prepRelation(relation, atts.iterator());
        
        instance.addInstance(i, relation, term1, term2);
    }

    /**
     * Test of addNonOccInstance method, of class nii.aloe.classify.DataSet.
     */
    public void testAddNonOccInstance() {
        System.out.println("addNonOccInstance");
        
        String relation = "relation";
        String term1 = "term1";
        String term2 = "term2";
        DataSet instance = new DataSet(termList);
        instance.prepRelation(relation, atts.iterator());
        
        instance.addNonOccInstance(relation, term1, term2);
    }

    /**
     * Test of buildClassifierSet method, of class nii.aloe.classify.DataSet.
     */
    public void testBuildClassifierSet() {
        System.out.println("buildClassifierSet");
        
        DataSet instance = new DataSet(termList);
        
        Map<String, Classifier> expResult = null;
        Map<String, Classifier> result = instance.buildClassifierSet();
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

  

    /**
     * Test of buildTrueModel method, of class nii.aloe.classify.DataSet.
     */
    public void testBuildTrueModel() {
        System.out.println("buildTrueModel");
        
        Logic logic = null;
        DataSet instance = new DataSet(termList);
        
        Model expResult = null;
        Model result = instance.buildTrueModel(logic);
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
