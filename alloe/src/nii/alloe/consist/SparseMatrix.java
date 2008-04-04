package nii.alloe.consist;
import java.util.*;
import java.io.*;
import nii.alloe.theory.*;

/**
 * A sparse matrix. Implemented as a tree map of single linked lists
 */
public class SparseMatrix implements Serializable {
    TreeMap<Integer, SparseNode> cols;
    TreeMap<Integer, SparseNode> rows;
    
    /** Create a new instance */
    public SparseMatrix() {
        rows = new TreeMap<Integer, SparseNode>();
        cols = new TreeMap<Integer, SparseNode>();
        operations = new Stack<Operation>();
    }
    
    /**
     * Add a new row to the matrix
     * @param index The index of the row
     * @param elems The set of non-zero values (value assumed to be 1)
     */
    public void addRow(Integer index, Set<Integer> elems) {
        if(elems.isEmpty()) {
            throw new IllegalArgumentException("elems is empty");
        }
        if(rows.get(index) != null) {
            throw new IllegalArgumentException("row already exists");
        }
        Iterator<Integer> i = elems.iterator();
        SparseNode first = null;
        SparseNode last = null;
        while(i.hasNext()) {
            Integer e = i.next();
            SparseNode newN = new SparseNode(index, e,1);
            if(cols.get(e) != null) {
                SparseNode n = findJustAbove(e, index);
                if(n != null) {
                    newN.down = n.down;
                    n.down = newN;
                } else {
                    newN.down = cols.get(e);
                    cols.put(e,newN);
                }
            } else {
                cols.put(e, newN);
            }
            if(last !=  null) {
                last.right = newN;
            } else {
                first = newN;
            }
            last = newN;
        }
        rows.put(index, first);
    }
    
    private SparseNode findJustAbove(Integer i, Integer j) {
        SparseNode n = cols.get(j);
        if(n == null || n.i >= i)
            return null;
        for(; n.down != null && n.down.i < i;
        n = n.down);
        return n;
    }
    
    /**
     * Remove a row from the matrix
     * @param index the row to remove
     */
    public void removeRow(Integer index) {
        removeRow(index, null);
    }
    
    /**
     * Remove a row from the matrix, this should be used if index was gained from an Iterator
     * generated from {@link #getCol(Integer)} to avoid ConcurrentModificationExceptions.
     * @param index the index to remove
     * @param iter an iterator currently in use from which index was obtained
     */
    public void removeRow(Integer index, Iterator<Integer> iter) {
        for(SparseNode n = rows.get(index); n != null; n = n.right) {
            SparseNode n2 = findJustAbove(n.i, n.j);
            if(n2 != null) {
                n2.down = n.down;
            } else if(n.down != null) {
                cols.put(n.j,n.down);
            } else {
                cols.remove(n.j);
            }
            n.down = null;
        }
        if(iter != null) {
            iter.remove();
        } else {
            rows.remove(index);
        }
    }
    
      /**
     * Add a new column to the matrix
     * @param index The index of the column
     * @param elems The set of non-zero values (value assumed to be 1)
     */
    public void addColumn(Integer index, TreeSet<Integer> elems) {
        if(elems.isEmpty()) {
            throw new IllegalArgumentException("elems is empty");
        }
        if(cols.get(index) != null) {
            throw new IllegalArgumentException("col already exists");
        }
        Iterator<Integer> i = elems.iterator();
        SparseNode first = null;
        SparseNode last = null;
        while(i.hasNext()) {
            Integer e = i.next();
            SparseNode newN = new SparseNode(e,index,1);
            if(rows.get(e) != null) {
                SparseNode n = findJustLeft(e, index);
                if(n != null) {
                    newN.right = n.right;
                    n.right = newN;
                } else {
                    newN.right = rows.get(e);
                    rows.put(e,newN);
                }
            } else {
                rows.put(e, newN);
            }
            if(last !=  null) {
                last.down = newN;
            } else {
                first = newN;
            }
            last = newN;
        }
        cols.put(index, first);
    }
    
    private SparseNode findJustLeft(Integer i, Integer j) {
        SparseNode n = rows.get(i);
        if(n == null || n.j >= j)
            return null;
        for(; n.right != null && n.right.j < j;
        n = n.right);
        return n;
    }
    
    /**
     * Remove a column from the matrix
     * @param index the column to remove
     */
    public void removeColumn(Integer index) {
        removeColumn(index, null);
    }
    
     /**
     * Remove a column from the matrix, this should be used if index was gained from an Iterator
     * generated from {@link #getRow(Integer)} to avoid ConcurrentModificationExceptions.
     * @param index the index to remove
     * @param iter an iterator currently in use from which index was obtained
     */
    public void removeColumn(Integer index, Iterator<Integer> iter) {
        for(SparseNode n = cols.get(index); n != null; n = n.down) {
            SparseNode n2 = findJustLeft(n.i, n.j);
            if(n2 != null) {
                n2.right = n.right;
            } else if(n.right != null) {
                rows.put(n.i,n.right);
            } else {
                rows.remove(n.i);
            }
            n.right = null;
        }
        if(iter != null) {
            iter.remove();
        } else {
            cols.remove(index);
        }
    }
    
    /**
     * @return true if (i,j) is non-zero */
    public boolean hasElem(Integer i, Integer j) {
        SparseNode n = rows.get(i);
        if(n != null) {
            for(; n != null && n.j < j; n = n.right);
            return n != null && n.j.equals(j);
        } else {
            return false;
        }
    }
    
    /**
     * @return the element vale at (i,j) */
    public double elemVal(Integer i, Integer j) {
        SparseNode n = rows.get(i);
        if(n != null) {
            for(; n != null && n.j < j; n = n.right);
            return n != null && n.j.equals(j) ? n.val : 0;
        } else {
            return 0;
        }
    }
    
    /**
     * Set element value at (i,j) to 1 */
    public void setElem(Integer i , Integer j) {
        setElem(i,j,1);
    }
    
    /**
     * Set element value at (i,j) to v */
    public void setElem(Integer i, Integer j, double v) {
        if(v == 0) {
            this.removeElem(i,j);
            return;
        }
        if(hasElem(i,j)) {
            SparseNode nTemp = getElem(i,j);
            nTemp.val = v;
            return;
        }
        SparseNode newN = new SparseNode(i,j,v);
        addElem(newN);
    }
    
    private void addElem(SparseNode newN) {
        SparseNode above = findJustAbove(newN.i,newN.j);
        if(above != null) {
            newN.down = above.down;
            above.down = newN;
        } else {
            newN.down = cols.get(newN.j);
            cols.put(newN.j,newN);
        }
        
        SparseNode left = findJustLeft(newN.i,newN.j);
        if(left != null) {
            newN.right = left.right;
            left.right = newN;
        } else {
            newN.right = rows.get(newN.i);
            rows.put(newN.i,newN);
        }
    }
    
    private SparseNode getElem(Integer i, Integer j) {
        SparseNode n = rows.get(i);
        if(n != null) {
            for(; n != null && n.j < j; n = n.right);
            if(n != null && n.j.equals(j)) {
                return n;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
    
    /**
     * Set value at (i,j) to 0 */
    public void removeElem(Integer i, Integer j) {
        removeElem(getElem(i,j));
    }
    
    private void removeElem(SparseNode n) {
        if(n == null)
            return;
        SparseNode above = findJustAbove(n.i,n.j);
        if(above != null) {
            above.down = n.down;
        } else if(n.down != null) {
            cols.put(n.j,n.down);
        } else {
            cols.remove(n.j);
        }
        
        SparseNode left = findJustLeft(n.i,n.j);
        if(left != null) {
            left.right = n.right;
        } else if(n.right != null) {
            rows.put(n.i,n.right);
        } else {
            rows.remove(n.i);
        }
    }
    
    /**
     * Get the values on row idx which are non-zero */
    public TreeSet<Integer> getRow(Integer idx) {
        SparseNode n = rows.get(idx);
        TreeSet<Integer> rval = new TreeSet<Integer>();
        if(n != null) {
            do {
                rval.add(n.j);
                n = n.right;
            } while(n != null);
        }
        return rval;
    }
    
    /**
     * Get the non-zero values on row idx as a map to their values */
    public TreeMap<Integer,Double> getRowVals(Integer idx) {
        SparseNode n = rows.get(idx);
        TreeMap<Integer,Double> rval = new TreeMap<Integer,Double>();
        if(n != null) {
            do {
                rval.put(n.j,n.val);
                n = n.right;
            } while(n != null);
        }
        return rval;
    }
    
    /** Get a set of the non-zero values on column idx */
    public TreeSet<Integer> getCol(Integer idx) {
        SparseNode n = cols.get(idx);
        TreeSet<Integer> rval = new TreeSet<Integer>();
        if(n != null) {
            do {
                rval.add(n.i);
                n = n.down;
            } while(n != null);
        }
        return rval;
    }
    
    /** Get a map of the non-zero values on column idx to their values */
    public TreeMap<Integer,Double> getColVals(Integer idx) {
        SparseNode n = cols.get(idx);
        TreeMap<Integer,Double> rval = new TreeMap<Integer,Double>();
        if(n != null) {
            do {
                rval.put(n.i,n.val);
                n = n.down;
            } while(n != null);
        }
        return rval;
    }
    
    /** Print the matrix in simplified form to out e.g.
     * <br>
     * <code>
     * 1: [ 1, 4 ] <br>
     * 2: [ 2, 3, 4 ] <br>
     * 4: [ 1 ]
     * </code>
     */
    public void printMatrix(PrintStream out) {
        Iterator<Map.Entry<Integer, SparseNode>> i = rows.entrySet().iterator();
        while(i.hasNext()) {
            Map.Entry<Integer, SparseNode> e = i.next();
            out.print(e.getKey() + ": [ ");
            for(SparseNode n = e.getValue(); n != null; n = n.right) {
                out.print(n.j + (n.right != null ? ", " : " "));
            }
            out.println("]");
        }
    }
    
    /** Print the matrix in full form to out e.g.
     * <br>
     * <code>
     * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 1 &nbsp;&nbsp;&nbsp;2 &nbsp;&nbsp;&nbsp;3 &nbsp;&nbsp;&nbsp;4 <br>
     * &nbsp;&nbsp;&nbsp;1 &nbsp;1.0&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 0.5 <br>
     * &nbsp;&nbsp;&nbsp;2&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 1.0&nbsp; 0.3 -1.0 <br>
     * &nbsp;&nbsp;&nbsp;4&nbsp; 1.0
     * </code>
     */
    public void printFull(PrintStream out) {
        out.print("    ");
        Iterator<Integer> citer = cols.keySet().iterator();
        while(citer.hasNext()) {
            out.printf("%1$4d ", citer.next());
        }
        out.println("");
        Iterator<Integer> riter =  rows.keySet().iterator();
        while(riter.hasNext()) {
            Integer row = riter.next();
            out.printf("%1$4d ", row);
            citer = cols.keySet().iterator();
            while(citer.hasNext()) {
                out.printf("%1$ 1.1f ", elemVal(row,citer.next()));
            }
            out.println("");
        }
    }
    
    /**
     * @return true if every non-zero column in row 1 is also non-zero in row 2
     */
    public boolean rowSubset(Integer row1, Integer row2) {
        SparseNode n1 = rows.get(row1);
        SparseNode n2 = rows.get(row2);
        while(n1 != null && n2 != null) {
            if(n2.j < n1.j)
                n2 = n2.right;
            else if(n2.j.equals(n1.j))
                n1 = n1.right;
            else if(n2.j > n1.j)
                return false;
        }
        return n1 == null;
    }
    
    /**
     * @return true if every non-zero row in col 1 is also non-zero in col 2
     */
    public boolean colSubset(Integer col1, Integer col2) {
        SparseNode n1 = cols.get(col1);
        SparseNode n2 = cols.get(col2);
        while(n1 != null && n2 != null) {
            if(n2.i < n1.i)
                n2 = n2.down;
            else if(n2.i.equals(n1.i))
                n1 = n1.down;
            else if(n2.i > n1.i)
                return false;
        }
        return n1 == null;
    }
    
    /**
     *  @return true if every non-zero row in col1 is contained in set col 2
     */
    public boolean colSubset(Integer col1, TreeSet<Integer> col2) {
        SparseNode n1 = cols.get(col1);
        Iterator<Integer> i2 = col2.iterator();
        while(n1 != null && i2.hasNext()) {
            Integer n2 = i2.next();
            if(n2.equals(n1.i))
                n1 = n1.down;
            else if(n2 > n1.i)
                return false;
        }
        return n1 == null;
    }
    
    /**
     * Returns number of non-zero elements in matrix */
    public int getElemCount() {
        int rval = 0;
        Iterator<SparseNode> si = rows.values().iterator();
        while(si.hasNext()) {
            for(SparseNode n = si.next(); n != null; n = n.right) {
                rval++;
            }
        }
        return rval;
    }
    
    /** Returns number of columns in matrix */
    public int getColumnCount() {
        return cols.size();
    }
    
    /** Returns number of rows in matrix */
    public int getRowCount() {
        return rows.size();
    }
    
    /** Convert to three sparse arrays of coordinates and values 
     * @throws IllegalArguentException is arrays do not match in length
     */
    public void toArrays(int []iArray, int []jArray, double []valArray) throws IllegalArgumentException {
        if(iArray.length != getElemCount() || jArray.length != getElemCount() || valArray.length != getElemCount()) {
            throw new IllegalArgumentException("Arrays passed do not match size of matrix!");
        }
        int i = 0;
        Iterator<SparseNode> si = rows.values().iterator();
        while(si.hasNext()) {
            for(SparseNode n = si.next(); n != null; n = n.right) {
                iArray[i] = n.i;
                jArray[i] = n.j;
                valArray[i++] = 1;
            }
        }
    }
    
    /** clone this matrix */
    public SparseMatrix createCopy() {
        SparseMatrix m = new SparseMatrix();
        Iterator<SparseNode> i = cols.values().iterator();
        
        while(i.hasNext()) {
            for(SparseNode n = i.next(); n!= null; n = n.down) {
                m.setElem(n.i,n.j,n.val);
            }
        }
        return m;
    }
    
    public boolean equals(Object obj) {
        if(!(obj instanceof SparseMatrix)) {
            return false;
        } else {
            SparseMatrix m = (SparseMatrix)obj;
            
            if(!m.cols.keySet().equals(cols.keySet()) || !m.rows.keySet().equals(rows.keySet())) {
                return false;
            }
            Iterator<Map.Entry<Integer,SparseNode>> i = cols.entrySet().iterator();
            Iterator<Map.Entry<Integer,SparseNode>> i2 = m.cols.entrySet().iterator();
            while(i.hasNext()) {
                Map.Entry<Integer,SparseNode> e = i.next();
                Map.Entry<Integer,SparseNode> e2 = i2.next();
                SparseNode n2 = e2.getValue();
                for(SparseNode n = e.getValue(); n != null; n = n.down) {
                    if(!n.equals(n2)) {
                        return false;
                    }
                    n2 = n2.down;
                }
            }
        }
        return true;
    }
    
    /** Check the matrix is consistent (for debugging) */
    public void isOK() {
        Iterator<Map.Entry<Integer,SparseNode>> citer = cols.entrySet().iterator();
        while(citer.hasNext()) {
            Map.Entry<Integer,SparseNode> entry = citer.next();
            for(SparseNode n = entry.getValue(); n != null; n = n.down) {
                if((n.down != null && n.down.i <= n.i) || !n.j.equals(entry.getKey()) || !rows.containsKey(n.i)) {
                    throw new RuntimeException("Invalid matrix!!");
                }
            }
        }
        Iterator<Map.Entry<Integer,SparseNode>> riter = rows.entrySet().iterator();
        while(riter.hasNext()) {
            Map.Entry<Integer,SparseNode> entry = riter.next();
            for(SparseNode n = entry.getValue(); n != null; n = n.right) {
                if((n.right != null && n.right.j <= n.j) || !n.i.equals(entry.getKey()) || !cols.containsKey(n.j)) {
                    throw new RuntimeException("Invalid matrix!!");
                }
            }
        }
    }

    public Set<Integer> getRows() {
        return rows.keySet();
    }
    
    public Set<Integer> getCols() {
        return cols.keySet();
    }
    
    /** Return the sets where each set has the same value on the row, at the column selected, ignores zeroes */
    public Vector<Set<Integer>> rowEqualitySets(Integer col) {
        Vector<Set<Integer>> rval = new Vector<Set<Integer>>();
        Vector<Double> vals = new Vector<Double>();
        for(SparseNode n = cols.get(col); n != null; n = n.down) {
            boolean cnt = false;
            for(int i = 0; i < vals.size(); i++) {
                if(n.val == vals.get(i).doubleValue()) {
                    rval.get(i).add(n.i);
                    cnt = true;
                    break;
                }
            }
            if(cnt)
                continue;
            vals.add(n.val);
            Set<Integer> s = new TreeSet<Integer>();
            s.add(n.i);
            rval.add(s);
        }
        return rval;
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Simplex functions
    
    int negativesInRow(Integer row) {
        int rval = 0;
        for(SparseNode n = rows.get(row); n != null; n = n.right) {
            if(n.val < 0)
                rval++;
        }
        return rval;
    }
    TreeSet<Integer> findMinRowIdx(Integer row) {
        double min = Double.MAX_VALUE;
        TreeSet<Integer> rval = new TreeSet<Integer>();
        for(SparseNode n = rows.get(row); n != null; n = n.right) {
            if(n.val < min) {
                rval.clear();
                rval.add(new Integer(n.j));
                min = n.val;
            } else if(n.val == min) {
                rval.add(new Integer(n.j));
            }
        }
        return rval;
    }
    
    double findMinRowVal(Integer row) {
        double min = Double.MAX_VALUE;
        for(SparseNode n = rows.get(row); n != null; n = n.right) {
            if(n.val < min) {
                min = n.val;
            }
        }
        return min;
    }
    
    double columnSum(Integer col) {
        double rval = 0;
        for(SparseNode n = cols.get(col); n != null; n = n.down) {
            rval += n.val;
        }
        return rval;
    }
    
    Integer minPosRatioIdx(Integer col1, Integer col2) {
        Integer rval = -1;
        double min = Double.MAX_VALUE;
        SparseNode n1 = cols.get(col1);
        SparseNode n2 = cols.get(col2);
        while(n1 != null && n2 != null) {
            if(n2.i < n1.i) {
                return n2.i;
            } else if(n2.i.equals(n1.i)) {
                if(n1.val / n2.val < min && n1.val / n2.val > 0) {
                    rval = n1.i;
                    min = n1.val / n2.val;
                }
                n1 = n1.down;
                n2 = n2.down;
            } else if(n2.i > n1.i)
                n1 = n1.down;
        }
        return rval;
    }
    
    /**
     * row = row / val
     */
    void divideRowBy(Integer row, double val) {
        for(SparseNode n = rows.get(row); n != null; n = n.right) {
            n.val = n.val / val;
        }
    }
    
    /**
     * row1 = row1 - row2 * val
     */
    void subtractRowFromRow(Integer row1, Integer row2, double val) {
        if(row1.equals(row2) || Double.isNaN(val) || Double.isInfinite(val))
            throw new IllegalArgumentException();
        SparseNode n1 = rows.get(row1);
        SparseNode n2 = rows.get(row2);
        while(n2 != null) {
            if(n1 == null || n2.j < n1.j) {
                setElem(row1, n2.j, -n2.val * val);
                n2 = n2.right;
            } else if(n2.j.equals(n1.j)) {
                n1.val = n1.val - n2.val * val;
                if(n1.val == 0) {
                    removeElem(n1);
                }
                n1 = n1.right;
                n2 = n2.right;
            } else if(n2.j > n1.j) {
                n1 = n1.right;
            }
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Solver functions;
    
    private class Operation implements Serializable {
        SparseNode n;
        boolean row;
        Operation(SparseNode n, boolean row) { this.n = n; this.row = row; }
    }
    
    private Stack<Operation> operations;
    
    void unstitchRow(Integer idx) {
        operations.push(new Operation(rows.get(idx),true));
        for(SparseNode n = rows.get(idx); n != null; n = n.right) {
            removeElem(n);
        }
    }
    
    void unstitchCol(Integer idx) {
        operations.push(new Operation(cols.get(idx),false));
        for(SparseNode n = cols.get(idx); n != null; n = n.down) {
            removeElem(n);
        }
    }
    
    void selectRow(Integer idx) {
        operations.push(new Operation(rows.get(idx),true));
        for(SparseNode n = rows.get(idx); n != null;) {
            Integer i = n.j;
            n = n.right;
            if(!i.equals(0)) {
                unstitchCol(i);
            }
        }
        rows.remove(idx);
    }
    
    void restitch() {
        Operation o = operations.pop();
        if(o.row) {
            for(SparseNode n = o.n; n != null;) {
                SparseNode temp = n.right;
                addElem(n);
                n = temp;
            }
        } else {
            while(!o.row) {
                for(SparseNode n = o.n; n != null;) {
                    SparseNode temp = n.down;
                    addElem(n);
                    n = temp;
                }
                o = operations.pop();
            }
            o.n.right = rows.get(o.n.i);
            rows.put(o.n.i,o.n);
        }
    }
    
    public Rule columnToRule(Integer col, Model model) {
        LinkedList<Integer> positives = new LinkedList<Integer>();
        LinkedList<Integer> negatives = new LinkedList<Integer>();
        for(SparseNode n = cols.get(col); n != null; n = n.down) {
            if(model.isConnected(n.i))
                positives.add(n.i);
            else
                negatives.add(n.i);
        }
        
        return new Rule(positives, negatives, model);
    }
    
    public class SparseNode implements Serializable {
        public SparseNode down,right;
        public final Integer i,j;
        public double val;
        
        public SparseNode(Integer i, Integer j, double val) {
            this.i  = i;
            this.j = j;
            this.val = val;
        }
        
        public boolean equals(Object o) {
            if((o instanceof SparseNode)) {
                SparseNode n = (SparseNode)o;
                return i.equals(n.i) && j.equals(n.j) && val >= n.val * (1 - ConsistProblem.PERTURBATION_SIZE) && val <= n.val * (1 + ConsistProblem.PERTURBATION_SIZE);
            } else
                return false;
        }
    }
}