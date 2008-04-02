package nii.alloe.termvar;
import nii.alloe.corpus.*;
import nii.alloe.niceties.*;
import java.util.*;

/**
 *
 * @author John McCrae, National Institute of Informatics
 */
public class GenerateTermVariationPatterns {
    TermPairSet termPairs;
    
    /** Creates a new instance of GenerateTermVariationPatterns */
    public GenerateTermVariationPatterns(TermPairSet termPairs) {
        this.termPairs = termPairs;
    }
    
    public Collection<List<MatchCharPair>> getTermVariationPatterns() {
        TreeSet<List<MatchCharPair>> queue = new TreeSet<List<MatchCharPair>>(new ToStringComparator<List<MatchCharPair>>());
        LinkedList<List<MatchCharPair>> rval = new LinkedList<List<MatchCharPair>>();
        for(String[] terms : termPairs) {
                if(terms[0].equals(terms[1]))
                    continue;
                Collection<TreeSet<Match>> matchings = FindMatchings.findMatchings(new StringList(terms[0]),new StringList(terms[1]));
                for(TreeSet<Match> match : matchings) {
                    queue.add(MatchCharPair.getMatchListFromStrings(match,terms[0],terms[1]));
                }
        }
        while(!queue.isEmpty()) {
            List<MatchCharPair> mcp = queue.pollFirst();
            rval.add(mcp);
            LinkedList<List<MatchCharPair>> newPatterns = new LinkedList<List<MatchCharPair>>();
            for(List<MatchCharPair> mcp2 : queue) {
                Collection<TreeSet<Match>> matchings = FindMatchings.findMatchings(mcp,mcp2);
                for(TreeSet<Match> match : matchings) {
                    List<MatchCharPair> mpcNew = MatchCharPair.getMatchListFromStrings(match,mcp,mcp2);
                    cleanPattern(mpcNew);
                    if(mpcNew.size() > 0 && !rval.contains(mpcNew) && !queue.contains(mpcNew)) {
                        newPatterns.add(mpcNew);
                        System.out.println("I haz nu pattern: " + mpcNew.toString() + " ^_^");
                    }
                }
            }
            queue.addAll(newPatterns);
        }
        return rval;
    }
    
    public void cleanPattern(List<MatchCharPair> pattern) {
        ListIterator<MatchCharPair> iter = pattern.listIterator();
        
        while(iter.hasNext()) {
            MatchCharPair mpc = iter.next();
            if(mpc.c1.type != MatchChar.TYPE_CHAR && mpc.c2.type != MatchChar.TYPE_CHAR) {
                iter.remove();
            } else {
                break;
            }
        }
        while(iter.hasNext()) {
            iter.next();
        }
        while(iter.hasPrevious()) {
            MatchCharPair mpc = iter.previous();
            if(mpc.c1.type != MatchChar.TYPE_CHAR && mpc.c2.type != MatchChar.TYPE_CHAR) {
                iter.remove();
            } else {
                break;
            }
        }
    }
    
}
