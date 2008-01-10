/*
 * ConsistProblemTest.java
 * JUnit based test
 *
 * Created on November 26, 2007, 6:33 AM
 */

package nii.alloe.consist;

import junit.framework.*;
import nii.alloe.theory.Logic;
import nii.alloe.theory.Model;
import nii.alloe.theory.ProbabilityGraph;
import nii.alloe.theory.SpecificGraph;
import java.util.*;

/**
 *
 * @author john
 */
public class ConsistProblemTest extends TestCase {
    
    public ConsistProblemTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    /**
     * Test of buildProblemMatrix method, of class nii.aloe.consist.ConsistProblem.
     */
    public void testBuildProblemMatrix() {
        System.out.println("buildProblemMatrix");
        
        Logic l = new Logic("logics/hypernym.logic");
        Model m = new Model(5);
        ProbabilityGraph g = m.addProbabilityGraph("r1");
        g.setBaseVal(0.02);
        g.setPosVal(0,1,.99);
        g.setPosVal(1,2,.99);
        g.setPosVal(2,4,.99);
        g.setPosVal(4,2,.99);
        g.setPosVal(1,3,.99);
        ConsistProblem instance = new ConsistProblem(l,m);
        
        
        
        SparseMatrix expResult = new SparseMatrix();
        expResult.setElem(1,0,Math.log(.99) - Math.log(.01));
        expResult.setElem(1,1);
        expResult.setElem(1,2);
        expResult.setElem(1,4);
        expResult.setElem(2,0,Math.log(.98) - Math.log(.02));
        expResult.setElem(2,1);
        expResult.setElem(3,0,Math.log(.98) - Math.log(.02));
        expResult.setElem(3,2);
        //expResult.setElem(4,0,Math.log(.98) - Math.log(.02));
        //expResult.setElem(4,6);
        expResult.setElem(7,0,Math.log(.99) - Math.log(.01));
        expResult.setElem(7,1);
        expResult.setElem(7,3);
        expResult.setElem(7,4);
        //expResult.setElem(8,0,Math.log(.99) - Math.log(.01));
        //expResult.setElem(8,2);
        expResult.setElem(9,0,Math.log(.98) - Math.log(.02));
        expResult.setElem(9,3);
        expResult.setElem(14,0,Math.log(.99) - Math.log(.01));
        expResult.setElem(14,3);
        expResult.setElem(14,4);
        expResult.setElem(14,6);
        expResult.setElem(22,0,Math.log(.99) - Math.log(.01));
        expResult.setElem(22,6);
        SparseMatrix result = instance.buildProblemMatrix();
        result.printMatrix(System.out);
        assertEquals(expResult, result);
 
        ConsistSolver solver = new ConsistSolver();
        solver.solve(expResult);
        TreeSet<Integer> soln = new TreeSet<Integer>();
        soln.add(1);
        soln.add(14);
        assertEquals(soln,solver.soln);
        
        Logic l2 = new Logic("logics/single-hyp.logic");
        m.addBasicGraphs(l2);
        ConsistProblem instance2 = new ConsistProblem(l2,m);
        result = instance2.buildProblemMatrix();
        
        assertTrue(result.rows.size() == 10 &&
                result.getRow(1).size() == 5 &&
                result.getRow(2).size() == 2 &&
                result.getRow(3).size() == 2 &&
                result.getRow(7).size() == 7 &&
                result.getRow(8).size() == 3 &&
                result.getRow(9).size() == 4 &&
                result.getRow(14).size() == 4 &&
                result.getRow(21).size() == 2 &&
                result.getRow(22).size() == 5 &&
                result.getRow(23).size() == 2);
        
        g.setPosVal(0,1,.73);
        g.setPosVal(0,2,.18);
        g.setPosVal(0,3,.18);
        g.setPosVal(0,4,.18);
        g.setPosVal(1,2,.73);
        g.setPosVal(1,3,.82);
        g.setPosVal(1,4,.18);
        g.setPosVal(2,2,.18);
        g.setPosVal(2,4,.95);
        g.setPosVal(4,2,.73);
        g.setPosVal(4,4,.18);
        
        expResult = new SparseMatrix();
        result = instance.buildProblemMatrix();
        result.printMatrix(System.out);
        Iterator<Integer> citer = result.cols.keySet().iterator();
        assertTrue(result.cols.size() == 4);
        Integer i = citer.next();
        expResult.setElem(1,i,g.removeVal(0,1));
        expResult.setElem(7,i,g.removeVal(1,2));
        expResult.setElem(14,i,g.removeVal(2,4));
        expResult.setElem(22,i,g.removeVal(4,2));
        i = citer.next();
        expResult.setElem(1,i);
        i = citer.next();
        expResult.setElem(7,i);
        expResult.setElem(14,i);
        i = citer.next();
        expResult.setElem(14,i);
        expResult.setElem(22,i);
        assertEquals(expResult,result);
        
        solver.solve(result);
        soln.clear();
        soln.add(1);
        soln.add(7);
        soln.add(22);
        assertEquals(soln,solver.soln);
        
        result = instance2.buildProblemMatrix();
        solver.solve(result);
        assertEquals(soln,solver.soln);
        
    }
    
    public void profileCompleter() {
        Logic l = new Logic("logics/hypernym.logic");
        Model m = new Model(100);
        SpecificGraph g = m.addSpecificGraph("r1");
        Model m2 = new Model(100);
        ConsistProblem p = new ConsistProblem(l,m);
        
        for(int i = 0; i <= 100; i++) {
            g.makeRandom(Math.log(i)/Math.log(100));
            
        }
        
    }
    
}
