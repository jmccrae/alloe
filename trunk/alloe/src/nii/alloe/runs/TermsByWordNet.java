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
import edu.smu.tspell.wordnet.*;
import java.io.*;
import nii.alloe.corpus.*;

/**
 * @author John McCrae, National Institute of Informatics
 */
public class TermsByWordNet {

    public static void main(String[] args) {
        if(args.length != 2) {
            System.err.println("Usage: java TermsByWordNet termList output-prefix");
            System.exit(-1);
        }
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(args[0]));
            TermList tl = (TermList)ois.readObject();
            ois.close();
            
            TermPairSet syns = new TermPairSet();
            TermPairSet hyps = new TermPairSet();
            
            System.setProperty("wordnet.database.dir", "/home/john/Desktop/WordNet-3.0/dict/");
            WordNetDatabase wndb = WordNetDatabase.getFileInstance();
            
            for(String term : tl) {
                Synset[] synsets = wndb.getSynsets(term);
                
                for(Synset synset : synsets) {
                    doHyp(synset, tl, hyps,term);
                    for(String form : synset.getWordForms()) {
                        if(tl.contains(form)) {
                            syns.add(term,form);
                            syns.add(form,term);
                        }
                    }
                }
            }
            
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(args[1] + "-syns.atps"));
            oos.writeObject(syns);
            oos.close();
            oos = new ObjectOutputStream(new FileOutputStream(args[1] + "-hyps.atps"));
            oos.writeObject(hyps);
            oos.close();
        } catch(Exception x) {
            x.printStackTrace();
        }
    }
    
    private static void doHyp(Synset synset, TermList tl, TermPairSet hyps, String base) {
        if(!(synset instanceof NounSynset))
            return; 
        Synset[] hypers = ((NounSynset)synset).getHypernyms();
        
        for(Synset ss : hypers) {
            for(String form : ss.getWordForms()) {
                if(tl.contains(form)) {
                    hyps.add(base,form);
                }
            }
            doHyp(ss, tl, hyps, base);
        }
    }
}
