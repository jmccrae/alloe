/*
 * MatchPairTest.java
 * JUnit based test
 *
 * Created on March 14, 2008, 9:39 AM
 */

package nii.alloe.termvar;

import junit.framework.*;
import java.util.*;

/**
 *
 * @author john
 */
public class MatchPairTest extends TestCase {
    
    public MatchPairTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    /**
     * Test of joinWith method, of class nii.alloe.termvar.MatchPair.
     */
    public void testJoinWith() {
        System.out.println("joinWith");
        
        MatchPair mp2 = new MatchPair("abc","cde");
        MatchPair instance = new MatchPair("cba","edc");
        
        Collection<List<MatchPair>> expResult = null;
        Collection<List<MatchPair>> result = instance.joinWith(mp2);
        assertEquals(expResult, result);
        
    }

    /**
     * Test of convert method, of class nii.alloe.termvar.MatchPair.
     */
    public void testConvert() {
        System.out.println("convert");
        
        SortedSet<Match> matches = new TreeSet<Match>();
        matches.add(new Match(0,0));
        matches.add(new Match(1,8));
        matches.add(new Match(2,13));
        matches.add(new Match(3,14));
        matches.add(new Match(4,15));
        matches.add(new Match(5,16));
        matches.add(new Match(6,17));
        String s1 = "romania";
        String s2 = "repulic of romania";
        
        
        List<MatchPair> result = MatchPair.convert(matches, s1, s2);
        assertEquals("[[r->r], [->epulic ], [o->o], [->f ro], [mania->mania]]",result.toString());
    }
  
}
