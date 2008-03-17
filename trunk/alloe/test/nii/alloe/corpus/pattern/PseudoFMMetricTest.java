/*
 * PseudoFMMetricTest.java
 * JUnit based test
 *
 * Created on March 2, 2008, 9:57 AM
 */

package nii.alloe.corpus.pattern;

import junit.framework.*;
import nii.alloe.corpus.Corpus;
import nii.alloe.corpus.EachTermPairAction;
import nii.alloe.corpus.TermPairSet;
import java.util.*;
import java.io.*;

/**
 *
 * @author john
 */
public class PseudoFMMetricTest extends TestCase {
    
    public PseudoFMMetricTest(String testName) {
        super(testName);
    }
    
    public Corpus corpus;
    public TermPairSet tps;

    protected void setUp() throws Exception {
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream("/home/john/wpshie/corpus"));
            corpus = (Corpus)ois.readObject();
            ois.close();
            ois = new ObjectInputStream(new FileInputStream("/home/john/wpshie/hyps.atps"));
            tps = (TermPairSet)ois.readObject();
            ois.close();
        } catch(Exception x) {
            x.printStackTrace();
        }
    }

    protected void tearDown() throws Exception {
    }

    /**
     * Test of scorePattern method, of class nii.alloe.corpus.pattern.PseudoFMMetric.
     */
    public void testScorePattern() {
        System.out.println("scorePattern");
        
        Pattern pattern = new Pattern("2= 1}}");
        PatternMetric instance = PatternMetricFactory.getPatternMetric(PatternMetricFactory.PSEUDO_FM,
                corpus, tps);
        
        double expResult = 0.0;
        double result = instance.scorePattern(pattern);
        assertEquals(expResult, result);
    }
    
}
