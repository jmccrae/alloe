package nii.alloe.runs;
import nii.alloe.theory.*;
import nii.alloe.consist.*;
import java.io.*;

/**
 *
 * @author john
 */
public class Disagreement {
    
    /** Creates a new instance of Disagreement */
    public Disagreement() {
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream("disagreement.model"));
            Model probModel = (Model)ois.readObject();
            ois.close();
            
            Logic logic = new Logic(new File("logics/hypernym.logic"));
            ConsistSolver cs = new ConsistSolver();
            cs.solve(logic,probModel);
            cs.getMatrix().printMatrix(System.out);
            System.out.println(cs.soln.toString());
            GrowingSolver gs = new GrowingSolver(logic,probModel);
            gs.solve();
            gs.getMatrix().printMatrix(System.out);
            gs.soln.symmDiffAll(probModel);
            System.out.println(gs.soln);
        } catch(Exception x) {
            x.printStackTrace();
        }
    }
    
}
