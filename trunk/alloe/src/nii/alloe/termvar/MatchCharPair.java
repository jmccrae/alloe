package nii.alloe.termvar;
import java.util.*;

/**
 *
 * @author John McCrae, National Institute of Informatics
 */
public class MatchCharPair {
    MatchChar c1,c2;
    /** Creates a new instance of MatchCharPair */
    public MatchCharPair(MatchChar c1, MatchChar c2) {
        this.c1 = c1;
        this.c2 = c2;
    }
    
    public static List<MatchCharPair> getMatchListFromStrings(TreeSet<Match> matches, String s1, String s2) {
        List<MatchCharPair> rval = new Vector<MatchCharPair>(Math.max(s1.length(),s2.length()));
        Match lastMatch = new Match(-1,-1);
        for(Match match : matches) {
            for(int i = 0; i < Math.max(match.i1 - lastMatch.i1 -1,match.i2 - lastMatch.i2 - 1); i++) {
                MatchChar c1, c2;
                if(lastMatch.i1 + i + 1 < match.i1) {
                    c1 = new MatchChar(s1.charAt(lastMatch.i1 + i + 1));
                } else {
                    c1 = MatchChar.EMPTY;
                }
                if(lastMatch.i2 + i + 1 < match.i2) {
                    c2 = new MatchChar(s2.charAt(lastMatch.i2 + i + 1));
                } else {
                    c2 = MatchChar.EMPTY;
                }
                rval.add(new MatchCharPair(c1,c2));
            }
            rval.add(new MatchCharPair(new MatchChar(s1.charAt(match.i1)), new MatchChar(s2.charAt(match.i2))));
            lastMatch = match;
        }
        for(int i = 0; i < Math.max(s1.length() - lastMatch.i1 - 1,s2.length() - lastMatch.i2 - 1); i++) {
            MatchChar c1, c2;
            if(lastMatch.i1 + i + 1 < s1.length()) {
                c1 = new MatchChar(s1.charAt(lastMatch.i1 + i + 1));
            } else {
                c1 = MatchChar.EMPTY;
            }
            if(lastMatch.i2 + i + 1 < s2.length()) {
                c2 = new MatchChar(s2.charAt(lastMatch.i2 + i + 1));
            } else {
                c2 = MatchChar.EMPTY;
            }
            rval.add(new MatchCharPair(c1,c2));
        }
        
        return rval;
    }
    
    public static List<MatchCharPair> getMatchListFromStrings(TreeSet<Match> matches, List<MatchCharPair> s1, List<MatchCharPair> s2) {
        List<MatchCharPair> rval = new Vector<MatchCharPair>(Math.max(s1.size(),s2.size()));
        Match lastMatch = new Match(-1,-1);
        for(Match match : matches) {
            for(int i = 0; i < Math.max(match.i1 - lastMatch.i1 -1,match.i2 - lastMatch.i2 - 1); i++) {
                MatchChar c1, c2;
                if(lastMatch.i1 + i + 1 < match.i1) {
                    c1 = MatchChar.ANY;
                } else {
                    c1 = MatchChar.EMPTY;
                }
                if(lastMatch.i2 + i + 1 < match.i2) {
                    c2 = MatchChar.ANY;
                } else {
                    c2 = MatchChar.EMPTY;
                }
                rval.add(new MatchCharPair(c1,c2));
            }
            rval.add(s1.get(match.i1));
            lastMatch = match;
        }
        for(int i = 0; i < Math.max(s1.size() - lastMatch.i1 - 1,s2.size() - lastMatch.i2 - 1); i++) {
            MatchChar c1, c2;
            if(lastMatch.i1 + i + 1 < s1.size()) {
                c1 = MatchChar.ANY;
            } else {
                c1 = MatchChar.EMPTY;
            }
            if(lastMatch.i2 + i + 1 < s2.size()) {
                c2 = MatchChar.ANY;
            } else {
                c2 = MatchChar.EMPTY;
            }
            rval.add(new MatchCharPair(c1,c2));
        }
        
        return rval;
    }
    
    public boolean equals(Object o) {
        if(o instanceof MatchCharPair) {
            MatchCharPair mcp = (MatchCharPair)o;
            return c1.equals(mcp.c1) && c2.equals(mcp.c2);
        }
        return false;
    }
    
    public String toString() { return "(" + c1.toString() + ">" + c2.toString() + ")"; }
}
