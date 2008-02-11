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
    }



    /**
     * Test of getContextsForTerms method, of class nii.aloe.corpus.Corpus.
     */
    public void testGetContextsForTerms() {
        System.out.println("getContextsForTerms");
        
        String term1 = "alice";
        String term2 = "joe bob";
        
        
        Iterator<String> result = instance.getContextsForTerms(term1, term2);
        assertEquals(result.next(), "alice hates joe bob");
        assertFalse(result.hasNext());
       
    }

    /**
     * Test of getContextsForPattern method, of class nii.aloe.corpus.Corpus.
     */
    public void testGetContextsForPattern() {
        System.out.println("getContextsForPattern");
        
        Pattern p = new Pattern("1 2, is hillbilly");
        
        Iterator<String> result = instance.getContextsForPattern(p);
        assertEquals(result.next(), "joe bob is a hillbilly");
        assertFalse(result.hasNext());
    }
    
}
