/*
 * PatternTest.java
 * JUnit based test
 *
 * Created on 10 December 2007, 13:33
 */

package nii.alloe.corpus.pattern;

import junit.framework.*;
import java.util.*;
import java.util.regex.*;
import nii.alloe.corpus.analyzer.AlloeAnalyzer;

/**
 *
 * @author john
 */
public class PatternTest extends TestCase {
    
    public PatternTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
    }
    
    protected void tearDown() throws Exception {
    }
    
    /**
     * Test of isAlignableWith method, of class nii.aloe.corpus.pattern.Pattern.
     */
    public void testIsAlignableWith() {
        System.out.println("isAlignableWith");
        
        Pattern pat = new Pattern("* 1 is same, as 2 * *");
        Pattern instance = new Pattern("1 x y * 2 * h *");
        
        boolean expResult = true;
        boolean result = instance.isAlignableWith(pat);
        assertEquals(expResult, result);
    }
    
    /**
     * Test of initialDist method, of class nii.aloe.corpus.pattern.Pattern.
     */
    public void testInitialDist() {
        System.out.println("initialDist");
        
        Pattern instance = new Pattern("* hi *, 1 2");
        
        int expResult = 6;
        int result = instance.initialDist();
        assertEquals(expResult, result);
        
    }
    
    /**
     * Test of interPairDist method, of class nii.aloe.corpus.pattern.Pattern.
     */
    public void testInterPairDist() {
        System.out.println("interPairDist");
        
        Pattern instance = new Pattern("1 * hi *, 2");
        
        int expResult = 7;
        int result = instance.interPairDist();
        assertEquals(expResult, result);
        
    }
    
    /**
     * Test of finalDist method, of class nii.aloe.corpus.pattern.Pattern.
     */
    public void testFinalDist() {
        System.out.println("finalDist");
        
        Pattern instance = new Pattern("1 2, * hi *");
        
        int expResult = 6;
        int result = instance.finalDist();
        assertEquals(expResult, result);
    }
    
    /**
     * Test of orientation method, of class nii.aloe.corpus.pattern.Pattern.
     */
    public void testOrientation() {
        System.out.println("orientation");
        
        Pattern instance = new Pattern("2 1");
        
        int expResult = -1;
        int result = instance.orientation();
        assertEquals(expResult, result);
        assertEquals(new Pattern("1 2").orientation(),1);
        
    }
    
    /**
     * Test of split method, of class nii.aloe.corpus.pattern.Pattern.
     */
    public void testSplit() {
        System.out.println("split");
        
        Pattern instance = new Pattern("1 hi, * three 2");
        
        String[] expResult = new String[9];
        expResult[0] = "1";
        expResult[1] = " ";
        expResult[2] = "hi";
        expResult[3] = ", ";
        expResult[4] = "*";
        expResult[5] = " ";
        expResult[6] = "three";
        expResult[7] = " ";
        expResult[8] = "2";
        String[] result = instance.split();
        assertEquals(expResult.length, result.length);
        for(int i = 0; i < result.length; i++)
            assertEquals(expResult[i], result[i]);
    }
    
    /**
     * Test of getAlignmentWith method, of class nii.aloe.corpus.pattern.Pattern.
     */
    public void testGetAlignmentWith() {
        System.out.println("getAlignmentWith");
        
        Pattern pattern2 = new Pattern("1 2");
        Pattern instance = new Pattern("* * 1 2 * *");
        
        String[] result = instance.getAlignmentWith(pattern2);
        assertTrue(result.length == 3);
        
    }
    
    /**
     * Test of makeMostDominant method, of class nii.aloe.corpus.pattern.Pattern.
     */
    public void testMakeMostDominant() {
        System.out.println("makeMostDominant");
        
        Pattern instance = new Pattern("* * 1 2 * *");
        
        instance.makeMostDominant();
        
        assertTrue(instance.getVal().equals("1 2"));
    }
    
    /**
     * Test of matches method, of class nii.aloe.corpus.pattern.Pattern.
     */
    public void testMatches() {
        System.out.println("matches");
        
        String str = "animals such as cats";
        String term1 = "animals";
        String term2 = "cats";
        Pattern instance = new Pattern("1 such * 2");
        
        boolean expResult = true;
        boolean result = instance.matches(str, term1, term2);
        assertEquals(expResult, result);
        
    }
    
    /**
     * Test of getTermMatch method, of class nii.aloe.corpus.pattern.Pattern.
     */
    public void testGetTermMatch() {
        System.out.println("getTermMatch");
        
        String str = "animals such as cats";
        Pattern instance = new Pattern("1 such * 2");
        
        String[] expResult = { "animals", "cats" };
        String[] result = instance.getTermMatch(str);
        assertEquals(expResult[0], result[0]);
        assertEquals(expResult[1], result[1]);
        
    }
    
    /**
     * Test of getQuery method, of class nii.aloe.corpus.pattern.Pattern.
     */
    public void testGetQuery() {
        System.out.println("getQuery");
        
        Pattern instance = new Pattern("hi * 1 but, 3 2");
        
        String expResult = "hi     but, 3  ";
        String result = instance.getQuery();
        assertEquals(expResult, result);
        
    }
    
    /**
     * Test of getQueryWithTerms method, of class nii.aloe.corpus.pattern.Pattern.
     */
    public void testGetQueryWithTerms() {
        System.out.println("getQueryWithTerms");
        
        String term1 = "term1";
        String term2 = "term2";
        Pattern instance = new Pattern("1 * hi 2");
        
        String expResult = "term1  hi term2";
        String result = instance.getQueryWithTerms(term1, term2);
        assertEquals(expResult, result);
    }
    
    /**
     * Test of getVal method, of class nii.aloe.corpus.pattern.Pattern.
     */
    public void testGetVal() {
        System.out.println("getVal");
        
        Pattern instance = new Pattern("1 hi 2");
        
        String expResult = "1 hi 2";
        String result = instance.getVal();
        assertEquals(expResult, result);
    }
    
    /**
     * Test of setVal method, of class nii.aloe.corpus.pattern.Pattern.
     */
    public void testSetVal() {
        System.out.println("setVal");
        
        String s = "1 * hi, 2";
        Pattern instance = new Pattern();
        
        instance.setVal(s);
        
        boolean ok = false;
        
        try {
            instance.setVal("hi1 * 3");
        } catch(IllegalArgumentException x) {
            ok = true;
        }
        assertTrue(ok);
    }

    /**
     * Test of isTrivial method, of class nii.alloe.corpus.pattern.Pattern.
     */
    public void testIsTrivial() {
        System.out.println("isTrivial");
        
        Pattern instance = new Pattern("of 1 * * * * * 2");
        
        boolean expResult = true;
        boolean result = instance.isTrivial();
        assertEquals(expResult, result);
    }
 
}
