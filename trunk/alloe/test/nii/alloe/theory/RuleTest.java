/*
 * RuleTest.java
 * JUnit based test
 *
 * Created on April 1, 2008, 11:04 PM
 */

package nii.alloe.theory;

import junit.framework.*;
import java.util.regex.*;
import java.util.*;

/**
 *
 * @author john
 */
public class RuleTest extends TestCase {
    
    public RuleTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(RuleTest.class);
        
        return suite;
    }

    /**
     * Test of length method, of class nii.alloe.theory.Rule.
     */
    public void testLength() {
        System.out.println("length");
        
        Rule instance = null;
        
        int expResult = 0;
        int result = instance.length();
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of loadRule method, of class nii.alloe.theory.Rule.
     */
    public void testLoadRule() {
        System.out.println("loadRule");
        
        String rule = "";
        
        Rule expResult = null;
        Rule result = Rule.loadRule(rule);
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of create method, of class nii.alloe.theory.Rule.
     */
    public void testCreate() {
        System.out.println("create");
        
        List<Integer> positives = null;
        List<Integer> negatives = null;
        Model model = null;
        
        Rule expResult = null;
        Rule result = new Rule(positives, negatives, model);
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of toString method, of class nii.alloe.theory.Rule.
     */
    public void testToString() {
        System.out.println("toString");
        
        Rule instance = null;
        
        String expResult = "";
        String result = instance.toString();
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }


    /**
     * Test of statementsForArgument method, of class nii.alloe.theory.Rule.
     */
    public void testStatementsForArgument() {
        System.out.println("statementsForArgument");
        
        Rule.Argument arg = null;
        Rule instance = null;
        
        LinkedList<Integer> expResult = null;
        LinkedList<Integer> result = instance.statementsForArgument(arg);
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isRuleSatisfied method, of class nii.alloe.theory.Rule.
     */
    public void testIsRuleSatisfied() {
        System.out.println("isRuleSatisfied");
        
        Model m = null;
        Rule instance = null;
        
        boolean expResult = true;
        boolean result = instance.isRuleSatisfied(m);
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of resolve method, of class nii.alloe.theory.Rule.
     */
    public void testResolve() {
        System.out.println("resolve");
        
        Rule rule = null;
        Model model = null;
        Rule instance = null;
        
        Rule expResult = null;
        Rule result = instance.resolve(rule, model);
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of tryAssign method, of class nii.alloe.theory.Rule.
     */
    public void testTryAssign() {
        System.out.println("tryAssign");
        
        int arg = 0;
        int i = 0;
        int j = 0;
        Rule instance = null;
        
        boolean expResult = true;
        boolean result = instance.tryAssign(arg, i, j);
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of simplify method, of class nii.alloe.theory.Rule.
     */
    public void testSimplify() {
        System.out.println("simplify");
        
        Rule r = null;
        Model model = null;
        
        Rule expResult = null;
        Rule result = Rule.simplify(r, model);
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of canResolveWith method, of class nii.alloe.theory.Rule.
     */
    public void testCanResolveWith() {
        System.out.println("canResolveWith");
        
        Rule r = null;
        Rule instance = null;
        
        boolean expResult = true;
        boolean result = instance.canResolveWith(r);
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of subsumes method, of class nii.alloe.theory.Rule.
     */
    public void testSubsumes() {
        System.out.println("subsumes");
        
        Rule r = null;
        Rule instance = null;
        
        boolean expResult = true;
        boolean result = instance.subsumes(r);
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of compareTo method, of class nii.alloe.theory.Rule.
     */
    public void testCompareTo() {
        System.out.println("compareTo");
        
        Rule r = null;
        Rule instance = null;
        
        int expResult = 0;
        int result = instance.compareTo(r);
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of equals method, of class nii.alloe.theory.Rule.
     */
    public void testEquals() {
        System.out.println("equals");
        
        Object obj = null;
        Rule instance = null;
        
        boolean expResult = true;
        boolean result = instance.equals(obj);
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of conclusionMustBeSatisified method, of class nii.alloe.theory.Rule.
     */
    public void testConclusionMustBeSatisified() {
        System.out.println("conclusionMustBeSatisified");
        
        Model model = null;
        Rule instance = null;
        
        boolean expResult = true;
        boolean result = instance.conclusionMustBeSatisified(model);
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of conclusionContains method, of class nii.alloe.theory.Rule.
     */
    public void testConclusionContains() {
        System.out.println("conclusionContains");
        
        TreeSet<Integer> testTerms = null;
        Model model = null;
        Rule instance = null;
        
        boolean expResult = true;
        boolean result = instance.conclusionContains(testTerms, model);
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }



    /**
     * Test of removeTerm method, of class nii.alloe.theory.Rule.
     */
    public void testRemoveTerm() {
        System.out.println("removeTerm");
        
        Integer term = null;
        Rule instance = null;
        
        instance.removeTerm(term);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }


    /**
     * Test of addTerm method, of class nii.alloe.theory.Rule.
     */
    public void testAddTerm() {
        System.out.println("addTerm");
        
        String rel = "";
        Rule.Argument[] term = null;
        boolean premise = true;
        Rule instance = null;
        
        instance.addTerm(rel, term, premise);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }


    /**
     * Test of forAllAssignments method, of class nii.alloe.theory.Rule.
     */
    public void testForAllAssignments() {
        System.out.println("forAllAssignments");
        
        Model model = null;
        AssignmentAction action = null;
        Rule instance = null;
        
        boolean expResult = true;
        boolean result = instance.forAllAssignments(model, action);
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of limitToModel method, of class nii.alloe.theory.Rule.
     */
    public void testLimitToModel() {
        System.out.println("limitToModel");
        
        Model model = null;
        Rule instance = null;
        
        instance.limitToModel(model);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }


    /**
     * Test of isOK method, of class nii.alloe.theory.Rule.
     */
    public void testIsOK() {
        System.out.println("isOK");
        
        Rule instance = null;
        
        boolean expResult = true;
        boolean result = instance.isOK();
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
