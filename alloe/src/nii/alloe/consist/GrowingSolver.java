package nii.alloe.consist;
import java.util.*;
import nii.alloe.theory.Logic;
import nii.alloe.theory.Model;
import nii.alloe.niceties.*;
import java.io.*;

/**
 * Solves a problem using the growing matrix method. This is similar to {@link ConsistSolver}, however instead
 * of including all potential links and building a problem matrix based on this matrix, a matrix is
 * formed where new relations can only be added if they can be gained directly from the base model.
 * Then this base model is expanded to include any new relations added by the solution and the method
 * is repeated until the base model converges.
 *
 * @author John McCrae, National Institute of Informatics
 */
public class GrowingSolver extends AlloeProcessAdapter implements AlloeProgressListener {
    /**
     * The solution is placed here after solve is called
     */
    public TreeSet<Integer> soln;
    /**
     * The minimal cost is placed here after solve is called
     */
    public double cost;
    
    private Logic logic;
    private Model probModel;
    
    /** Create a new instance */
    public GrowingSolver(Logic logic, Model probModel) {
        this.logic = logic;
        this.probModel = probModel;
    }
    
    private ConsistProblem cp;
    private ConsistSolver cs;
    private Model baseModel;
    private int iteration;
    private Collection<Integer> change;
    
    /**
     * Find closest model to data
     * 
     * @param logic The logic the model should be made constitent with
     * @param probModel A weighted model of the inconsistent data
     */
    public void solve() {
        if(cp == null) {
            cp = new ConsistProblem(logic, probModel);
            cs = new ConsistSolver();
            baseModel = probModel.createSpecificCopy();
            iteration = 1;
            change = baseModel;
        }
        cp.addProgressListener(this);
        cs.addProgressListener(this);
        while(state == STATE_OK) {
            state = STATE_MATRIX;
            SparseMatrix m = cp.buildGrowingProblemMatrix(baseModel,change);
            if(state != STATE_MATRIX)
                break;
            state = STATE_SOLVING;
            cs.solve(m);
            if(state == STATE_SOLVING)
                state = STATE_OK;
            
            change = (TreeSet<Integer>)cs.soln.clone();
            change.removeAll(baseModel);
            if(!baseModel.addAll(cs.soln)) 
                break;
            iteration++;
        }
        soln = cs.soln;
        cost = cs.cost;
        if(state == STATE_OK)
            fireFinished();
    }   

      private static final int STATE_MATRIX = 3;
      private static final int STATE_SOLVING = 4;
    
    
    /** Get a string representation of the current action being performed */
    public String getStateMessage() {
        if(state == STATE_OK) {
            return "Solving: ";
        } else if(state == STATE_MATRIX) {
            String s = cp.getStateMessage();
            return s.substring(0,s.length()-2) + " (Iteration " + iteration + "): ";
        } else if(state == STATE_SOLVING) {
            String s = cs.getStateMessage();
            return s.substring(0,s.length()-2) + " (Iteration " + iteration + "): ";
        } else {
            return "???";
        }
    }

    public void run() {
       solve(); 
    }

    public void finished() {
        // Yeah I know, but thanks for the warning that you are going to return
    }

    public void progressChange(double newProgress) {
        fireNewProgressChange(newProgress);
    }
}
