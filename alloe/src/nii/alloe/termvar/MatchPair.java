package nii.alloe.termvar;
import nii.alloe.niceties.*;
import java.util.*;

/**
 *
 * @author John McCrae, National Institute of Informatics
 */
public class MatchPair  {
    String s1, s2;
    
    /** Creates a new instance of MatchPair */
    public MatchPair(String s1, String s2) {
        this.s1 = s1;
        this.s2 = s2;
    }
    
    MatchPair() {}
    
    public Collection<List<MatchPair>> joinWith(MatchPair mp2) {
        Collection<TreeSet<Match>> matchings1 = FindMatchings.findMatchings(new StringList(s1), new StringList(mp2.s1));
        Collection<TreeSet<Match>> matchings2 = FindMatchings.findMatchings(new StringList(s2), new StringList(mp2.s2));
        LinkedList<List<MatchPair>> rval = new LinkedList<List<MatchPair>>();
        for(TreeSet<Match> match1 :  matchings1) {
            for(TreeSet<Match> match2 : matchings2) {
                TreeSet<Match> coMatches = new TreeSet<Match>();
                for(Match m1 : match1) {
                    for(Match m2 : match2) {
                        coMatches.add(new Match(m1.i1,m2.i1));
                    }
                }
                Collection<TreeSet<Match>> coMatch = FindMatchings.findMatchings(new TreeSet<Match>(),coMatches).getInfimumSet();
                for(TreeSet<Match> cm : coMatch) {
                    rval.add(convert(cm,s1,s2));
                }
            }
        }
        return rval;
    }
    
    public static List<MatchPair> convert(SortedSet<Match> matches, String s1, String s2) {
        Match lastMatch = new Match(-1,-1);
        LinkedList<MatchPair> rval = new LinkedList<MatchPair>();
        for(Match match : matches) {
            if(match.i1 - lastMatch.i1 == 1 && match.i2 - lastMatch.i2 == 1 &&
                    lastMatch.i1 == -1) {
                rval.add(new MatchPair(
                        s1.substring(0,1),
                        s2.substring(0,1)));
            } else if(match.i1 - lastMatch.i1 == 1 && match.i2 - lastMatch.i2 == 1) {
                MatchPair mp = rval.getLast();
                mp.s1 = mp.s1 + s1.charAt(match.i1);
                mp.s2 = mp.s2 + s2.charAt(match.i2);
            } else {
                rval.add(new MatchPair(
                        s1.substring(lastMatch.i1 + 1, match.i1),
                        s2.substring(lastMatch.i2 + 1, match.i2)));
                rval.add(new MatchPair(
                        s1.substring(match.i1,match.i1+1),
                        s2.substring(match.i2,match.i2+1)));
            }
            lastMatch = match;
        }
        return rval;
    }
    
    public String toString() { return "[" + s1 + "->" + s2 + "]"; }
}
