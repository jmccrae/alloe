package nii.alloe.theory;
import java.util.*;
import java.io.Serializable;
import nii.alloe.corpus.*;
import nii.alloe.tools.struct.MultiIterator;

/**
 * A model is a set of assignments to every possible link of a true/false value. This is in effect means that we have
 * a set of graphs which define the true/false value for each element in the set of elements.
 *
 */
public class Model extends AbstractSet<Integer> implements Serializable {
    /**
     * The graphs indexed by their id string
     */
    public final TreeMap<String, Graph> graphs;
    /**
     * The elements used in this model
     */
    public final Collection<Integer> elems;
    /**
     * The Logic
     */
    public final Logic logic;
    /**
     * A map used to order the graphs, this is used for creating IDs
     */
    private final Vector<String> relationIdx;
    
    private boolean hasCompulsory;
    private int compulsoryCount;
    
    /**
     * Create an empty model on the elements {1,...,n}
     */
    public Model(Logic logic) {
        elems = new LinkedList<Integer>();
        this.logic = logic;
        for(int i = 0; i < getFullModelSize(); i++) {
            elems.add(i);
        }
        graphs = new TreeMap<String, Graph>();
        relationIdx = new Vector<String>();
    }
    
    /**
     * Create a model with the specified graphs on the elements {1,...,n} DELETE this soon!!!
     */
    public Model(TreeMap<String, Graph> graphs, Logic logic)  {
        this.graphs = graphs;
        this.logic = logic;
        elems = new LinkedList<Integer>();
        for(int i = 0; i < getFullModelSize(); i++) {
            elems.add(i);
        }
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
        this.logic = m.logic;
        this.elems = m.elems;
        graphs = new TreeMap<String,Graph>();
    }
    
    
    public int getModelSize() {
        return logic.ruleSymbols.modelSize;
    }
    
    public int getFullModelSize() {
        return logic.ruleSymbols.fullModelSize;
    }
    
    /**
     * Create a model that is a copy of this but contains only the specified links
     */
    public Model subModel(Collection<Integer> rels) {
        Model rval = new Model(logic);
        rval.elems.clear();
        Iterator<String> iter = relationIdx.iterator();
        while(iter.hasNext()) {
            String relID = iter.next();
            Graph g = graphs.get(relID);
            if(g instanceof EquivalenceGraph || g instanceof MembershipGraph) {
                rval.graphs.put(relID,g);
            } else if(g instanceof SpecificGraph || g instanceof ProbabilityGraph) {
                rval.graphs.put(relID,new SpecificGraph(getModelSize(),relID));
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
                Iterator<Integer> i1 = pg.iterator(getFullModelSize());
                while(i1.hasNext()) {
                    int i2 = i1.next();
                    int i = i2 / getFullModelSize();
                    int j = i2 % getFullModelSize();
                    if(pg.isConnected(i,j)) {
                        g.add(i,j);
                    }
                }
                rval.graphs.put(str,g);
            } else {
                rval.graphs.put(str,pg);
            }
        }
        rval.compulsoryCount = this.compulsoryCount;
        return rval;
    }
    
    /**
     * Create a copy where all the specific graphs have been replaced by specific graphs with a certain value
     */
    public Model createProbabilityCopy(double posProb, double negProb) {
        Iterator<String> pgiter = graphNameIterator();
        Model rval = new Model(this);
        while(pgiter.hasNext()) {
            String str = pgiter.next();
            Graph pg = graphs.get(str);
            
            if(pg instanceof SpecificGraph) {
                ProbabilityGraph g = rval.addProbabilityGraph(str);
                g.setBaseVal(negProb);
                Iterator<Integer> i1 = pg.iterator(getFullModelSize());
                while(i1.hasNext()) {
                    int i2 = i1.next();
                    int i = i2 / getFullModelSize();
                    int j = i2 % getFullModelSize();
                    if(pg.isConnected(i,j)) {
                        g.setPosVal(i,j,posProb);
                    }
                }
                rval.graphs.put(str,g);
            } else {
                rval.graphs.put(str,pg);
            }
        }
        rval.compulsoryCount = this.compulsoryCount;
        return rval;
    }
    
    public int getCompulsoryCount() {
        return compulsoryCount;
    }
    
    /**
     * Create an exact clone of this model
     */
    public Model createCopy() {
        Iterator<String> graphIter = graphNameIterator();
        Model rval = new Model(this);
        while(graphIter.hasNext()) {
            String graphName = graphIter.next();
            Graph g = graphs.get(graphName);
            rval.graphs.put(graphName,g.createCopy());
        }
        rval.compulsoryCount = this.compulsoryCount;
        return rval;
    }
    
    /**
     * Create a copy where all probability graphs have been replaced by blank specific graphs
     */
    public Model createBlankSpecificCopy() {
        Iterator<String> pgiter = graphNameIterator();
        Model rval = new Model(this);
        while(pgiter.hasNext()) {
            String str = pgiter.next();
            Graph pg = graphs.get(str);
            
            if(pg instanceof ProbabilityGraph) {
                SpecificGraph g = rval.addSpecificGraph(str);
            } else {
                rval.graphs.put(str,pg);
            }
        }
        rval.compulsoryCount = this.compulsoryCount;
        return rval;
    }
    
    private class SetGraphAction implements EachTermPairAction {
        Graph g;
        TermList tl;
        SetGraphAction(Graph g, TermList tl) { this.g = g; this.tl = tl; }
        public void doAction(String term1, String term2) {
            g.add(tl.indexOf(term1),tl.indexOf(term2));
        }
    }
    
    /**
     * Set a graph to be a term pair set
     */
    public void setGraphAs(String name, TermPairSet termPairs, TermList termList) {
        Graph g = getGraphByName(name);
        Iterator<Integer> iter = g.iterator(getFullModelSize());
        while(iter.hasNext()) {
            Integer i = iter.next();
            g.remove(i / getFullModelSize(), i % getFullModelSize());
        }
        
        termPairs.forEachPair(new SetGraphAction(g,termList));
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
                Iterator<Integer> i1 = g.iterator(getFullModelSize());
                while(i1.hasNext()) {
                    int i2 = i1.next();
                    int i = i2 / getFullModelSize();
                    int j = i2 % getFullModelSize();
                    if(pg.isConnected(i,j) && !pg.mutable(i,j)) {
                        g.add(i,j);
                    }
                }
                rval.graphs.put(str,g);
            } else {
                rval.graphs.put(str,pg);
            }
        }
        rval.compulsoryCount = this.compulsoryCount;
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
        SpecificGraph rval = new SpecificGraph(getFullModelSize(),name);
        graphs.put(name,rval);
        return rval;
    }
    
    /**
     * Add a (empty) probability graph to the model
     */
    public ProbabilityGraph addProbabilityGraph(String name) {
        if(relationIdx.indexOf(name) == -1)
            relationIdx.add(name);
        ProbabilityGraph rval = new ProbabilityGraph(getFullModelSize());
        graphs.put(name, rval);
        return rval;
    }
    
    /**
     * Add all elements of another graph into this one. Note these graphs must have the same indexing,
     * ie, created by subModel, createSpecificCopy, createImmutableCopy etc.
     */
    public boolean add(Model m) {
        boolean rval = false;
        for(Integer i : m) {
            if(mutable(i))
                rval = add(i) || rval;
            else if(!isConnected(i))
                throw new LogicException("Attempting to add non-mutable: " + i);
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
     * Set this to symmetric difference of this and c
     **/
    public void symmDiffAll(Collection<Integer> c) {
        Iterator<Integer> iterator = c.iterator();
        while(iterator.hasNext()) {
            Integer i = iterator.next();
            if(contains(i)) {
                remove(i);
            } else {
                add(i);
            }
        }
    }
    
    /**
     * The id is a unique id of a relation in this model
     */
    public int id(String relation, int i, int j)  {
        if(i < 0 || j < 0) {
            throw new IllegalArgumentException("Cannot return ID of functional or unassigned argument!");
        }
        int n = getFullModelSize();
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
        return relationIdx.get(id / getFullModelSize() / getFullModelSize());
    }
    
    /**
     * ID -> left assignment
     */
    public int iByID(int id) {
        int n = getFullModelSize();
        return (id % (n*n)) / n;
    }
    
    /**
     * ID -> right assignment
     */
    public int jByID(int id) {
        int n = getFullModelSize();
        return (id % n);
    }
    
    /**
     * ID -> left assignment
     */
    public int iByID(Integer id) {
        int n = getFullModelSize();
        return (id % (n*n)) / n;
    }
    
    /**
     * ID -> right assignment
     */
    public int jByID(Integer id) {
        int n = getFullModelSize();
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
     * Compare one graph to another
     * @return A three element integer array, the first is the agreement, the second those found only in this, the third those found only in parameter
     * @throws IllegalArgumentException if the models do not have the same graph set
     */
    public int[] computeComparison(Model m) {
        if(!m.graphs.keySet().equals(graphs.keySet()))
            throw new IllegalArgumentException("Incomparable graphs!");
        Iterator<Integer> iter = iterator();
        int[] rval = new int[3];
        while(iter.hasNext()) {
            Integer i = iter.next();
            if(getGraphByID(i) instanceof EquivalenceGraph || getGraphByID(i) instanceof MembershipGraph)
                continue;
            if(m.isConnected(i))
                rval[0]++;
            else
                rval[1]++;
        }
        rval[0] -= compulsoryCount;
        rval[2] = m.size() - rval[0] - compulsoryCount;
        return rval;
    }
    
    /**
     * Sum of each graphs {@link Graph#linkCount()}
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
     * Add all the compulsory links
     */
    public void addCompulsorys(Logic logic) {
        if(!hasCompulsory) {
            Model m = logic.getCompulsoryModel(this);
            compulsoryCount = m.size();
            this.add(m);
            hasCompulsory = true;
        }
    }
    
    /**
     * An iterator for the name of every graph in this model, although similar can be achieved
     * graphs.keySet().iterator(), this iterator is ordered so that if a second model is created
     * and <em>all</em> graphs are added <em>in the same order</em> as this iterator these two models will use the same ID
     * for every link
     */
    public Iterator<String> graphNameIterator() { return relationIdx.iterator(); }
    
    public Iterator<Integer> iterator() { return new ModelIterator(); }
    
    private class ModelIterator extends MultiIterator<Integer> {
        private Vector<Integer> graphID;
        
        public ModelIterator() {
            init(construct());
        }
        private Iterator<Iterator<Integer>> construct() {
            Vector<Iterator<Integer>> v = new Vector<Iterator<Integer>>(graphs.size());
            graphID = new Vector<Integer>(graphs.size());
            for(Map.Entry<String,Graph> entries : graphs.entrySet()) {
                v.add(entries.getValue().iterator(getFullModelSize()));
                graphID.add(relationIdx.indexOf(entries.getKey()));
            }
            return v.iterator();
        }
        
        public Integer returnVal(Integer i, int graphNumber) {
            int n = getFullModelSize();
            return i + n * n * graphID.get(graphNumber);
        }
    }
    
    private void readObject(java.io.ObjectInputStream ios) throws java.io.IOException, ClassNotFoundException {
        ios.defaultReadObject();
        ProbabilityGraph.n = getFullModelSize();
    }
}
