/*
 * FindMatchingsTest.java
 * JUnit based test
 *
 * Created on 13 March 2008, 13:06
 */

package nii.alloe.termvar;

import junit.framework.*;
import java.awt.datatransfer.StringSelection;
import java.util.*;
import nii.alloe.niceties.*;

/**
 *
 * @author john
 */
public class FindMatchingsTest extends TestCase {
    
    public FindMatchingsTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    /**
     * Test of findMatchings method, of class nii.alloe.termvar.FindMatchings.
     */
    public void testFindMatchings() {
        System.out.println("findMatchings");
        
        StringList string1 = new StringList("te");
        StringList string2 = new StringList("cid");
        FindMatchings instance = new FindMatchings();
        
        
        Vector<String[]> result = instance.findMatchingsAsStrings(string1, string2);
        for(String[] ss : result) {
            System.out.println(ss[0]);
            System.out.println(ss[1]);
            System.out.println();
        }
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
