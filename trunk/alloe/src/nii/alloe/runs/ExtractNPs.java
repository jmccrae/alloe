/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nii.alloe.runs;

import nii.alloe.preprocess.*;
import nii.alloe.preprocess.postag.*;
import java.io.*;
import java.net.URL;
import java.util.*;
import nii.alloe.preprocess.chunking.Chunker;
import nii.alloe.tools.strings.Strings;

/**
 *
 * @author john
 */
public class ExtractNPs {

    public static void main(String[] args) {
        try {
            args = new String[1];
            args[0] = "/home/john/test-text";
            if(args.length == 0) {
                System.err.println("Specify input file");
                System.exit(-1);
            }
            Tokeniser token = new Tokeniser(new File("/home/john/alloe/src/res/tokeniser.dfsm"));
            SentenceSplitter split = new SentenceSplitter(new File("/home/john/alloe/src/res/known-abbrev"));
            POSTagger postag = new POSTagger(new URL("file:/home/john/alloe/src/res/lexicon"),
                    new URL("file:/home/john/alloe/src/res/ruleset"));
            Chunker chunk = new Chunker(new URL("file:/home/john/alloe/src/res/chunking-rules")); 
            
            BufferedReader br = new BufferedReader(new FileReader(args[0]));
            String s;
            List<String> tokens = new LinkedList<String>();
            while((s = br.readLine()) != null) {
                String[] ts = token.tokenise(s);
                for(String t : ts) {
                    if(!t.matches("\\s+"))
                        tokens.add(t);
                }
            }
            List<List<String>> sents = split.split(tokens.toArray(new String[tokens.size()]));
            List<List<String[]>> posRes = postag.runTagger(sents);
            for(List<String[]> l1 : posRes) {
                for(String[] ss : l1) {
                    System.out.print(ss[0] +"/" + ss[1] + " ");
                }
            }
            for(List<String[]> s2 : posRes) {
                List<String> words = new LinkedList<String>();
                List<String> pos = new LinkedList<String>();
                List<String> tags = new LinkedList<String>();
                for(String[] s3 : s2) {
                    words.add(s3[0]);
                    pos.add(s3[1]);
                    tags.add("I");
                }
                tags = chunk.chunkSentence(words, tags, pos);
                for(String s3 : tags) {
                    System.out.print(s3);
                }
                System.out.println("");
            }
        } catch(Exception x) {
            x.printStackTrace();
        }
    }
}
