/*
 * ConstructorTest.java
 * JUnit based test
 *
 * Created on February 10, 2008, 2:15 PM
 */

package nii.alloe.consist.solvers;

import junit.framework.*;
import nii.alloe.theory.*;
import java.util.*;
import java.io.*;

/**
 *
 * @author john
 */
public class ConstructorTest extends TestCase {
    
    public ConstructorTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    /**
     * Test of solve method, of class nii.alloe.consist.solvers.Constructor.
     */
    public void testSolve() {
        System.out.println("solve");
        Logic logic;
        try {
            logic = new Logic(new File("logics/synonym.logic"));
        } catch(IOException x) {
            fail();
            return;
        }
        logic.setModelSize(4);
        Model probModel = new Model(logic);
        probModel.addBasicGraphs(logic);
        ProbabilityGraph pg = probModel.addProbabilityGraph("r1");
        pg.setBaseVal(.1);
        pg.setVal(0,2,.7);
        pg.setVal(2,0,.7);
        pg.setVal(1,2,.3);
        pg.setVal(2,1,.3);
        pg.setVal(0,1,.9);
        pg.setVal(1,0,.9);
        pg.setVal(2,3,.6);
        pg.setVal(3,2,.6);
        
        Constructor instance = new Constructor(Constructor.ADD_NODES_MOST_CENTRAL,Constructor.METHOD_ASTAR);
        
        instance.solve(probModel, logic);
        
        System.out.println(instance.solvedModel + " cost= " + instance.cost);
        
    }
    
}
