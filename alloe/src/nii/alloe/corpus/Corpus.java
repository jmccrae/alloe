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
    private File indexFile;
    private transient HashMap<String, TreeSet<Integer>> termHits;
    private int trueContextNumber;
    private int maxSketchSize;
    private int docsSketched;
    private HashMap<String,Integer> sketchSize;
    private HashSet<String> sketchComplete;
    
    /** Creates a new instance of Corpus */
    public Corpus(TermList terms, String indexFile) {
        this(terms, new File(indexFile));
    }
    
    public Corpus(TermList terms, File indexFile) {
        this.terms = terms;
        this.indexFile = indexFile;
        termHits = new HashMap<String, TreeSet<Integer>>(terms.size());
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
        if (indexWriter == null) {
            throw new IllegalStateException("Attempting to add document to closed index");
        }
        Document d = new Document();
        contents = contents.replaceAll("'", "");
        d.add(new Field("contents", contents.toLowerCase(), Field.Store.YES, Field.Index.TOKENIZED));
        Iterator<String> termIter = terms.iterator();
        while (termIter.hasNext()) {
            String term = termIter.next();
            
            if (contents.matches(".*\\b" + term.toLowerCase() + "\\b.*")) {
                d.add(new Field("term", term.toLowerCase(), Field.Store.YES, Field.Index.TOKENIZED));
            }
        }
        indexWriter.addDocument(d);
    }
    
    /** Close the corpus, after which no more documents can be added */
    public void closeIndex() throws IOException {
        indexWriter.optimize();
        Directory d = indexWriter.getDirectory();
        indexWriter.close();
        
        Iterator<Map.Entry<String,Integer>> sketchIter = sketchSize.entrySet().iterator();
        while(sketchIter.hasNext()) {
            if(!sketchComplete.contains(sketchIter.next().getKey()))
                sketchIter.remove();
        }
        
        indexSearcher = new IndexSearcher(d);
        indexWriter = null;
    }
    
    private HitsIterator queryTerm(String term) {
        TreeSet<Integer> h = termHits.get(term);
        if (h == null) {
            try {
                QueryParser qp = new QueryParser("term", new AlloeAnalyzer());
                Query q = qp.parse("\"" + cleanQuery(term) + "\"");
                HitsIterator hi = new HitsIterator();
                if(sketchComplete.contains(term))
                    hi.limit = sketchSize.get(term);
                indexSearcher.search(q, hi);
                termHits.put(term, hi.hits);
                return hi;
            } catch (Exception x) {
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
        TreeSet<Integer> hr = (TreeSet<Integer>) h1.clone();
        hr.retainAll(h2);
        return new HitsIterator(hr);
    }
    
    /** Return the occurences of a particular string */
    public int getHitsForTerm(String term) {
        if(!sketchComplete.contains(term.toLowerCase()))
            return queryTerm(term).hits.size();
        else
            return queryTerm(term).hits.size() * trueContextNumber / sketchSize.get(term.toLowerCase());
    }
    
    /** Get all contexts containing term1 and term2 */
    public Iterator<String> getContextsForTerms(String term1, String term2) {
        return queryTerms(term1, term2);
    }
    
    /** Get all contexts which match the pattern p */
    public Iterator<String> getContextsForPattern(nii.alloe.corpus.pattern.Pattern p) {
        try {
            QueryParser qp = new QueryParser("contents", new AlloeAnalyzer());
            Query q = qp.parse(cleanQuery2(p.getQuery()));
            HitsIterator hi = new HitsIterator();
            indexSearcher.search(q, hi);
            return hi;
        } catch (Exception x) {
            x.printStackTrace();
            return null;
        }
    }
    
    /** Get all the contexts matching pattern with term1 and term inserted */
    public Iterator<String> getContextsForTermInPattern(nii.alloe.corpus.pattern.Pattern p, String term1, String term2) {
        try {
            String[] queries = {cleanQuery2(p.getQueryWithTerms(term1, term2)),
            "\"" + cleanQuery(term1) + "\" AND \"" + cleanQuery(term2) + "\""
            };
            String[] fields = {"contents", "terms"};
            MultiFieldQueryParser qp = new MultiFieldQueryParser(fields, new AlloeAnalyzer());
            Query q = MultiFieldQueryParser.parse(queries, fields, new AlloeAnalyzer());
            
            HitsIterator hi = new HitsIterator();
            indexSearcher.search(q, hi);
            return hi;
        } catch (Exception x) {
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
            indexSearcher.search(q, hi);
            return hi;
        } catch (Exception x) {
            x.printStackTrace();
            return null;
        }
    }
    
    /** Find the number of hits found by a prepared query
     * @param query The query object as returned from prepareQueryPattern
     */
    public int getPreparedQueryHits(Object query) {
        if (!(query instanceof PreparedQuery)) {
            throw new IllegalArgumentException("query passed to getPreparedQueryHits not valid");
        }
        return ((PreparedQuery) query).preparedHits.size();
    }
    
    /** Get the string iterator for a prepared query
     * @param query The query object as returned from prepareQueryPattern
     */
    public Iterator<String> getPreparedQuery(Object query) {
        if (!(query instanceof PreparedQuery)) {
            throw new IllegalArgumentException("query passed to getPreparedQuery not valid");
        }
        return new HitsIterator(((PreparedQuery) query).preparedHits);
    }
    
    /** Find term pairs on a prepared query. = getContextsForTermPrepared(term1,term2,query,true)
     * @see #preparedQueryPattern
     * @param query The query object as returned from prepareQueryPattern
     */
    public Iterator<String> getContextsForTermPrepared(String term1, String term2, Object query) {
        return getContextsForTermPrepared(term1, term2, query, true);
    }
    
    /** Find term pairs on a prepared query.
     *
     * @see #preparedQueryPattern
     * @param query The query object as returned from prepareQueryPattern
     * @param cache If true attempt to cache term hits */
    public Iterator<String> getContextsForTermPrepared(String term1, String term2, Object query, boolean cache) {
        if (!(query instanceof PreparedQuery)) {
            throw new IllegalArgumentException("query passed to getContextsForTermPrepared not valid");
        }
        
        if ((termHits.get(term1) == null || termHits.get(term2) == null) && !cache) {
            try {
                QueryParser qp = new QueryParser("term", new AlloeAnalyzer());
                Query q = qp.parse("\"" + cleanQuery(term1) + "\" AND \"" + cleanQuery(term2) + "\"");
                HitsIterator hi = ((PreparedQuery) query).preparedCopy();
                indexSearcher.search(q, hi);
                return hi;
            } catch (Exception x) {
                x.printStackTrace();
                return null;
            }
        } else {
            HitsIterator th = queryTerms(term1, term2);
            TreeSet<Integer> th2 = (TreeSet<Integer>) th.hits.clone();
            th2.retainAll(((PreparedQuery) query).preparedHits);
            return new HitsIterator(th2);
        }
    }
    private transient HashSet<String> singleTermsInCorpus;
    private transient HashSet<TermPair> termPairsInCorpus;
    
    public void initTermsInCorpusCache() {
        HashSet<String> tempSingleTermsInCorpus = new HashSet<String>();
        HashSet<TermPair> tempTermPairsInCorpus = new HashSet<TermPair>();
        
        Iterator<String> termIter1 = terms.iterator();
        while (termIter1.hasNext()) {
            String term1 = termIter1.next();
            if (isTermInCorpus(term1)) {
                tempSingleTermsInCorpus.add(term1);
            }
        }
        termIter1 = tempSingleTermsInCorpus.iterator();
        while (termIter1.hasNext()) {
            String term1 = termIter1.next();
            Iterator<String> termIter2 = tempSingleTermsInCorpus.iterator();
            while (termIter2.hasNext()) {
                String term2 = termIter2.next();
                
                if (areTermsInCorpus(term1, term2)) {
                    tempTermPairsInCorpus.add(new TermPair(term1, term2));
                }
            }
            
        }
        singleTermsInCorpus = tempSingleTermsInCorpus;
        termPairsInCorpus = tempTermPairsInCorpus;
    }
    
    public void clearTermsInCorpusCache() {
        singleTermsInCorpus = null;
        termPairsInCorpus = null;
    }
    
    /** @return true if term1 occurs in the corpus */
    public boolean isTermInCorpus(String term1) {
        if (singleTermsInCorpus == null) {
            HitsIterator hi = queryTerm(term1);
            return hi.hits.size() > 0;
        } else {
            return singleTermsInCorpus.contains(term1);
        }
    }
    
    /** @return true if term1 and term2 occur in the same document in the corpus */
    public boolean areTermsInCorpus(String term1, String term2) {
        if (termPairsInCorpus == null) {
            HitsIterator hi = queryTerms(term1, term2);
            return hi.hits.size() > 0;
        } else {
            return termPairsInCorpus.contains(new TermPair(term1,term2));
        }
    }
    
    public class TermPair implements Comparable<TermPair> {
        public TermPair(String term1, String term2) {
            this.term1 = term1;
            this.term2 = term2;
        }
        public String term1,  term2;
        
        public int compareTo(Corpus.TermPair o) {
            int rval = term1.compareTo(o.term1);
            if(rval == 0)
                return term2.compareTo(o.term2);
            else
                return rval;
        }
        
        public boolean equals(Object obj) {
            if(obj instanceof TermPair) {
                return compareTo((TermPair)obj) == 0;
            }
            return false;
        }
        
        public int hashCode() {
            int hash = 7;
            hash = 67 * hash + (this.term1 != null ? this.term1.hashCode() : 0);
            hash = 67 * hash + (this.term2 != null ? this.term2.hashCode() : 0);
            return hash;
        }
        
    }
    
    public Iterator<TermPair> getTermsInCorpus() {
        if (termPairsInCorpus != null) {
            return termPairsInCorpus.iterator();
        } else {
            throw new IllegalStateException();
        }
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
        s = s.replaceAll("^\\s*", "");
        s = s.replaceAll("\\s*$", "");
        s = s.replaceAll("\\s+", " AND ");
        return s;
    }
    
    /** @return number of documents in the corpus */
    public int size() {
        if (indexWriter != null) {
            return indexWriter.docCount();
        } else {
            try {
                return indexSearcher.maxDoc();
            } catch (IOException x) {
                x.printStackTrace();
                return -1;
            }
        }
    }
    
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        // We need to restore indexSearcher after loading
        indexSearcher = new IndexSearcher(indexFile.getAbsolutePath());
        termHits = new HashMap<String, TreeSet<Integer>>();
    }
    
    public Directory getDirectory() {
        return directory;
    }
    
    class HitsIterator extends HitCollector implements Iterator<String> {
        
        TreeSet<Integer> hits;
        Iterator<Integer> i;
        int limit;
        
        HitsIterator() {
            hits = new TreeSet<Integer>();
            i = null;
            limit = Integer.MAX_VALUE;
        }
        
        HitsIterator(TreeSet<Integer> hits) {
            this.hits = hits;
            i = null;
        }
        
        public void collect(int doc, float score) {
            if(doc < limit);
                hits.add(doc);
        }
        
        public void remove() {
            throw new UnsupportedOperationException();
        }
        
        public String next() {
            if (i == null) {
                i = hits.iterator();
            }
            try {
                Document d = indexSearcher.doc(i.next());
                //String s = d.getField("term").stringValue();
                return d.getField("contents").stringValue();
            } catch (IOException x) {
                x.printStackTrace();
                throw new RuntimeException("An IO Exception occurred");
            }
        }
        
        public boolean hasNext() {
            if (i == null) {
                i = hits.iterator();
            }
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
            if (preparing) {
                preparedHits.add(doc);
            } else {
                if (preparedHits.contains(doc)) {
                    hits.add(doc);
                }
            }
        }
        
        public PreparedQuery preparedCopy() {
            PreparedQuery pq = new PreparedQuery();
            pq.preparedHits = this.preparedHits;
            pq.preparing = this.preparing;
            return pq;
        }
    }
    
    /**
     * The maximum size of a sketch
     */
    public int getMaxSketchSize() { return maxSketchSize; }
    
    /**
     * Set the maximum sketch size (-1 for no sketching)
     */
    public void setMaxSketchSize(int maxSketchSize) { this.maxSketchSize = maxSketchSize; }
    
    /**
     * If sketching was used for the corpus this will give the true number of occurences of this
     * cooccurrence value
     */
    public int getTrueCooccurences(String term1, String term2, int cooccs) {
        Integer sketch1 = sketchSize.get(term1);
        Integer sketch2 = sketchSize.get(term2);
        if(!sketchComplete.contains(term1) && !sketchComplete.contains(term2)) {
            return cooccs;
        } else if(!sketchComplete.contains(term1)) {
            return trueContextNumber * cooccs / sketch2;
        } else if(!sketchComplete.contains(term2)) {
            return trueContextNumber * cooccs / sketch1;
        } else {
            return trueContextNumber * cooccs / Math.min(sketch1,sketch2);
        }
    }
    
    private void updateSketches(Vector<String> contexts, HashSet<String> terms, double progress) {
        Iterator<String> termIter = terms.iterator();
        docsSketched += contexts.size();
        while(termIter.hasNext()) {
            String term = termIter.next();
            Iterator<String> contextIter = contexts.iterator();
            while(contextIter.hasNext()) {
                String context = contextIter.next();
                if(context.matches(".*\\b" + term + "\\b.*")) {
                    if(sketchSize.get(term) == null)
                        sketchSize.put(term,1);
                    else if(!sketchComplete.contains(term)) {
                        sketchSize.put(term,sketchSize.get(term)+1);
                        if(sketchSize.get(term) >= maxSketchSize) {
                            sketchComplete.add(term);
                            sketchSize.put(term,docsSketched);
                            if(trueContextNumber == 0) {
                                trueContextNumber = (int)((double)docsSketched / progress);
                            }
                        }
                    }
                }
            }
        }
    }
    
    /** Returns only those areas in a fixed window of a particular term
     * @param progress For use with CorpusLoader really... important for sketching
     */
    public Vector<String> getContexts(String doc, int wordWindow, double progress) {
        TreeSet<Integer> bounds = new TreeSet<Integer>();
        doc = doc.toLowerCase();
        if(sketchSize == null) {
            sketchSize = new HashMap<String,Integer>();
            sketchComplete = new HashSet<String>();
        }
        
        HashSet<String> termsFound = new HashSet<String>();
        Iterator<String> termIter = terms.iterator();
        while (termIter.hasNext()) {
            String term = termIter.next().toLowerCase();
            if(sketchComplete.contains(term))
                continue;
            int idx = doc.indexOf(term, 0);
            if(doc.matches(".*\\b" + term + "\\b.*"))
                termsFound.add(term);
            else
                continue;
            while (idx >= 0) {
                if ((idx > 0 && !Character.isWhitespace(doc.charAt(idx - 1))) ||
                        (idx + term.length() < doc.length() &&
                        !Character.isWhitespace(doc.charAt(idx + term.length())))) {
                    idx = doc.indexOf(term, idx + term.length());
                    continue;
                }
                int before = findNWordsBeforeAfter(true, doc, idx, wordWindow);
                int after = findNWordsBeforeAfter(false, doc, idx + term.length(), wordWindow);
                boolean beforeFound = false, afterFound = false;
                
                Iterator<Integer> bIter = bounds.iterator();
                while (bIter.hasNext()) {
                    int oldBefore = bIter.next();
                    int oldAfter = bIter.next();
                    
                    if (!beforeFound && oldBefore < before && before <= oldAfter) {
                        before = oldBefore;
                        beforeFound = true;
                    }
                    if (!afterFound && after >= oldBefore && oldAfter > after) {
                        after = oldAfter;
                        afterFound = true;
                    }
                    if (afterFound && beforeFound) {
                        break;
                    }
                }
                bounds.add(before);
                bounds.add(after);
                // Clone to avoid those concurrent mod exes (grrr...)
                TreeSet<Integer> inBounds = new TreeSet<Integer>(bounds.subSet(before, false, after, false));
                bounds.removeAll(inBounds);
                idx = doc.indexOf(term, idx + term.length());
            }
        }
        Vector<String> rval = new Vector<String>(bounds.size() / 2);
        Iterator<Integer> bIter = bounds.iterator();
        while (bIter.hasNext()) {
            int before = bIter.next();
            int after = bIter.next();
            
            rval.add(doc.substring(before, after));
        }
        
        if(maxSketchSize > 0)
            updateSketches(rval,termsFound,progress);
        
        return rval;
    }
    /** The maximum number of non-word characters to count as a single window */
    private static final String nonWordMax = nii.alloe.corpus.pattern.Pattern.nonWord + "{10,}";
    
    private int findNWordsBeforeAfter(boolean before, String doc, int idx, int window) {
        String regex;
        
        
        if (before) {
            regex = ".*?(";
        } else // after
        {
            regex = "(";
        }
        for (int i = 0; i < window; i++) {
            regex = regex + (before ? "\\w+\\W+" : "\\W+\\w+");
        }
        if (before) {
            regex = regex + ")";
        } else // after
        {
            regex = regex + ").*";
        }
        String doc2;
        if (before) {
            doc2 = doc.substring(0, idx);
        } else //after
        {
            doc2 = doc.substring(idx, doc.length());
        }
        
        int offSet = 0;
        // Check for really long whitespace sections
        if (doc2.matches(".*" + nonWordMax + ".*")) {
            String[] ss = doc2.split(nonWordMax);
            if (ss.length == 0) {
                if (before) {
                    return doc2.length();
                } else {
                    return idx;
                }
            }
            if (before) {
                offSet = doc2.length() - ss[ss.length - 1].length();
                doc2 = ss[ss.length - 1];
            } else {
                doc2 = ss[0];
                offSet = idx + doc2.length();
            }
        }
        java.util.regex.Matcher m = java.util.regex.Pattern.compile(regex).matcher(doc2);
        if (!m.matches()) {
            if (before) {
                return offSet;
            } else {
                return offSet == 0 ? doc.length() : offSet;
            }
        } else {
            if (before) {
                return idx - m.group(1).length();
            } else //after
            {
                return idx + m.group(1).length();
            }
        }
    }
    
    public int getTotalDocs() {
        try {
            return indexSearcher.maxDoc();
        } catch (IOException x) {
            x.printStackTrace();
            return -1;
        }
    }
}
