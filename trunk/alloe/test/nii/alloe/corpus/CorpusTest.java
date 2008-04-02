/*
 * CorpusTest.java
 * JUnit based test
 *
 * Created on 11 December 2007, 17:54
 */

package nii.alloe.corpus;

import junit.framework.*;
import nii.alloe.corpus.pattern.Pattern;
import java.util.*;
import java.io.*;
import org.apache.lucene.index.*;
import org.apache.lucene.store.*;
import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.standard.*;
import org.apache.lucene.document.*;
import org.apache.lucene.search.*;
import org.apache.lucene.queryParser.*;
import nii.alloe.corpus.pattern.*;
import nii.alloe.corpus.analyzer.*;
import nii.alloe.niceties.Strings;

/**
 *
 * @author john
 */
public class CorpusTest extends TestCase {
    
    Corpus instance;
    
    public CorpusTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        TermList terms = new TermList();
        terms.add("alice");
        terms.add("bob");
        terms.add("joe bob");
        terms.add("joe");
        instance = new Corpus(terms, "test.idx");
        instance.openIndex(true);
        instance.addDoc("alice likes bob");
        instance.addDoc("bob is nice");
        instance.addDoc("joe bob is a hillbilly");
        instance.addDoc("alice hates joe bob");
        instance.addDoc("joe doesn't know alice or bob");
        instance.closeIndex();
    }
    
    protected void tearDown() throws Exception {
        File f = new File("test.idx");
        for(File f2 : f.listFiles()) {
            f2.delete();
        }
        f.delete();
    }
    
    
    
    /**
     * Test of getContextsForTerms method, of class nii.aloe.corpus.Corpus.
     */
    public void testGetContextsForTerms() {
        System.out.println("getContextsForTerms");
        
        String term1 = "alice";
        String term2 = "joe bob";
        
        
        Iterator<Corpus.Hit> result = instance.getContextsForTerms(term1, term2);
        assertEquals(result.next().getText(), "alice hates joe bob");
        assertFalse(result.hasNext());
        
    }
    
    /**
     * Test of getContextsForPattern method, of class nii.aloe.corpus.Corpus.
     */
    public void testGetContextsForPattern() {
        System.out.println("getContextsForPattern");
        
        Pattern p = new Pattern("1 2 is a hillbilly");
        
        Iterator<Corpus.Hit> result = instance.getContextsForPattern(p);
        assertEquals(result.next().getText(), "joe bob is a hillbilly");
        assertFalse(result.hasNext());
    }
    
    /**
     * Test of openIndex method, of class nii.alloe.corpus.Corpus.
     */
    public void testOpenIndex() throws Exception {
        System.out.println("openIndex (no test)");
        
    }
    
    /**
     * Test of addDoc method, of class nii.alloe.corpus.Corpus.
     */
    public void testAddDoc() throws Exception {
        System.out.println("addDoc (no test)");
    }
    
    /**
     * Test of closeIndex method, of class nii.alloe.corpus.Corpus.
     */
    public void testCloseIndex() throws Exception {
        System.out.println("closeIndex (no test)");
    }
    
    /**
     * Test of getHitsForTerm method, of class nii.alloe.corpus.Corpus.
     */
    public void testGetHitsForTerm() {
        System.out.println("getHitsForTerm");
        
        String term = "alice";
        
        int expResult = 3;
        int result = instance.getHitsForTerm(term);
        assertEquals(expResult, result);
    }
    
    /**
     * Test of getContextsForTermInPattern method, of class nii.alloe.corpus.Corpus.
     */
    public void testGetContextsForTermInPattern() {
        System.out.println("getContextsForTermInPattern");
        
        Pattern p = new Pattern("1 likes 2");
        String term1 = "alice";
        String term2 = "bob";
        
        Iterator<Corpus.Hit> result = instance.getContextsForTermInPattern(p, term1, term2);
        assertEquals("alice likes bob", result.next().getText());
    }
    
    /**
     * Test of prepareQueryPattern method, of class nii.alloe.corpus.Corpus.
     */
    public void testPrepareQueryPattern() {
        System.out.println("prepareQueryPattern (no test)");
    }
    
    /**
     * Test of getPreparedQueryHits method, of class nii.alloe.corpus.Corpus.
     */
    public void testGetPreparedQueryHits() {
        System.out.println("getPreparedQueryHits");
        
        Object query = instance.prepareQueryPattern(new Pattern("1 * know 2"));
        
        int expResult = 1;
        int result = instance.getPreparedQueryHits(query);
        assertEquals(expResult, result);
    }
    
    /**
     * Test of getPreparedQuery method, of class nii.alloe.corpus.Corpus.
     */
    public void testGetPreparedQuery() {
        System.out.println("getPreparedQuery");
        
        Object query = instance.prepareQueryPattern(new Pattern("1 hates 2"));
        
        Iterator<Corpus.Hit> result = instance.getPreparedQuery(query);
        assertEquals("alice hates joe bob", result.next().getText());
    }
    
    /**
     * Test of getContextsForTermPrepared method, of class nii.alloe.corpus.Corpus.
     */
    public void testGetContextsForTermPrepared() {
        System.out.println("getContextsForTermPrepared");
        
        String term1 = "alice";
        String term2 = "bob";
        Object query = instance.prepareQueryPattern(new Pattern("1 likes 2"));
        
        Iterator<Corpus.Hit> result = instance.getContextsForTermPrepared(term1, term2, query);
        assertEquals("alice likes bob", result.next().getText());
    }
    
    /**
     * Test of initTermsInCorpusCache method, of class nii.alloe.corpus.Corpus.
     */
    public void testInitTermsInCorpusCache() {
        System.out.println("initTermsInCorpusCache");
        instance.initTermsInCorpusCache();
        instance.clearTermsInCorpusCache();
    }
    
    /**
     * Test of clearTermsInCorpusCache method, of class nii.alloe.corpus.Corpus.
     */
    public void testClearTermsInCorpusCache() {
        System.out.println("clearTermsInCorpusCache (no test)");
    }
    
    /**
     * Test of isTermInCorpus method, of class nii.alloe.corpus.Corpus.
     */
    public void testIsTermInCorpus() {
        System.out.println("isTermInCorpus");
        
        String term1 = "alice";
        
        boolean expResult = true;
        boolean result = instance.isTermInCorpus(term1);
        assertEquals(expResult, result);
    }
    
    /**
     * Test of areTermsInCorpus method, of class nii.alloe.corpus.Corpus.
     */
    public void testAreTermsInCorpus() {
        System.out.println("areTermsInCorpus");
        
        String term1 = "alice";
        String term2 = "bob";
        
        boolean expResult = true;
        boolean result = instance.areTermsInCorpus(term1, term2);
        assertEquals(expResult, result);
    }
    
    /**
     * Test of getTermsInCorpus method, of class nii.alloe.corpus.Corpus.
     */
    public void testGetTermsInCorpus() {
        System.out.println("getTermsInCorpus");
        instance.initTermsInCorpusCache();
        Iterator<Corpus.TermPair> result = instance.getTermsInCorpus();
        boolean OK = false;
        int n = 0;
        while(result.hasNext()) {
            Corpus.TermPair pair = result.next();
            if(pair.term1.equals("alice") && pair.term2.equals("bob"))
                OK = true;
            n++;
        }
        assertTrue(OK);
        assertEquals(16,n);
        instance.clearTermsInCorpusCache();
    }
    
    /**
     * Test of cleanQuery method, of class nii.alloe.corpus.Corpus.
     */
    public void testCleanQuery() {
        System.out.println("cleanQuery");
        
        String s = "+ - && || ! ( ) { } [ ] ^ \" ~ * ? : \\";
        
        String expResult = "\\+ \\- \\&& \\|\\| \\! \\( \\) \\{ \\} \\[ \\] \\^ \\\" \\~ \\* \\? \\: \\\\";
        String result = Corpus.cleanQuery(s);
        assertEquals(expResult, result);
    }
    
    /**
     * Test of size method, of class nii.alloe.corpus.Corpus.
     */
    public void testSize() {
        System.out.println("size");
        
        int expResult = 5;
        int result = instance.size();
        assertEquals(expResult, result);
    }
    
    /**
     * Test of openCorpus method, of class nii.alloe.corpus.Corpus.
     */
    public void testOpenCorpus() throws Exception {
        System.out.println("openCorpus");
        
        File file = new File("test.idx");
        
        Corpus expResult = instance;
        Corpus result = Corpus.openCorpus(file);
        assertEquals(expResult.size(),result.size());
    }
    
    /**
     * Test of getDirectory method, of class nii.alloe.corpus.Corpus.
     */
    public void testGetDirectory() {
        System.out.println("getDirectory (no test)");    
    }
    
    /**
     * Test of sketchCorpus method, of class nii.alloe.corpus.Corpus.
     */
    public void testSketchCorpus() throws Exception {
        System.out.println("sketchCorpus (no test)");
    }
    
    /**
     * Test of getMaxSketchSize method, of class nii.alloe.corpus.Corpus.
     */
    public void testGetMaxSketchSize() {
        System.out.println("getMaxSketchSize (no test)");
    }
    
    /**
     * Test of setMaxSketchSize method, of class nii.alloe.corpus.Corpus.
     */
    public void testSetMaxSketchSize() {
        System.out.println("setMaxSketchSize (no test)");
    }
    
    /**
     * Test of getTrueCooccurences method, of class nii.alloe.corpus.Corpus.
     */
    public void testGetTrueCooccurences() {
        System.out.println("getTrueCooccurences (no test)");
    }
    
    /**
     * Test of getContexts method, of class nii.alloe.corpus.Corpus.
     */
    public void testGetContexts() {
        System.out.println("getContexts");
        
        String doc = "alice likes bob a lot";
        int wordWindow = 1;
        double progress = 0.0;
        
        Vector<String> expResult = new Vector<String>();
        expResult.add("alice likes bob a");
        Vector<String> result = instance.getContexts(doc, wordWindow, progress);
        assertEquals(expResult, result);
    }
    
    /**
     * Test of getTotalDocs method, of class nii.alloe.corpus.Corpus.
     */
    public void testGetTotalDocs() {
        System.out.println("getTotalDocs");
        
        int expResult = 5;
        int result = instance.getTotalDocs();
        assertEquals(expResult, result);
    }
    
}
