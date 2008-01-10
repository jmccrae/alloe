package nii.alloe.consist;
import java.util.*;
import nii.alloe.theory.Logic;
import nii.alloe.theory.Model;

/**
 * Solves a problem using the growing matrix method. This is similar to {@link ConsistSolver}, however instead
 * of including all potential links and building a problem matrix based on this matrix, a matrix is
 * formed where new relations can only be added if they can be gained directly from the base model.
 * Then this base model is expanded to include any new relations added by the solution and the method
 * is repeated until the base model converges.
 *
 * @author John McCrae, National Institute of Informatics
 */
public class GrowingSolver {
    /**
     * The solution is placed here after solve is called
     */
    TreeSet<Integer> soln;
    /**
     * The minimal cost is placed here after solve is called
     */
    double cost;
    
    /** Create a new instance */
    public GrowingSolver() {}
    
    /**
     * Find closest model to data
     * 
     * @param logic The logic the model should be made constitent with
     * @param probModel A weighted model of the inconsistent data
     */
    public void solve(Logic logic, Model probModel) {
        ConsistProblem cp = new ConsistProblem(logic, probModel);
        ConsistSolver cs = new ConsistSolver();
        Model baseModel = probModel.createSpecificCopy();
        while(true) {
            SparseMatrix m = cp.buildGrowingProblemMatrix(baseModel);
            cs.solve(m);
            
            if(!baseModel.addAll(cs.soln)) 
                break;
        }
        soln = cs.soln;
        cost = cs.cost;
    }   
}
