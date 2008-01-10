/*
 * SparseMatrixTest.java
 * JUnit based test
 *
 * Created on November 24, 2007, 7:18 AM
 */

package nii.alloe.consist;

import junit.framework.*;
import java.util.*;
import java.io.*;

/**
 *
 * @author john
 */
public class SparseMatrixTest extends TestCase {
    
    static Random rand = new Random();
    
    public SparseMatrixTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
    }
    
    protected void tearDown() throws Exception {
    }
    
    public SparseMatrix randomMatrix() {
        SparseMatrix rval = new SparseMatrix();
        for(int i = 0; i < 20; i++) {
            rval.setElem(rand.nextInt(10),rand.nextInt(10));
        }
        return rval;
    }
    
    /**
     * Test of addRow method, of class nii.aloe.consist.SparseMatrix.
     */
    public void testAddRow() {
        System.out.println("addRow");
        
        Integer index = new Integer(0);
        TreeSet<Integer> elems = new TreeSet<Integer>();
        SparseMatrix instance = new SparseMatrix();
        boolean exceptionCaught = false;
        try {
            instance.addRow(index, elems);
        } catch(IllegalArgumentException x) {
            exceptionCaught = true;
        }
        
        if(!exceptionCaught) {
            fail("did not throw error for empty set");
        }
        
        elems.add(1);
        elems.add(7);
        elems.add(3);
        instance.addRow(index,elems);
        if(!instance.hasElem(0,1) || !instance.hasElem(0,7) || !instance.hasElem(0,3)) {
            fail("oh dear");
        }
        exceptionCaught = false;
        try {
            instance.addRow(index,elems);
        } catch(IllegalArgumentException x) {
            exceptionCaught = true;
        }
        if(!exceptionCaught || !instance.hasElem(0,1) || !instance.hasElem(0,7) || !instance.hasElem(0,3)) {
            fail("oh dear");
        }
        instance.isOK();
    }
    

    
    /**
     * Test of removeRow method, of class nii.aloe.consist.SparseMatrix.
     */
    public void testRemoveRow() {
        System.out.println("removeRow");
        

        SparseMatrix instance = randomMatrix();
        Integer row;
        
        for(int i = 0; i < 10; i++) {
            if(instance.hasElem(i,0)) {
                instance.removeRow(new Integer(i));
            }
        }
        instance.isOK();
    }
    
    /**
     * Test of addColumn method, of class nii.aloe.consist.SparseMatrix.
     */
    public void testAddColumn() {
        System.out.println("addColumn");
        
        Integer index = new Integer(0);
        TreeSet<Integer> elems = new TreeSet<Integer>();
        SparseMatrix instance = new SparseMatrix();
        boolean exceptionCaught = false;
        try {
            instance.addColumn(index, elems);
        } catch(IllegalArgumentException x) {
            exceptionCaught = true;
        }
        
        if(!exceptionCaught) {
            fail("did not throw error for empty set");
        }
        
        elems.add(1);
        elems.add(7);
        elems.add(3);
        instance.addColumn(index,elems);
        if(!instance.hasElem(1,0) || !instance.hasElem(7,0) || !instance.hasElem(3,0)) {
            fail("oh dear");
        }
        exceptionCaught = false;
        try {
            instance.addColumn(index,elems);
        } catch(IllegalArgumentException x) {
            exceptionCaught = true;
        }
        if(!exceptionCaught || !instance.hasElem(1,0) || !instance.hasElem(7,0) || !instance.hasElem(3,0)) {
            fail("oh dear");
        }
        instance.isOK();
    }
    

    
    /**
     * Test of removeColumn method, of class nii.aloe.consist.SparseMatrix.
     */
    public void testRemoveColumn() {
        System.out.println("removeColumn");
        
     //SparseMatrix instance = randomMatrix();
        SparseMatrix instance = new SparseMatrix();
        instance.setElem(1,5);
        instance.setElem(2,1);
        instance.setElem(2,6);
        instance.setElem(3,7);
        instance.setElem(5,5);
        instance.setElem(5,6);
        instance.setElem(6,2);
        instance.setElem(6,3);
        instance.setElem(6,6);
        instance.setElem(7,0);
        instance.setElem(7,2);
        instance.setElem(7,3);
        instance.setElem(7,5);
        instance.setElem(8,3);
        instance.setElem(8,7);
        instance.setElem(8,9);
        instance.setElem(9,5);
        instance.setElem(9,8);
        instance.setElem(9,9);
     instance.printMatrix(System.out);
        Integer row;
        
        for(int i = 0; i < 10; i++) {
            if(instance.hasElem(i,0)) {
                instance.removeColumn(new Integer(i));
            }
        }
        instance.printMatrix(System.out);
        instance.isOK();
    }
    
    /**
     * Test of hasElem method, of class nii.aloe.consist.SparseMatrix.
     */
    public void testHasElem() {
        System.out.println("hasElem tested elsewhere");
    }
    
    /**
     * Test of elemVal method, of class nii.aloe.consist.SparseMatrix.
     */
    public void testElemVal() {
        System.out.println("elemVal");
        
        Integer i = 2;
        Integer j = 3;
        SparseMatrix instance = randomMatrix();
        instance.setElem(i,j,6);
        
        double expResult = 6;
        double result = instance.elemVal(i, j);
        assertEquals(expResult, result);
        
        instance.setElem(i,j,0);
        
        expResult = 0;
        result = instance.elemVal(i, j);
        assertEquals(expResult, result);
        
        
        instance.isOK();
    }
    
    /**
     * Test of setElem method, of class nii.aloe.consist.SparseMatrix.
     */
    public void testSetElem() {
        System.out.println("setElem");
        
        Integer i = 2;
        Integer j = 3;
        SparseMatrix instance = randomMatrix();
        
        instance.setElem(i, j);
        instance.setElem(i,j);
        
        // TODO review the generated test code and remove the default call to fail.
        assertEquals(instance.elemVal(i,j),(double)1.0);
        
        instance.isOK();
    }
    
  
    
    /**
     * Test of removeElem method, of class nii.aloe.consist.SparseMatrix.
     */
    public void testRemoveElem() {
        System.out.println("removeElem");
        
        Integer i = 6;
        Integer j = 5;
        SparseMatrix instance = randomMatrix();
        
        instance.removeElem(i, j);
        instance.setElem(i,j);
        instance.removeElem(i, j);
        
         assertEquals(instance.elemVal(i,j),(double)0);
        instance.isOK();
    }
    
    /**
     * Test of getRow method, of class nii.aloe.consist.SparseMatrix.
     */
    public void testGetRow() {
        System.out.println("getRow");
        
        Integer idx = 4;
        SparseMatrix instance = new SparseMatrix();
        
        TreeSet<Integer> expResult = new TreeSet<Integer>();
        expResult.add(1);
        expResult.add(7);
        instance.addRow(idx,expResult);
        TreeSet<Integer> result = instance.getRow(idx);
        assertEquals(expResult, result);
        instance.isOK();

    }
    
    /**
     * Test of getRowVals method, of class nii.aloe.consist.SparseMatrix.
     */
    public void testGetRowVals() {
        System.out.println("getRowVals");
        
        Integer idx = 3;
        SparseMatrix instance = new SparseMatrix();
        
        TreeMap<Integer, Double> expResult = new TreeMap<Integer,Double>();
        expResult.put(new Integer(1), new Double(5)); instance.setElem(idx,1,5);
        expResult.put(new Integer(3), new Double(-1)); instance.setElem(idx,3,-1);
        TreeMap<Integer, Double> result = instance.getRowVals(idx);
        assertEquals(expResult, result);
        instance.isOK();

    }
    
    /**
     * Test of getCol method, of class nii.aloe.consist.SparseMatrix.
     */
    public void testGetCol() {
        System.out.println("getCol");
        
        Integer idx = 2;
        SparseMatrix instance = new SparseMatrix();
        
        TreeSet<Integer> expResult = new TreeSet<Integer>();
        expResult.add(1);
        expResult.add(7);
        instance.addColumn(idx,expResult);
        TreeSet<Integer> result = instance.getCol(idx);
        assertEquals(expResult, result);
        instance.isOK();

    }
    
    /**
     * Test of getColVals method, of class nii.aloe.consist.SparseMatrix.
     */
    public void testGetColVals() {
        System.out.println("getColVals");
        
        Integer idx = 1;
        SparseMatrix instance = new SparseMatrix();
        
     
        TreeMap<Integer, Double> expResult = new TreeMap<Integer,Double>();
        expResult.put(new Integer(1), new Double(5)); instance.setElem(1,idx,5);
        expResult.put(new Integer(3), new Double(-1)); instance.setElem(3,idx,-1);
        TreeMap<Integer, Double> result = instance.getColVals(idx);
        assertEquals(expResult, result);
        instance.isOK();

    }

    
    /**
     * Test of rowSubset method, of class nii.aloe.consist.SparseMatrix.
     */
    public void testRowSubset() {
        System.out.println("rowSubset");
        
        
        Integer row1 = rand.nextInt(10);
        Integer row2 = rand.nextInt(10);
        SparseMatrix instance = randomMatrix();
        
        
        boolean expResult = instance.getRow(row2).containsAll(instance.getRow(row1));
        boolean result = instance.rowSubset(row1, row2);
        assertEquals(expResult, result);
        instance.isOK();
  
    }
    
    /**
     * Test of colSubset method, of class nii.aloe.consist.SparseMatrix.
     */
    public void testColSubset() {
        System.out.println("colSubset");
        
        Integer col1 = rand.nextInt(10);
        Integer col2 = rand.nextInt(10);
        SparseMatrix instance = randomMatrix();
        
        boolean result = instance.colSubset(col1,col2);
        boolean expResult = instance.getCol(col2).containsAll(instance.getCol(col1));
        assertEquals(expResult, result);
        instance.isOK();
     }
    
    /**
     * Test of getElemCount method, of class nii.aloe.consist.SparseMatrix.
     */
    public void testGetElemCount() {
        System.out.println("getElemCount");
        
        SparseMatrix instance = new SparseMatrix();
        
        int expResult = 0;
        int result = instance.getElemCount();
        assertEquals(expResult, result);
        
        instance.setElem(2,3);
        instance.setElem(10,0);
        instance.setElem(4,7);
        expResult = 3;
        assertEquals(expResult, instance.getElemCount());
        instance.isOK();
    }  
}
