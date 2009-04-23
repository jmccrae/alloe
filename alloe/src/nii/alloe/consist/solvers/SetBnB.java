/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nii.alloe.consist.solvers;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import nii.alloe.theory.Logic;
import nii.alloe.theory.Model;
import nii.alloe.theory.ProbabilityGraph;
import nii.alloe.theory.SpecificGraph;

/**
 *
 * @author john
 */
public class SetBnB {

    final Model model;
    final String relation;
    final ProbabilityGraph pg;
    public Model soln;

    public SetBnB(Model model, String relation) {
        this.model = model;
        this.relation = relation;
        this.pg = (ProbabilityGraph)model.graphs.get(relation);
    }
    
    public void solve() {
        Collection<Model> components = model.splitByComponents();
        List<List<Integer>> solnSets = new LinkedList<List<Integer>>();
        
        
        for(Model m : components) {
            bestCost = -Double.MAX_VALUE;
            solve(new LinkedList<List<Integer>>(), 0,new LinkedList<Integer>(m.elems));
            solnSets.addAll(bestSoln);
        }
        
        soln = convertSoln(solnSets);
    }
    
    private double bestCost;
    private List<List<Integer>> bestSoln;
    
    private void solve(List<List<Integer>> soln, double cost, List<Integer> E) {
        if(E.isEmpty()) {
            if(cost > bestCost) {
                bestCost = cost;
                bestSoln = cloneSoln(soln);
            }
            return;
        }
        if(cost + bound(soln,E) < bestCost)
            return;
        int i = E.get(E.size()-1);
        E.remove(E.size()-1);
            List<Integer> set2 = new LinkedList<Integer>();
            set2.add(i);
        for(List<Integer> set : new LinkedList<List<Integer>>(soln)) {
            if(joincost(set,set2) + joincost(set2, maxSet(E,i)) >= 0) {
                set.add(i);
                solve(soln, cost + addcost(set, i), E);
                set.remove(set.indexOf(i));
            }
        }
        soln.add(set2);
        solve(soln,cost,E);
        soln.remove(set2);
        E.add(i);
    }
    
    private List<List<Integer>> cloneSoln(List<List<Integer>> soln) {
        List<List<Integer>> rv = new LinkedList<List<Integer>>();
        for(List<Integer> s : soln) {
            rv.add(new LinkedList<Integer>(s));
        }
        return rv;
    }
    
    private double bound(List<List<Integer>> soln, List<Integer> E) {
        double rval = 0.0;
        for(List<Integer> set : soln) {
            for(int i  : set) {
                for(int j : E) {
                    if(pg.posVal(i, j) - pg.negVal(i, j) > 0)
                        rval += pg.posVal(i, j) - pg.negVal(i, j);
                    if(pg.posVal(j,i) - pg.negVal(j,i) > 0)
                        rval += pg.posVal(j,i) - pg.negVal(j,i);
                }
            }
        }
        return rval;
    }
    
    private double joincost(List<Integer> set1, List<Integer> set2) {
        double rval = 0;
        for(int i : set1) {
            for(int j : set2) {
                rval += pg.posVal(i, j) + pg.posVal(j, i) - pg.negVal(i, j) - pg.negVal(j, i);
            }
        }
        return rval;
    }
    
    private double addcost(List<Integer> set, int k) {
        double rval = 0.0;
        for(int i : set) {
            if(i == k)
                continue;
            rval += pg.posVal(i,k) - pg.negVal(i, k) 
                    +pg.posVal(k,i) - pg.negVal(k,i);
        }
        return rval;
    }
    
    private List<Integer> maxSet(List<Integer> E, int k) {
        List<Integer> rval = new LinkedList<Integer>();
        for(int i : E) {
            if(pg.posVal(i, k) - pg.negVal(i, k) +
                    pg.posVal(k,i) - pg.negVal(k,i) > 0) {
                rval.add(i);
            }
        }
        return rval;
    }
    
    private Model convertSoln(List<List<Integer>> soln) {
        Logic synLogic = new Logic("r1(1,2); r1(2,3) -> r1(1,3)\n-> r1(1,1)\nr1(1,2) -> r1(2,1)");
        synLogic.ruleSymbols.setModelSize(model.getModelSize());
        Model rval =  new Model(synLogic);
        SpecificGraph g = rval.addSpecificGraph("r1");
        rval.addBasicGraphs(synLogic);
        rval.addCompulsorys(synLogic);
        for(List<Integer> set : soln) {
            for(int e1 : set) {
                for(int e2 : set) {
                    g.add(e1, e2);
                }
            }
        }
        return rval;
    }
}
