/*
 * GrowingSolverTest.java
 * JUnit based test
 *
 * Created on February 15, 2008, 1:51 AM
 */

package nii.alloe.consist;

import junit.framework.*;
import java.util.*;
import nii.alloe.theory.*;
import java.io.*;

/**
 *
 * @author john
 */
public class GrowingSolverTest extends TestCase {
    
    public GrowingSolverTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
    }
    
    protected void tearDown() throws Exception {
    }
    
    /**
     * Test of solve method, of class nii.alloe.consist.GrowingSolver.
     */
    public void testSolve() {
        try {
            System.out.println("solve");
            Logic l = new Logic(new File("logics/hypernym.logic"));
            l.setModelSize(4);
            Model m = new Model(l);
            ProbabilityGraph g = m.addProbabilityGraph("r1");
            g.setBaseVal(0.02);
            g.setVal(0,1,0.99);
            g.setVal(1,2,0.8);
            g.setVal(2,3,0.99);
            g.setVal(0,2,.45);
            g.setVal(1,3,.45);
            g.setVal(0,3,.01);
            GrowingSolver instance = new GrowingSolver(l,m);
            
            instance.solve();
            
            Model expModel = new Model(l);
            expModel = m.createSpecificCopy();
            expModel.remove(expModel.id("r1",1,2));
            
            assertEquals(expModel,instance.soln);
            assertTrue(instance.cost >= (Math.log(.8) - Math.log(.2)) * (1 - ConsistProblem.PERTURBATION_SIZE) && 
                    instance.cost <= (Math.log(.8) - Math.log(.2)) * (1 + ConsistProblem.PERTURBATION_SIZE));
        } catch(IOException x) {
            x.printStackTrace();
            fail(x.getMessage());
        }
    }
}
