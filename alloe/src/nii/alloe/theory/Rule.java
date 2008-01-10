package nii.alloe.theory;
import java.util.regex.*;
import java.util.*;

/**
 * A rule in the logic.
 * 
 * This class encapsulates a rule in the logic system, these are stated of
 * the form: P1 n ... n PN -> C1 v ... v CM . For this rule to be satisfied
 * it requires that either one of the premises is not true, or one of the
 * conclusions is true. This form is normally referred to as Conjunctive
 * Normal Form. We allow statements to have two kinds of variables, standard
 * variables and functional variables. Standard variables must be satisfied first
 * and then a search is done for functional variables and a term is considered
 * to be false if it involves a functional variable which could not be found.
 * In first-order logic a standard variable x in a rule r(x) is equivalent
 * to FORALL x : r(x); a functional variable x() in a rule r(x) is equivalent
 * to EXISTS x() : r(x(y1,...,yn)) where y1,...,yn are the standard variables in
 * r(x). NB Constants are not yet supported but will be soon (hopefully)
 */
public class Rule implements Comparable<Rule> {
    public Vector<String> relations;
    public Vector<Argument[]> terms;
    public int premiseCount;
    public TreeMap<Argument,Argument> arguments;
    
    public Integer score, maxScore;
    
    private Rule() { arguments = new TreeMap<Argument,Argument>(); }
    
    /**
     * @return length (number of statements) of this rule
     */
    public int length() { return relations.size(); }
    
    /**
     * Loads a rule from a line of text. Each term is of the form r(n,m) where n
     * and m are integers and r is a defined relation. The rule then has the form
     * <code> term? [ ; term ] * -> term? [ ; term ]*</code>. The relation name
     * should be a defined relation or 'e' (representing the equivalence relation) or
     * 'in_set' where 'set' is a defined set.
     * @return The new Rule object based on the line
     */
    static public Rule loadRule(String rule) throws IllegalArgumentException {
        Rule r = new Rule();
        r.loadFromString(rule);
        return r;
    }
    
    private void loadFromString(String rule) throws IllegalArgumentException {
        String []pc = rule.split("->",-1);
        if(pc.length != 2) {
            throw new IllegalArgumentException(
                    "Unexpected number of splits in rule: " + rule);
        }
        String p = "",c = "";
        //if(pc.length == 2) {
        p = pc[0];
        c = pc[1];
        //} else if(pc.length == 1) {
        //if(rule.matches(".*\\S+.*->.*")) {
        //	p = pc[0];
        //	c = null;
        //} else if(rule.matches(".*->.*\\S+.*")) {
        //	p = null;
        //	c = pc[0];
        //} else {
        //	throw new IllegalArgumentException("Couldn't split:" + rule);
        //}
        //}
        if(p.matches("\\s*")) p = null;
        if(c.matches("\\s*")) c = null;
        
        String []ps = (p == null ? null : p.split(";"));
        String []cs = (c == null ? null : c.split(";"));
        
        premiseCount = (p == null ? 0 : ps.length);
        int conclusionCount = (c == null ? 0 : cs.length);
        String []ss = new String[premiseCount + conclusionCount];
        if(p != null) {
            System.arraycopy(ps,0,ss,0,ps.length);
        }
        if(c != null) {
            System.arraycopy(cs,0,ss,(p == null ? 0 : ps.length), cs.length);
        }
        
        
        relations = new Vector<String>(premiseCount + conclusionCount);
        terms = new Vector<Argument[]>(premiseCount + conclusionCount);
        
        Pattern pat1 = Pattern.compile("\\s*(\\w+)\\s*\\(\\s*(\\d+\\(?\\)?)\\s*,\\s*(\\d+\\(?\\)?)\\s*\\)\\s*");
        Pattern pat2 = Pattern.compile("\\s*(in_\\d+)\\s*\\(\\s*(\\d+\\(?\\)?)\\s*\\)\\s*");
        
        for(int i = 0; i < premiseCount + conclusionCount; i++) {
            Matcher m1 = pat1.matcher(ss[i]);
            Matcher m2 = pat2.matcher(ss[i]);
            if(ss[i].matches("^\\s*$")) {
                continue;
            } else if(m1.matches()) {
                relations.add(m1.group(1));
                Argument []term = new Argument[2];
                term[0] = makeArgument(m1.group(2));
                term[1] = makeArgument(m1.group(3));
                terms.add(term);
            } else if(m2.matches()) {
                relations.add(m2.group(1));
                Argument[] term = new Argument[2];
                term[0] = term[1] = makeArgument(m2.group(2));
                terms.add(term);
            } else {
                throw new IllegalArgumentException("Unrecognised statement: " + ss[i]);
            }
        }
    }
    
    public String toString() {
        String rval = "";
        for(int i = 0; i < length(); i++) {
            if(i == premiseCount) {
                rval = rval + "-> ";
            }
            rval = rval + relations.get(i) + "(" + terms.get(i)[0].toString() + "," +
                    terms.get(i)[1].toString() + "); ";
        }
        return rval;
    }
    
    /**
     * Removes assignments to all functions
     */
    public void removeFunctionAssignments() {
        Iterator<Argument> i = arguments.keySet().iterator();
        while(i.hasNext()) {
            Argument arg = i.next();
            if(arg instanceof FunctionArgument) {
                ((FunctionArgument)arg).setFunctionAssignment(-1);
            }
        }
    }
    
    
    /**
     * This functions attempts to find an assignment which satisfies all
     * "functions" in this Rule.
     *
     * The sketch of this algorithm is for each function in this Rule, we
     * attempt to find an assignment that matched the first statement this is
     * involved in, then we check this is valid with all other statement in
     * the rule involving this function
     *
     * @param m A mapping from the graph names to valid graphs
     * @return if a function assignment fails, true if this was in premise, false otherwise. Returns true is all function assignments successful.
     */
    public boolean addFunctionAssignments(Model m) {
        Iterator<Argument> i = arguments.keySet().iterator();
        return afaIterator(i, m);
    }
    
    private boolean afaIterator(Iterator<Argument> iter,
            Model m) {
        
        if(!iter.hasNext()) {
            return true;
        }
        Argument arg = iter.next();
        
        if(!(arg instanceof FunctionArgument)) {
            return afaIterator(iter,m);
        }
        
        List<Integer> stats = statementsForArgument(arg);
        return afaIterator2(stats.iterator(), (FunctionArgument)arg, iter, m);
    }
    
    private boolean afaIterator2(Iterator<Integer> stats,
            FunctionArgument arg,
            Iterator<Argument> iter,
            Model m) {
        
        if(stats.hasNext() && arg.getAssignment() == -1) {
            int i = stats.next().intValue();
            int sn;
            if(terms.get(i)[0].compareTo(arg) == 0) {
                sn = 0;
            } else {
                sn = 1;
            }
            Graph g = m.graphs.get(relations.get(i));
            for(int j = 0; j < length(); j++) {
                if((terms.get(i)[0].compareTo(arg) == 0 &&
                        g.isConnected(j,terms.get(i)[1].getAssignment())) ||
                        (terms.get(i)[1].compareTo(arg) == 0 &&
                        g.isConnected(terms.get(i)[1].getAssignment(),j))) {
                    arg.setFunctionAssignment(j);
                    if(afaIterator2(stats, arg, iter, m)) {
                        return i >= premiseCount;
                    } else {
                        arg.setFunctionAssignment(-1);
                    }
                }
            }
            return false;
        } else if(arg.getAssignment() >= 0) {
            int i = stats.next().intValue();
            Graph g = m.graphs.get(relations.get(i));
            if(!g.isConnected(terms.get(i)[0].getAssignment(),
                    terms.get(i)[1].getAssignment()))
                return false;
            else
                return afaIterator2(stats,arg,iter,m);
        } else {
            return afaIterator(iter,m);
        }
    }
    
    /*
     * @param arg An argument
     * @return a list of the arguments that arg is involved in
     */
    public LinkedList<Integer> statementsForArgument(Argument arg) {
        LinkedList<Integer> rval = new LinkedList<Integer>();
        
        for(int i = 0; i < length(); i++) {
            if(terms.get(i)[0].compareTo(arg) == 0 ||
                    terms.get(i)[1].compareTo(arg) == 0) {
                rval.add(new Integer(i));
            }
        }
        
        return rval;
    }
    
    /**
     * Checks if this rule is satisfied in a given context
     * This function must be called after all non-function assignments
     * have been made (!)
     * @param m A mapping from Graph names to graphs
     * @return true if the rule is satisfied
     */
    public boolean isRuleSatisfied(Model m) {
        addFunctionAssignments(m);
        for(int i = 0; i < length(); i++) {
            Graph g = m.graphs.get(relations.get(i));
            int v1 = terms.get(i)[0].getAssignment();
            int v2 = terms.get(i)[1].getAssignment();
            
            if((i < premiseCount && (v1 == -1 || v2 == -1 || !g.isConnected(v1,v2))) ||
                    (i >= premiseCount && v1 >= 0 && v2 >= 0 && g.isConnected(v1,v2))) {
                removeFunctionAssignments();
                return true;
            }
        }
        removeFunctionAssignments();
        return false;
    }
    
    
    private Argument makeArgument(String s) {
        Argument rval;
        if(s.matches("\\d+")) {
            rval = new Argument(Integer.parseInt(s));
        } else if(s.matches("\\d+\\(\\)")) {
            Matcher m = Pattern.compile("(\\d+)\\(\\)").matcher(s);
            
            m.matches();
            
            rval = new FunctionArgument(Integer.parseInt(m.group(1)));
        } else {
            throw new IllegalArgumentException("Couldn't read argument: " + s);
        }
        
        if(arguments.get(rval) == null) {
            arguments.put(rval,rval);
            return rval;
        } else {
            Argument arg = arguments.get(rval);
            if(((rval instanceof FunctionArgument) &&
                    !(arg instanceof FunctionArgument)) ||
                    (!(rval instanceof FunctionArgument) &&
                    (arg instanceof FunctionArgument))) {
                throw new IllegalArgumentException("Argument " + arg.id + " is defined as both functional and non-functional in this rule!");
            }
            return arg;
        }
    }
    
    /**
     * @return a list of the functional arguments in this rule
     */
    public LinkedList<FunctionArgument> getFunctionalArguments() {
        Iterator<Argument> i = arguments.keySet().iterator();
        LinkedList<FunctionArgument> rval = new LinkedList<FunctionArgument>();
        while(i.hasNext()) {
            Argument arg = i.next();
            if(arg instanceof FunctionArgument) {
                rval.add((FunctionArgument)arg);
            }
        }
        return rval;
    }
    
    /**
     * clone this rule
     */
    public Rule createCopy() {
        Rule r = new Rule();
        r.premiseCount = premiseCount;
        r.arguments = new TreeMap<Argument, Argument>();
        r.relations = (Vector<String>)relations.clone();
        r.terms = new Vector<Argument[]>();
        
        for(int i = 0; i < length(); i++) {
            r.terms.add(cloneArgPair(terms.get(i)));
            r.arguments.put(r.terms.get(i)[0], r.terms.get(i)[0]);
            r.arguments.put(r.terms.get(i)[0], r.terms.get(i)[0]);
        }
        
        return r;
    }
    
    public Rule resolve(Rule rule, Model model) {
        Rule newRule = new Rule();
        newRule.terms = new Vector<Argument[]>(length() + rule.length());
        newRule.relations = new Vector<String>(length() + rule.length());
        newRule.premiseCount = premiseCount + rule.premiseCount;
        
        for(int i = 0; i < premiseCount; i++) {
            newRule.terms.add(cloneArgPair(terms.get(i)));
            newRule.relations.add(relations.get(i));
        }
        for(int i = 0; i < rule.premiseCount; i++) {
            newRule.terms.add(cloneArgPair(rule.terms.get(i)));
            newRule.relations.add(rule.relations.get(i));
        }
        for(int i = premiseCount; i < length(); i++) {
            newRule.terms.add(cloneArgPair(terms.get(i)));
            newRule.relations.add(relations.get(i));
        }
        for(int i = rule.premiseCount; i < rule.length(); i++) {
            newRule.terms.add(cloneArgPair(rule.terms.get(i)));
            newRule.relations.add(rule.relations.get(i));
        }
        
        
        for(int i = 0; i < premiseCount; i++) {
            for(int k = length() + rule.premiseCount; k < newRule.length(); k++) {
                if(newRule.relations.get(i).equals(newRule.relations.get(k)) &&
                        newRule.terms.get(i)[0].getAssignment() == newRule.terms.get(k)[0].getAssignment() &&
                        newRule.terms.get(i)[1].getAssignment() == newRule.terms.get(k)[1].getAssignment()) {
                    newRule.relations.remove(i);
                    newRule.relations.remove(k-1);
                    newRule.terms.remove(i);
                    newRule.terms.remove(k-1);
                    newRule.premiseCount--;
                    i--;
                    break;
                }
            }
        }
        
        newRule.simplify(model);
        
        return newRule;
    }
    
    private static Argument[] cloneArgPair(Argument[] args) {
        Argument[] rval = new Argument[2];
        rval[0] = args[0].createCopy();
        rval[1] = args[1].createCopy();
        return rval;
    }
    
    /**
     * Simplifies this rule by removing terms which due to assignment have been duplicated
     * and detects is the rule is trivial (in which case all terms are removed
     * NB do not remove or change assignments after applying this rule!!
     */
    public void simplify(Model model) {
        
        // Check for resolution
        for(int i = 0; i < premiseCount; i++) {
            for(int k = premiseCount; k < terms.size(); k++) {
                if(relations.get(i).equals(relations.get(k)) &&
                        terms.get(i)[0].getAssignment() == terms.get(k)[0].getAssignment() &&
                        terms.get(i)[1].getAssignment() == terms.get(k)[1].getAssignment()) {
                    premiseCount = 0;
                    relations.clear();
                    terms.clear();
                    arguments.clear();
                    return;
                }
            }
        }
        
        // Check for duplication
        for(int i = 0; i < premiseCount; i++) {
            if(model != null) {
                Graph g = model.graphs.get(relations.get(i));
                if(!g.mutable(terms.get(i)[0].getAssignment(),
                        terms.get(i)[1].getAssignment())) {
                    if(g.isConnected(terms.get(i)[0].getAssignment(),
                            terms.get(i)[1].getAssignment())) {
                        relations.remove(i);
                        terms.remove(i);
                        premiseCount--;
                        i--;
                        continue;
                    } else {
                        premiseCount = 0;
                        relations.clear();
                        terms.clear();
                        arguments.clear();
                        return;
                    }
                }
            }
            for(int k = i+1; k < premiseCount; k++) {
                if(relations.get(i).equals(relations.get(k)) &&
                        terms.get(i)[0].getAssignment() == terms.get(k)[0].getAssignment() &&
                        terms.get(i)[1].getAssignment() == terms.get(k)[1].getAssignment()) {
                    relations.remove(k);
                    terms.remove(k);
                    premiseCount--;
                    k--;
                }
            }
        }
        for(int i = premiseCount; i < terms.size(); i++) {
            if(model != null) {
                Graph g = model.graphs.get(relations.get(i));
                if(!g.mutable(terms.get(i)[0].getAssignment(),
                        terms.get(i)[1].getAssignment())) {
                    if(!g.isConnected(terms.get(i)[0].getAssignment(),
                            terms.get(i)[1].getAssignment())) {
                        relations.remove(i);
                        terms.remove(i);
                        i--;
                        continue;
                    } else {
                        premiseCount = 0;
                        relations.clear();
                        terms.clear();
                        arguments.clear();
                        return;
                    }
                }
            }
            for(int k = i+1; k < terms.size(); k++) {
                if(relations.get(i).equals(relations.get(k)) &&
                        terms.get(i)[0].getAssignment() == terms.get(k)[0].getAssignment() &&
                        terms.get(i)[1].getAssignment() == terms.get(k)[1].getAssignment()) {
                    relations.remove(k);
                    terms.remove(k);
                    k--;
                }
            }
        }
        
        
        // Rebuild Arguments
        arguments = new TreeMap<Argument, Argument>();
        addArguments();
    }
    
    private void addArguments() {
        for(int i = 0; i < length(); i++) {
            arguments.put(terms.get(i)[0], terms.get(i)[0]);
            arguments.put(terms.get(i)[1], terms.get(i)[1]);
        }
    }
    
    /**
     * Check for resolution, note this is not a symmetric it checks if there is a resolution between this's premise and r's conclusion
     */
    public boolean canResolveWith(Rule r) {
        for(int i = 0; i < premiseCount; i++) {
            for(int k = r.premiseCount; k < r.length(); k++) {
                if(relations.get(i).equals(r.relations.get(k)) &&
                        terms.get(i)[0].getAssignment() == r.terms.get(k)[0].getAssignment() &&
                        terms.get(i)[1].getAssignment() == r.terms.get(k)[1].getAssignment() ) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * A rule subsumes another this rule if in any case that rule is true, this rule is also true. This means that every
     * term in this rule can be found in the other rule
     */
    public boolean subsumes(Rule r, Model m) {
        THIS_LOOP: for(int i = 0; i < premiseCount; i++) {
            for(int j = 0; j < r.premiseCount; j++) {
                if(relations.get(i).equals(r.relations.get(j)) &&
                        terms.get(i)[0].getAssignment() == r.terms.get(j)[0].getAssignment() &&
                        terms.get(i)[1].getAssignment() == r.terms.get(j)[1].getAssignment()) {
                    continue THIS_LOOP;
                }
            }
            return false;
        }
        
        THIS_LOOP2: for(int i = premiseCount; i < length(); i++) {
            if(m != null) {
                Graph g = m.graphs.get(relations.get(i));
                if(g.isConnected(terms.get(i)[0].getAssignment(),
                        terms.get(i)[1].getAssignment()) ||
                        !g.mutable(terms.get(i)[0].getAssignment(),
                        terms.get(i)[1].getAssignment())) {
                    continue;
                }
            }
            for(int j = r.premiseCount; j < r.length(); j++) {
                if(relations.get(i).equals(r.relations.get(j)) &&
                        terms.get(i)[0].getAssignment() == r.terms.get(j)[0].getAssignment() &&
                        terms.get(i)[1].getAssignment() == r.terms.get(j)[1].getAssignment()) {
                    continue THIS_LOOP2;
                }
            }
            return false;
        }
        return true;
    }
    
    public int compareTo(Rule r) {
        if(r == this)
            return 0;
        
        if(premiseCount != r.premiseCount) {
            return premiseCount < r.premiseCount ? -1 : 1;
        }
        if(length() != r.length()) {
            return length() < r.length() ? -1 : 1;
        }
        for(int i = 0; i < length(); i++) {
            if(!relations.get(i).equals(r.relations.get(i))) {
                return relations.get(i).compareTo(r.relations.get(i));
            }
            if(terms.get(i)[0].compareTo(r.terms.get(i)[0]) != 0) {
                return terms.get(i)[0].compareTo(r.terms.get(i)[0]);
            }
            if(terms.get(i)[0].getAssignment() != r.terms.get(i)[0].getAssignment()) {
                return terms.get(i)[0].getAssignment() < r.terms.get(i)[0].getAssignment() ? -1 : 1;
            }
            if(terms.get(i)[1].compareTo(r.terms.get(i)[1]) != 0) {
                return terms.get(i)[1].compareTo(r.terms.get(i)[1]);
            }
            if(terms.get(i)[1].getAssignment() != r.terms.get(i)[1].getAssignment()) {
                return terms.get(i)[1].getAssignment() < r.terms.get(i)[1].getAssignment() ? -1 : 1;
            }
        }
        
        return 0;
    }
    
    public boolean equals(Object obj) {
        if(obj instanceof Rule)
            return compareTo((Rule)obj) == 0;
        else
            return false;
    }
    
    
    /**
     * returns true if the conclusion must be satisfied ie. the conclusion contains a satisfied immutable
     */
    public boolean conclusionMustBeSatisified(Model model) {
        for(int i = premiseCount; i < length(); i++) {
            Graph g = model.graphs.get(relations.get(i));
            if(!g.mutable(terms.get(i)[0].getAssignment(),
                    terms.get(i)[1].getAssignment()) &&
                    g.isConnected(terms.get(i)[0].getAssignment(),
                    terms.get(i)[1].getAssignment())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Same as subsumes where testTerms is the representation of this rule in the model
     */
    public boolean conclusionContains(TreeSet<Integer> testTerms, Model model) {
        for(int i = premiseCount; i < length(); i++) {
            if(testTerms.contains(model.id(relations.get(i),
                    terms.get(i)[0].getAssignment(),
                    terms.get(i)[1].getAssignment()))) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Are all the premises and all the conclusions true in a specific model
     */
    public boolean completelyTrueIn(Model model) {
        for(int i = 0; i < length(); i++)
            if(!model.isConnected(model.id(this,i)))
                return false;
        return true;
    }
    
    /**
     * This rule can be used to simplify other rules, ie the conclusion is smaller than the premise
     */
    public boolean isReductive() {
        return premiseCount > (length() - premiseCount);
    }
    
    /**
     * Remove a term
     */
    public void removeTerm(Integer term) {
        relations.remove(term);
        terms.remove(term);
        
        // Rebuild Arguments
        arguments = new TreeMap<Argument, Argument>();
        addArguments();
    }
    
    /**
     * Remove a lit of terms
     */
    public void removeTerms(List<Integer> terms) {
        if(terms.isEmpty())
            return;
        Collections.sort(terms);
        Iterator<Integer> iter = terms.iterator();
        Integer last = iter.next();
        relations.remove(last);
        terms.remove(last);
        while(iter.hasNext()) {
            Integer i = iter.next();
            if(!last.equals(i)) {
                relations.remove(i);
                terms.remove(i);
            }
        }
        // Rebuild Arguments
        arguments = new TreeMap<Argument, Argument>();
        addArguments();
    }
    
    /**
     * Add a term
     */
    public void addTerm(String rel, Argument[] term, boolean premise) {
        if(premise) {
            relations.insertElementAt(rel, premiseCount);
            terms.insertElementAt(term,premiseCount);
            premiseCount++;
        } else {
            relations.add(rel);
            terms.add(term);
        }
        arguments.put(term[0],term[0]);
        arguments.put(term[1],term[1]);
    }
    
    /**
     * Set this rule to be identical (not merely a copy) of another rule
     */
    public void setRule(Rule r) {
        arguments = r.arguments;
        maxScore = r.maxScore;
        premiseCount = r.premiseCount;
        relations = r.relations;
        score = r.score;
        terms = r.terms;
    }
    
    /**
     * same as forAllAssignment(model,true,action)
     */
    public boolean forAllAssignments(Model model, AssignmentAction action) {
        return forAllAssignments(model,true,action);
    }
    
    /**
     * find all assignments to this rule and perform action
     */
    public boolean forAllAssignments(Model model, boolean validate, AssignmentAction action) {
        boolean rval = true;
        for(int i = 0; i < length(); i++) {
            int v1 = terms.get(i)[0].getAssignment();
            int v2 = terms.get(i)[1].getAssignment();
            
            if(v1 == -1 && v2 == -1) {
                Iterator<Integer> i1 = model.elems.iterator();
                while(i1.hasNext()) {
                    int j = i1.next();
                    Iterator<Integer> i2 = model.elems.iterator();
                    while(i2.hasNext()) {
                        int k = i2.next();
                        Graph g = model.graphs.get(relations.get(i));
                        if(!validate || (i < premiseCount && g.isConnected(j,k)) ||
                                (i >= premiseCount && !g.isConnected(j,k))) {
                            rval = rval && action.action(this, i,j,k);
                        }
                    }
                }
            } else if(v1 == -1) {
                Iterator<Integer> i1 = model.elems.iterator();
                while(i1.hasNext()) {
                    int j = i1.next();Graph g = model.graphs.get(relations.get(i));
                    if(!validate || (i < premiseCount && g.isConnected(j,v2)) ||
                            (i >= premiseCount && !g.isConnected(j,v2))) {
                        rval = rval && action.action(this,i,j,v2);
                    }
                }
            } else if(v2 == -1) {
                Iterator<Integer> i2 = model.elems.iterator();
                while(i2.hasNext()) {
                    int k = i2.next();Graph g = model.graphs.get(relations.get(i));
                    if(!validate || (i < premiseCount && g.isConnected(v1,k)) ||
                            (i >= premiseCount && !g.isConnected(v1,k))) {
                        rval = rval && action.action(this,i,v1,k);
                    }
                }
            } else {
                Graph g = model.graphs.get(relations.get(i));
                if(!validate || (i < premiseCount && g.isConnected(v1,v2)) ||
                        (i >= premiseCount && !g.isConnected(v1,v2))) {
                    rval = rval && action.action(this,i,v1,v2);
                }
            }
        }
        return rval;
    }
    
    /**
     * Limit this rule to the model, so the rule only contains terms with true assignments in the model.
     * Conclusions can be discarded freely but if a premise is not in the model this rule is not applicable
     * to that model so we set the rule to nothing
     */
    public void limitToModel(Model model) {
        boolean changed = false;
        for(int i = 0; i < length(); i++) {
            if(!model.isConnected(model.id(this,i))) {
                if(i < premiseCount) {
                    premiseCount = 0;
                    relations.clear();
                    terms.clear();
                    arguments.clear();
                    return;
                } else {
                    relations.remove(i);
                    terms.remove(i);
                    i--;
                    changed = true;
                }
            }
        }
        if(changed) {
            arguments.clear();
            addArguments();
        }
    }
    
    /**
     * Replace every functional assignment with a disjunction for ever element in elems
     * eg r1(1(),2=2) ->    and elems = (1,2,3) becomes
     *  r1(1=1,2=2) ; r1(1=2; 2=2) ; r1(1=3,2=3)
     */
    public void multiplexFunctions(AbstractCollection<Integer> elems) {
        boolean changed = false;
        for(int i = 0; i < length(); i++) {
            if(terms.get(i)[0].getAssignment() == -1) {
                Iterator<Integer> eiter = elems.iterator();
                int j = i;
                while(eiter.hasNext()) {
                    Integer elem = eiter.next();
                    Argument []newTerms = new Argument[2];
                    
                    newTerms[0] = new Argument(terms.get(i)[0].id);
                    newTerms[0].setAssignment(elem);
                    newTerms[1] = terms.get(i)[1];
                    terms.insertElementAt(newTerms,j);
                    relations.insertElementAt(relations.get(i),j++);
                }
                if(i < premiseCount)
                    premiseCount += elems.size();
                changed = true;
                i--;
            } else if(terms.get(i)[1].getAssignment() == -1) {
                Iterator<Integer> eiter = elems.iterator();
                int j = i;
                while(eiter.hasNext()) {
                    Integer elem = eiter.next();
                    Argument []newTerms = new Argument[2];
                    
                    newTerms[1] = new Argument(terms.get(i)[1].id);
                    newTerms[1].setAssignment(elem);
                    newTerms[0] = terms.get(i)[0];
                    terms.insertElementAt(newTerms,j);
                    relations.insertElementAt(relations.get(i),j++);
                }
                if(i < premiseCount)
                    premiseCount += elems.size();
                changed = true;
                i--;
            }
        }
        if(changed) {
            arguments.clear();
            addArguments();
        }
    }
    
    /**
     * Check the rule is not in some inconsistent state
     */
    public boolean isOK() {
        if(arguments.size() != terms.size() || premiseCount < 0 || premiseCount > length()) 
            return false;
        
        Iterator<Argument[]> aiter = terms.iterator();
        while(aiter.hasNext()) {
            Argument[] args = aiter.next();
            if(args.length != 2)
                return false;
            if(!arguments.containsKey(args[0]))
                return false;
            if(!arguments.containsKey(args[1]))
                return false;
        }
        return true;
    }
    
    /**
     * Argument this class is used for standard variables
     * it should not be instantiated directly but instead
     * constructed through Rule.loadRule()
     */
    public class Argument implements Comparable<Argument> {
        int id;
        // -1 means no assignment
        protected int assignment;
        
        public Argument(int id) { this.id = id; assignment = -1; }
        protected Argument() {}
        
        public int compareTo(Argument t) {
            if(id < t.id) {
                return -1;
            } else if(id > t.id) {
                return 1;
            } else {
                return 0;
            }
        }
        
        public boolean hasAssignment() { return assignment >= 0; }
        public int getAssignment() {
            if(hasAssignment()) {
                return assignment;
            } else {
                System.err.println("Tried to obtain assignment on unassigned argument!");
                return -1;
            }
        }
        public void setAssignment(int assign) {
            if(hasAssignment()) {
                System.err.println("Tried to set already assigned argument");
            } else {
                assignment = assign;
            }
        }
        public void unsetAssignment() {
            if(hasAssignment()) {
                assignment = -1;
            } else {
                System.err.println("Attempting to unset already set assignment");
            }
        }
        
        public boolean canGetAssignment() {
            return true;
        }
        
        public String toString() {
            return id + (assignment >= 0 ? ("=" + assignment) : "");
        }
        
        public int getId() {
            return id;
        }
        
        Argument createCopy() {
            Argument arg = new Argument(id);
            arg.setAssignment(assignment);
            return arg;
        }
    }
    
    public class FunctionArgument extends Argument {
        
        public FunctionArgument(int id) {
            super(id);
        }
        
        public boolean hasAssignment() { return true; }
        
        public int getAssignment() {
            return assignment;
        }
        
        public void setFunctionAssignment(int x) {
            assignment = x;
        }
        
        public int getFunctionAssignment() {
            return assignment;
        }
        
        public boolean hasFunctionAssignment() {
            return assignment >= 0;
        }
        
        Argument createCopy() {
            Argument arg = new FunctionArgument(id);
            arg.setAssignment(assignment);
            return arg;
        }
    }
};