/*
 * CorpusSaveTest.java
 * JUnit based test
 *
 * Created on April 1, 2008, 9:12 PM
 */

package nii.alloe.corpus;

import junit.framework.*;
import java.util.*;
import java.io.*;
import nii.alloe.corpus.pattern.*;
import nii.alloe.corpus.analyzer.*;
import nii.alloe.niceties.Strings;
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
public class CorpusSaveTest extends TestCase {
    
    public CorpusSaveTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }
    
}
