/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nii.alloe.theory;
import java.util.*;

/**
 *
 * @author john
 */
public class FirstOrderEqGraph implements Graph {
    final RuleSymbol ruleSymbols;
    final TreeSet<Integer> links;
    
    public FirstOrderEqGraph(RuleSymbol ruleSymbols) {
        this.ruleSymbols = ruleSymbols;
        links = new TreeSet<Integer>();
    }
    
    public FirstOrderEqGraph(FirstOrderEqGraph foeg) {
        this.ruleSymbols = foeg.ruleSymbols;
        this.links = new TreeSet<Integer>(foeg.links);
    }

    public void add(int i, int j) {
        if(i < ruleSymbols.modelSize && j < ruleSymbols.modelSize) {
            throw new IllegalArgumentException("Cannot add to immutable section of FirstOrderEqGraph");
        }
        links.add(i * ruleSymbols.fullModelSize + j);
    }

    public Graph createCopy() {
        return new FirstOrderEqGraph(this);
    }

    public void dumpToDot(String dotFile) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isConnected(int i, int j) {
        if(i < ruleSymbols.modelSize && j < ruleSymbols.modelSize) {
            return i == j;
        } else {
            return links.contains(i * ruleSymbols.fullModelSize + j);
        }
    }

    public Iterator<Integer> iterator(int n) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int linkCount() {
        return links.size();
    }

    public boolean mutable(int i, int j) {
        return i >= ruleSymbols.modelSize || j >= ruleSymbols.modelSize;
    }

    public void remove(int i, int j) {
        if(i < ruleSymbols.modelSize && j < ruleSymbols.modelSize) {
            throw new IllegalArgumentException("Cannot add to immutable section of FirstOrderEqGraph");
        }
        links.add(i * ruleSymbols.fullModelSize + j);
    }
}
