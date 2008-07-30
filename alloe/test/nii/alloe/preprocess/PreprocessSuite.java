/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nii.alloe.preprocess;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 * @author john
 */
public class PreprocessSuite extends TestCase {
    
    public PreprocessSuite(String testName) {
        super(testName);
    }            

    public static Test suite() {
        TestSuite suite = new TestSuite("PreprocessSuite");
        suite.addTest(nii.alloe.preprocess.TokeniserTest.suite());
        suite.addTest(nii.alloe.preprocess.SentenceSplitterTest.suite());
        return suite;
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

}
