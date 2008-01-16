package nii.alloe.corpus.pattern;
import java.util.*;

/**
 *
 * @author John McCrae, National Institute of Informatics
 */
public class PatternSet extends TreeMap<Pattern, Double> {
    
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
    
    
}
