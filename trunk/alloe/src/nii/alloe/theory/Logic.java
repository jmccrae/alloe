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
    public Logic(File file) throws IOException, IllegalArgumentException {
        loadFile(new FileReader(file));
    }
    
    /** Create a logic file, from a definition
     * @see #Logic(File)
     */
    public Logic(String logic) throws IllegalArgumentException {
        try {
            loadFile(new StringReader(logic));
        } catch(IOException x) {
            x.printStackTrace();
            assert false;
        }
    }
    
    private void loadFile(Reader reader) throws IllegalArgumentException, IOException {
        BufferedReader in = new BufferedReader(reader);
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
    
    private interface CheckerCondition {
        public boolean check(int argument, Rule rule, Graph g, int i, int j);
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
                finished = consistCheck(m, i.next(), 0, inconsist, new CheckerCondition() {
                    public boolean check(int argument, Rule rule, Graph g, int i, int j) {
                        return (argument < rule.premiseCount && g.isConnected(i,j)) ||
                                (argument >= rule.premiseCount && !g.isConnected(i,j));
                    }
                })
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
                finished = consistCheck(m, i.next(), 0, premiseFound, new CheckerCondition() {
                    public boolean check(int argument, Rule rule, Graph g, int i, int j) {
                        return (argument < rule.premiseCount && g.isConnected(i,j)) ||
                                argument >= rule.premiseCount;
                    }
                })
                && finished;
            }
        } while(!finished);
    }
    
    /** The workhorse for both consistCheck and premiseCheck */
    protected boolean consistCheck(Model m,
            Rule rule,
            int argument,
            InconsistentAction inconsist,
            CheckerCondition checker) {
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
                if(checker.check(argument,rule,g,i,i)) {
                    rule.terms.get(argument)[0].setAssignment(i);
                    
                    rval = consistCheck(m, rule,
                            argument + 1,inconsist,checker) && rval;
                    
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
                    if(checker.check(argument,rule,g,i,j)) {
                        rule.terms.get(argument)[0].setAssignment(i);
                        rule.terms.get(argument)[1].setAssignment(j);
                        
                        rval = consistCheck(m, rule,
                                argument + 1,inconsist,checker) && rval;
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
                if(checker.check(argument,rule,g,i,j)) {
                    rule.terms.get(argument)[1].setAssignment(j);
                    
                    rval = consistCheck(m, rule, argument + 1,inconsist,checker) && rval;
                    rule.terms.get(argument)[1].unsetAssignment();
                }
            }
        } else if(!rule.terms.get(argument)[0].hasAssignment()) {
            int j = rule.terms.get(argument)[1].getAssignment();
            Iterator<Integer> i1 = m.elems.iterator();
            while(i1.hasNext()) {
                int i = i1.next();
                if(checker.check(argument,rule,g,i,j)) {
                    rule.terms.get(argument)[0].setAssignment(i);
                    
                    rval = consistCheck(m, rule, argument + 1,inconsist,checker) && rval;
                    
                    rule.terms.get(argument)[0].unsetAssignment();
                }
            }
        } else {
            rval = consistCheck(m, rule, argument + 1,inconsist,checker);
        }
        return rval;
    }
    
    /** This function can also be used to save the logic */
    public String toString() {
        String rval = "";
        Iterator<Map.Entry<String,String>> relIter = relationNames.entrySet().iterator();
        while(relIter.hasNext()) {
            Map.Entry<String,String> entry = relIter.next();
            rval = rval + entry.getKey() + " = \"" + entry.getValue() + "\" " +
                    relationDensity.get(entry.getKey()) + "\n";
        }
        Iterator<Rule> ruleIter = rules.iterator();
        while(ruleIter.hasNext()) {
            rval = rval + ruleIter.next().toString() + "\n";
        }
        Iterator<Map.Entry<String,TreeSet<Integer>>> setiter = sets.entrySet().iterator();
        while(setiter.hasNext()) {
            Map.Entry<String,TreeSet<Integer>> entry = setiter.next();
            rval = rval + entry.getKey() + " <- ";
            Iterator<Integer> siter = entry.getValue().iterator();
            while(siter.hasNext()) {
                rval = rval + "\"" + namedElements.get(siter.next()) + "\"";
                if(siter.hasNext())
                    rval = rval + ", ";
            }
            rval = rval + "\n";
        }
        
        return rval;
    }
    
    public Model getCompulsoryModel(Model m) {
        Model model = m.createImmutableCopy();
        Iterator<Rule> ruleIter = rules.iterator();
        while(ruleIter.hasNext()) {
            Rule rule = ruleIter.next();
            if(rule.premiseCount != rule.length() -1)
                continue; // Multiple conclusions
            if(rule.terms.get(rule.premiseCount)[0] instanceof Rule.FunctionArgument ||
                    rule.terms.get(rule.premiseCount)[1] instanceof Rule.FunctionArgument) {
                continue; // Functional arguments
            }
            consistCheck(model, rule,0,new InconsistentAction() {
                public boolean doAction(Logic logic, Model m, Rule rule) {
                    m.add(m.id(rule.relations.get(rule.premiseCount),
                            rule.terms.get(rule.premiseCount)[0].getAssignment(),
                            rule.terms.get(rule.premiseCount)[1].getAssignment()));
                    return true;
                }
            }, new CheckerCondition() {
                public boolean check(int argument, Rule r, Graph g, int i, int j) {
                    return (argument < r.premiseCount && !g.mutable(i,j)) || argument >= r.premiseCount;
                }
            });
        }
        premiseSearch(model,new InconsistentAction() {
            public boolean doAction(Logic logic, Model m, Rule rule) {
                if(rule.premiseCount != rule.length() -1)
                    return true;
                if(rule.terms.get(rule.premiseCount)[0] instanceof Rule.FunctionArgument ||
                        rule.terms.get(rule.premiseCount)[1] instanceof Rule.FunctionArgument) {
                    return true;
                }
                return m.add(m.id(rule.relations.get(rule.premiseCount),
                        rule.terms.get(rule.premiseCount)[0].getAssignment(),
                        rule.terms.get(rule.premiseCount)[1].getAssignment()));
            }
        });
        return model;
    }
    
    private class NegativeModelAction implements InconsistentAction {
        List<Integer> rv;
        public NegativeModelAction(List<Integer> rv) {
            this.rv = rv;
        }
        public boolean doAction(Logic logic, Model m, Rule rule) {
            for(int i = rule.premiseCount; i < rule.length(); i++) {
                if(m.mutable(m.id(rule.relations.get(i),
                        rule.terms.get(i)[0].getAssignment(),
                        rule.terms.get(i)[1].getAssignment())))
                    return true;
            }
            int id = m.id(rule.relations.get(0),
                    rule.terms.get(0)[0].getAssignment(),
                    rule.terms.get(0)[1].getAssignment());
            if(!m.mutable(id))
                throw new LogicException("Immutable assignment (" + id + ") is impossible!");
            rv.add(id);
            return true;
        }
    }
    
    public List<Integer> getNegativeModel(Model m) {
        Model model = m.createImmutableCopy();
        List<Integer> rv = new LinkedList<Integer>();
        Iterator<Rule> ruleIter = rules.iterator();
        while(ruleIter.hasNext()) {
            Rule rule = ruleIter.next();
            if(rule.premiseCount == 1) {
                consistCheck(model,rule,0, new NegativeModelAction(rv), new CheckerCondition() {
                    public boolean check(int argument, Rule r, Graph g, int i, int j) {
                        return argument < r.premiseCount || !g.mutable(i,j);
                    }
                });
            }
        }
        return rv;
    }
}