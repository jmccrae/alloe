/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nii.alloe.consist;

import junit.framework.TestCase;
import nii.alloe.theory.*;
import java.io.*;

/**
 *
 * @author john
 */
public class ResFreeSolverTest extends TestCase {
    
    public ResFreeSolverTest(String testName) {
        super(testName);
    }            

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of solve method, of class ResFreeSolver.
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
            ResFreeSolver instance = new ResFreeSolver(l,m);
            
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
