package nii.alloe.corpus;
import java.util.*;
import java.io.*;
import nii.alloe.corpus.pattern.*;
import nii.alloe.corpus.analyzer.*;
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
    public TermList terms;
    private transient Directory directory;
    private String indexFile;
    private transient HashMap<String,TreeSet<Integer>> termHits;
    
    /** Creates a new instance of Corpus */
    public Corpus(TermList terms, String indexFile) {
        this.terms = terms;
        this.indexFile = indexFile;
        termHits = new HashMap<String,TreeSet<Integer>>(terms.size());
    }
    
    /** Opens the corpus so that new documents can be added
     * @param newIndex If true any index on existing path will be removed
     */
    public void openIndex(boolean newIndex) throws IOException {
        indexWriter = new IndexWriter(indexFile, new AlloeAnalyzer(), newIndex);
    }
    
    /** Add a new document to corpus
     * @param contents The text of the new document
     * @throws IllegalStateException if {@link #openIndex()} has not been called
     */
    public void addDoc(String contents) throws IOException {
        if(indexWriter == null)
            throw new IllegalStateException("Attempting to add document to closed index");
        Document d = new Document();
        d.add(new Field("contents", contents.toLowerCase(), Field.Store.YES, Field.Index.TOKENIZED));
        Iterator<String> termIter = terms.iterator();
        while(termIter.hasNext()) {
            String term = termIter.next();
            
            if(contents.matches(".*\\b" + term.toLowerCase() + "\\b.*")) {
                d.add(new Field("term",term.toLowerCase(), Field.Store.YES, Field.Index.TOKENIZED));
            }
        }
        indexWriter.addDocument(d);
    }
    
    /** Close the corpus, after which no more documents can be added */
    public void closeIndex() throws IOException {
        indexWriter.optimize();
        Directory d = indexWriter.getDirectory();
        indexWriter.close();
        
        indexSearcher = new IndexSearcher(d);
        indexWriter = null;
    }
    
    private HitsIterator queryTerm(String term) {
        TreeSet<Integer> h = termHits.get(term);
        if(h == null) {
            try {
                QueryParser qp = new QueryParser("term", new AlloeAnalyzer());
                Query q = qp.parse("\"" + cleanQuery(term) + "\"");
                HitsIterator hi = new HitsIterator();
                indexSearcher.search(q,hi);
                termHits.put(term,hi.hits);
                return hi;
            } catch(Exception x) {
                x.printStackTrace();
                return null;
            }
        } else {
            return new HitsIterator(h);
        }
    }
    
    private HitsIterator queryTerms(String term1, String term2) {
        TreeSet<Integer> h1 = queryTerm(term1).hits;
        TreeSet<Integer> h2 = queryTerm(term2).hits;
        TreeSet<Integer> hr = (TreeSet<Integer>)h1.clone();
        hr.retainAll(h2);
        return new HitsIterator(hr);
    }
    
    /** Return the occurences of a particular string */
    public int getHitsForTerm(String term) {
        return queryTerm(term).hits.size();
    }
    
    /** Get all contexts containing term1 and term2 */
    public Iterator<String> getContextsForTerms(String term1, String term2) {
        return queryTerms(term1,term2);
    }
    
    /** Get all contexts which match the pattern p */
    public Iterator<String> getContextsForPattern(nii.alloe.corpus.pattern.Pattern p) {
        try {
            QueryParser qp = new QueryParser("contents", new AlloeAnalyzer());
            Query q = qp.parse(cleanQuery2(p.getQuery()));
            HitsIterator hi = new HitsIterator();
            indexSearcher.search(q,hi);
            return hi;
        } catch(Exception x) {
            x.printStackTrace();
            return null;
        }
    }
    
    /** Get all the contexts matching pattern with term1 and term inserted */
    public Iterator<String> getContextsForTermInPattern(nii.alloe.corpus.pattern.Pattern p, String term1, String term2) {
        try {
            String[] queries = { cleanQuery2(p.getQueryWithTerms(term1, term2)),
            "\"" + cleanQuery(term1) + "\" AND \"" +  cleanQuery(term2) + "\"" };
            String[] fields = { "contents", "terms" };
            MultiFieldQueryParser qp = new MultiFieldQueryParser(fields, new AlloeAnalyzer());
            Query q = MultiFieldQueryParser.parse(queries, fields, new AlloeAnalyzer());
            
            HitsIterator hi = new HitsIterator();
            indexSearcher.search(q,hi);
            return hi;
        } catch(Exception x) {
            x.printStackTrace();
            return null;
        }
    }
    
    /** Call this if you want to check every term pair with a pattern. This first
     * initializes the query and then call {@link #getContextsForTermPrepared}.
     * @return The prepared query data
     */
    public Object prepareQueryPattern(Pattern p) {
        try {
            QueryParser qp = new QueryParser("contents", new AlloeAnalyzer());
            Query q = qp.parse(cleanQuery2(p.getQuery()));
            HitsIterator hi = new PreparedQuery();
            indexSearcher.search(q,hi);
            return hi;
        } catch(Exception x) {
            x.printStackTrace();
            return null;
        }
    }
    
    /** Find the number of hits found by a prepared query
     * @param query The query object as returned from prepareQueryPattern
     */
    public int getPreparedQueryHits(Object query) {
        if(!(query instanceof PreparedQuery))
            throw new IllegalArgumentException("query passed to getPreparedQueryHits not valid");
        return ((PreparedQuery)query).preparedHits.size();
    }
    
    /** Get the string iterator for a prepared query
     * @param query The query object as returned from prepareQueryPattern
     */
    public Iterator<String> getPreparedQuery(Object query) {
        if(!(query instanceof PreparedQuery))
            throw new IllegalArgumentException("query passed to getPreparedQuery not valid");
        return new HitsIterator(((PreparedQuery)query).preparedHits);
    }
    
    /** Find term pairs on a prepared query. = getContextsForTermPrepared(term1,term2,query,true)
     * @see #preparedQueryPattern
     * @param query The query object as returned from prepareQueryPattern
     */
    public Iterator<String> getContextsForTermPrepared(String term1, String term2, Object query) {
        return getContextsForTermPrepared(term1,term2,query,true);
    }
    
    /** Find term pairs on a prepared query.
     *
     * @see #preparedQueryPattern
     * @param query The query object as returned from prepareQueryPattern
     * @param cache If true attempt to cache term hits */
    public Iterator<String> getContextsForTermPrepared(String term1, String term2, Object query, boolean cache) {
        if(!(query instanceof PreparedQuery))
            throw new IllegalArgumentException("query passed to getContextsForTermPrepared not valid");
        
        if((termHits.get(term1) == null || termHits.get(term2) == null) && !cache) {
            try {
                QueryParser qp = new QueryParser("term", new AlloeAnalyzer());
                Query q = qp.parse("\"" + cleanQuery(term1) + "\" AND \"" +  cleanQuery(term2) + "\"");
                HitsIterator hi = ((PreparedQuery)query).preparedCopy();
                indexSearcher.search(q,hi);
                return hi;
            } catch(Exception x) {
                x.printStackTrace();
                return null;
            }
        } else {
            HitsIterator th = queryTerms(term1,term2);
            TreeSet<Integer> th2 = (TreeSet<Integer>)th.hits.clone();
            th2.retainAll(((PreparedQuery)query).preparedHits);
            return new HitsIterator(th2);
        }
    }
    
    /** @return true if term1 occurs in the corpus */
    public boolean isTermInCorpus(String term1) {
        HitsIterator hi = queryTerm(term1);
        return hi.hits.size() > 0;
        
    }
    
    /** @return true if term1 and term2 occur in the same document in the corpus */
    public boolean areTermsInCorpus(String term1, String term2) {
        HitsIterator hi = queryTerms(term1,term2);
        return hi.hits.size() > 0;
    }
    
    /** Put to lower case and bs all reserved terms */
    public static String cleanQuery(String s) {
        s = s.toLowerCase();
        s = s.replaceAll("([\\+\\-\\!\\(\\)\\[\\]\\^\\\"\\~\\?\\:\\\\]|\\|\\||\\&\\&)", "\\\\$1");
        return s;
    }
    
    private String cleanQuery2(String s) {
        s = s.toLowerCase();
        s = s.replaceAll("([\\+\\-\\!\\(\\)\\[\\]\\^\\\"\\~\\?\\:\\\\\\*]|\\|\\||\\&\\&)", "\\\\$1");
        s = s.replaceAll("^\\s*","");
        s = s.replaceAll("\\s*$", "");
        s = s.replaceAll("\\s+", " AND ");
        return s;
    }
    
    /** @return number of documents in the corpus */
    public int size() {
        if(indexWriter != null)
            return indexWriter.docCount();
        else {
            try {
                return indexSearcher.maxDoc();
            } catch(IOException x) {
                x.printStackTrace();
                return -1;
            }
        }
    }
    
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        // We need to restore indexSearcher after loading
        indexSearcher = new IndexSearcher(indexFile);
        termHits = new HashMap<String,TreeSet<Integer>>();
    }
    
    
    public Directory getDirectory() {
        return directory;
    }
    class HitsIterator extends HitCollector implements Iterator<String> {
        TreeSet<Integer> hits;
        Iterator<Integer> i;
        
        HitsIterator() {
            hits = new TreeSet<Integer>();
            i = null;
        }
        
        HitsIterator(TreeSet<Integer> hits) {
            this.hits = hits;
            i = null;
        }
        
        public void collect(int doc, float score) {
            hits.add(doc);
        }
        
        public void remove() {
            throw new UnsupportedOperationException();
        }
        
        public String next() {
            if(i == null)
                i = hits.iterator();
            try {
                Document d = indexSearcher.doc(i.next());
                //String s = d.getField("term").stringValue();
                return d.getField("contents").stringValue();
            } catch(IOException x) {
                x.printStackTrace();
                throw new RuntimeException("An IO Exception occurred");
            }
        }
        
        public boolean hasNext() {
            if(i == null)
                i = hits.iterator();
            return i.hasNext();
        }
    }
    
    private class PreparedQuery extends HitsIterator {
        private TreeSet<Integer> preparedHits;
        boolean preparing;
        
        PreparedQuery() {
            super();
            preparedHits = new TreeSet<Integer>();
            hits = new TreeSet<Integer>();
            preparing = true;
        }
        
        public void collect(int doc, float score) {
            if(preparing) {
                preparedHits.add(doc);
            } else {
                if(preparedHits.contains(doc))
                    hits.add(doc);
            }
        }
        
        public PreparedQuery preparedCopy() {
            PreparedQuery pq = new PreparedQuery();
            pq.preparedHits = this.preparedHits;
            pq.preparing = this.preparing;
            return pq;
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
    
    /** The maximum number of non-word characters to count as a single window */
    private static final String nonWordMax = nii.alloe.corpus.pattern.Pattern.nonWord + "{10,}";
    
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
        
        int offSet = 0;
        // Check for really long whitespace sections
        if(doc2.matches(".*" + nonWordMax + ".*")) {
            String[] ss = doc2.split(nonWordMax);
            if(ss.length == 0) {
                if(before)
                    return doc2.length();
                else
                    return idx;
            }
            if(before) {
                offSet = doc2.length() - ss[ss.length - 1].length();
                doc2 = ss[ss.length - 1];
            } else {
                doc2 = ss[0];
                offSet = idx + doc2.length();
            }
        }
        java.util.regex.Matcher m = java.util.regex.Pattern.compile(regex).matcher(doc2);
        if(!m.matches()) {
            if(before)
                return offSet;
            else
                return offSet == 0 ? doc.length() : offSet;
        } else {
            if(before)
                return idx - m.group(1).length();
            else //after
                return idx + m.group(1).length();
        }
    }
    
    public int getTotalDocs() {
        try {
            return indexSearcher.maxDoc();
        } catch(IOException x) {
            x.printStackTrace();
            return -1;
        }
    }
    
}
