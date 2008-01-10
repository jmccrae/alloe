/*
 * SimplexTest.java
 * JUnit based test
 *
 * Created on November 24, 2007, 6:24 AM
 */

package nii.alloe.consist;

import junit.framework.*;
import java.util.*;

/**
 *
 * @author john
 */
public class SimplexTest extends TestCase {
    
    public SimplexTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    /**
     * Test of simplexSolve method, of class nii.aloe.consist.Simplex.
     */
    public void testSimplexSolve() {
        System.out.println("simplexSolve");
        
        SparseMatrix m = new SparseMatrix();
        m.setElem(0,0,3);
        m.setElem(1,0,2);
        m.setElem(2,0,1);
        m.setElem(0,1);
        m.setElem(0,2);
        m.setElem(1,2);
        m.setElem(1,3);
        m.setElem(2,2);
        Simplex instance = new Simplex();
        
        instance.simplexSolve(m);
        
        if(instance.cost != 5 || instance.soln.size() != 2 || instance.soln.get(0) != 1 || instance.soln.get(1) != 1) {
            fail("Incorrect soln: cost=" + instance.cost + " vals " + instance.soln.toString());
        }
    }
    
}
