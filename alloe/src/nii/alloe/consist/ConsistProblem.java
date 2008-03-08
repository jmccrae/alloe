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
    
    // A model consisting of all possible terms
    private Model completeModel;
    // Used to store any inconsistent rules before adding to the queue
    private LinkedList<Rule> baseRules;
    // The set of rules yet to be processed
    private RuleQueue ruleQueue;
    /** Result of matrix generation */
    public SparseMatrix mat;
    // An ordering method on links
    private Vector<Integer> linkOrders;
    // The last reduction applied if any
    private int lastReduction = -1;
    // A set of rules which have been used (kept for subsumption checking)
    private TreeSet<Rule> usedRules;
    // For each term in a rule, the set of terms it is dependent on (for complete model reduction)
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
        completeAndReduceModel();
        if(state != STATE_BASE)
            return null;
        
        if(!baseRulesFormed) {
            formBaseRules();
            if(state != STATE_OK)
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
            
            if(!ruleQueue.lastPollConsistent())
                addRuleToMatrix(rule);
            else
                usedRules.add(rule);
            
            queueNewRules(rule,ruleQueue.lastPollConsistent());
            
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
    
    public LinkedList<Rule> getBases(Model generatedModel) {
        baseRules = new LinkedList<Rule>();
        
        logic.consistCheck(generatedModel,new BaseRuleBuilder(false));
        
        return baseRules;
    }
    
    /**
     * Build an incomplete problem matrix for the growing solver. Specifically it will only
     * create columns based on rules whose premise is entirely true in baseModel
     * @param generatedModel The current best candidate solution (for the first matrix this is generatedModel)
     * @return problem matrix or null if the candidate solution is consistent
     * @see GrowingSolver
     */
    public SparseMatrix buildGrowingProblemMatrix(Model generatedModel) {
        baseRules = new LinkedList<Rule>();
        TreeSet<Rule> oldRules = new TreeSet<Rule>();
        logic.consistCheck(generatedModel,new BaseRuleBuilder(false));
        if(mat == null) {
            mat = new SparseMatrix();
            if(baseRules.size() == 0)
                return null;
        } else {
            TreeSet<Integer> resolvePoints = new TreeSet<Integer>();
            for(Rule r : baseRules) {
                for(int i = 0; i < r.length(); i++) {
                    int id = probModel.id(r,i);
                    if(i < r.premiseCount && !probModel.isConnected(id) ||
                            i >= r.premiseCount && probModel.isConnected(id)) {
                        resolvePoints.add(id);
                    }
                }
            }
            if(baseRules.size() == 0)
                return null;
            for(Integer col : mat.cols.keySet()) {
                if(col < 1)
                    continue;
                if(!containsAny(mat.getCol(col),resolvePoints))
                    continue;
                Rule r = mat.columnToRule(col,probModel);
                baseRules.add(r);
                oldRules.add(r);
            }
            resolvePoints = null;
        }
        orderRules();
        initializeRuleQueue();
        while(ruleQueue.peek() != null && state == STATE_OK) {
            Rule rule = ruleQueue.poll();
            
            if(!ruleQueue.lastPollConsistent()) {
                if(!oldRules.contains(rule))
                    addRuleToMatrix(rule);
                else
                    oldRules.remove(rule);
            } else
                usedRules.add(rule);
            
            queueNewRules(rule,ruleQueue.lastPollConsistent());
        }
        addCosts();
        
        if(state == STATE_OK) {
            modelCompleted = modelReduced = baseRulesFormed = false;
            fireFinished();
        }
        
        return mat;
    }
    
    private boolean containsAny(Set<Integer> set1, Set<Integer> set2) {
        for(Integer i : set2) {
            if(set1.contains(i))
                return true;
        }
        return false;
    }
    
    /** Complete the model and then remove any unnecessary links
     */
    private void completeAndReduceModel() {
        if(!modelCompleted) {
            state = STATE_COMPLETING;
            fireNewProgressChange(-1);
            
            probModel.addCompulsorys(logic);
            List<Integer> impossible = logic.getNegativeModel(probModel);
            
            prepareCompleteGraphs();
            Completer c = new Completer();
            do {
                c.newLinks.clear();
                logic.consistCheck(completeModel,c);
                completeModel.addAll(c.newLinks);
            } while(!c.newLinks.isEmpty());
            completeModel.removeAll(impossible);
            impossible = null;
            
            modelCompleted = true;
            if(state == STATE_COMPLETING)
                state = STATE_REDUCING;
            else
                return;
        }
        if(!modelReduced) {
            fireNewProgressChange(-1);
            
            reduceCompleteGraph();
            
            modelReduced = true;
            if(state == STATE_REDUCING)
                state = STATE_BASE;
        }
    }
    
    /** Form all the base rules */
    private void formBaseRules() {
        fireNewProgressChange(-1);
        
        baseRules = new LinkedList<Rule>();
        logic.premiseSearch(completeModel,new BaseRuleBuilder());
        
        baseRulesFormed = true;
        if(state == STATE_BASE)
            state = STATE_OK;
    }
    
    /** Initialize for completion. That is add the probModel to completeModel and set
     * all the initial unifyingPremises */
    private void prepareCompleteGraphs() {
        unifyingPremises = new TreeMap<Integer, TreeSet<Integer>>();
        completeModel = probModel.createSpecificCopy();
        for(Integer i : completeModel) {
            TreeSet<Integer> premiseSet = new TreeSet<Integer>();
            premiseSet.add(i);
            unifyingPremises.put(i,premiseSet);
        }
    }
    
    /** Order the links by the largest number of base rules they occur in */
    private void orderLinks() {
        TreeMap<Integer,Integer> relationCounts = new TreeMap<Integer,Integer>();
        int max = 0;
        for(Rule r : baseRules) {
            for(int i = 0; i < r.length(); i++) {
                if(relationCounts.get(i) != null) {
                    relationCounts.put(i,relationCounts.get(i)+1);
                } else {
                    relationCounts.put(i,1);
                }
                max++;
            }
        }
        
        linkOrders = new Vector<Integer>(relationCounts.keySet());
        
        for(Integer link : probModel) {
            if(relationCounts.get(link) != null)
                relationCounts.put(link,relationCounts.get(link)+max);
        }
        
        Collections.sort(linkOrders, new InverseComparator<Integer>(
                new MapComparator<Integer,Integer>(relationCounts)));
    }
    
    /** Order the rules
     * @see orderLinks()
     */
    private void orderRules() {
        orderLinks();
        
        for(Rule r : baseRules) {
            scoreRule(r);
        }
    }
    
    /** Add the highest and lowest scoring link information to the rule */
    private void scoreRule(Rule r) {
        r.score = Integer.MAX_VALUE;
        r.maxScore = 0;
        
        for(int i = 0; i < r.length(); i++) {
            r.score = Math.min(r.score, linkOrders.indexOf(
                    probModel.id(r,i)));
            r.maxScore = Math.max(r.maxScore, linkOrders.indexOf(
                    probModel.id(r,i)));
        }
    }
    
    /** Add all rules found to rule queue */
    private void initializeRuleQueue() {
        ruleQueue = new RuleQueue();
        
        ruleQueue.addAll(baseRules);
        
        baseRules = null;
        
        if(usedRules == null) {
            usedRules = new TreeSet<Rule>();
            mat = new SparseMatrix();
        }
    }
    
    /** Used by the growing solver */
    private void expandRuleQueue() {
        if(usedRules.size() == 0 && mat.cols.size() == 0)
            return;
        LinkedList<Rule> newRules = new LinkedList<Rule>();
        for(Rule r : baseRules) {
            for(Rule r2 : usedRules) {
                newRules.addAll(getNewRules(r,r2));
            }
            for(Integer col : mat.cols.keySet()) {
                Rule r2 = mat.columnToRule(col, probModel);
                newRules.addAll(getNewRules(r,r2));
            }
        }
        ruleQueue.addAll(newRules);
    }
    
    private void addRuleToMatrix(Rule r) {
        assert(!r.isRuleSatisfied(probModel));
        //if(r.isRuleSatisfied(probModel)) {
        //    usedRules.add(r);
        //    return;
        //}
        //Output.out.println("Adding: " + r.toString());
        
        TreeSet<Integer> elems = columnForRule(r);
        
        if(mat.cols.isEmpty()) {
            mat.addColumn(new Integer(1),elems);
        } else {
            mat.addColumn(mat.cols.lastKey() + 1, elems);
        }
    }
    
    /** Find resolvents (if any) */
    private LinkedList<Rule> getNewRules(Rule r, Rule r2) {
        LinkedList<Rule> tempQueue = new LinkedList<Rule>();
        if(r.compareTo(r2) != 0) {
            if(r.canResolveWith(r2)) {
                Rule newR = r.resolve(r2,probModel);
                
                if(newR != null && !isSubsumed(newR,r)) {
                    scoreRule(newR);
                    tempQueue.add(newR);
                    Output.out.println(r.toString(probModel) + " + " + r2.toString(probModel) + " = " + newR.toString(probModel));
                }
            }
            if(r2.canResolveWith(r)) {
                Rule newR = r2.resolve(r,probModel);
                
                if(newR != null && !isSubsumed(newR,r)) {
                    scoreRule(newR);
                    tempQueue.add(newR);
                    Output.out.println(r.toString(probModel) + " + " + r2.toString(probModel) + " = " + newR.toString(probModel));
                }
            }
        }
        return tempQueue;
    }
    
    private void queueNewRules(Rule r, boolean ruleConsistent) {
        LinkedList<Rule> tempQueue = new LinkedList<Rule>();
        
        for(Rule r2 : (ruleConsistent ? ruleQueue.inconsistent : ruleQueue.consistent)) {
            tempQueue.addAll(getNewRules(r,r2));
        }
        
        // Check if generated rules subsume anything in queue
        for(Rule rNew : tempQueue) {
            Iterator<Rule> ruleQueueIter = ruleQueue.iterator();
            while(ruleQueueIter.hasNext()) {
                Rule r2 = ruleQueueIter.next();
                if(rNew.subsumes(r2))
                    ruleQueueIter.remove();
            }
        }
        
        ruleQueue.addAll(tempQueue);
    }
    
    private void checkMatrixSubsumptions() {
        Iterator<Rule> queueIter = ruleQueue.iterator();
        while(queueIter.hasNext()) {
            TreeSet<Integer> elems = columnForRule(queueIter.next());
            Iterator<Integer> colIter = mat.cols.keySet().iterator();
            while(colIter.hasNext()) {
                if(mat.colSubset(colIter.next(), elems)) {
                    queueIter.remove();
                    break;
                }
            }
        }
    }
    
    private boolean isSubsumed(Rule newR, Rule r) {
        // Subsumption check
        // 1. Zero length rule is a contradition
        if(newR.length() == 0)
            throw new LogicException("Contradiction reached! " + newR.toString() + " + " + r.toString());
        
        // 2. Unifying premises of some part of premise in conclusion
        if(unifyingPremises != null) {
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
        }
        
        // 3. Subsumed in Matrix
        TreeSet<Integer> elems = columnForRule(newR);
        for(Integer col : mat.cols.keySet()) {
            if(col < 1)
                continue;
            if(mat.colSubset(col, elems))
                return true;
        }
        
        // 4. Subsumed by queued rule
        for(Rule subsumer : ruleQueue) {
            if(subsumer.subsumes(newR))
                return true;
        }
        
        // 5. Subsumed by discarded rule
        for(Rule subsumer : usedRules) {
            if(subsumer.subsumes(newR))
                return true;
        }
        return false;
    }
    
    private boolean canReduce(Rule rule) {
        return rule.score > lastReduction;
    }
    
    /** Reduce a matrix. That is remove all rows where one row is a subset of another
     *  and is of higher cost, and all cols where a col is a superset of some other col.
     */
    public static void reduceMatrix(SparseMatrix m) {
        while(rowReduce(m) || columnReduce(m));
    }
    
    private void reduceMatrix(int reduceTo) {
        lastReduction = reduceTo;
        while(rowReduce(reduceTo) || columnReduce(mat));
        
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
    
    private static boolean rowReduce(SparseMatrix m) {
        boolean rval = false;
        Iterator<Integer> i = m.rows.keySet().iterator();
        while(i.hasNext()) {
            Integer row = i.next();
            for(Integer superRow : m.rows.keySet()) {
                if(m.elemVal(row,0) > m.elemVal(superRow,0) &&
                        m.rowSubset(row,superRow)) {
                    m.removeRow(row,i);
                    rval = true;
                    break;
                }
            }
        }
        return rval;
    }
    
    private boolean rowReduce(int reduceTo) {
        boolean rval = false;
        Iterator<Integer> i = mat.rows.keySet().iterator();
        while(i.hasNext()) {
            Integer row = i.next();
            if(linkOrders.indexOf(row) < reduceTo) {
                for(Integer superRow : mat.rows.keySet()) {
                    if(costForRow(row) > costForRow(superRow) &&
                            mat.rowSubset(row, superRow)) {
                        mat.removeRow(row, i);
                        rval = true;
                        break;
                    }
                }
            }
        }
        if(rval)
            checkMatrixSubsumptions();
        return rval;
    }
    
    private static boolean columnReduce(SparseMatrix mat) {
        boolean rval = false;
        Iterator<Integer> i = mat.cols.keySet().iterator();
        while(i.hasNext()) {
            Integer col = i.next();
            if(col.equals(0))
                continue;
            for(Integer col2 : mat.cols.keySet()) {
                if(col2.equals(0))
                    continue;
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
    
    private TreeSet<Integer> columnForRule(Rule r) {
        TreeSet<Integer> elems = new TreeSet<Integer>();
        
        for(int i = 0; i < r.length(); i++) {
            elems.add(probModel.id(r,i));
        }
        
        return elems;
    }
    
    private void addCosts() {
        for(Integer row : mat.rows.keySet()) {
            mat.setElem(row,0,costForRow(row));
        }
        applyPerturbations();
    }
    
    /** Size of maximum random perturbation, set to zero to disable perturbations */
    public static double PERTURBATION_SIZE = 1.0e-6;
    /** Random Permuations prevents the simplex algorithm from cycling */
    private void applyPerturbations() {
        Vector<Set<Integer>> eqSets = mat.rowEqualitySets(0);
        Random r = new Random();
        for(Set<Integer> eqSet : eqSets) {
            if(eqSet.size() == 1)
                continue;
            for(Integer ii : eqSet) {
                double v = mat.elemVal(ii,0);
                mat.setElem(ii,0,v + r.nextDouble() * v * PERTURBATION_SIZE);
            }
        }
    }
    
    private void reduceCompleteGraph() {
        for(Map.Entry<Integer,TreeSet<Integer>> entry : unifyingPremises.entrySet()) {
            if(entry.getValue().size() == 0)
                continue;
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
                
                for(Integer id : entry.getValue()) {
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
    
    /** Used to profile the completing process.
     * DEBUG */
    public long profileComplete() {
        long rval = System.nanoTime();
        
        prepareCompleteGraphs();
        
        logic.consistCheck(completeModel,new Completer());
        
        return System.nanoTime() - rval;
    }
    
    private class Completer implements InconsistentAction {
        TreeSet<Integer> newLinks;
        public Completer() { newLinks = new TreeSet<Integer>(); }
        public boolean doAction(Logic logic,
                Model m,
                Rule rule) {
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
                    newLinks.add(id);
                }
                
                if(probModel.mutable(id)) {
                    TreeSet<Integer> premiseSet = unifyingPremises.get(id);
                    if(premiseSet != null) {
                        premiseSet.retainAll(premises);
                    } else {
                        premiseSet = new TreeSet<Integer>();
                        premiseSet.addAll(premises);
                        premiseSet.add(id);
                        unifyingPremises.put(id,premiseSet);
                        
                    }
                }
            }
            return true;
        }
    }
    
    private class BaseRuleBuilder implements InconsistentAction {
        boolean limitToCompleteModel = true;
        public BaseRuleBuilder(){}
        public BaseRuleBuilder(boolean limitToCompleteModel) {
            this.limitToCompleteModel = limitToCompleteModel;
        }
        public boolean doAction(Logic logic,
                Model m,
                Rule rule) {
            Rule r = rule.createCopy();
            r.multiplexFunctions(m.elems);
            if(limitToCompleteModel)
                r.limitToModel(completeModel);
            r = Rule.simplify(r,probModel);
            if(r != null && r.length() > 0) {
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
    
    protected class QueueComparator implements Comparator<Rule> {
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
    }
    
    /**
     * Implementation of the double queue essential to this method
     * This queue is basically two queues put together, one of those which are
     * consistent with probModel and one for those inconsistent with probModel.
     * Note that when this queue is accessed through its poll() method it will
     * stop when either queue is empty. However iterating throw iterator() will
     * access every element in the queue */
    protected class RuleQueue extends AbstractQueue<Rule> {
        public PriorityQueue<Rule> consistent;
        public PriorityQueue<Rule> inconsistent;
        private int lastPollConsistent;
        private static final int ERROR = 0;
        private static final int YES = 1;
        private static final int NO = 2;
        
        public RuleQueue() {
            consistent = new PriorityQueue<Rule>(new TreeSet<Rule>(new QueueComparator()));
            inconsistent = new PriorityQueue<Rule>(new TreeSet<Rule>(new QueueComparator()));
            lastPollConsistent = ERROR;
        }
        
        public Iterator<Rule> iterator() {
            Vector<Iterator<Rule>> v = new Vector<Iterator<Rule>>();
            v.add(consistent.iterator());
            v.add(inconsistent.iterator());
            return new MultiIterator<Rule>(v.iterator());
        }
        
        public boolean offer(Rule rule) {
            if(rule.isRuleSatisfied(probModel)) {
                return consistent.offer(rule);
            } else {
                return inconsistent.offer(rule);
            }
        }
        
        public Rule peek() {
            Rule cr = consistent.peek();
            Rule icr = inconsistent.peek();
            if(icr == null)
                return null;
            if(cr == null)
                return icr;
            if(cr.score < icr.score) {
                return cr;
            } else {
                return icr;
            }
        }
        
        public Rule poll() {
            Rule cr = consistent.peek();
            Rule icr = inconsistent.peek();
            if(cr == null) {
                lastPollConsistent = NO;
                return inconsistent.poll();
            }
            if(icr == null) {
                lastPollConsistent = ERROR;
                return null;
            }
            if(cr.score < icr.score) {
                lastPollConsistent = YES;
                return consistent.poll();
            } else {
                lastPollConsistent = NO;
                return inconsistent.poll();
            }
        }
        
        public boolean lastPollConsistent() {
            if(lastPollConsistent == ERROR)
                throw new IllegalStateException();
            else if(lastPollConsistent == YES)
                return true;
            else //if(lastPollInconsistent == NO)
                return false;
        }
        
        public int size() {
            return Math.min(consistent.size(),inconsistent.size());
        }
    }
    
    //////////////////////////////////////////////////////////////////////////////////////////////
    // AlloeProcess stuff
    
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
            return "Completing Model: ";
        else if(state == STATE_REDUCING)
            return "Reducing Complete Model: ";
        else if(state == STATE_BASE)
            return "Creating Base Rules: ";
        else
            return "???: ";
    }
    
    public void run() {
        buildProblemMatrix();
    }
    
    /** Return the complexity (that is number of rules processed) */
    public int getComplexity() {
        if(usedRules != null)
            return usedRules.size();
        else
            return 0;
    }
}

