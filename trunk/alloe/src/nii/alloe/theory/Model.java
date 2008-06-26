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
        elems = new TreeSet<Integer>();
        this.logic = logic;
        for (int i = 0; i < getFullModelSize(); i++) {
            elems.add(i);
        }
        graphs = new TreeMap<String, Graph>();
        relationIdx = new Vector<String>();
    }

    /**
     * Create a model with the specified graphs on the elements {1,...,n} DELETE this soon!!!
     */
    public Model(TreeMap<String, Graph> graphs, Logic logic) {
        this.graphs = graphs;
        this.logic = logic;
        elems = new TreeSet<Integer>();
        for (int i = 0; i < getFullModelSize(); i++) {
            elems.add(i);
        }
        relationIdx = new Vector<String>();

        Iterator<String> reliter = graphs.keySet().iterator();
        while (reliter.hasNext()) {
            relationIdx.add(reliter.next());
        }
    }

    /**
     * Create a model that is identical to the previous model (NB graphs and elems are not cloned!)
     */
    public Model(Model m) {
        this.relationIdx = (Vector<String>) m.relationIdx.clone();
        this.logic = m.logic;
        this.elems = m.elems;
        graphs = new TreeMap<String, Graph>();
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
        while (iter.hasNext()) {
            String relID = iter.next();
            Graph g = graphs.get(relID);
            if (g instanceof EquivalenceGraph || g instanceof MembershipGraph) {
                rval.graphs.put(relID, g);
            } else if (g instanceof SpecificGraph) {
                rval.graphs.put(relID, new SpecificGraph(getModelSize(), relID));
            } else if (g instanceof ProbabilityGraph) {
                ProbabilityGraph pg = new ProbabilityGraph(getModelSize());
                rval.graphs.put(relID, pg);
                pg.setBaseVal(((ProbabilityGraph) g).getBaseValPos(),
                        ((ProbabilityGraph) g).getBaseValNeg());
            } else {
                try {
                    Class c = g.getClass();
                    Class[] ps = new Class[0];
                    rval.graphs.put(relID, (Graph) c.getConstructor(ps).newInstance());
                } catch (Exception x) {
                    x.printStackTrace();
                    System.exit(-1);
                }
            }
            rval.relationIdx.add(relID);
        }
        Iterator<Integer> riter = rels.iterator();
        while (riter.hasNext()) {
            Integer id = riter.next();
            if (rval.mutable(id)) {
                if (getGraphByID(id) instanceof ProbabilityGraph) {
                    int i = iByID(id);
                    int j = jByID(id);
                    ProbabilityGraph orig = (ProbabilityGraph) getGraphByID(id);
                    ((ProbabilityGraph) rval.getGraphByID(id)).setPosNegVal(
                            i, j, orig.posVal(i, j), orig.negVal(i, j));
                } else {
                    rval.add(id);
                }
            } else {
                if (!rval.isConnected(id)) {
                    throw new IllegalStateException("Graph is non-mutable but does not contain necessary link");
                }
            }
            rval.elems.add(iByID(id));
            rval.elems.add(jByID(id));
        }
        return rval;
    }

    /**
     * Split the model into a connected components. This first finds the set of all
     * elements connected by any parts and then forms a set of submodels for each of these
     * relations.
     */
    public Collection<Model> splitByComponents() {
        if (elems.size() != getFullModelSize()) {
            throw new IllegalStateException("Attempting to split an already-split model");
        }
        boolean[] usedElems = new boolean[getFullModelSize()];
        LinkedList<Model> rval = new LinkedList<Model>();

        for (int i = 0; i < getFullModelSize(); i++) {
            if (usedElems[i]) {
                continue;
            }
            TreeSet<Integer> comp = new TreeSet<Integer>();
            comp.add(i);
            usedElems[i] = true;
            boolean changed;
            do {
                changed = false;
                for (int j = 0; j < getFullModelSize(); j++) {
                    if (usedElems[j]) {
                        continue;
                    }
                    for (Graph g : graphs.values()) {
                        for (int k : comp) {
                            if (g.isConnected(k, j) || g.isConnected(j, k)) {
                                comp.add(j);
                                changed = usedElems[j] = true;
                                break;
                            }
                        }
                    }
                }
            } while (changed);
            TreeSet<Integer> rel = new TreeSet<Integer>();
            for (int r : this) {
                if (comp.contains(iByID(r)) &&
                        comp.contains(jByID(r))) {
                    rel.add(r);
                }
            }
            rval.add(subModel(rel));
        }
        return rval;
    }
    /** The largest number of values that will be assigned to first
     * the first order relations e(t,k). Essentially all should be
     * assigned but this is often impossible as the resulting number
     * of probablities would be modelSize * skolemSize, instead we
     * produce only MAX_ASSIGNED_FIRST_ORDER * skolemSize probabilites
     */
    public static final int MAX_ASSIGNED_FIRST_ORDER = 20;

    private class ArrayComp implements Comparator<Integer> {

        double[] array;

        ArrayComp(double[] array) {
            this.array = array;
        }

        public int compare(Integer i1, Integer i2) {
            if (array[i1] < array[i2]) {
                return 1;
            } else if (array[i1] == array[i2]) {
                return 0;
            } else {
                return -1;
            }
        }

        public boolean equals(Object o) {
            return o == this;
        }
    }

    /**
     * Add probabilities to the first-order relations e.
     * Calculates probabilities in the following way<br>
     * If e(t,k) represents the relation between term t and skolem element k<br>
     * Prob(e(t,k)) = prod(Prob({clauses k is involved in}))<br>
     * If P v Q(k) is a clause and P are the terms not involving in k and 
     * Q(k) are the clauses involving k.<br>
     * Prob(P v Q(k)) = Prob(P) + Prob(Q(e)) - Prob(P) * Prob(Q(e))<br>
     * If P = p_1 v ... v p_n and Q(k) = q_1(k) v .... v q_m(k) then<br>
     * Prob(P) = Sum_{i = 1..n} Prob(p_i) 
     * - Sum_{{i,j} subset {1..n}} Prob(p_i) * Prob(p_j) + 
     * Sum_{{i,j,k} subset {1..n}} Prob(p_i) * Prob(p_j) * Prob(p_k) - ....<br>
     */
    /*public void addFirstOrderGraphs() {
    for(int k = getModelSize(); k < getFullModelSize(); k++) {
    RuleSymbol.FunctionID id = logic.ruleSymbols.modelIdToFunctionID(k);
    double[] probs = new double[getModelSize()];
    for(int i = 0; i < probs.length; i++) {
    probs[i] = 1.0;
    }
    Collection<Rule> rules = null;//logic.findRulesByTerm(id.id);
    for(Rule rule : rules) {
    // Step 1: Check if this function makes any assignments
    for(Rule.Argument arg : rule.arguments.keySet()) {
    if(arg instanceof Rule.FunctionalArgument) {
    Rule.FunctionalArgument fArg = (Rule.FunctionalArgument)fArg;
    for(int i = 0; i < id.args.length; i++) {
    if(fArg.functionArgs[i].hasAssignment()) {
    if(fArg.functionArgs[i].getAssignment()
    != id.args[i]) {
    throw new LogicException("Non-matching functions in the same clause, please sort all variable in functions the same way");
    }
    } else {
    fArg.functionArgs[i].setAssignment(id.args[i]);
    }
    }
    }
    }
    // Step 2: Start recursing on unassigned variable
    double[] clauseProbs = calcFOCProb(rule, k, rules.arguments.keySet().iterator());
    // Step 3:Iteratively update overall probs
    for(int i = 0; i < probs.length; i++) {
    probs[i] *= clauseProbs[i];
    }
    }
    int[] vals = new int[getModelSize()];
    for(int i = 0; i < vals.length; i++) {
    vals[i] = i;
    }
    Arrays.sort(vals, new ArrayComp(probs));
    for(int i = 0; i < Math.min(MAX_ASSIGNED_FIRST_ORDER,getModelSize()); i++) {
    setVal(id("e", i, k),probs[i]);
    setVal(id("e", k, i),probs[i]);
    }
    }
    }
    double[] calcFOCProb(Rule rule, RuleSymbol.FunctionID skolem, Iterator<Rule.Argument> argIter) {
    if(argIter.hasNext()) {
    Rule.Argument arg = argiter.next();
    if(arg.hasAssignment())
    return calcFOCProb(rule, skolem, argIter);
    else {
    if(arg instanceof Rule.FunctionArgument) {
    // Go Screw !
    throw new RuntimeException("Unsupported (multiple functions in same clause)");
    }
    double[] probs = new double[getModelSize()];
    for(int i = 0; i < getModelSize(); i++) {
    probs[i] = 1.0;
    }
    for(int i = 0; i < getModelSize(); i++) {
    arg.setAssignment(i);
    double[] clauseProbs = calcFOCProb(rule,skolem,argIter);
    for(int j = 0; j < getModelSize(); j++) {
    probs[j] *= clauseProbs[j];
    }
    arg.unsetAssignment();
    }
    return probs;
    }
    } else {
    // Finally a clause we can calculate (!)
    Rule.FunctionalArgument fArg;
    for(Rule.Argument arg : rule.arguments) {
    if(arg instanceof FunctionalArgument &&
    arg.getID() == skolem.id) {
    fArg = arg;
    break;
    }
    }
    double[] rval = new double[getModelSize()];
    for(int i = 0; i < getModelSize(); i++) {
    rval[i] = 1.0;
    for(int j = 0; j < rule.length(); j++) {
    double p;
    if(rule.terms.get(j)[0] != fArg &&
    rule.terms.get(j)[0] != fArg) {
    p = getVal(id(rule,j));
    } else if(rule.terms.get(j)[0] != fArg) {
    p = getVal(id(rule.relations.get(j)[0], rule.relations.get(j)[0].getAssignment(), skolem));
    } else if(rule.terms.get(j)[1] != fArg) {
    p = getVal(id(rule.relations.get(j)[0], skolem, rule.relations.get(j)[0].getAssignment()));
    } else {
    p = getVal(id(rule.relations.get(j)[0], skolem, skolem));
    }
    rval[i] = rval[i] + p - rval[i] * p;
    }
    }
    return rval;
    }
    }*/
    /**
     * Join a set of model. This is intended to be used to undo splitByComponents.
     */
    public static Model joinModels(Collection<Model> models, Logic logic) {
        if (models.isEmpty()) {
            throw new IllegalArgumentException();
        }
        Model rval = new Model(logic);
        for (Model m : models) {
            for (String rel : m.relationIdx) {
                if (!rval.relationIdx.contains(rel)) {
                    rval.relationIdx.add(rel);
                }
            }
            for (int id : m) {
                String rel = m.relationByID(id);
                int i = m.iByID(id);
                int j = m.jByID(id);
                if (rval.graphs.get(rel) == null) {
                    Graph g = m.graphs.get(rel);
                    if (g instanceof EquivalenceGraph || g instanceof MembershipGraph) {
                        rval.addBasicGraphs(logic);
                    } else if (g instanceof SpecificGraph) {
                        rval.addSpecificGraph(rel);
                    } else if (g instanceof ProbabilityGraph) {
                        rval.addProbabilityGraph(rel);
                    }
                }
                if (rval.relationIdx.size() <= id / rval.getFullModelSize() / rval.getFullModelSize()) {
                    System.out.println("Oh noes!");
                }
                if (rval.mutable(id)) {
                    if (m.getGraphByID(id) instanceof ProbabilityGraph) {
                        ProbabilityGraph orig = (ProbabilityGraph) m.getGraphByID(id);
                        ((ProbabilityGraph) rval.getGraphByID(id)).setPosNegVal(
                                i, j, orig.posVal(i, j), orig.negVal(i, j));
                    } else {
                        rval.add(id);
                    }
                } else {
                    if (!rval.isConnected(id)) {
                        throw new IllegalStateException("Graph is non-mutable but does not contain necessary link");
                    }
                }
            }
        }
        return rval;
    }

    /**
     * Create a copy where all the probability graphs have been replaced by specific graphs
     */
    public Model createSpecificCopy() {
        Iterator<String> pgiter = graphNameIterator();
        Model rval = new Model(this);
        while (pgiter.hasNext()) {
            String str = pgiter.next();
            Graph pg = graphs.get(str);

            if (pg instanceof ProbabilityGraph) {
                SpecificGraph g = rval.addSpecificGraph(str);
                Iterator<Integer> i1 = pg.iterator(getFullModelSize());
                while (i1.hasNext()) {
                    int i2 = i1.next();
                    int i = i2 / getFullModelSize();
                    int j = i2 % getFullModelSize();
                    if (pg.isConnected(i, j)) {
                        g.add(i, j);
                    }
                }
                rval.graphs.put(str, g);
            } else {
                rval.graphs.put(str, pg);
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
        while (pgiter.hasNext()) {
            String str = pgiter.next();
            Graph pg = graphs.get(str);

            if (pg instanceof SpecificGraph) {
                ProbabilityGraph g = rval.addProbabilityGraph(str);
                g.setBaseVal(negProb);
                Iterator<Integer> i1 = pg.iterator(getFullModelSize());
                while (i1.hasNext()) {
                    int i2 = i1.next();
                    int i = i2 / getFullModelSize();
                    int j = i2 % getFullModelSize();
                    if (pg.isConnected(i, j)) {
                        g.setVal(i, j, posProb);
                    }
                }
                rval.graphs.put(str, g);
            } else {
                rval.graphs.put(str, pg);
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
        while (graphIter.hasNext()) {
            String graphName = graphIter.next();
            Graph g = graphs.get(graphName);
            rval.graphs.put(graphName, g.createCopy());
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
        while (pgiter.hasNext()) {
            String str = pgiter.next();
            Graph pg = graphs.get(str);

            if (pg instanceof ProbabilityGraph) {
                SpecificGraph g = rval.addSpecificGraph(str);
            } else {
                rval.graphs.put(str, pg);
            }
        }
        rval.compulsoryCount = this.compulsoryCount;
        return rval;
    }

    private class SetGraphAction implements EachTermPairAction {

        Graph g;
        TermList tl;

        SetGraphAction(Graph g, TermList tl) {
            this.g = g;
            this.tl = tl;
        }

        public void doAction(String term1, String term2) {
            g.add(tl.indexOf(term1), tl.indexOf(term2));
        }
    }

    /**
     * Set a graph to be a term pair set
     */
    public void setGraphAs(String name, TermPairSet termPairs, TermList termList) {
        Graph g = getGraphByName(name);
        if(g == null)
            g = addSpecificGraph(name);
        Iterator<Integer> iter = g.iterator(getFullModelSize());
        while (iter.hasNext()) {
            Integer i = iter.next();
            g.remove(i / getFullModelSize(), i % getFullModelSize());
        }

        termPairs.forEachPair(new SetGraphAction(g, termList));
    }

    /**
     * Create a copy of this where links are only present if they are immutable
     */
    public Model createImmutableCopy() {
        Iterator<String> pgiter = graphNameIterator();
        Model rval = new Model(this);
        while (pgiter.hasNext()) {
            String str = pgiter.next();
            Graph pg = graphs.get(str);

            if (!(pg instanceof EquivalenceGraph) && !(pg instanceof MembershipGraph)) {
                SpecificGraph g = rval.addSpecificGraph(str);
                Iterator<Integer> i1 = g.iterator(getFullModelSize());
                while (i1.hasNext()) {
                    int i2 = i1.next();
                    int i = i2 / getFullModelSize();
                    int j = i2 % getFullModelSize();
                    if (pg.isConnected(i, j) && !pg.mutable(i, j)) {
                        g.add(i, j);
                    }
                }
                rval.graphs.put(str, g);
            } else {
                rval.graphs.put(str, pg);
            }
        }
        rval.compulsoryCount = this.compulsoryCount;
        return rval;
    }

    /**
     * Adds the basic graphs that is an EquivalenceGraph indexed by "e" and a Membership graph for every set in l
     */
    public void addBasicGraphs(Logic l) {
        /*if(l.isFirstOrder()) {
        graphs.put("e", new SplitGraph(new EquivalenceGraph(), 
        new ProbabilityGraph(getFullModelSize()), 
        new MembershipGraph(getFullModelSize(), new TreeSet<Integer>())),
        getModelSize());
        } else */
        graphs.put("e", new EquivalenceGraph());
        if (relationIdx.indexOf("e") == -1) {
            relationIdx.add("e");
        }
        String str;
        for (Iterator<String> i2 = l.sets.keySet().iterator(); i2.hasNext();) {
            str = i2.next();
            graphs.put("in_" + str, new MembershipGraph(l.sets.get(str)));
            if (relationIdx.indexOf("in_" + str) == -1) {
                relationIdx.add("in_" + str);
            }
        }
    }

    /**
     * Add a (empty) specific graph to the model
     */
    public SpecificGraph addSpecificGraph(String name) {
        if (relationIdx.indexOf(name) == -1) {
            relationIdx.add(name);
        }
        SpecificGraph rval = new SpecificGraph(getFullModelSize(), name);
        graphs.put(name, rval);
        return rval;
    }

    /**
     * Add a (empty) probability graph to the model
     */
    public ProbabilityGraph addProbabilityGraph(String name) {
        if (relationIdx.indexOf(name) == -1) {
            relationIdx.add(name);
        }
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
        for (Integer i : m) {
            if (mutable(i)) {
                rval = add(i) || rval;
            } else if (!isConnected(i)) {
                throw new LogicException("Attempting to add non-mutable: " + i);
            }
        }
        return rval;
    }

       /**
     * Does this graph contain any elements in the list.
     */
    public boolean containsAny(Collection<Integer> ids) {
        Iterator<Integer> i = ids.iterator();
        while (i.hasNext()) {
            if (isConnected(i.next())) {
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
        while (iterator.hasNext()) {
            Integer i = iterator.next();
            if (contains(i)) {
                remove(i);
            } else {
                add(i);
            }
        }
    }

    /**
     * The id is a unique id of a relation in this model
     */
    public int id(String relation, int i, int j) {
        if (i < 0 || j < 0) {
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
        return (id % (n * n)) / n;
    }

    /**
     * ID -> right assignment
     */
    public int jByID(int id) {
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
     * Same as <code>Graph.isConnected(iByID(id),jByID(id))</code>
     */
    public boolean isConnected(int id) {
        return graphs.get(relationByID(id)).isConnected(iByID(id), jByID(id));
    }

    /**
     * Same as <code>Graph.mutable(iByID(id),jByID(id))</code>
     */
    public boolean mutable(int id) {
        return graphs.get(relationByID(id)).mutable(iByID(id), jByID(id));
    }

    /**
     * Same as <code>Graph.add(iByID(id),jByID(id))</code>
     */
    public boolean add(Integer id) {
        boolean rval = !graphs.get(relationByID(id)).isConnected(iByID(id), jByID(id));
        graphs.get(relationByID(id)).add(iByID(id), jByID(id));
        return rval;
    }

    /**
     * Same as <code>Graph.remove(iByID(id),jByID(id))</code>
     */
    public boolean remove(Integer id) {
        if (graphs.get(relationByID(id)).isConnected(iByID(id), jByID(id))) {
            graphs.get(relationByID(id)).remove(iByID(id), jByID(id));
            return true;
        }
        return false;
    }

    /**
     * Same as <code>Graph.setVal(iByID(id),jByID(id),val)</code>
     */
    public void setVal(int id, double val) {
        graphs.get(relationByID(id)).setVal(iByID(id), jByID(id), val);
    }

    /**
     * Same as <code>Graph.get(iByID(id),jByID(id),val)</code>
     */
    public void getVal(int id) {
        graphs.get(relationByID(id)).getVal(iByID(id), jByID(id));
    }

    /**
     * Compare one graph to another
     * @return A three element integer array, the first is the agreement, the second those found only in this, the third those found only in parameter
     * @throws IllegalArgumentException if the models do not have the same graph set
     */
    public int[] computeComparison(Model m) {
        if (!m.graphs.keySet().equals(graphs.keySet())) {
            throw new IllegalArgumentException("Incomparable graphs!");
        }
        Iterator<Integer> iter = iterator();
        int[] rval = new int[3];
        while (iter.hasNext()) {
            Integer i = iter.next();
            if (getGraphByID(i) instanceof EquivalenceGraph || getGraphByID(i) instanceof MembershipGraph) {
                continue;
            }
            if (m.isConnected(i)) {
                rval[0]++;
            } else {
                rval[1]++;
            }
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
        while (gi.hasNext()) {
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
        return (Vector<String>) relationIdx.clone();
    }

    /**
     * Add all the compulsory links
     */
    public void addCompulsorys(Logic logic) {
        if (!hasCompulsory) {
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
    public Iterator<String> graphNameIterator() {
        return relationIdx.iterator();
    }

    public Iterator<Integer> iterator() {
        return new ModelIterator();
    }

    private class ModelIterator extends MultiIterator<Integer> {

        private Vector<Integer> graphID;

        public ModelIterator() {
            init(construct());
        }

        private Iterator<Iterator<Integer>> construct() {
            Vector<Iterator<Integer>> v = new Vector<Iterator<Integer>>(graphs.size());
            graphID = new Vector<Integer>(graphs.size());
            for (Map.Entry<String, Graph> entries : graphs.entrySet()) {
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
