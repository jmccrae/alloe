package nii.alloe.corpus.pattern;
import java.util.*;

/**
 *
 * @author John McCrae, National Institute of Informatics
 */
public class PatternSet extends TreeMap<Pattern, Double> {
    static final long serialVersionUID = 1337973026672387552L;
    
    /** Creates a new instance of PatternSet */
    public PatternSet() {
        super();
    }
    public PatternSet(Comparator<Pattern> comp) {
        super(comp);
    }
    
    public PatternSet(Map<Pattern,Double> m) {
        super(m);
    }
    public PatternSet(SortedMap<Pattern,Double> m) {
        super(m);
    }
    
    /**
     * Holds value of property relationship.
     */
    private String relationship;
    
    /**
     * Getter for property relationship.
     * @return Value of property relationship.
     */
    public String getRelationship() {
        return this.relationship;
    }
    
    /**
     * Setter for property relationship.
     * @param relationship New value of property relationship.
     */
    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }
    
    
    public void limitToTop(int n) {
        TreeSet<Pattern> topN = new TreeSet<Pattern>(new Comparator<Pattern>() {
            public int compare(Pattern p1, Pattern p2) {
                int rval = get(p1).compareTo(get(p2));
                if(rval == 0)
                    return p1.compareTo(p2);
                else
                    return rval;
            }
        });
        Iterator<Map.Entry<Pattern,Double>> iter = entrySet().iterator();
        while(iter.hasNext()) {
            Map.Entry<Pattern,Double> entry = iter.next();
            if(entry.getValue().isInfinite() || entry.getValue().isNaN()) {
                iter.remove();
                continue;
            }
            topN.add(entry.getKey());
            if(topN.size() > n)
                topN.pollFirst();
        }
        iter = entrySet().iterator();
        while(iter.hasNext()) {
            Map.Entry<Pattern,Double> entry = iter.next();
            if(!topN.contains(entry.getKey()))
                iter.remove();
        }
    }
}
