/*
 * MinSupportPrecisionMetricTest.java
 * JUnit based test
 *
 * Created on April 7, 2008, 10:20 AM
 */

package nii.alloe.corpus.pattern;

import junit.framework.*;
import nii.alloe.corpus.*;
import java.util.*;
import java.io.*;

/**
 *
 * @author john
 */
public class MinSupportPrecisionMetricTest extends TestCase {
    
    public MinSupportPrecisionMetricTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
    }
    
    protected void tearDown() throws Exception {
    }
    
    /**
     * Test of scorePattern method, of class nii.alloe.corpus.pattern.MinSupportPrecisionMetric.
     */
    public void testScorePattern() {
        try {
            System.out.println("scorePattern");
            
            Pattern pattern = new Pattern("1 (* known as 2");
            Corpus corpus = Corpus.openCorpus(new File("/home/john/wpshie/corpus.idx"));
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream("/home/john/wpshie/syns.atps"));
            TermPairSet termPairs = (TermPairSet)ois.readObject();
            ois.close();
            MinSupportPrecisionMetric instance = new MinSupportPrecisionMetric(corpus, termPairs);
            
            double expResult = 0.0;
            double result = instance.scorePattern(pattern);
            System.out.println("Score = " + result);
        } catch(Exception x) {
            x.printStackTrace();
            fail();
        }
    }
    
}
