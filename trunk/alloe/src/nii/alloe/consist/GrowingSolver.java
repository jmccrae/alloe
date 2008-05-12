package nii.alloe.consist;
import java.util.*;
import nii.alloe.theory.Logic;
import nii.alloe.theory.Model;
import java.io.*;
import nii.alloe.tools.process.AlloeProcessAdapter;
import nii.alloe.tools.process.AlloeProgressListener;

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
    public Model soln;
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
        iteration = 0;
    }
    
    private ConsistProblem cp;
    private ConsistSolver cs;
    private Model candidate;
    private int iteration;
    
    /**
     * Find closest model to data
     * 
     */
    public void solve() {
        if(cp == null) {
            cp = new ConsistProblem(logic, probModel);
            if(approxSolve) {
                cs = new ApproxConsistSolver();
            } else {
                cs = new ConsistSolver();
            }
            candidate = probModel.createSpecificCopy();
            iteration = 1;
        }
        cp.addProgressListener(this);
        cs.addProgressListener(this);
        while(state == STATE_OK) {
            state = STATE_MATRIX;
            //System.out.println("Building matrix");
            SparseMatrix m = cp.buildGrowingProblemMatrix(candidate);
            if(iteration > 500) {
                System.out.println(probModel.toString());
                System.out.println(candidate.toString());
                m.printMatrix(System.out);
                System.out.println(cp.getBases(candidate));
                System.err.println("Iteartion limit reached");
                System.exit(-1);
            }
            if(m == null) {
                state = STATE_OK;
                break;
            }
            m = m.createCopy();
            ConsistProblem.reduceMatrix(m);
            if(state != STATE_MATRIX)
                break;
            state = STATE_SOLVING;
            cs.solve(m);
            if(state == STATE_SOLVING)
                state = STATE_OK;
            
            candidate = probModel.createSpecificCopy();
            candidate.symmDiffAll(cs.soln);
            iteration++;
        }
        soln = candidate;
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
    
    public SparseMatrix getMatrix() { return cp.mat; }

    public void run() {
       solve(); 
    }

    public void finished() {
        // Yeah I know, but thanks for the warning that you are going to return
    }

    public void progressChange(double newProgress) {
        fireNewProgressChange(newProgress);
    }

    /**
     * Holds value of property approxSolve.
     */
    private boolean approxSolve = false;

    /**
     * Getter for property approxSolve.
     * @return Value of property approxSolve.
     */
    public boolean isApproxSolve() {
        return this.approxSolve;
    }

    /**
     * Setter for property approxSolve.
     * @param approxSolve New value of property approxSolve.
     */
    public void setApproxSolve(boolean approxSolve) {
        this.approxSolve = approxSolve;
    }
}
