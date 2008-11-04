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

/**
 *
 * @author john
 */
public class ExtractNPs {

    public static void main(String[] args) {
        try {
            int WINDOW = 8;
            args = new String[1];
            args[0] = "/media/USB DISK/ProMed.unzoned.txt";
            if (args.length == 0) {
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
            LOOP:
            while ((s = br.readLine()) != null) {
                List<String> tokens = new LinkedList<String>();
                while (!s.matches("\\s*")) {
                    String[] ts = token.tokenise(s);
                    for (String t : ts) {
                        if (!t.matches("\\s+")) {
                            tokens.add(t);
                        }
                    }
                    s = br.readLine();
                    if (s == null) {
                        break LOOP;
                    }
                }

                List<List<String>> sents = split.split(tokens.toArray(new String[tokens.size()]));
                List<List<String[]>> posRes = postag.runTagger(sents);


                for (List<String[]> s2 : posRes) {
                    List<String> words = new LinkedList<String>();
                    List<String> pos = new LinkedList<String>();
                    List<String> tags = new LinkedList<String>();
                    for (String[] s3 : s2) {
                        words.add(s3[0]);
                        pos.add(s3[1]);
                        tags.add("I");
                    }
                    tags = chunk.chunkSentence(words, tags, pos);

                    LinkedList<Object[]> closeNPs = new LinkedList<Object[]>();

                    boolean inBaseNP = false;

                    String phrase = "";

                    for (int i = 0; i < words.size(); ++i) {
                        Iterator<Object[]> iter = closeNPs.iterator();
                        while (iter.hasNext()) {
                            Object[] closeNP = iter.next();
                            if (((Integer) closeNP[1]) < i - WINDOW) {
                                iter.remove();
                            }
                        }
                        String ct = (String) tags.get(i);

                        if (inBaseNP) {
                            if (ct.equals("B")) {
                                if (phrase.length() > 0) {
                                    Object[] oo = {phrase, i};
                                    for (Object[] closeNP : closeNPs) {
                                        System.out.println(closeNP[0] + " =*#= " + phrase);
                                    }
                                    closeNPs.add(oo);
                                }
                                if (!pos.get(i).equals("DT") && pos.get(i).matches("\\w+")) {
                                    phrase = words.get(i);
                                }
                            } else if (ct.equals("O")) {
                                if (phrase.length() > 0) {
                                    Object[] oo = {phrase, i};
                                    for (Object[] closeNP : closeNPs) {
                                        System.out.println(closeNP[0] + " =*#= " + phrase);
                                    }
                                    closeNPs.add(oo);
                                }
                                phrase = "";
                                inBaseNP = false;
                            } else if (ct.equals("I")) {
                                if (!pos.get(i).equals("DT") && pos.get(i).matches("\\w+")) {
                                    if (phrase.length() > 0) {
                                        phrase = phrase + " " + words.get(i);
                                    } else {
                                        phrase = words.get(i);
                                    }
                                }
                            }
                        } else {
                            if (ct.equals("B") || ct.equals("I")) {
                                if (!pos.get(i).equals("DT") && pos.get(i).matches("\\w+")) {
                                    phrase = phrase + words.get(i);
                                }
                                inBaseNP = true;
                            }
                        }
                    }

                    if (inBaseNP) {
                        Object[] oo = {phrase, words.size()};
                        for (Object[] closeNP : closeNPs) {
                            System.out.println(closeNP[0] + " =*#= " + phrase);
                        }
                        closeNPs.add(oo);
                    }
                    closeNPs.clear();

                }
            }

        } catch (Exception x) {
            x.printStackTrace();
        }
    }
}
