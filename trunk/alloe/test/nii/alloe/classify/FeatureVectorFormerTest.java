/*
 * FeatureVectorFormerTest.java
 * JUnit based test
 *
 * Created on 08 January 2008, 12:52
 */

package nii.alloe.classify;

import junit.framework.*;
import java.util.*;
import nii.alloe.corpus.Corpus;
import nii.alloe.corpus.TermPairSet;
import nii.alloe.corpus.pattern.Pattern;
import weka.core.*;

/**
 *
 * @author john
 */
public class FeatureVectorFormerTest extends TestCase {
    
    public FeatureVectorFormerTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(FeatureVectorFormerTest.class);
        
        return suite;
    }

    /**
     * Test of makeFeatureVectors method, of class nii.aloe.classify.FeatureVectorFormer.
     */
    public void testMakeFeatureVectors() {
        System.out.println("makeFeatureVectors");
        
        String relation = "";
        DataSet dataSet = null;
        Iterable<String> terms = null;
        List<Pattern> patterns = null;
        Corpus corpus = null;
        TermPairSet termPairs = null;
        //FeatureVectorFormer instance = new FeatureVectorFormer();
        
        //instance.makeFeatureVectors(relation, dataSet, terms, patterns, corpus, termPairs);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
