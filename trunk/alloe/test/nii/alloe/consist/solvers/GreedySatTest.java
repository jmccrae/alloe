/*
 * GreedySatTest.java
 * JUnit based test
 *
 * Created on February 15, 2008, 11:55 AM
 */

package nii.alloe.consist.solvers;

import junit.framework.*;
import nii.alloe.theory.*;
import java.util.*;
import java.io.*;
import nii.alloe.consist.ConsistProblem;

/**
 *
 * @author john
 */
public class GreedySatTest extends TestCase {
    
    public GreedySatTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
    }
    
    protected void tearDown() throws Exception {
    }
    
    /**
     * Test of solve method, of class nii.alloe.consist.solvers.GreedySat.
     */
    public void testSolve() {
        try {
            System.out.println("solve");
            
            Logic logic = new Logic(new File("logics/hypernym.logic"));
            logic.setModelSize(4);
            Model m = new Model(logic);
            ProbabilityGraph g = m.addProbabilityGraph("r1");
            g.setBaseVal(0.02);
            g.setVal(0,1,0.99);
            g.setVal(1,2,0.8);
            g.setVal(2,3,0.99);
            g.setVal(0,2,.45);
            g.setVal(1,3,.45);
            g.setVal(0,3,.01);
            GreedySat instance = new GreedySat(logic,m);
            
            instance.solve();
            
            Model expModel = new Model(logic);
            expModel = m.createSpecificCopy();
            expModel.remove(expModel.id("r1",1,2));
            
            assertEquals(expModel,instance.soln);
            assertTrue(instance.cost >= (Math.log(.8) - Math.log(.2)) * (1 - ConsistProblem.PERTURBATION_SIZE) &&
                    instance.cost <= (Math.log(.8) - Math.log(.2)) * (1 + ConsistProblem.PERTURBATION_SIZE));
        } catch(Exception x) {
            x.printStackTrace();
            fail(x.getMessage());
        }
    }
    
}
