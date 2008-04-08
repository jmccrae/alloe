package nii.alloe.corpus.pattern;
import java.util.*;
import java.util.regex.*;
import nii.alloe.corpus.analyzer.AlloeAnalyzer;

/**
 * A regular expression-like pattern used for extracting relations from the corpus.
 * <br>
 * Wildcards: <br>
 * A '*' represents any single term (sequence of word characters) <br>
 * A ' ' represents any non-word (sequence of nonword characters) <br>
 * Capturers: <br>
 * A '1' represents the left hand side of the relation <br>
 * A '2' represents the right hand side of the relation <br>
 * All other characters match directly. The pattern is considered to be split up
 * into alternating blocks of word characters, and non-word characters, where the
 * '*' wild card is considered to be a word block by itself.
 * <br>
 * TODO: Allow for 1,2 to match directly through '\1' or '\2'
 *
 * @author John McCrae, National Institute of Informatics
 */
public class Pattern implements java.io.Serializable, Comparable<Pattern> {
    
    /** The string representation of the pattern */
    private String val;
    
    public static final String word = "[\\w\\*\uff11\uff12]";
    public static final String wordDBS = "[\\\\w\\\\*\uff11\uff12]";
    public static final String nonWord = "[^\\w\\*\uff11\uff12]";
    public static final String nonWordDBS = "[^\\\\w\\\\*\uff11\uff12]";
    public static final String regexMetachars = "([\\.\\[\\]\\^\\$\\|\\?\\(\\)\\\\\\+\\{\\}\uff0a])";
    
    public static Collection<String> stopWords;
    
    static {
        stopWords = new TreeSet<String>();
        for(int i = 0; i < AlloeAnalyzer.STOP_WORDS.length; i++) {
            stopWords.add(AlloeAnalyzer.STOP_WORDS[i]);
        }
    }
    
    /** Creates a new instance of Pattern */
    public Pattern() {
        val = "";
    }
    
    /** Create a new instance with value val */
    public Pattern(String s) {
        setVal(s);
    }
    
    /** A pattern is considered alignable if it has the same number of wild cards between the
     * left hand side capturer ('1') and the right hand side capturer ('2'), and the patterns are
     * orientated the same way (ie both have the lhs capturer before rhs capturer or vica versa) */
    public boolean isAlignableWith(Pattern pat) {
        return interPairDist() == pat.interPairDist() && orientation() == pat.orientation();
    }
    
    /** @return the number of wildcards before the first capturer */
    public int initialDist() {
        String[] s = split();
        int i;
        for(i = 0; !s[i].equals("1") && !s[i].equals("2"); i++);
        return i;
    }
    
    /** @return the number of wildcards between capturers */
    public int interPairDist() {
        String[] s = split();
        int rval = 0;
        boolean count = false;
        for(int i = 0; i < s.length; i++) {
            if(s[i].equals("1") || s[i].equals("2"))
                count = !count;
            else if(count)
                rval++;
        }
        return rval;
    }
    
    /** @return the number of wildcards after the second capturer */
    public int finalDist() {
        String[] s = split();
        int i;
        boolean seenT = false;
        try {
            for(i = 0; ;i++) {
                if(s[i].equals("1") || s[i].equals("2")) {
                    if(seenT)
                        break;
                    else
                        seenT = true;
                }
            }
        } catch(ArrayIndexOutOfBoundsException x) {
            x.printStackTrace();
            System.err.println("Invalid pattern: " + val);
            System.exit(-1);
            return 0;
        }
        return s.length - i - 1;
    }
    
    /** @return 1 if the left hand side capturer precedes the right hand side, -1 otherwise */
    public int orientation() {
        if(val.matches("[^2]*1.*")) {
            return 1;
        } else {
            return -1;
        }
    }
    
    /** @return the split of the pattern, that is where every element of the array represents either
     * a wildcard, a capturer or a block
     */
    public String[] split() {
        String [] s = val.split("\\b|((?<=\\*))|(?=\\*)");
        return Arrays.copyOfRange(s,1,s.length);
    }
    
    /** @return a subset of the split of this pattern. If s1 = pattern1.getAlignmentWith(pattern2) and
     * s2 = pattern2.getAlignmentWith(pattern1) it is guaranteed that the two capturers will occur at the
     * same index in s1 and s2 */
    public String[] getAlignmentWith(Pattern pattern2) {
        int p1Init = initialDist();
        int p2Init = pattern2.initialDist();
        int p1Final = finalDist();
        int p2Final = pattern2.finalDist();
        int initOffset = p1Init > p2Init ? p1Init - p2Init : 0;
        int finalOffset = p1Final > p2Final ? p1Final - p2Final : 0;
        String [] s = split();
        return Arrays.copyOfRange(s,initOffset,s.length - finalOffset);
    }
    
    /** A pattern is considered to dominate another pattern if it matches everything that pattern matches.
     * If there are any intial or trailing wildcards these can be removed to create a more dominant pattern */
    public void makeMostDominant() {
        val = val.replaceAll("^[\\* ]*", "");
        val = val.replaceAll("[\\* ]*$","");
    }
    
    /** True if there is no non basic parts to the pattern */
    public boolean isTrivial() {
        if(val.matches("[12\\* ]*"))
            return true;
        String[] s = split();
        for(int i = 0; i < s.length; i++) {
            if(s[i].matches(word + "*") && !stopWords.contains(s[i]) && 
                    !s[i].equals("1") && !s[i].equals("2") && !s[i].equals("*"))
                return false;
            if(s[i].matches(nonWord + "*") && !s[i].matches("\\s*"))
                return false;
        }
        return true;
    }
    
    // Caching for matches
    private transient String matchesCache1,matchesCache2,matchesCache3;
    private transient int matchesCacheOr;
    
    /** @return true if this pattern matches str, with the capturers replaced by term1 and term2
     * @param term1 the left hand side of the relation
     * @param term2 the right hand side of the relation */
    public boolean matches(String str, String term1, String term2) {
        return matches(str,term1,term2,false);
    }
    
    /** @return true if this pattern matches str, with the capturers replaced by term1 and term2
     * @param term1 the left hand side of the relation
     * @param term2 the right hand side of the relation
     * @param lazy Use lazy matching (drop any wildcard)
     */
    public boolean matches(String str, String term1, String term2, boolean lazy) {
        String regex;
        term1 = cleanTerm(term1);
        term2 = cleanTerm(term2);
        if(matchesCacheOr == 0 || Math.abs(matchesCacheOr) == (lazy ? 1 : 2)) {
            regex = val.replaceAll(regexMetachars, "\\\\$1");
            regex = regex.replaceAll("\\*",wordDBS + (lazy ? "*" : "+"));
            regex = regex.replaceAll("\\s+",nonWordDBS + (lazy ? "*" : "+"));
            regex = ".*" + regex + ".*";
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("(.*)([12])(.*)([12])(.*)").matcher(regex);
            if(!m.matches()) assert false;
            matchesCache1 = deSafe(m.group(1));
            matchesCache2 = deSafe(m.group(3));
            matchesCache3 = deSafe(m.group(5));
            if(m.group(2).equals("1") && m.group(4).equals("2")) {
                matchesCacheOr = lazy ? 2 : 1;
            } else if(m.group(2).equals("2") && m.group(4).equals("1")) {
                matchesCacheOr = lazy ? -2 : -1;
            } else assert false;
            regex = regex.replaceAll("1",term1);
            regex = deSafe(regex.replaceAll("2",term2));
        } else if(matchesCacheOr > 0) {
            regex = matchesCache1 + term1 + matchesCache2 + term2 + matchesCache3;
        } else {
            regex = matchesCache1 + term2 + matchesCache2 + term1 + matchesCache3;
        }
        
        return str.matches(regex);
    }
    
    /** Match this pattern to str
     * @return A two element array where the first element is the left hand side and the second
     * element is the right hand side */
    public String[] getTermMatch(String str) {
        String regex = val.replaceAll(regexMetachars, "\\\\$1");
        regex = regex.replaceAll("\\*",wordDBS + "+");
        regex = regex.replaceAll("\\s",nonWordDBS + "+");
        // TODO: Think about this, very hard!
        //regex = regex.replaceAll("1","(" + wordDBS + "+)");
        //regex = regex.replaceAll("2","(" + wordDBS + "+)");
        regex = regex.replaceAll("1","\\\\b(.+?)\\\\b");
        regex = regex.replaceAll("2","\\\\b(.+?)\\\\b");
        regex = ".*" + regex + ".*";
        Matcher m = java.util.regex.Pattern.compile(deSafe(regex)).matcher(str);
        if(m.matches()) {
            String[] rval = new String[2];
            rval[0] = m.group(1);
            rval[1] = m.group(2);
            return rval;
        } else
            return null;
    }
    
    /** @return The pattern with all wildcards & capturers replaced with * */
    public String getQuery() {
        String rval = val.replaceAll("[\\*12]","*");
        //rval = rval.replace("1"," ");
        //rval = rval.replace("2"," ");
        return deSafe(rval);
    }
    
    /** @return The pattern with all wildcards stripped out and capturers replaced as usual */
    public String getQueryWithTerms(String term1, String term2) {
        String rval = val.replaceAll("\\*", "");
        rval = rval.replaceAll("1", term1);
        rval = rval.replaceAll("2", term2);
        return deSafe(rval);
    }
    
    /** Get the pattern's string representation */
    public String getVal() { return val; }
    
    /** Set the pattern from a string representation
     * @throws IllegalArgumentException if s is not a valid pattern
     */
    public void setVal(String s) {
        if(!checkPattern(s))
            throw new IllegalArgumentException("Attempting to set pattern to invalid value: " + s);
        val = s;
    }
    
    static private boolean checkPattern(String s) {
        if(s.indexOf("1") != s.lastIndexOf("1") ||
                s.indexOf("2") != s.lastIndexOf("2") ||
                s.indexOf("1") == -1 ||
                s.indexOf("2") == -1 ||
                s.matches(".*" + word + "\\*.*") ||
                s.matches(".*\\*" + word + ".*"))
            return false;
        return true;
    }
    
    public int compareTo(Pattern p) {
        return val.compareTo(p.val);
    }
    
    public String toString() { return deSafe(val); }
    
    /** Convert a string to a safe version. This replaces literal versions of normal characters with full width versions */
    public static String makeSafe(String str) {
        str = str.replaceAll("1", "\uff11");
        str = str.replaceAll("2", "\uff12");
        str = str.replaceAll("\\*", "\uff0a");
        return str;
    }
    
    private static String deSafe(String str) {
        str = str.replaceAll("\uff11", "1");
        str = str.replaceAll("\uff12", "2");
        str = str.replaceAll("\uff0a", "*");
        return str;
    }
    
    /** Convert to lower case and bs all metachars */
    public static String cleanTerm(String term) {
        term = term.replaceAll(regexMetachars,"\\\\$1");
        return term.toLowerCase();
    }
}
