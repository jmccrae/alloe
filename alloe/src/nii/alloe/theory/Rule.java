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
 * Normal Form. Variables are given by Rule.Argument objects, these can have
 * assignements. <br>
 * NB Constants are not yet supported but will be soon (hopefully) <br>
 * NB Skolemized arguments are not yet implemented either
 */
public class Rule implements Comparable<Rule> {
    /** The list of relationship names for each term */
    public Vector<String> relations;
    /** The arguments of each term. The array is always two-dimensional */
    public Vector<Argument[]> terms;
    /** The number of premises. Premises are always first */
    public int premiseCount;
    /** A map of all the arguments in the rule, each value maps to itself */
    public TreeMap<Argument,Argument> arguments;
    /** Scores which can be attached to the rule.
     * @see ConsistProblem */
    public Integer score, maxScore;
    RuleSymbol ruleSymbols;
    
    private Rule() { arguments = new TreeMap<Argument,Argument>(); }
    
    /** Create a rule with assignments */
    public Rule(List<Integer> neg, List<Integer> pos, Model model) {
        relations = new Vector<String>(neg.size() + pos.size());
        terms = new Vector<Argument[]>();
        premiseCount = neg.size();
        List<Integer> ts = new LinkedList<Integer>(neg);
        ts.addAll(pos);
        arguments = new TreeMap<Argument,Argument>();
        
        for(Integer id : ts) {
            relations.add(model.relationByID(id));
            Argument[] args = new Argument[2];
            args[0] = new Argument(model.iByID(id));
            args[0].setAssignment(model.iByID(id));
            args[1] = new Argument(model.jByID(id));
            args[1].setAssignment(model.jByID(id));
            terms.add(args);
        }
        ruleSymbols = model.logic.ruleSymbols;
        
        addArguments();
    }
    
    /** Copy constructor */
    public Rule(Rule rule) {
        arguments = new TreeMap<Argument,Argument>();
        premiseCount = rule.premiseCount;
        relations = (Vector<String>)rule.relations.clone();
        terms = new Vector<Argument[]>();
        ruleSymbols = rule.ruleSymbols;
        
        for(int i = 0; i < length(); i++) {
            terms.add(rule.cloneArgPair(rule.terms.get(i)));
            arguments.put(terms.get(i)[0], terms.get(i)[0]);
            arguments.put(terms.get(i)[0], terms.get(i)[0]);
        }
    }
    
    /**
     * Number of terms in the rule.
     * @return length (number of statements) of this rule
     */
    public int length() { return relations.size(); }
    
    /**
     * Loads a rule from a line of text. Each term is of the form r(n,m) where n
     * and m are integers and r is a defined relation. The rule then has the form
     * <code> term? [ ; term ] * -&gt; term? [ ; term ]*</code>. The relation name
     * should be a defined relation or 'e' (representing the equivalence relation) or
     * 'in_set' where 'set' is a defined set.
     * @return The new Rule object based on the line
     */
    static public Rule loadRule(String rule, RuleSymbol ruleSymbols) throws IllegalArgumentException {
        Rule r = new Rule();
        r.ruleSymbols = ruleSymbols;
        r.loadFromString(rule);
        return r;
    }
    
    // NB for r(2,3) group 1 captures (2,3), group 2 captures 2
    private static String argumentRegex = "\\d+(|\\(\\s*\\)|\\((\\s*\\d+\\s*,)*\\s*\\d\\s*\\))|\".*\"";
    
    private void loadFromString(String rule) throws IllegalArgumentException {
        String []pc = rule.split("->",-1);
        if(pc.length != 2) {
            throw new IllegalArgumentException(
                    "Unexpected number of splits in rule: " + rule);
        }
        String p = "",c = "";
        p = pc[0];
        c = pc[1];
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
        
        Pattern pat1 = Pattern.compile("\\s*(\\w+)\\s*\\(\\s*(" +
                argumentRegex +
                ")\\s*,\\s*(" +
                argumentRegex +")\\s*\\)\\s*");
        Pattern pat2 = Pattern.compile("\\s*(\\w+)\\s*\\(\\s*(" +
                argumentRegex +
                ")\\s*\\)\\s*");
        
        for(int i = 0; i < premiseCount + conclusionCount; i++) {
            Matcher m1 = pat1.matcher(ss[i]);
            Matcher m2 = pat2.matcher(ss[i]);
            if(ss[i].matches("^\\s*$")) {
                continue;
            } else if(m1.matches()) {
                relations.add(m1.group(1));
                Argument []term = new Argument[2];
                term[0] = makeArgument(m1.group(2));
                term[1] = makeArgument(m1.group(5));
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
                rval = rval + " -> ";
            }
            rval = rval + relations.get(i) + "(" + terms.get(i)[0].toString() + "," +
                    terms.get(i)[1].toString() + ")";
            if(i != premiseCount -1 && i != length() - 1)
                rval = rval + "; ";
        }
        return rval;
    }
    
    
    /**
     * Convert to string with marks for in/not in model (asterisk on those in model)
     */
    public String toString(Model m) {
        String rval = "";
        for(int i = 0; i < length(); i++) {
            if(i == premiseCount) {
                rval = rval + " -> ";
            }
            rval = rval + relations.get(i) + "(" + terms.get(i)[0].toString() + "," +
                    terms.get(i)[1].toString() + ")" + (m.isConnected(m.id(this,i)) ? "*" : "");
            if(i != premiseCount -1 && i != length() - 1)
                rval = rval + "; ";
        }
        return rval;
    }
    
    /**
     * Find the terms involving a particular argument.
     * @param arg An argument
     * @return a list of the argument indexes that arg is involved in
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
     * Checks if this rule is satisfied in a given context.
     * This function must be called after all non-function assignments
     * have been made (!)
     * @param m A mapping from Graph names to graphs
     * @return true if the rule is satisfied
     */
    public boolean isRuleSatisfied(Model m) {
        for(int i = 0; i < length(); i++) {
            Graph g = m.graphs.get(relations.get(i));
            int v1 = terms.get(i)[0].getAssignment();
            int v2 = terms.get(i)[1].getAssignment();
            
            if((i < premiseCount && (v1 == -1 || v2 == -1 || !g.isConnected(v1,v2))) ||
                    (i >= premiseCount && v1 >= 0 && v2 >= 0 && g.isConnected(v1,v2))) {
                return true;
            }
        }
        return false;
    }
    
    
    private Argument makeArgument(String s) {
        Argument rval;
        if(s.matches("\\d+")) {
            rval = new Argument(Integer.parseInt(s));
        } else if(s.matches("\\d+\\(.*\\)")) {
            Matcher m = Pattern.compile("(\\d+)\\((.*)\\)").matcher(s);
            
            m.matches();
            
            Argument[] functionArgs;
            if(m.group(2).equals("")) {
                functionArgs = new Argument[0];
            } else {
                String[] args = m.group(2).split(",");
                functionArgs = new Argument[args.length];
                for(int i = 0; i < args.length; i++) {
                    functionArgs[i] = makeArgument(args[i].replaceAll("\\s*",""));
                }
            }
            
            rval = new FunctionalArgument(Integer.parseInt(m.group(1)),functionArgs);
        } else if(s.matches("\".*\"")) {
            ruleSymbols.addConstant(s.substring(1,s.length()-1));
            rval = new ConstantArgument(ruleSymbols.getConstantIndex(s.substring(1,s.length()-1)));
        } else {
            throw new IllegalArgumentException("Couldn't read argument: " + s);
        }
        
        if(arguments.get(rval) == null) {
            arguments.put(rval,rval);
            return rval;
        } else {
            Argument arg = arguments.get(rval);
            return arg;
        }
    }
    
    
    
    /**
     * Resolve this rule with another rule. This does it maximally, ie at all potential
     * resolve points. The rule is simplified afterwards, note this is symmetric
     * and will return a new rule, even if the two rules are not resolvable. The method
     * works by unioning the two rules and removing anything that matches between the
     * new rule's premise and conclusion. After this simplification is applied.
     * @param rule The rule to be resolved with this
     * @param model A model (this is used to simplify the rule after resolution, this may be
     * null, see simplify(Rule,Model) for more details)
     * @return The resolved rule
     * @see #canResolveWith(Rule)
     * @see #simplify(Rule,Model)
     */
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
        
        
        for(int i = 0; i < newRule.premiseCount; i++) {
            for(int k = newRule.premiseCount; k < newRule.length(); k++) {
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
        
        newRule = simplify(newRule,model);
        
        return newRule;
    }
    
    private Argument[] cloneArgPair(Argument[] args) {
        Argument[] rval = new Argument[2];
        rval[0] = new Argument(args[0]);
        rval[1] = new Argument(args[1]);
        return rval;
    }
    
    /**
     * Assigns a value to a term (if possible).
     * @return true if the assignment was possible, false otherwise
     */
    public boolean tryAssign(int arg, int i, int j) {
        Argument arg1 = terms.get(arg)[0];
        if(arg1.hasAssignment()) {
            if(i != arg1.getAssignment()) {
                return false;
            } else {
                Argument arg2 = terms.get(arg)[1];
                if(arg2.hasAssignment()) {
                    if(j != arg2.getAssignment())
                        return false;
                    return true;
                }
                arg2.setAssignment(j);
                return true;
            }
        } else {
            Argument arg2 = terms.get(arg)[1];
            if(arg2.hasAssignment()) {
                if(j != arg2.getAssignment()) {
                    return false;
                } else {
                    arg1.setAssignment(i);
                    return true;
                }
            } else {
                if(arg1 == arg2 && i != j)
                    return false;
                else if(arg1 == arg2) {
                    arg1.setAssignment(i);
                    return true;
                }
                arg1.setAssignment(i);
                arg2.setAssignment(j);
                return true;
            }
        }
    }
    
    /**
     * Simplifies this rule. Performed by removing terms which due to assignment have been duplicated
     * and detects is the rule is trivial (in which case all terms are removed
     * NB do not remove or change assignments after applying this rule!!
     * @param r The rule to be simplified
     * @param model The model, this may be null, if it is not null the model is used to check if this
     * rule has any non-mutable terms, these are removed if they do not automatically make the rule
     * consistent, or null is returned if the non-mutable term makes the rule consistent
     * @return The simplified rule (note this will not be a new instance), or null if the
     * rule must be satisfied (ie it is tautologous or satisfied by some non-mutable).
     */
    public static Rule simplify(Rule r, Model model) {
        
        // Check for resolution
        for(int i = 0; i < r.premiseCount; i++) {
            for(int k = r.premiseCount; k < r.length(); k++) {
                if(r.relations.get(i).equals(r.relations.get(k)) &&
                        r.terms.get(i)[0].getAssignment() == r.terms.get(k)[0].getAssignment() &&
                        r.terms.get(i)[1].getAssignment() == r.terms.get(k)[1].getAssignment()) {
                    return null;
                }
            }
        }
        
        // Check for duplication
        for(int i = 0; i < r.premiseCount; i++) {
            if(model != null) {
                Graph g = model.graphs.get(r.relations.get(i));
                if(!g.mutable(r.terms.get(i)[0].getAssignment(),
                        r.terms.get(i)[1].getAssignment())) {
                    if(g.isConnected(r.terms.get(i)[0].getAssignment(),
                            r.terms.get(i)[1].getAssignment())) {
                        r.relations.remove(i);
                        r.terms.remove(i);
                        r.premiseCount--;
                        i--;
                        continue;
                    } else {
                        return null;
                    }
                }
            }
            for(int k = i+1; k < r.premiseCount; k++) {
                if(r.relations.get(i).equals(r.relations.get(k)) &&
                        r.terms.get(i)[0].getAssignment() == r.terms.get(k)[0].getAssignment() &&
                        r.terms.get(i)[1].getAssignment() == r.terms.get(k)[1].getAssignment()) {
                    r.relations.remove(k);
                    r.terms.remove(k);
                    r.premiseCount--;
                    k--;
                }
            }
        }
        for(int i = r.premiseCount; i < r.length(); i++) {
            if(model != null) {
                Graph g = model.graphs.get(r.relations.get(i));
                if(!g.mutable(r.terms.get(i)[0].getAssignment(),
                        r.terms.get(i)[1].getAssignment())) {
                    if(!g.isConnected(r.terms.get(i)[0].getAssignment(),
                            r.terms.get(i)[1].getAssignment())) {
                        r.relations.remove(i);
                        r.terms.remove(i);
                        i--;
                        continue;
                    } else {
                        return null;
                    }
                }
            }
            for(int k = i+1; k < r.terms.size(); k++) {
                if(r.relations.get(i).equals(r.relations.get(k)) &&
                        r.terms.get(i)[0].getAssignment() == r.terms.get(k)[0].getAssignment() &&
                        r.terms.get(i)[1].getAssignment() == r.terms.get(k)[1].getAssignment()) {
                    r.relations.remove(k);
                    r.terms.remove(k);
                    k--;
                }
            }
        }
        
        
        // Rebuild Arguments
        r.arguments = new TreeMap<Argument, Argument>();
        r.addArguments();
        return r;
    }
    
    private void addArguments() {
        for(int i = 0; i < length(); i++) {
            arguments.put(terms.get(i)[0], terms.get(i)[0]);
            arguments.put(terms.get(i)[1], terms.get(i)[1]);
        }
    }
    
    /**
     * Check for resolution. Note this is not a symmetric it checks if there is a resolution between this's premise and
     * r's conclusion
     *
     */
    protected boolean canResolveWith(Rule r) {
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
    
    /** Checks if two rules can resolve in some way. This is symmetric.
     * @see #resolve(Rule, Model)
     * @see #canResolveWith(Rule)
     */
    public static boolean canResolve(Rule rule1, Rule rule2) {
        return rule1.canResolveWith(rule2) || rule2.canResolveWith(rule1);
    }
    
    /**
     * Check if r subsumes this rule.
     * A rule subsumes another this rule if in any case that rule is true, this rule is also true. This means that every
     * term in this rule can be found in the other rule
     * @return true iff this rule is subsumed by r
     */
    public boolean subsumes(Rule r) {
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
    
    /**
     * Compare two rules.
     * Mostly so that rules can be put into a unique associative container, such as TreeSet
     * The ordering principle is as follows:
     * <ol>
     * <li> number of premises </li>
     * <li> number of conclusions </li>
     * <li> relationship of 1st term (by String.compareTo(Object))</li>
     * <li> 1st argument of 1st term </li>
     * <li> 2nd argument of 1st term </li>
     * <li> relationship of 2nd term (etc...) </li>
     * </ol>
     * @see Rule.Argument.compareTo(Argument)
     */
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
            if(terms.get(i)[1].compareTo(r.terms.get(i)[1]) != 0) {
                return terms.get(i)[1].compareTo(r.terms.get(i)[1]);
            }
        }
        
        return 0;
    }
    
    /**
     * Check if two rules are equal.
     * Derived from compareTo
     * @see #compareTo(Rule)
     */
    public boolean equals(Object obj) {
        if(obj instanceof Rule)
            return compareTo((Rule)obj) == 0;
        else
            return false;
    }
    
    
    /**
     * Returns true if the conclusion must be satisfied.
     * That is, the conclusion contains a satisfied immutable, note this function is really only
     * for ConsistProblem, as it avoids cloning the object, or risking changing the rule object
     * by using simplify(Rule,Model)
     * @see #simplify(Rule,Model)
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
     * Checks if this rule has a term in its conclusion corresponding to a value
     * in testTerms by the model.
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
     
    public void removeAllTerms(List<Integer> terms) {
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
    }*/
    
    
    /**
     * Same as forAllAssignment(model,true,action)
     * @see #forAllAssignments(Model,boolean,AssignmentAction)
     */
    public boolean forAllAssignments(Model model, AssignmentAction action) {
        return forAllAssignments(model,true,action);
    }
    
    /**
     * Find all assignments to this rule and perform action
     * @param model The model from which assignements can be drawn
     * @param validate Use only assignments where the ground instance is inconsistent
     * @param action The action to be performed
     * @see AssignmentAction
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
     * Limit this rule to the model. so the rule only contains terms with true assignments in the model.
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
     * Check the rule is not in some inconsistent state.
     * Used for testing and debugging.
     */
    public boolean isOK() {
        if(relations.size() != terms.size() || premiseCount < 0 || premiseCount > length())
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
     * A variable in the rule. This object may be given an
     * assignment.
     * this class is used for standard variables
     * it should not be instantiated directly but instead
     * constructed through Rule.loadRule()
     */
    public class Argument implements Comparable<Argument> {
        int id;
        // -1 means no assignment
        protected int assignment;
        
        Argument(int id) { this.id = id; assignment = -1; }
        /** Does nothing */
        protected Argument() {}
        /** Copy constructor */
        public Argument(Argument arg) {
            this.id = arg.id;
            this.assignment = arg.assignment;
        }
        
        /** Compares two arguments. if they both have assignments the value assigned
         * to them is used, otherwise the id of the variable is used */
        public int compareTo(Argument t) {
            if(hasAssignment() && t.hasAssignment()) {
                if(getAssignment() < t.getAssignment()) {
                    return -1;
                } else if(getAssignment() > t.getAssignment()) {
                    return +1;
                } else {
                    return 0;
                }
            }
            if(id < t.id) {
                return -1;
            } else if(id > t.id) {
                return 1;
            } else {
                return 0;
            }
        }
        
        /** Does this argument have an assigned value */
        public boolean hasAssignment() { return assignment >= 0; }
        
        /** Get the assignment
         * @throws IllegalStateException if the argument has no assignment */
        public int getAssignment() {
            if(hasAssignment()) {
                return assignment;
            } else {
                throw new IllegalStateException("Tried to obtain assignment on unassigned argument!");
            }
        }
        
        /** Set the assignment
         * @throws IllegalStateException if the argument already has an assignment */
        public void setAssignment(int assign) {
            if(hasAssignment()) {
                throw new IllegalStateException("Tried to set already assigned argument");
            } else {
                arguments.remove(this); // Momentary Blink
                assignment = assign;
                arguments.put(this, this);
            }
        }
        
        /** Clear the current assignment
         * @throws IllegalStateException  if the argument does not have an assignment */
        public void unsetAssignment() {
            if(hasAssignment()) {
                arguments.remove(this);
                assignment = -1;
                arguments.put(this,this);
            } else {
                throw new IllegalStateException("Attempting to unset already set assignment");
            }
        }
        
        public String toString() {
            return id + (assignment >= 0 ? ("=" + assignment) : "");
        }
        
        public int getId() {
            return id;
        }
    }
    
    /** Represents a constant value */
    public class ConstantArgument extends Argument {
        ConstantArgument(int val) {
            id = -1;
            assignment = val;
        }
        
        public void unsetAssignmnet() {
            throw new UnsupportedOperationException();
        }
        
        public String toString() {
            return "\"" + ruleSymbols.getConstant(assignment) + "\"";
        }
    }
    
    /** Represents a skolemized function */
    public class FunctionalArgument extends Argument {
        public Argument[] functionArgs;
        FunctionalArgument(int id, Argument[] functionArgs) {
            this.id = id;
            this.functionArgs = functionArgs;
            assignment = -1;
        }
        
        public boolean hasAssigment() {
            for(Argument a : functionArgs) {
                if(!a.hasAssignment())
                    return false;
            }
            return true;
        }
        
        public int getAssignment() {
            int r = ruleSymbols.getRange().get(id);
            for(int i = 0; i < functionArgs.length; i++) {
                if(!functionArgs[i].hasAssignment())
                    throw new IllegalStateException();
                r += functionArgs[i].getAssignment() *
                        (int)Math.pow(ruleSymbols.getModelSize(),functionArgs.length - i - 1);
            }
            return r;
        }
        
        public boolean setAssignment() {
            throw new UnsupportedOperationException();
        }
        
        public void unsetAssignment() {
            throw new UnsupportedOperationException();
        }
        
        public String toString() {
            String assigns = "";
            for(int i = 0; i < functionArgs.length; i++) {
                assigns = assigns + functionArgs.toString();
                if(i != functionArgs.length - 1)
                    assigns = assigns + ",";
            }
            return id + "(" + assigns + ")";
        }
    }
}
