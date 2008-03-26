/*
 * GreedySat.java
 *
 * Created on February 15, 2008, 11:40 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package nii.alloe.consist.solvers;
import nii.alloe.theory.*;
import java.util.*;

/**
 *
 * @author john
 */
public class GreedySat {
    public double cost;
    public Model soln;
    
    public static boolean MULTI_PASS = true;
    
    /** Creates a new instance of GreedySat */
    public GreedySat(Logic logic, Model probModel) {
        this.logic = logic;
        this.probModel = probModel;
    }
    
    private LinkedList<Rule> baseRules;
    private Model probModel;
    private Logic logic;
    private Model candidate;
    private GreedySatSet greedySats;
    
    public boolean solve() {
        candidate = probModel.createSpecificCopy();
        cost = 0;
        buildInitialSet(probModel);
        while(!greedySats.isEmpty()) {
            //System.out.println(greedySats.toString());
            int maxID = findCheapest();
            if(maxID == -1) {
                buildInitialSet(candidate);
                if(greedySats.isEmpty())
                    break;
                maxID = findCheapest();
                if(maxID /*still*/ == -1) {
                    System.out.println(probModel.toString());
                    System.out.println(candidate.toString());
                    System.out.println(greedySats.toString());
                    System.out.println("FAIL");
                    return false;                    
                }
            }
            cost += Math.abs(((ProbabilityGraph)probModel.getGraphByID(maxID)).addVal(probModel.iByID(maxID),probModel.jByID(maxID)));
            
            Collection<Integer> newNodes = greedySats.flip(maxID);
            for(Integer newNode : newNodes) {
                greedySats.put(newNode, getGreedySatNode(newNode, !candidate.contains(newNode)));
            }
            if(candidate.isConnected(maxID))
                candidate.remove(maxID);
            else
                candidate.add(maxID);
            if(MULTI_PASS) {
                if(greedySats.isEmpty()) {
                    buildInitialSet(candidate);
                }
            } else {
                addNewBreaks(maxID);
            }
        }
        soln = candidate;
        return true;
    }
    
    private int findCheapest() {
        int maxID = -1;
        double maxCost = 0;
        for(Map.Entry<Integer,GreedySatNode> entry : greedySats.entrySet()) {
            Graph g;
            if((g = probModel.getGraphByID(entry.getKey())) instanceof ProbabilityGraph) {
                double consistGain = (double)(entry.getValue().satisfys.size() - entry.getValue().breaks.size());
                double cost = Math.abs(((ProbabilityGraph)g).addVal(probModel.iByID(entry.getKey()),probModel.jByID(entry.getKey())));
                cost = consistGain / cost;
                if(cost > maxCost) {
                    maxID = entry.getKey();
                    maxCost = cost;
                }
            }
        }
        return maxID;
    }
    
    private void buildInitialSet(Model model) {
        baseRules = new LinkedList<Rule>();
        greedySats = new GreedySatSet();
        logic.consistCheck(model, new BaseRuleBuilder());
        for(Rule r : baseRules) {
            for(int i = 0; i < r.length(); i++) {
                Integer id = model.id(r,i);
                if(i < r.premiseCount && model.isConnected(id) ||
                        i >= r.premiseCount && !model.isConnected(id)) {
                    if(greedySats.get(id) == null) {
                        GreedySatNode node = new GreedySatNode();
                        node.addToSatisfys(r);
                        greedySats.put(id,node);
                    } else {
                        greedySats.get(id).addToSatisfys(r);
                    }
                }
            }
        }
        for(Map.Entry<Integer,GreedySatNode> entry : greedySats.entrySet()) {
            entry.getValue().addToBreaks(getBreaks(entry.getKey()));
        }
        baseRules = null;
    }
    
    private GreedySatNode getGreedySatNode(int id, boolean add) {
        GreedySatNode node = new GreedySatNode();
        for(Rule r : allRules) {
            for(int i = (add ? r.premiseCount : 0); i < (add? r.length() : r.premiseCount); i++) {
                if(probModel.id(r,i) == id) {
                    node.satisfys.add(r);
                }
            }
        }
        node.addToBreaks(getBreaks(id));
        return node;
    }
    
    private TreeSet<Rule> getBreaks(int id) {
        TreeSet<Rule> newRules = new TreeSet<Rule>();
        logic.findAllPotentialResolvers(candidate,id,newRules);
        Iterator<Rule> ruleIter = newRules.iterator();
        while(ruleIter.hasNext()) {
            Rule r = ruleIter.next();
            if(!r.isRuleSatisfied(candidate))
                ruleIter.remove();
            
        }
        return newRules;
    }
    
    private class NewBreaksCheck implements Logic.CheckerCondition {
        int arg;
        public NewBreaksCheck(int arg) { this.arg = arg; }
        public boolean check(int argument, Rule rule, Graph g, int i, int j) {
            if(argument == arg)
                return true;
            for(int k = 0; k < argument; k++) {
                if(k == arg)
                    continue;
                if(k < rule.premiseCount && !candidate.isConnected(candidate.id(rule,k)) ||
                        k >= rule.premiseCount && candidate.isConnected(candidate.id(rule,k)))
                    return (argument < rule.premiseCount && g.isConnected(i,j)) ||
                            (argument >= rule.premiseCount && !g.isConnected(i,j));
            }
            return true;
        }
        public boolean mustConnect(int argument, Rule rule) { return argument < rule.premiseCount; }
    }
    
    private class NewBreaksAction implements InconsistentAction {
        TreeSet<Rule> newBreaks;
        int arg;
        
        public NewBreaksAction(TreeSet<Rule> newBreaks, int arg) {
            this.newBreaks = newBreaks;
            this.arg = arg;
        }
        
        public boolean doAction(Logic logic, Model m, Rule rule) {
            int badPoints = 0;
            for(int i = 0; i < rule.length(); i++) {
                if(i == arg)
                    continue;
                if(i < rule.premiseCount && candidate.isConnected(candidate.id(rule,i)) ||
                        i >= rule.premiseCount && !candidate.isConnected(candidate.id(rule,i)))
                    badPoints++;
            }
            if(badPoints == rule.length() - 2)
                newBreaks.add(rule.createCopy());
            return true;
        }
    }
    
    private void addNewBreaks(int id) {
        TreeSet<Rule> newBreaks = new TreeSet<Rule>();
        boolean add = candidate.contains(id);
        for(Rule r : logic.rules) {
            if(r.length() == 1)
                continue;
            for(int i = (add ? 0 : r.premiseCount); i < (add ? r.premiseCount : r.length()); i++) {
                if(r.relations.get(i).equals(candidate.relationByID(id)) &&
                        r.tryAssign(i,candidate.iByID(id),candidate.jByID(id))) {
                    logic.consistCheck(candidate,r,0,new NewBreaksAction(newBreaks,i),new NewBreaksCheck(i));
                    r.terms.get(i)[0].unsetAssignment();
                    if(r.terms.get(i)[1].hasAssignment())
                        r.terms.get(i)[1].unsetAssignment();
                }
            }
        }
        
        for(Rule r : newBreaks) {
            int point = -1;
            for(int i = 0; i < r.length(); i++) {
                int id2 = candidate.id(r,i);
                if(id2 == id)
                    continue;
                if(i < r.premiseCount && !candidate.isConnected(id2) ||
                        i >= r.premiseCount && candidate.isConnected(id2)) {
                    point = i;
                    break;
                }
            }
            assert(point != -1);
            GreedySatNode node = greedySats.get(point);
            if(node == null) {
                greedySats.put(point, getGreedySatNode(point, !candidate.contains(point)));
            } else {
                node.addToBreaks(r);
            }
        }
    }
    
    // For Memory management
    private static TreeSet<Rule> allRules;
    
    static {
        allRules = new TreeSet<Rule>();
    }
    
    private class BaseRuleBuilder implements InconsistentAction {
        public boolean doAction(Logic logic,
                Model m,
                Rule rule) {
            Rule r = rule.createCopy();
            r.multiplexFunctions(m.elems);
            r = Rule.simplify(r,probModel);
            if(r != null && r.length() > 0) {
                baseRules.add(r);
            }
            return true;
        }
    }
    
    private class GreedySatNode {
        TreeSet<Rule> satisfys;
        TreeSet<Rule> breaks;
        
        public GreedySatNode() {
            satisfys = new TreeSet<Rule>();
            breaks = new TreeSet<Rule>();
        }
        
        public void flip() {
            TreeSet<Rule> temp;
            temp = satisfys;
            satisfys = breaks;
            breaks = temp;
        }
        
        private void add(TreeSet<Rule> set, Rule r) {
            NavigableSet<Rule> set2 = allRules.subSet(r,true,r,true);
            if(set2.isEmpty()) {
                set.add(r);
                allRules.add(r);
            } else {
                set.add(set2.first());
            }
        }
        
        public void addToBreaks(Rule r) {
            add(breaks,r);
        }
        
        public void addToBreaks(Collection<Rule> rules) {
            for(Rule r : rules) {
                add(breaks,r);
            }
        }
        
        public void addToSatisfys(Rule r) {
            add(satisfys,r);
        }
        
        public void addToSatisfys(Collection<Rule> rules) {
            for(Rule r : rules) {
                add(satisfys,r);
            }
        }
        
        public String toString() {
            return "Satisfys: " + satisfys.toString() + " Breaks: " + breaks.toString();
        }
    }
    
    private class GreedySatSet extends TreeMap<Integer,GreedySatNode> {
        public GreedySatSet() {}
        
        /** Flip the value of one link
         * @return the new values that need to be added into GreedySatSolve
         */
        public Collection<Integer> flip(Integer id) {
            GreedySatNode node = get(id);
            if(node == null)
                throw new IllegalArgumentException();
            Iterator<GreedySatNode> nodeIter = values().iterator();
            while(nodeIter.hasNext()) {
                GreedySatNode node2 = nodeIter.next();
                if(node2 == node)
                    continue;
                for(Rule r : node.satisfys) {
                    node2.satisfys.remove(r);
                }
                for(Rule r : node.breaks) {
                    if(node2.breaks.remove(r))
                        node2.satisfys.add(r);
                }
                Iterator<Rule> ruleIter = node2.breaks.iterator();
                while(ruleIter.hasNext()) {
                    Rule r = ruleIter.next();
                    for(int i = 0; i < r.length(); i++) {
                        if(probModel.id(r,i) == id) {
                            ruleIter.remove();
                            break;
                        }
                    }
                }
                if(node2.satisfys.isEmpty()) {
                    nodeIter.remove();
                }
            }
            TreeSet<Integer> rval = new TreeSet<Integer>();
            for(Rule r : node.satisfys) {
                allRules.remove(r);
            }
            for(Rule r : node.breaks) {
                for(int i = 0; i < r.length(); i++) {
                    Integer id2 = probModel.id(r,i);
                    if(id.equals(id2))
                        continue;
                    if(get(id2) == null)
                        rval.add(id2);
                    else
                        get(id2).satisfys.add(r);
                }
            }
            node.flip();
            if(node.satisfys.isEmpty())
                remove(id);
            return rval;
        }
    }
}
