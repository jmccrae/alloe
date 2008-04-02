/*
 * FindMatchingsTest.java
 * JUnit based test
 *
 * Created on 13 March 2008, 13:06
 */

package nii.alloe.termvar;

import junit.framework.*;
import java.awt.datatransfer.StringSelection;
import java.util.*;
import nii.alloe.niceties.*;
import nii.alloe.niceties.lattice.*;

/**
 *
 * @author john
 */
public class FindMatchingsTest extends TestCase {
    
    public FindMatchingsTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    /**
     * Test of findMatchings method, of class nii.alloe.termvar.FindMatchings.
     */
    public void testFindMatchingsAsStrings() {
        System.out.println("findMatchingsAsStrings");
        
        StringList string1 = new StringList("republic of romania");
        StringList string2 = new StringList("romania");
        FindMatchings instance = new FindMatchings();
        
        
        Vector<String[]> result = instance.findMatchingsAsStrings(string1, string2);
        for(String[] ss : result) {
            System.out.println(ss[0]);
            System.out.println(ss[1]);
            System.out.println();
        }
        
    }

    /**
     * Test of findMatchings method, of class nii.alloe.termvar.FindMatchings.
     */
    public void testFindMatchings() {
        System.out.println("findMatchings");
       
        StringList string1 = new StringList("republic of romania");
        StringList string2 = new StringList("romania");
        StringList string3 = new StringList("republic of kazakhstan");
        StringList string4 = new StringList("kazakhstan");
        
        Collection<TreeSet<Match>> result = FindMatchings.findMatchings(string1, string2);
        Collection<TreeSet<Match>> result2 = FindMatchings.findMatchings(string3, string4);
        for(TreeSet<Match> match : result) {
            List<MatchCharPair> mcp1 = MatchCharPair.getMatchListFromStrings(match, string1.toString(), string2.toString());
            for(TreeSet<Match> match2 : result2) {
                List<MatchCharPair> mcp2 = MatchCharPair.getMatchListFromStrings(match2,string3.toString(),string4.toString());
                Collection<TreeSet<Match>> result3 = FindMatchings.findMatchings(mcp1,mcp2);
                for(TreeSet<Match> match3 : result3) {
                    System.out.println(MatchCharPair.getMatchListFromStrings(match3,mcp1,mcp2));
                }
            }
        }
    }
    
}
