/*
 * QuickRegexTest.java
 * JUnit based test
 *
 * Created on 17 January 2008, 16:46
 */

package nii.alloe.niceties;

import java.util.regex.PatternSyntaxException;
import junit.framework.*;
import java.util.*;
import java.util.regex.*;

/**
 *
 * @author john
 */
public class QuickRegexTest extends TestCase {
    
    Random r;
    
    public QuickRegexTest(String testName) {
        super(testName);
        r = new Random();
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    
    private String makeRandomString(int n) {
        char []cs = new char[n];
        for(int i = 0; i < n; i++) {
            cs[i] = (char)(r.nextInt(94) + 32);
        }
        
        return new String(cs);
    }
    
    /**
     * Test of matches method, of class nii.alloe.niceties.QuickRegex.
     */
    public void testMatches() {
        System.out.println("matches");
        
        int N = 50000, n = 100;
        String[] strings = new String[N];
        String[] regexs = new String[N];
        
        for(int i = 0; i < N; i++) {
            strings[i] = makeRandomString(n);
            boolean success = false;
            while(!success) {
                success = true;
                regexs[i] = makeRandomString(n);
                try {
                    Pattern.compile(regexs[i]);
                } catch(PatternSyntaxException x) {
                    success = false;
                }
            }
        }
        
        long time = System.nanoTime();
        for(int i = 0; i < N; i++) {
            strings[i].matches(regexs[i]);
        }
        System.out.println("Java Native:" + (System.nanoTime() - time));
        
        time = System.nanoTime();
        for(int i = 0; i < N; i++) {
            QuickRegex.matches(strings[i],regexs[i]);
        }
        System.out.println("Quick Regex:" + (System.nanoTime() - time));
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
