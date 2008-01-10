package nii.alloe.corpus.pattern;
import java.util.*;
import java.util.regex.*;

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
public class Pattern {
    
    /** The string representation of the pattern */
    private String val;
    
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
        for(i = 0; ;i++) {
            if(s[i].equals("1") || s[i].equals("2")) {
                if(seenT)
                    break;
                else
                    seenT = true;
            }
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
    
    /** @return true if this pattern matches str, with the capturers replaced by term1 and term2
     * @param term1 the left hand side of the relation
     * @param term2 the right hand side of the relation */
    public boolean matches(String str, String term1, String term2) {
        String regex = val.replaceAll("([\\.\\[\\]\\^\\$\\(\\)\\\\\\+\\{\\}])", "\\$1");
        regex = regex.replaceAll("\\*","(\\\\w+)");
        regex = regex.replaceAll("\\s","(\\\\W+)");
        regex = regex.replaceAll("1",term1);
        regex = regex.replaceAll("2",term2);
        
        return str.matches(regex);
    }
    
    /** Match this pattern to str
     * @return A two element array where the first element is the left hand side and the second
     * element is the right hand side */
    public String[] getTermMatch(String str) {
        String regex = val.replaceAll("([\\.\\[\\]\\^\\$\\(\\)\\\\\\+\\{\\}])", "\\$1");
        regex = regex.replaceAll("\\*","\\\\w+");
        regex = regex.replaceAll("\\s","\\\\W+");
        regex = regex.replaceAll("1","(\\\\w+)");
        regex = regex.replaceAll("2","(\\\\w+)");
        Matcher m = java.util.regex.Pattern.compile(regex).matcher(str);
        if(m.matches()) {
            String[] rval = new String[2];
            rval[0] = m.group(1);
            rval[1] = m.group(2);
            return rval;
        } else
            return null;
    }
    
    /** @return The pattern with all wildcards & capturers stripped out (for use with indexer) */
    public String getQuery() {
        String rval = val.replaceAll("\\*"," ");
        rval = rval.replace("1"," ");
        rval = rval.replace("2"," ");
        return rval;
    }
    
    /** @return The pattern with all wildcards stripped out and capturers replaced as usual */
    public String getQueryWithTerms(String term1, String term2) {
        String rval = val.replaceAll("\\*", "");
        rval = rval.replaceAll("1", term1);
        rval = rval.replaceAll("2", term2);
        return rval;
    }
    
    /** Get the pattern's string representation */
    public String getVal() { return val; }
    
    /** Set the pattern from a string representation
     * @throws IllegalArgumentException if s is not a valid pattern
     */
    public void setVal(String s) {
        if(!checkPattern(s))
            throw new IllegalArgumentException("Attempting to set pattern to invalid value");
        val = s;
    }
    
    static private boolean checkPattern(String s) {
        if(s.indexOf("1") != s.lastIndexOf("1") ||
                s.indexOf("2") != s.lastIndexOf("2") ||
                s.indexOf("1") == -1 ||
                s.indexOf("2") == -1 ||
                s.matches(".*\\w\\*.*") ||
                s.matches(".*\\*\\w.*"))
            return false;
        return true;
    }
}
