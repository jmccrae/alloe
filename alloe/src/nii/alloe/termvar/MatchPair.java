package nii.alloe.termvar;
import nii.alloe.niceties.*;
import java.util.*;

/**
 *
 * @author John McCrae, National Institute of Informatics
 */
public class MatchPair  {
    public String s1, s2;
    
    /** Creates a new instance of MatchPair */
    public MatchPair(String s1, String s2) {
        this.s1 = s1;
        this.s2 = s2;
    }
    
    public Collection<List<MatchPair>> joinWith(MatchPair mp2) {
        Collection<TreeSet<Match>> matchings1 = FindMatchings.findMatchings(new StringList(s1), new StringList(mp2.s1));
        Collection<TreeSet<Match>> matchings2 = FindMatchings.findMatchings(new StringList(s2), new StringList(mp2.s2));
        LinkedList<List<MatchPair>> rval = new LinkedList<List<MatchPair>>();
        for(TreeSet<Match> match1 :  matchings1) {
            for(TreeSet<Match> match2 : matchings2) {
                LinkedList<MatchPair> matchPairs = new LinkedList<MatchPair>();
                TreeSet<Match> coMatches = new TreeSet<Match>();
                for(Match m1 : match1) {
                    for(Match m2 : match2) {
                        coMatches.add(new Match(m1.i1,m2.i1));
                    }
                }
                
                
            }
        }
        return rval;
    }
    

    
    public String toString() { return "[" + s1 + "->" + s2 + "]"; }
}
