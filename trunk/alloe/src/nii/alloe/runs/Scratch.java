/* 
 * Copyright (c) 2008, National Institute of Informatics
 *
 * This file is part of SRL, and is free
 * software, licenced under the GNU Library General Public License,
 * Version 2, June 1991.
 *
 * A copy of this licence is included in the distribution in the file
 * licence.html, and is also available at http://www.fsf.org/licensing/licenses/info/GPLv2.html.
*/
package nii.alloe.runs;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import nii.alloe.corpus.Corpus;
import nii.alloe.corpus.TermPairSet;
import nii.alloe.corpus.pattern.Pattern;
import nii.alloe.corpus.pattern.WordNetMetric;
import nii.alloe.tools.struct.Counter;

/**
 * @author John McCrae, National Institute of Informatics
 */
public class Scratch {

    public static void main(String[] args) {
        try {
            Corpus corpus = Corpus.openCorpus(new File("/home/john/promed/index"));
            List<Pattern> patterns = new LinkedList<Pattern>();
            patterns.add(new Pattern("1 such as 2"));
            patterns.add(new Pattern("such 1 as 2"));
            patterns.add(new Pattern("2 and other 1"));
            patterns.add(new Pattern("2 or other 1"));
            patterns.add(new Pattern("2 including 1"));
            patterns.add(new Pattern("2 especially 1"));
            WordNetMetric wnm = new WordNetMetric(corpus);
            
            for(Pattern pattern : patterns) {
                double[] pair = wnm.returnBoth(pattern);
                System.out.println(pattern.toString() + "," + pair[0] + "," + pair[1] + "," + pair[0]/pair[1]);
            }
        } catch(Exception x) {
            x.printStackTrace();
        }
        
    }
}
