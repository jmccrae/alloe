package nii.alloe.runs;
import nii.alloe.corpus.*;
import java.io.*;
import java.util.*;

/**
 * Wikipedia Synonymy & Hypernym Information Extraction
 *
 * @author John McCrae, National Institute of Informatics
 */
public class WPSHIE {
    
    TermPairSet hyps;
    TermPairSet syns;
    TreeSet<String> terms;
    Corpus corpus;
    
    /** Creates a new instance of WPSHIE */
    public WPSHIE() {
        terms = new TreeSet<String>();
        loadSyns();
        loadHyps();
        consolidateHyps();
        System.out.println("Number of Terms = " + terms.size());
        System.out.println("Number of Synonym Links = " +  syns.size());
        System.out.println("Number of Hypernym Links = " + hyps.size());
        
        try {
            ObjectOutputStream oos2 = new ObjectOutputStream(new FileOutputStream("/home/john/wpshie/terms"));
            TermList termsVec = new TermList(terms);
            oos2.writeObject(termsVec);
            oos2.close();
            
            oos2 = new ObjectOutputStream(new FileOutputStream("/home/john/wpshie/syns.atps"));
            oos2.writeObject(syns);
            oos2.close();
            
            oos2 = new ObjectOutputStream(new FileOutputStream("/home/john/wpshie/hyps.atps"));
            oos2.writeObject(hyps);
            oos2.close();
            
//            loadCorpus();
//            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("/e/wp/wp/corpus"));
//            oos.writeObject(corpus);
//            oos.writeObject(hyps);
//            oos.writeObject(syns);
//            oos.close();
//            
        } catch(Exception x) {
            x.printStackTrace();
            System.exit(-1);
        }
    }
    
    public static void main(String[] args) {
        new WPSHIE();
    }
    
    void loadSyns() {
        syns = new TermPairSet();
        try {
            BufferedReader in = new BufferedReader(new FileReader("/home/john/wpshie/syns"));
            String s = in.readLine();
            while(s != null) {
                if(!s.matches(".*\\w.*")) {
                    s = in.readLine();
                    continue;
                }
                String[] ss = s.split(", ");
                for(int i = 0; i < ss.length; i++) {
                    for(int j = 0; j < ss.length; j++) {
                        syns.add(ss[i],ss[j]);
                    }
                    terms.add(ss[i]);
                }
                s = in.readLine();
            }
        } catch(IOException x) {
            x.printStackTrace();
            System.exit(-1);
        }
    }
    
    void loadHyps() {
        hyps = new TermPairSet();
        try {
            BufferedReader in = new BufferedReader(new FileReader("/home/john/wpshie/hyps"));
            String s = in.readLine();
            while(s != null) {
                String[] ss = s.split(" < " );
                LinkedList<String> seen = new LinkedList<String>();
                for(int i = 0; i < ss.length; i++) {
                    Iterator<String> seeniter = seen.iterator();
                    while(seeniter.hasNext()) {
                        hyps.add(ss[i],seeniter.next());
                    }
                    seen.add(ss[i]);
                    if(!terms.contains(ss[i])) {
                        throw new RuntimeException("Hypernym term not found in synset file: " + ss[i]);
                    }
                }
                s = in.readLine();
            }
        } catch(IOException x) {
            x.printStackTrace();
            System.exit(-1);
        }
    }
    
    private String term1, term2;
    private LinkedList<String> toAdd;
    void consolidateHyps() {
        toAdd = new LinkedList<String>();
        syns.forEachPair(new EachTermPairAction() {
            public void doAction(String t1, String t2) {
                term1 = t1;
                term2 = t2;
                hyps.forEachRHS(term1, new EachTermPairAction() {
                    public void doAction(String t1, String term3) {
                        toAdd.add(term2 + " => " + term3);
                    }
                });
                hyps.forEachRHS(term2, new EachTermPairAction() {
                    public void doAction(String t1, String term3) {
                        toAdd.add(term1 + " => " + term3);
                    }
                });
            }
        });
        Iterator<String> taIter = toAdd.iterator();
        while(taIter.hasNext()) {
            String[] ss = taIter.next().split(" => ");
            hyps.add(ss[0],ss[1]);
        }
    }
    
    void loadCorpus() {
        corpus = new Corpus(new TermList(terms), "/e/wp/wp/corpus.idx");
        try {
            corpus.openIndex(true);
            BufferedReader in = new BufferedReader(new FileReader("/e/wp/wp/corpus.txt"),256);
            String s = in.readLine();
            while(s != null) {
                if(!s.matches(".*\\w.*")) {
                    s = in.readLine();
                    continue;
                }
                String[] ss = s.split("[\\.;]");
                for(int i = 0; i < ss.length; i++) {
                    Vector<String> ss2 = corpus.getContexts(ss[i],3);
                    Iterator<String> siter = ss2.iterator();
                    while(siter.hasNext()) {
                        String s2 = siter.next();
                        System.out.println("Adding Context: " + s2);
                        corpus.addDoc(s2);
                    }
                }
                s = in.readLine();
                if(s.length() > 200) {
                    String t = in.readLine();
                    s = s + " " + t;
                    while(t.length() > 200) {
                        t = in.readLine();
                        s = s + " " + t;
                    }
                }
                s.replaceAll("\\s\\s+", " ");
            }
            corpus.closeIndex();
        } catch(IOException x) {
            x.printStackTrace();
            System.exit(-1);
        }
    }
}
