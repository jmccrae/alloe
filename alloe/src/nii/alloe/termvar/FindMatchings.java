package nii.alloe.termvar;
import java.util.*;
import nii.alloe.niceties.*;
import nii.alloe.niceties.lattice.*;

/**
 *
 * @author John McCrae, National Institute of Informatics
 */
public class FindMatchings {
    
    public static Collection<TreeSet<Match>> findMatchings(List string1, List string2) {
        return findMatchings(string1, string2,true);   
    }
    public static Collection<TreeSet<Match>> findMatchings(List string1, List string2, boolean best) {
        Collection<TreeSet<Match>> matchings = findMatchings(new TreeSet<Match>(), getAllMatches(string1, string2)).getInfimumSet();
        LinkedList<TreeSet<Match>> rval = new LinkedList<TreeSet<Match>>();
        int score = Integer.MAX_VALUE;
        for(TreeSet<Match> matching : matchings) {
            int matchScore = scoreMatching(matching,string1,string2);
            if(matchScore < score) {
                rval.clear();
                rval.add(matching);
                score = matchScore;
            } else if(matchScore == score) {
                rval.add(matching);
            }
        }
        return rval;
    }
    
    public static int scoreMatching(TreeSet<Match> matching, List string1, List string2) {
        Match lastMatch = new Match(-1,-1);
        int score = 0;
        for(Match match : matching) {
            score += Math.max(match.i1 - lastMatch.i1, match.i2 - lastMatch.i2) - 1;
            lastMatch = match;
        }
        score += Math.max(string1.size() - lastMatch.i1, string2.size() - lastMatch.i2) - 1;
        return score;
    }
    
    public static Vector<String[]> findMatchingsAsStrings(List string1, List string2) {
        Collection<TreeSet<Match>> matches = findMatchings(string1, string2);
        Vector<String[]> rval = new Vector<String[]>();
        for(TreeSet<Match> matching : matches) {
            System.out.println(matching);
            String [] strings = new String[2];
            strings[0] = string1.toString();
            strings[1] = string2.toString();
            int i1Off = 0;
            int i2Off = 0;
            Match lastMatch = new Match(0,0);
            for(Match m : matching) {
                if(m.i2 - lastMatch.i2 < m.i1 - lastMatch.i1) {
                    strings[1] = strings[1].substring(0,m.i2 + i2Off) + Strings.repString("_",m.i1-m.i2 - lastMatch.i1 + lastMatch.i2) + strings[1].substring(m.i2 + i2Off,strings[1].length());
                    i2Off = m.i1 - m.i2;
                } else {
                    strings[0] = strings[0].substring(0,m.i1 + i1Off) + Strings.repString("_",m.i2-m.i1 - lastMatch.i2 + lastMatch.i1) + strings[0].substring(m.i1 + i1Off,strings[0].length());
                    i1Off = m.i2 - m.i1;
                }
                lastMatch = m;
            }
            rval.add(strings);
        }
        return rval;
    }
    
    static SemiLattice<TreeSet<Match>> findMatchings(TreeSet<Match> currentMatch, SortedSet<Match> possibleMatches) {
        SemiLattice<TreeSet<Match>> semilattice = new SemiLattice<TreeSet<Match>>(new SubsetSemiLatticeComparator<Match>());
        semilattice.add(currentMatch);
        for(Match m : possibleMatches) {
            Match lastMatch = null;
            if(!currentMatch.isEmpty())
                lastMatch = currentMatch.last();
            if(lastMatch == null || (lastMatch.i1 < m.i1 && lastMatch.i2 < m.i2)) {
                TreeSet<Match> newMatch = new TreeSet<Match>(currentMatch);
                newMatch.add(m);
                semilattice.addAll(findMatchings(newMatch,possibleMatches.tailSet(m)).getInfimumSet());
            }
        }
        return semilattice;
    }
    private static TreeSet<Match> getAllMatches(List string1, List string2) {
        TreeSet<Match> matches = new TreeSet<Match>();
        for(int i = 0; i < string1.size(); i++) {
            int j = -1;
            int j2;
            while((j2 = string2.subList(j+1,string2.size()).indexOf(string1.get(i)) + j + 1) >= j + 1) {
                matches.add(new Match(i,j=j2));
            }
        }
        return matches;
    }
}

