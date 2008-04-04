package nii.alloe.termvar;
import java.util.*;
import nii.alloe.tools.strings.StringList;

/**
 *
 * @author John McCrae, National Institute of Informatics
 */
public class TermVariation {
    
    /** Creates a new instance of TermVariation */
    public TermVariation() {
    }
    
    public static Collection<List<MatchPair>> joinMatchings(List<MatchPair> matching1, List<MatchPair> matching2) {
        Collection<TreeSet<Match>> matchOfMatches = FindMatchings.findMatchings(matching1, matching2);
        
        for(TreeSet<Match> matchOfMatch : matchOfMatches) {
            Match lastMatch = new Match(-1,-1);
            LinkedList<MatchPair> rval = new LinkedList<MatchPair>();
            for(Match match : matchOfMatch) {
                if(match.i1 - lastMatch.i1 == 1 && match.i2 - lastMatch.i2 == 1 &&
                        lastMatch.i1 == -1) {
               //     doNothing();
                } else if(match.i1 - lastMatch.i1 == 1 && match.i2 - lastMatch.i2 == 1) {
                 //   doNothing();
                } else {
                    String s1, s2,t1,t2;
                    s1 = s2 = t1=t2= "";
                    for(MatchPair mp : matching1.subList(lastMatch.i1 + 1, match.i1)) {
                        s1 = s1 + mp.s1;
                        s2 = s2 + mp.s2;
                    }
                    for(MatchPair mp : matching2.subList(lastMatch.i2 + 1, match.i2)) {
                        t1 = t1 + mp.s1;
                        t2 = t2 + mp.s2;
                    }
                    findGeneralizers(s1,s2,t1,t2);
                }
                lastMatch = match;
            }
           /* if(lastMatch.i1 + 1 != s1.length() || lastMatch.i2 + 1 != s2.length()) {
                rval.add(new MatchPair(
                        s1.substring(lastMatch.i1 + 1, s1.length()),
                        s2.substring(lastMatch.i2 + 1, s1.length())));
            }*/
        }
        return null;
    }
    
    public static LinkedList<List<MatchPair>> findGeneralizers(String s1, String s2, String t1, String t2) {
        Collection<TreeSet<Match>> matchings1 = FindMatchings.findMatchings(new StringList(s1), new StringList(t1));
        Collection<TreeSet<Match>> matchings2 = FindMatchings.findMatchings(new StringList(s2), new StringList(t2));
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
                    LinkedList<MatchPair> match = new LinkedList<MatchPair>();
                    Match lastMatch = new Match(-1,-1);
                    for(Match m : cm) {
                        
                        lastMatch = m;
                    }
                }
            }
        }
        return rval;
    }
}
