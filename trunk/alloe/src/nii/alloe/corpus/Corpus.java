package nii.alloe.corpus;
import java.util.*;
import java.io.*;
import java.util.regex.*;
import org.apache.lucene.index.*;
import org.apache.lucene.store.*;
import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.standard.*;
import org.apache.lucene.document.*;
import org.apache.lucene.search.*;
import org.apache.lucene.queryParser.*;
/**
 * An indexed corpus.
 *
 * @author John McCrae, National Institute of Informatics
 */
public class Corpus implements Serializable {
    
    private transient IndexWriter indexWriter;
    private transient IndexSearcher indexSearcher;
    private Vector<String> terms;
    private Directory directory;
    
    /** Creates a new instance of Corpus */
    public Corpus(Vector<String> terms) {
        this.terms = terms;
    }
    
    /** Opens the corpus so that new documents can be added */
    public void openIndex() throws IOException {
        if(directory == null)
            directory = new RAMDirectory();
        Analyzer analyzer = new StandardAnalyzer();
        indexWriter = new IndexWriter(directory, analyzer, true);
    }
    
    /** Add a new document to corpus
     * @param contents The text of the new document
     * @throws IllegalStateException if {@link #openIndex()} has not been called
     */
    public void addDoc(String contents) throws IOException {
        if(indexWriter == null)
            throw new IllegalStateException("Attempting to add document to closed index");
        Document d = new Document();
        d.add(new Field("contents", contents, Field.Store.YES, Field.Index.TOKENIZED));
        Iterator<String> termIter = terms.iterator();
        while(termIter.hasNext()) {
            String term = termIter.next();
            
            if(contents.contains(term)) {
                d.add(new Field("term",term, Field.Store.YES, Field.Index.TOKENIZED));
            }
        }
        indexWriter.addDocument(d);
    }
    
    /** Close the corpus, after which no more documents can be added */
    public void closeIndex() throws IOException {
        indexWriter.optimize();
        indexWriter.close();
        indexWriter = null;
        indexSearcher = new IndexSearcher(directory);
    }
    
    /** Get all contexts containing term1 and term2 */
    public Iterator<String> getContextsForTerms(String term1, String term2) {
        try {
            QueryParser qp = new QueryParser("term", new StandardAnalyzer());
            Query q = qp.parse("\"" + term1 + "\" AND \"" +  term2 + "\"");
            Hits hits = indexSearcher.search(q);
            return new HitsIterator(hits);
        } catch(Exception x) {
            x.printStackTrace();
            return null;
        }
        
    }
    
    /** Get all contexts which match the pattern p */
    public Iterator<String> getContextsForPattern(nii.alloe.corpus.pattern.Pattern p) {
        try {
            QueryParser qp = new QueryParser("contents", new StandardAnalyzer());
            Query q = qp.parse(p.getQuery());
            Hits hits = indexSearcher.search(q);
            return new HitsIterator(hits);
        } catch(Exception x) {
            x.printStackTrace();
            return null;
        }
    }
    
    /** Get all the contexts matching pattern with term1 and term inserted */
    public Iterator<String> getContextsForTermInPattern(nii.alloe.corpus.pattern.Pattern p, String term1, String term2) {
        try {
            String[] queries = { p.getQueryWithTerms(term1, term2),
            "\"" + term1 + "\" AND \"" +  term2 + "\"" };
            String[] fields = { "contents", "terms" };
            MultiFieldQueryParser qp = new MultiFieldQueryParser(fields, new StandardAnalyzer());
            Query q = MultiFieldQueryParser.parse(queries, fields, new StandardAnalyzer());
            
            Hits hits = indexSearcher.search(q);
            return new HitsIterator(hits);
        } catch(Exception x) {
            x.printStackTrace();
            return null;
        }
    }
    
    /** @return true if term1 occurs in the corpus */
    public boolean isTermInCorpus(String term1) {
        try {
            QueryParser  qp = new QueryParser("term", new StandardAnalyzer());
            Query q = qp.parse("\"" + term1 + "\"");
            Hits hits = indexSearcher.search(q);
            return hits.length() != 0;
        } catch(Exception x) {
            x.printStackTrace();
            return false;
        }
    }
    
    /** @return true if term1 and term2 occur in the same document in the corpus */
    public boolean areTermsInCorpus(String term1, String term2) {
        try {
            QueryParser qp = new QueryParser("term", new StandardAnalyzer());
            Query q = qp.parse("\"" + term1 + "\" AND \"" +  term2 + "\"");
            Hits hits = indexSearcher.search(q);
            return hits.length() != 0;
        } catch(Exception x) {
            x.printStackTrace();
            return false;
        }
    }
    
    /** @return number of documents in the corpus */
    public int size() {
        return indexWriter.docCount();
    }
    
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        // We need to restore indexSearcher after loading
        indexSearcher = new IndexSearcher(directory);
    }
    
    private class HitsIterator implements Iterator<String> {
        Hits hits;
        int i;
        
        HitsIterator(Hits hits) {
            this.hits = hits;
            i = 0;
        }
        
        public void remove() {
            throw new UnsupportedOperationException();
        }
        
        public String next() {
            try {
                Document d = hits.doc(i);
                String s = d.getField("term").stringValue();
                return hits.doc(i++).getField("contents").stringValue();
            } catch(IOException x) {
                x.printStackTrace();
                throw new RuntimeException("An IO Exception occurred");
            }
        }
        
        public boolean hasNext() {
            return i < hits.length();
        }
    }
    
    /** Returns only those areas in a fixed window of a particular term */
    public Vector<String> getContexts(String doc, int wordWindow) {
        TreeSet<Integer> bounds = new TreeSet<Integer>();
        doc = doc.toLowerCase();
        
        Iterator<String> termIter = terms.iterator();
        while(termIter.hasNext()) {
            String term = termIter.next().toLowerCase();
            int idx = doc.indexOf(term,0);
            while(idx >= 0) {
                if((idx > 0 && !Character.isWhitespace(doc.charAt(idx-1))) ||
                        (idx + term.length() < doc.length() && 
                        !Character.isWhitespace(doc.charAt(idx + term.length())))) {
                    idx = doc.indexOf(term, idx + term.length());
                    continue;
                }
                int before = findNWordsBeforeAfter(true,doc, idx, wordWindow);
                int after = findNWordsBeforeAfter(false,doc, idx + term.length(), wordWindow);
                boolean beforeFound = false, afterFound = false;
                
                Iterator<Integer> bIter = bounds.iterator();
                while(bIter.hasNext()) {
                    int oldBefore = bIter.next();
                    int oldAfter = bIter.next();
                    
                    if(!beforeFound && oldBefore < before && before <= oldAfter) {
                        before = oldBefore;
                        beforeFound = true;
                    } 
                    if(!afterFound &&  after >= oldBefore && oldAfter > after) {
                        after = oldAfter;
                        afterFound = true;
                    }
                    if(afterFound && beforeFound) {
                        break;
                    }
                }
                bounds.add(before);
                bounds.add(after);
                // Clone to avoid those concurrent mod exes (grrr...)
                TreeSet<Integer> inBounds = new TreeSet<Integer>(bounds.subSet(before,false,after,false));
                bounds.removeAll(inBounds);
                idx = doc.indexOf(term, idx + term.length());
            }
        }
        Vector<String> rval = new Vector<String>(bounds.size() / 2);
        Iterator<Integer> bIter = bounds.iterator();
        while(bIter.hasNext()) {
            int before = bIter.next();
            int after = bIter.next();
            
            rval.add(doc.substring(before, after));
        }
        
        return rval;
    }
    
    private int findNWordsBeforeAfter(boolean before, String doc, int idx, int window) {
        String regex;
        if(before)
            regex = ".*?(";
        else // after
            regex = "(";
        for(int i = 0; i < window; i++) {
            regex = regex + (before ? "\\w+\\W+" : "\\W+\\w+");
        }
        if(before)
            regex = regex + ")";
        else // after
            regex = regex + ").*";
        String doc2;
        if(before)
            doc2 = doc.substring(0,idx);
        else //after
            doc2 = doc.substring(idx,doc.length());
        Matcher m = java.util.regex.Pattern.compile(regex).matcher(doc2);
        if(!m.matches()) {
            if(before)
                return 0;
            else
                return doc.length();
        } else {
            if(before)
                return idx - m.group(1).length();
            else //after
                return idx + m.group(1).length();
        }
    }
    
}
