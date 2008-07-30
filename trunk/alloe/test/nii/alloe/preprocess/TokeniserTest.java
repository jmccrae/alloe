/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nii.alloe.preprocess;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 * @author john
 */
public class TokeniserTest extends TestCase {
    
    public TokeniserTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(TokeniserTest.class);
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
     * Test of tokenise method, of class Tokeniser.
     */
    public void testTokenise() {
        try {
            System.out.println("tokenise");
            String s1 = "Hello, my name is John. I live in Tokyo, Japan; and am 24 years-old. I don't like dogs.";
            //Tokeniser instance = new Tokeniser(TokeniserTest.class.getResource("res/tokeniser.dfsm"));
            Tokeniser instance = new Tokeniser(new java.io.File("/home/john/alloe/src/res/tokeniser.dfsm"));
            String[] result = instance.tokenise(s1);
            System.out.println(nii.alloe.tools.strings.Strings.join("|", result));
        } catch(Exception x) {
            x.printStackTrace();
            fail(x.getMessage());
        }
    }

}
