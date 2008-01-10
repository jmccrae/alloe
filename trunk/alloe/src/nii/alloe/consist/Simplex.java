package nii.alloe.consist;
import java.util.*;

/**
 * Simplex Algorithm. Implemented as follows, initially set the tableau to be
 * <table>
 *   <tr><td> </td><td> c </td> <td> x1 ... xn </td> <td> s1 ... sm </td> </tr>
 *   <tr><td> Li </td><td> costs </td> <td> consist matrix </td> <td> identity matrix </td> </tr>
 *   <tr><td> P </td> <td> 0 </td> <td> -1 -1 -1 ... </td> <td> 0 0 0 .... </td> </tr>
 * </table>
 *
 * Then choose the column (not c) such that the value in the P row is least (if several satisfy this choose least column sum, (then max row sum or at random) .
 * Choose a pivot row with minimum positive value for c / selected column
 * Subtract this row from all the others such that every value in the column is zero except for the pivot row
 * Repeat until all of row P is non-negative.
 * The value of c in row P is the minimum cost and the value of the variable s1 ... sm are the choice of rows
 */


public class Simplex {
    
    public SortedMap<Integer,Double> soln;
    public double cost;
    
    /** Creates a new instance of Simplex */
    public Simplex() {
    }
    
    /**
     * Find the optimal linear solution of a matrix m using simplex solve */
    public void simplexSolve(SparseMatrix m) {
        Integer slackBegin = m.cols.lastKey() + 1;
        Integer solnRow = m.rows.lastKey() + 1;
        
        // 
        Iterator<Integer> citer = m.cols.keySet().iterator();
        Integer i = citer.next();
        if(i != 0) {
            throw new IllegalArgumentException("Matrix does not contain cost col!");
        }
        while(citer.hasNext()) {
            i = citer.next();
            m.setElem(solnRow, i, -1);
        }
        Iterator<Integer> riter = m.rows.keySet().iterator();
        while(riter.hasNext()) {
            i = riter.next();
            if(!i.equals(solnRow))
                m.setElem(i,slackBegin+i);
        }
        
        while(m.findMinRowVal(solnRow) < 0) {
            m.printFull(System.out);
            System.out.println("");
            // Find Pivot Col
            TreeSet<Integer> pivotCols = m.findMinRowIdx(solnRow);
            Integer pivotCol = pivotCols.first();
            Iterator<Integer> piter = pivotCols.iterator();
            double mcs = Double.MAX_VALUE;
            while(piter.hasNext()) {
                Integer pc = piter.next();
                double cs = m.columnSum(pc);
                if(cs < mcs) {
                    mcs = cs;
                    pivotCol = pc;
                }
            }
            
            // Find Pivot row
            Integer pivotRow = m.minPosRatioIdx(0, pivotCol);
            
            if(pivotRow < 0 || pivotRow >= solnRow || pivotCol <= 0) {
                throw new RuntimeException("Failed to choose a pivot in simplex algorithm, matrix may be unfit!");
            }
            
            // Normalize pivot row
            Double d = new Double(m.elemVal(pivotRow, pivotCol));
            m.divideRowBy(pivotRow, d);
            
            // Remove non pivot zeros in pivot column
            riter = m.rows.keySet().iterator();
            while(riter.hasNext()) {
                try {
                    i = riter.next();
                } catch(ConcurrentModificationException x) {
                    riter = m.rows.keySet().iterator();
                    continue;
                }
                if(i.equals(pivotRow))
                    continue;
                
                m.subtractRowFromRow(i,pivotRow,m.elemVal(i,pivotCol));
            }
        }
        soln = m.getRowVals(solnRow).tailMap(slackBegin);
        TreeMap<Integer,Double> shiftedSoln = new TreeMap<Integer,Double>();
        Iterator<Map.Entry<Integer,Double>> eiter = soln.entrySet().iterator();
        while(eiter.hasNext()) {
            Map.Entry<Integer,Double> entry = eiter.next();
            shiftedSoln.put(entry.getKey() - slackBegin, entry.getValue());
        }
        soln = shiftedSoln;
        cost = m.elemVal(solnRow,0);
    }
    
}
