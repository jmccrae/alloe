package nii.alloe.niceties;
import java.util.*;

/**
 * Simply uses a map to compare things.
 *
 * @author John McCrae, National Institute of Informatics
 */
public class MapComparator<K,V extends Comparable> implements Comparator<K> {
    private Map<K,V> map;
    
    /** Creates a new instance of IndexedComparator */
    public MapComparator(Map<K,V> map) {
        this.map = map;
    }
    
    public int compare(K k1, K k2) {
        return map.get(k1).compareTo(map.get(k2));
    }

}
