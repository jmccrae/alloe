/*
 * GenerateTermVariationPatternsTest.java
 * JUnit based test
 *
 * Created on March 26, 2008, 12:57 PM
 */

package nii.alloe.termvar;

import junit.framework.*;
import nii.alloe.corpus.*;
import java.util.*;
import java.io.*;
import nii.alloe.tools.strings.*;

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
            /*GenerateTermVariationPatterns instance = new GenerateTermVariationPatterns(tps);
            
            Collection<List<MatchCharPair>> result = instance.getTermVariationPatterns();
            for(Object o : result) {
                System.out.println(o.toString());
            }*/
            for(String[] terms : tps) {
                Vector<String[]> res = FindMatchings.findMatchingsAsStrings(new StringList(terms[0]),new StringList(terms[1]));
                System.out.println(terms[0] + " => " + terms[1]);
                for(String[] r : res) {
                    System.out.println(r[0]);
                    System.out.println(r[1]);
                    System.out.println();
                }
            }
        } catch(Exception x) {
            x.printStackTrace();
            fail(x.getMessage());
        }
    }
    
}
