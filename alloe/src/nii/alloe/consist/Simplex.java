package nii.alloe.consist;
import java.util.*;
import nii.alloe.tools.process.Output;

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
    /** The rows and their values, rows are omitted if the value is zero */
    public SortedMap<Integer,Double> soln;
    /** The cost of the minimal solution */
    public double cost;
    /** True only after the simplex algorithm terminated */
    public boolean success;
    /** Change this to change the number of iterations */
    public static int ITERATION_MAX = 10000;
    /** Change this to make the algorithm more cautious about cycling */
    public static int CYCLE_DEPTH = 3;
    
    private transient Random random;
    
    /** Creates a new instance of Simplex */
    public Simplex() {
        
    }
    
    private class Pivot { 
        double row, col; 
        public boolean equals(Object o) { 
            if(!(o instanceof Pivot))
                return false;
            return ((Pivot)o).row == row && ((Pivot)o).col == col;
        }
    }
    private LinkedList<Pivot> pivots;
    
    /**
     * Find the optimal linear solution of a matrix m using simplex solve */
    public void simplexSolve(SparseMatrix m) {
        if(m.cols.isEmpty()) {
            soln = new TreeMap<Integer,Double>();
            cost = 0;
            success = true;
            return;
        }
            
        Integer slackBegin = m.cols.lastKey() + 1;
        Integer solnRow = m.rows.lastKey() + 1;
        success = true;
        random = new Random();
        pivots = new LinkedList<Pivot>();
        
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
        int iterations = 0;
        
        while(m.findMinRowVal(solnRow) < 0 && iterations < ITERATION_MAX && success) {
            // Find Pivot Col
            TreeSet<Integer> pivotCols = m.findMinRowIdx(solnRow);
            Integer pivotCol;
            if(pivotCols.size() == 1) {
                pivotCol = pivotCols.first();
            } else {
                // If there are multiple pivot cols, decide by column sum
                Iterator<Integer> piter = pivotCols.iterator();
                Vector<Integer> pivotCols2 = new Vector<Integer>();
                double mcs = Double.MAX_VALUE;
                while(piter.hasNext()) {
                    Integer pc = piter.next();
                    double cs = m.columnSum(pc);
                    if(cs < mcs) {
                        pivotCols2.clear();
                        pivotCols2.add(pc);
                        mcs = cs;
                    } else if(cs == mcs) {
                        pivotCols2.add(pc);
                    }
                }
                // Then if there is still no winner, choose at random (this also prevents cycling)
                if(pivotCols2.size() == 1) {
                    pivotCol = pivotCols2.get(0);
                } else {
                    pivotCol = pivotCols2.get(random.nextInt(pivotCols2.size()));
                }
            }
            
            // Find Pivot row
            Integer pivotRow = m.minPosRatioIdx(0, pivotCol);
            
            if(pivotRow < 0 || pivotRow >= solnRow || pivotCol <= 0) {
                throw new RuntimeException("Failed to choose a pivot in simplex algorithm, matrix may be unfit!");
            }
            pushPivot(pivotRow, pivotCol);
            
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
            iterations++;
            
            //Output.out.println("Negatives: " + m.negativesInRow(solnRow));
            // Check for cycling
            for(int n = 1; n <= CYCLE_DEPTH; n++) {
                if(isCycling(n)) {
                    Output.err.println("Cycling detected!");
                    success = false;
                    break;
                }
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
        if(iterations == ITERATION_MAX)
            success = false;
        else
            cost = m.elemVal(solnRow,0);
        pivots = null;
        
    }
    
    private void pushPivot(int row, int col) {
        Pivot p = new Pivot();
        p.row = row;
        p.col = col;
        pivots.add(p);
    }
    
    private boolean isCycling(int n) {
        if(pivots.size() < 2 * n)
            return false;
        return pivots.subList(pivots.size() - n, pivots.size()).equals(pivots.subList(pivots.size() - 2 *n, pivots.size() - n));
    }
}
