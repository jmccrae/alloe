package nii.alloe.consist;
import java.util.*;
import nii.alloe.theory.Logic;
import nii.alloe.theory.Model;

/**
 * Implements the branch and bound section of the solution. This is carried out by first attempting a 
 * relaxed (linear)
 * solution using {@link Simplex#simplexSolve(SparseMatrix)}, if any non integer solutions are found
 * the solver branches on this row (first non-integer row is always chosen). 
 *
 * @author John McCrae, National Institute of Informatics
 */
public class ConsistSolver {
    /**
     * The solution will be in this variable after calling solve()
     */
    TreeSet<Integer> soln;
    /**
     * The cost of the minimal solution will be in this variable after calling solve()
     */
    double cost;
    
    private static Simplex simplex;
    
    static {
        simplex = new Simplex();
    }
    
    /** Creates a new instance of ConsistSolver */
    public ConsistSolver() {
        soln = new TreeSet<Integer>();
    }
    
    private TreeSet<Integer> partSoln;
    private double partCost;
    
    /**
     * Find closest model to data
     * 
     * @param logic The logic the model should be made constitent with
     * @param probModel A weighted model of the inconsistent data
     */
    public void solve(Logic logic, Model probModel) {
        ConsistProblem cp = new ConsistProblem(logic, probModel);
        SparseMatrix m = cp.buildProblemMatrix();
        solve(m);
    }
    
    /**
     * Solves a problem matrix
     */
    public void solve(SparseMatrix m) {
        partSoln = new TreeSet<Integer>();
        cost = Double.MAX_VALUE;
        partCost = 0;
        solve2(m);
    }
    
    private void solve2(SparseMatrix m) {
        m.printMatrix(System.out);
        simplex.simplexSolve(m.createCopy());
        if(simplex.cost + partCost > cost)
            return;
        
        Iterator<Map.Entry<Integer,Double>> iter = simplex.soln.entrySet().iterator();
        boolean solnFound = true;
        while(iter.hasNext()) {
            Map.Entry<Integer,Double> entry = iter.next();
            if(!entry.getValue().equals(1.0)) {
                solnFound = false;
                
                partCost += m.elemVal(entry.getKey(),0);
                m.selectRow(entry.getKey());
                partSoln.add(entry.getKey());
                solve2(m);
                partSoln.remove(entry.getKey());
                m.restitch();
                partCost -= m.elemVal(entry.getKey(),0);
                
                int colCount = m.cols.size();
                m.unstitchRow(entry.getKey());
                if(m.cols.size() == colCount) { // Is there an impossible column?
                    solve2(m);
                }
                m.restitch();
            }
        }
        
        if(solnFound) {
            if(simplex.cost + partCost < cost) {
                soln.clear();
                soln.addAll(simplex.soln.keySet());
                soln.addAll(partSoln);
                cost = simplex.cost + partCost;
            }
        }
    }
    
}
