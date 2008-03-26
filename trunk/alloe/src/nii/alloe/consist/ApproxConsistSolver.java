package nii.alloe.consist;
import java.util.*;

/**
 *
 * @author John McCrae, National Institute of Informatics
 */
public class ApproxConsistSolver extends ConsistSolver {
    
    public void solve(SparseMatrix matrix) {
        this.matrix = matrix;
        fireNewProgressChange(0);
        simplex.simplexSolve(matrix.createCopy());
        soln = new TreeSet<Integer>();
        cost = 0;
        for(int i : simplex.soln.keySet()) {
            if(!simplex.soln.get(i).equals(0.0)) {
                soln.add(i);
                cost += matrix.elemVal(i,0);
            }
        }
        
        TreeSet<Integer> superfluous = new TreeSet<Integer>();
        while(true) {
            TreeSet<Integer> oversolved = new TreeSet<Integer>();
            for(int i : matrix.cols.keySet()) {
                if(i == 0)
                    continue;
                TreeSet<Integer> col = matrix.getCol(i);
                col.retainAll(soln);
                if(col.size() >= 2) {
                    oversolved.add(i);
                }
            }
            oversolved.add(0);
            superfluous.clear();
            for(int i : matrix.rows.keySet()) {
                if(soln.contains(i) && oversolved.containsAll(matrix.getRow(i))) {
                    superfluous.add(i);
                }
            }
            
            if(superfluous.isEmpty())
                break;
            
            int bestRemove = -1;
            double bestRemoveCost = -1;
            for(int i : superfluous) {
                double c = matrix.elemVal(i,0);
                if(c > bestRemoveCost) {
                    bestRemove = i;
                    bestRemoveCost = c;
                }
            }
            soln.remove(bestRemove);
            cost -= bestRemoveCost;
        }
    }
}
