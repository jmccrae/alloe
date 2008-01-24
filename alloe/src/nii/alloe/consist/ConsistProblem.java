package nii.alloe.consist;
import nii.alloe.theory.AssignmentAction;
import nii.alloe.theory.Graph;
import nii.alloe.theory.InconsistentAction;
import nii.alloe.theory.Logic;
import nii.alloe.theory.LogicException;
import nii.alloe.theory.Model;
import nii.alloe.theory.ProbabilityGraph;
import nii.alloe.theory.Rule;
import nii.alloe.niceties.*;
import java.util.*;

/**
 * Creates a matrix which can be solved by the 0-1 integer programming solver.
 * The life cycle of this class is
 * <ol><li> Generate Complete Graph: Apply every rule adding all conclusion until convergence </li>
 * <li> Generate "nearly-complete" Graph: For each link in the complete graph search for a cheaper <em>unifying premise</em>
 * (that is a a premise that this link would not be in the graph without) </li>
 * <li> Generate all rules: <ol>
 *      <li>Generate all base rules (premises satisfied in complete graph). </li>
 *      <li>Generate all rules by resolution</li>
 *      <li>For every rule with all premises satisfied, conclusion not in prob graph, add column to matrix </li>
 * </ol></li>
 * <li> Reduce Matrix: <ol>
 *      <li> Remove row_i if \exists row_j s.t. Cols(row_i) \subseteq Cols(row_j) and cost(row_i) > cost(row_j) </li>
 *      <li> For each col_i remove all cols in intersection of Cols(row_i) for row_i \in Rows(col_i) </li>
 * </ol></li>
 * <li> "Cooled Reduction": <ol>
 *      <li> Order all of the links in the complete graph by the number of base rules they participate in </li>
 *      <li> Order rules according to the least index of any link in the rule </li>
 *      <li> If a rule with score i has been added the matrix has been completed for the rows with index less than i </li>
 * </ol></li>
 * <li> Output SparseMatrix to be solved by 0-1 solver. </li></ol>
 *
 * @author John McCrae, National Institute of Informatics
 */
public class ConsistProblem implements AlloeProcess,java.io.Serializable,Runnable {
    // Inputs
    private Logic logic;
    private Model probModel;
    
    private Model completeModel;
    private LinkedList<Rule> baseRules;
    private PriorityQueue<Rule> ruleQueue;
    /** Result of matrix generation */
    public SparseMatrix mat;
    private Vector<Integer> linkOrders;
    private int lastReduction = -1;
    private TreeSet<Rule> usedRules;
    private TreeMap<Integer,TreeSet<Integer>> unifyingPremises;
    
    /** Creates a new instance of ConsistProblem
     * @param logic The logic the model should be made constitent with
     * @param probModel A weighted model of the inconsistent data
     */
    public ConsistProblem(Logic logic, Model probModel) {
        this.logic = logic;
        this.probModel = probModel;
    }
    
    private boolean modelCompleted = false;
    private boolean modelReduced = false;
    private boolean baseRulesFormed = false;
    
    /**
     * Create a problem matrix
     * @return problem matrix
     * @see ConsistSolver
     */
    public SparseMatrix buildProblemMatrix() {
        lastReduction = -1;
        
        if(!modelCompleted) {
            state = STATE_COMPLETING;
            fireNewProgressChange(-1);
            
            Model compulsory = logic.getCompulsoryModel(probModel);
            List<Integer> impossible = logic.getNegativeModel(probModel);
            if(compulsory.containsAny(impossible))
                throw new LogicException("Compulsory and Impossibles coincide");
            probModel.add(compulsory);
            
            prepareCompleteGraphs();
            logic.consistCheck(completeModel,new Completer());
            completeModel.removeAll(impossible);
            compulsory = null;
            impossible = null;
            
            modelCompleted = true;
            if(state == STATE_COMPLETING)
                state = STATE_REDUCING;
            else
                return null;
        }
        if(!modelReduced) {
            fireNewProgressChange(-1);
            
            reduceCompleteGraph();
            
            modelReduced = true;
            if(state == STATE_REDUCING)
                state = STATE_BASE;
            else
                return null;
        }
        if(!baseRulesFormed) {
            fireNewProgressChange(-1);
            
            baseRules = new LinkedList<Rule>();
            logic.premiseSearch(completeModel,new BaseRuleBuilder());
            
            baseRulesFormed = true;
            if(state == STATE_BASE)
                state = STATE_OK;
            else
                return null;
        }
        
        fireNewProgressChange(0);
        if(baseRules.size() == 0) {
            mat = new SparseMatrix();
            fireFinished();
            return mat;
        }
        
        orderRules();
        
        initializeRuleQueue();
        
        while(ruleQueue.peek() != null && state == STATE_OK) {
            Rule rule = ruleQueue.poll();
            
            addRuleToMatrix(rule);
            
            queueNewRules(rule);
            
            
            if(canReduce(rule)) {
                reduceMatrix(rule.score.intValue());
            }
            fireNewProgressChange((double)mat.rows.size() / (double)(ruleQueue.size() + mat.rows.size()));
        }
        reduceMatrix(linkOrders.size());
        
        addCosts();
        
        if(state == STATE_OK) {
            modelCompleted = modelReduced = baseRulesFormed = false;
            fireFinished();
        }
        
        return mat;
    }
    
    // TODO: Add expand matrix function
    
    /**
     * Build an incomplete problem matrix for the growing solver. Specifically it will only
     * create columns based on rules whose premise is entirely true in baseModel
     * @param baseModel The model so far generated by the growing solver algorithm
     * @return problem matrix
     * @see GrowingSolver
     */
    public SparseMatrix buildGrowingProblemMatrix(Model baseModel) {
        lastReduction = -1;
        
        if(!baseRulesFormed) {
            state = STATE_BASE;
            fireNewProgressChange(-1);
            
            baseRules = new LinkedList<Rule>();
            BaseRuleBuilder brb = new BaseRuleBuilder();
            brb.limitToCompleteModel = false;
            logic.premiseSearch(baseModel,brb);
            
            if(baseRules.size() == 0)
                return mat = new SparseMatrix();
            baseRulesFormed = true;
            if(state != STATE_BASE)
                return null;
        }
        state = STATE_OK;
        fireNewProgressChange(0);
        orderRules();
        
        initializeRuleQueue();
        
        while(ruleQueue.peek() != null && state == STATE_OK) {
            Rule rule = ruleQueue.poll();
            
            addRuleToMatrix(rule);
            
            queueNewRules(rule);
            
            
            if(canReduce(rule)) {
                reduceMatrix(rule.score.intValue());
            }
            
            fireNewProgressChange((double)mat.rows.size() / (double)(ruleQueue.size() + mat.rows.size()));
        }
        reduceMatrix(linkOrders.size());
        
        addCosts();
        
        if(state == STATE_OK) {
            baseRulesFormed = false;
            fireFinished();
        }
        
        return mat;
    }
    
    private void prepareCompleteGraphs() {
        unifyingPremises = new TreeMap<Integer, TreeSet<Integer>>();
        completeModel = probModel.createSpecificCopy();
        Iterator<Integer> iter = completeModel.iterator();
        while(iter.hasNext()) {
            TreeSet<Integer> premiseSet = new TreeSet<Integer>();
            Integer i = iter.next();
            premiseSet.add(i);
            unifyingPremises.put(i,premiseSet);
        }
    }
    
    private TreeMap<Integer,Integer> relationCounts;
    private void orderLinks() {
        relationCounts = new TreeMap<Integer,Integer>();
        
        Iterator<Rule> riter = baseRules.iterator();
        while(riter.hasNext()) {
            Rule r = riter.next();
            
            r.forAllAssignments(probModel, new AssignmentAction() {
                public boolean action(Rule r, int i, int j, int k) {
                    int id = probModel.id(r.relations.get(i),j,k);
                    if(relationCounts.get(id) != null) {
                        relationCounts.put(id,relationCounts.get(id)+1);
                    } else {
                        relationCounts.put(id,1);
                    }
                    return true;
                }
            });
        }
        
        int linkCount = relationCounts.size();
        
        
        linkOrders = new Vector<Integer>(linkCount);
        linkOrders.addAll(relationCounts.keySet());
        
        
        Collections.sort(linkOrders, new Comparator<Integer>() {
            public int compare(Integer i1, Integer i2) {
                if(relationCounts.get(i1) > relationCounts.get(i2)) {
                    return +1;
                } else if(relationCounts.get(i1) < relationCounts.get(i2)) {
                    return -1;
                } else {
                    return 0;
                }
            }
            public boolean equals(Object object) {
                return object == this;
            }
        });
        
        relationCounts = null;
    }
    
    
    private void orderRules() {
        orderLinks();
        
        
        Iterator<Rule> riter = baseRules.iterator();
        while(riter.hasNext()) {
            Rule r = riter.next();
            
            scoreRule(r);
            ruleMaxScore(r);
        }
        
    }
    
    private void scoreRule(Rule r) {
        r.score = Integer.MAX_VALUE;
        
        r.forAllAssignments(probModel,false,new AssignmentAction() {
            public boolean action(Rule r, int i, int j, int k) {
                r.score = Math.min(r.score, linkOrders.indexOf(new Integer(probModel.id(r.relations.get(i),j,k))));
                return true;
            }
        });
    }
    
    private void ruleMaxScore(Rule r) {
        r.maxScore = Integer.MAX_VALUE;
        
        r.forAllAssignments(probModel,false,new AssignmentAction() {
            public boolean action(Rule r, int i, int j, int k) {
                r.maxScore = Math.max(r.maxScore, linkOrders.indexOf(new Integer(probModel.id(r.relations.get(i),j,k))));
                return true;
            }
        });
    }
    
    private void initializeRuleQueue() {
        ruleQueue = new PriorityQueue<Rule>(baseRules.size(), new Comparator<Rule>() {
            public int compare(Rule r1, Rule r2) {
                int i = r1.score.compareTo(r2.score);
                if(i == 0) {
                    if(r1.length() < r2.length()) {
                        return -1;
                    } else if(r1.length() > r2.length()) {
                        return 1;
                    } else {
                        return 0;
                    }
                } else {
                    return i;
                }
            }
            public boolean equals(Object o) {
                if(o != this) return false; return true;
            }
        });
        
        ruleQueue.addAll(baseRules);
        
        usedRules = new TreeSet<Rule>();
        
        mat = new SparseMatrix();
    }
    
    private void addRuleToMatrix(Rule r) {
        if(r.isRuleSatisfied(probModel)) {
            usedRules.add(r);
            return;
        }
        System.out.println("Adding: " + r.toString());
        
        TreeSet<Integer> elems = columnForRule(r);
        
        if(mat.cols.isEmpty()) {
            mat.addColumn(new Integer(1),elems);
        } else {
            mat.addColumn(mat.cols.lastKey() + 1, elems);
        }
    }
    
    private void queueNewRules(Rule r) {
        Iterator<Rule> riter = ruleQueue.iterator();
        LinkedList<Rule> tempQueue = new LinkedList<Rule>();
        while(riter.hasNext()) {
            Rule r2 = riter.next();
            
            if(r.compareTo(r2) != 0) {
                if(r.canResolveWith(r2)) {
                    Rule newR = r.resolve(r2,probModel);
                    
                    //newR.limitToModel(completeModel);
                    
                    if(!isSubsumed(newR,r)) {
                        scoreRule(newR);
                        ruleMaxScore(newR);
                        tempQueue.add(newR);
                        System.out.println(r.toString() + " + " + r2.toString() + " = " + newR.toString());
                    }
                }
                if(r2.canResolveWith(r)) {
                    Rule newR = r2.resolve(r,probModel);
                    
                    //newR.limitToModel(completeModel);
                    
                    if(!isSubsumed(newR,r)) {
                        scoreRule(newR);
                        ruleMaxScore(newR);
                        tempQueue.add(newR);
                        System.out.println(r.toString() + " + " + r2.toString() + " = " + newR.toString());
                    }
                }
            }
            
        }
        ruleQueue.addAll(tempQueue);
    }
    
    private boolean isSubsumed(Rule newR, Rule r) {
        // Subsumption check
        // 1. Zero length rule is subsumed by all
        if(newR.length() == 0)
            return true;
        
        // 2. Sumsumption by creating rule (is this possible??)
        if(r.subsumes(newR,probModel))
            return true;
        
        // 3. Not in complete model
        //if(!newR.completelyTrueIn(completeModel))
        //    return true;
        
        // 4. Unifying premises of some part of premise in conclusion
        TreeSet<Integer> premises = new TreeSet<Integer>();
        for(int i = 0; i < newR.premiseCount; i++) {
            TreeSet<Integer> premiseSet = unifyingPremises.get(probModel.id(newR,i));
            if(premiseSet != null) {
                premises.addAll(premiseSet);
                if(newR.conclusionContains(premiseSet, probModel)) {
                    return true;
                }
            }
        }
        
        // 5. Subsumed in Matrix
        TreeSet<Integer> elems = columnForRule(newR);
        Iterator<Integer> coliter = mat.cols.keySet().iterator();
        while(coliter.hasNext()) {
            if(mat.colSubset(coliter.next(), elems))
                return true;
        }
        
        // 6. Subsumed by queued rule
        Iterator<Rule> subsumers = ruleQueue.iterator();
        while(subsumers.hasNext()) {
            if(subsumers.next().subsumes(newR,probModel))
                return true;
        }
        
        // 7. Subsumed by discarded rule
        subsumers = usedRules.iterator();
        while(subsumers.hasNext()) {
            if(subsumers.next().subsumes(newR,probModel))
                return true;
        }
        return false;
    }
    
    private boolean canReduce(Rule rule) {
        return rule.score > lastReduction;
    }
    
    private void reduceMatrix(int reduceTo) {
        lastReduction = reduceTo;
        while(rowReduce(reduceTo) || columnReduce());
        
        // While we are here clear out cached rules
        Iterator<Rule> i = usedRules.iterator();
        while(i.hasNext()) {
            Rule r = i.next();
            if(r.maxScore < reduceTo) {
                System.out.println("Clearing cached rule:" + r.toString() + " (score=" + r.maxScore + ")");
                i.remove();
            }
        }
    }
    
    private boolean rowReduce(int reduceTo) {
        boolean rval = false;
        Iterator<Integer> i = mat.rows.keySet().iterator();
        while(i.hasNext()) {
            Integer row = i.next();
            if(linkOrders.indexOf(row) < reduceTo) {
                Iterator<Integer> i2 = mat.rows.keySet().iterator();
                while(i2.hasNext()) {
                    Integer superRow = i2.next();
                    if(costForRow(row) > costForRow(superRow) &&
                            mat.rowSubset(row, superRow)) {
                        mat.removeRow(row, i);
                        rval = true;
                        break;
                    }
                }
            }
        }
        return rval;
    }
    
    private boolean columnReduce() {
        boolean rval = false;
        Iterator<Integer> i = mat.cols.keySet().iterator();
        while(i.hasNext()) {
            Integer col = i.next();
            Iterator<Integer> i2 = mat.cols.keySet().iterator();
            while(i2.hasNext()) {
                Integer col2 = i2.next();
                if(col != col2 && mat.colSubset(col2, col)) {
                    mat.removeColumn(col, i);
                    rval = true;
                    break;
                }
            }
        }
        return rval;
    }
    
    private double costForRow(Integer row) {
        ProbabilityGraph pg = (ProbabilityGraph)probModel.getGraphByID(row);
        int i = probModel.iByID(row);
        int j = probModel.jByID(row);
        if(pg.isConnected(i,j)) {
            return pg.removeVal(i,j);
        } else {
            return pg.addVal(i,j);
        }
    }
    
    private class ColumnForRuleAction implements AssignmentAction {
        TreeSet<Integer> elems;
        ColumnForRuleAction(TreeSet<Integer> e) { elems = e; }
        public boolean action(Rule r, int i, int j, int k) {
            elems.add(new Integer(probModel.id(r.relations.get(i), j, k)));
            return true;
        }
    }
    
    private TreeSet<Integer> columnForRule(Rule r) {
        TreeSet<Integer> elems = new TreeSet<Integer>();
        
        r.forAllAssignments(probModel,new ColumnForRuleAction(elems));
        
        return elems;
    }
    
    private void addCosts() {
        Iterator<Integer> i = mat.rows.keySet().iterator();
        while(i.hasNext()) {
            Integer row = i.next();
            mat.setElem(row,0,costForRow(row));
        }
    }
    
    private void reduceCompleteGraph() {
        Iterator<Map.Entry<Integer,TreeSet<Integer>>> iter = unifyingPremises.entrySet().iterator();
        while(iter.hasNext()) {
            Map.Entry<Integer,TreeSet<Integer>> entry = iter.next();
            Integer currID = entry.getKey();
            if(!probModel.isConnected(currID) &&
                    completeModel.isConnected(currID)) {
                Model subModel = probModel.subModel(entry.getValue());
                PremiseCost pc = new PremiseCost();
                logic.premiseSearch(subModel, pc);
                
                double cost = pc.cost;
                if(!pc.marked.contains(currID)) {
                    cost += costForRow(currID);
                }
                
                Iterator<Integer> iter2 = entry.getValue().iterator();
                while(iter2.hasNext()) {
                    Integer id = iter2.next();
                    double cost2 = costForRow(id);
                    if(cost > cost2) {
                        System.out.println("Removing from complete graph: " + completeModel.relationByID(currID)
                        + "("+ completeModel.iByID(currID) + "," + completeModel.jByID(currID) + ")");
                        completeModel.remove(currID);
                        break;
                    }
                }
            }
        }
    }
    
    public long profileComplete() {
        long rval = System.nanoTime();
        
        prepareCompleteGraphs();
        
        logic.consistCheck(completeModel,new Completer());
        
        return System.nanoTime() - rval;
    }
    
    private class Completer implements InconsistentAction {
        public boolean doAction(Logic logic,
                Model m,
                Rule rule) {
            boolean rval = true;
            if(rule.conclusionMustBeSatisified(probModel)) {
                return true;
            }
            
            TreeSet<Integer> premises = new TreeSet<Integer>();
            for(int i = 0; i < rule.premiseCount; i++) {
                TreeSet<Integer> premiseSet = unifyingPremises.get(probModel.id(rule.relations.get(i),
                        rule.terms.get(i)[0].getAssignment(),
                        rule.terms.get(i)[1].getAssignment()));
                if(premiseSet != null) {
                    premises.addAll(premiseSet);
                    if(rule.conclusionContains(premiseSet, probModel)) {
                        return true;
                    }
                }
            }
            
            for(int i = rule.premiseCount; i < rule.length(); i++) {
                Graph g = m.graphs.get(rule.relations.get(i));
                int id = probModel.id(rule,i);
                
                if(!m.isConnected(id) &&
                        m.mutable(id)) {
                    System.out.println("Adding to complete graph: " + m.relationByID(id) + "("+ m.iByID(id) + "," + m.jByID(id) + ")");
                    m.add(id);
                    rval = false;
                }
                
                if(probModel.mutable(id)) {
                    TreeSet<Integer> premiseSet = unifyingPremises.get(id);
                    if(premiseSet != null) {
                        rval = !premiseSet.retainAll(premises) && rval;
                    } else {
                        premiseSet = new TreeSet<Integer>();
                        premiseSet.addAll(premises);
                        premiseSet.add(id);
                        unifyingPremises.put(id,premiseSet);
                        rval = false;
                    }
                }
            }
            return rval;
        }
    }
    
    private class BaseRuleBuilder implements InconsistentAction {
        boolean limitToCompleteModel = true;
        public boolean doAction(Logic logic,
                Model m,
                Rule rule) {
            Rule r = rule.createCopy();
            r.multiplexFunctions(m.elems);
            if(limitToCompleteModel)
                r.limitToModel(completeModel);
            r.simplify(probModel);
            if(r.length() > 0) {
                baseRules.add(r);
            }
            return true;
        }
    }
    
    private class PremiseCost implements InconsistentAction {
        double cost;
        TreeSet<Integer> marked;
        
        PremiseCost() { cost = 0; marked = new TreeSet<Integer>(); }
        
        public boolean doAction(Logic logic,
                Model m,
                Rule rule) {
            double minCost = Double.MAX_VALUE;
            int minId = -1;
            for(int i = rule.premiseCount; i < rule.length(); i++) {
                Graph g = m.graphs.get(rule.relations.get(i));
                if(m.mutable(m.id(rule,i))) {
                    int id = m.id(rule,i);
                    if(marked.contains(id)) {
                        return true;
                    }
                    double thisCost = costForRow(m.id(rule,i));
                    if(thisCost < minCost) {
                        minCost = thisCost;
                        minId = id;
                    }
                } else {
                    minCost = -1;
                    minId = -1;
                }
            }
            if(minId >= 0) {
                cost += minCost;
                marked.add(minId);
            }
            return true;
        }
    }
    
    private transient LinkedList<AlloeProgressListener> aplListeners;
    private transient Thread theThread;
    private transient int state;
    private static final int STATE_OK = 0;
    private static final int STATE_STOPPING = 1;
    private static final int STATE_UNPAUSEABLE = 2;
    private static final int STATE_COMPLETING = 3;
    private static final int STATE_REDUCING = 4;
    private static final int STATE_BASE = 5;
    
     /** Register a progress listener */
    public void addProgressListener(AlloeProgressListener apl) {
        if(aplListeners == null)
            aplListeners = new LinkedList<AlloeProgressListener>();
        if(!aplListeners.contains(apl))
            aplListeners.add(apl);
    }
    
    
    
    private void fireNewProgressChange(double newProgress) {
        if(aplListeners != null) {
            Iterator<AlloeProgressListener> apliter = aplListeners.iterator();
            while(apliter.hasNext()) {
                apliter.next().progressChange(newProgress);
            }
        }
    }
    
    private void fireFinished() {
        if(aplListeners != null) {
            Iterator<AlloeProgressListener> apliter = aplListeners.iterator();
            while(apliter.hasNext()) {
                apliter.next().finished();
            }
        }
    }
    
    
    
    /** Start process. It is expected that this function should start the progress
     * in a new thread */
    public void start() {
        theThread = new Thread(this);
        state = STATE_OK;
        theThread.start();
    }
    
    /** Pause the process. The assumption is that this will work by changing a variable
     * in the running thread and then wait for this thread to finish by use of join().
     * It is assumed that the this object is Serializable, otherwise it's your problem
     * to assure the object is ok when resume() is called.
     *
     * @throws CannotPauseException If the process is not in a state where it can be resumed
     */
    public void pause() throws CannotPauseException {
        try {
            if(state == STATE_UNPAUSEABLE)
                throw new CannotPauseException("Some reason");
            state = STATE_STOPPING;
            theThread.join();
        } catch(InterruptedException x) {
            throw new CannotPauseException("The thread was interrupted");
        }
    }
    
    /** Resume the process.
     * @see #pause()
     */
    public void resume() {
        theThread = new Thread(this);
        state = STATE_OK;
        theThread.start();
    }
    
    /** Get a string representation of the current action being performed */
    public String getStateMessage() {
        if(state == STATE_OK)
            return "Building problem matrix: ";
        else if(state == STATE_STOPPING)
            return "Pausing: ";
        else if(state == STATE_COMPLETING)
            return "Completing Model";
        else if(state == STATE_REDUCING)
            return "Reducing Complete Model";
        else if(state == STATE_BASE)
            return "Creating Base Rules";
        else
            return "???";
    }

    public void run() {
        buildProblemMatrix();
    }
}

