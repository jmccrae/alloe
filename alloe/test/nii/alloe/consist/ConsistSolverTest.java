/*
 * ConsistSolverTest.java
 * JUnit based test
 *
 * Created on November 26, 2007, 5:35 AM
 */

package nii.alloe.consist;

import junit.framework.*;
import java.util.*;

/**
 *
 * @author john
 */
public class ConsistSolverTest extends TestCase {
    
    public ConsistSolverTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    /**
     * Test of solve method, of class nii.aloe.consist.ConsistSolver.
     */
    public void testSolve() {
        
        SparseMatrix m = new SparseMatrix();
        // This isn't a good example but it should work for the moment
        m.setElem(0,0);
        m.setElem(0,2);
        m.setElem(0,3);
        m.setElem(0,6,2);
        m.setElem(1,0);
        m.setElem(1,1);
        m.setElem(1,2);
        m.setElem(1,7,2);
        m.setElem(2,0);
        m.setElem(2,1);
        m.setElem(2,4);
        m.setElem(3,0);
        m.setElem(3,3);
        m.setElem(3,5);
        ConsistSolver instance = new ConsistSolver();
        
        instance.solve(m);

        if(instance.cost != 4 || instance.soln.size() != 4 || !instance.soln.contains(0) 
            || !instance.soln.contains(1) || !instance.soln.contains(2) || !instance.soln.contains(3)) {        
            fail("Incorrect soln: cost=" + instance.cost + " vals " + instance.soln.toString());
        }
        
    }
    
}
