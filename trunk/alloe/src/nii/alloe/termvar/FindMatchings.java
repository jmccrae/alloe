package nii.alloe.termvar;
import java.util.*;
import nii.alloe.niceties.*;
import nii.alloe.niceties.lattice.*;

/**
 *
 * @author John McCrae, National Institute of Informatics
 */
public class FindMatchings {
    
    /** Creates a new instance of FindMatchings */
    public FindMatchings() {
    }
    
    
    public Vector<String[]> findMatchings(String string1, String string2) {
        SemiLattice<TreeSet<Match>> matches = findMatchings(new TreeSet<Match>(), getAllMatches(string1,string2));
        Vector<String[]> rval = new Vector<String[]>();
        for(TreeSet<Match> matching : matches.getInfimumSet()) {
            System.out.println(matching);
            String [] strings = new String[2];
            strings[0] = new String(string1);
            strings[1] = new String(string2);
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
    
    private SemiLattice<TreeSet<Match>> findMatchings(TreeSet<Match> currentMatch, SortedSet<Match> possibleMatches) {
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
    
    private TreeSet<Match> getAllMatches(String string1,String string2) {
        TreeSet<Match> matches = new TreeSet<Match>();
        for(int i = 0; i < string1.length(); i++) {
            int j = -1;
            while((j = string2.indexOf(string1.substring(i,i+1),j+1)) >= 0) {
                matches.add(new Match(i,j));
            }
        }
        return matches;
    }
    
    private class Match implements Comparable<Match> {
        int i1, i2;
        
        Match(int i, int j) {
            i1 = i;
            i2 = j;
        }
        
        public int compareTo(Match m) {
            if(i1 < m.i1) {
                return -1;
            } else if(i1 > m.i1) {
                return +1;
            } else if(i2 < m.i2) {
                return -1;
            } else if(i2 > m.i2) {
                return +1;
            } else {
                return 0;
            }
        }
        
        public String toString() { return "[" + i1 + "->" + i2 + "]"; }
    }
    
    /*    if(string1.length() == 0 || string2.length() == 0) {
            Matching match = new Matching(string1,string2);
            Vector<Matching> matchings = new Vector<Matching>();
            matchings.add(match);
            return matchings;
        }
        String frontChar = string1.substring(0,1);
     
        // No match
        Vector<Matching> rval = findMatchings(string1.substring(1,string1.length()),string2);
        for(Matching match : rval) {
            match.string1 = frontChar + match.string1;
            match.string2 = "_" + match.string2;
        }
     
        int lastMatch = -1;
        int match;
        while((match = string2.indexOf(frontChar,lastMatch + 1)) != -1) {
            Vector<Matching> matches = findMatchings(string1.substring(1,string1.length()),
                    string2.substring(match + 1, string2.length()));
            for(Matching m : matches) {
                m.string1 = Strings.repString("_", match) + frontChar + m.string1;
                m.string2 = string2.substring(0,match+1) + m.string2;
                rval.add(m);
            }
            lastMatch = match;
        }
        return rval;
    }
     
    class Matching implements Comparable<Matching> {
        String string1;
        String string2;
        Matching(String string1, String string2) {
            this.string1 = string1;
            this.string2 = string2;
        }
     
        void simplify() {
            int last = 0;
            for(int i = 0; i < string1.length() && i < string2.length(); i++) {
                if(string1.charAt(i) == string2.charAt(i)) {
                    String s1 = "";
                    String s2 = "";
                    int diff1=0,diff2=0;
                    for(int j = last; j < i; j++) {
                        if(string1.charAt(j) != '_') {
                            s1 = s1 + string1.charAt(j);
                        } else {
                            diff1++;
                        }
                        if(string2.charAt(j) != '_') {
                            s2 = s2 + string2.charAt(j);
                        } else {
                            diff2++;
                        }
                    }
                    if(diff1 > diff2) {
                        diff1 -= diff2;
                        diff2 = 0;
                    } else {
                        diff2 -= diff1;
                        diff1 = 0;
                    }
                    string1 = string1.substring(0,last) +
                            s1 + Strings.repString("_",diff1) + string1.substring(i,string1.length());
                    string2 = string2.substring(0,last) +
                            s2 + Strings.repString("_",diff2) + string2.substring(i,string2.length());
                    last = i;
                }
            }
            if(string1.length() > string2.length()) {
                string2 = string2 + Strings.repString("_",string1.length() - string2.length());
            } else {
                string1 = string1 + Strings.repString("_",string2.length() - string1.length());
            }
     
            for(int i = 0; i < string1.length(); i++) {
                for(int j = i+1; j <= string2.length(); j++) {
                    String s1 = string1.substring(i,j).replaceAll("_","");
                    String s2 = string2.substring(i,j).replaceAll("_","");
                    if(s1.equals(s2)) {
                        string1 = string1.substring(0,i) + s1 + string1.substring(j,string1.length());
                        string2 = string2.substring(0,i) + s1 + string2.substring(j,string2.length());
                    }
                }
            }
        }
     
        public int compareTo(Matching m) {
            int rval = string1.compareTo(m.string1);
            if(rval == 0) {
                return string2.compareTo(m.string2);
            } else {
                return rval;
            }
        }
    }*/
}
