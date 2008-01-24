package nii.alloe.consist;
import java.util.*;
import nii.alloe.theory.Logic;
import nii.alloe.theory.Model;
import nii.alloe.niceties.*;

/**
 * Implements the branch and bound section of the solution. This is carried out by first attempting a
 * relaxed (linear)
 * solution using {@link Simplex#simplexSolve(SparseMatrix)}, if any non integer solutions are found
 * the solver branches on this row (first non-integer row is always chosen).
 *
 * @author John McCrae, National Institute of Informatics
 */
public class ConsistSolver implements AlloeProcess,java.io.Serializable,Runnable{
    /**
     * The solution will be in this variable after calling solve()
     */
    public TreeSet<Integer> soln;
    /**
     * The cost of the minimal solution will be in this variable after calling solve()
     */
    public double cost;
    
    private static Simplex simplex;
    
    static {
        simplex = new Simplex();
    }
    
    /** Creates a new instance of ConsistSolver */
    public ConsistSolver() {
        soln = new TreeSet<Integer>();
    }
    
    private SparseMatrix matrix;
    
    /** Create a new instance. Please use this constructor if you plan to start the solver
     * as a process */
    public ConsistSolver(SparseMatrix matrix) {
        this.matrix = matrix;
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
        matrix = cp.buildProblemMatrix();
        solve(matrix);
    }
    
    private LinkedList<Branch> branches;
    private transient int iterationDepth;
    private transient double progress;
    
    /**
     * Solves a problem matrix
     */
    public void solve(SparseMatrix matrix) {
        this.matrix = matrix;
        solve();
    }
    
    private void solve() {
        partSoln = new TreeSet<Integer>();
        cost = Double.MAX_VALUE;
        partCost = 0;
        iterationDepth = 0;
        progress = 0;
        if(branches == null)
            branches = new LinkedList<Branch>();
        fireNewProgressChange(0);
        solve2(matrix);
        
        iterationDepth = 0;
        progress = 0;
        
        fireFinished();
    }
    
    private void solve2(SparseMatrix m) {
        simplex.simplexSolve(m.createCopy());
        if(simplex.success && simplex.cost + partCost > cost) {
            progress += Math.pow(2,-iterationDepth);
            fireNewProgressChange(progress);
            return;
        }
        
        Iterator<Map.Entry<Integer,Double>> iter = simplex.soln.entrySet().iterator();
        Branch branch = null;
        if(branches.size() <= iterationDepth) {
            while(iter.hasNext()) {
                Map.Entry<Integer,Double> entry = iter.next();
                if(!entry.getValue().equals(1.0) && entry.getKey() >= 0) {
                    branch = new Branch();
                    branch.branchType = ADD;
                    branch.row = entry.getKey();
                    branches.add(branch);
                    break;
                }
            }
        } else {
            branch = branches.pop();
        }
        if(state != STATE_OK)
            return;
        
        if(branch == null && !simplex.success) {
            Output.out.println("Simplex failed with no hints, branching at random");
            branch = new Branch();
            branch.branchType = ADD;
            branch.row = simplex.soln.keySet().iterator().next();
            branches.add(branch);
        }
        
        if(branch != null) {
            iterationDepth++;
            if(branch.branchType == ADD) {
                partCost += m.elemVal(branch.row,0);
                m.selectRow(branch.row);
                partSoln.add(branch.row);
                Output.out.println("Branching: ADD " + branch.row);
                solve2(m);
                partSoln.remove(branch.row);
                m.restitch();
            }
            if(state != STATE_OK)
                return;
            branch.branchType = REMOVE;
            int colCount = m.cols.size();
            m.unstitchRow(branch.row);
            if(m.cols.size() == colCount) { // Is there an impossible column?
                Output.out.println("Branching: REMOVE " + branch.row);
                solve2(m);
            }
            m.restitch();
            iterationDepth--;
        } else {
            if(simplex.cost + partCost < cost) {
                soln.clear();
                soln.addAll(simplex.soln.keySet());
                soln.addAll(partSoln);
                cost = simplex.cost + partCost;
                progress += Math.pow(2,-iterationDepth);
                fireNewProgressChange(progress);
            }
        }
    }
    
    private transient LinkedList<AlloeProgressListener> aplListeners;
    private transient Thread theThread;
    private transient int state;
    private static final int STATE_OK = 0;
    private static final int STATE_STOPPING = 1;
    private static final int STATE_UNPAUSEABLE = 2;
    
    /** Register a progress listener */
    public void addProgressListener(AlloeProgressListener apl) {
        if(aplListeners == null)
            aplListeners = new LinkedList<AlloeProgressListener>();
        if(!aplListeners.contains(apl))
            aplListeners.add(apl);
    }
    
    
    
    private void fireNewProgressChange(double newProgress) {
        if(aplListeners != null) {
            Iterator<AlloeProgressListener> apliter = aplListeners.iterator();
            while(apliter.hasNext()) {
                apliter.next().progressChange(newProgress);
            }
        }
    }
    
    private void fireFinished() {
        if(aplListeners != null) {
            Iterator<AlloeProgressListener> apliter = aplListeners.iterator();
            while(apliter.hasNext()) {
                apliter.next().finished();
            }
        }
    }
    
    
    
    /** Start process. It is expected that this function should start the progress
     * in a new thread */
    public void start() {
        theThread = new Thread(this);
        state = STATE_OK;
        theThread.start();
    }
    
    /** Pause the process. The assumption is that this will work by changing a variable
     * in the running thread and then wait for this thread to finish by use of join().
     * It is assumed that the this object is Serializable, otherwise it's your problem
     * to assure the object is ok when resume() is called.
     *
     * @throws CannotPauseException If the process is not in a state where it can be resumed
     */
    public void pause() throws CannotPauseException {
        try {
            if(state == STATE_UNPAUSEABLE)
                throw new CannotPauseException("Some reason");
            state = STATE_STOPPING;
            theThread.join();
        } catch(InterruptedException x) {
            throw new CannotPauseException("The thread was interrupted");
        }
    }
    
    /** Resume the process.
     * @see #pause()
     */
    public void resume() {
        theThread = new Thread(this);
        state = STATE_OK;
        theThread.start();
    }
    
    
    private static final int ADD = 0;
    private static final int REMOVE = 1;
    
    /** Get a string representation of the current action being performed */
    public String getStateMessage() { return "Solving Matrix: "; }
    
    private class Branch {
        int branchType;
        int row;
    }

    public void run() {
        solve();
    }
}
