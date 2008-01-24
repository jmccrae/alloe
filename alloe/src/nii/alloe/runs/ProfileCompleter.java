/*
 * ProfileCompletere.java
 *
 * Created on November 30, 2007, 7:39 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package nii.alloe.runs;
import nii.alloe.consist.ConsistProblem;
import nii.alloe.simulate.Simulate;

/**
 *
 * @author john
 */
public class ProfileCompleter {
    
    public static void main(String[] args) {
        System.out.println("Profiling completer");
        Simulate simulate;
        try {
            simulate = new Simulate("logics/sh.logic",0.7,0.7,100);
        } catch(java.io.IOException x) {
            x.printStackTrace();
            System.exit(-1);
            return;
        }
        long []times = new long[100];
        int i = 0;
        
        for(Double d = -2.0; d <= 0.0; d += 0.1) {
            simulate.l.relationDensity.put("r1", d);
            simulate.createModels();
            ConsistProblem prob = new ConsistProblem(simulate.l, simulate.probModel);
            times[i++] = prob.profileComplete();
        }
        
        for(i = 0; i < times.length; i++) {
            System.out.println(times[i]);
        }
    }
}
