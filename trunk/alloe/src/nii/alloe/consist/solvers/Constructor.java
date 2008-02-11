package nii.alloe.consist.solvers;
import nii.alloe.theory.*;
import java.util.*;


/**
 * Builds a correct model iteratively by adding one more element at every iteration.
 * Search can either be done greedily or by A* (for optimal solution)
 *
 * @author John McCrae, National Institute of Informatics
 */
public class Constructor {
    
    public Model solvedModel;
    public double cost;
    private int nodeStrategy;
    private int method;
    public static final int ADD_NODES_MOST_LINKS = 0;
    public static final int ADD_NODES_LEAST_LINKS = 1;
    public static final int ADD_NODES_MOST_CENTRAL = 2;
    public static final int ADD_NODES_LEAST_CENTRAL = 3;
    public static final int METHOD_GREEDY = 0;
    public static final int METHOD_ASTAR = 1;
    private PriorityQueue<Solution> solutions;
    private NextNodeFinder nextNode;
    private Model probModel;
    private Logic logic;
    
    /** Creates a new instance of Constructor */
    public Constructor(int nodeStrategy, int method) {
        this.nodeStrategy = nodeStrategy;
        this.method = method;
    }
    
    public void solve(Model probModel, Logic logic) {
        this.probModel = probModel;
        this.logic = logic;
        Model emptyModel = probModel.createBlankSpecificCopy();
        nextNode = getNextNodeFinder();
        nextNode.init(probModel);
        solutions = new PriorityQueue<Solution>(new TreeSet<Solution>());
        double baseCost = getBaseCost();
        solutions.add(new Solution(baseCost,-baseCost,emptyModel,-1));
        logic.consistCheck(emptyModel, new ConclusionBranchingInconsistentAction(new TreeSet<Solution>(),solutions.peek()));
        while(solutions.peek() != null) {
            System.out.println(solutions);
            Solution soln = solutions.poll();
            if(soln.prevNode >= 0 && !nextNode.hasNext(soln.prevNode)) {
                solvedModel = soln.model;
                cost = soln.cost;
                return;
            }
            if(method == METHOD_GREEDY) {
                solutions.add(getNewSolutions(soln).first());
            } else {
                solutions.addAll(getNewSolutions(soln));
            }
        }
        throw new IllegalStateException("Shouldn't be here");
    }
    
    private TreeSet<Solution> getNewSolutions(Solution soln) {
        int next = (soln.prevNode == -1 ? nextNode.firstNode() : nextNode.nextNode(soln.prevNode));
        soln.prevNode = next;
        Iterator<Integer> newLinks = getNewLinks(soln,next);
        if(!newLinks.hasNext()) {
            TreeSet<Solution> temp =  new TreeSet<Solution>();
            temp.add(soln);
            return temp;
        }
        TreeSet<Solution> rval = new TreeSet<Solution>();
        while(newLinks.hasNext()) {
            Integer newLink = newLinks.next();
            TreeSet<Solution> cbs;
            if(rval.isEmpty()) {
                if(soln.disallowed.contains(newLink))
                    continue;
                cbs = conclusionBranchingSolutions(soln, newLink);
            } else {
                cbs = new TreeSet<Solution>();
                Iterator<Solution> solnIter = rval.iterator();
                while(solnIter.hasNext()) {
                    Solution soln2 = solnIter.next();
                    if(soln2.model.contains(newLink))
                        continue;
                    cbs.addAll(conclusionBranchingSolutions(soln2,newLink));
                }
            }
            rval.addAll(cbs);
        }
        return rval;
    }
    
    private Iterator<Integer> getNewLinks(Solution soln, int next) {
        Iterator<Integer> linkIter = probModel.iterator();
        TreeSet<Integer> rval = new TreeSet<Integer>();
        while(linkIter.hasNext()) {
            Integer i = linkIter.next();
            if((soln.model.iByID(i) == next || soln.model.jByID(i) == next) && soln.model.mutable(i) && !soln.model.isConnected(i)) {
                rval.add(i);
            }
        }
        return rval.iterator();
    }
    
    private TreeSet<Solution> conclusionBranchingSolutions(Solution soln, int newLink) {
        Solution newSoln = new Solution(soln);
        Solution noBranch = new Solution(soln);
        TreeSet<Solution> solutions = new TreeSet<Solution>();
        
        noBranch.disallow(newLink);
        solutions.add(noBranch);
        if(!newSoln.disallowed.contains(newLink)) {
            newSoln.add(newLink);
            solutions.add(newSoln);
            logic.consistCheck(newSoln.model, new ConclusionBranchingInconsistentAction(solutions,newSoln));
        }
        
        return solutions;
    }
    
    private double getBaseCost() {
        Iterator<Integer> linkIter = probModel.iterator();
        double baseCost = 0.0;
        while(linkIter.hasNext()) {
            Integer link = linkIter.next();
            Graph g = probModel.getGraphByID(link);
            if(!(g instanceof ProbabilityGraph)) {
                continue;
            }
            ProbabilityGraph pg = (ProbabilityGraph)g;
            baseCost += pg.removeVal(probModel.iByID(link),probModel.jByID(link));
        }
        return baseCost;
    }
    
    private class ConclusionBranchingInconsistentAction implements InconsistentAction {
        boolean fail;
        TreeSet<Solution> solns;
        Solution thisBranch;
        ConclusionBranchingInconsistentAction(TreeSet<Solution> solns, Solution thisBranch) {
            this.solns = solns;
            this.thisBranch = thisBranch;
            fail = false;
        }
        public boolean doAction(Logic logic, Model m, Rule rule) {
            if(fail) {
                return true;
            } else if(rule.isRuleSatisfied(m)) {
                return true;
            } else if(rule.premiseCount == rule.length()) {
                fail = true;
                solns.remove(thisBranch);
                return true;
            } else if(rule.premiseCount == rule.length() - 1) {
                if(thisBranch.disallowed.contains(m.id(rule,rule.premiseCount))) {
                    fail = true;
                    solns.remove(thisBranch);
                    return true;
                } else {
                    thisBranch.add(m.id(rule,rule.premiseCount));
                    return false;
                }
            } else {
                int i = rule.premiseCount;
                while(thisBranch.disallowed.contains(m.id(rule,i))) {
                    i++;
                    if(i == rule.length()) {
                        fail = true;
                        solns.remove(thisBranch);
                        return true;
                    }
                }
                thisBranch.add(m.id(rule,i));
                for(i = i+1; i < rule.length(); i++) {
                    if(thisBranch.disallowed.contains(m.id(rule,i)))
                        continue;
                    Solution newBranch = new Solution(thisBranch);
                    newBranch.remove(m.id(rule,i-1));
                    newBranch.add(m.id(rule,i));
                    solns.add(newBranch);
                    logic.consistCheck(newBranch.model, new ConclusionBranchingInconsistentAction(solns,newBranch));
                }
                return false;
            }
        }
    }
    
    private NextNodeFinder getNextNodeFinder() {
        if(nodeStrategy <= ADD_NODES_LEAST_LINKS) {
            return new MostLinkedNodeFinder();
        } else {
            return new CentralNodeFinder();
        }
    }
    
    private interface NextNodeFinder {
        public int firstNode();
        public int nextNode(int prevNode);
        public boolean hasNext(int prevNode);
        public void init(Model specModel);
    }
    
    private class MostLinkedNodeFinder implements NextNodeFinder {
        private TreeSet<Integer> nodeOrder;
        private int []linkCount;
        public void init(Model specModel) {
            linkCount = new int[specModel.n];
            Iterator<String> graphIter = specModel.graphNameIterator();
            while(graphIter.hasNext()) {
                Graph g = specModel.getGraphByName(graphIter.next());
                Iterator<Integer> linkIter = g.iterator(specModel.n);
                while(linkIter.hasNext()) {
                    int link = linkIter.next();
                    linkCount[link % specModel.n]++;
                    linkCount[link / specModel.n]++;
                }
            }
            if(nodeStrategy == ADD_NODES_LEAST_LINKS) {
                nodeOrder = new TreeSet<Integer>(new Comparator<Integer>() {
                    public int compare(Integer i1, Integer i2) {
                        return (linkCount[i1] < linkCount[i2] ? - 1 : (linkCount[i1] == linkCount[i2] ? (i1<i2 ? -1 : (i1 == i2 ? 0 : +1)) : +1));
                    }
                });
            } else if(nodeStrategy == ADD_NODES_MOST_LINKS) {
                nodeOrder = new TreeSet<Integer>(new Comparator<Integer>() {
                    public int compare(Integer i1, Integer i2) {
                        return (linkCount[i1] < linkCount[i2] ? +1 : (linkCount[i1] == linkCount[i2] ? (i1<i2 ? -1 : (i1 == i2 ? 0 : +1)) : -1));
                    }
                });
            }
            for(int i = 0; i < specModel.n; i++) {
                nodeOrder.add(i);
            }
        }
        public int firstNode() { return nodeOrder.first(); }
        public int nextNode(int prevNode) { return nodeOrder.tailSet(prevNode,false).first(); }
        public boolean hasNext(int prevNode) { return !nodeOrder.tailSet(prevNode,false).isEmpty(); }
    }
    
    private class CentralNodeFinder implements NextNodeFinder {
        double[] centrality;
        TreeSet<Integer> nodeOrder;
        public void init(Model model) {
            centrality = new double[model.n];
            double []newCentrality = new double[model.n];
            for(int i = 0; i < model.n; i++) {
                newCentrality[i] = 1 / (double)model.n;
            }
            do {
                System.arraycopy(newCentrality,0,centrality,0,model.n);
                newCentrality = new double[model.n];
                Iterator<Integer> linkIter = model.iterator();
                double sum = 0.0;
                while(linkIter.hasNext()) {
                    Integer link = linkIter.next();
                    newCentrality[model.iByID(link)] += centrality[model.jByID(link)];
                    sum += centrality[model.jByID(link)];
                }
                if(sum == 0.0)
                    return;
                for(int i = 0; i < model.n; i++) {
                    newCentrality[i] = newCentrality[i] / sum;
                }
                
            } while(diffGreater(centrality,newCentrality,0.0001));
            
            System.arraycopy(newCentrality,0,centrality,0,model.n);
            if(nodeStrategy == ADD_NODES_LEAST_CENTRAL) {
                nodeOrder = new TreeSet<Integer>(new Comparator<Integer>() {
                    public int compare(Integer i1, Integer i2) {
                        return (centrality[i1] < centrality[i2] ? - 1 : (centrality[i1] == centrality[i2] ? (i1<i2 ? -1 : (i1 == i2 ? 0 : +1)) : +1));
                    }
                });
            } else if(nodeStrategy == ADD_NODES_MOST_CENTRAL) {
                nodeOrder = new TreeSet<Integer>(new Comparator<Integer>() {
                    public int compare(Integer i1, Integer i2) {
                        return (centrality[i1] < centrality[i2] ? +1 : (centrality[i1] == centrality[i2] ? (i1<i2 ? -1 : (i1 == i2 ? 0 : +1)) : -1));
                    }
                });
            }
            for(int i = 0; i < model.n; i++) {
                nodeOrder.add(i);
            }
        }
        
        public boolean diffGreater(double[] v1, double[] v2, double eps) {
            double r = 0.0;
            for(int i = 0; i < v1.length; i++) {
                r += Math.abs(v1[i] - v2[i]);
                if(r > eps)
                    return true;
            }
            return false;
        }
        public int firstNode() { return nodeOrder.first(); }
        public int nextNode(int prevNode) { return nodeOrder.tailSet(prevNode,false).first(); }
        public boolean hasNext(int prevNode) { return !nodeOrder.tailSet(prevNode,false).isEmpty(); }
    }
    
    private class Solution implements Comparable<Solution> {
        public double cost;
        public double heuristicCost;
        public Model model;
        public int prevNode;
        public TreeSet<Integer> disallowed;
        
        public Solution(double cost, double heuristicCost, Model model, int prevNode) {
            this.cost = cost;
            this.heuristicCost = heuristicCost;
            this.model = model;
            this.prevNode = prevNode;
            disallowed = new TreeSet<Integer>();
        }
        
        public Solution(Solution s) {
            this.cost = s.cost;
            this.heuristicCost = s.heuristicCost;
            this.model = s.model.createCopy();
            this.prevNode = s.prevNode;
            disallowed = new TreeSet<Integer>(s.disallowed);
        }
        
        public int compareTo(Solution sol) {
            if(cost + heuristicCost < sol.cost +  sol.heuristicCost) {
                return -1;
            } else if(cost + heuristicCost > sol.cost + sol.heuristicCost) {
                return +1;
            } else {
                return sol.equals(this) ? 0 : (hashCode() < sol.hashCode() ? -1 : 1);
            }
        }
        
        public void add(int newLink) {
            if(model.contains(newLink))
                throw new RuntimeException();
            model.add(newLink);
            double c = ((ProbabilityGraph)probModel.getGraphByID(newLink)).removeVal(
                    model.iByID(newLink),model.jByID(newLink));
            cost -= c;
            if(c > 0) {
                heuristicCost += c;
            }
        }
        
        public void remove(int link) {
            model.remove(link);
            double c = ((ProbabilityGraph)probModel.getGraphByID(link)).addVal(
                    model.iByID(link),model.jByID(link));
            cost -= c;
            if(c < 0) {
                heuristicCost -= c;
            }
        }
        
        public String toString() {
            return model.toString() + "~" + disallowed.toString() + ": " + cost + " + " + heuristicCost + " @ " + prevNode;
        }
        
        public void disallow(int link) {
            heuristicCost += ((ProbabilityGraph)probModel.getGraphByID(link)).removeVal(
                    model.iByID(link),model.jByID(link));
            disallowed.add(link);
        }
    }
    
}
