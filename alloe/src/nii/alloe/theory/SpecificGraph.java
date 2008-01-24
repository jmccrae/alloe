package nii.alloe.theory;

import java.util.*;
import java.io.*;

/**
 * A graph with no weights on each link.
 *
 * @see Graph
 */

public class SpecificGraph implements Graph, Serializable {
    public String relation;
    private TreeSet<Integer> links;
    private static int n;
    
    SpecificGraph(int n, String relation) {
        this.n = n;
        this.relation = relation;
        links = new TreeSet<Integer>();
    }
    
    public void makeRandom(double prob) {
        Random r = new Random();
        
        for(int i = 0; i < n * n; i++) {
            if(r.nextDouble() <= prob) {
                links.add(i);
            } else {
                links.remove(i);
            }
        }
    }
    
    public boolean mutable(int i, int j) {
            return true;
    }
    
    public boolean isConnected(int i, int j) {
        if(i >= 0 && j >= 0) {
            return links.contains(i * n + j);
        } else if(i >= 0) {
            for(j = 0; j < n; j++) {
                if(links.contains(i * n + j))
                    return true;
            }
            return false;
        } else if(j >= 0) {
            for(i = 0; i < n; i++) {
                if(links.contains(i * n + j))
                    return true;
            }
            return false;
        } else {
            return linkCount() > 0;
        }
    }
    
    public void add(int i, int j) {
        //System.out.println("Adding: " + i + " -> " + j);
        if(i >= 0 && j >= 0) {
            links.add(i * n + j);
        } else if(i >= 0) {
            for(j = 0; j < n; j++) {
                links.add(i * n + j);
            }
        } else if(j >= 0) {
            for(i = 0; i < n; i++) {
                links.add(i * n + j);
            }
        } else {
            for(i = 0; i < n; i++) {
                for(j = 0; j < n; j++) {
                    links.add(i * n + j);
                }
            }
        }

    }
    
    public void remove(int i, int j) {
        //System.out.println("Removing: " + i + " -> " + j);
        if(i >= 0 && j >= 0) {
            links.remove(i * n + j);
        } else if(i >= 0) {
            for(j = 0; j < n; j++) {
                links.remove(i * n + j);
            }
        } else if(j >= 0) {
            for(i = 0; i < n; i++) {
                links.remove(i * n + j);
            }
        } else {
            for(i = 0; i < n; i++) {
                for(j = 0; j < n; j++) {
                    links.remove(i * n + j);
                }
            }
        }

    }
    
    public int linkCount() {
        return links.size();
    }
    
    public void dumpToDot(String dotFile) {
        try {
            PrintStream dot = new PrintStream(new FileOutputStream(dotFile));
            dot.println("digraph G {");
            for(int i = 0; i < n; i++) {
                for(int j = 0; j < n; j++) {
                    if(i != j && isConnected(i,j)) {
                        dot.println("\tn"+i+" -> n"+j+";");
                    }
                }
            }
            dot.println("}");
            dot.close();
        } catch(IOException x) {
            x.printStackTrace();
            System.exit(0);
        }
    }

    
    public Iterator<Integer> iterator(int n) {
        if(this.n != n)
            throw new IllegalArgumentException();
        else
            return links.iterator();
    }
};