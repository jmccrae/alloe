/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nii.alloe.consist.solvers;

import java.util.LinkedList;
import java.util.List;
import nii.alloe.theory.Model;
import nii.alloe.theory.ProbabilityGraph;
import nii.alloe.theory.SpecificGraph;

/**
 *
 * @author john
 */
public class GreedySets {

    final Model probModel;
    final ProbabilityGraph pg;
    final String relation;
    public Model soln;
    public double cost;

    public GreedySets(Model probModel, String relation) {
        this.probModel = probModel;
        this.pg = (ProbabilityGraph)probModel.graphs.get(relation);
        this.relation = relation;
    }
    
    public void solve() {
        List<Integer> E =  new LinkedList<Integer>(probModel.elems);
        List<List<Integer>> C =  new LinkedList<List<Integer>>();
        
        while(!E.isEmpty()) {
            int i = E.get(0);
            E.remove(0);
            List<Integer> candidate = null;
            double value = 0;
            for(List<Integer> C_i : C) {
                double v = 0.0;
                for(int j : C_i) {
                    v += pg.posVal(i, j) - pg.negVal(i, j) + pg.posVal(j, i) - pg.negVal(j, i);
                }
                if(v > value) {
                    value = v;
                    candidate = C_i;
                }
            }
            if(candidate == null) {
                List<Integer> set = new LinkedList<Integer>();
                set.add(i);
                C.add(set);
            } else {
                candidate.add(i);
            }
        }
        soln = new Model(probModel);
        SpecificGraph g = soln.addSpecificGraph(relation);
        soln.addBasicGraphs(probModel.logic);
        soln.addCompulsorys(probModel.logic);
        for(List<Integer> set : C) {
            for(int i : set) {
                for(int j : set) {
                    g.add(i, j);
                }
            }
        }
        cost = 0.0;
    }
}
