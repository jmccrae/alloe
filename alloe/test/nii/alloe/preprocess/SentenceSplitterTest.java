/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nii.alloe.preprocess;

import java.util.List;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 * @author john
 */
public class SentenceSplitterTest extends TestCase {
    
    public SentenceSplitterTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(SentenceSplitterTest.class);
        return suite;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }


    /**
     * Test of split method, of class SentenceSplitter.
     */
    public void testSplit() {
        System.out.println("split");
        try {
            String s1 = "Hello, my name is John. I live in Tokyo, Japan; and am 24 years-old. I don't like dogs.";
            Tokeniser tokeniser = new Tokeniser(new java.io.File("/home/john/alloe/src/res/tokeniser.dfsm"));
            String[] tokens = tokeniser.tokenise(s1);
            SentenceSplitter instance = new SentenceSplitter(new java.io.File("/home/john/alloe/src/res/known-abbrev"));
            List<List<String>> result = instance.split(tokens);
            for(List<String> sentence : result) {
                System.out.println(nii.alloe.tools.strings.Strings.join("|", sentence));
            }
        } catch(Exception x) {
            x.printStackTrace();
            fail(x.getMessage());
        }
    }


}
