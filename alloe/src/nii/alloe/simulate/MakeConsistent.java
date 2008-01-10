package nii.alloe.simulate;
import nii.alloe.theory.Graph;
import nii.alloe.theory.InconsistentAction;
import nii.alloe.theory.Logic;
import nii.alloe.theory.Model;
import nii.alloe.theory.Rule;
import java.util.*;
/**
 * Create a correct model from a random model. This is used for generating 
 * simulate data, the idea is to generate random models and then call
 * {@link Logic#consistCheck(Model,InconsistentAction)} with an instance of this
 * class as the InconsistAction. 
 *
 * @author John McCrae, National Institute of Informatics
 */
public class MakeConsistent implements InconsistentAction {
    
    /** Creates a new instance of MakeConsistent */
    public MakeConsistent() {
    }
    
    /** When it is called this method will choose with equal probability to
     * either remove a (mutable) premise of rule from m or add a (mutable) conclusion of rule to m
     */
    public boolean doAction(Logic logic,
            Model m,
            Rule rule) {
        if(rule.isRuleSatisfied(m))
            return true;
        
        System.out.println(rule.toString());
        
        Random r = new Random();
        
        LinkedList<Integer> conclusions = getMutableConclusions(m,rule);
        LinkedList<Integer> premises = getMutablePremises(m,rule);
        
        if(conclusions.size() == 0 &&
                premises.size() == 0) {
            System.err.println("Couldn't solve unsatisfied rule: " + rule.toString());
            return false;
        }
        
        int i = r.nextInt(conclusions.size() + premises.size());
        
        if(i < premises.size()) { // Remove Premise
            int targetRule = premises.get(i);
            Graph g = m.graphs.get(rule.relations.get(targetRule));
            g.remove(rule.terms.get(targetRule)[0].getAssignment(),
                    rule.terms.get(targetRule)[1].getAssignment());
        } else { // Accept Concequence
            int targetRule = conclusions.get(i - premises.size());
            
            Graph g = m.graphs.get(rule.relations.get(targetRule));
            if(rule.terms.get(targetRule)[0].getAssignment() >= 0 &&
                    rule.terms.get(targetRule)[1].getAssignment() >= 0) {
                g.add(rule.terms.get(targetRule)[0].getAssignment(),
                        rule.terms.get(targetRule)[1].getAssignment());
            } else if(rule.terms.get(targetRule)[1].getAssignment() >= 0) {
                // Note all code from here on is to deal with functional arguments (grrr!!)
                // And it's not even fucking finished :(
                LinkedList<Integer> terms = rule.statementsForArgument(rule.terms.get(targetRule)[0]);
                LinkedList<Integer> possAssignments = new LinkedList<Integer>();
                
                Iterator<Integer> i4 = m.elems.iterator();
                while(i4.hasNext()) {
                    int i3 = i4.next();
                    possAssignments.add(new Integer(i3));
                }
                
                Iterator<Integer> titer = terms.iterator();
                while(titer.hasNext()) {
                    int t = titer.next().intValue();
                    Graph g2 = m.graphs.get(rule.relations.get(t));
                    Iterator<Integer> jiter = possAssignments.iterator();
                    while(jiter.hasNext()) {
                        int j = jiter.next().intValue();
                        if(!g2.mutable(j,rule.terms.get(targetRule)[1].getAssignment()) && 
                                !g2.isConnected(j,rule.terms.get(targetRule)[1].getAssignment())) {
                            jiter.remove();
                        }
                    }
                }
                
                if(possAssignments.isEmpty()) {
                    System.err.println("Failed to assign to functional variable");
                    return false;
                } else {
                    int a = possAssignments.get(r.nextInt(possAssignments.size())).intValue();
                    titer = terms.iterator();
                    while(titer.hasNext()) {
                        int t = titer.next().intValue();
                        Graph g2 = m.graphs.get(rule.relations.get(t));
                        if(g2.mutable(a,rule.terms.get(targetRule)[1].getAssignment())) {
                            g2.add(a,rule.terms.get(targetRule)[1].getAssignment());
                        }
                    }
                }
                
            } else {
                System.err.println("I must fix this!!!!");
            }
        }
        return false;
    }
    
    private LinkedList<Integer> getMutablePremises(Model m,
            Rule rule) {
        LinkedList<Integer> rval = new LinkedList<Integer>();
        for(int i = 0; i < rule.premiseCount; i++) {
            Graph g = m.graphs.get(rule.relations.get(i));
            if(g.mutable(rule.terms.get(i)[0].getAssignment(),
                    rule.terms.get(i)[1].getAssignment())) {
                rval.add(new Integer(i));
            }
        }
        return rval;
    }
    
    private LinkedList<Integer> getMutableConclusions(Model m,
            Rule rule) {
        LinkedList<Integer> rval = new LinkedList<Integer>();
        for(int i = rule.premiseCount; i < rule.length(); i++) {
            Graph g = m.graphs.get(rule.relations.get(i));
            if(g.mutable(rule.terms.get(i)[0].getAssignment(),
                    rule.terms.get(i)[1].getAssignment())) {
                rval.add(new Integer(i));
            }
        }
        return rval;
    }
    
}
