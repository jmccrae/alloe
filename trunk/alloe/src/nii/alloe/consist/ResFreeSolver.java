package nii.alloe.consist;
import java.util.*;
import nii.alloe.theory.*;
import nii.alloe.tools.process.*;

/** ResFree Solver, uses the linear symmetric difference method to from
 *  matrices. This works essentially as such, an inconsistent rule is added
 * by A_{ij} = 1 if term j occurs in rule i and j is positive, but not in the
 * model or j is negative and in the model, similarly A_{ij} = -1 if term
 * j is positive and in the model or j is negative and not in the  model. The
 * matrix is then solved by A_{ij} &gt;= b, where b = -1 + <i>{number of
 * negatives in the column</i>. The operation of the solution then essentially
 * follows that of <code>GrowingSolver</code>
 * @see GrowingSolver
 */
public class ResFreeSolver extends AlloeProcessAdapter {
    Logic logic;
    Model probModel;
    public Model soln;
    public double cost;

    /** Create an instance
     * @param logic The logic for consistency
     * @param probModel The probability model
     */
    public ResFreeSolver(Logic logic, Model probModel) {
	this.logic = logic;
	this.probModel = probModel;
    }

    private static final int STATE_MATRIX = 3;
    private static final int STATE_SOLVING = 4;

    Model candidate;
    int iteration;
    SparseMatrix matrix;
    Vector<Double> solnRow;
    double lastCost;

    /** Find the closest model to the data */
    public void solve() {
	if(candidate == null) {
	    candidate = probModel.createSpecificCopy();
	    iteration = 0;
	    matrix = new SparseMatrix();
	    solnRow = new Vector<Double>();
	    lastCost = 0.0;
	}
	
	while(state == STATE_OK) {
	    state = STATE_MATRIX;

	    if(buildMatrix()) { // Success!
		soln = candidate;
		cost = lastCost;
		fireFinished();
		return;
	    }		

	    if(state != STATE_MATRIX)
		break;
	    state = STATE_SOLVING;

	    lastCost = solveMatrix();

	    if(state != STATE_SOLVING)
	       break;
	    state = STATE_OK;
	    iteration++;
	}
    }
    

    /** Builds the matrix
     * @return True if there were no inconsistencies
     */
    boolean buildMatrix() {
	LinkedList<Rule> inconsistencies = new LinkedList<Rule>();
	logic.consistCheck(candidate, new InconsistencyFinder(inconsistencies));

	if(inconsistencies.isEmpty())
	    return true;
	for(Rule r : inconsistencies) {
	    int newColumn = matrix.cols.isEmpty() ? 1 : 
		matrix.cols.lastKey() + 1;
	    double solnVal = -1;
	    for(int i = 0; i < r.length(); i++) {
		int id = probModel.id(r,i);
		if(i <  r.premiseCount && probModel.contains(id) ||
		   i >= r.premiseCount && !probModel.contains(id)) {
		    matrix.setElem(id,newColumn,1);
		} else {
		    matrix.setElem(id,newColumn,-1);
		    solnVal++;
		}
	    }
	    solnRow.add(solnVal);
	}
	return false;
    }

    class InconsistencyFinder implements InconsistentAction {
	LinkedList<Rule> inc;
	InconsistencyFinder(LinkedList<Rule> inc) { this.inc = inc; }
	public boolean doAction(Logic logic, Model m, Rule r) {
	    Rule r2  = Rule.simplify(r,m);
	    if(r2 != null)
		inc.add(r2);
	    return true;
	}
    }

    ConsistSolver cs;

    /** Solve the matrix
     * @return The cost of the solution
     */
    double solveMatrix() {
        cs = new ConsistSolver();
	cs.solve(matrix,solnRow);
	candidate = probModel.createSpecificCopy();
	candidate.symmDiffAll(cs.soln);
	return cs.cost;
    }
    
    public String getStateMessage() {
        if(state == STATE_OK) {
            return "Solving: ";
        } else if(state == STATE_MATRIX) {
            String s = "Building Matrix";
            return s + " (Iteration " + iteration + "): ";
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
}
	