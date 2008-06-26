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
        
        Rule instance = Rule.loadRule("r(1,2) ; r(2,3) -> r(1,3)", new RuleSymbol());
        
        int expResult = 3;
        int result = instance.length();
        assertEquals(expResult, result);
        assertTrue(instance.isOK());
    }

    /**
     * Test of loadRule method, of class nii.alloe.theory.Rule.
     */
    public void testLoadRule() {
        System.out.println("loadRule");
        
        String rule = "r(1,2) ; r(2,3) -> r(1,3)";
        assertNotNull(Rule.loadRule(rule, new RuleSymbol()));
         rule = "r(1(),2) ; r(2,3) -> r(1(),3)";
        assertNotNull(Rule.loadRule(rule, new RuleSymbol()));
         rule = "r(1,2) ; r(2,3(2)) -> r(1,3(2))";
        assertNotNull(Rule.loadRule(rule, new RuleSymbol())); 
        rule = "r(\"const1\",2) ; r(2,3) -> r(\"const1\",3)";
        assertNotNull(Rule.loadRule(rule, new RuleSymbol()));
    }

    /**
     * Test of create method, of class nii.alloe.theory.Rule.
     */
    public void testCreate() {
        System.out.println("create");
        
        List<Integer> positives = new LinkedList<Integer>();
        List<Integer> negatives = new LinkedList<Integer>();
        Logic l = new Logic("r(1,1) ->");
        l.setModelSize(4);
        Model model = new Model(l);
        model.addBasicGraphs(l);
        model.addSpecificGraph("r");
        positives.add(model.id("r",1,2));
        positives.add(model.id("r",2,3));
        negatives.add(model.id("r",1,3));
        
        Rule expResult = Rule.loadRule("r(1,2) ; r(2,3) -> r(1,3)", new RuleSymbol());
        expResult.terms.get(0)[0].setAssignment(1);
        expResult.terms.get(0)[1].setAssignment(2);
        expResult.terms.get(1)[1].setAssignment(3);
        Rule result = new Rule(positives, negatives, model);
        assertEquals(expResult, result);
        
    }

   
    /**
     * Test of statementsForArgument method, of class nii.alloe.theory.Rule.
     */
    public void testStatementsForArgument() {
        System.out.println("statementsForArgument");
        
        Rule instance = Rule.loadRule("r(1,2) ; r(2,3) -> r(1,3)", new RuleSymbol());
        Rule.Argument arg = instance.terms.get(0)[0];
        
        LinkedList<Integer> expResult = new LinkedList<Integer>();
        expResult.add(0);
        expResult.add(2);
        LinkedList<Integer> result = instance.statementsForArgument(arg);
        assertEquals(expResult, result);
        assertTrue(instance.isOK());
    }

    /**
     * Test of isRuleSatisfied method, of class nii.alloe.theory.Rule.
     */
    public void testIsRuleSatisfied() {
        System.out.println("isRuleSatisfied");
        
        Logic l = new Logic("");
        l.setModelSize(4);
        Model m = new Model(l);
        m.addBasicGraphs(l);
        m.addSpecificGraph("r");
        m.add(m.id("r", 1,2));
        Rule instance = Rule.loadRule("r(1,2) ; r(2,3) -> r(1,3)", new RuleSymbol());
        instance.terms.get(0)[0].setAssignment(1);
        instance.terms.get(0)[1].setAssignment(2);
        instance.terms.get(1)[1].setAssignment(3);
       
        
        boolean expResult = true;
        boolean result = instance.isRuleSatisfied(m);
        assertEquals(expResult, result);
        expResult = false;
        m.add(m.id("r",2,3));
        result = instance.isRuleSatisfied(m);
        assertEquals(expResult,result);
        assertTrue(instance.isOK());
    }

    /**
     * Test of resolve method, of class nii.alloe.theory.Rule.
     */
    public void testResolve() {
        System.out.println("resolve");
        
        Rule rule = Rule.loadRule("r(1,2) ; r(2,3) -> r(1,3)", new RuleSymbol());
        rule.terms.get(0)[0].setAssignment(0);
        rule.terms.get(0)[1].setAssignment(2);
        rule.terms.get(1)[1].setAssignment(3);
        Logic l = new Logic("r(1,1) ->");
        l.setModelSize(4);
        Model model = new Model(l);
        model.addBasicGraphs(l);
        model.addSpecificGraph("r");
        Rule instance = Rule.loadRule("r(1,2) ; r(2,3) -> r(1,3)", new RuleSymbol());
        instance.terms.get(0)[0].setAssignment(0);
        instance.terms.get(0)[1].setAssignment(1);
        instance.terms.get(1)[1].setAssignment(2);
        
        Rule expResult = Rule.loadRule("r(1,2) ; r(2,3) ; r(3,4) -> r(1,4)", new RuleSymbol());
        expResult.terms.get(0)[0].setAssignment(0);
        expResult.terms.get(0)[1].setAssignment(1);
        expResult.terms.get(1)[1].setAssignment(2);
        expResult.terms.get(2)[1].setAssignment(3);
        Rule result = instance.resolve(rule, model);
        assertEquals(expResult, result);
        assertTrue(result.isOK());
        assertTrue(instance.isOK());
    }


    /**
     * Test of simplify method, of class nii.alloe.theory.Rule.
     */
    public void testSimplify() {
        System.out.println("simplify");
        
        Rule r = Rule.loadRule("r(1,1) ; r(2,2) -> r(1,2)", new RuleSymbol());
        r.terms.get(0)[0].setAssignment(1);
        r.terms.get(1)[1].setAssignment(2);
        Logic l = new Logic("r(1,1) ->");
        l.setModelSize(4);
        Model model = new Model(l);
        model.addBasicGraphs(l);
        model.addSpecificGraph("r");
        
        Rule expResult = r;
        Rule result = Rule.simplify(r, model);
        assertEquals(expResult, result);
        r.terms.get(1)[1].unsetAssignment();
        r.terms.get(1)[1].setAssignment(1);
        assertNull(Rule.simplify(r, model));
    }


    /**
     * Test of subsumes method, of class nii.alloe.theory.Rule.
     */
    public void testSubsumes() {
        System.out.println("subsumes");
        
        Rule r = Rule.loadRule("r(1,2) ; r(2,2) -> r(2,1)", new RuleSymbol());
        r.terms.get(0)[0].setAssignment(1);
        r.terms.get(0)[1].setAssignment(2);
        Rule instance = Rule.loadRule("r(1,2) -> r(2,1)", new RuleSymbol());
        instance.terms.get(0)[0].setAssignment(1);
        instance.terms.get(0)[1].setAssignment(2);
        
        boolean expResult = true;
        boolean result = instance.subsumes(r);
        assertEquals(expResult, result);
        assertTrue(instance.isOK());
    }


    /**
     * Test of equals method, of class nii.alloe.theory.Rule.
     */
    public void testEquals() {
        System.out.println("equals");
        
        Rule rule = Rule.loadRule("r(1,2) ; r(2,3) -> r(1,3)", new RuleSymbol());
        rule.terms.get(0)[0].setAssignment(0);
        rule.terms.get(0)[1].setAssignment(1);
        rule.terms.get(1)[1].setAssignment(2);
       
        Rule instance = Rule.loadRule("r(2,1) ; r(1,3) -> r(2,3)", new RuleSymbol());
       instance.terms.get(0)[0].setAssignment(0);
        instance.terms.get(0)[1].setAssignment(1);
        instance.terms.get(1)[1].setAssignment(2);
        
        boolean expResult = true;
        boolean result = instance.equals(rule);
        assertEquals(expResult, result);
        assertTrue(instance.isOK());
    }

    /**
     * Test of conclusionMustBeSatisified method, of class nii.alloe.theory.Rule.
     */
    public void testConclusionMustBeSatisified() {
        System.out.println("conclusionMustBeSatisified (no test)");
    }

    /**
     * Test of conclusionContains method, of class nii.alloe.theory.Rule.
     */
    public void testConclusionContains() {
        System.out.println("conclusionContains");
        
        Logic l = new Logic("-> r(1,1)");
        l.setModelSize(4);
        Model model = new Model(l);
        model.addBasicGraphs(l);
        model.addSpecificGraph("r");
        TreeSet<Integer> testTerms = new TreeSet<Integer>();
        testTerms.add(model.id("r",1,1));
        testTerms.add(model.id("r",1,2));
        Rule instance = Rule.loadRule("r(1,2) -> r(2,1) ; r(1,1) ; r(2,2)", new RuleSymbol());
        instance.terms.get(0)[0].setAssignment(1);
        instance.terms.get(0)[1].setAssignment(2);
        
        boolean expResult = true;
        boolean result = instance.conclusionContains(testTerms, model);
        assertEquals(expResult, result);
        assertTrue(instance.isOK());
    }

    /**
     * Test of forAllAssignments method, of class nii.alloe.theory.Rule.
     */
    public void testForAllAssignments() {
        System.out.println("forAllAssignments (no test: laziness)");
    }

    /**
     * Test of limitToModel method, of class nii.alloe.theory.Rule.
     */
    public void testLimitToModel() {
        System.out.println("limitToModel ");
        
        Logic l = new Logic("-> r(1,1)");
        l.setModelSize(4);
        Model model = new Model(l);
        model.addBasicGraphs(l);
        Graph g = model.addSpecificGraph("r");
        g.add(0, 1);
        g.add(1, 2);
        Rule instance = Rule.loadRule("r(1,2) ; r(2,3) -> r(1,3)", new RuleSymbol());
        instance.terms.get(0)[0].setAssignment(0);
        instance.terms.get(0)[1].setAssignment(1);
        instance.terms.get(1)[1].setAssignment(2);
        
        instance.limitToModel(model);
        assertEquals(2,instance.length());
        assertTrue(instance.isOK());
    }    
}
