/*
 * LogicTest.java
 * JUnit based test
 *
 * Created on April 1, 2008, 11:04 PM
 */

package nii.alloe.theory;

import junit.framework.*;
import java.io.*;
import java.util.regex.*;
import java.util.*;

/**
 *
 * @author john
 */
public class LogicTest extends TestCase {
    Model model;
    String eqLogic = "r = \"syn\" -1\nr(1,2); r(2,3) -> r(1,3)\n-> r(1,1)\nr(1,2) -> r(2,1)\n";
    
    public LogicTest(String testName) {
        super(testName);
    }

    private class CheckContains implements InconsistentAction {
        private Rule r;
        private boolean found =false;
        
        public CheckContains(Rule r) {
            this.r = r;
        }
        
        public boolean doAction(Logic logic, Model m, Rule rule) {
            if(rule.equals(r))
                found = true;
            return true;
        }
        
        public boolean wasFound() {
            return found;
        }
    }
    
    protected void setUp() throws Exception {
        model = new Model(5);
        SpecificGraph g = model.addSpecificGraph("r");
        g.add(1,2);
        g.add(2,3);
        g.add(4,5);
        g.add(5,4);
    }

    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(LogicTest.class);
        
        return suite;
    }

    /**
     * Test of consistCheck method, of class nii.alloe.theory.Logic.
     */
    public void testConsistCheck() {
        System.out.println("consistCheck");
        Rule r = Rule.loadRule("r(1,2); r(2,3) -> r(1,3)");
        r.terms.get(0)[0].setAssignment(1);
        r.terms.get(0)[1].setAssignment(2);
        r.terms.get(1)[1].setAssignment(3);
        CheckContains inconsist = new CheckContains(r);
        Logic instance = new Logic(eqLogic);
        
        instance.consistCheck(model, inconsist);
        assertTrue(inconsist.wasFound());
    }

    /**
     * Test of premiseSearch method, of class nii.alloe.theory.Logic.
     */
    public void testPremiseSearch() {
        System.out.println("premiseSearch");
       Rule r = Rule.loadRule("r(1,2); r(2,3) -> r(1,3)");
        r.terms.get(0)[0].setAssignment(1);
        r.terms.get(0)[1].setAssignment(2);
        r.terms.get(1)[1].setAssignment(3);
        CheckContains inconsist = new CheckContains(r);
        Logic instance = new Logic(eqLogic);
        
        instance.consistCheck(model, inconsist);
        assertTrue(inconsist.wasFound());
    }

    /**
     * Test of findAllPotentialResolvers method, of class nii.alloe.theory.Logic.
     */
    public void testFindAllPotentialResolvers() {
        System.out.println("findAllPotentialResolvers");
 
        Set<Integer> resolvePoints = new TreeSet<Integer>();
        resolvePoints.add(2 * 5 + 4);
        Collection<Rule> potentialResolvers = new TreeSet<Rule>();
        Logic instance = new Logic(eqLogic);
        
        instance.findAllPotentialResolvers(model, resolvePoints, potentialResolvers);
        Rule r = Rule.loadRule("r(1,2); r(2,3) -> r(1,3)");
        r.terms.get(0)[0].setAssignment(1);
        r.terms.get(0)[1].setAssignment(2);
        r.terms.get(1)[1].setAssignment(4);
        assertTrue(potentialResolvers.contains(r));
    }

    /**
     * Test of getCompulsoryModel method, of class nii.alloe.theory.Logic.
     */
    public void testGetCompulsoryModel() {
        System.out.println("getCompulsoryModel");
        
        Logic instance = new Logic(eqLogic);
        
        Model expResult = new Model(5);
        SpecificGraph g = expResult.addSpecificGraph("r");
        g.add(0,0);
        g.add(1,1);
        g.add(2,2);
        g.add(3,3);
        g.add(4,4);
        Model result = instance.getCompulsoryModel(model);
        assertEquals(expResult, result);
    }

    /**
     * Test of getNegativeModel method, of class nii.alloe.theory.Logic.
     */
    public void testGetNegativeModel() {
        System.out.println("getNegativeModel");
        
        Logic instance = new Logic("r1 = \"hyp\" -1\nr(1,2); r(2,3) -> r(1,3)\nr(1,1) -> ");
        
        Model expResult = new Model(5);
        SpecificGraph g = expResult.addSpecificGraph("r");
        g.add(0,0);
        g.add(1,1);
        g.add(2,2);
        g.add(3,3);
        g.add(4,4);
        Model result = instance.getCompulsoryModel(model);
        assertEquals(expResult, result);
    }
    
}
