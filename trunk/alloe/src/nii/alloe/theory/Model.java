package nii.alloe.theory;
import java.util.*;
import java.io.Serializable;

/**
 * A model is a set of assignments to every possible link of a true/false value. This is in effect means that we have
 * a set of graphs which define the true/false value for each element in the set of elements.
 *
 */
public class Model extends AbstractCollection<Integer> implements Serializable {
    /**
     * The graphs indexed by their id string
     */
    public final TreeMap<String, Graph> graphs;
    /**
     * The elements used in this model
     */
    public final AbstractCollection<Integer> elems;
    /**
     * A map used to order the graphs, this is used for creating IDs
     */
    private final Vector<String> relationIdx;
    /**
     * The number of elements used in this model
     */
    public int n;
    
    /**
     * Create an empty model on the elements {1,...,n}
     */
    public Model(int n) {
        elems = new LinkedList<Integer>();
        for(int i = 0; i < n; i++) {
            elems.add(i);
        }
        this.n = n;
        graphs = new TreeMap<String, Graph>();
        relationIdx = new Vector<String>();
    }
    
    /**
     * Create a model with the specified graphs on the elements {1,...,n}
     */
    public Model(TreeMap<String, Graph> graphs, int n)  {
        this.graphs = graphs;
        elems = new LinkedList<Integer>();
        for(int i = 0; i < n; i++) {
            elems.add(i);
        }
        this.n = n;
        relationIdx = new Vector<String>();
        
        Iterator<String> reliter = graphs.keySet().iterator();
        while(reliter.hasNext()) {
            relationIdx.add(reliter.next());
        }
    }
    
    /**
     * Create a model that is identical to the previous model (NB graphs and elems are not cloned!)
     */
    public Model(Model m) {
        this.relationIdx = (Vector<String>)m.relationIdx.clone();
        this.n = m.n;
        this.elems = m.elems;
        graphs = new TreeMap<String,Graph>();
    }
    
    private Model() {
        this.graphs = new TreeMap<String,Graph>();
        this.elems = new TreeSet<Integer>();
        relationIdx = new Vector<String>();
    }
    
    /**
     * Create a model that is a copy of this but contains only the specified links
     */
    public Model subModel(AbstractCollection<Integer> rels) {
        Model rval = new Model();
        rval.n = n;
        Iterator<String> iter = relationIdx.iterator();
        while(iter.hasNext()) {
            String relID = iter.next();
            Graph g = graphs.get(relID);
            if(g instanceof EquivalenceGraph || g instanceof MembershipGraph) {
                rval.graphs.put(relID,g);
            } else if(g instanceof SpecificGraph || g instanceof ProbabilityGraph) {
                rval.graphs.put(relID,new SpecificGraph(n,relID));
            } else {
                try {
                    Class c = g.getClass();
                    Class[] ps = new Class[0];
                    rval.graphs.put(relID,(Graph)c.getConstructor(ps).newInstance());
                } catch(Exception x) {
                    x.printStackTrace();
                    System.exit(-1);
                }
            }
            rval.relationIdx.add(relID);
        }
        Iterator<Integer> riter = rels.iterator();
        while(riter.hasNext()) {
            Integer id = riter.next();
            rval.add(id);
            rval.elems.add(iByID(id));
            rval.elems.add(jByID(id));
        }
        return rval;
    }
    
    /**
     * Create a copy where all the probability graphs have been replaced by specific graphs
     */
    public Model createSpecificCopy() {
        Iterator<String> pgiter = graphNameIterator();
        Model rval = new Model(this);
        while(pgiter.hasNext()) {
            String str = pgiter.next();
            Graph pg = graphs.get(str);
            
            if(pg instanceof ProbabilityGraph) {
                SpecificGraph g = rval.addSpecificGraph(str);
                Iterator<Integer> i1 = pg.iterator(this.n);
                while(i1.hasNext()) {
                    int i2 = i1.next();
                    int i = i2 / n;
                    int j = i2 % n;
                    if(pg.isConnected(i,j)) {
                        g.add(i,j);
                    }
                }
                rval.graphs.put(str,g);
            } else {
                rval.graphs.put(str,pg);
            }
        }
        return rval;
    }
    
    /**
     * Create a copy of this where links are only present if they are immutable
     */
    public Model createImmutableCopy() {
        Iterator<String> pgiter = graphNameIterator();
        Model rval = new Model(this);
        while(pgiter.hasNext()) {
            String str = pgiter.next();
            Graph pg = graphs.get(str);
            
            if(!(pg instanceof EquivalenceGraph) && !(pg instanceof MembershipGraph)) {
                SpecificGraph g = rval.addSpecificGraph(str);
                Iterator<Integer> i1 = g.iterator(this.n);
                while(i1.hasNext()) {
                    int i2 = i1.next();
                    int i = i2 / n;
                    int j = i2 % n;
                    if(pg.isConnected(i,j) && !pg.mutable(i,j)) {
                        g.add(i,j);
                    }
                }
                rval.graphs.put(str,g);
            } else {
                rval.graphs.put(str,pg);
            }
        }
        return rval;
    }
    
    /**
     * Adds the basic graphs that is an EquivalenceGraph indexed by "e" and a Membership graph for every set in l
     */
    public void addBasicGraphs(Logic l) {
        graphs.put("e",new EquivalenceGraph());
        if(relationIdx.indexOf("e") == -1)
            relationIdx.add("e");
        String str;
        for(Iterator<String> i2 = l.sets.keySet().iterator(); i2.hasNext();) {
            str = i2.next();
            graphs.put("in_" + str,new MembershipGraph(l.sets.get(str)));
            if(relationIdx.indexOf("in_" + str) == -1)
                relationIdx.add("in_" + str);
        }
    }
    
    /**
     * Add a (empty) specific graph to the model
     */
    public SpecificGraph addSpecificGraph(String name) {
        if(relationIdx.indexOf(name) == -1)
            relationIdx.add(name);
        SpecificGraph rval = new SpecificGraph(n,name);
        graphs.put(name,rval);
        return rval;
    }
    
    /**
     * Add a (empty) probability graph to the model
     */
    public ProbabilityGraph addProbabilityGraph(String name) {
        if(relationIdx.indexOf(name) == -1)
            relationIdx.add(name);
        ProbabilityGraph rval = new ProbabilityGraph(n);
        graphs.put(name, rval);
        return rval;
    }
    
    /**
     * Add all elements of another graph into this one. Note these graphs must have the same indexing,
     * ie, created by subModel, createSpecificCopy, createImmutableCopy etc.
     */
    public boolean add(Model m) {
        Iterator<Integer> i = m.iterator();
        boolean rval = false;
        while(i.hasNext()) {
            rval = add(i.next()) || rval;
        }
        return rval;
    }
    
    /**
     * Remove all elements of another graph into this one. Note these graphs must have the same indexing.
     */
    public boolean remove(Model m) {
        Iterator<Integer> i = m.iterator();
        boolean rval = false;
        while(i.hasNext()) {
            rval = remove(i.next()) || rval;
        }
        return rval;
    }
    
    /**
     * Does this graph contain any elements in the list.
     */
    public boolean containsAny(Collection<Integer> ids) {
        Iterator<Integer> i = ids.iterator();
        while(i.hasNext()) {
            if(isConnected(i.next())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * The id is a unique id of a relation in this model
     */
    public int id(String relation, int i, int j)  {
        if(i < 0 || j < 0) {
            throw new IllegalArgumentException("Cannot return ID of functional or unassigned argument!");
        }
        return relationIdx.indexOf(relation) * n * n + i * n + j;
    }
    
    /**
     * The id of the ith part of rule. Rule should have all assignements
     */
    public int id(Rule rule, int i) {
        return id(rule.relations.get(i),
                rule.terms.get(i)[0].getAssignment(),
                rule.terms.get(i)[1].getAssignment());
    }
    
    /**
     * ID -> Graph Name
     */
    public String relationByID(int id) {
        return relationIdx.get(id / n / n);
    }
    
    /**
     * ID -> left assignment
     */
    public int iByID(int id) {
        return (id % (n*n)) / n;
    }
    
    /**
     * ID -> right assignment
     */
    public int jByID(int id) {
        return (id % n);
    }
    
    /**
     * ID -> left assignment
     */
    public int iByID(Integer id) {
        return (id % (n*n)) / n;
    }
    
    /**
     * ID -> right assignment
     */
    public int jByID(Integer id) {
        return (id % n);
    }
    
    /**
     * ID -> graph instance
     */
    public Graph getGraphByID(int id) {
        return graphs.get(relationByID(id));
    }
    
    /**
     * ID -> graph instance
     */
    public Graph getGraphByID(Integer id) {
        return graphs.get(relationByID(id.intValue()));
    }
    
    /**
     * Same as Graph.isConnected(iByID(id),jByID(id))
     */
    public boolean isConnected(Integer id) {
        return graphs.get(relationByID(id)).isConnected(iByID(id),jByID(id));
    }
    
    /**
     * Same as Graph.isConnected(iByID(id),jByID(id))
     */
    public boolean isConnected(int id) {
        return graphs.get(relationByID(id)).isConnected(iByID(id),jByID(id));
    }
    
    /**
     * Same as Graph.mutable(iByID(id),jByID(id))
     */
    public boolean mutable(Integer id) {
        return graphs.get(relationByID(id)).mutable(iByID(id),jByID(id));
    }
    
    /**
     * Same as Graph.mutable(iByID(id),jByID(id))
     */
    public boolean mutable(int id) {
        return graphs.get(relationByID(id)).mutable(iByID(id),jByID(id));
    }
    
    /**
     * Same as Graph.add(iByID(id),jByID(id))
     */
    public boolean add(Integer id) {
        boolean rval = !graphs.get(relationByID(id)).isConnected(iByID(id),jByID(id));
        graphs.get(relationByID(id)).add(iByID(id),jByID(id));
        return rval;
    }
    /**
     * Same as Graph.add(iByID(id),jByID(id))
     */
    public boolean add(int id) {
        boolean rval = !graphs.get(relationByID(id)).isConnected(iByID(id),jByID(id));
        graphs.get(relationByID(id)).add(iByID(id),jByID(id));
        return rval;
    }
    /**
     * Same as Graph.remove(iByID(id),jByID(id))
     */
    public boolean remove(Integer id) {
        boolean rval = graphs.get(relationByID(id)).isConnected(iByID(id),jByID(id));
        graphs.get(relationByID(id)).remove(iByID(id),jByID(id));
        return rval;
    }
    /**
     * Same as Graph.remove(iByID(id),jByID(id))
     */
    public void remove(int id) {
        graphs.get(relationByID(id)).remove(iByID(id),jByID(id));
    }
    
    /**
     * Sum of each graphs {@link Graph.linkCount}
     **/
    public int size() {
        int rval = 0;
        Iterator<Graph> gi = graphs.values().iterator();
        while(gi.hasNext()) {
            rval += gi.next().linkCount();
        }
        return rval;
    }
    
    /**
     * Name -> Graph ID
     */
    public int getGraphIDByName(String name) {
        return relationIdx.indexOf(name);
    }
    
    /**
     * Name -> Graph object
     */
    public Graph getGraphByName(String name) {
        return graphs.get(name);
    }
    
    /**
     * The set of graph names
     */
    public Vector<String> getGraphNames() {
        // Don't really want someone else changing relationIdx
        return (Vector<String>)relationIdx.clone();
    }
    
    /**
     * An iterator for the name of every graph in this model, although similar can be achieved
     * graphs.keySet().iterator(), this iterator is ordered so that if a second model is created
     * and <em>all</em> graphs are added <em>in the same order</em> as this iterator these two models will use the same ID
     * for every link
     */
    public Iterator<String> graphNameIterator() { return relationIdx.iterator(); }
    
    public Iterator<Integer> iterator() { return new ModelIterator(); }
    
    private class ModelIterator implements Iterator<Integer> {
        Iterator<Map.Entry<String,Graph>> graphIterator;
        Iterator<Integer> specIterator;
        Integer offset;
        
        public ModelIterator() {
            graphIterator = graphs.entrySet().iterator();
            if(graphIterator.hasNext()) {
                Map.Entry<String,Graph> e = graphIterator.next();
                specIterator = e.getValue().iterator(n);
                offset = relationIdx.indexOf(e.getKey()) * n * n;
            } else
                specIterator = null;
        }
        
        public boolean hasNext() {
            if(specIterator == null)
                return false;
            if(specIterator.hasNext())
                return true;
            if(!graphIterator.hasNext())
                return false;
            do {
                Map.Entry<String,Graph> e = graphIterator.next();
                specIterator = e.getValue().iterator(n);
                offset = relationIdx.indexOf(e.getKey()) * n * n;
            } while(graphIterator.hasNext() && !specIterator.hasNext());
            return specIterator.hasNext();
        }
        public Integer next() {
            if(specIterator == null) {
                throw new NoSuchElementException();
            } else if(specIterator.hasNext()) {
                return specIterator.next() + offset;
            } else if(graphIterator.hasNext()) {
                do {
                    Map.Entry<String,Graph> e = graphIterator.next();
                    specIterator = e.getValue().iterator(n);
                    offset = relationIdx.indexOf(e.getKey()) * n * n;
                } while(graphIterator.hasNext() && !specIterator.hasNext());
                return next();
            } else {
                throw new NoSuchElementException();
            }
        }
        public void remove() {
            if(specIterator == null)
                throw new IllegalStateException();
            else
                specIterator.remove();
        }
    }
    
    private void readObject(java.io.ObjectInputStream ios) throws java.io.IOException, ClassNotFoundException {
        ios.defaultReadObject();
        ProbabilityGraph.n = n;
    }
}
