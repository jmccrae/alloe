/*
 * GenerateTermVariationPatternsTest.java
 * JUnit based test
 *
 * Created on March 26, 2008, 12:57 PM
 */

package nii.alloe.termvar;

import junit.framework.*;
import nii.alloe.corpus.*;
import nii.alloe.niceties.*;
import java.util.*;
import java.io.*;

/**
 *
 * @author john
 */
public class GenerateTermVariationPatternsTest extends TestCase {
    
    public GenerateTermVariationPatternsTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
    }
    
    protected void tearDown() throws Exception {
    }
    
    /**
     * Test of getTermVariationPatterns method, of class nii.alloe.termvar.GenerateTermVariationPatterns.
     */
    public void testGetTermVariationPatterns() {
        System.out.println("getTermVariationPatterns");
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream("/home/john/wpshie/syns-short.atps"));
            TermPairSet tps = (TermPairSet)ois.readObject();
            ois.close();
            GenerateTermVariationPatterns instance = new GenerateTermVariationPatterns(tps);
            
            Collection<List<MatchCharPair>> result = instance.getTermVariationPatterns();
            for(Object o : result) {
                System.out.println(o.toString());
            }
        } catch(Exception x) {
            x.printStackTrace();
            fail(x.getMessage());
        }
    }
    
}
