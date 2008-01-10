package nii.alloe.theory;
import java.io.*;
import java.util.regex.*;
import java.util.*;

/** A logic on the graphs. This represents the set of conditions that the
 * relationships in the graph must satisfy. In effect it simply stores
 * all the rules, relations and named elements in the system
 *
 * @author John McCrae, National Institute of Informatics */

public class Logic {
    /** The set of rules that constitute this logic */
    public LinkedList<Rule> rules;
    /** Names of relations used in the rules */
    public TreeMap<String,String> relationNames;
    /** For each relation the expected density. 
     * If the value is  x and we have n elements we would expect to find n ^ x links
     * in a randomly generated graphs (although this may not be held for example 
     * symmetric graphs have at least n links). It is clear we have x <= 2.
     */
    public TreeMap<String,Double> relationDensity;
    /** Any elements named in the logic */
    public Vector<String> namedElements;
    /** The sets (if any) defined in the logic */
    public TreeMap<String,TreeSet<Integer> > sets;
    
    /** Load a logic from a file. <br>
     * Syntax of file: <br>
     * Rule definitions: see {@link Rule#loadRule(String)} <br>
     * Relation definitions are: <code>relationName = "printName" relationDensity</code> <br>
     * Set definitions are: <code>setName <- "elemName" [ , "elemName" ]* <br>
     */
    public Logic(String filename) {
        loadFile(filename);
    }
   
    private void loadFile(String filename) throws IllegalArgumentException {
        try {
            BufferedReader in = new BufferedReader(new FileReader(filename));
            relationNames = new TreeMap<String,String>();
            relationDensity = new TreeMap<String,Double>();
            namedElements = new Vector<String>();
            sets = new TreeMap<String,TreeSet<Integer> >();
            String str;
            rules = new LinkedList<Rule>();
            while((str = in.readLine()) != null) {
                if(str.matches(".*->.*")) {
                    Rule r = Rule.loadRule(str);
                    rules.addLast(r);
                    System.out.println(r.toString());
                } else if(str.matches(".*=.*")) {
                    readRelationDefinition(str);
                } else if(str.matches(".*<-.*")) {
                    readSetDefinition(str);
                } else if (!str.matches("^\\s*$") && !str.matches("^#.*")) {
                    throw new IllegalArgumentException("Unrecognised line:" + str);
                }
            }
        } catch(IOException x) {
            x.printStackTrace();
            System.exit(-1);
        }
    }
    
    private void readRelationDefinition(String str) {
        String []splits = str.split("=");
        if(splits.length != 2) {
            throw new IllegalArgumentException("Incorrect definition of relation name:" + str);
        }
        Matcher m = Pattern.compile("\\s*(\\w+)\\s*").matcher(splits[0]);
        if(!m.matches()) {
            throw new IllegalArgumentException("Incorrect relation name:" + splits[0]);
        }
        Matcher m2 = Pattern.compile("\\s*\\\"(.*)\\\"\\s*([-\\.0-9]*)\\s*").matcher(splits[1]);
        
        if(!m2.matches()) {
            throw new IllegalArgumentException("Incorrect relation name:" + splits[1]);
        }
        relationNames.put(m.group(1),
                m2.group(1).replaceAll("\\s",""));
        relationDensity.put(m.group(1),
                Double.valueOf(Double.parseDouble(m2.group(2))));
    }
    
    private void readSetDefinition(String str) {
        String []splits = str.split("<-");
        if(splits.length != 2) {
            throw new IllegalArgumentException("Incorrect definition of set:" + str);
        }
        Matcher m = Pattern.compile("\\s*(\\w+)\\s*").matcher(splits[0]);
        if(!m.matches()) {
            throw new IllegalArgumentException("Incorrect set name:" + splits[0]);
        }
        
        String []elements = splits[1].split(",");
        TreeSet<Integer> set = new TreeSet<Integer>();
        for(int i = 0; i < elements.length; i++) {
            Matcher m2 = Pattern.compile("\\s*\"(.*)\"\\s*").matcher(elements[i]);
            if(!m2.matches()) {
                throw new IllegalArgumentException("Incorrect element name in set: " + elements[i]);
            }
            String elementName = m2.group(1);
            int idx = namedElements.indexOf(elementName);
            if(idx >= 0) {
                set.add(idx);
            } else {
                namedElements.add(elementName);
                set.add(namedElements.size() - 1);
            }
        }
        
        sets.put(m.group(1), set);
    }
    
    /** Check the model is consistent, or more accurately search for inconsistencies.
     * When a inconsistency is found inconsist.doAction(...) is called, thus 
     * this function can (and does) do a number of different things */
    public void consistCheck(Model m, InconsistentAction inconsist) {
        boolean finished;
        do{
            finished = true;
            Iterator<Rule> i = rules.iterator();
            
            while(i.hasNext()) {
                finished = consistCheck(m, i.next(), 0, inconsist, false)
                && finished;
            }
        } while(!finished);
    }
    
    /** Check the model for any case where we can assign a rule so that its premise
     * is satisfied. We then called premiseFound.doAction(...), similarly to 
     * consistCheck, this function actually does several things */
    public void premiseSearch(Model m, InconsistentAction premiseFound) {
        boolean finished;
        do {
            finished = true;
            
            Iterator<Rule> i = rules.iterator();
            
            while(i.hasNext()) {
                finished = consistCheck(m, i.next(), 0, premiseFound, true)
                && finished;
            }
        } while(!finished);
    }
    
    /** The workhorse for both consistCheck and premiseCheck */
    protected boolean consistCheck(Model m,
            Rule rule,
            int argument,
            InconsistentAction inconsist,
            boolean premiseOnly) {
        if(argument == rule.length()) {
            if(inconsist != null)
                return inconsist.doAction(this, m, rule);
            else
                return rule.isRuleSatisfied(m);
        }
        Graph g = m.graphs.get(rule.relations.get(argument));
        boolean rval = true;
        
        if(rule.terms.get(argument)[0] == rule.terms.get(argument)[1] &&
                !rule.terms.get(argument)[0].hasAssignment()) {
            Iterator<Integer> i1 = m.elems.iterator();
            while(i1.hasNext()) {
                int i = i1.next();
                if((argument < rule.premiseCount && g.isConnected(i,i)) ||
                        (!premiseOnly && (argument >= rule.premiseCount && !g.isConnected(i,i)))) {
                    rule.terms.get(argument)[0].setAssignment(i);
                    
                    rval = consistCheck(m, rule,
                            argument + 1,inconsist,premiseOnly) && rval;
                    
                    rule.terms.get(argument)[0].unsetAssignment();
                }
            }
        } else if(!rule.terms.get(argument)[0].hasAssignment() &&
                !rule.terms.get(argument)[1].hasAssignment()) {
            Iterator<Integer> i1 = m.elems.iterator();
            while(i1.hasNext()) {
                int i = i1.next();
                Iterator<Integer> j2 = m.elems.iterator();
                while(j2.hasNext()) {
                    int j = j2.next();
                    if((argument < rule.premiseCount && g.isConnected(i,j)) ||
                            (!premiseOnly && (argument >= rule.premiseCount && !g.isConnected(i,j)))) {
                        rule.terms.get(argument)[0].setAssignment(i);
                        rule.terms.get(argument)[1].setAssignment(j);
                        
                        rval = consistCheck(m, rule,
                                argument + 1,inconsist,premiseOnly) && rval;
                        rule.terms.get(argument)[0].unsetAssignment();
                        rule.terms.get(argument)[1].unsetAssignment();
                    }
                }
            }
        } else if(!rule.terms.get(argument)[1].hasAssignment()) {
            int i = rule.terms.get(argument)[0].getAssignment();
            Iterator<Integer> j2 = m.elems.iterator();
            while(j2.hasNext()) {
                int j = j2.next();
                if((argument < rule.premiseCount && g.isConnected(i,j)) ||
                        (!premiseOnly && (argument >= rule.premiseCount && !g.isConnected(i,j)))) {
                    rule.terms.get(argument)[1].setAssignment(j);
                    
                    rval = consistCheck(m, rule, argument + 1,inconsist,premiseOnly) && rval;
                    rule.terms.get(argument)[1].unsetAssignment();
                }
            }
        } else if(!rule.terms.get(argument)[0].hasAssignment()) {
            int j = rule.terms.get(argument)[1].getAssignment();
            Iterator<Integer> i1 = m.elems.iterator();
            while(i1.hasNext()) {
                int i = i1.next();
                if((argument < rule.premiseCount && g.isConnected(i,j)) ||
                        (!premiseOnly && (argument >= rule.premiseCount && !g.isConnected(i,j)))) {
                    rule.terms.get(argument)[0].setAssignment(i);
                    
                    rval = consistCheck(m, rule, argument + 1,inconsist,premiseOnly) && rval;
                    
                    rule.terms.get(argument)[0].unsetAssignment();
                }
            }
        } else {
            rval = consistCheck(m, rule, argument + 1,inconsist,premiseOnly);
        }
        return rval;
    }
};